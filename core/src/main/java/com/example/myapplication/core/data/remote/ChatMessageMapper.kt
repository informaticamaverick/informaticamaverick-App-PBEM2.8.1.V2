package com.example.myapplication.core.data.remote

import android.content.Context
import com.example.myapplication.core.data.local.entity.MessageEntity
import com.example.myapplication.core.domain.model.MessageType
import com.example.myapplication.core.utils.ImageUtils
import com.google.firebase.database.DataSnapshot
import java.util.UUID

/**
 * --- CHAT MESSAGE MAPPER (COMPARTIDO) ---
 * Centraliza la conversión de DataSnapshot (Firebase Realtime Database) a MessageEntity (Room).
 * Maneja la compatibilidad entre los esquemas de la App Cliente y App Prestador.
 */
object ChatMessageMapper {

    fun fromDataSnapshot(snapshot: DataSnapshot, chatId: String, context: Context? = null): MessageEntity? {
        return try {
            val senderId = snapshot.child("senderId").getValue(String::class.java) ?: ""
            if (senderId.isBlank()) return null

            // Resolución de IDs
            val id = snapshot.child("id").getValue(String::class.java)
                ?: snapshot.child("messageId").getValue(String::class.java)
                ?: snapshot.key
                ?: UUID.randomUUID().toString()

            val content = snapshot.child("content").getValue(String::class.java)
                ?: snapshot.child("text").getValue(String::class.java)
                ?: ""

            val typeStr = snapshot.child("type").getValue(String::class.java)
                ?: snapshot.child("messageType").getValue(String::class.java)
                ?: "TEXT"
            val type = try { MessageType.valueOf(typeStr) } catch (e: Exception) { MessageType.TEXT }

            // --- Lógica de Optimización Multimedia (Costo Zero) ---
            var finalImageUrl = snapshot.child("imageUrl").getValue(String::class.java)
            
            // Si recibimos una imagen/audio codificado en Base64 (común en transferencias rápidas entre apps)
            // lo guardamos como archivo local inmediatamente para no saturar la memoria con Strings pesados.
            if (context != null && (type == MessageType.IMAGE || type == MessageType.AUDIO) && content.length > 500) {
                val prefix = if (type == MessageType.IMAGE) "IMG_" else "AUD_"
                val ext = if (type == MessageType.IMAGE) ".webp" else ".m4a"
                val localPath = ImageUtils.saveBase64ToFile(context, content, id, prefix, ext)
                if (localPath != null) {
                    finalImageUrl = localPath
                }
            }

            MessageEntity(
                id = id,
                chatId = snapshot.child("chatId").getValue(String::class.java)
                    ?: snapshot.child("conversationId").getValue(String::class.java)
                    ?: chatId,
                senderId = senderId,
                receiverId = snapshot.child("receiverId").getValue(String::class.java) ?: "",
                type = type,
                content = if (content.length > 500) "[Multimedia]" else content,
                imageUrl = finalImageUrl,
                latitude = snapshot.child("latitude").getValue(Double::class.java),
                longitude = snapshot.child("longitude").getValue(Double::class.java),
                locationAddress = snapshot.child("locationAddress").getValue(String::class.java)
                    ?: snapshot.child("budgetRequestClientAddress").getValue(String::class.java),
                durationSeconds = snapshot.child("durationSeconds").getValue(Int::class.java),
                relatedId = snapshot.child("relatedId").getValue(String::class.java)
                    ?: snapshot.child("budgetId").getValue(String::class.java)
                    ?: snapshot.child("tenderId").getValue(String::class.java),
                
                // --- Datos de Turnos / Citas ---
                appointmentDate = snapshot.child("appointmentDate").getValue(String::class.java)
                    ?: snapshot.child("date").getValue(String::class.java),
                appointmentTime = snapshot.child("appointmentTime").getValue(String::class.java)
                    ?: snapshot.child("time").getValue(String::class.java),
                appointmentStatus = snapshot.child("appointmentStatus").getValue(String::class.java),
                appointmentType = snapshot.child("appointmentType").getValue(String::class.java),
                providerAddress = snapshot.child("providerAddress").getValue(String::class.java),
                companyId = snapshot.child("companyId").getValue(String::class.java),
                categoryId = snapshot.child("categoryId").getValue(String::class.java),

                // --- Respuestas ---
                replyToId = snapshot.child("replyToId").getValue(String::class.java),
                replyToContent = snapshot.child("replyToContent").getValue(String::class.java),
                replyToSenderName = snapshot.child("replyToSenderName").getValue(String::class.java),

                // --- Metadatos ---
                timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis(),
                status = snapshot.child("status").getValue(String::class.java) ?: "SENT",
                isRead = snapshot.child("isRead").getValue(Boolean::class.java) ?: false,
                isSynced = true
            )
        } catch (e: Exception) {
            null
        }
    }
}
