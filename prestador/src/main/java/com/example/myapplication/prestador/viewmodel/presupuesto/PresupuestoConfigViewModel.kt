package com.example.myapplication.prestador.viewmodel.presupuesto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.repository.PresupuestoConfig
import com.example.myapplication.prestador.data.repository.PresupuestoConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PresupuestoConfigViewModel @Inject constructor(
    private val repository: PresupuestoConfigRepository
) : ViewModel() {

    // Todos los ViewModels comparten el mismo StateFlow del repository singleton
    val config: StateFlow<PresupuestoConfig> = repository.config
        .stateIn(viewModelScope, SharingStarted.Eagerly, repository.config.value)

    fun setValidezDias(dias: Int) = updateAndSave { it.copy(validezDias = dias) }
    fun setMoneda(moneda: String) = updateAndSave { it.copy(moneda = moneda) }
    fun setPrefijo(prefijo: String) = updateAndSave { it.copy(prefijo = prefijo) }
    fun setProximoNumero(numero: Int) = updateAndSave { it.copy(proximoNumero = numero) }
    fun setNotaLegal(nota: String) = updateAndSave { it.copy(notaLegal = nota) }
    fun setShowArticlesByDefault(show: Boolean) = updateAndSave { it.copy(showArticlesByDefault = show)}
    fun setShowServicesByDefault(show: Boolean) = updateAndSave { it.copy(showServicesByDefault = show) }
    fun setShowFeesByDefault(show: Boolean) = updateAndSave { it.copy(showFeesByDefault = show) }
    fun setShowMiscByDefault(show: Boolean) = updateAndSave { it.copy(showMiscByDefault = show) }
    fun setShowTaxesByDefault(show: Boolean) = updateAndSave { it.copy(showTaxesByDefault = show) }
    fun setShowAttachmentsByDefault(show: Boolean) = updateAndSave { it.copy(showAttachmentsByDefault = show) }
    fun setNotaObservacionesDefault(nota: String) = updateAndSave { it.copy(notaObservacionesDefault = nota) }
    fun setDescuentoDefaultPct(pct: Double) = updateAndSave { it.copy(descuentoDefault = pct) }
    fun setCategoriaDefault(cat: String) = updateAndSave { it.copy(categoriaDefault = cat) }

    fun acknowledgeMonedaChange() = updateAndSave { it.copy(lastAcknowledgedMoneda = it.moneda) }

    fun refreshConfig() {} // No-op: el StateFlow del repo siempre está actualizado

    private fun updateAndSave(transform: (PresupuestoConfig) -> PresupuestoConfig) {
        viewModelScope.launch {
            repository.saveConfig(transform(repository.config.value))
        }
    }
}
