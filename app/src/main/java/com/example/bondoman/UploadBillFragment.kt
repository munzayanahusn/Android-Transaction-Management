package com.example.bondoman

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.bondoman.api.APIService
import com.example.bondoman.api.BillUploadResponse
import com.example.bondoman.api.RetrofitHelper
import com.example.bondoman.data.TransactionDatabase
import com.example.bondoman.data.TransactionRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class UploadBillFragment : Fragment() {
    private lateinit var transactionRepository: TransactionRepository
    private var imagePath: String? = null
    private var source: String? = null
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{
            imagePath = it.getString("imagePath")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val database = TransactionDatabase.getInstance(requireActivity().application).transactionDao()
        transactionRepository = TransactionRepository(database)

        val view = inflater.inflate(R.layout.fragment_upload_bill, container, false)
        val imageView: ImageView = view.findViewById(R.id.imageView)

        progressBar = view.findViewById(R.id.progressBar)

        imagePath?.let {
            imageView.setImageURI(Uri.parse(it))
        }

        val uploadButton = view.findViewById<Button>(R.id.uploadBtn)
        uploadButton.setOnClickListener {
            uploadImage()
        }

        val cobaLagiBtn = view.findViewById<Button>(R.id.cobaLagiBtn)
        cobaLagiBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }


    private fun uploadImage() {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {

            val retrofit = RetrofitHelper.getInstance()
            val service = retrofit.create(APIService::class.java)
            var imageName = "";
            imageName = Uri.parse(imagePath).toString()
            val imageUri = Uri.parse(imagePath)
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            val requestBody: RequestBody = inputStream!!.readBytes().toRequestBody("image/jpeg".toMediaTypeOrNull())

            val body = MultipartBody.Part.createFormData("file", imageName, requestBody)
            val partMap = HashMap<String, RequestBody>()
            try {
                val token = getToken()
                val response = service.uploadBill("Bearer $token", body, partMap)
                if (response.isSuccessful) {
                    response.body()?.string()?.let { jsonString ->
                        Log.d("Upload", "Success: $jsonString")
                        val gson = Gson()
                        val itemsResponse: BillUploadResponse =
                            gson.fromJson(jsonString, BillUploadResponse::class.java)

                        val price = itemsResponse.items.items.sumByDouble { it.price * it.qty }

                        val fragment = AddTransactionFragment().apply {
                            arguments = Bundle().apply{
                                putDouble("price", price)
                            }
                        }

                        parentFragmentManager.beginTransaction()
                            .replace(R.id.frame_layout, fragment)
                            .addToBackStack(null)
                            .commit()

                    }
                } else {
                    Log.e("Upload", "Error: ${response.errorBody()?.string()}")
                    withContext(Dispatchers.Main) {
                        val text = "Upload bill gagal"
                        val duration = Toast.LENGTH_SHORT

                        Toast.makeText(requireContext().applicationContext, text, duration).show()
                    }

                }
            } catch (e: Exception) {
                Log.e("Upload", "Exception", e)
            } finally {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun getToken(): String? {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("token", null)
    }

    companion object {

        fun newInstance(imagePath: String, source: String): UploadBillFragment {
            val fragment = UploadBillFragment()
            val args = Bundle()
            args.putString("imagePath", imagePath)
            args.putString("source",source)
            fragment.arguments = args
            return fragment
        }
    }

}