
/**
package com.example.myapplication.presentation.features.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.common.extensions.matchesSmart
import com.example.myapplication.core.common.extensions.prepareForSearch
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.data.local.entity.BudgetEntity
import com.example.myapplication.core.data.local.entity.TenderEntity
import com.example.myapplication.data.model.ProviderDisplayModel
import com.example.myapplication.presentation.components.BeEmotion
import com.example.myapplication.presentation.components.BeMessage
import com.example.myapplication.presentation.components.ControlItem
import com.example.myapplication.presentation.global.HUDContext
import com.example.myapplication.presentation.global.BeBrainViewModel
//import com.example.myapplication.presentation.registry.BeMenuRegistry
import com.example.myapplication.presentation.registry.BeDictionaryConversation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ==========================================================================================
// --- MODELOS DE INTERACCIÓN REACTIVA ---
// ==========================================================================================

/**
 * Representa los resultados de búsqueda consolidados para Be.
 */
data class SearchResult(
    val categories: List<CategoryEntity> = emptyList(),
    val superCategories: List<SuperCategory> = emptyList(),
    val favorites: List<ProviderDisplayModel> = emptyList(), // Cambiado a DisplayModel para consistencia
    val tenders: List<TenderEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val providers: List<ProviderDisplayModel> = emptyList(),
    val genericItems: List<ControlItem> = emptyList()
) {
    companion object {
        val Empty = SearchResult()
    }
    fun isEmpty() = categories.isEmpty() && superCategories.isEmpty() && favorites.isEmpty() && 
                   tenders.isEmpty() && budgets.isEmpty() && providers.isEmpty() && genericItems.isEmpty()
}

/**
 * Representa los diferentes tipos de secciones que la burbuja puede renderizar.
 */
sealed class BubbleSection {
    data class Categories(val items: List<CategoryEntity>) : BubbleSection()
    data class SuperCategories(val items: List<SuperCategory>) : BubbleSection()
    data class Favorites(val items: List<ProviderDisplayModel>) : BubbleSection()
    data class Budgets(val items: List<BudgetEntity>) : BubbleSection()
    data class Tenders(val items: List<TenderEntity>) : BubbleSection()
    data class Providers(val items: List<ProviderDisplayModel>) : BubbleSection()
    data class Filters(val title: String, val items: List<ControlItem>) : BubbleSection()
    data class Generic(val items: List<ControlItem>) : BubbleSection()
    object SortOptions : BubbleSection()
}

/**
 * Representa una sección dentro de una respuesta compleja de Be.
 */
data class BeReactionSection(
    val text: String? = null,
    val tags: List<ControlItem> = emptyList(),
    val results: SearchResult = SearchResult.Empty,
    val icon: String? = null,
    val color: Color = Color.White
)

/**
 * Representa la respuesta de Be a una acción del usuario (como escribir en la búsqueda).
 */
data class BeSearchReaction(
    val message: BeMessage? = null,
    val actionId: String? = null, 
    val type: ReactionType = ReactionType.NONE,
    val tags: List<ControlItem> = emptyList(), 
    val results: SearchResult = SearchResult.Empty, 
    val subSections: List<BeReactionSection> = emptyList(), 
    val organizedSections: List<BubbleSection> = emptyList(),
    val isCategoryExploration: Boolean = false
)

enum class ReactionType {
    NONE, SUGGESTION, NOT_FOUND, EASTER_EGG
}

/**
 * BE INTERACTION VIEWMODEL
 * Este es el "Lóbulo Frontal" de Be. Se encarga de procesar el lenguaje natural simplificado
 * del usuario en la barra de búsqueda y decidir cómo debe reaccionar Be emocional y visualmente.
 */
@HiltViewModel
class BeInteractionViewModel @Inject constructor() : ViewModel() {

    private val _currentReaction = MutableStateFlow<BeSearchReaction?>(null)
    val currentReaction: StateFlow<BeSearchReaction?> = _currentReaction.asStateFlow()

