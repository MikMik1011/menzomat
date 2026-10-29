package st.misa.menzomat.card.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext
import st.misa.menzomat.card.data.local.LastDumpStore
import st.misa.menzomat.card.domain.model.CardReadError
import st.misa.menzomat.card.domain.model.NfcStatus
import st.misa.menzomat.card.domain.model.ScanEvent
import st.misa.menzomat.card.domain.nfc.NfcReader
import st.misa.menzomat.card.domain.nfc.NfcTag
import st.misa.menzomat.card.domain.nfc.TagLostException
import st.misa.menzomat.card.domain.nfc.UnsupportedTagException
import st.misa.menzomat.card.domain.repository.CardRepository
import st.misa.menzomat.core.data.desfire.CardDumper
import st.misa.menzomat.core.data.desfire.IsoDesfireClient
import st.misa.menzomat.core.domain.desfire.CardDump
import st.misa.menzomat.core.domain.desfire.DesfireError
import okio.IOException
import kotlin.coroutines.cancellation.CancellationException

internal class DefaultCardRepository(
    private val nfcReader: NfcReader,
    private val lastDumpStore: LastDumpStore,
    private val io: CoroutineDispatcher,
) : CardRepository {

    override val nfcStatus: StateFlow<NfcStatus> = nfcReader.status

    override val lastDump: Flow<CardDump?> = lastDumpStore.dump

    // A new card cancels a read still in progress for the previous one.
    @OptIn(ExperimentalCoroutinesApi::class)
    override val scans: Flow<ScanEvent> = nfcReader.tags.transformLatest { tag ->
        emit(ScanEvent.Started)
        emit(read(tag))
    }

    override suspend fun clearLastDump() = lastDumpStore.clear()

    private suspend fun read(tag: NfcTag): ScanEvent = try {
        val dump = withContext(io) { tag.useTransceiver { CardDumper(IsoDesfireClient(it)).dump(tag.uid) } }
        if (dump.apps.isEmpty()) {
            ScanEvent.Failed(CardReadError.NoApplications)
        } else {
            lastDumpStore.save(dump)
            ScanEvent.Completed(dump)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: UnsupportedTagException) {
        ScanEvent.Failed(CardReadError.NotDesfire)
    } catch (e: TagLostException) {
        ScanEvent.Failed(CardReadError.TagLost)
    } catch (e: DesfireError.Malformed) {
        ScanEvent.Failed(CardReadError.NotDesfire)
    } catch (e: DesfireError) {
        ScanEvent.Failed(CardReadError.Communication(e.message))
    } catch (e: IOException) {
        ScanEvent.Failed(CardReadError.Communication(e.message))
    }
}
