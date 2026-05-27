package com.example.myapplication.core.data.remote

import android.util.Log
import com.example.myapplication.core.data.local.entity.ProviderEntity
import com.example.myapplication.core.domain.model.AddressProvider
import com.example.myapplication.core.domain.model.CompanyProvider
import com.google.firebase.firestore.DocumentSnapshot
import java.util.UUID

/**
 * --- PROVIDER DATA MAPPER (COMPARTIDO) ---
 * Centraliza la lógica de conversión de documentos de Firestore a entidades de Room.
 * Unifica las funciones de mapeo para asegurar consistencia en todo el ecosistema.
 */
object ProviderDataMapper {

    fun fromFirestore(doc: DocumentSnapshot): ProviderEntity? {
        return try {
            val perfil = doc.get("perfil") as? Map<*, *> ?: emptyMap<String, Any>()
            val ubicacion = doc.get("ubicacion") as? Map<*, *>
            val companiesRaw = doc.get("companies") as? List<*> ?: doc.get("empresas") as? List<*> ?: emptyList<Any>()
            
            val mappedCompanies = companiesRaw.mapNotNull { it as? Map<*, *> }.map { c ->
                CompanyProvider(
                    id = c["id"] as? String ?: UUID.randomUUID().toString(),
                    name = c["name"] as? String ?: c["nombre"] as? String ?: "",
                    razonSocial = c["razonSocial"] as? String ?: "",
                    cuit = c["cuit"] as? String ?: "",
                    description = c["description"] as? String ?: "",
                    isVerified = c["isVerified"] as? Boolean ?: false,
                    photoUrl = c["photoUrl"] as? String,
                    rating = (c["rating"] as? Number)?.toFloat() ?: 0f
                )
            }

            // Función auxiliar para buscar datos en múltiples fuentes (sin costo extra, solo en memoria)
            fun getDeepString(key: String, map: Map<*, *>? = null): String? {
                return (map?.get(key) as? String) ?: doc.getString(key)
            }

            val name = getDeepString("nombre", perfil) ?: doc.getString("name") ?: ""
            val lastName = getDeepString("apellido", perfil) ?: doc.getString("lastName") ?: ""
            val displayName = "${name} ${lastName}".trim().ifEmpty { doc.getString("displayName") ?: "Prestador" }
            
            // Búsqueda inteligente de imagen (priorizando formatos de la app prestador)
            val photo = getDeepString("imageUrl", perfil) 
                ?: getDeepString("imageBase64", perfil) 
                ?: doc.getString("photoUrl") 
                ?: doc.getString("imageUrl")
                ?: doc.getString("imageBase64")

            val banner = getDeepString("bannerImageUrl", perfil) 
                ?: getDeepString("bannerUrl", perfil)
                ?: doc.getString("bannerImageUrl")
                ?: doc.getString("bannerUrl")
            
            ProviderEntity(
                id = doc.id,
                email = (perfil["email"] as? String) ?: doc.getString("email") ?: "",
                displayName = displayName,
                name = name,
                lastName = lastName,
                phoneNumber = getDeepString("telefono", perfil) ?: doc.getString("phoneNumber") ?: doc.getString("telefono") ?: "",
                rating = (doc.get("rating") as? Number)?.toFloat() ?: 0f,
                categories = (doc.get("servicios") as? List<*>)?.map { it.toString() } ?: emptyList(),
                isSubscribed = doc.getBoolean("isSubscribed") ?: false,
                isVerified = doc.getBoolean("isVerified") ?: doc.getBoolean("verificado") ?: false,
                isOnline = doc.getBoolean("isOnline") ?: false,
                doesService = doc.getBoolean("doesService") ?: false,
                doesProduct = doc.getBoolean("doesProduct") ?: false,
                works24h = doc.getBoolean("works24h") ?: doc.getBoolean("atencionUrgencias") ?: false,
                doesHomeVisits = doc.getBoolean("doesHomeVisits") ?: doc.getBoolean("vaDomicilio") ?: false,
                doesShipping = doc.getBoolean("doesShipping") ?: doc.getBoolean("envios") ?: false,
                acceptsAppointments = doc.getBoolean("acceptsAppointments") ?: false,
                photoUrl = photo,
                bannerImageUrl = banner,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                address = AddressProvider(
                    id = UUID.randomUUID().toString(),
                    calle = ubicacion?.get("calle")?.toString() ?: "",
                    numero = ubicacion?.get("numero")?.toString() ?: "",
                    localidad = ubicacion?.get("localidad")?.toString() ?: "",
                    provincia = ubicacion?.get("provincia")?.toString() ?: "",
                    pais = ubicacion?.get("pais")?.toString() ?: "Argentina",
                    codigoPostal = ubicacion?.get("codigoPostal")?.toString() ?: "",
                    latitude = (ubicacion?.get("latitude") as? Number)?.toDouble() ?: 0.0,
                    longitude = (ubicacion?.get("longitude") as? Number)?.toDouble() ?: 0.0,
                    label = ubicacion?.get("label")?.toString() ?: "Principal"
                ),
                companies = mappedCompanies
            )
        } catch (e: Exception) {
            Log.e("ProviderDataMapper", "Error mapeando DocumentSnapshot ${doc.id}: ${e.message}")
            null
        }
    }
}
