package com.ingrify.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val backButton: ImageView = findViewById(R.id.iv_back_button)
        val tilName: TextInputLayout = findViewById(R.id.til_name)
        val tilAge: TextInputLayout = findViewById(R.id.til_age)
        val tilUsername: TextInputLayout = findViewById(R.id.til_username)
        val tilCreatePassword: TextInputLayout = findViewById(R.id.til_create_password)
        val tilConfirmPassword: TextInputLayout = findViewById(R.id.til_confirm_password)
        val signUpButton: Button = findViewById(R.id.btn_signup_submit)
        val loginLink: TextView = findViewById(R.id.tv_login_link)

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        signUpButton.setOnClickListener {
            val name = tilName.editText?.text.toString().trim()
            val ageString = tilAge.editText?.text.toString().trim()
            val username = tilUsername.editText?.text.toString().trim()
            val createPassword = tilCreatePassword.editText?.text.toString().trim()
            val confirmPassword = tilConfirmPassword.editText?.text.toString().trim()

            // Basic validation
            if (name.isEmpty() || ageString.isEmpty() || username.isEmpty() || createPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (createPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val age = ageString.toIntOrNull()
            if (age == null) {
                Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Perform API call for registration
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val request = RegisterRequest(name, age, username, createPassword)
                    val response = RetrofitClient.apiService.registerUser(request)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            val registerResponse = response.body()
                            Toast.makeText(this@SignUpActivity, registerResponse?.message ?: "Registration successful!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                            intent.putExtra("username", username) // optional: pre-fill username
                            startActivity(intent)
                            finish()

                        } else {
                            val errorBody = response.errorBody()?.string()
                            Toast.makeText(this@SignUpActivity, "Registration failed: ${errorBody ?: response.message()}", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SignUpActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                }
            }
        }

        loginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
