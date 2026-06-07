package com.example.myapplication.presentation.features.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.local.entity.*
import com.example.myapplication.core.domain.model.MessageType
import com.example.myapplication.core.data.repository.BudgetRepository
import com.example.myapplication.core.data.repository.ChatRepository
import com.example.myapplication.core.data.repository.UserRepository
import com.example.myapplication.core.data.repository.CategoryRepository
import com.example.myapplication.core.data.repository.ProviderRepository // 🔥 Inyectamos ProviderRepository
import com.example.myapplication.data.repository.ShortcutRepository
import com.example.myapplication.presentation.global.AppActionCoordinator
import com.example.myapplication.presentation.components.DropdownItemData
import com.example.myapplication.presentation.components.FilterSortItem
import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import android.util.Log
import com.example.myapplication.core.utils.ImageUtils
import com.example.myapplication.presentation.registry.BeDictionary
import com.example.myapplication.presentation.registry.MaverickIcons
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.myapplication.core.utils.prepareForSearch
import com.example.myapplication.core.utils.wordStartsWithSmart
import com.example.myapplication.core.utils.normalizeForTopic
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.myapplication.core.domain.model.AddressUnico
import com.example.myapplication.presentation.global.HUDContext
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

// AnalyticsState eliminado a favor de MarketAnalyticsState de MarketAnalyticsModels.kt

/**
 * 🔥 [REQUERIMIENTO ELITE] Estructura para estadísticas de licitaciones
 * Permite acceso O(1) durante el renderizado de la lista.
 */
