package com.example.myapplication.prestador.viewmodel.empresa.visitas

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.myapplication.core.dominio.modelos.InventarioActivoDominio
import com.example.myapplication.core.dominio.modelos.PrestadorDominio

/**
 * --- UI STATE: GESTIÓN DE VISITAS TÉCNICAS (v2026.SUPREME) ---
 */
@RequiresApi(Build.VERSION_CODES.O)
data class GestionVisitasUiState(
    val sucursales: List<PrestadorDominio> = emptyList(),
    val sucursalSeleccionada: PrestadorDominio? = null,
    val tecnicos: List<InventarioActivoDominio> = emptyList(),
    val tabSeleccionada: Int = 0,
    val busqueda: String = "",
    val estaCargando: Boolean = false,
    val snackbarMensaje: String? = null,
    
    // --- NAVEGACIÓN TEMPORAL ---
    val fechaSeleccionada: java.time.LocalDate = java.time.LocalDate.now(),
    val mostrarDatePicker: Boolean = false,

    // --- ESTADO DE EXPANSIÓN ---
    val idTecnicoExpandido: String? = null,
    
    // --- EDICIÓN ---
    val tecnicoEnEdicion: com.example.myapplication.core.datos.local.entidades.EquipoTrabajoEntity? = null,
    val mostrarEditorTecnico: Boolean = false
)
