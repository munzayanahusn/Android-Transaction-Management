package com.example.bondoman

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.bondoman.data.Transaction
import com.example.bondoman.databinding.TransactionRowBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ListTransactionAdapter : RecyclerView.Adapter<ListTransactionAdapter.MyViewHolder>() {

    private var transactionList = emptyList<Transaction>()

    inner class MyViewHolder(private val binding: TransactionRowBinding) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault())

        init {
            binding.root.setOnClickListener {
                val transaction = transactionList[adapterPosition]
                transaction.location?.let { it1 -> openGoogleMaps(it1) }
            }

            binding.updateButton.setOnClickListener {
                val transaction = transactionList[adapterPosition]
                val action = ListTransactionFragmentDirections.actionListTransactionFragmentToUpdateTransactionFragment(transaction)
                itemView.findNavController().navigate(action)
            }
        }

        fun bind(transaction: Transaction) {
            binding.apply {
                idTransaction.text = transaction.id.toString()
                namaTxt.text = transaction.name
                kategoriTxt.text = transaction.category
                priceTxt.text = formatToIDR(transaction.price)
                lokasiTxt.text = transaction.location
                tanggalTxt.text = dateFormat.format(transaction.date)
            }
        }

        private fun formatToIDR(price: Double): String {
            val localeID = Locale("id", "ID")
            val formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(localeID)
            return formatRupiah.format(price)
        }

        private fun openGoogleMaps(location: String) {
            val coordinates = location.split(", ")
            if (coordinates.size < 2) {
                Toast.makeText(itemView.context, "Format Lokasi Tidak Didukung", Toast.LENGTH_SHORT).show()
                return
            }

            val latitude = coordinates[0]
            val longitude = coordinates[1]
            Log.d("TAG", "Location latitude: $latitude")
            Log.d("TAG", "Location longitude: $longitude")

            val uri = "geo:$latitude,$longitude?q=$latitude,$longitude"
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

            mapIntent.setPackage("com.google.android.apps.maps")

            val packageManager = itemView.context.packageManager
            if (packageManager != null && mapIntent.resolveActivity(packageManager) != null) {
                itemView.context.startActivity(mapIntent)
            } else {
                Toast.makeText(itemView.context, "Google Maps tidak tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = TransactionRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(transactionList[position])
    }

    fun setData(transactionList: List<Transaction>) {
        this.transactionList = transactionList
        notifyDataSetChanged()
    }
}