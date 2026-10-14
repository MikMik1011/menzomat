package st.misa.menzomat.card.domain.profile

import st.misa.menzomat.card.domain.model.ProfileSelection
import st.misa.menzomat.core.domain.desfire.CardDump

data class Scored(val profile: CardProfile, val score: Int)

sealed interface Resolution {
    /** Every registered profile with its score for this card, best first. */
    val candidates: List<Scored>

    data class Resolved(
        val profile: CardProfile,
        /** True when the profile was picked by detection rather than by the user. */
        val isAuto: Boolean,
        val score: Int,
        override val candidates: List<Scored>,
    ) : Resolution {
        /** The user forced a profile the card does not look like. */
        val isLowConfidence: Boolean get() = score == 0
    }

    data class Unrecognized(override val candidates: List<Scored>) : Resolution
}

class ProfileResolver(private val registry: ProfileRegistry) {

    fun resolve(dump: CardDump, selection: ProfileSelection): Resolution {
        val candidates = registry.all()
            .map { Scored(it, it.score(dump)) }
            .sortedByDescending { it.score }

        if (selection is ProfileSelection.Manual) {
            candidates.firstOrNull { it.profile.id == selection.profileId }?.let {
                return Resolution.Resolved(it.profile, isAuto = false, score = it.score, candidates = candidates)
            }
        }
        val best = candidates.firstOrNull()?.takeIf { it.score > 0 }
            ?: return Resolution.Unrecognized(candidates)
        return Resolution.Resolved(best.profile, isAuto = true, score = best.score, candidates = candidates)
    }
}
