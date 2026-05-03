package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.ConversationEntity
import com.example.myapplication.prestador.data.local.entity.MessageEntity
import com.example.myapplication.prestador.data.local.entity.ProviderEntity
import com.example.myapplication.prestador.data.repository.ChatRepository
import com.example.myapplication.prestador.data.repository.ProviderRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val providerRepository: ProviderRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val myUserId =
        FirebaseAuth.getInstance().currentUser?.uid ?:
        ""
    private var currentConversationId = ""
    private var currentCompanyId: String? = null
    private var currentCategoryId: String? = null

    private val _activeCompanyId = MutableStateFlow<String?>(null)
    val activeCompanyId: StateFlow<String?> = _activeCompanyId.asStateFlow()

    fun setActiveCompanyId(companyId: String?) {
        _activeCompanyId.value = companyId
    }

    private val _providerProfile = MutableStateFlow<ProviderEntity?>(null)
    val providerProfile: StateFlow<ProviderEntity?> = _providerProfile.asStateFlow()

    fun loadProviderProfile(providerId: String) {
        viewModelScope.launch {
            providerRepository.getProviderById(providerId).collect {
                _providerProfile.value = it
            }
        }
    }

    private val _isLoading =
        MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val conversations: StateFlow<List<ConversationEntity>> = _activeCompanyId
        .flatMapLatest { companyId ->
            repository.getConversationsByCompany(companyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages:
            StateFlow<List<MessageEntity>> =
        _messages.asStateFlow()

    private val _errorMessage =
        MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> =
        _errorMessage.asStateFlow()

    private val _successMessage =
        MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> =
        _successMessage.asStateFlow()

    val totalUnreadCount: StateFlow<Int> = repository.getTotalUnreadCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _isClientTyping = MutableStateFlow(false)
    val isClientTyping: StateFlow<Boolean> = _isClientTyping.asStateFlow()
    private var typingObserveJob: kotlinx.coroutines.Job? = null

    fun observeClientTyping(chatId: String, clientId: String) {
        typingObserveJob?.cancel()
        typingObserveJob = viewModelScope.launch {
            repository.observeClientTyping(chatId, clientId).collect {
                _isClientTyping.value = it
            }
        }
    }

    fun loadMessagesByConversation(conversationId: String) {
        currentConversationId = conversationId

        repository.startListening(conversationId, myUserId)
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Cargar contexto de la conversación
                val conv = repository.getConversationById(conversationId)
                currentCompanyId = conv?.companyId
                currentCategoryId = conv?.categoryId
                
                repository.getMessagesByConversation(conversationId).collect { newList ->
                    _messages.value = newList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar mensajes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private var typingJob: kotlinx.coroutines.Job? = null

    fun setTypingStatus(isTyping: Boolean) {
        if (currentConversationId.isEmpty())
            return
        if (isTyping) {
            repository.setTypingStatus(currentConversationId, myUserId, true)
            typingJob?.cancel()
            typingJob = viewModelScope.launch {
                delay(3000)
                repository.setTypingStatus(currentConversationId, myUserId, false)
            }
        } else {
            typingJob?.cancel()
            repository.setTypingStatus(currentConversationId, myUserId, false)
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || currentConversationId.isEmpty()) return
        viewModelScope.launch {
            try {
                repository.sendMessage(
                    currentConversationId,
                    text, 
                    myUserId,
                    currentCompanyId,
                    currentCategoryId
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error al enviar mensaje: ${e.message}"
            }
        }
    }

    fun sendBudgetMessage(pres: com.example.myapplication.prestador.data.local.entity.PresupuestoEntity) {
        if (currentConversationId.isEmpty())
            return
        viewModelScope.launch {
            try {
                repository.sendBudgetMessage(
                    conversationId = currentConversationId,
                    myUserId = myUserId,
                    pres = pres,
                    companyId = currentCompanyId,
                    categoryId = currentCategoryId
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error al enviar presupuesto: ${e.message}"
            }
        }
    }


    fun sendImage(uri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch {
            try {
                val base64 = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val bytes = com.example.myapplication.prestador.utils.ImageUtils.compressImageToWebP(context, uri)
                    if (bytes != null) {
                        com.example.myapplication.prestador.utils.ImageUtils.bytesToBase64(bytes)
                    } else null
                } ?: return@launch
                repository.sendImageMessage(
                    currentConversationId, 
                    base64, 
                    myUserId,
                    currentCompanyId,
                    currentCategoryId
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun sendLocation(latitude: Double, longitude: Double) {
        if (currentConversationId.isEmpty())
            return
        viewModelScope.launch {
            try {
                repository.sendLocationMessage(
                    conversationId = currentConversationId,
                    latitude = latitude,
                    longitude = longitude,
                    senderId = myUserId,
                    companyId = currentCompanyId,
                    categoryId = currentCategoryId
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error al enviar ubicacion: ${e.message}"
            }
        }
    }

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    private var mediaRecorder: android.media.MediaRecorder? = null
    private var audioFilePath: String? = null
    private var recordingTimerJob: Job? = null
    private val _recordingTime = MutableStateFlow(0)
    val recordingTime: StateFlow<Int> = _recordingTime.asStateFlow()

    fun startRecording() {
        viewModelScope.launch {
            try {
                val audioFile = java.io.File(context.cacheDir, "audio_${System.currentTimeMillis()}.m4a")
                audioFilePath = audioFile.absolutePath

                mediaRecorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    android.media.MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    android.media.MediaRecorder()
                }.apply {
                    setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
                    setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC)
                    setAudioEncodingBitRate(32_000)
                    setAudioSamplingRate(16_000)
                    setOutputFile(audioFilePath)
                    prepare()
                    start()
                }
                _isRecording.value = true
                startRecordingTimer()
            } catch (e: Exception) {
                e.printStackTrace()
                _isRecording.value = false
            }
        }
    }

    private fun startRecordingTimer() {
        recordingTimerJob?.cancel()
        _recordingTime.value = 0
        recordingTimerJob = viewModelScope.launch {
            while (_isRecording.value) {
                delay(1000)
                _recordingTime.value += 1
                if (_recordingTime.value >= 60) {
                    stopRecordingAndSend()
                    break
                }
            }
        }
    }

    fun stopRecordingAndSend() {
        if (!_isRecording.value) return
        val pathToSend = audioFilePath
        val duration = _recordingTime.value

        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder = null
        _isRecording.value = false
        recordingTimerJob?.cancel()
        _recordingTime.value = 0
        audioFilePath = null

        pathToSend?.let { path ->
            viewModelScope.launch {
                try {
                    repository.sendAudioMessage(
                        currentConversationId, 
                        path, 
                        duration, 
                        myUserId,
                        currentCompanyId,
                        currentCategoryId
                    )
                } catch (e: Exception) {
                    _errorMessage.value = "Error al enviar audio: ${e.message}"
                }
            }
        }
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder = null
        _isRecording.value = false
        recordingTimerJob?.cancel()
        _recordingTime.value = 0
        audioFilePath?.let { java.io.File(it).delete() }
        audioFilePath = null
    }

    fun sendAudioMessage(audioPath: String, durationSeconds: Int) {
        if (currentConversationId.isEmpty()) return
        viewModelScope.launch {
            try {
                repository.sendAudioMessage(
                    currentConversationId, 
                    audioPath, 
                    durationSeconds, 
                    myUserId,
                    currentCompanyId,
                    currentCategoryId
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error al enviar audio: ${e.message}"
            }
        }
    }

    fun markMessagesAsRead(conversationId: String) {
        viewModelScope.launch {
            try {
                repository.markMessagesAsRead(conversationId)
            } catch (e: Exception) {
                _errorMessage.value = "Error al marcar mensajes: ${e.message}"
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            try {
                repository.deleteMessage(messageId)
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar mensaje: ${e.message}"
            }
        }
    }

    // ── Enviar calendario de disponibilidad
    fun sendCalendarInvite(
        startDate: String,        // "yyyy-MM-dd"
        endDate: String,          // "yyyy-MM-dd"
        availabilityJson: String, // JSON con las reglas de horario
        bookedSlotsJson: String,  // JSON con los slots ya ocupados
        appointmentType: String,  // "TECHNICAL_VISIT" o "LOCAL_APPOINTMENT"
        providerAddress: String?  // Dirección del local (opcional)
    ) {
        if (currentConversationId.isEmpty()) return
        viewModelScope.launch {
            try {
                repository.sendCalendarInviteMessage(
                    conversationId = currentConversationId,
                    myUserId = myUserId,
                    startDate = startDate,
                    endDate = endDate,
                    availabilityJson = availabilityJson,
                    bookedSlotsJson = bookedSlotsJson,
                    companyId = currentCompanyId,
                    categoryId = currentCategoryId,
                    appointmentType = appointmentType,
                    providerAddress = providerAddress
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error al enviar calendario:${e.message}"
            }
        }
    }

    // ── Responder a una solicitud de turno del cliente
    fun respondToAppointmentRequest(
        messageId: String,
        clientName: String,
        date: String,   // "yyyy-MM-dd"
        time: String,   // "HH:mm"
        service: String = "",
        providerName: String = "",
        serviceType: String = "PROFESSIONAL",
        doesHomeVisits: Boolean = false,
        profession: String? = null,
        providerAddress: String? = null,
        accepted: Boolean,
        rejectionReason: String? = null
    ) {
        if (currentConversationId.isEmpty()) return
        viewModelScope.launch {
            try {
                val newStatus = if (accepted) "ACCEPTED" else "REJECTED"
                // 1. Actualizar estado del mensaje en Room + RTDB
                repository.updateAppointmentRequestStatus(
                    messageId = messageId,
                    conversationId = currentConversationId,
                    newStatus = newStatus,
                    rejectionReason = rejectionReason
                )

                // 2. Si aceptó, enviar comprobante visual
                if (accepted) {
                    val receipt = buildAppointmentReceipt(
                        date = date,
                        time = time,
                        service = service,
                        providerName = providerName,
                        serviceType = serviceType,
                        doesHomeVisits = doesHomeVisits,
                        profession = profession,
                        providerAddress = providerAddress
                    )
                    repository.sendAppointmentReceiptMessage(
                        conversationId = currentConversationId,
                        myUserId = myUserId,
                        date = receipt.date,
                        time = receipt.time,
                        service = receipt.service,
                        providerName = receipt.providerName,
                        isTechnician = receipt.isTechnician,
                        profession = receipt.profession,
                        address = receipt.address,
                        code = receipt.code,
                        companyId = currentCompanyId,
                        categoryId = currentCategoryId
                    )
                } else {
                    // Si rechazó, enviar mensaje de rechazo
                    val motivo = if (!rejectionReason.isNullOrBlank()) "Motivo: $rejectionReason" else ""
                    val rejectText = "❌ No puedo atenderte el $date a las $time. $motivo Por favor elegí otro horario."
                    repository.sendMessage(
                        currentConversationId, 
                        rejectText, 
                        myUserId,
                        currentCompanyId,
                        currentCategoryId
                    )
                }

                // 3. Si aceptó, guardar en Room local del prestador
                if (accepted) {
                    val conversation = repository.getConversationById(currentConversationId)
                    val clientId = conversation?.userId ?: ""
                    repository.saveBookedAppointmet(
                        messageId = messageId,
                        clientId = clientId,
                        clientName = clientName,
                        date = date,
                        time = time,
                        service = service,
                        chatId = currentConversationId
                    )
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al responder solicitud:${e.message}"
            }
        }
    }

    private data class ReceiptData(
        val date: String,
        val time: String,
        val service: String,
        val providerName: String,
        val isTechnician: Boolean,
        val profession: String?,
        val address: String?,
        val code: String
    )

    private fun buildAppointmentReceipt(
        date: String,
        time: String,
        service: String,
        providerName: String,
        serviceType: String,
        doesHomeVisits: Boolean,
        profession: String?,
        providerAddress: String?
    ): ReceiptData {
        val isTechnician = serviceType == "TECHNICAL" || doesHomeVisits
        val dateFormatted = formatDateForDisplay(date)
        val timeFormatted = if (time.isNotBlank()) "$time hs" else time
        val code = if (isTechnician) {
            "#VIS-${date.replace("-", "")}-001"
        } else {
            "#TRN-${date.replace("-", "")}-001"
        }

        return ReceiptData(
            date = dateFormatted,
            time = timeFormatted,
            service = service,
            providerName = providerName,
            isTechnician = isTechnician,
            profession = if (!profession.isNullOrBlank()) profession else null,
            address = if (!providerAddress.isNullOrBlank()) providerAddress else null,
            code = code
        )
    }

    private fun formatDateForDisplay(dateStr: String): String {
        return try {
            val parts = dateStr.split("-")
            if (parts.size == 3) {
                val year = parts[0]
                val month = parts[1]
                val day = parts[2]
                val dayOfWeek = getDayOfWeek(dateStr)
                "$dayOfWeek $day/$month/$year"
            } else {
                dateStr
            }
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun getDayOfWeek(dateStr: String): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale("es", "ES"))
            val date = sdf.parse(dateStr) ?: return ""
            val cal = java.util.Calendar.getInstance()
            cal.time = date
            val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
            when (dayOfWeek) {
                1 -> "Dom"
                2 -> "Lun"
                3 -> "Mar"
                4 -> "Mié"
                5 -> "Jue"
                6 -> "Vie"
                7 -> "Sáb"
                else -> ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun syncConversations() {
        if (myUserId.isEmpty()) return
        repository.syncConversationsFromFirestore(myUserId)
        repository.startGlobalListening(myUserId)
    }

    override fun onCleared() {
        super.onCleared()
        try { mediaRecorder?.stop() } catch (_: Exception) {}
        try { mediaRecorder?.release() } catch (_: Exception) {}
        mediaRecorder = null
        recordingTimerJob?.cancel()
        repository.stopListening()
        repository.stopGlobalListening()
    }

    fun deleteConversations(userIds: Set<String>) {
        viewModelScope.launch {
            try {
                repository.deleteConversations(userIds)
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar conversaciones: ${e.message}"
            }
        }
    }
}
