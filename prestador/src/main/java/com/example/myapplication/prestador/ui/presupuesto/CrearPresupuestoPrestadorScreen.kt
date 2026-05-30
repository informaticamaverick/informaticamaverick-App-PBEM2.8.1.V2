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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
//import com.example.myapplication.prestador.data.PrestadorProfile
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.presupuesto.PresupuestoViewModel
import com.example.myapplication.prestador.viewmodel.presupuesto.PresupuestoConfigViewModel
import androidx.compose.runtime.collectAsState
import com.example.myapplication.prestador.viewmodel.profile.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.profile.ProfileState
import com.example.myapplication.prestador.viewmodel.profile.DireccionViewModel
import com.example.myapplication.prestador.viewmodel.profile.DireccionUiState
import com.example.myapplication.prestador.utils.formatInline
import com.example.myapplication.prestador.utils.toPrestadorProfile
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import com.example.myapplication.prestador.data.ChatData
import com.example.myapplication.prestador.data.local.entity.ClienteEntity
import kotlinx.coroutines.launch
import com.example.myapplication.prestador.ui.presupuesto.sheets.*
import com.example.myapplication.prestador.ui.presupuesto.components.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.zIndex
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

enum class SheetType { Article, Service, ProfessionalFee, Misc, Tax, Attachment, Sections, ClientPicker }

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPresupuestoPrestadorScreen(
    appointmentId: String = "",
    onBack: () -> Unit = {},
    viewModel: PresupuestoViewModel = hiltViewModel(),
    editProfileViewModel: EditProfileViewModel = hiltViewModel(),
    configViewModel: PresupuestoConfigViewModel = hiltViewModel(),
    // direccionViewModel: DireccionViewModel = hiltViewModel() // ARCHIVO REDUNDANTE
) {
    val colors = getPrestadorColors()
    val profileState by editProfileViewModel.profileState.collectAsState()
    val businessEntity by editProfileViewModel.businessEntity.collectAsState()
    val presupuestoConfig by configViewModel.config.collectAsState()
    // val consultorioUiState by direccionViewModel.consultorioState.collectAsState() // SSOT: Usar profileState

    val isProviderProfessional: Boolean = (profileState as? ProfileState.Success)?.provider?.serviceType
        .equals("PROFESSIONAL", ignoreCase = true)
    val provider = (profileState as? ProfileState.Success)?.provider

    // =========================================================================
    // SECCIÓN: CARGA DE DATOS (SSOT)
    // Se comenta la carga via DireccionViewModel por ser redundante.
    // Los datos ya vienen embebidos en el objeto Provider.
    // =========================================================================
    /*
    LaunchedEffect(isProviderProfessional, provider?.id) {
        if (isProviderProfessional && !provider?.id.isNullOrBlank()) {
            direccionViewModel.loadConsultorioDireccion(provider!!.id)
        }
    }
    */

    // val consultorioDireccion = (consultorioUiState as? DireccionUiState.Success)?.direccion
    // val consultorioDisplayAddress = consultorioDireccion?.formatInline().orEmpty()
    
    // Obtener la dirección del consultorio directamente del modelo jerárquico
    val consultorioDisplayAddress = provider?.address?.fullString().orEmpty()

    val providerDisplayName = when {
        provider?.tieneEmpresa == true && !provider.nombreEmpresa.isNullOrBlank() -> provider.nombreEmpresa!!
        provider?.tieneEmpresa == true && !businessEntity?.nombreNegocio.isNullOrBlank() -> businessEntity!!.nombreNegocio
        else -> "${provider?.name.orEmpty()} ${provider?.apellido.orEmpty()}".trim()
    }
    val providerDisplayAnddress = when {
        isProviderProfessional && consultorioDisplayAddress.isNotBlank() -> consultorioDisplayAddress
        provider?.tieneEmpresa == true && !businessEntity?.direccion.isNullOrBlank() -> businessEntity!!.direccion
        provider?.tieneEmpresa == true && !provider.direccionEmpresa.isNullOrBlank() -> provider.direccionEmpresa!!
        provider?.turnosEnLocal == true && !provider.direccionLocal.isNullOrBlank() -> provider.direccionLocal!!
        provider?.address != null -> provider.address?.fullString().orEmpty()
        else -> ""
    }
    // --- SECCIÓN: OBTENCIÓN DE DATOS DEL PRESTADOR ---
    // Se utiliza el modelo real ProviderEntity proveniente del ViewModel
    val prestador = remember(provider) {
        provider?.toPrestadorProfile(businessEntity)
    }

    // --- SECCIÓN: CONTEXTO ---
    val context = androidx.compose.ui.platform.LocalContext.current

    // Recarga el config cada vez que esta pantalla vuelve al foco (ej: al volver desde PresupuestoConfigScreen)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                configViewModel.refreshConfig()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val items = remember { mutableStateListOf<BudgetItem>() }
    val services = remember { mutableStateListOf<BudgetService>() }
    val professionalFees = remember { mutableStateListOf<BudgetProfessionalFee>() }
    val miscExpenses = remember { mutableStateListOf<BudgetMiscExpense>() }
    val taxes = remember { mutableStateListOf<BudgetTax>() }
    val attachments = remember { mutableStateListOf<BudgetAttachment>() }

    val lazyListState = rememberLazyListState()

    var showPreviewDialog by remember { mutableStateOf(false) }
    var showSaveCatalogDialog by remember { mutableStateOf(false) }
    var pendingPresupuesto by remember { mutableStateOf<com.example.myapplication.prestador.data.local.entity.PresupuestoEntity?>(null) }
    val presupuestos by viewModel.presupuestos.collectAsState()
    val clientes by viewModel.clientes.collectAsState()
    val articleCatalog by viewModel.articleCatalog.collectAsState()
    val serviceCatalog by viewModel.serviceCatalog.collectAsState()
    val feeCatalog by viewModel.feeCatalog.collectAsState()
    val savedArticleItems = remember(articleCatalog) {
        val json = articleCatalog?.itemsJson ?: ""
        if (json.isBlank()) emptyList()
        else json.split("|").mapNotNull { s ->
            val parts = s.split(";")
            if (parts.size >= 4) BudgetItem(
                id = 0L, code = parts[0], description = parts[1],
                quantity = parts[2].toIntOrNull() ?: 1,
                unitPrice = parts[3].toDoubleOrNull() ?: 0.0,
                taxPercentage = parts.getOrNull(4)?.toDoubleOrNull() ?: 0.0,
                discountPercentage = parts.getOrNull(5)?.toDoubleOrNull() ?: 0.0
            ) else null
        }.distinctBy { it.description }
    }
    val savedServiceItems = remember(serviceCatalog) {
        val json = serviceCatalog?.serviciosJson ?: ""
        if (json.isBlank()) emptyList()
        else json.split("|").mapNotNull { s ->
            val parts = s.split(";")
            if (parts.size >= 2) BudgetService(id = 0L, code = parts[0], description = parts[1], total = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0) else null
        }.distinctBy { it.description }
    }
    val savedFeeItems = remember(feeCatalog) {
        val json = feeCatalog?.honorariosJson ?: ""
        if (json.isBlank()) emptyList()
        else json.split("|").mapNotNull { s ->
            val parts = s.split(";")
            if (parts.size >= 2) BudgetProfessionalFee(id = 0L, code = parts[0], description = parts[1], total = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0) else null
        }.distinctBy { it.description }
    }
    var pendingSimPresupuestoId by remember { mutableStateOf("") }
    var pendingSimClienteName by remember { mutableStateOf("") }
    var pendingSimTotal by remember { mutableStateOf(0.0) }

    // --- BOTTOM SHEET STATES ---
    var sheetType by remember { mutableStateOf<SheetType?>(null) }
    var itemToEdit by remember { mutableStateOf<Any?>(null) }


    // --- SECTION EXPANSION STATES ---
    var isArticlesExpanded by remember { mutableStateOf(true) }
    var isServicesExpanded by remember { mutableStateOf(true) }
    var isProfessionalFeesExpanded by remember { mutableStateOf(true) }
    var isMiscExpanded by remember { mutableStateOf(true) }
    var isTaxesExpanded by remember { mutableStateOf(true) }
    var isAttachmentsExpanded by remember { mutableStateOf(true) }
    var showIIBBDialog by remember { mutableStateOf(false) }
    var showTaxDetail by remember { mutableStateOf(false) }

    // --- SECTION VISIBILITY STATES ---
    var showArticlesSection by
    remember(presupuestoConfig.showArticlesByDefault) {
        mutableStateOf(presupuestoConfig.showArticlesByDefault) }
    var showServicesSection by
    remember(presupuestoConfig.showServicesByDefault) { mutableStateOf(presupuestoConfig.showServicesByDefault) }
    var showProfessionalFeesSection by
    remember(presupuestoConfig.showFeesByDefault) {
        mutableStateOf(presupuestoConfig.showFeesByDefault) }
    var showMiscSection by remember(presupuestoConfig.showMiscByDefault) {
        mutableStateOf(presupuestoConfig.showMiscByDefault) }
    var showTaxesSection by remember(presupuestoConfig.showTaxesByDefault) {
        mutableStateOf(presupuestoConfig.showTaxesByDefault) }
    var showAttachmentsSection by
    remember(presupuestoConfig.showAttachmentsByDefault) {
        mutableStateOf(presupuestoConfig.showAttachmentsByDefault) }


    // --- NEW FIELDS STATE ---
    var validity by remember(presupuestoConfig.validezDias) { mutableStateOf(presupuestoConfig.validezDias.toString()) }
    var notes by remember { mutableStateOf("") }


    // --- CLIENTE STATE ---
    var clienteNombre by remember { mutableStateOf("") }
    var clienteEmail by remember { mutableStateOf("") }
    var clienteTelefono by remember { mutableStateOf("") }
    var clienteDireccion by remember { mutableStateOf("") }
    var selectedClienteId by remember { mutableStateOf<String?>(null) }

    // --- CATEGORIA STATE ---
    val providerCategories = remember(provider) { provider?.categories ?: emptyList() }
    var selectedBudgetCategory by remember(providerCategories) {
        mutableStateOf(providerCategories.firstOrNull() ?: "")
    }
    var budgetCategoryExpanded by remember { mutableStateOf(false) }
    //BUSCADOR INLINE (clientes del chat)
    var clienteQuery by remember { mutableStateOf("")}
    var clienteFieldFocused by remember { mutableStateOf(false) }
    var attemptedSend by remember { mutableStateOf(false) }

    val clientFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val coroutineScope= rememberCoroutineScope()

    val serviceTypeForChat = provider?.serviceType ?: if (isProviderProfessional) "PROFESSIONAL"
    else "TECHNICAL"
    val chatClients = remember(serviceTypeForChat)
    {
        ChatData.getConversationsByServiceType(serviceTypeForChat) //clientes que ven en chat
    }

    val filteredChatClients = remember(chatClients, clienteQuery) {
        val q = clienteQuery.trim()
        if (q.isBlank()) emptyList()
        else chatClients.filter { c ->
            c.userName.contains(q, ignoreCase = true)
        }.take(8)
    }

    // --- CALCULATED TOTALS ---
    // Calcular subtotal base (sin impuestos ni descuentos)
    val itemsBaseSubtotal = items.sumOf { it.unitPrice * it.quantity }
    
    // Calcular impuestos totales
    val itemsTaxTotal = items.sumOf {
        val base = it.unitPrice * it.quantity
        base * (it.taxPercentage / 100)
    }
    
    // Calcular descuentos totales
    val itemsDiscountTotal = items.sumOf {
        val base = it.unitPrice * it.quantity
        val taxAmount = base * (it.taxPercentage / 100)
        val withTax = base + taxAmount
        withTax * (it.discountPercentage / 100)
    }
    
    // Subtotal de items después de impuestos y descuentos
    val itemsSubtotal = itemsBaseSubtotal + itemsTaxTotal - itemsDiscountTotal

    val servicesSubtotal = services.sumOf { it.total }
    val professionalFeesSubtotal = professionalFees.sumOf { it.total }
    val miscSubtotal = miscExpenses.sumOf { it.amount }
    val taxesSubtotal = taxes.sumOf { it.amount }

    val subtotal = itemsSubtotal + servicesSubtotal + professionalFeesSubtotal + miscSubtotal
    val grandTotal = subtotal + taxesSubtotal

    // Base para chips de IVA: excluye artículos que ya tienen IVA propio aplicado
    // (evita doble imposición cuando el artículo tiene taxPercentage > 0)
    val ivaBase = items.filter { it.taxPercentage == 0.0 }.sumOf { it.unitPrice * it.quantity } +
                  servicesSubtotal + professionalFeesSubtotal + miscSubtotal


    // --- DERIVED STATE FOR SCROLL ---
    val isScrolledToEnd by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (layoutInfo.totalItemsCount == 0) {
                true
            } else {
                val lastVisibleItem = visibleItemsInfo.lastOrNull()
                lastVisibleItem != null && lastVisibleItem.index == layoutInfo.totalItemsCount - 1
            }
        }
    }

    Scaffold(
        containerColor = colors.backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isProviderProfessional) "Crear Consulta" else "Crear Presupuesto",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.primaryOrange
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val clienteId = selectedClienteId
                    if (clienteId.isNullOrBlank()) {
                        attemptedSend = true
                        clientFocusRequester.requestFocus()
                        return@FloatingActionButton
                    }
                    attemptedSend = false

                    // Usar el ID del prestador actual (o crear uno demo si no existe)
                    val prestadorId = "prestador_${System.currentTimeMillis()}"

                    // Convertir items a JSON
                    val itemsJson = items.joinToString("|") { item ->
                        "${item.code};${item.description};${item.quantity};${item.unitPrice};${item.taxPercentage};${item.discountPercentage}"
                    }
                    val serviciosJson = services.joinToString("|") { service ->
                        "${service.code};${service.description};${service.total}"
                    }
                    val honorariosJson = professionalFees.joinToString("|") { fee ->
                        "${fee.code};${fee.description};${fee.total}"
                    }
                    val gastosJson = miscExpenses.joinToString("|") { misc ->
                        "${misc.description};${misc.amount}"
                    }
                    val impuestosJsonStr = taxes.joinToString("|") { tax ->
                        "${tax.description};${tax.amount}"
                    }

                val nuevoPresupuesto = com.example.myapplication.prestador.data.local.entity.PresupuestoEntity(
                        id = "pres_${System.currentTimeMillis()}",
                        numeroPresupuesto = "${presupuestoConfig.prefijo}-%04d".format(presupuestoConfig.proximoNumero),
                        clienteId = clienteId,
                        prestadorId = prestadorId,
                        fecha = java.time.LocalDate.now().toString(),
                        validezDias = validity.toIntOrNull() ?: presupuestoConfig.validezDias,
                        subtotal = subtotal,
                        impuestos = itemsTaxTotal + taxesSubtotal,
                        total = grandTotal,
                        estado = "Enviado",
                        notas = notes,
                        notaLegal = presupuestoConfig.notaLegal,
                        itemsJson = itemsJson,
                        serviciosJson = serviciosJson,
                        honorariosJson = honorariosJson,
                        gastosJson = gastosJson,
                        impuestosJson = impuestosJsonStr,
                        appointmentId = appointmentId.ifBlank { null },
                        categorias = provider?.categories?.joinToString(",") ?: ""
                    )

                    pendingSimPresupuestoId = nuevoPresupuesto.id
                    pendingSimClienteName = clienteNombre.ifBlank { "Cliente" }
                    pendingSimTotal = grandTotal
                    pendingPresupuesto = nuevoPresupuesto
                    configViewModel.setProximoNumero(presupuestoConfig.proximoNumero + 1)
                    val hasNewItems = items.isNotEmpty() || services.isNotEmpty() ||
                        professionalFees.isNotEmpty() || miscExpenses.isNotEmpty() || taxes.isNotEmpty()
                    if (hasNewItems) showSaveCatalogDialog = true else showPreviewDialog = true
                },
                containerColor = colors.primaryOrange,
                contentColor = Color.White
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
            }
        }
    ) { paddingValues ->
        val monedaChanged = presupuestoConfig.moneda != presupuestoConfig.lastAcknowledgedMoneda
        val monedaLabel = if (presupuestoConfig.moneda == "USD") "Dólares (US$)" else "Pesos ($)"

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            // Banner de aviso cuando cambió la moneda
            if (monedaChanged) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFF856404),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Cambiaste la moneda a $monedaLabel. Revisá los precios de tus artículos y servicios.",
                            fontSize = 13.sp,
                            color = Color(0xFF856404),
                            modifier = Modifier.weight(1f),
                            lineHeight = 18.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = { configViewModel.acknowledgeMonedaChange() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Entendido",
                                tint = Color(0xFF856404),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                val numeroPreview = "${presupuestoConfig.prefijo}-%04d".format(presupuestoConfig.proximoNumero)
                item { PrestadorHeader(prestador = prestador ?: return@item, numeroPresupuesto = numeroPreview,
                    onFilterClick = { sheetType = SheetType.Sections}) }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Cliente",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )

                           val hasSelectedCliente = !selectedClienteId.isNullOrBlank()

                            OutlinedTextField(
                                value = if (hasSelectedCliente)
                                clienteNombre else clienteQuery,
                                onValueChange = { value ->
                                    if (!hasSelectedCliente) {
                                        clienteQuery = value
                                        attemptedSend = false
                                    }
                                },
                                readOnly = hasSelectedCliente,
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodySmall,
                                label = { Text("Cliente", style = MaterialTheme.typography.labelSmall) },
                                placeholder = {
                                    if (!hasSelectedCliente) Text("Escribí nombre, email o telefono", style = MaterialTheme.typography.labelSmall)
                                },
                                leadingIcon = {
                                    Icon(
                                        if ( hasSelectedCliente)
                                        Icons.Default.Person else Icons.Default.Search,
                                        contentDescription = null,
                                        tint = colors.textSecondary
                                    )
                                },
                                isError = attemptedSend && !hasSelectedCliente,
                                trailingIcon = {
                                    if (hasSelectedCliente || clienteQuery.isNotBlank()) {
                                        IconButton(onClick = {
                                            attemptedSend = false

                                            selectedClienteId = null
                                            clienteNombre = ""
                                            clienteEmail = ""
                                            clienteTelefono = ""
                                            clienteDireccion = ""
                                            clienteQuery = ""

                                            //vuelve a modo Busqueda
                                            clientFocusRequester.requestFocus()
                                        }) {
                                            Icon(Icons.Default.Close, contentDescription = "Cambiar cliente")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth(0.75f)
                                    .height(52.dp)
                                    .focusRequester(clientFocusRequester)
                                    .onFocusChanged { clienteFieldFocused = it.isFocused },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.primaryOrange,
                                    unfocusedBorderColor = if (hasSelectedCliente)
                                    colors.primaryOrange.copy(alpha = 0.65f) else colors.border,
                                    focusedLabelColor = colors.primaryOrange,
                                    unfocusedLabelColor = colors.textSecondary,
                                    cursorColor = colors.primaryOrange,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary,
                                    focusedContainerColor = if (hasSelectedCliente)
                                    colors.primaryOrange.copy(alpha = 0.06f) else Color.Transparent,
                                    unfocusedContainerColor = if (hasSelectedCliente)
                                    colors.primaryOrange.copy(alpha = 0.06f) else Color.Transparent,
                                ),
                                shape = RoundedCornerShape(8.dp)

                            )

                            if (hasSelectedCliente) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = colors.primaryOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "Seleccionado",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            if (attemptedSend && selectedClienteId.isNullOrBlank()) {
                                Text(
                                    "Seleccioná un cliente para continuar.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }

                            val showSuggetions = clienteFieldFocused && selectedClienteId.isNullOrBlank() && clienteQuery.isNotBlank()
                            if (showSuggetions) {
                                Spacer(Modifier.height(8.dp))

                                if (filteredChatClients.isEmpty()) {
                                    Text("No se encontraron clientes.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary)
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        filteredChatClients.forEach { conv ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = colors.backgroundColor),
                                                shape = RoundedCornerShape(12.dp),
                                                onClick = {
                                                    //Seleción
                                                    selectedClienteId = conv.userId
                                                    clienteNombre = conv.userName
                                                    clienteQuery = conv.userName

                                                    //Asegurar que exista en room (como un Budgetchatsheet)

                                                    viewModel.insertCliente(
                                                        ClienteEntity(
                                                            id = conv.userId,
                                                            nombre = conv.userName
                                                        )
                                                    )

                                                    focusManager.clearFocus()
                                                }
                                            ) {
                                                Column(Modifier.padding(12.dp)) {
                                                    Text(conv.userName,
                                                        fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (selectedClienteId.isNullOrBlank()) {
                                Text(
                                    "Obligatorio",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }

                // --- CATEGORIA SELECTOR ---
                if (providerCategories.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    "Rubro / Categoría",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
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
                                            Text(
                                                selectedBudgetCategory.ifBlank { "Seleccionar categoría" },
                                                modifier = Modifier.weight(1f),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (selectedBudgetCategory.isBlank()) colors.textSecondary else colors.textPrimary
                                            )
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
                    }
                }

                // --- SECTIONS ---
                if (showArticlesSection) {
                    item {
                        CollapsibleSection(
                            title = "Artículos",
                            items = items,
                            sectionTotal = itemsSubtotal,
                            isExpanded = isArticlesExpanded,
                            onToggleExpand = { isArticlesExpanded = !isArticlesExpanded },
                            onAddClick = {
                                itemToEdit = null
                                sheetType = SheetType.Article
                            },
                            quickAddSlot = {
                                ArticleAutoCompleteFields(
                                    suggestions = savedArticleItems,
                                    onAdd = { items.add(it.copy(id = System.currentTimeMillis())); isArticlesExpanded = true },
                                    items = items,
                                    onEdit = { item -> itemToEdit = item; sheetType = SheetType.Article },
                                    onDelete = { index -> items.removeAt(index) }
                                )
                            },
                            showDefaultContent = false
                        ) { _, _ -> }
                    }
                }

                if (showServicesSection) {
                    item {
                        CollapsibleSection(
                            title = "Mano de Obra / Servicios",
                            items = services,
                            sectionTotal = servicesSubtotal,
                            isExpanded = isServicesExpanded,
                            onToggleExpand = { isServicesExpanded = !isServicesExpanded },
                            onAddClick = {
                                itemToEdit = null
                                sheetType = SheetType.Service
                            },
                            quickAddSlot = {
                                ServiceAutoCompleteFields(
                                    suggestions = savedServiceItems,
                                    onAdd = { services.add(it.copy(id = System.currentTimeMillis())); isServicesExpanded = true },
                                    items = services,
                                    onEdit = { item -> itemToEdit = item; sheetType = SheetType.Service },
                                    onDelete = { index -> services.removeAt(index) }
                                )
                            },
                            showDefaultContent = false
                        ) { _, _ -> }
                    }
                }

                if (showProfessionalFeesSection) {
                    item {
                        CollapsibleSection(
                            title = "Honorarios del Profesional",
                            items = professionalFees,
                            sectionTotal = professionalFeesSubtotal,
                            isExpanded = isProfessionalFeesExpanded,
                            onToggleExpand = { isProfessionalFeesExpanded = !isProfessionalFeesExpanded },
                            onAddClick = { itemToEdit = null; sheetType = SheetType.ProfessionalFee },
                            quickAddSlot = {
                                FeeAutoCompleteFields(
                                    suggestions = savedFeeItems,
                                    onAdd = { professionalFees.add(it.copy(id = System.currentTimeMillis())); isProfessionalFeesExpanded = true },
                                    items = professionalFees,
                                    onEdit = { item -> itemToEdit = item; sheetType = SheetType.ProfessionalFee },
                                    onDelete = { index -> professionalFees.removeAt(index) }
                                )
                            },
                            showDefaultContent = false
                        ) { _, _ -> }
                    }
                }

                if (showMiscSection) {
                    item {
                        CollapsibleSection(
                            title = "Gastos Varios",
                            items = miscExpenses,
                            sectionTotal = miscSubtotal,
                            isExpanded = isMiscExpanded,
                            onToggleExpand = { isMiscExpanded = !isMiscExpanded },
                            onAddClick = { itemToEdit = null; sheetType = SheetType.Misc },
                            quickAddSlot = {
                                val suggMisc = remember(presupuestos) {
                                    presupuestos.flatMap { p ->
                                        if (p.gastosJson.isBlank()) emptyList()
                                        else p.gastosJson.split("|").mapNotNull { s ->
                                            val parts = s.split(";")
                                            if (parts.size >= 1 && parts[0].isNotBlank()) parts[0] else null
                                        }
                                    }.distinct()
                                }
                                DescriptionAutoCompleteField(label = "Buscar gasto...", suggestions = suggMisc, onSelect = { desc ->
                                    val prev = presupuestos.flatMap { p -> if (p.gastosJson.isBlank()) emptyList() else p.gastosJson.split("|").mapNotNull { s -> val parts = s.split(";"); if (parts[0] == desc) parts.getOrNull(1)?.toDoubleOrNull() else null } }.firstOrNull() ?: 0.0
                                    miscExpenses.add(BudgetMiscExpense(description = desc, amount = prev)); isMiscExpanded = true
                                })
                            }
                        ) { item, index ->
                            MiscExpenseSummaryRow(
                                item = item,
                                onEdit = { itemToEdit = item; sheetType = SheetType.Misc },
                                onDelete = { miscExpenses.removeAt(index) }
                            )
                        }
                    }
                }

                if (showTaxesSection) {
                    item {
                        CollapsibleSection(
                            title = "Impuestos",
                            items = taxes,
                            sectionTotal = taxesSubtotal,
                            isExpanded = isTaxesExpanded,
                            onToggleExpand = { isTaxesExpanded = !isTaxesExpanded },
                            onAddClick = { itemToEdit = null; sheetType = SheetType.Tax },
                            quickAddSlot = {
                                val predefinedLabels = setOf("IVA 21%", "IVA 10.5%", "IVA 27%")
                                val predefinedTaxes = listOf("IVA 21%" to 21.0, "IVA 10.5%" to 10.5, "IVA 27%" to 27.0)
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    predefinedTaxes.forEach { (label, pct) ->
                                        val alreadyAdded = taxes.any { it.description == label }
                                        FilterChip(
                                            selected = alreadyAdded,
                                            onClick = {
                                                if (!alreadyAdded) { taxes.add(BudgetTax(id = System.currentTimeMillis(), description = label, amount = ivaBase * pct / 100)); isTaxesExpanded = true }
                                                else taxes.removeAll { it.description == label }
                                            },
                                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = colors.primaryOrange, selectedLabelColor = Color.White, containerColor = colors.primaryOrange.copy(alpha = 0.08f), labelColor = colors.textPrimary),
                                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = alreadyAdded, borderColor = colors.border, selectedBorderColor = colors.primaryOrange)
                                        )
                                    }
                                    if (itemsTaxTotal > 0.0) {
                                        Text(
                                            "⚠ Algunos artículos ya incluyen IVA",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.primaryOrange,
                                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                                        )
                                    }
                                    val iibbAdded = taxes.any { it.description.startsWith("IIBB") }
                                    FilterChip(
                                        selected = iibbAdded,
                                        onClick = { if (!iibbAdded) showIIBBDialog = true else taxes.removeAll { it.description.startsWith("IIBB") } },
                                        label = { Text("IIBB", style = MaterialTheme.typography.labelSmall) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = colors.primaryOrange, selectedLabelColor = Color.White, containerColor = colors.primaryOrange.copy(alpha = 0.08f), labelColor = colors.textPrimary),
                                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = iibbAdded, borderColor = colors.border, selectedBorderColor = colors.primaryOrange)
                                    )
                                    savedCustomTaxes.forEach { (desc, savedAmt) ->
                                        val alreadyAdded = taxes.any { it.description == desc }
                                        val pctFromDesc = Regex("(\\d+(?:\\.\\d+)?)%").find(desc)?.groupValues?.get(1)?.toDoubleOrNull()
                                        FilterChip(
                                            selected = alreadyAdded,
                                            onClick = {
                                                if (!alreadyAdded) { val amount = if (pctFromDesc != null) subtotal * pctFromDesc / 100.0 else savedAmt; taxes.add(BudgetTax(id = System.currentTimeMillis(), description = desc, amount = amount)); isTaxesExpanded = true }
                                                else taxes.removeAll { it.description == desc }
                                            },
                                            label = { Text(desc, style = MaterialTheme.typography.labelSmall) },
                                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = colors.primaryOrange, selectedLabelColor = Color.White, containerColor = colors.primaryOrange.copy(alpha = 0.08f), labelColor = colors.textPrimary),
                                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = alreadyAdded, borderColor = colors.border, selectedBorderColor = colors.primaryOrange)
                                        )
                                    }
                                    AssistChip(
                                        onClick = { itemToEdit = null; sheetType = SheetType.Tax },
                                        label = { Text("+ Otro", style = MaterialTheme.typography.labelSmall) },
                                        colors = AssistChipDefaults.assistChipColors(labelColor = colors.primaryOrange),
                                        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = colors.primaryOrange.copy(alpha = 0.4f))
                                    )
                                }
                                // Toggle: detallar impuestos en el presupuesto
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp),
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

                if (showAttachmentsSection) {
                    item {
                        CollapsibleSection(
                            title = "Archivos Adjuntos",
                            items = attachments,
                            sectionTotal = 0.0,
                            isExpanded = isAttachmentsExpanded,
                            onToggleExpand = { isAttachmentsExpanded = !isAttachmentsExpanded },
                            onAddClick = {
                                itemToEdit = null
                                sheetType = SheetType.Attachment
                            }
                        ) { item, index ->
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                AttachmentSummaryRow(
                                    modifier = Modifier.pointerInput(Unit) {
                                        detectTapGestures(onLongPress = { showMenu = true })
                                    },
                                    item = item,
                                )
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(text = { Text("Editar Descripción") }, onClick = {
                                        itemToEdit = item; sheetType = SheetType.Attachment; showMenu = false
                                    })
                                    DropdownMenuItem(text = { Text("Eliminar") }, onClick = { attachments.removeAt(index); showMenu = false })
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.surfaceColor
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Observaciones", 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Escriba aquí sus observaciones...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.primaryOrange,
                                    unfocusedBorderColor = colors.border,
                                    focusedLabelColor = colors.primaryOrange,
                                    unfocusedLabelColor = colors.textSecondary,
                                    cursorColor = colors.primaryOrange,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                )
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically, 
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    if (isProviderProfessional) "Consulta válida por" else "Presupuesto válido por",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textPrimary
                                )
                                CompactTextField(
                                    value = validity,
                                    onValueChange = { validity = it.filter { char -> char.isDigit() } },
                                    modifier = Modifier.width(60.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    textStyle = TextStyle(textAlign = TextAlign.Center)
                                )
                                Text(
                                    "días.", 
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(150.dp)) }
            }

            // --- TOTALS SUMMARY (STICKY) ---
            TotalsSummary(
                modifier = Modifier.align(Alignment.BottomCenter),
                isExpanded = isScrolledToEnd,
                grandTotal = grandTotal
            )

            // --- SAVE CATALOG DIALOG ---
            if (showSaveCatalogDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showSaveCatalogDialog = false; showPreviewDialog = true },
                    title = { Text("¿Guardar en lista?", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                    text = { Text(if (isProviderProfessional) "¿Querés guardar estos ítems para autocompletar futuras consultas?" else "¿Querés guardar estos ítems para autocompletar futuros presupuestos?") },
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = {
                            pendingPresupuesto?.let { viewModel.insertPresupuesto(it) }
                            showSaveCatalogDialog = false
                            showPreviewDialog = true
                        }) { Text("Sí, guardar", color = colors.primaryOrange) }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = {
                            showSaveCatalogDialog = false
                            showPreviewDialog = true
                        }) { Text("No") }
                    }
                )
            }

            // --- PREVIEW DIALOG ---
            if (showPreviewDialog) {
                val capturedSimId = pendingSimPresupuestoId
                val capturedSimName = pendingSimClienteName
                val capturedSimTotal = pendingSimTotal
            // Solo mostramos si tenemos el proveedor
            if (provider != null) {
                BudgetPreviewPDFDialog(
                    prestador = provider,
                    items = items,
                    services = services,
                    professionalFees = professionalFees,
                    miscExpenses = miscExpenses,
                    taxes = taxes,
                    grandTotal = grandTotal,
                    subtotal = subtotal,
                    taxAmount = itemsTaxTotal + taxesSubtotal,
                    discountAmount = itemsDiscountTotal,
                    showTaxDetail = showTaxDetail,
                    onDismiss = {
                        showPreviewDialog = false
                    },
                    onEnviar = {
                        showPreviewDialog = false
                        if (capturedSimId.isNotBlank()) {
                            viewModel.simulateClientResponse(
                                context = context,
                                presupuestoId = capturedSimId,
                                clienteName = capturedSimName,
                                total = capturedSimTotal
                            )
                        }
                        onBack()
                    },
                    onCompartirPDF = { bitmap ->
                        try {
                            //Convertir bitmap a PDF
                            val pdfDoc = PdfDocument()
                            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                            val page = pdfDoc.startPage(pageInfo)
                            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                            pdfDoc.finishPage(page)

                            //Guardar en caché
                            val dir = File(context.cacheDir, "presupuestos").also { it.mkdirs() }
                            val file = File(dir, "presupuesto_${System.currentTimeMillis()}.pdf")
                            FileOutputStream(file).use { pdfDoc.writeTo(it) }
                            pdfDoc.close()

                            //Compartir
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(Intent.EXTRA_SUBJECT, "Presupuesto")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir presupuesto"))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    clientName = clienteNombre,
                    clientAddress = clienteDireccion.ifBlank { null },
                    providerName = providerDisplayName,
                    providerAddress = providerDisplayAnddress,
                    isProfessional = isProviderProfessional,
                    presupuestoNumero = pendingPresupuesto?.numeroPresupuesto ?: "",
                    category = selectedBudgetCategory,
                    validezDias = validity.toIntOrNull() ?: presupuestoConfig.validezDias,
                    notaLegal = presupuestoConfig.notaLegal
                )
            }
            }

            // --- BOTTOM SHEETS ---
            if (sheetType != null) {
                Dialog(
                    onDismissRequest = { sheetType = null },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
            val view = androidx.compose.ui.platform.LocalView.current
                    DisposableEffect(view) {
                        val window = (view.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
                        if (window != null) {
                            WindowCompat.setDecorFitsSystemWindows(window, false)
                        }
                        onDispose {}
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding(),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        color = colors.backgroundColor
                    ) {
                        when (sheetType) {
                        SheetType.Article -> AddArticleSheetContent(
                            itemToEdit = itemToEdit as? BudgetItem,
                            suggestionItems = savedArticleItems,
                            onAddItem = { newItem -> items.add(newItem); isArticlesExpanded = true },
                            onUpdateItem = { updatedItem ->
                                val index = items.indexOfFirst { it.id == updatedItem.id }
                                if (index != -1) items[index] = updatedItem
                                sheetType = null
                            },
                            onDeleteSaved = { saved -> viewModel.deleteArticleFromSuggestions(saved.description) },
                            onSaveToSuggestions = { viewModel.saveArticleToSuggestions(it) },
                            onDismiss = { sheetType = null }
                        )
                        SheetType.Service -> AddServiceSheetContent(
                            itemToEdit = itemToEdit as? BudgetService,
                            suggestionItems = savedServiceItems,
                            onAddItem = { newItem -> services.add(newItem); isServicesExpanded = true },
                            onUpdateItem = { updatedItem ->
                                val index = services.indexOfFirst { it.id == updatedItem.id }
                                if (index != -1) services[index] = updatedItem
                                sheetType = null
                            },
                            onDeleteSaved = { saved -> viewModel.deleteServiceFromSuggestions(saved.description) },
                            onSaveToSuggestions = { viewModel.saveServiceToSuggestions(it) },
                            onDismiss = { sheetType = null }
                        )
                        SheetType.ProfessionalFee -> AddProfessionalFeeSheetContent(
                            itemToEdit = itemToEdit as? BudgetProfessionalFee,
                            suggestionItems = savedFeeItems,
                            onAddItem = { newItem -> professionalFees.add(newItem); isProfessionalFeesExpanded = true },
                            onUpdateItem = { updatedItem ->
                                val index = professionalFees.indexOfFirst { it.id == updatedItem.id }
                                if (index != -1) professionalFees[index] = updatedItem
                                sheetType = null
                            },
                            onDeleteSaved = { saved -> viewModel.deleteProfessionalFeeFromSuggestions(saved.description) },
                            onSaveToSuggestions = { viewModel.saveProfessionalFeeToSuggestions(it) },
                            onDismiss = { sheetType = null }
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
                            onAddItem = { newItems -> miscExpenses.addAll(newItems); isMiscExpanded = true },
                            onUpdateItem = { updatedItem ->
                                val index = miscExpenses.indexOfFirst { it.id == updatedItem.id }
                                if (index != -1) miscExpenses[index] = updatedItem
                                sheetType = null
                            },
                            onDeleteItem = { item -> miscExpenses.removeAll { it.id == item.id } },
                            onDeleteSaved = { desc -> viewModel.deleteMiscExpenseFromSuggestions(desc) },
                            onUpdateSaved = { oldDesc, newDesc, newAmt -> viewModel.updateMiscExpenseInSuggestions(oldDesc, newDesc, newAmt) },
                            onDismiss = { sheetType = null }
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
                                onAddItem = { newItems -> taxes.addAll(newItems); sheetType = null; isTaxesExpanded = true },
                                onUpdateItem = { updatedItem ->
                                    val index = taxes.indexOfFirst { it.id == updatedItem.id }
                                    if (index != -1) taxes[index] = updatedItem
                                    sheetType = null
                                },
                                onDismiss = { sheetType = null }
                            )
                        }
                        SheetType.Attachment -> AddAttachmentSheetContent(
                            itemToEdit = itemToEdit as? BudgetAttachment,
                            onAddItem = { newItem -> attachments.add(newItem); sheetType = null; isAttachmentsExpanded = true },
                            onUpdateItem = { updatedItem ->
                                val index = attachments.indexOfFirst { it.id == updatedItem.id }
                                if (index != -1) attachments[index] = updatedItem
                                sheetType = null
                            }
                        )
                        SheetType.Sections -> SectionsSheetContent(
                            isProfessional = isProviderProfessional,
                            showArticles = showArticlesSection,
                            showServices = showServicesSection,
                            showProfessionalFees = showProfessionalFeesSection,
                            showMisc = showMiscSection,
                            showTaxes = showTaxesSection,
                            showAttachments = showAttachmentsSection,
                            onShowArticlesChange = { showArticlesSection = it },
                            onShowServicesChange = { showServicesSection = it },
                            onShowProfessionalFeesChange = { showProfessionalFeesSection = it },
                            onShowMiscChange = { showMiscSection = it },
                            onShowTaxesChange = { showTaxesSection = it },
                            onShowAttachmentsChange = { showAttachmentsSection = it }
                        )
                        SheetType.ClientPicker -> ClientPickerSheetContent(
                            clientes = clientes,
                            selectedClienteId = selectedClienteId,
                            onSelectCliente = { c ->
                                selectedClienteId = c.id
                                clienteNombre = c.nombre
                                clienteEmail = c.email.orEmpty()
                                clienteTelefono = c.telefono.orEmpty()
                                clienteDireccion = c.direccion.orEmpty()
                                sheetType = null
                            },
                            onClose = { sheetType = null }
                        )
                        null -> {}
                    }
                    }
                }
            }
            } // end inner Box
        }
    }

    // IIBB Dialog
    if (showIIBBDialog) {
        val colors = getPrestadorColors()
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
                        isTaxesExpanded = true
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

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun CrearPresupuestoPrestadorScreenPreview() {
    MaterialTheme {
        CrearPresupuestoPrestadorScreen()
    }
}