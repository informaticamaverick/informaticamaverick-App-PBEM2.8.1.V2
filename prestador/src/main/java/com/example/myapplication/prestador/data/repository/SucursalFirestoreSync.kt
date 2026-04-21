package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.entity.SucursalEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SucursalFirestoreSync @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun branchesRef(provideId: String, companyId: String) = firestore.collection("providers")
        .document(provideId)
        .collection("companies")
        .document(companyId)
        .collection("branches")

    suspend fun subirSucursal(sucursal: SucursalEntity, providerId: String): Result<Unit> {
        return try {
            val data = mapOf(
                "id" to sucursal.id,
                "provider" to providerId,
                "businessId" to sucursal.businessId,
                "nombre" to sucursal.nombre,
                "telefono" to sucursal.telefono,
                "email" to sucursal.email,
                "horario" to sucursal.horario,
                "direccionId" to sucursal.direccionId,
                "referenteId" to sucursal.referenteId,
                "isActive" to sucursal.isActive,
                "doesService" to sucursal.doesService,
                "doesProduct" to sucursal.doesProduct,
                "works24h" to sucursal.works24h,
                "hasPhysicalLocation" to sucursal.hasPhysicalLocation,
                "doesHomeVisits" to sucursal.doesHomeVisits,
                "doesShipping" to sucursal.doesShipping,
                "acceptsAppointments" to sucursal.acceptsAppointments,
                "rating" to sucursal.rating,
                "galleryImages" to try { org.json.JSONArray(sucursal.galleryImages).let { arr -> (0 until arr.length()).map { arr.getString(it) } } } catch(e: Exception) { emptyList<String>() },
                "name" to sucursal.nombre, // Replicado de App Cliente
                "workingHours" to sucursal.horario, // Replicado de App Cliente
                "createdAt" to sucursal.createdAt,
                "updatedAt" to System.currentTimeMillis() // Corregido de updateAt a updatedAt
            )
            branchesRef(providerId, sucursal.businessId)
                .document(sucursal.id)
                .set(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarSucursal(
        sucursalId: String,
        providerId: String,
        companyId: String
    ): Result<Unit> {
        return try {
            branchesRef(providerId, companyId)
                .document(sucursalId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
