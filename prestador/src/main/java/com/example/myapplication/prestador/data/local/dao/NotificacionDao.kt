package com.example.myapplication.prestador.data.local.dao

import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.NotificacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificacionDao {

    @Query("SELECT * FROM notificaciones ORDER BY fechaMs DESC")
    fun getAllFlow():
            Flow<List<NotificacionEntity>>
    @Query("SELECT * FROM notificaciones WHERE leida = 0 ORDER BY fechaMs DESC")
        fun getUnreadFlow():
                Flow<List<NotificacionEntity>>

        @Query("SELECT * FROM notificaciones WHERE tipo = :tipo ORDER BY fechaMs DESC")
            fun getByTipoFlow(tipo: String):
                    Flow<List<NotificacionEntity>>

            @Query("SELECT COUNT(*) FROM notificaciones WHERE leida = 0")
                fun getUnreadCount(): Flow<Int>

                @Insert(onConflict = OnConflictStrategy.REPLACE)
                suspend fun insert(entity: NotificacionEntity): Long

                @Query("UPDATE notificaciones SET leida = 1 WHERE id = :id")
                            suspend fun marcarLeida(id: Long)

                    @Query("UPDATE notificaciones SET leida = 1")
                                suspend fun marcarTodasLeidas()

                        @Query("DELETE FROM notificaciones WHERE id = :id")
                                suspend fun deleteById(id: Long)

                            @Query("DELETE FROM notificaciones")
                            suspend fun deleteAll()
                            @Query("SELECT COUNT(*) FROM notificaciones")
                            suspend fun getTotalCount(): Int
}