    private val _searchMenuOptions = MutableStateFlow<List<ControlItem>>(emptyList())
    val searchMenuOptions: StateFlow<List<ControlItem>> = _searchMenuOptions.asStateFlow()

    private val _selectedOptionIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedOptionIds: StateFlow<Set<String>> = _selectedOptionIds.asStateFlow()

    private val _availableFilters = MutableStateFlow<List<ControlItem>>(emptyList())
    private val _availableSorts = MutableStateFlow<List<ControlItem>>(emptyList())
    private val _availableCategories = MutableStateFlow<List<ControlItem>>(emptyList())
    private val _currentResults = MutableStateFlow<SearchResult>(SearchResult.Empty)
    private val _activeConversations = MutableStateFlow<List<ProviderDisplayModel>>(emptyList())
    private val _currentContext = MutableStateFlow(HUDContext.HOME)

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _reactionHistory = MutableStateFlow<List<BeSearchReaction>>(emptyList())

    private var _easterEggStep = 0

    fun resetEasterEgg() { _easterEggStep = 0 }

    private val ROG_Cyan = Color(0xFF00FFFF)
    private val ROG_Red = Color(0xFFE11D48)

    init {
        setupSearchMenu()
    }

    private fun setupSearchMenu() {
        val options = listOf(
            ControlItem("Servicios (categorias)", null, "🛠️", Color(0xFFF59E0B), "menu_categories"),
            ControlItem("Presupuestos", null, "💰", Color(0xFF10B981), "menu_budgets"),
            ControlItem("filtros", Icons.Default.FilterList, "🔍", Color(0xFF22D3EE), "menu_filters"),
            ControlItem("ordenamiento", Icons.Default.Sort, "↕️", Color(0xFF9B51E0), "menu_sort"),
            ControlItem("profecionales ( prestadores)", Icons.Default.People, "👥", Color(0xFF2197F5), "menu_providers"),
            ControlItem("Conversacion con Be", Icons.AutoMirrored.Filled.Chat, "💬", Color(0xFFF43F5E), "menu_conversation"),
            ControlItem("Configuracion", Icons.Default.Settings, "⚙️", Color(0xFF64748B), "menu_settings")
        )
        _searchMenuOptions.value = options
        _selectedOptionIds.value = options.map { it.id }.toSet()
    }

    private var beBrain: BeBrainViewModel? = null
    fun setBeBrain(brain: BeBrainViewModel) {
        this.beBrain = brain
    }

    fun onMenuOptionClick(optionId: String) {
        val current = _selectedOptionIds.value.toMutableSet()
        if (!current.add(optionId)) current.remove(optionId)
        _selectedOptionIds.value = current
        updateReactionForSelection(optionId)
    }

