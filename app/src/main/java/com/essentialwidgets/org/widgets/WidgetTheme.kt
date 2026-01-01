package com.essentialwidgets.org.widgets

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.glance.material3.ColorProviders
import com.essentialwidgets.org.ui.theme.n_gray
import com.essentialwidgets.org.ui.theme.n_red
import com.essentialwidgets.org.ui.theme.n_yellow

object WidgetTheme {
    // Define the colors for the light theme using Glance's ColorScheme
    private val lightScheme = lightColorScheme(
        primary = n_red,
        onPrimary = Color.White,
        surface = Color.White,
        onSurface = Color.Black,
        secondary = n_gray,
        tertiary = n_yellow,
    )

    // Define the colors for the dark theme using Glance's ColorScheme
    private val darkScheme = darkColorScheme(
        primary = n_red,
        onPrimary = Color.Black,
        surface = Color(0xFF1C1C1E),
        onSurface = Color.White,
        secondary = n_gray,
        tertiary = n_yellow,
    )

    // This correctly calls ColorProviders(light: ColorScheme, dark: ColorScheme)
    val colors = ColorProviders(
        light = lightScheme,
        dark = darkScheme
    )
}
