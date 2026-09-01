package com.example.myapplication.prestador.viewmodel.presupuesto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.repositorios.ConfiguracionRepositorio
import com.example.myapplication.core.dominio.modelos.PresupuestoConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- VIEWMODEL DE CONFIGURACIÓN DE PRESUPUESTOS (V2026.12) ---
 * [UNIFICADO]: Consume ConfiguracionRepositorio del Core.
 * [LEY #9]: Nombres en Español.
 */
@HiltViewModel
class PrePresupuestoConfigViewModel @Inject constructor(
    private val repositorio: ConfiguracionRepositorio
) : ViewModel() {

    val config: StateFlow<PresupuestoConfig> = repositorio.presupuestoConfig
        .stateIn(viewModelScope, SharingStarted.Eagerly, PresupuestoConfig())

    fun establecerValidezDias(dias: Int) = actualizarYGuardar { it.copy(validezDias = dias) }
    fun establecerMoneda(moneda: String) = actualizarYGuardar { it.copy(moneda = moneda) }
    fun establecerPrefijo(prefijo: String) = actualizarYGuardar { it.copy(prefijo = prefijo) }
    fun establecerProximoNumero(numero: Int) = actualizarYGuardar { it.copy(proximoNumero = numero) }
    fun establecerNotaLegal(nota: String) = actualizarYGuardar { it.copy(notaLegal = nota) }

    // Configuración de visualización por defecto
    fun establecerMostrarArticulosDefault(mostrar: Boolean) = actualizarYGuardar { it.copy(showArticlesByDefault = mostrar)}
    fun establecerMostrarServiciosDefault(mostrar: Boolean) = actualizarYGuardar { it.copy(showServicesByDefault = mostrar) }
    fun establecerMostrarHonorariosDefault(mostrar: Boolean) = actualizarYGuardar { it.copy(showFeesByDefault = mostrar) }
    fun establecerMostrarVariosDefault(mostrar: Boolean) = actualizarYGuardar { it.copy(showMiscByDefault = mostrar) }
    fun establecerMostrarImpuestosDefault(mostrar: Boolean) = actualizarYGuardar { it.copy(showTaxesByDefault = mostrar) }
    fun establecerMostrarAdjuntosDefault(mostrar: Boolean) = actualizarYGuardar { it.copy(showAttachmentsByDefault = mostrar) }

    fun establecerNotaObservacionesDefault(nota: String) = actualizarYGuardar { it.copy(notaObservacionesDefault = nota) }
    fun establecerDescuentoDefaultPct(porcentaje: Double) = actualizarYGuardar { it.copy(descuentoDefault = porcentaje) }
    fun establecerCategoriaDefault(categoria: String) = actualizarYGuardar { it.copy(categoriaDefault = categoria) }

    fun confirmarCambioMoneda() = actualizarYGuardar { it.copy(lastAcknowledgedMoneda = it.moneda) }

    private fun actualizarYGuardar(transformacion: (PresupuestoConfig) -> PresupuestoConfig) {
        viewModelScope.launch {
            val actual = config.value
            repositorio.guardarConfigPresupuesto(transformacion(actual))
        }
    }
}














































