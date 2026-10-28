package st.misa.menzomat.card.presentation.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import st.misa.menzomat.card.domain.model.CardReadError
import st.misa.menzomat.card.domain.model.ProfileSelection
import st.misa.menzomat.card.domain.model.ScanEvent
import st.misa.menzomat.card.domain.profile.CardProfile
import st.misa.menzomat.card.domain.profile.ProfileResolver
import st.misa.menzomat.card.domain.profile.Resolution
import st.misa.menzomat.card.domain.profile.Scored
import st.misa.menzomat.card.domain.repository.CardRepository
import st.misa.menzomat.card.domain.repository.SettingsRepository
import st.misa.menzomat.core.domain.desfire.CardDump

class CardViewModel(
    repository: CardRepository,
    private val settings: SettingsRepository,
    private val resolver: ProfileResolver,
) : ViewModel(), CardActions {

    private val isReading = MutableStateFlow(false)
    private val error = MutableStateFlow<CardReadError?>(null)

    private val _effects = Channel<CardEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /** The profile is resolved here so that switching it re-decodes the stored dump without another tap. */
    val uiState: StateFlow<CardUiState> = combine(
        repository.nfcStatus,
        repository.lastDump,
        settings.settings.map { it.selection }.distinctUntilChanged(),
        isReading,
        error,
    ) { nfc, dump, selection, reading, readError ->
        CardUiState(
            isLoaded = true,
            nfcStatus = nfc,
            isReading = reading,
            error = readError,
            selection = selection,
            content = dump?.let { contentFor(it, selection) } ?: CardContent.Empty,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CardUiState())

    init {
        viewModelScope.launch {
            repository.scans.collect { event ->
                when (event) {
                    ScanEvent.Started -> {
                        isReading.value = true
                        error.value = null
                    }
                    is ScanEvent.Completed -> isReading.value = false
                    is ScanEvent.Failed -> {
                        isReading.value = false
                        error.value = event.error
                    }
                }
            }
        }
    }

    override fun onSelectProfile(selection: ProfileSelection) {
        viewModelScope.launch { settings.setSelection(selection) }
    }

    override fun onOpenNfcSettings() {
        _effects.trySend(CardEffect.OpenNfcSettings)
    }

    override fun onShareDump() {
        currentDump()?.let { _effects.trySend(CardEffect.ShareText(it.toJson())) }
    }

    override fun onCopyDump() {
        currentDump()?.let { _effects.trySend(CardEffect.CopyText(it.toJson())) }
    }

    override fun onSaveDump() {
        currentDump()?.let { _effects.trySend(CardEffect.SaveText(dumpFileName(it), it.toJson())) }
    }

    override fun onDismissError() {
        error.update { null }
    }

    private fun currentDump(): CardDump? = when (val content = uiState.value.content) {
        is CardContent.Decoded -> content.dump
        is CardContent.Unrecognized -> content.dump
        CardContent.Empty -> null
    }

    private fun CardDump.toJson(): String = json.encodeToString(CardDump.serializer(), this)

    private fun contentFor(dump: CardDump, selection: ProfileSelection): CardContent =
        when (val resolution = resolver.resolve(dump, selection)) {
            is Resolution.Resolved -> CardContent.Decoded(
                card = resolution.profile.decode(dump),
                dump = dump,
                profile = resolution.profile.toInfo(),
                isAuto = resolution.isAuto,
                isLowConfidence = resolution.isLowConfidence,
                candidates = resolution.candidates.map { it.toCandidate() },
            )
            is Resolution.Unrecognized -> CardContent.Unrecognized(dump, resolution.candidates.map { it.toCandidate() })
        }

    private fun Scored.toCandidate() = ProfileCandidate(profile.toInfo(), score)

    private fun CardProfile.toInfo() = ProfileInfo(id, displayName, shortName)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        val json = Json { prettyPrint = true }
    }
}
