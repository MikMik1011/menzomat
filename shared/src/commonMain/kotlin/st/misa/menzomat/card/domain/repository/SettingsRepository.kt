package st.misa.menzomat.card.domain.repository

import kotlinx.coroutines.flow.Flow
import st.misa.menzomat.card.domain.model.AppSettings
import st.misa.menzomat.card.domain.model.ProfileSelection
import st.misa.menzomat.card.domain.model.ThemeMode

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setSelection(selection: ProfileSelection)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setUseDynamicColor(enabled: Boolean)
}
