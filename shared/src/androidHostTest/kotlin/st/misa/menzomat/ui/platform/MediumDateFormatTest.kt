package st.misa.menzomat.ui.platform

import java.time.LocalDate
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class MediumDateFormatTest {
    private val date = LocalDate.of(2003, 11, 10)

    @Test
    fun `serbian dates have no space after the dots`() {
        assertEquals("10.11.2003.", mediumDateFormatter(Locale.forLanguageTag("sr-Latn-RS")).format(date))
        assertEquals("10.11.2003.", mediumDateFormatter(Locale.forLanguageTag("sr-RS")).format(date))
    }

    @Test
    fun `serbian single digit days and months are not padded`() {
        assertEquals("5.3.2024.", mediumDateFormatter(Locale.forLanguageTag("sr-Latn-RS")).format(LocalDate.of(2024, 3, 5)))
    }

    @Test
    fun `other languages keep their own style`() {
        assertEquals("Nov 10, 2003", mediumDateFormatter(Locale.US).format(date))
    }
}
