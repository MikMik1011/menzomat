package st.misa.menzomat.card.presentation.card.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.profile_low_confidence
import st.misa.menzomat.ui.theme.spacing

@Composable
internal fun LowConfidenceNote(profileName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Row(Modifier.padding(MaterialTheme.spacing.medium), horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
            Icon(Icons.Rounded.Warning, contentDescription = null)
            Text(stringResource(Res.string.profile_low_confidence, profileName), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
