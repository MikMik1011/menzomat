package st.misa.menzomat.card.presentation.card

import st.misa.menzomat.card.domain.model.Validity
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlin.test.Test
import kotlin.test.assertEquals

class ValidityStatusTest {
    private val today = LocalDate(2026, 10, 13)

    private fun status(validatedUntil: LocalDate?) =
        validityStatus(Validity(issuedOn = null, cardValidUntil = null, validatedUntil = validatedUntil), today)

    @Test
    fun `no validation date means not validated`() = assertEquals(ValidityStatus.NotValidated, status(null))

    @Test
    fun `far in the future is valid`() {
        val until = LocalDate(2027, 10, 31)
        assertEquals(ValidityStatus.Valid(until), status(until))
    }

    @Test
    fun `within a month it expires soon`() {
        val until = LocalDate(2026, 10, 31)
        assertEquals(ValidityStatus.ExpiresSoon(until, daysLeft = 18), status(until))
    }

    @Test
    fun `the last day is still valid and the day after is expired`() {
        assertEquals(ValidityStatus.ExpiresSoon(today, 0), status(today))
        val yesterday = today.minus(1, DateTimeUnit.DAY)
        assertEquals(ValidityStatus.Expired(yesterday), status(yesterday))
    }
}
