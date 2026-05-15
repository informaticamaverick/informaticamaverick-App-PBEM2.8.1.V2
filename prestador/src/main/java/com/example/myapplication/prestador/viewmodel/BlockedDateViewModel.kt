package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.BlockedDateEntity
import com.example.myapplication.prestador.data.local.entity.BlockedDateReason
import com.example.myapplication.prestador.data.repository.BlockedDateFirestoreSync
import com.example.myapplication.prestador.data.repository.BlockedDateRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle

@HiltViewModel
class BlockedDateViewModel @Inject constructor(
    private val repository: BlockedDateRepository,
    private val sync: BlockedDateFirestoreSync,
    private val auth: FirebaseAuth,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    data class HolidayItem(val date: String, val label: String)

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val msg: String) : UiState()
        data class Error(val msg: String) : UiState()
    }

    // Usa owner_id de los args de navegación si está disponible (empresa/sucursal),
    // de lo contrario usa el uid del prestador autenticado.
    private val providerId get() = savedStateHandle.get<String>("owner_id") ?: auth.currentUser?.uid ?: ""

    val blockedDates: StateFlow<List<BlockedDateEntity>> = repository
        .getActiveByProvider(providerId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _argentineHolidays = MutableStateFlow<List<HolidayItem>>(emptyList())
    val argentineHolidays: StateFlow<List<HolidayItem>> = _argentineHolidays.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (providerId.isNotBlank()) {
                sync.pullToRoom(providerId)
            }
        }
        viewModelScope.launch {
            fetchHolidays()
        }
    }

    private suspend fun fetchHolidays() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        try {
            val items = mutableListOf<HolidayItem>()
            listOf(currentYear, currentYear + 1).forEach { year ->
                val response = withContext(Dispatchers.IO) {
                    val conn = URL("https://nolaborables.com.ar/api/v2/feriados/$year").openConnection()
                    conn.connectTimeout = 5000
                    conn.readTimeout = 5000
                    conn.getInputStream().bufferedReader().readText()
                }
                val array = JSONArray(response)
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val month = item.optInt("mes")
                    val day = item.optInt("dia")
                    if (month in 1..12 && day in 1..31) {
                        items.add(
                            HolidayItem(
                                date = String.format("%04d-%02d-%02d", year, month, day),
                                label = item.optString("motivo").ifBlank { "Feriado" }
                            )
                        )
                    }
                }
            }
            _argentineHolidays.value = items.sortedBy { it.date }.distinctBy { it.date }
        } catch (e: Exception) {
            // Sin internet: usar feriados fijos de Argentina como fallback
            _argentineHolidays.value = buildFallbackHolidays(currentYear)
        }
    }

    private fun buildFallbackHolidays(baseYear: Int): List<HolidayItem> {
        // Feriados inamovibles de Argentina para 2 años
        val fixed = listOf(
            Pair(1,  1)  to "Año Nuevo",
            Pair(3,  24) to "Día de la Memoria",
            Pair(4,  2)  to "Día de Malvinas",
            Pair(5,  1)  to "Día del Trabajador",
            Pair(5,  25) to "Revolución de Mayo",
            Pair(6,  20) to "Paso a la Inmortalidad del Gral. Belgrano",
            Pair(7,  9)  to "Día de la Independencia",
            Pair(12, 8)  to "Inmaculada Concepción de María",
            Pair(12, 25) to "Navidad"
        )
        return listOf(baseYear, baseYear + 1).flatMap { year ->
            fixed.map { (md, label) ->
                HolidayItem(
                    date = String.format("%04d-%02d-%02d", year, md.first, md.second),
                    label = label
                )
            }
        }.sortedBy { it.date }
    }

    fun isDateBlocked(date: String): Boolean {
        return blockedDates.value.any { it.date == date && it.isActive }
    }

    fun toggleHoliday(holiday: HolidayItem) {
        viewModelScope.launch {
            if (providerId.isBlank()) {
                _uiState.value = UiState.Error("No se pudo identificar al prestador")
                return@launch
            }

            _uiState.value = UiState.Loading
            try {
                val existing = repository.getByProviderAndDate(providerId, holiday.date)
                if (existing != null) {
                    repository.deleteById(existing.id)
                    val syncResult = sync.deleteById(existing.id)
                    if (syncResult.isFailure) {
                        _uiState.value = UiState.Error(
                            "Fecha desbloqueada localmente, pero falló la sincronización: ${syncResult.exceptionOrNull()?.message ?: "Error"}"
                        )
                        return@launch
                    }
                    _uiState.value = UiState.Success("Fecha desbloqueada correctamente")
                } else {
                    val entity = BlockedDateEntity(
                        id = "${providerId}_${holiday.date}",
                        providerId = providerId,
                        date = holiday.date,
                        label = holiday.label,
                        reason = BlockedDateReason.HOLIDAY.name,
                        isActive = true
                    )
                    repository.save(entity)
                    val syncResult = sync.upsert(entity)
                    if (syncResult.isFailure) {
                        _uiState.value = UiState.Error(
                            "Fecha bloqueada localmente, pero falló la sincronización: ${syncResult.exceptionOrNull()?.message ?: "Error"}"
                        )
                        return@launch
                    }
                    _uiState.value = UiState.Success("Fecha bloqueada correctamente")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "No se pudo actualizar el feriado")
            }
        }
    }

    fun addCustomDate(date: String, label: String) {
        viewModelScope.launch {
            if (providerId.isBlank()) {
                _uiState.value = UiState.Error("No se pudo identificar al prestador")
                return@launch
            }
            if (date.isBlank()) {
                _uiState.value = UiState.Error("Seleccioná una fecha")
                return@launch
            }

            _uiState.value = UiState.Loading
            try {
                val existing = repository.getByProviderAndDate(providerId, date)
                val newEntity = BlockedDateEntity(
                    id = if (existing?.reason == BlockedDateReason.CUSTOM.name) existing.id else UUID.randomUUID().toString(),
                    providerId = providerId,
                    date = date,
                    label = label.ifBlank { existing?.label ?: "Día bloqueado" },
                    reason = BlockedDateReason.CUSTOM.name,
                    isActive = true,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis()
                )

                if (existing != null && existing.id != newEntity.id) {
                    repository.deleteById(existing.id)
                    sync.deleteById(existing.id)
                }

                repository.save(newEntity)
                val syncResult = sync.upsert(newEntity)
                if (syncResult.isFailure) {
                    _uiState.value = UiState.Error(
                        "Fecha guardada localmente, pero falló la sincronización: ${syncResult.exceptionOrNull()?.message ?: "Error"}"
                    )
                    return@launch
                }

                _uiState.value = UiState.Success("Día bloqueado agregado correctamente")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "No se pudo agregar la fecha")
            }
        }
    }

    fun deleteBlocked(id: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                repository.deleteById(id)
                val syncResult = sync.deleteById(id)
                if (syncResult.isFailure) {
                    _uiState.value = UiState.Error(
                        "Fecha eliminada localmente, pero falló la sincronización: ${syncResult.exceptionOrNull()?.message ?: "Error"}"
                    )
                    return@launch
                }
                _uiState.value = UiState.Success("Día bloqueado eliminado correctamente")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "No se pudo eliminar la fecha")
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
