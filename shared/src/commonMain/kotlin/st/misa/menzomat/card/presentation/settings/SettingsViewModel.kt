package st.misa.menzomat.card.presentation.settings

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import st.misa.menzomat.card.domain.model.AppSettings
import st.misa.menzomat.card.domain.model.ProfileSelection
import st.misa.menzomat.card.domain.model.ThemeMode
import st.misa.menzomat.card.domain.profile.ProfileRegistry
import st.misa.menzomat.card.domain.repository.CardRepository
import st.misa.menzomat.card.domain.repository.SettingsRepository
import st.misa.menzomat.card.presentation.card.dumpFileName
import st.misa.menzomat.core.domain.desfire.toJson
import st.misa.menzomat.ui.platform.TextAttachment

data class ProfileOption(val id: String, val name: String)

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val profiles: List<ProfileOption> = emptyList(),
)

sealed interface SettingsEffect {
    data class ReportBug(val attachment: TextAttachment?) : SettingsEffect
}

@Stable
interface SettingsActions {
    fun onSelectProfile(selection: ProfileSelection)
    fun onSelectTheme(mode: ThemeMode)
    fun onToggleDynamicColor(enabled: Boolean)
    fun onReportBug()
}

object NoSettingsActions : SettingsActions {
    override fun onSelectProfile(selection: ProfileSelection) = Unit
    override fun onSelectTheme(mode: ThemeMode) = Unit
    override fun onToggleDynamicColor(enabled: Boolean) = Unit
    override fun onReportBug() = Unit
}

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val cards: CardRepository,
    registry: ProfileRegistry,
) : ViewModel(), SettingsActions {

    private val profiles = registry.all().map { ProfileOption(it.id, it.displayName) }

    private val _effects = Channel<SettingsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    val uiState: StateFlow<SettingsUiState> = repository.settings
        .map { SettingsUiState(it, profiles) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), SettingsUiState(profiles = profiles))

    override fun onSelectProfile(selection: ProfileSelection) {
        viewModelScope.launch { repository.setSelection(selection) }
    }

    override fun onSelectTheme(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    override fun onToggleDynamicColor(enabled: Boolean) {
        viewModelScope.launch { repository.setUseDynamicColor(enabled) }
    }

    override fun onReportBug() {
        viewModelScope.launch {
            val attachment = cards.lastDump.first()?.let { TextAttachment(dumpFileName(it), it.toJson(), JSON_MIME_TYPE) }
            _effects.send(SettingsEffect.ReportBug(attachment))
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        const val JSON_MIME_TYPE = "application/json"
    }
}
