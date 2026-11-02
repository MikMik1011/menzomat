package st.misa.menzomat.card.presentation.card

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import st.misa.menzomat.card.domain.model.Validity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExpiryWarningTest {
    private val today = LocalDate(2026, 11, 1)

    private fun cardValidUntil(date: LocalDate, validatedUntil: LocalDate? = null) =
        Validity(issuedOn = null, cardValidUntil = date, validatedUntil = validatedUntil)

    private fun daysFromToday(days: Int) = today.plus(DatePeriod(days = days))

    @Test
    fun `no validity means no warning`() {
        assertNull(expiryWarning(null, today))
        assertNull(expiryWarning(Validity(null, null, null), today))
    }

    @Test
    fun `a card whose date has passed is expired`() {
        val yesterday = daysFromToday(-1)
        assertEquals(ExpiryWarning.Expired(yesterday, daysAgo = 1), expiryWarning(cardValidUntil(yesterday), today))
    }

    @Test
    fun `the days since a card expired are counted`() {
        val tenDaysAgo = daysFromToday(-10)
        assertEquals(ExpiryWarning.Expired(tenDaysAgo, daysAgo = 10), expiryWarning(cardValidUntil(tenDaysAgo), today))
    }

    @Test
    fun `a card is still valid on its last day but expires today`() {
        assertEquals(ExpiryWarning.ExpiresSoon(today, daysLeft = 0), expiryWarning(cardValidUntil(today), today))
    }

    @Test
    fun `a card that expires within a month is about to expire`() {
        val inTenDays = daysFromToday(10)
        assertEquals(ExpiryWarning.ExpiresSoon(inTenDays, daysLeft = 10), expiryWarning(cardValidUntil(inTenDays), today))
    }

    @Test
    fun `the warning starts thirty days ahead and not before`() {
        assertEquals(ExpiryWarning.ExpiresSoon(daysFromToday(30), 30), expiryWarning(cardValidUntil(daysFromToday(30)), today))
        assertNull(expiryWarning(cardValidUntil(daysFromToday(31)), today))
    }

    @Test
    fun `a renewed card is not expired even if its card date is old`() {
        val renewed = cardValidUntil(daysFromToday(-30), validatedUntil = daysFromToday(364))
        assertNull(expiryWarning(renewed, today))
    }

    @Test
    fun `only the validation date is enough`() {
        val validity = Validity(issuedOn = null, cardValidUntil = null, validatedUntil = daysFromToday(-5))
        assertEquals(ExpiryWarning.Expired(daysFromToday(-5), daysAgo = 5), expiryWarning(validity, today))
    }
}
