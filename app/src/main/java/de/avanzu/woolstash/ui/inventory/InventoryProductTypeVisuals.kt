package de.avanzu.woolstash.ui.inventory

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.avanzu.woolstash.R
import de.avanzu.woolstash.domain.model.ProductType

@DrawableRes
internal fun ProductType.artworkRes(): Int = when (this) {
    ProductType.Yarn -> R.drawable.product_yarn
    ProductType.Fiber -> R.drawable.product_fiber
}

@Composable
internal fun ProductType.containerColor(): Color = when (this) {
    ProductType.Yarn -> MaterialTheme.colorScheme.primaryContainer
    ProductType.Fiber -> MaterialTheme.colorScheme.secondaryContainer
}

@Composable
internal fun ProductType.onContainerColor(): Color = when (this) {
    ProductType.Yarn -> MaterialTheme.colorScheme.onPrimaryContainer
    ProductType.Fiber -> MaterialTheme.colorScheme.onSecondaryContainer
}

@Composable
internal fun ProductTypeArtwork(
    productType: ProductType,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 76.dp,
    showContainer: Boolean = true,
) {
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (showContainer) {
                    Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(productType.containerColor())
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(productType.artworkRes()),
            contentDescription = contentDescription,
            modifier = Modifier
                .matchParentSize()
                .padding(if (showContainer) 5.dp else 0.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
internal fun ProductTypePill(
    productType: ProductType,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = productType.containerColor(),
        contentColor = productType.onContainerColor(),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            text = productType.productTypeLabel(),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
