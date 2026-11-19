package st.misa.menzomat.card.presentation.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import st.misa.menzomat.card.domain.model.AppSettings
import st.misa.menzomat.card.domain.model.NfcStatus
import st.misa.menzomat.card.domain.model.ProfileSelection
import st.misa.menzomat.card.domain.model.ScanEvent
import st.misa.menzomat.card.domain.model.ThemeMode
import st.misa.menzomat.card.domain.profile.ProfileRegistry
import st.misa.menzomat.card.domain.repository.CardRepository
import st.misa.menzomat.card.domain.repository.SettingsRepository
import st.misa.menzomat.card.presentation.card.dumpFileName
import st.misa.menzomat.core.domain.desfire.Aid
import st.misa.menzomat.core.domain.desfire.AppDump
import st.misa.menzomat.core.domain.desfire.CardDump
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private class FakeCards : CardRepository {
        override val nfcStatus: StateFlow<NfcStatus> = MutableStateFlow(NfcStatus.Ready)
        override val scans: Flow<ScanEvent> = MutableSharedFlow()
        val dump = MutableStateFlow<CardDump?>(null)
        override val lastDump: Flow<CardDump?> = dump
        override suspend fun clearLastDump() { dump.value = null }
    }

    private class FakeSettings : SettingsRepository {
        val state = MutableStateFlow(AppSettings())
        override val settings: Flow<AppSettings> = state
        override suspend fun setSelection(selection: ProfileSelection) = state.update { it.copy(selection = selection) }
        override suspend fun setThemeMode(mode: ThemeMode) = state.update { it.copy(themeMode = mode) }
        override suspend fun setUseDynamicColor(enabled: Boolean) = state.update { it.copy(useDynamicColor = enabled) }
    }

    private val cards = FakeCards()
    private val dump = CardDump("04AA", 0, listOf(AppDump(Aid(13))))

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = SettingsViewModel(FakeSettings(), cards, ProfileRegistry(emptyList()))

    @Test
    fun `reporting a bug attaches the last card read as a json file`() = runTest {
        cards.dump.value = dump
        val vm = viewModel()

        vm.onReportBug()
        val effect = assertIs<SettingsEffect.ReportBug>(vm.effects.first())
        val attachment = assertNotNull(effect.attachment)
        assertEquals(dumpFileName(dump), attachment.fileName)
        assertEquals("application/json", attachment.mimeType)
        assertTrue("04AA" in attachment.text, attachment.text)
    }

    @Test
    fun `reporting a bug before any card was read has nothing to attach`() = runTest {
        val vm = viewModel()

        vm.onReportBug()
        val effect = assertIs<SettingsEffect.ReportBug>(vm.effects.first())
        assertNull(effect.attachment)
    }
}
