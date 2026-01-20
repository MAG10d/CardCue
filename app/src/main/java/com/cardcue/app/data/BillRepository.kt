package com.cardcue.app.data

import kotlinx.coroutines.flow.Flow

class BillRepository(private val billDao: BillDao) {
    val allBills: Flow<List<BillEntity>> = billDao.getAllBills()

    suspend fun insert(bill: BillEntity) {
        billDao.insertBill(bill)
    }

    suspend fun update(bill: BillEntity) {
        billDao.updateBill(bill)
    }

    suspend fun delete(bill: BillEntity) {
        billDao.deleteBill(bill)
    }

    suspend fun getBillById(id: Int): BillEntity? {
        return billDao.getBillById(id)
    }
}
