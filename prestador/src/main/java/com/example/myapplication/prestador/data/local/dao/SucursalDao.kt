package com.example.myapplication.prestador.data.local.dao

/*
 * ARCHIVO EN DESUSO
 * Motivo: Se ha centralizado la fuente de verdad en los modelos del módulo :core.
 * Se recomienda usar el repositorio central com.example.myapplication.core.data.repository.ProviderRepository
 * y gestionar la persistencia a través de com.example.myapplication.core.data.local.dao.ProviderDao.
 */

/*
import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.SucursalEntity
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow

@Dao
interface  SucursalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSucursal(sucursal: SucursalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSucursales(sucursales: List<SucursalEntity>)

    @Update
    suspend fun updateSucursal(sucursal: SucursalEntity)

    @Delete
    suspend fun deleteSucursal(sucursal: SucursalEntity)

    @Query("DELETE FROM sucursales WHERE id = :sucursalId")
    suspend fun deleteSucursalById(sucursalId: String)

    @Query("SELECT * FROM sucursales WHERE businessId = :businessId ORDER BY nombre ASC")
    fun getSucursalesByBusiness(businessId: String): Flow<List<SucursalEntity>>

    @Query("SELECT * FROM sucursales WHERE businessId = :businessId ORDER BY nombre ASC")
    suspend fun getSucursalesByBusinessOnce(businessId: String): List<SucursalEntity>

    @Query("SELECT * FROM sucursales WHERE id = :sucursalId")
    suspend fun getSucursalById(sucursalId: String): SucursalEntity?
    
    @Query("SELECT * FROM sucursales WHERE id = :sucursalId")
    fun getSucursalByIdFlow(sucursalId: String): Flow<SucursalEntity?>

    @Query("SELECT * FROM sucursales WHERE businessId = :businessId AND isActive = 1 ORDER BY nombre ASC")
    fun getActiveSucursales(businessId: String): Flow<List<SucursalEntity>>

    @Query("SELECT COUNT(*) FROM sucursales WHERE businessId = :businessId")
    suspend fun countSucursales(businessId: String): Int

    @Query("UPDATE sucursales SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :sucursalId")
    suspend fun updateSucursalStatus(sucursalId: String, isActive: Boolean, updatedAt: Long)

    @Query("DELETE FROM sucursales WHERE businessId = :businessId")
    suspend fun deleteAllSucursalesByBusiness(businessId: String)

    @Query("DELETE FROM sucursales")
    suspend fun deleteAllSucursales()

    @Query("SELECT * FROM sucursales ORDER BY nombre ASC")
    fun getAllSucursales(): Flow<List<SucursalEntity>>

    @Query("SELECT * FROM sucursales WHERE nombre LIKE :name ORDER BY nombre ASC")
    fun searchSucursalesByName(name: String): Flow<List<SucursalEntity>>

    @Query("UPDATE sucursales SET direccionId = :direccionId, updatedAt = :updatedAt WHERE id = :sucursalId")
    suspend fun updateSucursalDireccion(sucursalId: String, direccionId: String, updatedAt: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM sucursales WHERE id = :sucursalId)")
    suspend fun sucursalExists(sucursalId: String): Boolean

    @Query("SELECT COUNT(*) FROM sucursales WHERE businessId = :businessId")
    suspend fun countSucursalesByBusiness(businessId: String): Int

    @Query("SELECT COUNT(*) FROM sucursales WHERE businessId = :businessId AND isActive = 1")
    suspend fun countActiveSucursales(businessId: String): Int

    @Query("UPDATE sucursales SET updatedAt = :timestamp WHERE id = :sucursalId")
    suspend fun updateSucursalTimestamp(sucursalId: String, timestamp: Long)

}
*/
