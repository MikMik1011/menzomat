package st.misa.menzomat.card.data.nfc

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import st.misa.menzomat.card.domain.model.NfcStatus
import st.misa.menzomat.card.domain.nfc.NfcReader
import st.misa.menzomat.card.domain.nfc.NfcTag
import st.misa.menzomat.core.domain.desfire.Transceiver

/**
 * Android's reader mode only works while an activity is in the foreground, so the activity calls
 * [enable] from `onResume` and [disable] from `onPause`.
 */
class AndroidNfcReader(context: Context) : NfcReader {
    private val adapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)

    private val _status = MutableStateFlow(currentStatus())
    override val status: StateFlow<NfcStatus> = _status.asStateFlow()

    private val _tags = MutableSharedFlow<NfcTag>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val tags: Flow<NfcTag> = _tags.asSharedFlow()

    /** Shared by every tag: one conversation with a card at a time. */
    private val ioLock = Mutex()

    fun enable(activity: Activity) {
        _status.value = currentStatus()
        val options = Bundle().apply { putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, PRESENCE_CHECK_MS) }
        adapter?.enableReaderMode(
            activity,
            { tag -> _tags.tryEmit(IsoDepTag(tag, ioLock)) },
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            options,
        )
    }

    fun disable(activity: Activity) {
        adapter?.disableReaderMode(activity)
    }

    private fun currentStatus(): NfcStatus = when {
        adapter == null -> NfcStatus.Unavailable
        !adapter.isEnabled -> NfcStatus.Disabled
        else -> NfcStatus.Ready
    }

    private companion object {
        const val PRESENCE_CHECK_MS = 250
    }
}

private class IsoDepTag(private val tag: Tag, private val lock: Mutex) : NfcTag {
    override val uid: String = tag.id.toHexString(HexFormat.UpperCase)

    override suspend fun <T> useTransceiver(block: suspend (Transceiver) -> T): T =
        withIsoDep(lock, open = { IsoDep.get(tag)?.let(::PlatformIsoDep) }, block)
}

private class PlatformIsoDep(private val isoDep: IsoDep) : IsoDepLink {
    override fun connect() = isoDep.connect()
    override fun setTimeout(millis: Int) {
        isoDep.timeout = millis
    }
    override fun transceive(apdu: ByteArray): ByteArray = isoDep.transceive(apdu)
    override fun close() = isoDep.close()
}
