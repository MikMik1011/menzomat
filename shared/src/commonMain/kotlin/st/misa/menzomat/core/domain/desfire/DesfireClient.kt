package st.misa.menzomat.core.domain.desfire

data class FileSettings(
    val type: FileType?,
    val communication: Int,
    val accessRights: Int,
    /** Byte size for data files, record size for record files, null for value files. */
    val size: Int?,
    val rawHex: String,
)

/** Native DESFire commands, wrapped in ISO 7816 APDUs. Only what is needed for unauthenticated, free-read access. */
interface DesfireClient {
    suspend fun getApplicationIds(): List<Aid>
    suspend fun selectApplication(aid: Aid)
    suspend fun getFileIds(): List<Int>
    suspend fun getFileSettings(fileId: Int): FileSettings

    /** [length] of 0 reads from [offset] to the end of the file. */
    suspend fun readData(fileId: Int, offset: Int = 0, length: Int = 0): ByteArray
    suspend fun getValue(fileId: Int): Int
    suspend fun readRecords(fileId: Int, offset: Int = 0, count: Int = 0): ByteArray
}
