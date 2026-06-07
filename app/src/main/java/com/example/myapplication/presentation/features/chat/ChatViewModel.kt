package com.example.myapplication.presentation.features.chat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.domain.model.MessageType
import com.example.myapplication.core.domain.model.Provider
import com.example.myapplication.core.data.repository.ChatRepository
import com.example.myapplication.core.data.repository.BudgetRepository
import com.example.myapplication.core.data.repository.ProviderRepository
import com.example.myapplication.core.data.local.entity.*
import com.example.myapplication.core.data.remote.CalendarMapper
import com.example.myapplication.core.ChatIdHelper
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.example.myapplication.core.utils.ImageUtils
import com.example.myapplication.core.utils.AudioUtils
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// ==========================================
// MODELOS DE UI PARA EL CHAT
// ==========================================

data class ChatMessageUiModel(
    val message: MessageEntity,
    val budget: BudgetEntity? = null,
    val categoryEmoji: String? = null
)

// Los modelos DayAvailability y TimeSlot se consumen desde CalendarMapper
typealias DayAvailability = CalendarMapper.DayAvailability
typealias TimeSlot = CalendarMapper.TimeSlot

data class BookingUiState(
    val availableDays: List<DayAvailability> = emptyList(),
    val selectedDay: DayAvailability? = null,
    val slots: List<TimeSlot> = emptyList(),
    val selectedTime: String? = null,
    val selectedAddress: com.example.myapplication.core.domain.model.AddressUnico? = null
)

data class ChatUiState(
    val messages: List<ChatMessageUiModel> = emptyList(), // Legacy support if needed
    val pagingMessages: Flow<PagingData<ChatMessageUiModel>> = emptyFlow(),
    val isRecording: Boolean = false,
    val isProviderTyping: Boolean = false,
    val isProviderOnline: Boolean = false,
    val selectedBudget: BudgetEntity? = null,
    val isFetchingFullBudget: Boolean = false, // 🔥 [NUEVO] Para ONDEMAND
    val activeProvider: Provider? = null,
    val confirmedInviteIds: Set<String> = emptySet(),
    val allCategories: List<CategoryEntity> = emptyList(),
    val bookingUiState: BookingUiState = BookingUiState(),
    val replyingToMessage: MessageEntity? = null,
    val activeBranchId: String? = null   // 🔥 Contexto del prestador
)

sealed class ChatUiEvent {
    data class ShowError(val message: String) : ChatUiEvent()
    object MessageSent : ChatUiEvent()
}

