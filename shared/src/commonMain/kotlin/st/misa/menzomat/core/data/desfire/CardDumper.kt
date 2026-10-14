package st.misa.menzomat.core.data.desfire

import st.misa.menzomat.core.domain.desfire.Aid
import st.misa.menzomat.core.domain.desfire.AppDump
import st.misa.menzomat.core.domain.desfire.CardDump
import st.misa.menzomat.core.domain.desfire.DesfireClient
import st.misa.menzomat.core.domain.desfire.DesfireError
import st.misa.menzomat.core.domain.desfire.FileType
import st.misa.menzomat.core.domain.desfire.RawFile
import kotlin.time.Clock

/**
 * Reads every application and file on the card that allows it, without knowing what any of it means.
 * A file or application that refuses to be read is recorded with its error instead of aborting the dump.
 */
class CardDumper(
    private val client: DesfireClient,
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) {
    /** @throws DesfireError if the card does not answer DESFire commands at all. */
    suspend fun dump(uid: String): CardDump {
        val apps = client.getApplicationIds().filter { it != Aid.PICC }.map { dumpApplication(it) }
        return CardDump(uid = uid, readAtEpochMillis = now(), apps = apps)
    }

    private suspend fun dumpApplication(aid: Aid): AppDump = try {
        client.selectApplication(aid)
        AppDump(aid, files = client.getFileIds().map { readFile(it) })
    } catch (e: DesfireError) {
        AppDump(aid, error = e.message)
    }

    private suspend fun readFile(id: Int): RawFile {
        val settings = try {
            client.getFileSettings(id)
        } catch (e: DesfireError) {
            null
        }
        return try {
            val data = when (settings?.type) {
                FileType.Value -> client.getValue(id).toLittleEndianBytes()
                FileType.LinearRecord, FileType.CyclicRecord -> client.readRecords(id)
                else -> client.readData(id)
            }
            RawFile(id, settings?.type, settings?.size, settings?.rawHex, data.toHexString())
        } catch (e: DesfireError) {
            RawFile(id, settings?.type, settings?.size, settings?.rawHex, error = e.message)
        }
    }

    private fun Int.toLittleEndianBytes() =
        byteArrayOf(toByte(), (this shr 8).toByte(), (this shr 16).toByte(), (this shr 24).toByte())
}
