package com.example.myapplication.ui.componentes.be.vm

import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.coordinadores.CoordinadorAcciones
import com.example.myapplication.ui.componentes.be.modelos.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * --- BE FÍSICA VIEWMODEL (EL CORAZÓN v2026.ELITE) ---
 * [PROPÓSITO]: Gestionar ojos, parpadeo, pupilas y posicionamiento Docking (Física pasiva).
 * [FUNCIONAMIENTO INTERNO]: Implementa un bucle de parpadeo aleatorio y reacciona al contexto HUD
 * para ajustar el relleno inferior del asistente, evitando colisiones con la navegación.
 * [RELACIÓN]: Provee el `estadoFisico` a `FabAsistenteBe`.
 * [LEY #12]: Higiene de Recursos. Detiene procesos físicos si Be no es visible.
 */
@HiltViewModel
class BeFisicaViewModel @Inject constructor(
    private val coordinador: CoordinadorAcciones,
    private val navCoordinador: com.example.myapplication.coordinadores.CoordinadorNavegacion, // 🔥 [NEW]
    private val beBusquedaMotor: com.example.myapplication.core.dominio.motores.BeBusquedaMotor
) : ViewModel() {

    private val _estadoFisico = MutableStateFlow(EstadoFisicoBeAsistente())
    val estadoFisico = _estadoFisico.asStateFlow()

    private var trabajoOjos: Job? = null

    init {
        // Sincronizar Docking con el HUD soberano
        combine(
            navCoordinador.contratoActivo,
            beBusquedaMotor.estaBusquedaActiva
        ) { contrato, buscando ->
            val padding = if (contrato.mostrarBarraNavegacion && !buscando) 66.dp else 4.dp
            _estadoFisico.update { it.copy(rellenoInferior = padding) }
        }.launchIn(viewModelScope)

        // Control de actividad de ojos (Ley #12: Higiene de Recursos)
        navCoordinador.contratoActivo
            .map { it.mostrarBe }
            .distinctUntilChanged()
            .onEach { visible ->
                if (visible) iniciarOjos()
                else detenerOjos()
            }.launchIn(viewModelScope)

        // Seguimiento de mirada según Toast activo (v2026.ELITE)
        coordinador.toastActivo.onEach { toast ->
            actualizarMirada(toast?.tipo)
        }.launchIn(viewModelScope)
    }

    private fun iniciarOjos() {
        detenerOjos()
        trabajoOjos = viewModelScope.launch {
            while (true) {
                delay((3000..8000).random().toLong().milliseconds)
                _estadoFisico.update { it.copy(estaParpadeando = true) }
                delay(120.milliseconds)
                _estadoFisico.update { it.copy(estaParpadeando = false) }
            }
        }
    }

    private fun detenerOjos() {
        trabajoOjos?.cancel()
        _estadoFisico.update { it.copy(estaParpadeando = false) }
    }

    private var trabajoMirada: Job? = null
    private fun actualizarMirada(tipo: TipoBeToast?) {
        trabajoMirada?.cancel()
        trabajoMirada = viewModelScope.launch {
            when (tipo) {
                TipoBeToast.EXITO -> _estadoFisico.update { it.copy(pupilaX = 0f, pupilaY = 0f) }
                TipoBeToast.ERROR -> _estadoFisico.update { it.copy(pupilaX = 0f, pupilaY = 0f) }
                null -> {
                    // En reposo, mirada aleatoria ocasional
                    _estadoFisico.update { it.copy(pupilaX = (-2..2).random().toFloat(), pupilaY = (-2..2).random().toFloat()) }
                }
                else -> _estadoFisico.update { it.copy(pupilaX = 0f, pupilaY = 0f) }
            }
        }
    }
}

