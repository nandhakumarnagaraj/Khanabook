package com.khanabook.lite.pos

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.khanabook.lite.pos.data.local.DatabaseInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class KhanaBookApplication : Application(), Configuration.Provider {

    companion object {
        lateinit var instance: KhanaBookApplication
            private set
    }

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var databaseInitializer: DatabaseInitializer

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize Global Crash Handler
        com.khanabook.lite.pos.domain.util.GlobalCrashHandler.initialize(this)

        // Ensure unique device ID for sync logic
        val sessionManager = dagger.hilt.android.EntryPointAccessors.fromApplication(this, com.khanabook.lite.pos.di.SessionManagerEntryPoint::class.java).sessionManager()
        if (sessionManager.getDeviceId() == "default_device") {
            sessionManager.saveDeviceId(java.util.UUID.randomUUID().toString())
        }

        // Schedule Periodic Sync
        com.khanabook.lite.pos.worker.MasterSyncWorker.schedule(this)

        // Initialize sample data on startup
        val exceptionHandler =
                kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
                    Log.e("KhanaBookApp", "Coroutine Exception", throwable)
                }

        MainScope().launch(exceptionHandler) {
            try {
                if (::databaseInitializer.isInitialized) {
                    databaseInitializer.initialize()
                }
            } catch (e: Exception) {
                Log.e("KhanaBookApp", "Failed to initialize database", e)
            }
        }
    }
}