// ==========================================
// VIEWMODEL: EL OBRERO DE CHAT
// ==========================================

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val budgetRepository: BudgetRepository,
    private val providerRepository: ProviderRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ChatUiEvent>()
    val events: SharedFlow<ChatUiEvent> = _events.asSharedFlow()

    private var mediaRecorder: android.media.MediaRecorder? = null
    private var audioFilePath: String? = null
    private val _recordingTime = MutableStateFlow(0)
    val recordingTime: StateFlow<Int> = _recordingTime.asStateFlow()
    private var recordingTimerJob: kotlinx.coroutines.Job? = null

    private var currentChatId: String = ""
    private val currentUserId = auth.currentUser?.uid ?: ""
    private var activeBranchId: String? = null
    private var activeCategoryId: String? = null
    private var clientBranchId: String? = null 
    private var clientCompanyId: String? = null // 🔥 [NUEVO v7.9] Contexto corporativo local

    // 🔥 [NUEVO] Contexto de navegación multi-empresa
    private val _currentContextId = MutableStateFlow<String?>(null) // companyId o branchId
    val currentContextId = _currentContextId.asStateFlow()

    private var onlineStatusListener: ValueEventListener? = null
    private var typingTimeoutJob: kotlinx.coroutines.Job? = null

    fun initialize(
        chatId: String, 
        companyId: String? = null, // Deprecated v6
        branchId: String? = null,
        clientCompanyId: String? = null, 
        clientBranchId: String? = null, 
        categoryId: String? = null,
        initialProvider: Provider? = null,
        categories: List<CategoryEntity> = emptyList()
    ) {
        if (currentChatId == chatId && this.clientBranchId == clientBranchId) return

        currentChatId = chatId
        activeBranchId = branchId
        activeCategoryId = categoryId
        this.clientBranchId = clientBranchId
        this.clientCompanyId = clientCompanyId 

        Log.d("ChatViewModel", "🚀 Inicializando Chat: $chatId | MyBranch: $clientBranchId | RemoteBranch: $branchId")

        _uiState.update { it.copy(
            allCategories = categories,
            activeBranchId = branchId
        ) }

        initialProvider?.let { p ->
            val decoratedProvider = providerRepository.decorateProvider(p, null) // No usamos companyId para decoración
            _uiState.update { it.copy(activeProvider = decoratedProvider) }
        } ?: viewModelScope.launch {
            // Si no viene el provider, intentamos recuperarlo del repositorio
            val otherId = ChatIdHelper.extractOtherParticipantId(chatId, currentUserId)
            val p = providerRepository.getProviderById(otherId)
            p?.let {
                val decoratedProvider = providerRepository.decorateProvider(it, null)
                _uiState.update { it.copy(activeProvider = decoratedProvider) }
            } ?: run {
                // Si no está en Room, forzamos un fetch remoto
                providerRepository.fetchAndSyncProviderDetail(otherId)
                val remoteP = providerRepository.getProviderById(otherId)
                remoteP?.let {
                    val decoratedProvider = providerRepository.decorateProvider(it, null)
                    _uiState.update { it.copy(activeProvider = decoratedProvider) }
                }
            }
        }

        val otherId = ChatIdHelper.extractOtherParticipantId(chatId, currentUserId)
        setupOnlineStatusListener(otherId)

        chatRepository.startListening(chatId)

        // 🔥 [ELITE v4] REFACTOR A PAGING 3
        val pagingFlow = chatRepository.getMessagesPaging(chatId)
            .map { pagingData ->
                pagingData.map { msg ->
                    // 🔥 [FIX v8.0] Verificación de integridad del mensaje cargado
                    if (msg.content.isBlank() && msg.type == MessageType.TEXT) {
                        Log.w("ChatViewModel", "⚠️ Mensaje vacío detectado en UI: ${msg.id} | ChatId: ${msg.chatId}")
                    }

                    val bId = if (msg.type == MessageType.BUDGET) msg.relatedId ?: msg.id else null
                    val budget = if (msg.type == MessageType.BUDGET && !msg.budgetDataJson.isNullOrBlank()) {
                        com.example.myapplication.core.data.remote.ChatMessageMapper.parseBudgetSummaryFromJson(
                            msg.budgetDataJson!!,
                            bId ?: msg.id
                        )
                    } else null
                    
                    val categoryMap = _uiState.value.allCategories.associateBy { it.name }
                    val catName = budget?.category ?: msg.categoryId
                    val emoji = categoryMap[catName]?.icon
                    
                    ChatMessageUiModel(msg, budget, emoji)
                }
            }
            .cachedIn(viewModelScope)

        _uiState.update { it.copy(pagingMessages = pagingFlow) }

        // [LEGACY SYNC] Mantenemos confirmedInviteIds escuchando el flujo normal si es necesario, 
        // o lo extraemos de los metadatos del chat. Por ahora, para Paging, 
        // el estado de 'confirmed' se puede derivar en la UI o mediante un Flow separado.
        
        val providerId = ChatIdHelper.extractOtherParticipantId(chatId, currentUserId)
        viewModelScope.launch {
            chatRepository.observeTypingStatus(chatId, providerId).collect { isTyping ->
                _uiState.update { it.copy(isProviderTyping = isTyping) }
                
                // 🔥 [ELITE v8.6] Timeout de seguridad para estado "Escribiendo"
                // Evita que el prestador quede perpetuamente en typing si se desconecta.
                if (isTyping) {
                    typingTimeoutJob?.cancel()
                    typingTimeoutJob = viewModelScope.launch {
                        delay(8000) // 8 segundos de gracia
                        _uiState.update { it.copy(isProviderTyping = false) }
                    }
                } else {
                    typingTimeoutJob?.cancel()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatRepository.stopListening()
        typingTimeoutJob?.cancel()
        
        onlineStatusListener?.let {
            val providerId = ChatIdHelper.extractOtherParticipantId(currentChatId, currentUserId)
            FirebaseDatabase.getInstance().reference
                .child("users").child(providerId).child("online")
                .removeEventListener(it)
        }

        if (currentChatId.isNotEmpty()) {
            setTypingStatus(false)
        }
    }

    private fun setupOnlineStatusListener(providerId: String) {
        if (providerId.isEmpty()) return
        
        val ref = FirebaseDatabase.getInstance().reference
            .child("users").child(providerId).child("online")
        
        onlineStatusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOnline = snapshot.getValue(Boolean::class.java) ?: false
                _uiState.update { it.copy(isProviderOnline = isOnline) }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(onlineStatusListener!!)
    }

    fun sendText(text: String) {
        if (text.isBlank()) return
        val replyToMessage = uiState.value.replyingToMessage
        
        viewModelScope.launch {
            val message = createMessage(MessageType.TEXT, text, replyToMessage)
            chatRepository.sendMessage(message)
            _events.emit(ChatUiEvent.MessageSent)
            if (replyToMessage != null) setReplyMessage(null)
        }
    }

    fun sendImage(uri: Uri, context: Context) {
        val replyToMessage = uiState.value.replyingToMessage
        viewModelScope.launch {
            // 🔥 [ELITE] Compresión y Thumbnail en hilo secundario para no bloquear la UI
            val result = withContext(Dispatchers.Default) {
                val compressed = ImageUtils.compressElite(context, uri)
                val thumb = ImageUtils.generateThumbnailBase64(context, uri)
                compressed to thumb
            }

            val compressedBytes = result.first
            val thumbnail = result.second

            if (compressedBytes != null) {
                // 1. Guardar archivo físico local para el emisor (Blindaje de Room)
                val fileName = "IMG_SENT_${System.currentTimeMillis()}"
                val localPath = ImageUtils.saveBytesToFile(context, compressedBytes, fileName)

                // 2. Preparar el Base64 (Solo para el viaje a Firebase)
                val base64 = ImageUtils.bytesToBase64(compressedBytes)

                // 3. Construir mensaje
                // 🔥 [ELITE] Room Shielding: Guardamos "[Imagen]" en content para Room,
                // pero enviamos el Base64 a Firebase.
                val message = createMessage(MessageType.IMAGE, "[Imagen]", replyToMessage).copy(
                    imageLocalPath = localPath,
                    thumbnailBase64 = thumbnail
                )

                // 4. Enviar (ChatRepository.sendMessage guardará en Room)
                chatRepository.sendMessage(message, base64)

                _events.emit(ChatUiEvent.MessageSent)
            } else {
                _events.emit(ChatUiEvent.ShowError("No se pudo procesar la imagen"))
            }

            if (replyToMessage != null) setReplyMessage(null)
        }
    }

    fun sendAudio(path: String, duration: Int) {
        val replyToMessage = uiState.value.replyingToMessage
        viewModelScope.launch {
            val audioFile = java.io.File(path)
            if (audioFile.exists()) {
                // [SEGURIDAD ELITE]: Leer en fragmentos IO. 
                // AAC @ 32kbps x 60s ≈ 240KB. Es seguro leerlo completo para audios de 1 min.
                val bytes = withContext(Dispatchers.IO) { audioFile.readBytes() }
                val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                
                // 🔥 [ELITE] Room Shielding: Guardamos "[Audio]" en content para Room
                val message = createMessage(MessageType.AUDIO, "[Audio]", replyToMessage).copy(
                    audioLocalPath = path,
                    durationSeconds = duration
                )
                chatRepository.sendMessage(message, base64)
                _events.emit(ChatUiEvent.MessageSent)
            }
            if (replyToMessage != null) setReplyMessage(null)
        }
    }

    fun sendLocation(lat: Double, lng: Double, address: String) {
        val replyToMessage = uiState.value.replyingToMessage
        viewModelScope.launch {
            val message = createMessage(MessageType.LOCATION, address, replyToMessage).copy(
                latitude = lat,
                longitude = lng,
                locationAddress = address
            )
            chatRepository.sendMessage(message)
            _events.emit(ChatUiEvent.MessageSent)
            if (replyToMessage != null) setReplyMessage(null)
        }
    }

    fun sendBudgetRequest(problem: String, address: String, lat: Double, lng: Double) {
        val replyToMessage = uiState.value.replyingToMessage
        viewModelScope.launch {
            val message = createMessage(MessageType.BUDGET_REQUEST, problem, replyToMessage).copy(
                latitude = lat,
                longitude = lng,
                locationAddress = address,
                budgetRequestDescription = problem,
                budgetRequestClientAddress = address
            )
            chatRepository.sendMessage(message)
            _events.emit(ChatUiEvent.MessageSent)
            if (replyToMessage != null) setReplyMessage(null)
        }
    }

    fun sendAppointment(date: String, time: String, notes: String, type: String? = null, address: String? = null) {
        val replyToMessage = uiState.value.replyingToMessage
        viewModelScope.launch {
            val message = createMessage(MessageType.VISIT, notes, replyToMessage).copy(
                appointmentDate = date,
                appointmentTime = time,
                appointmentStatus = "PENDING",
                appointmentType = type ?: "TECHNICAL_VISIT",
                providerAddress = address
            )
            chatRepository.sendMessage(message)
            _events.emit(ChatUiEvent.MessageSent)
            if (replyToMessage != null) setReplyMessage(null)
        }
    }

    fun startRecording(context: Context) {
        viewModelScope.launch {
            try {
                val audioFile = java.io.File(context.cacheDir, "record_${System.currentTimeMillis()}${AudioUtils.getRecommendedExtension()}")
                audioFilePath = audioFile.absolutePath

                mediaRecorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    android.media.MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    android.media.MediaRecorder()
                }.apply {
                    AudioUtils.configureEliteRecorder(this)
                    setOutputFile(audioFilePath)
                    prepare()
                    start()
                }
                _uiState.update { it.copy(isRecording = true) }
                startRecordingTimer()
            } catch (e: Exception) {
                e.printStackTrace()
                _events.emit(ChatUiEvent.ShowError("No se pudo iniciar la grabación: ${e.message}"))
                _uiState.update { it.copy(isRecording = false) }
            }
        }
    }

    private fun startRecordingTimer() {
        recordingTimerJob?.cancel()
        _recordingTime.value = 0
        recordingTimerJob = viewModelScope.launch {
            while (_uiState.value.isRecording) {
                delay(1000)
                _recordingTime.value += 1
                if (_recordingTime.value >= 60) { // 🔥 [ELITE] Límite de 1 minuto
                    stopRecordingAndSend()
                    break
                }
            }
        }
    }

    fun stopRecordingAndSend() {
        if (!_uiState.value.isRecording) return
        val path = audioFilePath
        val duration = _recordingTime.value

        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) { 
            e.printStackTrace() 
        }
        
        mediaRecorder = null
        _uiState.update { it.copy(isRecording = false) }
        recordingTimerJob?.cancel()
        _recordingTime.value = 0
        audioFilePath = null

        if (path != null && duration > 0) {
            sendAudio(path, duration)
        }
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) { }
        mediaRecorder = null
        _uiState.update { it.copy(isRecording = false) }
        recordingTimerJob?.cancel()
        _recordingTime.value = 0
        audioFilePath?.let { java.io.File(it).delete() }
        audioFilePath = null
    }

    fun onBudgetClicked(budgetId: String) {
        viewModelScope.launch {
            // 🔥 [ELITE ONDEMAND] Carga profunda del presupuesto solo al hacer click
            _uiState.update { it.copy(isFetchingFullBudget = true) }
            val budget = budgetRepository.getBudgetById(budgetId)
            _uiState.update { it.copy(selectedBudget = budget, isFetchingFullBudget = false) }
        }
    }

    fun clearSelectedBudget() {
        _uiState.update { it.copy(selectedBudget = null) }
    }

    fun setTypingStatus(isTyping: Boolean) {
        chatRepository.setTypingStatus(currentChatId, currentUserId, isTyping)
    }

    fun markAsRead() {
        viewModelScope.launch {
            chatRepository.markMessagesAsRead(currentChatId, currentUserId)
        }
    }

    fun setReplyMessage(message: MessageEntity?) {
        _uiState.update { it.copy(replyingToMessage = message) }
    }

    /**
     * [ELITE v4] Cambia el contexto del chat (Personal -> Sucursal).
     * Esto regenera el ChatID y reinicia la escucha.
     */
    fun switchChatContext(branchId: String?) {
        val provider = uiState.value.activeProvider ?: return
        val newChatId = ChatIdHelper.generateChatId(
            uid1 = currentUserId, 
            uid2 = provider.uid, 
            b1 = clientBranchId, 
            b2 = branchId
        )
        
        if (newChatId == currentChatId) return

        // Limpiar recursos anteriores
        chatRepository.stopListening()
        
        // Inicializar nuevo contexto
        initialize(
            chatId = newChatId,
            branchId = branchId,
            categoryId = activeCategoryId,
            initialProvider = provider,
            categories = uiState.value.allCategories
        )
    }

    fun sendTenderInvitation(tender: TenderEntity) {
        val replyToMessage = uiState.value.replyingToMessage
        viewModelScope.launch {
            val message = createMessage(MessageType.TEXT, "Te invito a participar en mi licitación: ${tender.title}", replyToMessage).copy(
                relatedId = tender.tenderId
            )
            chatRepository.sendMessage(message)
            if (replyToMessage != null) setReplyMessage(null)
        }
    }

    fun getMatchingTenders(category: String): Flow<List<TenderEntity>> {
        return budgetRepository.allTenders.map { tenders ->
            tenders.filter { (it.category == category) && (it.status == "OPEN") }
        }
    }

    fun openBookingDialog(message: MessageEntity, availableAddresses: List<com.example.myapplication.core.domain.model.AddressUnico>) {
        val days = CalendarMapper.parseAvailabilityJson(message.availabilityJson ?: "[]")
        _uiState.update { it.copy(
            bookingUiState = BookingUiState(
                availableDays = days,
                selectedDay = days.firstOrNull(),
                selectedAddress = availableAddresses.firstOrNull()
            )
        ) }
        updateSlotsForSelectedDay(message.bookedSlotsJson ?: "[]")
    }

    fun onDaySelected(day: DayAvailability, bookedSlotsJson: String) {
        _uiState.update { it.copy(
            bookingUiState = it.bookingUiState.copy(
                selectedDay = day,
                selectedTime = null
            )
        ) }
        updateSlotsForSelectedDay(bookedSlotsJson)
    }

    fun onTimeSelected(time: String) {
        _uiState.update { it.copy(
            bookingUiState = it.bookingUiState.copy(selectedTime = time)
        ) }
    }

    fun onAddressSelected(address: com.example.myapplication.core.domain.model.AddressUnico) {
        _uiState.update { it.copy(
            bookingUiState = it.bookingUiState.copy(selectedAddress = address)
        ) }
    }

    private fun updateSlotsForSelectedDay(bookedSlotsJson: String) {
        val state = _uiState.value.bookingUiState
        val day = state.selectedDay ?: return
        val booked = CalendarMapper.parseBookedSlotsJson(bookedSlotsJson)
        val slots = CalendarMapper.generateSlotsFromAvailability(day, booked)
        _uiState.update { it.copy(
            bookingUiState = it.bookingUiState.copy(slots = slots)
        ) }
    }

    private fun createMessage(type: MessageType, content: String, replyTo: MessageEntity? = null): MessageEntity {
        // 🔥 [ELITE v8.0] SYMMETRIC ROOM TAGGING (SSOT)
        // Ya no confiamos solo en la extracción del ID del chat. 
        // Usamos la identidad inyectada en el initialize() para marcar el mensaje.
        
        return MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = currentChatId,
            senderId = currentUserId,
            receiverId = ChatIdHelper.extractOtherParticipantId(currentChatId, currentUserId),
            type = type,
            content = content,
            
            // --- BLOQUE DE IDENTIDAD ELITE v8.0 ---
            senderBranchId = clientBranchId, 
            senderCompanyId = clientCompanyId,
            receiverBranchId = activeBranchId, 
            receiverCompanyId = null,
            
            // [SOBERANÍA LOCAL]: Tags de filtrado para Room
            localBranchId = clientBranchId,
            localCompanyId = clientCompanyId,
            remoteBranchId = activeBranchId,
            remoteCompanyId = null,

            // Compatibilidad legacy (Sincronización Firebase)
            branchId = clientBranchId,
            companyId = clientCompanyId,
            categoryId = activeCategoryId,

            appointmentType = if (type == MessageType.VISIT) "TECHNICAL_VISIT" else null,
            replyToId = replyTo?.id,
            replyToContent = replyTo?.let { getReplyPreviewText(it) },
            replyToSenderName = replyTo?.let { 
                if (it.senderId == currentUserId) "Tú" 
                else _uiState.value.activeProvider?.displayName ?: "Prestador" 
            },
            timestamp = System.currentTimeMillis()
        )
    }

    private fun getReplyPreviewText(message: MessageEntity): String {
        return when (message.type) {
            MessageType.IMAGE -> "[Imagen]"
            MessageType.AUDIO -> "[Audio]"
            MessageType.LOCATION -> "[Ubicación]"
            MessageType.BUDGET -> "[Presupuesto]"
            MessageType.VISIT -> "[Turno]"
            MessageType.BUDGET_REQUEST -> "[Solicitud de presupuesto]"
            else -> message.content
        }
    }
}

