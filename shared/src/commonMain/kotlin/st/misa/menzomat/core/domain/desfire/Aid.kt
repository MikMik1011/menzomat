package st.misa.menzomat.core.domain.desfire

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * DESFire application id. [value] is the numeric id; on the wire it travels least significant byte first.
 */
@Serializable
@JvmInline
value class Aid(val value: Int) {
    init {
        require(value in 0..0xFFFFFF) { "AID must fit in 24 bits: $value" }
    }

    fun toWireBytes(): ByteArray =
        byteArrayOf(value.toByte(), (value shr 8).toByte(), (value shr 16).toByte())

    override fun toString(): String = "0x" + value.toString(16).uppercase().padStart(6, '0')

    companion object {
        val PICC = Aid(0)

        fun fromWireBytes(bytes: ByteArray, offset: Int = 0): Aid = Aid(
            (bytes[offset].toInt() and 0xFF) or
                ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
                ((bytes[offset + 2].toInt() and 0xFF) shl 16),
        )
    }
}
