package st.misa.menzomat.card.presentation.card

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import st.misa.menzomat.card.domain.model.Validity

/** How many days before the last valid day the app starts to warn. */
const val EXPIRES_SOON_DAYS = 30

sealed interface ExpiryWarning {
    /** The card was valid until [on], which was [daysAgo] days ago. */
    data class Expired(val on: LocalDate, val daysAgo: Int) : ExpiryWarning

    /** The card is valid through [on], which is [daysLeft] days away (0 means today is the last day). */
    data class ExpiresSoon(val on: LocalDate, val daysLeft: Int) : ExpiryWarning
}

/** Null when the card is valid for longer than [EXPIRES_SOON_DAYS] or carries no date at all. */
fun expiryWarning(validity: Validity?, today: LocalDate): ExpiryWarning? {
    val on = validity?.expiresOn ?: return null
    val daysLeft = today.daysUntil(on)
    return when {
        daysLeft < 0 -> ExpiryWarning.Expired(on, daysAgo = -daysLeft)
        daysLeft <= EXPIRES_SOON_DAYS -> ExpiryWarning.ExpiresSoon(on, daysLeft)
        else -> null
    }
}
