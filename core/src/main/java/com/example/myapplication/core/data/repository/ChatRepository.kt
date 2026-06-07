package com.example.myapplication.core.data.repository

import android.content.Context
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
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
    // === SECCIÓN: COMPARTIDA (CLIENTE Y PRESTADOR) ===
    // =========================================================================

    /**
     * Obtiene el flujo de mensajes de un chat desde la base de datos local.
     */
    fun getMessages(chatId: String): Flow<List<MessageEntity>> = chatDao.getMessagesForChat(chatId)

    /**
     * [ELITE v4] Obtiene flujo de mensajes paginados (Ley #4).
     */
    fun getMessagesPaging(chatId: String): Flow<PagingData<MessageEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { chatDao.getMessagesForChatPaging(chatId) }
        ).flow
    }

    /**
     * Envía un mensaje simple o estructurado a la nube.
     * @param message Entidad que se guardará en Room (Limpia).
     * @param networkContentOverride Si se provee, este contenido se usará para Firebase
     * (útil para enviar Base64 sin ensuciar Room).
     */
    suspend fun sendMessage(message: MessageEntity, networkContentOverride: String? = null) {
        chatDao.insertMessage(message)
        sendToRemote(message, networkContentOverride)
    }

    private suspend fun sendToRemote(message: MessageEntity, networkContentOverride: String? = null) {
        try {
            val contentForRemote = networkContentOverride ?: message.content
            
            if (contentForRemote.isBlank() && message.type == MessageType.TEXT) {
                Log.e("ChatRepository", "❌ INTENTO DE ENVIAR MENSAJE VACÍO CANCELADO: ${message.id}")
                return
            }

            // Actualizar metadatos en Firestore para la lista de chats
            firestore.collection("chats").document(message.chatId).set(
                mapOf(
                    "participants" to listOf(message.senderId, message.receiverId),
                    "lastMessage" to if (networkContentOverride != null) "[Multimedia]" else message.content,
                    "lastMessageTimestamp" to message.timestamp,
                    "updatedAt" to System.currentTimeMillis(),
                    
                    // 🔥 [FIX v8.7] Metadatos de Contexto en Firestore
                    // Permite que la escucha global sepa a qué empresa/sucursal pertenece el chat
                    // sin tener que parsear el chatId.
                    "companyId" to message.companyId,
                    "branchId" to message.branchId,
                    "senderBranchId" to message.senderBranchId,
                    "receiverBranchId" to message.receiverBranchId
                ),
                SetOptions.merge()
            ).await()

            // Enviar el objeto completo a Realtime Database
            val msgData = mutableMapOf<String, Any?>(
                "id" to message.id,
                "messageId" to message.id, 
                "chatId" to message.chatId,
                "conversationId" to message.chatId,
                "senderId" to message.senderId,
                "receiverId" to message.receiverId,
                "type" to message.type.name,
                "messageType" to message.type.name,
                "content" to contentForRemote, 
                "text" to contentForRemote,
                "timestamp" to message.timestamp,
                "isRead" to message.isRead,
                "imageUrl" to message.imageUrl,
                "thumbnailBase64" to message.thumbnailBase64,
                "latitude" to message.latitude,
                "longitude" to message.longitude,
                "locationAddress" to message.locationAddress,
                "durationSeconds" to message.durationSeconds,
                "relatedId" to message.relatedId,

                // Campos Embebidos
                "budgetDataJson" to message.budgetDataJson,
                "rejectionReason" to message.rejectionReason,
                "budgetRequestDescription" to message.budgetRequestDescription,
                "budgetRequestClientAddress" to message.budgetRequestClientAddress,

                // Datos de Turnos / Citas
                "appointmentDate" to message.appointmentDate,
                "appointmentTime" to message.appointmentTime,
                "appointmentStatus" to message.appointmentStatus,
                "appointmentType" to message.appointmentType,
                "providerAddress" to message.providerAddress,
                "companyId" to message.companyId,
                "branchId" to message.branchId,
                "categoryId" to message.categoryId,

                // 🔥 [ELITE v8.1] SYMMETRIC TAGS (OBLIGATORIOS PARA MULTIMEDIA)
                "senderBranchId" to message.senderBranchId,
                "senderCompanyId" to message.senderCompanyId,
                "receiverBranchId" to message.receiverBranchId,
                "receiverCompanyId" to message.receiverCompanyId,

                // Tags de Room (Sincronizados para reciprocidad inmediata)
                "localBranchId" to message.localBranchId,
                "localCompanyId" to message.localCompanyId,
                "remoteBranchId" to message.remoteBranchId,
                "remoteCompanyId" to message.remoteCompanyId,

                // Invitaciones de Calendario
                "calendarStartDate" to message.calendarStartDate,
                "calendarEndDate" to message.calendarEndDate,
                "availabilityJson" to message.availabilityJson,
                "bookedSlotsJson" to message.bookedSlotsJson,
                "calendarInviteMessageId" to message.calendarInviteMessageId,

                // Comprobantes de Turno
                "receiptService" to message.receiptService,
                "receiptProviderName" to message.receiptProviderName,
                "receiptIsTechnician" to message.receiptIsTechnician,
                "receiptProfession" to message.receiptProfession,
                "receiptAddress" to message.receiptAddress,
                "receiptCode" to message.receiptCode,
                "receiptPrioritizeCompany" to message.receiptPrioritizeCompany,

                "replyToId" to message.replyToId,
                "replyToContent" to message.replyToContent,
                "replyToSenderName" to message.replyToSenderName,

                // Metadatos
                "isDelivered" to message.isDelivered,
                "isFromCurrentUser" to false
            )
            
            database.reference.child("chats").child(message.chatId).child("messages").child(message.id).setValue(msgData).await()
            chatDao.updateMessageSynced(message.id)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error enviando mensaje a remoto: ${e.message}")
        }
    }

    /**
     * Activa la escucha de mensajes para un chat específico.
     * 🔥 [ELITE] Implementa Sincronización por Deltas (Solo descarga lo nuevo).
     */
    fun startListening(chatId: String) {
        stopListening()
        val myUserId = auth.currentUser?.uid ?: ""
        
        scope.launch {
            val lastTimestamp = chatDao.getLastMessageTimestamp(chatId) ?: 0L
            val ref = database.reference.child("chats").child(chatId).child("messages")
            
            // 🔥 Filtro Delta: Solo traer mensajes después del último guardado
            val query = ref.orderByChild("timestamp").startAt((lastTimestamp + 1).toDouble())

            activeListener = query.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    // 🔥 [FIX v8.0] Sincronización Total (SSOT)
                    val messageFromCloud = ChatMessageMapper.fromDataSnapshot(snapshot, chatId, context, myUserId)

                    messageFromCloud?.let { newMessage ->
                        scope.launch {
                            // 1. [ELITE] Merge SSOT: Preservar rutas locales
                            val existing = chatDao.getMessageById(newMessage.id)
                            
                            val finalMsg = if (existing != null) {
                                newMessage.copy(
                                    imageLocalPath = existing.imageLocalPath ?: newMessage.imageLocalPath,
                                    audioLocalPath = existing.audioLocalPath ?: newMessage.audioLocalPath,
                                    content = newMessage.content.ifBlank { existing.content },
                                    imageUrl = if (newMessage.imageUrl == null && existing.imageUrl?.startsWith("/") == true) 
                                               existing.imageUrl else newMessage.imageUrl
                                )
                            } else newMessage

                            // 2. Persistencia en Room (Ley #2)
                            chatDao.insertMessage(finalMsg)

                            // 3. 🔥 [LEY #8]: Lógica de Tránsito Efímero
                            // Si el mensaje es multimedia (Imagen o Audio), y ya lo tenemos en Room, 
                            // lo eliminamos de Firebase para ahorrar costo y proteger privacidad.
                            if (finalMsg.type == MessageType.IMAGE || finalMsg.type == MessageType.AUDIO) {
                                if (snapshot.exists()) {
                                    snapshot.ref.removeValue().addOnSuccessListener {
                                        Log.d("ChatRepository", "🚀 [EPHEMERAL_CLEANUP] Mensaje ${finalMsg.id} eliminado de la nube tras persistencia en Room.")
                                    }.addOnFailureListener { e ->
                                        Log.e("ChatRepository", "❌ [EPHEMERAL_CLEANUP_ERROR] ${e.message}")
                                    }
                                }
                            }

                            // 4. Sincronización proactiva de datos comerciales (Budgets/Appointments)
                            when (finalMsg.type) {
                                MessageType.BUDGET -> {
                                    finalMsg.relatedId?.let { budgetId ->
                                        budgetRepository.syncBudgetFromRemote(budgetId)
                                    }
                                    
                                    if (!finalMsg.budgetDataJson.isNullOrBlank()) {
                                        val budget = ChatMessageMapper.parseBudgetFromJson(
                                            finalMsg.budgetDataJson!!,
                                            finalMsg.relatedId ?: finalMsg.id,
                                            finalMsg.senderId,
                                            finalMsg.receiverId
                                        )
                                        budget?.let { budgetRepository.receiveBudget(it) }
                                    }
                                }
                                MessageType.VISIT, MessageType.APPOINTMENT_RECEIPT, MessageType.CALENDAR_INVITE -> {
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
                    val messageFromCloud = ChatMessageMapper.fromDataSnapshot(snapshot, chatId, context, myUserId)
                    messageFromCloud?.let { newMessage -> 
                        scope.launch { 
                            // 🔥 [ELITE] Merge SSOT: Preservar rutas locales en actualizaciones
                            val existing = chatDao.getMessageById(newMessage.id)
                            val finalMsg = if (existing != null) {
                                newMessage.copy(
                                    imageLocalPath = existing.imageLocalPath ?: newMessage.imageLocalPath,
                                    audioLocalPath = existing.audioLocalPath ?: newMessage.audioLocalPath,
                                    content = newMessage.content.ifBlank { existing.content }, // 🔥 [FIX v8.0]
                                    imageUrl = if (newMessage.imageUrl == null && existing.imageUrl?.startsWith("/") == true) 
                                               existing.imageUrl else newMessage.imageUrl
                                )
                            } else newMessage
                            
                            chatDao.insertMessage(finalMsg) 
                            
                            if (finalMsg.type == MessageType.VISIT || finalMsg.type == MessageType.APPOINTMENT_RECEIPT) {
                                val currentUserId = auth.currentUser?.uid ?: ""
                                appointmentRepository.syncAppointments(currentUserId, false)
                            }

                            if (finalMsg.type == MessageType.BUDGET && !finalMsg.budgetDataJson.isNullOrBlank()) {
                                val budget = ChatMessageMapper.parseBudgetFromJson(
                                    finalMsg.budgetDataJson!!,
                                    finalMsg.relatedId ?: finalMsg.id,
                                    finalMsg.senderId,
                                    finalMsg.receiverId
                                )
                                budget?.let { b -> budgetRepository.receiveBudget(b) }
                            }
                        } 
                    }
                }
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    fun stopListening() {
        activeListener?.let { /* Limpieza de referencia */ }
        activeListener = null
    }

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

    // =========================================================================
    // === SECCIÓN: COMPARTIDA (CLIENTE Y PRESTADOR) ===
    // =========================================================================

    /**
     * Marca todos los mensajes de un chat como leídos.
     * Sincroniza localmente y con Realtime Database.
     */
    suspend fun markMessagesAsRead(chatId: String, myUserId: String) {
        Log.d("ChatRepository", "📖 [MARK_READ] Chat: $chatId | User: $myUserId")
        chatDao.markChatAsRead(chatId, myUserId)
        
        // 🔥 [ELITE v8.8] Sincronización de ACK de lectura en Firebase
        scope.launch {
            try {
                // Buscamos mensajes que recibimos y no están leídos en la nube
                val ref = database.reference.child("chats").child(chatId).child("messages")
                val snapshot = ref.get().await()
                snapshot.children.forEach { msgSnap ->
                    val receiverId = msgSnap.child("receiverId").getValue(String::class.java)
                    val isRead = msgSnap.child("isRead").getValue(Boolean::class.java) ?: false
                    
                    if (receiverId == myUserId && !isRead) {
                        msgSnap.ref.child("isRead").setValue(true)
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "❌ [MARK_READ_ERROR] RTDB Sync failed: ${e.message}")
            }
        }
    }

    // =========================================================================
    // === SECCIÓN: APP CLIENTE ===
    // =========================================================================

    fun getTotalUnreadCount(myUserId: String): Flow<Int> = chatDao.getTotalUnreadCount(myUserId)

    fun getUnreadCountsPerChat(myUserId: String): Flow<List<ChatUnreadCount>> = chatDao.getUnreadCountsPerChat(myUserId)

    // =========================================================================
    // === SECCIÓN: APP PRESTADOR ===
    // =========================================================================

    fun getActiveChatSummaries(myUserId: String): Flow<List<ChatSummary>> = chatDao.getActiveChatSummaries(myUserId)

    /**
     * Obtiene conversaciones filtradas por empresa (o personales si companyId es null).
     */
    fun getConversationsByCompany(myUserId: String, companyId: String?): Flow<List<ChatSummary>> {
        return chatDao.getActiveChatSummaries(myUserId).map { list ->
            if (companyId == null) {
                // Filtro para perfil personal: branch y company deben ser null
                list.filter { it.branchId == null && it.companyId == null }
            } else {
                // Filtro para empresa: debe coincidir el companyId
                list.filter { it.companyId == companyId }
            }
        }
    }

    suspend fun deleteConversations(chatIds: List<String>) {
        chatIds.forEach { chatDao.deleteMessagesByChatId(it) }
    }

    /**
     * Actualiza el estado de una solicitud de turno (Aceptar/Rechazar) en RTDB.
     */
    suspend fun updateAppointmentStatus(messageId: String, chatId: String, status: String, rejectionReason: String? = null) {
        chatDao.updateAppointmentStatus(messageId, status)
        try {
            val updates = mutableMapOf<String, Any>("appointmentStatus" to status)
            if (rejectionReason != null) updates["rejectionReason"] = rejectionReason
            database.reference.child("chats").child(chatId)
                .child("messages").child(messageId)
                .updateChildren(updates).await()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error actualizando estado de turno en RTDB: ${e.message}")
        }
    }

    /**
     * Envía notificaciones de estado de turno (Cancelación, Reprogramación, etc.)
     */
    suspend fun sendStatusNotice(message: MessageEntity) {
        sendMessage(message)
    }

    // --- ESCUCHA GLOBAL (NOTIFICACIONES EN SEGUNDO PLANO) ---

    private val globalRtdbListeners = mutableMapOf<String, ChildEventListener>()
    private var globalFirestoreListener: com.google.firebase.firestore.ListenerRegistration? = null

    /**
     * Sincronización Global: Escucha mensajes de todos los chats donde participa el usuario.
     * 🔥 [ELITE v7.9] Asegurar propagación de Identidad.
     */
    fun startGlobalListening(myUserId: String) {
        if (globalFirestoreListener != null) return
        
        globalFirestoreListener = firestore.collection("chats")
            .whereArrayContains("participants", myUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                
                snapshot.documents.forEach { doc ->
                    val chatId = doc.id
                    if (!globalRtdbListeners.containsKey(chatId)) {
                        setupRtdbDeltaListener(chatId, myUserId)
                    }
                }
            }
    }

    fun startGlobalListeningForCompany(companyId: String) {
        // Similar al global, pero filtrando por la empresa como participante
        firestore.collection("chats")
            .whereArrayContains("participants", companyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                
                snapshot.documents.forEach { doc ->
                    val chatId = doc.id
                    if (!globalRtdbListeners.containsKey(chatId)) {
                        setupRtdbDeltaListener(chatId, companyId)
                    }
                }
            }
    }

    private fun setupRtdbDeltaListener(chatId: String, myUserId: String) {
        val ref = database.reference.child("chats").child(chatId).child("messages")
        
        scope.launch {
            val lastTimestamp = chatDao.getLastMessageTimestamp(chatId) ?: 0L
            val query = ref.orderByChild("timestamp").startAt((lastTimestamp + 1).toDouble())
            
            val listener = query.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val senderId = snapshot.child("senderId").getValue(String::class.java)
                    if (senderId == myUserId) return 

                    // 🔥 [FIX v7.9] Pasar myUserId para el Room Tagging correcto
                    val message = ChatMessageMapper.fromDataSnapshot(snapshot, chatId, context, myUserId)
                    message?.let { 
                        scope.launch { chatDao.insertMessage(it) } 
                        // Aquí se podrían disparar notificaciones locales
                    }
                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
            globalRtdbListeners[chatId] = listener
        }
    }

    fun stopGlobalListening() {
        globalFirestoreListener?.remove()
        globalFirestoreListener = null
        globalRtdbListeners.forEach { (chatId, listener) ->
            database.reference.child("chats").child(chatId).child("messages").removeEventListener(listener)
        }
        globalRtdbListeners.clear()
    }
}
