package com.cardcue.app.data

import kotlinx.coroutines.flow.Flow

interface BillsRepository {
    fun getAllBillsStream(): Flow<List<CreditCardBill>>
    fun getBillStream(id: Int): Flow<CreditCardBill?>
    suspend fun insertBill(bill: CreditCardBill)
    suspend fun deleteBill(bill: CreditCardBill)
    suspend fun updateBill(bill: CreditCardBill)
}

class OfflineBillsRepository(private val billDao: BillDao) : BillsRepository {
    override fun getAllBillsStream(): Flow<List<CreditCardBill>> = billDao.getAllBills()

    override fun getBillStream(id: Int): Flow<CreditCardBill?> = billDao.getBill(id)

    override suspend fun insertBill(bill: CreditCardBill) = billDao.insert(bill)

    override suspend fun deleteBill(bill: CreditCardBill) = billDao.delete(bill)

    override suspend fun updateBill(bill: CreditCardBill) = billDao.update(bill)
}
