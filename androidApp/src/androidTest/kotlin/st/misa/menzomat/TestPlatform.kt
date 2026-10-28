package st.misa.menzomat

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import st.misa.menzomat.ui.platform.AppInfo
import st.misa.menzomat.ui.platform.DateFormatter
import st.misa.menzomat.ui.platform.FileSaver
import st.misa.menzomat.ui.platform.NfcSettings
import st.misa.menzomat.ui.platform.NoPlatformTheme
import st.misa.menzomat.ui.platform.PlatformServices
import st.misa.menzomat.ui.platform.ShareSheet
import st.misa.menzomat.ui.platform.TextClipboard

/** Platform services for UI tests: ISO dates, a fixed version, nothing to share, copy, save or open. */
val testPlatform = PlatformServices(
    shareSheet = object : ShareSheet { override suspend fun shareText(text: String) = Unit },
    clipboard = object : TextClipboard { override suspend fun copyText(text: String) = Unit },
    fileSaver = object : FileSaver { override suspend fun saveText(suggestedName: String, text: String) = true },
    nfcSettings = object : NfcSettings { override fun open() = Unit },
    dateFormatter = object : DateFormatter {
        override fun mediumDate(date: LocalDate) = date.toString()
        override fun monthAndYear(month: YearMonth) = month.toString()
    },
    theme = NoPlatformTheme,
    appInfo = AppInfo(versionName = "1.2.3"),
)
