package com.example.myapplication.prestador.viewmodel

import android.util.Log
import com.example.myapplication.prestador.data.model.Message
import com.example.myapplication.prestador.data.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 🎯 NUEVO SISTEMA INMUTABLE para reprogramación de citas
 * 
 * Soluciona el problema de recomposición usando StateFlow inmutable.
 * NO MUTA objetos - siempre crea nuevas instancias.
 */
object AppointmentRescheduleManager {
    
    private val TAG = "🔥 RescheduleManager"
    
    // Ámbito para operaciones asíncronas de persistencia
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Referencia al repositorio (se debe inicializar desde un punto de entrada de Hilt o ViewModel)
    private var chatRepository: ChatRepository? = null
    
    // StateFlow para cada conversación: clientId -> List<Message>
    private val _conversationFlows = mutableMapOf<String, MutableStateFlow<List<Message>>>()
    
    /**
     * 🟢 SECCIÓN: INICIALIZACIÓN
     * Permite vincular el repositorio real al manager
     */
    fun initialize(repository: ChatRepository) {
        this.chatRepository = repository
        Log.d(TAG, "🚀 Manager inicializado con ChatRepository real")
    }
    
    /**
     * 🟢 SECCIÓN: OBTENCIÓN DE DATOS
     * Obtiene el StateFlow de mensajes para un cliente específico
     */
    fun getMessages(clientId: String): StateFlow<List<Message>> {
        return _conversationFlows.getOrPut(clientId) {
            MutableStateFlow(emptyList())
        }.asStateFlow()
    }
    
    /**
     * 🟢 SECCIÓN: ACTUALIZACIÓN DE PROPUESTAS
     * Actualiza propuesta de cita con NUEVA fecha/hora y persiste en DB Real
     */
    fun updateAppointmentProposal(
        clientId: String,
        appointmentId: String,
        newDate: String,
        newTime: String
    ) {
        Log.d(TAG, "📝 Actualizando propuesta real: id=$appointmentId, $newDate $newTime")
        
        val flow = _conversationFlows[clientId] ?: return
        
        // 1. Actualización local inmediata para UI fluida
        val updatedMessages = flow.value.map { message ->
            if (message.type == Message.MessageType.APPOINTMENT && message.appointmentId == appointmentId) {
                message.copy(
                    appointmentDate = newDate,
                    appointmentTime = newTime,
                    appointmentStatus = Message.AppointmentProposalStatus.PENDING,
                    timestamp = System.currentTimeMillis()
                )
            } else {
                message
            }
        }
        flow.value = updatedMessages

        // 2. Persistencia en Base de Datos Real (Asíncrono)
        managerScope.launch {
            try {
                val targetMsg = updatedMessages.find { it.appointmentId == appointmentId }
                
                targetMsg?.let { msg ->
                    chatRepository?.updateAppointmentStatus(msg.id, "PENDING", null)
                    Log.d(TAG, "✅ Persistencia de reprogramación completada para $appointmentId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al persistir reprogramación: ${e.message}")
            }
        }
    }
    
    /**
     * 🟢 SECCIÓN: GESTIÓN DE ESTADOS
     * Actualiza estado de propuesta (ACCEPTED/REJECTED) en Flow y DB Real
     */
    fun updateAppointmentStatus(
        clientId: String,
        appointmentId: String,
        newStatus: String // Recibe String para facilidad desde UI
    ) {
        val statusEnum = try {
            Message.AppointmentProposalStatus.valueOf(newStatus)
        } catch (e: Exception) {
            Message.AppointmentProposalStatus.PENDING
        }

        Log.d(TAG, "🎯 Actualizando estado real: id=$appointmentId, status=$statusEnum")
        
        val flow = _conversationFlows[clientId] ?: return
        var messageIdToUpdate: String? = null
        
        val updatedMessages = flow.value.map { message ->
            if (message.type == Message.MessageType.APPOINTMENT && message.appointmentId == appointmentId) {
                messageIdToUpdate = message.id
                message.copy(
                    appointmentStatus = statusEnum,
                    timestamp = System.currentTimeMillis()
                )
            } else {
                message
            }
        }
        
        flow.value = updatedMessages
        
        // Persistir en ChatRepository Real
        messageIdToUpdate?.let { msgId ->
            managerScope.launch {
                chatRepository?.updateAppointmentStatus(msgId, newStatus, null)
                Log.d(TAG, "✅ Estado $newStatus persistido en DB para mensaje $msgId")
            }
        }
    }
    
    /**
     * 🟢 SECCIÓN: SINCRONIZACIÓN
     * Carga mensajes reales desde una lista externa (usualmente desde un ViewModel)
     */
    fun syncWithRealData(clientId: String, realMessages: List<Message>) {
        val flow = _conversationFlows.getOrPut(clientId) {
            MutableStateFlow(emptyList())
        }
        flow.value = realMessages.map { it.copy() }
        Log.d(TAG, "🔄 Sincronizado con datos reales: ${realMessages.size} mensajes para $clientId")
    }
    
    /**
     * Agrega un nuevo mensaje a la conversación y lo envía vía repositorio
     */
    fun addMessage(clientId: String, message: Message) {
        val flow = _conversationFlows.getOrPut(clientId) {
            MutableStateFlow(emptyList())
        }
        
        flow.value = flow.value + message
        
        // Enviar mensaje real si es del usuario actual
        if (message.isFromCurrentUser) {
            managerScope.launch {
                chatRepository?.sendMessage(
                    conversationId = clientId, // O el ID de chat correspondiente
                    text = message.text ?: "",
                    myUserId = "current_user_id" // Obtener del Auth real
                )
            }
        }
    }
    
    fun reset(clientId: String) {
        _conversationFlows.remove(clientId)
        Log.d(TAG, "🗑️ Flow liberado para $clientId")
    }
}
