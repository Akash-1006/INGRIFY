package com.ingrify.app

import android.content.Context
import android.content.SharedPreferences

object AllergenManager {

    private const val PREFS_NAME = "AllergenPrefs"
    private const val KEY_ALLERGENS = "allergens"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Save allergens persistently
    fun saveAllergens(context: Context, list: List<String>) {
        val lowerCaseList = list.map { it.lowercase().trim() } // normalize
        getPrefs(context).edit()
            .putStringSet(KEY_ALLERGENS, lowerCaseList.toSet())
            .apply()
    }

    // Load allergens
    fun getAllergens(context: Context): List<String> {
        return getPrefs(context).getStringSet(KEY_ALLERGENS, emptySet())?.toList() ?: emptyList()
    }

    // Clear all allergens
    fun clearAllergens(context: Context) {
        getPrefs(context).edit().remove(KEY_ALLERGENS).apply()
    }
}
