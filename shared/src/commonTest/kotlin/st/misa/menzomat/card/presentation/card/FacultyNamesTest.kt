package st.misa.menzomat.card.presentation.card

import st.misa.menzomat.card.data.profile.novisad.NoviSadLayout
import st.misa.menzomat.card.presentation.card.components.knownFacultyName
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FacultyNamesTest {
    @Test
    fun `every faculty the novi sad layout knows has a name in both languages`() {
        NoviSadLayout.facultyKeys.values.forEach { assertNotNull(knownFacultyName(it), "no name resource for $it") }
    }

    @Test
    fun `a faculty the app does not know has no name`() {
        assertNull(knownFacultyName("somewhere-else"))
    }
}
