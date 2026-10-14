package st.misa.menzomat.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import st.misa.menzomat.card.domain.model.NfcStatus
import st.misa.menzomat.card.domain.nfc.NfcReader
import st.misa.menzomat.card.domain.nfc.NfcTag
import st.misa.menzomat.core.domain.desfire.Aid
import st.misa.menzomat.core.domain.desfire.Transceiver

class FakeNfcReader : NfcReader {
    override val status: StateFlow<NfcStatus> = MutableStateFlow(NfcStatus.Ready)
    val tagFlow = MutableSharedFlow<NfcTag>()
    override val tags: Flow<NfcTag> = tagFlow
}

class FakeTag(
    override val uid: String = "04AABBCC",
    private val behaviour: suspend (suspend (Transceiver) -> Any?) -> Any?,
) : NfcTag {
    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> useTransceiver(block: suspend (Transceiver) -> T): T =
        behaviour { block(it) } as T

    companion object {
        fun withCard(card: Transceiver) = FakeTag { run -> run(card) }
        fun failing(error: Throwable) = FakeTag { throw error }
    }
}

val canteenAid = Aid(0x00000D)

fun canteenCard(): FakeDesfireCard = FakeDesfireCard(
    mapOf(
        Aid.PICC to emptyMap(),
        canteenAid to mapOf(1 to FakeDesfireCard.FakeFile.Data(type = 1, bytes = byteArrayOf(2, 1, 0x1F))),
    ),
)
