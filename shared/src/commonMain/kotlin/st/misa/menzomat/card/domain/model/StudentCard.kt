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
    val faculty: Faculty? = null,
    val studyYear: Int? = null,
    val studentId: String? = null,
)

/** The card names a faculty by number. [key] says which faculty it is when the profile knows, and the app words it. */
data class Faculty(val id: Int, val key: String? = null)

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

/**
 * Escape hatch for data that only one university's cards carry. The app words a known [key] in the
 * user's language and shows [label] for any other, so keep the value free of language.
 */
data class ExtraField(
    val key: String,
    val label: String,
    val value: ExtraValue,
)

sealed interface ExtraValue {
    data class Text(val text: String) : ExtraValue
    data class Date(val date: LocalDate) : ExtraValue

    /** The card says there is nothing, such as a check-up that was never done. */
    data object Absent : ExtraValue
}

data class DecodeWarning(
    val field: String,
    val reason: WarningReason,
)

/** Why a field could not be decoded. Free of language; the app words it. */
sealed interface WarningReason {
    /** The file ended before the field did. */
    data object Truncated : WarningReason

    /** The file is not on the card. */
    data object Missing : WarningReason

    /** The bytes decode to something impossible; [detail] shows the value. */
    data class Invalid(val detail: String) : WarningReason

    /** The card refused the read; [detail] is the card's own reason. */
    data class Unreadable(val detail: String?) : WarningReason
}