    private fun updateReactionForSelection(lastOptionId: String) {
        val reaction = when (lastOptionId) {
            "menu_filters" -> {
                BeSearchReaction(
                    message = BeMessage("🔍", "Aquí tienes los filtros disponibles para esta pantalla:", null, Color(0xFF22D3EE), emotion = BeEmotion.HAPPY),
                    tags = _availableFilters.value,
                    type = ReactionType.SUGGESTION,
                    results = SearchResult(genericItems = _availableFilters.value),
                    organizedSections = listOf(BubbleSection.Filters("Filtros disponibles", _availableFilters.value))
                )
            }
            "menu_sort" -> {
                BeSearchReaction(
                    message = BeMessage("↕️", "¿Cómo quieres ordenar los resultados?", null, Color(0xFF9B51E0), emotion = BeEmotion.THINKING),
                    tags = _availableSorts.value,
                    type = ReactionType.SUGGESTION,
                    results = SearchResult(genericItems = _availableSorts.value),
                    organizedSections = listOf(BubbleSection.Filters("Opciones de ordenado", _availableSorts.value))
                )
            }
            "menu_providers" -> {
                BeSearchReaction(
                    message = BeMessage("👥", "Estos son los prestadores destacados en tu zona:", null, Color(0xFF10B981), emotion = BeEmotion.HAPPY),
                    type = ReactionType.SUGGESTION,
                    results = _currentResults.value
                )
            }
            "menu_budgets" -> {
                BeSearchReaction(
                    message = BeMessage("💰", "Gestiona tus presupuestos y ofertas activas:", null, Color(0xFF10B981), emotion = BeEmotion.HAPPY),
                    type = ReactionType.SUGGESTION
                )
            }
            "menu_conversation" -> {
                BeSearchReaction(
                    message = BeMessage("💬", "¿En qué puedo ayudarte hoy? Podemos charlar sobre tus servicios o cualquier duda.", null, Color(0xFFF43F5E), emotion = BeEmotion.HAPPY),
                    type = ReactionType.SUGGESTION
                )
            }
            "menu_categories" -> {
                BeSearchReaction(
                    message = BeMessage("📁", "Explora las categorías de servicios:", null, Color(0xFFF59E0B), emotion = BeEmotion.HAPPY),
                    tags = _availableCategories.value,
                    type = ReactionType.SUGGESTION,
                    results = SearchResult(genericItems = _availableCategories.value),
                    organizedSections = listOf(BubbleSection.Filters("Categorías principales", _availableCategories.value))
                )
            }
            "menu_chats" -> {
                val chatItems = _activeConversations.value.map { service ->
                    ControlItem(
                        label = service.title,
                        icon = null,
                        emoji = "💬",
                        color = Color(0xFFF43F5E),
                        id = "chat_${service.id}"
                    )
                }
                BeSearchReaction(
                    message = BeMessage("💬", "Acceso rápido a tus conversaciones recientes:", null, Color(0xFFF43F5E), emotion = BeEmotion.HAPPY),
                    tags = chatItems,
                    type = ReactionType.SUGGESTION,
                    results = SearchResult(genericItems = chatItems),
                    organizedSections = listOf(BubbleSection.Filters("Tus chats", chatItems))
                )
            }
            "menu_settings" -> {
                BeSearchReaction(
                    message = BeMessage("⚙️", "Configuraciones rápidas del asistente:", null, Color(0xFF64748B), emotion = BeEmotion.THINKING),
                    tags = listOf(
                        ControlItem("Notificaciones", Icons.Default.Settings, "🔔", Color.Gray, "set_notif"),
                        ControlItem("Privacidad", Icons.Default.Settings, "🔒", Color.Gray, "set_priv")
                    ),
                    type = ReactionType.SUGGESTION
                )
            }
            "menu_talks" -> {
                val talkItems = _reactionHistory.value.mapIndexed { index, reaction ->
                    ControlItem(
                        label = reaction.message?.text?.take(20) ?: "...",
                        icon = null,
                        emoji = reaction.message?.icon ?: "🔍",
                        color = reaction.message?.bubbleColor ?: Color.White,
                        id = "talk_$index"
                    )
                }.reversed()

                BeSearchReaction(
                    message = BeMessage("✨", "Historial de nuestras interacciones recientes:", null, Color(0xFFFFB6C1), emotion = BeEmotion.HAPPY),
                    tags = talkItems,
                    type = ReactionType.SUGGESTION,
                    results = SearchResult(genericItems = talkItems),
                    organizedSections = listOf(BubbleSection.Filters("¿Sobre qué quieres hablar?", talkItems))
                )
            }
            else -> {
                BeSearchReaction(
                    message = BeMessage("✨", "Pronto tendré más funciones para $lastOptionId", null, Color.Gray, emotion = BeEmotion.THINKING),
                    type = ReactionType.NONE,
                    results = _currentResults.value
                )
            }
        }
        _currentReaction.value = reaction
    }

    fun updateResults(results: SearchResult, context: HUDContext) {
        val finalResults = if (context == HUDContext.HOME) SearchResult.Empty else results
        _currentResults.value = finalResults
        _currentContext.value = context
        
        if (_currentReaction.value == null || _currentReaction.value?.type == ReactionType.NONE) {
            applyContextHierarchy(finalResults, context)
        } else {
            val reaction = _currentReaction.value
            if (reaction != null) {
                _currentReaction.value = reaction.copy(
                    organizedSections = getOrganizedSections(finalResults, context)
                )
            }
        }
    }

