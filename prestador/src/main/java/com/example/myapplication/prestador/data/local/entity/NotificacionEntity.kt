package com.example.myapplication.prestador.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.prestador.data.model.NotificacionItem
import com.example.myapplication.prestador.data.model.TipoNotificacion

@Entity(tableName = "notificaciones")
data class NotificacionEntity(
    @PrimaryKey(autoGenerate = true ) val id: Long = 0,
    val tipo: String,
    val titulo: String,
    val mensaje: String,
    val fechaMs: Long,
    val leida: Boolean,
    val accionRoute: String?

) {
    fun toModel() = NotificacionItem(
        id = id,
        tipo = TipoNotificacion.valueOf(tipo),
        titulo = titulo,
        mensaje = mensaje,
        fechaMs = fechaMs,
        leida = leida,
        accionRoute = accionRoute
    )
}

fun NotificacionItem.toEntity() = NotificacionEntity(
    id = id,
    tipo = tipo.name,
    titulo = titulo,
    mensaje = mensaje,
    fechaMs = fechaMs,
    leida = leida,
    accionRoute = accionRoute
)