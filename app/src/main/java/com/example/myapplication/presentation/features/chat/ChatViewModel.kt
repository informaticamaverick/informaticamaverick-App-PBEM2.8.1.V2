package com.example.myapplication.presentation.features.chat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.local.entity.BudgetEntity
import com.example.myapplication.core.data.local.entity.MessageEntity
import com.example.myapplication.core.data.local.entity.TenderEntity
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.domain.model.MessageType
import com.example.myapplication.core.domain.model.Provider
import com.example.myapplication.core.data.repository.ChatRepository
import com.example.myapplication.core.data.repository.BudgetRepository
import com.example.myapplication.core.data.repository.ProviderRepository
import com.example.myapplication.presentation.components.DayAvailability
import com.example.myapplication.presentation.components.TimeSlot
import com.example.myapplication.core.ChatIdHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

data class BookingUiState(
    val availableDays: List<DayAvailability> = emptyList(),
    val selectedDay: DayAvailability? = null,
    val slots: List<TimeSlot> = emptyList(),
    val selectedTime: String? = null,
    val selectedAddress: com.example.myapplication.presentation.components.AddressInfo? = null
)

data class ChatUiState(
    val messages: List<ChatMessageUiModel> = emptyList(),
    val isRecording: Boolean = false,
    val isProviderTyping: Boolean = false,
    val isProviderOnline: Boolean = false,
    val selectedBudget: BudgetEntity? = null,
    val providerPhotoUrl: String? = null,
    val activeProvider: Provider? = null,
    val confirmedInviteIds: Set<String> = emptySet(),
    val allCategories: List<CategoryEntity> = emptyList(),
    val bookingUiState: BookingUiState = BookingUiState(),
    val replyingToMessage: MessageEntity? = null
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
    auth: FirebaseAuth
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
    private var activeCompanyId: String? = null
    private var activeCategoryId: String? = null

    private var onlineStatusListener: ValueEventListener? = null

    fun initialize(
        chatId: String, 
        companyId: String? = null, 
        categoryId: String? = null,
        initialProvider: Provider? = null,
        categories: List<CategoryEntity> = emptyList()
    ) {
        if (currentChatId == chatId) return
        currentChatId = chatId
        activeCompanyId = companyId
        activeCategoryId = categoryId

        _uiState.update { it.copy(allCategories = categories) }

        initialProvider?.let { p ->
            val decoratedProvider = providerRepository.decorateProvider(p, companyId)
            _uiState.update { it.copy(activeProvider = decoratedProvider) }
        } ?: viewModelScope.launch {
            // Si no viene el provider, intentamos recuperarlo del repositorio
            val otherId = ChatIdHelper.extractOtherParticipantId(chatId, currentUserId)
            val p = providerRepository.getProviderById(otherId)
            p?.let {
                val decoratedProvider = providerRepository.decorateProvider(it, companyId)
                _uiState.update { it.copy(activeProvider = decoratedProvider) }
            } ?: run {
                // Si no está en Room, forzamos un fetch remoto
                providerRepository.fetchAndSyncProviderDetail(otherId)
                val remoteP = providerRepository.getProviderById(otherId)
                remoteP?.let {
                    val decoratedProvider = providerRepository.decorateProvider(it, companyId)
                    _uiState.update { it.copy(activeProvider = decoratedProvider) }
                }
            }
        }

        val otherId = ChatIdHelper.extractOtherParticipantId(chatId, currentUserId)
        setupOnlineStatusListener(otherId)

        chatRepository.startListening(chatId)

        viewModelScope.launch {
            chatRepository.getMessages(chatId).collect { messages ->
                val confirmedIds = messages
                    .filter { it.type == MessageType.APPOINTMENT_RECEIPT }
                    .mapNotNull { it.calendarInviteMessageId }
                    .toSet()

                val uiMessages = messages.map { msg ->
                    val budgetId = if (msg.type == MessageType.BUDGET) {
                        msg.relatedId ?: msg.id
                    } else null
                    val budget = budgetId?.let { budgetRepository.getBudgetById(it) }
                    
                    val catName = budget?.category ?: msg.categoryId
                    val emoji = _uiState.value.allCategories.find { it.name == catName }?.icon
                    
                    ChatMessageUiModel(msg, budget, emoji)
                }
                _uiState.update { it.copy(
                    messages = uiMessages,
                    confirmedInviteIds = confirmedIds
                ) }
            }
        }

        val providerId = ChatIdHelper.extractOtherParticipantId(chatId, currentUserId)
        viewModelScope.launch {
            chatRepository.observeTypingStatus(chatId, providerId).collect { isTyping ->
                _uiState.update { it.copy(isProviderTyping = isTyping) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatRepository.stopListening()
        
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

    fun sendImage(uri: Uri) {
        val replyToMessage = uiState.value.replyingToMessage
        viewModelScope.launch {
            val message = createMessage(MessageType.IMAGE, "[Imagen]", replyToMessage)
            chatRepository.sendMessage(message.copy(imageUrl = uri.toString()))
            _events.emit(ChatUiEvent.MessageSent)
            if (replyToMessage != null) setReplyMessage(null)
        }
    }

    fun sendAudio(path: String) {
        val replyToMessage = uiState.value.replyingToMessage
        viewModelScope.launch {
            val message = createMessage(MessageType.AUDIO, "[Audio]", replyToMessage).copy(
                imageUrl = path
            )
            chatRepository.sendMessage(message)
            _events.emit(ChatUiEvent.MessageSent)
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
                locationAddress = address
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
                appointmentType = type,
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
                    setAudioEncodingBitRate(12200)
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
                if (_recordingTime.value >= 60) {
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
            sendAudio(path)
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
            val budget = budgetRepository.getBudgetById(budgetId)
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

    fun setReplyMessage(message: MessageEntity?) {
        _uiState.update { it.copy(replyingToMessage = message) }
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

    fun openBookingDialog(message: MessageEntity, availableAddresses: List<com.example.myapplication.presentation.components.AddressInfo>) {
        val days = parseAvailabilityJson(message.availabilityJson ?: "[]")
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

    fun onAddressSelected(address: com.example.myapplication.presentation.components.AddressInfo) {
        _uiState.update { it.copy(
            bookingUiState = it.bookingUiState.copy(selectedAddress = address)
        ) }
    }

    private fun updateSlotsForSelectedDay(bookedSlotsJson: String) {
        val state = _uiState.value.bookingUiState
        val day = state.selectedDay ?: return
        val booked = parseBookedSlotsJson(bookedSlotsJson)
        val slots = generateSlotsFromAvailability(day, booked)
        _uiState.update { it.copy(
            bookingUiState = it.bookingUiState.copy(slots = slots)
        ) }
    }

    private fun parseAvailabilityJson(json: String): List<DayAvailability> {
        val list = mutableListOf<DayAvailability>()
        try {
            val array = JSONArray(json)
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val dateStr = obj.getString("date")
                val startTimeStr = obj.getString("startTime")
                val endTimeStr = obj.getString("endTime")
                val duration = obj.getInt("durationMinutes")
                
                val date = dateFormatter.parse(dateStr)
                if (date != null) {
                    list.add(DayAvailability(
                        date = date,
                        startTime = startTimeStr,
                        endTime = endTimeStr,
                        slotDurationMinutes = duration
                    ))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.sortedBy { it.date }
    }

    private fun parseBookedSlotsJson(json: String): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val dateStr = obj.getString("date")
                val timeStr = obj.getString("time")
                list.add(dateStr to timeStr)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun generateSlotsFromAvailability(avail: DayAvailability, booked: List<Pair<String, String>>): List<TimeSlot> {
        val slots = mutableListOf<TimeSlot>()
        val timeSdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateKey = dateSdf.format(avail.date)
        
        if (avail.slotDurationMinutes <= 0) return emptyList()

        try {
            var current = timeSdf.parse(avail.startTime)
            val end = timeSdf.parse(avail.endTime)
            
            if (current != null && end != null) {
                val calendar = Calendar.getInstance()
                while (true) {
                    val currentTime = current!!
                    calendar.time = currentTime
                    val next = Calendar.getInstance().apply {
                        time = currentTime
                        add(Calendar.MINUTE, avail.slotDurationMinutes)
                    }.time
                    
                    if (next.after(end)) break
                    
                    val currentTimeStr = timeSdf.format(current)
                    val isOccupied = booked.any { it.first == dateKey && it.second == currentTimeStr }
                    slots.add(TimeSlot(time = currentTimeStr, isOccupied = isOccupied))
                    current = next
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return slots
    }

    private fun createMessage(type: MessageType, content: String, replyTo: MessageEntity? = null): MessageEntity {
        return MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = currentChatId,
            senderId = currentUserId,
            receiverId = ChatIdHelper.extractOtherParticipantId(currentChatId, currentUserId),
            type = type,
            content = content,
            companyId = activeCompanyId,
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
