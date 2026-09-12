package de.avanzu.woolstash.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.avanzu.woolstash.R
import de.avanzu.woolstash.domain.model.ProductType
import de.avanzu.woolstash.ui.theme.WoolStashTheme

enum class CreateInventoryItemType {
    Yarn,
    Fiber,
}

@Composable
fun CreateInventoryItemScreen(
    type: CreateInventoryItemType?,
    onBackClick: () -> Unit,
    onCreateYarn: (CreateInventoryItemInput) -> Unit,
    onCreateFiber: (CreateInventoryItemInput) -> Unit,
    modifier: Modifier = Modifier,
    tagSuggestions: List<String> = emptyList(),
    referenceSuggestions: InventoryReferenceSuggestions = InventoryReferenceSuggestions(),
) {
    val itemType = requireNotNull(type) { "CreateInventoryItemScreen requires a product type." }
    val title = when (itemType) {
        CreateInventoryItemType.Yarn -> stringResource(R.string.create_yarn_title)
        CreateInventoryItemType.Fiber -> stringResource(R.string.create_fiber_title)
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background,
    ) {
        Scaffold(
            topBar = {
                CreateItemTopBar(
                    title = title,
                    onBackClick = onBackClick,
                )
            },
        ) {
            Surface(
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    CreateProductIntro(itemType = itemType)

                    Text(
                        text = stringResource(R.string.create_section_basics),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    InventoryCoreFieldsEditor(
                        initialName = "",
                        initialColorDescription = null,
                        initialMaterialDescription = null,
                        initialWeightGrams = null,
                        initialLocation = null,
                        initialManufacturer = null,
                        initialPurchaseSource = null,
                        initialTags = emptyList(),
                        tagSuggestions = tagSuggestions,
                        referenceSuggestions = referenceSuggestions,
                        submitLabel = stringResource(R.string.action_create),
                        onSubmit = { input ->
                            when (itemType) {
                                CreateInventoryItemType.Yarn -> onCreateYarn(input)
                                CreateInventoryItemType.Fiber -> onCreateFiber(input)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateProductIntro(
    itemType: CreateInventoryItemType,
    modifier: Modifier = Modifier,
) {
    val productType = when (itemType) {
        CreateInventoryItemType.Yarn -> ProductType.Yarn
        CreateInventoryItemType.Fiber -> ProductType.Fiber
    }
    val description = when (itemType) {
        CreateInventoryItemType.Yarn -> stringResource(R.string.create_yarn_description)
        CreateInventoryItemType.Fiber -> stringResource(R.string.create_fiber_description)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = productType.containerColor(),
        contentColor = productType.onContainerColor(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProductTypeArtwork(
                productType = productType,
                contentDescription = null,
                size = 88.dp,
                showContainer = false,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = productType.productTypeLabel(),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun CreateItemTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBackClick,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateInventoryItemScreenPreview() {
    WoolStashTheme {
        CreateInventoryItemScreen(
            type = CreateInventoryItemType.Yarn,
            onBackClick = {},
            onCreateYarn = {},
            onCreateFiber = {},
            tagSuggestions = listOf("socken", "natur"),
            referenceSuggestions = InventoryReferenceSuggestions(
                locations = listOf("Kiste Schlafzimmer"),
                manufacturers = listOf("Malabrigo"),
                purchaseSources = listOf("Wollgeschäft"),
            ),
        )
    }
}