    // ==========================================================================================
    // --- SECCIÓN: INTELIGENCIA CONTEXTUAL (EL OBRERO ORGANIZA) ---
    // ==========================================================================================

    /**
     * PASO 2: ORGANIZAR SECCIONES SEGÚN CONTEXTO (LÓGICA DEL OBRERO)
     * Decide el orden y visibilidad de las secciones basándose en HUDContext.
     */
    fun getOrganizedSections(results: SearchResult, context: HUDContext): List<BubbleSection> {
        if (results.isEmpty()) return emptyList()

        return when (context) {
            HUDContext.HOME -> {
                val sections = mutableListOf<BubbleSection>()
                if (results.categories.isNotEmpty()) sections.add(BubbleSection.Categories(results.categories))
                if (results.superCategories.isNotEmpty()) sections.add(BubbleSection.SuperCategories(results.superCategories))
                if (results.favorites.isNotEmpty()) sections.add(BubbleSection.Favorites(results.favorites))
                sections
            }
            HUDContext.BUDGETS, HUDContext.BUDGETS_TENDERS -> {
                listOf(
                    BubbleSection.Filters("Filtros", _availableFilters.value),
                    BubbleSection.Tenders(results.tenders),
                    BubbleSection.SortOptions
                )
            }
            HUDContext.BUDGETS_DIRECT, HUDContext.TENDER_DETAILS -> {
                listOf(
                    BubbleSection.Budgets(results.budgets),
                    BubbleSection.SortOptions
                )
            }
            HUDContext.SEARCH_RESULTS, HUDContext.FAST -> {
                listOf(
                    BubbleSection.Filters("Filtros", _availableFilters.value),
                    BubbleSection.Providers(results.providers),
                    BubbleSection.SortOptions
                )
            }
            else -> listOf(BubbleSection.Generic(results.genericItems))
        }
    }

    private fun applyContextHierarchy(results: SearchResult, context: HUDContext) {
        val organized = getOrganizedSections(results, context)
        
        val reaction = when (context) {
            HUDContext.HOME -> {
                // En HOME, la burbuja puede mostrar un mensaje de bienvenida o ayuda contextual
                // si se dispara manualmente, pero por defecto no mostramos resultados redundantes.
                BeSearchReaction(
                    message = BeMessage("🏠", "¿Qué buscas hoy?", null, Color(0xFF22D3EE)),
                    results = SearchResult.Empty,
                    tags = emptyList(),
                    organizedSections = emptyList()
                )
            }
            HUDContext.BUDGETS, HUDContext.BUDGETS_TENDERS -> {
                BeSearchReaction(
                    message = BeMessage("⚖️", "Gestiona tus licitaciones", null, Color(0xFF9B51E0)),
                    results = results,
                    tags = _availableFilters.value,
                    organizedSections = organized
                )
            }
            HUDContext.SEARCH_RESULTS, HUDContext.FAST -> {
                BeSearchReaction(
                    message = BeMessage("🔍", "Resultados directos para ti", null, Color(0xFF10B981)),
                    results = results,
                    organizedSections = organized
                )
            }
            else -> {
                BeSearchReaction(
                    results = results,
                    organizedSections = organized
                )
            }
        }
        
        _currentReaction.value = reaction
    }

