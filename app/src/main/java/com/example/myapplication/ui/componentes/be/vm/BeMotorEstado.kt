package com.example.myapplication.ui.componentes.be.vm

import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import com.example.myapplication.ui.componentes.be.modelos.*
import kotlin.time.Duration.Companion.milliseconds

class BeMotorEstado(private val alcance: CoroutineScope) {
    private val _estadoFisico = MutableStateFlow(EstadoFisicoBeAsistente())
    val estadoFisico = _estadoFisico.asStateFlow()
    private var trabajoLogicaOjos: Job? = null
    private var trabajoMirada: Job? = null
    init { iniciarLogicaOjos() }
    fun alternarActividadFisica(activa: Boolean) {
        if (activa) iniciarLogicaOjos()
        else { trabajoLogicaOjos?.cancel(); trabajoMirada?.cancel(); _estadoFisico.update { it.copy(estaParpadeando = false) } }
    }
    fun actualizarLayout(estaBarraInferiorVisible: Boolean, estaBusquedaActiva: Boolean) {
        val paddingObjetivo = if (estaBarraInferiorVisible && !estaBusquedaActiva) 66.dp else 4.dp
        _estadoFisico.update { it.copy(rellenoInferior = paddingObjetivo) }
    }
    private fun iniciarLogicaOjos() {
        trabajoLogicaOjos?.cancel()
        trabajoLogicaOjos = alcance.launch {
            while (true) {
                delay((3000..8000).random().toLong().milliseconds)
                _estadoFisico.update { it.copy(estaParpadeando = true) }
                delay(120.milliseconds); _estadoFisico.update { it.copy(estaParpadeando = false) }
            }
        }
    }
    fun actualizarMirada(estado: EstadoBe, emocion: EmocionBe) {
        trabajoMirada?.cancel()
        trabajoMirada = alcance.launch {
            when (estado) {
                EstadoBe.REPOSO -> { _estadoFisico.update { it.copy(pupilaX = (-3..3).random().toFloat(), pupilaY = (-4..4).random().toFloat()) } }
                EstadoBe.HABLANDO -> { _estadoFisico.update { it.copy(pupilaX = 0f, pupilaY = if (emocion == EmocionBe.SONROJADO) 4f else 0f) } }
            }
        }
    }
}
