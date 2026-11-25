package st.misa.menzomat.card.presentation.card

import st.misa.menzomat.core.domain.util.hex

/**
 * `0000  05 4D 49 C5 A0 41 0B 53  .MI..A.S`, by default eight bytes per line so a line fits a phone screen.
 * Like `hexdump -C`, a run of identical lines is shown once followed by `*`; the last line is always shown.
 */
fun formatHexDump(bytes: ByteArray, bytesPerLine: Int = NARROW_BYTES_PER_LINE): String {
    val rows = bytes.asList().chunked(bytesPerLine)
    val lines = mutableListOf<String>()
    var skipping = false
    rows.forEachIndexed { index, row ->
        val repeatsPrevious = index > 0 && row == rows[index - 1] && index != rows.lastIndex
        if (repeatsPrevious) {
            if (!skipping) lines += "*"
            skipping = true
        } else {
            skipping = false
            val hex = row.joinToString(" ") { (it.toInt() and 0xFF).hex(2) }.padEnd(bytesPerLine * 3 - 1)
            val text = row.joinToString("") { if (it in PRINTABLE) it.toInt().toChar().toString() else "." }
            lines += (index * bytesPerLine).hex(4) + "  " + hex + "  " + text
        }
    }
    return lines.joinToString("\n")
}

/** How the files of a dump are laid out. [labelBeside]: the file's label stands left of its bytes instead of above. */
internal enum class RawLayout(val bytesPerLine: Int, val labelBeside: Boolean) {
    Narrow(NARROW_BYTES_PER_LINE, labelBeside = false),
    Wide(WIDE_BYTES_PER_LINE, labelBeside = false),
    WideLabelBeside(WIDE_BYTES_PER_LINE, labelBeside = true),
}

/** The roomiest layout that fits, all widths in pixels. [label] is the widest label, with the gap after it. */
internal fun rawLayout(available: Int, label: Int, wideLine: Int): RawLayout = when {
    label + wideLine <= available -> RawLayout.WideLabelBeside
    wideLine <= available -> RawLayout.Wide
    else -> RawLayout.Narrow
}

private const val NARROW_BYTES_PER_LINE = 8
private const val WIDE_BYTES_PER_LINE = 16
private val PRINTABLE = 0x20..0x7E
