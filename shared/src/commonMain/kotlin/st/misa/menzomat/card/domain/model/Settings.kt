package st.misa.menzomat.card.domain.model

enum class ThemeMode { System, Light, Dark }

data class AppSettings(
    val selection: ProfileSelection = ProfileSelection.Auto,
    val themeMode: ThemeMode = ThemeMode.System,
    /** Off by default so the ISIC brand colors are used. */
    val useDynamicColor: Boolean = false,
)
