package com.example.myapplication.ui.pantallas.chat.turnos

import com.example.myapplication.core.dominio.motores.CalculadoraDisponibilidad.BloqueHorario
import java.time.LocalDate

/**
 * --- UI STATE: SELECTOR DE TURNO DINÁMICO (v2026.SUPREME) ---
 */
data class SelectorTurnoUiState(
    val estaCargando: Boolean = false,
    val error: String? = null,
    
    // Contexto del Prestador
    val nombrePrestador: String = "",
    val direccionPrestador: String = "",
    val categoriaServicio: String = "",
    
    // Reglas de Agenda (Recibidas por Chat)
    val esAgendaAbierta: Boolean = false,
    val recursosDisponibles: List<RecursoConSlots> = emptyList(),
    
    // Navegación Temporal
    val fechaSeleccionada: LocalDate = LocalDate.now(),
    val fechasDisponibles: List<LocalDate> = emptyList(),
    
    // Selección Actual
    val idRecursoSeleccionado: String? = null,
    val bloqueSeleccionado: BloqueHorario? = null,
    
    val puedeConfirmar: Boolean = false
)

data class RecursoConSlots(
    val id: String,
    val nombre: String,
    val especialidad: String = "",
    val slots: List<BloqueHorario> = emptyList(),
    val duracionMinutos: Int = 30
)
