package st.misa.menzomat.card.presentation.card.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.card.domain.model.StudentCard
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.cash_label
import st.misa.menzomat.shared.resources.cash_value
import st.misa.menzomat.shared.resources.last_transaction_label
import st.misa.menzomat.shared.resources.meal_times
import st.misa.menzomat.shared.resources.meals_breakfast
import st.misa.menzomat.shared.resources.meals_dinner
import st.misa.menzomat.shared.resources.meals_lunch
import st.misa.menzomat.shared.resources.transaction_purchase
import st.misa.menzomat.shared.resources.transaction_with_date
import st.misa.menzomat.ui.components.InfoRow
import st.misa.menzomat.ui.components.InfoCard
import st.misa.menzomat.ui.platform.LocalPlatformServices
import st.misa.menzomat.ui.theme.brand

@Composable
internal fun AccountSection(card: StudentCard) {
    val cash = card.cashBalanceRsd
    val transaction = card.lastTransaction
    if (cash == null && transaction == null) return
    InfoCard {
        cash?.let {
            InfoRow(
                label = stringResource(Res.string.cash_label),
                value = stringResource(Res.string.cash_value, it),
                trailing = { HighlightBadge() },
            )
        }
        transaction?.let { tx ->
            val dates = LocalPlatformServices.current.dateFormatter
            val eaten = buildList {
                if (tx.mealsEaten.breakfast > 0) add(stringResource(Res.string.meal_times, stringResource(Res.string.meals_breakfast), tx.mealsEaten.breakfast))
                if (tx.mealsEaten.lunch > 0) add(stringResource(Res.string.meal_times, stringResource(Res.string.meals_lunch), tx.mealsEaten.lunch))
                if (tx.mealsEaten.dinner > 0) add(stringResource(Res.string.meal_times, stringResource(Res.string.meals_dinner), tx.mealsEaten.dinner))
            }.joinToString(", ").ifEmpty { stringResource(Res.string.transaction_purchase) }
            val value = tx.date?.let { stringResource(Res.string.transaction_with_date, dates.mediumDate(it), eaten) } ?: eaten
            InfoRow(label = stringResource(Res.string.last_transaction_label), value = value)
        }
    }
}

@Composable
private fun HighlightBadge() {
    Surface(shape = CircleShape, color = MaterialTheme.brand.highlight, contentColor = MaterialTheme.brand.onHighlight) {
        Icon(Icons.Rounded.Payments, contentDescription = null, modifier = Modifier.padding(8.dp).size(20.dp))
    }
}
