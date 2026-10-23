package st.misa.menzomat.card.data.profile.novisad

import st.misa.menzomat.core.domain.desfire.Aid
import st.misa.menzomat.core.domain.desfire.AppDump
import st.misa.menzomat.core.domain.desfire.CardDump
import st.misa.menzomat.core.domain.desfire.FileType
import st.misa.menzomat.core.domain.desfire.RawFile
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant

/** Hex of [bytes] (spaces allowed) zero-padded to [size] bytes. */
fun padded(bytes: String, size: Int): String {
    val clean = bytes.replace(" ", "")
    return clean + "00".repeat(size - clean.length / 2)
}

fun text(value: String): String = value.encodeToByteArray().let { it.size.toString(16).padStart(2, '0') + it.toHexString() }

/** File 0x00 with made-up personal data; the real dumps are never committed. */
fun nameFile(first: String = "ANA", last: String = "ANIC") = padded(text(first) + text(last), 256)

/**
 * A card as TagInfo dumped it on 13 Oct 2026 (personal data replaced, everything else byte-exact).
 * The value file 0x03 reads fine without a key (confirmed on a real tap); see [cardWithUnreadableCounter] for the degraded case.
 */
val currentCard: Map<Int, String?> = mapOf(
    0 to nameFile(),
    1 to "02011f091a02323132353033303334",
    2 to "1a0a010000 1e1f1d 000000".replace(" ", ""),
    3 to "df030000",
    4 to "010a1a000002",
    5 to "000000000000",
    6 to padded("160a18 160a18 00 1f0a1a", 24),
    7 to padded("040306 3230392f3232", 22),
    8 to "000030000000",
    9 to padded("160c19 010101", 128),
    10 to "01010000000000",
)

fun cardDump(
    files: Map<Int, String?>,
    readOn: LocalDate = LocalDate(2026, 10, 13),
    aid: Aid = Aid(0x00000D),
): CardDump {
    val raw = files.map { (id, hex) ->
        val type = when (id) {
            0 -> FileType.Standard
            3 -> FileType.Value
            else -> FileType.Backup
        }
        if (hex == null) {
            RawFile(id, type, error = "Permission denied (0x9D)")
        } else {
            RawFile(id, type, size = if (type == FileType.Value) null else hex.length / 2, dataHex = hex)
        }
    }
    val readAt = readOn.atTime(21, 44).toInstant(TimeZone.of("Europe/Belgrade")).toEpochMilliseconds()
    return CardDump("04328C4A071A90", readAt, listOf(AppDump(aid, raw)))
}

fun Map<Int, String?>.with(vararg changes: Pair<Int, String?>): Map<Int, String?> = this + changes

/** The counter file refusing to be read, as it would on a card configured differently. */
val cardWithUnreadableCounter: Map<Int, String?> = currentCard.with(3 to null)
