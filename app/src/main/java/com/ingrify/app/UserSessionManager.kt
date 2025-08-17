package com.ingrify.app
import android.content.Context
import android.content.SharedPreferences

object UserSessionManager {

    private const val PREF_NAME = "IngrifyUserSession"
    private const val KEY_USER_NAME = "userName" // For displaying immediately from login/signup response
    private const val KEY_AUTH_TOKEN = "authToken"// NEW: To store the authentication token
    private const val KEY_NAME = "name"

    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserName(name: String) {
        sharedPreferences.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun saveName(name: String) {
        sharedPreferences.edit().putString(KEY_NAME, name).apply()
    }
    fun getName(): String? {
        return sharedPreferences.getString(KEY_NAME, null)
    }

    fun getUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }

    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    // NEW: Check if user is logged in (based on token presence)
    fun isLoggedIn(): Boolean {
        return !getAuthToken().isNullOrEmpty()
    }

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
}
