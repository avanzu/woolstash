package de.avanzu.woolstash

import android.os.Bundle
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import de.avanzu.woolstash.data.backup.IncompatibleBackupException
import de.avanzu.woolstash.data.backup.WoolStashBackupService
import de.avanzu.woolstash.data.backup.WoolStashStoragePreferences
import de.avanzu.woolstash.data.backup.WoolStashStorageSlot
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
    private var activeServices: WoolStashServices? = null

    private val storagePreferences: WoolStashStoragePreferences by lazy {
        WoolStashStoragePreferences(applicationContext)
    }

    private val backupService: WoolStashBackupService by lazy {
        WoolStashBackupService(
            contentResolver = contentResolver,
            databasesDirectory = applicationContext.getDatabasePath(WoolStashDatabase.DATABASE_NAME).parentFile
                ?: applicationContext.filesDir,
            filesDirectory = applicationContext.filesDir,
            cacheDirectory = applicationContext.cacheDir,
            appVersionCode = appVersionCode(),
            appVersionName = appVersionName(),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WoolStashTheme {
                var activeSlot by remember {
                    mutableStateOf(
                        if (backupService.hasStaging()) {
                            storagePreferences.activeSlot()
                        } else {
                            WoolStashStorageSlot.Main
                        },
                    )
                }
                var servicesGeneration by remember {
                    mutableIntStateOf(0)
                }
                val services = remember(activeSlot, servicesGeneration) {
                    openServices(activeSlot)
                }
                val viewModel: InventoryListViewModel = viewModel(
                    key = "inventory-${activeSlot.name}-$servicesGeneration",
                    factory = InventoryListViewModelFactory(services.inventoryRepository),
                )
                val items by viewModel.items.collectAsStateWithLifecycle()
                val referenceSuggestions by viewModel.referenceSuggestions.collectAsStateWithLifecycle()
                var isBackupBusy by remember {
                    mutableStateOf(false)
                }
                var messageDialog by remember {
                    mutableStateOf<String?>(null)
                }
                var incompatibleBackupDialogVisible by remember {
                    mutableStateOf(false)
                }
                var pendingRestoreUri by remember {
                    mutableStateOf<android.net.Uri?>(null)
                }
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
                val exportLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.CreateDocument("application/zip"),
                ) { uri ->
                    uri ?: return@rememberLauncherForActivityResult
                    coroutineScope.launch {
                        isBackupBusy = true
                        try {
                            backupService.exportMainBackup(
                                database = services.database,
                                targetUri = uri,
                            )
                            messageDialog = getString(R.string.backup_export_success)
                        } catch (_: Exception) {
                            messageDialog = getString(R.string.backup_export_error)
                        } finally {
                            isBackupBusy = false
                        }
                    }
                }
                val importLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.OpenDocument(),
                ) { uri ->
                    uri ?: return@rememberLauncherForActivityResult
                    pendingRestoreUri = uri
                }

                LaunchedEffect(activeSlot) {
                    storagePreferences.setActiveSlot(activeSlot)
                }

                LaunchedEffect(activeSlot, items.map { item -> item.id }) {
                    photoPreviews = buildMap {
                        items.forEach { item ->
                            services.inventoryMediaStore.listPhotos(item.id)
                                .firstOrNull()
                                ?.let { photo -> put(item.id, photo) }
                        }
                    }
                }

                pendingRestoreUri?.let { uri ->
                    AlertDialog(
                        onDismissRequest = {
                            pendingRestoreUri = null
                        },
                        title = {
                            Text(stringResource(R.string.backup_restore_confirm_title))
                        },
                        text = {
                            Text(stringResource(R.string.backup_restore_confirm_message))
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    pendingRestoreUri = null
                                    coroutineScope.launch {
                                        isBackupBusy = true
                                        try {
                                            backupService.restoreToStaging(uri)
                                            closeActiveServices()
                                            activeSlot = WoolStashStorageSlot.Staging
                                            servicesGeneration += 1
                                            selectedItemId = null
                                            createItemType = null
                                            photoPreviews = emptyMap()
                                            messageDialog = getString(R.string.backup_restore_success)
                                        } catch (_: IncompatibleBackupException) {
                                            incompatibleBackupDialogVisible = true
                                        } catch (_: Exception) {
                                            messageDialog = getString(R.string.backup_restore_error)
                                        } finally {
                                            isBackupBusy = false
                                        }
                                    }
                                },
                            ) {
                                Text(stringResource(R.string.backup_restore_confirm_action))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    pendingRestoreUri = null
                                },
                            ) {
                                Text(stringResource(R.string.action_cancel))
                            }
                        },
                    )
                }

                if (incompatibleBackupDialogVisible) {
                    AlertDialog(
                        onDismissRequest = {
                            incompatibleBackupDialogVisible = false
                        },
                        title = {
                            Text(stringResource(R.string.backup_restore_incompatible_title))
                        },
                        text = {
                            Text(stringResource(R.string.backup_restore_incompatible_message))
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    incompatibleBackupDialogVisible = false
                                },
                            ) {
                                Text(stringResource(android.R.string.ok))
                            }
                        },
                    )
                }

                messageDialog?.let { message ->
                    AlertDialog(
                        onDismissRequest = {
                            messageDialog = null
                        },
                        text = {
                            Text(message)
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    messageDialog = null
                                },
                            ) {
                                Text(stringResource(android.R.string.ok))
                            }
                        },
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
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
                        var photos by remember(selectedItem.id, activeSlot) {
                            mutableStateOf<List<InventoryPhotoFile>>(emptyList())
                        }
                        var isImportingPhoto by remember(selectedItem.id, activeSlot) {
                            mutableStateOf(false)
                        }
                        var photoImportError by remember(selectedItem.id, activeSlot) {
                            mutableStateOf<String?>(null)
                        }

                        LaunchedEffect(selectedItem.id, activeSlot) {
                            photos = services.inventoryMediaStore.listPhotos(selectedItem.id)
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
                                        services.inventoryPhotoImporter.importPhoto(
                                            inventoryItemId = selectedItem.id,
                                            sourceUri = uri,
                                        )
                                        photos = services.inventoryMediaStore.listPhotos(selectedItem.id)
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
                                    services.inventoryMediaStore.setHeroPhoto(
                                        inventoryItemId = selectedItem.id,
                                        photoId = photo.photoId,
                                    )
                                    photos = services.inventoryMediaStore.listPhotos(selectedItem.id)
                                    photoPreviews = photoPreviews.withPreview(
                                        itemId = selectedItem.id,
                                        photo = photos.firstOrNull(),
                                    )
                                }
                            },
                            onDeletePhoto = { photo ->
                                coroutineScope.launch {
                                    services.inventoryMediaStore.deletePhoto(photo)
                                    photos = services.inventoryMediaStore.listPhotos(selectedItem.id)
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
                            isStagingActive = activeSlot == WoolStashStorageSlot.Staging,
                            isBackupBusy = isBackupBusy,
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
                            onCreateBackupClick = {
                                exportLauncher.launch(getString(R.string.backup_default_file_name))
                            },
                            onRestoreBackupClick = {
                                importLauncher.launch(arrayOf("application/zip", "application/octet-stream", "*/*"))
                            },
                        )
                    }

                    if (activeSlot == WoolStashStorageSlot.Staging) {
                        StagingBanner(
                            isBusy = isBackupBusy,
                            onAcceptClick = {
                                coroutineScope.launch {
                                    isBackupBusy = true
                                    try {
                                        closeActiveServices()
                                        backupService.promoteStagingToMain()
                                        activeSlot = WoolStashStorageSlot.Main
                                        storagePreferences.setActiveSlot(WoolStashStorageSlot.Main)
                                        servicesGeneration += 1
                                        selectedItemId = null
                                        createItemType = null
                                        photoPreviews = emptyMap()
                                        messageDialog = getString(R.string.backup_staging_accepted)
                                    } catch (_: Exception) {
                                        messageDialog = getString(R.string.backup_staging_action_error)
                                    } finally {
                                        isBackupBusy = false
                                    }
                                }
                            },
                            onDiscardClick = {
                                coroutineScope.launch {
                                    isBackupBusy = true
                                    try {
                                        closeActiveServices()
                                        backupService.discardStaging()
                                        activeSlot = WoolStashStorageSlot.Main
                                        storagePreferences.setActiveSlot(WoolStashStorageSlot.Main)
                                        servicesGeneration += 1
                                        selectedItemId = null
                                        createItemType = null
                                        photoPreviews = emptyMap()
                                        messageDialog = getString(R.string.backup_staging_discarded)
                                    } catch (_: Exception) {
                                        messageDialog = getString(R.string.backup_staging_action_error)
                                    } finally {
                                        isBackupBusy = false
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        closeActiveServices()
        super.onDestroy()
    }

    private fun openServices(slot: WoolStashStorageSlot): WoolStashServices {
        activeServices?.takeIf { services -> services.slot == slot }?.let { services ->
            return services
        }
        closeActiveServices()

        val database = Room.databaseBuilder(
            applicationContext,
            WoolStashDatabase::class.java,
            slot.databaseName,
        )
            .addMigrations(
                WoolStashDatabase.Migration1To2,
                WoolStashDatabase.Migration2To3,
                WoolStashDatabase.Migration3To4,
            )
            .build()
        val mediaFilesDir = when (slot) {
            WoolStashStorageSlot.Main -> applicationContext.filesDir
            WoolStashStorageSlot.Staging -> applicationContext.filesDir.resolve("restore/staging/files")
        }
        val mediaStore = InventoryMediaStore(
            filesDir = mediaFilesDir,
            inventoryItemPhotoDao = database.inventoryItemPhotoDao(),
        )
        val services = WoolStashServices(
            slot = slot,
            database = database,
            inventoryRepository = InventoryRepository(database = database),
            inventoryMediaStore = mediaStore,
            inventoryPhotoImporter = InventoryPhotoImporter(
                contentResolver = contentResolver,
                mediaStore = mediaStore,
            ),
        )
        activeServices = services
        return services
    }

    private fun closeActiveServices() {
        activeServices?.database?.close()
        activeServices = null
    }

    @Suppress("DEPRECATION")
    private fun appVersionCode(): Long {
        val info = packageManager.getPackageInfo(packageName, 0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            info.versionCode.toLong()
        }
    }

    private fun appVersionName(): String {
        return packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()
    }
}

@Composable
private fun StagingBanner(
    isBusy: Boolean,
    onAcceptClick: () -> Unit,
    onDiscardClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(12.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        tonalElevation = 6.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.backup_staging_banner),
                color = MaterialTheme.colorScheme.inverseOnSurface,
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(
                enabled = !isBusy,
                onClick = onDiscardClick,
            ) {
                Text(stringResource(R.string.backup_staging_discard))
            }
            Button(
                enabled = !isBusy,
                onClick = onAcceptClick,
            ) {
                Text(stringResource(R.string.backup_staging_accept))
            }
        }
    }
}

private data class WoolStashServices(
    val slot: WoolStashStorageSlot,
    val database: WoolStashDatabase,
    val inventoryRepository: InventoryRepository,
    val inventoryMediaStore: InventoryMediaStore,
    val inventoryPhotoImporter: InventoryPhotoImporter,
)

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
