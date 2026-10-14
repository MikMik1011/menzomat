package st.misa.menzomat.ui.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/** Launches the system share sheet. Returns once the sheet is presented, not when the user finishes with it. */
interface ShareSheet {
    suspend fun shareText(text: String)
}

/** Puts plain text on the system clipboard. */
interface TextClipboard {
    suspend fun copyText(text: String)
}

/** Takes the user to wherever NFC can be switched on. Platforms without such a switch do nothing. */
interface NfcSettings {
    fun open()
}

/** Dates the way the phone's language and region write them. */
interface DateFormatter {
    fun mediumDate(date: LocalDate): String
    fun monthAndYear(month: YearMonth): String
}

/** Theme inputs that only the platform can answer. */
interface ThemePlatform {
    val supportsDynamicColor: Boolean

    /** The wallpaper-derived scheme. Only called when [supportsDynamicColor] is true. */
    @Composable
    fun dynamicColorScheme(dark: Boolean): ColorScheme

    /** True when the user turned animations off in the system settings. */
    @Composable
    fun prefersReducedMotion(): Boolean
}

/** What the shared UI needs from the platform, provided once at the root by each app. */
class PlatformServices(
    val shareSheet: ShareSheet,
    val clipboard: TextClipboard,
    val nfcSettings: NfcSettings,
    val dateFormatter: DateFormatter,
    val theme: ThemePlatform,
)

val LocalPlatformServices = staticCompositionLocalOf<PlatformServices> {
    error("PlatformServices not provided; wrap the UI in MenzomatApp or provide LocalPlatformServices")
}

/** For previews and tests: no wallpaper colors, animations on, ISO dates, nothing to open or share. */
object NoPlatformTheme : ThemePlatform {
    override val supportsDynamicColor = false

    @Composable
    override fun dynamicColorScheme(dark: Boolean): ColorScheme = error("no dynamic color")

    @Composable
    override fun prefersReducedMotion(): Boolean = false
}
