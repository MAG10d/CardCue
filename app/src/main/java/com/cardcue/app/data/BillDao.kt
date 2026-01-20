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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bill: CreditCardBill)

    @Update
    suspend fun update(bill: CreditCardBill)

    @Delete
    suspend fun delete(bill: CreditCardBill)

    @Query("SELECT * from bills WHERE id = :id")
    suspend fun getBill(id: Int): CreditCardBill?

    @Query("SELECT * from bills")
    fun getAllBills(): Flow<List<CreditCardBill>>
}
