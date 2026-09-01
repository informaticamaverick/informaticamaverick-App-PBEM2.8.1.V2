package com.example.myapplication.core.datos.local.dao

import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.DireccionEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE DIRECCIONES (SSOT) ---
 */
@Dao
interface DireccionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(direccion: DireccionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(direcciones: List<DireccionEntity>)

    @Query("SELECT * FROM direcciones WHERE id = :id")
    fun obtenerPorId(id: String): Flow<DireccionEntity?>

    @Query("SELECT * FROM direcciones WHERE id = :id")
    suspend fun obtenerPorIdSync(id: String): DireccionEntity?

    @Query("SELECT * FROM direcciones WHERE idPropietario = :uid")
    fun obtenerPorPropietario(uid: String): Flow<List<DireccionEntity>>

    @Query("SELECT * FROM direcciones WHERE idPropietario = :uid")
    suspend fun obtenerPorPropietarioSync(uid: String): List<DireccionEntity>

    @Query("SELECT * FROM direcciones WHERE idSucursal = :sucId")
    fun obtenerPorSucursal(sucId: String): Flow<List<DireccionEntity>>

    @Query("SELECT * FROM direcciones WHERE idSucursal = :sucId")
    suspend fun obtenerPorSucursalSync(sucId: String): List<DireccionEntity>

    @Query("SELECT * FROM direcciones WHERE idReferencia = :refId")
    fun obtenerPorReferencia(refId: String): Flow<List<DireccionEntity>>

    @Query("SELECT * FROM direcciones WHERE idReferencia = :refId")
    suspend fun obtenerPorReferenciaSync(refId: String): List<DireccionEntity>

    @Query("DELETE FROM direcciones WHERE id = :id")
    suspend fun eliminarPorId(id: String)

    @Query("DELETE FROM direcciones WHERE idPropietario = :uid")
    suspend fun eliminarPorPropietario(uid: String)
}
