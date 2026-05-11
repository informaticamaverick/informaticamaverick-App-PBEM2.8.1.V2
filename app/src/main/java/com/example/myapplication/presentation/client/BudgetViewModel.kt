package com.example.myapplication.presentation.client

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.*
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.data.repository.BudgetRepository
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.presentation.components.BeSmallActionModel
import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.myapplication.util.ImageUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.myapplication.data.utils.SearchUtils.matchesSmart
import com.example.myapplication.data.utils.SearchUtils.prepareForSearch
import com.example.myapplication.data.utils.SearchUtils.wordStartsWithSmart
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Locale
import javax.inject.Inject

data class AnalyticsState(
    val items: List<ChartBudgetItem> = emptyList(),
    val avgTotal: Double = 0.0,
    val minPrice: Double = 0.0,
    val maxPrice: Double = 0.0,
    val validCount: Int = 0,
    val isAnalyzing: Boolean = true
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: BudgetRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository, // 🔥 Inyectamos UserRepository para datos del cliente
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context // 🔥 Inyección de Context para ImageUtils
) : ViewModel() {

    // ==========================================================
    // 1. ESTADOS DE BUSQUEDA Y SELECCION (Delegados)
    // ==========================================================
    private val _searchQueryFromBe = MutableStateFlow("") 
    val searchQuery = _searchQueryFromBe.asStateFlow()

    private val _activeFiltersFromBe = MutableStateFlow<Set<String>>(emptySet()) 

    private val _isMultiSelectionActive = MutableStateFlow(false)
    val isMultiSelectionActive = _isMultiSelectionActive.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds = _selectedIds.asStateFlow()

    private val _currentHUDContext = MutableStateFlow(HUDContext.BUDGETS)
    val currentHUDContext = _currentHUDContext.asStateFlow()

    // --- ESTADO DE COINCIDENCIAS PARA EL ASISTENTE (Be) ---
    private val _hasMatches = MutableStateFlow(true)
    val hasMatches = _hasMatches.asStateFlow()

    private val _selectedTenderId = MutableStateFlow<String?>(null)
    fun setSelectedTenderId(id: String?) { 
        _selectedTenderId.value = id 
    }

    fun setContext(context: HUDContext) {
        // Si el contexto cambia (por ejemplo de TENDERS a DIRECT), reseteamos la UI
        if (_currentHUDContext.value != context) {
            resetPageState()
        }
        _currentHUDContext.value = context
    }

    // ==========================================================
    // 2. DATOS FILTRADOS Y ORDENADOS
    // ==========================================================
    val allBudgets: StateFlow<List<BudgetEntity>> = repository.allBudgets 
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTenders: StateFlow<List<TenderEntity>> = repository.allTenders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkExpiredTenders()
    }

    /**
     * Revisa periódicamente o al iniciar si hay licitaciones cuya fecha de cierre ya pasó
     * y las marca como CERRADAS en la base de datos.
     */
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

    /**
     * Resetea filtros, búsqueda y multiselección de esta pantalla específica
     */
    fun resetPageState() {
        _isMultiSelectionActive.value = false
        _selectedIds.value = emptySet()
        _searchQueryFromBe.value = ""
        // Si quieres resetear también los filtros tácticos:
        _activeFiltersFromBe.value = emptySet()
    }

    val filteredTenders: StateFlow<List<TenderEntity>> = combine(
        allTenders,
        _searchQueryFromBe,
        _activeFiltersFromBe,
        allBudgets,
        _currentHUDContext
    ) { tenders, query, activeFilters, allBudgetsList, context ->
        var list = if (query.isNotEmpty()) {
            val normalized = query.prepareForSearch()
            tenders.filter { it.title.wordStartsWithSmart(normalized) }
        } else {
            tenders
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

        list = list.sortedWith(compareBy<TenderEntity> { tender ->
            when (tender.status) {
                "ABIERTA" -> 1
                "ADJUDICADA" -> 2
                "CERRADA" -> 3
                "CANCELADA" -> 4
                else -> 5
            }
        }.thenByDescending { tender ->
            if (tender.status == "ABIERTA") {
                allBudgetsList.any { it.tenderId == tender.tenderId && !it.isRead }
            } else false
        }.thenByDescending { it.dateTimestamp })

        if (activeFilters.contains("sort_alpha")) list = list.sortedBy { it.title }
        if (activeFilters.contains("sort_date")) list = list.sortedByDescending { it.dateTimestamp }
        
        // ==========================================================
        // --- SECCIÓN: LÓGICA DE FALLBACK Y NOTIFICACIÓN A BE ---
        // ==========================================================
        if (query.isNotEmpty() && list.isEmpty()) {
            // Si no hay coincidencias, avisamos al asistente y mostramos fallback
            if (context == HUDContext.BUDGETS_TENDERS) _hasMatches.value = false
            tenders.sortedByDescending { it.dateTimestamp }
        } else {
            // Si hay coincidencias o no hay búsqueda, reseteamos el estado
            if (context == HUDContext.BUDGETS_TENDERS) _hasMatches.value = true
            list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredDirectBudgets: StateFlow<List<BudgetEntity>> = combine(
        repository.directBudgets,
        _searchQueryFromBe,
        _activeFiltersFromBe,
        _currentHUDContext
    ) { budgets, query, activeFilters, context ->
        val list = applyBudgetFilters(budgets, query, activeFilters)

        // ==========================================================
        // --- SECCIÓN: LÓGICA DE FALLBACK Y NOTIFICACIÓN A BE ---
        // ==========================================================
        if (query.isNotEmpty() && list.isEmpty()) {
            if (context == HUDContext.BUDGETS_DIRECT || context == HUDContext.BUDGETS) _hasMatches.value = false
            budgets.sortedWith(budgetComparator()) // Fallback: Lista completa
        } else {
            if (context == HUDContext.BUDGETS_DIRECT || context == HUDContext.BUDGETS) _hasMatches.value = true
            list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredOverlayBudgets: StateFlow<List<BudgetEntity>> = _selectedTenderId
        .flatMapLatest { id ->
            if (id == null) {
                _hasMatches.value = true
                flowOf(emptyList())
            } else {
                repository.getBudgetsForTender(id).combine(
                    combine(_searchQueryFromBe, _activeFiltersFromBe, _currentHUDContext) { q, f, c -> Triple(q, f, c) }
                ) { budgets, params ->
                    val (query, filters, context) = params
                    val list = applyBudgetFilters(budgets, query, filters)
                    
                    // ==========================================================
                    // --- SECCIÓN: LÓGICA DE FALLBACK Y NOTIFICACIÓN A BE ---
                    // ==========================================================
                    if (query.isNotEmpty() && list.isEmpty()) {
                        if (context == HUDContext.TENDER_DETAILS) _hasMatches.value = false
                        budgets.sortedWith(budgetComparator()) // Fallback: Lista completa
                    } else {
                        if (context == HUDContext.TENDER_DETAILS) _hasMatches.value = true
                        list
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getFilteredBudgetsForTender(tenderId: String): StateFlow<List<BudgetEntity>> {
        return combine(
            repository.getBudgetsForTender(tenderId), 
            _searchQueryFromBe, 
            _activeFiltersFromBe,
            _currentHUDContext
        ) { budgets, query, activeFilters, context ->
            val list = applyBudgetFilters(budgets, query, activeFilters)

            // Implementamos fallback también para las vistas individuales si hay búsqueda
            if (query.isNotEmpty() && list.isEmpty()) {
                // Solo actualizamos hasMatches si es la licitación que se está viendo en detalle
                if (context == HUDContext.TENDER_DETAILS && _selectedTenderId.value == tenderId) {
                    _hasMatches.value = false
                }
                budgets.sortedWith(budgetComparator())
            } else {
                if (context == HUDContext.TENDER_DETAILS && _selectedTenderId.value == tenderId) {
                    _hasMatches.value = true
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
    // 3. FLUJO DE ACCIONES DINÁMICAS
    // ==========================================================
    val beActions: StateFlow<List<BeSmallActionModel>> = combine(
        _isMultiSelectionActive,
        _selectedIds,
        _currentHUDContext
    ) { isMulti, selected, context ->
        val actions = mutableListOf<BeSmallActionModel>()
        val count = selected.size

        if (isMulti) {
            if (context == HUDContext.BUDGETS_TENDERS) {
                // Requerimiento Licitaciones: cerrar, divider vertical, detalles (solo si count == 1), eliminar
                actions.add(BeSmallActionModel("cancel", Icons.Default.Close, "Cerrar") { })
                actions.add(BeSmallActionModel("divider_v_1", Icons.Default.VerticalAlignBottom, "Divider") { })

                // Icono de detalles solo si hay una seleccionada
                if (count == 1) {
                    actions.add(BeSmallActionModel("view_tender_details", Icons.AutoMirrored.Filled.Assignment, "Detalles", emoji = "📋") { })
                    actions.add(BeSmallActionModel("divider_v_2", Icons.Default.VerticalAlignBottom, "Divider") { })
                }

                actions.add(BeSmallActionModel("delete_multi", Icons.Default.Delete, "Eliminar", tint = Color.Red) { })
            } else {
                // Requerimiento Presupuestos Directos: cerrar, divider vertical, comparar(solo si > 1), divider vertical, Todos, leidos, divider vertical, eliminar
                actions.add(BeSmallActionModel("cancel", Icons.Default.Close, "Cerrar") { })
                actions.add(BeSmallActionModel("divider_v_1", Icons.Default.VerticalAlignBottom, "Divider") { }) 
                
                if (count > 1) {
                    actions.add(BeSmallActionModel("compare_selected", Icons.AutoMirrored.Filled.CompareArrows, "Comparar", emoji = "⚖️") { })
                    actions.add(BeSmallActionModel("divider_v_2", Icons.Default.VerticalAlignBottom, "Divider") { })
                }

                // Unificamos IDs para que PresupuestosScreen los capture globalmente
                actions.add(BeSmallActionModel("select_all", Icons.Default.SelectAll, "Todos", emoji = "✅") { })
                actions.add(BeSmallActionModel("mark_as_read", Icons.Default.DoneAll, "Leídos", emoji = "📖") { })
                actions.add(BeSmallActionModel("divider_v_3", Icons.Default.VerticalAlignBottom, "Divider") { })
                actions.add(BeSmallActionModel("delete_multi", Icons.Default.Delete, "Eliminar", tint = Color.Red) { })
            }
        } else {

            when (context) {
                HUDContext.TENDER_DETAILS -> {
                    actions.add(
                        BeSmallActionModel(
                            id = "compare_all",
                            icon = Icons.AutoMirrored.Filled.CompareArrows,
                            label = "Comparar Todo",
                            emoji = "⚖️",
                            isDefault = true
                        ) {
                        }
                    )
                    actions.add(BeSmallActionModel("divider_v_1", Icons.Default.VerticalAlignBottom, "Divider") { })
                    actions.add(BeSmallActionModel("select_all", Icons.Default.SelectAll, "Todos", emoji = "✅") { })
                    actions.add(BeSmallActionModel("mark_as_read", Icons.Default.DoneAll, "Leídos", emoji = "📖") { })
                    actions.add(BeSmallActionModel("divider_v_3", Icons.Default.VerticalAlignBottom, "Divider") { })
                    actions.add(BeSmallActionModel("delete_multi", Icons.Default.Delete, "Eliminar", tint = Color.Red) { })
                }
                else -> {}
            }

        }
        actions
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==========================================================
    // 4. MÉTODOS DE DELEGACIÓN (Acciones)
    // ==========================================================
    fun setSearchQuery(query: String) { _searchQueryFromBe.value = query }
    fun setFilters(filters: Set<String>) { _activeFiltersFromBe.value = filters }
    
    fun updateMultiSelection(active: Boolean) {
        _isMultiSelectionActive.value = active
        if (!active) _selectedIds.value = emptySet()
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
    /**
     * ── SECCIÓN: CREACIÓN DE LICITACIÓN (Firebase Sync) ──────────────────────────────────
     * Este método procesa imágenes, datos del usuario/empresa y sincroniza con Firestore.
     */
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
        location: LocationOption?,
        imageUrls: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: "user_demo_66"

            // 1. PROCESAMIENTO DE UBICACIÓN Y DATOS DEL EMISOR
            val currentUser = userRepository.userProfile.firstOrNull()
            
            val addr = when (location) {
                is LocationOption.Personal -> location.address
                is LocationOption.Business -> location.address
                is LocationOption.Gps -> location.address
                else -> null
            }
            val num = when (location) {
                is LocationOption.Personal -> location.number
                is LocationOption.Business -> location.number
                is LocationOption.Gps -> location.number
                else -> null
            }
            val loc = when (location) {
                is LocationOption.Personal -> location.locality
                is LocationOption.Business -> location.locality
                is LocationOption.Gps -> location.locality
                else -> null
            }
            val cp = when (location) {
                is LocationOption.Personal -> location.postalCode
                is LocationOption.Business -> location.postalCode
                is LocationOption.Gps -> location.postalCode
                else -> null
            }
            val type = when (location) {
                is LocationOption.Personal -> "PERSONAL"
                is LocationOption.Business -> "BUSINESS"
                is LocationOption.Gps -> "GPS"
                else -> null
            }

            // Datos de la Empresa / Cliente
            val compName = if (location is LocationOption.Business) location.companyName else null
            val branchName = if (location is LocationOption.Business) location.branchName else null
            
            // 🔥 CORRECCIÓN: Aseguramos que si LocationOption es Personal/Business,
            // los datos de PostalCode se extraigan correctamente de la entidad.
            val finalPostalCode = when(location) {
                is LocationOption.Personal -> location.postalCode
                is LocationOption.Business -> location.postalCode
                else -> cp 
            }

            val clientDisplayName = if (location is LocationOption.Business) {
                location.companyName
            } else {
                "${currentUser?.name ?: ""} ${currentUser?.lastName ?: ""}".trim().ifEmpty { currentUser?.displayName }
            }

            // 2. PROCESAMIENTO DE IMÁGENES (Compresión WebP y Storage)
            val uploadedUrls = mutableListOf<String>()
            val tenderId = UUID.randomUUID().toString().take(8).uppercase()

            imageUrls.forEachIndexed { index, uriString ->
                val uri = Uri.parse(uriString)
                // Comprimimos usando ImageUtils
                val compressedBytes = ImageUtils.compressImageToWebP(context, uri)
                compressedBytes?.let {
                    val url = repository.uploadTenderImage(tenderId, index, it)
                    url?.let { uploadedUrls.add(it) }
                }
            }

            // 3. GENERACIÓN DE MATCH KEY Y EXPIRACIÓN (Costo Cero)
            // Topic: tender_{cp}_{categoria}
            // 🔥 CORRECCIÓN: Si postal code está vacío, usar un placeholder o omitir para evitar topics rotos.
            val cleanCp = finalPostalCode?.takeIf { it.isNotBlank() }?.normalizeForTopic() ?: "t4000"
            val cleanCat = category.normalizeForTopic()

            // 🔥 [VALIDACIÓN DE FLUJO] Aseguramos formato estricto y agregamos LOG para depuración
            val matchKey = "tender_${cleanCp}_$cleanCat"
            Log.d("FCM_FLOW", "MatchKey Generado (Cliente): $matchKey")

            // Expiración: fecha de fin + 1 día de gracia
            val expiresAt = if (endDate > 0) endDate + TimeUnit.DAYS.toMillis(1) else null

            // 4. CREACIÓN DE LA ENTIDAD COMPLETA
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
                locationPostalCode = cleanCp, // Guardamos la versión limpia
                locationType = type,
                clientDisplayName = clientDisplayName,
                companyName = compName,
                branchName = branchName,
                imageUrls = uploadedUrls,
                matchKey = matchKey,
                expiresAt = expiresAt,
                isActive = true
            )

            // 5. PERSISTENCIA Y SINCRONIZACIÓN
            repository.createNewTender(newTender)

            // 6. ENVÍO DE NOTIFICACIÓN AL TOPIC (Costo Cero)
            repository.sendTopicNotification(
                topic = matchKey,
                title = "🚀 Nueva Licitación: $category",
                body = "Se busca: $title en tu zona ($cleanCp). ¡Postúlate ahora!",
                tenderId = tenderId
            )
        }
    }

    /**
     * ── SECCIÓN: GESTIÓN DE ESTADOS (Limpieza de Nube) ──────────────────────────────────
     * Actualiza el estado local y elimina de Firestore si la licitación no está más activa.
     */
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
                
                // Actualizamos localmente (Room)
                repository.createNewTender(updated)

                // Limpieza de Firestore (Costo Cero)
                if (newStatus == "ADJUDICADA" || newStatus == "CANCELADA" || newStatus == "CERRADA") {
                    repository.removeFromCloud(tenderId)
                }
            }
        }
    }

    fun cancelTender(tender: TenderEntity) {
        viewModelScope.launch {
            val updated = tender.copy(
                status = "CANCELADA",
                cancellationDate = System.currentTimeMillis(),
                isActive = false
            )
            repository.createNewTender(updated)
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
            // 1. Aceptar el presupuesto
            repository.updateBudgetStatus(budget.budgetId, BudgetStatus.ACEPTADO)
            
            // 2. Si el presupuesto pertenece a una licitación, adjudicarla
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

            // 3. Enviar mensaje de notificación
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

    // ==========================================================
    // 5. ANALÍTICA (Fase 5)
    // ==========================================================
    private val _analyticsState = MutableStateFlow(AnalyticsState())
    val analyticsState: StateFlow<AnalyticsState> = _analyticsState.asStateFlow()

    fun analyzeBudgets(budgets: List<BudgetEntity>) {
        viewModelScope.launch(Dispatchers.Default) {
            _analyticsState.value = _analyticsState.value.copy(isAnalyzing = true)
            if (budgets.isEmpty()) {
                _analyticsState.value = AnalyticsState(isAnalyzing = false)
                return@launch
            }
            val mapped = budgets.map { budget ->
                val mat = budget.items.sumOf { it.unitPrice * it.quantity }
                val lab = budget.services.sumOf { it.total }
                val tax = budget.taxAmount
                val total = mat + lab + tax
                val isIrr = total !in 15000.0..200000.0
                ChartBudgetItem(budget, total, mat, lab, tax, isIrr, false)
            }
            val validItems = mapped.filter { !it.isIrrisory }
            val avg = if (validItems.isNotEmpty()) validItems.map { it.total }.average() else 0.0
            val optMin = avg * 0.85
            val optMax = avg * 1.15
            val finalItems = mapped.map {
                it.copy(isOptimal = !it.isIrrisory && it.total in optMin..optMax)
            }.sortedBy { it.total }
            _analyticsState.value = AnalyticsState(
                items = finalItems, avgTotal = avg,
                minPrice = validItems.minOfOrNull { it.total } ?: 0.0,
                maxPrice = validItems.maxOfOrNull { it.total } ?: 0.0,
                validCount = validItems.size, isAnalyzing = false
            )
        }
    }
}
// Extensiones para la búsqueda inteligente, compartidas con otros ViewModels.
private fun String.removeAccents(): String {
    val normalized = java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
    return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(normalized, "")
}

/**
 * Normaliza una cadena para ser usada como nombre de Tópico en Firebase.
 * Elimina acentos, paréntesis, espacios y caracteres especiales.
 */
fun String.normalizeForTopic(): String {
    return this.removeAccents()
        .replace(" ", "_")
        .replace("(", "")
        .replace(")", "")
        .replace(Regex("[^a-zA-Z0-9-_.~%]"), "") // Solo caracteres permitidos por FCM
        .lowercase()
}

fun CategoryEntity.matches(normalizedQuery: String): Boolean = this.name.wordStartsWithSmart(normalizedQuery)
