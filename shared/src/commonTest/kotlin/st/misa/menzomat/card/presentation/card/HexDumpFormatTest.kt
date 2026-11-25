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
    fun `sixteen bytes per line keep the ascii column aligned`() {
        assertEquals(
            "0000  " + "41 ".repeat(16) + " " + "A".repeat(16) + "\n" +
                "0010  41" + " ".repeat(45 + 2) + "A",
            formatHexDump(ByteArray(17) { 0x41 }, bytesPerLine = 16),
        )
    }

    @Test
    fun `the roomiest raw layout that fits is chosen`() {
        assertEquals(RawLayout.WideLabelBeside, rawLayout(available = 700, label = 160, wideLine = 540))
        assertEquals(RawLayout.Wide, rawLayout(available = 699, label = 160, wideLine = 540))
        assertEquals(RawLayout.Wide, rawLayout(available = 540, label = 160, wideLine = 540))
        assertEquals(RawLayout.Narrow, rawLayout(available = 539, label = 160, wideLine = 540))
    }

    @Test
    fun `an empty file is an empty dump`() = assertEquals("", formatHexDump(ByteArray(0)))
}
