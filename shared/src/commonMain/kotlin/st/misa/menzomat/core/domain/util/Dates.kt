package st.misa.menzomat.core.domain.util

import kotlinx.datetime.LocalDate

/** Terminals disagree on whether a stored month is 1-based (January = 1) or 0-based (January = 0). */
enum class MonthBase(internal val shift: Int) {
    OneBased(0),
    ZeroBased(1),
}

/** Cards store years as two digits; 2000 + [twoDigitYear]. Returns null for impossible dates. */
fun dateOrNull(day: Int, month: Int, twoDigitYear: Int, base: MonthBase = MonthBase.OneBased): LocalDate? =
    try {
        LocalDate(2000 + twoDigitYear, month + base.shift, day)
    } catch (e: IllegalArgumentException) {
        null
    }
