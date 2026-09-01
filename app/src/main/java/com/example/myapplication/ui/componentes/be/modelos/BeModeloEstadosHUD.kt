package com.example.myapplication.ui.componentes.be.modelos

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
//import com.example.myapplication.core.dominio.modelos.SuperCategoriaDominio
//import com.example.myapplication.core.dominio.modelos.PrestadorDominio

enum class ModoBarraHerramientasBe {
    NORMAL,
    MULTI_SELECCION,
    EDICION
}

/**
 * --- CONTEXTO HUD (PLANTILLAS SOBERANAS v2026) ---
 * [LEY #12]: Define los valores por defecto para el contrato visual de cada pantalla.
 */
enum class ContextoHUD(
    private val mostrarBeDefault: Boolean = true,
    private val mostrarNavDefault: Boolean = true,
    val idsPrimarias: List<String> = emptyList(),
    val mostrarHerramientasPorDefecto: Boolean = false 
) {
    INICIO(true, true, listOf("fast", "fav"), true),
    CONCURSOS(true, true, listOf("concurso_nuevo"), true),
    CHAT(true, true, listOf("archivo_chat"), true),
    CALENDARIO(true, true, listOf("goto_history"), true),
    WIZARD_CONCURSO(false, false, listOf("atras", "sig", "publicar", "cerrar_wizard")),
    
    // --- PANTALLAS SIN ASISTENTE ---
    PROMO(false, true),
    PERFIL(false, false),
    PERFIL_PRESTADOR(false, false),
    CHAT_CONVERSACION(false, false),
    RESULTADOS_BUSQUEDA(true, false),
    URGENCIA(false, false),
    FAVORITOS_SCREEN(false, false),
    ANALITICAS(false, false),
    VACIO(false, false);

    /**
     * 🔥 [v2026.ELITE]: Genera un contrato soberano único.
     */
    fun crearConfiguracionBase(
        mensajes: List<MensajeBe> = emptyList(),
        pistaBusqueda: String = if (this == INICIO) "BUSCA CON BE..." else "BUSCAR...",
        edicion: List<String> = emptyList(),
        navegacion: List<String> = emptyList(), // 🔥 [NEW]
        abrirTeclado: Boolean = false,
        ocultarBeOverride: Boolean? = null,
        ocultarNavOverride: Boolean? = null,
        idOverride: String? = null // 🔥 [v2026.SUPREME]
    ): ConfiguracionContextoBe {
        return ConfiguracionContextoBe(
            id = idOverride ?: "root_${this.name.lowercase()}", // 🔥 ID Estable por contexto
            primarias = this.idsPrimarias,
            navegacion = navegacion, 
            mensajes = mensajes,
            pistaBusqueda = pistaBusqueda,
            mostrarHerramientas = this.mostrarHerramientasPorDefecto,
            edicion = edicion,
            abrirTecladoEnBusqueda = abrirTeclado,
            mostrarBe = ocultarBeOverride?.not() ?: this.mostrarBeDefault,
            mostrarBarraNavegacion = ocultarNavOverride?.not() ?: this.mostrarNavDefault
        )
    }
}

enum class EstadoBe { REPOSO, HABLANDO }
enum class EmocionBe { NORMAL, FELIZ, SORPRENDIDO, ENOJADO, PENSANDO, DURMIENDO, SONROJADO, TRISTE }

data class MensajeBe(
    val icono: String,
    val texto: String,
    val textoAccion: String? = null,
    val colorBurbuja: Color,
    val colorTexto: Color = Color(0xFF05070A),
    val emocion: EmocionBe = EmocionBe.NORMAL,
    val estaCentrado: Boolean = false
)

data class ModeloAccionPequenaBe(
    val id: String,
    val icono: ImageVector,
    val etiqueta: String,
    val emoji: String? = null,
    val estaVisible: Boolean = true,
    val estaHabilitado: Boolean = true,
    val estaSeleccionado: Boolean = false,
    val esPredeterminado: Boolean = false,
    val tinte: Color = Color.White,
    val alHacerClick: () -> Unit = {}
)

data class EstadoUiBusqueda(val crudo: String = "", val normalizado: String = "")

data class EstadoFisicoBeAsistente(
    val estaParpadeando: Boolean = false,
    val pupilaX: Float = 0f,
    val pupilaY: Float = 0f,
    val rellenoInferior: Dp = 0.dp
)

/**
 * --- CONFIGURACIÓN DE CONTEXTO BE (SUPREME v2026) ---
 * [PROPÓSITO]: Contrato de Soberanía Único para controlar el HUD y la Interfaz.
 */
data class ConfiguracionContextoBe(
    val id: String = "default",
    val primarias: List<String> = emptyList(),
    val sistema: List<String> = emptyList(),
    val navegacion: List<String> = emptyList(),
    val edicion: List<String> = emptyList(),
    val accionesDeshabilitadas: List<String> = emptyList(),
    val mensajes: List<MensajeBe> = emptyList(),
    val pistaBusqueda: String = "BUSCA CON BE...",
    val mostrarHerramientas: Boolean = false,
    val abrirTecladoEnBusqueda: Boolean = false,
    val ocultarOjos: Boolean = false,
    val ocultarHerramientasSistemaBusqueda: Boolean = false, // 🔥 [NEW]: Urgencias / Táctico
    
    // 🔥 [v2026.ELITE]: Control unificado de visibilidad en el contrato soberano
    val mostrarBe: Boolean = true,
    val mostrarBarraNavegacion: Boolean = true
)

data class BeToastState(
    val mensaje: String,
    val tipo: TipoBeToast,
    val duracionMs: Long = 3000L
)

enum class TipoBeToast {
    PROCESANDO, EXITO, ERROR, INFO, HABLANDO
}

/**
 * --- ESTADO UI CONSOLIDADO SUPREME ---
 */
data class EstadoUiBeAsistente(
    val mostrarBe: Boolean = true,
    val estaBusquedaActiva: Boolean = false,
    val consultaBusqueda: String = "",
    val consultaNormalizada: String = "",
    val estado: EstadoBe = EstadoBe.REPOSO,
    val estaDormido: Boolean = false,
    
    // --- EL CONTRATO SOBERANO ANIDADO (Ley #12) ---
    val configuracion: ConfiguracionContextoBe = ConfiguracionContextoBe(),
    
    // Grupos de herramientas materializadas
    val herramientasPrimarias: List<ModeloAccionPequenaBe> = emptyList(),
    val herramientasSistema: List<ModeloAccionPequenaBe> = emptyList(),
    val herramientasNavegacion: List<ModeloAccionPequenaBe> = emptyList(),
    val herramientasEdicion: List<ModeloAccionPequenaBe> = emptyList(),
    
    val estaMultiseleccion: Boolean = false,
    val estaTodoSeleccionado: Boolean = false,
    val modoBarraHerramientas: ModoBarraHerramientasBe = ModoBarraHerramientasBe.NORMAL,
    val toastActivo: BeToastState? = null
)

data class FeedbackVisualBe(
    val mensaje: String,
    val emocion: EmocionBe = EmocionBe.NORMAL,
    val icono: String? = null,
    val duracionMs: Long = 3500L
)

