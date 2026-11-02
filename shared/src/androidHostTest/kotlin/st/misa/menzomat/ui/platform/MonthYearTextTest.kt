package st.misa.menzomat.ui.platform

import java.time.YearMonth
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class MonthYearTextTest {
    private val serbian = Locale.forLanguageTag("sr-Latn-RS")

    @Test
    fun `serbian month names start with a capital letter`() {
        assertEquals("Oktobar 2026", monthYearText(YearMonth.of(2026, 10), serbian))
        assertEquals("Novembar 2026", monthYearText(YearMonth.of(2026, 11), serbian))
    }

    @Test
    fun `english month names are unchanged`() {
        assertEquals("October 2026", monthYearText(YearMonth.of(2026, 10), Locale.US))
    }
}
