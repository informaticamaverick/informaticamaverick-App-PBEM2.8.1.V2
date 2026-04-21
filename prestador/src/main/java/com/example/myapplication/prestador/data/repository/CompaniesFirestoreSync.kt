package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.entity.BusinessEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompaniesFirestoreSync @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun companiesRef(providerId: String) =
        firestore.collection("providers")
            .document(providerId)
            .collection("companies")

    suspend fun subirCompany(businees: BusinessEntity, providerId: String): Result<Unit> {
        return try {
            val data = mapOf(
                "id" to businees.id,
                "providerId" to providerId,
                "nombreNegocio" to businees.nombreNegocio,
                "razonSocial" to businees.razonSocial,
                "cuitNegocio" to businees.cuitNegocio,
                "direccion" to businees.direccion,
                "codigoPostal" to businees.codigoPostal,
                "telefono" to businees.telefono,
                "email" to businees.email,
                "descripcion" to businees.descripcion,
                "imageUrl" to businees.imageUrl,
                "photoUrl" to businees.imageUrl, // Alias para consistencia
                "horario" to businees.horario,
                "categories" to try { org.json.JSONArray(businees.categorias).let { arr -> (0 until arr.length()).map { arr.getString(it) } } } catch(e: Exception) { emptyList<String>() },
                "imagenesProductos" to try { org.json.JSONArray(businees.imagenesProductos).let { arr -> (0 until arr.length()).map { arr.getString(it) } } } catch(e: Exception) { emptyList<String>() },
                "atencion24hs" to businees.atencion24hs,
                "localComercial" to businees.localComercial,
                "visitaDomicilio" to businees.visitaDomicilio,
                "envios" to businees.envios,
                "turnos" to businees.turnos,
                "verificado" to businees.verificado,
                "rating" to businees.rating,
                "name" to businees.nombreNegocio, // Replicado de App Cliente
                "photoUrl" to businees.imageUrl, // Replicado de App Cliente
                "createdAt" to businees.createdAt,
                "updatedAt" to System.currentTimeMillis() // Corregido de updateAt a updatedAt para consistencia

            )
            companiesRef(providerId).document(businees.id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarCompany(companyId: String, providerId: String): Result<Unit> {
        return  try {
            companiesRef(providerId).document(companyId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}