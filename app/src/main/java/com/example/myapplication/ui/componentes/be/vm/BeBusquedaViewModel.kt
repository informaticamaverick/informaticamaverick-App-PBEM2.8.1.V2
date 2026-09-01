package com.example.myapplication.ui.componentes.be.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.coordinadores.CoordinadorAcciones
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- BE BUSQUEDA VIEWMODEL (EL ESCÁNER TÁCTICO v2026.ELITE) ---
 * [PROPÓSITO]: Gestionar la entrada de búsqueda y la visibilidad del teclado global.
 * [FUNCIONAMIENTO INTERNO]: Actúa como un puente reactivo entre la UI de búsqueda y el Coordinador,
 * emitiendo señales de enfoque para el teclado mediante un SharedFlow.
 * [RELACIÓN]: Es consumido por la BarraBusquedaTacticaV3 y coordinado por NavegacionHUDAsistente.
 * [LEY #12]: Soberanía por Contrato. Fragmentado para evitar recomposiciones costosas.
 */
@HiltViewModel
class BeBusquedaViewModel @Inject constructor(
    private val coordinador: CoordinadorAcciones,
    private val beBusquedaMotor: com.example.myapplication.core.dominio.motores.BeBusquedaMotor
) : ViewModel() {

    val estaBusquedaActiva = beBusquedaMotor.estaBusquedaActiva
    val consultaBusqueda = beBusquedaMotor.consultaCruda

    private val _solicitarTeclado = MutableSharedFlow<Unit>(replay = 0)
    val solicitarTeclado = _solicitarTeclado.asSharedFlow()

    fun actualizarConsulta(nueva: String) {
        beBusquedaMotor.actualizarConsulta(nueva)
    }

    fun alternarBusqueda() {
        val nuevoEstado = !estaBusquedaActiva.value
        beBusquedaMotor.establecerEstaBusquedaActiva(nuevoEstado)
        if (nuevoEstado) {
            // [SUPREME.FIX]: Ya no abrimos el teclado aquí para permitir 
            // que solo el botón de "Teclado" lo dispare.
        } else {
            cerrarBusqueda()
        }
    }

    private fun cerrarBusqueda() {
        beBusquedaMotor.establecerEstaBusquedaActiva(false)
        viewModelScope.launch { coordinador.dispararAccion("close_all_sheets") }
    }

    fun abrirTeclado() {
        viewModelScope.launch { _solicitarTeclado.emit(Unit) }
    }

    fun cerrarTeclado() { /* El teclado se cierra con el foco */ }
}

