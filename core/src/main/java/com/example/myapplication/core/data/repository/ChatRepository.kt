package com.example.myapplication.core.data.repository

import android.content.Context
import android.util.Log
import com.example.myapplication.core.data.local.dao.ChatDao
import com.example.myapplication.core.data.local.dao.ChatSummary
import com.example.myapplication.core.data.local.dao.ChatUnreadCount
import com.example.myapplication.core.data.local.entity.MessageEntity
import com.example.myapplication.core.data.remote.ChatMessageMapper
import com.example.myapplication.core.domain.model.MessageType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE CHAT (COMPARTIDO) ---
 * Este repositorio es el motor de comunicación en tiempo real del proyecto.
 * Gestiona el envío de mensajes a Firebase (Realtime Database y Firestore)
 * y su persistencia local en Room. 
 * 
 * Funcionalidades principales:
 * 1. Envío y recepción de mensajes (Texto, Imagen, Audio, Presupuestos).
 * 2. Gestión de estados de lectura (isRead).
 * 3. Notificación de escritura (Typing status).
 * 4. Sincronización global de conversaciones.
 */
@Singleton
class ChatRepository @Inject constructor(
    private val chatDao: ChatDao,
    private val firestore: FirebaseFirestore,
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth,
    private val budgetRepository: BudgetRepository,
    private val appointmentRepository: AppointmentRepository,
    @ApplicationContext private val context: Context
) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var activeListener: ChildEventListener? = null

    // =========================================================================
    // === SECCIÓN: COMÚN (LÓGICA COMPARTIDA CLIENTE/PRESTADOR) ===
    // =========================================================================

    /**
     * Obtiene el flujo de mensajes de un chat desde la base de datos local.
     */
    fun getMessages(chatId: String): Flow<List<MessageEntity>> = chatDao.getMessagesForChat(chatId)

    /**
     * Envía un mensaje simple o estructurado a la nube.
     */
    suspend fun sendMessage(message: MessageEntity) {
        chatDao.insertMessage(message)
        sendToRemote(message)
    }

    private suspend fun sendToRemote(message: MessageEntity) {
        try {
            // Actualizar metadatos en Firestore para la lista de chats
            firestore.collection("chats").document(message.chatId).set(
                mapOf(
                    "participants" to listOf(message.senderId, message.receiverId),
                    "lastMessage" to message.content,
                    "lastMessageTimestamp" to message.timestamp,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            ).await()

            // Preparar datos del mensaje
            val contentToSend = if ((message.type == MessageType.IMAGE || message.type == MessageType.AUDIO) && 
                message.imageUrl?.startsWith("http") == false) {
                // Si es un archivo local, intentar convertir a Base64 para el prestador
                try {
                    val file = java.io.File(message.imageUrl)
                    if (file.exists()) {
                        android.util.Base64.encodeToString(file.readBytes(), android.util.Base64.NO_WRAP)
                    } else message.content
                } catch (e: Exception) {
                    message.content
                }
            } else {
                message.content
            }

            // Enviar el objeto completo a Realtime Database
            // Usamos un mapa exhaustivo para asegurar compatibilidad TOTAL con el esquema antiguo del prestador
            val msgData = mutableMapOf<String, Any?>(
                "id" to message.id,
                "messageId" to message.id, 
                "chatId" to message.chatId,
                "conversationId" to message.chatId,
                "senderId" to message.senderId,
                "receiverId" to message.receiverId,
                "type" to message.type.name,
                "messageType" to message.type.name,
                "content" to contentToSend,
                "text" to message.content, 
                "timestamp" to message.timestamp,
                "isRead" to message.isRead,
                "imageUrl" to message.imageUrl,
                "latitude" to message.latitude,
                "longitude" to message.longitude,
                "locationAddress" to message.locationAddress,
                "durationSeconds" to message.durationSeconds,
                "relatedId" to message.relatedId,
                "appointmentDate" to message.appointmentDate,
                "appointmentTime" to message.appointmentTime,
                "appointmentStatus" to message.appointmentStatus,
                "appointmentType" to message.appointmentType,
                "providerAddress" to message.providerAddress,
                "companyId" to message.companyId,
                "categoryId" to message.categoryId,
                "replyToId" to message.replyToId,
                "replyToContent" to message.replyToContent,
                "replyToSenderName" to message.replyToSenderName,
                // Campos adicionales esperados por el prestador
                "isDelivered" to false,
                "isFromCurrentUser" to false // Para el que lo recibe
            )
            
            database.reference.child("chats").child(message.chatId).child("messages").child(message.id).setValue(msgData).await()
            chatDao.updateMessageSynced(message.id)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error enviando mensaje a remoto: ${e.message}")
        }
    }

    /**
     * Activa la escucha de mensajes para un chat específico.
     */
    fun startListening(chatId: String) {
        stopListening()
        val myUserId = auth.currentUser?.uid ?: ""
        val ref = database.reference.child("chats").child(chatId).child("messages")
        activeListener = ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val senderId = snapshot.child("senderId").getValue(String::class.java)
                if (senderId == myUserId) return 

                val finalMsg = ChatMessageMapper.fromDataSnapshot(snapshot, chatId, context)

                finalMsg?.let { message ->
                    scope.launch {
                        chatDao.insertMessage(message)

                        // 🔥 SIDE EFFECT SYNC: Sincronización proactiva de datos comerciales
                        when (message.type) {
                            MessageType.BUDGET -> {
                                message.relatedId?.let { budgetId ->
                                    budgetRepository.syncBudgetFromRemote(budgetId)
                                }
                            }
                            MessageType.VISIT, MessageType.APPOINTMENT_RECEIPT, MessageType.CALENDAR_INVITE -> {
                                // Si es un comprobante o invitación de turno, sincronizar con el repo de citas
                                val currentUserId = auth.currentUser?.uid ?: ""
                                if (currentUserId.isNotBlank()) {
                                    appointmentRepository.syncAppointments(currentUserId, false)
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val message = ChatMessageMapper.fromDataSnapshot(snapshot, chatId, context)
                message?.let { 
                    scope.launch { 
                        chatDao.insertMessage(it) 
                        
                        // Si el estado de una cita o visita cambió en el chat, refrescar el repositorio de citas
                        if (it.type == MessageType.VISIT || it.type == MessageType.APPOINTMENT_RECEIPT) {
                            val currentUserId = auth.currentUser?.uid ?: ""
                            appointmentRepository.syncAppointments(currentUserId, false)
                        }
                    } 
                }
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun stopListening() {
        activeListener?.let { /* Limpieza de referencia */ }
        activeListener = null
    }

    // =========================================================================
    // === SECCIÓN: ESPECÍFICO (APP CLIENTE / APP PRESTADOR) ===
    // =========================================================================

    fun getTotalUnreadCount(myUserId: String): Flow<Int> = chatDao.getTotalUnreadCount(myUserId)

    fun getActiveChatSummaries(myUserId: String): Flow<List<ChatSummary>> = chatDao.getActiveChatSummaries(myUserId)

    fun getUnreadCountsPerChat(myUserId: String): Flow<List<ChatUnreadCount>> = chatDao.getUnreadCountsPerChat(myUserId)

    fun setTypingStatus(chatId: String, userId: String, isTyping: Boolean) {
        database.reference.child("chats").child(chatId).child("typing").child(userId).setValue(if (isTyping) true else null)
    }

    fun observeTypingStatus(chatId: String, otherUserId: String): Flow<Boolean> = callbackFlow {
        val ref = database.reference.child("chats").child(chatId).child("typing").child(otherUserId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { trySend(snapshot.getValue(Boolean::class.java) ?: false) }
            override fun onCancelled(error: DatabaseError) { trySend(false) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun setUserOnline(userId: String, isOnline: Boolean) {
        if (userId.isBlank()) return
        database.reference.child("status").child(userId).child("online").setValue(isOnline)
        database.reference.child("status").child(userId).child("lastSeen").setValue(System.currentTimeMillis())
    }

    suspend fun markChatAsRead(chatId: String, myUserId: String) {
        chatDao.markChatAsRead(chatId, myUserId)
    }

    suspend fun deleteChats(chatIds: List<String>) {
        chatIds.forEach { chatDao.deleteMessagesByChatId(it) }
    }

    fun startGlobalListening(myUserId: String) { /* TODO */ }
    fun stopGlobalListening() { }
    fun getActiveChatIds(myId: String) {}
}
