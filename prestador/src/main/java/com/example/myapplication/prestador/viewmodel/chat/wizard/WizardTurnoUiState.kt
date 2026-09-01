package com.example.myapplication.prestador.viewmodel.chat.wizard

import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.core.dominio.motores.CalculadoraDisponibilidad.BloqueHorario

/**
 * --- UI STATE: WIZARD DE PROPUESTA DE TURNO (v2026.SUPREME) ---
 */
data class WizardTurnoUiState(
    val pasoActual: PasoWizard = PasoWizard.IDENTIDAD,
    val estaCargando: Boolean = false,
    
    // --- DATOS DE IDENTIDAD (Paso 1) ---
    val nombrePrestador: String = "",
    val categoriaServicio: String = "",
    val iconoCategoria: String = "",
    val direccionesDisponibles: List<DireccionDominio> = emptyList(),
    val direccionSeleccionada: DireccionDominio? = null,
    
    val nombreCliente: String = "",
    val urlFotoCliente: String? = null,

    // --- CONFIGURACIÓN TÉCNICA (Paso 2) ---
    val modoAgenda: ModoAgendaTurno = ModoAgendaTurno.CERRADA,
    
    // Recursos y Equipo
    val recursosDisponibles: List<RecursoDominio> = emptyList(),
    val equipoDisponible: List<EquipoTrabajoDominio> = emptyList(),
    
    val recursoSeleccionado: RecursoDominio? = null,
    val personalAsignado: EquipoTrabajoDominio? = null, // Opcional
    
    // Para Modo Abierto (Multiselección)
    val recursosPermitidosIds: Set<String> = emptySet(),
    val personalPermitidoIds: Set<String> = emptySet(),

    // --- TIEMPO Y DISPONIBILIDAD ---
    val fechaSeleccionadaMillis: Long = System.currentTimeMillis(),
    val fechaTexto: String = "",
    val bloquesDisponibles: List<BloqueHorario> = emptyList(),
    val horaSeleccionada: String = "", // Solo para modo CERRADO
    
    // Rango para Modo Abierto
    val diasPermitidos: List<Int> = listOf(1, 2, 3, 4, 5), // Lun-Vie por defecto
    val franjaHorariaInicio: String = "08:00",
    val franjaHorariaFin: String = "20:00"
)

enum class PasoWizard {
    IDENTIDAD,      // Datos Prestador/Cliente y Dirección
    CONFIGURACION   // Modo (Cerrado/Abierto), Recursos, Personal y Tiempo
}

enum class ModoAgendaTurno {
    CERRADA, // Punto fijo: Lunes 5, 16hs, Box 1
    ABIERTA  // Rango: Lunes a Viernes, 08-20hs, Cualquier Box
}

