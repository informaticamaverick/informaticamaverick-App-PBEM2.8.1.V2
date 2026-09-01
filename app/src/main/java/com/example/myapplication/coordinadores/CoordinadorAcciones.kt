package com.example.myapplication.coordinadores

import com.example.myapplication.core.utilidades.SensorEstadoGps
import com.example.myapplication.core.utilidades.normalizeFull
import android.util.Log
import com.example.myapplication.core.dominio.descubrimiento.GeneradorTópicosFCM
import com.example.myapplication.core.dominio.descubrimiento.ProtocoloPrefijos
import com.example.myapplication.core.dominio.ubicacion.NormalizadorDirecciones
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.dominio.modelos.CategoriaDominio
import com.example.myapplication.core.dominio.modelos.SuperCategoriaDominio
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.core.dominio.repository.TopicoRepositorio
import com.example.myapplication.datos.repositorios.ConsultasUsuarioRepositorio
import com.example.myapplication.core.datos.local.dao.CategoriaDao
import com.example.myapplication.core.datos.local.dao.SuscripcionTopicDao
import com.example.myapplication.core.datos.local.entidades.SuscripcionTopicEntity
import com.example.myapplication.core.datos.repositorios.UbicacionGpsRepositorio
import com.example.myapplication.ui.componentes.be.modelos.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@Singleton
class CoordinadorAcciones @Inject constructor(
    private val consultasUserRepo: ConsultasUsuarioRepositorio,
    private val categoryDao: CategoriaDao,
    private val suscripcionDao: SuscripcionTopicDao,
    private val repositorioTopic: TopicoRepositorio,
    private val generadorTopicos: GeneradorTópicosFCM,
    private val hardwareProvider: SensorEstadoGps,
    private val auth: FirebaseAuth,
    val beBusquedaMotor: com.example.myapplication.core.dominio.motores.BeBusquedaMotor, // 🔥 [PUBLIC]
    private val ubicacionGpsRepo: UbicacionGpsRepositorio,
    val navCoordinador: CoordinadorNavegacion // 🔥 [NEW]
) {
    private val alcance = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main.immediate)
    private val topicosActivos = mutableSetOf<String>()
    private val mutexTopicos = Mutex()

    val estaWifiActivado = hardwareProvider.estaWifiHabilitado.stateIn(alcance, SharingStarted.WhileSubscribed(5000), true)
    val estaGpsActivado = hardwareProvider.estaGpsHabilitado.stateIn(alcance, SharingStarted.WhileSubscribed(5000), true)
    val estaEnLinea = hardwareProvider.estaEnLinea.stateIn(alcance, SharingStarted.WhileSubscribed(5000), true)

    private val _modoGpsActivo = MutableStateFlow(false)
    val modoGpsActivo = _modoGpsActivo.asStateFlow()

    private val _eventoAccion = MutableSharedFlow<String>()
    val eventoAccion = _eventoAccion.asSharedFlow()

    private val _toastActivo = MutableStateFlow<BeToastState?>(null)
    val toastActivo = _toastActivo.asStateFlow()

    fun mostrarToast(mensaje: String, tipo: TipoBeToast, duracionMs: Long = 3000L) {
        alcance.launch {
            _toastActivo.value = BeToastState(mensaje, tipo, duracionMs)
            if (duracionMs > 0) {
                delay(duracionMs.milliseconds)
                if (_toastActivo.value?.mensaje == mensaje) {
                    _toastActivo.value = null
                }
            }
        }
    }

    fun ocultarToast() {
        _toastActivo.value = null
    }

    private val _tieneCoincidencias = MutableStateFlow(true)
    val tieneCoincidencias = _tieneCoincidencias.asStateFlow()

    private val _estaMultiseleccionActiva = MutableStateFlow(false)
    val estaMultiseleccionActiva = _estaMultiseleccionActiva.asStateFlow()

    private val _categoriasEncontradasBe = MutableStateFlow<List<CategoriaDominio>>(emptyList())
    val categoriasEncontradasBe = _categoriasEncontradasBe.asStateFlow()

    private val _superCategoriasEncontradasBe = MutableStateFlow<List<SuperCategoriaDominio>>(emptyList())
    val superCategoriasEncontradasBe = _superCategoriasEncontradasBe.asStateFlow()

    private val _favoritosEncontradosBe = MutableStateFlow<List<PrestadorDominio>>(emptyList())
    val favoritosEncontradosBe = _favoritosEncontradosBe.asStateFlow()

    fun publicarCategoriasEncontradas(lista: List<CategoriaDominio>) { _categoriasEncontradasBe.value = lista }
    fun publicarSuperCategoriasEncontradas(lista: List<SuperCategoriaDominio>) { _superCategoriasEncontradasBe.value = lista }
    fun publicarFavoritosEncontradas(lista: List<PrestadorDominio>) { _favoritosEncontradosBe.value = lista }

    private val _superCategoriaSeleccionada = MutableStateFlow<com.example.myapplication.core.dominio.modelos.SuperCategoriaDominio?>(null)
    val superCategoriaSeleccionada = _superCategoriaSeleccionada.asStateFlow()

    private val _todoSeleccionado = MutableStateFlow(false)
    val todoSeleccionado = _todoSeleccionado.asStateFlow()

    fun actualizarMultiseleccion(activa: Boolean) { _estaMultiseleccionActiva.value = activa }

    fun seleccionarSuperCategoria(superCat: com.example.myapplication.core.dominio.modelos.SuperCategoriaDominio?) {
        _superCategoriaSeleccionada.value = superCat
        navCoordinador.actualizarVisibilidadHoja(superCat != null)
    }

    fun actualizarTodoSeleccionado(seleccionado: Boolean) { _todoSeleccionado.value = seleccionado }

    /**
     * 🔥 [v2026.ELITE]: Limpieza radical del estado táctico.
     * Garantiza que Be Assistant vuelva a reposo al navegar o cerrar.
     */
    fun limpiarModoTactico() {
        _estaMultiseleccionActiva.value = false
        _todoSeleccionado.value = false
        ocultarToast()
    }

    fun ejecutarCierreMaestro(forzar: Boolean = false) {
        alcance.launch {
            // 🔥 [v2026.ELITE]: Prioridad de cierre escalonado.
            // Si hay búsqueda activa y no forzamos, solo la desactivamos.
            if (beBusquedaMotor.estaBusquedaActiva.value && !forzar) {
                beBusquedaMotor.establecerEstaBusquedaActiva(false)
                return@launch
            }

            beBusquedaMotor.establecerEstaBusquedaActiva(false)
            navCoordinador.actualizarVisibilidadHoja(false)
            _superCategoriaSeleccionada.value = null
            navCoordinador.establecerEstaMenuLateralAbierto(false)
            
            limpiarModoTactico() // 🔥 [SANEAMIENTO]
            
            // 🔥 [v2026.ELITE]: Solo limpiamos el registro si realmente estamos cerrando todo hacia el Home.
            navCoordinador.limpiarCierreMaestro()
            
            dispararAccion("close_all_sheets")
        }
    }

    fun establecerTieneCoincidencias(coincidencias: Boolean) { _tieneCoincidencias.value = coincidencias }

    private val _eventosFeedbackVisual = MutableSharedFlow<FeedbackVisualBe>()
    val eventosFeedbackVisual = _eventosFeedbackVisual.asSharedFlow()

    fun emitirFeedbackVisual(mensaje: String, emocion: EmocionBe = EmocionBe.NORMAL, icono: String? = null, duracionMs: Long = 3500L) {
        alcance.launch { 
            // 🔥 [ELITE]: Redirección táctica al nuevo sistema de Toasts (Unificación)
            mostrarToast(mensaje, TipoBeToast.HABLANDO, duracionMs)
            _eventosFeedbackVisual.emit(FeedbackVisualBe(mensaje, emocion, icono, duracionMs)) 
        }
    }

    suspend fun dispararAccion(idAccion: String) { _eventoAccion.emit(idAccion) }
    
    fun actualizarConsultaBusqueda(consulta: String) { beBusquedaMotor.actualizarConsulta(consulta) }

    private val _idPerfilSeleccionado = MutableStateFlow<String?>(null)
    val idPerfilSeleccionado: StateFlow<String?> = _idPerfilSeleccionado.asStateFlow()

    fun seleccionarPerfil(idPerfil: String?) { _idPerfilSeleccionado.value = idPerfil }

    private val _idDireccionSeleccionada = MutableStateFlow<String?>(null)
    val idDireccionSeleccionada = _idDireccionSeleccionada.asStateFlow()

    val direccionGpsSobreajustada: StateFlow<DireccionDominio?> = ubicacionGpsRepo.ubicacionCacheada
        .stateIn(alcance, SharingStarted.WhileSubscribed(5000), null)

    fun seleccionarDireccion(idDireccion: String?) {
        if (idDireccion.isNullOrBlank()) { restaurarDireccionPorDefecto(); return }
        _idDireccionSeleccionada.value = idDireccion
        if (idDireccion != "gps_current") {
            _modoGpsActivo.value = false
        } else {
            _modoGpsActivo.value = true
        }
    }

    fun restaurarDireccionPorDefecto() {
        _idDireccionSeleccionada.value = null
        _idPerfilSeleccionado.value = null
        _modoGpsActivo.value = false
    }

    fun actualizarDireccionDesdeGps(direccion: DireccionDominio) {
        _idDireccionSeleccionada.value = "gps_current"
        _modoGpsActivo.value = true
        _idPerfilSeleccionado.value = null
    }
    
    fun alternarModoGps(activo: Boolean) {
        _modoGpsActivo.value = activo
        if (activo) _idDireccionSeleccionada.value = "gps_current"
        else _idDireccionSeleccionada.value = null
    }

    val informacionDireccionesDisponibles: Flow<List<DireccionDominio>> = combine(
        auth.currentUser?.uid?.let { consultasUserRepo.obtenerUsuarioCompletoFlujo(it) } ?: flowOf(null),
        direccionGpsSobreajustada
    ) { completo, gpsOverride ->
        val lista = mutableListOf<DireccionDominio>()
        gpsOverride?.let { lista.add(it) }
        if (completo == null) return@combine lista
        completo.direcciones.forEach { dir -> lista.add(dir.copy(idPropietario = completo.perfil.id)) }
        lista
    }

    val direccionActiva: Flow<DireccionDominio?> = combine(
        estaGpsActivado, _modoGpsActivo, _idDireccionSeleccionada, direccionGpsSobreajustada, informacionDireccionesDisponibles
    ) { gpsHardware, modoGpsApp, idSeleccionado, gpsOverride, todasLasDirecciones ->
        if (modoGpsApp && gpsHardware) {
            return@combine gpsOverride ?: DireccionDominio(
                id = "gps_current",
                calle = "Detectando...",
                localidad = "Buscando satélites",
                etiqueta = "GPS Tracker",
                latitud = 0.0,
                longitud = 0.0
            )
        }
        todasLasDirecciones.find { it.id == idSeleccionado } ?: todasLasDirecciones.firstOrNull()
    }

    fun sincronizarEcosistemaRed(cp: String, categorias: List<String> = emptyList()) {
        alcance.launch {
            val topicsDeseados = mutableSetOf<String>()
            val cpStd = NormalizadorDirecciones.limpiarCodigoPostal(cp)
            if (cpStd.isBlank()) return@launch
            
            topicsDeseados.add(generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.ZONA, cpStd))
            topicsDeseados.add(generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.OFERTA, cpStd))
            
            categorias.forEach { cat ->
                val infoCat = categoryDao.obtenerPorId(cat)
                topicsDeseados.add(generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.OFERTA, cpStd, cat))
                infoCat?.idSuperCategoria?.let { superId ->
                    topicsDeseados.add(generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.OFERTA, cpStd, superId))
                }
                topicsDeseados.add(generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.CONCURSO, cpStd, cat))
            }
            actualizarSuscripcionesPersistentes(topicsDeseados)
        }
    }

    private suspend fun actualizarSuscripcionesPersistentes(topicsNuevos: Set<String>) {
        mutexTopicos.withLock {
            val aEliminar = topicosActivos - topicsNuevos
            aEliminar.forEach { topic ->
                repositorioTopic.unsubscribeFromTopic(topic)
                suscripcionDao.eliminarSuscripcion(topic)
                topicosActivos.remove(topic)
            }
            
            val aSuscribir = topicsNuevos - topicosActivos
            aSuscribir.filter { it.isNotBlank() }.forEachIndexed { index, topic ->
                val suscripcionExistente = suscripcionDao.obtenerSuscripcion(topic)
                if (suscripcionExistente == null) {
                    Log.d("MavElite", "🚀 [SUBSCRIBE_EXEC] Enviando suscripción a FCM: $topic")
                    delay((250 * (index + 1)).milliseconds)
                    repositorioTopic.subscribeToTopic(topic)
                    suscripcionDao.insertarSuscripcion(SuscripcionTopicEntity(topic, "CLIENTE_AUTO"))
                } else {
                    Log.v("MavElite", "✅ [SUBSCRIBE_SKIP] Ya suscrito (DB): $topic")
                }
                topicosActivos.add(topic)
            }
        }
    }

    suspend fun limpiarTodosLosTopics() {
        mutexTopicos.withLock {
            topicosActivos.forEach { repositorioTopic.unsubscribeFromTopic(it) }
            topicosActivos.clear()
            suscripcionDao.limpiarTodosLosTopics()
        }
    }
}
typealias AppActionCoordinator = CoordinadorAcciones
