package st.misa.menzomat.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okio.Path.Companion.toPath
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import st.misa.menzomat.card.data.DefaultCardRepository
import st.misa.menzomat.card.data.local.LastDumpStore
import st.misa.menzomat.card.data.preferences.DataStoreSettingsRepository
import st.misa.menzomat.card.data.profile.novisad.NoviSadProfile
import st.misa.menzomat.card.domain.profile.CardProfile
import st.misa.menzomat.card.domain.profile.ProfileRegistry
import st.misa.menzomat.card.domain.profile.ProfileResolver
import st.misa.menzomat.card.domain.repository.CardRepository
import st.misa.menzomat.card.domain.repository.SettingsRepository
import st.misa.menzomat.card.presentation.card.CardViewModel
import st.misa.menzomat.card.presentation.settings.SettingsViewModel

/** Where the preferences file lives; each platform module provides it. */
class PreferencesPath(val value: String)

/** Qualifier of the IO dispatcher, which common code cannot name; each platform module provides it. */
internal val IO = named("io")
private val STORAGE_SCOPE = named("storage")

/**
 * Everything that does not depend on the platform. A platform module must additionally provide
 * a [PreferencesPath], a [CoroutineDispatcher] named [IO] and a [st.misa.menzomat.card.domain.nfc.NfcReader].
 */
val sharedModule = module {
    single<CoroutineScope>(STORAGE_SCOPE) { CoroutineScope(SupervisorJob() + get<CoroutineDispatcher>(IO)) }

    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath(
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            scope = get(STORAGE_SCOPE),
            produceFile = { get<PreferencesPath>().value.toPath() },
        )
    }
    single { LastDumpStore(get()) }
    single<SettingsRepository> { DataStoreSettingsRepository(get()) }
    single<CardRepository> { DefaultCardRepository(get(), get(), get(IO)) }

    // Every supported university is one line here; detection and the profile picker pick it up from the list.
    single<List<CardProfile>> { listOf(NoviSadProfile()) }
    single { ProfileRegistry(get<List<CardProfile>>()) }
    single { ProfileResolver(get()) }

    viewModel { CardViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
}
