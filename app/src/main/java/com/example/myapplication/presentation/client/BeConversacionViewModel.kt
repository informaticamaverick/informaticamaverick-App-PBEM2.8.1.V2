package com.example.myapplication.presentation.client

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.presentation.components.BeMessage
import com.example.myapplication.presentation.registry.BeDictionary
import com.example.myapplication.presentation.registry.BeConversacion
import com.example.myapplication.data.repository.AppActionCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- BE CONVERSACION VIEWMODEL (EL OBRERO DE LAS PALABRAS) ---
 * Este ViewModel se encarga de gestionar el contenido de las burbujas de Be.
 * Escucha el contexto de BeBrain y procesa las búsquedas para generar respuestas.
 */
@HiltViewModel
class BeConversacionViewModel @Inject constructor(
    private val coordinator: AppActionCoordinator
) : ViewModel() {

    // ======================================================================================
    // --- 1. ESTADO DE MENSAJES (COLA / TIPS) ---
    // ======================================================================================
    private val _contextMessages = MutableStateFlow<List<BeMessage>>(emptyList())
    val contextMessages: StateFlow<List<BeMessage>> = _contextMessages.asStateFlow()

    private val _currentContext = MutableStateFlow(HUDContext.HOME)

    // ======================================================================================
    // --- 2. ESTADO DE RESPUESTA ACTIVA (BÚSQUEDA) ---
    // ======================================================================================
    private val _activeResponse = MutableStateFlow<BeMessage?>(null)
    val activeResponse: StateFlow<BeMessage?> = _activeResponse.asStateFlow()

    init {
        // ==================================================================================
        // --- SECCIÓN: OBSERVADORES GLOBALES (SSOT) ---
        // ==================================================================================
        
        // 0. Sincronización Automática de Contexto (Coordinator -> Obrero)
        viewModelScope.launch {
            coordinator.currentHUDContext.collectLatest { context ->
                updateContext(context)
            }
        }

        // 1. Escuchamos el cambio de consulta para limpiar si es necesario
        viewModelScope.launch {
            coordinator.globalSearchQuery.collectLatest { query ->
                if (query.isEmpty()) {
                    _activeResponse.value = null
                    coordinator.updateMatchedCategories(emptyList())
                }
            }
        }

        // 2. Escuchamos las categorías encontradas por las pantallas (Obreros)
        viewModelScope.launch {
            coordinator.matchedCategories.collectLatest { categories ->
                // Solo reaccionamos si hay una búsqueda activa y estamos en contexto FAST
                if (coordinator.globalSearchQuery.value.isNotEmpty()) {
                    processSearchQuery(coordinator.globalSearchQuery.value, categories, isFinalSearch = false)
                }
            }
        }

        // 3. Escuchamos cuando el usuario envía la búsqueda (Enter)
        viewModelScope.launch {
            coordinator.searchSubmittedEvent.collectLatest { query ->
                processSearchQuery(query, isFinalSearch = true)
            }
        }
    }

    /**
     * Actualiza la lista de mensajes (tips) según la pantalla actual.
     */
    fun updateContext(context: HUDContext) {
        if (_currentContext.value == context && _contextMessages.value.isNotEmpty()) return
        _currentContext.value = context
        
        val messages = when (context) {
            HUDContext.HOME -> BeDictionary.HomeMessages
            HUDContext.BUDGETS, HUDContext.BUDGETS_TENDERS -> BeDictionary.BudgetMessages
            HUDContext.CHAT -> BeDictionary.ChatMessages
            HUDContext.CALENDAR -> BeDictionary.CalendarMessages
            HUDContext.SEARCH_RESULTS -> listOf(BeMessage("🔍", "Aquí tienes los prestadores de esta categoría.", null, Color(0xFF22D3EE)))
            HUDContext.FAST -> listOf(BeMessage("⚡", "Búsqueda táctica activada. Solo unidades de respuesta inmediata.", null, Color(0xFF22D3EE)))
            else -> BeDictionary.DefaultMessages
        }
        _contextMessages.value = messages
    }

    /**
     * Procesa una consulta de búsqueda y genera una respuesta para la burbuja superior.
     * En contexto FAST, puede recibir categorías coincidentes para mostrarlas.
     */
    fun processSearchQuery(
        query: String, 
        matchedCategories: List<com.example.myapplication.data.local.CategoryEntity> = emptyList(),
        isFinalSearch: Boolean = false
    ) {
        if (query.isEmpty()) {
            _activeResponse.value = null
            return
        }

        viewModelScope.launch {
            if (_currentContext.value == HUDContext.FAST && !isFinalSearch) {
                // ==================================================================================
                // --- SECCIÓN: RESPUESTA DINÁMICA CON CATEGORÍAS (MODO FAST - TIEMPO REAL) ---
                // ==================================================================================
                if (matchedCategories.isNotEmpty()) {
                    _activeResponse.value = BeMessage(
                        icon = "🔍",
                        text = "He encontrado estas categorías para ti:",
                        bubbleColor = Color(0xFF22D3EE),
                        categories = matchedCategories.take(3) // Mostramos máximo 3 para no saturar
                    )
                } else {
                    _activeResponse.value = BeMessage(
                        icon = "⚡",
                        text = "Escribe para buscar servicios de urgencia...",
                        bubbleColor = Color(0xFF22D3EE)
                    )
                }
            } else if (isFinalSearch) {
                // ==================================================================================
                // --- SECCIÓN: RESPUESTA CONVERSACIONAL (SOLO AL PRESIONAR ENTER) ---
                // ==================================================================================
                val response = BeConversacion.getResponse(query)
                _activeResponse.value = response
            }
        }
    }

    /**
     * Limpia la respuesta activa (por ejemplo, al cerrar la búsqueda).
     */
    fun clearActiveResponse() {
        _activeResponse.value = null
    }
}
