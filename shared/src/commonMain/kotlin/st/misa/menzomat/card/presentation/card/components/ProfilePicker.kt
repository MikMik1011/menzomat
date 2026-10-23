package st.misa.menzomat.card.presentation.card.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.card.domain.model.ProfileSelection
import st.misa.menzomat.card.presentation.card.ProfileCandidate
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.profile_auto
import st.misa.menzomat.shared.resources.profile_close
import st.misa.menzomat.shared.resources.profile_picker_title
import st.misa.menzomat.ui.components.ChoiceRow
import st.misa.menzomat.ui.theme.spacing

/** Bottom sheet on narrow windows, dialog on wide ones. Picking a profile applies and closes at once. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePicker(
    candidates: List<ProfileCandidate>,
    selection: ProfileSelection,
    onSelect: (ProfileSelection) -> Unit,
    onDismiss: () -> Unit,
    useDialog: Boolean,
) {
    val choose = { choice: ProfileSelection ->
        onSelect(choice)
        onDismiss()
    }
    if (useDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(Res.string.profile_picker_title)) },
            text = { Options(candidates, selection, choose) },
            confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.profile_close)) } },
        )
    } else {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(Modifier.navigationBarsPadding().padding(bottom = MaterialTheme.spacing.medium)) {
                Text(
                    text = stringResource(Res.string.profile_picker_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.large, vertical = MaterialTheme.spacing.small),
                )
                Options(candidates, selection, choose)
            }
        }
    }
}

@Composable
private fun Options(
    candidates: List<ProfileCandidate>,
    selection: ProfileSelection,
    onChoose: (ProfileSelection) -> Unit,
) {
    Column {
        ChoiceRow(
            title = stringResource(Res.string.profile_auto),
            selected = selection is ProfileSelection.Auto,
            onClick = { onChoose(ProfileSelection.Auto) },
        )
        // Best match first; a profile the card does not look like is dimmed rather than explained.
        candidates.forEach { candidate ->
            ChoiceRow(
                title = candidate.profile.name,
                selected = (selection as? ProfileSelection.Manual)?.profileId == candidate.profile.id,
                onClick = { onChoose(ProfileSelection.Manual(candidate.profile.id)) },
                dimmed = candidate.score == 0,
            )
        }
    }
}
