package st.misa.menzomat.card.domain.profile

import st.misa.menzomat.card.domain.model.DecodeWarning
import st.misa.menzomat.card.domain.model.WarningReason
import st.misa.menzomat.core.domain.util.DecodeException

/** Collects warnings while a profile decodes, so one broken file does not take the rest down. */
class DecodeScope {
    private val collected = mutableListOf<DecodeWarning>()

    val warnings: List<DecodeWarning> get() = collected

    fun warn(field: String, reason: WarningReason) {
        collected += DecodeWarning(field, reason)
    }

    /** Runs [block]; on failure records a warning for [field] and returns null. */
    inline fun <T> field(field: String, block: () -> T?): T? = try {
        block()
    } catch (e: DecodeException) {
        warn(field, WarningReason.Truncated)
        null
    } catch (e: IndexOutOfBoundsException) {
        warn(field, WarningReason.Truncated)
        null
    }
}
