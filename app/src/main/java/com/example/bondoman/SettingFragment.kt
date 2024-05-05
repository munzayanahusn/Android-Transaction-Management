package com.example.bondoman

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bondoman.data.Transaction
import com.example.bondoman.data.TransactionViewModel
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random


class SettingFragment : Fragment() {

    private lateinit var transactionViewModel: TransactionViewModel
    private var selectedFormat: String = "XLSX"
    private var emailAddress = ""
    private val subject = "Daftar Transaksi Bondoman"
    private var logoutListener: LogoutListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)
        emailAddress = getEmailFromSharedPreferences()
        transactionViewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        view.findViewById<Button>(R.id.download_button)?.setOnClickListener {
            showFileNameDialog()
        }

        view.findViewById<Button>(R.id.email_button)?.setOnClickListener {
            saveTransactionsToSpreadsheet(subject, selectedFormat, 2)
        }

        view.findViewById<Button>(R.id.random_button)?.setOnClickListener {
            broadcastRandomTransaction()
        }

        view.findViewById<Button>(R.id.logout)?.setOnClickListener {
            logoutListener?.handleLogout()
        }

        view.findViewById<Button>(R.id.twibbon)?.setOnClickListener {
            openTwibbonFragment();
        }

        return view
    }

    private fun showFileNameDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_download_transaksi, null)
        builder.setView(dialogView)
        builder.setTitle("Simpan Berkas")

        val fileNameEditText = dialogView.findViewById<EditText>(R.id.edit_text_file_name)
        val radioXlsx = dialogView.findViewById<RadioButton>(R.id.radio_xlsx)
        val radioXls = dialogView.findViewById<RadioButton>(R.id.radio_xls)

        radioXlsx.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedFormat = "XLSX"
            }
        }

        radioXls.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedFormat = "XLS"
            }
        }

        builder.setPositiveButton("Simpan") { dialog, _ ->
            val fileName = fileNameEditText.text.toString().trim()

            if (fileName.isNotEmpty()) {
                saveTransactionsToSpreadsheet(fileName, selectedFormat, 1)
            } else {
                Toast.makeText(requireContext(), "Nama file tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.show()
    }
    private fun saveTransactionsToSpreadsheet(fileName: String, format: String, act: Int) {
        transactionViewModel.getAllTransactions.observe(viewLifecycleOwner)
        { transactions ->
            val workbook = convertTransactionsToWorkbook(transactions)
            if (act == 1) saveWorkbookToStorage(workbook, fileName, format)
            else {
                val message = generateEmailMessage(transactions)
                sendEmail(workbook, message)
            }
        }
    }

    private fun convertTransactionsToWorkbook(transactions: List<Transaction>): Workbook {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Transaksi")

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Tanggal")
        headerRow.createCell(1).setCellValue("Kategori Transaksi")
        headerRow.createCell(2).setCellValue("Nominal Transaksi")
        headerRow.createCell(3).setCellValue("Nama Transaksi")
        headerRow.createCell(4).setCellValue("Lokasi")

        transactions.forEachIndexed { index, transaction ->
            val row = sheet.createRow(index + 1)
            val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            row.createCell(0).setCellValue(dateFormatter.format(transaction.date))
            row.createCell(1).setCellValue(transaction.category)
            row.createCell(2).setCellValue(transaction.price)
            row.createCell(3).setCellValue(transaction.name)
            row.createCell(4).setCellValue(transaction.location ?: "")
        }

        return workbook
    }

    private fun saveWorkbookToStorage(workbook: Workbook, fileName: String, format: String): Boolean {
        val fileDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "Bondoman")
        if (!fileDirectory.exists()) {
            fileDirectory.mkdirs()
        }

        val fileExtension = if (format == "XLSX") "xlsx" else "xls"
        val file = File(fileDirectory, "$fileName.$fileExtension")

        return try {
            val fos = FileOutputStream(file)
            workbook.write(fos)
            fos.close()
            Toast.makeText(requireContext(), "File disimpan di ${file.absolutePath}", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    private fun sendEmail(workbook: Workbook, message: String) {
        val file = kotlin.io.path.createTempFile("Daftar Transaksi Bondoman - ", ".xlsx").toFile()
        val fos = FileOutputStream(file)
        workbook.write(fos)
        fos.close()

        val uri = FileProvider.getUriForFile(requireContext(), requireContext().applicationContext.packageName + ".provider", file)
        requireContext().grantUriPermission(requireContext().packageName, uri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.ms-excel"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // `package` = "com.google.android.gm"
        }

        /*
        val pm = context?.packageManager
        val activities = pm?.queryIntentActivities(emailIntent, PackageManager.MATCH_DEFAULT_ONLY)

        activities?.forEach {
            Log.d("PackageManager", "Package Name: ${it.activityInfo.packageName}")
        }
         */

        if (emailIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(Intent.createChooser(emailIntent, "Kirim Email Menggunakan:"))
        } else {
            Toast.makeText(requireContext(), "Gagal mengirim email", Toast.LENGTH_SHORT).show()
        }
    }



    private fun generateEmailMessage(transactions: List<Transaction>): String {
        val totalTransactions = transactions.size
        val totalIncomeTransactions = transactions.count { it.category == "Pemasukan" }
        val totalExpenseTransactions = transactions.count { it.category == "Pengeluaran" }
        val totalIncomeAmount = transactions.filter { it.category == "Pemasukan" }.sumOf { it.price }
        val totalExpenseAmount = transactions.filter { it.category == "Pengeluaran" }.sumOf { it.price }
        val totalAmount = totalIncomeAmount + totalExpenseAmount
        val incomePercentage = (totalIncomeAmount / totalAmount) * 100
        val expensePercentage = (totalExpenseAmount / totalAmount) * 100

        val emailMessage = """
            Halo,

            Berikut adalah ringkasan transaksi terbaru:

            - Total Transaksi: $totalTransactions
              Terdiri dari $totalIncomeTransactions Transaksi Pemasukan dan $totalExpenseTransactions Transaksi Pengeluaran
            - Total Transaksi Pemasukan: $totalIncomeAmount (${String.format("%.2f", incomePercentage)}%)
            - Total Transaksi Pengeluaran: $totalExpenseAmount (${String.format("%.2f", expensePercentage)}%)

            Terima kasih atas penggunaan aplikasi kami. Semoga harimu menyenangkan!

            Hormat kami,
            Tim Bondoman
        """.trimIndent()

        return emailMessage
    }

    private fun broadcastRandomTransaction() {
        val randomTransaction = generateRandomTransaction()

        val randomTansactionIntent = Intent("com.example.bondoman.NEW_TRANSACTION")
        randomTansactionIntent.putExtra("new_transaction", randomTransaction)
        Log.d("Broadcast", randomTransaction.toString())

        Log.d("Broadcast", "Sender Context Package Name: ${requireContext().packageName} ")
        Log.d("Broadcast", "Intent Filter Name: ${randomTansactionIntent.action}")
        requireActivity().sendBroadcast(randomTansactionIntent)
    }

    private fun generateRandomTransaction(): Transaction {
        return Transaction(
            id = 0,
            name = "Transaksi Random",
            category = if (Random.nextBoolean()) "Pemasukan" else "Pengeluaran",
            price = Random.nextInt(100000, 200000).toDouble(),
            location = getString(R.string.default_location),
            date = Date()
        )
    }


    fun setLogoutListener(logoutListener: LogoutListener?) {
        this.logoutListener = logoutListener
    }

    private fun openTwibbonFragment() {
        val twibbonFragment = TwibbonFragment()
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.frame_layout, twibbonFragment)
            addToBackStack(null)
            commit()
        }
    }

    private fun getEmailFromSharedPreferences(): String {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("email", null) ?: ""
    }
}