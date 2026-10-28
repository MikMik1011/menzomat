package st.misa.menzomat.card.presentation.card

import st.misa.menzomat.card.domain.model.CardReadError
import st.misa.menzomat.card.domain.model.NfcStatus
import st.misa.menzomat.card.domain.model.ProfileSelection
import st.misa.menzomat.card.domain.model.StudentCard
import st.misa.menzomat.core.domain.desfire.CardDump

data class ProfileInfo(val id: String, val name: String, val shortName: String)

data class ProfileCandidate(val profile: ProfileInfo, val score: Int)

sealed interface CardContent {
    data object Empty : CardContent

    data class Decoded(
        val card: StudentCard,
        val dump: CardDump,
        val profile: ProfileInfo,
        val isAuto: Boolean,
        /** The user forced a profile this card does not look like. */
        val isLowConfidence: Boolean,
        val candidates: List<ProfileCandidate>,
    ) : CardContent

    data class Unrecognized(
        val dump: CardDump,
        val candidates: List<ProfileCandidate>,
    ) : CardContent
}

data class CardUiState(
    val isLoaded: Boolean = false,
    val nfcStatus: NfcStatus = NfcStatus.Ready,
    val isReading: Boolean = false,
    val error: CardReadError? = null,
    val selection: ProfileSelection = ProfileSelection.Auto,
    val content: CardContent = CardContent.Empty,
)

sealed interface CardEffect {
    data object OpenNfcSettings : CardEffect
    data class ShareText(val text: String) : CardEffect
    data class CopyText(val text: String) : CardEffect
    data class SaveText(val fileName: String, val text: String) : CardEffect
}
