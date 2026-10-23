package st.misa.menzomat.card.presentation.card.components

import androidx.compose.runtime.Composable
import st.misa.menzomat.card.domain.model.StudentCard
import st.misa.menzomat.ui.components.InfoCard
import st.misa.menzomat.ui.components.InfoRow

@Composable
internal fun ExtrasSection(card: StudentCard) {
    if (card.extras.isEmpty()) return
    InfoCard {
        card.extras.forEach { InfoRow(it.label, it.value) }
    }
}
