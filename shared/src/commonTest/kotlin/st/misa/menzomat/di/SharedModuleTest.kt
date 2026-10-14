package st.misa.menzomat.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import st.misa.menzomat.card.domain.nfc.NfcReader
import st.misa.menzomat.card.domain.profile.CardProfile
import st.misa.menzomat.card.domain.profile.ProfileRegistry
import st.misa.menzomat.card.domain.profile.ProfileResolver
import st.misa.menzomat.card.domain.repository.CardRepository
import st.misa.menzomat.card.domain.repository.SettingsRepository
import st.misa.menzomat.card.presentation.card.CardViewModel
import st.misa.menzomat.card.presentation.settings.SettingsViewModel
import st.misa.menzomat.fake.FakeNfcReader
import st.misa.menzomat.fake.tempFilePath
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** What each platform module has to provide, faked, so the shared wiring is checked without a device. */
@OptIn(ExperimentalCoroutinesApi::class)
class SharedModuleTest {
    private val fakePlatform = module {
        single<CoroutineDispatcher>(IO) { Dispatchers.Default }
        single { PreferencesPath(tempFilePath("wiring.preferences_pb").toString()) }
        single<NfcReader> { FakeNfcReader() }
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `everything the app asks for can be created`() {
        val koin = koinApplication { modules(sharedModule, fakePlatform) }.koin

        koin.get<CardRepository>()
        koin.get<SettingsRepository>()
        koin.get<ProfileRegistry>()
        koin.get<ProfileResolver>()
        koin.get<CardViewModel>()
        koin.get<SettingsViewModel>()
    }

    @Test
    fun `the supported universities are registered`() {
        val koin = koinApplication { modules(sharedModule, fakePlatform) }.koin
        val ids = koin.get<List<CardProfile>>().map { it.id }
        assertEquals(listOf("uns"), ids)
        assertEquals(ids, koin.get<ProfileRegistry>().all().map { it.id })
    }
}
