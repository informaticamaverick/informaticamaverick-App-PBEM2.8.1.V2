package com.example.myapplication.prestador.ui.pantallas.register

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.core.datos.repositorios.CategoriaRepositorio
import com.example.myapplication.prestador.datos.repositorios.PrestadorAutenticacionRepositorio
import com.example.myapplication.prestador.dominio.motores.MotorPerfilPrestadorDeep
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.example.myapplication.core.utilidades.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * --- ESTADO DEL REGISTRO (UI) ---
 */
sealed class EstadoRegistro {
    object Idle : EstadoRegistro()
    object Cargando : EstadoRegistro()
    object Exito : EstadoRegistro()
    data class Error(val mensaje: String) : EstadoRegistro()
}

/**
 * --- VIEWMODEL DE REGISTRO PRESTADOR (PRE) ---
 * 
 * [PROPÓSITO]: Orquestar el alta de nuevos profesionales, asegurando la 
 * integridad de los 5 Pilares (Identidad, Empresa, Sucursal).
 */
@HiltViewModel
class PrestadorRegisterViewModel @Inject constructor(
    @ApplicationContext private val contexto: android.content.Context,
    private val authRepository: PrestadorAutenticacionRepositorio,
    private val motorDeep: MotorPerfilPrestadorDeep,
    private val categoryRepository: CategoriaRepositorio
) : ViewModel() {

    private val _estadoRegistro = MutableStateFlow<EstadoRegistro>(EstadoRegistro.Idle)
    val estadoRegistro: StateFlow<EstadoRegistro> = _estadoRegistro.asStateFlow()

    private val _estaDetectandoUbicacion = MutableStateFlow(false)
    val estaDetectandoUbicacion: StateFlow<Boolean> = _estaDetectandoUbicacion.asStateFlow()

    val servicios = categoryRepository.todasLasCategorias
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val clienteUbicacion = LocationServices.getFusedLocationProviderClient(contexto)

    // --- SECTOR: ACCIONES ---

    @android.annotation.SuppressLint("MissingPermission")
    fun detectarUbicacionActual(alRecibir: (DireccionDominio) -> Unit) {
        viewModelScope.launch {
            _estaDetectandoUbicacion.value = true
            try {
                val loc = clienteUbicacion.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                loc?.let {
                    val dir = com.example.myapplication.core.utilidades.GeoUtils.obtenerDireccionDesdeCoordenadas(contexto, it.latitude, it.longitude)
                    dir?.let { d -> alRecibir(d) }
                }
            } catch (e: Exception) {
                Log.e("RegisterVM", "Error GPS: ${e.message}")
            } finally {
                _estaDetectandoUbicacion.value = false
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
        isGoogleUser: Boolean,
        profileImageUri: Uri?,
        telefono: String,
        dniCuit: String,
        calle: String,
        numero: String,
        localidad: String,
        provincia: String,
        codigoPostal: String,
        latitud: Double,
        longitud: Double,
        tieneNegocio: Boolean,
        nombreNegocio: String,
        razonSocial: String,
        cuitNegocio: String,
        sucursales: List<Map<String, String>>,
        isHomeService: Boolean,
        is24Hours: Boolean,
        hasPhysicalStore: Boolean,
        doesService: Boolean,
        doesProduct: Boolean
    ) {
        viewModelScope.launch {
            _estadoRegistro.value = EstadoRegistro.Cargando
            
            var rutaLocalFoto: String? = null
            var miniaturaBase64: String? = null

            // 1. Procesamiento de Imagen (v2026.ELITE)
            profileImageUri?.let { uri ->
                try {
                    val originalBytes = ImageUtils.getBytesFromUri(contexto, uri)
                    originalBytes?.let { bytes ->
                        // [SUPREME]: Usamos compresión de alta fidelidad para el perfil principal
                        val compressed = ImageUtils.compressSupreme(contexto, uri) 
                        val finalBytes = if (uri.scheme?.startsWith("http") == true) {
                            ImageUtils.compressBytesToWebP(bytes, 1200, 1200, 90)
                        } else compressed

                        finalBytes?.let { 
                            rutaLocalFoto = ImageUtils.saveBytesToFile(contexto, it, "perfil_${System.currentTimeMillis()}")
                        }
                        
                        miniaturaBase64 = if (uri.scheme?.startsWith("http") == true) {
                            ImageUtils.generateThumbnailFromBytes(bytes)
                        } else ImageUtils.generateThumbnailBase64(contexto, uri)
                    }
                    Log.d("RegisterVM", "📸 [PHOTO_PROCESSED] Miniatura: ${miniaturaBase64?.length} | Local: $rutaLocalFoto")
                } catch (e: Exception) {
                    android.util.Log.e("RegisterVM", "❌ [PHOTO_ERROR] ${e.message}")
                }
            }

            val uid = authRepository.obtenerUsuarioActual()?.uid ?: throw Exception("Sesión no iniciada")

            val dir = DireccionDominio(
                id = java.util.UUID.randomUUID().toString(),
                idPropietario = uid,
                idReferencia = uid,
                calle = calle, 
                numero = numero, 
                localidad = localidad, 
                provincia = provincia, 
                codigoPostal = codigoPostal, 
                latitud = latitud, 
                longitud = longitud,
                tieneLocalFisico = hasPhysicalStore
            )
            
            try {
                // 2. Construir Ecosistema Maestro (v2026.DEEP)
                val cuenta = CuentaEntity(
                    id = uid,
                    correoGoogle = email,
                    idPerfilActivo = if (tieneNegocio) null else uid, // Se actualizará después
                    ultimaSincronizacion = System.currentTimeMillis()
                )

                val perfilIndividual = PrestadorDominio(
                    id = uid,
                    idPropietario = uid,
                    titulo = "$nombre $apellido",
                    biografia = mensaje,
                    correo = email,
                    numeroTelefono = telefono,
                    cuitCuil = dniCuit,
                    urlFoto = rutaLocalFoto,
                    urlMiniatura = miniaturaBase64,
                    brindaServicio = doesService,
                    brindaProducto = doesProduct,
                    atiende24h = is24Hours,
                    visitaADomicilio = isHomeService,
                    brindaTurnos = true,
                    idCategorias = listOf(categoria),
                    direcciones = listOf(dir)
                )

                val empresasList = mutableListOf<EmpresaDominioCompleto>()

                if (tieneNegocio && !nombreNegocio.isNullOrBlank()) {
                    val idEmpresa = java.util.UUID.randomUUID().toString()
                    val empresa = EmpresaDominio(
                        id = idEmpresa,
                        idPropietario = uid,
                        nombre = nombreNegocio,
                        razonSocial = razonSocial,
                        cuit = cuitNegocio,
                        urlFoto = rutaLocalFoto,
                        urlMiniatura = miniaturaBase64,
                        idCategorias = listOf(categoria)
                    )

                    // 4. Pilar: Sucursales
                    val sucursalesCompletas = sucursales.map { data ->
                        val idSuc = java.util.UUID.randomUUID().toString()
                        // [SUPREME]: Heredamos la dirección del wizard a la sucursal por defecto
                        val dirSuc = dir.copy(id = java.util.UUID.randomUUID().toString(), idPropietario = idSuc, idReferencia = idSuc)
                        
                        SucursalDominioCompleto(
                            sucursal = SucursalDominio(
                                id = idSuc,
                                idPropietario = uid,
                                idEmpresaPadre = idEmpresa,
                                nombre = data["nombre"] ?: "Sucursal Central",
                                atiende24Horas = is24Hours,
                                visitaADomicilio = isHomeService,
                                brindaTurnos = true,
                                brindaServicio = doesService,
                                brindaProducto = doesProduct
                            ),
                            direccion = dirSuc
                        )
                    }

                    empresasList.add(EmpresaDominioCompleto(empresa, sucursalesCompletas))
                }

                val ecosistema = PerfilPrestadorDeepModelo(
                    cuenta = if (empresasList.isNotEmpty()) cuenta.copy(idPerfilActivo = empresasList.first().empresa.id, priorizarEmpresa = true) else cuenta.copy(idPerfilActivo = uid, priorizarEmpresa = false),
                    prestador = PrestadorDominioCompleto(perfilIndividual, listOf(dir)),
                    empresas = empresasList
                )

                // 5. Impacto Atómico (Room -> Firebase -> Search Index)
                Log.d("RegisterVM", "🚀 [ATOMIC_IMPACT] Iniciando cadena de impacto profundo para: $uid")
                motorDeep.impactarEcosistemaYActualizarIndices(ecosistema)

                _estadoRegistro.value = EstadoRegistro.Exito

            } catch (e: Exception) {
                Log.e("RegisterVM", "❌ [FATAL_REG] ${e.message}", e)
                _estadoRegistro.value = EstadoRegistro.Error(e.message ?: "Error en el registro")
            }
        }
    }

    fun resetState() { _estadoRegistro.value = EstadoRegistro.Idle }
    
    val registerState = estadoRegistro
    val loadingServicios = MutableStateFlow(false)
}

















































