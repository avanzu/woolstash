package de.avanzu.woolstash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.avanzu.woolstash.domain.model.SampleInventoryItems
import de.avanzu.woolstash.ui.inventory.InventoryListScreen
import de.avanzu.woolstash.ui.theme.WoolStashTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WoolStashTheme {

                InventoryListScreen(items = SampleInventoryItems.items)
            }
        }
    }
}
