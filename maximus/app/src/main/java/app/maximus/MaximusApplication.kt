package app.maximus

import android.app.Application
import app.maximus.core.memory.HeavyResourceGovernor
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MaximusApplication : Application() {

    @Inject
    lateinit var governor: HeavyResourceGovernor

    override fun onCreate() {
        System.loadLibrary("sqlcipher")
        super.onCreate()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        governor.onTrimMemory(level)
    }
}
