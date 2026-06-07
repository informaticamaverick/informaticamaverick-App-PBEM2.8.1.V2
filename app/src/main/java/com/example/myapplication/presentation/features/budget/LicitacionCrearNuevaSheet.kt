package com.example.myapplication.presentation.features.budget

import com.example.myapplication.core.domain.model.AddressUnico
import com.example.myapplication.presentation.features.home.UbicacionClimaViewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.core.data.local.entity.UserEntity
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.designsystem.components.*
import com.example.myapplication.core.notifications.NotificationHelper
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.global.BeBrainViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState

/**
 * ==========================================================================================
 * --- 🏗️ COMPONENTE: CREAR LICITACIÓN SHEET (MAVERICK ELITE M3) ---
 * ==========================================================================================
 * Panel premium reestructurado bajo estándares Material Design 3 y Protocolo SSOT.
 * Optimizado para flujo de datos bidireccional entre el Cerebro (BeBrain) y el Obrero (Budget).
 */
@Composable
fun CrearLicitacionSheet(
    isVisible: Boolean,
    onClose: () -> Unit,
    onSuccess: () -> Unit = onClose,
    onAnimationFinished: () -> Unit = {},
    budgetViewModel: BudgetViewModel = hiltViewModel(),
    beViewModel: BeBrainViewModel = hiltViewModel(),
    ubicacionViewModel: UbicacionClimaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    // --- SUSCRIPCIÓN AL CEREBRO (SSOT) ---
    val userFromBrain by beViewModel.userState.collectAsStateWithLifecycle()
    val activeAddress by beViewModel.activeAddress.collectAsStateWithLifecycle()
    val activeName by beViewModel.activeProfileName.collectAsStateWithLifecycle()
    val activePhoto by beViewModel.activeProfilePhoto.collectAsStateWithLifecycle()
    val allCategories by beViewModel.allCategories.collectAsStateWithLifecycle()
    val isGpsEnabled by ubicacionViewModel.isGpsEnabled.collectAsStateWithLifecycle()
    val selectedProfileId by beViewModel.selectedProfileId.collectAsStateWithLifecycle()

    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    // 🔥 SINCRONIZACIÓN DE CONTEXTO: Actualizamos el estado del Cerebro para ayuda contextual.
    LaunchedEffect(isVisible) {
        if (isVisible) {
            beViewModel.onRouteChanged("crear_licitacion")
        }
        beViewModel.setSheetVisible(isVisible)
    }

    fun handleClose() {
        if (hasUnsavedChanges) {
            showExitConfirmDialog = true
        } else {
            onClose()
        }
    }

    SheetEmergenteVertical(
        isVisible = isVisible,
        onClose = { handleClose() },
        title = "NUEVA LICITACIÓN",
        helperText = "Concurso Público Elite",
        emoji = "⚖️",
        topOffset = 40.dp, // 🔥 M3: Casi pantalla completa para mayor área de foco
        showActions = true,
        actions = {
            SheetActionButton(icon = "❓", label = "AYUDA", onClick = { /* Ayuda contextual */ })
        },
        isDraggable = true,
        initialAnchorIsFull = true,
        isScrollable = false, // Manejamos scroll interno para optimización IME
        onAnimationFinished = onAnimationFinished
    ) {
        // --- SECCIÓN: IDENTIDAD Y ORIGEN (Inyectado en la "Cabecera" de contenido) ---
        var showUserPopup by remember { mutableStateOf(false) }
        var showLocationPopup by remember { mutableStateOf(false) }

        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                MoldePremiumContextCard(
                    user = userFromBrain,
                    activeProfileName = activeName,
                    activeProfilePhoto = activePhoto,
                    mainAddress = (activeAddress?.streetAndNumber ?: "SCANNING...").uppercase(),
                    localityInfo = (activeAddress?.localidad ?: "PROCESANDO...").uppercase(),
                    description = activeAddress?.let { addr ->
                        if (addr.ownerName?.isNotBlank() == true && addr.ownerName != "Mi Ubicación") {
                            buildString {
                                append(addr.ownerName)
                                if (addr.label.isNotBlank()) append(" - ${addr.label}")
                            }
                        } else if (addr.id == "gps_current") "Mi Ubicación" else "ORIGEN TÁCTICO"
                    },
                    isGpsActive = activeAddress?.id == "gps_current",
                    onUserClick = { showUserPopup = true },
                    onLocationClick = { showLocationPopup = true },
                    onGpsToggle = { ubicacionViewModel.toggleGps(context) }
                )
            }
            
            DepthDividerHorizontal(modifier = Modifier.padding(horizontal = 16.dp))

            // --- SECCIÓN: ORQUESTACIÓN DE CONTENIDO ---
            CrearLicitacionContent(
                userState = userFromBrain,
                activeAddressFromBrain = activeAddress,
                isGpsEnabled = isGpsEnabled,
                onGpsToggle = { ubicacionViewModel.toggleGps(context) },
                onRefreshLocation = { ubicacionViewModel.ejecutarCalculoUbicacionGps(context) },
                allCategories = allCategories,
                onCreateTender = { title, desc, cat, start, end, visit, pay, guar, doc, loc, imgs ->
                    budgetViewModel.createTender(title, desc, cat, start, end, visit, pay, guar, doc, loc, imgs.map { it.toString() })
                },
                onSuccess = onSuccess,
                onHasChangesChanged = { changed -> hasUnsavedChanges = changed },
                showUserPopup = showUserPopup,
                onUserPopupDismiss = { showUserPopup = false },
                showLocationPopup = showLocationPopup,
                onLocationPopupDismiss = { showLocationPopup = false },
                selectedProfileId = selectedProfileId,
                onProfileSelected = { beViewModel.selectProfile(it) },
                onAddressSelected = { beViewModel.selectAddress(it) }
            )
        }
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text("¿DESCARTAR CAMBIOS?", style = CyberTypography.TitleTech.copy(fontSize = 18.sp)) },
            text = { Text("Se perderá la información cargada en este concurso público.", style = CyberTypography.BodyCyber) },
            confirmButton = {
                TextButton(onClick = { 
                    showExitConfirmDialog = false
                    onClose() 
                }) {
                    Text("SÍ, DESCARTAR", color = MaverickColors.ErrorRed, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text("CONTINUAR CARGANDO", color = Color.White)
                }
            },
            containerColor = Color(0xFF121418),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}

