package de.avanzu.woolstash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import de.avanzu.woolstash.data.local.WoolStashDatabase
import de.avanzu.woolstash.data.repository.InventoryRepository
import de.avanzu.woolstash.domain.model.InventoryItemId
import de.avanzu.woolstash.ui.inventory.CreateInventoryItemScreen
import de.avanzu.woolstash.ui.inventory.CreateInventoryItemType
import de.avanzu.woolstash.ui.inventory.InventoryItemDetailScreen
import de.avanzu.woolstash.ui.inventory.InventoryListScreen
import de.avanzu.woolstash.ui.inventory.InventoryListViewModel
import de.avanzu.woolstash.ui.inventory.InventoryListViewModelFactory
import de.avanzu.woolstash.ui.theme.WoolStashTheme

class MainActivity : ComponentActivity() {
    private val database: WoolStashDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            WoolStashDatabase::class.java,
            "wool_stash.db",
        ).build()
    }

    private val inventoryRepository: InventoryRepository by lazy {
        InventoryRepository(
            inventoryItemDao = database.inventoryItemDao(),
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
                var selectedItemId by remember {
                    mutableStateOf<InventoryItemId?>(null)
                }
                var createItemType by remember {
                    mutableStateOf<CreateInventoryItemType?>(null)
                }
                val selectedItem = items.firstOrNull { item -> item.id == selectedItemId }

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
                    )
                } else if (selectedItem != null) {
                    InventoryItemDetailScreen(
                        item = selectedItem,
                        onBackClick = {
                            selectedItemId = null
                        },
                        onUpdateCoreFields = viewModel::updateCoreFields,
                        onUpdateProductDetails = viewModel::updateProductDetails,
                    )
                } else {
                    InventoryListScreen(
                        items = items,
                        onDeleteItemsConfirmed = viewModel::deleteItems,
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
