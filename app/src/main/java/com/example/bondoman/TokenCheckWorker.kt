import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.bondoman.LoginActivity
import com.example.bondoman.api.APIService
import com.example.bondoman.api.RetrofitHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class TokenCheckWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val sharedPreferences = applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("token", null)
            Log.d("Token","Token Checking...")
            if (token != null) {
                try {
                    val retrofit = RetrofitHelper.getInstance()
                    val service = retrofit.create(APIService::class.java)
                    val response = service.getToken("Bearer $token")
                    if (response.isSuccessful) {
                        val tokenResponse = response.body()
                        if (tokenResponse != null) {
                            val expirationTime = tokenResponse.exp * 1000 // Convert to milliseconds
                            val currentTime = System.currentTimeMillis()
                            if (expirationTime > currentTime) {
                                // Token is not expired
                                // Schedule the next check according to expiration time
                                scheduleNextCheck(expirationTime)
                                Result.success()
                            } else {
                                // Token is expired
                                handleLogout(applicationContext)
                                Result.failure(createOutputData("Token is expired"))
                            }
                        } else {
                            handleLogout(applicationContext)
                            Result.failure(createOutputData("Token response is null"))
                        }
                    } else {
                        handleLogout(applicationContext)
                        Result.failure(createOutputData("Token check request failed: ${response.code()}"))
                    }
                } catch (e: Exception) {
                    handleLogout(applicationContext)
                    Result.failure(createOutputData("Exception during token check: ${e.message}"))
                }
            } else {
                handleLogout(applicationContext)
                Result.failure(createOutputData("Token not found. User not logged in."))
            }
        }
    }

    private fun createOutputData(message: String): Data {
        return Data.Builder().putString("failureMessage", message).build()
    }


    private fun scheduleNextCheck(expirationTime: Long) {
        val currentTime = System.currentTimeMillis()
        val delay = expirationTime - currentTime

        if (delay > 0) {
            val inputData = Data.Builder().putLong("expirationTime", expirationTime).build()

            val workRequest = OneTimeWorkRequestBuilder<TokenCheckWorker>()
                .setInputData(inputData)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(applicationContext).enqueue(workRequest)
        }
    }

    private fun handleLogout(context: Context) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("token").apply()
        sharedPreferences.edit().remove("email").apply()

        // Start LoginActivity
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}
