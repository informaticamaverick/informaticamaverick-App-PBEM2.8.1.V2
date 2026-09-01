package com.example.myapplication.core.datos.local.dao

import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.SucursalEntity
import com.example.myapplication.core.datos.local.relaciones.SucursalCompletaRelacionesBD
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO PARA SUCURSALES (POS) ---
 */
@Dao
interface SucursalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSucursal(sucursal: SucursalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(sucursales: List<SucursalEntity>)

    @Query("SELECT * FROM sucursales WHERE id = :id")
    fun obtenerPorId(id: String): Flow<SucursalEntity?>

    @Query("SELECT * FROM sucursales WHERE id = :id")
    suspend fun obtenerPorIdSync(id: String): SucursalEntity?

    @Query("SELECT * FROM sucursales WHERE idEmpresaPadre = :idEmpresa")
    fun obtenerPorEmpresa(idEmpresa: String): Flow<List<SucursalEntity>>

    @Query("SELECT * FROM sucursales WHERE idEmpresaPadre = :idEmpresa")
    suspend fun obtenerPorEmpresaSync(idEmpresa: String): List<SucursalEntity>

    @Query("SELECT * FROM sucursales WHERE idPropietario = :uid")
    fun obtenerPorPropietario(uid: String): Flow<List<SucursalEntity>>

    @Query("SELECT * FROM sucursales WHERE idPropietario = :uid")
    suspend fun obtenerPorPropietarioSync(uid: String): List<SucursalEntity>

    @Query("DELETE FROM sucursales WHERE id = :id")
    suspend fun eliminarPorId(id: String)

    @Query("DELETE FROM sucursales WHERE idPropietario = :uid")
    suspend fun eliminarPorPropietario(uid: String)

    @Query("DELETE FROM sucursales WHERE idEmpresaPadre = :idEmpresa")
    suspend fun eliminarPorEmpresa(idEmpresa: String)

    @Transaction
    @Query("SELECT * FROM sucursales WHERE id = :id")
    fun obtenerSucursalCompleta(id: String): Flow<SucursalCompletaRelacionesBD?>

    @Transaction
    @Query("SELECT * FROM sucursales WHERE idEmpresaPadre = :idEmpresa")
    fun obtenerSucursalesCompletasPorEmpresa(idEmpresa: String): Flow<List<SucursalCompletaRelacionesBD>>
}
