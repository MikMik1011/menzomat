package st.misa.menzomat.card.presentation.card.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.card.domain.model.StudentCard
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.faculty_number
import st.misa.menzomat.shared.resources.holder_unknown
import st.misa.menzomat.shared.resources.study_year
import st.misa.menzomat.ui.theme.spacing

@Composable
internal fun HolderHeader(card: StudentCard) {
    val name = listOfNotNull(card.holder?.firstName, card.holder?.lastName).joinToString(" ")
        .ifBlank { stringResource(Res.string.holder_unknown) }
    val institution = card.institution
    val details = listOfNotNull(
        institution?.faculty?.let { it.name ?: stringResource(Res.string.faculty_number, it.id) },
        institution?.studyYear?.let { stringResource(Res.string.study_year, it) },
        institution?.studentId,
    ).joinToString(" · ")
    Column(Modifier.padding(horizontal = MaterialTheme.spacing.small)) {
        Text(name, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
        if (details.isNotBlank()) Text(details, style = MaterialTheme.typography.titleMedium)
    }
}
