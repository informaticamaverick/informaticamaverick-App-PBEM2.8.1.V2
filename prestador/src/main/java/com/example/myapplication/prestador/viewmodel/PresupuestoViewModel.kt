package com.example.myapplication.prestador.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkInfo
import com.example.myapplication.prestador.data.local.entity.PresupuestoEntity
import com.example.myapplication.prestador.data.local.entity.ClienteEntity
import com.example.myapplication.prestador.data.repository.PresupuestoRepository
import com.example.myapplication.prestador.di.ApplicationScope
import com.example.myapplication.prestador.utils.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.myapplication.prestador.data.local.entity.PlantillaPresupuestoEntity
import com.example.myapplication.prestador.ui.presupuesto.BudgetItem
import com.example.myapplication.prestador.ui.presupuesto.BudgetService
import com.example.myapplication.prestador.ui.presupuesto.BudgetProfessionalFee
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.temporal.TemporalAmount
import kotlinx.coroutines.flow.map


@HiltViewModel
class PresupuestoViewModel @Inject constructor(
    private val repository: PresupuestoRepository,
    @ApplicationScope private val appScope: CoroutineScope
) : ViewModel() {

    val presupuestos: StateFlow<List<PresupuestoEntity>> =
        repository.getAllPresupuestos()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val clientes: StateFlow<List<ClienteEntity>> =
        repository.getAllClientes()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val articleCatalog: StateFlow<PresupuestoEntity?> =
        repository.getCatalogPresupuesto("__catalog_articles__")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val serviceCatalog: StateFlow<PresupuestoEntity?> =
        repository.getCatalogPresupuesto("__catalog_services__")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val feeCatalog: StateFlow<PresupuestoEntity?> =
        repository.getCatalogPresupuesto("__catalog_fees__")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun insertPresupuesto(presupuesto: PresupuestoEntity) {
        viewModelScope.launch { repository.insertPresupuesto(presupuesto) }
    }

    fun insertCliente(cliente: com.example.myapplication.prestador.data.local.entity.ClienteEntity) {
        viewModelScope.launch { repository.insertCliente(cliente) }
    }

    fun updatePresupuesto(presupuesto: PresupuestoEntity) {
        viewModelScope.launch { repository.updatePresupuesto(presupuesto) }
    }

    fun updateEstado(id: String, estado: String) {
        viewModelScope.launch { repository.updateEstado(id, estado) }
    }

    fun deletePresupuesto(presupuesto: PresupuestoEntity) {
        viewModelScope.launch { repository.deletePresupuesto(presupuesto) }
    }

    suspend fun getClienteById(id: String) = repository.getClienteById(id)

    suspend fun getPresupuestosForAppointment(appointmentId: String) =
        repository.getLatestPresupuestoForAppointment(appointmentId)

    suspend fun getTotalGanancias(prestadorId: String): Double =
        repository.getTotalGanancias(prestadorId)

    suspend fun getGananciasSemanales(prestadorId: String): Double {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val fromDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(cal.time)
        return repository.getGananciasDesde(prestadorId, fromDate)
    }

    fun simulateClientResponse(
        context: Context,
        presupuestoId: String,
        clienteName: String,
        total: Double
    ) {
        appScope.launch {
            repository.updateEstado(presupuestoId, "Aceptado")
            NotificationHelper(context).showPresupuestoAceptadoNotification(clienteName, total)
        }
    }

    //PLANTILLAS
    val plantillas: StateFlow<List<PlantillaPresupuestoEntity>> = repository.getAllPlantillas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun savePlantilla(nombre: String, itemsJson: String, serviciosJson: String) {
        viewModelScope.launch {
            repository.savePlantilla(
                PlantillaPresupuestoEntity(
                    id = "plantilla_${System.currentTimeMillis()}",
                    nombre = nombre,
                    itemsJson = itemsJson,
                    serviciosJson = serviciosJson
                )

            )
        }
    }

    fun deletePlantilla(id: String) {
        viewModelScope.launch { repository.deletePlantilla(id) }
    }

    fun updateCustomTaxInSuggestions(desc: String, newAmount: Double) {
        viewModelScope.launch {
            presupuestos.value.forEach { p ->
                if (p.impuestosJson.contains(desc)) {
                    val updated = p.impuestosJson.split("|").joinToString("|") { s ->
                        val parts = s.split(";")
                        if (parts.getOrNull(0) == desc) "$desc;$newAmount" else s
                    }
                    repository.updatePresupuesto(p.copy(impuestosJson = updated))
                }
            }
        }
    }

    fun deleteArticleFromSuggestions(description: String) {
        viewModelScope.launch {
            presupuestos.value.forEach { p ->
                if (p.itemsJson.contains(description)) {
                    val updated = p.itemsJson.split("|")
                        .filter { it.split(";").getOrNull(1) != description }
                        .joinToString("|")
                    repository.updatePresupuesto(p.copy(itemsJson = updated))
                }
            }
        }
    }

    fun saveArticleToSuggestions(item: BudgetItem) {
        viewModelScope.launch {
            val catalogId = "__catalog_articles__"
            val newEntry = "${item.code};${item.description};${item.quantity};${item.unitPrice};${item.taxPercentage};${item.discountPercentage}"
            val existing = repository.getPresupuestoById(catalogId)
            if (existing != null) {
                val alreadyIn = existing.itemsJson.split("|").any { it.split(";").getOrNull(1) == item.description }
                if (!alreadyIn) {
                    val updated = if (existing.itemsJson.isBlank()) newEntry else "${existing.itemsJson}|$newEntry"
                    repository.updatePresupuesto(existing.copy(itemsJson = updated))
                }
            } else {
                repository.insertPresupuesto(
                    PresupuestoEntity(
                        id = catalogId, numeroPresupuesto = "", clienteId = "", prestadorId = "",
                        fecha = "", subtotal = 0.0, impuestos = 0.0, total = 0.0,
                        estado = "__catalog__", itemsJson = newEntry
                    )
                )
            }
        }
    }

    fun deleteCustomTaxFromSuggestions(desc: String) {
        viewModelScope.launch {
            presupuestos.value.forEach { p ->
                if (p.impuestosJson.contains(desc)) {
                    val updated = p.impuestosJson.split("|")
                        .filter { it.split(";").getOrNull(0) != desc }
                        .joinToString("|")
                    repository.updatePresupuesto(p.copy(impuestosJson = updated))
                }
            }
        }
    }

    fun updateMiscExpenseInSuggestions(oldDesc: String, newDesc: String, newAmount: Double) {
        viewModelScope.launch {
            presupuestos.value.forEach { p ->
                if (p.gastosJson.contains(oldDesc)) {
                    val updated = p.gastosJson.split("|").joinToString("|") { s ->
                        val parts = s.split(";")
                        if (parts.getOrNull(0) == oldDesc) "$newDesc;$newAmount" else s
                    }
                    repository.updatePresupuesto(p.copy(gastosJson = updated))
                }
            }
        }
    }

    fun deleteMiscExpenseFromSuggestions(desc: String) {
        viewModelScope.launch {
            presupuestos.value.forEach { p ->
                if (p.gastosJson.contains(desc)) {
                    val updated = p.gastosJson.split("|")
                        .filter { it.split(";").getOrNull(0) != desc }
                        .joinToString("|")
                    repository.updatePresupuesto(p.copy(gastosJson = updated))
                }
            }
        }
    }

    fun saveServiceToSuggestions(item: BudgetService) {
        viewModelScope.launch {
            val catalogId = "__catalog_services__"
            val newEntry = "${item.code};${item.description};${item.total}"
            val existing = repository.getPresupuestoById(catalogId)
            if (existing != null) {
                val alreadyIn = existing.serviciosJson.split("|").any { it.split(";").getOrNull(1) == item.description }
                if (!alreadyIn) {
                    val updated = if (existing.serviciosJson.isBlank()) newEntry else "${existing.serviciosJson}|$newEntry"
                    repository.updatePresupuesto(existing.copy(serviciosJson = updated))
                }
            } else {
                repository.insertPresupuesto(PresupuestoEntity(id = catalogId, numeroPresupuesto = "", clienteId = "", prestadorId = "", fecha = "", subtotal = 0.0, impuestos = 0.0, total = 0.0, estado = "__catalog__", serviciosJson = newEntry))
            }
        }
    }

    fun deleteServiceFromSuggestions(description: String) {
        viewModelScope.launch {
            presupuestos.value.forEach { p ->
                if (p.serviciosJson.contains(description)) {
                    val updated = p.serviciosJson.split("|").filter { it.split(";").getOrNull(1) != description }.joinToString("|")
                    repository.updatePresupuesto(p.copy(serviciosJson = updated))
                }
            }
        }
    }

    fun saveProfessionalFeeToSuggestions(item: BudgetProfessionalFee) {
        viewModelScope.launch {
            val catalogId = "__catalog_fees__"
            val newEntry = "${item.code};${item.description};${item.total}"
            val existing = repository.getPresupuestoById(catalogId)
            if (existing != null) {
                val alreadyIn = existing.honorariosJson.split("|").any { it.split(";").getOrNull(1) == item.description }
                if (!alreadyIn) {
                    val updated = if (existing.honorariosJson.isBlank()) newEntry else "${existing.honorariosJson}|$newEntry"
                    repository.updatePresupuesto(existing.copy(honorariosJson = updated))
                }
            } else {
                repository.insertPresupuesto(PresupuestoEntity(id = catalogId, numeroPresupuesto = "", clienteId = "", prestadorId = "", fecha = "", subtotal = 0.0, impuestos = 0.0, total = 0.0, estado = "__catalog__", honorariosJson = newEntry))
            }
        }
    }

    fun deleteProfessionalFeeFromSuggestions(description: String) {
        viewModelScope.launch {
            presupuestos.value.forEach { p ->
                if (p.honorariosJson.contains(description)) {
                    val updated = p.honorariosJson.split("|").filter { it.split(";").getOrNull(1) != description }.joinToString("|")
                    repository.updatePresupuesto(p.copy(honorariosJson = updated))
                }
            }
        }
    }

    //Multi-select
    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val isSelectionMode: StateFlow<Boolean> = _selectedIds
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleSelection(id: String) {
        _selectedIds.update { current ->
            if (current.contains(id)) current - id else current + id
        }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val toDelete = presupuestos.value.filter { it.id in _selectedIds.value }
            toDelete.forEach { repository.deletePresupuesto(it) }
            _selectedIds.value = emptySet()
        }
    }


}
