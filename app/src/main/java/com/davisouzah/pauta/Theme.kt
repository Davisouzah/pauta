package com.davisouzah.pauta

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
  primary = Color(0xFF35506E),
  onPrimary = Color(0xFFFFFFFF),
  primaryContainer = Color(0xFFD8E5F6),
  onPrimaryContainer = Color(0xFF102A44),
  secondary = Color(0xFF8A6430),
  onSecondary = Color(0xFFFFFFFF),
  secondaryContainer = Color(0xFFF6E2C4),
  onSecondaryContainer = Color(0xFF2C1F0B),
  tertiary = Color(0xFF5C6B4D),
  onTertiary = Color(0xFFFFFFFF),
  background = Color(0xFFFAF8F2),
  onBackground = Color(0xFF1B1C18),
  surface = Color(0xFFFFFDF8),
  onSurface = Color(0xFF1B1C18),
  surfaceVariant = Color(0xFFEFEAE0),
  onSurfaceVariant = Color(0xFF625E53),
  outline = Color(0xFFD3CCBE),
  outlineVariant = Color(0xFFE7E1D5),
  error = Color(0xFFB3261E),
  onError = Color(0xFFFFFFFF),
  errorContainer = Color(0xFFF9DEDC),
  onErrorContainer = Color(0xFF410E0B),
)

private val DarkColors = darkColorScheme(
  primary = Color(0xFFAAC9E8),
  onPrimary = Color(0xFF0D2F4D),
  primaryContainer = Color(0xFF24445F),
  onPrimaryContainer = Color(0xFFD8E5F6),
  secondary = Color(0xFFE0BE86),
  onSecondary = Color(0xFF3B2A0D),
  secondaryContainer = Color(0xFF54401D),
  onSecondaryContainer = Color(0xFFF6E2C4),
  tertiary = Color(0xFFB9C9A6),
  onTertiary = Color(0xFF26351B),
  background = Color(0xFF121310),
  onBackground = Color(0xFFE8E4DA),
  surface = Color(0xFF1A1B17),
  onSurface = Color(0xFFE8E4DA),
  surfaceVariant = Color(0xFF2A2B25),
  onSurfaceVariant = Color(0xFFBAB5A8),
  outline = Color(0xFF4B4A42),
  outlineVariant = Color(0xFF33342D),
  error = Color(0xFFF2B8B5),
  onError = Color(0xFF601410),
  errorContainer = Color(0xFF8C1D18),
  onErrorContainer = Color(0xFFF9DEDC),
)

private val PautaTypography = Typography(
  displaySmall = TextStyle(
    fontFamily = FontFamily.Serif,
    fontWeight = FontWeight.Bold,
    fontSize = 36.sp,
    lineHeight = 42.sp,
    letterSpacing = (-0.6).sp,
  ),
  headlineMedium = TextStyle(
    fontFamily = FontFamily.Serif,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 34.sp,
    letterSpacing = (-0.3).sp,
  ),
  titleLarge = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 21.sp,
    lineHeight = 27.sp,
    letterSpacing = (-0.2).sp,
  ),
  titleMedium = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 17.sp,
    lineHeight = 23.sp,
    letterSpacing = 0.sp,
  ),
  bodyLarge = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 17.sp,
    lineHeight = 27.sp,
    letterSpacing = 0.1.sp,
  ),
  bodyMedium = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
  ),
  labelLarge = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.1.sp,
  ),
  labelMedium = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.3.sp,
  ),
  labelSmall = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 15.sp,
    letterSpacing = 0.4.sp,
  ),
)

private val PautaShapes = Shapes(
  extraSmall = RoundedCornerShape(8.dp),
  small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(18.dp),
  large = RoundedCornerShape(24.dp),
  extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun PautaTheme(
  darkTheme: Boolean,
  dynamicColor: Boolean,
  content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  val scheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    darkTheme -> DarkColors
    else -> LightColors
  }
  MaterialTheme(
    colorScheme = scheme,
    typography = PautaTypography,
    shapes = PautaShapes,
    content = content,
  )
}