package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.local.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    // Escucha en tiempo real los mensajes de un chat ordenados por fecha
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId IN (:chatIds) ORDER BY timestamp ASC")
    fun getMessagesForChats(chatIds: List<String>): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    // Marcar todos los mensajes de una conversación como leídos
    @Query("UPDATE messages SET isRead = 1 WHERE chatId = :chatId AND receiverId = :myUserId AND isRead = 0")
    suspend fun markChatAsRead(chatId: String, myUserId: String)

    @Query("UPDATE messages SET isRead = 1 WHERE chatId IN (:chatIds) AND receiverId = :myUserId AND isRead = 0")
    suspend fun markChatsAsRead(chatIds: List<String>, myUserId: String)

    // Contar total de mensajes no leídos para el usuario actual
    @Query("SELECT COUNT(*) FROM messages WHERE receiverId = :myUserId AND isRead = 0")
    fun getTotalUnreadCount(myUserId: String): Flow<Int>

    // Obtener un mapa de chatId -> cantidad de no leídos
    // [DEBUG] Corregido: Solo contar mensajes donde el usuario es receptor y no es el autor (aunque receiverId ya filtra eso habitualmente)
    @Query("SELECT chatId, COUNT(*) as count FROM messages WHERE receiverId = :myUserId AND isRead = 0 GROUP BY chatId")
    fun getUnreadCountsPerChat(myUserId: String): Flow<List<ChatUnreadCount>>

    // Obtener IDs de usuarios con los que tengo chats (ordenados por el mensaje más reciente)
    @Query("""
        SELECT userId FROM (
            SELECT receiverId as userId, MAX(timestamp) as lastMsg FROM messages WHERE senderId = :myUserId GROUP BY receiverId
            UNION
            SELECT senderId as userId, MAX(timestamp) as lastMsg FROM messages WHERE receiverId = :myUserId GROUP BY senderId
        ) GROUP BY userId ORDER BY MAX(lastMsg) DESC
    """)
    fun getActiveConversationIds(myUserId: String): Flow<List<String>>

    // Obtener un resumen de las conversaciones activas (ordenadas por el mensaje más reciente)
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMessages(messages: List<MessageEntity>)

    @Query("SELECT COUNT(*) > 0 FROM messages WHERE id = :id")
    suspend fun messageExists(id: String): Boolean

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: String): MessageEntity?

    @Query("""SELECT m1.chatId, m1.content as lastMessage, m1.timestamp as lastTimestamp, m1.senderId as lastSenderId
        FROM messages m1 WHERE (m1.senderId = :myUserId OR m1.receiverId = :myUserId)
        AND m1.timestamp = (SELECT MAX(m2.timestamp) FROM messages m2 WHERE m2.chatId = m1.chatId)
        GROUP BY m1.chatId""")
    fun getLastMessagePerChat(myUserId: String): Flow<List<ChatLastMessage>>

    @Query("UPDATE messages SET appointmentStatus = :status WHERE id = :messageId")
    suspend fun updateAppointmentStatus(messageId: String, status: String)

    @Query("UPDATE messages SET isRead = :isRead WHERE id = :messageId")
    suspend fun updateMessageIsRead(messageId: String, isRead: Boolean)

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("SELECT id FROM messages WHERE chatId = :chatId AND receiverId = :myUserId AND isRead = 0")
    suspend fun getUnreadMessageIds(chatId: String, myUserId: String): List<String>

    @Query("UPDATE messages SET isSynced = 1 WHERE id = :messageId")
    suspend fun updateMessageSynced(messageId: String)

    // --- ELIMINACIÓN DE CONVERSACIONES (POLÍTICA ZERO COST) ---
    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesByChatId(chatId: String)

    @Query("DELETE FROM messages WHERE chatId IN (:chatIds)")
    suspend fun deleteMessagesByChatIds(chatIds: List<String>)
}

// Clase de apoyo para el resultado del GROUP BY
data class ChatUnreadCount(
    val chatId: String,
    val count: Int
)

data class ChatLastMessage(
    val chatId: String,
    val lastMessage: String,
    val lastTimestamp: Long,
    val lastSenderId: String
)

data class ChatSummary(
    val chatId: String,
    val userId: String,
    val companyId: String?,
    val categoryId: String?,
    val lastMessage: String,
    val lastTimestamp: Long
)

