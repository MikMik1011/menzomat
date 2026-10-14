package st.misa.menzomat

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.koin.android.ext.android.inject
import st.misa.menzomat.card.data.nfc.AndroidNfcReader
import st.misa.menzomat.ui.platform.androidPlatformServices

class MainActivity : ComponentActivity() {
    private val nfcReader: AndroidNfcReader by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val platform = androidPlatformServices(this)
        setContent {
            MenzomatApp(
                platform = platform,
                onDarkTheme = { dark ->
                    // The system bars follow the app's own theme setting, not just the system's.
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                        navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                    )
                },
            )
        }
    }

    // Reader mode only works while this activity is in the foreground.
    override fun onResume() {
        super.onResume()
        nfcReader.enable(this)
    }

    override fun onPause() {
        nfcReader.disable(this)
        super.onPause()
    }
}