@Composable
fun CrearLicitacionContent(
    userState: UserEntity?,
    activeAddressFromBrain: AddressUnico?,
    isGpsEnabled: Boolean = true,
    onGpsToggle: () -> Unit = {},
    onRefreshLocation: () -> Unit = {},
    allCategories: List<CategoryEntity>,
    onCreateTender: (String, String, String, Long, Long, Boolean, Boolean, Boolean, Boolean, AddressUnico?, List<Uri>) -> Unit,
    onSuccess: () -> Unit,
    onHasChangesChanged: (Boolean) -> Unit = {},
    showUserPopup: Boolean = false,
    onUserPopupDismiss: () -> Unit = {},
    showLocationPopup: Boolean = false,
    onLocationPopupDismiss: () -> Unit = {},
    selectedProfileId: String? = null,
    onProfileSelected: (String?) -> Unit = {},
    onAddressSelected: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val notificationHelper = remember { NotificationHelper(context) }
    
    // --- ESTADOS LOCALES (Formulario M3) ---
    var titleInput by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var categorySearch by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var selectedLocation by remember { mutableStateOf(activeAddressFromBrain) }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Fechas del Proyecto
    val calendar = Calendar.getInstance()
    var startDate by remember { mutableStateOf(calendar.time) }
    var endDate by remember { mutableStateOf(Date(calendar.timeInMillis + TimeUnit.DAYS.toMillis(7))) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Estados para Popups interactivos
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Sincronización de ubicación desde el Brain
    LaunchedEffect(activeAddressFromBrain) { activeAddressFromBrain?.let { selectedLocation = it } }

    // Detección de Cambios (Para protección al cerrar)
    LaunchedEffect(titleInput, description, categorySearch, selectedImages) {
        val hasChanges = titleInput.isNotBlank() || description.isNotBlank() || categorySearch.isNotBlank() || selectedImages.isNotEmpty()
        onHasChangesChanged(hasChanges)
    }
    
    // Cláusulas Elite
    var requiresVisit by remember { mutableStateOf(false) }
    var requiresPaymentMethod by remember { mutableStateOf(false) }
    var requiresWorkGuarantee by remember { mutableStateOf(false) }
    var requiresProviderDoc by remember { mutableStateOf(false) }

    val isFormValid = remember(titleInput, selectedCategory, categorySearch, selectedLocation, userState) {
        titleInput.isNotBlank() && 
        (selectedCategory != null || categorySearch.isNotBlank()) && 
        selectedLocation != null && 
        selectedLocation?.codigoPostal?.isNotBlank() == true &&
        userState != null
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris -> 
        selectedImages = (selectedImages + uris).distinct()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(24.dp))

            // === SECCIÓN 1: DEFINICIÓN DEL REQUERIMIENTO ===
            ModernSectionHeader(title = "DEFINICIÓN TÉCNICA", icon = Icons.Default.SettingsInputComponent)
            
            StandardTextFieldM3(
                value = titleInput,
                onValueChange = { titleInput = it },
                label = "TÍTULO DEL PROYECTO",
                placeholder = "Ej: Pintura de Fachada Completa",
                modifier = Modifier.padding(vertical = 4.dp).height(52.dp),
                shape = RoundedCornerShape(2.dp)
            )

            CategorySearchField(
                searchQuery = categorySearch,
                onSearchChange = { categorySearch = it },
                selectedCategory = selectedCategory,
                allCategories = allCategories,
                onCategorySelected = { 
                    selectedCategory = it
                    categorySearch = it.name
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            )

            Spacer(Modifier.height(24.dp))

            // === SECCIÓN 2: CRONOGRAMA Y TIEMPOS ===
            ModernSectionHeader(title = "CRONOGRAMA", icon = Icons.Default.Event)
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CompactDateField(
                    label = "INICIO",
                    value = dateFormatter.format(startDate),
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.weight(1f)
                )
                CompactDateField(
                    label = "CIERRE",
                    value = dateFormatter.format(endDate),
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // === SECCIÓN 3: ESPECIFICACIONES ===
            ModernSectionHeader(title = "ESPECIFICACIONES", icon = Icons.Default.Description)
            
            StandardTextFieldM3(
                value = description,
                onValueChange = { description = it },
                label = "MEMORIA DESCRIPTIVA",
                placeholder = "Describe materiales, medidas, condiciones...",
                singleLine = false,
                modifier = Modifier.heightIn(min = 100.dp).padding(vertical = 4.dp),
                shape = RoundedCornerShape(2.dp)
            )

            // Evidencia Visual Compacta
            MultimediaSelector(
                uris = selectedImages,
                onAddImages = { galleryLauncher.launch("image/*") },
                onRemoveImage = { uri -> selectedImages = selectedImages.filter { it != uri } }
            )

            Spacer(Modifier.height(24.dp))

            // === SECCIÓN 4: CLÁUSULAS ELITE ===
            ModernSectionHeader(title = "CLÁUSULAS DE SEGURIDAD", icon = Icons.Default.Shield)
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.02f))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(2.dp))
            ) {
                CompactSwitchItem(title = "Visita Técnica", checked = requiresVisit, onCheckedChange = { requiresVisit = it })
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
                CompactSwitchItem(title = "Acuerdo de Pago", checked = requiresPaymentMethod, onCheckedChange = { requiresPaymentMethod = it })
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
                CompactSwitchItem(title = "Garantía Post-Obra", checked = requiresWorkGuarantee, onCheckedChange = { requiresWorkGuarantee = it })
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
                CompactSwitchItem(title = "Cumplimiento Legal", checked = requiresProviderDoc, onCheckedChange = { requiresProviderDoc = it })
            }

            Spacer(Modifier.height(40.dp))

            // === BOTÓN DE LANZAMIENTO ===
            Button(
                onClick = {
                    val catName = selectedCategory?.name ?: categorySearch
                    onCreateTender(titleInput, description, catName, startDate.time, endDate.time, requiresVisit, requiresPaymentMethod, requiresWorkGuarantee, requiresProviderDoc, selectedLocation, selectedImages)
                    
                    val locality = selectedLocation?.localidad ?: "tu zona"
                    notificationHelper.showNotification("🚀 LICITACIÓN EN VIVO", "Notificando a profesionales en $locality")
                    onSuccess()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaverickColors.ElectricCyan,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.White.copy(alpha = 0.05f),
                    disabledContentColor = Color.White.copy(alpha = 0.2f)
                )
            ) {
                Text("PUBLICAR CONCURSO PÚBLICO", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
    }

    // ==========================================
    // --- DIÁLOGOS Y POPUPS (Sincronizados) ---
    // ==========================================
    if (showUserPopup && userState != null) {
        ProfileDialog(
            show = showUserPopup,
            user = userState.toDomain(),
            isPersonalProfile = selectedProfileId == null,
            selectedProfileId = selectedProfileId,
            onProfileSelected = { onProfileSelected(it) },
            navController = androidx.navigation.compose.rememberNavController(),
            onLogout = { onUserPopupDismiss() },
            onDismiss = { onUserPopupDismiss() }
        )
    }

    if (showLocationPopup) {
        LocationDialog(
            show = showLocationPopup,
            availableAddresses = userState?.personalAddresses ?: emptyList(),
            activeAddress = selectedLocation,
            isGpsSystemEnabled = isGpsEnabled,
            onRefresh = { onRefreshLocation() },
            onGpsToggle = onGpsToggle,
            onLocationSelected = {
                onAddressSelected(it.id)
                onLocationPopupDismiss()
            },
            onDismiss = { onLocationPopupDismiss() }
        )
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate.time)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { startDate = Date(it) }
                    showStartDatePicker = false
                }) { Text("ACEPTAR") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("CANCELAR") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate.time)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { endDate = Date(it) }
                    showEndDatePicker = false
                }) { Text("ACEPTAR") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("CANCELAR") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * COMPONENTE: Cabecera de Sección Moderna M3
 */
@Composable
fun ModernSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
    ) {
        Icon(
            icon, 
            null, 
            tint = Color.White.copy(alpha = 0.6f), 
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = title.uppercase(), 
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color.White,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        )
    }
}

