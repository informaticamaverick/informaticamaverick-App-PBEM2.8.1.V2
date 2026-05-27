package com.example.myapplication.presentation.features.budget

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
import com.example.myapplication.presentation.components.UserProfilePopup
import com.example.myapplication.presentation.components.LocationPopup
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
) {
    // --- SUSCRIPCIÓN AL CEREBRO (SSOT) ---
    val userFromBrain by beViewModel.userState.collectAsStateWithLifecycle()
    val activeAddress by beViewModel.activeAddress.collectAsStateWithLifecycle()
    val allCategories by beViewModel.allCategories.collectAsStateWithLifecycle()

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
            SheetActionButton(icon = "📋", label = "REGLAS", onClick = { /* Protocolo Maverick */ })
        },
        isDraggable = true,
        initialAnchorIsFull = true,
        isScrollable = false, // Manejamos scroll interno para optimización IME
        onAnimationFinished = onAnimationFinished
    ) {
        // --- SECCIÓN: ORQUESTACIÓN DE CONTENIDO ---
        CrearLicitacionContent(
            userState = userFromBrain,
            activeAddressFromBrain = activeAddress,
            allCategories = allCategories,
            onCreateTender = { title, desc, cat, start, end, visit, pay, guar, doc, loc, imgs ->
                budgetViewModel.createTender(title, desc, cat, start, end, visit, pay, guar, doc, loc, imgs.map { it.toString() })
            },
            onSuccess = onSuccess,
            onHasChangesChanged = { changed -> hasUnsavedChanges = changed }
        )
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

/**
 * UI DE CONTENIDO: Implementación Stateless con Anatomía M3.
 * Estructurada en secciones lógicas para guiar al usuario en la creación.
 */
@Composable
fun CrearLicitacionContent(
    userState: UserEntity?,
    activeAddressFromBrain: AddressInfo?,
    allCategories: List<CategoryEntity>,
    onCreateTender: (String, String, String, Long, Long, Boolean, Boolean, Boolean, Boolean, AddressInfo?, List<Uri>) -> Unit,
    onSuccess: () -> Unit,
    onHasChangesChanged: (Boolean) -> Unit = {}
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
    var showUserPopup by remember { mutableStateOf(false) }
    var showLocationPopup by remember { mutableStateOf(false) }
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
        selectedLocation?.postalCode?.isNotBlank() == true &&
        userState != null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp) // Más aire para Bento
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))

        // === SECCIÓN 1: IDENTIDAD Y ORIGEN (Compacto Pixel Style) ===
        SectionHeaderM3(title = "IDENTIDAD Y ORIGEN", icon = Icons.Default.VerifiedUser, accentColor = MaterialTheme.colorScheme.primary, emoji = "🛡️")
        
        Row(
            modifier = Modifier.fillMaxWidth().height(85.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // SLOT A: USUARIO (Normal / Clean)
            userState?.let { user ->
                StandardDisplayFieldM3(
                    label = "SOLICITANTE",
                    value = (user.displayName.ifBlank { "PERFIL" }).uppercase(),
                    modifier = Modifier.weight(1f).fillMaxHeight().clickable { showUserPopup = true },
                    icon = Icons.Default.Person
                )
            }

            // SLOT B: UBICACIÓN (Normal / Clean)
            val addressText = selectedLocation?.streetAndNumber ?: "No definida"
            
            StandardDisplayFieldM3(
                label = "ORIGEN TÁCTICO",
                value = addressText.uppercase(),
                modifier = Modifier.weight(1.5f).fillMaxHeight().clickable { showLocationPopup = true },
                icon = Icons.Default.MyLocation
            )
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
        Spacer(Modifier.height(24.dp))

        // === SECCIÓN 2: CRONOGRAMA DEL PROYECTO (Normal / Clean) ===
        SectionHeaderM3(title = "CRONOGRAMA ESTIMADO", icon = Icons.Default.Event, accentColor = MaterialTheme.colorScheme.primary, emoji = "📅")
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // FECHA INICIO
            StandardTextFieldM3(
                value = dateFormatter.format(startDate),
                onValueChange = {},
                readOnly = true,
                label = "INICIO",
                modifier = Modifier.weight(1f).clickable { showStartDatePicker = true },
                leadingIcon = { 
                    Icon(Icons.Default.CalendarToday, null)
                }
            )

            // FECHA CIERRE
            StandardTextFieldM3(
                value = dateFormatter.format(endDate),
                onValueChange = {},
                readOnly = true,
                label = "CIERRE CONCURSO",
                modifier = Modifier.weight(1f).clickable { showEndDatePicker = true },
                leadingIcon = {
                    Icon(Icons.Default.EventAvailable, null)
                }
            )
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
        Spacer(Modifier.height(24.dp))

        // === SECCIÓN 3: DEFINICIÓN DEL PROYECTO (M3 Standard) ===
        SectionHeaderM3(title = "CLASIFICACIÓN TÉCNICA", icon = Icons.Default.SettingsInputComponent, accentColor = MaterialTheme.colorScheme.primary, emoji = "⚛️")
        
        StandardTextFieldM3(
            value = titleInput,
            onValueChange = { titleInput = it },
            label = "TÍTULO DEL REQUERIMIENTO",
            placeholder = "Ej: Pintura de Fachada Completa",
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Assignment, null) },
            helperText = "Asigna un nombre descriptivo a tu concurso.",
            trailingIcon = if (titleInput.isNotEmpty()) {
                { IconButton(onClick = { titleInput = "" }) { Icon(Icons.Default.Clear, "Limpiar") } }
            } else null
        )

        Spacer(Modifier.height(16.dp))

        // 🔥 MEJORA: BÚSQUEDA DE CATEGORÍA ELITE
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
        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
        Spacer(Modifier.height(24.dp))

        // === SECCIÓN 4: ESPECIFICACIONES TÉCNICAS (M3 Standard) ===
        SectionHeaderM3(title = "MEMORIA DESCRIPTIVA", icon = Icons.Default.Description, accentColor = MaterialTheme.colorScheme.primary, emoji = "📄")
        
        StandardTextFieldM3(
            value = description,
            onValueChange = { description = it },
            label = "DETALLE TÉCNICO",
            placeholder = "Describe materiales, medidas, fallas o condiciones...",
            singleLine = false,
            modifier = Modifier.heightIn(min = 120.dp),
            helperText = "Cuanto más detalle proporciones, mejores serán los presupuestos.",
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null) }
        )

        Spacer(Modifier.height(16.dp))

        // Evidencia Visual M3 Elite
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PhotoLibrary, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("EVIDENCIA MULTIMEDIA", style = CyberTypography.MonospaceData.copy(fontSize = 10.sp, color = Color.Gray))
        }
        Spacer(Modifier.height(12.dp))
        
        val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris -> 
            selectedImages = (selectedImages + uris).distinct()
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AddAPhoto, null, tint = MaverickColors.GeminiAccent)
            }
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(selectedImages) { uri ->
                    Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp))) {
                        AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        IconButton(
                            onClick = { selectedImages = selectedImages.filter { it != uri } },
                            modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.Black.copy(0.6f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
        Spacer(Modifier.height(24.dp))

        // === SECCIÓN 5: CONDICIONES ELITE (M3 Standard) ===
        SectionHeaderM3(title = "CLÁUSULAS DE SEGURIDAD", icon = Icons.Default.Shield, accentColor = MaterialTheme.colorScheme.primary, emoji = "🛡️")
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                M3SwitchItem(title = "Visita Técnica", subtitle = "Exigir inspección previa en sitio", checked = requiresVisit, onCheckedChange = { requiresVisit = it })
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                M3SwitchItem(title = "Acuerdo de Pago", subtitle = "Definir condiciones de cobro/seña", checked = requiresPaymentMethod, onCheckedChange = { requiresPaymentMethod = it })
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                M3SwitchItem(title = "Garantía Post-Obra", subtitle = "Certificado de respaldo técnico", checked = requiresWorkGuarantee, onCheckedChange = { requiresWorkGuarantee = it })
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                M3SwitchItem(title = "Cumplimiento Legal", subtitle = "Solicitar Seguros/ART/AFIP", checked = requiresProviderDoc, onCheckedChange = { requiresProviderDoc = it })
            }
        }

        Spacer(Modifier.height(40.dp))

        // === SECCIÓN 6: ACCIÓN DE LANZAMIENTO (M3 Standard Button) ===
        Button(
            onClick = {
                val catName = selectedCategory?.name ?: categorySearch
                onCreateTender(titleInput, description, catName, startDate.time, endDate.time, requiresVisit, requiresPaymentMethod, requiresWorkGuarantee, requiresProviderDoc, selectedLocation, selectedImages)
                
                val locality = selectedLocation?.locality ?: "tu zona"
                notificationHelper.showNotification("🚀 LICITACIÓN EN VIVO", "Notificando a profesionales en $locality")
                onSuccess()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = isFormValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.RocketLaunch, null)
                Spacer(Modifier.width(12.dp))
                Text(
                    "PUBLICAR CONCURSO PÚBLICO", 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
            }
        }
        
        Spacer(Modifier.height(32.dp))
    }

    // ==========================================
    // --- DIÁLOGOS Y POPUPS (Sincronizados) ---
    // ==========================================
    if (showUserPopup && userState != null) {
        UserProfilePopup(
            user = userState,
            isScrollable = false,
            onClose = { showUserPopup = false },
            onLogout = { showUserPopup = false }, // En este contexto no debería desloguear
            onProfileClick = { showUserPopup = false }
        )
    }

    if (showLocationPopup) {
        LocationPopup(
            user = userState,
            isScrollable = false,
            onClose = { showLocationPopup = false },
            onRefresh = { /* Ya se maneja globalmente */ },
            onLocationSelected = { 
                selectedLocation = it
                showLocationPopup = false 
            },
            activeAddress = selectedLocation ?: AddressInfo(
                id = "searching",
                companyOrUserName = "BUSCANDO...",
                branchName = "UBICACIÓN",
                streetAndNumber = "SCANNING...",
                locality = "PROCESANDO...",
                postalCode = "",
                isCompany = false,
                lat = 0.0,
                lng = 0.0
            )
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
 * COMPONENTE: Cabecera de Sección M3
 */
@Composable
fun SectionHeaderM3(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accentColor: Color, emoji: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.1f))
                .border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (emoji != null) {
                Text(emoji, fontSize = 12.sp)
            } else {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = title.uppercase(), 
            style = MaterialTheme.typography.labelMedium.copy(
                color = accentColor, 
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )
        )
    }
}

/**
 * COMPONENTE: Búsqueda de Categoría con Sugerencias
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
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = { 
                if (selectedCategory != null) Icon(Icons.Default.CheckCircle, null, tint = MaverickColors.SuccessGreen)
                else if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) { Icon(Icons.Default.Clear, null) }
                }
            },
            helperText = "Selecciona el rubro técnico para notificar a los prestadores correctos."
        )

        AnimatedVisibility(
            visible = isExpanded && filteredCategories.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaverickColors.AsSidebarBg,
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
                            Text(category.name, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

/**
 * COMPONENTE: Switch Estilizado M3
 */
@Composable
fun M3SwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = MaverickColors.ElectricCyan,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121418)
@Composable
fun PreviewCrearLicitacionRestructured() {
    val mockAddress = AddressInfo(
        id = "gps_current",
        companyOrUserName = "Juan",
        branchName = "GPS",
        streetAndNumber = "AV. SANTA FE 1234",
        locality = "CABA",
        postalCode = "1425",
        isCompany = false,
        lat = 0.0,
        lng = 0.0
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










