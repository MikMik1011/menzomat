package st.misa.menzomat.card.domain.model

sealed interface ProfileSelection {
    data object Auto : ProfileSelection
    data class Manual(val profileId: String) : ProfileSelection
}
