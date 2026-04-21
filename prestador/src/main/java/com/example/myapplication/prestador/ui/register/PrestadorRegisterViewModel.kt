package com.example.myapplication.prestador.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.dao.ProviderDao
import com.example.myapplication.prestador.data.local.entity.BusinessEntity
import com.example.myapplication.prestador.data.local.entity.ProviderEntity
import com.example.myapplication.prestador.data.local.entity.SucursalEntity
import com.example.myapplication.prestador.data.model.AddressProvider
import com.example.myapplication.prestador.data.model.BranchProvider
import com.example.myapplication.prestador.data.model.CompanyProvider
import com.example.myapplication.prestador.data.model.ServicioFirebase
import com.example.myapplication.prestador.data.repository.BusinessRepository
import com.example.myapplication.prestador.data.repository.CompaniesFirestoreSync
import com.example.myapplication.prestador.data.repository.ServiciosRepository
import com.example.myapplication.prestador.data.repository.SucursalFirestoreSync
import com.example.myapplication.prestador.data.repository.SucursalRepository
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
    private val serviciosRepository: ServiciosRepository,
    // =========================================================================
    // SECCIÓN: REPOSITORIOS (SSOT)
    // Se añade el repositorio centralizado para la arquitectura jerárquica
    // =========================================================================
    private val providerRepository: com.example.myapplication.prestador.data.repository.ProviderRepository,
    /* 
    private val businessRepository: BusinessRepository,
    private val sucursalRepository: SucursalRepository,
    private val companiesFirestoreSync: CompaniesFirestoreSync,
    private val sucursalFirestoreSync: SucursalFirestoreSync 
    */
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    private val _servicios = MutableStateFlow<List<ServicioFirebase>>(emptyList())
    val servicios: StateFlow<List<ServicioFirebase>> = _servicios

    private val _loadingServicios = MutableStateFlow(false)
    val loadingServicios: StateFlow<Boolean> = _loadingServicios

    init {
        cargarServicios()
    }

    fun cargarServicios() {
        viewModelScope.launch {
            _loadingServicios.value = true
            try {
                _servicios.value = serviciosRepository.getServicios()
            } catch (e: Exception) {
                //Si falla Firebase, la lista queda vacia
            }finally {
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
                val mainAddress = AddressProvider(
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
                        address = AddressProvider(calle = sucMap["direccion"] ?: ""),
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
                val providerToSync = com.example.myapplication.prestador.data.model.Provider(
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
                    hasCompanyProfile = tieneNegocio,
                    categories = serviciosList,
                    serviceType = serviceType,
                    works24h = is24Hours,
                    doesHomeVisits = isHomeService,
                    hasPhysicalLocation = hasStoreAppointments,
                    acceptsAppointments = hasStoreAppointments,
                    doesService = doesService,
                    doesProduct = doesProduct,
                    createdAt = System.currentTimeMillis()
                )

                // =========================================================================
                // SECCIÓN: SINCRONIZACIÓN ATÓMICA
                // Única llamada para persistencia en Room y Firestore (Jerarquía completa)
                // =========================================================================
                providerRepository.syncProviderWithFirebase(providerToSync)

                _registerState.value = RegisterState.Success
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Error al registrar")
            }
        }
    }

    fun resetState() {
        _registerState.value = RegisterState.Idle
    }

    // =========================================================================
    // SECCIÓN: MÉTODOS OBSOLETOS (SSOT)
    // Se comentan para evitar duplicidad, ya que syncProviderWithFirebase
    // maneja toda la jerarquía de forma atómica.
    // =========================================================================
    /*
    private suspend fun saveCompanyAndBranches(
        userId: String,
        nombreNegocio: String,
        razonSocial: String,
        cuitNegocio: String,
        direccionNegocio: String,
        codigoPostalNegocio: String,
        sucursales: List<Map<String, String>>
    ) {
        val companyId = UUID.randomUUID().toString()
        val business = BusinessEntity(
            id = companyId,
            providerId = userId,
            nombreNegocio = nombreNegocio,
            razonSocial = razonSocial,
            cuitNegocio = cuitNegocio,
            direccion = direccionNegocio,
            codigoPostal = codigoPostalNegocio,
            createdAt = System.currentTimeMillis()
        )
        businessRepository.saveBusiness(business)
        companiesFirestoreSync.subirCompany(business, userId)

        sucursales.forEach { sucMap ->
            val sucId = UUID.randomUUID().toString()
            val sucursal = SucursalEntity(
                id = sucId,
                businessId = companyId,
                nombre = sucMap["nombre"] ?: "Sucursal",
                direccionId = sucMap["direccion"], // Podría ser un ID o string temporal
                telefono = sucMap["telefono"],
                doesService = true, // Valores por defecto para sucursal inicial
                hasPhysicalLocation = true,
                acceptsAppointments = true
            )
            sucursalRepository.saveSucursal(sucursal)
            sucursalFirestoreSync.subirSucursal(sucursal, userId)
        }
    }

    private suspend fun saveProviderToRoom(
        id: String,
        nombre: String,
        apellido: String,
        email: String,
        telefono: String,
        mensaje: String,
        servicios: List<String>,
        serviceType: String,
        dniCuit: String = "",
        profesion: String = "",
        matricula: String = "",
        tieneEmpresa: Boolean = false,
        direccion: String = "",
        provincia: String = "",
        codigoPostal: String = "",
        atencionUrgencias: Boolean = false,
        vaDomicilio: Boolean = false,
        turnosEnLocal: Boolean = false,
        doesService: Boolean = false,
        doesProduct: Boolean = false
    ) {
        val mainAddress = AddressProvider(
            id = "main",
            calle = direccion,
            numero = "",
            localidad = "",
            provincia = provincia,
            pais = "Argentina",
            codigoPostal = codigoPostal
        )

        // Obtener empresas y sucursales guardadas para ProviderEntity
        val companiesList = if (tieneEmpresa) {
            val businesses = businessRepository.getBusinessesByProvider(id).first()
            businesses.map { b ->
                val branches = sucursalRepository.getSucursalesByBusiness(b.id).first()
                CompanyProvider(
                    id = b.id,
                    name = b.nombreNegocio,
                    razonSocial = b.razonSocial,
                    cuit = b.cuitNegocio,
                    description = b.descripcion ?: "",
                    branches = branches.map { s ->
                        BranchProvider(
                            id = s.id,
                            name = s.nombre,
                            address = AddressProvider(calle = s.direccionId ?: ""),
                            doesService = s.doesService,
                            doesProduct = s.doesProduct,
                            works24h = s.works24h,
                            hasPhysicalLocation = s.hasPhysicalLocation,
                            doesHomeVisits = s.doesHomeVisits,
                            doesShipping = s.doesShipping,
                            acceptsAppointments = s.acceptsAppointments,
                            rating = s.rating,
                            galleryImages = try { org.json.JSONArray(s.galleryImages).let { arr -> (0 until arr.length()).map { arr.getString(it) } } } catch(e: Exception) { emptyList() }
                        )
                    }
                )
            }
        } else emptyList()

        val providerEntity = ProviderEntity(
            id = id,
            name = nombre,
            lastName = apellido,
            displayName = "$nombre $apellido".trim(),
            email = email,
            phoneNumber = telefono,
            photoUrl = null,
            description = mensaje,
            cuilCuit = dniCuit,
            profesion = profesion,
            matricula = matricula,
            address = mainAddress,
            addresses = listOf(mainAddress),
            companies = companiesList,
            hasCompanyProfile = tieneEmpresa,
            rating = 0f,
            categories = servicios,
            works24h = atencionUrgencias,
            doesHomeVisits = vaDomicilio,
            hasPhysicalLocation = turnosEnLocal,
            acceptsAppointments = turnosEnLocal,
            doesService = doesService,
            doesProduct = doesProduct,
            createdAt = System.currentTimeMillis()
        )

        providerDao.insertProvider(providerEntity)
    }
    */
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}
