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
    
    // =========================================================================
    // === SECCIÓN: COMPARTIDA (CLIENTE Y PRESTADOR) ===
    // =========================================================================

    /**
     * Obtiene el flujo de mensajes de una conversación específica.
     */
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>

    /**
     * [ELITE v4] Obtiene mensajes paginados desde Room.
     * Se ordena descendente para Paging 3 (los más nuevos primero en la lista reversa).
     */
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC")
    fun getMessagesForChatPaging(chatId: String): androidx.paging.PagingSource<Int, MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMessages(messages: List<MessageEntity>)

    @Query("UPDATE messages SET isRead = :isRead WHERE id = :messageId")
    suspend fun updateMessageIsRead(messageId: String, isRead: Boolean)

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("UPDATE messages SET isSynced = 1 WHERE id = :messageId")
    suspend fun updateMessageSynced(messageId: String)

    @Query("SELECT MAX(timestamp) FROM messages WHERE chatId = :chatId")
    suspend fun getLastMessageTimestamp(chatId: String): Long?

    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?

    // =========================================================================
    // === SECCIÓN: APP CLIENTE ===
    // =========================================================================

    @Query("SELECT * FROM messages WHERE chatId IN (:chatIds) ORDER BY timestamp ASC")
    fun getMessagesForChats(chatIds: List<String>): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET isRead = 1 WHERE chatId = :chatId AND receiverId = :myUserId AND isRead = 0")
    suspend fun markChatAsRead(chatId: String, myUserId: String)

    @Query("SELECT COUNT(*) FROM messages WHERE receiverId = :myUserId AND isRead = 0")
    fun getTotalUnreadCount(myUserId: String): Flow<Int>

    @Query("SELECT chatId, COUNT(*) as count FROM messages WHERE receiverId = :myUserId AND isRead = 0 GROUP BY chatId")
    fun getUnreadCountsPerChat(myUserId: String): Flow<List<ChatUnreadCount>>

    // =========================================================================
    // === SECCIÓN: APP PRESTADOR ===
    // =========================================================================

    @Query("UPDATE messages SET appointmentStatus = :status WHERE id = :messageId")
    suspend fun updateAppointmentStatus(messageId: String, status: String)

    @Query("""
        SELECT 
            sub.chatId, 
            sub.userId, 
            -- 🔥 [ELITE v8.5] Normalización Atómica (none/empty -> NULL)
            NULLIF(NULLIF(sub.localCompanyId, 'none'), '') as companyId, 
            NULLIF(NULLIF(sub.localBranchId, 'none'), '') as branchId,
            NULLIF(NULLIF(sub.remoteBranchId, 'none'), '') as remoteBranchId,
            NULLIF(NULLIF(sub.remoteCompanyId, 'none'), '') as remoteCompanyId,
            sub.categoryId, 
            sub.lastMessage, 
            sub.lastTimestamp,
            COALESCE(p.displayName, u.displayName, 'Usuario') as userName,
            COALESCE(p.photoUrl, u.photoUrl) as userPhoto,
            COALESCE(p.isOnline, u.isOnline) as isOnline,
            COALESCE(p.isVerified, u.isVerified) as isVerified,
            (SELECT COUNT(*) FROM messages m_unread WHERE m_unread.chatId = sub.chatId AND m_unread.receiverId = :myUserId AND m_unread.isRead = 0) as unreadCount
        FROM (
            SELECT m1.chatId, 
                   CASE WHEN m1.senderId = :myUserId THEN m1.receiverId ELSE m1.senderId END as userId,
                   m1.localCompanyId, 
                   m1.localBranchId,
                   m1.remoteBranchId,
                   m1.remoteCompanyId,
                   m1.categoryId,
                   m1.content as lastMessage, m1.timestamp as lastTimestamp
            FROM messages m1
            INNER JOIN (
                SELECT chatId, MAX(timestamp) as maxTs
                FROM messages
                GROUP BY chatId
            ) m2 ON m1.chatId = m2.chatId AND m1.timestamp = m2.maxTs
            WHERE (m1.senderId = :myUserId OR m1.receiverId = :myUserId)
            GROUP BY m1.chatId
        ) sub
        LEFT JOIN provider_profile p ON p.id = sub.userId
        LEFT JOIN user_profile u ON u.id = sub.userId
        ORDER BY sub.lastTimestamp DESC
    """)
    fun getActiveChatSummaries(myUserId: String): Flow<List<ChatSummary>>

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
    val companyId: String?, // Local Company ID
    val branchId: String?,  // Local Branch ID
    val remoteBranchId: String? = null,
    val remoteCompanyId: String? = null,
    val categoryId: String?,
    val lastMessage: String,
    val lastTimestamp: Long,
    val userName: String?,
    val userPhoto: String?,
    val isOnline: Boolean?,
    val isVerified: Boolean?,
    val unreadCount: Int = 0
)
