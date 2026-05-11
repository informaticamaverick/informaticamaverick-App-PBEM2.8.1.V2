package com.example.myapplication.prestador.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.prestador.data.local.entity.BlockedDateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedDateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(date: BlockedDateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dates: List<BlockedDateEntity>)

    @Delete
    suspend fun delete(date: BlockedDateEntity)

    @Query("DELETE FROM blocked_dates WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM blocked_dates WHERE providerId = :providerId AND isActive = 1 ORDER BY date ASC")
    fun getActiveByProvider(providerId: String): Flow<List<BlockedDateEntity>>

    @Query("SELECT * FROM blocked_dates WHERE providerId = :providerId ORDER BY date ASC")
    fun getAllByProvider(providerId: String): Flow<List<BlockedDateEntity>>

    @Query("SELECT * FROM blocked_dates WHERE providerId = :providerId AND date = :date LIMIT 1")
    suspend fun getByProviderAndDate(providerId: String, date: String): BlockedDateEntity?
}
