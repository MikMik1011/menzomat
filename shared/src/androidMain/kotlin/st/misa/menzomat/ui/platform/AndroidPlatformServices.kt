package st.misa.menzomat.ui.platform

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** The Android bindings of the shared UI's platform needs. Activity-owned: the share sheet and settings are launched from it. */
fun androidPlatformServices(activity: Activity): PlatformServices = PlatformServices(
    shareSheet = AndroidShareSheet(activity),
    clipboard = AndroidTextClipboard(activity),
    nfcSettings = AndroidNfcSettings(activity),
    dateFormatter = AndroidDateFormatter(),
    theme = AndroidThemePlatform(),
    appInfo = AppInfo(activity.appVersionName()),
)

private fun Activity.appVersionName(): String = runCatching {
    val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, 0)
    }
    info.versionName
}.getOrNull().orEmpty()

private class AndroidShareSheet(private val activity: Activity) : ShareSheet {
    override suspend fun shareText(text: String) {
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        activity.startActivity(Intent.createChooser(send, null))
    }
}

private class AndroidTextClipboard(private val activity: Activity) : TextClipboard {
    override suspend fun copyText(text: String) {
        val clipboard = activity.getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText("card", text))
    }
}

private class AndroidNfcSettings(private val activity: Activity) : NfcSettings {
    override fun open() {
        runCatching { activity.startActivity(Intent(Settings.ACTION_NFC_SETTINGS)) }
    }
}

/**
 * The phone's medium date style, except in Serbian: the standard pattern there is "10. 11. 2003." with a
 * space after each dot, while Serbian documents and cards write "10.11.2003.".
 */
internal fun mediumDateFormatter(locale: Locale): DateTimeFormatter =
    if (locale.language == "sr") {
        DateTimeFormatter.ofPattern("d.M.yyyy.", locale)
    } else {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
    }

private class AndroidDateFormatter : DateFormatter {
    private val medium = mediumDateFormatter(Locale.getDefault())
    private val monthYear = DateTimeFormatter.ofPattern("LLLL yyyy")

    override fun mediumDate(date: LocalDate): String = medium.format(date.toJavaLocalDate())

    override fun monthAndYear(month: YearMonth): String =
        monthYear.format(java.time.YearMonth.of(month.year, month.month.ordinal + 1))
}

private class AndroidThemePlatform : ThemePlatform {
    override val supportsDynamicColor: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    @Composable
    override fun dynamicColorScheme(dark: Boolean): ColorScheme {
        val context = LocalContext.current
        return if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }

    @Composable
    override fun prefersReducedMotion(): Boolean {
        val context = LocalContext.current
        return remember(context) {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }
    }
}
