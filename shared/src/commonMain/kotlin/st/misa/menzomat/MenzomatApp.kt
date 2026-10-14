package st.misa.menzomat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import st.misa.menzomat.card.domain.model.AppSettings
import st.misa.menzomat.card.domain.repository.SettingsRepository
import st.misa.menzomat.card.presentation.card.CardRoute
import st.misa.menzomat.card.presentation.settings.SettingsRoute
import st.misa.menzomat.ui.platform.PlatformServices
import st.misa.menzomat.ui.platform.LocalPlatformServices
import st.misa.menzomat.ui.theme.LocalReducedMotion
import st.misa.menzomat.ui.theme.MenzomatTheme
import st.misa.menzomat.ui.theme.isDarkTheme

/**
 * The whole UI, shared by every platform app. The app supplies [platform] and, if it cares,
 * learns the resolved dark mode through [onDarkTheme] (Android uses it for the system bars).
 */
@Composable
fun MenzomatApp(
    platform: PlatformServices,
    onDarkTheme: (Boolean) -> Unit = {},
) {
    val settingsRepository = koinInject<SettingsRepository>()
    val settings by settingsRepository.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val dark = isDarkTheme(settings.themeMode)
    LaunchedEffect(dark) { onDarkTheme(dark) }

    CompositionLocalProvider(LocalPlatformServices provides platform) {
        MenzomatTheme(
            themeMode = settings.themeMode,
            useDynamicColor = settings.useDynamicColor,
            platform = platform.theme,
        ) {
            MenzomatNavHost()
        }
    }
}

/** Two destinations, so the "back stack" is one flag. The ViewModels live as long as the app, so state survives the trip. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MenzomatNavHost() {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showSettings) { showSettings = false }

    val reducedMotion = LocalReducedMotion.current
    AnimatedContent(
        targetState = showSettings,
        transitionSpec = {
            if (reducedMotion) EnterTransition.None togetherWith ExitTransition.None
            else fadeIn() togetherWith fadeOut()
        },
        label = "destination",
    ) { settingsVisible ->
        if (settingsVisible) {
            SettingsRoute(onBack = { showSettings = false })
        } else {
            CardRoute(onOpenSettings = { showSettings = true })
        }
    }
}
