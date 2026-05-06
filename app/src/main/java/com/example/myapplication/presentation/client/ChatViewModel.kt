package com.example.myapplication.presentation.client

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.MessageEntity
import com.example.myapplication.data.local.TenderEntity
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.data.repository.BudgetRepository
import com.example.myapplication.util.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatMessageUiModel(
    val message: MessageEntity,
    val budget: BudgetEntity? = null
)

data class ChatUiState(
    val messages: List<ChatMessageUiModel> = emptyList(),
    val isRecording: Boolean = false,
    val isProviderTyping: Boolean = false,
    val selectedBudget: BudgetEntity? = null,
    val providerPhotoUrl: String? = null,
    val confirmedInviteIds: Set<String> = emptySet() // IDs de invitaciones ya confirmadas
)

sealed class ChatUiEvent {
    data class ShowError(val message: String) : ChatUiEvent()
    object MessageSent : ChatUiEvent()
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val budgetRepository: BudgetRepository,
    auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ChatUiEvent>()
    val events: SharedFlow<ChatUiEvent> = _events.asSharedFlow()

    // --- SECCIÓN: AUDIO RECORDING (SSOT) ---
    private var mediaRecorder: android.media.MediaRecorder? = null
    private var audioFilePath: String? = null
    private val _recordingTime = MutableStateFlow(0)
    val recordingTime: StateFlow<Int> = _recordingTime.asStateFlow()
    private var recordingTimerJob: kotlinx.coroutines.Job? = null

    private var currentChatId: String = ""
    private val currentUserId = auth.currentUser?.uid ?: ""
    private var activeCompanyId: String? = null
    private var activeCategoryId: String? = null

    /**
     * Inicialización manual para asegurar que tenemos el chatId unificado.
     */
    fun initialize(chatId: String, companyId: String? = null, categoryId: String? = null) {
        if (currentChatId == chatId) return
        currentChatId = chatId
        activeCompanyId = companyId
        activeCategoryId = categoryId

        // 1. Activar escucha activa desde el servidor (Firebase RTDB -> Room)
        chatRepository.startListening(chatId)

        // 2. Escuchar mensajes desde Room (SSOT)
        viewModelScope.launch {
            chatRepository.getMessages(chatId).collect { messages ->
                // Identificar invitaciones que ya tienen un comprobante asociado
                val confirmedIds = messages
                    .filter { it.type == MessageType.APPOINTMENT_RECEIPT }
                    .mapNotNull { it.calendarInviteMessageId }
                    .toSet()

                val uiMessages = messages.map { msg ->
                    val budgetId = if (msg.type == MessageType.BUDGET) {
                        msg.relatedId ?: msg.id
                    } else null
                    val budget = budgetId?.let { chatRepository.getBudgetById(it) }
                    ChatMessageUiModel(msg, budget)
                }
                _uiState.update { it.copy(
                    messages = uiMessages,
                    confirmedInviteIds = confirmedIds
                ) }
            }
        }

        // 3. Observar estado de "escribiendo" del proveedor
        val providerId = extractOtherParticipantId(chatId)
        viewModelScope.launch {
            chatRepository.observeProviderTyping(chatId, providerId).collect { isTyping ->
                _uiState.update { it.copy(isProviderTyping = isTyping) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // [PASO CRÍTICO] Detener escucha activa para ahorrar batería y datos
        chatRepository.stopListening()
        // Limpiar estado de escribiendo al salir
        if (currentChatId.isNotEmpty()) {
            setTypingStatus(false)
        }
    }

    private fun extractOtherParticipantId(chatId: String): String {
        return chatId.split("_").firstOrNull { it != currentUserId } ?: ""
    }

    fun sendText(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val message = createMessage(MessageType.TEXT, text)
            chatRepository.sendMessage(message)
            _events.emit(ChatUiEvent.MessageSent)
        }
    }

    /**
     * [POLÍTICA ZERO COST] Comprime a WebP y guarda localmente ANTES de enviar.
     * Esto asegura que el emisor vea la imagen al instante sin depender de la nube.
     */
    fun sendImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            val bytes = ImageUtils.compressImageToWebP(context, uri)
            if (bytes != null) {
                val msgId = UUID.randomUUID().toString()
                // Guardamos copia local para visibilidad inmediata en la UI (SSOT local)
                val localPath = ImageUtils.saveBytesToFile(context, bytes, msgId)
                
                val base64 = ImageUtils.bytesToBase64(bytes)
                val message = createMessage(MessageType.IMAGE, "[Imagen]").copy(
                    id = msgId,
                    imageUrl = localPath // Ruta local para que la UI la pinte
                )
                chatRepository.sendImageMessageWithBase64(message, base64)
                _events.emit(ChatUiEvent.MessageSent)
            }
        }
    }

