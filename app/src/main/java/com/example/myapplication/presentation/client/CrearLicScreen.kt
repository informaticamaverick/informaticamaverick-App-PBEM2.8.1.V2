package com.example.myapplication.presentation.client

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.AddressClient
import com.example.myapplication.presentation.components.GoogleVerticalInterstitialAd
import com.example.myapplication.presentation.profile.ProfileViewModel
import com.example.myapplication.presentation.util.NotificationHelper
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * PANTALLA DE CREACIÓN DE LICITACIÓN (VERSIÓN PREMIUM)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearLicScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit = onBack, // 🔥 Nuevo callback para éxito
    profileViewModel: ProfileViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    budgetViewModel: BudgetViewModel = hiltViewModel(),
    beViewModel: BeBrainViewModel = hiltViewModel() // 🔥 Agregamos el Cerebro de Be
) {
    val userState by profileViewModel.userState.collectAsState()
    //val allCategories by categoryViewModel.categories.collectAsState()
    val categories by categoryViewModel.allCategories.collectAsStateWithLifecycle()

    CrearLicContent(
        onBack = onBack,
        onSuccess = onSuccess,
        userState = userState,
        allCategories = categories,
        beViewModel = beViewModel, // 🔥 Pasamos el cerebro al contenido
        onCreateTender = { title, description, category, startDate, endDate, requiresVisit, requiresPayment, requiresGuarantee, requiresDoc, location, images ->
            budgetViewModel.createTender(
                title = title,
                description = description,
                category = category,
                startDate = startDate,
                endDate = endDate,
                requiresVisit = requiresVisit,
                requiresPaymentMethod = requiresPayment,
                requiresWorkGuarantee = requiresGuarantee,
                requiresProviderDoc = requiresDoc,
                location = location,
                imageUrls = images.map { it.toString() }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearLicContent(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    userState: UserEntity?,
    allCategories: List<CategoryEntity>,
    beViewModel: BeBrainViewModel, // 🔥 Recibimos el cerebro
    onCreateTender: (
        title: String, 
        description: String, 
        category: String, 
        startDate: Long,
        endDate: Long,
        requiresVisit: Boolean,
        requiresPayment: Boolean,
        requiresGuarantee: Boolean,
        requiresDoc: Boolean,
        location: LocationOption?,
        images: List<Uri>
    ) -> Unit
) {
    // 🔥 CENTRALIZACIÓN: Obtenemos datos recordados por Be
    val userFromBrain by beViewModel.userState.collectAsStateWithLifecycle()
    val locationFromBrain by beViewModel.selectedLocation.collectAsStateWithLifecycle()

    CrearLicUIContent(
        onBack = onBack,
        onSuccess = onSuccess,
        userState = userState,
        allCategories = allCategories,
        userFromBrain = userFromBrain,
        locationFromBrain = locationFromBrain,
        onCreateTender = onCreateTender
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearLicUIContent(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    userState: UserEntity?,
    allCategories: List<CategoryEntity>,
    userFromBrain: UserEntity?,
    locationFromBrain: LocationOption?,
    onCreateTender: (
        title: String, 
        description: String, 
        category: String, 
        startDate: Long,
        endDate: Long,
        requiresVisit: Boolean,
        requiresPayment: Boolean,
        requiresGuarantee: Boolean,
        requiresDoc: Boolean,
        location: LocationOption?,
        images: List<Uri>
    ) -> Unit
) {
    // --- ESTADOS DEL FORMULARIO ---
    var tenderId by remember { mutableStateOf(UUID.randomUUID().toString().take(8).uppercase()) }
    var titleInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("") }
    var showCategorySuggestions by remember { mutableStateOf(false) }
    
    // El usuario final será el del cerebro o el inyectado localmente
    val finalUser = userFromBrain ?: userState

    // Estado local para la ubicación, inicializado con lo que el Asistente Be "recuerda"
    var selectedLocation by remember { mutableStateOf<LocationOption?>(locationFromBrain) }

    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }
    val calendar = Calendar.getInstance()
    var startDate by remember { mutableStateOf(calendar.time) }
    var endDate by remember { mutableStateOf(Date(calendar.timeInMillis + TimeUnit.DAYS.toMillis(1))) }
    var description by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // --- NUEVOS ESTADOS DE SWITCHES ---
    var requiresVisit by remember { mutableStateOf(false) }
    var requiresPaymentMethod by remember { mutableStateOf(false) }
    var requiresWorkGuarantee by remember { mutableStateOf(false) }
    var requiresProviderDoc by remember { mutableStateOf(false) }

    // --- ESTADOS PARA ACORDEONES ---
    var expandedTitle by remember { mutableStateOf(true) }
    var expandedCategory by remember { mutableStateOf(true) }
    var expandedDates by remember { mutableStateOf(true) }
    var expandedDetails by remember { mutableStateOf(true) }
    var expandedClauses by remember { mutableStateOf(true) }

    // --- DIÁLOGO DE INFORMACIÓN ---
    var showInfoDialog by remember { mutableStateOf(false) }
    var infoDialogTitle by remember { mutableStateOf("") }
    var infoDialogText by remember { mutableStateOf("") }

    // --- ESTADO PARA ANUNCIO ---
    var showAd by remember { mutableStateOf(false) }

    val showInfo = { title: String, text: String ->
        infoDialogTitle = title
        infoDialogText = text
        showInfoDialog = true
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> selectedImages = selectedImages + uris }

    // --- CÁMARA LAUNCHER Y PERMISOS (REGLA DE ORO: Captura a resolución completa) ---
    val tempPhotoUri = remember {
        val photoFile = java.io.File(context.cacheDir, "tender_photo_${System.currentTimeMillis()}.jpg")
        androidx.core.content.FileProvider.getUriForFile(context, "com.example.myapplication.provider", photoFile)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImages = selectedImages + tempPhotoUri
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(tempPhotoUri)
        } else {
            Toast.makeText(context, "Se requiere permiso de cámara para tomar fotos", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleCameraClick() {
        val hasPermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            cameraLauncher.launch(tempPhotoUri)
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    // 🔥 SINCRONIZACIÓN: Si Be tiene una ubicación guardada, la cargamos automáticamente
    LaunchedEffect(locationFromBrain) {
        if (selectedLocation == null && locationFromBrain != null) {
            selectedLocation = locationFromBrain
        }
    }

    // Fallback: Si no hay ubicación en Be, intentamos cargar la dirección personal del usuario
    LaunchedEffect(finalUser) {
        if (selectedLocation == null && finalUser != null) {
            val user = finalUser
            if (user.personalAddresses.isNotEmpty()) {
                val addr = user.personalAddresses.first()
                selectedLocation = LocationOption.Personal(addr.calle, addr.numero, addr.localidad)
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0A0E14),
        topBar = {
            Column(modifier = Modifier.background(Color.Black)) { // Negro Mate
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📄", fontSize = 18.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "NUEVA LICITACIÓN", 
                                fontWeight = FontWeight.Black, 
                                fontSize = 15.sp, 
                                letterSpacing = 1.sp,
                                color = Color.White
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "ID: $tenderId",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black,
                        titleContentColor = Color.White
                    )
                )
                HorizontalDivider(color = Color.White, thickness = 1.dp) // Borde inferior blanco
            }
        }
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val safePadding = remember(paddingValues, layoutDirection) {
            PaddingValues(
                start = paddingValues.calculateStartPadding(layoutDirection).coerceAtLeast(0.dp),
                top = paddingValues.calculateTopPadding().coerceAtLeast(0.dp),
                end = paddingValues.calculateEndPadding(layoutDirection).coerceAtLeast(0.dp),
                bottom = paddingValues.calculateBottomPadding().coerceAtLeast(0.dp)
            )
        }
        Column(
            modifier = Modifier
                .padding(safePadding)
                .fillMaxSize()
                //.padding(horizontal = 14.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. DATOS DEL USUARIO (Ahora usando finalUser centralizado)
            finalUser?.let { user ->
                CollapsibleHeaderReal(
                    user = user,
                    selectedLocation = selectedLocation,
                    onLocationChange = { selectedLocation = it }
                )
            } ?: Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                // Indicador de carga si el Cerebro aún no tiene los datos
                CircularProgressIndicator(
                    color = Color(0xFF22D3EE),
                    modifier = Modifier.size(24.dp)
                )
            }
            // --- 2. SECCIÓN CON PADDING (TODO EL FORMULARIO) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) // Aquí aplicamos el margen solo al contenido
            ) {


            Spacer(modifier = Modifier.height(16.dp))

            // 1.5 NOMBRE DE LA LICITACIÓN
            PremiumCollapsibleSection(
                title = "NOMBRE DE LICITACIÓN",
                icon = Icons.AutoMirrored.Filled.Label,
                isExpanded = expandedTitle,
                onToggle = { expandedTitle = !expandedTitle },
                onInfoClick = null
            ) {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: Pintura completa de fachada", color = Color.Gray) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF22D3EE),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            }

            // 2. RUBRO DEL SERVICIO
            PremiumCollapsibleSection(
                title = "RUBRO DEL SERVICIO",
                icon = Icons.Default.Search,
                isExpanded = expandedCategory,
                onToggle = { expandedCategory = !expandedCategory },
                onInfoClick = null
            ) {
                Box {
                    OutlinedTextField(
                        value = categoryInput,
                        onValueChange = {
                            categoryInput = it
                            showCategorySuggestions = it.isNotEmpty()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ej: Electricidad, Pintura...", color = Color.Gray) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF22D3EE),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }

                    if (showCategorySuggestions) {
                        val filtered = allCategories.filter {
                            it.name.contains(
                                categoryInput,
                                ignoreCase = true
                            )
                        }
                        if (filtered.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 60.dp)
                                    .zIndex(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                            ) {
                                Column {
                                    filtered.take(5).forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat.name, color = Color.White) },
                                            leadingIcon = { Text(cat.icon) },
                                            onClick = {
                                                categoryInput = cat.name
                                                showCategorySuggestions = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. PLAZOS ESTIMADOS
            PremiumCollapsibleSection(
                title = "PLAZOS ESTIMADOS",
                icon = Icons.Default.DateRange,
                isExpanded = expandedDates,
                onToggle = { expandedDates = !expandedDates },
                onInfoClick = {
                    showInfo(
                        "Plazos Estimados",
                        "Define el período de tiempo en el que los profesionales pueden enviar sus presupuestos y consultas."
                    )
                }
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DatePickerField(
                                label = "INICIO",
                                date = startDate,
                                onClick = {
                                    showDatePicker(
                                        context,
                                        startDate,
                                        minDate = System.currentTimeMillis()
                                    ) { newDate ->
                                        startDate = newDate
                                        if (endDate.before(newDate)) endDate =
                                            Date(newDate.time + TimeUnit.DAYS.toMillis(1))
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            DatePickerField(
                                label = "FIN",
                                date = endDate,
                                onClick = {
                                    val maxDate = startDate.time + TimeUnit.DAYS.toMillis(15)
                                    showDatePicker(
                                        context,
                                        endDate,
                                        minDate = startDate.time,
                                        maxDate = maxDate
                                    ) { newDate -> endDate = newDate }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        val diffDays = TimeUnit.MILLISECONDS.toDays(endDate.time - startDate.time)
                        Text(
                            text = "Duración: $diffDays días (Máx 15)",
                            color = if (diffDays > 15) Color.Red else Color(0xFF22D3EE),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }

            // 4. MEMORIA DESCRIPTIVA
            PremiumCollapsibleSection(
                title = "DETALLES DEL PROYECTO",
                icon = Icons.Default.Edit,
                isExpanded = expandedDetails,
                onToggle = { expandedDetails = !expandedDetails },
                onInfoClick = {
                    showInfo(
                        "Memoria Descriptiva",
                        "Describe el trabajo. Cuanta más información proporciones (medidas, estado actual), presupuestos más precisos recibirás."
                    )
                }
            ) {
                Column {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        placeholder = {
                            Text(
                                "Detalle técnico del trabajo...",
                                color = Color.Gray
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF22D3EE),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                    Spacer(Modifier.height(16.dp))

                    Text(
                        "FOTOS DEL PROYECTO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    if (selectedImages.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(selectedImages) { uri ->
                                Box(Modifier.size(100.dp).clip(RoundedCornerShape(12.dp))) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = {
                                            selectedImages = selectedImages.filter { it != uri }
                                        },
                                        modifier = Modifier.align(Alignment.TopEnd)
                                            .background(Color.Black.copy(0.5f), CircleShape)
                                            .size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFF22D3EE).copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(
                                    0xFF22D3EE
                                )
                            )
                        ) {
                            Icon(Icons.Default.Collections, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("GALERÍA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { handleCameraClick() },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFE91E63).copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(
                                    0xFFE91E63
                                )
                            )
                        ) {
                            Icon(Icons.Default.PhotoCamera, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("CÁMARA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 5. CLÁUSULAS Y REQUISITOS
            PremiumCollapsibleSection(
                title = "CLÁUSULAS Y REQUISITOS",
                icon = Icons.Default.Gavel,
                isExpanded = expandedClauses,
                onToggle = { expandedClauses = !expandedClauses },
                onInfoClick = {
                    showInfo(
                        "Cláusulas y Requisitos",
                        "Establece las condiciones obligatorias para los profesionales. Esto filtrará a los que no cumplan con tus estándares."
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.02f))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                ) {
                    PremiumSwitchItem(
                        title = "¿Requiere visita técnica?",
                        subtitle = "El profesional debe inspeccionar el lugar antes de cotizar.",
                        checked = requiresVisit,
                        onCheckedChange = { requiresVisit = it }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    PremiumSwitchItem(
                        title = "Formas de Pago",
                        subtitle = "Especificar si se requieren opciones de pago en etapas o transferencias.",
                        checked = requiresPaymentMethod,
                        onCheckedChange = { requiresPaymentMethod = it }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    PremiumSwitchItem(
                        title = "Garantías del trabajo",
                        subtitle = "Exigir un período de garantía sobre el servicio terminado.",
                        checked = requiresWorkGuarantee,
                        onCheckedChange = { requiresWorkGuarantee = it }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    PremiumSwitchItem(
                        title = "Solicitar documentación del Prestador",
                        subtitle = "Requerir seguros, ART, constancia de AFIP u otros legales.",
                        checked = requiresProviderDoc,
                        onCheckedChange = { requiresProviderDoc = it }
                    )
                }
            }

                Spacer(Modifier.height(24.dp))

                // BOTÓN PUBLICAR
                Button(
                    onClick = { showAd = true },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22D3EE)),
                    enabled = titleInput.isNotBlank() && categoryInput.isNotBlank() && description.isNotBlank()
                ) {
                    Text(
                        "PUBLICAR LICITACIÓN",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.Black
                    )
                }

                Spacer(Modifier.height(40.dp))
            }
        }
        
        // --- ANUNCIO INTERSTITIAL ---
        GoogleVerticalInterstitialAd(
            show = showAd,
            onDismiss = {
                showAd = false
                
                // 🔥 [VALIDACIÓN DE FLUJO] Ejecutamos la creación real en el ViewModel
                onCreateTender(
                    titleInput,
                    description,
                    categoryInput,
                    startDate.time,
                    endDate.time,
                    requiresVisit,
                    requiresPaymentMethod,
                    requiresWorkGuarantee,
                    requiresProviderDoc,
                    selectedLocation,
                    selectedImages
                )
                
                // 🔥 NOTIFICACIÓN LOCAL (UI Feedback) 🔥
                val locLabel = when (val loc = selectedLocation) {
                    is LocationOption.Personal -> loc.locality
                    is LocationOption.Business -> loc.locality
                    else -> "tu ciudad"
                }
                notificationHelper.showNotification(
                    "🚀 Licitación Enviada con Éxito",
                    "Se notificó a todos los '$categoryInput' en '$locLabel'"
                )

                onSuccess() // 🔥 NAVEGACIÓN A PRESUPUESTOS
            }
        )

        // --- DIÁLOGO DE INFORMACIÓN ---
        if (showInfoDialog) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
               // title = { Text(infoDialogTitle, fontWeight = FontWeight.Bold) },
                //text = { Text(infoDialogText) },
                //confirmButton = { TextButton(onClick = { showInfoDialog = false }) { Text("ENTENDIDO") } },
                containerColor = Color(0xFF1A1C1E),
                titleContentColor = Color.White,
                textContentColor = Color.Gray,
                icon = { Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(36.dp)) },
                title = { Text(infoDialogTitle, fontWeight = FontWeight.Bold) },
                text = { Text(infoDialogText, fontSize = 14.sp, lineHeight = 20.sp) },
                confirmButton = {
                    TextButton(onClick = { showInfoDialog = false }) {
                        Text("ENTENDIDO", color = Color(0xFF22D3EE), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }



/**
 * COMPONENTE REUTILIZABLE: SECCIÓN COLAPSABLE PREMIUM
 */
@Composable
fun PremiumCollapsibleSection(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onInfoClick: (() -> Unit)?,
    content: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF22D3EE), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.width(12.dp))
            HorizontalDivider(
                modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
                color = Color.White.copy(alpha = 0.2f),
                thickness = 0.5.dp
            )
            Spacer(modifier = Modifier.width(12.dp))

            if (onInfoClick != null) {
                IconButton(
                    onClick = { onInfoClick() },
                    modifier = Modifier.size(32.dp).padding(4.dp)
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = "Información",
                        tint = Color(0xFFE91E63)
                    )
                }
            }

            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Expandir",
                tint = Color.Gray,
                modifier = Modifier.rotate(rotation)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                content()
            }
        }
    }
}

/**
 * COMPONENTE REUTILIZABLE: ITEM DE SWITCH
 */
@Composable
fun PremiumSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, color = Color.Gray, fontSize = 11.sp, lineHeight = 14.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = Color(0xFF22D3EE),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}


// ----------- COMPONENTES AUXILIARES -----------

@Composable
fun CollapsibleHeaderReal(user: UserEntity, selectedLocation: LocationOption?, onLocationChange: (LocationOption) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var showAddressSelector by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f)
    val cardGradient = Brush.verticalGradient(listOf(Color.White.copy(0.15f), Color.White.copy(0.03f)))

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(topStart = 0.dp,topEnd = 0.dp,bottomStart = 24.dp,bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.background(cardGradient).padding(horizontal = 10.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(50.dp).clip(CircleShape).border(1.5.dp, Color(0xFF22D3EE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (user.photoUrl != null) AsyncImage(model = user.photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(user.displayName.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text("TITULAR DE LICITACIÓN", color = Color(0xFF22D3EE), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.rotate(rotation))
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(Modifier.height(16.dp))
                    when (val loc = selectedLocation) {
                        is LocationOption.Business -> {
                            val company = user.companies.find { it.name == loc.companyName }
                            InfoLine("Empresa", loc.companyName)
                            InfoLine("Sucursal", loc.branchName)
                            InfoLine("CUIT", company?.cuit ?: "N/A")
                            InfoLine("Dirección", "${loc.address} ${loc.number}")
                            InfoLine("Localidad", loc.locality)
                        }
                        is LocationOption.Personal -> {
                            val addr = user.personalAddresses.find { it.calle == loc.address }
                            InfoLine("Usuario", "${user.name} ${user.lastName}")
                            InfoLine("CUIL/DNI", user.id)
                            InfoLine("Dirección", "${loc.address} ${loc.number}")
                            InfoLine("C.P.", addr?.codigoPostal ?: "-")
                            InfoLine("Localidad", loc.locality)
                        }
                        else -> InfoLine("Ubicación", "No seleccionada")
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { showAddressSelector = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.SwapHoriz, null, tint = Color(0xFF22D3EE))
                        Spacer(Modifier.width(8.dp))
                        Text("CAMBIAR ORIGEN / DIRECCIÓN", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showAddressSelector) {
        AlertDialog(
            onDismissRequest = { showAddressSelector = false },
            containerColor = Color(0xFF1A1C1E),
            title = { Text("Seleccionar Ubicación", color = Color.White) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Personales", color = Color(0xFF22D3EE), fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                    user.personalAddresses.forEach { addr ->
                        ListItem(
                            headlineContent = { Text("${addr.calle} ${addr.numero}", color = Color.White) },
                            supportingContent = { Text(addr.localidad, color = Color.Gray) },
                            modifier = Modifier.clickable {
                                onLocationChange(LocationOption.Personal(addr.calle, addr.numero, addr.localidad))
                                showAddressSelector = false
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                    if (user.companies.isNotEmpty()) {
                        Text("Empresas", color = Color(0xFFE91E63), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                        user.companies.forEach { company ->
                            company.branches.forEach { branch ->
                                ListItem(
                                    headlineContent = { Text("${company.name} - ${branch.name}", color = Color.White) },
                                    supportingContent = { Text("${branch.address.calle} ${branch.address.numero}", color = Color.Gray) },
                                    modifier = Modifier.clickable {
                                        onLocationChange(LocationOption.Business(company.name, branch.name, branch.address.calle, branch.address.numero, branch.address.localidad))
                                        showAddressSelector = false
                                    },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAddressSelector = false }) { Text("CERRAR", color = Color.Gray) } }
        )
    }
}

@Composable
fun InfoLine(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label: ", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
fun DatePickerField(label: String, date: Date, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    Column(modifier = modifier) {
        Text(label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF22D3EE), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(formatter.format(date), color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

private fun showDatePicker(
    context: android.content.Context,
    currentDate: Date,
    minDate: Long? = null,
    maxDate: Long? = null,
    onDateSelected: (Date) -> Unit
) {
    val cal = Calendar.getInstance()
    cal.time = currentDate
    val dialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val result = Calendar.getInstance()
            result.set(year, month, day)
            onDateSelected(result.time)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    )
    minDate?.let { dialog.datePicker.minDate = it }
    maxDate?.let { dialog.datePicker.maxDate = it }
    dialog.show()
}

@Preview(showBackground = true)
@Composable
fun CrearLicScreenPreview() {
    MyApplicationTheme {
        val sampleUser = UserEntity(
            id = "1",
            email = "test@example.com",
            displayName = "Juan Perez",
            name = "Juan",
            lastName = "Perez",
            personalAddresses = listOf(
                AddressClient(calle = "Av. Siempre Viva", numero = "742", localidad = "Springfield")
            )
        )
        val sampleCategories = listOf(
            CategoryEntity(
                name = "Electricidad",
                icon = "⚡",
                //color = 0xFFFAD2E1,
                superCategory = "Hogar",
                isNew = false,
                isNewPrestador = false,
                isAd = false
            ),
            CategoryEntity(
                name = "Plomería",
                icon = "🪠",
               // color = 0xFFD4A5A5,
                superCategory = "Hogar",
                isNew = false,
                isNewPrestador = false,
                isAd = false
            )
        )

        CrearLicUIContent(
            onBack = {},
            onSuccess = {},
            userState = sampleUser,
            allCategories = sampleCategories,
            userFromBrain = sampleUser,
            locationFromBrain = LocationOption.Personal("Av. Siempre Viva", "742", "Springfield"),
            onCreateTender = { _, _, _, _, _, _, _, _, _, _, _ -> }
        )
    }
}