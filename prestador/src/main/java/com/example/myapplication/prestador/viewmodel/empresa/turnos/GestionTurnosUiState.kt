package com.example.myapplication.prestador.viewmodel.empresa.turnos

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.myapplication.core.dominio.modelos.InventarioActivoDominio
import com.example.myapplication.core.dominio.modelos.PrestadorDominio

/**
 * --- UI STATE: GESTIÓN DE TURNOS (v2026.SUPREME) ---
 */
@RequiresApi(Build.VERSION_CODES.O)
data class GestionTurnosUiState(
    val sucursales: List<PrestadorDominio> = emptyList(),
    val sucursalSeleccionada: PrestadorDominio? = null,
    val inventario: List<InventarioActivoDominio> = emptyList(),
    val tabSeleccionada: Int = 0,
    val busqueda: String = "",
    val estaCargando: Boolean = false,
    val snackbarMensaje: String? = null,
    
    // --- MÉTRICAS DE RESUMEN ---
    val ocupacionHoy: Int = 0,
    val staffActivo: Int = 0,
    val recursosListos: Int = 0,
    val fechaSeleccionada: java.time.LocalDate = java.time.LocalDate.now(),
    val mostrarDatePicker: Boolean = false,

    // --- FILTRADO Y EXPANSIÓN ---
    val filtroResumen: TipoFiltroResumen = TipoFiltroResumen.RECURSOS,
    val idStaffExpandido: String? = null,
    
    // --- ESTADOS DE EDICIÓN (Sheets) ---
    val recursoEnEdicion: com.example.myapplication.core.datos.local.entidades.RecursoEntity? = null,
    val equipoEnEdicion: com.example.myapplication.core.datos.local.entidades.EquipoTrabajoEntity? = null,
    
    val mostrarEditorRecurso: Boolean = false,
    val mostrarEditorEquipo: Boolean = false
)

enum class TipoFiltroResumen {
    OCUPACION, STAFF, RECURSOS
}
