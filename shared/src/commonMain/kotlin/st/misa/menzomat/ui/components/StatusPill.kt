package st.misa.menzomat.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import st.misa.menzomat.ui.theme.brand
import st.misa.menzomat.ui.theme.spacing

enum class StatusKind { Good, Warning, Bad }

@Composable
fun StatusPill(
    text: String,
    icon: ImageVector,
    kind: StatusKind,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val (container, content) = when (kind) {
        StatusKind.Good -> colors.surface to colors.onSurface
        StatusKind.Warning -> MaterialTheme.brand.highlight to MaterialTheme.brand.onHighlight
        StatusKind.Bad -> colors.errorContainer to colors.onErrorContainer
    }
    Surface(modifier = modifier, shape = CircleShape, color = container, contentColor = content) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (kind == StatusKind.Good) colors.primary else content,
            )
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}
