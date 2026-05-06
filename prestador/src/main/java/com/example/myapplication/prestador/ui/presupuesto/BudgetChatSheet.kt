package com.example.myapplication.prestador.ui.presupuesto

import android.R
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
//import com.example.myapplication.prestador.data.PPrestadorProfileFalso
import com.example.myapplication.prestador.data.local.entity.ClienteEntity
import com.example.myapplication.prestador.data.local.entity.PresupuestoEntity
import com.example.myapplication.prestador.data.model.Message
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.PresupuestoViewModel
import com.example.myapplication.prestador.viewmodel.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.ProfileState
import com.example.myapplication.prestador.utils.displayAddress
import com.example.myapplication.prestador.utils.displayCompanyOrFullName
import com.example.myapplication.prestador.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import com.example.myapplication.prestador.ui.presupuesto.sheets.*
import com.example.myapplication.prestador.ui.presupuesto.components.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetChatSheet(
    userId: String,
    userName: String,
    providerId: String, // ID del prestador real
    initialClientAddress: String? = null,
    onDismiss: () -> Unit,
    viewModel: PresupuestoViewModel = hiltViewModel(),
    editProfileViewModel: EditProfileViewModel = hiltViewModel(),
    chatViewModel: com.example.myapplication.prestador.viewmodel.ChatViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val profileState by editProfileViewModel.profileState.collectAsState()
    val businessEntity by editProfileViewModel.businessEntity.collectAsState()

    // --- SECCIÓN: OBTENCIÓN DE DATOS REALES DEL PERFIL ---
    val isProfessional = (profileState as? ProfileState.Success) ?.provider?.serviceType.equals("PROFESSIONAL", ignoreCase = true) == true
    val provider = ( profileState as? ProfileState.Success)?.provider
    
    // Usar utilidades de visualización para datos reales
    val providerDisplayName = provider?.displayCompanyOrFullName(businessEntity) ?: "Prestador"
    val providerDisplayAddress = provider?.displayAddress(businessEntity) ?: ""

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // --- ITEMS STATE ---
    val items = remember { mutableStateListOf<BudgetItem>() }
    val services = remember { mutableStateListOf<BudgetService>() }
    val professionalFees = remember { mutableStateListOf<BudgetProfessionalFee>() }
    val miscExpenses = remember { mutableStateListOf<BudgetMiscExpense>() }
    val taxes = remember { mutableStateListOf<BudgetTax>() }

    // --- DATOS DEL CLIENTE ---
    var clienteData by remember { mutableStateOf<ClienteEntity?>(null) }
    var overrideClientAddress by remember { mutableStateOf(initialClientAddress) }
    LaunchedEffect(initialClientAddress) {
        if (!initialClientAddress.isNullOrBlank()) overrideClientAddress = initialClientAddress
    }
    LaunchedEffect(userId) {
        if (userId.isNotBlank())  {
            clienteData = viewModel.getClienteById(userId)
        }
    }

    //---SECTION EPANSION (accordion: solo una abierta ala vez, todas cerradas por defecto) ---
    var expandedSection by remember { mutableStateOf<String?>(null) }

    // --- DIALOG STATES ---
    var sheetType by remember { mutableStateOf<SheetType?>(null) }
    var itemToEdit by remember { mutableStateOf<Any?>(null) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var pendingPresupuesto by remember { mutableStateOf<PresupuestoEntity?>(null) }
    var showIIBBDialog by remember { mutableStateOf(false) }
    var showTaxDetail by remember { mutableStateOf(false) }
    val presupuestos by viewModel.presupuestos.collectAsState()
    val articleCatalog by viewModel.articleCatalog.collectAsState()
    val serviceCatalog by viewModel.serviceCatalog.collectAsState()
    val feeCatalog by viewModel.feeCatalog.collectAsState()

    // --- NOTES / VALIDITY ---
    var tituloTrabajo by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var validity by remember { mutableStateOf("7") }

    // --- CATEGORIA STATE ---
    val providerCategories = remember(provider) { provider?.categories ?: emptyList() }
    var selectedBudgetCategory by remember(providerCategories) {
        mutableStateOf(providerCategories.firstOrNull() ?: "")
    }
    var budgetCategoryExpanded by remember { mutableStateOf(false) }

    // --- CALCULATED TOTALS ---
    val itemsBaseSubtotal = items.sumOf { it.unitPrice * it.quantity }
    val itemsTaxTotal = items.sumOf {
        val base = it.unitPrice * it.quantity
        base * (it.taxPercentage / 100)
    }
    val itemsDiscountTotal = items.sumOf {
        val base = it.unitPrice * it.quantity
        val taxAmt = base * (it.taxPercentage / 100)
        (base + taxAmt) * (it.discountPercentage / 100)
    }
    val itemsSubtotal = itemsBaseSubtotal + itemsTaxTotal - itemsDiscountTotal
    val servicesSubtotal = services.sumOf { it.total }
    val feesSubtotal = professionalFees.sumOf { it.total }
    val miscSubtotal = miscExpenses.sumOf { it.amount }
    val taxesSubtotal = taxes.sumOf { it.amount }
    val subtotal = itemsSubtotal + servicesSubtotal + feesSubtotal + miscSubtotal
    val grandTotal = subtotal + taxesSubtotal

    val lazyListState = rememberLazyListState()

    val hasItems = items.isNotEmpty() || services.isNotEmpty() ||
            professionalFees.isNotEmpty() || miscExpenses.isNotEmpty()

    // Suggestion items from catalog
    val suggestionItems = remember(articleCatalog) {
        val json = articleCatalog?.itemsJson ?: ""
        if (json.isBlank()) emptyList()
        else json.split("|").mapNotNull { s ->
            val parts = s.split(";")
            if (parts.size >= 4) BudgetItem(
                id = 0L,
                code = parts[0],
                description = parts[1],
                quantity = parts[2].toIntOrNull() ?: 1,
                unitPrice = parts[3].toDoubleOrNull() ?: 0.0,
                taxPercentage = parts.getOrNull(4)?.toDoubleOrNull() ?: 0.0,
                discountPercentage = parts.getOrNull(5)?.toDoubleOrNull() ?: 0.0
            ) else null
        }.distinctBy { it.description }
    }

    val suggServices = remember(serviceCatalog) {
        val json = serviceCatalog?.serviciosJson ?: ""
        if (json.isBlank()) emptyList()
        else json.split("|").mapNotNull { s ->
            val parts = s.split(";")
            if (parts.size >= 2) BudgetService(
                id = 0L, code = parts[0],
                description = parts[1],
                total = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0
            ) else null
        }.distinctBy { it.description }
    }

    val suggFees = remember(feeCatalog) {
        val json = feeCatalog?.honorariosJson ?: ""
        if (json.isBlank()) emptyList()
        else json.split("|").mapNotNull { s ->
            val parts = s.split(";")
            if (parts.size >= 2) BudgetProfessionalFee(
                id = 0L, code = parts[0],
                description = parts[1],
                total = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0
            ) else null
        }.distinctBy { it.description }
    }

    // Descripciones ya guardadas en el catálogo (para detectar items nuevos)
    val knownItemDescriptions = remember(articleCatalog) {
        val json = articleCatalog?.itemsJson ?: ""
        if (json.isBlank()) emptySet()
        else json.split("|").mapNotNull { s -> s.split(";").getOrNull(1) }.toSet()
    }
    val knownServiceDescriptions = remember(serviceCatalog) {
        val json = serviceCatalog?.serviciosJson ?: ""
        if (json.isBlank()) emptySet()
        else json.split("|").mapNotNull { s -> s.split(";").getOrNull(1) }.toSet()
    }
    val knownFeeDescriptions = remember(feeCatalog) {
        val json = feeCatalog?.honorariosJson ?: ""
        if (json.isBlank()) emptySet()
        else json.split("|").mapNotNull { s -> s.split(";").getOrNull(1) }.toSet()
    }
    val knownMiscDescriptions = remember(presupuestos) {
        presupuestos.flatMap { p ->
            if (p.gastosJson.isBlank()) emptyList()
            else p.gastosJson.split("|").mapNotNull { s -> s.split(";").getOrNull(0) }
        }.toSet()
    }
    val knownTaxDescriptions = remember(presupuestos) {
        presupuestos.flatMap { p ->
            if (p.impuestosJson.isBlank()) emptyList()
            else p.impuestosJson.split("|").mapNotNull { s -> s.split(";").getOrNull(0) }
        }.toSet()
    }

    fun hasNewItems(): Boolean {
        val predefinedTaxLabels = setOf("IVA 21%", "IVA 10.5%", "IVA 27%") + taxes.filter { it.description.startsWith("IIBB") }.map { it.description }.toSet()
        return items.any { it.description !in knownItemDescriptions } ||
               services.any { it.description !in knownServiceDescriptions } ||
               professionalFees.any { it.description !in knownFeeDescriptions } ||
               miscExpenses.any { it.description !in knownMiscDescriptions } ||
               taxes.any { it.description !in knownTaxDescriptions && it.description !in predefinedTaxLabels }
    }

    fun buildPresupuesto(): PresupuestoEntity {
        val itemsJson = items.joinToString("|") {
            "${it.code};${it.description};${it.quantity};${it.unitPrice};${it.taxPercentage};${it.discountPercentage}"
        }
        val serviciosJson = services.joinToString("|") {
            "${it.code};${it.description};${it.total}"
        }
        val honorariosJson = professionalFees.joinToString("|") {
            "${it.code};${it.description};${it.total}"
        }
        val gastosJson = miscExpenses.joinToString("|") {
            "${it.description};${it.amount}"
        }
        val impuestosJson = taxes.joinToString("|") {
            "${it.description};${it.amount}"
        }
        return PresupuestoEntity(
            id = "pres_chat_${System.currentTimeMillis()}",
            numeroPresupuesto = (if (isProfessional) "C-%03d" else "P-%03d").format(presupuestos.size + 1),
            clienteId = userId,
            prestadorId = providerId,
            fecha = java.time.LocalDate.now().toString(),
            validezDias = validity.toIntOrNull() ?: 7,
            subtotal = subtotal,
            impuestos = itemsTaxTotal + taxesSubtotal,
            total = grandTotal,
            estado = "Enviado",
            tituloTrabajo = tituloTrabajo,
            notas = notes,
            itemsJson = itemsJson,
            serviciosJson = serviciosJson,
            honorariosJson = honorariosJson,
            gastosJson = gastosJson,
            impuestosJson = impuestosJson,
            categorias = provider?.categories?.joinToString(",") ?: "",
            providerCompanyName = providerDisplayName
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.backgroundColor
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.93f)) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = colors.primaryOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            if (isProfessional) "Consulta para" else "Presupuesto para",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                        Text(
                            userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            HorizontalDivider(color = colors.border)

            // --- CONTENT ---
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {

                // --- CATEGORIA SELECTOR ---
                if (providerCategories.isNotEmpty()) {
                    item {
                        ExposedDropdownMenuBox(
                            expanded = budgetCategoryExpanded,
                            onExpandedChange = { budgetCategoryExpanded = it }
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .clickable { budgetCategoryExpanded = true },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, colors.primaryOrange.copy(alpha = 0.5f)),
                                color = colors.primaryOrange.copy(alpha = 0.05f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Category,
                                        contentDescription = null,
                                        tint = colors.primaryOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Rubro / Categoría",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.textSecondary
                                        )
                                        Text(
                                            selectedBudgetCategory.ifBlank { "Seleccionar categoría" },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (selectedBudgetCategory.isBlank()) colors.textSecondary else colors.textPrimary
                                        )
                                    }
                                    Icon(
                                        if (budgetCategoryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = colors.primaryOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            ExposedDropdownMenu(
                                expanded = budgetCategoryExpanded,
                                onDismissRequest = { budgetCategoryExpanded = false },
                                containerColor = colors.surfaceColor
                            ) {
                                providerCategories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                cat,
                                                fontWeight = if (cat == selectedBudgetCategory) FontWeight.Bold else FontWeight.Normal,
                                                color = if (cat == selectedBudgetCategory) colors.primaryOrange else colors.textPrimary
                                            )
                                        },
                                        onClick = {
                                            selectedBudgetCategory = cat
                                            budgetCategoryExpanded = false
                                        },
                                        leadingIcon = {
                                            if (cat == selectedBudgetCategory) {
                                                Icon(Icons.Default.Check, null, tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            if (isProfessional) "Nombre del servicio / consulta" else "Nombre del trabajo / proyecto",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = tituloTrabajo,
                            onValueChange = {
                                tituloTrabajo = it },
                            placeholder = {
                                Text(
                                    if (isProfessional) "Ej: Consulta técnica eléctrica" else "Ej: Instalación de red Wi-Fi",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primaryOrange,
                                focusedLabelColor = colors.primaryOrange,
                                cursorColor = colors.primaryOrange
                            )
                        )
                    }
                }

                // Articles
                if (!isProfessional) item {
                    CollapsibleSection(
                        title = "Artículos",
                        items = items,
                        sectionTotal = itemsSubtotal,
                        isExpanded = expandedSection == "articles",
                        onToggleExpand = { expandedSection = if (expandedSection == "articles") null else "articles" },
                        onAddClick = { itemToEdit = null; sheetType = SheetType.Article },
                        quickAddSlot = {
                            ArticleAutoCompleteFields(
                                suggestions = suggestionItems,
                                onAdd = { selected ->
                                    items.add(selected.copy(id = System.currentTimeMillis()))
                                    expandedSection = "articles"
                                }
                            )
                        }
                    ) { item, index ->
                            ArticleSummaryRow(
                                item = item,
                                onEdit = { itemToEdit = item; sheetType = SheetType.Article },
                                onDelete = { items.removeAt(index) }
                            )
                    }
                }
                // Services
                if (!isProfessional) item {
                    CollapsibleSection(
                        title = "Mano de Obra / Servicios",
                        items = services,
                        sectionTotal = servicesSubtotal,
                        isExpanded = expandedSection == "services",
                        onToggleExpand = { expandedSection = if (expandedSection == "services") null else "services" },
                        onAddClick = { itemToEdit = null; sheetType = SheetType.Service },
                        quickAddSlot = {
                            ServiceAutoCompleteFields(
                                suggestions = suggServices,
                                onAdd = { selected ->
                                    services.add(selected.copy(id = System.currentTimeMillis()))
                                    expandedSection = "services"
                                }
                            )
                        }
                    ) { item, index ->
                        ServiceSummaryRow(
                            item = item,
                            onEdit = { itemToEdit = item; sheetType = SheetType.Service },
                            onDelete = { services.removeAt(index) }
                        )
                    }
                }
                // Professional Fees
                item {
                    CollapsibleSection(
                        title = "Honorarios del Profesional",
                        items = professionalFees,
                        sectionTotal = feesSubtotal,
                        isExpanded = expandedSection == "fees",
                        onToggleExpand = { expandedSection = if (expandedSection == "fees") null else "fees" },
                        onAddClick = { itemToEdit = null; sheetType = SheetType.ProfessionalFee },
                        quickAddSlot = {
                            FeeAutoCompleteFields(
                                suggestions = suggFees,
                                onAdd = { selected ->
                                    professionalFees.add(selected.copy(id = System.currentTimeMillis()))
                                    expandedSection = "fees"
                                }
                            )
                        }
                    ) { item, index ->
                        ProfessionalFeeSummaryRow(
                            item = item,
                            onEdit = { itemToEdit = item; sheetType = SheetType.ProfessionalFee },
                            onDelete = { professionalFees.removeAt(index) }
                        )
                    }
                }
                // Misc
                if (!isProfessional) item {
                    CollapsibleSection(
                        title = "Gastos Varios",
                        items = miscExpenses,
                        sectionTotal = miscSubtotal,
                        isExpanded = expandedSection == "misc",
                        onToggleExpand = { expandedSection = if (expandedSection == "misc") null else "misc" },
                        onAddClick = { itemToEdit = null; sheetType = SheetType.Misc },
                        quickAddSlot = {
                            val suggMisc = remember(presupuestos) {
                                presupuestos.flatMap { p ->
                                    if (p.gastosJson.isBlank()) emptyList()
                                    else p.gastosJson.split("|").mapNotNull { s ->
                                        val parts = s.split(";")
                                        if (parts.size >= 1) parts[0] else null
                                    }
                                }.distinct()
                            }
                            DescriptionAutoCompleteField(
                                label = "Descripción de gasto",
                                suggestions = suggMisc,
                                onSelect = { desc ->
                                    val prevAmount = presupuestos.flatMap { p ->
                                        if (p.gastosJson.isBlank()) emptyList()
                                        else p.gastosJson.split("|").mapNotNull { s ->
                                            val parts = s.split(";")
                                            if (parts.size >= 2 && parts[0] == desc) parts[1].toDoubleOrNull() else null
                                        }
                                    }.firstOrNull() ?: 0.0
                                    miscExpenses.add(BudgetMiscExpense(id = System.currentTimeMillis(), description = desc, amount = prevAmount))
                                    expandedSection = "misc"
                                }
                            )
                        }
                    ) { item, index ->
                        MiscExpenseSummaryRow(
                            item = item,
                            onEdit = { itemToEdit = item; sheetType = SheetType.Misc },
                            onDelete = { miscExpenses.removeAt(index) }
                        )
                    }
                }
                // Taxes
                if (!isProfessional) item {
                    CollapsibleSection(
                        title = "Impuestos",
                        items = taxes,
                        sectionTotal = taxesSubtotal,
                        isExpanded = expandedSection == "taxes",
                        onToggleExpand = { expandedSection = if (expandedSection == "taxes") null else "taxes" },
                        onAddClick = { itemToEdit = null; sheetType = SheetType.Tax },
                        quickAddSlot = {
                            val predefinedLabels = setOf("IVA 21%", "IVA 10.5%", "IVA 27%")
                            val predefinedTaxes = listOf(
                                "IVA 21%" to 21.0,
                                "IVA 10.5%" to 10.5,
                                "IVA 27%" to 27.0
                            )
                            // Custom taxes saved in previous presupuestos
                            val savedCustomTaxes = remember(presupuestos) {
                                presupuestos.flatMap { p ->
                                    if (p.impuestosJson.isBlank()) emptyList()
                                    else p.impuestosJson.split("|").mapNotNull { s ->
                                        val parts = s.split(";")
                                        val desc = parts.getOrNull(0) ?: return@mapNotNull null
                                        val amt = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                                        if (desc !in predefinedLabels && !desc.startsWith("IIBB")) desc to amt else null
                                    }
                                }.distinctBy { it.first }
                            }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                predefinedTaxes.forEach { (label, pct) ->
                                    val alreadyAdded = taxes.any { it.description == label }
                                    FilterChip(
                                        selected = alreadyAdded,
                                        onClick = {
                                            if (!alreadyAdded) {
                                                taxes.add(BudgetTax(id = System.currentTimeMillis(), description = label, amount = subtotal * pct / 100))
                                                expandedSection = "taxes"
                                            } else {
                                                taxes.removeAll { it.description == label }
                                            }
                                        },
                                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = colors.primaryOrange,
                                            selectedLabelColor = Color.White,
                                            containerColor = colors.primaryOrange.copy(alpha = 0.08f),
                                            labelColor = colors.textPrimary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = alreadyAdded,
                                            borderColor = colors.border,
                                            selectedBorderColor = colors.primaryOrange
                                        )
                                    )
                                }
                                // IIBB chip
                                val iibbAdded = taxes.any { it.description.startsWith("IIBB") }
                                FilterChip(
                                    selected = iibbAdded,
                                    onClick = {
                                        if (!iibbAdded) showIIBBDialog = true
                                        else taxes.removeAll { it.description.startsWith("IIBB") }
                                    },
                                    label = { Text("IIBB", style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primaryOrange,
                                        selectedLabelColor = Color.White,
                                        containerColor = colors.primaryOrange.copy(alpha = 0.08f),
                                        labelColor = colors.textPrimary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = iibbAdded,
                                        borderColor = colors.border,
                                        selectedBorderColor = colors.primaryOrange
                                    )
                                )
                                // Saved custom taxes as chips
                                savedCustomTaxes.forEach { (desc, savedAmt) ->
                                    val alreadyAdded = taxes.any { it.description == desc }
                                    val pctFromDesc = Regex("(\\d+(?:\\.\\d+)?)%").find(desc)?.groupValues?.get(1)?.toDoubleOrNull()
                                    FilterChip(
                                        selected = alreadyAdded,
                                        onClick = {
                                            if (!alreadyAdded) {
                                                val amount = if (pctFromDesc != null) subtotal * pctFromDesc / 100.0 else savedAmt
                                                taxes.add(BudgetTax(id = System.currentTimeMillis(), description = desc, amount = amount))
                                                expandedSection = "taxes"
                                            } else {
                                                taxes.removeAll { it.description == desc }
                                            }
                                        },
                                        label = { Text(desc, style = MaterialTheme.typography.labelSmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = colors.primaryOrange,
                                            selectedLabelColor = Color.White,
                                            containerColor = colors.primaryOrange.copy(alpha = 0.08f),
                                            labelColor = colors.textPrimary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = alreadyAdded,
                                            borderColor = colors.border,
                                            selectedBorderColor = colors.primaryOrange
                                        )
                                    )
                                }
                                // + Otro
                                AssistChip(
                                    onClick = { itemToEdit = null; sheetType = SheetType.Tax },
                                    label = { Text("+ Otro", style = MaterialTheme.typography.labelSmall) },
                                    colors = AssistChipDefaults.assistChipColors(labelColor = colors.primaryOrange),
                                    border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = colors.primaryOrange.copy(alpha = 0.4f))
                                )
                            }
                            // Toggle: detallar impuestos en el presupuesto
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Detallar en el presupuesto", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                                Switch(
                                    checked = showTaxDetail,
                                    onCheckedChange = { showTaxDetail = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.primaryOrange)
                                )
                            }
                        }
                    ) { item, index ->
                        TaxSummaryRow(
                            item = item,
                            onEdit = { itemToEdit = item; sheetType = SheetType.Tax },
                            onDelete = { taxes.removeAt(index) }
                        )
                    }
                }
            }

            // --- BOTTOM BAR: totals + generate button ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1E293B),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            "$ ${String.format("%,.2f", grandTotal)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Button(
                        onClick = {
                            pendingPresupuesto = buildPresupuesto()
                            showPreviewDialog = true
                        },
                        enabled = hasItems,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryOrange
                        )
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Vista Previa", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // --- ITEM FORMS as Dialog (to avoid nested ModalBottomSheet) ---
    if (sheetType != null) {
        Dialog(
            onDismissRequest = { sheetType = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.97f)
                    .fillMaxHeight(0.88f),
                shape = RoundedCornerShape(16.dp),
                color = colors.backgroundColor
            ) {
                when (sheetType) {
                    SheetType.Article -> AddArticleSheetContent(
                        itemToEdit = itemToEdit as? BudgetItem,
                        suggestionItems = suggestionItems,
                        currentItems = items,
                        onAddItem = { items.add(it); expandedSection = "articles" },
                        onUpdateItem = { updated ->
                            val i = items.indexOfFirst { it.id == updated.id }
                            if (i != -1) items[i] = updated
                            sheetType = null
                        },
                        onDeleteCurrentItem = { index -> if (index in items.indices) items.removeAt(index) },
                        onDeleteSaved = { saved -> viewModel.deleteArticleFromSuggestions(saved.description) },
                        onSaveToSuggestions = { viewModel.saveArticleToSuggestions(it) },
                        onAddComplete = { sheetType = null }
                    )
                    SheetType.Service -> AddServiceSheetContent(
                        itemToEdit = itemToEdit as? BudgetService,
                        suggestionItems = suggServices,
                        currentItems = services,
                        onAddItem = { services.add(it); expandedSection = "services" },
                        onUpdateItem = { updated ->
                            val i = services.indexOfFirst { it.id == updated.id }
                            if (i != -1) services[i] = updated
                            sheetType = null
                        },
                        onDeleteCurrentItem = { index -> if (index in services.indices) services.removeAt(index) },
                        onDeleteSaved = { saved -> viewModel.deleteServiceFromSuggestions(saved.description) },
                        onSaveToSuggestions = { viewModel.saveServiceToSuggestions(it) },
                        onAddComplete = { sheetType = null }
                    )
                    SheetType.ProfessionalFee -> AddProfessionalFeeSheetContent(
                        itemToEdit = itemToEdit as? BudgetProfessionalFee,
                        suggestionItems = suggFees,
                        currentItems = professionalFees,
                        onAddItem = { professionalFees.add(it); expandedSection = "fees" },
                        onUpdateItem = { updated ->
                            val i = professionalFees.indexOfFirst { it.id == updated.id }
                            if (i != -1) professionalFees[i] = updated
                            sheetType = null
                        },
                        onDeleteCurrentItem = { index -> if (index in professionalFees.indices) professionalFees.removeAt(index) },
                        onDeleteSaved = { saved -> viewModel.deleteProfessionalFeeFromSuggestions(saved.description) },
                        onSaveToSuggestions = { viewModel.saveProfessionalFeeToSuggestions(it) },
                        onAddComplete = { sheetType = null }
                    )
                    SheetType.Misc -> AddMiscExpenseSheetContent(
                        itemToEdit = itemToEdit as? BudgetMiscExpense,
                        existingItems = miscExpenses.toList(),
                        savedGastos = presupuestos.flatMap { p ->
                            if (p.gastosJson.isBlank()) emptyList()
                            else p.gastosJson.split("|").mapNotNull { s ->
                                val parts = s.split(";")
                                val desc = parts.getOrNull(0) ?: return@mapNotNull null
                                val amt = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                                desc to amt
                            }
                        }.distinctBy { it.first },
                        onAddItem = { list -> miscExpenses.addAll(list); expandedSection = "misc" },
                        onUpdateItem = { updated ->
                            val i = miscExpenses.indexOfFirst { it.id == updated.id }
                            if (i != -1) miscExpenses[i] = updated
                            sheetType = null
                        },
                        onDeleteItem = { item -> miscExpenses.removeAll { it.id == item.id } },
                        onDeleteSaved = { desc -> viewModel.deleteMiscExpenseFromSuggestions(desc) },
                        onUpdateSaved = { oldDesc, newDesc, newAmt -> viewModel.updateMiscExpenseInSuggestions(oldDesc, newDesc, newAmt) }
                    )
                    SheetType.Tax -> {
                        val predefinedLabels = setOf("IVA 21%", "IVA 10.5%", "IVA 27%")
                        val customForSheet = presupuestos.flatMap { p ->
                            if (p.impuestosJson.isBlank()) emptyList()
                            else p.impuestosJson.split("|").mapNotNull { s ->
                                val parts = s.split(";")
                                val desc = parts.getOrNull(0) ?: return@mapNotNull null
                                val amt = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                                if (desc !in predefinedLabels && !desc.startsWith("IIBB")) desc to amt else null
                            }
                        }.distinctBy { it.first }
                        AddTaxSheetContent(
                            itemToEdit = itemToEdit as? BudgetTax,
                            subtotal = subtotal,
                            savedCustomTaxes = customForSheet,
                            onDeleteSaved = { desc -> viewModel.deleteCustomTaxFromSuggestions(desc) },
                            onUpdateSaved = { desc, newAmt -> viewModel.updateCustomTaxInSuggestions(desc, newAmt) },
                            onAddItem = { list -> taxes.addAll(list); sheetType = null; expandedSection = "taxes" },
                            onUpdateItem = { updated ->
                                val i = taxes.indexOfFirst { it.id == updated.id }
                                if (i != -1) taxes[i] = updated
                                sheetType = null
                            }
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    // --- PREVIEW WITH CAPTURE ---
    if (showPreviewDialog) {
        // Mapea el proveedor real directamente para usar en el diálogo
        val prestadorReal = remember(provider) {
            provider
        }
        
        // Solo mostramos el diálogo si tenemos el proveedor real
        if (prestadorReal != null) {
            BudgetPreviewPDFDialog(
                prestador = prestadorReal,
                items = items,
                services = services,
                professionalFees = professionalFees,
                miscExpenses = miscExpenses,
                taxes = taxes,
                grandTotal = grandTotal,
                subtotal = subtotal,
                taxAmount = itemsTaxTotal + taxesSubtotal,
                discountAmount = itemsDiscountTotal,
                onDismiss = { showPreviewDialog = false },
                onEnviar = { showPreviewDialog = false; onDismiss() },
                showTaxDetail = showTaxDetail,
                providerName = providerDisplayName,
                providerAddress = providerDisplayAddress,
                isProfessional = isProfessional,
                tituloTrabajo = tituloTrabajo,
                presupuestoNumero = pendingPresupuesto?.numeroPresupuesto ?: "",
                onEnviarBudget = {
                    val pres = pendingPresupuesto ?: buildPresupuesto()
                    //Asegurar que el cliente exista y luego guardar el presupesto
                    coroutineScope.launch {
                        if (clienteData == null) {
                            viewModel.insertCliente(
                                ClienteEntity(
                                    id = userId,
                                    nombre = userName,
                                    email = "",
                                    telefono = "",
                                    direccion = ""
                                )
                            )
                            kotlinx.coroutines.delay(100)
                        }
                        viewModel.insertPresupuesto(pres)
                    }
                    chatViewModel.sendBudgetMessage(pres = pres)
                    showPreviewDialog = false
                    onDismiss()
                },
                clientName = userName,
                clientAddress = overrideClientAddress ?: clienteData?.direccion,
                category = selectedBudgetCategory
            )
        }
    }

    // IIBB Dialog
    if (showIIBBDialog) {
        var iibbPctText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showIIBBDialog = false; iibbPctText = "" },
            title = { Text("IIBB", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = iibbPctText,
                    onValueChange = { iibbPctText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Alícuota %") },
                    singleLine = true,
                    suffix = { Text("%") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primaryOrange,
                        focusedLabelColor = colors.primaryOrange,
                        cursorColor = colors.primaryOrange
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val pct = iibbPctText.toDoubleOrNull() ?: 0.0
                    if (pct > 0) {
                        taxes.add(BudgetTax(id = System.currentTimeMillis(), description = "IIBB ${iibbPctText}%", amount = subtotal * pct / 100))
                        expandedSection = "taxes"
                    }
                    showIIBBDialog = false; iibbPctText = ""
                }) { Text("Agregar", color = colors.primaryOrange, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showIIBBDialog = false; iibbPctText = "" }) { Text("Cancelar") }
            }
        )
    }
}