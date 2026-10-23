package st.misa.menzomat.card.presentation.card

import st.misa.menzomat.card.data.profile.novisad.NoviSadProfile
import st.misa.menzomat.card.data.profile.novisad.cardDump
import st.misa.menzomat.card.data.profile.novisad.currentCard
import st.misa.menzomat.card.presentation.card.components.knownExtraLabel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ExtraLabelsTest {
    @Test
    fun `every extra the novi sad profile reports has a label in both languages`() {
        val keys = NoviSadProfile().decode(cardDump(currentCard)).extras.map { it.key }
        assertEquals(listOf("medical-check", "transaction-counter"), keys)
        keys.forEach { assertNotNull(knownExtraLabel(it), "no label resource for $it") }
    }

    @Test
    fun `an extra the app does not know keeps the profile's own label`() {
        assertNull(knownExtraLabel("something-new"))
    }
}
