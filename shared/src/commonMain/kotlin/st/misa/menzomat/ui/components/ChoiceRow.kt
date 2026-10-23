package st.misa.menzomat.ui.components

import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role

/** One option of a single choice list. [dimmed] marks an option that is available but unlikely to be right. */
@Composable
fun ChoiceRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    ListItem(
        modifier = modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        headlineContent = { Text(title) },
        trailingContent = { RadioButton(selected = selected, onClick = null) },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            headlineColor = if (dimmed) colors.onSurfaceVariant else colors.onSurface,
        ),
    )
}
