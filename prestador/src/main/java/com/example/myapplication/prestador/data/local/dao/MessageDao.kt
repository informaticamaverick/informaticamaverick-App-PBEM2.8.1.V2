package com.example.myapplication.prestador.data.local.dao

import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.MessageEntity
import com.example.myapplication.prestador.data.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de mensajes
 */

@Dao
interface MessageDao {
    //INSERTAR
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMesages(messages: List<MessageEntity>)

    //CONSULTAR
   @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesByConversation(conversationId: String): Flow<List<MessageEntity>>

    @Query(" SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMesagesByConversationSync(conversationId: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE messageId = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND isFromCurrentUser = 0 AND isRead = 0")
    suspend fun getUnreadMessages(conversationId: String): List<MessageEntity>

    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId AND isFromCurrentUser = 0 AND isRead = 0")
    fun getUnreadCount(conversationId: String): Flow<Int>

    @Query("SELECT * FROM messages WHERE isSynced = 0")
    suspend fun getUnsyncedMessages(): List<MessageEntity>

    //ACTUALIZAR
    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET isRead = 1 WHERE conversationId = :conversationId AND isFromCurrentUser = 0")
    suspend fun markAllAsRead(conversationId: String)

    @Query("UPDATE messages SET isRead = 1 WHERE messageId = :messageId")
    suspend fun markAsRead(messageId: String)

    @Query("UPDATE messages SET isSynced = 1 WHERE messageId = :messageId")
    suspend fun markAsSynced(messageId: String)

    @Query("UPDATE messages SET isDelivered = 1 WHERE messageId = :messageId")
    suspend fun marAsDelivered(messageId: String)
    
    @Query("UPDATE messages SET appointmentStatus = :status, rejectionReason = :reason WHERE messageId = :messageId")
    suspend fun updateAppointmentStatus(messageId: String, status: String, reason: String?)

    //ELIMINAR
    @Delete
    suspend fun  deleteMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE messageId = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteAllMessagesFromConversation(conversationId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    // ============ BÚSQUEDA ============
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND text LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchMessages(conversationId: String, query: String): List<MessageEntity>

    //ESTADISTICAS
    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId")
    suspend fun getMessageCount(conversationId: String): Int

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessage(conversationId: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE messageType = 'APPOINTMENT' AND isFromCurrentUser = 1 AND appointmentStatus IN ('ACCEPTED', 'REJECTED')")
    suspend fun getAcceptedAppointmentMessages(): List<MessageEntity>
}