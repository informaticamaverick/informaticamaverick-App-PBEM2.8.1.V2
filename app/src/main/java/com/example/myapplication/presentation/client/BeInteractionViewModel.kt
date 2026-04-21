package com.example.myapplication.presentation.client

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ServiceDisplayModel
import com.example.myapplication.presentation.components.BeEmotion
import com.example.myapplication.presentation.components.BeMessage
import com.example.myapplication.presentation.components.ControlItem
import com.example.myapplication.presentation.client.BeBrainViewModel.SearchResult
import com.example.myapplication.presentation.registry.BeMenuRegistry
import com.example.myapplication.presentation.registry.BeDictionary
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
 * Representa los diferentes tipos de secciones que la burbuja puede renderizar.
 */
sealed class BubbleSection {
    data class Categories(val items: List<com.example.myapplication.data.local.CategoryEntity>) : BubbleSection()
    data class SuperCategories(val items: List<com.example.myapplication.presentation.client.SuperCategory>) : BubbleSection()
    data class Favorites(val items: List<com.example.myapplication.data.model.Provider>) : BubbleSection()
    data class Budgets(val items: List<com.example.myapplication.data.local.BudgetEntity>) : BubbleSection()
    data class Tenders(val items: List<com.example.myapplication.data.local.TenderEntity>) : BubbleSection()
    data class Providers(val items: List<ServiceDisplayModel>) : BubbleSection()
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
    val actionId: String? = null, // ID del filtro o comando a ejecutar (ej: "sort_price")
    val type: ReactionType = ReactionType.NONE,
    val tags: List<ControlItem> = emptyList(), // 🔥 Etiquetas relacionadas (Categorías, Filtros, etc)
    val results: SearchResult = SearchResult.Empty, // 🔥 Resultados reales de búsqueda fusionados
    val subSections: List<BeReactionSection> = emptyList(), // 🔥 Secciones adicionales para respuestas complejas
    val organizedSections: List<BubbleSection> = emptyList(), // 🔥 NUEVO: El Obrero decide el orden aquí
    val isCategoryExploration: Boolean = false
)

enum class ReactionType {
    NONE,       // No hay reacción especial
    SUGGESTION, // Sugerencia de filtro/orden encontrada
    NOT_FOUND,  // No se entiende lo que escribió
    EASTER_EGG  // Coincidencia con secreto
}

/**
 * BE INTERACTION VIEWMODEL
 * Este es el "Lóbulo Frontal" de Be. Se encarga de procesar el lenguaje natural simplificado
 * del usuario en la barra de búsqueda y decidir cómo debe reaccionar Be emocional y visualmente.
 */
@HiltViewModel
class BeInteractionViewModel @Inject constructor() : ViewModel() {

    // --- ESTADO DE LA REACCIÓN ACTUAL ---
    private val _currentReaction = MutableStateFlow<BeSearchReaction?>(null)
    val currentReaction: StateFlow<BeSearchReaction?> = _currentReaction.asStateFlow()

    // --- NUEVO: ESTADO DEL MENÚ DE BÚSQUEDA (LISTA SOLICITADA) ---
    private val _searchMenuOptions = MutableStateFlow<List<ControlItem>>(emptyList())
    val searchMenuOptions: StateFlow<List<ControlItem>> = _searchMenuOptions.asStateFlow()

    private val _selectedOptionIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedOptionIds: StateFlow<Set<String>> = _selectedOptionIds.asStateFlow()

    // --- RECURSOS DINÁMICOS (SINCRONIZADOS DESDE EL CEREBRO) ---
    private val _availableFilters = MutableStateFlow<List<ControlItem>>(emptyList())
    private val _availableSorts = MutableStateFlow<List<ControlItem>>(emptyList())
    private val _availableCategories = MutableStateFlow<List<ControlItem>>(emptyList())
    private val _currentResults = MutableStateFlow<SearchResult>(SearchResult.Empty)
    private val _activeConversations = MutableStateFlow<List<ServiceDisplayModel>>(emptyList())
    private val _currentContext = MutableStateFlow(HUDContext.HOME)

    // --- ESTADO DE "PENSANDO" ---
    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    // --- HISTORIAL PARA "CHARLAS" ---
    private val _reactionHistory = MutableStateFlow<List<BeSearchReaction>>(emptyList())

    // ==========================================================================================
    // --- SECCIÓN: LOGICA DE HUEVO DE PASCUA (ESTADO LOCAL DEL OBRERO) ---
    // ==========================================================================================
    private var _easterEggStep = 0

