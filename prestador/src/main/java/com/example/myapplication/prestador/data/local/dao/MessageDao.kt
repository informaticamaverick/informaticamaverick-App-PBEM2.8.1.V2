/**
package com.example.myapplication.prestador.data.local.dao

import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.MessageEntity
import com.example.myapplication.prestador.data.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * --- OBSOLETO (MAVERICK ELITE v4.0) ---
 * Este DAO ha sido inactivado en favor de com.example.myapplication.core.data.local.dao.ChatDao.
 * Se mantiene comentado por referencia histórica según auditoría.
 * Todas las inyecciones ahora deben apuntar al ChatDao del módulo :core.
 */
/*
@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?

    @Query("UPDATE messages SET isRead = 1 WHERE chatId = :chatId AND senderId != :myUserId")
    suspend fun markChatAsRead(chatId: String, myUserId: String)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesByChatId(chatId: String)
}
*/
*/
