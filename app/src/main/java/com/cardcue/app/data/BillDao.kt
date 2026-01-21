package com.cardcue.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY dueDate ASC")
    fun getAllBills(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE cardId = :cardId ORDER BY dueDate DESC")
    fun getBillsForCard(cardId: Int): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE isPaid = 0")
    fun getUnpaidBills(): Flow<List<BillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity)

    @Update
    suspend fun updateBill(bill: BillEntity)

    @Delete
    suspend fun deleteBill(bill: BillEntity)

    @Query("SELECT * FROM bills WHERE id = :id")
    suspend fun getBillById(id: Int): BillEntity?
}
