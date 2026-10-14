package st.misa.menzomat.core.domain.util

/** Upper-case hex digits of this number, zero padded to [width]: `5.hex(2) == "05"`. */
fun Int.hex(width: Int): String = toString(16).uppercase().padStart(width, '0')