    fun onSearchSubmitted(query: String, resultsFound: Boolean, onComplete: (BeSearchReaction) -> Unit) {
        viewModelScope.launch {
            val norm = query.prepareForSearch()

            if (norm == "por que te llamas be" || norm == "porque te llamas be") {
                _easterEggStep = 1
                val reaction = BeSearchReaction(
                    message = BeDictionaryConversation.EasterEggMessages.Step1,
                    type = ReactionType.EASTER_EGG
                )
                _currentReaction.value = reaction
                onComplete(reaction)
                return@launch
            }

            if (_easterEggStep == 1) {
                if (norm.contains("ja ja ja buen chiste")) {
                    _easterEggStep = 2
                    val reaction = BeSearchReaction(
                        message = BeDictionaryConversation.EasterEggMessages.Step2,
                        type = ReactionType.EASTER_EGG,
                        actionId = "easter_egg_final"
                    )
                    _currentReaction.value = reaction
                    onComplete(reaction)
                } else {
                    _easterEggStep = 0
                    val failureReaction = BeSearchReaction(
                        message = BeDictionaryConversation.EasterEggMessages.Failure,
                        type = ReactionType.NOT_FOUND
                    )
                    _currentReaction.value = failureReaction
                    onComplete(failureReaction)
                }
                return@launch
            }

            val current = _currentReaction.value
            if (current?.type == ReactionType.EASTER_EGG) {
                onComplete(current)
                return@launch
            }

            _isThinking.value = true
            val thinkingTime = if (query.length < 5) 600L else 1200L
            delay(thinkingTime)

            val finalReaction = if (resultsFound) {
                BeSearchReaction(
                    message = BeMessage("✨", "¡Encontré estos para ti!", null, Color(0xFF22D3EE), emotion = BeEmotion.HAPPY),
                    results = _currentResults.value,
                    organizedSections = getOrganizedSections(_currentResults.value, _currentContext.value)
                )
            } else {
                BeSearchReaction(
                    message = BeDictionaryConversation.SearchConversationalMessages.NotFound,
                    type = ReactionType.NOT_FOUND,
                    organizedSections = getOrganizedSections(_currentResults.value, _currentContext.value)
                )
            }

            _isThinking.value = false
            _currentReaction.value = finalReaction
            onComplete(finalReaction)
        }
    }

    fun syncResources(
        filters: List<ControlItem>, 
        sorts: List<ControlItem>, 
        categories: List<ControlItem>,
        results: SearchResult = SearchResult.Empty,
        chats: List<ProviderDisplayModel> = emptyList()
    ) {
        _availableFilters.value = filters
        _availableSorts.value = sorts
        _availableCategories.value = categories
        _currentResults.value = results
        _activeConversations.value = chats
        
        val current = _currentReaction.value
        if (current != null) {
            _currentReaction.value = current.copy(
                results = if (_currentContext.value == HUDContext.HOME) SearchResult.Empty else results
            )
        }
    }

