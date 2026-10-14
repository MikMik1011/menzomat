package st.misa.menzomat.core.domain.util

class DecodeException(message: String) : Exception(message)

/** Sequential reader over a file's bytes; running off the end is a [DecodeException], not a crash. */
class ByteCursor(private val bytes: ByteArray, position: Int = 0) {
    var position: Int = position
        private set

    val remaining: Int get() = bytes.size - position

    fun u8(): Int {
        if (remaining < 1) throw DecodeException("expected 1 byte at offset $position, file has ${bytes.size}")
        return bytes[position++].toInt() and 0xFF
    }

    fun u24be(): Int = (u8() shl 16) or (u8() shl 8) or u8()

    fun u32le(): Int = u8() or (u8() shl 8) or (u8() shl 16) or (u8() shl 24)

    fun skip(count: Int) {
        if (remaining < count) throw DecodeException("cannot skip $count bytes at offset $position")
        position += count
    }

    fun bytes(count: Int): ByteArray {
        if (remaining < count) throw DecodeException("expected $count bytes at offset $position, ${remaining} left")
        return bytes.copyOfRange(position, position + count).also { position += count }
    }

    /** One length byte followed by that many bytes of text. */
    fun lengthPrefixedText(): String = bytes(u8()).decodeText()
}

/** The cards write UTF-8, but older terminals may have written Windows-1250; try strict UTF-8 first. */
fun ByteArray.decodeText(): String =
    try {
        decodeToString(throwOnInvalidSequence = true)
    } catch (e: CharacterCodingException) {
        Windows1250.decode(this)
    }
