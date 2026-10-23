package st.misa.menzomat.card.presentation.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.card.presentation.card.CardActions
import st.misa.menzomat.card.presentation.card.CardContent
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.unrecognized_pick
import st.misa.menzomat.shared.resources.unrecognized_title
import st.misa.menzomat.ui.theme.spacing

@Composable
internal fun UnrecognizedCard(content: CardContent.Unrecognized, actions: CardActions, onPickProfile: () -> Unit) {
    val spacing = MaterialTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        ) {
            Column(Modifier.padding(spacing.medium), verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                Text(
                    stringResource(Res.string.unrecognized_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.semantics { heading() },
                )
                OutlinedButton(onClick = onPickProfile) { Text(stringResource(Res.string.unrecognized_pick)) }
            }
        }
        RawSection(content.dump, actions, initiallyExpanded = true)
    }
}
