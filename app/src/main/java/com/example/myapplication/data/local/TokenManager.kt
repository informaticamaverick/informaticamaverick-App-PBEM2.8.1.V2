package com.example.myapplication.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // 1. Creamos la llave maestra
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // 2. Abrimos el archivo encriptado
    private val sharedPreferences = try {
        createEncryptedSharedPreferences()
    } catch (e: Exception) {
        // Si falla (por ejemplo, por cambio de llaves en el Keystore), borramos y reintentamos
        context.deleteSharedPreferences("secure_prefs")
        createEncryptedSharedPreferences()
    }

    private fun createEncryptedSharedPreferences() = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        sharedPreferences.edit().putString("auth_token", token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }

    // --- LÓGICA DE POPUP DE DIRECCIÓN (POR ÚNICA VEZ) ---
    fun shouldShowAddressPopup(): Boolean {
        return sharedPreferences.getBoolean("show_address_popup", true)
    }

    fun setAddressPopupShown() {
        sharedPreferences.edit().putBoolean("show_address_popup", false).apply()
    }

    // --- LÓGICA DE PRIMERA VEZ ---
    fun isFirstTime(): Boolean {
        return sharedPreferences.getBoolean("is_first_time", true)
    }

    fun setFirstTimeCompleted() {
        sharedPreferences.edit().putBoolean("is_first_time", false).apply()
    }
}
