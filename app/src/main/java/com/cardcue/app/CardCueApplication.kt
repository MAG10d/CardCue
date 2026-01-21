package com.cardcue.app

import android.app.Application
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cardcue.app.data.AppDatabase
import com.cardcue.app.data.BillRepository
import com.cardcue.app.data.UserPreferencesRepository
import com.cardcue.app.worker.BillReminderWorker
import java.util.concurrent.TimeUnit

class CardCueApplication : Application(), Configuration.Provider {

    // Database & Repositories (From Remote)
    val database by lazy { AppDatabase.getDatabase(this) }
    val billRepository by lazy { BillRepository(database.billDao()) }
    val userPreferencesRepository by lazy { UserPreferencesRepository(this) }

    override fun onCreate() {
        super.onCreate()
        setupWorker()
    }

    private fun setupWorker() {
        // Run daily
        val workRequest = PeriodicWorkRequestBuilder<BillReminderWorker>(1, TimeUnit.DAYS)
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