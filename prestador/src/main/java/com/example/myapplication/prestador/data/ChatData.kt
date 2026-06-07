package com.example.myapplication.prestador.data

import androidx.compose.ui.graphics.Color
//import com.example.myapplication.prestador.data.mock.ClientesMockData
//import com.example.myapplication.prestador.data.mock.ConversacionesMock
import com.example.myapplication.prestador.data.model.ChatConversation
import com.example.myapplication.prestador.data.model.Message

/**
 * Datos de prueba para el chat del prestador
 * Usa los clientes mock y conversaciones realistas
 */
object ChatData {

    // Clase de conversación para la lista de chats
    data class Conversation(
        val userId: String,
        val userName: String,
        val lastMessage: String,
        val timestamp: Long,
        val unreadCount: Int = 0,
        val notificationsEnabled: Boolean = true,
        val isVisible: Boolean = true,
        val isLocked: Boolean = false,
        val conversationId: String = "",
        val userAvatarUrl: String? = null,
        val isOnline: Boolean = false,
        val isVerified: Boolean = false,
        val branchId: String? = null // 🔥 [NUEVO] Contexto de sucursal
    )

    /**
     * [OBSOLETO] Se comenta para evitar la creación de conversaciones mock
     * al acceder a la lista de chats, priorizando datos reales de Room/RTDB.
     *
    val conversations = ClientesMockData.clientes.mapIndexed { index, cliente ->
        ChatConversation(
            userId = cliente.id,
            name = "${cliente.nombre} ${cliente.apellido}",
            job = getJobDescription(cliente.serviceType),
            avatarColor = getAvatarColor(index),
            lastMessage = cliente.ultimoMensaje ?: "Sin mensajes",
            lastMessageTime = formatLastMessageTime(index),
            isOnline = cliente.isOnline
        )
    }
    */
    val conversations = emptyList<ChatConversation>()

    //Descripcion del trabao segun el tipo de servicio
    private fun getJobDescription(serviceType: String): String {
        return when (serviceType) {
            "TECHNICAL" -> "Solicita servicio tecnico"
            "PROFESSIONAL" -> "Consulta Profesional"
            "RENTAL" -> "Consulta alquiler"
            else -> "Consulta general"
        }
    }

    private fun getAvatarColor(index: Int): Color {
        val colors = listOf(
            Color(0xFF3B82F6),
            Color(0xFFEF4444), Color(0xFF10B981),
            Color(0xFF8B5CF6),
            Color(0xFFF59E0B), Color(0xFFEC4899),
            Color(0xFF14B8A6),
            Color(0xFFF97316), Color(0xFF6366F1),
            Color(0xFF84CC16),
            Color(0xFF3B82F6), Color(0xFFEF4444),
            Color(0xFF10B981),
            Color(0xFF8B5CF6), Color(0xFFF59E0B),
            Color(0xFFEC4899),
            Color(0xFF14B8A6), Color(0xFFF97316),
            Color(0xFF6366F1),
            Color(0xFF84CC16)
        )
        return colors[index % colors.size]
    }

    private fun formatLastMessageTime(index: Int): String {
        return when (index) {
            0 -> "Hace 10 min"; 1 -> "Hace 30 min";
            2 -> "Hace 1 hora"
            3 -> "Hace 2 horas"; 4 -> "Ayer";
            5 -> "Hace 2 días"
            6 -> "Hace 3 días"; 7 -> "Lunes";
            8 -> "15/02"
            else -> "14/02"
        }
    }

    //Devuelte todas las conversaciones (sin filtro)
    fun getAllConversations(): List<Conversation> {
        return conversations.map { chat ->
            Conversation(
                userId = chat.userId,
                userName = chat.name,
                lastMessage = chat.lastMessage,
                timestamp = System.currentTimeMillis() - (1..10000000).random(),
                unreadCount = if (chat.isOnline) (0..3).random() else 0,
                notificationsEnabled = true,
                isVisible = true,
                isLocked = false
            )
        }
    }

    //Nuevo: Devuelve solo las conversaciones del serviceType indicado

    /**
     * [OBSOLETO] Se comenta para evitar filtrado de clientes mock.
     * 
    fun getConversationsByServiceType(serviceType: String): List<Conversation> {
        val clientesFiltrados = ClientesMockData.clientes
            .filter { it.serviceType == serviceType }
        return clientesFiltrados.mapIndexed { index, cliente ->
            Conversation(
                userId = cliente.id,
                userName = "${cliente.nombre} ${cliente.apellido}",
                lastMessage = cliente.ultimoMensaje ?: "Sin mensajes",
                timestamp = System.currentTimeMillis() - (1..10000000).random(),
                unreadCount = if (cliente.isOnline) (0..3).random() else 0,
                notificationsEnabled = true,
                isVisible = true,
                isLocked = false
            )
        }
    }
    */
    fun getConversationsByServiceType(serviceType: String): List<Conversation> = emptyList()

    fun getConversationById(userId: String): ChatConversation? {
        return conversations.find { it.userId == userId }
    }

 /**
    fun getMessagesForUser(userId: String): List<Message> {
        return ConversacionesMock.obtenerMensajes(userId)
    }



    fun addMessageToUser(userId: String, message: Message){
        ConversacionesMock.agregarMensaje(userId, message)
    }

    fun updateAppoitmentProposal(userId: String, appointmentId: String, newDate: String, newTime: String) {
        ConversacionesMock.actualizarPropuestaCita(userId, appointmentId, newDate, newTime)
    }

    fun updateAppointmenStatus(userId: String, appointmentId: String, newStatus: Message.AppointmentProposalStatus,
                               rejectionReason: String? = null){
        ConversacionesMock.actualizarEstadoPropuesta(userId, appointmentId, newStatus, rejectionReason)
    }
**/
}