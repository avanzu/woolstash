package de.avanzu.woolstash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import de.avanzu.woolstash.data.local.WoolStashDatabase
import de.avanzu.woolstash.data.media.InventoryMediaStore
import de.avanzu.woolstash.data.media.InventoryPhotoFile
import de.avanzu.woolstash.data.media.InventoryPhotoImporter
import de.avanzu.woolstash.data.repository.InventoryRepository
import de.avanzu.woolstash.domain.model.InventoryItemId
import de.avanzu.woolstash.domain.model.normalizedDistinct
import de.avanzu.woolstash.ui.inventory.CreateInventoryItemScreen
import de.avanzu.woolstash.ui.inventory.CreateInventoryItemType
import de.avanzu.woolstash.ui.inventory.InventoryItemDetailScreen
import de.avanzu.woolstash.ui.inventory.InventoryListScreen
import de.avanzu.woolstash.ui.inventory.InventoryListViewModel
import de.avanzu.woolstash.ui.inventory.InventoryListViewModelFactory
import de.avanzu.woolstash.ui.theme.WoolStashTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val database: WoolStashDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            WoolStashDatabase::class.java,
            "wool_stash.db",
        )
            .addMigrations(
                WoolStashDatabase.Migration1To2,
                WoolStashDatabase.Migration2To3,
                WoolStashDatabase.Migration3To4,
            )
            .build()
    }

    private val inventoryRepository: InventoryRepository by lazy {
        InventoryRepository(
            database = database,
        )
    }

    private val inventoryMediaStore: InventoryMediaStore by lazy {
        InventoryMediaStore(
            filesDir = applicationContext.filesDir,
            inventoryItemPhotoDao = database.inventoryItemPhotoDao(),
        )
    }

    private val inventoryPhotoImporter: InventoryPhotoImporter by lazy {
        InventoryPhotoImporter(
            contentResolver = contentResolver,
            mediaStore = inventoryMediaStore,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WoolStashTheme {
                val viewModel: InventoryListViewModel = viewModel(
                    factory = InventoryListViewModelFactory(inventoryRepository),
                )
                val items by viewModel.items.collectAsStateWithLifecycle()
                val referenceSuggestions by viewModel.referenceSuggestions.collectAsStateWithLifecycle()
                var selectedItemId by remember {
                    mutableStateOf<InventoryItemId?>(null)
                }
                var createItemType by remember {
                    mutableStateOf<CreateInventoryItemType?>(null)
                }
                val selectedItem = items.firstOrNull { item -> item.id == selectedItemId }
                val tagSuggestions = remember(items) {
                    items
                        .flatMap { item -> item.tags }
                        .normalizedDistinct()
                        .map { tag -> tag.name }
                        .sorted()
                }
                val coroutineScope = rememberCoroutineScope()
                var photoPreviews by remember {
                    mutableStateOf<Map<InventoryItemId, InventoryPhotoFile>>(emptyMap())
                }

                LaunchedEffect(items.map { item -> item.id }) {
                    photoPreviews = buildMap {
                        items.forEach { item ->
                            inventoryMediaStore.listPhotos(item.id)
                                .firstOrNull()
                                ?.let { photo -> put(item.id, photo) }
                        }
                    }
                }

                if (createItemType != null) {
                    CreateInventoryItemScreen(
                        type = createItemType,
                        onBackClick = {
                            createItemType = null
                        },
                        onCreateYarn = { input ->
                            viewModel.createYarn(input) { createdItemId ->
                                createItemType = null
                                selectedItemId = createdItemId
                            }
                        },
                        onCreateFiber = { input ->
                            viewModel.createFiber(input) { createdItemId ->
                                createItemType = null
                                selectedItemId = createdItemId
                            }
                        },
                        tagSuggestions = tagSuggestions,
                        referenceSuggestions = referenceSuggestions,
                    )
                } else if (selectedItem != null) {
                    var photos by remember(selectedItem.id) {
                        mutableStateOf<List<InventoryPhotoFile>>(emptyList())
                    }
                    var isImportingPhoto by remember(selectedItem.id) {
                        mutableStateOf(false)
                    }
                    var photoImportError by remember(selectedItem.id) {
                        mutableStateOf<String?>(null)
                    }

                    LaunchedEffect(selectedItem.id) {
                        photos = inventoryMediaStore.listPhotos(selectedItem.id)
                    }

                    InventoryItemDetailScreen(
                        item = selectedItem,
                        photos = photos,
                        isImportingPhoto = isImportingPhoto,
                        photoImportError = photoImportError,
                        onBackClick = {
                            selectedItemId = null
                        },
                        onUpdateCoreFields = viewModel::updateCoreFields,
                        onUpdateProductDetails = viewModel::updateProductDetails,
                        tagSuggestions = tagSuggestions,
                        referenceSuggestions = referenceSuggestions,
                        onPhotoSelected = { uri ->
                            coroutineScope.launch {
                                isImportingPhoto = true
                                photoImportError = null
                                try {
                                    inventoryPhotoImporter.importPhoto(
                                        inventoryItemId = selectedItem.id,
                                        sourceUri = uri,
                                    )
                                    photos = inventoryMediaStore.listPhotos(selectedItem.id)
                                    photoPreviews = photoPreviews.withPreview(
                                        itemId = selectedItem.id,
                                        photo = photos.firstOrNull(),
                                    )
                                } catch (_: Exception) {
                                    photoImportError = getString(R.string.detail_photo_import_error)
                                } finally {
                                    isImportingPhoto = false
                                }
                            }
                        },
                        onSetHeroPhoto = { photo ->
                            coroutineScope.launch {
                                inventoryMediaStore.setHeroPhoto(
                                    inventoryItemId = selectedItem.id,
                                    photoId = photo.photoId,
                                )
                                photos = inventoryMediaStore.listPhotos(selectedItem.id)
                                photoPreviews = photoPreviews.withPreview(
                                    itemId = selectedItem.id,
                                    photo = photos.firstOrNull(),
                                )
                            }
                        },
                        onDeletePhoto = { photo ->
                            coroutineScope.launch {
                                inventoryMediaStore.deletePhoto(photo)
                                photos = inventoryMediaStore.listPhotos(selectedItem.id)
                                photoPreviews = photoPreviews.withPreview(
                                    itemId = selectedItem.id,
                                    photo = photos.firstOrNull(),
                                )
                            }
                        },
                    )
                } else {
                    InventoryListScreen(
                        items = items,
                        photoPreviews = photoPreviews,
                        onDeleteItemsConfirmed = { ids ->
                            viewModel.deleteItems(ids)
                            photoPreviews = photoPreviews - ids
                        },
                        onItemClick = { item ->
                            selectedItemId = item.id
                        },
                        onAddYarnClick = {
                            createItemType = CreateInventoryItemType.Yarn
                        },
                        onAddFiberClick = {
                            createItemType = CreateInventoryItemType.Fiber
                        },
                    )
                }
            }
        }
    }
}

private fun Map<InventoryItemId, InventoryPhotoFile>.withPreview(
    itemId: InventoryItemId,
    photo: InventoryPhotoFile?,
): Map<InventoryItemId, InventoryPhotoFile> {
    return if (photo == null) {
        this - itemId
    } else {
        this + (itemId to photo)
    }
}
