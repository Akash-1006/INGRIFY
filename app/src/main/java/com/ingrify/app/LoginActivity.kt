package com.ingrify.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.ingrify.app.LoginRequest

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Find views by their IDs
        val backButton: ImageView = findViewById(R.id.iv_back_button_login)
        val etUsername: TextInputEditText = findViewById(R.id.et_username_login)
        val etPassword: TextInputEditText = findViewById(R.id.et_password_login)
        val loginButton: Button = findViewById(R.id.btn_login_submit)

        // Set click listener for the back button
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Go back to the previous activity
        }

        // Set click listener for the Login button
        loginButton.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString()

            // Basic validation
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            } else {
                // Here you would typically send data to. a backend for user authentication
                Toast.makeText(this, "Attempting login for $username...", Toast.LENGTH_LONG).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
