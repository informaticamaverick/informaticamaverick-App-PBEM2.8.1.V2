package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.dao.SucursalDao
import com.example.myapplication.prestador.data.local.entity.SucursalEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Deprecated("Use ProviderRepository as SSOT")
@Singleton
class SucursalRepository @Inject constructor(
    // private val sucursalDao: SucursalDao
) {
    /*
    suspend fun saveSucursal(sucursal: SucursalEntity) {
        sucursalDao.insertSucursal(sucursal)
    }

    suspend fun saveSucursales(sucursales: List<SucursalEntity>) {
        sucursalDao.insertSucursales(sucursales)
    }

    suspend fun updateSucursal(sucursal: SucursalEntity) {
        sucursalDao.updateSucursal(sucursal)
    }

    suspend fun deleteSucursal(sucursalId: String) {
        sucursalDao.deleteSucursalById(sucursalId)
    }

    suspend fun deleteAllSucursales() {
        sucursalDao.deleteAllSucursales()
    }

    fun getSucursalById(sucursalId: String): Flow<SucursalEntity?> {
        return sucursalDao.getSucursalByIdFlow(sucursalId)
    }

    fun getAllSucursales(): Flow<List<SucursalEntity>> {
        return sucursalDao.getAllSucursales()
    }

    fun getSucursalesByBusiness(businessId: String): Flow<List<SucursalEntity>> {
        return sucursalDao.getSucursalesByBusiness(businessId)
    }

    fun getActiveSucursales(businessId: String): Flow<List<SucursalEntity>> {
        return sucursalDao.getActiveSucursales(businessId)
    }

    fun searchSucursalesByName(name: String): Flow<List<SucursalEntity>> {
        return sucursalDao.searchSucursalesByName("%$name%")
    }

    suspend fun updateSucursalStatus(sucursalId: String, isActive: Boolean) {
        sucursalDao.updateSucursalStatus(sucursalId, isActive, System.currentTimeMillis())
    }

    suspend fun updateSucursalDireccion(sucursalId: String, direccionId: String) {
        sucursalDao.updateSucursalDireccion(sucursalId, direccionId, System.currentTimeMillis())
    }

    suspend fun sucursalExists(sucursalId: String): Boolean {
        return sucursalDao.sucursalExists(sucursalId)
    }

    suspend fun countSucursalesByBusiness(businessId: String): Int {
        return sucursalDao.countSucursalesByBusiness(businessId)
    }

    suspend fun countActiveSucursales(businessId: String): Int {
        return sucursalDao.countActiveSucursales(businessId)
    }

    suspend fun updateSucursalTimestamp(sucursalId: String, timestamp: Long) {
        sucursalDao.updateSucursalTimestamp(sucursalId, timestamp)
    }
    */
}