package com.example.myapplication.core.database

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- TOKEN MANAGER (APP CLIENTE) ---
 * Gestiona el almacenamiento local de tokens y flags de primera sesión.
 * Exclusivo de la aplicación del Cliente.
 */
@Singleton
class TokenManager @Inject constructor(
    private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("maverick_prefs", Context.MODE_PRIVATE)

    fun isFirstTime(): Boolean = prefs.getBoolean("is_first_time", true)

    fun setFirstTimeCompleted() {
        prefs.edit().putBoolean("is_first_time", false).apply()
    }
}
