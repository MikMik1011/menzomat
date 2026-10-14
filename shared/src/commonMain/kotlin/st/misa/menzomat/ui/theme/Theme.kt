package st.misa.menzomat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import st.misa.menzomat.card.domain.model.ThemeMode
import st.misa.menzomat.ui.platform.NoPlatformTheme
import st.misa.menzomat.ui.platform.ThemePlatform

internal val LocalBrandColors = staticCompositionLocalOf { MenzomatBrandColors }

@Composable
fun isDarkTheme(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}

/**
 * ISIC brand colors by default; wallpaper-derived Material You colors only when the user opts in.
 * Type and shape use the Material 3 scales as they are.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MenzomatTheme(
    themeMode: ThemeMode = ThemeMode.System,
    useDynamicColor: Boolean = false,
    platform: ThemePlatform = NoPlatformTheme,
    content: @Composable () -> Unit,
) {
    val dark = isDarkTheme(themeMode)
    val colorScheme = when {
        useDynamicColor && platform.supportsDynamicColor -> platform.dynamicColorScheme(dark)
        dark -> MenzomatDarkColors
        else -> MenzomatLightColors
    }
    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalReducedMotion provides platform.prefersReducedMotion(),
        LocalBrandColors provides MenzomatBrandColors,
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = MotionScheme.expressive(),
            shapes = Shapes(),
            typography = Typography(),
            content = content,
        )
    }
}
