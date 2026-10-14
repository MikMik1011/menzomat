package st.misa.menzomat.card.presentation.card.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.card.domain.model.ProfileSelection
import st.misa.menzomat.card.presentation.card.ProfileCandidate
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.profile_auto
import st.misa.menzomat.shared.resources.profile_auto_summary
import st.misa.menzomat.shared.resources.profile_close
import st.misa.menzomat.shared.resources.profile_match
import st.misa.menzomat.shared.resources.profile_no_match
import st.misa.menzomat.shared.resources.profile_picker_title
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
        Option(
            title = stringResource(Res.string.profile_auto),
            summary = stringResource(Res.string.profile_auto_summary),
            selected = selection is ProfileSelection.Auto,
            onClick = { onChoose(ProfileSelection.Auto) },
        )
        candidates.forEach { candidate ->
            Option(
                title = candidate.profile.name,
                summary = if (candidate.score > 0) {
                    stringResource(Res.string.profile_match, candidate.score)
                } else {
                    stringResource(Res.string.profile_no_match)
                },
                selected = (selection as? ProfileSelection.Manual)?.profileId == candidate.profile.id,
                onClick = { onChoose(ProfileSelection.Manual(candidate.profile.id)) },
            )
        }
    }
}

@Composable
private fun Option(
    title: String,
    summary: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(Modifier.fillMaxWidth().selectable(selected = selected, role = Role.RadioButton, onClick = onClick)) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { Text(summary) },
            trailingContent = { RadioButton(selected = selected, onClick = null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}
