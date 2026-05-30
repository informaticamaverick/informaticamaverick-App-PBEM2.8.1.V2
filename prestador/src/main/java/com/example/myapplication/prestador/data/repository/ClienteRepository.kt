package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.dao.ClienteDao
import com.example.myapplication.prestador.data.local.entity.ClienteEntity
import com.example.myapplication.prestador.data.model.ClienteDireccion
import com.example.myapplication.prestador.data.model.ClienteEmpresa
import com.example.myapplication.prestador.data.model.ClienteProfile
import com.example.myapplication.prestador.data.model.ClienteSucursal
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClienteRepository @Inject constructor(
    private val clienteDao: ClienteDao,
    private val firestore: FirebaseFirestore
) {
    suspend fun saveCliente(cliente: ClienteEntity) =
        clienteDao.insertCliente(cliente)
    suspend fun saveClientes(clientes: List<ClienteEntity>) =
        clienteDao.insertClientes(clientes)
    suspend fun updateCliente(cliente: ClienteEntity) =
        clienteDao.updateCliente(cliente)
    suspend fun deleteCliente(clienteId: String) =
        clienteDao.deleteClienteById(clienteId)
    suspend fun deleteAllClientes() = clienteDao.deleteAllClientes()
    fun getClienteById(clienteId: String): Flow<ClienteEntity?> =
        clienteDao.getClienteById(clienteId)
    fun getAllClientes(): Flow<List<ClienteEntity>> =
        clienteDao.getAllClientes()
    fun searchClientesByNombre(nombre: String): Flow<List<ClienteEntity>> =
        clienteDao.searchClientesByNombre("%$nombre%")
    fun getClienteByEmail(email: String): Flow<ClienteEntity?> =
        clienteDao.getClienteByEmail(email)
    fun getClienteByTelefono(telefono: String): Flow<ClienteEntity?> =
        clienteDao.getClienteByTelefono(telefono)
    suspend fun clienteExists(clienteId: String): Boolean =
        clienteDao.clienteExists(clienteId)
    suspend fun countClientes(): Int = clienteDao.countClientes()

    // ─── FIRESTORE

    suspend fun fetchClienteProfile(clientId: String): ClienteProfile {
        val doc =
            firestore.collection("usuarios").document(clientId).get().await()
        if (!doc.exists()) throw Exception("No se encontró el perfil del cliente")
            val data = doc.data ?: emptyMap()
        //El perfil puede estar anidado a un submapa "perfil" (igual que UserDataMapper)
        val perfil = data["perfil"] as? Map<*, *> ?: emptyMap<String, Any>()
        fun getDeepString(key: String): String? = (perfil[key] as? String) ?: (data[key] as? String)
        fun getDeepBoolean(key: String, default: Boolean = false): Boolean = (perfil[key] as? Boolean) ?: (data[key] as? Boolean) ?: default

        val addresses = firestore.collection("usuarios")

            .document(clientId).collection("personalAddresses").get().await()
            .documents.map { a ->
                val d = a.data ?: emptyMap()
                ClienteDireccion(
                    label = d["label"] as? String ?: "",
                    calle = d["calle"] as? String ?: "",
                    numero = d["numero"] as? String ?: "",
                    localidad = d["localidad"] as? String ?: "",
                    provincia = d["provincia"] as? String ?: "",
                    pais = d["pais"] as? String ?: "",
                    codigoPostal = d["codigoPostal"] as? String ?: "",
                    latitude = (d["latitude"] as? Number)?.toDouble() ?:
                    0.0,
                    longitude = (d["longitude"] as? Number)?.toDouble() ?:
                    0.0
                )
            }

        val companiesSnapshot = firestore.collection("usuarios")
            .document(clientId).collection("companies").get().await()

        val companies = companiesSnapshot.documents.map { compDoc ->
            val c = compDoc.data ?: emptyMap()
            val branches = firestore.collection("usuarios")
                .document(clientId).collection("companies")
                .document(compDoc.id).collection("branches").get().await()
                .documents.map { branchDoc ->
                    val b = branchDoc.data ?: emptyMap()
                    @Suppress("UNCHECKED_CAST")
                    val addr = b["address"] as? Map<String, Any> ?:
                    emptyMap()
                    ClienteSucursal(
                        id = b["id"] as? String ?: branchDoc.id,
                        name = b["name"] as? String ?: "",
                        isMainBranch = b["isMainBranch"] as? Boolean ?:
                        false,
                        galleryImages = b["galleryImages"] as? List<String>
                            ?: emptyList(),
                        address = ClienteDireccion(
                            label = addr["label"] as? String ?: "",
                            calle = addr["calle"] as? String ?: "",
                            numero = addr["numero"] as? String ?: "",
                            localidad = addr["localidad"] as? String ?: "",
                            provincia = addr["provincia"] as? String ?: "",
                            pais = addr["pais"] as? String ?: "",
                            codigoPostal = addr["codigoPostal"] as? String
                                ?: ""
                        )
                    )
                }
            ClienteEmpresa(
                id = c["id"] as? String ?: compDoc.id,
                name = c["name"] as? String ?: "",
                razonSocial = c["razonSocial"] as? String ?: "",
                cuit = c["cuit"] as? String ?: "",
                email = c["email"] as? String ?: "",
                phoneNumber = c["phoneNumber"] as? String ?: "",
                photoUrl = c["photoUrl"] as? String,
                bannerImageUrl = c["bannerImageUrl"] as? String,
                branches = branches
            )
        }

        @Suppress("UNCHECKED_CAST")
        return ClienteProfile(
            clientId = clientId,
            name = getDeepString("name") ?: getDeepString("nombre") ?: "",
            lastName = getDeepString("lastName") ?: getDeepString("apellido") ?: "",
            displayName = getDeepString("displayName") ?: "",
            email = getDeepString("email") ?: "",
            phoneNumber = getDeepString("phoneNumber") ?: getDeepString("telefono") ?: "",
            bio = getDeepString("bio") ?: "",
            photoUrl = getDeepString("photoUrl") ?: getDeepString("imageUrl") ?: getDeepString("photo"),
            bannerImageUrl = getDeepString("bannerImageUrl") ?: getDeepString("bannerUrl"),
            isVerified = getDeepBoolean("isVerified"),
            isOnline = getDeepBoolean("isOnline"),
            isSubscribed = getDeepBoolean("isSubscribed"),
            isPublicProfile = getDeepBoolean("isPublicProfile"),
            rating = (perfil["rating"] as? Number ?: data["rating"] as? Number)?.toFloat() ?: 0f,
            galleryImages = ((data["galleryImages"] ?: perfil["galleryImages"]) as? List<*>)?.map { it.toString() } ?: emptyList(),
            personalAddresses = addresses,
            hasCompanyProfile = getDeepBoolean("hasCompanyProfile"),
            companies = companies,
            createdAt = (perfil["createdAt"] as? Number ?: data["createdAt"] as? Number)?.toLong() ?: 0L
        )
    }

    fun observerClienteProfile(clientId: String): Flow<ClienteProfile?> = callbackFlow {
        val listener = firestore.collection("usuarios").document(clientId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val data = snapshot.data ?: emptyMap()
                val perfil = data["perfil"]  as? Map<*, *> ?: emptyMap<String, Any>()
                fun getDeepString(key: String): String? = (perfil[key] as? String) ?: (data[key] as? String)
                fun getDeepBoolean(key: String, default: Boolean = false): Boolean = (perfil[key] as? Boolean) ?: (data[key] as? Boolean) ?: default
                val profile = ClienteProfile(
                    clientId = clientId,
                    name = getDeepString("name") ?: getDeepString("nombre") ?: "",
                    lastName = getDeepString("lastName") ?: getDeepString("apellido") ?: "",
                    displayName = getDeepString("displayName") ?: "",
                    email = getDeepString("email") ?: "",
                    phoneNumber = getDeepString("phoneNumber") ?: getDeepString("telefono") ?: "",
                    bio = getDeepString("bio") ?: "",
                    photoUrl = getDeepString("photoUrl") ?: getDeepString("imageUrl") ?: getDeepString("photo"),
                    bannerImageUrl = getDeepString("bannerImageUrl") ?: getDeepString("bannerUrl"),
                    isVerified = getDeepBoolean("isVerified"),
                    isOnline = getDeepBoolean("isOnline"),
                    isSubscribed = getDeepBoolean("isSubscribed"),
                    isPublicProfile = getDeepBoolean("isPublicProfile"),
                    rating = (perfil["rating"] as? Number ?: data["rating"] as? Number)?.toFloat() ?: 0f,
                    galleryImages = ((data["galleryImages"] ?: perfil["galleryImages"]) as? List<*>)?.map { it.toString() } ?: emptyList(),
                    hasCompanyProfile = getDeepBoolean("hasCompanyProfile"),
                    createdAt = (perfil["createdAt"] as? Number ?: data["createdAt"] as? Number)?.toLong() ?: 0L
                )
                trySend(profile)
            }
        awaitClose { listener.remove() }
    }
}
