package st.misa.menzomat.core.data.desfire

import st.misa.menzomat.core.domain.desfire.Aid
import st.misa.menzomat.core.domain.desfire.DesfireClient
import st.misa.menzomat.core.domain.desfire.DesfireError
import st.misa.menzomat.core.domain.desfire.FileSettings
import st.misa.menzomat.core.domain.desfire.FileType
import st.misa.menzomat.core.domain.desfire.Transceiver
import st.misa.menzomat.core.domain.util.hex

internal class IsoDesfireClient(private val transceiver: Transceiver) : DesfireClient {

    override suspend fun getApplicationIds(): List<Aid> {
        val data = command(GET_APPLICATION_IDS)
        if (data.size % 3 != 0) throw DesfireError.Malformed("application id list length ${data.size}")
        return List(data.size / 3) { Aid.fromWireBytes(data, it * 3) }
    }

    override suspend fun selectApplication(aid: Aid) {
        command(SELECT_APPLICATION, aid.toWireBytes())
    }

    override suspend fun getFileIds(): List<Int> =
        command(GET_FILE_IDS).map { it.toInt() and 0xFF }

    override suspend fun getFileSettings(fileId: Int): FileSettings {
        val data = command(GET_FILE_SETTINGS, byteArrayOf(fileId.toByte()))
        if (data.size < 4) throw DesfireError.Malformed("file settings length ${data.size}")
        val type = FileType.fromCode(data[0].toInt() and 0xFF)
        val size = when (type) {
            FileType.Standard, FileType.Backup, FileType.LinearRecord, FileType.CyclicRecord ->
                if (data.size >= 7) u24le(data, 4) else null
            else -> null
        }
        return FileSettings(
            type = type,
            communication = data[1].toInt() and 0xFF,
            accessRights = (data[2].toInt() and 0xFF) or ((data[3].toInt() and 0xFF) shl 8),
            size = size,
            rawHex = data.toHexString(),
        )
    }

    override suspend fun readData(fileId: Int, offset: Int, length: Int): ByteArray =
        command(READ_DATA, byteArrayOf(fileId.toByte()) + le24(offset) + le24(length))

    override suspend fun getValue(fileId: Int): Int {
        val data = command(GET_VALUE, byteArrayOf(fileId.toByte()))
        if (data.size != 4) throw DesfireError.Malformed("value length ${data.size}")
        return (data[0].toInt() and 0xFF) or
            ((data[1].toInt() and 0xFF) shl 8) or
            ((data[2].toInt() and 0xFF) shl 16) or
            ((data[3].toInt() and 0xFF) shl 24)
    }

    override suspend fun readRecords(fileId: Int, offset: Int, count: Int): ByteArray =
        command(READ_RECORDS, byteArrayOf(fileId.toByte()) + le24(offset) + le24(count))

    /** Sends [instruction] and follows `91 AF` continuation frames until the card reports completion. */
    private suspend fun command(instruction: Int, data: ByteArray = ByteArray(0)): ByteArray {
        var payload = ByteArray(0)
        var apdu = frame(instruction, data)
        while (true) {
            val response = transceiver.transceive(apdu)
            if (response.size < 2) throw DesfireError.Malformed("response of ${response.size} bytes")
            val sw1 = response[response.size - 2].toInt() and 0xFF
            val sw2 = response[response.size - 1].toInt() and 0xFF
            payload += response.copyOfRange(0, response.size - 2)
            when {
                sw1 == 0x90 && sw2 == 0x00 -> return payload
                sw1 != 0x91 -> throw DesfireError.Malformed("status ${sw1.hex(2)}${sw2.hex(2)}")
                sw2 == STATUS_OK -> return payload
                sw2 == STATUS_MORE -> apdu = frame(ADDITIONAL_FRAME, ByteArray(0))
                else -> throw DesfireError.fromStatus(sw2)
            }
        }
    }

    private fun frame(instruction: Int, data: ByteArray): ByteArray =
        if (data.isEmpty()) {
            byteArrayOf(0x90.toByte(), instruction.toByte(), 0x00, 0x00, 0x00)
        } else {
            byteArrayOf(0x90.toByte(), instruction.toByte(), 0x00, 0x00, data.size.toByte()) + data + 0x00
        }

    private fun le24(value: Int) = byteArrayOf(value.toByte(), (value shr 8).toByte(), (value shr 16).toByte())

    private fun u24le(bytes: ByteArray, offset: Int) =
        (bytes[offset].toInt() and 0xFF) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 16)

    private companion object {
        const val GET_APPLICATION_IDS = 0x6A
        const val SELECT_APPLICATION = 0x5A
        const val GET_FILE_IDS = 0x6F
        const val GET_FILE_SETTINGS = 0xF5
        const val READ_DATA = 0xBD
        const val GET_VALUE = 0x6C
        const val READ_RECORDS = 0xBB
        const val ADDITIONAL_FRAME = 0xAF
        const val STATUS_OK = 0x00
        const val STATUS_MORE = 0xAF
    }
}
