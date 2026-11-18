package st.misa.menzomat.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/**
 * ISIC brand palette (brandguide.isic.cz/color). This is the only file that contains color literals.
 * Brand Black is left out: it is a violet black that clashes with the green, so text and surfaces use [Neutral].
 */
private object Isic {
    val White = Color(0xFFFFFFFF)

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
    val KhakiTint = Color(0xFFFFF6C8)
    val KhakiDeep = Color(0xFF92864E)
}

/**
 * Near-neutral grays with a faint Light Sea Green tint, the tones Material derives for a teal seed (as with
 * wallpaper colors). Named by tone: 0 is black, 100 white. Variant tones carry a little more of the tint.
 */
private object Neutral {
    val T4 = Color(0xFF090F10)
    val T6 = Color(0xFF0E1415)
    val T10 = Color(0xFF161D1E)
    val T12 = Color(0xFF1A2122)
    val T17 = Color(0xFF252B2C)
    val T20 = Color(0xFF2B3132)
    val T22 = Color(0xFF2F3637)
    val T24 = Color(0xFF343A3B)
    val T90 = Color(0xFFDEE4E4)
    val Variant30 = Color(0xFF3F4949)
    val Variant50 = Color(0xFF6F7979)
    val Variant60 = Color(0xFF899392)
    val Variant80 = Color(0xFFBEC8C8)
    val Variant90 = Color(0xFFDAE4E4)
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

/** Light: the Light Sea Green fills the hero surfaces and always carries near-black text. */
internal val MenzomatLightColors: ColorScheme = lightColorScheme(
    primary = Isic.Sea600,
    onPrimary = Isic.White,
    primaryContainer = Isic.Sea,
    onPrimaryContainer = Neutral.T6,
    inversePrimary = Isic.Sea300,
    secondary = Isic.Sea800,
    onSecondary = Isic.White,
    secondaryContainer = lerp(Isic.Sea50, Isic.Sea300, 0.5f),
    onSecondaryContainer = Neutral.T10,
    tertiary = Isic.Tomato600,
    onTertiary = Isic.White,
    tertiaryContainer = Isic.Tomato50,
    onTertiaryContainer = Isic.Tomato900,
    error = MaterialError.Light,
    onError = Isic.White,
    errorContainer = MaterialError.LightContainer,
    onErrorContainer = MaterialError.OnLightContainer,
    background = Isic.White,
    onBackground = Neutral.T10,
    surface = Isic.White,
    onSurface = Neutral.T10,
    surfaceVariant = Neutral.Variant90,
    onSurfaceVariant = Neutral.Variant30,
    surfaceTint = Isic.Sea600,
    inverseSurface = Neutral.T20,
    inverseOnSurface = Isic.Sea50,
    outline = Neutral.Variant50,
    outlineVariant = Neutral.Variant80,
    scrim = Color.Black,
    surfaceBright = Isic.White,
    surfaceDim = lerp(Isic.Sea50, Neutral.Variant90, 0.5f),
    surfaceContainerLowest = Isic.White,
    surfaceContainerLow = lerp(Isic.White, Isic.Sea50, 0.5f),
    surfaceContainer = lerp(Isic.White, Isic.Sea50, 0.8f),
    surfaceContainerHigh = Isic.Sea50,
    surfaceContainerHighest = lerp(Isic.Sea50, Isic.Sea300, 0.25f),
)

internal val MenzomatDarkColors: ColorScheme = darkColorScheme(
    primary = Isic.Sea,
    onPrimary = Neutral.T6,
    primaryContainer = Isic.Sea800,
    onPrimaryContainer = Isic.Sea50,
    inversePrimary = Isic.Sea600,
    secondary = Isic.Sea300,
    onSecondary = Neutral.T6,
    secondaryContainer = lerp(Neutral.Variant30, Isic.Sea800, 0.25f),
    onSecondaryContainer = Isic.Sea50,
    tertiary = Isic.Tomato300,
    onTertiary = Neutral.T6,
    tertiaryContainer = Isic.Tomato900,
    onTertiaryContainer = Isic.Tomato50,
    error = MaterialError.Dark,
    onError = MaterialError.OnDark,
    errorContainer = MaterialError.DarkContainer,
    onErrorContainer = MaterialError.LightContainer,
    background = Neutral.T6,
    onBackground = Neutral.T90,
    surface = Neutral.T6,
    onSurface = Neutral.T90,
    surfaceVariant = Neutral.Variant30,
    onSurfaceVariant = Neutral.Variant80,
    surfaceTint = Isic.Sea,
    inverseSurface = Neutral.T90,
    inverseOnSurface = Neutral.T20,
    outline = Neutral.Variant60,
    outlineVariant = Neutral.Variant30,
    scrim = Color.Black,
    surfaceBright = Neutral.T24,
    surfaceDim = Neutral.T6,
    surfaceContainerLowest = Neutral.T4,
    surfaceContainerLow = Neutral.T10,
    surfaceContainer = Neutral.T12,
    surfaceContainerHigh = Neutral.T17,
    surfaceContainerHighest = Neutral.T22,
)

/** Brand colors that have no Material role. Khaki is an accent: used sparingly, per the guide's proportions. */
@Immutable
class BrandColors(
    val highlight: Color,
    val onHighlight: Color,
)

internal val MenzomatBrandColors = BrandColors(highlight = Isic.Khaki, onHighlight = Neutral.T6)

/**
 * A warning in a calm tint, with the text in the normal text color. Dark: the brand Khaki mixed into the surface,
 * and Khaki itself for the icon. Light: the brand's pale Khaki tint (mixing yellow into a teal-gray surface turns
 * it green) and its deep shade for the icon. A full Khaki banner is too loud for something shown on every visit.
 */
@Immutable
class WarningColors(val container: Color, val content: Color, val icon: Color)

internal fun warningColors(scheme: ColorScheme): WarningColors {
    val dark = scheme.surface.luminance() < 0.5f
    return WarningColors(
        container = if (dark) lerp(scheme.surfaceContainerHigh, Isic.Khaki, 0.16f) else Isic.KhakiTint,
        content = scheme.onSurface,
        icon = if (dark) Isic.Khaki else Isic.KhakiDeep,
    )
}

/** The warning colors of the current theme, so they follow wallpaper colors too. */
val MaterialTheme.warning: WarningColors
    @Composable @ReadOnlyComposable get() = warningColors(colorScheme)
