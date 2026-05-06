package com.example.myapplication.data.repository


import android.R
import android.util.Log
import com.example.myapplication.data.local.BudgetDao
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.BudgetItem
import com.example.myapplication.data.local.BudgetMiscExpense
import com.example.myapplication.data.local.BudgetProfessionalFee
import com.example.myapplication.data.local.BudgetService
import com.example.myapplication.data.local.BudgetStatus
import com.example.myapplication.data.local.BudgetTax
import com.example.myapplication.data.local.CalendarDao
import com.example.myapplication.data.local.CalendarEventEntity
import com.example.myapplication.data.local.ChatDao
import com.example.myapplication.data.local.ProviderDao
import com.example.myapplication.data.repository.util.ProviderMapper
import com.example.myapplication.data.local.ChatUnreadCount
import com.example.myapplication.data.local.EventType
import com.example.myapplication.data.local.MessageEntity
import com.example.myapplication.data.local.TenderEntity
import com.example.myapplication.data.local.VisitStatus
import com.example.myapplication.data.model.MessageType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import android.net.Uri
import com.example.myapplication.data.local.ChatLastMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatDao: ChatDao,
    private val budgetDao: BudgetDao,
    private val calendarDao: CalendarDao, // 🔥 RE-AGREGADO: Necesario para sincronizar con el calendario
    private val providerDao: ProviderDao, // 🔥 NUEVO: Necesario para sincronizar prestadores
    private val firestore: FirebaseFirestore,
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val notificationHelper: com.example.myapplication.presentation.util.NotificationHelper
) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val activeListeners = mutableListOf<Pair<DatabaseReference, ChildEventListener>>()

    // --- MENSAJES (desde Room, se actualiza en tiempo real via Firestore listener) ---
    fun getMessages(chatId: String): Flow<List<MessageEntity>> =
        chatDao.getMessagesForChat(chatId)

    fun getMessages(chatIds: List<String>): Flow<List<MessageEntity>> =
        chatDao.getMessagesForChats(chatIds.distinct())

    // --- ENVIAR MENSAJE: guarda en Room Y envía a Firestore ---
    suspend fun sendMessage(message: MessageEntity) {
        chatDao.insertMessage(message)
        sendToFirestore(message)
    }

    // --- ENVIAR IMAGEN: guarda base64 en RTDB ---
    suspend fun sendImageMessageWithBase64(message: MessageEntity, imageBase64: String) {
        chatDao.insertMessage(message)
        sendToFirestore(message.copy(content = imageBase64))
    }

    // --- ENVIAR AUDIO: guarda base64 en RTDB ---
    suspend fun sendAudioMessageWithBase64(message: MessageEntity, audioBase64: String) {
        chatDao.insertMessage(message)
        sendAudioToRTDB(message, audioBase64)
    }

    // --- ESCUCHAR MENSAJES ENTRANTES DEL PRESTADOR EN TIEMPO REAL ---
    fun startListening(chatId: String) {
        startListening(listOf(chatId), chatId)
    }

    fun startListening(chatIds: List<String>, canonicalChatId: String) {
        activeListeners.forEach { (ref, listener) -> ref.removeEventListener(listener) }
        activeListeners.clear()
        val myUid = auth.currentUser?.uid ?: return

        chatIds.distinct().forEach { sourceChatId ->
            val messagesRef = database.reference
                .child("chats").child(sourceChatId).child("messages")
            val listener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousKey: String?) {
                    val senderId = snapshot.child("senderId").getValue(String::class.java) ?: return
                    if (senderId == myUid)
                        return
                    val msgId =
                        snapshot.child("messageId").getValue(String::class.java) ?: snapshot.key
                        ?: return
                    
                    val isReadInServer = snapshot.child("isRead").getValue(Boolean::class.java) ?: false
                    
                    scope.launch {
                        if (chatDao.messageExists(msgId)) {
                            chatDao.updateMessageIsRead(msgId, isReadInServer)
                            return@launch
                        }
                        val rawType = snapshot.child("type").getValue(String::class.java) ?: "TEXT"
                        // ... (rest of code logic remains same but using isReadInServer)
                        val type = try {
                            if (rawType == "APPOINTMENT") MessageType.VISIT
                            else MessageType.valueOf(rawType)
                        } catch (e: Exception) {
                            MessageType.TEXT
                        }
                        val rawContent = snapshot.child("content").getValue(String::class.java)
                            ?: (if (type == MessageType.AUDIO) snapshot.child("audioUrl")
                                .getValue(String::class.java) else null) ?: snapshot.child("text")
                                .getValue(String::class.java) ?: ""
                        val appointmentDate =
                            snapshot.child("appointmentDate").getValue(String::class.java)
                        val appointmentTime =
                            snapshot.child("appointmentTime").getValue(String::class.java)
                        val appointmentTitle =
                            snapshot.child("appointmentTitle").getValue(String::class.java) ?: ""
                        val appointmentStatus =
                            snapshot.child("appointmentStatus").getValue(String::class.java)
                        
                        var localImagePath: String? = null
                        var localAudioPath: String? = null
                        
                        // --- SECCIÓN: PROCESAMIENTO DE CONTENIDO PESADO (BASE64) ---
                        val content = when {
                            type == MessageType.IMAGE && rawContent.isNotEmpty() && !rawContent.startsWith("http") -> {
                                // Guardar Imagen Base64 localmente y retornar "[Imagen]" para Room
                                localImagePath = com.example.myapplication.util.ImageUtils.saveBase64ToFile(context, rawContent, msgId, "IMG_", ".webp")
                                "[Imagen]"
                            }
                            type == MessageType.AUDIO && rawContent.isNotEmpty() && !rawContent.startsWith("/")
                                    && !rawContent.startsWith("http") -> {
                                // Guardar Audio Base64 localmente en almacenamiento interno
                                localAudioPath = com.example.myapplication.util.ImageUtils.saveBase64ToFile(context, rawContent, msgId, "AUD_", ".3gp")
                                "[Audio]"
                            }
                            type == MessageType.VISIT && appointmentDate != null ->
                                "$appointmentTitle|$appointmentDate|${appointmentTime ?: ""}|$rawContent"
                            else -> rawContent
                        }

                        var budgetSavedId: String? = null
                        val budgetDataJson = snapshot.child("budgetDataJson").getValue(String::class.java)
                        val categorias = snapshot.child("categorias").getValue(String::class.java)
                        
                        if (type == MessageType.BUDGET && budgetDataJson != null) {
                            budgetSavedId = parseAndSaveBudget(budgetDataJson, msgId, myUid, senderId, snapshot.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis(), categorias)
                        }

                        val entity = MessageEntity(
                            id = msgId,
                            chatId = canonicalChatId,
                            senderId = senderId,
                            receiverId = snapshot.child("receiverId").getValue(String::class.java)
                                ?: myUid,
                            type = type,
                            content = content,
                            imageUrl = localImagePath ?: localAudioPath,
                            latitude = snapshot.child("latitude").getValue(Double::class.java),
                            longitude = snapshot.child("longitude").getValue(Double::class.java),
                            locationAddress = snapshot.child("locationAddress")
                                .getValue(String::class.java),
                            durationSeconds = snapshot.child("durationSeconds")
                                .getValue(Long::class.java)?.toInt(),
                            relatedId = budgetSavedId ?: snapshot.child("relatedId")
                                .getValue(String::class.java) ?: snapshot.child("appointmentId")
                                .getValue(String::class.java),
                            appointmentDate = appointmentDate,
                            appointmentTime = appointmentTime,
                            appointmentStatus = appointmentStatus,
                            appointmentType = snapshot.child("appointmentType").getValue(String::class.java),
                            providerAddress = snapshot.child("providerAddress").getValue(String::class.java),
                            calendarStartDate = snapshot.child("calendarStartDate").getValue(String::class.java),
                            calendarEndDate = snapshot.child("calendarEndDate").getValue(String::class.java),
                            availabilityJson = snapshot.child("availabilityJson").getValue(String::class.java),
                            bookedSlotsJson = snapshot.child("bookedSlotsJson").getValue(String::class.java),
                            receiptService = snapshot.child("receiptService").getValue(String::class.java),
                            receiptProviderName = snapshot.child("receiptProviderName").getValue(String::class.java),
                            receiptIsTechnician = snapshot.child("receiptIsTechnician").getValue(Boolean::class.java),
                            receiptAddress = snapshot.child("receiptAddress").getValue(String::class.java),
                            receiptCode = snapshot.child("receiptCode").getValue(String::class.java),
                            timestamp = snapshot.child("timestamp").getValue(Long::class.java)
                                ?: System.currentTimeMillis(),
                            isRead = isReadInServer,
                            isSynced = true
                        )
                        chatDao.insertMessage(entity)
                        Log.d("ChatRepository", "Mensaje recibido (RTBD): ${entity.content}")

                        if (type == MessageType.APPOINTMENT_RECEIPT || appointmentStatus == "ACCEPTED") {
                            saveToCalendar(entity, myUid)
                        }

                        scope.launch {
                            try {
                                messagesRef.child(msgId).child("isDelivered").setValue(true)
                            } catch (e: Exception) {
                                Log.e("ChatRepository", "Error marcando isDelivered: ${e.message}")
                            }
                        }

                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousKey: String?) {
                    val senderIdDoc =
                        snapshot.child("senderId").getValue(String::class.java) ?: return
                    if (senderIdDoc != myUid)
                        return
                    val msgId =
                        snapshot.child("messageId").getValue(String::class.java) ?: snapshot.key
                        ?: return
                    scope.launch {
                        if (!chatDao.messageExists(msgId)) return@launch
                        val newStatus =
                            snapshot.child("appointmentStatus").getValue(String::class.java)
                        if (newStatus != null) chatDao.updateAppointmentStatus(msgId, newStatus)
                        val isReadNow =
                            snapshot.child("isRead").getValue(Boolean::class.java) ?: false
                        if (isReadNow) chatDao.updateMessageIsRead(msgId, true)
                        val isDeliveredNow =
                            snapshot.child("isDelivered").getValue(Boolean::class.java) ?: false
                        if (isDeliveredNow) chatDao.updateMessageStatus(msgId, "DELIVERED")
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousKey: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        "ChatRepository",
                        "RTDB listener cancelado: ${error.message}"
                    )
                }
            }

            messagesRef.orderByChild("timestamp").addChildEventListener(listener)
            activeListeners += Pair(messagesRef, listener)
        }
    }

    //Listener global notificaciones en todos los chats
    private val globalListeners = mutableMapOf<String, Pair<DatabaseReference, ChildEventListener>>()
    private var firestoreGlobalListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var currentListeningUserId: String? = null // 🔥 Track user ID to allow restarts

    private var syncStartTime = System.currentTimeMillis()

    fun startGlobalListening(myUserId: String) {
        // [REGLA DE ORO] Si el ID de usuario cambió, reiniciamos la escucha
        if (currentListeningUserId != null && currentListeningUserId != myUserId) {
            stopGlobalListening()
        }
        
        if (firestoreGlobalListener != null) return // ya escuchando al mismo usuario

        currentListeningUserId = myUserId
        firestoreGlobalListener = firestore.collection("chats")
            .whereArrayContains("participants", myUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val currentChatIds = snapshot.documents.map { it.id }.toSet()

                // Remover listeners de chats que ya no están en Firestore
                val toRemove = globalListeners.keys.filter { it !in currentChatIds }
                toRemove.forEach { chatId ->
                    globalListeners[chatId]?.let { (ref, listener) -> ref.removeEventListener(listener) }
                    globalListeners.remove(chatId)
                }

                // Agregar listeners solo para chats nuevos
                for (doc in snapshot.documents) {
                    val chatId = doc.id
                    if (globalListeners.containsKey(chatId)) continue

                    val messageRef = database.reference
                        .child("chats").child(chatId).child("messages")
                    val listener = object : ChildEventListener {
                        override fun onChildAdded(snapshot: DataSnapshot, previousKey: String?) {
                            val senderId = snapshot.child("senderId").getValue(String::class.java) ?: return
                            if (senderId == myUserId) return
                            val msgId = snapshot.child("messageId").getValue(String::class.java) ?: snapshot.key ?: return
                            
                            val isReadInServer = snapshot.child("isRead").getValue(Boolean::class.java) ?: false
                            val msgTimestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()

                            scope.launch {
                                if (chatDao.messageExists(msgId)) {
                                    // Si ya existe pero el estado de lectura cambió en el servidor (ej: sincronización entre dispositivos)
                                    chatDao.updateMessageIsRead(msgId, isReadInServer)
                                    return@launch
                                }
                                
                                val rawType = snapshot.child("type").getValue(String::class.java) ?: "TEXT"
                                val type = runCatching { MessageType.valueOf(rawType) }.getOrElse { MessageType.TEXT }
                                val rawContent = snapshot.child("content").getValue(String::class.java)
                                    ?: snapshot.child("text").getValue(String::class.java) ?: ""

                                var localImagePath: String? = null
                                var localAudioPath: String? = null

                                val content = when {
                                    type == MessageType.IMAGE && rawContent.isNotEmpty() && !rawContent.startsWith("http") -> {
                                        localImagePath = com.example.myapplication.util.ImageUtils.saveBase64ToFile(context, rawContent, msgId, "IMG_", ".webp")
                                        "[Imagen]"
                                    }
                                    type == MessageType.AUDIO && rawContent.isNotEmpty()
                                            && !rawContent.startsWith("/") && !rawContent.startsWith("http") -> {
                                        localAudioPath = com.example.myapplication.util.ImageUtils.saveBase64ToFile(context, rawContent, msgId, "AUD_", ".3gp")
                                        "[Audio]"
                                    }
                                    else -> rawContent
                                }

                                val appointmentStatus = snapshot.child("appointmentStatus").getValue(String::class.java)
                                val categorias = snapshot.child("categorias").getValue(String::class.java)
                                
                                var budgetSavedId: String? = null
                                val budgetDataJson = snapshot.child("budgetDataJson").getValue(String::class.java)
                                if (type == MessageType.BUDGET && budgetDataJson != null) {
                                    budgetSavedId = parseAndSaveBudget(budgetDataJson, msgId, myUserId, senderId, msgTimestamp, categorias)
                                }

                                // 🔥 [ZERO COST SYNC] Si el prestador no existe en Room, lo traemos de Firestore 🔥
                                scope.launch {
                                    if (providerDao.getProviderById(senderId) == null) {
                                        try {
                                            val provDoc = firestore.collection("providers").document(senderId).get().await()
                                            val providerEntity = ProviderMapper.fromFirestore(provDoc)
                                            if (providerEntity != null) {
                                                providerDao.insertAll(listOf(providerEntity))
                                                Log.d("ChatRepository", "✅ Prestador $senderId sincronizado silenciosamente al recibir mensaje")
                                            }
                                        } catch (e: Exception) {
                                            Log.w("ChatRepository", "⚠️ No se pudo sincronizar prestador $senderId: ${e.message}")
                                        }
                                    }
                                }

                                val entity = MessageEntity(
                                    id = msgId,
                                    chatId = chatId,
                                    senderId = senderId,
                                    receiverId = myUserId,
                                    type = type,
                                    content = content,
                                    imageUrl = localImagePath ?: localAudioPath,
                                    latitude = snapshot.child("latitude").getValue(Double::class.java),
                                    longitude = snapshot.child("longitude").getValue(Double::class.java),
                                    locationAddress = snapshot.child("locationAddress").getValue(String::class.java),
                                    durationSeconds = snapshot.child("durationSeconds").getValue(Long::class.java)?.toInt(),
                                    relatedId = budgetSavedId ?: snapshot.child("relatedId").getValue(String::class.java),
                                    appointmentStatus = appointmentStatus,
                                    appointmentType = snapshot.child("appointmentType").getValue(String::class.java),
                                    providerAddress = snapshot.child("providerAddress").getValue(String::class.java),
                                    appointmentDate = snapshot.child("appointmentDate").getValue(String::class.java),
                                    appointmentTime = snapshot.child("appointmentTime").getValue(String::class.java),
                                    calendarStartDate = snapshot.child("calendarStartDate").getValue(String::class.java),
                                    calendarEndDate = snapshot.child("calendarEndDate").getValue(String::class.java),
                                    availabilityJson = snapshot.child("availabilityJson").getValue(String::class.java),
                                    bookedSlotsJson = snapshot.child("bookedSlotsJson").getValue(String::class.java),
                                    receiptService = snapshot.child("receiptService").getValue(String::class.java),
                                    receiptProviderName = snapshot.child("receiptProviderName").getValue(String::class.java),
                                    receiptIsTechnician = snapshot.child("receiptIsTechnician").getValue(Boolean::class.java),
                                    receiptAddress = snapshot.child("receiptAddress").getValue(String::class.java),
                                    receiptCode = snapshot.child("receiptCode").getValue(String::class.java),
                                    timestamp = msgTimestamp,
                                    isRead = isReadInServer,
                                    isSynced = true
                                )
                                chatDao.insertMessage(entity)

                                // 🔥 SI EL MENSAJE YA LLEGA ACEPTADO O ES RECIBO, GUARDAR EN CALENDARIO LOCAL 🔥
                                if (type == MessageType.APPOINTMENT_RECEIPT || appointmentStatus == "ACCEPTED") {
                                    saveToCalendar(entity, myUserId)
                                }
                                
                                // Solo notificar si es un mensaje REALMENTE nuevo (posterior al inicio de la app)
                                if (msgTimestamp > syncStartTime && !isReadInServer) {
                                    val senderName = try {
                                        val provDoc = firestore.collection("providers").document(senderId).get().await()
                                        val perfil = provDoc.get("perfil") as? Map<*, *>
                                        (perfil?.get("nombre") as? String)
                                            ?: provDoc.getString("nombre")
                                            ?: senderId
                                    } catch (e: Exception) { senderId }
                                    
                                    notificationHelper.showChatNotification(
                                        senderId = senderId,
                                        senderName = senderName,
                                        msgType = rawType,
                                        appointmentStatus = appointmentStatus,
                                        appointmentTitle = snapshot.child("appointmentTitle").getValue(String::class.java)
                                    )
                                    Log.d("ChatRepository", "Notificación global enviada para: $msgId en chat: $chatId")
                                }
                            }
                        }
                        override fun onChildChanged(snapshot: DataSnapshot, previousKey: String?) {
                            val msgId = snapshot.child("messageId").getValue(String::class.java) ?: snapshot.key ?: return
                            val isReadNow = snapshot.child("isRead").getValue(Boolean::class.java) ?: false
                            val appointmentStatus = snapshot.child("appointmentStatus").getValue(String::class.java)

                            scope.launch {
                                if (chatDao.messageExists(msgId)) {
                                    chatDao.updateMessageIsRead(msgId, isReadNow)
                                    if (appointmentStatus != null) {
                                        chatDao.updateAppointmentStatus(msgId, appointmentStatus)
                                    }
                                }
                            }
                        }
                        override fun onChildRemoved(snapshot: DataSnapshot) {}
                        override fun onChildMoved(snapshot: DataSnapshot, previousKey: String?) {}
                        override fun onCancelled(error: DatabaseError) {}
                    }
                    messageRef.orderByChild("timestamp").addChildEventListener(listener)
                    globalListeners[chatId] = Pair(messageRef, listener)
                }
            }
    }

    fun stopGlobalListening() {
        firestoreGlobalListener?.remove()
        firestoreGlobalListener = null
        currentListeningUserId = null
        globalListeners.values.forEach { (ref, listener) -> ref.removeEventListener(listener) }
        globalListeners.clear()
    }

    fun stopListening() {
        activeListeners.forEach { (ref, listener) ->
            ref.removeEventListener(listener)
        }
        activeListeners.clear()
    }

    // --- RESTO DE MÉTODOS SIN CAMBIOS ---
    suspend fun markChatAsRead(chatId: String, myUserId: String) {
        val unreadIds = chatDao.getUnreadMessageIds(chatId, myUserId)
        chatDao.markChatAsRead(chatId, myUserId)
        if (unreadIds.isNotEmpty()) syncReadStatusToRTDB(chatId, unreadIds)
    }

    suspend fun markChatsAsRead(chatIds: List<String>, myUserId: String) {
        val distinct = chatIds.distinct()
        val unreadByChat = distinct.associateWith { chatDao.getUnreadMessageIds(it, myUserId) }
        chatDao.markChatsAsRead(distinct, myUserId)
        unreadByChat.forEach { (cid, ids) -> if (ids.isNotEmpty()) syncReadStatusToRTDB(cid, ids) }
    }

    fun getTotalUnreadCount(myUserId: String): Flow<Int> =
        chatDao.getTotalUnreadCount(myUserId)

    fun getUnreadCountsPerChat(myUserId: String): Flow<List<ChatUnreadCount>> =
        chatDao.getUnreadCountsPerChat(myUserId)

    fun getLastMessagePerChat(myUserId: String): Flow<List<ChatLastMessage>> = chatDao.getLastMessagePerChat(myUserId)

    suspend fun getBudgetById(budgetId: String): BudgetEntity? =
        budgetDao.getBudgetById(budgetId)

    fun getActiveChatIds(myUserId: String): Flow<List<String>> =
        chatDao.getActiveConversationIds(myUserId)

    fun getActiveChatSummaries(myUserId: String): Flow<List<com.example.myapplication.data.local.ChatSummary>> =
        chatDao.getActiveChatSummaries(myUserId)

    fun getOpenTendersByCategory(category: String): Flow<List<TenderEntity>> =
        budgetDao.getOpenTendersByCategory(category)

    /**
     * Elimina múltiples chats de la base de datos local y opcionalmente del servidor.
     */
    suspend fun deleteChats(chatIds: List<String>) {
        chatDao.deleteMessagesByChatIds(chatIds)
        // Opcional: Podrías marcar el chat como eliminado en Firestore o RTDB si es necesario
        Log.d("ChatRepository", "Chats eliminados localmente: ${chatIds.size}")
    }

    // --- HELPER: escribir mensaje en Firestore ---
    private suspend fun sendToFirestore(message: MessageEntity) {
        try {
            // Preparar contenido para RTDB
            var rtdbContent = message.content
            if (message.type == MessageType.IMAGE && message.imageUrl != null) {
                // Si es imagen, leemos el archivo local y lo pasamos a Base64 para el envío
                val file = java.io.File(message.imageUrl)
                if (file.exists()) {
                    val bytes = file.readBytes()
                    rtdbContent = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                }
            }

            //Firestore: solo metadata de la conversacion
            firestore.collection("chats").document(message.chatId).set(
                mapOf(
                    "participants" to listOf(message.senderId, message.receiverId),
                    "lastMessage" to if (message.type == MessageType.IMAGE) "[Imagen]" else message.content,
                    "lastMessageTimestamp" to message.timestamp,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            ).await()

            // Realtime Database: el mensaje completo
            val msgData = mutableMapOf<String, Any?>(
                "messageId" to message.id,
                "chatId" to message.chatId,
                "senderId" to message.senderId,
                "receiverId" to message.receiverId,
                "type" to message.type.name,
                "content" to rtdbContent,
                "text" to if (message.type == MessageType.IMAGE) "[Imagen]" else message.content,
                "timestamp" to message.timestamp,
                "isRead" to false,
                "isDelivered" to false,
                "latitude" to message.latitude,
                "longitude" to message.longitude,
                "locationAddress" to message.locationAddress,
                "durationSeconds" to message.durationSeconds,
                "relatedId" to message.relatedId,
                "companyId" to message.companyId,
                "categoryId" to message.categoryId,
                "appointmentType" to message.appointmentType,
                "providerAddress" to message.providerAddress,
                "appointmentDate" to message.appointmentDate,
                "appointmentTime" to message.appointmentTime
            )
            // [SINCRONIZACIÓN] Asegurar que el estado del turno se envíe correctamente
            if (message.type == MessageType.VISIT) {
                msgData["appointmentStatus"] = message.appointmentStatus ?: "PENDING"
            }
            database.reference
                .child("chats").child(message.chatId)
                .child("messages").child(message.id)
                .setValue(msgData)
            chatDao.updateMessageSynced(message.id)
            Log.d("ChatRepository", "Mensaje enviado a RTDB: ${message.id}")
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error enviado a RTBD: ${e.message}")
        }
    }


    // --- HELPER: enviar audio a RTDB ---
    private suspend fun sendAudioToRTDB(message: MessageEntity, audioBase64: String) {
        try {
            // [OPTIMIZACIÓN] El audio viaja como cadena Base64 en el campo 'content'
            // Firestore: Actualizamos metadata de la conversación
            firestore.collection("chats").document(message.chatId).set(
                mapOf(
                    "participants" to listOf(message.senderId, message.receiverId),
                    "lastMessage" to "[Audio]",
                    "lastMessageTimestamp" to message.timestamp,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            ).await()

            // Realtime Database: Enviamos el mensaje con el contenido pesado
            val msgData = mapOf(
                "messageId" to message.id,
                "chatId" to message.chatId,
                "senderId" to message.senderId,
                "receiverId" to message.receiverId,
                "type" to "AUDIO",
                "content" to audioBase64, // Aquí va el Base64
                "text" to "[Audio]",
                "timestamp" to message.timestamp,
                "isRead" to false,
                "isDelivered" to false,
                "durationSeconds" to message.durationSeconds
            )

            database.reference
                .child("chats").child(message.chatId)
                .child("messages").child(message.id)
                .setValue(msgData)

            chatDao.updateMessageSynced(message.id)
            Log.d("ChatRepository", "Audio enviado a RTDB (Base64): ${message.id}")
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error enviando audio a RTDB: ${e.message}")
        }
    }

    fun setTypingStatus(chatId: String, userId: String, isTyping: Boolean) {
        Log.d("TYPING_CLIENT", "Escribiendo: $isTyping → chats/$chatId/typing/$userId")
        database.reference
            .child("chats").child(chatId).child("typing").child(userId)
            .setValue(if (isTyping) true else null)
    }

    fun observeProviderTyping(chatId: String, providerId: String):
            kotlinx.coroutines.flow.Flow<Boolean> = kotlinx.coroutines.flow.callbackFlow {
        val ref = database.reference
            .child("chats").child(chatId).child("typing").child(providerId)
        val listener = object : com.google.firebase.database.ValueEventListener
        {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                trySend(snapshot.getValue(Boolean::class.java) ?: false)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                trySend(false)
            }

        }
        ref.addValueEventListener(listener)
        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    fun setUserOnline(userId: String, isOnline: Boolean) {
        val ref = database.reference.child("users").child(userId).child("online")
        if (isOnline) {
            ref.setValue(true)
            ref.onDisconnect().setValue(false)
        } else {
            ref.setValue(false)
        }
    }

    fun observeUserOnline(userId: String):
            kotlinx.coroutines.flow.Flow<Boolean> = kotlinx.coroutines.flow.callbackFlow {
        val ref = database.reference.child("users").child(userId).child("online")
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                trySend(snapshot.getValue(Boolean::class.java) ?: false)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                trySend(false)
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    /**
     * ── Helper centralizado para parsear y guardar presupuestos ──────────────────
     */
    private suspend fun parseAndSaveBudget(rawJson: String, msgId: String, myUid: String, senderId: String, timestamp: Long, categoriasSibling: String?): String? {
        return try {
            val obj = org.json.JSONObject(rawJson)
            
            fun parseItems(s: String) = if (s.isBlank()) emptyList() else
                s.split("|").mapNotNull { row ->
                    val p = row.split(";")
                    if (p.size >= 4) BudgetItem(
                        code = p.getOrElse(0) { "" },
                        description = p.getOrElse(1) { "" },
                        quantity = p.getOrElse(2) { "1" }.toIntOrNull() ?: 1,
                        unitPrice = p.getOrElse(3) { "0" }.toDoubleOrNull() ?: 0.0,
                        taxPercentage = p.getOrElse(4) { "0" }.toDoubleOrNull() ?: 0.0,
                        discountPercentage = p.getOrElse(5) { "0" }.toDoubleOrNull() ?: 0.0
                    ) else null
                }

            fun parseService(s: String) = if (s.isBlank()) emptyList() else
                s.split("|").mapNotNull { row ->
                    val p = row.split(";")
                    if (p.size >= 2) BudgetService(
                        code = p.getOrElse(0) { "" },
                        description = p.getOrElse(1) { "" },
                        total = p.getOrElse(2) { "0" }.toDoubleOrNull() ?: 0.0
                    ) else null
                }

            val budgetEntity = BudgetEntity(
                budgetId = msgId,
                clientId = myUid,
                providerId = senderId,
                providerName = senderId, // Fallback
                providerCompanyName = obj.optString("companyName").ifBlank { null },
                category = (obj.optString("categorias").ifBlank { null } ?: categoriasSibling)?.split(",")?.firstOrNull(),
                items = parseItems(obj.optString("items")),
                services = parseService(obj.optString("servicios")),
                subtotal = obj.optDouble("subtotal", 0.0),
                taxAmount = obj.optDouble("impuestos", 0.0),
                grandTotal = obj.optDouble("total", 0.0),
                validityDays = obj.optInt("validezDias", 7),
                notes = obj.optString("notas").ifBlank { null },
                status = BudgetStatus.PENDIENTE,
                dateTimestamp = timestamp
            )
            budgetDao.insertBudget(budgetEntity)
            msgId
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error parsing budget: ${e.message}")
            null
        }
    }

    private suspend fun saveToCalendar(message: MessageEntity, myUserId: String, providerPhotoUrl: String? = null) {
        try {
            val date = message.appointmentDate ?: return
            val time = message.appointmentTime ?: ""

            // Determinar tipo de evento prioritariamente por appointmentType
            val eventType = when {
                message.appointmentType == "TECHNICAL_VISIT" -> EventType.VISIT
                message.appointmentType == "LOCAL_APPOINTMENT" -> EventType.APPOINTMENT
                message.type == MessageType.APPOINTMENT_RECEIPT && (message.receiptIsTechnician == true) -> EventType.VISIT
                message.type == MessageType.APPOINTMENT_RECEIPT -> EventType.APPOINTMENT
                else -> EventType.VISIT
            }

            // Identificar quién es el prestador
            val providerId = if (message.senderId == myUserId) message.receiverId else message.senderId

            // Extraer datos del remitente
            var providerName = message.receiptProviderName
            var title = message.receiptService
            var address = message.providerAddress ?: message.receiptAddress

            if (providerName == null) {
                // Si no es un recibo formal, buscamos el nombre del remitente en Firestore
                try {
                    val provDoc = firestore.collection("providers").document(providerId).get().await()
                    val perfil = provDoc.get("perfil") as? Map<*, *>
                    providerName = (perfil?.get("nombre") as? String)
                        ?: provDoc.getString("nombre")
                        ?: providerId
                } catch (e: Exception) {
                    providerName = "Prestador"
                }
            }

            if (title == null || title.length > 30 && title.contains("-")) {
                // Si el título es nulo o parece un ID, usamos el contenido del mensaje (que trae la nota del turno)
                title = if (message.type == MessageType.VISIT && !message.content.contains("|")) {
                    message.content 
                } else if (message.receiptService != null) {
                    message.receiptService
                } else {
                    eventType.label
                }
            }

            if (address == null || address == "A convenir") {
                address = message.providerAddress ?: message.receiptAddress ?: message.locationAddress ?: "A convenir"
            }

            // 🔥 [REGLA DE ORO] Intentar obtener la foto del proveedor si no está en el mensaje
            var finalPhotoUrl: String? = message.imageUrl ?: providerPhotoUrl
            if (finalPhotoUrl == null) {
                try {
                    val provDoc = firestore.collection("providers").document(providerId).get().await()
                    finalPhotoUrl = provDoc.getString("photoUrl")
                } catch (e: Exception) {
                    Log.w("ChatRepository", "No se pudo recuperar la foto del proveedor para el calendario")
                }
            }

            val event = CalendarEventEntity(
                id = "evt_${message.id}",
                date = date,
                time = time,
                type = eventType,
                title = title ?: "Cita confirmada",
                provider = providerName ?: "Prestador",
                providerId = providerId,
                address = address ?: "A convenir",
                status = VisitStatus.CONFIRMED,
                providerPhotoUrl = finalPhotoUrl,
                avatarColorLong = 0xFF2197F5 // Azul Maverick por defecto
            )

            calendarDao.insertEvent(event)
            Log.d("ChatRepository", "Cita guardada en calendario local: ${event.title} para el $date")
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error guardando en calendario: ${e.message}")
        }
    }

    private fun syncReadStatusToRTDB(chatId: String, msgIds: List<String>) {
        scope.launch {
            try {
                val messageRef = database.reference
                    .child("chats").child(chatId).child("messages")
                for (msgId in msgIds) {
                    // 1. Marcar como leído
                    messageRef.child(msgId).child("isRead").setValue(true)
                    
                    // 2. [LIMPIEZA AUTOMÁTICA] Borrar contenido pesado (Base64) una vez leído
                    // Esto mantiene el costo de almacenamiento en RTDB en CERO
                    val snapshot = messageRef.child(msgId).get().await()
                    val type = snapshot.child("type").getValue(String::class.java)
                    if (type == "IMAGE" || type == "AUDIO") {
                        messageRef.child(msgId).child("content").removeValue()
                        Log.d("ChatRepository", "Limpieza: Contenido Base64 eliminado para $msgId")
                    }
                }
                Log.d("ChatRepository", "isRead RTDB sincronizado: ${msgIds.size} mensajes")
            } catch (e: Exception) {
                Log.e("ChatRepository", "Error sync isRead RTDB: ${e.message}")
            }
        }
    }
}
