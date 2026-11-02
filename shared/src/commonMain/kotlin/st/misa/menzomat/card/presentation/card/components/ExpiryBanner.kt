package st.misa.menzomat.card.presentation.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.card.domain.model.Validity
import st.misa.menzomat.card.presentation.card.ExpiryWarning
import st.misa.menzomat.card.presentation.card.expiryWarning
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.expiry_ago
import st.misa.menzomat.shared.resources.expiry_soon
import st.misa.menzomat.shared.resources.expiry_today
import st.misa.menzomat.shared.resources.expiry_yesterday
import st.misa.menzomat.ui.theme.spacing
import st.misa.menzomat.ui.theme.warning
import st.misa.menzomat.ui.theme.spacing
import kotlin.time.Clock

/** A card that has expired, or soon will, says how long ago or how soon, in a calm warning tint. Nothing is shown for a valid card. */
@Composable
internal fun ExpiryBanner(validity: Validity?, today: LocalDate = rememberToday()) {
    val expiry = expiryWarning(validity, today) ?: return
    val warning = MaterialTheme.warning
    val icon = if (expiry is ExpiryWarning.Expired) Icons.Rounded.Warning else Icons.Rounded.Schedule
    val text = when (expiry) {
        is ExpiryWarning.Expired ->
            if (expiry.daysAgo == 1) {
                stringResource(Res.string.expiry_yesterday)
            } else {
                pluralStringResource(Res.plurals.expiry_ago, expiry.daysAgo, expiry.daysAgo)
            }
        is ExpiryWarning.ExpiresSoon ->
            if (expiry.daysLeft == 0) {
                stringResource(Res.string.expiry_today)
            } else {
                pluralStringResource(Res.plurals.expiry_soon, expiry.daysLeft, expiry.daysLeft)
            }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = warning.container, contentColor = warning.content),
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            Icon(icon, contentDescription = null, tint = warning.icon)
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun rememberToday(): LocalDate = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
