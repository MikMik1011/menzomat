package st.misa.menzomat.card.presentation.card.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Card
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.card.presentation.card.CardActions
import st.misa.menzomat.card.presentation.card.formatHexDump
import st.misa.menzomat.core.domain.desfire.CardDump
import st.misa.menzomat.core.domain.util.hex
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.raw_app
import st.misa.menzomat.shared.resources.raw_collapse
import st.misa.menzomat.shared.resources.raw_copy
import st.misa.menzomat.shared.resources.raw_expand
import st.misa.menzomat.shared.resources.raw_file
import st.misa.menzomat.shared.resources.raw_file_error
import st.misa.menzomat.shared.resources.raw_file_unknown_size
import st.misa.menzomat.shared.resources.raw_privacy
import st.misa.menzomat.shared.resources.raw_save
import st.misa.menzomat.shared.resources.raw_share
import st.misa.menzomat.shared.resources.raw_title
import st.misa.menzomat.ui.components.HexDump
import st.misa.menzomat.ui.theme.spacing

/** Every file as read from the card, for checking decoded values and for supporting new universities. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RawSection(dump: CardDump, actions: CardActions, initiallyExpanded: Boolean) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    val spacing = MaterialTheme.spacing
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(vertical = spacing.small)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClickLabel = stringResource(if (expanded) Res.string.raw_collapse else Res.string.raw_expand)) {
                        expanded = !expanded
                    }
                    .padding(horizontal = spacing.medium, vertical = spacing.small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    stringResource(Res.string.raw_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics { heading() },
                )
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier.padding(horizontal = spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(spacing.small),
                ) {
                    dump.apps.forEach { app ->
                        Text(stringResource(Res.string.raw_app, app.aid.toString()), style = MaterialTheme.typography.labelLarge)
                        app.error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
                        app.files.forEach { file ->
                            val type = file.type?.name ?: "?"
                            val title = file.size?.let { stringResource(Res.string.raw_file, file.id.hex(2), type, it) }
                                ?: stringResource(Res.string.raw_file_unknown_size, file.id.hex(2), type)
                            Text(title, style = MaterialTheme.typography.labelMedium)
                            val bytes = file.data
                            when {
                                bytes != null -> HexDump(formatHexDump(bytes))
                                else -> Text(
                                    stringResource(Res.string.raw_file_error, file.id.hex(2), file.error ?: "?"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                    Text(
                        stringResource(Res.string.raw_privacy),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(spacing.small),
                        verticalArrangement = Arrangement.spacedBy(spacing.small),
                    ) {
                        RawAction(Icons.Rounded.Share, stringResource(Res.string.raw_share), actions::onShareDump)
                        RawAction(Icons.Rounded.ContentCopy, stringResource(Res.string.raw_copy), actions::onCopyDump)
                        RawAction(Icons.Rounded.Save, stringResource(Res.string.raw_save), actions::onSaveDump)
                    }
                }
            }
        }
    }
}

@Composable
private fun RawAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, contentPadding = ButtonDefaults.ButtonWithIconContentPadding) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(label)
    }
}
