package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.dao.ReferenteDao
import com.example.myapplication.prestador.data.local.entity.ReferenteEntity
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReferenteRepository @Inject constructor(
    private val referenteDao: ReferenteDao
) {

    suspend fun addReferente(
        providerId: String,
        nombre: String,
        apellido: String? = null,
        cargo: String? = null,
        imageUrl: String? = null,
        empresaId: String? = null,
        sucursalId: String? = null
    ): Result<ReferenteEntity> {
        return try {
            if (nombre.isBlank()) {
                return Result.failure(Exception("El nombre del referente es obligatorio"))
            }
            val referente = ReferenteEntity(
                id = UUID.randomUUID().toString(),
                providerId = providerId,
                nombre = nombre.trim(),
                apellido = apellido?.trim(),
                cargo = cargo?.trim(),
                imageUrl = imageUrl,
                empresaId = empresaId,
                sucursalId = sucursalId,
                activo = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            referenteDao.insertReferente(referente)
            Result.success(referente)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReferente(referente: ReferenteEntity): Result<ReferenteEntity> {
        return try {
            if (referente.nombre.isBlank()) {
                return Result.failure(Exception("El nombre del referente es obligatorio"))
            }
            val updated = referente.copy(updatedAt = System.currentTimeMillis())
            referenteDao.updateReferente(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun desactivarReferente(referenteId: String): Result<Unit> {
        return try {
            referenteDao.desactivarReferente(referenteId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReferenteById(id: String): ReferenteEntity? {
        return referenteDao.getReferenteById(id)
    }

    suspend fun getReferentesByProvider(providerId: String): List<ReferenteEntity> {
        return referenteDao.getReferentesByProvider(providerId)
    }

    suspend fun getReferentesByEmpresa(empresaId: String): List<ReferenteEntity> {
        return referenteDao.getReferentesByEmpresa(empresaId)
    }

    suspend fun getReferentesBySucursal(sucursalId: String): List<ReferenteEntity> {
        return referenteDao.getReferentesBySucursal(sucursalId)
    }
}