package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val GeometricLightColorScheme = lightColorScheme(
    primary = BrandPurple,
    onPrimary = Color.White,
    primaryContainer = LightVioletHighlight,
    onPrimaryContainer = ActiveSlateBlack,
    secondary = BrandPurple,
    onSecondary = Color.White,
    secondaryContainer = SoftGreyPurple,
    onSecondaryContainer = ActiveSlateBlack,
    tertiary = AccentAmber,
    onTertiary = Color.White,
    tertiaryContainer = SoftWarmGold,
    onTertiaryContainer = AccentAmber,
    background = SoftBackgroundViolet,
    onBackground = ActiveSlateBlack,
    surface = SoftBackgroundViolet,
    onSurface = ActiveSlateBlack,
    surfaceVariant = SoftGreyPurple,
    onSurfaceVariant = ActiveSlateBlack,
    outline = BorderPurpleOutline,
)

private val GeometricDarkColorScheme = darkColorScheme(
    primary = BrandPurpleLight,
    onPrimary = DarkContainerPurple,
    primaryContainer = DarkContainerPurple,
    onPrimaryContainer = BrandPurpleLight,
    secondary = BrandPurpleLight,
    onSecondary = DarkContainerPurple,
    secondaryContainer = DarkGreyPurple,
    onSecondaryContainer = ActiveSlateWhite,
    tertiary = AccentAmberLight,
    onTertiary = DarkWarmGold,
    tertiaryContainer = DarkWarmGold,
    onTertiaryContainer = AccentAmberLight,
    background = DarkBackgroundPurple,
    onBackground = ActiveSlateWhite,
    surface = DarkBackgroundPurple,
    onSurface = ActiveSlateWhite,
    surfaceVariant = DarkGreyPurple,
    onSurfaceVariant = ActiveSlateWhite,
    outline = DarkBorderOutline,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Enforce our custom "Geometric Balance" theme by setting default dynamicColor to false
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> GeometricDarkColorScheme
        else -> GeometricLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
