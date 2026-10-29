package st.misa.menzomat.card.presentation.card.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.profile_better_keep
import st.misa.menzomat.shared.resources.profile_better_switch
import st.misa.menzomat.shared.resources.profile_better_title
import st.misa.menzomat.ui.components.ConfirmDialog

/** Asks whether to move to a profile that fits the card better than the one the user forced. */
@Composable
internal fun BetterMatchDialog(profileName: String, onSwitch: () -> Unit, onKeep: () -> Unit) {
    ConfirmDialog(
        icon = Icons.Rounded.SwapHoriz,
        title = stringResource(Res.string.profile_better_title, profileName),
        confirmText = stringResource(Res.string.profile_better_switch),
        dismissText = stringResource(Res.string.profile_better_keep),
        onConfirm = onSwitch,
        onDismiss = onKeep,
    )
}
