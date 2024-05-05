package com.example.bondoman

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bondoman.api.APIService
import com.example.bondoman.api.LoginRequest
import com.example.bondoman.api.RetrofitHelper
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)

        // Initialize views
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.login)

        // Add text change listeners to enable/disable login button
        emailEditText.addTextChangedListener(textWatcher)
        passwordEditText.addTextChangedListener(textWatcher)

        // Set click listener for login button
        loginButton.setOnClickListener {
            // Perform login logic when login button is clicked
            performLogin()
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            // Enable login button only if both username and password fields are not empty
            loginButton.isEnabled = !emailEditText.text.isNullOrBlank() && !passwordEditText.text.isNullOrBlank()
        }
    }

    private fun performLogin() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val loginRequest = LoginRequest(email, password)

        lifecycleScope.launch {
            val retrofit = RetrofitHelper.getInstance()
            val service = retrofit.create(APIService::class.java)
            try {
                val response = service.login(loginRequest)
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        val token = loginResponse.token
                        saveCredentials(email,token)
                        navigateToMainActivity()
                    } else {
                        Log.e("Login", "Empty response body")
                        showToast("Login failed. Please try again.")
                    }
                } else {
                    // Incorrect Credentials
                    Log.e("Login", "Login failed: ${response.code()}")
                    when (response.code()) {
                        401 -> showToast("Invalid email or password. Please try again.")
                        else -> showToast("Login failed. Please try again later.")
                    }
                }
            } catch (e: Exception) {
                // Handle exceptions
                Log.e("Login", "Exception", e)
                showToast("An error occurred. Please check your internet connection and try again.")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun saveCredentials(email: String, token: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString("email", email)
            .putString("token", token)
            .apply()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}
