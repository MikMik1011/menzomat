package st.misa.menzomat.card.presentation.settings

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.junit.Rule
import org.junit.Test
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.about_license
import st.misa.menzomat.shared.resources.about_source
import st.misa.menzomat.testPlatform
import st.misa.menzomat.ui.platform.LocalPlatformServices
import st.misa.menzomat.ui.theme.MenzomatTheme
import kotlin.test.assertEquals

class SettingsScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private fun string(res: StringResource): String = runBlocking { getString(res) }

    private val opened = mutableListOf<String>()

    private fun show() {
        val uriHandler = object : UriHandler { override fun openUri(uri: String) { opened += uri } }
        rule.setContent {
            CompositionLocalProvider(LocalPlatformServices provides testPlatform, LocalUriHandler provides uriHandler) {
                MenzomatTheme {
                    SettingsScreen(SettingsUiState(), NoSettingsActions, onBack = {})
                }
            }
        }
    }

    @Test
    fun aboutShowsTheVersionAndBothLinks() {
        show()
        rule.onNodeWithText(testPlatform.appInfo.versionName).performScrollTo().assertIsDisplayed()
        rule.onNodeWithText(string(Res.string.about_source)).performScrollTo().assertIsDisplayed()
        rule.onNodeWithText(string(Res.string.about_license)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun sourceCodeAndLicenseOpenOutsideTheApp() {
        show()
        rule.onNodeWithText(string(Res.string.about_source)).performScrollTo().performClick()
        rule.onNodeWithText(string(Res.string.about_license)).performScrollTo().performClick()
        assertEquals(
            listOf(
                "https://github.com/MikMik1011/menzomat",
                "https://github.com/MikMik1011/menzomat/blob/main/LICENSE",
            ),
            opened,
        )
    }
}
