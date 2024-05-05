package com.example.bondoman.data

import androidx.lifecycle.LiveData

class TransactionRepository(private val transactionDao: TransactionDao) {
    val getAllTransactions: LiveData<List<Transaction>> = transactionDao.getAllTransactions()

    fun getTransactionById(id: Int) : LiveData<Transaction> = transactionDao.getTransactionById(id)
    suspend fun addTransaction(vararg tc: Transaction){
        transactionDao.addTransaction(*tc)
    }

    suspend fun updateTransaction(tc: Transaction){
        transactionDao.updateTransaction(tc)
    }

    suspend fun deleteTransaction(transactionId: Int) {
        transactionDao.deleteTransactionById(transactionId)
    }

}