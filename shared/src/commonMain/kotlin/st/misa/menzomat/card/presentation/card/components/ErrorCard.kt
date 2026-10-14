package st.misa.menzomat.card.presentation.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.card.domain.model.CardReadError
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.error_communication
import st.misa.menzomat.shared.resources.error_communication_unknown
import st.misa.menzomat.shared.resources.error_dismiss
import st.misa.menzomat.shared.resources.error_no_apps
import st.misa.menzomat.shared.resources.error_not_desfire
import st.misa.menzomat.shared.resources.error_tag_lost
import st.misa.menzomat.ui.theme.spacing

@Composable
internal fun ErrorCard(error: CardReadError, onDismiss: () -> Unit) {
    val message = when (error) {
        CardReadError.TagLost -> stringResource(Res.string.error_tag_lost)
        CardReadError.NotDesfire -> stringResource(Res.string.error_not_desfire)
        CardReadError.NoApplications -> stringResource(Res.string.error_no_apps)
        is CardReadError.Communication ->
            error.detail?.let { stringResource(Res.string.error_communication, it) }
                ?: stringResource(Res.string.error_communication_unknown)
    }
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.errorContainer, contentColor = colors.onErrorContainer),
    ) {
        Column(Modifier.padding(MaterialTheme.spacing.medium), verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
                Icon(Icons.Rounded.Error, contentDescription = null)
                Text(message, style = MaterialTheme.typography.bodyLarge)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text(stringResource(Res.string.error_dismiss)) }
            }
        }
    }
}
