package com.example.bondoman.api

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bondoman.data.Transaction
import okhttp3.MultipartBody
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Response
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import java.util.Date

interface APIService {
    @Multipart
    @POST("api/bill/upload")
    suspend fun uploadBill(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part,
        @PartMap partMap: Map<String, @JvmSuppressWildcards RequestBody>? = null
    ): Response<ResponseBody>

    @POST("api/auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/token")
    suspend fun getToken(
        @Header("Authorization") authorization: String
    ): Response<TokenResponse>
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String
)

data class TokenResponse(
    val nim: String,
    val iat: Long,
    val exp: Long
)

object RetrofitHelper {
    val baseUrl = "https://pbd-backend-2024.vercel.app"

    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

data class BillUploadResponse(val items: ItemsContainer)
data class ItemsContainer(val items: List<TransactionItem>)
data class TransactionItem(val name: String,  val qty: Int, val price: Double)
