package st.misa.menzomat.card.domain.nfc

import okio.IOException
import st.misa.menzomat.core.domain.desfire.Transceiver

/** A card in the field. Abstracts the platform's tag type so everything above this interface is testable on the JVM. */
interface NfcTag {
    /** Hex uid, upper case. */
    val uid: String

    /**
     * Connects, runs [block] against the card, and disconnects. Blocks the calling thread on I/O,
     * so call it from an IO dispatcher.
     *
     * @throws UnsupportedTagException if the tag does not speak ISO-DEP.
     * @throws TagLostException if the tag leaves the field mid read.
     * @throws IOException if the connection fails otherwise.
     */
    suspend fun <T> useTransceiver(block: suspend (Transceiver) -> T): T
}

class UnsupportedTagException : IOException("Tag does not support ISO-DEP")

/** The card moved away while it was being read. Platform readers translate their own exception into this one. */
class TagLostException(message: String? = null) : IOException(message ?: "Tag was lost")