    /** Resetea el progreso del huevo de pascua */
    fun resetEasterEgg() {
        _easterEggStep = 0
    }

    // --- COLORES ROG PARA LA BURBUJA ---
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
        // Por defecto todos habilitados
        _selectedOptionIds.value = options.map { it.id }.toSet()
    }

    /**
     * Inyecta el Cerebro (BeBrain) para notificarle eventos importantes.
     */
    private var beBrain: BeBrainViewModel? = null
    fun setBeBrain(brain: BeBrainViewModel) {
        this.beBrain = brain
    }

    /**
     * Maneja el clic en una opción del menú de búsqueda de la burbuja (Multi-selección).
     */
    fun onMenuOptionClick(optionId: String) {
        val current = _selectedOptionIds.value.toMutableSet()
        if (current.contains(optionId)) {
            current.remove(optionId)
        } else {
            current.add(optionId)
        }
        _selectedOptionIds.value = current
        
        // La reacción se actualiza según lo que quede seleccionado o el último clic
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
                    results = _currentResults.value // Si el cerebro ya tiene proveedores, se muestran
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

    /**
     * Sincroniza los resultados y el contexto desde el Cerebro (BeBrain).
     * Implementa la Jerarquía por Contexto (Paso 3).
     */
    fun updateResults(results: SearchResult, context: HUDContext) {
        // En HOME, forzamos resultados vacíos para que la burbuja no muestre categorías (redundancia)
        val finalResults = if (context == HUDContext.HOME) SearchResult.Empty else results
        _currentResults.value = finalResults
        _currentContext.value = context
        
        // Si no hay una búsqueda activa o reacción manual, aplicamos la jerarquía inteligente
        if (_currentReaction.value == null || _currentReaction.value?.type == ReactionType.NONE) {
            applyContextHierarchy(finalResults, context)
        } else {
            // Si hay una reacción activa, actualizamos sus secciones organizadas
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

    /**
     * PASO 3: IMPLEMENTAR LA JERARQUÍA POR CONTEXTO (La Inteligencia)
     * Decide qué mostrar prioritariamente en la burbuja según dónde esté el usuario.
     */
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
                    tags = _availableFilters.value, // Prioridad 1: Filtros de estado
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

    /**
     * PASO 5: EL "ENTER" CONVERSACIONAL
     * Dispara un estado de "Pensando" y decide si mostrar resultados o charlar.
     */
    fun onSearchSubmitted(query: String, resultsFound: Boolean, onComplete: (BeSearchReaction) -> Unit) {
        viewModelScope.launch {
            val norm = query.lowercase().trim()

            // --- LÓGICA DE HUEVO DE PASCUA (AL SUBMITIR) ---
            if (norm == "por que te llamas be?" || norm == "porque te llamas be?") {
                _easterEggStep = 1
                val reaction = BeSearchReaction(
                    message = BeDictionary.EasterEggMessages.Step1,
                    type = ReactionType.EASTER_EGG
                )
                _currentReaction.value = reaction
                onComplete(reaction)
                return@launch
            }

            if (_easterEggStep == 1) {
                // Normalizar entrada del usuario para ser flexible con espacios
                val normStep2 = norm.replace("\\s+".toRegex(), " ")
                if (normStep2.contains("ja ja ja buen chiste")) {
                    _easterEggStep = 2
                    val reaction = BeSearchReaction(
                        message = BeDictionary.EasterEggMessages.Step2,
                        type = ReactionType.EASTER_EGG,
                        actionId = "easter_egg_final"
                    )
                    _currentReaction.value = reaction
                    onComplete(reaction)
                } else {
                    _easterEggStep = 0 // Reiniciar ciclo si falla
                    val failureReaction = BeSearchReaction(
                        message = BeDictionary.EasterEggMessages.Failure,
                        type = ReactionType.NOT_FOUND
                    )
                    _currentReaction.value = failureReaction
                    onComplete(failureReaction)
                }
                return@launch
            }

            // Prioridad 1: Si ya tenemos una reacción de tipo EASTER_EGG o Conversacional alta, la mantenemos
            val current = _currentReaction.value
            if (current?.type == ReactionType.EASTER_EGG) {
                onComplete(current)
                return@launch
            }

            _isThinking.value = true
            
            // Si el query es corto, Be "piensa" menos
            val thinkingTime = if (query.length < 5) 600L else 1200L
            delay(thinkingTime)
            
            val finalReaction = if (resultsFound) {
                BeSearchReaction(
                    message = BeMessage("✨", "¡Encontré estos para ti!", null, Color(0xFF22D3EE), emotion = BeEmotion.HAPPY),
                    results = _currentResults.value,
                    organizedSections = getOrganizedSections(_currentResults.value, _currentContext.value)
                )
            } else {
                // Modo charla usando el diccionario
                BeSearchReaction(
                    message = BeDictionary.SearchConversationalMessages.NotFound,
                    type = ReactionType.NOT_FOUND,
                    organizedSections = getOrganizedSections(_currentResults.value, _currentContext.value)
                )
            }

            _isThinking.value = false
            _currentReaction.value = finalReaction
            onComplete(finalReaction)
        }
    }

    /**
     * Sincroniza los recursos disponibles en el contexto actual.
     */
    fun syncResources(
        filters: List<ControlItem>, 
        sorts: List<ControlItem>, 
        categories: List<ControlItem>,
        results: SearchResult = SearchResult.Empty,
        chats: List<ServiceDisplayModel> = emptyList()
    ) {
        _availableFilters.value = filters
        _availableSorts.value = sorts
        _availableCategories.value = categories
        _currentResults.value = results
        _activeConversations.value = chats
        
        // Si hay una reacción activa y cambian los resultados, actualizamos la reacción
        val current = _currentReaction.value
        if (current != null) {
            _currentReaction.value = current.copy(
                results = if (_currentContext.value == HUDContext.HOME) SearchResult.Empty else results
            )
        }
    }

    /**
     * Procesa lo que el usuario escribe en la barra de búsqueda.
     * Analiza palabras clave para sugerir filtros, categorías y respuestas conversacionales.
     */
    fun processSearchQuery(query: String, hasMatches: Boolean = true) {
        if (query.isBlank()) {
            _currentReaction.value = null
            return
        }

        val norm = query.lowercase().trim()

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
                    message = BeDictionary.SearchConversationalMessages.Welcome,
                    type = ReactionType.EASTER_EGG
                )
                norm == "ayuda" || norm == "que haces" -> BeSearchReaction(
                    message = BeDictionary.SearchConversationalMessages.Help,
                    type = ReactionType.SUGGESTION
                )
                norm == "quien eres" || norm == "be" -> BeSearchReaction(
                    message = BeDictionary.SearchConversationalMessages.WhoAmI,
                    type = ReactionType.EASTER_EGG
                )
                else -> null
            }

            if (conversationalMatch != null) {
                // En HOME, no queremos que el saludo arrastre categorías/resultados
                val cleanResults = if (_currentContext.value == HUDContext.HOME) SearchResult.Empty else _currentResults.value
                val reaction = conversationalMatch.copy(results = cleanResults)
                _currentReaction.value = reaction
                addToHistory(reaction)
                return@launch
            }

            // --- SECCIÓN 2: DETECCIÓN MULTI-TAG (CATEGORÍAS + FILTROS + SORTS) ---
            val detectedTags = mutableListOf<ControlItem>()
            val selectedIds = _selectedOptionIds.value
            
            // --- LÓGICA DE INTELIGENCIA CONTEXTUAL (PREGUNTA VS BÚSQUEDA) ---
            val isQuestion = norm.contains("?") || norm.startsWith("que") || norm.startsWith("como") || norm.startsWith("cuales") || norm.startsWith("donde") || norm.contains("filtros") || norm.contains("orden")

            if (isQuestion) {
                // Si pregunta por filtros o ordenamiento específicamente
                if (norm.contains("filtros") || norm.contains("filtrar")) {
                    _currentReaction.value = BeSearchReaction(
                        message = BeMessage("🔍", "Mira, estos son los filtros que puedes aplicar aquí:", null, Color(0xFF22D3EE), emotion = BeEmotion.HAPPY),
                        tags = _availableFilters.value,
                        type = ReactionType.SUGGESTION,
                        subSections = listOf(
                            BeReactionSection(
                                text = "Además, podrías organizar la vista con estos métodos:",
                                tags = _availableSorts.value,
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
                        message = BeMessage("↕️", "Puedes ordenar los resultados con estas opciones:", null, Color(0xFF9B51E0), emotion = BeEmotion.HAPPY),
                        tags = _availableSorts.value,
                        type = ReactionType.SUGGESTION,
                        subSections = listOf(
                            BeReactionSection(
                                text = "También tienes estos filtros para precisar tu búsqueda:",
                                tags = _availableFilters.value,
                                icon = "🔍",
                                color = Color(0xFF22D3EE)
                            )
                        )
                    )
                    beBrain?.setHasNewMessage(true)
                    return@launch
                }
            }

            // --- REACCIÓN ESPECÍFICA POR CONTEXTO (Ej: "abierta" en Presupuestos) ---
            if (_currentContext.value == HUDContext.BUDGETS || _currentContext.value == HUDContext.BUDGETS_TENDERS) {
                if (norm == "abierta" || norm == "activas" || norm == "activa") {
                    val filterAbierta = _availableFilters.value.find { 
                        it.label.lowercase().contains("abierta") || it.label.lowercase().contains("activas") 
                    }
                    if (filterAbierta != null) {
                        _currentReaction.value = BeSearchReaction(
                            message = BeMessage("⚖️", "He encontrado el filtro para licitaciones activas. ¿Deseas aplicarlo?", "APLICAR FILTRO", Color(0xFF10B981), emotion = BeEmotion.HAPPY),
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
                // Si el usuario está buscando o el buscador está activo en HOME
                if (query.isNotEmpty() || hasMatches) {
                    val homeTags = listOf(
                        BeMenuRegistry.VIEW_COMPACT,
                        BeMenuRegistry.SORT_HOT
                    )
                    
                    _currentReaction.value = BeSearchReaction(
                        message = BeMessage(
                            icon = "✨",
                            text = "mira puedes cambiar la forma de ver los resultados y la forma de ordenarlos , puedes aplicar los filtros dedes de aqui",
                            bubbleColor = Color(0xFF22D3EE),
                            emotion = BeEmotion.HAPPY
                        ),
                        tags = homeTags,
                        type = ReactionType.SUGGESTION,
                        organizedSections = listOf(
                            BubbleSection.Filters("Explorar categorías", _availableCategories.value)
                        ),
                        isCategoryExploration = true
                    )
                    beBrain?.setHasNewMessage(true)
                } else if (!hasMatches) {
                    _currentReaction.value = BeSearchReaction(
                        message = null, 
                        type = ReactionType.NOT_FOUND
                    )
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
                    // Mensaje eliminado por redundancia, Be se mantiene en estado NOT_FOUND pero sin texto
                    _currentReaction.value = BeSearchReaction(
                        message = null,
                        type = ReactionType.NOT_FOUND
                    )
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
                    it.label.lowercase().contains(norm) || norm.contains(it.label.lowercase())
                }

                if (filteredList.isNotEmpty()) {
                    val reactionMessage = current.message
                    val reaction = current.copy(
                        message = reactionMessage?.copy(text = "Resultados para \"$query\":"),
                        tags = filteredList,
                        results = SearchResult(genericItems = filteredList)
                    )
                    _currentReaction.value = reaction
                    return@launch
                }
            }

            // Buscar en Categorías (Solo si NO estamos en HOME para evitar redundancia)
            if (selectedIds.contains("menu_categories") && _currentContext.value != HUDContext.HOME) {
                _availableCategories.value.filter { it.label.lowercase().contains(norm) || norm.contains(it.label.lowercase()) }
                    .forEach { detectedTags.add(it) }
            }

            // Buscar en Filtros
            if (selectedIds.contains("menu_filters")) {
                _availableFilters.value.filter { it.label.lowercase().contains(norm) || norm.contains(it.label.lowercase()) }
                    .forEach { detectedTags.add(it) }
            }

            // Buscar en Ordenamientos
            if (selectedIds.contains("menu_sort")) {
                _availableSorts.value.filter { it.label.lowercase().contains(norm) || norm.contains(it.label.lowercase()) }
                    .forEach { detectedTags.add(it) }
            }

            if (detectedTags.isNotEmpty()) {
                val primary = detectedTags.first()
                _currentReaction.value = BeSearchReaction(
                    message = BeMessage(
                        icon = primary.emoji ?: "🔍",
                        text = if (detectedTags.size > 1) 
                            "He encontrado varias opciones que coinciden con tu búsqueda:" 
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
        if (current.size > 10) current.removeAt(0) // Mantener últimos 10
        _reactionHistory.value = current
    }

    /**
     * Limpia la reacción actual (ej: al cerrar la burbuja o borrar el texto).
     */
    fun clearReaction() {
        _currentReaction.value = null
    }
}
