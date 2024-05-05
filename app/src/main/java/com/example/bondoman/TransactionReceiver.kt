package com.example.bondoman

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.bondoman.data.Transaction

class TransactionReceiver(private val activity: MainActivity) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("Broadcast", "Broadcast Received")
        Toast.makeText(context, "Broadcast Diterima", Toast.LENGTH_SHORT).show()
        if (intent.action == "com.example.bondoman.NEW_TRANSACTION") {
            val transaction = intent.getParcelableExtra<Transaction>("new_transaction")
            if (transaction != null) {
                val bundle = Bundle().apply {
                    putParcelable("new_transaction", transaction)
                }
                Log.d("Broadcast", bundle.toString())

                activity.navigateToAddTransactionFragment(bundle)
            }
        }
    }
}