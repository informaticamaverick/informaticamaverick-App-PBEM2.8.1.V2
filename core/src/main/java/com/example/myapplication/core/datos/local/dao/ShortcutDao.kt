package com.example.myapplication.core.datos.local.dao

import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.ShortcutEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- SHORTCUT DAO (SSOT 2026) ---
 * [LEY #9]: Estándar Mav (Idioma Español).
 */
@Dao
interface ShortcutDao {
    @Query("SELECT * FROM shortcuts WHERE contexto = :contexto ORDER BY marcaTiempo DESC")
    fun obtenerShortcutsPorContexto(contexto: String): Flow<List<ShortcutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarShortcut(shortcut: ShortcutEntity)

    @Query("DELETE FROM shortcuts WHERE contexto = :contexto AND idDestino = :idDestino")
    suspend fun eliminarShortcut(contexto: String, idDestino: String)

    @Query("SELECT EXISTS(SELECT 1 FROM shortcuts WHERE contexto = :contexto AND idDestino = :idDestino)")
    suspend fun existe(contexto: String, idDestino: String): Boolean

    @Query("DELETE FROM shortcuts WHERE contexto = :contexto AND tipo = :tipo")
    suspend fun eliminarShortcutsPorContextoYTipo(contexto: String, tipo: String)
}

































