package st.misa.menzomat.card.data.nfc

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.IOException
import st.misa.menzomat.card.domain.nfc.TagLostException
import st.misa.menzomat.card.domain.nfc.UnsupportedTagException
import st.misa.menzomat.core.domain.desfire.Transceiver

/** The part of `android.nfc.tech.IsoDep` this reader uses, so the connection rules can be tested without Android. */
internal interface IsoDepLink {
    fun connect()
    fun setTimeout(millis: Int)
    fun transceive(apdu: ByteArray): ByteArray
    fun close()
}

private const val TIMEOUT_MS = 5_000

/** Text of the exception `BasicTagTechnology.connect()` throws when its `Tag` still counts as connected. */
private const val ALREADY_CONNECTED = "Only one TagTechnology can be connected at a time"

private const val LOG_TAG = "Menzomat"

/**
 * Connects, runs [block], and always closes. [lock] makes sure only one read talks to the card at a time,
 * because Android allows a single connection per `Tag`.
 *
 * Should the `Tag` still count as connected anyway, the connection is used as it is. Android's `connect()`
 * has reached the card by the time it complains, `transceive()` only needs the tag to be marked connected,
 * and the `close()` at the end clears the mark. That keeps a stale connection from failing a good card.
 */
internal suspend fun <T> withIsoDep(
    lock: Mutex,
    open: () -> IsoDepLink?,
    block: suspend (Transceiver) -> T,
): T = lock.withLock {
    val link = open() ?: throw UnsupportedTagException()
    connect(link)
    try {
        link.setTimeout(TIMEOUT_MS)
        block { apdu -> translatingNfcErrors { link.transceive(apdu) } }
    } finally {
        runCatching { link.close() }
    }
}

private fun connect(link: IsoDepLink) {
    try {
        translatingNfcErrors { link.connect() }
    } catch (e: IOException) {
        if (e.message?.startsWith(ALREADY_CONNECTED) != true) throw e
        Log.w(LOG_TAG, "Tag was still marked connected; using the existing connection")
    }
}

/**
 * Android reports a vanished card as its own exception, or as a SecurityException for a stale tag, and
 * complains about its own bookkeeping with IllegalStateException. None of them may escape as a crash.
 */
internal inline fun <T> translatingNfcErrors(action: () -> T): T = try {
    action()
} catch (e: android.nfc.TagLostException) {
    throw TagLostException(e.message)
} catch (e: SecurityException) {
    throw TagLostException(e.message)
} catch (e: IllegalStateException) {
    throw IOException(e.message)
}
