package st.misa.menzomat.card.presentation.card

import kotlin.test.Test
import kotlin.test.assertEquals

class HexDumpFormatTest {
    @Test
    fun `eight bytes per line with offsets and an ascii column`() {
        val bytes = byteArrayOf(0x4D, 0x49, 0x4B, 0, 1, 2, 3, 4, 0x41, 0x42)
        assertEquals(
            "0000  4D 49 4B 00 01 02 03 04  MIK.....\n" +
                "0008  41 42                    AB",
            formatHexDump(bytes),
        )
    }

    @Test
    fun `bytes outside printable ascii show as dots`() {
        val bytes = byteArrayOf(0x1F, 0x20, 0x7E, 0x7F, 0xC5.toByte(), 0xFF.toByte())
        // "1F 20 7E 7F C5 FF" is padded to the width of eight bytes, then two spaces before the text column.
        assertEquals("0000  1F 20 7E 7F C5 FF" + " ".repeat(6 + 2) + ". ~...", formatHexDump(bytes))
    }

    @Test
    fun `a run of identical lines is collapsed but the last line is kept`() {
        val bytes = ByteArray(40) // five lines of zeros
        assertEquals(
            "0000  00 00 00 00 00 00 00 00  ........\n*\n0020  00 00 00 00 00 00 00 00  ........",
            formatHexDump(bytes),
        )
    }

    @Test
    fun `identical lines that are not adjacent are not collapsed`() {
        val bytes = ByteArray(8) + ByteArray(8) { 1 } + ByteArray(8)
        assertEquals(
            "0000  00 00 00 00 00 00 00 00  ........\n" +
                "0008  01 01 01 01 01 01 01 01  ........\n" +
                "0010  00 00 00 00 00 00 00 00  ........",
            formatHexDump(bytes),
        )
    }

    @Test
    fun `an empty file is an empty dump`() = assertEquals("", formatHexDump(ByteArray(0)))
}
