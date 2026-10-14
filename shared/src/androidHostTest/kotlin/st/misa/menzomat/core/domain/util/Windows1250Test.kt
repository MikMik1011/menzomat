package st.misa.menzomat.core.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals

/** The common table is checked against the JVM's own windows-1250 decoder, which only a JVM test can reach. */
class Windows1250Test {
    @Test
    fun `every defined byte decodes like the jvm charset`() {
        val jvm = charset("windows-1250")
        val undefined = setOf(0x81, 0x83, 0x88, 0x90, 0x98)
        for (value in 0..255) {
            if (value in undefined) continue
            val bytes = byteArrayOf(value.toByte())
            assertEquals(String(bytes, jvm), Windows1250.decode(bytes), "byte 0x${value.hex(2)}")
        }
    }

    @Test
    fun `undefined bytes become the replacement character`() {
        assertEquals("�", Windows1250.decode(byteArrayOf(0x81.toByte())))
    }
}
