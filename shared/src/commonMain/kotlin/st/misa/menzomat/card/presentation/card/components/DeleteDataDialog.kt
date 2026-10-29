package st.misa.menzomat.card.presentation.card.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.delete_data_cancel
import st.misa.menzomat.shared.resources.delete_data_confirm
import st.misa.menzomat.shared.resources.delete_data_title
import st.misa.menzomat.ui.components.ConfirmDialog

/** Asks before the saved card is forgotten, because it cannot be read back without the card. */
@Composable
internal fun DeleteDataDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ConfirmDialog(
        icon = Icons.Rounded.Delete,
        title = stringResource(Res.string.delete_data_title),
        confirmText = stringResource(Res.string.delete_data_confirm),
        dismissText = stringResource(Res.string.delete_data_cancel),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        destructive = true,
    )
}
