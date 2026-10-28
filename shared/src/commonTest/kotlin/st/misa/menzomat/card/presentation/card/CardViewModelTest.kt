package st.misa.menzomat.card.presentation.card

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import st.misa.menzomat.card.domain.model.AppSettings
import st.misa.menzomat.card.domain.model.CardReadError
import st.misa.menzomat.card.domain.model.NfcStatus
import st.misa.menzomat.card.domain.model.ProfileSelection
import st.misa.menzomat.card.domain.model.ScanEvent
import st.misa.menzomat.card.domain.model.StudentCard
import st.misa.menzomat.card.domain.model.ThemeMode
import st.misa.menzomat.card.domain.profile.CardProfile
import st.misa.menzomat.card.domain.profile.ProfileRegistry
import st.misa.menzomat.card.domain.profile.ProfileResolver
import st.misa.menzomat.card.domain.repository.CardRepository
import st.misa.menzomat.card.domain.repository.SettingsRepository
import st.misa.menzomat.core.domain.desfire.Aid
import st.misa.menzomat.core.domain.desfire.AppDump
import st.misa.menzomat.core.domain.desfire.CardDump
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CardViewModelTest {

    private class FakeRepository : CardRepository {
        override val nfcStatus: StateFlow<NfcStatus> = MutableStateFlow(NfcStatus.Ready)
        val scanEvents = MutableSharedFlow<ScanEvent>()
        override val scans: Flow<ScanEvent> = scanEvents
        val dump = MutableStateFlow<CardDump?>(null)
        override val lastDump: Flow<CardDump?> = dump
    }

    private class FakeSettings : SettingsRepository {
        val state = MutableStateFlow(AppSettings())
        override val settings: Flow<AppSettings> = state
        override suspend fun setSelection(selection: ProfileSelection) = state.update { it.copy(selection = selection) }
        override suspend fun setThemeMode(mode: ThemeMode) = state.update { it.copy(themeMode = mode) }
        override suspend fun setUseDynamicColor(enabled: Boolean) = state.update { it.copy(useDynamicColor = enabled) }
    }

    private class StubProfile(override val id: String, private val score: Int) : CardProfile {
        override val displayName = "University $id"
        override val shortName = id.uppercase()
        override fun score(dump: CardDump) = score
        override fun decode(dump: CardDump) = StudentCard(cardNumber = "decoded-by-$id")
    }

    private val repository = FakeRepository()
    private val settings = FakeSettings()
    private val dump = CardDump("04AA", 0, listOf(AppDump(Aid(13))))

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(vararg profiles: CardProfile) =
        CardViewModel(repository, settings, ProfileResolver(ProfileRegistry(profiles.toList())))

    private fun kotlinx.coroutines.test.TestScope.states(vm: CardViewModel): List<CardUiState> {
        val seen = mutableListOf<CardUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect { seen += it } }
        return seen
    }

    @Test
    fun `without a stored card the screen is empty but loaded`() = runTest {
        val seen = states(viewModel(StubProfile("uns", 100)))
        assertEquals(CardContent.Empty, seen.last().content)
        assertTrue(seen.last().isLoaded)
    }

    @Test
    fun `a stored card is decoded by the best profile`() = runTest {
        repository.dump.value = dump
        val seen = states(viewModel(StubProfile("uns", 100), StubProfile("bg", 40)))

        val content = assertIs<CardContent.Decoded>(seen.last().content)
        assertEquals("uns", content.profile.id)
        assertTrue(content.isAuto)
        assertEquals("decoded-by-uns", content.card.cardNumber)
        assertEquals(listOf("uns", "bg"), content.candidates.map { it.profile.id })
    }

    @Test
    fun `switching profile re-decodes the stored card without a new read`() = runTest {
        repository.dump.value = dump
        val vm = viewModel(StubProfile("uns", 100), StubProfile("bg", 40))
        val seen = states(vm)

        vm.onSelectProfile(ProfileSelection.Manual("bg"))

        val content = assertIs<CardContent.Decoded>(seen.last().content)
        assertEquals("bg", content.profile.id)
        assertEquals(false, content.isAuto)
        assertEquals("decoded-by-bg", content.card.cardNumber)
        assertEquals(ProfileSelection.Manual("bg"), settings.state.value.selection)

        vm.onSelectProfile(ProfileSelection.Auto)
        assertEquals("uns", assertIs<CardContent.Decoded>(seen.last().content).profile.id)
    }

    @Test
    fun `a card no profile recognizes keeps its raw dump`() = runTest {
        repository.dump.value = dump
        val seen = states(viewModel(StubProfile("uns", 0)))
        val content = assertIs<CardContent.Unrecognized>(seen.last().content)
        assertEquals(dump, content.dump)
    }

    @Test
    fun `reading and failure are reflected and can be dismissed`() = runTest {
        val vm = viewModel(StubProfile("uns", 100))
        val seen = states(vm)
        // Let the init collector subscribe before emitting.
        repository.scanEvents.subscriptionCount.first { it > 0 }

        repository.scanEvents.emit(ScanEvent.Started)
        assertTrue(seen.last().isReading)
        assertNull(seen.last().error)

        repository.scanEvents.emit(ScanEvent.Failed(CardReadError.TagLost))
        assertEquals(false, seen.last().isReading)
        assertEquals(CardReadError.TagLost, seen.last().error)

        vm.onDismissError()
        assertNull(seen.last().error)

        repository.scanEvents.emit(ScanEvent.Started)
        repository.scanEvents.emit(ScanEvent.Completed(dump))
        assertEquals(false, seen.last().isReading)
    }

    @Test
    fun `starting a new read clears the previous error`() = runTest {
        val vm = viewModel(StubProfile("uns", 100))
        val seen = states(vm)
        repository.scanEvents.subscriptionCount.first { it > 0 }

        repository.scanEvents.emit(ScanEvent.Failed(CardReadError.NotDesfire))
        assertEquals(CardReadError.NotDesfire, seen.last().error)
        repository.scanEvents.emit(ScanEvent.Started)
        assertNull(seen.last().error)
    }

    @Test
    fun `sharing exports the raw dump as json`() = runTest {
        repository.dump.value = dump
        val vm = viewModel(StubProfile("uns", 100))
        states(vm)

        vm.onShareDump()
        val effect = assertIs<CardEffect.ShareText>(vm.effects.first())
        assertTrue("04AA" in effect.text, effect.text)
    }

    @Test
    fun `saving offers the raw dump as a named json file`() = runTest {
        repository.dump.value = dump
        val vm = viewModel(StubProfile("uns", 100))
        states(vm)

        vm.onSaveDump()
        val effect = assertIs<CardEffect.SaveText>(vm.effects.first())
        assertEquals(dumpFileName(dump), effect.fileName)
        assertTrue(effect.fileName.endsWith(".json"), effect.fileName)
        assertTrue("04AA" in effect.text, effect.text)
    }

    @Test
    fun `there is nothing to export before a card was read`() = runTest {
        val vm = viewModel(StubProfile("uns", 100))
        states(vm)
        vm.onCopyDump()
        vm.onSaveDump()
        vm.onOpenNfcSettings()
        assertEquals(CardEffect.OpenNfcSettings, vm.effects.first())
    }
}
