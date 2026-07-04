package de.avanzu.woolstash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import de.avanzu.woolstash.domain.model.SampleInventoryItems
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.avanzu.woolstash.ui.inventory.InventoryListScreen
import de.avanzu.woolstash.ui.theme.WoolStashTheme
import de.avanzu.woolstash.data.local.WoolStashDatabase
import de.avanzu.woolstash.data.repository.InventoryRepository
import de.avanzu.woolstash.ui.inventory.InventoryListViewModel
import de.avanzu.woolstash.ui.inventory.InventoryListViewModelFactory
import androidx.compose.runtime.getValue

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
                val items by viewModel.items.collectAsState()
                InventoryListScreen(items = items)
            }
        }
    }
}
