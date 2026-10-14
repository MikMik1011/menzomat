package st.misa.menzomat

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import st.misa.menzomat.di.androidPlatformModule
import st.misa.menzomat.di.sharedModule

class MenzomatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MenzomatApplication)
            modules(sharedModule, androidPlatformModule)
        }
    }
}
