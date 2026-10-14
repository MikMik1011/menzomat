package st.misa.menzomat.card.domain.profile

import st.misa.menzomat.card.domain.model.ProfileSelection
import st.misa.menzomat.card.domain.model.StudentCard
import st.misa.menzomat.core.domain.desfire.CardDump
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ProfileResolverTest {
    private class FakeProfile(override val id: String, private val score: Int) : CardProfile {
        override val displayName = id.uppercase()
        override val shortName = id
        override fun score(dump: CardDump) = score
        override fun decode(dump: CardDump) = StudentCard()
    }

    private val dump = CardDump("00", 0, emptyList())
    private val ns = FakeProfile("ns", 90)
    private val bg = FakeProfile("bg", 40)
    private val other = FakeProfile("other", 0)
    private val resolver = ProfileResolver(ProfileRegistry(listOf(bg, other, ns)))

    @Test
    fun `auto picks the highest scoring profile`() {
        val result = assertIs<Resolution.Resolved>(resolver.resolve(dump, ProfileSelection.Auto))
        assertEquals("ns", result.profile.id)
        assertTrue(result.isAuto)
        assertEquals(listOf("ns", "bg", "other"), result.candidates.map { it.profile.id })
    }

    @Test
    fun `manual selection wins over a better scoring profile`() {
        val result = assertIs<Resolution.Resolved>(resolver.resolve(dump, ProfileSelection.Manual("bg")))
        assertEquals("bg", result.profile.id)
        assertFalse(result.isAuto)
        assertFalse(result.isLowConfidence)
    }

    @Test
    fun `manual selection of a profile that does not match is flagged`() {
        val result = assertIs<Resolution.Resolved>(resolver.resolve(dump, ProfileSelection.Manual("other")))
        assertTrue(result.isLowConfidence)
    }

    @Test
    fun `manual selection of an unknown profile falls back to detection`() {
        val result = assertIs<Resolution.Resolved>(resolver.resolve(dump, ProfileSelection.Manual("gone")))
        assertEquals("ns", result.profile.id)
        assertTrue(result.isAuto)
    }

    @Test
    fun `no match at all is unrecognized but still lists the candidates`() {
        val none = ProfileResolver(ProfileRegistry(listOf(other)))
        val result = assertIs<Resolution.Unrecognized>(none.resolve(dump, ProfileSelection.Auto))
        assertEquals(1, result.candidates.size)
    }
}
