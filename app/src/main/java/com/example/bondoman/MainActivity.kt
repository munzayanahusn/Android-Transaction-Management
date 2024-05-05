package com.example.bondoman

import GraphFragment
import TokenCheckWorker
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.bondoman.api.APIService
import com.example.bondoman.api.RetrofitHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), LogoutListener {

    lateinit var bottomNav: BottomNavigationView
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var transactionReceiver: TransactionReceiver
    private var noInternetDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        NetworkSensingUtility.init(this)

        GlobalScope.launch(Dispatchers.Main) {
            val isLoggedIn = isLoggedIn()
            if (!isLoggedIn) {
                // If the user is not logged in, navigate to the login screen
                navigateToLogin()
            } else {
                setContentView(R.layout.main_activity)
                supportActionBar?.show()
                supportActionBar?.setBackgroundDrawable(resources.getDrawable(R.drawable.ic_launcher_background))

                transactionReceiver = TransactionReceiver(this@MainActivity)
                registerReceiver(
                    transactionReceiver,
                    IntentFilter("com.example.bondoman.NEW_TRANSACTION"),
                    RECEIVER_EXPORTED
                )

                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
                loadCrudNavHost()

                bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                bottomNav.setOnItemSelectedListener {
                    when (it.itemId) {
                        R.id.transaction -> {
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            loadCrudNavHost()
                            supportActionBar?.title = resources.getString(R.string.transaksi)
                            true
                        }
                        R.id.scanner -> {
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            loadFragment(ScannerFragment())
                            supportActionBar?.title = resources.getString(R.string.scanner)
                            true
                        }
                        R.id.graph -> {
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                            loadFragment(GraphFragment())
                            supportActionBar?.title = resources.getString(R.string.graph)
                            true
                        }
                        R.id.settings -> {
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            loadFragment(SettingFragment(),this@MainActivity)
                            supportActionBar?.title = resources.getString(R.string.setting)
                            true
                        }
                        else -> false
                    }

                }

            }
        }
    }

    private fun loadFragment(fragment: Fragment, logoutListener: LogoutListener? = null) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
        transaction.commit()

        if (fragment is SettingFragment) {
            fragment.setLogoutListener(logoutListener)
        }
    }

    private suspend fun isLoggedIn(): Boolean {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        if (!token.isNullOrEmpty()) {
            // Token exists, perform token check
            return try {
                val retrofit = RetrofitHelper.getInstance()
                val service = retrofit.create(APIService::class.java)
                val response = service.getToken("Bearer $token")
                if (response.isSuccessful) {
                    // Token is valid, parse the token response to check expiration time
                    val tokenResponse = response.body()
                    if (tokenResponse != null) {
                        val expirationTime = tokenResponse.exp * 1000 // Convert to milliseconds
                        val currentTime = System.currentTimeMillis()
                        if (expirationTime > currentTime) {
                            // Token is not expired, schedule next check using Worker
                            val tokenCheckWorkRequest = OneTimeWorkRequestBuilder<TokenCheckWorker>()
                                .setInitialDelay(expirationTime - currentTime, TimeUnit.MILLISECONDS)
                                .build()
                            WorkManager.getInstance(applicationContext).enqueue(tokenCheckWorkRequest)
                            Log.d("Token","Token Check Scheduled")
                            true
                        } else {
                            // Token is expired, handle accordingly
                            Log.e("Token", "Token is expired")
                            // logout
                            handleLogout()
                            false
                        }
                    } else {
                        Log.e("Token", "Token response is null")
                        false
                    }
                } else {
                    // Token check request failed
                    Log.e("Token", "Token check request failed: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                // Handle exceptions
                Log.e("Token", "Exception during token check", e)
                false
            }
        } else {
            Log.e("Token", "Token not found. User not logged in.")
            // Token not found, user not logged in
            return false
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun handleLogout() {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("token").apply()
        sharedPreferences.edit().remove("email").apply()
        navigateToLogin()
    }

    override fun onResume() {
        super.onResume()
        if (::transactionReceiver.isInitialized) {
            registerReceiver(
                transactionReceiver,
                IntentFilter("com.example.bondoman.NEW_TRANSACTION"),
                RECEIVER_EXPORTED
            )
        }
        NetworkSensingUtility.registerNetworkCallback(this,
            onAvailable = {
                runOnUiThread {
                    noInternetDialog?.dismiss()
                }
            },
            onLost = {
                runOnUiThread {
                    showNoInternetDialog(this@MainActivity);
                }
            }
        )
    }

    override fun onPause() {
        NetworkSensingUtility.unregisterNetworkCallback()
        if (::transactionReceiver.isInitialized){
            unregisterReceiver(transactionReceiver)

        }
        super.onPause()
    }


    private fun loadCrudNavHost() {
        supportFragmentManager.fragments.forEach {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }

        navHostFragment = NavHostFragment.create(R.navigation.crud_nav)
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, navHostFragment)
            .setPrimaryNavigationFragment(navHostFragment)
            .commit()

        navHostFragment.viewLifecycleOwnerLiveData.observe(this) { viewLifecycleOwner ->
            viewLifecycleOwner?.lifecycle?.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
                fun onCreated() {
                    setupActionBarWithNavController(navHostFragment.navController)
                }
            })
        }
    }

    fun navigateToAddTransactionFragment(bundle: Bundle) {
        bottomNav.selectedItemId = R.id.transaction
        if (navHostFragment.isAdded) {
            navHostFragment.navController.navigate(R.id.action_listTransactionFragment_to_addTransactionFragment, bundle)
        } else {
            navHostFragment.viewLifecycleOwnerLiveData.observe(this) { viewLifecycleOwner ->
                viewLifecycleOwner?.lifecycle?.addObserver(object : LifecycleObserver {
                    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
                    fun onResume() {
                        navHostFragment.navController.navigate(R.id.action_listTransactionFragment_to_addTransactionFragment, bundle)
                        viewLifecycleOwner.lifecycle.removeObserver(this)
                    }
                })
            }
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
    override fun onDestroy() {
        super.onDestroy()
    }

    private fun showNoInternetDialog(context: Context) {
        if ((context as Activity).isFinishing) {
            return
        }
        noInternetDialog?.dismiss()

        noInternetDialog = AlertDialog.Builder(context)
            .setTitle("No Internet Connection")
            .setMessage("Please check your internet connection and try again.")
            .setPositiveButton("Retry") { dialog, which ->
                retryInternetConnection(context)
            }
            .setNegativeButton("Cancel") {dialog, which ->
                navigateToLogin(context)
            }
            .setCancelable(false)
            .create()

        noInternetDialog?.show()
    }

    private fun navigateToLogin(context: Context) {
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
    }

    private fun retryInternetConnection(context: Context) {
        if (NetworkSensingUtility.isInternetAvailable(context)) {
            noInternetDialog?.dismiss()
        } else {
            showNoInternetDialog(this@MainActivity)
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}