package com.example.myapplication.core.datos.local.dao

import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.NotificacionEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE NOTIFICACIONES ---
 */
@Dao
interface NotificacionDao {

    @Query("SELECT * FROM notificaciones ORDER BY fechaMs DESC")
    fun obtenerTodas(): Flow<List<NotificacionEntity>>

    @Query("SELECT * FROM notificaciones WHERE leida = 0 ORDER BY fechaMs DESC")
    fun obtenerNoLeidas(): Flow<List<NotificacionEntity>>

    @Query("SELECT * FROM notificaciones WHERE tipo = :tipo ORDER BY fechaMs DESC")
    fun obtenerPorTipo(tipo: com.example.myapplication.core.dominio.modelos.TipoNotificacion): Flow<List<NotificacionEntity>>

    @Query("SELECT COUNT(*) FROM notificaciones WHERE leida = 0")
    fun obtenerConteoNoLeidas(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(notificacion: NotificacionEntity)

    @Query("UPDATE notificaciones SET leida = 1 WHERE id = :id")
    suspend fun marcarComoLeida(id: Long)

    @Query("UPDATE notificaciones SET leida = 1")
    suspend fun marcarTodasComoLeidas()

    @Query("DELETE FROM notificaciones WHERE id = :id")
    suspend fun eliminarPorId(id: Long)

    @Query("DELETE FROM notificaciones")
    suspend fun eliminarTodas()

    @Query("SELECT COUNT(*) FROM notificaciones")
    suspend fun obtenerConteoTotal(): Int
}
