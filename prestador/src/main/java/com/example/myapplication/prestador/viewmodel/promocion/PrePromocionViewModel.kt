package com.example.myapplication.prestador.viewmodel.promocion

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.core.dominio.mapeadores.PromocionMappers
import com.example.myapplication.core.datos.repositorios.PromocionRepositorio
import com.example.myapplication.core.datos.repositorios.CategoriaRepositorio
import com.example.myapplication.prestador.dominio.motores.MotorPromocionPrestador
import com.example.myapplication.prestador.datos.repositorios.PrestadorPromocionRepositorio
import com.example.myapplication.prestador.datos.repositorios.ConsultasPrestadorRepositorio
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * --- VIEWMODEL DE PROMOCIONES PRESTADOR (ELITE v2026.FINAL) ---
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PrePromocionViewModel @Inject constructor(
    private val promotionRepository: PromocionRepositorio,
    private val prestadorPromocionRepositorio: PrestadorPromocionRepositorio,
    private val motorPromocion: MotorPromocionPrestador,
    private val consultasRepo: ConsultasPrestadorRepositorio,
    private val categoryRepo: CategoriaRepositorio,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val categoriasMap = categoryRepo.todasLasCategorias
        .map { lista -> lista.associateBy({ it.id }, { it.nombre }) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun limpiarError() { _error.value = null }

    private val _idPerfilActivo = MutableStateFlow<String?>(null)
    val idPerfilActivo = _idPerfilActivo.asStateFlow()

    private val _perfilActivo = MutableStateFlow<PrestadorDominio?>(null)
    val perfilActivo = _perfilActivo.asStateFlow()

    val todasMisIdentidades: StateFlow<List<PrestadorDominio>> = auth.currentUser?.uid?.let { uid ->
        consultasRepo.obtenerPerfilPrestadorDeepFlujo(uid).map { maestro ->
            maestro?.aModelosUi() ?: emptyList()
        }
    }?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()) 
        ?: MutableStateFlow(emptyList<PrestadorDominio>()).asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val rubrosDisponibles: StateFlow<List<Pair<String, String>>> = combine(_perfilActivo, categoriasMap) { perfil, catMap ->
        perfil?.idCategorias?.map { id ->
            id to (catMap[id] ?: id)
        } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setPerfilPublicador(perfil: PrestadorDominio) {
        _perfilActivo.value = perfil
        android.util.Log.d("PrePromocionVM", "🎭 [PUBLISHER_CHANGE] Identidad seleccionada: ${perfil.titulo} (${perfil.id})")
    }

    init {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { uid ->
                consultasRepo.obtenerPerfilPrestadorDeepFlujo(uid).collect { maestro ->
                    val idNuevo = maestro?.cuenta?.idPerfilActivo ?: uid
                    _idPerfilActivo.value = idNuevo

                    val identidades = maestro?.aModelosUi() ?: emptyList()
                    val perfilEncontrado = identidades.find { it.id == idNuevo } ?: identidades.firstOrNull()
                    _perfilActivo.value = perfilEncontrado
                    
                    android.util.Log.d("PrePromocionVM", "👤 [PERFIL_ACTIVO] ID: $idNuevo | Rubros: ${perfilEncontrado?.idCategorias}")
                    refreshMyPromotions()
                }
            }
        }
    }

    private val _loadedPromotion = MutableStateFlow<PromocionDominio?>(null)
    val loadedPromotion = _loadedPromotion.asStateFlow()

    val misPublicaciones: StateFlow<List<Promocion>> = _idPerfilActivo.flatMapLatest { id ->
        if (id == null) flowOf(emptyList<Promocion>())
        else promotionRepository.obtenerPromocionesPorPrestador(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refreshMyPromotions() {
        viewModelScope.launch {
            val id = _idPerfilActivo.value
            if (id != null) {
                _estaCargando.value = true
                prestadorPromocionRepositorio.sincronizarMisPromociones(id)
                _estaCargando.value = false
            }
        }
    }

    fun getMyPromotions(uid: String): Flow<List<PromocionDominio>> {
        return promotionRepository.obtenerPromocionesPorPrestador(uid)
            .map { list -> list.map { PromocionMappers.aUiModel(it) } }
    }

    fun deletePromotion(id: String) {
        viewModelScope.launch {
            _estaCargando.value = true
            try {
                motorPromocion.eliminarPromocion(id)
            } catch (e: Exception) {
                android.util.Log.e("PrePromocionVM", "❌ Fallo al eliminar promoción: ${e.message}", e)
                _error.value = "No se pudo eliminar: ${e.message ?: "error desconocido"}"
            } finally {
                _estaCargando.value = false
            }
        }
    }

    fun republishPromotion(promo: Promocion) {
        viewModelScope.launch {
            _estaCargando.value = true
            try {
                val nuevaPromo = promo.copy(
                    id = UUID.randomUUID().toString(),
                    fechaCreacion = System.currentTimeMillis(),
                    fechaExpiracion = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)
                )
                motorPromocion.publicarNuevaPromocion(nuevaPromo)
            } finally {
                _estaCargando.value = false
            }
        }
    }

    fun loadPromotion(id: String) {
        viewModelScope.launch {
            promotionRepository.obtenerPromocionPorId(id).collect {
                _loadedPromotion.value = it?.let { p -> PromocionMappers.aUiModel(p) }
            }
        }
    }

    fun getComments(promotionId: String): Flow<List<PromocionComentario>> = 
        promotionRepository.obtenerComentarios(promotionId)

    fun createPromotion(
        idPrestador: String,
        nombrePrestador: String,
        urlFotoPrestador: String?,
        tipo: TipoPromocion,
        tipoCategoria: TipoCategoriaPromo,
        titulo: String,
        descripcion: String,
        urisImagenes: List<Uri>,
        descuento: String?,
        etiquetaPromocion: String?,
        etiquetas: List<String> = emptyList(),
        idCategorias: Set<String> = emptySet(),
        codigoPostal: String? = null,
        estaVerificado: Boolean = false,
        idEmpresa: String? = null,
        idSucursal: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _estaCargando.value = true
            try {
                val ahora = System.currentTimeMillis()
                val duracionMs = if (tipo == TipoPromocion.HISTORIA) 24 * 3600000L else 7 * 24 * 3600000L

                val promo = Promocion(
                    id = UUID.randomUUID().toString(),
                    idPrestador = idPrestador,
                    nombrePrestador = nombrePrestador,
                    urlFotoPrestador = urlFotoPrestador,
                    tipo = tipo,
                    tipoPromocion = tipoCategoria,
                    titulo = titulo,
                    descripcion = descripcion,
                    urlImagenes = urisImagenes.map { it.toString() },
                    porcentajeDescuento = descuento?.toIntOrNull(),
                    etiquetaPromocion = etiquetaPromocion,
                    etiquetas = etiquetas.mapNotNull { EtiquetaPromo.desdeNombre(it) },
                    idCategorias = idCategorias.toList(),
                    codigoPostal = codigoPostal,
                    estaVerificado = estaVerificado,
                    idEmpresa = idEmpresa,
                    idSucursal = idSucursal,
                    fechaCreacion = ahora,
                    fechaExpiracion = ahora + duracionMs
                )
                motorPromocion.publicarNuevaPromocion(promo)
                onSuccess()
            } catch (e: Exception) {
                // [ELITE]: si falla la subida de imágenes (p.ej. Storage sin emulador/sin red),
                // no dejamos que la excepción se propague sin atajar — eso tumbaba toda la app.
                android.util.Log.e("PrePromocionVM", "❌ Fallo al publicar promoción: ${e.message}", e)
                _error.value = "No se pudo publicar: ${e.message ?: "error desconocido"}"
            } finally {
                _estaCargando.value = false
            }
        }
    }
}


