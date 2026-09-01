package com.example.myapplication.core.datos.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.core.datos.local.entidades.RecursoEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO PARA RECURSOS (ESTABLECIMIENTOS / PROFESIONALES) ---
 */
@Dao
interface RecursoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(recurso: RecursoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(recursos: List<RecursoEntity>)

    @Query("SELECT * FROM recursos WHERE id = :id")
    fun obtenerPorId(id: String): Flow<RecursoEntity?>

    @Query("SELECT * FROM recursos WHERE id = :id")
    suspend fun obtenerPorIdSync(id: String): RecursoEntity?

    @Query("SELECT * FROM recursos WHERE idPropietario = :uid")
    fun obtenerPorPropietario(uid: String): Flow<List<RecursoEntity>>

    @Query("SELECT * FROM recursos WHERE idSucursal = :idSucursal")
    fun obtenerPorSucursal(idSucursal: String): Flow<List<RecursoEntity>>

    @Query("SELECT * FROM recursos WHERE idSucursal = :idSucursal")
    suspend fun obtenerPorSucursalSync(idSucursal: String): List<RecursoEntity>

    @Query("DELETE FROM recursos WHERE id = :id")
    suspend fun eliminarPorId(id: String)

    @Query("DELETE FROM recursos WHERE idPropietario = :uid")
    suspend fun eliminarPorPropietario(uid: String)

    @Query("DELETE FROM recursos WHERE idSucursal = :idSucursal")
    suspend fun eliminarPorSucursal(idSucursal: String)

    @Query("DELETE FROM recursos")
    suspend fun eliminarTodo()
}
