package de.avanzu.woolstash.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.avanzu.woolstash.R
import de.avanzu.woolstash.domain.model.FiberForm
import de.avanzu.woolstash.domain.model.FiberPreparation
import de.avanzu.woolstash.domain.model.LengthBasis
import de.avanzu.woolstash.domain.model.ProductType
import de.avanzu.woolstash.domain.model.TwistDirection
import de.avanzu.woolstash.domain.model.YarnWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InventorySearchFilterSheet(
    criteria: InventorySearchCriteria,
    availableTags: List<String>,
    resultCount: (InventorySearchCriteria) -> Int,
    onApply: (InventorySearchCriteria) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember(criteria) {
        mutableStateOf(criteria)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                text = stringResource(R.string.search_filters_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    SearchFilterSection(title = stringResource(R.string.drawer_section_product_type)) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ProductTypeFilterChoice(
                                label = stringResource(R.string.inventory_type_filter_all),
                                selected = draft.productType == null,
                                onClick = { draft = draft.withProductType(null) },
                            )
                            ProductTypeFilterChoice(
                                label = stringResource(R.string.product_type_yarn),
                                selected = draft.productType == ProductType.Yarn,
                                onClick = { draft = draft.withProductType(ProductType.Yarn) },
                            )
                            ProductTypeFilterChoice(
                                label = stringResource(R.string.product_type_fiber),
                                selected = draft.productType == ProductType.Fiber,
                                onClick = { draft = draft.withProductType(ProductType.Fiber) },
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = stringResource(R.string.inventory_show_depleted),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Switch(
                            checked = draft.includeDepletedItems,
                            onCheckedChange = { include ->
                                draft = draft.copy(includeDepletedItems = include)
                            },
                        )
                    }
                }

                item {
                    SearchFilterSection(title = stringResource(R.string.drawer_section_tags)) {
                        if (availableTags.isEmpty()) {
                            Text(
                                text = stringResource(R.string.inventory_tag_filter_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                availableTags.forEach { tag ->
                                    FilterChip(
                                        selected = tag in draft.tags,
                                        onClick = {
                                            draft = draft.copy(tags = draft.tags.toggle(tag))
                                        },
                                        label = { Text(tag) },
                                    )
                                }
                            }
                        }
                    }
                }

                if (draft.productType == ProductType.Yarn) {
                    item {
                        HorizontalDivider()
                    }
                    item {
                        Text(
                            text = stringResource(R.string.search_filters_yarn_section),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    item {
                        OptionalValueFilterSection(
                            title = stringResource(R.string.detail_field_yarn_weight),
                            values = YarnWeight.entries,
                            filter = draft.yarnWeights,
                            label = { value -> value.label() },
                            onFilterChange = { filter -> draft = draft.copy(yarnWeights = filter) },
                        )
                    }
                    item {
                        OptionalValueFilterSection(
                            title = stringResource(R.string.detail_field_length_basis),
                            values = LengthBasis.entries,
                            filter = draft.lengthBases,
                            label = { value -> value.label() },
                            onFilterChange = { filter -> draft = draft.copy(lengthBases = filter) },
                        )
                    }
                    item {
                        OptionalValueFilterSection(
                            title = stringResource(R.string.detail_field_twist_direction),
                            values = TwistDirection.entries,
                            filter = draft.twistDirections,
                            label = { value -> value.label() },
                            onFilterChange = { filter -> draft = draft.copy(twistDirections = filter) },
                        )
                    }
                }

                if (draft.productType == ProductType.Fiber) {
                    item {
                        HorizontalDivider()
                    }
                    item {
                        Text(
                            text = stringResource(R.string.search_filters_fiber_section),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    item {
                        OptionalValueFilterSection(
                            title = stringResource(R.string.detail_field_fiber_form),
                            values = FiberForm.entries,
                            filter = draft.fiberForms,
                            label = { value -> value.label() },
                            onFilterChange = { filter -> draft = draft.copy(fiberForms = filter) },
                        )
                    }
                    item {
                        OptionalValueFilterSection(
                            title = stringResource(R.string.detail_field_preparation),
                            values = FiberPreparation.entries,
                            filter = draft.fiberPreparations,
                            label = { value -> value.label() },
                            onFilterChange = { filter -> draft = draft.copy(fiberPreparations = filter) },
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = { draft = InventorySearchCriteria() },
                ) {
                    Text(stringResource(R.string.search_filters_reset))
                }
                Button(
                    onClick = { onApply(draft) },
                ) {
                    Text(
                        stringResource(
                            R.string.search_filters_show_results,
                            resultCount(draft),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductTypeFilterChoice(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}

@Composable
private fun SearchFilterSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Composable
private fun <T> OptionalValueFilterSection(
    title: String,
    values: List<T>,
    filter: OptionalValueFilter<T>,
    label: @Composable (T) -> String,
    onFilterChange: (OptionalValueFilter<T>) -> Unit,
) {
    SearchFilterSection(title = title) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            values.forEach { value ->
                FilterChip(
                    selected = value in filter.values,
                    onClick = {
                        onFilterChange(filter.copy(values = filter.values.toggle(value)))
                    },
                    label = { Text(label(value)) },
                )
            }
            FilterChip(
                selected = filter.includesUnspecified,
                onClick = {
                    onFilterChange(filter.copy(includesUnspecified = !filter.includesUnspecified))
                },
                label = { Text(stringResource(R.string.detail_field_empty)) },
            )
        }
    }
}

private fun <T> Set<T>.toggle(value: T): Set<T> {
    return if (value in this) this - value else this + value
}
