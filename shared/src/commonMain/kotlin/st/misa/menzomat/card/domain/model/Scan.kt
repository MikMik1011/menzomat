package st.misa.menzomat.card.domain.model

import st.misa.menzomat.core.domain.desfire.CardDump

enum class NfcStatus { Unavailable, Disabled, Ready }

sealed interface CardReadError {
    data object TagLost : CardReadError
    data object NotDesfire : CardReadError
    data object NoApplications : CardReadError
    data class Communication(val detail: String?) : CardReadError
}

sealed interface ScanEvent {
    data object Started : ScanEvent
    data class Completed(val dump: CardDump) : ScanEvent
    data class Failed(val error: CardReadError) : ScanEvent
}
