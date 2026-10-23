package st.misa.menzomat.card.presentation.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.card.domain.model.MealBalance
import st.misa.menzomat.card.domain.model.StudentCard
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.meals_breakfast
import st.misa.menzomat.shared.resources.meals_dinner
import st.misa.menzomat.shared.resources.meals_left_to_buy
import st.misa.menzomat.shared.resources.meals_line
import st.misa.menzomat.shared.resources.meals_lunch
import st.misa.menzomat.shared.resources.meals_next_month
import st.misa.menzomat.ui.components.MealCounter
import st.misa.menzomat.ui.platform.LocalPlatformServices
import st.misa.menzomat.ui.theme.spacing

@Composable
internal fun MealsHero(card: StudentCard) {
    val meals = card.meals ?: return
    val dates = LocalPlatformServices.current.dateFormatter
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.primaryContainer, contentColor = colors.onPrimaryContainer),
    ) {
        Column(Modifier.padding(spacing.large), verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
            card.balanceMonth?.let {
                Text(
                    dates.monthAndYear(it),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() },
                )
            }
            Row(Modifier.fillMaxWidth()) {
                MealCounter(stringResource(Res.string.meals_breakfast), meals.breakfast, Modifier.weight(1f), colors.onPrimaryContainer)
                MealCounter(stringResource(Res.string.meals_lunch), meals.lunch, Modifier.weight(1f), colors.onPrimaryContainer)
                MealCounter(stringResource(Res.string.meals_dinner), meals.dinner, Modifier.weight(1f), colors.onPrimaryContainer)
            }
            card.mealsLeftToBuy?.let {
                MealRow(Icons.Rounded.AddShoppingCart, stringResource(Res.string.meals_left_to_buy, it.summary()), it)
            }
            card.nextMonthMeals?.takeIf { !it.isEmpty }?.let {
                MealRow(Icons.Rounded.CalendarMonth, stringResource(Res.string.meals_next_month, it.summary()), it)
            }
        }
    }
}

/**
 * Three numbers under the three counters, marked by an icon at the start instead of a sentence.
 * Screen readers get the sentence through [description].
 */
@Composable
private fun MealRow(icon: ImageVector, description: String, meals: MealBalance) {
    Box(
        modifier = Modifier.fillMaxWidth().clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.CenterStart,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Row(Modifier.fillMaxWidth()) {
            listOf(meals.breakfast, meals.lunch, meals.dinner).forEach {
                Text(
                    it.toString(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun MealBalance.summary(): String = stringResource(Res.string.meals_line, breakfast, lunch, dinner)
