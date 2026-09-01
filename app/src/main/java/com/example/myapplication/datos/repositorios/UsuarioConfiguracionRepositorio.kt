package com.example.myapplication.datos.repositorios

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class AjustesNotificacion(
    val notifChat: Boolean = true,
    val notifCalendario: Boolean = true,
    val notifLicitaciones: Boolean = true,
    val notifPromociones: Boolean = true
)

/**
 * --- REPOSITORIO DE CONFIGURACIÓN DE USUARIO (US) ---
 * [LEY #9]: Estándar Mav en Español.
 */
@Singleton
class UsuarioConfiguracionRepositorio @Inject constructor(
    private val contexto: Context
) {
    private val preferencias: SharedPreferences = contexto.getSharedPreferences("ajustes_usuario", Context.MODE_PRIVATE)

    private val _ajustesNotificacion = MutableStateFlow(cargarAjustes())
    val ajustesNotificacion: StateFlow<AjustesNotificacion> = _ajustesNotificacion.asStateFlow()

    private fun cargarAjustes(): AjustesNotificacion {
        return AjustesNotificacion(
            notifChat = preferencias.getBoolean("notif_chat", true),
            notifCalendario = preferencias.getBoolean("notif_calendario", true),
            notifLicitaciones = preferencias.getBoolean("notif_licitaciones", true),
            notifPromociones = preferencias.getBoolean("notif_promociones", true)
        )
    }

    suspend fun actualizarNotifChat(habilitada: Boolean) {
        preferencias.edit().putBoolean("notif_chat", habilitada).apply()
        _ajustesNotificacion.value = cargarAjustes()
    }

    suspend fun actualizarNotifCalendario(habilitada: Boolean) {
        preferencias.edit().putBoolean("notif_calendario", habilitada).apply()
        _ajustesNotificacion.value = cargarAjustes()
    }

    suspend fun actualizarNotifLicitaciones(habilitada: Boolean) {
        preferencias.edit().putBoolean("notif_licitaciones", habilitada).apply()
        _ajustesNotificacion.value = cargarAjustes()
    }

    suspend fun actualizarNotifPromociones(habilitada: Boolean) {
        preferencias.edit().putBoolean("notif_promociones", habilitada).apply()
        _ajustesNotificacion.value = cargarAjustes()
    }
}


































