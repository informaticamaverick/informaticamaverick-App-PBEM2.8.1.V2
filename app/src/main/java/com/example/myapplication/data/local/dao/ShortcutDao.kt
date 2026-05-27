package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.ShortcutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM shortcuts WHERE context = :context ORDER BY timestamp DESC")
    fun getShortcutsByContext(context: String): Flow<List<ShortcutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: ShortcutEntity)

    @Query("DELETE FROM shortcuts WHERE context = :context AND targetId = :targetId")
    suspend fun deleteShortcut(context: String, targetId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM shortcuts WHERE context = :context AND targetId = :targetId)")
    suspend fun exists(context: String, targetId: String): Boolean
}
