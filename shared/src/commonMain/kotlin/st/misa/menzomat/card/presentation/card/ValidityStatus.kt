package st.misa.menzomat.card.presentation.card

import st.misa.menzomat.card.domain.model.Validity
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

sealed interface ValidityStatus {
    data class Valid(val until: LocalDate) : ValidityStatus
    data class ExpiresSoon(val until: LocalDate, val daysLeft: Int) : ValidityStatus
    data class Expired(val until: LocalDate) : ValidityStatus
    data object NotValidated : ValidityStatus
}

const val EXPIRES_SOON_DAYS = 30

fun validityStatus(validity: Validity, today: LocalDate): ValidityStatus {
    val until = validity.validatedUntil ?: return ValidityStatus.NotValidated
    val daysLeft = today.daysUntil(until)
    return when {
        daysLeft < 0 -> ValidityStatus.Expired(until)
        daysLeft <= EXPIRES_SOON_DAYS -> ValidityStatus.ExpiresSoon(until, daysLeft)
        else -> ValidityStatus.Valid(until)
    }
}
