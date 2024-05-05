package com.example.bondoman.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date desc")
    fun getAllTransactions(): LiveData<List<Transaction>>
    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionById(id: Int): LiveData<Transaction>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTransaction(vararg tc: Transaction)
    @Update
    fun updateTransaction(tc: Transaction)
    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransactionById(transactionId: Int)

}