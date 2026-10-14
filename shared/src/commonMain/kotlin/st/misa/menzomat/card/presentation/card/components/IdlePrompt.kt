package st.misa.menzomat.card.presentation.card.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.prompt_subtitle
import st.misa.menzomat.shared.resources.prompt_title
import st.misa.menzomat.shared.resources.reading_subtitle
import st.misa.menzomat.shared.resources.reading_title
import st.misa.menzomat.ui.components.NfcPrompt

@Composable
internal fun IdlePrompt(isReading: Boolean) {
    if (isReading) {
        NfcPrompt(stringResource(Res.string.reading_title), stringResource(Res.string.reading_subtitle), isReading = true)
    } else {
        NfcPrompt(stringResource(Res.string.prompt_title), stringResource(Res.string.prompt_subtitle), isReading = false)
    }
}
