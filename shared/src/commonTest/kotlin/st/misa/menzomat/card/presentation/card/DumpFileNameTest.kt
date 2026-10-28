package st.misa.menzomat.card.presentation.card

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import st.misa.menzomat.core.domain.desfire.CardDump
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class DumpFileNameTest {
    private val zone = TimeZone.of("Europe/Belgrade")

    private fun dumpReadAt(date: LocalDate, hour: Int, minute: Int) =
        CardDump("04AABBCC", date.atTime(hour, minute).toInstant(zone).toEpochMilliseconds(), emptyList())

    @Test
    fun `the name carries the day and the minute of the read`() {
        assertEquals("menzomat-2026-10-23-2144.json", dumpFileName(dumpReadAt(LocalDate(2026, 10, 23), 21, 44), zone))
    }

    @Test
    fun `single digits are padded`() {
        assertEquals("menzomat-2026-01-05-0907.json", dumpFileName(dumpReadAt(LocalDate(2026, 1, 5), 9, 7), zone))
    }

    @Test
    fun `the name does not contain the card uid`() {
        assertFalse("04AABBCC" in dumpFileName(dumpReadAt(LocalDate(2026, 10, 23), 21, 44), zone))
    }
}
