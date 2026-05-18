package com.example.myapplication.prestador.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

@Singleton
class AppSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val THEME_MODE = stringPreferencesKey("theme_mode")
    private val NOTIF_MESSAGES = booleanPreferencesKey("notif_messages")
    private val NOTIF_PRESUPUESTOS = booleanPreferencesKey("notif_presupuestos")
    private val NOTIF_PEDIDOS = booleanPreferencesKey("notif_pedidos")

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[THEME_MODE]) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK"  -> ThemeMode.DARK
            else    -> ThemeMode.SYSTEM
        }
    }

    val notifMessages: Flow<Boolean> = context.dataStore.data.map { it[NOTIF_MESSAGES] ?: true }
    val notifPresupuestos: Flow<Boolean> = context.dataStore.data.map { it[NOTIF_PRESUPUESTOS] ?: true }
    val notifPedidos: Flow<Boolean> = context.dataStore.data.map { it[NOTIF_PEDIDOS] ?: true }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[THEME_MODE] = mode.name }
    }

    suspend fun setNotifMessages(enabled: Boolean) {
        context.dataStore.edit { it[NOTIF_MESSAGES] = enabled }
    }

    suspend fun setNotifPresupuestos(enabled: Boolean) {
        context.dataStore.edit { it[NOTIF_PRESUPUESTOS] = enabled }
    }

    suspend fun setNotifPedidos(enabled: Boolean) {
        context.dataStore.edit { it[NOTIF_PEDIDOS] = enabled }
    }
}
