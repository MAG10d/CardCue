package com.cardcue.app

import android.app.Application
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cardcue.app.data.AppContainer
import com.cardcue.app.data.AppDataContainer
import com.cardcue.app.worker.BillReminderWorker
import java.util.concurrent.TimeUnit

class CardCueApplication : Application(), Configuration.Provider {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        setupWorker()
    }

    private fun setupWorker() {
        val workRequest = PeriodicWorkRequestBuilder<BillReminderWorker>(1, TimeUnit.DAYS)
            // Ideally start at 8 AM, but for now periodic daily is fine.
            // WorkManager handles constraints and battery optimizations.
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "BillReminderWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
