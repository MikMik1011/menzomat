package st.misa.menzomat.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertTrue

/** Every foreground/background pair of the brand schemes must stay readable (WCAG AA). */
class ContrastTest {
    private class Pair(val name: String, val background: Color, val foreground: Color, val minimum: Double)

    private fun ColorScheme.pairs(): List<Pair> = listOf(
        Pair("primary", primary, onPrimary, TEXT),
        Pair("primaryContainer", primaryContainer, onPrimaryContainer, TEXT),
        Pair("secondary", secondary, onSecondary, TEXT),
        Pair("secondaryContainer", secondaryContainer, onSecondaryContainer, TEXT),
        Pair("tertiary", tertiary, onTertiary, TEXT),
        Pair("tertiaryContainer", tertiaryContainer, onTertiaryContainer, TEXT),
        Pair("error", error, onError, TEXT),
        Pair("errorContainer", errorContainer, onErrorContainer, TEXT),
        Pair("surface", surface, onSurface, TEXT),
        Pair("surface/variant text", surface, onSurfaceVariant, TEXT),
        Pair("surfaceContainerLow", surfaceContainerLow, onSurface, TEXT),
        Pair("surfaceContainerLow/variant", surfaceContainerLow, onSurfaceVariant, TEXT),
        Pair("surfaceContainerHighest", surfaceContainerHighest, onSurface, TEXT),
        Pair("surfaceContainerHighest/variant", surfaceContainerHighest, onSurfaceVariant, TEXT),
        Pair("inverseSurface", inverseSurface, inverseOnSurface, TEXT),
        // Accent roles are also used as text and icon colors directly on the page.
        Pair("primary on surface", surface, primary, TEXT),
        Pair("secondary on surface", surface, secondary, TEXT),
        Pair("tertiary on surface", surface, tertiary, TEXT),
        Pair("outline on surface", surface, outline, COMPONENT),
        Pair("container edge", surface, primaryContainer, COMPONENT_FILL),
    )

    @Test
    fun `light scheme is readable`() = check("light", MenzomatLightColors)

    @Test
    fun `dark scheme is readable`() = check("dark", MenzomatDarkColors)

    @Test
    fun `highlight carries brand black text`() {
        val ratio = contrast(MenzomatBrandColors.highlight, MenzomatBrandColors.onHighlight)
        assertTrue(ratio >= TEXT, "highlight contrast $ratio")
    }

    private fun check(scheme: String, colors: ColorScheme) {
        val failures = colors.pairs().mapNotNull { pair ->
            val ratio = contrast(pair.background, pair.foreground)
            "$scheme ${pair.name}: ${(ratio * 100).roundToInt() / 100.0} < ${pair.minimum}".takeIf { ratio < pair.minimum }
        }
        assertTrue(failures.isEmpty(), failures.joinToString("\n"))
    }

    private fun contrast(a: Color, b: Color): Double {
        val l1 = luminance(a)
        val l2 = luminance(b)
        return (maxOf(l1, l2) + 0.05) / (minOf(l1, l2) + 0.05)
    }

    private fun luminance(color: Color): Double {
        fun channel(value: Float): Double {
            val v = value.toDouble()
            return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)
    }

    private companion object {
        const val TEXT = 4.5
        const val COMPONENT = 3.0
        // The hero fill against the page only needs to be visible as a shape in dark mode; light mode is checked in text pairs.
        const val COMPONENT_FILL = 1.0
    }
}
