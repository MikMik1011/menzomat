package st.misa.menzomat.card.presentation.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.card.domain.model.MealBalance
import st.misa.menzomat.card.domain.model.StudentCard
import st.misa.menzomat.card.domain.model.Validity
import st.misa.menzomat.card.presentation.card.ValidityStatus
import st.misa.menzomat.card.presentation.card.validityStatus
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.meals_breakfast
import st.misa.menzomat.shared.resources.meals_dinner
import st.misa.menzomat.shared.resources.meals_left_to_buy
import st.misa.menzomat.shared.resources.meals_line
import st.misa.menzomat.shared.resources.meals_lunch
import st.misa.menzomat.shared.resources.meals_next_month
import st.misa.menzomat.shared.resources.validity_expired
import st.misa.menzomat.shared.resources.validity_expires_in_days
import st.misa.menzomat.shared.resources.validity_not_validated
import st.misa.menzomat.shared.resources.validity_valid
import st.misa.menzomat.ui.components.MealCounter
import st.misa.menzomat.ui.components.StatusKind
import st.misa.menzomat.ui.components.StatusPill
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.small), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    card.balanceMonth?.let {
                        Text(
                            dates.monthAndYear(it),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.semantics { heading() },
                        )
                    }
                }
                card.validity?.let { ValidityPill(it) }
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

@Composable
private fun ValidityPill(validity: Validity) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val dates = LocalPlatformServices.current.dateFormatter
    when (val status = validityStatus(validity, today)) {
        is ValidityStatus.Valid ->
            StatusPill(stringResource(Res.string.validity_valid, dates.mediumDate(status.until)), Icons.Rounded.CheckCircle, StatusKind.Good)
        is ValidityStatus.ExpiresSoon ->
            StatusPill(
                pluralStringResource(Res.plurals.validity_expires_in_days, status.daysLeft, status.daysLeft),
                Icons.Rounded.Schedule,
                StatusKind.Warning,
            )
        is ValidityStatus.Expired ->
            StatusPill(stringResource(Res.string.validity_expired, dates.mediumDate(status.until)), Icons.Rounded.Error, StatusKind.Bad)
        ValidityStatus.NotValidated ->
            StatusPill(stringResource(Res.string.validity_not_validated), Icons.Rounded.Schedule, StatusKind.Warning)
    }
}