data class TenderStats(
    val totalCount: Int,
    val unreadCount: Int
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: BudgetRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
    private val shortcutRepository: ShortcutRepository,
    private val providerRepository: ProviderRepository, // 🔥 Inyectamos ProviderRepository
    private val auth: FirebaseAuth,
    val coordinator: AppActionCoordinator,
    @ApplicationContext private val context: Context
) : ViewModel() {

    /**
     * 🔥 ELITE: Exponemos el flujo de presupuestos para acciones bajo demanda (ej: Comparar).
     */
    val allBudgets: Flow<List<BudgetEntity>> = repository.allBudgets

    // ==========================================================
    // 1. ESTADOS DE BUSQUEDA Y SELECCION (Delegados al Coordinador)
    // ==========================================================
    val searchQuery = coordinator.globalSearchQuery

    private val _isMultiSelectionActive = MutableStateFlow(false)
    val isMultiSelectionActive = _isMultiSelectionActive.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds = _selectedIds.asStateFlow()

    val currentHUDContext = coordinator.currentHUDContext

    /**
     * Accesos directos dinámicos guardados en Room.
     */
    val shortcuts: StateFlow<List<FilterSortItem>> = shortcutRepository.getShortcutsByContext("budget")
        .map { list ->
            list.mapNotNull { shortcut ->
                BeDictionary.Filters[shortcut.targetId]?.let { data ->
                    FilterSortItem(
                        id = data.id,
                        label = data.label,
                        emoji = data.emoji ?: "🔹",
                        icon = data.icon,
                        color = data.color,
                        section = data.section
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun manageShortcut(id: String, add: Boolean) {
        viewModelScope.launch {
            if (add) shortcutRepository.addShortcut("budget", id, "filter")
            else shortcutRepository.removeShortcut("budget", id, "filter")
        }
    }

    private val _activeSortCriteria = MutableStateFlow<List<String>>(emptyList())
    val activeSortCriteria: StateFlow<List<String>> = _activeSortCriteria.asStateFlow()

    fun toggleFilter(id: String) {
        val currentFilters = coordinator.activeFilters.value.toMutableSet()

        if (id.startsWith("sort_")) {
            setSortOrder(id)
            return
        }
        
        if (id == "CLEAR_ALL") {
            currentFilters.clear()
            _activeSortCriteria.value = emptyList()
        } else {
            if (!currentFilters.remove(id)) {
                currentFilters.add(id)
            }
        }

        coordinator.updateFilters(currentFilters)
    }

    fun setSortOrder(sortId: String?) {
        if (sortId == null) {
            _activeSortCriteria.value = emptyList()
            return
        }

        val current = _activeSortCriteria.value.toMutableList()
        if (current.contains(sortId)) {
            current.remove(sortId)
        } else {
            current.add(sortId)
        }
        _activeSortCriteria.value = current
    }

    fun clearSpecificFilters(prefixes: List<String>) {
        val current = coordinator.activeFilters.value.toMutableSet()
        current.removeAll { filterId -> prefixes.any { filterId.startsWith(it) } }
        coordinator.updateFilters(current)
    }

    val hasMatches = coordinator.hasMatches

    private val _selectedTenderId = MutableStateFlow<String?>(null)
    fun setSelectedTenderId(id: String?) { 
        _selectedTenderId.value = id 
    }

    fun setContext(context: HUDContext) {
        val current = coordinator.currentHUDContext.value
        val isTenderRelated = (current == HUDContext.BUDGETS_TENDERS || current == HUDContext.TENDER_DETAILS) &&
                             (context == HUDContext.BUDGETS_TENDERS || context == HUDContext.TENDER_DETAILS)

        if (current != context && !isTenderRelated) {
            resetPageState()
        }
        coordinator.updateHUDContext(context)
    }

    // ==========================================================
    // 2. DATOS FILTRADOS Y ORDENADOS (Elite SSOT)
    // ==========================================================
    val allTenders: StateFlow<List<TenderEntity>> = repository.allTenders
        .map { tenders ->
            val currentUserId = auth.currentUser?.uid ?: "user_demo_66"
            tenders.filter { it.clientId == currentUserId }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * 🔥 [CARGA ON-DEMAND] Metadatos de presupuestos por licitación.
     * Delegamos al Repositorio para obtener solo IDs y conteos (Costo Cero RAM).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val budgetStats: StateFlow<Map<String, TenderStats>> = repository.allTenders
        .flatMapLatest { tenders ->
            // Si el repositorio aún no tiene el flujo optimizado, usamos el Flow de todos los presupuestos
            // pero filtrando solo lo necesario en este Obrero.
            repository.allBudgets.map { budgets ->
                budgets.filter { it.tenderId != null }
                    .groupBy { it.tenderId!! }
                    .mapValues { (_, list) ->
                        TenderStats(
                            totalCount = list.size,
                            unreadCount = list.count { !it.isRead }
                        )
                    }
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // --- SECCIÓN: FILTROS DINÁMICOS (Mapeo Elite) ---
    val filterDropdownItems: StateFlow<List<DropdownItemData>> = combine(
        allTenders,
        categoryRepository.allCategories
    ) { tenders, categories ->
        val tenderCats = tenders.map { it.category }.distinct().map { catName ->
            val categoryInfo = categories.find { it.name.equals(catName, ignoreCase = true) }
            DropdownItemData("cat_$catName", catName, "CATEGORÍAS", categoryInfo?.icon ?: "📋", MaverickIcons.Filter)
        }
        listOfNotNull(
            BeDictionary.Filters["filter_tender_active"],
            BeDictionary.Filters["filter_tender_closed"],
            BeDictionary.Filters["filter_tender_awarded"]
        ) + tenderCats
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sortDropdownItems: StateFlow<List<DropdownItemData>> = flowOf(
        listOfNotNull(
            BeDictionary.Sorts["sort_alpha"],
            BeDictionary.Sorts["sort_date"]
        )
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkExpiredTenders()
    }

    private fun checkExpiredTenders() {
        viewModelScope.launch {
            allTenders.collectLatest { tenders ->
                val now = System.currentTimeMillis()
                tenders.filter { 
                    (it.status == "ABIERTA" || it.status == "ACTIVO") && 
                    it.endDate > 0 && 
                    now > it.endDate 
                }.forEach { expired ->
                    updateTenderStatus(expired.tenderId, "CERRADA")
                }
            }
        }
    }

    fun resetPageState() {
        updateMultiSelection(false)
        coordinator.updateSearchQuery("")
        coordinator.updateFilters(emptySet<String>())
    }

    val filteredTenders: StateFlow<List<TenderEntity>> = combine(
        allTenders,
        coordinator.globalSearchQuery,
        coordinator.activeFilters,
        _activeSortCriteria,
        budgetStats,
        coordinator.currentHUDContext,
        coordinator.selectedProfileId
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val tenders = args[0] as List<TenderEntity>
        val query = args[1] as String
        @Suppress("UNCHECKED_CAST")
        val activeFilters = args[2] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val sortCriteria = args[3] as List<String>
        @Suppress("UNCHECKED_CAST")
        val statsMap = args[4] as Map<String, TenderStats>
        val context = args[5] as HUDContext
        val profileId = args[6] as String?

        var list = if (query.isNotEmpty()) {
            val normalized = query.prepareForSearch()
            tenders.filter { it.title.wordStartsWithSmart(normalized) }
        } else {
            tenders
        }

        // --- FILTRADO POR PERFIL (SSOT) ---
        list = if (profileId == null) {
            // Perfil Personal: Tenders sin nombre de empresa (o donde el usuario es el emisor personal)
            list.filter { it.companyName == null }
        } else {
            // Perfil de Empresa: Tenders emitidos por esa empresa
            // [TODO ELITE]: En el futuro, usar companyId para mayor precisión.
            // Por ahora, usamos el ID del perfil que el coordinador sincroniza (que coincide con el ownerId).
            // Pero como TenderEntity no tiene companyId, intentamos buscar por nombre si es posible,
            // o asumimos que el repositorio ya nos dio solo los del usuario.
            // [FIX TEMPORAL]: Si hay un profileId, filtramos por companyName no nulo.
            list.filter { it.companyName != null }
        }

        val catFilters = activeFilters.filter { it.startsWith("cat_") }.map { it.removePrefix("cat_") }
        if (catFilters.isNotEmpty()) {
            list = list.filter { tender -> 
                catFilters.any { it.equals(tender.category, ignoreCase = true) }
            }
        }

        val stateFilters = activeFilters.filter { it.startsWith("filter_tender_") }
        if (stateFilters.isNotEmpty()) {
            list = list.filter { tender ->
                stateFilters.any { filterId ->
                    when (filterId) {
                        "filter_tender_active" -> tender.status == "ABIERTA"
                        "filter_tender_closed" -> tender.status == "CERRADA"
                        "filter_tender_canceled" -> tender.status == "CANCELADA"
                        "filter_tender_awarded" -> tender.status == "ADJUDICADA"
                        else -> false
                    }
                }
            }
        }

        // --- ORDENAMIENTO ELITE EN CASCADA ---
        var comparator = compareBy<TenderEntity> { tender ->
            when (tender.status) {
                "ABIERTA" -> 1
                "ADJUDICADA" -> 2
                "CERRADA" -> 3
                "CANCELADA" -> 4
                else -> 5
            }
        }.thenByDescending { tender ->
            if (tender.status == "ABIERTA") {
                (statsMap[tender.tenderId]?.unreadCount ?: 0) > 0
            } else false
        }

        sortCriteria.forEach { criteria ->
            comparator = when (criteria) {
                "sort_alpha" -> comparator.thenBy { it.title }
                "sort_date" -> comparator.thenByDescending { it.dateTimestamp }
                else -> comparator
            }
        }

        list = list.sortedWith(comparator.thenByDescending { it.dateTimestamp })
        
        val isTenderContext = context == HUDContext.BUDGETS_TENDERS || context == HUDContext.BUDGETS

        if (query.isNotEmpty() && list.isEmpty()) {
            if (isTenderContext) coordinator.setHasMatches(false)
            tenders.sortedByDescending { it.dateTimestamp }
        } else {
            if (isTenderContext) coordinator.setHasMatches(true)
            list
        }
    }
    .flowOn(Dispatchers.Default)
    .distinctUntilChanged()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredDirectBudgets: StateFlow<List<BudgetEntity>> = combine(
        repository.directBudgets,
        coordinator.globalSearchQuery,
        coordinator.activeFilters,
        coordinator.currentHUDContext
    ) { budgets, query, activeFilters, context ->
        val list = applyBudgetFilters(budgets, query, activeFilters)

        if (query.isNotEmpty() && list.isEmpty()) {
            if (context == HUDContext.BUDGETS_DIRECT || context == HUDContext.BUDGETS) coordinator.setHasMatches(false)
            budgets.sortedWith(budgetComparator())
        } else {
            if (context == HUDContext.BUDGETS_DIRECT || context == HUDContext.BUDGETS) coordinator.setHasMatches(true)
            list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredOverlayBudgets: StateFlow<List<BudgetEntity>> = _selectedTenderId
        .flatMapLatest { id ->
            if (id == null) {
                coordinator.setHasMatches(true)
                flowOf(emptyList())
            } else {
                repository.getBudgetsForTender(id).combine(
                    combine(coordinator.globalSearchQuery, coordinator.activeFilters, coordinator.currentHUDContext) { q, f, c -> Triple(q, f, c) }
                ) { budgets, params ->
                    val (query, filters, context) = params
                    val list = applyBudgetFilters(budgets, query, filters)
                    
                    if (query.isNotEmpty() && list.isEmpty()) {
                        if (context == HUDContext.TENDER_DETAILS) coordinator.setHasMatches(false)
                        budgets.sortedWith(budgetComparator())
                    } else {
                        if (context == HUDContext.TENDER_DETAILS) coordinator.setHasMatches(true)
                        list
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getFilteredBudgetsForTender(tenderId: String): StateFlow<List<BudgetEntity>> {
        return combine(
            repository.getBudgetsForTender(tenderId), 
            coordinator.globalSearchQuery, 
            coordinator.activeFilters,
            coordinator.currentHUDContext
        ) { budgets, query, activeFilters, context ->
            val list = applyBudgetFilters(budgets, query, activeFilters)

            if (query.isNotEmpty() && list.isEmpty()) {
                if (context == HUDContext.TENDER_DETAILS && _selectedTenderId.value == tenderId) {
                    coordinator.setHasMatches(false)
                }
                budgets.sortedWith(budgetComparator())
            } else {
                if (context == HUDContext.TENDER_DETAILS && _selectedTenderId.value == tenderId) {
                    coordinator.setHasMatches(true)
                }
                list
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    private fun applyBudgetFilters(budgets: List<BudgetEntity>, query: String, activeFilters: Set<String>): List<BudgetEntity> {
        var list = if (query.isNotEmpty()) {
            val normalized = query.prepareForSearch()
            budgets.filter { 
                it.providerName.wordStartsWithSmart(normalized) || 
                (it.providerCompanyName?.wordStartsWithSmart(normalized) ?: false) || 
                it.grandTotal.toString().startsWith(normalized) || 
                it.budgetId.wordStartsWithSmart(normalized)
            }
        } else {
            budgets
        }

        val catFilters = activeFilters.filter { it.startsWith("cat_") }.map { it.removePrefix("cat_") }
        if (catFilters.isNotEmpty()) {
            list = list.filter { budget -> 
                catFilters.any { it.equals(budget.category, ignoreCase = true) }
            }
        }

        val statusFilters = activeFilters.filter { it.startsWith("filter_budget_") }
        if (statusFilters.isNotEmpty()) {
            list = list.filter { budget ->
                statusFilters.any { filterId ->
                    when (filterId) {
                        "filter_budget_pending" -> budget.status == BudgetStatus.PENDIENTE
                        "filter_budget_accepted" -> budget.status == BudgetStatus.ACEPTADO
                        "filter_budget_rejected" -> budget.status == BudgetStatus.RECHAZADO
                        else -> false
                    }
                }
            }
        }

        list = list.sortedWith(budgetComparator())

        if (activeFilters.contains("sort_alpha")) list = list.sortedBy { it.providerName }
        if (activeFilters.contains("sort_date")) list = list.sortedByDescending { it.dateTimestamp }
        if (activeFilters.contains("sort_price")) list = list.sortedBy { it.grandTotal }
        return list
    }

    private fun budgetComparator() = compareByDescending<BudgetEntity> { !it.isRead && it.status == BudgetStatus.PENDIENTE }
        .thenByDescending { it.status == BudgetStatus.PENDIENTE }
        .thenByDescending { it.status == BudgetStatus.ACEPTADO }
        .thenByDescending { it.status == BudgetStatus.RECHAZADO || it.status == BudgetStatus.VENCIDO }
        .thenByDescending { it.dateTimestamp }

    // ==========================================================
    // 3. FLUJO DE ACCIONES DINÁMICAS (IDs para el Cerebro)
    // ==========================================================
    /**
     * 🔥 [ELITE SSOT] El ViewModel solo emite IDs de comando.
     * BeBrainViewModel se encarga de mapearlos a Iconos y Colores.
     */
    val beActionIds: StateFlow<List<String>> = combine(
        _isMultiSelectionActive,
        _selectedIds,
        coordinator.currentHUDContext
    ) { isMulti, selected, context ->
        val ids = mutableListOf<String>()
        val count = selected.size

        if (isMulti) {
            if (context == HUDContext.BUDGETS_TENDERS) {
                ids.add("cancel")
                ids.add("divider_v_1")
                if (count == 1) {
                    ids.add("view_tender_details")
                    ids.add("divider_v_2")
                }
                ids.add("delete_multi")
            } else {
                ids.add("cancel")
                ids.add("divider_v_1")
                if (count > 1) {
                    ids.add("compare_selected")
                    ids.add("divider_v_2")
                }
                ids.add("select_all")
                ids.add("mark_as_read")
                ids.add("divider_v_3")
                ids.add("delete_multi")
            }
        } else {
            if (context == HUDContext.TENDER_DETAILS) {
                ids.add("compare_all")
                ids.add("divider_v_1")
                ids.add("select_all")
                ids.add("mark_as_read")
                ids.add("divider_v_3")
                ids.add("delete_multi")
            }
        }
        ids
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==========================================================
    // 4. MÉTODOS DE DELEGACIÓN (Acciones)
    // ==========================================================
    fun setSearchQuery(query: String) { coordinator.updateSearchQuery(query) }
    fun setFilters(filters: Set<String>) { coordinator.updateFilters(filters) }

    fun updateMultiSelection(active: Boolean) {
        _isMultiSelectionActive.value = active
        if (!active) _selectedIds.value = emptySet()
        // Notificamos al coordinador global para que el Cerebro reaccione (HUD/BottomBar)
        coordinator.updateSheetVisibility(active) 
    }

    fun toggleSelection(id: String) {
        val current = _selectedIds.value.toMutableSet()
        if (!current.add(id)) current.remove(id)
        _selectedIds.value = current
    }

    fun selectAll(ids: List<String>) {
        _selectedIds.value = ids.toSet()
    }

    // --- Lógica de Negocio ---
    fun createTender(
        title: String,
        description: String,
        category: String,
        startDate: Long,
        endDate: Long,
        requiresVisit: Boolean,
        requiresPaymentMethod: Boolean,
        requiresWorkGuarantee: Boolean,
        requiresProviderDoc: Boolean,
        location: AddressUnico?,
        imageUrls: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: "user_demo_66"
            val currentUser = userRepository.userProfile.firstOrNull()
            
            val addr = location?.streetAndNumber
            val num = "" // El número ya está en streetAndNumber
            val loc = location?.localidad
            val type = if (location?.id == "gps_current") "GPS" else if (location?.isCompany == true) "BUSINESS" else "PERSONAL"

            val compName = if (location?.isCompany == true) location.ownerName else null
            val branchName = if (location?.isCompany == true) location.label else null
            
            val finalPostalCode = location?.codigoPostal

            val clientDisplayName = if (location?.isCompany == true) {
                location.ownerName
            } else {
                "${currentUser?.name ?: ""} ${currentUser?.lastName ?: ""}".trim().ifEmpty { currentUser?.displayName }
            }

            val uploadedUrls = mutableListOf<String>()
            val tenderId = UUID.randomUUID().toString().take(8).uppercase()

            imageUrls.forEachIndexed { index, uriString ->
                val uri = android.net.Uri.parse(uriString)
                val compressedBytes = ImageUtils.compressImageToWebP(context, uri)
                compressedBytes?.let { bytes ->
                    val url = repository.uploadTenderImage(tenderId, index, bytes)
                    url?.let { uploadedUrls.add(it) }
                }
            }

            val cleanCp = finalPostalCode?.takeIf { it.isNotBlank() }?.normalizeForTopic() ?: "t4000"
            val cleanCat = category.normalizeForTopic()

            val matchKey = "tender_${cleanCp}_$cleanCat"
            Log.d("TENDER_FLOW", "🚀 Publicando Licitación: $tenderId")

            val expiresAt = if (endDate > 0) endDate + TimeUnit.DAYS.toMillis(1) else null

            val newTender = TenderEntity(
                tenderId = tenderId,
                clientId = currentUserId,
                title = title,
                description = description,
                category = category,
                startDate = startDate,
                endDate = endDate,
                requiresVisit = requiresVisit,
                requiresPaymentMethod = requiresPaymentMethod,
                requiresWorkGuarantee = requiresWorkGuarantee,
                requiresProviderDoc = requiresProviderDoc,
                locationAddress = addr,
                locationNumber = num,
                locationLocality = loc,
                locationPostalCode = cleanCp,
                locationType = type,
                clientDisplayName = clientDisplayName,
                companyName = compName,
                branchName = branchName,
                imageUrls = uploadedUrls,
                matchKey = matchKey,
                expiresAt = expiresAt,
                isActive = true
            )

            repository.createNewTender(newTender)

            // 🔥 [ELITE] Suscripción on-demand al topic de la zona para esta licitación
            coordinator.syncZoneTopic(cleanCp)

            repository.sendTopicNotification(
                topic = matchKey,
                title = "🚀 Nueva Licitación: $category",
                body = "Se busca: $title en tu zona ($cleanCp). ¡Postúlate ahora!",
                tenderId = tenderId
            )
        }
    }

    fun updateTenderStatus(tenderId: String, newStatus: String) {
        viewModelScope.launch {
            val tender = allTenders.value.find { it.tenderId == tenderId }
            tender?.let {
                val updated = it.copy(
                    status = newStatus,
                    cancellationDate = if (newStatus == "CANCELADA") System.currentTimeMillis() else it.cancellationDate,
                    isActive = when(newStatus) {
                        "ABIERTA" -> it.budgetCount < 100
                        else -> false
                    }
                )

                repository.createNewTender(updated)

                if (newStatus == "ADJUDICADA" || newStatus == "CANCELADA" || newStatus == "CERRADA") {
                    repository.removeFromCloud(tenderId)
                }
            }
        }
    }

    fun markAsRead(ids: Set<String>) {
        viewModelScope.launch {
            ids.forEach { repository.markBudgetAsRead(it) }
        }
    }

    fun deleteBudgets(ids: Set<String>) {
        viewModelScope.launch {
            ids.forEach { repository.removeBudget(it) }
            updateMultiSelection(false)
        }
    }

    fun deleteTenders(ids: Set<String>) {
        viewModelScope.launch {
            ids.forEach { repository.removeTender(it) }
            updateMultiSelection(false)
        }
    }

    fun acceptBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.updateBudgetStatus(budget.budgetId, BudgetStatus.ACEPTADO)

            budget.tenderId?.let { tId ->
                val tender = allTenders.value.find { it.tenderId == tId }
                tender?.let {
                    val updatedTender = it.copy(
                        status = "ADJUDICADA",
                        isActive = false,
                        awardedProviderId = budget.providerId,
                        awardedProviderName = budget.providerCompanyName ?: budget.providerName,
                        awardedBudgetId = budget.budgetId,
                        awardedProviderPhotoUrl = budget.providerPhotoUrl
                    )
                    repository.createNewTender(updatedTender)
                }
            }

            sendDecisionMessage(budget, "✅ ¡Hola! He ACEPTADO el presupuesto #${budget.budgetId.takeLast(4)}...")
        }
    }

    fun rejectBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.updateBudgetStatus(budget.budgetId, BudgetStatus.RECHAZADO)
            sendDecisionMessage(budget, "❌ Hola. He decidido RECHAZAR el presupuesto #${budget.budgetId.takeLast(4)}...")
        }
    }

    private suspend fun sendDecisionMessage(budget: BudgetEntity, text: String) {
        val chatId = "chat_${budget.clientId}_${budget.providerId}"
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = budget.clientId,
            receiverId = budget.providerId,
            type = MessageType.TEXT,
            content = text,
            timestamp = System.currentTimeMillis(),
            status = "SENT"
        )
        chatRepository.sendMessage(message)
    }

    suspend fun getProviderById(providerId: String) = providerRepository.getProviderById(providerId)

    // ==========================================================
    // 5. ANALÍTICA (Fase 5)
    // ==========================================================
    private val _analyticsState = MutableStateFlow(MarketAnalyticsState())
    val analyticsState: StateFlow<MarketAnalyticsState> = _analyticsState.asStateFlow()

    fun analyzeBudgets(budgets: List<BudgetEntity>) {
        viewModelScope.launch(Dispatchers.Default) {
            _analyticsState.value = _analyticsState.value.copy(isAnalyzing = true)
            if (budgets.isEmpty()) {
                _analyticsState.value = MarketAnalyticsState(isAnalyzing = false)
                return@launch
            }

            val avg = budgets.map { it.grandTotal }.average()
            val min = budgets.minOf { it.grandTotal }
            val max = budgets.maxOf { it.grandTotal }

            val items = budgets.map { b ->
                val total = b.grandTotal
                val isIrrisory = total > avg * 1.8 
                val isOptimal = total <= avg && total >= min * 0.9

                ChartBudgetItem(
                    budget = b,
                    total = total,
                    mat = b.items.sumOf { it.unitPrice * it.quantity },
                    lab = b.services.sumOf { it.total } + b.professionalFees.sumOf { it.total },
                    tax = b.taxAmount,
                    isIrrisory = isIrrisory,
                    isOptimal = isOptimal
                )
            }

            _analyticsState.value = MarketAnalyticsState(
                items = items,
                avgTotal = avg,
                minPrice = min,
                maxPrice = max,
                validCount = items.count { !it.isIrrisory },
                isAnalyzing = false
            )
        }
    }
}