/**
 * COMPONENTE: Selector de Fecha Compacto
 */
@Composable
fun CompactDateField(label: String, value: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarToday, null, tint = Color.White, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(8.dp))
            Text(value, style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * COMPONENTE: Selector Multimedia Compacto
 */
@Composable
fun MultimediaSelector(
    uris: List<Uri>,
    onAddImages: () -> Unit,
    onRemoveImage: (Uri) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text("EVIDENCIA MULTIMEDIA", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color.Gray)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                    .clickable { onAddImages() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AddAPhoto, null, tint = MaverickColors.ElectricCyan, modifier = Modifier.size(18.dp))
            }
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(uris) { uri ->
                    Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(2.dp))) {
                        AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        IconButton(
                            onClick = { onRemoveImage(uri) },
                            modifier = Modifier.align(Alignment.TopEnd).size(18.dp).background(Color.Black.copy(0.6f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(10.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * COMPONENTE: Búsqueda de Categoría con Sugerencias (Moderno)
 */
@Composable
fun CategorySearchField(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: CategoryEntity?,
    allCategories: List<CategoryEntity>,
    onCategorySelected: (CategoryEntity) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val filteredCategories = remember(searchQuery, allCategories) {
        if (searchQuery.length < 2) emptyList()
        else allCategories.filter { it.name.contains(searchQuery, ignoreCase = true) }.take(5)
    }

    Column {
        StandardTextFieldM3(
            value = searchQuery,
            onValueChange = { 
                onSearchChange(it)
                isExpanded = it.isNotEmpty()
            },
            label = "RUBRO / CATEGORÍA",
            placeholder = "¿Qué profesional necesitas?",
            leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp)) },
            trailingIcon = { 
                if (selectedCategory != null) Icon(Icons.Default.CheckCircle, null, tint = MaverickColors.SuccessGreen, modifier = Modifier.size(16.dp))
                else if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }, modifier = Modifier.size(16.dp)) { Icon(Icons.Default.Clear, null) }
                }
            },
            modifier = Modifier.padding(vertical = 4.dp).height(52.dp),
            shape = RoundedCornerShape(2.dp)
        )

        AnimatedVisibility(
            visible = isExpanded && filteredCategories.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1A1D23),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column {
                    filteredCategories.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    onCategorySelected(category)
                                    isExpanded = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category.icon, modifier = Modifier.padding(end = 12.dp))
                            Text(category.name, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

/**
 * COMPONENTE: Switch Item Compacto M3
 */
@Composable
fun CompactSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodySmall, color = Color.White, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = MaverickColors.ElectricCyan,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
            ),
            modifier = Modifier.scale(0.7f)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121418)
@Composable
fun PreviewCrearLicitacionRestructured() {
    val mockAddress = AddressUnico(
        id = "gps_current",
        ownerName = "Juan",
        label = "GPS",
        calle = "AV. SANTA FE",
        numero = "1234",
        localidad = "CABA",
        codigoPostal = "1425",
        isCompany = false,
        latitude = 0.0,
        longitude = 0.0
    )
    MyApplicationTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color(0xFF121418))
        ) {
            CrearLicitacionContent(
                userState = UserEntity(id = "1", email = "test@pixel.com", displayName = "ODETTE", photoUrl = null),
                activeAddressFromBrain = mockAddress,
                allCategories = emptyList(),
                onCreateTender = { _, _, _, _, _, _, _, _, _, _, _ -> },
                onSuccess = {}
            )
        }
    }
}











