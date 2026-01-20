package com.cardcue.app

import android.app.Application
import com.cardcue.app.data.AppDatabase
import com.cardcue.app.data.BillRepository
import com.cardcue.app.data.UserPreferencesRepository

class CardCueApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val billRepository by lazy { BillRepository(database.billDao()) }
    val userPreferencesRepository by lazy { UserPreferencesRepository(this) }
}
