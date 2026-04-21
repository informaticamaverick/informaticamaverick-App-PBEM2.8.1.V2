package com.example.myapplication.presentation.client

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.MessageEntity
import com.example.myapplication.data.local.TenderEntity
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.data.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

class ChatViewModel(
    private val repository: ChatRepository,
    val chatId: String,
    val currentUserId: String,
    private val receiverId: String,
    private val context: Context
) : ViewModel() {

    private val legacyChatId = "chat_${currentUserId}_${receiverId}"
    private val activeChatIds = listOf(chatId, legacyChatId).distinct()

    val messages:
            StateFlow<List<MessageEntity>> =
        repository.getMessages(activeChatIds)
            .stateIn(viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList())

    private val _isRecording =
        MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> =
        _isRecording

    private val _selectedBudget =
        MutableStateFlow<BudgetEntity?>(null)
    val selectedBudget:
            StateFlow<BudgetEntity?> =
        _selectedBudget.asStateFlow()

    private var mediaRecorder: MediaRecorder?
            = null
    private var currentAudioPath: String? =
        null

    private val _providerPhotoUrl = MutableStateFlow<String?>(null)
    val providerPhotoUrl: StateFlow<String?> = _providerPhotoUrl.asStateFlow()

    init {
        // Escuchar mensajes entrantes del prestador en tiempo real
        repository.startListening(activeChatIds, chatId)
        // Obtener foto del prestador directo de Firestore
        viewModelScope.launch {
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("providers").document(receiverId).get().await()
                _providerPhotoUrl.value = doc.getString("imageUrl")
                    ?: doc.getString("imageBase64")
                            ?: doc.getString("photoUrl")
            } catch (_: Exception) {}
        }
    }

    // --- ENVIAR MENSAJES ---

    fun sendText(text: String) {
        if (text.isBlank()) return
        sendMessageToRepo(createMessage(MessageType.TEXT, text))
    }

    fun sendImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val (base64, localPath) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val bytes = com.example.myapplication.util.ImageUtils.compressImageToWebP(context, uri)
                    if (bytes != null) {
                        val b64 = com.example.myapplication.util.ImageUtils.bytesToBase64(bytes)
                        val path = com.example.myapplication.util.ImageUtils.saveBytesToFile(
                            context,
                            bytes,
                            System.currentTimeMillis().toString()
                        )
                        Pair(b64, path)
                    } else null
                } ?: return@launch

                val message = createMessage(MessageType.IMAGE, base64).copy(
                    imageUrl = localPath
                )
                sendMessageToRepo(message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendLocation(lat: Double, lng: Double,
                     address: String? = null) {
        val msg =
            createMessage(MessageType.LOCATION, address ?:
            "Ubicación compartida")
                .copy(latitude = lat, longitude =
                    lng, locationAddress = address)
        sendMessageToRepo(msg)
    }

    fun sendAppointment(date: String, time: String, notes: String) {
        val content = "Solicitud de cita|$date|$time|$notes"
        val msg = createMessage(MessageType.VISIT, content).copy(
            appointmentStatus = "PENDING",
            appointmentDate = date,
            appointmentTime = time
        )
        sendMessageToRepo(msg)
    }

    fun sendBudget(budgetId: String, summary:
    String = "Nuevo presupuesto recibido") {
        val msg =
            createMessage(MessageType.BUDGET,
                summary).copy(relatedId = budgetId)
        sendMessageToRepo(msg)
    }

    fun sendTenderInvitation(tender:
                             TenderEntity) {
        val content =
            "${tender.title}|${tender.description}"
        val msg =
            createMessage(MessageType.TENDER,
                content).copy(relatedId = tender.tenderId)
        sendMessageToRepo(msg)
    }

    // --- PRESUPUESTOS ---

    fun onBudgetClicked(budgetId: String) {
        viewModelScope.launch {
            _selectedBudget.value =
                repository.getBudgetById(budgetId)
        }
    }

    fun clearSelectedBudget() {
        _selectedBudget.value = null
    }

    fun getMatchingTenders(providerCategory:
                           String): Flow<List<TenderEntity>> =
        repository.getOpenTendersByCategory(providerCategory)

    // --- AUDIO ---

    fun startRecording(context: Context) {
        try {
            val audioFile =
                File(context.cacheDir,
                    "audio_${System.currentTimeMillis()}.m4a")
            currentAudioPath =
                audioFile.absolutePath
            mediaRecorder = if
                                    (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S)
                MediaRecorder(context) else
                MediaRecorder()
            mediaRecorder?.apply {

                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(32_000)
                setAudioSamplingRate(16_000)
                setOutputFile(currentAudioPath)
                prepare()
                start()
            }
            _isRecording.value = true
        } catch (e: Exception) {
            e.printStackTrace() }
    }

    fun stopRecordingAndSend() {
        if (!_isRecording.value) return
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            _isRecording.value = false
            currentAudioPath?.let { path ->
                viewModelScope.launch {
                    try {
                        val file = File(path)
                        if (!file.exists() || file.length() == 0L) {
                            android.util.Log.e("ChatViewModel", "Archivo de audio no existe o vacío: $path")
                            return@launch
                        }
                        val base64 = android.util.Base64.encodeToString(file.readBytes(), android.util.Base64.NO_WRAP)
                        val localMessage = createMessage(MessageType.AUDIO, path)
                        repository.sendAudioMessageWithBase64(localMessage, base64)
                    } catch (e: Exception) {
                        android.util.Log.e("ChatViewModel", "Error enviando audio: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            _isRecording.value = false
            currentAudioPath?.let {
                File(it).delete() }
        } catch (e: Exception) {
            e.printStackTrace() }
    }

    // --- HELPERS ---

    private fun createMessage(type:
                              MessageType, content: String) = MessageEntity(
        id = UUID.randomUUID().toString(),
        chatId = chatId,
        senderId = currentUserId,
        receiverId = receiverId,
        type = type,
        content = content,
        timestamp = System.currentTimeMillis()
    )

    private fun sendMessageToRepo(message:
                                  MessageEntity) {
        viewModelScope.launch {
            repository.sendMessage(message)
        }
    }

    private var typingJob: kotlinx.coroutines.Job? = null

    private var _isproviderTyping = MutableStateFlow(false)
    val isProviderTyping: StateFlow<Boolean> = _isproviderTyping.asStateFlow()

    fun observeProviderTyping() {
        viewModelScope.launch {
            repository.observeProviderTyping(chatId, receiverId).collect {
                _isproviderTyping.value = it
            }
        }
    }

    fun setTypingStatus(isTyping: Boolean) {
        if (isTyping) {
            repository.setTypingStatus(chatId, currentUserId, true)
            typingJob?.cancel()
            typingJob = viewModelScope.launch {
                kotlinx.coroutines.delay(3000)
                repository.setTypingStatus(chatId, currentUserId, false)
            }
        } else {
            typingJob?.cancel()
            repository.setTypingStatus(chatId, currentUserId, false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopListening()
        repository.setTypingStatus(chatId, currentUserId, false)
        mediaRecorder?.release()
    }
}

class ChatViewModelFactory(
    private val repository: ChatRepository,
    private val chatId: String,
    private val currentUserId: String,
    private val receiverId: String,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel>
            create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository,
                chatId, currentUserId, receiverId, context) as
                    T
        }
        throw
        IllegalArgumentException("Unknown ViewModel class")
    }
}
