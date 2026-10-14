package st.misa.menzomat.core.domain.util

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ByteCursorTest {
    @Test
    fun `reads length prefixed utf8 text`() {
        val bytes = byteArrayOf(5, 0x4D, 0x49, 0xC5.toByte(), 0xA0.toByte(), 0x41)
        assertEquals("MIŠA", ByteCursor(bytes).lengthPrefixedText())
    }

    @Test
    fun `falls back to windows 1250 when the text is not utf8`() {
        // "Šđ" in Windows-1250 is 8A F0 — not valid UTF-8.
        val bytes = byteArrayOf(2, 0x8A.toByte(), 0xF0.toByte())
        assertEquals("Šđ", ByteCursor(bytes).lengthPrefixedText())
    }

    @Test
    fun `windows 1250 covers the serbian latin letters`() {
        // Š š Č č Ć ć Đ đ Ž ž
        val bytes = byteArrayOf(10, 0x8A.toByte(), 0x9A.toByte(), 0xC8.toByte(), 0xE8.toByte(), 0xC6.toByte(), 0xE6.toByte(), 0xD0.toByte(), 0xF0.toByte(), 0x8E.toByte(), 0x9E.toByte())
        assertEquals("ŠšČčĆćĐđŽž", ByteCursor(bytes).lengthPrefixedText())
    }

    @Test
    fun `big endian 24 bit and little endian 32 bit`() {
        assertEquals(622, ByteCursor(byteArrayOf(0, 2, 0x6E)).u24be())
        assertEquals(981, ByteCursor(byteArrayOf(0xD5.toByte(), 3, 0, 0)).u32le())
    }

    @Test
    fun `running off the end is a decode error`() {
        assertFailsWith<DecodeException> { ByteCursor(byteArrayOf(5, 1, 2)).lengthPrefixedText() }
    }

    @Test
    fun `month base shifts the stored month`() {
        assertEquals(LocalDate(2026, 10, 31), dateOrNull(31, 10, 26, MonthBase.OneBased))
        assertEquals(LocalDate(2026, 10, 31), dateOrNull(31, 9, 26, MonthBase.ZeroBased))
        assertNull(dateOrNull(31, 9, 26, MonthBase.OneBased), "31 September does not exist")
        assertNull(dateOrNull(1, 12, 26, MonthBase.ZeroBased), "month 13 does not exist")
    }
}
