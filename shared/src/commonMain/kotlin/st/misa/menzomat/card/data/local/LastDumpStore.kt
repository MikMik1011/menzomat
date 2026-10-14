package st.misa.menzomat.card.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import okio.IOException
import st.misa.menzomat.core.domain.desfire.CardDump

/** The last card that was read, kept as JSON in the preferences file so it can be shown offline. */
internal class LastDumpStore(private val store: DataStore<Preferences>) {
    val dump: Flow<CardDump?> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY]?.let(::decode) }

    suspend fun save(dump: CardDump) {
        store.edit { it[KEY] = json.encodeToString(CardDump.serializer(), dump) }
    }

    // A value written by an older version, or damaged, is treated as "no card yet" rather than an error.
    private fun decode(text: String): CardDump? = try {
        json.decodeFromString(CardDump.serializer(), text)
    } catch (e: IllegalArgumentException) {
        null
    }

    private companion object {
        val KEY = stringPreferencesKey("last_dump")
        val json = Json { ignoreUnknownKeys = true }
    }
}
