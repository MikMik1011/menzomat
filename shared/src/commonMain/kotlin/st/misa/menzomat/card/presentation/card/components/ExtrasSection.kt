package st.misa.menzomat.card.presentation.card.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.card.domain.model.StudentCard
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.extras_title
import st.misa.menzomat.ui.components.InfoRow
import st.misa.menzomat.ui.components.SectionCard

@Composable
internal fun ExtrasSection(card: StudentCard) {
    if (card.extras.isEmpty()) return
    SectionCard(stringResource(Res.string.extras_title)) {
        card.extras.forEach { InfoRow(it.label, it.value) }
    }
}
