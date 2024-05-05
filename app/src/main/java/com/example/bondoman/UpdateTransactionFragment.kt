package com.example.bondoman

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.bondoman.data.Transaction
import com.example.bondoman.data.TransactionViewModel
import com.example.bondoman.databinding.FragmentUpdateTransactionBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

class UpdateTransactionFragment : Fragment() {

    private lateinit var mTransactionViewModel: TransactionViewModel
    private lateinit var binding: FragmentUpdateTransactionBinding
    private val args: UpdateTransactionFragmentArgs by navArgs()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUpdateTransactionBinding.inflate(inflater, container, false)
        mTransactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val selectedTransactionId = args.currentTransaction.id

        if (selectedTransactionId != -1) {
            Log.d("UpdateTransactionFragment", "Selected Transaction ID: $selectedTransactionId")
            mTransactionViewModel.getTransactionById(selectedTransactionId)
                .observe(viewLifecycleOwner) { transaction ->
                    if (transaction != null) {
                        binding.editTextName.setText(transaction.name)
                        binding.editTextCategory.setText(transaction.category)
                        binding.editTextPrice.setText(transaction.price.toString())
                        binding.editTextLocation.setText(transaction.location)

                        val dateFormat =
                            SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault())
                        binding.editTextDate.setText(dateFormat.format(transaction.date))
                    } else {
                        Toast.makeText(requireContext(), "Transaksi Tidak Ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(requireContext(), "ID Transaksi Tidak Valid", Toast.LENGTH_SHORT).show()
        }

        binding.buttonSave.setOnClickListener {
            updateTransaction(selectedTransactionId)
        }

        binding.buttonDelete.setOnClickListener {
            deleteTransaction(selectedTransactionId)
        }

        binding.buttonUpdateLocation.setOnClickListener {
            updateLocation()
        }

        return binding.root
    }

    private fun updateTransaction(transactionId: Int) {
        val name = binding.editTextName.text.toString()
        val price = binding.editTextPrice.text.toString().toDoubleOrNull()
        val location = binding.editTextLocation.text.toString()
        val category = binding.editTextCategory.text.toString()
        val date = binding.editTextDate.text.toString()

        if (name.isNotEmpty() && price != null && location.isNotEmpty()) {
            val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault())
            val dateFormatted = dateFormat.parse(date)

            val updatedTransaction = Transaction(
                transactionId,
                name,
                category,
                price,
                location,
                dateFormatted ?: Date()
            )
            mTransactionViewModel.updateTransaction(updatedTransaction)

            Toast.makeText(requireContext(), "Transaksi Berhasil Diperbarui", Toast.LENGTH_SHORT).show()

            findNavController().navigateUp()
        } else {
            Toast.makeText(requireContext(), "Mohon isi semua kolom dengan benar", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun deleteTransaction(transactionId: Int) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.apply {
            setTitle("Hapus Transaksi")
            setMessage("Yakin ingin menghapus transaksi ini?")
            setPositiveButton("Yes") { _, _ ->
                mTransactionViewModel.deleteTransaction(transactionId)
                Toast.makeText(requireContext(), "Transaksi Berhasil Dihapus", Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigateUp()
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        }
        alertDialogBuilder.create().show()
    }

    private fun updateLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val locationText = "${it.latitude}, ${it.longitude}"
                        binding.editTextLocation.setText(locationText)
                    } ?: run {
                        binding.editTextLocation.setText(getString(R.string.default_location))
                        Toast.makeText(requireContext(), "Gagal Mendapatkan Lokasi", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            binding.editTextLocation.setText(getString(R.string.default_location))
        }
    }

    private fun checkLocationPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}