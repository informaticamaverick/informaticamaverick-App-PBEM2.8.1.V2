package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.dao.BlockedDateDao
import com.example.myapplication.prestador.data.local.entity.BlockedDateEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockedDateRepository @Inject constructor(
    private val blockedDateDao: BlockedDateDao
) {
    fun getActiveByProvider(providerId: String): Flow<List<BlockedDateEntity>> {
        return blockedDateDao.getActiveByProvider(providerId)
    }

    fun getAllByProvider(providerId: String): Flow<List<BlockedDateEntity>> {
        return blockedDateDao.getAllByProvider(providerId)
    }

    suspend fun save(entity: BlockedDateEntity) {
        blockedDateDao.insert(entity)
    }

    suspend fun insertAll(dates: List<BlockedDateEntity>) {
        blockedDateDao.insertAll(dates)
    }

    suspend fun deleteById(id: String) {
        blockedDateDao.deleteById(id)
    }

    suspend fun getByProviderAndDate(providerId: String, date: String): BlockedDateEntity? {
        return blockedDateDao.getByProviderAndDate(providerId, date)
    }
}
