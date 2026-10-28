package st.misa.menzomat.card.presentation.card

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import st.misa.menzomat.core.domain.desfire.CardDump
import kotlin.time.Instant

private val FILE_STAMP = LocalDateTime.Format {
    year()
    char('-')
    monthNumber()
    char('-')
    day()
    char('-')
    hour()
    minute()
}

/** The suggested name of a saved dump: unique per read, and without the card's uid. Example: `menzomat-2026-10-23-2144.json`. */
fun dumpFileName(dump: CardDump, zone: TimeZone = TimeZone.currentSystemDefault()): String {
    val readAt = Instant.fromEpochMilliseconds(dump.readAtEpochMillis).toLocalDateTime(zone)
    return "menzomat-${FILE_STAMP.format(readAt)}.json"
}
