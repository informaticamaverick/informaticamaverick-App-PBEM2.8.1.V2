package com.example.myapplication.prestador.viewmodel.chat.wizard

import com.example.myapplication.core.datos.local.entidades.MensajeEntity
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.core.dominio.motores.CalculadoraDisponibilidad.BloqueHorario

/**
 * --- UI STATE: WIZARD DE PROPUESTA DE VISITA TÉCNICA (v2026.SUPREME) ---
 */
data class WizardVisitaUiState(
    val pasoActual: PasoWizard = PasoWizard.IDENTIDAD,
    val estaCargando: Boolean = false,
    
    // --- DATOS DE IDENTIDAD (Paso 1) ---
    val nombrePrestador: String = "",
    val categoriaServicio: String = "",
    val iconoCategoria: String = "",
    val direccionesOrigen: List<DireccionDominio> = emptyList(),
    val direccionOrigenSeleccionada: DireccionDominio? = null,
    
    val nombreCliente: String = "",
    val urlFotoCliente: String? = null,

    // --- CONFIGURACIÓN TÉCNICA (Paso 2) ---
    val modoAgenda: ModoAgendaTurno = ModoAgendaTurno.CERRADA,
    
    // Destino (Ubicaciones del chat)
    val direccionesDestinoDisponibles: List<MensajeEntity> = emptyList(),
    val direccionDestinoSeleccionada: MensajeEntity? = null,
    
    // Equipo
    val equipoDisponible: List<EquipoTrabajoDominio> = emptyList(),
    val equipoSeleccionadoIds: Set<String> = emptySet(),
    
    // Presupuesto
    val presupuestosDisponibles: List<PresupuestoResumenDominio> = emptyList(),
    val presupuestoSeleccionado: PresupuestoResumenDominio? = null,

    // --- TIEMPO Y DISPONIBILIDAD ---
    val fechaSeleccionadaMillis: Long = System.currentTimeMillis() + 86400000,
    val fechaTexto: String = "",
    val bloquesDisponibles: List<BloqueHorario> = emptyList(),
    val horaSeleccionada: String = "",
    
    val costoTrasladoEstimado: Double = 0.0
)