    fun processSearchQuery(query: String, hasMatches: Boolean = true) {
        if (query.isBlank()) {
            _currentReaction.value = null
            return
        }

        // --- LÓGICA DE INTELIGENCIA LINGÜÍSTICA (CLASE ELITE) ---
        // Normalizamos y limpiamos palabras irrelevantes para búsqueda fuzzy
        val norm = query.prepareForSearch()
        val stopWords = setOf("necesito", "un", "una", "de", "del", "la", "el", "en", "para", "con", "por", "quiero", "busco", "alguien")
        val cleanWords = norm.split(" ").filter { it !in stopWords && it.length > 2 }
        val fuzzyQuery = if (cleanWords.isNotEmpty()) cleanWords.joinToString(" ") else norm

        viewModelScope.launch {
            // ==========================================================================================
            // --- SECCIÓN 0: LÓGICA DE HUEVO DE PASCUA (PREVISUALIZACIÓN) ---
            // ==========================================================================================
            // En tiempo real no mostramos el mensaje del huevo de pascua,
            // solo dejamos que BeInteraction sepa que estamos en el flujo.
            val isEasterEggStart = norm == "por que te llamas be?" || norm == "porque te llamas be?"
            val isEasterEggStep2 = _easterEggStep == 1 && norm.contains("ja ja ja buen chiste")

            if (isEasterEggStart || isEasterEggStep2) {
                // Durante el tipeo, si detectamos la palabra clave, podemos poner a Be a "pensar"
                // o simplemente no mostrar nada hasta el Enter.
                // Para cumplir el requerimiento: "no debe mostrar de inmediato la respuesta"
                _currentReaction.value = null
                return@launch
            }

            // --- SECCIÓN 1: DETECCIÓN CONVERSACIONAL (DICCIONARIO CENTRALIZADO) ---
            val conversationalMatch = when {
                norm == "hola" || norm == "buenos dias" -> BeSearchReaction(
                    message = BeDictionaryConversation.SearchConversationalMessages.Welcome,
                    type = ReactionType.EASTER_EGG
                )

                norm == "ayuda" || norm == "que haces" -> BeSearchReaction(
                    message = BeDictionaryConversation.SearchConversationalMessages.Help,
                    type = ReactionType.SUGGESTION
                )

                norm == "quien eres" || norm == "be" -> BeSearchReaction(
                    message = BeDictionaryConversation.SearchConversationalMessages.WhoAmI,
                    type = ReactionType.EASTER_EGG
                )

                else -> null
            }

            if (conversationalMatch != null) {
                // En HOME, no queremos que el saludo arrastre categorías/resultados
                val cleanResults =
                    if (_currentContext.value == HUDContext.HOME) SearchResult.Empty else _currentResults.value
                val reaction = conversationalMatch.copy(results = cleanResults)
                _currentReaction.value = reaction
                addToHistory(reaction)
                return@launch
            }

            // --- SECCIÓN 2: DETECCIÓN MULTI-TAG (CATEGORÍAS + FILTROS + SORTS) ---
            val detectedTags = mutableListOf<ControlItem>()
            val selectedIds = _selectedOptionIds.value

            // --- LÓGICA DE INTELIGENCIA CONTEXTUAL (PREGUNTA VS BÚSQUEDA) ---
            val isQuestion =
                norm.contains("?") || norm.startsWith("que") || norm.startsWith("como") || norm.startsWith(
                    "cuales"
                ) || norm.startsWith("donde") || norm.contains("filtros") || norm.contains("orden")

            if (isQuestion) {
                // Si pregunta por filtros o ordenamiento específicamente
                if (norm.contains("filtros") || norm.contains("filtrar")) {
                    _currentReaction.value = BeSearchReaction(
                        message = BeMessage(
                            "🔍",
                            "Mira, estos son los filtros que puedes aplicar aquí:",
                            null,
                            Color(0xFF22D3EE),
                            emotion = BeEmotion.HAPPY
                        ),
                        tags = _availableFilters.value,
                        type = ReactionType.SUGGESTION,
                        subSections = listOf(
                            BeReactionSection(
                                "Además, podrías organizar la vista con estos métodos:",
                                _availableSorts.value,
                                icon = "↕️",
                                color = Color(0xFF9B51E0)
                            )
                        )
                    )
                    beBrain?.setHasNewMessage(true)
                    return@launch
                }

                if (norm.contains("orden") || norm.contains("ordenar")) {
                    _currentReaction.value = BeSearchReaction(
                        message = BeMessage(
                            "↕️",
                            "Puedes ordenar los resultados con estas opciones:",
                            null,
                            Color(0xFF9B51E0),
                            emotion = BeEmotion.HAPPY
                        ),
                        tags = _availableSorts.value,
                        type = ReactionType.SUGGESTION,
                        subSections = listOf(
                            BeReactionSection(
                                "También tienes estos filtros para precisar tu búsqueda:",
                                _availableFilters.value,
                                icon = "🔍",
                                color = Color(0xFF22D3EE)
                            )
                        )
                    )
                    beBrain?.setHasNewMessage(true)
                    return@launch
                }
            }

            if (_currentContext.value == HUDContext.BUDGETS || _currentContext.value == HUDContext.BUDGETS_TENDERS) {
                if (norm == "abierta" || norm == "activas" || norm == "activa") {
                    val filterAbierta = _availableFilters.value.find {
                        it.label.matchesSmart(fuzzyQuery) || fuzzyQuery.matchesSmart(it.label)
                    }
                    if (filterAbierta != null) {
                        _currentReaction.value = BeSearchReaction(
                            message = BeMessage(
                                "⚖️",
                                "He encontrado el filtro para licitaciones activas. ¿Deseas aplicarlo?",
                                "APLICAR FILTRO",
                                Color(0xFF10B981),
                                emotion = BeEmotion.HAPPY
                            ),
                            actionId = filterAbierta.id,
                            tags = listOf(filterAbierta),
                            type = ReactionType.SUGGESTION
                        )
                        return@launch
                    }
                }
            }

            // --- REACCIÓN EN HOME ---
            if (_currentContext.value == HUDContext.HOME && !isQuestion) {
                if (query.isNotEmpty() || hasMatches) {
                    // val homeTags = listOf(BeMenuRegistry.VIEW_COMPACT, BeMenuRegistry.SORT_HOT)
                    _currentReaction.value = BeSearchReaction(
                        message = BeMessage(
                            "✨",
                            "mira puedes cambiar la forma de ver los resultados y la forma de ordenarlos , puedes aplicar los filtros dedes de aqui",
                            null,
                            Color(0xFF22D3EE),
                            emotion = BeEmotion.HAPPY
                        ),
                        //   tags = homeTags,
                        type = ReactionType.SUGGESTION,
                        organizedSections = listOf(
                            BubbleSection.Filters(
                                "Explorar categorías",
                                _availableCategories.value
                            )
                        ),
                        isCategoryExploration = true
                    )
                    beBrain?.setHasNewMessage(true)
                } else if (!hasMatches) {
                    _currentReaction.value =
                        BeSearchReaction(message = null, type = ReactionType.NOT_FOUND)
                    beBrain?.setHasNewMessage(true)
                } else {
                    _currentReaction.value = null
                }
                return@launch
            }

            // --- REACCIÓN EN PRESUPUESTOS (NUEVO) ---
            val isBudgetContext = _currentContext.value == HUDContext.BUDGETS ||
                    _currentContext.value == HUDContext.BUDGETS_TENDERS ||
                    _currentContext.value == HUDContext.BUDGETS_DIRECT ||
                    _currentContext.value == HUDContext.TENDER_DETAILS

            if (isBudgetContext && !isQuestion) {
                if (!hasMatches) {
                    _currentReaction.value =
                        BeSearchReaction(message = null, type = ReactionType.NOT_FOUND)
                    beBrain?.setHasNewMessage(true)
                } else {
                    // Si hay matches, limpiamos reacción previa para permitir detección de tags
                    _currentReaction.value = null
                }
                // No retornamos aquí para permitir que la detección de tags de abajo (filtros, etc) funcione
            }

            // --- NUEVO: FILTRADO POR CONTEXTO DE MENÚ ---
            // Si hay una reacción de tipo SUGGESTION que proviene de un menú (ej: Categorías),
            // la búsqueda actual filtra esa lista en lugar de buscar globalmente.
            val current = _currentReaction.value
            if (current != null && current.results.genericItems.isNotEmpty()) {
                val filteredList = current.results.genericItems.filter {
                    it.label.matchesSmart(fuzzyQuery) || fuzzyQuery.matchesSmart(it.label)
                }

                if (filteredList.isNotEmpty()) {
                    val reaction = current.copy(
                        message = current.message?.copy(text = "Resultados para \"$query\":"),
                        tags = filteredList,
                        results = SearchResult(genericItems = filteredList)
                    )
                    _currentReaction.value = reaction
                    return@launch
                }
            }

            // Buscar en Categorías (Solo si NO estamos en HOME para evitar redundancia)
            if (selectedIds.contains("menu_categories") && _currentContext.value != HUDContext.HOME) {
                _availableCategories.value.filter {
                    it.label.matchesSmart(fuzzyQuery) || fuzzyQuery.matchesSmart(
                        it.label
                    )
                }
                    .forEach { detectedTags.add(it) }
            }

            // Buscar en Filtros
            if (selectedIds.contains("menu_filters")) {
                _availableFilters.value.filter {
                    it.label.matchesSmart(fuzzyQuery) || fuzzyQuery.matchesSmart(
                        it.label
                    )
                }
                    .forEach { detectedTags.add(it) }
            }

            if (selectedIds.contains("menu_sort")) {
                _availableSorts.value.filter {
                    it.label.matchesSmart(fuzzyQuery) || fuzzyQuery.matchesSmart(
                        it.label
                    )
                }
                    .forEach { detectedTags.add(it) }
            }

            if (detectedTags.isNotEmpty()) {
                val primary = detectedTags.first()
                _currentReaction.value = BeSearchReaction(
                    message = BeMessage(
                        icon = primary.emoji ?: "🔍",
                        text = if (detectedTags.size > 1) "He encontrado varias opciones que coinciden con tu búsqueda:"
                        else "¿Te gustaría aplicar este filtro para mejorar los resultados?",
                        actionText = if (detectedTags.size == 1) "APLICAR" else null,
                        bubbleColor = primary.color,
                        emotion = BeEmotion.HAPPY
                    ),
                    actionId = if (detectedTags.size == 1) primary.id else null,
                    tags = detectedTags,
                    type = ReactionType.SUGGESTION,
                    results = _currentResults.value
                )
                return@launch
            }

            // --- SECCIÓN 3: LÓGICA DE DETECCIÓN MANUAL (FALLBACK) ---
            /**
            val reaction = when {
            norm.contains("menor") || norm.contains("barato") || norm.contains("precio") -> {
            val priceSort = _availableSorts.value.find { it.id == BeMenuRegistry.SORT_PRICE.id }
            BeSearchReaction(
            message = BeMessage("💰", "¿Quieres que ordene por el menor precio?", "ORDENAR AHORA", ROG_Cyan, emotion = BeEmotion.HAPPY),
            actionId = BeMenuRegistry.SORT_PRICE.id,
            tags = priceSort?.let { listOf(it) } ?: emptyList(),
            type = ReactionType.SUGGESTION,
            results = _currentResults.value
            )
            }
            norm.contains("cerca") || norm.contains("distancia") || norm.contains("ubicacion") -> {
            val distSort = _availableSorts.value.find { it.id == BeMenuRegistry.SORT_DISTANCE.id }
            BeSearchReaction(
            message = BeMessage("📍", "¿Quieres ver los resultados más cercanos a ti?", "ORDENAR POR CERCANÍA", ROG_Cyan, emotion = BeEmotion.SURPRISED),
            actionId = BeMenuRegistry.SORT_DISTANCE.id,
            tags = distSort?.let { listOf(it) } ?: emptyList(),
            type = ReactionType.SUGGESTION,
            results = _currentResults.value
            )
            }
            // 🔥 ELIMINADO: Fallback de "Buscando..." y "No encontrado" para evitar redundancia
            else -> null
            }
             */
        }
            _currentReaction.value = reaction
            if (reaction != null) {
                addToHistory(reaction)
                // 🔥 NOTIFICAR AL CEREBRO QUE HAY UNA NUEVA RESPUESTA SI NO ES SILENCIOSA
                if (reaction.type != ReactionType.NONE) {
                    beBrain?.setHasNewMessage(true)
                }
            }
        }

    }

    fun restoreFromHistory(index: Int) {
        val history = _reactionHistory.value
        if (index in history.indices) {
            // Restauramos la reacción pero sin volver a agregarla al historial
            // para evitar bucles infinitos de la misma reacción.
            _currentReaction.value = history[index]
        }
    }

    private fun addToHistory(reaction: BeSearchReaction) {
        val current = _reactionHistory.value.toMutableList()
        // Evitar duplicados consecutivos del mismo mensaje
        if (current.lastOrNull()?.message?.text == reaction.message?.text) return
        current.add(reaction)
        if (current.size > 10) current.removeAt(0)
        _reactionHistory.value = current
    }

    fun clearReaction() {
        _currentReaction.value = null
    }
}
*/