package st.misa.menzomat.ui.platform

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.LabeledIntent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toJavaLocalDate
import java.io.File
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** The Android bindings of the shared UI's platform needs. Activity-owned: the share sheet and settings are launched from it. */
fun androidPlatformServices(activity: ComponentActivity): PlatformServices = PlatformServices(
    shareSheet = AndroidShareSheet(activity),
    clipboard = AndroidTextClipboard(activity),
    fileSaver = AndroidFileSaver(activity),
    bugReporter = AndroidBugReporter(activity),
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

/**
 * The email apps are listed by hand: a send intent alone would offer every chat app, and a `mailto:` selector on it
 * makes the system's chooser show just one.
 */
private class AndroidBugReporter(private val activity: Activity) : BugReporter {
    override suspend fun compose(address: String, subject: String, body: String, attachment: TextAttachment?): Boolean {
        val uri = attachment?.let { cacheAndShare(it) ?: return false }
        val mimeType = attachment?.mimeType ?: "text/plain"
        val mail = Intent(Intent.ACTION_SEND)
            .setType(mimeType)
            .putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
            .putExtra(Intent.EXTRA_SUBJECT, subject)
            .putExtra(Intent.EXTRA_TEXT, body)
        if (attachment != null && uri != null) {
            mail.putExtra(Intent.EXTRA_STREAM, uri)
            mail.clipData = ClipData(ClipDescription(attachment.fileName, arrayOf(attachment.mimeType)), ClipData.Item(uri))
            mail.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val apps = emailApps(mimeType)
        if (apps.isEmpty()) return false
        val intents = apps.map { Intent(mail).setClassName(it.activityInfo.packageName, it.activityInfo.name) }
        val launch = if (intents.size == 1) {
            intents.single()
        } else {
            val others = apps.zip(intents).dropLast(1).map { (app, intent) ->
                LabeledIntent(intent, app.activityInfo.packageName, app.loadLabel(activity.packageManager), app.iconResource)
            }
            Intent.createChooser(intents.last(), null).putExtra(Intent.EXTRA_INITIAL_INTENTS, others.toTypedArray<Parcelable>())
        }
        return try {
            activity.startActivity(launch)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    private fun emailApps(mimeType: String): List<ResolveInfo> {
        val packageManager = activity.packageManager
        val sharing = packageManager.handlersOf(Intent(Intent.ACTION_SEND).setType(mimeType))
            .mapTo(HashSet()) { it.activityInfo.packageName to it.activityInfo.name }
        return packageManager.handlersOf(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")))
            .filter { (it.activityInfo.packageName to it.activityInfo.name) in sharing }
    }

    /** Only the latest report is kept: it holds personal data. */
    private suspend fun cacheAndShare(attachment: TextAttachment): Uri? = withContext(Dispatchers.IO) {
        runCatching {
            val folder = File(activity.cacheDir, REPORT_FOLDER).apply { mkdirs() }
            folder.listFiles()?.forEach { it.delete() }
            val file = File(folder, attachment.fileName).apply { writeText(attachment.text) }
            FileProvider.getUriForFile(activity, "${activity.packageName}.fileprovider", file)
        }.getOrNull()
    }

    private companion object {
        const val REPORT_FOLDER = "reports"
    }
}

private fun PackageManager.handlersOf(intent: Intent): List<ResolveInfo> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        queryIntentActivities(intent, 0)
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

/** "October 2026" in the locale's own words, always starting with a capital: Serbian writes its months in lower case. */
internal fun monthYearText(month: java.time.YearMonth, locale: Locale): String =
    DateTimeFormatter.ofPattern("LLLL yyyy", locale).format(month).replaceFirstChar { it.titlecase(locale) }

private class AndroidDateFormatter : DateFormatter {
    private val locale = Locale.getDefault()
    private val medium = mediumDateFormatter(locale)

    override fun mediumDate(date: LocalDate): String = medium.format(date.toJavaLocalDate())

    override fun monthAndYear(month: YearMonth): String =
        monthYearText(java.time.YearMonth.of(month.year, month.month.ordinal + 1), locale)
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
