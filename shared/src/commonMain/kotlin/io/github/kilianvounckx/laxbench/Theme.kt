package io.github.kilianvounckx.laxbench

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Solid black from the Belgium Lacrosse logo. */
private val BrandBlack = Color(0xFF000000)

/** Solid white, the neutral base of the Belgium Lacrosse visual identity. */
private val BrandWhite = Color(0xFFFFFFFF)

/** Bright gold from the Belgium Lacrosse logo, a softer accent for highlights and containers. */
private val BrandGold = Color(0xFFFCAF2F)

/**
 * Bright red from the Belgium Lacrosse logo, the strongest secondary accent for interactive
 * elements.
 */
private val BrandRed = Color(0xFFDE3A2C)

/**
 * Laxbench Material3 light color scheme: primary is the brand black per explicit product decision;
 * secondary is brand red as the stronger accent; tertiary is brand gold as the softer highlight.
 * Error is left at Material3's baseline to stay visually distinct from secondary (brand-red).
 * Background and surface stay white to match the web site (which has no dark theme).
 */
val LaxbenchColorScheme: ColorScheme =
  lightColorScheme(
    primary = BrandBlack,
    onPrimary = BrandWhite,
    primaryContainer = Color(0xFFD9D9D9),
    onPrimaryContainer = BrandBlack,
    secondary = BrandRed,
    onSecondary = BrandWhite,
    secondaryContainer = Color(0xFFF9DEDC),
    onSecondaryContainer = Color(0xFF410E0B),
    tertiary = BrandGold,
    onTertiary = BrandBlack,
    tertiaryContainer = Color(0xFFFFE8B8),
    onTertiaryContainer = Color(0xFF3A2900),
    background = BrandWhite,
    onBackground = BrandBlack,
    surface = BrandWhite,
    onSurface = BrandBlack,
    surfaceVariant = Color(0xFFE7E7E7),
    onSurfaceVariant = Color(0xFF444444),
    outline = Color(0xFF6B6B6B),
    outlineVariant = Color(0xFFC7C7C7),
  )

private val baselineTypography = Typography()

/**
 * Restyles only the display/headline/title families to a bolder, tighter weight evoking the brand's
 * heavy condensed wordmark, entirely via FontWeight/letterSpacing on the platform's default font
 * family (no custom FontFamily/font loading, per this feature's non-goals). Body and label styles
 * are left at Material3's baseline for legibility.
 */
val LaxbenchTypography =
  Typography(
    displayLarge =
      baselineTypography.displayLarge.copy(
        fontWeight = FontWeight.Black,
        letterSpacing = (-1.0).sp,
      ),
    displayMedium =
      baselineTypography.displayMedium.copy(
        fontWeight = FontWeight.Black,
        letterSpacing = (-0.75).sp,
      ),
    displaySmall =
      baselineTypography.displaySmall.copy(
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.5).sp,
      ),
    headlineLarge =
      baselineTypography.headlineLarge.copy(
        fontWeight = FontWeight.Black,
        letterSpacing = (-0.5).sp,
      ),
    headlineMedium =
      baselineTypography.headlineMedium.copy(
        fontWeight = FontWeight.Black,
        letterSpacing = (-0.25).sp,
      ),
    headlineSmall =
      baselineTypography.headlineSmall.copy(
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.25).sp,
      ),
    titleLarge = baselineTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = baselineTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
    titleSmall = baselineTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
  )

/**
 * Sharper, boxier corner radii than Material3's defaults (4/8/12/16/28dp), reading as clean and
 * bold rather than soft, echoing the brand wordmark's sharp-edged type.
 */
val LaxbenchShapes =
  Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp),
  )
