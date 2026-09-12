package de.avanzu.woolstash.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandBerry,
    onPrimary = Color(0xFF3C0B2D),
    primaryContainer = Color(0xFF5B234A),
    onPrimaryContainer = Color(0xFFFFD8EE),
    secondary = BrandPetrol,
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF004F49),
    onSecondaryContainer = Color(0xFF9CF2E6),
    tertiary = BrandCream,
    onTertiary = BrandAubergine,
    background = DarkBackground,
    onBackground = BrandCream,
    surface = DarkSurface,
    onSurface = BrandCream,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFD2C8D2),
    surfaceDim = DarkBackground,
    surfaceBright = Color(0xFF37313D),
    surfaceContainerLowest = Color(0xFF0D0B11),
    surfaceContainerLow = Color(0xFF18151E),
    surfaceContainer = Color(0xFF211D29),
    surfaceContainerHigh = Color(0xFF29242F),
    surfaceContainerHighest = Color(0xFF322C38),
    inverseSurface = BrandCream,
    inverseOnSurface = Color(0xFF322D33),
    surfaceTint = BrandBerry,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

private val LightColorScheme = lightColorScheme(
    primary = BrandBerryDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD8ED),
    onPrimaryContainer = Color(0xFF3B0A2D),
    secondary = BrandPetrolDeep,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF9CF2E6),
    onSecondaryContainer = Color(0xFF00201D),
    tertiary = BrandAubergine,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF514951),
    surfaceDim = Color(0xFFE6DFE2),
    surfaceBright = LightSurface,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFAF4EF),
    surfaceContainer = Color(0xFFF4EEE9),
    surfaceContainerHigh = Color(0xFFEEE8E4),
    surfaceContainerHighest = Color(0xFFE8E1DE),
    inverseSurface = Color(0xFF362F36),
    inverseOnSurface = Color(0xFFFBEFF7),
    surfaceTint = BrandBerryDeep,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

@Composable
fun WoolStashTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
