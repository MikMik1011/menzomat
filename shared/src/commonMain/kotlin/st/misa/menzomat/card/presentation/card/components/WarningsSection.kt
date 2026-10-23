package st.misa.menzomat.card.presentation.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.card.domain.model.StudentCard
import st.misa.menzomat.card.domain.model.WarningReason
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.warning_invalid
import st.misa.menzomat.shared.resources.warning_line
import st.misa.menzomat.shared.resources.warning_missing
import st.misa.menzomat.shared.resources.warning_truncated
import st.misa.menzomat.shared.resources.warning_unreadable
import st.misa.menzomat.shared.resources.warning_unreadable_unknown
import st.misa.menzomat.shared.resources.warnings_title
import st.misa.menzomat.ui.theme.spacing

@Composable
internal fun WarningsSection(card: StudentCard) {
    if (card.warnings.isEmpty()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(MaterialTheme.spacing.medium), verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
                Icon(Icons.Rounded.Warning, contentDescription = null)
                Text(
                    stringResource(Res.string.warnings_title),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.semantics { heading() },
                )
            }
            card.warnings.forEach {
                Text(stringResource(Res.string.warning_line, it.field, reasonText(it.reason)), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun reasonText(reason: WarningReason): String = when (reason) {
    WarningReason.Truncated -> stringResource(Res.string.warning_truncated)
    WarningReason.Missing -> stringResource(Res.string.warning_missing)
    is WarningReason.Invalid -> stringResource(Res.string.warning_invalid, reason.detail)
    is WarningReason.Unreadable ->
        reason.detail?.let { stringResource(Res.string.warning_unreadable, it) } ?: stringResource(Res.string.warning_unreadable_unknown)
}
