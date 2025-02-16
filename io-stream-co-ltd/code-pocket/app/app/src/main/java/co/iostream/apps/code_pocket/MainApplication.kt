package co.iostream.apps.code_pocket

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application(), LifecycleObserver {

    override fun onCreate() {
        super.onCreate()
    }
}