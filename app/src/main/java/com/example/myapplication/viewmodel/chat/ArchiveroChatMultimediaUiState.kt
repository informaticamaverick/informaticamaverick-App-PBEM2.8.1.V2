package com.example.myapplication.viewmodel.chat

import androidx.compose.runtime.Immutable
import com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio
import com.example.myapplication.core.datos.local.entidades.MensajeEntity
import com.example.myapplication.core.dominio.modelos.EventoDominio
import com.example.myapplication.core.dominio.modelos.PrestadorDominio

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * --- TIPOS DE CONTENIDO MULTIMEDIA (v2026.ELITE) ---
 */
enum class TipoContenidoMultimedia(val titulo: String, val subtitulo: String, val emoji: String) {
    PRESUPUESTOS("PRESUPUESTOS RECIBIDOS", "Historial de cotizaciones técnicas", "📄"),
    IMAGENES("GALERÍA MULTIMEDIA", "Fotos y archivos compartidos", "🖼️"),
    UBICACIONES("UBICACIONES COMPARTIDAS", "Direcciones y puntos de encuentro", "📍"),
    VISITAS("VISITAS TÉCNICAS", "Registro de inspecciones", "🛠️"),
    TURNOS("AGENDA DE TURNOS", "Citas y reservas programadas", "📅")
}

/**
 * --- ARCHIVERO CHAT MULTIMEDIA UI STATE (v2026.ELITE) ---
 * [PROPÓSITO]: Estado atómico para la gestión multimedia de una conversación.
 * [LEY #1]: Pantalla Tonta.
 */
@Immutable
data class ArchiveroChatMultimediaUiState(
    val tipoActivo: TipoContenidoMultimedia = TipoContenidoMultimedia.PRESUPUESTOS,
    val prestador: PrestadorDominio? = null,
    val presupuestosPaginados: Flow<PagingData<PresupuestoResumenDominio>> = flowOf(PagingData.empty()),
    val imagenesPaginadas: Flow<PagingData<MensajeEntity>> = flowOf(PagingData.empty()),
    val ubicaciones: List<MensajeEntity> = emptyList(),
    val visitas: List<EventoDominio> = emptyList(),
    val turnos: List<EventoDominio> = emptyList(),
    val estaCargando: Boolean = true,
    val estaRefrescando: Boolean = false,
    val menuSelectorAbierto: Boolean = false,
    
    // --- Sector: Menús y Filtros ---
    val filtrosActivos: Set<String> = emptySet(),
    val itemsFiltro: List<com.example.myapplication.ui.componentes.DropdownItemData> = emptyList(),
    val itemsOrden: List<com.example.myapplication.ui.componentes.DropdownItemData> = emptyList(),
    val itemsRubros: List<com.example.myapplication.ui.componentes.DropdownItemData> = emptyList(),
    val menuFiltrosAbierto: String? = null,

    // --- Sector: Multiselección ---
    val estaMultiseleccion: Boolean = false,
    val idsSeleccionados: Set<String> = emptySet(),
    val totalItems: Int = 0 // 🔥 [NEW v2026.ELITE]: Para validación de Select All
)
