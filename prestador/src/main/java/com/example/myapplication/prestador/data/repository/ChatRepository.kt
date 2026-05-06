package com.example.myapplication.prestador.data.repository

import android.util.Log
import java.util.Collections
import com.example.myapplication.prestador.data.local.dao.ConversationDao
import com.example.myapplication.prestador.data.local.dao.MessageDao
import com.example.myapplication.prestador.data.local.entity.ConversationEntity
import com.example.myapplication.prestador.data.local.entity.MessageEntity
import com.example.myapplication.prestador.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import android.net.Uri
import android.text.SpannedString
import com.example.myapplication.prestador.data.local.dao.BookedAppointmentDao
import com.google.firebase.database.ChildEvent
import com.google.firebase.database.ValueEventListener
import com.example.myapplication.prestador.data.repository.NotificacionRepository
import com.example.myapplication.prestador.data.model.NotificacionItem
import com.example.myapplication.prestador.data.model.TipoNotificacion
import com.example.myapplication.prestador.data.local.entity.BookedAppointmentEntity

@Singleton
class ChatRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val firestore: FirebaseFirestore,
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val notificationHelper: NotificationHelper,
    private val notificacionRepository: NotificacionRepository,
    private val bookedAppointmentDao: BookedAppointmentDao
) {
    private var messageListenerRef: DatabaseReference? = null
    private var messageChildListener: ChildEventListener? = null
    private var globalListener: ListenerRegistration? = null
    private val globalRtdbListeners = mutableMapOf<String, ChildEventListener>()
    private val notifiedMessageIds = mutableSetOf<String>()
    private val scope = CoroutineScope(Dispatchers.IO)

    // ── Metadata de conversacion en Firestore ──────────────────────────────────
    private suspend fun updateConversationMetadata(
        conversationId: String,
        senderId: String,
        receiverId: String,
        lastMessage: String,
        timestamp: Long
    ) {
        try {
            firestore.collection("chats").document(conversationId).set(
                mapOf(
                    "participants" to listOf(senderId, receiverId),
                    "lastMessage" to lastMessage,
                    "lastMessageTimestamp" to timestamp,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error metadata: ${e.message}")
        }
    }

    // ── Enviar mensaje de texto ────────────────────────────────────────────────
    suspend fun sendMessage(
        conversationId: String,
        text: String,
        myUserId: String,
        companyId: String? = null,
        categoryId: String? = null
    ): MessageEntity {
        val receiverId = conversationDao.getConversationById(conversationId)?.userId ?: ""
        val message = MessageEntity(
            messageId = UUID.randomUUID().toString(),
            conversationId = conversationId,
            text = text,
            timestamp = System.currentTimeMillis(),
            isFromCurrentUser = true,
            messageType = "TEXT",
            companyId = companyId,
            categoryId = categoryId
        )
        messageDao.insertMessage(message)
        conversationDao.updateLastMessage(conversationId, text, message.timestamp, "TEXT")
        updateConversationMetadata(conversationId, myUserId, receiverId, text, message.timestamp)
        try {
            database.reference.child("chats").child(conversationId)
                .child("messages").child(message.messageId)
                .setValue(hashMapOf(
                    "messageId" to message.messageId,
                    "chatId" to conversationId,
                    "senderId" to myUserId,
                    "receiverId" to receiverId,
                    "text" to text,
                    "content" to text,
                    "type" to "TEXT",
                    "timestamp" to message.timestamp,
                    "isRead" to false,
                    "isDelivered" to false,
                    "companyId" to companyId,
                    "categoryId" to categoryId
                )).await()
            messageDao.markAsSynced(message.messageId)
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error RTDB sendMessage: ${e.message}")
        }
        return message
    }

    // ── Enviar presupuesto ─────────────────────────────────────────────────────
    suspend fun sendBudgetMessage(
        conversationId: String,
        myUserId: String,
        pres: com.example.myapplication.prestador.data.local.entity.PresupuestoEntity,
        companyId: String? = null,
        categoryId: String? = null
    ): MessageEntity {
        val receiverId = conversationDao.getConversationById(conversationId)?.userId ?: ""
        val budgetJson = org.json.JSONObject().apply {
            put("numero", pres.numeroPresupuesto)
            put("total", pres.total)
            put("subtotal", pres.subtotal)
            put("impuestos", pres.impuestos)
            put("items", pres.itemsJson)
            put("servicios", pres.serviciosJson)
            put("honorarios", pres.honorariosJson)
            put("gastos", pres.gastosJson)
            put("impuestosJ", pres.impuestosJson)
            put("notas", pres.notas)
            put("validezDias", pres.validezDias)
            put("titulo", pres.tituloTrabajo)
            put("companyName", pres.providerCompanyName) // 🔥 Enviamos el nombre legible de la empresa
        }.toString()
        val message = MessageEntity(
            messageId = UUID.randomUUID().toString(),
            conversationId = conversationId,
            text = "Presupuesto ${pres.numeroPresupuesto}",
            timestamp = System.currentTimeMillis(),
            isFromCurrentUser = true,
            messageType = "BUDGET",
            budgetDataJson = budgetJson,
            companyId = companyId,
            categoryId = categoryId
        )
        messageDao.insertMessage(message)
        conversationDao.updateLastMessage(conversationId, "Presupuesto ${pres.numeroPresupuesto}", message.timestamp, "BUDGET")
        updateConversationMetadata(conversationId, myUserId, receiverId, "Presupuesto ${pres.numeroPresupuesto}", message.timestamp)
        try {
            database.reference.child("chats").child(conversationId)
                .child("messages").child(message.messageId)
                .setValue(hashMapOf(
                    "messageId" to message.messageId,
                    "chatId" to conversationId,
                    "senderId" to myUserId,
                    "receiverId" to receiverId,
                    "text" to "Presupuesto ${pres.numeroPresupuesto}",
                    "type" to "BUDGET",
                    "timestamp" to message.timestamp,
                    "isRead" to false,
                    "isDelivered" to false,
                    "budgetDataJson" to budgetJson,
                    "categorias" to pres.categorias,
                    "companyId" to companyId,
                    "categoryId" to categoryId
                )).await()
            messageDao.markAsSynced(message.messageId)
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error RTDB budget: ${e.message}")
        }
        return message
    }

    // ── Enviar imagen ──────────────────────────────────────────────────────────
    suspend fun sendImageMessage(
        conversationId: String,
        imageBase64: String,
        senderId: String,
        companyId: String? = null,
        categoryId: String? = null
    ) {
        val receiverId = conversationDao.getConversationById(conversationId)?.userId ?: ""
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        // 1. Guardar localmente primero
        val localPath = com.example.myapplication.prestador.utils.ImageUtils.saveBase64ToFile(context, imageBase64, messageId)

        val message = MessageEntity(
            messageId = messageId,
            conversationId = conversationId,
            text = "[Imagen]",
            timestamp = timestamp,
            isFromCurrentUser = true,
            messageType = "IMAGE",
            imageLocalPath = localPath,
            imageUrl = "[Imagen]",
            companyId = companyId,
            categoryId = categoryId
        )
        messageDao.insertMessage(message)
        conversationDao.updateLastMessage(conversationId, "[Imagen]", timestamp, "IMAGE")

        updateConversationMetadata(conversationId, senderId, receiverId, "[Imagen]", timestamp)
        try {
            database.reference.child("chats").child(conversationId)
                .child("messages").child(messageId)
                .setValue(hashMapOf(
                    "messageId" to messageId,
                    "chatId" to conversationId,
                    "senderId" to senderId,
                    "receiverId" to receiverId,
                    "text" to "[Imagen]",
                    "content" to imageBase64,
                    "type" to "IMAGE",
                    "timestamp" to timestamp,
                    "isRead" to false,
                    "isDelivered" to false,
                    "companyId" to companyId,
                    "categoryId" to categoryId
                ))
            messageDao.markAsSynced(messageId)
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error RTDB image: ${e.message}")
        }
    }

    // ── Enviar ubicacion ───────────────────────────────────────────────────────
    suspend fun sendLocationMessage(
        conversationId: String,
        latitude: Double,
        longitude: Double,
        senderId: String,
        companyId: String? = null,
        categoryId: String? = null
    ) {
        val receiverId = conversationDao.getConversationById(conversationId)?.userId ?: ""
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val message = MessageEntity(
            messageId = messageId,
            conversationId = conversationId,
            text = "Ubicacion compartida",
            timestamp = timestamp,
            isFromCurrentUser = true,
            messageType = "LOCATION",
            latitude = latitude,
            longitude = longitude,
            companyId = companyId,
            categoryId = categoryId
        )
        messageDao.insertMessage(message)
        conversationDao.updateLastMessage(conversationId, "Ubicacion compartida", timestamp, "LOCATION")
        updateConversationMetadata(conversationId, senderId, receiverId, "Ubicacion compartida", timestamp)
        try {
            database.reference.child("chats").child(conversationId)
                .child("messages").child(messageId)
                .setValue(hashMapOf(
                    "messageId" to messageId,
                    "chatId" to conversationId,
                    "senderId" to senderId,
                    "receiverId" to receiverId,
                    "text" to "Ubicacion compartida",
                    "content" to "Ubicacion compartida",
                    "type" to "LOCATION",
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "timestamp" to timestamp,
                    "isRead" to false,
                    "isDelivered" to false,
                    "companyId" to companyId,
                    "categoryId" to categoryId
                )).await()
            messageDao.markAsSynced(messageId)
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error RTDB location: ${e.message}")
        }
    }

    // ── Enviar audio ───────────────────────────────────────────────────────────
    suspend fun sendAudioMessage(
        conversationId: String,
        audioPath: String,
        durationSeconds: Int,
        senderId: String,
        companyId: String? = null,
        categoryId: String? = null
    ) {
        val receiverId = conversationDao.getConversationById(conversationId)?.userId ?: ""
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val file = java.io.File(audioPath)
        if (!file.exists() || file.length() == 0L) {
            Log.e("ChatRepo", "Archivo de audio no existe: $audioPath")
            return
        }
        val audioBase64 = try {
            android.util.Base64.encodeToString(file.readBytes(), android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error codificando audio: ${e.message}")
            return
        }
        val message = MessageEntity(
            messageId = messageId,
            conversationId = conversationId,
            text = "[Audio]",
            timestamp = timestamp,
            isFromCurrentUser = true,
            messageType = "AUDIO",
            audioUrl = audioPath,
            audioLocalPath = audioPath,
            audioDuration = durationSeconds,
            companyId = companyId,
            categoryId = categoryId
        )
        messageDao.insertMessage(message)
        conversationDao.updateLastMessage(conversationId, "[Audio]", timestamp, "AUDIO")
        updateConversationMetadata(conversationId, senderId, receiverId, "[Audio]", timestamp)
        try {
            database.reference.child("chats").child(conversationId)
                .child("messages").child(messageId)
                .setValue(hashMapOf(
                    "messageId" to messageId,
                    "chatId" to conversationId,
                    "senderId" to senderId,
                    "receiverId" to receiverId,
                    "content" to audioBase64, // El audio viaja como cadena de caracteres
                    "text" to "[Audio]",
                    "durationSeconds" to durationSeconds,
                    "type" to "AUDIO",
                    "timestamp" to timestamp,
                    "isRead" to false,
                    "isDelivered" to false,
                    "companyId" to companyId,
                    "categoryId" to categoryId
                )).await()
            messageDao.markAsSynced(messageId)
            Log.d("ChatRepo", "Audio enviado a RTDB desde prestador")
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error RTDB audio: ${e.message}")
        }
    }

    // ── Enviar turno/cita ──────────────────────────────────────────────────────
    suspend fun sendAppointmentMessage(
        conversationId: String,
        senderId: String,
        appointmentId: String,
        title: String,
        date: String,
        time: String,
        notes: String,
        companyId: String? = null,
        categoryId: String? = null
    ) {
        val receiverId = conversationDao.getConversationById(conversationId)?.userId ?: ""
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val message = MessageEntity(
            messageId = messageId,
            conversationId = conversationId,
            text = notes.ifBlank { "Nueva propuesta de turno" },
            timestamp = timestamp,
            isFromCurrentUser = true,
            messageType = "APPOINTMENT",
            appointmentId = appointmentId,
            appointmentTitle = title,
            appointmentDate = date,
            appointmentTime = time,
            appointmentStatus = "PENDING",
            companyId = companyId,
            categoryId = categoryId
        )
        messageDao.insertMessage(message)
        conversationDao.updateLastMessage(conversationId, "Propuesta de turno: $date $time", timestamp, "APPOINTMENT")
        updateConversationMetadata(conversationId, senderId, receiverId, "Propuesta de turno: $date $time", timestamp)
        try {
            database.reference.child("chats").child(conversationId)
                .child("messages").child(messageId)
                .setValue(hashMapOf(
                    "messageId" to messageId,
                    "chatId" to conversationId,
                    "senderId" to senderId,
                    "receiverId" to receiverId,
                    "text" to (notes.ifBlank { "Nueva propuesta de turno" }),
                    "content" to (notes.ifBlank { "Nueva propuesta de turno" }),
                    "type" to "APPOINTMENT",
                    "timestamp" to timestamp,
                    "isRead" to false,
                    "isDelivered" to false,
                    "appointmentId" to appointmentId,
                    "appointmentTitle" to title,
                    "appointmentDate" to date,
                    "appointmentTime" to time,
                    "appointmentStatus" to "PENDING",
                    "companyId" to companyId,
                    "categoryId" to categoryId
                )).await()
            messageDao.markAsSynced(messageId)
            Log.d("ChatRepo", "Appointment enviado a RTDB")
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error RTDB appointment: ${e.message}")
        }
    }

    //Enviar invitacion de calendario (CALENDAR_INVITE
    //El prestador comparte su disponibilidad; el cliente elige dia y hora
    suspend fun sendCalendarInviteMessage(
        conversationId: String,
        myUserId: String,
        startDate: String,
        endDate: String,
        availabilityJson: String,
        bookedSlotsJson: String,
        companyId: String? = null,
        categoryId: String? = null,
        appointmentType: String? = null,
        providerAddress: String? = null,
        serviceCategory: String = ""
    ): MessageEntity {
        val receiverId = conversationDao.getConversationById(conversationId)?.userId ?: ""
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val message = MessageEntity(
            messageId = messageId,
            conversationId = conversationId,
            text = "Calendario de disponibilidad enviado",
            timestamp = timestamp,
            isFromCurrentUser = true,
            messageType = "CALENDAR_INVITE",
            calendarStartDate = startDate,
            calendarEndDate = endDate,
            availabilityJson = availabilityJson,
            bookedSlotsJson = bookedSlotsJson,
            companyId = companyId,
            categoryId = categoryId,
            appointmentType = appointmentType,
            providerAddress = providerAddress
        )
        messageDao.insertMessage(message)
        conversationDao.updateLastMessage(conversationId, "Calendario de disponibilidad", timestamp, "CALENDAR_INVITE")
        updateConversationMetadata(conversationId, myUserId, receiverId, "Calendario de disponibilidad", timestamp)
        try {
            database.reference.child("chats").child(conversationId)
                .child("messages").child(messageId)
                .setValue(hashMapOf(
                    "messageId" to messageId,
                    "chatId" to conversationId,
                    "senderId" to myUserId,
                    "receiverId" to receiverId,
                    "text" to "Calendario de disponibilidad enviado",
                    "type" to "CALENDAR_INVITE",
                    "timestamp" to timestamp,
                    "isRead" to false,
                    "isDelivered" to false,
                    "calendarStartDate" to startDate,
                    "calendarEndDate" to endDate,
                    "availabilityJson" to availabilityJson,
                    "bookedSlotsJson" to bookedSlotsJson,
                    "companyId" to companyId,
                    "categoryId" to categoryId,
                    "appointmentType" to appointmentType,
                    "providerAddress" to providerAddress,
                    "serviceCategory" to serviceCategory
                )).await()
            messageDao.markAsSynced(messageId)
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error RTDB calendarInvite: ${e.message}")
        }
        return message
    }

    //Actualizar estado de solicitud de turno
    //El prestador acepta o rechaza; se actualiza Room + RTDB
    suspend fun updateAppointementRequestStatus(
        messageId: String,
        conversationId: String,
        newStatus: String,        // "ACCEPTED" o "REJECTED"
        rejectionReason: String? = null
    ) {
        //Actualizar en room
        messageDao.updateAppointmentStatus(messageId, newStatus, rejectionReason)
        //Actualizar en Firebase RTDB
        try {
            val updates = mutableMapOf<String, Any>("appointmentStatus" to newStatus)
            if (rejectionReason != null) updates["rejectionReason"] = rejectionReason
            database.reference.child("chats").child(conversationId)
                .child("messages").child(messageId)
                .updateChildren(updates).await()
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error RTDB updateRequestStatus: ${e.message}")
        }
    }

    suspend fun updateAppointmentRequestStatus(
        messageId: String,
        conversationId: String,
        newStatus: String,
        rejectionReason: String? = null
    ) = updateAppointementRequestStatus(messageId, conversationId, newStatus, rejectionReason)

    // ── Escuchar mensajes en tiempo real (RTDB) ────────────────────────────────
    fun startListening(conversationId: String, myUserId: String) {
        messageChildListener?.let { messageListenerRef?.removeEventListener(it) }
        val messagesRef = database.reference.child("chats").child(conversationId).child("messages")
        messageListenerRef = messagesRef
        val listeningStartAt = System.currentTimeMillis()
        messageChildListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousKey: String?) {
                val senderId = snapshot.child("senderId").getValue(String::class.java)
                    ?: return
                val isOwn = senderId == myUserId
                if (isOwn) return  // ya insertado por sendMessage, evitar duplicado por race condition
                val msgId = snapshot.child("messageId").getValue(String::class.java) ?:
                snapshot.key ?: return
                val msgType = snapshot.child("type").getValue(String::class.java) ?:
                "TEXT"

                var resolvedImageUrl: String? = null
                var localImagePath: String? = null

                if (msgType == "IMAGE") {
                    val base64 = snapshot.child("content").getValue(String::class.java)
                        ?: snapshot.child("imageUrl").getValue(String::class.java)
                    if (base64 != null && !base64.startsWith("http") && !base64.startsWith("/")) {
                        localImagePath = com.example.myapplication.prestador.utils.ImageUtils.saveBase64ToFile(context, base64, msgId)
                        resolvedImageUrl = localImagePath // Correción: Ahora usa el path local en vez de "[imagen]"
                    } else {
                        resolvedImageUrl = base64
                    }
                }

                val resolvedAudioUrl: String? = if (msgType == "AUDIO") {
                    val base64 = snapshot.child("audioUrl").getValue(String::class.java)
                        ?: snapshot.child("content").getValue(String::class.java)
                    if (base64 != null && !base64.startsWith("http") && !base64.startsWith("/")) try {
                        val bytes = android.util.Base64.decode(base64, android.util.Base64.NO_WRAP)
                        val tmp = java.io.File(context.cacheDir, "audio_recv_${msgId}.m4a")
                        if (!tmp.exists()) tmp.writeBytes(bytes)
                        tmp.absolutePath
                    } catch (e: Exception) { null }
                    else base64
                } else null
                val rawText = snapshot.child("text").getValue(String::class.java) ?: ""
                val visitParts = if (msgType == "VISIT" && rawText.contains("|")) rawText.split("|") else null
                val parsedTitle = visitParts?.getOrNull(0) ?: snapshot.child("appointmentTitle").getValue(String::class.java)
                val parsedDate  = visitParts?.getOrNull(1) ?: snapshot.child("appointmentDate").getValue(String::class.java)
                val parsedTime  = visitParts?.getOrNull(2) ?: snapshot.child("appointmentTime").getValue(String::class.java)
                val parsedNotes = visitParts?.getOrNull(3) ?: rawText

                val msg = MessageEntity(
                    messageId = msgId,
                    conversationId = conversationId,
                    text = if (visitParts != null) parsedNotes else rawText,
                    timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis(),
                    isFromCurrentUser = isOwn,
                    messageType = if (msgType == "VISIT") "APPOINTMENT_REQUEST" else msgType,
                    imageUrl = resolvedImageUrl,
                    imageLocalPath = localImagePath, // Ruta local guardada
                    audioDuration = snapshot.child("audioDuration").getValue(Long::class.java)?.toInt(),
                    latitude = snapshot.child("latitude").getValue(Double::class.java),
                    longitude = snapshot.child("longitude").getValue(Double::class.java),
                    appointmentId = snapshot.child("appointmentId").getValue(String::class.java),
                    appointmentTitle = parsedTitle,
                    appointmentDate = parsedDate,
                    appointmentTime = parsedTime,
                    appointmentStatus = snapshot.child("appointmentStatus").getValue(String::class.java)
                        ?: if (msgType == "VISIT") "PENDING" else null,
                    appointmentType = snapshot.child("appointmentType").getValue(String::class.java),
                    providerAddress = snapshot.child("providerAddress").getValue(String::class.java),
                    rejectionReason = snapshot.child("rejectionReason").getValue(String::class.java),
                    budgetDataJson = snapshot.child("budgetDataJson").getValue(String::class.java),
                    calendarStartDate = snapshot.child("calendarStartDate").getValue(String::class.java),
                    calendarEndDate = snapshot.child("calendarEndDate").getValue(String::class.java),
                    availabilityJson = snapshot.child("availabilityJson").getValue(String::class.java),
                    bookedSlotsJson = snapshot.child("bookedSlotsJson").getValue(String::class.java),
                    calendarInviteMessageId = snapshot.child("calendarInviteMessageId").getValue(String::class.java),
                    receiptService = snapshot.child("receiptService").getValue(String::class.java),
                    budgetRequestDescription = snapshot.child("budgetRequestDescription").getValue(String::class.java),
                    budgetRequestClientAddress = snapshot.child("budgetRequestClientAddress").getValue(String::class.java),
                    receiptProviderName = snapshot.child("receiptProviderName").getValue(String::class.java),
                    receiptProfession = snapshot.child("receiptProfession").getValue(String::class.java),
                    receiptAddress = snapshot.child("receiptAddress").getValue(String::class.java),
                    receiptCode = snapshot.child("receiptCode").getValue(String::class.java),
                    receiptIsTechnician = snapshot.child("receiptIsTechnician").getValue(Boolean::class.java) ?: false,
                    receiptPrioritizeCompany = snapshot.child("receiptPrioritizeCompany").getValue(Boolean::class.java) ?: false,
                    categoryId = snapshot.child("categoryId").getValue(String::class.java),
                )
                scope.launch {
                    try {
                        if (conversationDao.getConversationById(conversationId) == null) {
                            // Buscar nombre y foto del remitente (cliente o prestador)
                            val resolved = resolveParticipantData(senderId, null)
                            // Si el remitente no existe en Firebase, ignorar el mensaje
                            if (resolved == null) {
                                Log.w("ChatRepo", "Remitente $senderId no encontrado en Firebase. Ignorando mensaje.")
                                return@launch
                            }
                            val (displayName, avatarUrl) = resolved
                            conversationDao.insertConversation(
                                ConversationEntity(
                                    conversationId = conversationId,
                                    userId = senderId,
                                    userName = displayName,
                                    userAvatarUrl = avatarUrl,
                                    isSynced = false,
                                    lastMessageTimestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis(),
                                    lastMessage = snapshot.child("text").getValue(String::class.java) ?: ""
                                )
                            )
                            Log.d("ChatRepo", "Conversación creada en startListening: $conversationId")
                        }

                        val existsInRoom = messageDao.getMessageById(msgId) != null
                        if (!existsInRoom) {
                            messageDao.insertMessage(msg)
                        }
                        val isNewMessage = msg.timestamp >= listeningStartAt - 5_000
                        if (!isOwn && !existsInRoom) {
                            conversationDao.incrementUnreadCount(conversationId)
                            val senderName = conversationDao.getConversationById(conversationId)?.userName ?: senderId

                            notificationHelper.showChatNotification(
                                senderId = senderId,
                                senderName = senderName,
                                msgType = msgType,
                                appointmentStatus = msg.appointmentStatus,
                                appointmentTitle = msg.appointmentTitle
                            )

                            val (nTitulo, nMensaje, nTipo) = when (msgType) {
                                "AUDIO" -> Triple(senderName, " Te envió un audio", TipoNotificacion.MENSAJE)
                                "IMAGE" -> Triple(senderName, " Te envió un imagen", TipoNotificacion.MENSAJE)
                                "LOCATION" -> Triple(senderName, " Compartió su ubicación", TipoNotificacion.MENSAJE)
                                "BUDGET" -> Triple(" Presupuesto", "$senderName respondió un presupuesto", TipoNotificacion.PRESUPUESTO)
                                "VISIT", "APPOINTMENT" -> {
                                    val appt = msg.appointmentTitle?.let { "\"$it\"" } ?: "una cita"
                                    when (msg.appointmentStatus) {
                                        "PENDING" -> Triple(" Solicitud de cita", "$senderName solicitó $appt", TipoNotificacion.CITA)
                                        "CONFIRMED" -> Triple(" Cita confirmada", "$senderName solicitó $appt", TipoNotificacion.CITA)
                                        "REJECTED" -> Triple(" Cita rechazada", "$senderName rechazó $appt", TipoNotificacion.CITA)
                                        else -> Triple(" Cita", "$senderName actializó $appt", TipoNotificacion.CITA)
                                    }
                                }
                                else -> Triple(senderName, " Nuevo mensaje", TipoNotificacion.MENSAJE)
                            }

                            notificacionRepository.guardar(
                                NotificacionItem(
                                    tipo = nTipo,
                                    titulo = nTitulo,
                                    mensaje = nMensaje,
                                    fechaMs = msg.timestamp,
                                    leida = false,
                                    accionRoute = "open_chat/$senderId"
                                )
                            )

                            try {
                                messagesRef.child(msgId).child("isDelivered").setValue(true)
                            } catch (e: Exception) {
                                Log.e("ChatRepo", "Error marcando isDelivered: ${e.message}")
                            }
                        }

                        conversationDao.updateLastMessage(conversationId, msg.text ?: "", msg.timestamp, "TEXT")

                        if (isOwn) {
                            val isReadNow = snapshot.child("isRead").getValue(Boolean::class.java) ?: false
                            if (isReadNow) messageDao.markAsRead(msg.messageId)
                            val isDeliveredNow = snapshot.child("isDelivered").getValue(Boolean::class.java) ?: false
                            if (isDeliveredNow) messageDao.marAsDelivered(msg.messageId)
                        }
                    } catch (e: Exception) {
                        Log.e("ChatRepo", "Error en startListening coroutine: ${e.message}")
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousKey: String?) {
                val senderId = snapshot.child("senderId").getValue(String::class.java) ?: return
                if (senderId != myUserId) return
                val msgId = snapshot.child("messageId").getValue(String::class.java) ?: snapshot.key ?: return
                scope.launch {
                    val isReadNow = snapshot.child("isRead").getValue(Boolean::class.java) ?: false
                    if (isReadNow) messageDao.markAsRead(msgId)
                    val isDeliveredNow = snapshot.child("isDelivered").getValue(Boolean::class.java) ?: false
                    if (isDeliveredNow) messageDao.marAsDelivered(msgId)
                    val newStatus = snapshot.child("appointmentStatus").getValue(String::class.java)
                    if (newStatus != null) messageDao.updateAppointmentStatus(msgId, newStatus, null)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousKey: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatRepo", "RTDB listener cancelado: ${error.message}")
            }
        }
        messagesRef.orderByChild("timestamp").addChildEventListener(messageChildListener!!)
    }

    fun stopListening() {
        messageChildListener?.let { messageListenerRef?.removeEventListener(it) }
        messageChildListener = null
        messageListenerRef = null
    }

    fun observeClientTyping(chatId: String, clientUserId: String): Flow<Boolean> = callbackFlow {
        Log.d("TYPING_PRESTADOR", "Observando: chats/$chatId/typing/$clientUserId")
        val ref = database.reference
            .child("chats").child(chatId).child("typing").child(clientUserId)
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Boolean::class.java) == true
                Log.d("TYPING_PRESTADOR", "onDataChange → $value")
                trySend(value)
            }
            override fun onCancelled(error: DatabaseError) { trySend(false) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun setTypingStatus(chatId: String, userId: String, isTyping: Boolean) {
        database.reference
            .child("chats").child(chatId).child("typing").child(userId)
            .setValue(if (isTyping) true else null)
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
            kotlinx.coroutines.flow.Flow<Boolean> = callbackFlow {
        val ref = database.reference.child("users").child(userId).child("online")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Boolean::class.java) ?: false)
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(false)
            }
        }
        ref.addValueEventListener(listener)
        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    // ── Resolución de nombre/avatar de un participante ─────────────────────────
    /**
     * Busca nombre y avatar del participante: primero en "usuarios", luego en "providers".
     * Devuelve null SOLO si el documento NO existe en ninguna colección (usuario eliminado de Firebase).
     * Si el documento existe pero no tiene nombre, devuelve el userId como nombre (usuario sin perfil completo).
     */
    private suspend fun resolveParticipantData(userId: String, fallbackAvatarUrl: String?): Pair<String, String?>? {

        fun extractName(doc: com.google.firebase.firestore.DocumentSnapshot): String {
            val name = doc.getString("name") ?: ""
            val lastName = doc.getString("lastName") ?: ""
            val displayNameField = doc.getString("displayName") ?: ""
            val emailField = doc.getString("email") ?: ""
            return when {
                name.isNotBlank() || lastName.isNotBlank() -> "$name $lastName".trim()
                displayNameField.isNotBlank() -> displayNameField
                emailField.isNotBlank() -> emailField.substringBefore("@")
                else -> ""
            }
        }

        // Intentar en colección de clientes
        try {
            val userDoc = firestore.collection("usuarios").document(userId).get().await()
            if (userDoc.exists()) {
                // Documento encontrado → el usuario existe, usar lo que haya (o "Cliente" si todo vacío)
                val name = extractName(userDoc).ifBlank { "Cliente" }
                val photo = userDoc.getString("photoUrl") ?: fallbackAvatarUrl
                Log.d("ChatRepo", "✅ usuarios/$userId → nombre='$name'")
                return Pair(name, photo)
            }
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error buscando en usuarios/$userId: ${e.message}")
        }

        // Fallback: intentar en colección de prestadores
        try {
            val provDoc = firestore.collection("providers").document(userId).get().await()
            if (provDoc.exists()) {
                val name = extractName(provDoc).ifBlank { "Cliente" }
                val photo = provDoc.getString("photoUrl") ?: fallbackAvatarUrl
                Log.d("ChatRepo", "✅ providers/$userId → nombre='$name'")
                return Pair(name, photo)
            }
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error buscando en providers/$userId: ${e.message}")
        }

        // Documento no existe en ninguna colección → usuario eliminado
        Log.w("ChatRepo", "⚠️ $userId no encontrado en usuarios ni providers")
        return null
    }

    // ── Dirección del cliente (para visitas técnicas) ──────────────────────────
    suspend fun getClientMainAddress(clientId: String): String? {
        return try {
            val addressDocs = firestore
                .collection("usuarios")
                .document(clientId)
                .collection("personalAddresses")
                .get()
                .await()
            val first = addressDocs.documents.firstOrNull() ?: return null
            val calle = first.getString("calle").orEmpty()
            val numero = first.getString("numero").orEmpty()
            val localidad = first.getString("localidad").orEmpty()
            val provincia = first.getString("provincia").orEmpty()
            listOf("$calle $numero".trim(), localidad, provincia)
                .filter { it.isNotBlank() }
                .joinToString(", ")
                .ifBlank { null }
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error obteniendo dirección del cliente $clientId: ${e.message}")
            null
        }
    }

    // ── Sincronizar conversaciones desde Firestore ─────────────────────────────
    private val userDataCache = mutableMapOf<String, Pair<String, String?>>()
    // IDs ya detectados como fantasmas — evita que coroutines concurrentes los procesen múltiples veces
    private val ghostsBeingDeleted = Collections.newSetFromMap(java.util.concurrent.ConcurrentHashMap<String, Boolean>())

    fun syncConversationsFromFirestore(myUserId: String) {
        firestore.collection("chats")
            .whereArrayContains("participants", myUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e("ChatRepo", "Error sync conversaciones: ${error?.message}")
                    return@addSnapshotListener
                }
                scope.launch {
                    for (doc in snapshot.documents) {
                        @Suppress("UNCHECKED_CAST")
                        val participants = doc.get("participants") as? List<String> ?: continue
                        val otherUserId = participants.firstOrNull { it != myUserId } ?: continue
                        val existing = conversationDao.getConversationById(doc.id)

                        // 1. Siempre re-fetchear si el nombre parece un ID crudo o está vacío
                        val storedName = existing?.userName ?: ""
                        val nameSeemsBad = storedName.isBlank()
                                || storedName == otherUserId
                                || storedName == "Cliente"
                                || storedName.length > 20 && storedName.none { it == ' ' } // UID largo sin espacios
                                || storedName.matches(Regex("^[A-Za-z0-9_-]{20,}$"))       // UID alfanumérico largo
                                || storedName.matches(Regex("^[A-Za-z]+-?[A-Za-z]+-?\\d+$")) // P-Jar-0
                        var displayName = storedName.takeIf { !nameSeemsBad } ?: otherUserId
                        var freshAvatarUrl: String? = existing?.userAvatarUrl

                        if (existing == null || !userDataCache.containsKey(otherUserId) || nameSeemsBad) {
                            val resolved = resolveParticipantData(otherUserId, freshAvatarUrl)
                            if (resolved == null) {
                                // Si ya está siendo eliminado por otra coroutine, saltar
                                if (!ghostsBeingDeleted.add(doc.id)) continue
                                Log.w("ChatRepo", "Usuario $otherUserId no encontrado en Firebase. Eliminando conversación ${doc.id}.")
                                conversationDao.deleteConversationById(doc.id)
                                try {
                                    firestore.collection("chats").document(doc.id).delete().await()
                                    Log.w("ChatRepo", "Chat fantasma eliminado de Firestore: ${doc.id}")
                                } catch (e: Exception) {
                                    Log.e("ChatRepo", "Error borrando chat fantasma: ${e.message}")
                                    ghostsBeingDeleted.remove(doc.id) // permitir reintento si falló
                                }
                                continue
                            }
                            displayName = resolved.first
                            freshAvatarUrl = resolved.second
                            userDataCache[otherUserId] = resolved
                        } else {
                            // Usar cache si ya lo pedimos en esta sesión y el nombre era válido
                            userDataCache[otherUserId]?.let {
                                displayName = it.first
                                freshAvatarUrl = it.second
                            }
                        }

                        val conversation = ConversationEntity(
                            conversationId = doc.id,
                            userId = otherUserId,
                            userName = displayName,
                            userAvatarUrl = freshAvatarUrl,
                            lastMessage = doc.getString("lastMessage") ?: "",
                            lastMessageTimestamp = doc.getLong("lastMessageTimestamp") ?: 0L,
                            unreadCount = existing?.unreadCount ?: 0,
                            notificationsEnabled = existing?.notificationsEnabled ?: true,
                            isVisible = existing?.isVisible ?: true,
                            isLocked = existing?.isLocked ?: false,
                            isSynced = true
                        )

                        if (existing == null) {
                            // Eliminar duplicado si ya existe una conv con el mismo userId pero distinto ID
                            val duplicate = conversationDao.getConversationByUserId(otherUserId)
                            if (duplicate != null && duplicate.conversationId != doc.id) {
                                Log.w("ChatRepo", "Eliminando conversación duplicada: ${duplicate.conversationId} para usuario $otherUserId")
                                conversationDao.deleteConversationById(duplicate.conversationId)
                            }
                            conversationDao.insertConversation(conversation)
                        } else {
                            // Solo actualizar si algo relevante cambió para evitar loops de recomposición
                            if (existing.lastMessage != conversation.lastMessage ||
                                existing.lastMessageTimestamp != conversation.lastMessageTimestamp ||
                                existing.userName != conversation.userName ||
                                existing.userAvatarUrl != conversation.userAvatarUrl) {
                                conversationDao.updateConversation(conversation)
                            }
                        }
                    }
                }
            }
    }

    fun getConversationsByProvider(providerId: String): Flow<List<ConversationEntity>> =
        conversationDao.getAllConversations()

    fun getConversationsByCompany(companyId: String?): Flow<List<ConversationEntity>> =
        conversationDao.getConversationsByCompany(companyId)

    fun getMessagesByConversation(conversationId: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesByConversation(conversationId)

    fun getAllConversations(): Flow<List<ConversationEntity>> =
        conversationDao.getAllConversations()

    fun getActiveConversations(): Flow<List<ConversationEntity>> =
        conversationDao.getActiveConversations()

    fun getTotalUnreadCount(): Flow<Int> =
        conversationDao.getTotalUnreadCountFlow()

    suspend fun saveMessage(message: MessageEntity) {
        messageDao.insertMessage(message)
    }

    suspend fun saveConversation(conversation: ConversationEntity) {
        conversationDao.insertConversation(conversation)
    }

    // ── Marcar mensajes como leidos (RTDB) ────────────────────────────────────
    suspend fun markMessagesAsRead(conversationId: String) {
        conversationDao.resetUnreadCount(conversationId)
        messageDao.markAllAsRead(conversationId)
        scope.launch {
            try {
                val unreadMessages = messageDao.getUnreadMessages(conversationId)
                if (unreadMessages.isEmpty()) return@launch
                val messagesRef = database.reference.child("chats").child(conversationId).child("messages")
                for (msg in unreadMessages) {
                    // 1. Marcar como leído en RTDB
                    messagesRef.child(msg.messageId).child("isRead").setValue(true)

                    // 2. [LIMPIEZA AUTOMÁTICA] Borrar contenido pesado (Base64) una vez leído
                    // Mantiene el costo de almacenamiento en RTDB en CERO
                    if (msg.messageType == "IMAGE" || msg.messageType == "AUDIO") {
                        messagesRef.child(msg.messageId).child("content").removeValue()
                        Log.d("ChatRepo", "Limpieza: Contenido Base64 eliminado para ${msg.messageId}")
                    }
                }
                Log.d("ChatRepo", "isRead RTDB sincronizado: ${unreadMessages.size} mensajes")
            } catch (e: Exception) {
                Log.e("ChatRepo", "Error sincronizando isRead RTDB: ${e.message}")
            }
        }
    }

    suspend fun deleteMessage(messageId: String) {
        messageDao.deleteMessageById(messageId)
    }

    // ── Actualizar estado de cita (RTDB) ───────────────────────────────────────
    suspend fun updateAppointmentStatus(messageId: String, status: String, reason: String?) {
        messageDao.updateAppointmentStatus(messageId, status, reason)
        val message = messageDao.getMessageById(messageId) ?: return
        try {
            val msgRef = database.reference
                .child("chats").child(message.conversationId)
                .child("messages").child(messageId)
            msgRef.child("appointmentStatus").setValue(status)
            if (reason != null) msgRef.child("rejectionReason").setValue(reason)
            Log.d("ChatRepo", "appointmentStatus RTDB actualizado: $status")
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error actualizando appointment en RTDB: ${e.message}")
        }
        if (status == "ACCEPTED" && !message.appointmentDate.isNullOrBlank()) {
            try {
                val conversation = conversationDao.getConversationById(message.conversationId)
                val clientId = conversation?.userId ?: message.conversationId
                val querySnap = firestore.collection("appointments")
                    .whereEqualTo("clientId", clientId)
                    .whereEqualTo("date", message.appointmentDate)
                    .whereEqualTo("time", message.appointmentTime ?: "")
                    .get().await()
                for (doc in querySnap.documents) {
                    doc.reference.update("status", "accepted").await()
                }
            } catch (e: Exception) {
                Log.e("ChatRepo", "Error actualizando appointments Firestore: ${e.message}")
            }
        }
    }

    suspend fun getMessageById(messageId: String): MessageEntity? =
        messageDao.getMessageById(messageId)

    suspend fun getConversationById(conversationId: String): ConversationEntity? =
        conversationDao.getConversationById(conversationId)

    // ── Listener global para notificaciones (RTDB) ────────────────────────────
    fun startGlobalListening(myUserId: String) {
        val listeningStartAt = System.currentTimeMillis()

        // No reiniciar todo si ya está escuchando Firestore
        if (globalListener != null) return

        globalListener = firestore.collection("chats")
            .whereArrayContains("participants", myUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val currentChatIds = snapshot.documents.map { it.id }.toSet()

                // Remover listeners de chats que ya no están en la lista
                val toRemove = globalRtdbListeners.keys.filter { it !in currentChatIds }
                toRemove.forEach { convId ->
                    val ref = database.reference.child("chats").child(convId).child("messages")
                    globalRtdbListeners[convId]?.let { ref.removeEventListener(it) }
                    globalRtdbListeners.remove(convId)
                }

                for (chatDoc in snapshot.documents) {
                    val conversationId = chatDoc.id

                    // Solo agregar si no tenemos ya un listener para este chat
                    if (globalRtdbListeners.containsKey(conversationId)) continue

                    val ref = database.reference.child("chats").child(conversationId).child("messages")
                        .orderByChild("timestamp").limitToLast(1)
                    val listener = object : ChildEventListener {
                        override fun onChildAdded(snap: DataSnapshot, prev: String?) {
                            val senderId = snap.child("senderId").getValue(String::class.java) ?: return
                            if (senderId == myUserId) return
                            val msgId = snap.child("messageId").getValue(String::class.java) ?: snap.key ?: return
                            val msgType = snap.child("type").getValue(String::class.java) ?: "TEXT"
                            val appointmentStatus = snap.child("appointmentStatus").getValue(String::class.java)
                            val appointmentTitle = snap.child("appointmentTitle").getValue(String::class.java)
                            val msgTimestamp = snap.child("timestamp").getValue(Long::class.java) ?: 0L
                            val isNewMessage = msgTimestamp >= listeningStartAt - 10_000L
                            scope.launch {
                                try {
                                    // 1. Verificar si la conversación existe.
                                    val existingConv = conversationDao.getConversationById(conversationId)
                                    if (existingConv == null) {
                                        // Crear conversación básica si no existe
                                        conversationDao.insertConversation(
                                            ConversationEntity(
                                                conversationId = conversationId,
                                                userId = senderId,
                                                userName = senderId, // Se actualizará luego por el sync de Firestore
                                                isSynced = false,
                                                lastMessageTimestamp = msgTimestamp,
                                                lastMessage = snap.child("text").getValue(String::class.java) ?: ""
                                            )
                                        )
                                    }

                                    // 2. Insertar el mensaje
                                    val existsInRoom = messageDao.getMessageById(msgId) != null
                                    if (!existsInRoom) {
                                        val rawContent = snap.child("content").getValue(String::class.java) ?: ""
                                        var localImagePath: String? = null
                                        var localAudioPath: String? = null

                                        if (msgType == "IMAGE" && rawContent.isNotEmpty() && !rawContent.startsWith("http")) {
                                            localImagePath = com.example.myapplication.prestador.utils.ImageUtils.saveBase64ToFile(context, rawContent, msgId, "IMG_", ".webp")
                                        } else if (msgType == "AUDIO" && rawContent.isNotEmpty() && !rawContent.startsWith("http")) {
                                            localAudioPath = com.example.myapplication.prestador.utils.ImageUtils.saveBase64ToFile(context, rawContent, msgId, "AUD_", ".3gp")
                                        }

                                        val msg = MessageEntity(
                                            messageId = msgId,
                                            conversationId = conversationId,
                                            text = snap.child("text").getValue(String::class.java) ?: "",
                                            timestamp = msgTimestamp,
                                            isFromCurrentUser = false,
                                            messageType = if (msgType == "VISIT") "APPOINTMENT_REQUEST" else msgType,
                                            appointmentTitle = appointmentTitle,
                                            appointmentDate = snap.child("appointmentDate").getValue(String::class.java),
                                            appointmentTime = snap.child("appointmentTime").getValue(String::class.java),
                                            appointmentStatus = appointmentStatus,
                                            appointmentType = snap.child("appointmentType").getValue(String::class.java),
                                            providerAddress = snap.child("providerAddress").getValue(String::class.java),
                                            imageLocalPath = localImagePath,
                                            audioLocalPath = localAudioPath,
                                            imageUrl = if (localImagePath != null) "[Imagen]" else null,
                                            audioUrl = if (localAudioPath != null) "[Audio]" else null
                                        )
                                        messageDao.insertMessage(msg)
                                        conversationDao.updateLastMessage(conversationId, msg.text ?: "", msgTimestamp, msgType)
                                        conversationDao.incrementUnreadCount(conversationId)
                                    }

                                    // 3. Notificar
                                    if (!isNewMessage) return@launch
                                    if (!notifiedMessageIds.add(msgId)) return@launch

                                    val senderName = conversationDao.getConversationById(conversationId)?.userName ?: senderId
                                    notificationHelper.showChatNotification(
                                        senderId = senderId,
                                        senderName = senderName,
                                        msgType = msgType,
                                        appointmentStatus = appointmentStatus,
                                        appointmentTitle = appointmentTitle
                                    )

                                    val (gTitulo, gMensaje, gTipo) = when (msgType) {
                                        "AUDIO" -> Triple(senderName, " Te envió un audio", TipoNotificacion.MENSAJE)
                                        "IMAGE" -> Triple(senderName, " Te envió una imagen", TipoNotificacion.MENSAJE)
                                        "LOCATION" -> Triple(senderName, " Compartió su ubicación", TipoNotificacion.MENSAJE)
                                        "BUDGET" -> Triple(" Presupuesto", "$senderName respondió un presupuesto", TipoNotificacion.PRESUPUESTO)
                                        "VISIT", "APPOINTMENT" -> {
                                            val appt = appointmentTitle?.let { "\"$it\"" } ?: "una cita"
                                            when (appointmentStatus) {
                                                "PENDING" -> Triple(" Solicitud de cita", "$senderName solicitó $appt", TipoNotificacion.CITA)
                                                "CONFIRMED" -> Triple(" Cita confirmada", "$senderName confirmó $appt", TipoNotificacion.CITA)
                                                "REJECTED" -> Triple(" Cita rechazada", "$senderName rechazó $appt", TipoNotificacion.CITA)
                                                else -> Triple("📅 Cita", "$senderName actualizó $appt", TipoNotificacion.CITA)
                                            }
                                        }
                                        else -> Triple(senderName, " Nuevo mensaje", TipoNotificacion.MENSAJE)
                                    }

                                    notificacionRepository.guardar(
                                        NotificacionItem(
                                            tipo = gTipo,
                                            titulo = gTitulo,
                                            mensaje = gMensaje,
                                            fechaMs = System.currentTimeMillis(),
                                            leida = false,
                                            accionRoute = "open_chat/$senderId"
                                        )
                                    )
                                } catch (e: Exception) {
                                    Log.e("ChatRepo", "Error procesando mensaje global: ${e.message}")
                                }
                            }
                        }
                        override fun onChildChanged(snap: DataSnapshot, prev: String?) {}
                        override fun onChildRemoved(snap: DataSnapshot) {}
                        override fun onChildMoved(snap: DataSnapshot, prev: String?) {}
                        override fun onCancelled(error: DatabaseError) {}
                    }
                    ref.addChildEventListener(listener)
                    globalRtdbListeners[conversationId] = listener
                }
            }
    }

    fun stopGlobalListening() {
        globalListener?.remove()
        globalListener = null
        globalRtdbListeners.forEach { (convId, listener) ->
            database.reference.child("chats").child(convId).child("messages").removeEventListener(listener)
        }
        globalRtdbListeners.clear()
    }

    suspend fun deleteConversations(userIds: Set<String>) {
        for (userId in userIds) {
            val conversation = conversationDao.getConversationByUserId(userId) ?: continue
            val convId = conversation.conversationId
            //1.Borrar en Room
            conversationDao.deleteConversationById(convId)
            //2. Parar listener en RTDB
            val rtdbRef = database.reference.child("chats").child(convId).child("Mensajes")
            globalRtdbListeners[convId]?. let { rtdbRef.removeEventListener(it) }
            globalRtdbListeners.remove(convId)
            //3. Borrar de Firestore
            try {
                firestore.collection("chats").document(convId).delete().await()
                Log.d("ChatRepo", "Conversación $convId eliminada de Firestore")
            } catch (e: Exception) {
                Log.e("ChatRepo", "Error borrando de Firestore: ${e.message}")
            }
        }
    }

    suspend fun saveBookedAppointmet(
        messageId: String,
        clientId: String,
        clientName: String,
        date: String,
        time: String,
        service: String = "",
        chatId: String
    ) {
        // Si hay un turno RESCHEDULED del mismo chat, actualizarlo con la nueva fecha
        val rescheduled = bookedAppointmentDao.getRescheduledByChatId(chatId)
        if (rescheduled != null) {
            bookedAppointmentDao.updateDateTimeStatus(
                id = rescheduled.id,
                date = date,
                time = time,
                status = "CONFIRMED"
            )
        } else {
            bookedAppointmentDao.insertAppointment(
                BookedAppointmentEntity(
                    id = "appt_$messageId",
                    clientId = clientId,
                    clientName = clientName,
                    date = date,
                    time = time,
                    service = service,
                    status = "CONFIRMED",
                    chatId = chatId,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    // ── Enviar comprobante de turno confirmado ──────────────────────────────────
    suspend fun sendAppointmentReceiptMessage(
        conversationId: String,
        myUserId: String,
        date: String,
        time: String,
        service: String,
        providerName: String,
        isTechnician: Boolean,
        profession: String?,
        address: String?,
        code: String,
        prioritizeCompany: Boolean = false,
        companyId: String? = null,
        categoryId: String? = null,
        appointmentType: String = "TECHNICAL_VISIT"
    ): MessageEntity {
        val receiverId = conversationDao.getConversationById(conversationId)?.userId ?: ""
        val message = MessageEntity(
            messageId = UUID.randomUUID().toString(),
            conversationId = conversationId,
            text = "Turno confirmado",
            timestamp = System.currentTimeMillis(),
            isFromCurrentUser = true,
            messageType = "APPOINTMENT_RECEIPT",
            appointmentDate = date,
            appointmentTime = time,
            receiptService = service,
            receiptProviderName = providerName,
            receiptIsTechnician = isTechnician,
            receiptProfession = profession,
            receiptAddress = address,
            receiptCode = code,
            receiptPrioritizeCompany = prioritizeCompany,
            appointmentType = appointmentType,
            categoryId = categoryId
        )
        messageDao.insertMessage(message)
        conversationDao.updateLastMessage(conversationId, "Turno confirmado", message.timestamp, "APPOINTMENT_RECEIPT")
        updateConversationMetadata(conversationId, myUserId, receiverId, "Turno confirmado", message.timestamp)
        try {
            val receiptData = hashMapOf(
                "messageId" to message.messageId,
                "chatId" to conversationId,
                "senderId" to myUserId,
                "receiverId" to receiverId,
                "text" to "Turno confirmado",
                "type" to "APPOINTMENT_RECEIPT",
                "timestamp" to message.timestamp,
                "appointmentDate" to date,
                "appointmentTime" to time,
                "receiptService" to service,
                "receiptProviderName" to providerName,
                "receiptIsTechnician" to isTechnician,
                "receiptProfession" to (profession ?: ""),
                "receiptAddress" to (address ?: ""),
                "receiptCode" to code,
                "receiptPrioritizeCompany" to prioritizeCompany,
                "appointmentType" to appointmentType,
                "isRead" to false,
                "isDelivered" to false,
                "companyId" to companyId,
                "categoryId" to categoryId
            )
            database.reference.child("chats").child(conversationId)
                .child("messages").child(message.messageId)
                .setValue(receiptData).await()
            messageDao.markAsSynced(message.messageId)
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error sending receipt: ${e.message}")
        }
        return message
    }

    suspend fun sendRescheduleNoticeMessage(
        conversationId: String,
        myUserId: String,
        originalDate: String,
        originalTime: String
    ): MessageEntity {
        val receiverId = conversationDao.getConversationById(conversationId)?.userId ?: ""
        val message = MessageEntity(
            messageId = UUID.randomUUID().toString(),
            conversationId = conversationId,
            text = "Tu turno será reprogramado",
            timestamp = System.currentTimeMillis(),
            isFromCurrentUser = true,
            messageType = "RESCHEDULE_NOTICE",
            appointmentDate = originalDate,
            appointmentTime = originalTime
        )
        messageDao.insertMessage(message)
        conversationDao.updateLastMessage(conversationId, "Tu turno será reprogramado", message.timestamp, "RESCHEDULE_NOTICE")
        updateConversationMetadata(conversationId, myUserId, receiverId, "Tu turno será reprogramado", message.timestamp)
        try {
            val data = hashMapOf(
                "messageId" to message.messageId,
                "chatId" to conversationId,
                "senderId" to myUserId,
                "receiverId" to receiverId,
                "text" to "Tu turno será reprogramado",
                "type" to "RESCHEDULE_NOTICE",
                "timestamp" to message.timestamp,
                "appointmentDate" to originalDate,
                "appointmentTime" to originalTime,
                "isRead" to false,
                "isDelivered" to false
            )
            database.reference.child("chats").child(conversationId)
                .child("messages").child(message.messageId)
                .setValue(data).await()
            messageDao.markAsSynced(message.messageId)
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error sending reschedule notice: ${e.message}")
        }
        return message
    }

    suspend fun sendCancellationNoticeMessage(
        conversationId: String,
        myUserId: String,
        originalDate: String,
        originalTime: String,
        reason: String
    ): MessageEntity {
        val receiverId = conversationDao.getConversationById(conversationId)?.userId ?: ""
        val message = MessageEntity(
            messageId = UUID.randomUUID().toString(),
            conversationId = conversationId,
            text = "Turno cancelado",
            timestamp = System.currentTimeMillis(),
            isFromCurrentUser = true,
            messageType = "CANCELLATION_NOTICE",
            appointmentDate = originalDate,
            appointmentTime = originalTime,
            rejectionReason = reason

        )

        messageDao.insertMessage(message)
        conversationDao.updateLastMessage(conversationId, "Turno cancelado", message.timestamp, "CANCELLATION:NOTICE")
        updateConversationMetadata(conversationId, myUserId, receiverId, "Turno cancelado", message.timestamp)
        try {
            val data = hashMapOf(
                "messageId" to message.messageId,
                "chatId" to conversationId,
                "senderId" to myUserId,
                "receiverId" to receiverId,
                "text" to "Turno cancelado",
                "type" to "CANCELLATION_NOTICE",
                "timestamp" to message.timestamp,
                "appointmentDate" to originalDate,
                "appointmentTime" to originalTime,
                "rejectionReason" to reason,
                "isRead" to false,
                "isDelivered" to false
            )
            database.reference.child("chats").child(conversationId)
                .child("messages").child(message.messageId)
                .setValue(data).await()
            messageDao.markAsSynced(message.messageId)
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error sending cancellation notice: ${e.message}")
        }
        return message
    }

    suspend fun sendCompletionNoticeMessage(
        conversationId: String,
        myUserId: String,
        originalDate: String,
        originalTime: String
    ): MessageEntity {
        val receiverId = conversationDao.getConversationById(conversationId)?.userId ?: ""
        val message = MessageEntity(
            messageId = UUID.randomUUID().toString(),
            conversationId = conversationId,
            text = "Turno completado",
            timestamp = System.currentTimeMillis(),
            isFromCurrentUser = true,
            messageType = "COMPLETION_NOTICE",
            appointmentDate = originalDate,
            appointmentTime = originalTime
        )
        messageDao.insertMessage(message)
        conversationDao.updateLastMessage(conversationId, "Turno completado", message.timestamp, "COMPLETION_NOTICE")
        updateConversationMetadata(conversationId, myUserId, receiverId, "Turno completado", message.timestamp)
        try {
            val data = hashMapOf(
                "messageId" to message.messageId,
                "chatId" to conversationId,
                "senderId" to myUserId,
                "receiverId" to receiverId,
                "text" to "Turno completado",
                "type" to "COMPLETION_NOTICE",
                "timestamp" to message.timestamp,
                "appointmentDate" to originalDate,
                "appointmentTime" to originalTime,
                "isRead" to false,
                "isDelivered" to false
            )
            database.reference.child("chats").child(conversationId)
                .child("messages").child(message.messageId)
                .setValue(data).await()
            messageDao.markAsSynced(message.messageId)
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error sending completion notice: ${e.message}")
        }
        return message
    }

    suspend fun addBookedSlot(chatId: String, messageId: String, date: String, time: String) {
        try {
            val msgRef = database.reference
                .child("chats").child(chatId).child("messages").child(messageId)

            //Leer valor actual
            val snapshot = msgRef.child("bookedSlotsJson").get().await()
            val currentJson = snapshot.getValue(String::class.java) ?: "[]"

            //Agregar nuevo slot
            val newSlot = org.json.JSONObject().apply {
                put("date", date)
                put("time", time)
            }

            val array = org.json.JSONArray(currentJson)
            array.put(newSlot)


            msgRef.child("bookedSlotsJson").setValue(array.toString()).await()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error al bloquear slot: ${e.message}")
        }
    }


}
