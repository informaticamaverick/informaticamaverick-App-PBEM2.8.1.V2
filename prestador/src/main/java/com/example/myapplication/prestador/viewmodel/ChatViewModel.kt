package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.ConversationEntity
import com.example.myapplication.prestador.data.local.entity.MessageEntity
import com.example.myapplication.prestador.data.model.Message
import com.example.myapplication.prestador.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val myUserId =
        FirebaseAuth.getInstance().currentUser?.uid ?:
        ""
    private var currentConversationId = ""

    private val _isLoading =
        MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()

    val conversations: StateFlow<List<ConversationEntity>> = repository.getAllConversations()
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
        android.util.Log.d("TYPING_VM", "observeClientTyping llamado chatId=$chatId clientId=$clientId")
        typingObserveJob?.cancel()
        typingObserveJob = viewModelScope.launch {
            android.util.Log.d("TYPING_VM", "coroutine iniciada, colectando flow...")
            repository.observeClientTyping(chatId, clientId).collect {
                android.util.Log.d("TYPING_VM", "isClientTyping = $it")
                _isClientTyping.value = it
            }
        }
        android.util.Log.d("TYPING_VM", "job lanzado: $typingObserveJob")
    }

    fun loadConversationsByProvider(providerId: String) {
        // Ahora usamos StateIn directamente sobre el Flow del repositorio
    }

    fun
            loadMessagesByConversation(conversationId:
                                       String) {
        currentConversationId = conversationId

        repository.startListening(conversationId,
            myUserId)
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getMessagesByConversation(conversationId).collect { newList ->
                    _messages.value = newList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erroral cargar mensajes: ${e.message}"
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
                kotlinx.coroutines.delay(3000)
                repository.setTypingStatus(currentConversationId, myUserId, false)
            }
        } else {
            typingJob?.cancel()
            repository.setTypingStatus(currentConversationId, myUserId, false)
        }
    }

    fun setUserOnline(isOnline: Boolean) {
        if (myUserId.isEmpty()) return
        repository.setUserOnline(myUserId, isOnline)
    }

    fun sendMessage(text: String) {
        if (text.isBlank() ||
            currentConversationId.isEmpty()) return
        viewModelScope.launch {
            try {

                repository.sendMessage(currentConversationId,
                    text, myUserId)
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
                    pres = pres
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
                repository.sendImageMessage(currentConversationId, base64, myUserId)
                
                // Limpieza local de archivos temporales si es necesario
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
                    senderId = myUserId
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
                    repository.sendAudioMessage(currentConversationId, path, duration, myUserId)
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
        // Mantenido por compatibilidad si se llama externamente, pero lo ideal es usar los métodos de arriba
        if (currentConversationId.isEmpty()) return
        viewModelScope.launch {
            try {
                repository.sendAudioMessage(currentConversationId, audioPath, durationSeconds, myUserId)
            } catch (e: Exception) {
                _errorMessage.value = "Error al enviar audio: ${e.message}"
            }
        }
    }



    fun createConversation(conversation:
                           ConversationEntity) {
        viewModelScope.launch {
            try {

                repository.saveConversation(conversation)
                _successMessage.value =
                    "Conversación creada"
            } catch (e: Exception) {
                _errorMessage.value = "Error al crear conversación: ${e.message}"
            }
        }
    }

    fun markMessagesAsRead(conversationId:
                           String) {
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

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    // Inicia la escucha en Firestore para descubrir conversaciones nuevas del prestador
    // y actualiza _conversations desde Room automáticamente
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
