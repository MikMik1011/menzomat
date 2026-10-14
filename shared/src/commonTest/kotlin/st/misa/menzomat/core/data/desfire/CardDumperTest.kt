package st.misa.menzomat.core.data.desfire

import kotlinx.coroutines.test.runTest
import st.misa.menzomat.core.domain.desfire.Aid
import st.misa.menzomat.core.domain.desfire.DesfireError
import st.misa.menzomat.core.domain.desfire.FileType
import st.misa.menzomat.core.domain.desfire.Transceiver
import st.misa.menzomat.fake.FakeDesfireCard
import st.misa.menzomat.fake.FakeDesfireCard.FakeFile
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CardDumperTest {
    private val aid = Aid(0x00000D)
    private val big = ByteArray(256) { it.toByte() }

    private fun card() = FakeDesfireCard(
        mapOf(
            Aid.PICC to emptyMap(),
            aid to mapOf(
                0 to FakeFile.Data(type = 0, bytes = big),
                1 to FakeFile.Data(type = 1, bytes = byteArrayOf(1, 2, 3)),
                3 to FakeFile.Value(value = 981, readable = false),
                4 to FakeFile.Data(type = 1, bytes = byteArrayOf(9), readable = false),
            ),
        ),
    )

    @Test
    fun `application ids are read least significant byte first`() = runTest {
        val ids = IsoDesfireClient(card()).getApplicationIds()
        assertEquals(listOf(Aid.PICC, aid), ids)
    }

    @Test
    fun `selecting an application sends its wire bytes`() = runTest {
        val fake = card()
        IsoDesfireClient(fake).selectApplication(aid)
        assertEquals("905a00000" + "3" + "0d000000", fake.sent.single())
    }

    @Test
    fun `long files are reassembled from additional frames`() = runTest {
        val fake = card()
        val client = IsoDesfireClient(fake)
        client.selectApplication(aid)
        assertContentEquals(big, client.readData(0))
        assertTrue(fake.sent.count { it.startsWith("90af") } >= 4)
    }

    @Test
    fun `dump keeps readable files and records the ones that refuse`() = runTest {
        val dump = CardDumper(IsoDesfireClient(card()), now = { 42L }).dump("04AABB")

        assertEquals(42L, dump.readAtEpochMillis)
        assertNull(dump.app(Aid.PICC), "PICC is not dumped")
        val app = assertNotNull(dump.app(aid))
        assertContentEquals(big, app.file(0)?.data)
        assertEquals(FileType.Backup, app.file(1)?.type)
        assertContentEquals(byteArrayOf(1, 2, 3), app.file(1)?.data)

        val value = assertNotNull(app.file(3))
        assertEquals(FileType.Value, value.type)
        assertNull(value.data)
        assertNotNull(value.error)

        assertNotNull(app.file(4)?.error)
    }

    @Test
    fun `value files are stored as four little endian bytes`() = runTest {
        val readable = FakeDesfireCard(mapOf(aid to mapOf(3 to FakeFile.Value(981, readable = true))))
        val file = CardDumper(IsoDesfireClient(readable)).dump("00").app(aid)!!.file(3)!!
        assertContentEquals(byteArrayOf(0xD5.toByte(), 0x03, 0, 0), file.data)
    }

    @Test
    fun `a non desfire answer is reported as malformed`() = runTest {
        val client = IsoDesfireClient { byteArrayOf(0x6A, 0x82.toByte()) }
        val error = runCatching { client.getApplicationIds() }.exceptionOrNull()
        assertTrue(error is DesfireError.Malformed)
    }

    @Test
    fun `unknown application is skipped without aborting the dump`() = runTest {
        val picky = object : Transceiver {
            val inner = card()
            override suspend fun transceive(apdu: ByteArray): ByteArray =
                if ((apdu[1].toInt() and 0xFF) == 0x5A) byteArrayOf(0x91.toByte(), 0xA0.toByte())
                else inner.transceive(apdu)
        }
        val dump = CardDumper(IsoDesfireClient(picky)).dump("00")
        assertNotNull(dump.app(aid)?.error)
    }
}
