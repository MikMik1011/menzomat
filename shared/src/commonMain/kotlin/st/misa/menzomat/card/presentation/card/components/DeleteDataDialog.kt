package st.misa.menzomat.card.presentation.card.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.delete_data_cancel
import st.misa.menzomat.shared.resources.delete_data_confirm
import st.misa.menzomat.shared.resources.delete_data_title

/** Asks before the saved card is forgotten, because it cannot be read back without the card. */
@Composable
internal fun DeleteDataDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.delete_data_title)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(Res.string.delete_data_confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.delete_data_cancel)) } },
    )
}
