package com.cardcue.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BillRepository(private val billDao: BillDao, private val cardDao: CardDao) {
    val allBills: Flow<List<BillEntity>> = billDao.getAllBills()
    val allCards: Flow<List<CardEntity>> = cardDao.getAllCards()
    val cardsWithBills: Flow<List<CardWithBills>> = cardDao.getCardsWithBills()

    // Bill Operations
    suspend fun insertBill(bill: BillEntity) = billDao.insertBill(bill)
    suspend fun updateBill(bill: BillEntity) = billDao.updateBill(bill)
    suspend fun deleteBill(bill: BillEntity) = billDao.deleteBill(bill)
    suspend fun getBillById(id: Int): BillEntity? = billDao.getBillById(id)

    fun getBillsForCard(cardId: Int): Flow<List<BillEntity>> = billDao.getBillsForCard(cardId)
    fun getUnpaidBills(): Flow<List<BillEntity>> = billDao.getUnpaidBills()

    // Card Operations
    suspend fun insertCard(card: CardEntity): Long = cardDao.insertCard(card)
    suspend fun updateCard(card: CardEntity) = cardDao.updateCard(card)
    suspend fun deleteCard(card: CardEntity) = cardDao.deleteCard(card)
    suspend fun getCardById(id: Int): CardEntity? = cardDao.getCardById(id)
}
