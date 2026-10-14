package st.misa.menzomat.card.presentation.card

import st.misa.menzomat.core.domain.util.hex

/**
 * `0000  05 4D 49 C5 A0 41 0B 53  .MI..A.S`, eight bytes per line so a line fits a phone screen.
 * Like `hexdump -C`, a run of identical lines is shown once followed by `*`; the last line is always shown.
 */
fun formatHexDump(bytes: ByteArray): String {
    val rows = bytes.asList().chunked(BYTES_PER_LINE)
    val lines = mutableListOf<String>()
    var skipping = false
    rows.forEachIndexed { index, row ->
        val repeatsPrevious = index > 0 && row == rows[index - 1] && index != rows.lastIndex
        if (repeatsPrevious) {
            if (!skipping) lines += "*"
            skipping = true
        } else {
            skipping = false
            val hex = row.joinToString(" ") { (it.toInt() and 0xFF).hex(2) }.padEnd(HEX_WIDTH)
            val text = row.joinToString("") { if (it in PRINTABLE) it.toInt().toChar().toString() else "." }
            lines += (index * BYTES_PER_LINE).hex(4) + "  " + hex + "  " + text
        }
    }
    return lines.joinToString("\n")
}

private const val BYTES_PER_LINE = 8
private const val HEX_WIDTH = BYTES_PER_LINE * 3 - 1
private val PRINTABLE = 0x20..0x7E
