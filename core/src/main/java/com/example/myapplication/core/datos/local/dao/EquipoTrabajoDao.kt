package com.example.myapplication.core.datos.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.core.datos.local.entidades.EquipoTrabajoEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE EQUIPO DE TRABAJO ---
 */
@Dao
interface EquipoTrabajoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(equipo: EquipoTrabajoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(lista: List<EquipoTrabajoEntity>)

    @Query("SELECT * FROM equipo_trabajo WHERE id = :id")
    fun obtenerPorId(id: String): Flow<EquipoTrabajoEntity?>

    @Query("SELECT * FROM equipo_trabajo WHERE idPropietario = :idPropietario")
    fun obtenerPorPropietario(idPropietario: String): Flow<List<EquipoTrabajoEntity>>

    @Query("SELECT * FROM equipo_trabajo WHERE idSucursal = :idSucursal")
    fun obtenerPorSucursal(idSucursal: String): Flow<List<EquipoTrabajoEntity>>

    @Query("SELECT COUNT(*) FROM equipo_trabajo WHERE idPropietario = :idPropietario")
    suspend fun obtenerConteoPorPropietario(idPropietario: String): Int

    @Query("DELETE FROM equipo_trabajo WHERE idPropietario = :idPropietario")
    suspend fun eliminarPorPropietario(idPropietario: String)

    @Query("DELETE FROM equipo_trabajo WHERE idSucursal = :idSucursal")
    suspend fun eliminarPorSucursal(idSucursal: String)

    @Query("DELETE FROM equipo_trabajo")
    suspend fun eliminarTodo()

    @Query("DELETE FROM equipo_trabajo WHERE id = :id")
    suspend fun eliminarPorId(id: String)
}
