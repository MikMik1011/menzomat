package st.misa.menzomat.card.presentation.card.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.profile_better_keep
import st.misa.menzomat.shared.resources.profile_better_switch
import st.misa.menzomat.shared.resources.profile_better_title

/** Asks whether to move to a profile that fits the card better than the one the user forced. The title says it all. */
@Composable
internal fun BetterMatchDialog(profileName: String, onSwitch: () -> Unit, onKeep: () -> Unit) {
    AlertDialog(
        onDismissRequest = onKeep,
        title = { Text(stringResource(Res.string.profile_better_title, profileName)) },
        confirmButton = { TextButton(onClick = onSwitch) { Text(stringResource(Res.string.profile_better_switch)) } },
        dismissButton = { TextButton(onClick = onKeep) { Text(stringResource(Res.string.profile_better_keep)) } },
    )
}
