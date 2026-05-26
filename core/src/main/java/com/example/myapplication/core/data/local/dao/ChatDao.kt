package com.example.myapplication.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.core.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE CHAT (COMPARTIDO) ---
 * Centraliza todas las consultas a la base de datos local relativas a la mensajería.
 * Al estar en el módulo :core, asegura que ambas apps guarden y consulten los
 * mensajes con la misma lógica.
 */
@Dao
interface ChatDao {
    
    // --- CONSULTAS DE MENSAJES ---

    /**
     * Obtiene el flujo de mensajes de una conversación específica.
     */
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId IN (:chatIds) ORDER BY timestamp ASC")
    fun getMessagesForChats(chatIds: List<String>): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMessages(messages: List<MessageEntity>)

    // --- GESTIÓN DE ESTADO (LEÍDO/SINCRONIZADO) ---

    @Query("UPDATE messages SET isRead = 1 WHERE chatId = :chatId AND receiverId = :myUserId AND isRead = 0")
    suspend fun markChatAsRead(chatId: String, myUserId: String)

    @Query("UPDATE messages SET appointmentStatus = :status WHERE id = :messageId")
    suspend fun updateAppointmentStatus(messageId: String, status: String)

    @Query("UPDATE messages SET isRead = :isRead WHERE id = :messageId")
    suspend fun updateMessageIsRead(messageId: String, isRead: Boolean)

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("UPDATE messages SET isSynced = 1 WHERE id = :messageId")
    suspend fun updateMessageSynced(messageId: String)

    // --- CONSULTAS DE RESUMEN Y CONTADORES ---

    @Query("SELECT COUNT(*) FROM messages WHERE receiverId = :myUserId AND isRead = 0")
    fun getTotalUnreadCount(myUserId: String): Flow<Int>

    @Query("SELECT chatId, COUNT(*) as count FROM messages WHERE receiverId = :myUserId AND isRead = 0 GROUP BY chatId")
    fun getUnreadCountsPerChat(myUserId: String): Flow<List<ChatUnreadCount>>

    @Query("""
        SELECT m1.chatId, 
               CASE WHEN m1.senderId = :myUserId THEN m1.receiverId ELSE m1.senderId END as userId,
               (SELECT MAX(companyId) FROM messages m3 WHERE m3.chatId = m1.chatId) as companyId,
               (SELECT MAX(categoryId) FROM messages m4 WHERE m4.chatId = m1.chatId) as categoryId,
               m1.content as lastMessage, m1.timestamp as lastTimestamp
        FROM messages m1
        WHERE (m1.senderId = :myUserId OR m1.receiverId = :myUserId)
        AND m1.timestamp = (SELECT MAX(m2.timestamp) FROM messages m2 WHERE m2.chatId = m1.chatId)
        GROUP BY m1.chatId
        ORDER BY lastTimestamp DESC
    """)
    fun getActiveChatSummaries(myUserId: String): Flow<List<ChatSummary>>

    // --- ELIMINACIÓN ---

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesByChatId(chatId: String)
}

// --- CLASES DE APOYO PARA RESULTADOS COMPUESTOS ---

data class ChatUnreadCount(
    val chatId: String,
    val count: Int
)

data class ChatSummary(
    val chatId: String,
    val userId: String,
    val companyId: String?,
    val categoryId: String?,
    val lastMessage: String,
    val lastTimestamp: Long
)
