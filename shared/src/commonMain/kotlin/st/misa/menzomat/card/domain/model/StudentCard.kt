package st.misa.menzomat.card.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/** University-independent view of a canteen card. Profiles fill what they know; everything else stays null. */
data class StudentCard(
    val holder: Holder? = null,
    val cardNumber: String? = null,
    val institution: Institution? = null,
    val validity: Validity? = null,
    val balanceMonth: YearMonth? = null,
    val meals: MealBalance? = null,
    val mealsLeftToBuy: MealBalance? = null,
    val nextMonthMeals: MealBalance? = null,
    val cashBalanceRsd: Int? = null,
    val lastTransaction: Transaction? = null,
    val extras: List<ExtraField> = emptyList(),
    val warnings: List<DecodeWarning> = emptyList(),
)

data class Holder(
    val firstName: String?,
    val lastName: String?,
)

data class Institution(
    val name: String,
    val faculty: String? = null,
    val studyYear: Int? = null,
    val studentId: String? = null,
)

data class Validity(
    val issuedOn: LocalDate?,
    /** Date printed into the card number record; present even before the yearly validation. */
    val cardValidUntil: LocalDate?,
    /** Null until the card is validated for the current academic year. */
    val validatedUntil: LocalDate?,
)

data class MealBalance(
    val breakfast: Int,
    val lunch: Int,
    val dinner: Int,
) {
    val total: Int get() = breakfast + lunch + dinner
    val isEmpty: Boolean get() = total == 0
}

data class Transaction(
    val date: LocalDate?,
    val mealsEaten: MealBalance,
)

/** Escape hatch for data that only one university's cards carry. */
data class ExtraField(
    val key: String,
    val label: String,
    val value: String,
)

data class DecodeWarning(
    val field: String,
    val message: String,
)
