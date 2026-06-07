package com.example.myapplication.core.data.remote

import android.util.Log
import com.example.myapplication.core.data.local.entity.ProviderEntity
import com.example.myapplication.core.domain.model.*
import com.google.firebase.firestore.DocumentSnapshot
import java.util.UUID

/**
 * --- PROVIDER DATA MAPPER (COMPARTIDO) ---
 * [ELITE v5.1]: Usa AddressUnico y mantiene paridad jerárquica.
 */
object ProviderDataMapper {

    fun fromFirestore(doc: DocumentSnapshot): ProviderEntity? {
        return try {
            val perfil = doc.get("perfil") as? Map<*, *> ?: emptyMap<String, Any>()
            val ubicacion = doc.get("ubicacion") as? Map<*, *>
            val companiesRaw = doc.get("companies") as? List<*> ?: doc.get("empresas") as? List<*> ?: emptyList<Any>()
            
            val mappedCompanies = companiesRaw.mapNotNull { it as? Map<*, *> }.map { c ->
                val branchesRaw = c["branches"] as? List<*> ?: emptyList<Any>()
                val mappedBranches = branchesRaw.mapNotNull { it as? Map<*, *> }.map { b ->
                    val addr = b["address"] as? Map<*, *> ?: emptyMap<String, Any>()
                    BranchProvider(
                        id = b["id"] as? String ?: UUID.randomUUID().toString(),
                        name = b["name"] as? String ?: b["nombre"] as? String ?: "",
                        description = b["description"] as? String ?: "",
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
                        rating = (b["rating"] as? Number)?.toFloat() ?: 0f
                    )
                }

                CompanyProvider(
                    id = c["id"] as? String ?: UUID.randomUUID().toString(),
                    name = c["name"] as? String ?: c["nombre"] as? String ?: "",
                    razonSocial = c["razonSocial"] as? String ?: "",
                    cuit = c["cuit"] as? String ?: "",
                    description = c["description"] as? String ?: "",
                    isVerified = c["isVerified"] as? Boolean ?: false,
                    photoUrl = c["photoUrl"] as? String,
                    thumbnailBase64 = c["thumbnailBase64"] as? String,
                    rating = (c["rating"] as? Number)?.toFloat() ?: 0f,
                    categories = (c["categories"] as? List<*>)?.map { it.toString() } ?: (c["servicios"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                    branches = mappedBranches
                )
            }

            fun getDeepString(key: String, map: Map<*, *>? = null): String? {
                return (map?.get(key) as? String) ?: doc.getString(key)
            }

            // [ALINEACIÓN] Soporta tanto llaves en Inglés (User) como Español (Legacy Provider)
            val name = getDeepString("name", perfil) ?: getDeepString("nombre", perfil) ?: doc.getString("name") ?: ""
            val lastName = getDeepString("lastName", perfil) ?: getDeepString("apellido", perfil) ?: doc.getString("lastName") ?: ""
            val displayName = doc.getString("displayName") ?: "${name} ${lastName}".trim().ifEmpty { "Prestador" }
            
            val photo = getDeepString("photoUrl", perfil) ?: doc.getString("photoUrl")
            val thumbnail = getDeepString("profileThumbnail", perfil) ?: doc.getString("profileThumbnail")

            val localMap = doc.get("local") as? Map<*, *>
            val modalidadMap = doc.get("modalidad") as? Map<*, *>
            val empresaMap = doc.get("empresa") as? Map<*, *>
            
            val schedule = getDeepString("workingHours", localMap) ?: doc.getString("workingHours") ?: ""
            val priorizarEmpresa = (empresaMap?.get("priorizarEmpresa") as? Boolean) ?: false
            
            // Carga de direcciones desde el Array (Paridad con Cliente)
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

            // [SSOT]: Si no hay mapa 'ubicacion', usamos la primera dirección del array
            val firstAddr = mappedAddresses.firstOrNull()
            
            val lat = (ubicacion?.get("latitude") as? Number)?.toDouble() ?: firstAddr?.latitude ?: 0.0
            val lng = (ubicacion?.get("longitude") as? Number)?.toDouble() ?: firstAddr?.longitude ?: 0.0

            val mainAddress = if (ubicacion != null && ubicacion["calle"]?.toString()?.isNotBlank() == true) {
                AddressUnico(
                    id = UUID.randomUUID().toString(),
                    calle = ubicacion["calle"]?.toString() ?: "",
                    numero = ubicacion["numero"]?.toString() ?: "",
                    piso = ubicacion["piso"]?.toString() ?: "",
                    departamento = ubicacion["departamento"]?.toString() ?: "",
                    localidad = ubicacion["localidad"]?.toString() ?: "",
                    provincia = ubicacion["provincia"]?.toString() ?: "",
                    pais = ubicacion["pais"]?.toString() ?: "Argentina",
                    codigoPostal = ubicacion["codigoPostal"]?.toString() ?: "",
                    latitude = lat,
                    longitude = lng,
                    label = "Principal"
                )
            } else firstAddr ?: AddressUnico(label = "Principal", latitude = lat, longitude = lng)

            ProviderEntity(
                id = doc.id,
                providerId = doc.id, // [ELITE] UID real del prestador
                email = (perfil["email"] as? String) ?: doc.getString("email") ?: "",
                emails = (doc.get("emails") as? List<*>)?.map { it.toString() } ?: emptyList(),
                displayName = displayName,
                name = name,
                lastName = lastName,
                phoneNumber = getDeepString("phoneNumber", perfil) ?: getDeepString("telefono", perfil) ?: doc.getString("phoneNumber") ?: "",
                rating = (doc.get("rating") as? Number)?.toFloat() ?: 0f,
                categories = (doc.get("categories") as? List<*>)?.map { it.toString() } ?: (doc.get("servicios") as? List<*>)?.map { it.toString() } ?: emptyList(),
                description = getDeepString("bio", perfil) ?: getDeepString("description", perfil) ?: doc.getString("description") ?: "",
                matricula = getDeepString("matricula", perfil) ?: doc.getString("matricula"),
                profesion = getDeepString("profesion", perfil) ?: doc.getString("profesion"),
                cuilCuit = getDeepString("dniCuit", perfil) ?: doc.getString("cuilCuit"),
                isSubscribed = doc.getBoolean("isSubscribed") ?: true,
                isVerified = doc.getBoolean("isVerified") ?: false,
                isOnline = doc.getBoolean("isOnline") ?: false,
                priorizarEmpresa = priorizarEmpresa,
                doesService = doc.getBoolean("doesService") ?: false,
                doesProduct = doc.getBoolean("doesProduct") ?: false,
                works24h = (modalidadMap?.get("atencionUrgencias") as? Boolean) ?: doc.getBoolean("works24h") ?: false,
                doesHomeVisits = (modalidadMap?.get("vaDomicilio") as? Boolean) ?: doc.getBoolean("doesHomeVisits") ?: false,
                doesShipping = (modalidadMap?.get("envios") as? Boolean) ?: doc.getBoolean("doesShipping") ?: false,
                acceptsAppointments = (localMap?.get("aceptaTurnos") as? Boolean) ?: doc.getBoolean("acceptsAppointments") ?: false,
                hasPhysicalLocation = (localMap?.get("atencionEnLocal") as? Boolean) ?: doc.getBoolean("hasPhysicalLocation") ?: false,
                trabajaConOtros = doc.getBoolean("trabajaConOtros") ?: false,
                photoUrl = photo,
                profileThumbnail = thumbnail,
                workingHours = schedule,
                fullAddress = doc.getString("fullAddress") ?: mainAddress.fullString(),
                geohash = doc.getString("geohash") ?: com.example.myapplication.core.utils.MaverickGeoUtils.computeGeohash(lat, lng),
                latitude = lat,
                longitude = lng,
                lastSyncTimestamp = System.currentTimeMillis(),
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                address = mainAddress,
                addresses = mappedAddresses,
                companies = mappedCompanies
            )
        } catch (e: Exception) {
            Log.e("ProviderDataMapper", "Error mapeando DocumentSnapshot ${doc.id}: ${e.message}")
            null
        }
    }

    /**
     * [ELITE] Mapea un documento del "Índice de Puntos de Servicio".
     * Devuelve una entidad de Room parcial pero funcional para Shallow Loading.
     */
    fun fromSearchIndex(doc: DocumentSnapshot): ProviderEntity? {
        return try {
            val providerId = doc.getString("providerId") ?: return null
            val companyId = doc.getString("companyId")
            val branchId = doc.getString("branchId")
            
            // [ELITE] Si hay sucursal, cargamos sus datos específicos del índice
            val addrMap = doc.get("address") as? Map<*, *>
            val address = AddressUnico(
                codigoPostal = doc.getString("zipCode") ?: addrMap?.get("codigoPostal")?.toString() ?: "",
                latitude = doc.getDouble("latitude") ?: (addrMap?.get("latitude") as? Number)?.toDouble() ?: 0.0,
                longitude = doc.getDouble("longitude") ?: (addrMap?.get("longitude") as? Number)?.toDouble() ?: 0.0,
                calle = addrMap?.get("calle")?.toString() ?: "",
                numero = addrMap?.get("numero")?.toString() ?: "",
                localidad = addrMap?.get("localidad")?.toString() ?: "",
                provincia = addrMap?.get("provincia")?.toString() ?: ""
            )

            val branch = BranchProvider(
                id = branchId ?: "main",
                name = doc.getString("branchName") ?: "",
                address = address,
                works24h = doc.getBoolean("works24h") ?: false,
                hasPhysicalLocation = doc.getBoolean("hasPhysicalLocation") ?: false,
                doesHomeVisits = doc.getBoolean("doesHomeVisits") ?: false,
                doesShipping = doc.getBoolean("doesShipping") ?: false,
                acceptsAppointments = doc.getBoolean("acceptsAppointments") ?: false,
                rating = (doc.get("rating") as? Number)?.toFloat() ?: 0f
            )

            val company = CompanyProvider(
                id = companyId ?: "main",
                name = doc.getString("title") ?: "Prestador",
                thumbnailBase64 = doc.getString("thumbnailBase64"),
                photoUrl = doc.getString("photoUrl"),
                categories = (doc.get("categories") as? List<*>)?.map { it.toString() } ?: emptyList(),
                isVerified = doc.getBoolean("isVerified") ?: false,
                branches = listOf(branch)
            )

            ProviderEntity(
                id = doc.id, // [ELITE] PK única por sucursal/punto de servicio
                providerId = providerId, // [ELITE] UID real para agrupamiento
                displayName = doc.getString("title") ?: "Prestador",
                name = doc.getString("name") ?: "",
                lastName = doc.getString("lastName") ?: "",
                profileThumbnail = doc.getString("thumbnailBase64"),
                photoUrl = doc.getString("photoUrl"),
                rating = (doc.get("rating") as? Number)?.toFloat() ?: 0f,
                categories = company.categories,
                isSubscribed = doc.getBoolean("isSubscribed") ?: true,
                isVerified = company.isVerified,
                isOnline = doc.getBoolean("isOnline") ?: false,
                works24h = branch.works24h,
                hasPhysicalLocation = branch.hasPhysicalLocation,
                doesHomeVisits = branch.doesHomeVisits,
                doesShipping = branch.doesShipping,
                acceptsAppointments = branch.acceptsAppointments,
                fullAddress = doc.getString("fullAddress") ?: address.fullString(),
                geohash = doc.getString("geohash") ?: com.example.myapplication.core.utils.MaverickGeoUtils.computeGeohash(address.latitude, address.longitude),
                latitude = address.latitude,
                longitude = address.longitude,
                address = address,
                companies = listOf(company),
                email = "", phoneNumber = "",
                createdAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e("ProviderDataMapper", "Error mapeando SearchIndex ${doc.id}: ${e.message}")
            null
        }
    }
}
