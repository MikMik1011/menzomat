package st.misa.menzomat.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp

/**
 * Cards next to each other, equally wide and as tall as the tallest one. A section that emits nothing leaves no
 * gap: the others share its width. Each section must emit at most one element.
 */
@Composable
fun PairRow(
    spacing: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(content, modifier) { measurables, constraints ->
        if (measurables.isEmpty()) return@Layout layout(0, 0) {}
        val gap = spacing.roundToPx()
        val width = (constraints.maxWidth - gap * (measurables.size - 1)) / measurables.size
        val height = measurables.maxOf { it.maxIntrinsicHeight(width) }
        val placeables = measurables.map { it.measure(Constraints(width, width, minHeight = height)) }
        layout(constraints.maxWidth, placeables.maxOf { it.height }) {
            placeables.forEachIndexed { index, placeable -> placeable.placeRelative(index * (width + gap), 0) }
        }
    }
}
