package com.example.myapplication.core.datos.local.dao

import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.ExcepcionHorariaEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO PARA EXCEPCIONES HORARIAS ---
 */
@Dao
interface ExcepcionHorariaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(excepcion: ExcepcionHorariaEntity)

    @Query("SELECT * FROM excepciones_horarias WHERE idReferencia = :idRef")
    fun obtenerPorReferencia(idRef: String): Flow<List<ExcepcionHorariaEntity>>

    @Query("DELETE FROM excepciones_horarias WHERE id = :id")
    suspend fun eliminarPorId(id: String)
}
