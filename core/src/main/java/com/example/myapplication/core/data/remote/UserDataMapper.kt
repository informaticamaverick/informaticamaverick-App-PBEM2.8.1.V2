package com.example.myapplication.core.data.remote

import android.util.Log
import com.example.myapplication.core.data.local.entity.UserEntity
import com.example.myapplication.core.domain.model.*
import com.google.firebase.firestore.DocumentSnapshot
import java.util.UUID

/**
 * --- USER DATA MAPPER (COMPARTIDO) ---
 * [ELITE v5.1]: Limpieza de metadatos de prestador y soporte para AddressUnico.
 */
object UserDataMapper {

    fun fromFirestore(doc: DocumentSnapshot): UserEntity? {
        return try {
            if (!doc.exists()) return null

            val perfil = doc.get("perfil") as? Map<*, *> ?: emptyMap<String, Any>()
            val companiesRaw = doc.get("companies") as? List<*> ?: emptyList<Any>()
            
            val mappedCompanies = companiesRaw.mapNotNull { it as? Map<*, *> }.map { c ->
                val branchesRaw = c["branches"] as? List<*> ?: emptyList<Any>()
                val mappedBranches = branchesRaw.mapNotNull { it as? Map<*, *> }.map { b ->
                    val addr = b["address"] as? Map<*, *> ?: emptyMap<String, Any>()
                    val repsRaw = b["representatives"] as? List<*> ?: emptyList<Any>()
                    val mappedReps = repsRaw.mapNotNull { it as? Map<*, *> }.map { r ->
                        RepresentativeClient(
                            id = r["id"] as? String ?: UUID.randomUUID().toString(),
                            nombre = r["nombre"] as? String ?: "",
                            apellido = r["apellido"] as? String ?: "",
                            cargo = r["cargo"] as? String ?: "",
                            photoUrl = r["photoUrl"] as? String,
                            thumbnailBase64 = r["thumbnailBase64"] as? String
                        )
                    }

                    BranchClient(
                        id = b["id"] as? String ?: UUID.randomUUID().toString(),
                        name = b["name"] as? String ?: "",
                        description = b["description"] as? String ?: "",
                        workingHours = b["workingHours"] as? String ?: "",
                        isMainBranch = b["isMainBranch"] as? Boolean ?: false,
                        address = AddressUnico(
                            id = addr["id"] as? String ?: UUID.randomUUID().toString(),
                            calle = addr["calle"] as? String ?: "",
                            numero = addr["numero"] as? String ?: "",
                            piso = addr["piso"] as? String ?: "",
                            departamento = addr["departamento"] as? String ?: "",
                            localidad = addr["localidad"] as? String ?: "",
                            provincia = addr["provincia"] as? String ?: "",
                            pais = addr["pais"] as? String ?: "Argentina",
                            codigoPostal = addr["codigoPostal"] as? String ?: "",
                            latitude = (addr["latitude"] as? Number)?.toDouble() ?: 0.0,
                            longitude = (addr["longitude"] as? Number)?.toDouble() ?: 0.0,
                            label = addr["label"] as? String ?: ""
                        ),
                        representatives = mappedReps
                    )
                }

                CompanyClient(
                    id = c["id"] as? String ?: UUID.randomUUID().toString(),
                    name = c["name"] as? String ?: "",
                    razonSocial = c["razonSocial"] as? String ?: "",
                    cuit = c["cuit"] as? String ?: "",
                    email = c["email"] as? String ?: "",
                    phoneNumber = c["phoneNumber"] as? String ?: "",
                    photoUrl = c["photoUrl"] as? String,
                    thumbnailBase64 = c["thumbnailBase64"] as? String,
                    branches = mappedBranches
                )
            }

            val addressesRaw = doc.get("personalAddresses") as? List<*> ?: emptyList<Any>()
            val mappedAddresses = addressesRaw.mapNotNull { it as? Map<*, *> }.map { a ->
                AddressUnico(
                    id = a["id"] as? String ?: UUID.randomUUID().toString(),
                    calle = a["calle"] as? String ?: "",
                    numero = a["numero"] as? String ?: "",
                    piso = a["piso"] as? String ?: "",
                    departamento = a["departamento"] as? String ?: "",
                    localidad = a["localidad"] as? String ?: "",
                    provincia = a["provincia"] as? String ?: "",
                    pais = a["pais"] as? String ?: "Argentina",
                    codigoPostal = a["codigoPostal"] as? String ?: "",
                    latitude = (a["latitude"] as? Number)?.toDouble() ?: 0.0,
                    longitude = (a["longitude"] as? Number)?.toDouble() ?: 0.0,
                    label = a["label"] as? String ?: ""
                )
            }

            UserEntity(
                id = doc.id,
                email = (perfil["email"] as? String) ?: doc.getString("email") ?: "",
                displayName = doc.getString("displayName") ?: "",
                name = (perfil["name"] as? String) ?: doc.getString("name") ?: "",
                lastName = (perfil["lastName"] as? String) ?: doc.getString("lastName") ?: "",
                phoneNumber = (perfil["phoneNumber"] as? String) ?: doc.getString("phoneNumber") ?: "",
                bio = (perfil["bio"] as? String) ?: doc.getString("bio") ?: "",
                photoUrl = (perfil["photoUrl"] as? String) ?: doc.getString("photoUrl"),
                profileThumbnail = (perfil["profileThumbnail"] as? String) ?: doc.getString("profileThumbnail"),
                personalAddresses = mappedAddresses,
                hasCompanyProfile = doc.getBoolean("hasCompanyProfile") ?: false,
                companies = mappedCompanies,
                isOnline = doc.getBoolean("isOnline") ?: false,
                isSubscribed = doc.getBoolean("isSubscribed") ?: false,
                isVerified = doc.getBoolean("isVerified") ?: false,
                notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: true,
                isPublicProfile = doc.getBoolean("isPublicProfile") ?: false,
                isProfileComplete = doc.getBoolean("isProfileComplete") ?: false,
                rating = (doc.get("rating") as? Number)?.toFloat() ?: 0f,
                favoriteProviderIds = (doc.get("favoriteProviderIds") as? List<*>)?.map { it.toString() } ?: emptyList(),
                latitude = (doc.get("latitude") as? Number)?.toDouble() ?: 0.0,
                longitude = (doc.get("longitude") as? Number)?.toDouble() ?: 0.0,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e("UserDataMapper", "Error mapeando Usuario ${doc.id}: ${e.message}")
            null
        }
    }
}
