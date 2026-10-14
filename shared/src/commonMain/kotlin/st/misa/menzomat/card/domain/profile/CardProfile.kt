package st.misa.menzomat.card.domain.profile

import st.misa.menzomat.card.domain.model.StudentCard
import st.misa.menzomat.core.domain.desfire.CardDump

/**
 * Knows how one university lays out its card. Pure: no I/O, so the same [CardDump] can be
 * scored and decoded by any number of profiles.
 */
interface CardProfile {
    /** Stable identifier; persisted in settings. */
    val id: String
    val displayName: String
    val shortName: String

    /** 0 means "not this card"; higher is a more confident match (max 100). */
    fun score(dump: CardDump): Int

    /** Tolerant: never throws on unexpected bytes. A bad field becomes null plus a warning. */
    fun decode(dump: CardDump): StudentCard
}
