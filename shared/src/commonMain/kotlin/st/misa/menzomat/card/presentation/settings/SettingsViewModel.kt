package st.misa.menzomat.card.presentation.settings

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import st.misa.menzomat.card.domain.model.AppSettings
import st.misa.menzomat.card.domain.model.ProfileSelection
import st.misa.menzomat.card.domain.model.ThemeMode
import st.misa.menzomat.card.domain.profile.ProfileRegistry
import st.misa.menzomat.card.domain.repository.SettingsRepository

data class ProfileOption(val id: String, val name: String)

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val profiles: List<ProfileOption> = emptyList(),
)

@Stable
interface SettingsActions {
    fun onSelectProfile(selection: ProfileSelection)
    fun onSelectTheme(mode: ThemeMode)
    fun onToggleDynamicColor(enabled: Boolean)
}

object NoSettingsActions : SettingsActions {
    override fun onSelectProfile(selection: ProfileSelection) = Unit
    override fun onSelectTheme(mode: ThemeMode) = Unit
    override fun onToggleDynamicColor(enabled: Boolean) = Unit
}

class SettingsViewModel(
    private val repository: SettingsRepository,
    registry: ProfileRegistry,
) : ViewModel(), SettingsActions {

    private val profiles = registry.all().map { ProfileOption(it.id, it.displayName) }

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

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
