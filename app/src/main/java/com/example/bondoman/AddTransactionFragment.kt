package com.example.bondoman

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.bondoman.data.Transaction
import com.example.bondoman.data.TransactionViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Date


class AddTransactionFragment : Fragment() {

    private lateinit var mTransactionViewModel: TransactionViewModel
    private var editTextName: EditText? = null
    private var radioIncome: RadioButton? = null
    private var radioExpense: RadioButton? = null
    private var editTextPrice: EditText? = null
    private var editTextLocation: EditText? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    private var isFragmentInitialized = false
    private var source: String? = "crud"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_transaction, container, false)

        editTextName = view.findViewById(R.id.edit_text_name)
        radioIncome = view.findViewById(R.id.radio_income)
        radioExpense = view.findViewById(R.id.radio_expense)
        editTextPrice = view.findViewById(R.id.edit_text_price)
        editTextLocation = view.findViewById(R.id.edit_text_location)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isFragmentInitialized = true

        mTransactionViewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        view.findViewById<Button>(R.id.button_save).setOnClickListener {
            insertDataToDatabase()
        }

        setupLocationPermissionLauncher()
        getCurrentLocation()

        // Transaction from scanner
        arguments?.getDouble("price")?.let { price ->
            source = "scanner"
            editTextPrice?.setText(price.toString())
            radioExpense?.isChecked = true
            radioIncome?.isChecked = false
            editTextName?.setText("new_transaction")
        }

        arguments?.let {
            val transaction = it.getParcelable<Transaction>("new_transaction")
            if (transaction != null) {
                updateTextField(transaction)
            }
        }
    }

    private fun updateTextField(transaction: Transaction) {
        editTextName?.setText(transaction.name)
        radioIncome?.let {
            it.isChecked = transaction.category == "Pemasukan"
        }
        radioExpense?.let {
            it.isChecked = transaction.category != "Pemasukan"
        }
        editTextPrice?.setText(transaction.price.toString())
        editTextLocation?.setText(transaction.location)
    }


    private fun setupLocationPermissionLauncher() {
        locationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(requireContext(), "Izin Lokasi Ditolak", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun getCurrentLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val locationText = "${it.latitude}, ${it.longitude}"
                        editTextLocation?.setText(locationText)
                    } ?: run {
                        editTextLocation?.setText(getString(R.string.default_location))
                        Toast.makeText(requireContext(), "Gagal Mendapatkan Lokasi", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            editTextLocation?.setText(getString(R.string.default_location))
        }
    }

    private fun checkLocationPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun insertDataToDatabase() {
        val name = editTextName?.text.toString().trim()
        val category = if (radioIncome?.isChecked == true) "Pemasukan" else "Pengeluaran"
        val priceString = editTextPrice?.text.toString().trim()
        val location = editTextLocation?.text.toString().trim()

        val price = priceString.toDoubleOrNull()

        if (inputCheck(name, category, price, location)) {
            val transaction = Transaction(0, name, category, price!!, location, Date())
            mTransactionViewModel.addTransaction(transaction)

            Toast.makeText(requireContext(), "Transaksi Berhasil Ditambahkan", Toast.LENGTH_SHORT).show()

            // back to uploadbill
            if (source != "scanner"){
                findNavController().navigateUp()
            } else {
                parentFragmentManager.popBackStack()
                parentFragmentManager.popBackStack()
            }
        } else {
            Toast.makeText(requireContext(), "Mohon isi semua kolom dengan benar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun inputCheck(name: String, category: String, price: Double?, location: String): Boolean {
        return !(name.isEmpty() || category.isEmpty() || location.isEmpty() || price == null)
    }
}
