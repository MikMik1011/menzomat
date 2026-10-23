package st.misa.menzomat.card.presentation.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.nfc_disabled_action
import st.misa.menzomat.shared.resources.nfc_disabled_title
import st.misa.menzomat.ui.theme.spacing

@Composable
internal fun NfcDisabledCard(onTurnOn: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(MaterialTheme.spacing.medium), verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
            Text(stringResource(Res.string.nfc_disabled_title), style = MaterialTheme.typography.titleMedium)
            Button(onClick = onTurnOn) { Text(stringResource(Res.string.nfc_disabled_action)) }
        }
    }
}
