package st.misa.menzomat.card.data

import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import st.misa.menzomat.card.data.local.LastDumpStore
import st.misa.menzomat.card.data.preferences.DataStoreSettingsRepository
import st.misa.menzomat.card.data.preferences.decodeSelection
import st.misa.menzomat.card.data.preferences.encodeSelection
import st.misa.menzomat.card.domain.model.AppSettings
import st.misa.menzomat.card.domain.model.ProfileSelection
import st.misa.menzomat.card.domain.model.ThemeMode
import st.misa.menzomat.core.domain.desfire.Aid
import st.misa.menzomat.core.domain.desfire.AppDump
import st.misa.menzomat.core.domain.desfire.CardDump
import st.misa.menzomat.core.domain.desfire.FileType
import st.misa.menzomat.core.domain.desfire.RawFile
import st.misa.menzomat.fake.tempFilePath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StoresTest {
    private val dump = CardDump(
        uid = "04AABB",
        readAtEpochMillis = 1_700_000_000_000,
        apps = listOf(
            AppDump(
                Aid(0x00000D),
                listOf(
                    RawFile(1, FileType.Backup, size = 3, dataHex = "02011f"),
                    RawFile(3, FileType.Value, error = "Permission denied (0x9D)"),
                ),
            ),
        ),
    )

    private fun TestScope.preferences(path: okio.Path = tempFilePath("prefs.preferences_pb")) =
        PreferenceDataStoreFactory.createWithPath(
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            scope = backgroundScope,
            produceFile = { path },
        )

    @Test
    fun `a stored dump survives a round trip`() = runTest {
        val store = LastDumpStore(preferences())
        assertNull(store.dump.first())
        store.save(dump)
        assertEquals(dump, store.dump.first())
    }

    @Test
    fun `a damaged dump value is treated as no card and can be replaced`() = runTest {
        val prefs = preferences()
        prefs.edit { it[stringPreferencesKey("last_dump")] = "{ this is not json" }
        val store = LastDumpStore(prefs)

        assertNull(store.dump.first())
        store.save(dump)
        assertEquals(dump, store.dump.first())
    }

    @Test
    fun `a corrupt preferences file is replaced instead of failing every read`() = runTest {
        val path = tempFilePath("prefs.preferences_pb")
        FileSystem.SYSTEM.write(path) { writeUtf8("this is not a preferences file") }
        val store = LastDumpStore(preferences(path))

        assertNull(store.dump.first())
        store.save(dump)
        assertEquals(dump, store.dump.first())
    }

    @Test
    fun `settings default to auto detection and the brand colors`() = runTest {
        val repo = DataStoreSettingsRepository(preferences())
        assertEquals(AppSettings(), repo.settings.first())
    }

    @Test
    fun `settings are persisted`() = runTest {
        val repo = DataStoreSettingsRepository(preferences())
        repo.setSelection(ProfileSelection.Manual("uns"))
        repo.setThemeMode(ThemeMode.Dark)
        repo.setUseDynamicColor(true)
        assertEquals(AppSettings(ProfileSelection.Manual("uns"), ThemeMode.Dark, true), repo.settings.first())

        repo.setSelection(ProfileSelection.Auto)
        assertEquals(ProfileSelection.Auto, repo.settings.first().selection)
    }

    @Test
    fun `settings and the last dump share one file without disturbing each other`() = runTest {
        val prefs = preferences()
        val settings = DataStoreSettingsRepository(prefs)
        val store = LastDumpStore(prefs)

        settings.setThemeMode(ThemeMode.Light)
        store.save(dump)
        settings.setUseDynamicColor(true)

        assertEquals(dump, store.dump.first())
        assertEquals(AppSettings(themeMode = ThemeMode.Light, useDynamicColor = true), settings.settings.first())
    }

    @Test
    fun `selection codec tolerates garbage`() {
        assertEquals(ProfileSelection.Auto, decodeSelection(null))
        assertEquals(ProfileSelection.Auto, decodeSelection("manual:"))
        assertEquals(ProfileSelection.Auto, decodeSelection("whatever"))
        assertEquals(ProfileSelection.Manual("bg"), decodeSelection(encodeSelection(ProfileSelection.Manual("bg"))))
    }
}
