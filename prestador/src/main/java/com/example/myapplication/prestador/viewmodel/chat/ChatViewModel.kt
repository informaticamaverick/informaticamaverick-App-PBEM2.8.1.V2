package com.example.myapplication.prestador.viewmodel.chat

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.myapplication.core.ChatIdHelper
import com.example.myapplication.core.data.local.entity.BudgetEntity
import com.example.myapplication.core.data.repository.BudgetRepository
import com.example.myapplication.core.data.repository.ProviderRepository
import com.example.myapplication.core.data.repository.UserRepository
import com.example.myapplication.core.domain.model.MessageType
import com.example.myapplication.core.domain.model.User
import com.example.myapplication.core.domain.model.Provider
import com.example.myapplication.core.utils.AudioUtils
import com.example.myapplication.core.utils.ImageUtils
import com.example.myapplication.core.data.repository.ChatRepository
import com.example.myapplication.core.data.local.entity.MessageEntity
import com.example.myapplication.core.data.local.dao.ChatSummary
import com.example.myapplication.prestador.data.ChatData
// import com.example.myapplication.prestador.data.repository.ChatRepository // REMOVED
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import com.example.myapplication.core.data.remote.CalendarMapper

// ==========================================
// MODELOS DE UI PARA EL CHAT (MAVERICK ELITE)
// ==========================================

enum class InboxType {
    PERSONAL, EMPRESA
}

data class ChatUiState(
    val pagingMessages: Flow<PagingData<MessageEntity>> = emptyFlow(),
    val isRecording: Boolean = false,
    val recordingTime: Int = 0,
    val isClientTyping: Boolean = false,
    val isClientOnline: Boolean = false, // 🔥 [NUEVO] Estado online
    val clientProfile: User? = null,
    val isLoadingProfile: Boolean = false,
    val activeCompanyId: String? = null,
    val activeBranchId: String? = null, // 🔥 Identidad del Prestador (Sucursal)
    val clientBranchId: String? = null, // 🔥 [NUEVO] Identidad del Cliente (Sucursal)
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val pendingReply: MessageEntity? = null,
    val selectedBudget: BudgetEntity? = null,
    val isFetchingFullBudget: Boolean = false,
    val bookingUiState: BookingUiState = BookingUiState()
)

typealias DayAvailability = CalendarMapper.DayAvailability
typealias TimeSlot = CalendarMapper.TimeSlot

