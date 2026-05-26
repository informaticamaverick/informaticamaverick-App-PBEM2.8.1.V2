package com.example.myapplication.core.data.remote

import android.util.Log
import com.example.myapplication.core.data.local.entity.UserEntity
import com.example.myapplication.core.domain.model.*
import com.google.firebase.firestore.DocumentSnapshot
import java.util.UUID

/**
 * --- USER DATA MAPPER (COMPARTIDO) ---
 * Centraliza la conversión de perfiles de usuario desde Firestore a Room.
 * Garantiza que tanto la App Cliente como la App Prestador visualicen los mismos datos de perfil.
 */
object UserDataMapper {

    fun fromFirestore(doc: DocumentSnapshot): UserEntity? {
        if (!doc.exists()) return null
        return try {
            val data = doc.data ?: return null
            val perfil = data["perfil"] as? Map<*, *> ?: emptyMap<String, Any>()

            // Función auxiliar para búsqueda inteligente (Root o Perfil)
            fun getDeepString(key: String): String? {
                return (perfil[key] as? String) ?: (data[key] as? String)
            }

            fun getDeepBoolean(key: String, default: Boolean = false): Boolean {
                return (perfil[key] as? Boolean) ?: (data[key] as? Boolean) ?: default
            }

            // Mapeo de Direcciones
            val addressesRaw = (data["personalAddresses"] ?: perfil["personalAddresses"]) as? List<*> ?: emptyList<Any>()
            val mappedAddresses = addressesRaw.mapNotNull { it as? Map<*, *> }.map { a ->
                AddressClient(
                    id = a["id"] as? String ?: UUID.randomUUID().toString(),
                    calle = a["calle"] as? String ?: "",
                    numero = a["numero"] as? String ?: "",
                    localidad = a["localidad"] as? String ?: "",
                    provincia = a["provincia"] as? String ?: "",
                    pais = a["pais"] as? String ?: "",
                    codigoPostal = a["codigoPostal"] as? String ?: "",
                    latitude = (a["latitude"] as? Number)?.toDouble() ?: 0.0,
                    longitude = (a["longitude"] as? Number)?.toDouble() ?: 0.0,
                    label = a["label"] as? String ?: "Dirección"
                )
            }

            // Mapeo de Empresas
            val companiesRaw = (data["companies"] ?: perfil["companies"] ?: data["empresas"] ?: perfil["empresas"]) as? List<*> ?: emptyList<Any>()
            val mappedCompanies = companiesRaw.mapNotNull { it as? Map<*, *> }.map { c ->
                val branchesRaw = c["branches"] as? List<*> ?: emptyList<Any>()
                val mappedBranches = branchesRaw.mapNotNull { it as? Map<*, *> }.map { b ->
                    BranchClient(
                        id = b["id"] as? String ?: UUID.randomUUID().toString(),
                        name = b["name"] as? String ?: "",
                        isMainBranch = b["isMainBranch"] as? Boolean ?: false,
                        address = (b["address"] as? Map<*, *>)?.let { adr ->
                            AddressClient(
                                id = adr["id"] as? String ?: UUID.randomUUID().toString(),
                                calle = adr["calle"] as? String ?: "",
                                localidad = adr["localidad"] as? String ?: ""
                            )
                        } ?: AddressClient()
                    )
                }

                CompanyClient(
                    id = c["id"] as? String ?: UUID.randomUUID().toString(),
                    name = c["name"] as? String ?: c["nombre"] as? String ?: "",
                    razonSocial = c["razonSocial"] as? String ?: "",
                    cuit = c["cuit"] as? String ?: "",
                    email = c["email"] as? String ?: "",
                    phoneNumber = c["phoneNumber"] as? String ?: "",
                    branches = mappedBranches
                )
            }

            UserEntity(
                id = doc.id,
                email = getDeepString("email") ?: "",
                name = getDeepString("name") ?: getDeepString("nombre") ?: "",
                lastName = getDeepString("lastName") ?: getDeepString("apellido") ?: "",
                displayName = getDeepString("displayName") ?: "",
                phoneNumber = getDeepString("phoneNumber") ?: getDeepString("telefono") ?: "",
                bio = getDeepString("bio") ?: "",
                photoUrl = getDeepString("photoUrl") ?: getDeepString("imageUrl") ?: getDeepString("photo"),
                bannerImageUrl = getDeepString("bannerImageUrl") ?: getDeepString("bannerUrl"),
                galleryImages = ((data["galleryImages"] ?: perfil["galleryImages"]) as? List<*>)?.map { it.toString() } ?: emptyList(),
                additionalEmails = ((data["additionalEmails"] ?: perfil["additionalEmails"]) as? List<*>)?.map { it.toString() } ?: emptyList(),
                additionalPhones = ((data["additionalPhones"] ?: perfil["additionalPhones"]) as? List<*>)?.map { it.toString() } ?: emptyList(),
                personalAddresses = mappedAddresses,
                hasCompanyProfile = getDeepBoolean("hasCompanyProfile"),
                companies = mappedCompanies,
                isOnline = getDeepBoolean("isOnline"),
                isSubscribed = getDeepBoolean("isSubscribed"),
                isVerified = (perfil["isVerified"] as? Boolean) ?: (data["isVerified"] as? Boolean) ?: (perfil["verificado"] as? Boolean) ?: (data["verificado"] as? Boolean) ?: false,
                notificationsEnabled = getDeepBoolean("notificationsEnabled"),
                isPublicProfile = getDeepBoolean("isPublicProfile"),
                isProfileComplete = getDeepBoolean("isProfileComplete"),
                rating = (data["rating"] as? Number ?: perfil["rating"] as? Number)?.toFloat() ?: 0f,
                favoriteProviderIds = ((data["favoriteProviderIds"] ?: perfil["favoriteProviderIds"]) as? List<*>)?.map { it.toString() } ?: emptyList(),
                latitude = (data["latitude"] as? Number ?: perfil["latitude"] as? Number)?.toDouble() ?: 0.0,
                longitude = (data["longitude"] as? Number ?: perfil["longitude"] as? Number)?.toDouble() ?: 0.0,
                createdAt = (data["createdAt"] as? Number ?: perfil["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e("UserDataMapper", "Error mapeando Usuario ${doc.id}: ${e.message}")
            null
        }
    }
}
