package com.example.myapplication.prestador.ui.profile

import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import com.example.myapplication.prestador.data.model.ServiceType
import com.example.myapplication.prestador.ui.register.components.FloatingLabelTextField
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.ProfileState
import com.example.myapplication.prestador.viewmodel.UpdateState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.prestador.viewmodel.PhotoUploadState
import okhttp3.internal.http2.Header
import androidx.compose.runtime.collectAsState
import com.example.myapplication.prestador.data.model.AddressProvider
import com.example.myapplication.prestador.data.model.BranchProvider
import com.example.myapplication.prestador.data.model.CompanyProvider
import com.example.myapplication.prestador.data.model.EmployeeProvider
import com.example.myapplication.prestador.data.model.ServicioFirebase
import com.example.myapplication.prestador.ui.profile.sections.*
import com.example.myapplication.prestador.ui.profile.dialogs.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreenUnified(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val scope = rememberCoroutineScope()
    val profileState by viewModel.profileState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val photoUploadState by viewModel.photoUploadState.collectAsState()
    var isUploadingPhoto by remember { mutableStateOf(false)}
    
    val providerId = (profileState as? ProfileState.Success)?.provider?.id
    val verificado = (profileState as? ProfileState.Success)?.provider?.verificado ?: false
    val galleryImagesFlow by viewModel.galleryImages.collectAsState()
    val servicios by viewModel.servicios.collectAsState()
    val loadingServicios by viewModel.loadingServicios.collectAsState()
    
    // Estados del formulario vinculados al SSOT
    var name by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dniCuit by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var profesion by remember { mutableStateOf("") }
    var tieneMatricula by remember { mutableStateOf(false) }
    var matricula by remember { mutableStateOf("") }
    var pais by remember { mutableStateOf("Argentina") }
    var provincia by remember { mutableStateOf("") }
    var localidad by remember { mutableStateOf("") }
    var calle by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var codigoPostal by remember { mutableStateOf("") }
    var atencionUrgencias by remember { mutableStateOf(false) }
    var vaDomicilio by remember { mutableStateOf(false) }
    var envios by remember { mutableStateOf(false) }
    var turnosEnLocal by remember { mutableStateOf(false) }
    var doesService by remember { mutableStateOf(false) }
    var doesProduct by remember { mutableStateOf(false) }
    var direccionLocal by remember { mutableStateOf("") }
    var provinciaLocal by remember { mutableStateOf("") }
    var codigoPostalLocal by remember { mutableStateOf("") }
    var tieneEmpresa by remember { mutableStateOf(false) }
    var tieneSucursales by remember { mutableStateOf(false) }
    var atiendeVirtual by remember { mutableStateOf(false) }
    var trabajaConOtros by remember { mutableStateOf(false) }
    var nombreEmpresa by remember { mutableStateOf("") }
    var cuitEmpresa by remember { mutableStateOf("") }
    var direccionEmpresa by remember { mutableStateOf("") }
    var empresaGuardadaTrigger by remember { mutableStateOf(0) }
    var pendingEmpresaRefresh by remember { mutableStateOf(false) }
    var serviceType by remember { mutableStateOf(ServiceType.TECHNICAL) }
    var horarioLocal by remember { mutableStateOf("") }
    var horarioCasaCentral by remember { mutableStateOf("") }
    var galleryImages by remember { mutableStateOf("[]") }
    var categorias by remember { mutableStateOf("[]") }
    var rating by remember { mutableStateOf(0.0) }
    var reviewCount by remember { mutableStateOf(0) }

    // =========================================================================
    // SECCIÓN: OBSERVACIÓN DEL ESTADO (SSOT)
    // =========================================================================
    LaunchedEffect(profileState) {
        if (profileState is ProfileState.Success) {
            val p = (profileState as ProfileState.Success).provider
            name = p.name ?: ""
            apellido = p.apellido ?: ""
            email = p.email ?: ""
            phone = p.phone ?: ""
            dniCuit = p.dniCuit ?: ""
            imageUrl = p.imageUrl ?: ""
            description = p.description ?: ""
            profesion = p.profesion ?: ""
            tieneMatricula = p.tieneMatricula
            matricula = p.matricula ?: ""
            
            // Dirección personal mapeada del objeto embebido AddressProvider
            pais = p.pais ?: "Argentina"
            provincia = p.provincia ?: ""
            localidad = p.address?.localidad ?: ""
            calle = p.address?.calle ?: ""
            numero = p.address?.numero ?: ""
            codigoPostal = p.codigoPostal ?: ""
            address = p.address?.fullString() ?: ""

            atencionUrgencias = p.atencionUrgencias
            vaDomicilio = p.vaDomicilio
            envios = p.envios
            turnosEnLocal = p.turnosEnLocal
            doesService = p.doesService
            doesProduct = p.doesProduct
            
            direccionLocal = p.direccionLocal ?: ""
            provinciaLocal = p.provinciaLocal ?: ""
            codigoPostalLocal = p.codigoPostalLocal ?: ""
            
            tieneEmpresa = p.tieneEmpresa
            atiendeVirtual = p.atiendeVirtual
            trabajaConOtros = p.trabajaConOtros
            
            nombreEmpresa = p.nombreEmpresa ?: ""
            cuitEmpresa = p.cuitEmpresa ?: ""
            direccionEmpresa = p.direccionEmpresa ?: ""
            
            horarioLocal = p.horarioLocal ?: ""
            serviceType = ServiceType.fromString(p.serviceType)
            
            // Ranking y Stats
            rating = p.rating.toDouble()
            
            // Colecciones jerárquicas
            val firstCompany = p.companies.firstOrNull()
            horarioCasaCentral = firstCompany?.branches?.firstOrNull()?.workingHours ?: ""
            tieneSucursales = (firstCompany?.branches?.size ?: 0) > 1
            
            galleryImages = try { org.json.JSONArray(p.galleryImages).toString() } catch(e: Exception) { "[]" }
            categorias = try { org.json.JSONArray(p.categories).toString() } catch(e: Exception) { "[]" }
        }
    }


    val snackbarHostState = remember { SnackbarHostState() }

    // Estado del acordeón: qué sección está expandida
    var expandedSection by remember { mutableStateOf<String?>("personal") }
    
    // Dialog states
    var showServiceTypeDialog by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }
    var validationErrorMessage by remember { mutableStateOf("") }
    
    // Launcher para seleccionar imagen
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            viewModel.uploadProfilePhoto(it)
        }
    }
    
    // Estado de scroll para TopBar animada
    val listState = rememberLazyListState()
    val scrollProgress = remember {
        derivedStateOf {
            val maxScroll = 200f
            val currentScroll = listState.firstVisibleItemScrollOffset.toFloat()
            (currentScroll / maxScroll).coerceIn(0f, 1f)
        }
    }
    val topBarAlpha by animateFloatAsState(
        targetValue = scrollProgress.value,
        label = "topBarAlpha"
    )
    
    LaunchedEffect(photoUploadState) {
        when (val state = photoUploadState) {
            is PhotoUploadState.Loading -> {
                isUploadingPhoto = true
            }
            is PhotoUploadState.Success -> {
                isUploadingPhoto = false
                imageUrl = state.url
                snackbarHostState.showSnackbar("✅ Foto actualizada correctamente")
            }
            is PhotoUploadState.Error -> {
                isUploadingPhoto = false
                snackbarHostState.showSnackbar("❌ Error: ${state.message}")
            }
            else -> isUploadingPhoto = false
        }
    }

    // ELIMINADO: LaunchedEffect(businessEntity) y segundo LaunchedEffect(profileState) redundante
    // La lógica ha sido consolidada en el primer LaunchedEffect(profileState)
    
    // Mostrar mensaje de éxito
    LaunchedEffect(updateState) {
        if (updateState is UpdateState.Success) {
            if (pendingEmpresaRefresh) {
                pendingEmpresaRefresh = false
                empresaGuardadaTrigger++
            }
            kotlinx.coroutines.delay(1500)
            viewModel.resetUpdateState()
        }
    }
    
    Scaffold(
        containerColor = colors.backgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = topBarAlpha },
                color = colors.surfaceColor,
                shadowElevation = if (topBarAlpha > 0.01f) 4.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colors.primaryOrange
                        )
                    }
                    Text(
                        text = "Mi Perfil",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        when (val state = profileState) {
            is ProfileState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primaryOrange)
                }
            }
            
            is ProfileState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = colors.primaryOrange,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            color = colors.textPrimary,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadProfile() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primaryOrange
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            
            is ProfileState.Success -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        // HERO HEADER
                        item {
                            Box {
                                HeaderSection(
                                    name = name,
                                    apellido = apellido,
                                    profesion = profesion,
                                    imageUrl = imageUrl,
                                    selectedImageUri = selectedImageUri,
                                    tieneEmpresa = tieneEmpresa,
                                    colors = colors,
                                    paddingValues = paddingValues,
                                    onBack = onBack,
                                    onImageClick = {
                                        if (!isUploadingPhoto) galleryLauncher.launch("image/*")
                                    }
                                )
                                if (isUploadingPhoto) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(start = 90.dp, bottom = 16.dp)
                                            .size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                        item {
                            if (verificado) {
                                VerificadoBadge(modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
                            }
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        
                        item {
                                PersonalDataSection(
                                    name = name,
                                    onNameChange = { name = it },
                                    apellido = apellido,
                                    onApellidoChange = { apellido = it },
                                    email = email,
                                    onEmailChange = { email = it },
                                    phone = phone,
                                    onPhoneChange = { phone = it },
                                    dniCuit = dniCuit,
                                    onDniCuitChange = { dniCuit = it },
                                    expanded = expandedSection == "personal",
                                    onExpandChange = { expandedSection = if (expandedSection == "personal") null else "personal" },
                                    colors = colors
                                )
                            }
                            
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                            item {
                                val currentProvider = (profileState as? ProfileState.Success)?.provider
                                DireccionSection(
                                    titulo = "Dirección personal",
                                    direccion = currentProvider?.address?.let {
                                        com.example.myapplication.prestador.data.local.entity.DireccionEntity(
                                            id = it.id,
                                            referenciaId = currentProvider.id,
                                            referenciaTipo = "PRESTADOR",
                                            pais = it.pais ?: "Argentina",
                                            provincia = it.provincia ?: "",
                                            localidad = it.localidad ?: "",
                                            codigoPostal = it.codigoPostal ?: "",
                                            calle = it.calle ?: "",
                                            numero = it.numero ?: "",
                                            latitud = it.latitude,
                                            longitud = it.longitude
                                        )
                                    },
                                    expanded = expandedSection == "direccion",
                                    onExpandChange = { expandedSection = if (expandedSection == "direccion") null else "direccion" },
                                    onGuardar = { p, prov, loc, cp, c, n, lat, lon ->
                                        // 1. Crear el objeto de dirección jerárquico
                                        val newAddress = AddressProvider(
                                            id = currentProvider?.address?.id ?: "principal",
                                            calle = c,
                                            numero = n,
                                            localidad = loc,
                                            provincia = prov,
                                            pais = p,
                                            codigoPostal = cp,
                                            latitude = lat,
                                            longitude = lon
                                        )
                                        
                                        // 2. Guardar a través del ViewModel (SSOT)
                                        // El ViewModel ahora se encarga de actualizar el ProviderEntity completo
                                        viewModel.saveAdditionalAddress(newAddress)
                                        
                                        // 3. Sincronizar campos legacy en raíz solo si es necesario para compatibilidad UI inmediata
                                        val direccionTexto = "$c $n".trim()
                                        address = direccionTexto
                                        provincia = prov
                                        codigoPostal = cp
                                        localidad = loc
                                        calle = c
                                        numero = n
                                    }
                                )
                            }

                            item { Spacer(modifier = Modifier.height(16.dp)) }

                            item {
                                ProfessionalDataSection(
                                    profesion = profesion,
                                    onProfesionChange = { profesion = it },
                                    tieneMatricula = tieneMatricula,
                                    onTieneMatriculaChange = { tieneMatricula = it },
                                    matricula = matricula,
                                    onMatriculaChange = { matricula = it },
                                    description = description,
                                    onDescriptionChange = { description = it },
                                    expanded = expandedSection == "professional",
                                    onExpandChange = { expandedSection = if (expandedSection == "professional") null else "professional" },
                                    colors = colors
                                )
                            }

                            item { Spacer(modifier = Modifier.height(16.dp)) }

                            item {
                                ArchiveroSection(
                                    title = "Categorías",
                                    sectionId = "categorias",
                                    icon = Icons.Default.Category,
                                    color = Color(0xFF00897B),
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    expanded = expandedSection == "categorias",
                                    onExpandChange = { expandedSection = if (expandedSection == "categorias") null else "categorias" }
                                ) {
                                    CategoriasSelector(
                                        categoriasJson = categorias,
                                        onCategoriasActualizadas = { json ->
                                            categorias = json
                                            viewModel.updateCategorias(json)
                                        },
                                        serviciosFirebase = servicios
                                    )
                                }
                            }

                            item { Spacer(modifier = Modifier.height(16.dp)) }

                            item {
                                ImagenGaleriaSection(
                                    imagenesJson = galleryImages,
                                    empresaId = providerId ?: "",
                                    onImagenesActualizadas = { json ->
                                        galleryImages = json
                                        viewModel.updateGalleryImages(json)
                                    },
                                    expanded = expandedSection == "portafolio",
                                    onExpandChange = {
                                        expandedSection = if (expandedSection == "portafolio") null else "portafolio"
                                    }
                                )
                            }

                            item { Spacer(modifier = Modifier.height(16.dp)) }

                            item {
                                ServiceConfigSection(
                                        atencionUrgencias = atencionUrgencias,
                                        onAtencionUrgenciasChange = { atencionUrgencias = it },
                                        vaDomicilio = vaDomicilio,
                                        onVaDomicilioChange = { vaDomicilio = it },
                                        envios = envios,
                                        onEnviosChange = { envios = it },
                                        turnosEnLocal = turnosEnLocal,
                                        onTurnosEnLocalChange = { turnosEnLocal = it },
                                        direccionLocal = direccionLocal,
                                        onDireccionLocalChange = { direccionLocal = it },
                                        provinciaLocal = provinciaLocal,
                                        onProvinciaLocalChange = { provinciaLocal = it },
                                        codigoPostalLocal = codigoPostalLocal,
                                        onCodigoPostalLocalChange = { codigoPostalLocal = it },
                                        horarioLocal = horarioLocal,
                                        onHorarioLocalChange = { horarioLocal = it },
                                        serviceType = serviceType,
                                        onServiceTypeClick = { showServiceTypeDialog = true },
                                        doesService = doesService,
                                        onDoesServiceChange = { value ->
                                            doesService = value
                                            viewModel.updateProfile(doesService = value)
                                        },
                                        doesProduct = doesProduct,
                                        onDoesProductChange = { value ->
                                            doesProduct = value
                                            viewModel.updateProfile(doesProduct = value)
                                        },
                                        providerId = providerId,
                                        isEmpresaMode = false,
                                        direccionEmpresa = "",
                                        expanded = expandedSection == "services",
                                        onExpandChange = { expandedSection = if (expandedSection == "services") null else "services" },
                                        colors = colors
                                    )
                            }

                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        if (serviceType == ServiceType.PROFESSIONAL) {
                            item {
                                val currentProvider = (profileState as? ProfileState.Success)?.provider
                                val consultorioAddress = currentProvider?.addresses?.find { it.id == "consultorio" || it.id == "oficina" }
                                ProfessionalModalidadBlock(
                                    turnosEnLocal = turnosEnLocal,
                                    onTurnosEnLocalChange = { value ->
                                        turnosEnLocal = value
                                        viewModel.updateProfile(turnosEnLocal = value)
                                    },
                                    vaDomicilio = vaDomicilio,
                                    onAtiendeVirtualChange = { value ->
                                        atiendeVirtual = value
                                        viewModel.updateProfile(atiendeVirtual = value)
                                    },
                                    atiendeVirtual = atiendeVirtual,
                                    onVaDomicilioChange = { value ->
                                        vaDomicilio = value
                                        viewModel.updateProfile(vaDomicilio = value)
                                    },
                                    provinciaLocal = provinciaLocal,
                                    onProvinciaLocalChange = { provinciaLocal = it },
                                    direccionLocal = direccionLocal,
                                    onDireccionLocalChange = { direccionLocal = it },
                                    codigoPostalLocal = codigoPostalLocal,
                                    onCodigoPostalLocalChange = { codigoPostalLocal = it },
                                    consultorioDireccion = consultorioAddress?.let {
                                        com.example.myapplication.prestador.data.local.entity.DireccionEntity(
                                            id = it.id,
                                            referenciaId = currentProvider.id,
                                            referenciaTipo = "CONSULTORIO",
                                            pais = it.pais ?: "Argentina",
                                            provincia = it.provincia ?: "",
                                            localidad = it.localidad ?: "",
                                            codigoPostal = it.codigoPostal ?: "",
                                            calle = it.calle ?: "",
                                            numero = it.numero ?: "",
                                            latitud = it.latitude,
                                            longitud = it.longitude
                                        )
                                    },
                                    onGuardarConsultorio = { p, prov, loc, cp, c, n, lat, lon ->
                                        val newAddress = AddressProvider(
                                            id = "consultorio",
                                            calle = c,
                                            numero = n,
                                            localidad = loc,
                                            provincia = prov,
                                            pais = p,
                                            codigoPostal = cp,
                                            latitude = lat,
                                            longitude = lon
                                        )
                                        viewModel.saveAdditionalAddress(newAddress)
                                        
                                        // Sincronizar campos legacy en raíz
                                        val direccionTexto = "$c $n".trim()
                                        direccionLocal = direccionTexto
                                        provinciaLocal = prov
                                        codigoPostalLocal = cp
                                    },
                                    colors = colors
                                )
                            }
                        } else {

                            // Toggle: ¿Tiene empresa registrada?
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                                    border = BorderStroke(1.5.dp, Color(0xFF9C27B0))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val newValue = !tieneEmpresa
                                                tieneEmpresa = newValue
                                                viewModel.updateProfile(tieneEmpresa = newValue)
                                            }
                                            .padding(horizontal = 20.dp, vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF9C27B0).copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Business,
                                                contentDescription = null,
                                                tint = Color(0xFF9C27B0),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Empresa registrada",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = if (tieneEmpresa) "Activo — mostrás datos de empresa" else "Activar para cargar datos de empresa",
                                                fontSize = 12.sp,
                                                color = colors.textSecondary
                                            )
                                        }
                                        Switch(
                                            checked = tieneEmpresa,
                                            onCheckedChange = { newValue ->
                                                tieneEmpresa = newValue
                                                viewModel.updateProfile(tieneEmpresa = newValue)
                                            },
                                            modifier = Modifier.scale(0.85f),
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Color(0xFF9C27B0),
                                                uncheckedThumbColor = Color.White,
                                                uncheckedTrackColor = colors.textSecondary.copy(
                                                    alpha = 0.3f
                                                )
                                            )
                                        )
                                    }
                                }
                            }

                            item {
                                AnimatedVisibility(
                                    visible = tieneEmpresa,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 0.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Spacer(modifier = Modifier.height(4.dp))

                                        // PRIMARY empresa card
                                        val currentProvider = (profileState as? ProfileState.Success)?.provider
                                        val firstCompany = currentProvider?.companies?.firstOrNull()
                                        val firstBranch = firstCompany?.branches?.firstOrNull()
                                        
                                        EmpresaUnificadaCard(
                                            titulo = "Datos de Empresa",
                                            nombreEmpresa = nombreEmpresa,
                                            razonSocial = direccionEmpresa,
                                            cuitEmpresa = cuitEmpresa,
                                            provincia = firstBranch?.address?.provincia ?: "",
                                            localidad = firstBranch?.address?.localidad ?: "",
                                            codigoPostal = firstBranch?.address?.codigoPostal ?: "",
                                            calle = firstBranch?.address?.calle ?: "",
                                            numero = firstBranch?.address?.numero ?: "",
                                            horario = horarioCasaCentral,
                                            tieneSucursales = tieneSucursales,
                                            onTieneSucursalesChange = { tieneSucursales = it },
                                            expanded = expandedSection == "empresa_0",
                                            onExpandChange = {
                                                expandedSection = if (expandedSection == "empresa_0") null else "empresa_0"
                                            },
                                            onGuardar = { nombre, razon, cuit, prov, localidad, cp, calle, numero, horario ->
                                                pendingEmpresaRefresh = true
                                                horarioCasaCentral = horario
                                                val address = AddressProvider(
                                                    calle = calle,
                                                    numero = numero,
                                                    localidad = localidad,
                                                    provincia = prov,
                                                    codigoPostal = cp
                                                )
                                                val branch = BranchProvider(
                                                    name = "Casa Central",
                                                    address = address,
                                                    workingHours = horario
                                                )
                                                val company = CompanyProvider(
                                                    name = nombre,
                                                    razonSocial = razon,
                                                    cuit = cuit,
                                                    branches = listOf(branch)
                                                )
                                                viewModel.addCompany(company)
                                                
                                                // Sync legacy
                                                viewModel.updateProfile(
                                                    nombreEmpresa = nombre,
                                                    direccionEmpresa = "$calle $numero".trim(),
                                                    cuitEmpresa = cuit,
                                                    tieneEmpresa = true
                                                )
                                            },
                                            onEliminar = null,
                                            extraContent = {
                                                val team = firstBranch?.employees ?: emptyList()
                                                val referentesLegacy = team.map { emp ->
                                                    com.example.myapplication.prestador.data.local.entity.ReferenteEntity(
                                                        id = emp.id,
                                                        providerId = currentProvider?.id ?: "",
                                                        nombre = emp.name,
                                                        apellido = emp.lastName,
                                                        cargo = emp.position,
                                                        imageUrl = emp.photoUrl
                                                    )
                                                }
                                                ReferentesSection(
                                                    referentes = referentesLegacy,
                                                    onAgregar = { n, a, c, uri ->
                                                        scope.launch {
                                                            val url: String? = uri?.let { u ->
                                                                try {
                                                                    val ref = com.google.firebase.storage.FirebaseStorage.getInstance()
                                                                        .reference
                                                                        .child("referentes/${currentProvider?.id}/${java.util.UUID.randomUUID()}.jpg")
                                                                    ref.putFile(u).await()
                                                                    ref.downloadUrl.await().toString()
                                                                } catch (e: Exception) { null }
                                                            }
                                                            viewModel.addEmployee(EmployeeProvider(
                                                                name = n,
                                                                lastName = a,
                                                                position = c,
                                                                photoUrl = url
                                                            ))
                                                        }
                                                    },
                                                    onDesactivar = { ref ->
                                                        viewModel.removeEmployee(ref.id)
                                                    }
                                                )
                                            },
                                            colors = colors,
                                            refreshTrigger = empresaGuardadaTrigger,
                                            onUploadImage = { uri ->
                                                try {
                                                    val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"
                                                    val ref = com.google.firebase.storage.FirebaseStorage.getInstance()
                                                        .reference
                                                        .child("sucursales/$uid/${java.util.UUID.randomUUID()}.jpg")
                                                    ref.putFile(uri).await()
                                                    ref.downloadUrl.await().toString()
                                                } catch (e: Exception) { null }
                                            },
                                            onSucursalAgregada = {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("✅ Sucursal guardada correctamente")
                                                }
                                            }
                                        )

                                        // ADDITIONAL empresa cards (Empresa 2, 3, etc.)
                                        val additionalBusinesses = currentProvider?.companies?.drop(1) ?: emptyList()

                                        additionalBusinesses.forEachIndexed { index, company ->
                                            val branch = company.branches.firstOrNull()
                                            EmpresaUnificadaCard(
                                                titulo = "Empresa ${index + 2}",
                                                nombreEmpresa = company.name,
                                                razonSocial = company.razonSocial,
                                                cuitEmpresa = company.cuit,
                                                provincia = branch?.address?.provincia ?: "",
                                                localidad = branch?.address?.localidad ?: "",
                                                codigoPostal = branch?.address?.codigoPostal ?: "",
                                                calle = branch?.address?.calle ?: "",
                                                numero = branch?.address?.numero ?: "",
                                                horario = branch?.workingHours ?: "",
                                                tieneSucursales = false,
                                                onTieneSucursalesChange = { /* not supported for additional */ },
                                                expanded = expandedSection == "empresa_${index + 1}",
                                                onExpandChange = {
                                                    expandedSection = if (expandedSection == "empresa_${index + 1}") null else "empresa_${index + 1}"
                                                },
                                                onGuardar = { nombre, razon, cuit, prov, loc, cp, calle, num, horario ->
                                                    val updatedCompany = company.copy(
                                                        name = nombre,
                                                        razonSocial = razon,
                                                        cuit = cuit,
                                                        branches = listOf(BranchProvider(
                                                            name = "Sucursal",
                                                            address = AddressProvider(calle=calle, numero=num, localidad=loc, provincia=prov, codigoPostal=cp),
                                                            workingHours = horario
                                                        ))
                                                    )
                                                    viewModel.addCompany(updatedCompany) // Actúa como update si ID coincide
                                                },
                                                onEliminar = {
                                                    viewModel.removeCompany(company.id)
                                                },
                                                colors = colors
                                            )
                                        }

                                        // "+" button to add a new empresa
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.addCompany(CompanyProvider(name = "Nueva Empresa"))
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryOrange),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.primaryOrange)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Agregar otra empresa", fontSize = 14.sp)
                                        }
                                    }
                                }

                            }
                        }

                            item { Spacer(modifier = Modifier.height(16.dp)) }

                            item {
                                EmpleadosSection(
                                    trabajaConOtros = trabajaConOtros,
                                    onTrabajaConOtrosChange = { trabajaConOtros = it },
                                    expanded = expandedSection == "team",
                                    onExpandChange = { expandedSection = if (expandedSection == "team") null else "team" },
                                    isProfessional = serviceType == ServiceType.PROFESSIONAL
                                )
                            }
                    }
                    
                    // FAB GUARDAR
                    FloatingActionButton(
                        onClick = {
                            // Validar datos antes de guardar
                            if (turnosEnLocal) {
                                if (serviceType == ServiceType.PROFESSIONAL) {
                                    val currentProvider = (profileState as? ProfileState.Success)?.provider
                                    val consultorioDireccion = currentProvider?.addresses?.find { it.id == "consultorio" }
                                    val consultorioOk = consultorioDireccion != null &&
                                        !consultorioDireccion.provincia.isNullOrBlank() &&
                                        !consultorioDireccion.codigoPostal.isNullOrBlank() &&
                                        !consultorioDireccion.calle.isNullOrBlank()

                                    if (!consultorioOk) {
                                        validationErrorMessage = "Guardá la dirección del consultorio/oficina para activar atención en local"
                                        showValidationError = true
                                        return@FloatingActionButton
                                    }
                                } else {
                                    if (direccionLocal.isBlank() || provinciaLocal.isBlank() || codigoPostalLocal.isBlank()) {
                                        validationErrorMessage = "Completá la dirección del local para activar atención en local"
                                        showValidationError = true
                                        return@FloatingActionButton
                                    }
                                }
                            }
                            
                            // Si pasa validación, guardar
                            viewModel.updateProfile(
                                name = name,
                                apellido = apellido,
                                phone = phone,
                                dniCuit = dniCuit,
                                description = description,
                                address = address,
                                profesion = profesion,
                                tieneMatricula = tieneMatricula,
                                matricula = matricula.takeIf { tieneMatricula },
                                provincia = provincia,
                                codigoPostal = codigoPostal,
                                pais = pais,
                                atencionUrgencias = atencionUrgencias,
                                vaDomicilio = vaDomicilio,
                                envios = envios,
                                turnosEnLocal = turnosEnLocal,
                                direccionLocal = direccionLocal.takeIf { turnosEnLocal },
                                provinciaLocal = provinciaLocal.takeIf { turnosEnLocal },
                                codigoPostalLocal = codigoPostalLocal.takeIf { turnosEnLocal },
                                tieneEmpresa = tieneEmpresa,
                                atiendeVirtual = atiendeVirtual,
                                trabajaConOtros = trabajaConOtros,
                                nombreEmpresa = nombreEmpresa.takeIf { tieneEmpresa },
                                cuitEmpresa = cuitEmpresa.takeIf { tieneEmpresa },
                                direccionEmpresa = direccionEmpresa.takeIf { tieneEmpresa },
                                serviceType = serviceType.name,
                                horarioLocal = horarioLocal.takeIf { turnosEnLocal },
                                categorias = categorias,
                                // Campos extra detectados para sincronización completa
                                doesService = doesService,
                                doesProduct = doesProduct
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp),
                        containerColor = colors.primaryOrange,
                        contentColor = Color.White
                    ) {
                        when (updateState) {
                            is UpdateState.Loading -> {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            is UpdateState.Success -> {
                                Icon(Icons.Default.Check, contentDescription = "Guardado")
                            }
                            else -> {
                                Icon(Icons.Default.Save, contentDescription = "Guardar")
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Dialog de selección de tipo de servicio
    if (showServiceTypeDialog) {
        ServiceTypeSelectorDialog(
            currentServiceType = serviceType,
            onDismiss = { showServiceTypeDialog = false },
            onServiceTypeSelected = { newType ->
                serviceType = newType
            },
            colors = colors
        )
    }
    
    // Dialog de error de validación
    if (showValidationError) {
        AlertDialog(
            onDismissRequest = { showValidationError = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = colors.primaryOrange
                )
            },
            title = {
                Text(
                    text = "Datos incompletos",
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = validationErrorMessage,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { showValidationError = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryOrange
                    )
                ) {
                    Text("Entendido")
                }
            }
        )
    }
}

// COMPONENTE: Header con foto y toggle