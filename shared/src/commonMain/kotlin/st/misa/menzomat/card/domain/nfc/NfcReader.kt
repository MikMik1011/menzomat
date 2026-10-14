package st.misa.menzomat.card.domain.nfc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import st.misa.menzomat.card.domain.model.NfcStatus

/**
 * The card reader of the device. How scanning starts and stops is up to the platform binding
 * (Android listens while the app is in the foreground), so none of that appears here.
 */
interface NfcReader {
    val status: StateFlow<NfcStatus>

    /** Emits once per card presented. */
    val tags: Flow<NfcTag>
}
