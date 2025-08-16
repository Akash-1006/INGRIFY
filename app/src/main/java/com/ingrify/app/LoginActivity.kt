package com.ingrify.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize UserSessionManager
        UserSessionManager.init(this)

        // If user is already logged in, skip login
        if (UserSessionManager.isLoggedIn()) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        backButton = findViewById(R.id.iv_back_button_login)
        etUsername = findViewById(R.id.et_username_login)
        etPassword = findViewById(R.id.et_password_login)
        loginButton = findViewById(R.id.btn_login_submit)

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        loginButton.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            } else {
                performLogin(username, password)
            }
        }
    }

    private fun performLogin(username: String, password: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val request = LoginRequest(username, password)
                val response = RetrofitClient.apiService.loginUser(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val loginResponse = response.body()
                        val token = loginResponse?.token
                        val returnedUserName = loginResponse?.userName

                        if (token != null) {
                            // ✅ Save session using UserSessionManager
                            UserSessionManager.saveAuthToken(token)
                            UserSessionManager.saveUserName(returnedUserName ?: username)

                            Toast.makeText(
                                this@LoginActivity,
                                "Welcome ${returnedUserName ?: username}",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Go to HomeActivity
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "Login successful, but no token received.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Toast.makeText(
                            this@LoginActivity,
                            "Login failed: ${errorBody ?: response.message()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
    }
}
