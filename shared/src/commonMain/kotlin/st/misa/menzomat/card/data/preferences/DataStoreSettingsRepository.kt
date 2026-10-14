package st.misa.menzomat.card.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import st.misa.menzomat.card.domain.model.AppSettings
import st.misa.menzomat.card.domain.model.ProfileSelection
import st.misa.menzomat.card.domain.model.ThemeMode
import st.misa.menzomat.card.domain.repository.SettingsRepository
import okio.IOException

internal class DataStoreSettingsRepository(
    private val store: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<AppSettings> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            AppSettings(
                selection = decodeSelection(prefs[SELECTION]),
                themeMode = prefs[THEME]?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
                    ?: ThemeMode.System,
                useDynamicColor = prefs[DYNAMIC_COLOR] ?: false,
            )
        }

    override suspend fun setSelection(selection: ProfileSelection) {
        store.edit { it[SELECTION] = encodeSelection(selection) }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        store.edit { it[THEME] = mode.name }
    }

    override suspend fun setUseDynamicColor(enabled: Boolean) {
        store.edit { it[DYNAMIC_COLOR] = enabled }
    }

    private companion object {
        val SELECTION = stringPreferencesKey("profile_selection")
        val THEME = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    }
}

private const val AUTO = "auto"
private const val MANUAL_PREFIX = "manual:"

internal fun encodeSelection(selection: ProfileSelection): String = when (selection) {
    ProfileSelection.Auto -> AUTO
    is ProfileSelection.Manual -> MANUAL_PREFIX + selection.profileId
}

internal fun decodeSelection(stored: String?): ProfileSelection = when {
    stored != null && stored.startsWith(MANUAL_PREFIX) && stored.length > MANUAL_PREFIX.length ->
        ProfileSelection.Manual(stored.removePrefix(MANUAL_PREFIX))
    else -> ProfileSelection.Auto
}
