package st.misa.menzomat.card.presentation.card

import androidx.compose.runtime.Stable
import st.misa.menzomat.card.domain.model.ProfileSelection

@Stable
interface CardActions {
    fun onSelectProfile(selection: ProfileSelection)
    fun onOpenNfcSettings()
    fun onShareDump()
    fun onCopyDump()
    fun onSaveDump()
    fun onDismissError()
}

/** For previews and tests. */
object NoCardActions : CardActions {
    override fun onSelectProfile(selection: ProfileSelection) = Unit
    override fun onOpenNfcSettings() = Unit
    override fun onShareDump() = Unit
    override fun onCopyDump() = Unit
    override fun onSaveDump() = Unit
    override fun onDismissError() = Unit
}
