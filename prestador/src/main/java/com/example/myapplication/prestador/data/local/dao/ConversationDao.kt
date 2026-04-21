package com.example.myapplication.prestador.data.local.dao

import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de conversaciones
 */
@Dao
interface ConversationDao {

    //INSERTAR
    @Insert(onConflict = OnConflictStrategy.REPLACE)

    suspend fun insertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationEntity>)

    //CONSULTAR
    @Query("SELECT * FROM conversations ORDER BY lastMessageTimestamp DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations ORDER BY lastMessageTimestamp DESC")
    suspend fun  getAllConversationsSync(): List<ConversationEntity>

    @Query("SELECT * FROM conversations WHERE conversationId = :conversationId")
    suspend fun getConversationById(conversationId: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE userId = :userId")
    suspend fun getConversationByUserId(userId: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE conversationId = :conversationId")
    fun getConversationByIdFlow(conversationId: String): Flow<ConversationEntity?>

    //FILTROS
    @Query("SELECT * FROM conversations WHERE isArchived = 0 ORDER BY lastMessageTimestamp DESC")
    fun getActiveConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isArchived = 1 ORDER BY lastMessageTimestamp DESC")
    fun getArchivedConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isPinned = 1 ORDER BY lastMessageTimestamp DESC")
    fun getPinnedConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE unreadCount > 0 ORDER BY lastMessageTimestamp DESC")
    fun getUnreadConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE notificationsEnabled = 1 ORDER BY lastMessageTimestamp DESC")
    fun getConversationsWithNotifications(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isVisible = 1 ORDER BY lastMessageTimestamp DESC")
    fun getVisibleConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isLocked = 1 ORDER BY lastMessageTimestamp DESC")
    fun getLockedConversations(): Flow<List<ConversationEntity>>

    //BUSQUEDA
    @Query("SELECT * FROM conversations WHERE userName LIKE '%' || :query || '%' OR lastMessage LIKE '%' || :query || '%' ORDER BY lastMessageTimestamp DESC")
    suspend fun searchConversations(query: String): List<ConversationEntity>

    //ACTUALIZAR
    @Update suspend fun updateConversation(conversation: ConversationEntity)

    @Query("UPDATE conversations SET lastMessage = :lastMessage, lastMessageTimestamp = :timestamp, lastMessageType = :messageType, totalMessages = totalMessages + 1, updatedAt = :timestamp WHERE conversationId = :conversationId")
    suspend fun updateLastMessage(conversationId: String, lastMessage: String, timestamp: Long, messageType: String)

    @Query("UPDATE conversations SET unreadCount = unreadCount + 1 WHERE conversationId = :conversationId")
    suspend fun incrementUnreadCount(conversationId: String)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE conversationId = :conversationId")
    suspend fun resetUnreadCount(conversationId: String)

    @Query("UPDATE conversations SET isPinned = :isPinned WHERE conversationId = :conversationId")
    suspend fun setPinned(conversationId: String, isPinned: Boolean)

    @Query("UPDATE conversations SET isMuted = :isMuted WHERE conversationId = :conversationId")
    suspend fun  setMuted(conversationId: String, isMuted: Boolean)

    @Query("UPDATE conversations SET isArchived = :isArchived WHERE conversationId = :conversationId")
    suspend fun setArchived(conversationId: String, isArchived: Boolean)

    @Query("UPDATE conversations SET isBlocked = :isBlocked WHERE conversationId = :conversationId")
    suspend fun setBlocked(conversationId: String, isBlocked: Boolean)

    @Query("UPDATE conversations SET notificationsEnabled = :enabled WHERE conversationId = :conversationId")
    suspend fun setNotificationsEnabled(conversationId: String, enabled: Boolean)

    @Query("UPDATE conversations SET isVisible = :isVisible WHERE conversationId = :conversationId")
    suspend fun setVisible(conversationId: String, isVisible: Boolean)

    @Query("UPDATE conversations SET  isLocked = :isLocked, lockPassword = :password WHERE conversationId = :conversationId")
    suspend fun setLocked(conversationId: String, isLocked: Boolean, password: String?)

    @Query("UPDATE conversations SET isOnline = :isOnline WHERE conversationId = :conversationId")
    suspend fun setOnlineStatus(conversationId: String, isOnline: Boolean)

    @Query("UPDATE conversations SET isSynced = 1 WHERE conversationId = :conversationId")
    suspend fun markAsSynced(conversationId: String)

    //ELIMINAR
    @Delete
    suspend fun deleteConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE conversationId = :conversationId")
    suspend fun deleteConversationById(conversationId: String)

    @Query("DELETE FROM conversations")
    suspend fun deleteAllConversations()

    //ESTADISTICAS
    @Query("SELECT COUNT(*) FROM conversations")
    suspend fun getConversationCount(): Int

    @Query("SELECT SUM(unreadCount) FROM conversations")
    suspend fun getTotalUnreadCount(): Int

    @Query("SELECT SUM(unreadCount) FROM conversations")
    fun getTotalUnreadCountFlow(): Flow<Int>
}