package st.misa.menzomat.ui.platform

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okio.IOException

/**
 * Android's "save as" dialog: the user picks the folder and may change the suggested name, and the system
 * grants write access to that one file. Created in the activity's `onCreate`, because the result
 * launcher has to be registered before the activity starts.
 */
internal class AndroidFileSaver(private val activity: ComponentActivity) : FileSaver {
    private var chosen: CompletableDeferred<Uri?>? = null
    private val oneDialogAtATime = Mutex()

    private val launcher = activity.registerForActivityResult(ActivityResultContracts.CreateDocument(MIME_TYPE)) { uri ->
        chosen?.complete(uri)
    }

    override suspend fun saveText(suggestedName: String, text: String): Boolean = oneDialogAtATime.withLock {
        val answer = CompletableDeferred<Uri?>().also { chosen = it }
        launcher.launch(suggestedName)
        val uri = answer.await() ?: return false
        withContext(Dispatchers.IO) {
            val output = activity.contentResolver.openOutputStream(uri, "wt") ?: throw IOException("Cannot open $uri")
            output.use { it.write(text.encodeToByteArray()) }
        }
        true
    }

    private companion object {
        const val MIME_TYPE = "application/json"
    }
}
