package com.example.myapplication.prestador.data.local.dao

import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.SucursalEntity
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow

/**
 * DAO para la tabla de Sucursales
 */
@Dao
interface  SucursalDao {
    /**
     * INSERTAT una nueva sucursal
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSucursal(sucursal: SucursalEntity)

    /**
     * INSERTAR multiples sucursales
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSucursales(sucursales: List<SucursalEntity>)

    /**
     * ACTUALIXAR sucursa existente
     */
    @Update
    suspend fun updateSucursal(sucursal: SucursalEntity)

    /**
     *ELIMINAR sucursal
     */
    @Delete
    suspend fun deleteSucursal(sucursal: SucursalEntity)

    /**
     * ELIMINAR sucursal por ID
     */
    @Query("DELETE FROM sucursales WHERE id = :sucursalId")
    suspend fun deleteSucursalById(sucursalId: String)

    /**
     * OBTENER todas las sucursales de un negocio
     */
    @Query("SELECT * FROM sucursales WHERE businessId = :businessId ORDER BY nombre ASC")
    fun getSucursalesByBusiness(businessId: String): Flow<List<SucursalEntity>>

    /**
     * OBTENER todas las sucursales de un negocio (una vez)
     */
    @Query("SELECT * FROM sucursales WHERE businessId = :businessId ORDER BY nombre ASC")
    suspend fun getSucursalesByBusinessOnce(businessId: String): List<SucursalEntity>

    /**
     * OBTENER sucursal por ID
     */
    @Query("SELECT * FROM sucursales WHERE id = :sucursalId")
    suspend fun getSucursalById(sucursalId: String): SucursalEntity?
    
    /**
     * OBTENER sucursal por ID (observando cambios)
     */
    @Query("SELECT * FROM sucursales WHERE id = :sucursalId")
    fun getSucursalByIdFlow(sucursalId: String): Flow<SucursalEntity?>

    /**
     * OBTENER solo sucursales activas
     */
    @Query("SELECT * FROM sucursales WHERE businessId = :businessId AND isActive = 1 ORDER BY nombre ASC")
    fun getActiveSucursales(businessId: String): Flow<List<SucursalEntity>>

    /**
     * CONTAR sucursales de UN NEGOCIO
     */
    @Query("SELECT COUNT(*) FROM sucursales WHERE businessId = :businessId")
    suspend fun countSucursales(businessId: String): Int

    /**
     * ACTIVAR/DESACTIVAR sucursal
     */
    @Query("UPDATE sucursales SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :sucursalId")
    suspend fun updateSucursalStatus(sucursalId: String, isActive: Boolean, updatedAt: Long)

    /**
     * ELIMINAR todfas las sucursales de un negocio
     */
    @Query("DELETE FROM sucursales WHERE businessId = :businessId")
    suspend fun deleteAllSucursalesByBusiness(businessId: String)

    /**
     * ELIMINAR todas las sucursales
     */
    @Query("DELETE FROM sucursales")
    suspend fun deleteAllSucursales()

    /**
     * OBTENER todas las sucursales
     */
    @Query("SELECT * FROM sucursales ORDER BY nombre ASC")
    fun getAllSucursales(): Flow<List<SucursalEntity>>

    /**
     * BUSCAR sucursales por nombre
     */
    @Query("SELECT * FROM sucursales WHERE nombre LIKE :name ORDER BY nombre ASC")
    fun searchSucursalesByName(name: String): Flow<List<SucursalEntity>>

    /**
     * ACTUALIZAR direccion de sucursal (via DireccionEntity)
     */
    @Query("UPDATE sucursales SET direccionId = :direccionId, updatedAt = :updatedAt WHERE id = :sucursalId")
    suspend fun updateSucursalDireccion(sucursalId: String, direccionId: String, updatedAt: Long)

    /**
     * VERIFICAR si existe una sucursal
     */
    @Query("SELECT EXISTS(SELECT 1 FROM sucursales WHERE id = :sucursalId)")
    suspend fun sucursalExists(sucursalId: String): Boolean

    /**
     * CONTAR sucursales por negocio
     */
    @Query("SELECT COUNT(*) FROM sucursales WHERE businessId = :businessId")
    suspend fun countSucursalesByBusiness(businessId: String): Int

    /**
     * CONTAR sucursales activas
     */
    @Query("SELECT COUNT(*) FROM sucursales WHERE businessId = :businessId AND isActive = 1")
    suspend fun countActiveSucursales(businessId: String): Int

    /**
     * ACTUALIZAR timestamp
     */
    @Query("UPDATE sucursales SET updatedAt = :timestamp WHERE id = :sucursalId")
    suspend fun updateSucursalTimestamp(sucursalId: String, timestamp: Long)

}