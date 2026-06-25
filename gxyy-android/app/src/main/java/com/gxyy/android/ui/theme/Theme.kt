package com.gxyy.android.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Airbnb Design System colors
val Rausch = Color(0xFFFF385C)
val RauschDark = Color(0xFFE00B41)
val RauschLight = Color(0xFFFFD1DA)
val Luxe = Color(0xFF460479)
val Ink = Color(0xFF222222)
val Body = Color(0xFF3F3F3F)
val Muted = Color(0xFF6A6A6A)
val MutedSoft = Color(0xFF929292)
val Canvas = Color(0xFFFFFFFF)
val SurfaceSoft = Color(0xFFF7F7F7)
val SurfaceStrong = Color(0xFFF2F2F2)
val Hairline = Color(0xFFDDDDDD)
val HairlineSoft = Color(0xFFEBEBEB)
val ErrorText = Color(0xFFC13515)
val SuccessGreen = Color(0xFF2DA844)
val WarningOrange = Color(0xFFB85F0A)

private val LightColorScheme = lightColorScheme(
    primary = Rausch,
    onPrimary = Color.White,
    primaryContainer = RauschLight,
    onPrimaryContainer = RauschDark,
    secondary = Luxe,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5EDFF),
    error = ErrorText,
    onError = Color.White,
    errorContainer = Color(0xFFFEF2F2),
    background = Canvas,
    onBackground = Ink,
    surface = Canvas,
    onSurface = Ink,
    surfaceVariant = SurfaceSoft,
    onSurfaceVariant = Muted,
    outline = Hairline,
    outlineVariant = HairlineSoft,
)

private val DarkColorScheme = darkColorScheme(
    primary = Rausch,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5C1A28),
    onPrimaryContainer = RauschLight,
    secondary = Color(0xFFA0A0D8),
    onSecondary = Color.White,
    error = Color(0xFFE85D3D),
    onError = Color.White,
    errorContainer = Color(0xFF3D1515),
    background = Color(0xFF1A1A1A),
    onBackground = Color(0xFFE8E8E8),
    surface = Color(0xFF242424),
    onSurface = Color(0xFFE8E8E8),
    surfaceVariant = Color(0xFF2E2E2E),
    onSurfaceVariant = Color(0xFF888888),
    outline = Color(0xFF3A3A3A),
    outlineVariant = Color(0xFF2E2E2E),
)

// Airbnb-style shapes
val AirbnbShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4),
    small = androidx.compose.foundation.shape.RoundedCornerShape(8),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(12),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(20),
)

@Composable
fun GxyyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = AirbnbShapes,
        content = content
    )
}