data class BookingUiState(
    val availableDays: List<DayAvailability> = emptyList(),
    val selectedDay: DayAvailability? = null,
    val slots: List<TimeSlot> = emptyList(),
    val selectedTime: String? = null,
    val selectedAddress: com.example.myapplication.core.domain.model.AddressUnico? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val providerRepository: ProviderRepository,
    private val userRepository: UserRepository,
    private val budgetRepository: BudgetRepository,
    private val auth: FirebaseAuth,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val myUserId = auth.currentUser?.uid ?: ""
    private var currentChatId: String = ""

    // 🔥 [NUEVO] Estados para Inbox y Perfil (Requeridos por ChatScreen)
    private val _selectedInbox = MutableStateFlow(InboxType.PERSONAL)
    val selectedInbox = _selectedInbox.asStateFlow()

    private val _hasCompanyInbox = MutableStateFlow(false)
    val hasCompanyInbox = _hasCompanyInbox.asStateFlow()

    private val _providerPhotoUrl = MutableStateFlow<String?>(null)
    val providerPhotoUrl = _providerPhotoUrl.asStateFlow()

    private val _companyPhotoUrl = MutableStateFlow<String?>(null)
    val companyPhotoUrl = _companyPhotoUrl.asStateFlow()

    private val _companyName = MutableStateFlow<String?>(null)
    val companyName = _companyName.asStateFlow()

    private val _providerProfile = MutableStateFlow<Provider?>(null)
    val providerProfile = _providerProfile.asStateFlow()

    private val _activeCompanyId = MutableStateFlow<String?>(null)
    val activeCompanyId = _activeCompanyId.asStateFlow()

    private val _activeBranchId = MutableStateFlow<String?>(null)
    val activeBranchId = _activeBranchId.asStateFlow()

    private val _liveClientPhotos = MutableStateFlow<Map<String, String?>>(emptyMap())
    val liveClientPhotos = _liveClientPhotos.asStateFlow()

    // 🔥 [NUEVO] Total de no leídos (Para badge en Dashboard)
    val totalUnreadCount: Flow<Int> = chatRepository.getTotalUnreadCount(myUserId)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val conversations: StateFlow<List<ChatData.Conversation>> = combine(
        chatRepository.getActiveChatSummaries(myUserId),
        _selectedInbox,
        _activeCompanyId,
        _activeBranchId,
        _providerProfile.map { it?.companies ?: emptyList() }.distinctUntilChanged()
    ) { args ->
        val summaries = args[0] as List<ChatSummary>
        val inboxType = args[1] as InboxType
        val companyId = args[2] as String?
        val branchId = args[3] as String?
        val myCompanies = args[4] as List<com.example.myapplication.core.domain.model.CompanyProvider>

        Log.d("ChatViewModel", "🕵️ [SYNC_CONVERSATIONS] Inbox: $inboxType | Company: $companyId | Branch: $branchId | Total Summaries: ${summaries.size}")

        summaries.filter { summary ->
            val sBranchId = summary.branchId?.takeIf { it.isNotBlank() && it != "none" }
            val sCompanyId = summary.companyId?.takeIf { it.isNotBlank() && it != "none" }

            // 🔥 [ELITE v10.3] OPTIMIZACIÓN DE IDENTIDAD
            // 1. Clasificación Primaria (Basada en Room SSOT)
            val profileKeys = mutableListOf<String>()
            
            if (sBranchId == null && sCompanyId == null) {
                profileKeys.add("personal")
            } else {
                // Si el mensaje ya trae companyId, es la fuente primaria
                if (sCompanyId != null) profileKeys.add(sCompanyId)
                
                // Fallback: Mapeo reverso si solo tenemos branchId (usa el perfil cargado)
                if (sCompanyId == null && sBranchId != null) {
                    myCompanies.forEach { company ->
                        if (company.branches.any { it.id == sBranchId }) profileKeys.add(company.id)
                    }
                }
            }

            // Si no se pudo clasificar por tags, lo mandamos a personal como último recurso
            if (profileKeys.isEmpty()) profileKeys.add("personal")

            // Aplicamos el filtro de la bandeja actual
            val currentIdentityId = if (inboxType == InboxType.PERSONAL) "personal" else companyId
            
            val matchesIdentity = profileKeys.contains(currentIdentityId)
            
            if (!matchesIdentity) return@filter false

            // Si estamos en una empresa, aplicamos el filtro de sucursal (Nivel 2)
            if (inboxType == InboxType.EMPRESA && branchId != null) {
                sBranchId == branchId
            } else {
                true // Si branchId es null, mostramos todos los de la empresa (General)
            }
        }.map { summary ->
            // Sincronización proactiva de perfiles
            if (summary.userName == "Usuario" || summary.userName == "Cliente") {
                viewModelScope.launch { userRepository.fetchAndSyncUserDetail(summary.userId) }
            }

            ChatData.Conversation(
                userId = summary.userId,
                userName = summary.userName ?: "Cliente",
                lastMessage = summary.lastMessage,
                timestamp = summary.lastTimestamp,
                unreadCount = summary.unreadCount,
                conversationId = summary.chatId,
                userAvatarUrl = summary.userPhoto,
                isOnline = summary.isOnline ?: false,
                isVerified = summary.isVerified ?: false,
                branchId = summary.branchId
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var activeCategoryId: String? = null

    // -- Grabación de Audio --
    private var mediaRecorder: android.media.MediaRecorder? = null
    private var audioFilePath: String? = null
    private var recordingTimerJob: Job? = null

    /**
     * LEY #5: Carga Proactiva
     */
    fun syncConversations() {
        if (myUserId.isEmpty()) return
        chatRepository.startGlobalListening(myUserId)
        
        viewModelScope.launch {
            providerRepository.loadFullProfile(myUserId)
            val provider = providerRepository.getProviderById(myUserId)
            provider?.companies?.forEach { company ->
                chatRepository.startGlobalListeningForCompany(company.id)
            }
        }
    }

    fun loadProviderProfile(providerId: String) {
        viewModelScope.launch {
            val provider = providerRepository.getProviderById(providerId)
            _providerProfile.value = provider
            _providerPhotoUrl.value = provider?.photoUrl
            _hasCompanyInbox.value = provider?.companies?.isNotEmpty() ?: false

            if (provider?.companies?.isNotEmpty() == true && _selectedInbox.value == InboxType.EMPRESA) {
                val firstCompany = provider.companies.first()
                _activeCompanyId.value = firstCompany.id
                _companyName.value = firstCompany.name
                _companyPhotoUrl.value = firstCompany.photoUrl
            }
        }
    }

    fun refreshProviderProfile(providerId: String) {
        viewModelScope.launch {
            providerRepository.loadFullProfile(providerId)
            loadProviderProfile(providerId)
        }
    }

    fun selectInbox(type: InboxType, companyId: String?, branchId: String? = null) {
        _selectedInbox.value = type
        _activeCompanyId.value = companyId
        _activeBranchId.value = branchId

        if (type == InboxType.EMPRESA && companyId != null) {
            val company = _providerProfile.value?.companies?.find { it.id == companyId }
            _companyName.value = company?.name
            _companyPhotoUrl.value = company?.photoUrl ?: company?.thumbnailBase64
        } else {
            _companyName.value = null
            _companyPhotoUrl.value = null
        }
    }

    fun deleteConversations(userIds: Set<String>) {
        viewModelScope.launch {
            val chatIds = conversations.value
                .filter { it.userId in userIds }
                .map { it.conversationId }

            if (chatIds.isNotEmpty()) {
                chatRepository.deleteConversations(chatIds)
            }
        }
    }

    fun observeClientPhoto(clientId: String) {
        viewModelScope.launch {
            userRepository.getUserById(clientId).collect { user ->
                _liveClientPhotos.update { it + (clientId to user?.photoUrl) }
            }
        }
    }

    private var onlineStatusListener: com.google.firebase.database.ValueEventListener? = null
    private var typingTimeoutJob: Job? = null

    /**
     * LEY #3: Carga Shallow vs Deep
     */
    fun loadConversation(
        chatId: String,
        companyId: String? = null,
        branchId: String? = null,
        clientBranchId: String? = null, // 🔥 [NUEVO]
        categoryId: String? = null
    ) {
        if (currentChatId == chatId) return
        currentChatId = chatId
        _activeCompanyId.value = companyId
        activeCategoryId = categoryId

        _uiState.update { it.copy(
            activeCompanyId = companyId,
            activeBranchId = branchId,
            clientBranchId = clientBranchId
        ) }

        // [ELITE v4] Paginación Real desde Room via Repository del Core
        val messagesFlow = chatRepository.getMessagesPaging(chatId)
            .cachedIn(viewModelScope)

        _uiState.update { it.copy(pagingMessages = messagesFlow) }

        val clientId = ChatIdHelper.extractOtherParticipantId(chatId, myUserId)
        loadClientProfile(clientId)
        setupOnlineStatusListener(clientId)

        chatRepository.startListening(chatId)
        observeClientTyping(chatId, clientId)
    }

    private fun setupOnlineStatusListener(clientId: String) {
        onlineStatusListener?.let {
            com.google.firebase.database.FirebaseDatabase.getInstance().reference
                .child("users").child(ChatIdHelper.extractOtherParticipantId(currentChatId, myUserId))
                .child("online").removeEventListener(it)
        }

        if (clientId.isEmpty()) return

        val ref = com.google.firebase.database.FirebaseDatabase.getInstance().reference
            .child("users").child(clientId).child("online")

        onlineStatusListener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val isOnline = snapshot.getValue(Boolean::class.java) ?: false
                _uiState.update { it.copy(isClientOnline = isOnline) }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }
        ref.addValueEventListener(onlineStatusListener!!)
    }

    private fun loadClientProfile(clientId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProfile = true) }
            userRepository.getUserById(clientId).collect { user ->
                _uiState.update { it.copy(
                    clientProfile = user,
                    isLoadingProfile = false
                ) }
            }
        }
    }

    fun refreshClientProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProfile = true) }
            userRepository.refreshUserFromRemote()
            _uiState.update { it.copy(isLoadingProfile = false) }
        }
    }

    private fun observeClientTyping(chatId: String, clientId: String) {
        viewModelScope.launch {
            chatRepository.observeTypingStatus(chatId, clientId).collect { isTyping ->
                _uiState.update { it.copy(isClientTyping = isTyping) }
                
                // 🔥 [ELITE v8.6] Timeout de seguridad para estado "Escribiendo"
                // Evita que el cliente quede perpetuamente en typing si se desconecta.
                if (isTyping) {
                    typingTimeoutJob?.cancel()
                    typingTimeoutJob = viewModelScope.launch {
                        delay(8000) // 8 segundos de gracia
                        _uiState.update { it.copy(isClientTyping = false) }
                    }
                } else {
                    typingTimeoutJob?.cancel()
                }
            }
        }
    }

    fun setTypingStatus(isTyping: Boolean) {
        if (currentChatId.isEmpty()) return
        chatRepository.setTypingStatus(currentChatId, myUserId, isTyping)
    }

    fun respondToAppointmentRequest(
        messageId: String,
        clientName: String,
        date: String,
        time: String,
        service: String = "",
        accepted: Boolean,
        rejectionReason: String? = null
    ) {
        viewModelScope.launch {
            if (accepted) {
                // 1. Actualizar estado en Room y RTDB (CORE)
                chatRepository.updateAppointmentStatus(messageId, currentChatId, "ACCEPTED")

                // 2. Enviar comprobante al cliente (ELITE v4 Standard)
                val provider = _providerProfile.value
                val receiptMessage = MessageEntity(
                    chatId = currentChatId,
                    senderId = myUserId,
                    receiverId = ChatIdHelper.extractOtherParticipantId(currentChatId, myUserId),
                    type = MessageType.APPOINTMENT_RECEIPT,
                    content = "Turno confirmado",
                    appointmentDate = date,
                    appointmentTime = time,
                    receiptService = service,
                    receiptProviderName = provider?.displayName ?: "",
                    receiptIsTechnician = provider?.serviceType == "TECHNICAL",
                    receiptProfession = provider?.profesion,
                    receiptAddress = provider?.address?.fullString(),
                    receiptCode = UUID.randomUUID().toString().take(6).uppercase(),
                    receiptPrioritizeCompany = provider?.priorizarEmpresa ?: false,
                    companyId = _activeCompanyId.value,
                    categoryId = activeCategoryId
                )
                chatRepository.sendMessage(receiptMessage)

                // 3. Guardar en el repositorio local de citas (Efecto secundario del prestador)
                // Usamos el repositorio local del prestador que aún maneja BookedAppointment
                saveLocalBookedAppointment(messageId, clientName, date, time, service)
            } else {
                chatRepository.updateAppointmentStatus(messageId, currentChatId, "REJECTED", rejectionReason)
            }
        }
    }

    private suspend fun saveLocalBookedAppointment(
        messageId: String,
        clientName: String,
        date: String,
        time: String,
        service: String
    ) {
        val clientId = ChatIdHelper.extractOtherParticipantId(currentChatId, myUserId)
        // NOTA: Como hemos unificado el ChatRepository, el guardado de BookedAppointment
        // debe hacerse a través de un DAO o Repositorio específico del prestador que inyectemos aquí.
        // Por ahora, asumo que ChatViewModel tiene acceso a lo necesario o lo inyectamos.
    }

    fun sendCancellationNotice(chatId: String, date: String, time: String, reason: String) {
        viewModelScope.launch {
            val message = createBaseMessage(MessageType.VISIT, "Turno cancelado").copy(
                appointmentDate = date,
                appointmentTime = time,
                rejectionReason = reason
            )
            chatRepository.sendStatusNotice(message)
        }
    }

    fun sendCompletionNotice(chatId: String, date: String, time: String) {
        viewModelScope.launch {
            val message = createBaseMessage(MessageType.VISIT, "Turno completado").copy(
                appointmentDate = date,
                appointmentTime = time,
                appointmentStatus = "COMPLETED"
            )
            chatRepository.sendStatusNotice(message)
        }
    }

    fun sendRescheduleNotice(chatId: String, date: String, time: String) {
        viewModelScope.launch {
            val message = createBaseMessage(MessageType.VISIT, "Tu turno será reprogramado").copy(
                appointmentDate = date,
                appointmentTime = time,
                appointmentStatus = "RESCHEDULED"
            )
            chatRepository.sendStatusNotice(message)
        }
    }

    // -- Envío de Mensajes Especiales (Simetría Elite v4) --

    fun sendLocation(lat: Double, lng: Double, address: String) {
        val reply = _uiState.value.pendingReply
        viewModelScope.launch {
            val message = createBaseMessage(MessageType.LOCATION, address, reply).copy(
                latitude = lat,
                longitude = lng,
                locationAddress = address
            )
            chatRepository.sendMessage(message)
            _uiState.update { it.copy(pendingReply = null) }
        }
    }

    fun sendBudgetMessage(pres: com.example.myapplication.prestador.data.local.entity.PresupuestoEntity) {
        val reply = _uiState.value.pendingReply
        viewModelScope.launch {
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
                put("companyName", pres.providerCompanyName)
                put("categorias", pres.categorias)
                put("clienteId", pres.clienteId)
            }.toString()

            val message = createBaseMessage(MessageType.BUDGET, "Presupuesto ${pres.numeroPresupuesto}", reply).copy(
                budgetDataJson = budgetJson
            )
            chatRepository.sendMessage(message)
            _uiState.update { it.copy(pendingReply = null) }
        }
    }

    fun sendCalendarInvite(
        startDate: String,
        endDate: String,
        availabilityJson: String,
        bookedSlotsJson: String,
        appointmentType: String,
        providerAddress: String?,
        serviceCategory: String = ""
    ) {
        val reply = _uiState.value.pendingReply
        viewModelScope.launch {
            val message = createBaseMessage(MessageType.CALENDAR_INVITE, "Calendario de disponibilidad enviado", reply).copy(
                calendarStartDate = startDate,
                calendarEndDate = endDate,
                availabilityJson = availabilityJson,
                bookedSlotsJson = bookedSlotsJson,
                appointmentType = appointmentType,
                providerAddress = providerAddress
            )
            chatRepository.sendMessage(message)
            _uiState.update { it.copy(pendingReply = null) }
        }
    }

    fun sendAppointment(date: String, time: String, notes: String, type: String? = null, address: String? = null) {
        val reply = _uiState.value.pendingReply
        viewModelScope.launch {
            val message = createBaseMessage(MessageType.VISIT, notes.ifBlank { "Nueva propuesta de turno" }, reply).copy(
                appointmentDate = date,
                appointmentTime = time,
                appointmentStatus = "PENDING",
                appointmentType = type,
                providerAddress = address
            )
            chatRepository.sendMessage(message)
            _uiState.update { it.copy(pendingReply = null) }
        }
    }

    fun onBudgetClicked(budgetId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingFullBudget = true) }
            val budget = budgetRepository.getBudgetById(budgetId)
            _uiState.update { it.copy(selectedBudget = budget, isFetchingFullBudget = false) }
        }
    }

    fun clearSelectedBudget() {
        _uiState.update { it.copy(selectedBudget = null) }
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

    private fun updateSlotsForSelectedDay(bookedSlotsJson: String) {
        val state = _uiState.value.bookingUiState
        val day = state.selectedDay ?: return
        val booked = CalendarMapper.parseBookedSlotsJson(bookedSlotsJson)
        val slots = CalendarMapper.generateSlotsFromAvailability(day, booked)
        _uiState.update { it.copy(
            bookingUiState = it.bookingUiState.copy(slots = slots)
        ) }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || currentChatId.isEmpty()) return
        val reply = _uiState.value.pendingReply

        viewModelScope.launch {
            val message = createBaseMessage(MessageType.TEXT, text, reply)
            chatRepository.sendMessage(message)
            _uiState.update { it.copy(pendingReply = null) }
        }
    }

    fun sendImage(uri: Uri) {
        viewModelScope.launch {
            try {
                // 🔥 [ELITE] Compresión y Thumbnail en hilo secundario (Ley #4)
                val result = withContext(Dispatchers.Default) {
                    val compressed = ImageUtils.compressElite(context, uri)
                    val thumb = ImageUtils.generateThumbnailBase64(context, uri)
                    compressed to thumb
                }

                val compressedBytes = result.first
                val thumbnail = result.second

                if (compressedBytes != null) {
                    // 1. Guardar archivo físico local para el emisor (Blindaje de Room - Ley #2)
                    val fileName = "IMG_SENT_${System.currentTimeMillis()}"
                    val localPath = withContext(Dispatchers.IO) {
                        ImageUtils.saveBytesToFile(context, compressedBytes, fileName)
                    }

                    // 2. Preparar el Base64 (Solo para el viaje a Firebase)
                    val base64 = ImageUtils.bytesToBase64(compressedBytes)

                    // 3. Construir mensaje
                    // 🔥 [ELITE] Room Shielding: Guardamos "[Imagen]" en content para Room,
                    // pero enviamos el Base64 a Firebase.
                    val message = createBaseMessage(MessageType.IMAGE, "[Imagen]").copy(
                        imageLocalPath = localPath,
                        thumbnailBase64 = thumbnail
                    )

                    // 4. Enviar (ChatRepository.sendMessage guardará en Room)
                    chatRepository.sendMessage(message, base64)
                }
            } catch (e: Exception) {
                Log.e("ChatVM", "Error enviando imagen: ${e.message}")
            }
        }
    }

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
                    AudioUtils.configureEliteRecorder(this)
                    setOutputFile(audioFilePath)
                    prepare()
                    start()
                }
                _uiState.update { it.copy(isRecording = true, recordingTime = 0) }
                startRecordingTimer()
            } catch (e: Exception) {
                Log.e("ChatVM", "Error al grabar: ${e.message}")
            }
        }
    }

    private fun startRecordingTimer() {
        recordingTimerJob?.cancel()
        recordingTimerJob = viewModelScope.launch {
            while (_uiState.value.isRecording) {
                delay(1000)
                _uiState.update { it.copy(recordingTime = it.recordingTime + 1) }
                if (_uiState.value.recordingTime >= 60) {
                    stopRecordingAndSend()
                    break
                }
            }
        }
    }

    fun stopRecordingAndSend() {
        if (!_uiState.value.isRecording) return
        val path = audioFilePath
        val duration = _uiState.value.recordingTime

        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) { e.printStackTrace() }

        mediaRecorder = null
        _uiState.update { it.copy(isRecording = false) }
        recordingTimerJob?.cancel()

        if (path != null && duration > 0) {
            viewModelScope.launch {
                val (base64, audioPath) = withContext(Dispatchers.IO) {
                    val file = java.io.File(path)
                    val b64 = android.util.Base64.encodeToString(file.readBytes(), android.util.Base64.NO_WRAP)
                    b64 to path
                }
                val message = createBaseMessage(MessageType.AUDIO, "[Audio]").copy(
                    durationSeconds = duration,
                    audioLocalPath = audioPath
                )
                chatRepository.sendMessage(message, base64)
            }
        }
    }

    fun cancelRecording() {
        try { mediaRecorder?.stop(); mediaRecorder?.release() } catch (e: Exception) { }
        mediaRecorder = null
        _uiState.update { it.copy(isRecording = false) }
        recordingTimerJob?.cancel()
        audioFilePath?.let { java.io.File(it).delete() }
        audioFilePath = null
    }

    // -- Utilidades Internas --

    private fun createBaseMessage(
        type: MessageType,
        content: String,
        replyTo: MessageEntity? = null
    ): MessageEntity {
        val receiverId = ChatIdHelper.extractOtherParticipantId(currentChatId, myUserId)

        // 🔥 [ELITE v7.8] EXPLICIT SYMMETRIC ROOM TAGGING
        // Extraemos los contextos basándonos en nuestra identidad actual en la sesión.
        val (cMy, bMy, _) = ChatIdHelper.extractMyContext(currentChatId, myUserId)
        val (cOther, bOther, _) = ChatIdHelper.extractOtherContext(currentChatId, myUserId)

        return MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = currentChatId,
            senderId = myUserId,
            receiverId = receiverId,
            type = type,
            content = content,
            timestamp = System.currentTimeMillis(),
            
            // Tags legacy para Firebase (compatibilidad con apps v6-)
            branchId = bMy,
            companyId = cMy,
            
            // 🔥 [ELITE v7.8] SYMMETRIC IDENTITY TAGS (Explícitos para el receptor)
            senderBranchId = bMy,
            senderCompanyId = cMy,
            receiverBranchId = bOther,
            receiverCompanyId = cOther,

            // Tags locales para Room (Filtrado instantáneo)
            localBranchId = bMy,
            localCompanyId = cMy,
            remoteBranchId = bOther,
            remoteCompanyId = cOther,

            categoryId = activeCategoryId,
            replyToId = replyTo?.id,
            replyToContent = replyTo?.let { getReplyPreviewText(it) },
            replyToSenderName = replyTo?.let {
                if (it.senderId == myUserId) "Tú" else _uiState.value.clientProfile?.fullName ?: "Cliente"
            }
        )
    }

    fun setReply(message: MessageEntity) {
        _uiState.update { it.copy(pendingReply = message) }
    }

    fun clearReply() {
        _uiState.update { it.copy(pendingReply = null) }
    }

    fun markAsRead() {
        if (currentChatId.isEmpty()) return
        viewModelScope.launch {
            chatRepository.markMessagesAsRead(currentChatId, myUserId)
        }
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

    override fun onCleared() {
        super.onCleared()
        chatRepository.stopListening()
        recordingTimerJob?.cancel()
        typingTimeoutJob?.cancel()
        onlineStatusListener?.let {
            if (currentChatId.isNotEmpty()) {
                val clientId = ChatIdHelper.extractOtherParticipantId(currentChatId, myUserId)
                com.google.firebase.database.FirebaseDatabase.getInstance().reference
                    .child("users").child(clientId).child("online")
                    .removeEventListener(it)
            }
        }
        try { mediaRecorder?.release() } catch (_: Exception) {}
    }
}
