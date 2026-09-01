package com.example.myapplication.viewmodel.chat

import com.example.myapplication.core.dominio.modelos.ConversacionHiloMDominio
import com.example.myapplication.ui.componentes.DropdownItemData

/**
 * UI STATE PARA LA BANDEJA DE ENTRADA (v2026.ELITE)
 */
data class ListaChatsUiState(
    val hilos: Map<String, List<ConversacionHiloMDominio>> = emptyMap(),
    val conteoNoLeidos: Map<String, Int> = emptyMap(),
    val conteoNoLeidosIdentidad: Map<String, Int> = emptyMap(),
    val estaRefrescando: Boolean = false,
    val totalItems: Int = 0,
    val filtrosActivos: Set<String> = emptySet(),
    val idPerfilSeleccionado: String = "personal",
    val modoMultiseleccion: Boolean = false,
    val idsChatsSeleccionados: Set<String> = emptySet(),
    val itemsCategoria: List<DropdownItemData> = emptyList(),
    val itemsFiltro: List<DropdownItemData> = emptyList(),
    val itemsOrden: List<DropdownItemData> = emptyList()
)
