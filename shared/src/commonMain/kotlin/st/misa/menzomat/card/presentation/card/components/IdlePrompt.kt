package st.misa.menzomat.card.presentation.card.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.prompt_title
import st.misa.menzomat.shared.resources.reading_title
import st.misa.menzomat.ui.components.NfcPrompt

@Composable
internal fun IdlePrompt(isReading: Boolean) {
    NfcPrompt(
        title = stringResource(if (isReading) Res.string.reading_title else Res.string.prompt_title),
        isReading = isReading,
    )
}
