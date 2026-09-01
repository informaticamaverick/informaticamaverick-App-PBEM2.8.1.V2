package com.example.myapplication.ui.componentes.be.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.coordinadores.CoordinadorAcciones
import com.example.myapplication.ui.componentes.be.modelos.*
import com.example.myapplication.uishared.estilos.SharedPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- BE CUERPO VIEWMODEL (HUD v2026.ELITE) ---
 * [PROPÓSITO]: Maestro de Herramientas, Visibilidad y Feedback del HUD de Be.
 * [FUNCIONAMIENTO INTERNO]: Orquesta la construcción dinámica de herramientas segmentadas
 * basadas en el contexto HUD soberano y gestiona el ciclo de feedback visual (burbujas).
 * [RELACIÓN]: Actúa como orquestador primario del `FabAsistenteBe`.
 * [LEY #12]: Soberanía por Contrato. Be como Intermediario (Portavoz).
 */
@HiltViewModel
class BeCuerpoViewModel @Inject constructor(
    val coordinador: CoordinadorAcciones,
    val navCoordinador: com.example.myapplication.coordinadores.CoordinadorNavegacion, // 🔥 [NEW]
    val beBusquedaMotor: com.example.myapplication.core.dominio.motores.BeBusquedaMotor
) : ViewModel() {

    // Estados internos de UI local
    private val _estaDormido = MutableStateFlow(false)
    private val _mostrarHerramientasLocal = MutableStateFlow(false)

    // --- ESTADO DE UI CONSOLIDADO (FOCO EN HERRAMIENTAS) ---
    val uiState: StateFlow<EstadoUiBeAsistente> = combine(
        navCoordinador.contratoActivo, // 🔥 [ELITE]: SSOT de Navegación
        beBusquedaMotor.estaBusquedaActiva,
        coordinador.estaMultiseleccionActiva,
        coordinador.todoSeleccionado,
        _estaDormido,
        _mostrarHerramientasLocal,
        navCoordinador.estaMenuLateralAbierto,
        navCoordinador.estaHojaVisible, // 🔥 [NEW]
        coordinador.toastActivo
    ) { flows ->
        val config = flows[0] as ConfiguracionContextoBe
        val buscando = flows[1] as Boolean
        val multi = flows[2] as Boolean
        val todoSel = flows[3] as Boolean
        val dormido = flows[4] as Boolean
        val menuAbierto = flows[6] as Boolean
        val hojaVisible = flows[7] as Boolean // 🔥 [NEW]
        val toastActivo = flows[8] as BeToastState?

        android.util.Log.v("BE_COMBINE", "🔄 [COMBINE] HUD_ID=${config.id} | multi=$multi | buscando=$buscando | primarias=${config.primarias}")

        // 🔥 [ELITE]: Si hay multiselección, Be se enfoca en las herramientas.
        val configConMensajes = if (multi && !buscando) {
            config.copy(
                mensajes = emptyList()
            )
        } else config

        val estadoFinal = when {
            buscando && !coordinador.tieneCoincidencias.value -> EstadoBe.HABLANDO 
            else -> EstadoBe.REPOSO
        }

        // 🔥 [v2026.ELITE]: Lógica de Visibilidad Inteligente (Higiene vs Herramientas)
        // Be se oculta si hay un menú abierto.
        // Si hay una hoja visible, solo se muestra si hay búsqueda, multiselección o herramientas declaradas.
        val mostrarBeFinal = configConMensajes.mostrarBe && !menuAbierto && 
                (!hojaVisible || buscando || multi || configConMensajes.primarias.isNotEmpty() || configConMensajes.sistema.isNotEmpty() || configConMensajes.navegacion.isNotEmpty())
        
        // 🔥 [ELITE FIX]: LEY DE EXCLUSIVIDAD RADICAL (v2026).
        // Segmentamos las herramientas por jerarquía de prioridad para evitar mezclas.
        
        // 1. PRIORIDAD MÁXIMA: Multiselección (Edición)
        // Ignoramos el estado de búsqueda para permitir que la edición gane soberanía.
        val idsEdicionFinal = if (multi) {
            configConMensajes.edicion.distinct() // 🔥 [FIX]: Respetamos el orden soberano (incluyendo cancel)
        } else emptyList()
        val edicion = construirAcciones(idsEdicionFinal, configConMensajes.accionesDeshabilitadas, todoSel)

        // 2. PRIORIDAD MEDIA: Herramientas de Pantalla (Primarias y Navegación)
        // 🔥 [ELITE]: Si hay multiselección, permitimos Navegación para crear "Islas" secundarias (ej: Comparar).
        val primarias = if (!multi && !buscando) {
            construirAcciones(configConMensajes.primarias, configConMensajes.accionesDeshabilitadas, todoSel)
        } else emptyList()
        
        val navegacion = if (multi || !buscando) {
            construirAcciones(configConMensajes.navegacion, configConMensajes.accionesDeshabilitadas, todoSel)
        } else emptyList()

        // 3. PRIORIDAD BASE: Herramientas de Sistema
        // 🔥 [v2026.SUPREME]: Siempre mostramos teclado/cerrar si hay búsqueda activa para cumplir el contrato visual.
        val sistemaIds = mutableListOf<String>()
        if (buscando) {
            if (!configConMensajes.ocultarHerramientasSistemaBusqueda) {
                sistemaIds.add("teclado")
                sistemaIds.add("cerrar_todo")
                sistemaIds.addAll(configConMensajes.sistema)
            }
        } else if (!multi && primarias.isEmpty()) {
            sistemaIds.addAll(configConMensajes.sistema)
        }
        
        val sistema = construirAcciones(sistemaIds.distinct(), configConMensajes.accionesDeshabilitadas, todoSel, esSistema = true)

        android.util.Log.d("BE_VM", "🧬 [RADICAL_HUD] multi=$multi | primarias=${primarias.size} | sistema=${sistema.size}")

        EstadoUiBeAsistente(
            mostrarBe = mostrarBeFinal, 
            estaBusquedaActiva = buscando,
            estado = estadoFinal,
            estaDormido = dormido,
            configuracion = configConMensajes,
            herramientasPrimarias = primarias,
            herramientasEdicion = edicion,
            herramientasSistema = sistema,
            herramientasNavegacion = navegacion,
            estaMultiseleccion = multi,
            estaTodoSeleccionado = todoSel,
            modoBarraHerramientas = if(multi) ModoBarraHerramientasBe.MULTI_SELECCION else ModoBarraHerramientasBe.NORMAL,
            toastActivo = toastActivo
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EstadoUiBeAsistente())

    private fun construirAcciones(
        ids: List<String>, 
        deshabilitadas: List<String>, 
        todoSel: Boolean,
        esSistema: Boolean = false
    ): List<ModeloAccionPequenaBe> {
        val idsLimpios = if (esSistema) ids.distinct() else {
            ids.distinct().filter { id ->
                !(id == "teclado" || id == "cerrar_todo") || ids.size == 1
            }
        }
        return idsLimpios.mapNotNull { id ->
            BeDictionary.Actions[id]?.let { visuales ->
                ModeloAccionPequenaBe(
                    id = id,
                    icono = BeDictionary.obtenerIconoSoberano(id, todoSel) ?: visuales.icon,
                    etiqueta = BeDictionary.obtenerEtiquetaSoberana(id, todoSel),
                    emoji = if (id == "select_all" && todoSel) null else visuales.emoji,
                    tinte = visuales.tint,
                    estaHabilitado = !deshabilitadas.contains(id),
                    esPredeterminado = visuales.isDefault
                ) { dispararAccion(id) }
            }
        }
    }

    fun alHacerClickBe(alAlternarBusqueda: () -> Unit) {
        if (_estaDormido.value) _estaDormido.value = false
        else alAlternarBusqueda()
    }

    fun alHacerDobleClickBe() {
        _estaDormido.update { !it }
    }

    fun alHacerClickLargoBe() {
        if (!_estaDormido.value) _mostrarHerramientasLocal.update { !it }
    }

    fun ocultarBurbuja() { 
        // 🔥 [ELITE]: En el nuevo sistema de Toasts, ocultar la burbuja simplemente limpia el log actual si es necesario
        coordinador.ocultarToast()
    }

    fun cerrarBeAssistantCompleto() {
        coordinador.ejecutarCierreMaestro()
    }

    fun dispararAccion(idAccion: String) {
        when (idAccion) {
            "cerrar_todo" -> {
                // 🔥 [v2026.SUPREME]: Si el contrato visual de la pantalla exige cierre maestro,
                // forzamos el cierre ignorando el estado de búsqueda local de la sheet.
                val contratoHUD = navCoordinador.contratoActivo.value
                val esDetalleSoberano = contratoHUD.id.startsWith("detalle_super_")
                
                coordinador.ejecutarCierreMaestro(forzar = esDetalleSoberano)
            }
            "cancel" -> {
                // 🔥 [ELITE]: Cancelar solo desactiva la multiselección global,
                // permitiendo que las pantallas limpien sus estados localmente.
                coordinador.actualizarMultiseleccion(false)
            }
            "teclado" -> {
                if (!beBusquedaMotor.estaBusquedaActiva.value) {
                    beBusquedaMotor.establecerEstaBusquedaActiva(true)
                }
                viewModelScope.launch { coordinador.dispararAccion(idAccion) }
            }
            else -> viewModelScope.launch { coordinador.dispararAccion(idAccion) }
        }
    }
}

