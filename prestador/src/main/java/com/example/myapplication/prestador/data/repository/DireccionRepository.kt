package com.example.myapplication.prestador.data.repository

//import com.example.myapplication.prestador.data.local.dao.DireccionDao
//import com.example.myapplication.prestador.data.local.entity.DireccionEntity
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Deprecated("Use ProviderRepository as SSOT")
@Singleton
class DireccionRepository @Inject constructor(
    // private val direccionDao: DireccionDao
) {
    /*
    suspend fun saveDireccion(direccion: DireccionEntity
    ): Result<DireccionEntity> {
        return try {
            direccionDao.insertDireccion(direccion)
            Result.success(direccion)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDireccion(direccion: DireccionEntity): Result<DireccionEntity> {
        return try {
            val update = direccion.copy(updatedAt = System.currentTimeMillis())
            direccionDao.updateDireccion(update)
            Result.success(update)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDireccion(direccion: DireccionEntity): Result<Unit> {
        return try {
            direccionDao.deleteDireccion(direccion)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDireccionById(id: String): DireccionEntity? {
        return direccionDao.getDireccionById(id)
    }

    suspend fun getDireccionByReferencia(referenciaId: String, tipo: String): DireccionEntity? {
        return direccionDao.getDireccionByReferencia(referenciaId, tipo)
    }

    suspend fun upsertDireccion(
        referenciaId: String,
        referenciaTipo: String,
        pais: String = "Argentina",
        provincia: String? = null,
        localidad: String? = null,
        codigoPostal: String? = null,
        calle: String? = null,
        numero: String? = null,
        latitud: Double? = null,
        longitud: Double? = null
    ): Result<DireccionEntity> {
        return try {
            val existing = direccionDao.getDireccionByReferencia(referenciaId, referenciaTipo)
            val direccion = existing?.copy(pais = pais, provincia = provincia, localidad = localidad, codigoPostal = codigoPostal, calle = calle, numero = numero, latitud = latitud, longitud = longitud, updatedAt = System.currentTimeMillis()) ?: DireccionEntity(
                id = UUID.randomUUID().toString(),
                referenciaId = referenciaId,
                referenciaTipo = referenciaTipo,
                pais = pais,
                provincia = provincia,
                localidad = localidad,
                codigoPostal = codigoPostal,
                calle = calle,
                numero = numero,
                latitud = latitud,
                longitud = longitud
            )

            direccionDao.insertDireccion(direccion)
            Result.success(direccion)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    */
}