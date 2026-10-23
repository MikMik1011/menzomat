package st.misa.menzomat.card.data.profile.novisad

import st.misa.menzomat.card.domain.model.DecodeWarning
import st.misa.menzomat.card.domain.model.ExtraValue
import st.misa.menzomat.card.domain.model.Faculty
import st.misa.menzomat.card.domain.model.MealBalance
import st.misa.menzomat.card.domain.model.WarningReason
import st.misa.menzomat.core.domain.desfire.Aid
import st.misa.menzomat.core.domain.desfire.AppDump
import st.misa.menzomat.core.domain.desfire.CardDump
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NoviSadProfileTest {
    private val profile = NoviSadProfile()

    private fun decode(files: Map<Int, String?> = currentCard, readOn: LocalDate = LocalDate(2026, 10, 13)) =
        profile.decode(cardDump(files, readOn))

    @Test
    fun `decodes a complete current card`() {
        val card = decode()

        assertEquals("ANA", card.holder?.firstName)
        assertEquals("ANIC", card.holder?.lastName)
        assertEquals("212503034", card.cardNumber)

        assertEquals("Univerzitet u Novom Sadu", card.institution?.name)
        assertEquals(4, card.institution?.studyYear, "renewed card: one more than the 3 written in file 0x07")
        assertEquals("209/22", card.institution?.studentId)

        assertEquals(LocalDate(2024, 10, 22), card.validity?.issuedOn)
        assertEquals(LocalDate(2026, 10, 31), card.validity?.validatedUntil)
        assertEquals(LocalDate(2026, 10, 31), card.validity?.cardValidUntil)

        assertEquals(YearMonth(2026, 10), card.balanceMonth)
        assertEquals(MealBalance(1, 0, 0), card.meals)
        assertEquals(MealBalance(30, 31, 29), card.mealsLeftToBuy)
        assertEquals(MealBalance(0, 0, 0), card.nextMonthMeals)
        assertEquals(48, card.cashBalanceRsd)

        assertEquals(LocalDate(2026, 10, 1), card.lastTransaction?.date)
        assertEquals(MealBalance(0, 0, 2), card.lastTransaction?.mealsEaten)

        assertEquals(ExtraValue.Date(LocalDate(2025, 12, 22)), card.extras.single { it.key == "medical-check" }.value)
        assertEquals(ExtraValue.Text("991"), card.extras.single { it.key == "transaction-counter" }.value)
        assertEquals(emptyList(), card.warnings)
    }

    @Test
    fun `months written one based are read as is`() {
        val card = decode(
            currentCard.with(
                2 to "1a09010000 1c1510 000000".replace(" ", ""),
                4 to "1d091a000100",
            ),
        )
        assertEquals(LocalDate(2026, 9, 29), card.lastTransaction?.date)
        assertEquals(MealBalance(0, 1, 0), card.lastTransaction?.mealsEaten)
    }

    @Test
    fun `months written zero based by older terminals are corrected`() {
        // 12 Jan 2026: balance month 01, transaction month stored as 00.
        val card = decode(
            currentCard.with(2 to "1a01000000 1f1b1f 000000".replace(" ", ""), 4 to "09001a000100"),
            readOn = LocalDate(2026, 1, 12),
        )
        assertEquals(LocalDate(2026, 1, 9), card.lastTransaction?.date)
    }

    @Test
    fun `december first written zero based stays in december`() {
        val card = decode(
            currentCard.with(2 to "190c000000 1e1f1f 000000".replace(" ", ""), 4 to "010b19010000"),
            readOn = LocalDate(2025, 12, 1),
        )
        assertEquals(LocalDate(2025, 12, 1), card.lastTransaction?.date)
        assertEquals(MealBalance(1, 0, 0), card.lastTransaction?.mealsEaten)
    }

    @Test
    fun `an ambiguous date that would lie in the future is read the other way`() {
        // Balance month 10, transaction month 09: zero based would be 30 Oct, after the read date.
        val card = decode(currentCard.with(4 to "1e091a000100"))
        assertEquals(LocalDate(2026, 9, 30), card.lastTransaction?.date)
    }

    @Test
    fun `a card that was not renewed shows the year written on it`() {
        val card = decode(currentCard.with(6 to padded("160a18 160a18 00 010100", 24)))
        assertEquals(3, card.institution?.studyYear)
    }

    @Test
    fun `a card whose renewal file cannot be read shows the year written on it`() {
        val card = decode(currentCard.with(6 to null))
        assertEquals(3, card.institution?.studyYear)
    }

    @Test
    fun `an unvalidated card has no validated until date`() {
        val card = decode(
            currentCard.with(
                6 to padded("160a18 160a18 00 010100", 24),
                9 to padded("010100 000101", 128),
            ),
        )
        assertNull(card.validity?.validatedUntil)
        assertEquals(LocalDate(2024, 10, 22), card.validity?.issuedOn)
        assertEquals(LocalDate(2026, 10, 31), card.validity?.cardValidUntil, "still known from file 0x01")
        assertEquals(ExtraValue.Absent, card.extras.single { it.key == "medical-check" }.value)
    }

    @Test
    fun `an unreadable transaction counter is left out without a warning`() {
        val card = decode(cardWithUnreadableCounter)
        assertTrue(card.extras.none { it.key == "transaction-counter" })
        assertEquals(emptyList(), card.warnings)
    }

    @Test
    fun `names in windows 1250 are recovered`() {
        val first = "8a" // Š in windows-1250, invalid as utf-8
        val file = padded("01$first 02" + "4141", 256)
        val card = decode(currentCard.with(0 to file))
        assertEquals("Š", card.holder?.firstName)
        assertEquals("AA", card.holder?.lastName)
    }

    @Test
    fun `faculty and index of a three letter index`() {
        val card = decode(currentCard.with(7 to padded("010308 53562031392f3233", 22)))
        assertEquals("SV 19/23", card.institution?.studentId)
        assertEquals(Faculty(id = 1), card.institution?.faculty, "the id travels with the card; the app words the fallback")
    }

    @Test
    fun `a truncated file degrades to a warning and leaves the rest intact`() {
        val card = decode(currentCard.with(2 to "1a0a01"))
        assertNull(card.meals)
        assertNull(card.mealsLeftToBuy)
        assertTrue(card.warnings.any { it.field == "0x02" && it.reason == WarningReason.Truncated })
        assertEquals("ANA", card.holder?.firstName)
        assertEquals(48, card.cashBalanceRsd)
    }

    @Test
    fun `a missing file is a warning not a crash`() {
        val card = profile.decode(cardDump(currentCard - 8))
        assertNull(card.cashBalanceRsd)
        assertTrue(card.warnings.any { it.field == "0x08" && it.reason == WarningReason.Missing })
    }

    @Test
    fun `a file the card refused to read carries the reason the card gave`() {
        val card = decode(currentCard.with(8 to null))
        assertEquals(
            listOf(DecodeWarning("0x08", WarningReason.Unreadable("Permission denied (0x9D)"))),
            card.warnings,
        )
    }

    @Test
    fun `an impossible balance month is reported as invalid`() {
        val card = decode(currentCard.with(2 to padded("1a 0d 01 01 02 1e 17 16 00 00 00", 11)))
        assertTrue(card.warnings.any { it.field == "0x02" && it.reason == WarningReason.Invalid("13/26") })
    }

    @Test
    fun `a card without the application decodes to an empty card`() {
        val card = profile.decode(CardDump("00", 0, emptyList()))
        assertNull(card.holder)
        assertNull(card.meals)
    }

    @Test
    fun `scores a complete card at 100`() {
        assertEquals(100, profile.score(cardDump(currentCard)))
    }

    @Test
    fun `scores zero without the application`() {
        assertEquals(0, profile.score(CardDump("00", 0, listOf(AppDump(Aid(0x123456))))))
    }

    @Test
    fun `scores a card that has the application but a different layout lower`() {
        val score = profile.score(cardDump(mapOf(0 to padded("00", 64), 1 to padded("00", 4))))
        assertTrue(score in 1..99, "score was $score")
    }

    @Test
    fun `accepts the application id in either byte order`() {
        assertEquals(100, profile.score(cardDump(currentCard, aid = Aid(0x0D0000))))
        assertNotNull(profile.decode(cardDump(currentCard, aid = Aid(0x0D0000))).holder)
    }
}
