package st.misa.menzomat.card.data.nfc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import okio.IOException
import st.misa.menzomat.card.domain.nfc.UnsupportedTagException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsoDepConnectionTest {

    /** What android.nfc.Tag remembers: whether some technology is connected. Every IsoDep of one Tag shares it. */
    private class TagState(var connected: Boolean = false)

    /** Follows the rules of android.nfc.tech.BasicTagTechnology. */
    private class FakeLink(
        private val tag: TagState,
        private val onTransceive: () -> Unit = {},
        private val connectError: IOException? = null,
    ) : IsoDepLink {
        override fun connect() {
            connectError?.let { throw it }
            if (tag.connected) throw IOException("Only one TagTechnology can be connected at a time.")
            tag.connected = true
        }

        override fun setTimeout(millis: Int) = Unit

        override fun transceive(apdu: ByteArray): ByteArray {
            check(tag.connected) { "Call connect() first!" }
            onTransceive()
            return apdu.reversedArray()
        }

        override fun close() {
            tag.connected = false
        }
    }

    @Test
    fun `a connected tag is read and closed afterwards`() = runBlocking {
        val tag = TagState()
        val answer = withIsoDep(Mutex(), { FakeLink(tag) }) { it.transceive(byteArrayOf(1, 2)) }
        assertContentEquals(byteArrayOf(2, 1), answer)
        assertFalse(tag.connected)
    }

    @Test
    fun `a tag that android still counts as connected is used anyway and reset afterwards`() = runBlocking {
        val tag = TagState(connected = true)
        val answer = withIsoDep(Mutex(), { FakeLink(tag) }) { it.transceive(byteArrayOf(1, 2)) }
        assertContentEquals(byteArrayOf(2, 1), answer)
        assertFalse(tag.connected, "closing must clear the stale connection")
    }

    @Test
    fun `reads of the same tag never run at the same time`() = runBlocking {
        val tag = TagState()
        val lock = Mutex()
        val running = AtomicInteger()
        val peak = AtomicInteger()
        val link = {
            FakeLink(tag, onTransceive = {
                peak.accumulateAndGet(running.incrementAndGet(), ::maxOf)
                Thread.sleep(50)
                running.decrementAndGet()
            })
        }

        val reads = List(3) { async(Dispatchers.IO) { withIsoDep(lock, link) { it.transceive(byteArrayOf(7)) } } }
        reads.awaitAll()

        assertEquals(1, peak.get())
        assertFalse(tag.connected)
    }

    @Test
    fun `other connect failures are not swallowed`() = runBlocking<Unit> {
        val tag = TagState()
        val failure = assertFailsWith<IOException> {
            withIsoDep(Mutex(), { FakeLink(tag, connectError = IOException("boom")) }) { it.transceive(byteArrayOf(1)) }
        }
        assertEquals("boom", failure.message)
    }

    @Test
    fun `a state error from the framework becomes an io error`() = runBlocking<Unit> {
        val tag = TagState()
        // The link was never connected because connect() was a no-op, so android would say "Call connect() first!".
        val link = object : IsoDepLink by FakeLink(tag) {
            override fun connect() = Unit
        }
        val failure = assertFailsWith<IOException> {
            withIsoDep(Mutex(), { link }) { it.transceive(byteArrayOf(1)) }
        }
        assertTrue(failure.message.orEmpty().contains("connect()"))
    }

    @Test
    fun `a tag without iso dep is unsupported`() = runBlocking<Unit> {
        assertFailsWith<UnsupportedTagException> {
            withIsoDep<Unit>(Mutex(), { null }) { }
        }
    }
}
