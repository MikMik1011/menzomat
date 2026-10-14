package st.misa.menzomat.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * ISIC brand palette (brandguide.isic.cz/color). This is the only file that contains color literals.
 * The guide asks for no new colors, so surface tones are blends of brand tints, not new hues.
 */
private object Isic {
    val White = Color(0xFFFFFFFF)

    val SlateBlue = Color(0xFF5438DC)
    val SlateBlue50 = Color(0xFFF5F3FD)
    val SlateBlue300 = Color(0xFF9888EA)
    val SlateBlue700 = Color(0xFF3921A9)
    val SlateBlue900 = Color(0xFF231651)

    val Sea = Color(0xFF40B8B8)
    val Sea50 = Color(0xFFE3F5F5)
    val Sea300 = Color(0xFFA0DCDC)
    val Sea600 = Color(0xFF2D8181)
    val Sea800 = Color(0xFF006666)

    val Tomato300 = Color(0xFFFAA39E)
    val Tomato50 = Color(0xFFFEF0EF)
    val Tomato600 = Color(0xFFAC4741)
    val Tomato900 = Color(0xFF622925)

    val Khaki = Color(0xFFFFE66D)

    val Ink = Color(0xFF120B29)
    val Ink50 = Color(0xFFDCDAE3)
    val Ink400 = Color(0xFF726A90)
    val Ink600 = Color(0xFF4F4574)
    val Ink800 = Color(0xFF201749)
}

/** Standard Material error roles; ISIC has no error color and Tomato must not read as one. */
private object MaterialError {
    val Light = Color(0xFFB3261E)
    val LightContainer = Color(0xFFF9DEDC)
    val OnLightContainer = Color(0xFF410E0B)
    val Dark = Color(0xFFF2B8B5)
    val OnDark = Color(0xFF601410)
    val DarkContainer = Color(0xFF8C1D18)
}

/** Light: the Light Sea Green fills the hero surfaces and always carries brand Black text. */
internal val MenzomatLightColors: ColorScheme = lightColorScheme(
    primary = Isic.Sea600,
    onPrimary = Isic.White,
    primaryContainer = Isic.Sea,
    onPrimaryContainer = Isic.Ink,
    inversePrimary = Isic.Sea300,
    secondary = Isic.SlateBlue,
    onSecondary = Isic.White,
    secondaryContainer = Isic.SlateBlue50,
    onSecondaryContainer = Isic.SlateBlue900,
    tertiary = Isic.Tomato600,
    onTertiary = Isic.White,
    tertiaryContainer = Isic.Tomato50,
    onTertiaryContainer = Isic.Tomato900,
    error = MaterialError.Light,
    onError = Isic.White,
    errorContainer = MaterialError.LightContainer,
    onErrorContainer = MaterialError.OnLightContainer,
    background = Isic.White,
    onBackground = Isic.Ink,
    surface = Isic.White,
    onSurface = Isic.Ink,
    surfaceVariant = Isic.Ink50,
    onSurfaceVariant = Isic.Ink600,
    surfaceTint = Isic.Sea600,
    inverseSurface = Isic.Ink,
    inverseOnSurface = Isic.Sea50,
    outline = Isic.Ink400,
    outlineVariant = Isic.Ink50,
    scrim = Isic.Ink,
    surfaceBright = Isic.White,
    surfaceDim = lerp(Isic.Sea50, Isic.Ink50, 0.5f),
    surfaceContainerLowest = Isic.White,
    surfaceContainerLow = lerp(Isic.White, Isic.Sea50, 0.5f),
    surfaceContainer = lerp(Isic.White, Isic.Sea50, 0.8f),
    surfaceContainerHigh = Isic.Sea50,
    surfaceContainerHighest = lerp(Isic.Sea50, Isic.Sea300, 0.25f),
)

internal val MenzomatDarkColors: ColorScheme = darkColorScheme(
    primary = Isic.Sea,
    onPrimary = Isic.Ink,
    primaryContainer = Isic.Sea800,
    onPrimaryContainer = Isic.Sea50,
    inversePrimary = Isic.Sea600,
    secondary = Isic.SlateBlue300,
    onSecondary = Isic.Ink,
    secondaryContainer = Isic.SlateBlue700,
    onSecondaryContainer = Isic.SlateBlue50,
    tertiary = Isic.Tomato300,
    onTertiary = Isic.Ink,
    tertiaryContainer = Isic.Tomato900,
    onTertiaryContainer = Isic.Tomato50,
    error = MaterialError.Dark,
    onError = MaterialError.OnDark,
    errorContainer = MaterialError.DarkContainer,
    onErrorContainer = MaterialError.LightContainer,
    background = Isic.Ink,
    onBackground = Isic.Sea50,
    surface = Isic.Ink,
    onSurface = Isic.Sea50,
    surfaceVariant = Isic.Ink600,
    onSurfaceVariant = Isic.Ink50,
    surfaceTint = Isic.Sea,
    inverseSurface = Isic.Sea50,
    inverseOnSurface = Isic.Ink,
    outline = Isic.Ink400,
    outlineVariant = Isic.Ink600,
    scrim = Isic.Ink,
    surfaceBright = lerp(Isic.Ink800, Isic.Ink600, 0.55f),
    surfaceDim = Isic.Ink,
    surfaceContainerLowest = lerp(Isic.Ink, Color.Black, 0.4f),
    surfaceContainerLow = lerp(Isic.Ink, Isic.Ink800, 0.5f),
    surfaceContainer = Isic.Ink800,
    surfaceContainerHigh = lerp(Isic.Ink800, Isic.Ink600, 0.3f),
    surfaceContainerHighest = lerp(Isic.Ink800, Isic.Ink600, 0.55f),
)

/** Brand colors that have no Material role. Khaki is an accent: used sparingly, per the guide's proportions. */
@Immutable
class BrandColors(
    val highlight: Color,
    val onHighlight: Color,
)

internal val MenzomatBrandColors = BrandColors(highlight = Isic.Khaki, onHighlight = Isic.Ink)
