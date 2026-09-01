package com.example.myapplication.core.datos.local.dao

import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.TelemetryEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE TELEMETRÍA (Ley #7 - Auditoría) ---
 */
@Dao
interface TelemetryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrarEvento(evento: TelemetryEntity)

    @Query("SELECT * FROM telemetry WHERE isSynced = 0")
    suspend fun obtenerPendientes(): List<TelemetryEntity>

    @Query("UPDATE telemetry SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun marcarComoSincronizados(ids: List<String>)

    @Query("DELETE FROM telemetry WHERE isSynced = 1 AND timestamp < :limite")
    suspend fun limpiarSincronizados(limite: Long)
}

































