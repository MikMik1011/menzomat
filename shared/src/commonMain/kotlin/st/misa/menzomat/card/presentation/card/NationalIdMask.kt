package st.misa.menzomat.card.presentation.card

/** The first seven digits of a JMBG are the date of birth and not sensitive; the rest identify the person. */
private const val PUBLIC_DIGITS = 7

fun maskNationalId(nationalId: String): String =
    nationalId.take(PUBLIC_DIGITS) + "•".repeat((nationalId.length - PUBLIC_DIGITS).coerceAtLeast(0))
