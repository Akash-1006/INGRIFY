package com.ingrify.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DISPLAY_LENGTH = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        UserSessionManager.init(this)

        Handler(Looper.getMainLooper()).postDelayed({
            if (UserSessionManager.isLoggedIn()) {
                val intent = Intent(this@SplashActivity, HomeActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
            }
            finish()
        }, SPLASH_DISPLAY_LENGTH)
    }
}
