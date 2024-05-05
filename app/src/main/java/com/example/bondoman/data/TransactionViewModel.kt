package com.example.bondoman.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application): AndroidViewModel(application) {

    val getAllTransactions: LiveData<List<Transaction>>
    private val repository: TransactionRepository

    init {
        val transactionDao = TransactionDatabase.getInstance(application).transactionDao()
        repository = TransactionRepository(transactionDao)
        getAllTransactions = repository.getAllTransactions
    }

    fun addTransaction(transaction: Transaction){
        viewModelScope.launch (Dispatchers.IO) {
            repository.addTransaction(transaction)
        }
    }

    fun getTransactionById(selectedTransactionId: Int): LiveData<Transaction> {
        return repository.getTransactionById(selectedTransactionId)
    }

    fun updateTransaction(updatedTransaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTransaction(updatedTransaction)
        }
    }

    fun deleteTransaction(transactionId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(transactionId)
        }
    }
}