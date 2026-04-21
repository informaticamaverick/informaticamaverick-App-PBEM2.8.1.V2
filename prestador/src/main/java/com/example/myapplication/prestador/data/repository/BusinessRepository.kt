package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.dao.BusinessDao
import com.example.myapplication.prestador.data.local.entity.BusinessEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Deprecated("Use ProviderRepository as SSOT")
@Singleton
class BusinessRepository @Inject constructor(
    // private val businessDao: BusinessDao
) {
    /*
    suspend fun saveBusiness(business: BusinessEntity) {
        businessDao.insertBusiness(business)
    }

    suspend fun saveBusinesses(businesses: List<BusinessEntity>) {
        businessDao.insertBusinesses(businesses)
    }

    suspend fun updateBusiness(business: BusinessEntity) {
        businessDao.updateBusiness(business)
    }

    suspend fun deleteBusiness(businessId: String) {
        businessDao.deleteBusinessById(businessId)
    }

    suspend fun deleteAllBusinesses() {
        businessDao.deleteAllBusinesses()
    }

    fun getBusinessById(businessId: String): Flow<BusinessEntity?> {
        return businessDao.getBusinessById(businessId)
    }

    fun getAllBusinesses(): Flow<List<BusinessEntity>> {
        return businessDao.getAllBusinesses()
    }

    fun getBusinessesByProvider(providerId: String): Flow<List<BusinessEntity>> {
        return businessDao.getBusinessesByProvider(providerId)
    }

    fun searchBusinessesByName(name: String): Flow<List<BusinessEntity>> {
        return businessDao.searchBusinessesByName("%$name%")
    }

    fun getBusinessByCuit(cuit: String): Flow<BusinessEntity?> {
        return businessDao.getBusinessByCuit(cuit)
    }

    suspend fun businessExists(businessId: String): Boolean {
        return businessDao.businessExists(businessId)
    }

    suspend fun countBusinesses(): Int {
        return businessDao.countBusinesses()
    }

    suspend fun updateBusinessTimestamp(businessId: String, timestamp: Long) {
        businessDao.updateBusinessTimestamp(businessId, timestamp)
    }
    */
}