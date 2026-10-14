package st.misa.menzomat.core.domain.desfire

import kotlinx.serialization.Serializable

enum class FileType(val code: Int) {
    Standard(0),
    Backup(1),
    Value(2),
    LinearRecord(3),
    CyclicRecord(4),
    TransactionMac(5),
    ;

    companion object {
        fun fromCode(code: Int): FileType? = entries.firstOrNull { it.code == code }
    }
}

/** Everything that could be read from a card in one tap, independent of any [CardProfile]-specific meaning. */
@Serializable
data class CardDump(
    val uid: String,
    val readAtEpochMillis: Long,
    val apps: List<AppDump>,
) {
    fun app(aid: Aid): AppDump? = apps.firstOrNull { it.aid == aid }
}

@Serializable
data class AppDump(
    val aid: Aid,
    val files: List<RawFile> = emptyList(),
    val error: String? = null,
) {
    fun file(id: Int): RawFile? = files.firstOrNull { it.id == id }
}

@Serializable
data class RawFile(
    val id: Int,
    val type: FileType? = null,
    val size: Int? = null,
    val settingsHex: String? = null,
    val dataHex: String? = null,
    val error: String? = null,
) {
    val data: ByteArray? get() = dataHex?.hexToByteArray()
}
