package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.dao.AppointmentDao
import com.example.myapplication.prestador.data.local.entity.AppointmentEntity
import com.example.myapplication.prestador.data.model.ClienteDireccion
import com.example.myapplication.prestador.data.model.ClienteEmpresa
import com.example.myapplication.prestador.data.model.ClienteProfile
import com.example.myapplication.prestador.data.model.ClienteSucursal
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class ClientePerfilUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val profile: ClienteProfile = ClienteProfile(),
    val appointments: List<AppointmentEntity> = emptyList()
)

@HiltViewModel
class ClientePerfilViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val firestore: FirebaseFirestore,
    private val appointmentDao: AppointmentDao
) : ViewModel() {

    private val clientId: String = checkNotNull(savedStateHandle["clientId"])

    private val _uiState = MutableStateFlow(ClientePerfilUiState())
    val uiState: StateFlow<ClientePerfilUiState> = _uiState.asStateFlow()

    init {
        loadClienteProfile()
        loadAppointmentHistory()
    }

    private fun loadClienteProfile() {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("usuarios")
                    .document(clientId)
                    .get()
                    .await()
                if (!doc.exists()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No se encontró el perfil del cliente"
                    )
                    return@launch
                }

                val data = doc.data ?: emptyMap()

                // Leer direcciones desde la subcolección personalAddresses
                val addressesSnapshot = firestore.collection("usuarios")
                    .document(clientId)
                    .collection("personalAddresses")
                    .get()
                    .await()
                val addresses = addressesSnapshot.documents.map { addrDoc ->
                    val a = addrDoc.data ?: emptyMap()
                    ClienteDireccion(
                        label = a["label"] as? String ?: "",
                        calle = a["calle"] as? String ?: "",
                        numero = a["numero"] as? String ?: "",
                        localidad = a["localidad"] as? String ?: "",
                        provincia = a["provincia"] as? String ?: "",
                        pais = a["pais"] as? String ?: "",
                        codigoPostal = a["codigoPostal"] as? String?: "",
                        latitude = (a["latitude"] as? Number)?.toDouble() ?: 0.0,
                        longitude = (a["longitude"] as? Number)?.toDouble() ?: 0.0
                    )
                }

               //Leer empresas desde la subcolección companies
                val hasCompanyProfile = data["hasCompanyProfile"] as? Boolean ?: false
                    val companiesSnapshot = firestore.collection("usuarios")
                        .document(clientId)
                        .collection("companies")
                        .get()
                        .await()
                    val companies = companiesSnapshot.documents.map { compDoc ->
                        val c = compDoc.data ?: emptyMap()
                        //Leer sucursales desde la subcoleccion branches
                        val branchesSnapshot = firestore.collection("usuarios")
                            .document(clientId)
                            .collection("companies")
                            .document(compDoc.id)
                            .collection("branches")
                            .get()
                            .await()
                        val branches = branchesSnapshot.documents.map { branchDoc ->
                            val b = branchDoc.data ?: emptyMap()

                            @Suppress("UNCHECKED_CAST")
                            val addr = b["address"] as? Map<String, Any> ?: emptyMap()
                            ClienteSucursal(
                                id = b["id"] as? String ?: branchDoc.id,
                                name = b["name"] as? String ?: "",
                                isMainBranch = b["isMainBranch"] as? Boolean ?: false,
                                galleryImages = b["galleryImages"] as? List<String> ?: emptyList(),
                                address = ClienteDireccion(
                                    label = addr["label"] as? String ?: "",
                                    calle = addr["calle"] as? String ?: "",
                                    numero = addr["numero"] as? String ?: "",
                                    localidad = addr["localidad"] as? String ?: "",
                                    provincia = addr["provincia"] as? String ?: "",
                                    pais = addr["pais"] as? String ?: "",
                                    codigoPostal = addr["codigoPostal"] as? String ?: ""
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
                val galleryImages = data["galleryImages"] as? List<String> ?: emptyList()

                val profile = ClienteProfile(
                    clientId = clientId,
                    name = data["name"] as? String ?: "",
                    lastName = data["lastName"] as? String ?: "",
                    displayName = data["displayName"] as? String ?: "",
                    email = data["email"] as? String ?: "",
                    phoneNumber = data["phoneNumber"] as? String ?: "",
                    bio = data["bio"] as? String ?: "",
                    photoUrl = data["photoUrl"] as? String,
                    bannerImageUrl = data["bannerImageUrl"] as? String,
                    isVerified = data["isVerified"] as? Boolean ?: false,
                    isOnline = data["isOnline"] as? Boolean ?: false,
                    isSubscribed = data["isSubscribed"] as? Boolean ?: false,
                    isPublicProfile = data["isPublicProfile"] as? Boolean ?: false,
                    rating = (data["rating"] as? Number)?.toFloat() ?: 0f,
                    galleryImages = galleryImages,
                    personalAddresses = addresses,
                    hasCompanyProfile = hasCompanyProfile,
                    companies = companies,
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L
                )

                _uiState.value = _uiState.value.copy(isLoading = false, profile = profile)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar el perfil: ${e.message}"
                )
            }
        }
    }

    private fun loadAppointmentHistory() {
        viewModelScope.launch {
            appointmentDao.getAppointmentsByClient(clientId).collect { list ->
                _uiState.value = _uiState.value.copy(appointments = list)
            }
        }
    }
}
