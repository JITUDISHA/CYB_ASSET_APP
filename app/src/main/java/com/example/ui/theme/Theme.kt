package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
  primary = CrimsonPrimary,
  onPrimary = OnCrimson,
  primaryContainer = CrimsonContainer,
  onPrimaryContainer = OnCrimsonContainer,
  secondary = GoldSecondary,
  onSecondary = OnSecondary,
  background = NoirBackground,
  onBackground = OnSurfaceWhite,
  surface = NoirSurface,
  onSurface = OnSurfaceWhite,
  surfaceVariant = NoirSurfaceContainer,
  onSurfaceVariant = OnSurfaceMuted,
  outline = SlateOutline,
  outlineVariant = SlateOutlineVariant,
  error = CrimsonPrimary,
  onError = OnCrimson
)

private val LightColorScheme = lightColorScheme(
  primary = CrimsonPrimaryLight,
  onPrimary = OnCrimsonLight,
  primaryContainer = CrimsonContainerLight,
  onPrimaryContainer = OnCrimsonContainerLight,
  secondary = GoldSecondary,
  onSecondary = OnSecondary,
  background = IvoryBackground,
  onBackground = OnSurfaceBlack,
  surface = IvorySurface,
  onSurface = OnSurfaceBlack,
  surfaceVariant = IvorySurfaceContainer,
  onSurfaceVariant = OnSurfaceMutedLight,
  outline = SlateOutline,
  outlineVariant = IvorySurfaceContainerHigh,
  error = CrimsonPrimaryLight,
  onError = OnCrimsonLight
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
