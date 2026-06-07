package com.example.myapplication.prestador.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.local.dao.ProviderDao
//import com.example.myapplication.prestador.data.local.entity.BusinessEntity
//import com.example.myapplication.prestador.data.local.entity.ProviderEntity
//import com.example.myapplication.prestador.data.local.entity.SucursalEntity
import com.example.myapplication.core.domain.model.AddressUnico
import com.example.myapplication.core.domain.model.BranchProvider
import com.example.myapplication.core.domain.model.CompanyProvider
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.data.repository.ProviderRepository
import com.example.myapplication.core.data.repository.CategoryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.first
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PrestadorRegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val providerDao: ProviderDao,
    private val categoryRepository: CategoryRepository,
    // =========================================================================
    // SECCIÓN: REPOSITORIOS (SSOT)
    // Se utiliza el repositorio centralizado del módulo :core
    // =========================================================================
    private val providerRepository: ProviderRepository,
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    private val _servicios = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val servicios: StateFlow<List<CategoryEntity>> = _servicios

    private val _loadingServicios = MutableStateFlow(false)
    val loadingServicios: StateFlow<Boolean> = _loadingServicios

    init {
        cargarServicios()
    }

    fun cargarServicios() {
        viewModelScope.launch {
            _loadingServicios.value = true
            // [LEY #6]: Soberanía Local de Catálogos
            categoryRepository.allCategories.collect { list ->
                _servicios.value = list
                _loadingServicios.value = false
            }
        }
    }

    fun register(
        email: String,
        password: String,
        nombre: String,
        apellido: String,
        categoria: String,
        mensaje: String,
        serviceType: String,
        isGoogleUser: Boolean = false,
        telefono: String = "",
        dniCuit: String = "",
        matricula: String = "",
        profesion: String = "",
        direccion: String = "",
        codigoPostal: String = "",
        provincia: String = "",
        tieneNegocio: Boolean = false,
        nombreNegocio: String = "",
        razonSocial: String = "",
        cuitNegocio: String = "",
        direccionNegocio: String = "",
        codigoPostalNegocio: String = "",
        sucursales: List<Map<String, String>> = emptyList(),
        isHomeService: Boolean = false,
        is24Hours: Boolean = false,
        hasPhysicalStore: Boolean = false,
        hasStoreAppointments: Boolean = false,
        doesService: Boolean = false,
        doesProduct: Boolean = false
    ) {

        //Validar campos antes de llamar Firebase
        if (!isGoogleUser) {
            if (email.isBlank() || !email.contains("@")) {
               _registerState.value = RegisterState.Error("Ingresá un correo electrónico valido")
                return
            }
            if (password.length < 6) {
                _registerState.value = RegisterState.Error("La contraseña debe tener al menso 6 caracteres")
                return
            }
        }

        if (nombre.isBlank()) {
            _registerState.value = RegisterState.Error("Ingresa tunombre")
            return
        }

        viewModelScope.launch {
            _registerState.value = RegisterState.Loading

            try {
                val serviciosList = listOf(categoria).filter { it.isNotBlank() }
                
                // 1. Obtener o crear el UID del usuario
                val userId = if (isGoogleUser) {
                    auth.currentUser?.uid ?: throw Exception("Usuario de Google no encontrado")
                } else {
                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                    result.user?.uid ?: throw Exception("Error al crear usuario")
                }

                // =========================================================================
                // SECCIÓN: CONSTRUCCIÓN JERÁRQUICA (SSOT)
                // Se construye el objeto Provider siguiendo el nuevo modelo jerárquico
                // =========================================================================
                
                // A. Dirección Principal
                val mainAddress = AddressUnico(
                    id = "main_address",
                    calle = direccion,
                    provincia = provincia,
                    codigoPostal = codigoPostal,
                    pais = "Argentina"
                )

                // B. Sucursales (si existen)
                val branchProviders = sucursales.map { sucMap ->
                    BranchProvider(
                        id = UUID.randomUUID().toString(),
                        name = sucMap["nombre"] ?: "Sucursal",
                        address = AddressUnico(calle = sucMap["direccion"] ?: ""),
                        hasPhysicalLocation = true,
                        acceptsAppointments = true,
                        doesService = true
                    )
                }

                // C. Empresa (si tieneNegocio es true)
                val companyProviders = if (tieneNegocio) {
                    listOf(CompanyProvider(
                        id = UUID.randomUUID().toString(),
                        name = nombreNegocio,
                        razonSocial = razonSocial,
                        cuit = cuitNegocio,
                        branches = branchProviders
                    ))
                } else emptyList()

                // D. Objeto Provider de Dominio
                val providerToSync = com.example.myapplication.core.domain.model.Provider(
                    uid = userId,
                    email = if (isGoogleUser) (auth.currentUser?.email ?: email) else email,
                    displayName = "$nombre $apellido".trim(),
                    name = nombre,
                    lastName = apellido,
                    phoneNumber = telefono,
                    cuilCuit = dniCuit,
                    profesion = profesion,
                    matricula = matricula,
                    address = mainAddress,
                    addresses = listOf(mainAddress),
                    companies = companyProviders,
                    works24h = is24Hours,
                    doesHomeVisits = isHomeService,
                    hasPhysicalLocation = hasStoreAppointments,
                    acceptsAppointments = hasStoreAppointments,
                    doesService = doesService,
                    doesProduct = doesProduct,
                    categories = serviciosList,
                    serviceType = serviceType,
                    priorizarEmpresa = tieneNegocio,
                    createdAt = System.currentTimeMillis()
                )

                // =========================================================================
                // SECCIÓN: SINCRONIZACIÓN ATÓMICA
                // Única llamada para persistencia en Room y Firestore (Jerarquía completa)
                // =========================================================================
                providerRepository.syncProviderWithFirebase(providerToSync)

                // Guardar FCM token ahora que el documento existe en Firestore
                com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token ->
                        firestore.collection("providers").document(userId)
                            .update("fcmToken", token)
                            .addOnFailureListener { /* ignorar */ }
                    }

                _registerState.value = RegisterState.Success
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Error al registrar")
            }
        }
    }

    fun resetState() {
        _registerState.value = RegisterState.Idle
    }



    fun setPriorizarEmpresa(value: Boolean) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            firestore.collection("providers").document(userId)
                .update("empresa.priorizarEmpresa", value)
                .await()
        }
    }
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()


}

