package st.misa.menzomat.card.presentation.card

import kotlin.test.Test
import kotlin.test.assertEquals

class NationalIdMaskTest {
    @Test
    fun `keeps the seven date of birth digits and hides the other six`() =
        assertEquals("0101990••••••", maskNationalId("0101990123456"))

    @Test
    fun `a short value is never padded`() = assertEquals("12345", maskNationalId("12345"))

    @Test
    fun `an empty value stays empty`() = assertEquals("", maskNationalId(""))
}
