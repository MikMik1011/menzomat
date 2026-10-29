package st.misa.menzomat.ui.components

import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Fixed, so the dialog is the same size in every language instead of shrinking around its text. */
private val DIALOG_WIDTH = 312.dp

/**
 * A question with two answers. The icon says what it is about and the question is centered under it, so there is
 * no body text; [destructive] tints the icon as a warning. Used for every yes or no the app asks.
 */
@Composable
fun ConfirmDialog(
    icon: ImageVector,
    title: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.width(DIALOG_WIDTH),
        icon = { Icon(icon, contentDescription = null) },
        title = { Text(title, textAlign = TextAlign.Center) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissText) } },
        iconContentColor = if (destructive) MaterialTheme.colorScheme.error else AlertDialogDefaults.iconContentColor,
    )
}
