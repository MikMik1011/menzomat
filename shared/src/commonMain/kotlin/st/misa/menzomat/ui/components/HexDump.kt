package st.misa.menzomat.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

/** Columns must line up, so lines never wrap; with very large text the dump scrolls sideways instead. */
@Composable
fun HexDump(text: String, modifier: Modifier = Modifier) {
    SelectionContainer {
        Text(
            text = text,
            modifier = modifier.horizontalScroll(rememberScrollState()),
            softWrap = false,
            style = hexDumpStyle(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** The text style of [HexDump], for measuring how wide a line will be. */
@Composable
fun hexDumpStyle(): TextStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
