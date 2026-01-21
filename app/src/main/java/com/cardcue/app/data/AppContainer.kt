package com.cardcue.app.data

import android.content.Context

interface AppContainer {
    val billsRepository: BillsRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val billsRepository: BillsRepository by lazy {
        OfflineBillsRepository(AppDatabase.getDatabase(context).billDao())
    }
}
