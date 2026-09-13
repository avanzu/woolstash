package de.avanzu.woolstash.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.avanzu.woolstash.R
import de.avanzu.woolstash.domain.model.InventoryItem
import de.avanzu.woolstash.domain.model.ProductType

@Composable
internal fun InventorySourceSelector(
    items: List<InventoryItem>,
    amountTexts: Map<String, String>,
    onAmountTextsChange: (Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val selectableItems = items
        .filter { item ->
            item.productType == ProductType.Fiber &&
                item.weight?.grams?.let { grams -> grams > 0 } == true
        }
        .sortedBy { item -> item.name.lowercase() }
    val selectedItems = items
        .filter { item ->
            item.productType == ProductType.Fiber && item.id.value in amountTexts
        }
        .sortedBy { item -> item.name.lowercase() }
    val filteredItems = selectableItems
        .filterNot { item -> item.id.value in amountTexts }
        .filterBySearchQuery(searchQuery)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.create_from_stock_section),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.create_from_stock_help),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (selectableItems.size < 2) {
            Text(
                text = stringResource(R.string.create_from_stock_not_enough_sources),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = searchQuery,
            onValueChange = { query -> searchQuery = query },
            label = {
                Text(stringResource(R.string.create_from_stock_search))
            },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = if (searchQuery.isNotBlank()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.action_clear_search),
                        )
                    }
                }
            } else {
                null
            },
            singleLine = true,
        )

        if (selectedItems.isNotEmpty()) {
            Text(
                text = stringResource(R.string.create_from_stock_selected, selectedItems.size),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            selectedItems.forEach { item ->
                InventorySourceChoice(
                    item = item,
                    isSelected = true,
                    amountText = amountTexts[item.id.value].orEmpty(),
                    onSelectedChange = { selected ->
                        onAmountTextsChange(
                            if (selected) amountTexts else amountTexts - item.id.value,
                        )
                    },
                    onAmountChange = { value ->
                        onAmountTextsChange(amountTexts + (item.id.value to value))
                    },
                )
            }
        }

        Text(
            text = stringResource(R.string.create_from_stock_results, filteredItems.size),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        if (filteredItems.isEmpty()) {
            Text(
                text = stringResource(R.string.create_from_stock_no_results),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            filteredItems.forEach { item ->
                InventorySourceChoice(
                    item = item,
                    isSelected = false,
                    amountText = "",
                    onSelectedChange = { selected ->
                        if (selected) {
                            onAmountTextsChange(amountTexts + (item.id.value to ""))
                        }
                    },
                    onAmountChange = {},
                )
            }
        }

        if (amountTexts.size < 2) {
            Text(
                text = stringResource(R.string.create_from_stock_minimum),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InventorySourceChoice(
    item: InventoryItem,
    isSelected: Boolean,
    amountText: String,
    onSelectedChange: (Boolean) -> Unit,
    onAmountChange: (String) -> Unit,
) {
    val parsedAmount = amountText
        .trim()
        .replace(',', '.')
        .toDoubleOrNull()
    val isAmountValid = parsedAmount?.let { grams -> grams.isFinite() && grams > 0 } == true

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = onSelectedChange,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = stringResource(
                            R.string.create_from_stock_available,
                            item.weight?.let { weight -> formatGrams(weight.grams) }
                                ?: stringResource(R.string.detail_field_empty),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (isSelected) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = amountText,
                    onValueChange = onAmountChange,
                    label = {
                        Text(stringResource(R.string.create_from_stock_consumed_grams))
                    },
                    suffix = {
                        Text(stringResource(R.string.unit_grams))
                    },
                    isError = !isAmountValid,
                    supportingText = if (isAmountValid) {
                        null
                    } else {
                        {
                            Text(stringResource(R.string.create_weight_error))
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }
        }
    }
}
