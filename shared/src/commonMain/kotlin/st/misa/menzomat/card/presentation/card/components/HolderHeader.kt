package st.misa.menzomat.card.presentation.card.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.card.domain.model.Faculty
import st.misa.menzomat.card.domain.model.StudentCard
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.faculty_number
import st.misa.menzomat.shared.resources.faculty_uns_ff
import st.misa.menzomat.shared.resources.faculty_uns_ftn
import st.misa.menzomat.shared.resources.faculty_uns_pmf
import st.misa.menzomat.shared.resources.holder_unknown
import st.misa.menzomat.shared.resources.study_year
import st.misa.menzomat.ui.theme.spacing

/** The name of a faculty the app knows, in the user's language. Any other faculty shows as "Faculty N". */
internal fun knownFacultyName(key: String): StringResource? = when (key) {
    "uns-ftn" -> Res.string.faculty_uns_ftn
    "uns-ff" -> Res.string.faculty_uns_ff
    "uns-pmf" -> Res.string.faculty_uns_pmf
    else -> null
}

@Composable
internal fun HolderHeader(card: StudentCard) {
    val name = listOfNotNull(card.holder?.firstName, card.holder?.lastName).joinToString(" ")
        .ifBlank { stringResource(Res.string.holder_unknown) }
    val institution = card.institution
    val faculty = institution?.faculty?.let { facultyName(it) }
    val studies = listOfNotNull(
        institution?.studyYear?.let { stringResource(Res.string.study_year, it) },
        institution?.studentId,
    ).joinToString(" · ")
    Column(Modifier.padding(horizontal = MaterialTheme.spacing.small)) {
        Text(name, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
        if (faculty != null) Text(faculty, style = MaterialTheme.typography.titleMedium)
        if (studies.isNotBlank()) Text(studies, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun facultyName(faculty: Faculty): String =
    faculty.key?.let(::knownFacultyName)?.let { stringResource(it) } ?: stringResource(Res.string.faculty_number, faculty.id)
