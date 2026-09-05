package com.example.runts

import android.app.Application
import com.example.runts.data.remote.database.NeonPostgresManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class RuntsApplication : Application(), androidx.work.Configuration.Provider {

    @Inject lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory
    override val workManagerConfiguration: androidx.work.Configuration
        get() = androidx.work.Configuration.Builder().setWorkerFactory(workerFactory).build()

    @Inject
    lateinit var neonPostgresManager: NeonPostgresManager

    override fun onCreate() {
        super.onCreate()
        com.example.runts.data.worker.SyncWorkManager(this).apply {
            schedulePeriodicSync()
            syncImmediately()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // A criação é idempotente e não insere dados fictícios no dispositivo do usuário.
                neonPostgresManager.initDatabaseTables()
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

}
