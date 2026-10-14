package st.misa.menzomat.core.domain.desfire

/** The hardware seam: sends one ISO 7816 APDU and returns the raw response including SW1 SW2. */
fun interface Transceiver {
    /** @throws java.io.IOException when the link to the card fails. */
    suspend fun transceive(apdu: ByteArray): ByteArray
}
