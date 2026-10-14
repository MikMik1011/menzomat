package st.misa.menzomat.card.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import st.misa.menzomat.card.domain.model.NfcStatus
import st.misa.menzomat.card.domain.model.ScanEvent
import st.misa.menzomat.core.domain.desfire.CardDump

/**
 * Error boundary of the data layer: NFC, DESFire and storage exceptions are turned into
 * [ScanEvent.Failed] here and never reach a ViewModel.
 */
interface CardRepository {
    val nfcStatus: StateFlow<NfcStatus>

    /** One [ScanEvent.Started] then a [ScanEvent.Completed] or [ScanEvent.Failed] per card presented. */
    val scans: Flow<ScanEvent>

    /** The last successfully read card, kept for the offline view. Null if none yet. */
    val lastDump: Flow<CardDump?>
}
