package com.example.myapplication.ui.componentes.be.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.coordinadores.CoordinadorAcciones
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- BE CEREBRO VIEWMODEL (EL PORTAVOZ v2026.ELITE) ---
 * [PROPÓSITO]: Portavoz del HUD de Be. Gestiona eventos de acción y filtros globales.
 * [FUNCIONAMIENTO INTERNO]: Actúa como puente reactivo entre el Coordinador y las pantallas
 * soberanas para la ejecución de comandos y sincronización de filtros.
 * [RELACIÓN]: Centraliza la comunicación de eventos visuales y de red para el Asistente.
 * [LEY #12]: Be como Intermediario (Portavoz).
 */
@HiltViewModel
class BeCerebroViewModel @Inject constructor(
    val coordinador: CoordinadorAcciones,
    val navCoordinador: com.example.myapplication.coordinadores.CoordinadorNavegacion, // 🔥 [NEW]
    val beBusquedaMotor: com.example.myapplication.core.dominio.motores.BeBusquedaMotor
) : ViewModel() {

    // --- SECTOR: EVENTOS Y COORDINACIÓN ---
    val actionEvent = coordinador.eventoAccion
    val eventoErrorSincro = MutableSharedFlow<String>() 

    fun dispararAccion(idAccion: String) {
        viewModelScope.launch { coordinador.dispararAccion(idAccion) }
    }
}

