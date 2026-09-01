package com.example.myapplication.viewmodel.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity
import com.example.myapplication.core.datos.repositorios.CategoriaRepositorio
import com.example.myapplication.dominio.motores.MotorConcursoUsuario
import com.example.myapplication.coordinadores.CoordinadorAcciones
import com.example.myapplication.datos.repositorios.ConsultasUsuarioRepositorio
import com.example.myapplication.core.dominio.modelos.CuentaMaestroUsuario
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.ui.componentes.be.modelos.ConfiguracionContextoBe
import com.example.myapplication.ui.componentes.be.modelos.ContextoHUD
import com.google.firebase.auth.FirebaseAuth
import com.example.myapplication.core.dominio.descubrimiento.ProtocoloPrefijos
import com.example.myapplication.core.dominio.ubicacion.NormalizadorDirecciones
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- VIEWMODEL BORRADOR DE CONCURSO (v2026.ELITE) ---
 * [PROPÓSITO]: Gestionar el estado efímero del Wizard de creación de licitaciones.
 * [LEY #12]: Soberanía por Contrato (Pilot Mode).
 * [LEY #9]: Estándar Mav en Español.
 */
@HiltViewModel
class BorradorConcursoViewModel @Inject constructor(
    private val motorConcurso: MotorConcursoUsuario,
    private val repositorioCategoria: CategoriaRepositorio,
    private val consultasRepo: ConsultasUsuarioRepositorio,
    private val autenticacion: FirebaseAuth,
    val coordinador: CoordinadorAcciones,
    val navCoordinador: com.example.myapplication.coordinadores.CoordinadorNavegacion // 🔥 [NEW]
) : ViewModel() {

    private val _pasoActual = MutableStateFlow(0)
    val pasoActual = _pasoActual.asStateFlow()

    private val _titulo = MutableStateFlow("")
    val titulo = _titulo.asStateFlow()

    private val _descripcion = MutableStateFlow("")
    val descripcion = _descripcion.asStateFlow()

    private val _idCategoria = MutableStateFlow("")
    val idCategoria = _idCategoria.asStateFlow()

    private val _nombreCategoria = MutableStateFlow("")
    val nombreCategoria = _nombreCategoria.asStateFlow()

    private val _iconoCategoria = MutableStateFlow("📋")
    val iconoCategoria = _iconoCategoria.asStateFlow()
    
    private val _descripcionCategoria = MutableStateFlow("")
    val descripcionCategoria = _descripcionCategoria.asStateFlow()

    private val _exigeVisita = MutableStateFlow(false)
    val exigeVisita = _exigeVisita.asStateFlow()

    private val _exigeGarantia = MutableStateFlow(false)
    val exigeGarantia = _exigeGarantia.asStateFlow()

    private val _exigeMetodoPago = MutableStateFlow(true)
    val exigeMetodoPago = _exigeMetodoPago.asStateFlow()

    private val _exigeDocPrestador = MutableStateFlow(false)
    val exigeDocPrestador = _exigeDocPrestador.asStateFlow()

    private val _duracionDias = MutableStateFlow(7)
    val duracionDias = _duracionDias.asStateFlow()

    private val _urisDeImagenes = MutableStateFlow<List<String>>(emptyList())
    val urisDeImagenes = _urisDeImagenes.asStateFlow()

    // --- IDENTIDAD Y PERFILES ---
    private val _idPerfilSeleccionado = MutableStateFlow<String?>(null) // null = Personal
    val idPerfilSeleccionado = _idPerfilSeleccionado.asStateFlow()

    // --- UBICACIÓN ---
    private val _direccionSeleccionada = MutableStateFlow<DireccionDominio?>(null)
    val direccionSeleccionada = _direccionSeleccionada.asStateFlow()

    private val _esDireccionManual = MutableStateFlow(false)
    val esDireccionManual = _esDireccionManual.asStateFlow()

    private val _calleManual = MutableStateFlow("")
    val calleManual = _calleManual.asStateFlow()
    private val _numeroManual = MutableStateFlow("")
    val numeroManual = _numeroManual.asStateFlow()
    private val _ciudadManual = MutableStateFlow("")
    val ciudadManual = _ciudadManual.asStateFlow()
    private val _cpManual = MutableStateFlow("")
    val cpManual = _cpManual.asStateFlow()

    private val _estaActivo = MutableStateFlow(false)

    /**
     * 🔥 [v2026.ELITE]: ID de soberanía vinculado al slot en la pila del HUD.
     */
    var idSoberania: String = "default"

    private val _mostrarPublicidad = MutableStateFlow(false)
    val mostrarPublicidad = _mostrarPublicidad.asStateFlow()

    private val _publicidadFinalizada = MutableSharedFlow<Unit>(replay = 0)

    private val _finalizarExitosamente = MutableSharedFlow<Unit>(replay = 0)
    val finalizarExitosamente = _finalizarExitosamente.asSharedFlow()

    fun cerrarPublicidad() { 
        android.util.Log.d("BorradorVM", "🔌 [ADS_OFF] Reseteando estado de publicidad.")
        _mostrarPublicidad.value = false 
        viewModelScope.launch { _publicidadFinalizada.emit(Unit) }
    }

    // --- ESTADOS DE BÚSQUEDA DE CATEGORÍA ---
    private val _queryCategoria = MutableStateFlow("")
    val queryCategoria = _queryCategoria.asStateFlow()

    private val _menuCategoriasExpandido = MutableStateFlow(false)
    val menuCategoriasExpandido = _menuCategoriasExpandido.asStateFlow()

    fun actualizarQueryCategoria(query: String) { _queryCategoria.value = query }
    fun alternarMenuCategorias(visible: Boolean) { _menuCategoriasExpandido.value = visible }

    // --- ESTADOS DE MENÚS Y GPS ---
    private val _estaGpsActivo = MutableStateFlow(false)
    val estaGpsActivo = _estaGpsActivo.asStateFlow()
    
    private val _mostrarMenuPerfil = MutableStateFlow(false)
    val mostrarMenuPerfil = _mostrarMenuPerfil.asStateFlow()

    private val _mostrarMenuUbicacion = MutableStateFlow(false)
    val mostrarMenuUbicacion = _mostrarMenuUbicacion.asStateFlow()

    fun alternarMenuPerfil(visible: Boolean) { _mostrarMenuPerfil.value = visible }
    fun alternarMenuUbicacion(visible: Boolean) { _mostrarMenuUbicacion.value = visible }

    val todasLasCategorias: StateFlow<List<CategoriaEntity>> = repositorioCategoria.todasLasCategorias
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val estadoCuenta: StateFlow<CuentaMaestroUsuario?> = autenticacion.currentUser?.uid?.let { uid ->
        consultasRepo.obtenerCuentaMaestroUsuarioFlujo(uid)
    }?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null) ?: MutableStateFlow(null)

    init {
        // 1. Escuchar eventos tácticos de Be (Pilot Mode)
        viewModelScope.launch {
            coordinador.eventoAccion.collect { idAccion ->
                if (!_estaActivo.value) return@collect
                android.util.Log.d("BorradorVM", "🎮 [EVENTO_BE] id: $idAccion")
                when (idAccion) {
                    "sig" -> {
                        if (_pasoActual.value < 2) _pasoActual.value++
                    }
                    "atras" -> if (_pasoActual.value > 0) _pasoActual.value--
                    "publicar" -> {
                        publicarLicitacionConPublicidad()
                    }
                    "cerrar_wizard" -> {
                        coordinador.ejecutarCierreMaestro()
                        _finalizarExitosamente.emit(Unit) // Dispara el cierre visual de la pantalla
                    }
                    "wizard_preview" -> {
                        coordinador.mostrarToast("Generando vista previa...", com.example.myapplication.ui.componentes.be.modelos.TipoBeToast.INFO)
                    }
                }
            }
        }

        // 2. Sincronizar HUD soberano con el progreso del Wizard (Pilot Mode)
        combine(
            _pasoActual,
            _urisDeImagenes,
            _estaActivo,
            _idCategoria,
            _titulo,
            _descripcion,
            _direccionSeleccionada,
            _esDireccionManual,
            _calleManual,
            _numeroManual
        ) { flows ->
            val paso = flows[0] as Int
            val uris = flows[1] as List<String>
            val activo = flows[2] as Boolean
            val idCat = flows[3] as String
            val tit = flows[4] as String
            val desc = flows[5] as String
            val dirSel = flows[6] as DireccionDominio?
            val esManual = flows[7] as Boolean
            val calleMan = flows[8] as String
            val numMan = flows[9] as String

            if (activo) {
                val acciones = mutableListOf<String>()
                val deshabilitadas = mutableListOf<String>()
                
                // Siempre permitir cerrar
                acciones.add("cerrar_wizard")

                if (paso > 0) acciones.add("atras")
                
                val pasoValido = when(paso) {
                    0 -> idCat.isNotBlank() && (dirSel != null || (esManual && calleMan.isNotBlank() && numMan.isNotBlank()))
                    1 -> tit.isNotBlank() && desc.isNotBlank()
                    else -> true
                }

                if (paso < 2) {
                    acciones.add("sig")
                    if (!pasoValido) deshabilitadas.add("sig") // 🔥 v2026.ELITE: Visible pero deshabilitado
                } else {
                    acciones.add("publicar")
                    if (!pasoValido) deshabilitadas.add("publicar")
                }

                if (uris.isNotEmpty()) {
                    acciones.add("wizard_preview")
                }
                
                navCoordinador.actualizarContratoActual(
                    ConfiguracionContextoBe(
                        id = idSoberania, // 🔥 [FIX]: Usamos el ID vinculado
                        primarias = acciones,
                        accionesDeshabilitadas = deshabilitadas, 
                        mostrarHerramientas = true,
                        pistaBusqueda = "", // Sin barra de búsqueda en el Wizard
                        ocultarOjos = true
                    )
                )
            }
        }.launchIn(viewModelScope)
    }

    /**
     * 🔥 [ELITE]: Gestiona la soberanía del HUD (Ley #12).
     */
    fun configurarHUD(activo: Boolean) {
        _estaActivo.value = activo
        android.util.Log.d("HUD_BORRADOR", "🎯 [CONFIG_HUD] Activo: $activo")
    }

    fun actualizarPaso(paso: Int) { _pasoActual.value = paso }
    fun actualizarTitulo(texto: String) { _titulo.value = texto }
    fun actualizarDescripcion(texto: String) { _descripcion.value = texto }
    fun actualizarCategoria(cat: CategoriaEntity) { 
        _idCategoria.value = cat.id 
        _nombreCategoria.value = cat.nombre
        _iconoCategoria.value = cat.icono
        _descripcionCategoria.value = cat.descripcion ?: ""
    }
    
    fun seleccionarPerfil(idEmpresa: String?, idSucursal: String?) {
        // null, null = Personal
        _idPerfilSeleccionado.value = idSucursal
    }
    
    fun seleccionarDireccion(direccion: DireccionDominio?) {
        _direccionSeleccionada.value = direccion
        _esDireccionManual.value = false
    }

    fun activarDireccionManual(activar: Boolean) {
        _esDireccionManual.value = activar
        if (activar) _direccionSeleccionada.value = null
    }

    fun actualizarCalleManual(texto: String) { _calleManual.value = texto }
    fun actualizarNumeroManual(texto: String) { _numeroManual.value = texto }
    fun actualizarCiudadManual(texto: String) { _ciudadManual.value = texto }
    fun actualizarCpManual(texto: String) { _cpManual.value = texto }

    fun actualizarExigeVisita(valor: Boolean) { _exigeVisita.value = valor }
    fun actualizarExigeGarantia(valor: Boolean) { _exigeGarantia.value = valor }
    fun actualizarExigeMetodoPago(valor: Boolean) { _exigeMetodoPago.value = valor }
    fun actualizarExigeDocPrestador(valor: Boolean) { _exigeDocPrestador.value = valor }
    fun actualizarDuracion(dias: Int) { _duracionDias.value = dias }
    
    fun agregarImagen(uri: String) {
        val lista = _urisDeImagenes.value.toMutableList()
        if (lista.size < 6) {
            lista.add(uri)
            _urisDeImagenes.value = lista
        }
    }

    fun eliminarImagen(uri: String) {
        val lista = _urisDeImagenes.value.toMutableList()
        lista.remove(uri)
        _urisDeImagenes.value = lista
    }

    fun publicarLicitacionConPublicidad() {
        val uid = autenticacion.currentUser?.uid ?: run {
            android.util.Log.e("BorradorVM", "❌ [AUTH_ERR] No hay usuario autenticado para publicar.")
            return
        }
        val idPerfilActual = _idPerfilSeleccionado.value
        val cuentaActual = estadoCuenta.value

        android.util.Log.d("BorradorVM", "🚀 [PUBLISH_START] Iniciando flujo de publicación para UID: $uid")

        viewModelScope.launch(Dispatchers.IO) {
            // 1. Mostrar Toast de procesamiento (Log)
            coordinador.mostrarToast("Publicando concurso... ☕", com.example.myapplication.ui.componentes.be.modelos.TipoBeToast.PROCESANDO)

            var idEmpresaRel: String? = null
            var nombreEmpresaRel: String? = null
            var idSucursalRel: String? = null
            var nombreSucursalRel: String? = null

            cuentaActual?.empresas?.forEach { empComp ->
                empComp.sucursales.find { it.sucursal.id == idPerfilActual }?.let { sucComp ->
                    idEmpresaRel = empComp.empresa.id
                    nombreEmpresaRel = empComp.empresa.nombre
                    idSucursalRel = sucComp.sucursal.id
                    nombreSucursalRel = sucComp.sucursal.nombre
                }
            }

            val ubicacionFinal = if (_esDireccionManual.value) {
                DireccionDominio(
                    calle = _calleManual.value,
                    numero = _numeroManual.value,
                    localidad = _ciudadManual.value,
                    codigoPostal = _cpManual.value
                )
            } else {
                _direccionSeleccionada.value
            }

            if (ubicacionFinal == null) {
                android.util.Log.e("BorradorVM", "❌ [LOC_ERR] Ubicación nula al publicar.")
                coordinador.mostrarToast("Falta definir la ubicación", com.example.myapplication.ui.componentes.be.modelos.TipoBeToast.ERROR)
                return@launch
            }

            val cpNormalizado = NormalizadorDirecciones.limpiarCodigoPostal(ubicacionFinal.codigoPostal ?: "")
            val tagsBusqueda = listOf(
                "${ProtocoloPrefijos.ZONA}_$cpNormalizado",
                "${ProtocoloPrefijos.CONCURSO}_${cpNormalizado}_${_idCategoria.value}"
            )

            val concurso = ConcursoPublicoEntity(
                idConcurso = java.util.UUID.randomUUID().toString(),
                idCliente = uid,
                idEmpresa = idEmpresaRel,
                nombreEmpresa = nombreEmpresaRel,
                idSucursal = idSucursalRel,
                nombreSucursal = nombreSucursalRel,
                titulo = _titulo.value,
                descripcion = _descripcion.value,
                idCategoria = _idCategoria.value,
                fechaInicio = System.currentTimeMillis(),
                fechaFin = System.currentTimeMillis() + (_duracionDias.value * 24 * 60 * 60 * 1000L),
                exigeVisita = _exigeVisita.value,
                exigeMetodoPago = _exigeMetodoPago.value,
                exigeGarantia = _exigeGarantia.value,
                exigeDocPrestador = _exigeDocPrestador.value,
                direccionCalle = ubicacionFinal.calle,
                direccionNumero = ubicacionFinal.numero,
                direccionLocalidad = ubicacionFinal.localidad,
                direccionCodigoPostal = cpNormalizado,
                urlImagenes = _urisDeImagenes.value,
                nombreCliente = cuentaActual?.usuario?.perfil?.nombreVisible ?: "Usuario",
                miniaturaCliente = (cuentaActual?.usuario?.perfil?.urlMiniatura ?: cuentaActual?.usuario?.perfil?.urlFoto)?.toString(),
                estado = "ABIERTA",
                marcaTiempo = System.currentTimeMillis(),
                filtrosBusqueda = tagsBusqueda
            )
            
            try {
                // Persistencia real atómica
                android.util.Log.d("BorradorVM", "📦 [ATÓMICO] Iniciando publicación vía Motor...")
                motorConcurso.publicarNuevoConcurso(concurso)
                android.util.Log.d("BorradorVM", "✅ [ATÓMICO_OK] Concurso publicado con éxito.")
                
                // --- 🚀 [ELITE] ESTRATEGIA DE INMEDIATEZ MAVERICK ---
                viewModelScope.launch(Dispatchers.Main) {
                    // 1. Cierre visual inmediato del Wizard para dar feedback de "acción realizada"
                    android.util.Log.d("BorradorVM", "🧹 [LIMPIEZA] Cerrando wizard visualmente.")
                    _finalizarExitosamente.emit(Unit)
                    configurarHUD(false)
                    
                    // 🔥 [NUEVO]: Forzar recarga de concursos en la pantalla principal
                    dispararRecargaEnPantallaPrincipal()
                    
                    // 🔥 [LIMPIEZA]: Limpiar datos del borrador inmediatamente para evitar que sigan ahí al cerrar publicidad
                    resetearBorrador()

                    // 2. Be toma el control visual antes del video
                    android.util.Log.d("BorradorVM", "📺 [ADS_HANDOFF] Be preparando entorno para publicidad...")
                    coordinador.mostrarToast("Publicando... preparando el entorno ☕", com.example.myapplication.ui.componentes.be.modelos.TipoBeToast.INFO)
                    
                    // 3. Inmediatez Maverick: Eliminamos el retraso táctico para que el video aparezca lo más rápido posible
                    // android.util.Log.d("BorradorVM", "⏳ [ADS_DELAY] Esperando 1.5s...")
                    // kotlinx.coroutines.delay(1500) 
                    
                    // 4. Disparar Video
                    android.util.Log.d("BorradorVM", "📺 [ADS_START] Disparando Rewarded Interstitial Video de inmediato.")
                    _mostrarPublicidad.value = true
                    
                    // 5. Mensaje secundario (se verá un instante antes de que el video pise todo)
                    coordinador.mostrarToast("Disfruta el video mientras finalizamos... ☕", com.example.myapplication.ui.componentes.be.modelos.TipoBeToast.INFO)
                    
                    // 6. Esperar a que el usuario cierre el video para dar el golpe final
                    _publicidadFinalizada.first() 
                    android.util.Log.d("BorradorVM", "📺 [ADS_END] Video finalizado por el usuario.")
                    
                    // 7. Sincronización final de red
                    android.util.Log.d("BorradorVM", "📡 [SYNC_RED] Sincronizando ecosistema geográfico: ${ubicacionFinal.codigoPostal}")
                    coordinador.sincronizarEcosistemaRed(ubicacionFinal.codigoPostal ?: "", listOf(_idCategoria.value))
                    
                    // 8. Mensaje de éxito
                    coordinador.mostrarToast("¡Concurso Publicado Felicidades! 🚀", com.example.myapplication.ui.componentes.be.modelos.TipoBeToast.EXITO)
                    android.util.Log.d("BorradorVM", "🏁 [FLOW_FINISHED] Wizard completado exitosamente.")
                }
            } catch (e: Exception) {
                android.util.Log.e("BorradorVM", "❌ [CONCURSO_ERR] Fallo crítico: ${e.message}", e)
                coordinador.mostrarToast("Error al publicar. Reintenta.", com.example.myapplication.ui.componentes.be.modelos.TipoBeToast.ERROR)
            }
        }
    }

    private fun resetearBorrador() {
        _pasoActual.value = 0
        _titulo.value = ""
        _descripcion.value = ""
        _idCategoria.value = ""
        _nombreCategoria.value = ""
        _urisDeImagenes.value = emptyList()
        _idPerfilSeleccionado.value = null
        _direccionSeleccionada.value = null
        _esDireccionManual.value = false
        _mostrarPublicidad.value = false // 🛡️ [SEGURIDAD] Evitar estados zombies al resetear
    }

    private fun dispararRecargaEnPantallaPrincipal() {
        viewModelScope.launch {
            coordinador.dispararAccion("refresh_concursos")
        }
    }
}


