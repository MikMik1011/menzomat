package st.misa.menzomat.card.domain.profile

class ProfileRegistry(profiles: Collection<CardProfile>) {
    private val profiles: List<CardProfile> = profiles.sortedBy { it.displayName }

    fun all(): List<CardProfile> = profiles

    fun byId(id: String): CardProfile? = profiles.firstOrNull { it.id == id }
}
