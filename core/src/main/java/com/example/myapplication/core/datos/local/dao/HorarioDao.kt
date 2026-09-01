package com.example.myapplication.core.datos.local.dao

import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.HorarioEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO PARA GESTIÓN DE HORARIOS Y DISPONIBILIDAD ---
 */
@Dao
interface HorarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(horario: HorarioEntity)

    @Query("SELECT * FROM horarios WHERE idReferencia = :idReferencia")
    fun obtenerPorReferencia(idReferencia: String): Flow<HorarioEntity?>

    @Query("SELECT * FROM horarios WHERE idReferencia = :idReferencia")
    suspend fun obtenerPorReferenciaSync(idReferencia: String): HorarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(horarios: List<HorarioEntity>)

    @Query("SELECT * FROM horarios WHERE idReferenciaPadre = :idSucursal AND tipo = 'Horario_Disponibilidad'")
    fun obtenerHorariosSucursal(idSucursal: String): Flow<List<HorarioEntity>>

    @Query("SELECT * FROM horarios WHERE idReferenciaPadre = :idSucursal AND tipo = 'Horario_Disponibilidad'")
    suspend fun obtenerHorariosSucursalSync(idSucursal: String): List<HorarioEntity>

    @Query("DELETE FROM horarios WHERE idReferencia = :idRef")
    suspend fun eliminarPorReferencia(idRef: String)

    @Query("DELETE FROM horarios WHERE idSucursal = :idSucursal")
    suspend fun eliminarPorSucursal(idSucursal: String)

    @Query("DELETE FROM horarios")
    suspend fun eliminarTodo()
}