    fun sendLocation(lat: Double, lng: Double, address: String) {
        viewModelScope.launch {
            val message = createMessage(MessageType.LOCATION, address).copy(
                latitude = lat,
                longitude = lng,
                locationAddress = address
            )
            chatRepository.sendMessage(message)
            _events.emit(ChatUiEvent.MessageSent)
        }
    }

    /**
     * Envía una solicitud de presupuesto formal desde el cliente.
     */
    fun sendBudgetRequest(problem: String, address: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            val message = createMessage(MessageType.BUDGET_REQUEST, problem).copy(
                latitude = lat,
                longitude = lng,
                locationAddress = address
            )
            chatRepository.sendMessage(message)
            _events.emit(ChatUiEvent.MessageSent)
        }
    }

    fun sendAppointment(date: String, time: String, notes: String, type: String? = null, address: String? = null) {
        viewModelScope.launch {
            val message = createMessage(MessageType.VISIT, notes).copy(
                appointmentDate = date,
                appointmentTime = time,
                appointmentStatus = "PENDING",
                appointmentType = type,
                providerAddress = address
            )
            chatRepository.sendMessage(message)
            _events.emit(ChatUiEvent.MessageSent)
        }
    }

    fun startRecording(context: Context) {
        viewModelScope.launch {
            try {
                val audioFile = java.io.File(context.cacheDir, "record_${System.currentTimeMillis()}.3gp")
                audioFilePath = audioFile.absolutePath

                mediaRecorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    android.media.MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    android.media.MediaRecorder()
                }.apply {
                    setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
                    setOutputFormat(android.media.MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AMR_NB)
                    setAudioEncodingBitRate(12200) // [POLÍTICA ZERO COST] Bitrate ultra bajo
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
            // Si falla el stop, intentamos liberar de todos modos
            try { mediaRecorder?.release() } catch (ex: Exception) {}
        }
        
        mediaRecorder = null
        _uiState.update { it.copy(isRecording = false) }
        recordingTimerJob?.cancel()
        _recordingTime.value = 0
        audioFilePath = null

        // Solo enviamos si la duración es razonable (> 0 segundos)
        if (path != null && duration > 0) {
            viewModelScope.launch {
                val file = java.io.File(path)
                if (file.exists()) {
                    val bytes = file.readBytes()
                    val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    val message = createMessage(MessageType.AUDIO, "[Audio]").copy(
                        durationSeconds = duration,
                        imageUrl = path // Usamos imageUrl para guardar la ruta local temporal
                    )
                    chatRepository.sendAudioMessageWithBase64(message, base64)
                    _events.emit(ChatUiEvent.MessageSent)
                }
            }
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
            val budget = chatRepository.getBudgetById(budgetId)
            _uiState.update { it.copy(selectedBudget = budget) }
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
            chatRepository.markChatAsRead(currentChatId, currentUserId)
        }
    }

    fun sendTenderInvitation(tender: TenderEntity) {
        viewModelScope.launch {
            val message = createMessage(MessageType.TEXT, "Te invito a participar en mi licitación: ${tender.title}").copy(
                relatedId = tender.tenderId
            )
            chatRepository.sendMessage(message)
        }
    }

    fun getMatchingTenders(category: String): Flow<List<TenderEntity>> {
        return budgetRepository.allTenders.map { tenders ->
            tenders.filter { (it.category == category) && (it.status == "OPEN") }
        }
    }

    private fun createMessage(type: MessageType, content: String): MessageEntity {
        return MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = currentChatId,
            senderId = currentUserId,
            receiverId = extractOtherParticipantId(currentChatId),
            type = type,
            content = content,
            companyId = activeCompanyId,
            categoryId = activeCategoryId,
            // [REGLA DE ORO] TECHNICAL_VISIT como fallback para solicitudes de cliente
            appointmentType = if (type == MessageType.VISIT) "TECHNICAL_VISIT" else null,
            timestamp = System.currentTimeMillis()
        )
    }
}
