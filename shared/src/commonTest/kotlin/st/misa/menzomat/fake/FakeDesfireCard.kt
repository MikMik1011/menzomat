package st.misa.menzomat.fake

import st.misa.menzomat.core.domain.desfire.Aid
import st.misa.menzomat.core.domain.desfire.Transceiver

/** A scripted DESFire card: speaks native-wrapped APDUs, chunks long answers with `91 AF`. */
class FakeDesfireCard(private val apps: Map<Aid, Map<Int, FakeFile>>) : Transceiver {
    sealed interface FakeFile {
        class Data(val type: Int, val bytes: ByteArray, val readable: Boolean = true) : FakeFile
        class Value(val value: Int, val readable: Boolean) : FakeFile
    }

    val sent = mutableListOf<String>()
    private var selected: Aid? = null
    private var pending: ArrayDeque<ByteArray>? = null

    override suspend fun transceive(apdu: ByteArray): ByteArray {
        sent += apdu.toHexString()
        val ins = apdu[1].toInt() and 0xFF
        if (ins == 0xAF) return nextChunk()
        val lc = if (apdu.size > 5) apdu[4].toInt() and 0xFF else 0
        val data = if (lc > 0) apdu.copyOfRange(5, 5 + lc) else ByteArray(0)
        return when (ins) {
            0x6A -> respond(apps.keys.flatMap { it.toWireBytes().toList() }.toByteArray())
            0x5A -> {
                val aid = Aid.fromWireBytes(data)
                if (aid in apps) { selected = aid; ok() } else status(0xA0)
            }
            0x6F -> respond(files().keys.map { it.toByte() }.toByteArray())
            0xF5 -> files()[data[0].toInt()]?.let { respond(settings(it)) } ?: status(0xF0)
            0xBD -> when (val f = files()[data[0].toInt()]) {
                is FakeFile.Data -> if (f.readable) respond(f.bytes) else status(0x9D)
                else -> status(0xF0)
            }
            0x6C -> when (val f = files()[data[0].toInt()]) {
                is FakeFile.Value -> if (f.readable) respond(le32(f.value)) else status(0x9D)
                else -> status(0xF0)
            }
            else -> status(0x1C)
        }
    }

    private fun files() = apps.getValue(checkNotNull(selected) { "no application selected" })

    private fun settings(f: FakeFile): ByteArray = when (f) {
        is FakeFile.Data -> byteArrayOf(f.type.toByte(), 0x03, 0xE0.toByte(), 0xEE.toByte()) +
            byteArrayOf(f.bytes.size.toByte(), (f.bytes.size shr 8).toByte(), 0)
        is FakeFile.Value -> byteArrayOf(2, 0x03, 0xF0.toByte(), 0x4F) + ByteArray(13)
    }

    private fun le32(v: Int) = byteArrayOf(v.toByte(), (v shr 8).toByte(), (v shr 16).toByte(), (v shr 24).toByte())

    private fun ok() = byteArrayOf(0x91.toByte(), 0x00)

    private fun status(code: Int) = byteArrayOf(0x91.toByte(), code.toByte())

    private fun respond(payload: ByteArray): ByteArray {
        val chunks = ArrayDeque(payload.toList().chunked(CHUNK).map { it.toByteArray() })
        if (chunks.isEmpty()) return ok()
        pending = chunks
        return nextChunk()
    }

    private fun nextChunk(): ByteArray {
        val queue = checkNotNull(pending)
        val chunk = queue.removeFirst()
        return chunk + byteArrayOf(0x91.toByte(), if (queue.isEmpty()) 0x00 else 0xAF.toByte())
    }

    private companion object {
        const val CHUNK = 60
    }
}
