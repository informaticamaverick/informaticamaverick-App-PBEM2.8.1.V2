package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.data.model.MessageType
import java.util.UUID

@Entity(tableName = "messages")
data class MessageEntity(
    // ID único (UUID es mejor para sincronizar con Firebase luego)
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    // ID de la conversación (ej: "clienteID_prestadorID")
    val chatId: String,

    // IDs de los participantes
    val senderId: String,
    val receiverId: String,

    // TIPO: Define cómo dibujar la burbuja (Foto, Mapa, Audio, etc.)
    val type: MessageType,

    // CONTENIDO PRINCIPAL:
    // - TEXT: El texto del mensaje.
    // - IMAGE/AUDIO: La ruta del archivo en el celular (file://...).
    // - LOCATION: Puede tener una descripción o dirección formateada.
    // - VISIT/BUDGET: Puede tener un resumen.
    val content: String,
    val imageUrl: String? = null, // 🔥 [NUEVO] Ruta local de la imagen para evitar Base64 constante
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationAddress: String? = null,
    val durationSeconds: Int? = null,
    val relatedId: String? = null,

    // 🔥 Campos de apoyo para mostrar info rápida sin consultar otra tabla (opcional pero útil)
    val appointmentDate: String? = null,
    val appointmentTime: String? = null,
    val appointmentStatus: String? = null, // PENDING, ACCEPTED, REJECTED

    // --- METADATOS ---
    val timestamp: Long = System.currentTimeMillis(),

    // Estado del mensaje: SENT (Enviado), READ (Leído), ERROR
    val status: String = "SENT",

    // 🔥 [NUEVO] Campo para saber si el mensaje fue leído por el usuario actual.
    // Por defecto, un mensaje nuevo llega como "no leído".
    val isRead: Boolean = false,

    val isSynced: Boolean = false
)

