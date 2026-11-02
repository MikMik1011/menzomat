package st.misa.menzomat.card.domain.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ValidityTest {
    private val earlier = LocalDate(2026, 10, 31)
    private val later = LocalDate(2027, 10, 31)

    @Test
    fun `a card without dates does not expire`() {
        assertNull(Validity(issuedOn = null, cardValidUntil = null, validatedUntil = null).expiresOn)
    }

    @Test
    fun `the card date counts when the card was never validated`() {
        assertEquals(earlier, Validity(null, cardValidUntil = earlier, validatedUntil = null).expiresOn)
    }

    @Test
    fun `the validation date counts when the card date is missing`() {
        assertEquals(later, Validity(null, cardValidUntil = null, validatedUntil = later).expiresOn)
    }

    @Test
    fun `the later of the two dates is when the card expires`() {
        assertEquals(later, Validity(null, cardValidUntil = earlier, validatedUntil = later).expiresOn)
        assertEquals(later, Validity(null, cardValidUntil = later, validatedUntil = earlier).expiresOn)
    }
}
