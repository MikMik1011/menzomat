package st.misa.menzomat.card.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import st.misa.menzomat.card.data.local.LastDumpStore
import st.misa.menzomat.card.domain.model.CardReadError
import st.misa.menzomat.card.domain.model.ScanEvent
import st.misa.menzomat.card.domain.nfc.TagLostException
import st.misa.menzomat.card.domain.nfc.UnsupportedTagException
import st.misa.menzomat.core.domain.desfire.Aid
import st.misa.menzomat.core.domain.desfire.DesfireError
import st.misa.menzomat.fake.FakeDesfireCard
import st.misa.menzomat.fake.FakeNfcReader
import st.misa.menzomat.fake.FakeTag
import st.misa.menzomat.fake.tempFilePath
import st.misa.menzomat.fake.canteenAid
import st.misa.menzomat.fake.canteenCard
import okio.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultCardRepositoryTest {

    private class Harness(private val scope: TestScope) {
        val reader = FakeNfcReader()
        val store = LastDumpStore(
            PreferenceDataStoreFactory.createWithPath(
                scope = scope.backgroundScope,
                produceFile = { tempFilePath("last.preferences_pb") },
            ),
        )
        val repository = DefaultCardRepository(reader, store, UnconfinedTestDispatcher(scope.testScheduler))
        val events = mutableListOf<ScanEvent>()

        init {
            scope.backgroundScope.launch(UnconfinedTestDispatcher(scope.testScheduler)) {
                repository.scans.collect { events += it }
            }
        }

        /** DataStore touches real files, so wait in wall-clock time for the two events a read produces. */
        suspend fun tap(tag: FakeTag) {
            val expected = events.size + 2
            reader.tagFlow.emit(tag)
            withContext(Dispatchers.Default) {
                withTimeout(5.seconds) {
                    while (events.size < expected) {
                        scope.testScheduler.advanceUntilIdle()
                        delay(10)
                    }
                }
            }
        }
    }

    @Test
    fun `a good card emits started then completed and is stored`() = runTest {
        val h = Harness(this)
        h.tap(FakeTag.withCard(canteenCard()))

        assertEquals(ScanEvent.Started, h.events[0])
        val completed = assertIs<ScanEvent.Completed>(h.events[1])
        assertEquals("04AABBCC", completed.dump.uid)
        assertEquals(1, completed.dump.app(canteenAid)?.files?.size)
        assertEquals(completed.dump, h.repository.lastDump.first())
    }

    @Test
    fun `clearing the last dump leaves no card to show`() = runTest {
        val h = Harness(this)
        h.tap(FakeTag.withCard(canteenCard()))
        assertNotNull(h.repository.lastDump.first())

        h.repository.clearLastDump()

        assertNull(h.repository.lastDump.first())
    }

    @Test
    fun `a tag that is not iso dep is reported as not desfire`() = runTest {
        val h = Harness(this)
        h.tap(FakeTag.failing(UnsupportedTagException()))
        assertEquals(ScanEvent.Failed(CardReadError.NotDesfire), h.events.last())
        assertNull(h.repository.lastDump.first())
    }

    @Test
    fun `a non desfire answer is reported as not desfire`() = runTest {
        val h = Harness(this)
        h.tap(FakeTag.withCard { byteArrayOf(0x6A, 0x82.toByte()) })
        assertEquals(ScanEvent.Failed(CardReadError.NotDesfire), h.events.last())
    }

    @Test
    fun `losing the card mid read is reported as tag lost and keeps the previous dump`() = runTest {
        val h = Harness(this)
        h.tap(FakeTag.withCard(canteenCard()))
        val previous = h.repository.lastDump.first()
        assertNotNull(previous)

        h.tap(FakeTag.failing(TagLostException("gone")))
        assertEquals(ScanEvent.Failed(CardReadError.TagLost), h.events.last())
        assertEquals(previous, h.repository.lastDump.first())
    }

    @Test
    fun `other io failures carry their message`() = runTest {
        val h = Harness(this)
        h.tap(FakeTag.failing(IOException("timeout")))
        assertEquals(ScanEvent.Failed(CardReadError.Communication("timeout")), h.events.last())
    }

    @Test
    fun `a card with no applications is reported`() = runTest {
        val h = Harness(this)
        h.tap(FakeTag.withCard(FakeDesfireCard(mapOf(Aid.PICC to emptyMap()))))
        assertEquals(ScanEvent.Failed(CardReadError.NoApplications), h.events.last())
    }

    @Test
    fun `a desfire status error aborts the read with a communication error`() = runTest {
        val h = Harness(this)
        h.tap(FakeTag.failing(DesfireError.Status(0x1C)))
        assertIs<ScanEvent.Failed>(h.events.last())
        assertIs<CardReadError.Communication>((h.events.last() as ScanEvent.Failed).error)
    }
}
