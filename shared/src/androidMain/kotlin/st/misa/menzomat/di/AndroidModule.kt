package st.misa.menzomat.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import st.misa.menzomat.card.data.nfc.AndroidNfcReader
import st.misa.menzomat.card.domain.nfc.NfcReader

/** What [sharedModule] needs from Android. */
val androidPlatformModule = module {
    single<CoroutineDispatcher>(IO) { Dispatchers.IO }
    single { PreferencesPath(androidContext().filesDir.resolve("menzomat.preferences_pb").absolutePath) }
    single { AndroidNfcReader(androidContext()) } bind NfcReader::class
}
