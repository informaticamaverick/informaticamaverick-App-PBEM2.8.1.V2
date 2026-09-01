package com.example.myapplication.core.datos.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.core.dominio.modelos.ElementoNotificacion
import com.example.myapplication.core.dominio.modelos.TipoNotificacion

/**
 * --- ENTIDAD DE NOTIFICACIÓN MAVERICK (SSOT 2026) ---
 */
@Entity(tableName = "notificaciones")
data class NotificacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String,
    val titulo: String,
    val mensaje: String,
    val fechaMs: Long,
    val leida: Boolean,
    val rutaAccion: String?
) {
    fun aModelo() = ElementoNotificacion(
        id = id,
        tipo = try { TipoNotificacion.valueOf(tipo) } catch (e: Exception) { TipoNotificacion.SISTEMA },
        titulo = titulo,
        mensaje = mensaje,
        fechaMs = fechaMs,
        leida = leida,
        rutaAccion = rutaAccion
    )
}

fun ElementoNotificacion.aEntidad() = NotificacionEntity(
    id = id,
    tipo = tipo.name,
    titulo = titulo,
    mensaje = mensaje,
    fechaMs = fechaMs,
    leida = leida,
    rutaAccion = rutaAccion
)

































