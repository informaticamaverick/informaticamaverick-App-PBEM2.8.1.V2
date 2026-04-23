package com.example.myapplication.prestador.ui.profile.sections

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.asImageBitmap
import com.example.myapplication.prestador.data.model.ServiceType
import com.example.myapplication.prestador.ui.register.components.FloatingLabelTextField
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.DireccionUiState
import com.example.myapplication.prestador.viewmodel.DireccionViewModel
import com.example.myapplication.prestador.viewmodel.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.ProfileState
import com.example.myapplication.prestador.viewmodel.UpdateState
import com.example.myapplication.prestador.viewmodel.ReferenteViewModel
import com.example.myapplication.prestador.viewmodel.ReferentesUiState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.prestador.viewmodel.PhotoUploadState
import com.example.myapplication.prestador.viewmodel.BusinessViewModel
import androidx.compose.runtime.collectAsState
import com.example.myapplication.prestador.data.model.AddressProvider
import com.example.myapplication.prestador.data.model.ServicioFirebase
import com.example.myapplication.prestador.ui.profile.LOCALIDADES_POR_PROVINCIA
import com.example.myapplication.prestador.ui.profile.PROVINCIAS_ARGENTINA
import com.example.myapplication.prestador.ui.profile.DireccionSection
import com.example.myapplication.prestador.ui.profile.SucursalesSection
import com.example.myapplication.prestador.ui.profile.HorarioSelectorField
import com.example.myapplication.prestador.ui.profile.dialogs.CambiarEmailDialog
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import androidx.compose.ui.text.style.TextAlign
import okhttp3.Address

@Composable
fun HeaderSection(
    name: String,
    apellido: String,
    profesion: String,
    imageUrl: String?,
    selectedImageUri: Uri?,
    bannerImageUrl: String?,
    selectedBannerUri: Uri?,
    tieneEmpresa: Boolean,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onImageClick: () -> Unit,
    onBannerClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // ── BANNER ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
        ) {
            val bannerModel: Any? = selectedBannerUri
                ?: bannerImageUrl?.takeIf { it.isNotEmpty() }
            if (bannerModel != null) {
                AsyncImage(
                    model = bannerModel,
                    contentDescription = "Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    colors.primaryOrange.copy(alpha = 0.8f),
                                    Color(0xFFFF5722).copy(alpha = 0.5f),
                                    colors.backgroundColor
                                )
                            )
                        )
                )
            }
            // Degradado inferior
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent,
                                colors.backgroundColor.copy(alpha = 0.85f)),
                            startY = 100f
                        )
                    )
            )
            // Barra superior: Volver + Editar banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = paddingValues.calculateTopPadding())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.35f)
                ) {
                    IconButton(onClick = onBack, modifier =
                        Modifier.fillMaxSize()) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.35f)
                ) {
                    IconButton(onClick = onBannerClick, modifier =
                        Modifier.fillMaxSize()) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "Editar banner",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // ── FOTO DE PERFIL (solapando el banner) ────────────────────────
        Box(
            modifier = Modifier
                .padding(top = 148.dp, start = 20.dp)
                .size(90.dp)
                .clip(CircleShape)
                .border(3.dp, colors.primaryOrange, CircleShape)
                .clickable { onImageClick() }
        ) {
            when {
                selectedImageUri != null -> AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                !imageUrl.isNullOrEmpty() && imageUrl.startsWith("http") ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                !imageUrl.isNullOrEmpty() -> {
                    val bitmap = remember(imageUrl) {
                        try {
                            val bytes = android.util.Base64.decode(imageUrl,
                                android.util.Base64.DEFAULT)

                            android.graphics.BitmapFactory.decodeByteArray(bytes, 0,
                                bytes.size)?.asImageBitmap()
                        } catch (e: Exception) { null }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(

                            Modifier.fillMaxSize().background(colors.primaryOrange.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null,
                                Modifier.size(48.dp), tint = colors.primaryOrange.copy(alpha = 0.5f))
                        }
                    }
                }
                else -> Box(

                    Modifier.fillMaxSize().background(colors.primaryOrange.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, Modifier.size(48.dp),
                        tint = colors.primaryOrange.copy(alpha = 0.5f))
                }
            }
            // Ícono cámara
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd).size(28.dp),
                shape = CircleShape,
                color = colors.primaryOrange,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White,
                        modifier = Modifier.size(15.dp))
                }
            }
        }

        // ── NOMBRE + PROFESIÓN ───────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 10.dp)
        ) {
            Text(
                text = "$name $apellido".trim().ifEmpty { "Prestador" },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            if (profesion.isNotEmpty()) {
                Text(
                    text = profesion,
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}


// SECCIÓN: Datos Personales
@Composable
fun PersonalDataSection(
    name: String,
    onNameChange: (String) -> Unit,
    apellido: String,
    onApellidoChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    dniCuit: String,
    onDniCuitChange: (String) -> Unit,
    expanded: Boolean,
    onExpandChange: () -> Unit,
    onGuarguar: (nombre: String, apellido: String, phone: String, dniCuit: String) -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
){
    ArchiveroSection(
        title = "Datos Personales",
        sectionId = "personal",
        icon = Icons.Default.Person,
        color = colors.primaryOrange,
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandChange = { onExpandChange() }
    ) {
        // Nombre + Apellido en la misma fila
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                FloatingLabelTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = "Nombre",
                    leadingIcon = Icons.Default.Person
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                FloatingLabelTextField(
                    value = apellido,
                    onValueChange = onApellidoChange,
                    label = "Apellido",
                    leadingIcon = Icons.Default.Person
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Email — con opción de cambio vía reautenticación + verificación Firebase
        var showEmailDialog by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = colors.surfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Email", fontSize = 11.sp, color = colors.textSecondary)
                    Text(
                        text = email.ifBlank { "—" },
                        fontSize = 14.sp,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
                TextButton(
                    onClick = { showEmailDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Cambiar", fontSize = 12.sp, color = colors.primaryOrange)
                }
            }
        }

        if (showEmailDialog) {
            CambiarEmailDialog(
                onDismiss = { showEmailDialog = false }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FloatingLabelTextField(
                value = phone,
                onValueChange = { if (it.all { c -> c.isDigit() || c == ' ' || c == '-' }) onPhoneChange(it) },
                label = "Telefono",
                leadingIcon = null,
                leadingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null,
                            tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                        Text("+54", color = colors.textSecondary, fontSize = 12.sp)
                    }
                },
                keyboardType = KeyboardType.Phone,
                modifier = Modifier.weight(1f)
            )

            FloatingLabelTextField(
                value = dniCuit,
                onValueChange = { if (it.all { c -> c.isDigit() }) onDniCuitChange(it) },
                label = "DNI / CUIT",
                leadingIcon = Icons.Default.Badge,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)

            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onGuarguar(name, apellido, phone, dniCuit) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            Text("Guardar datos personales", fontSize = 13.sp)
        }

    }
}

// SECCIÓN: Datos Profesionales
@Composable
fun ProfessionalDataSection(
    profesion: String,
    onProfesionChange: (String) -> Unit,
    tieneMatricula: Boolean,
    onTieneMatriculaChange: (Boolean) -> Unit,
    matricula: String,
    onMatriculaChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    expanded: Boolean,
    onExpandChange: () -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    // Mapeo profesión → label + hint de matrícula
    val matriculaInfo = remember(profesion) {
        when {
            profesion.contains("gasista", ignoreCase = true) ||
            profesion.contains("gas", ignoreCase = true) ->
                Pair("Habilitaci\u00f3n ENARGAS", "Ej: MAT-12345 \u2014 emitida por ENARGAS")
            profesion.contains("m\u00e9dico", ignoreCase = true) ||
            profesion.contains("medico", ignoreCase = true) ||
            profesion.contains("doctor", ignoreCase = true) ->
                Pair("Matr\u00edcula M\u00e9dica (MP/MN)", "Ej: MP 12345 \u2014 Matr\u00edcula Provincial o Nacional")
            profesion.contains("abogado", ignoreCase = true) ->
                Pair("Tomo y Folio", "Ej: T.XV F.123 \u2014 Colegio de Abogados")
            profesion.contains("contador", ignoreCase = true) ||
            profesion.contains("contable", ignoreCase = true) ->
                Pair("Matr\u00edcula FACPCE", "Ej: 12345 \u2014 Consejo Profesional de Cs. Econ\u00f3micas")
            profesion.contains("arquitecto", ignoreCase = true) ||
            profesion.contains("ingeniero", ignoreCase = true) ->
                Pair("Matr\u00edcula Profesional", "Ej: 12345 \u2014 Consejo Profesional de Ing./Arq.")
            profesion.contains("electricista", ignoreCase = true) ->
                Pair("Habilitaci\u00f3n El\u00e9ctrica", "Ej: HAB-12345 \u2014 ENRE o habilitaci\u00f3n municipal")
            profesion.contains("plomero", ignoreCase = true) ||
            profesion.contains("plomer", ignoreCase = true) ->
                Pair("Habilitaci\u00f3n Municipal", "Ej: 12345 \u2014 habilitaci\u00f3n municipal")
            profesion.contains("psic\u00f3logo", ignoreCase = true) ||
            profesion.contains("psicologo", ignoreCase = true) ->
                Pair("Matr\u00edcula Profesional", "Ej: 12345 \u2014 Colegio de Psic\u00f3logos")
            profesion.contains("farmac\u00e9utico", ignoreCase = true) ||
            profesion.contains("farmaceutico", ignoreCase = true) ->
                Pair("Matr\u00edcula Farmac\u00e9utica", "Ej: 12345 \u2014 Colegio de Farm.")
            else ->
                Pair("N\u00famero de Matr\u00edcula", "Ingres\u00e1 el n\u00famero de tu matr\u00edcula o habilitaci\u00f3n")
        }
    }

    ArchiveroSection(
        title = "Datos Profesionales",
        sectionId = "professional",
        icon = Icons.Default.Work,
        color = Color(0xFF4CAF50),
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandChange = { onExpandChange() }
    ) {
        FloatingLabelTextField(
            value = profesion,
            onValueChange = onProfesionChange,
            label = "Profesi\u00f3n / Oficio",
            leadingIcon = Icons.Default.Work
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Switch matrícula
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTieneMatriculaChange(!tieneMatricula) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CardMembership, contentDescription = null,
                tint = colors.textSecondary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "\u00bfTiene matr\u00edcula profesional?",
                color = colors.textPrimary, fontSize = 15.sp, modifier = Modifier.weight(1f))
            androidx.compose.material3.Switch(
                checked = tieneMatricula,
                onCheckedChange = onTieneMatriculaChange,
                modifier = Modifier.scale(0.85f),
                colors = androidx.compose.material3.SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colors.primaryOrange,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = colors.textSecondary.copy(alpha = 0.3f)
                )
            )
        }

        // Campo matrícula + mensajes
        AnimatedVisibility(
            visible = tieneMatricula,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                FloatingLabelTextField(
                    value = matricula,
                    onValueChange = onMatriculaChange,
                    label = matriculaInfo.first,
                    leadingIcon = Icons.Default.CardMembership
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Hint del formato
                Text(
                    text = "\uD83D\uDCA1 ${matriculaInfo.second}",
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Banner verificación pendiente
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF3E0),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null,
                            tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
                        Column {
                            Text("\u23F3 Verificaci\u00f3n pendiente",
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE65100))
                            Text("Pr\u00f3ximamente podr\u00e1s verificar tu matr\u00edcula para obtener el badge \u2705 en tu perfil.",
                                fontSize = 11.sp, color = Color(0xFFBF360C),
                                lineHeight = 15.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FloatingLabelTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = "Descripci\u00f3n / Sobre m\u00ed",
            leadingIcon = Icons.Default.Info
        )
    }
}

// SECCIÓN: Ubicación
@Composable
fun LocationSection(
    address: String,
    onAddressChange: (String) -> Unit,
    provincia: String,
    onProvinciaChange: (String) -> Unit,
    codigoPostal: String,
    onCodigoPostalChange: (String) -> Unit,
    pais: String,
    onPaisChange: (String) -> Unit,
    expanded: Boolean,
    onExpandChange: () -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    ArchiveroSection(
        title = "Ubicación",
        sectionId = "location",
        icon = Icons.Default.LocationOn,
        color = Color(0xFF2196F3),
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandChange = { onExpandChange() }
    ) {
        FloatingLabelTextField(
            value = address,
            onValueChange = onAddressChange,
            label = "Dirección",
            leadingIcon = Icons.Default.LocationOn
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        FloatingLabelTextField(
            value = provincia,
            onValueChange = onProvinciaChange,
            label = "Provincia",
            leadingIcon = Icons.Default.Place
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        FloatingLabelTextField(
            value = codigoPostal,
            onValueChange = onCodigoPostalChange,
            label = "Código Postal",
            leadingIcon = Icons.Default.PinDrop,
            keyboardType = KeyboardType.Number
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        FloatingLabelTextField(
            value = pais,
            onValueChange = onPaisChange,
            label = "País",
            leadingIcon = Icons.Default.Public,
            enabled = false
        )
    }
}

// SECCIÓN: Datos de Empresa (Unificada)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpresaUnificadaCard(
    titulo: String = "Datos de Empresa",
    nombreEmpresa: String,
    razonSocial: String,
    cuitEmpresa: String,
    provincia: String,
    localidad: String,
    codigoPostal: String,
    calle: String,
    numero: String,
    horario: String,
    tieneSucursales: Boolean,
    onTieneSucursalesChange: (Boolean) -> Unit,
    expanded: Boolean,
    onExpandChange: () -> Unit,
    onGuardar: (nombre: String, razonSocial: String, cuit: String, provincia: String, localidad: String, codigoPostal: String, calle: String, numero: String, horario: String, categories: List<String>, photoUrl: String?, pendingPhotoUri: android.net.Uri?, lat: Double?, lng: Double?, pais: String) -> Unit,
    onEliminar: (() -> Unit)? = null,
    extraContent: (@Composable ColumnScope.() -> Unit)? = null,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    refreshTrigger: Int = 0,
    onUploadImage: (suspend (android.net.Uri) -> String?)? = null,
    onSucursalAgregada: (() -> Unit)? = null,
    categoriesEmpresa: List<String> = emptyList(),
    categoriasPrestador: List<String> = emptyList(),
    onUploadCompanyImage: (suspend (android.net.Uri) -> String?)? = null,
    latitude: Double? = null,
    longitude: Double? = null,
    pais: String = "Argentina",
){
    ArchiveroSection(
        title = titulo,
        sectionId = "empresa_unificada_$titulo",
        icon = Icons.Default.Business,
        color = Color(0xFFFF9800),
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandChange = { onExpandChange() }
    ) {
        var editando by remember(nombreEmpresa) { mutableStateOf(nombreEmpresa.isBlank()) }
        var cuitError by remember { mutableStateOf("") }
        var localNombre by remember(nombreEmpresa) { mutableStateOf(nombreEmpresa) }
        var localRazon by remember(razonSocial) { mutableStateOf(razonSocial) }
        var cuitValue by remember(cuitEmpresa) {
            mutableStateOf(TextFieldValue(cuitEmpresa, TextRange(cuitEmpresa.length)))
        }
        var localProvincia by remember(provincia) { mutableStateOf(provincia) }
        var localLocalidad by remember(localidad) { mutableStateOf(localidad) }
        var localCP by remember(codigoPostal) { mutableStateOf(codigoPostal) }
        var localCalle by remember(calle) { mutableStateOf(calle) }
        var localNumero by remember(numero) { mutableStateOf(numero) }
        var localHorario by remember(horario) { mutableStateOf(horario) }
        //Estados lat/lng/pais para geocodificacion
        var geocodedLat by remember(latitude) { mutableStateOf(latitude)}
        var geocodedLng by remember(longitude) { mutableStateOf(longitude) }
        var localPais by remember(pais) { mutableStateOf(pais) }
        var tieneSucursalesLocal by remember { mutableStateOf(tieneSucursales) }
        var localCategories by remember(categoriesEmpresa) { mutableStateOf(categoriesEmpresa)}
        // Uri local — NO se sube hasta que el usuario toque Guardar
        var pendingPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
        val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) pendingPhotoUri = uri
        }
        var geocodingLoading by remember { mutableStateOf(false) }
        var mostrarSugerenciasProvincia by remember { mutableStateOf(false) }
        var mostrarSugerenciasLocalidad by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val empresaCardScope = rememberCoroutineScope()
        val locationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                empresaCardScope.launch {
                    geocodingLoading = true
                    try {
                        val fusedClient = com.google.android.gms.location.LocationServices
                            .getFusedLocationProviderClient(context)
                        @Suppress("MissingPermission")
                        val loc = fusedClient.lastLocation.await()
                        if (loc != null) {
                            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                val addr = addresses[0]
                                val calleDetectada = buildString {
                                    if (!addr.thoroughfare.isNullOrBlank()) append(addr.thoroughfare)
                                }
                                if (calleDetectada.isNotBlank()) localCalle = calleDetectada
                                if (!addr.subThoroughfare.isNullOrBlank()) localNumero = addr.subThoroughfare!!
                                if (!addr.locality.isNullOrBlank()) localLocalidad = addr.locality!!
                                if (!addr.adminArea.isNullOrBlank()) localProvincia = addr.adminArea!!
                                if (!addr.postalCode.isNullOrBlank()) localCP = addr.postalCode!!
                                if (!addr.countryName.isNullOrBlank()) localPais = addr.countryName!!
                                geocodedLat = loc.latitude
                                geocodedLng = loc.longitude
                            }
                        }
                    } catch (_: Exception) {
                    } finally {
                        geocodingLoading = false
                    }
                }
            }
        }

        LaunchedEffect(tieneSucursales) { tieneSucursalesLocal = tieneSucursales }

        val provinciasFiltradas = if (localProvincia.isBlank()) emptyList()
            else PROVINCIAS_ARGENTINA.filter { it.contains(localProvincia.trim(), ignoreCase = true) }
        val localidadesDeProvincia = LOCALIDADES_POR_PROVINCIA.entries
            .firstOrNull { it.key.equals(localProvincia.trim(), ignoreCase = true) }?.value ?: emptyList()
        val localidadesFiltradas = if (localLocalidad.isBlank()) localidadesDeProvincia
            else localidadesDeProvincia.filter { it.nombre.contains(localLocalidad.trim(), ignoreCase = true) }

        if (!editando && nombreEmpresa.isNotBlank()) {
            // ── MODO LECTURA ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = colors.surfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(6.dp), color = colors.primaryOrange.copy(alpha = 0.12f)) {
                            Icon(Icons.Default.Business, contentDescription = null,
                                tint = colors.primaryOrange, modifier = Modifier.padding(6.dp).size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Nombre", fontSize = 11.sp, color = colors.textSecondary)
                            Text(nombreEmpresa, fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                        }
                    }
                    if (razonSocial.isNotBlank()) {
                        HorizontalDivider(color = colors.border)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(6.dp), color = colors.primaryOrange.copy(alpha = 0.12f)) {
                                Icon(Icons.Default.BusinessCenter, contentDescription = null,
                                    tint = colors.primaryOrange, modifier = Modifier.padding(6.dp).size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Razón social", fontSize = 11.sp, color = colors.textSecondary)
                                Text(razonSocial, fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    if (cuitEmpresa.isNotBlank()) {
                        HorizontalDivider(color = colors.border)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(6.dp), color = colors.primaryOrange.copy(alpha = 0.12f)) {
                                Icon(Icons.Default.Numbers, contentDescription = null,
                                    tint = colors.primaryOrange, modifier = Modifier.padding(6.dp).size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("CUIT", fontSize = 11.sp, color = colors.textSecondary)
                                Text(cuitEmpresa, fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    val hasAddress = listOf(calle, numero, localidad, provincia, codigoPostal).any { it.isNotBlank() }
                    if (hasAddress) {
                        HorizontalDivider(color = colors.border)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(6.dp), color = colors.primaryOrange.copy(alpha = 0.12f)) {
                                Icon(Icons.Default.LocationOn, contentDescription = null,
                                    tint = colors.primaryOrange, modifier = Modifier.padding(6.dp).size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Dirección Casa Central", fontSize = 11.sp, color = colors.textSecondary)
                                val addrLine = listOfNotNull(
                                    if (calle.isNotBlank()) calle else null,
                                    if (numero.isNotBlank()) numero else null
                                ).joinToString(" ").ifBlank { null }
                                val cityLine = listOfNotNull(
                                    if (localidad.isNotBlank()) localidad else null,
                                    if (provincia.isNotBlank()) provincia else null,
                                    if (codigoPostal.isNotBlank()) "CP $codigoPostal" else null
                                ).joinToString(", ").ifBlank { null }
                                if (addrLine != null) Text(addrLine, fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                                if (cityLine != null) Text(cityLine, fontSize = 13.sp, color = colors.textPrimary)
                            }
                        }
                    }
                    if (horario.isNotBlank()) {
                        HorizontalDivider(color = colors.border)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(6.dp), color = colors.primaryOrange.copy(alpha = 0.12f)) {
                                Icon(Icons.Default.Schedule, contentDescription = null,
                                    tint = colors.primaryOrange, modifier = Modifier.padding(6.dp).size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Horario", fontSize = 11.sp, color = colors.textSecondary)
                                Text(horario, fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (onEliminar != null) Arrangement.SpaceBetween else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onEliminar != null) {
                    OutlinedButton(
                        onClick = onEliminar,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar", fontSize = 13.sp)
                    }
                }
                OutlinedButton(
                    onClick = { editando = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryOrange),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.primaryOrange),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar", fontSize = 13.sp)
                }
            }
        } else {
            // ── MODO EDICIÓN ──
            FloatingLabelTextField(
                value = localNombre,
                onValueChange = { localNombre = it },
                label = "Nombre de la empresa",
                leadingIcon = Icons.Default.Business
            )
            Spacer(modifier = Modifier.height(12.dp))

            FloatingLabelTextField(
                value = localRazon,
                onValueChange = { localRazon = it },
                label = "Razón social",
                leadingIcon = Icons.Default.BusinessCenter
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = cuitValue,
                onValueChange = { tvInput ->
                    val soloDigitos = tvInput.text.filter { c -> c.isDigit() }.take(11)
                    val formateado = when {
                        soloDigitos.length <= 2 -> soloDigitos
                        soloDigitos.length <= 10 -> "${soloDigitos.take(2)}-${soloDigitos.drop(2)}"
                        else -> "${soloDigitos.take(2)}-${soloDigitos.drop(2).take(8)}-${soloDigitos[10]}"
                    }
                    cuitValue = TextFieldValue(formateado, TextRange(formateado.length))
                    cuitError = if (soloDigitos.length == 11 && !validarCuit(soloDigitos)) "CUIT inválido" else ""
                },
                label = { Text("CUIT") },
                leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null, tint = colors.textSecondary) },
                isError = cuitError.isNotEmpty(),
                supportingText = if (cuitError.isNotEmpty()) {
                    { Text(cuitError, color = MaterialTheme.colorScheme.error) }
                } else null,
                placeholder = { Text("XX-XXXXXXXX-X") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryOrange,
                    focusedLabelColor = colors.primaryOrange,
                    unfocusedBorderColor = colors.border
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Dirección Casa Central header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = colors.border)
                Text(
                    text = "Dirección Casa Central",
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = colors.border)
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Provincia with autocomplete
            Column(modifier = Modifier.fillMaxWidth()) {
                FloatingLabelTextField(
                    value = localProvincia,
                    onValueChange = { localProvincia = it; mostrarSugerenciasProvincia = true },
                    label = "Provincia",
                    leadingIcon = Icons.Default.Place
                )
                AnimatedVisibility(
                    visible = mostrarSugerenciasProvincia && provinciasFiltradas.isNotEmpty(),
                    enter = fadeIn(tween(200)) + expandVertically(tween(250)),
                    exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                ) {
                    Surface(shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp), shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            provinciasFiltradas.take(5).forEach { prov ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            localProvincia = prov
                                            mostrarSugerenciasProvincia = false
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text(prov, fontSize = 13.sp)
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Localidad with autocomplete
            Column(modifier = Modifier.fillMaxWidth()) {
                FloatingLabelTextField(
                    value = localLocalidad,
                    onValueChange = { localLocalidad = it; mostrarSugerenciasLocalidad = true },
                    label = "Localidad",
                    leadingIcon = Icons.Default.LocationCity
                )
                AnimatedVisibility(
                    visible = mostrarSugerenciasLocalidad && localidadesFiltradas.isNotEmpty(),
                    enter = fadeIn(tween(200)) + expandVertically(tween(250)),
                    exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                ) {
                    Surface(shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp), shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            localidadesFiltradas.take(5).forEach { loc ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            localLocalidad = loc.nombre
                                            localCP = loc.codigoPostal
                                            mostrarSugerenciasLocalidad = false
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(loc.nombre, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                    Text(loc.codigoPostal, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            FloatingLabelTextField(
                value = localCP,
                onValueChange = { localCP = it },
                label = "Código Postal",
                leadingIcon = Icons.Default.PinDrop,
                keyboardType = KeyboardType.Text
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = localCalle,
                onValueChange = { localCalle = it },
                label = { Text("Calle") },
                leadingIcon = { Icon(Icons.Default.EditRoad, contentDescription = null, tint = colors.textSecondary) },
                trailingIcon = {
                    if (geocodingLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.primaryOrange)
                    } else {
                        IconButton(onClick = {
                            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                        }) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Detectar ubicación", tint = colors.primaryOrange)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryOrange,
                    focusedLabelColor = colors.primaryOrange,
                    unfocusedBorderColor = colors.border
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            FloatingLabelTextField(
                value = localNumero,
                onValueChange = { localNumero = it },
                label = "Número",
                leadingIcon = Icons.Default.Numbers,
                keyboardType = KeyboardType.Number
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Botón para obtener lat/lng desde la dirección escrita
            OutlinedButton(
                onClick = {
                    empresaCardScope.launch {
                        geocodingLoading = true
                        try {
                            val geocoder =
                                android.location.Geocoder(context, java.util.Locale.getDefault())
                            val query = "$localCalle $localNumero,$localLocalidad, $localProvincia"
                            @Suppress("DEPRECATION")
                            val results =
                                geocoder.getFromLocationName(query, 1)
                            if (!results.isNullOrEmpty()) {
                                val r = results[0]
                                geocodedLat = r.latitude
                                geocodedLng = r.longitude
                                if (!r.countryName.isNullOrBlank())
                                    localPais = r.countryName!!
                                if (!r.postalCode.isNullOrBlank() &&
                                    localCP.isBlank()) localCP = r.postalCode!!
                            }
                        } catch (_: Exception) {
                        } finally {
                            geocodingLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp,
                    colors.primaryOrange)
            ) {
                if (geocodingLoading) {
                    CircularProgressIndicator(modifier =
                        Modifier.size(16.dp), strokeWidth = 2.dp, color = colors.primaryOrange)
                } else {
                    Icon(Icons.Default.MyLocation, contentDescription =
                        null, tint = colors.primaryOrange, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sincronizar coordenadas", fontSize = 13.sp, color =
                    colors.primaryOrange)
            }
            Spacer(modifier = Modifier.height(12.dp))

            HorarioSelectorField(
                horario = localHorario,
                onHorarioChange = { localHorario = it }

            )
            Spacer(modifier = Modifier.height(12.dp))




            extraContent?.invoke(this)
            Spacer(modifier = Modifier.height(8.dp))




            // Sucursales divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = colors.border)
                Text("Sucursales", fontSize = 12.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
                HorizontalDivider(modifier = Modifier.weight(1f), color = colors.border)
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Sucursales switch row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.backgroundColor.copy(alpha = 0.5f))
                    .clickable {
                        val newVal = !tieneSucursalesLocal
                        tieneSucursalesLocal = newVal
                        onTieneSucursalesChange(newVal)
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Store,
                    contentDescription = null,
                    tint = if (tieneSucursalesLocal) colors.primaryOrange else colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sucursales",
                    fontSize = 14.sp,
                    color = colors.textPrimary,
                    fontWeight = if (tieneSucursalesLocal) FontWeight.Medium else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = tieneSucursalesLocal,
                    onCheckedChange = { checked ->
                        tieneSucursalesLocal = checked
                        onTieneSucursalesChange(checked)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = colors.primaryOrange,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = colors.textSecondary.copy(alpha = 0.3f)
                    )
                )
            }

            AnimatedVisibility(
                visible = tieneSucursalesLocal,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                if (onUploadImage != null) {
                    SucursalesSection(
                        colors = colors,
                        refreshTrigger = refreshTrigger,
                        onUploadImage = { uri -> onUploadImage(uri) },
                        onSucursalAgregada = { onSucursalAgregada?.invoke() }
                    )
                } else {
                    Text(
                        text = "Guardá los datos de la empresa para gestionar sucursales",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Foto de empresa
            HorizontalDivider(color = colors.border)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Foto de empresa", fontSize = 13.sp, color = colors.textSecondary)
                OutlinedButton(
                    onClick = { imageLauncher.launch("image/*") },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (pendingPhotoUri != null) "Cambiar foto" else "Agregar foto", fontSize = 12.sp)
                }
            }
            if (pendingPhotoUri != null) {
                Text("📷 Foto seleccionada (se sube al guardar)", fontSize = 11.sp, color = Color(0xFF4CAF50))
            }

            // Categorías de empresa (solo las que el prestador ya eligió)
            HorizontalDivider(color = colors.border)
            Text("Categorías de la empresa", fontSize = 13.sp, color = colors.textSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))
            if (categoriasPrestador.isEmpty()) {
                Text(
                    "Primero seleccioná categorías en la sección Categorías",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categoriasPrestador.forEach { cat ->
                        val selected = cat in localCategories
                        FilterChip(
                            selected = selected,
                            onClick = {
                                localCategories = if (selected)
                                    localCategories - cat
                                else
                                    localCategories + cat
                            },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botones Cancelar / Guardar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (nombreEmpresa.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            localNombre = nombreEmpresa
                            localRazon = razonSocial
                            cuitValue = TextFieldValue(cuitEmpresa, TextRange(cuitEmpresa.length))
                            cuitError = ""
                            localProvincia = provincia
                            localLocalidad = localidad
                            localCP = codigoPostal
                            localCalle = calle
                            localNumero = numero
                            localHorario = horario
                            localCategories = categoriesEmpresa
                            editando = false
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) { Text("Cancelar", fontSize = 13.sp) }
                }
                Button(
                    onClick = {
                        if (localNombre.isBlank()) return@Button
                        if (cuitError.isNotEmpty()) return@Button
                        onGuardar(localNombre, localRazon, cuitValue.text, localProvincia, localLocalidad, localCP, localCalle, localNumero, localHorario, localCategories, null, pendingPhotoUri, geocodedLat, geocodedLng, localPais)
                        editando = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) { Text("Guardar", fontSize = 13.sp) }
            }
        }
    }
}

private fun validarCuit(digits: String): Boolean {
    if (digits.length != 11) return false
    val weight = intArrayOf(5, 4, 3, 2, 7, 6, 5, 4, 3, 2)
    val sum = weight.indices.sumOf { digits[it].digitToInt() * weight[it] }
    val resto = sum % 11
    val digitoVerificador = if (resto == 0) 0 else 11 - resto
    if (digitoVerificador == 10) return false
    return digitoVerificador == digits[10].digitToInt()
}

// SECCIÓN: Configuración de Servicios
@Composable
fun ServiceConfigSection(
    atencionUrgencias: Boolean,
    onAtencionUrgenciasChange: (Boolean) -> Unit,
    vaDomicilio: Boolean,
    onVaDomicilioChange: (Boolean) -> Unit,
    envios: Boolean,
    onEnviosChange: (Boolean) -> Unit,
    turnosEnLocal: Boolean,
    onTurnosEnLocalChange: (Boolean) -> Unit,
    direccionLocal: String,
    onDireccionLocalChange: (String) -> Unit,
    provinciaLocal: String,
    onProvinciaLocalChange: (String) -> Unit,
    codigoPostalLocal: String,
    onCodigoPostalLocalChange: (String) -> Unit,
    horarioLocal: String,
    onHorarioLocalChange: (String) -> Unit,
    serviceType: ServiceType,
    onServiceTypeClick: () -> Unit,
    doesService: Boolean,
    onDoesServiceChange: (Boolean) -> Unit,
    doesProduct: Boolean,
    onDoesProductChange: (Boolean) -> Unit,
    providerId: String? = null,
    isEmpresaMode: Boolean = false,
    direccionEmpresa: String = "",
    expanded: Boolean,
    onExpandChange: () -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    var mostrarInfoServicio by remember { mutableStateOf(false) }

    ArchiveroSection(
        title = "Configuración de Servicios",
        sectionId = "services",
        icon = Icons.Default.Settings,
        color = Color(0xFF9C27B0),
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandChange = { onExpandChange() },
        headerTrailingContent = {
            Box {
                IconButton(
                    onClick = { mostrarInfoServicio = !mostrarInfoServicio },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.HelpOutline,
                        contentDescription = "Información",
                        tint = if (mostrarInfoServicio) Color(0xFF2196F3) else colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = mostrarInfoServicio,
                    onDismissRequest = { mostrarInfoServicio = false }
                ) {
                    Column(modifier = Modifier.padding(12.dp).widthIn(max = 260.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (serviceType == ServiceType.PROFESSIONAL) "Configurá tu agenda:" else "Configurá tus servicios:",
                                color = Color(0xFF2196F3),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val bullets = if (serviceType == ServiceType.PROFESSIONAL) listOf(
                            "Agregá los días y horarios en que atendés.",
                            "Podés tener múltiples franjas horarias (mañana y tarde).",
                            "La duración del turno define cada cuánto se reservan citas.",
                            "Tus clientes verán solo los turnos disponibles."
                        ) else listOf(
                            "Podés activar una o más opciones.",
                            "Urgencias 24/7: emergencias fuera de horario.",
                            "A domicilio: te desplazás al cliente.",
                            "Envíos: mandás productos o materiales.",
                            "Turnos en local: el cliente viene a vos."
                        )
                        bullets.forEach { item ->
                            Row(
                                modifier = Modifier.padding(top = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("•  ", color = Color(0xFF2196F3), fontSize = 13.sp)
                                Text(item, color = colors.textPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    ) {
        // Tipo de servicio — siempre visible arriba
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = colors.primaryOrange.copy(alpha = 0.1f),
            onClick = onServiceTypeClick
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (serviceType) {
                        ServiceType.TECHNICAL -> Icons.Default.Build
                        ServiceType.PROFESSIONAL -> Icons.Default.CalendarMonth
                        ServiceType.RENTAL -> Icons.Default.Stadium
                        ServiceType.OTHER -> Icons.Default.MoreHoriz
                    },
                    contentDescription = null,
                    tint = colors.primaryOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Tipo de servicio", fontSize = 12.sp, color = colors.textSecondary)
                    Text(
                        text = serviceType.displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.primaryOrange
                    )
                }
                Icon(Icons.Default.Edit, contentDescription = "Cambiar", tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Qué ofrece este prestador
        ModalidadToggleCard(
            icon = Icons.Default.Build,
            label = "Ofrece servicios",
            description = if (doesService) "Aparece en búsquedas de servicios" else "No aparece en búsquedas de servicios",
            checked = doesService,
            onCheckedChange = onDoesServiceChange,
            accentColor = Color(0xFF9C27B0),
            colors = colors
        )
        Spacer(modifier = Modifier.height(8.dp))
        ModalidadToggleCard(
            icon = Icons.Default.Store,
            label = "Vende productos",
            description = if (doesProduct) "Aparece en búsquedas de productos" else "No aparece en búsquedas de productos",
            checked = doesProduct,
            onCheckedChange = onDoesProductChange,
            accentColor = Color(0xFF9C27B0),
            colors = colors
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Configuración según el tipo de servicio
        when (serviceType) {
            ServiceType.PROFESSIONAL -> {
                // Para profesionales con agenda
                AvailabilityScheduleSection(colors = colors)
            }
            ServiceType.TECHNICAL -> {
                TechnicalServiceConfig(
                    atencionUrgencias = atencionUrgencias,
                    onAtencionUrgenciasChange = onAtencionUrgenciasChange,
                    vaDomicilio = vaDomicilio,
                    onVaDomicilioChange = onVaDomicilioChange,
                    envios = envios,
                    onEnviosChange = onEnviosChange,
                    turnosEnLocal = turnosEnLocal,
                    onTurnosEnLocalChange = onTurnosEnLocalChange,
                    direccionLocal = direccionLocal,
                    onDireccionLocalChange = onDireccionLocalChange,
                    provinciaLocal = provinciaLocal,
                    onProvinciaLocalChange = onProvinciaLocalChange,
                    codigoPostalLocal = codigoPostalLocal,
                    onCodigoPostalLocalChange = onCodigoPostalLocalChange,
                    isEmpresaMode = isEmpresaMode,
                    horarioLocal = horarioLocal,
                    onHorarioLocalChange = onHorarioLocalChange,
                    direccionEmpresa = direccionEmpresa,
                    mostrarInfoServicio = mostrarInfoServicio,
                    colors = colors
                )
            }
            ServiceType.RENTAL -> {
                RentalSpacesSection(colors = colors, providerId = providerId)
            }
            ServiceType.OTHER -> {
                TechnicalServiceConfig(
                    atencionUrgencias = atencionUrgencias,
                    onAtencionUrgenciasChange = onAtencionUrgenciasChange,
                    vaDomicilio = vaDomicilio,
                    onVaDomicilioChange = onVaDomicilioChange,
                    envios = envios,
                    onEnviosChange = onEnviosChange,
                    turnosEnLocal = turnosEnLocal,
                    onTurnosEnLocalChange = onTurnosEnLocalChange,
                    direccionLocal = direccionLocal,
                    onDireccionLocalChange = onDireccionLocalChange,
                    provinciaLocal = provinciaLocal,
                    onProvinciaLocalChange = onProvinciaLocalChange,
                    codigoPostalLocal = codigoPostalLocal,
                    onCodigoPostalLocalChange = onCodigoPostalLocalChange,
                    isEmpresaMode = isEmpresaMode,
                    horarioLocal = horarioLocal,
                    onHorarioLocalChange = onHorarioLocalChange,
                    direccionEmpresa = direccionEmpresa,
                    mostrarInfoServicio = mostrarInfoServicio,
                    colors = colors
                )
            }
        }
    }
}

@Composable
fun RentalSpacesSection(colors: PrestadorColors, providerId: String?) {
    TODO("Not yet implemented")
}

@Composable
fun AvailabilityScheduleSection(colors: PrestadorColors) {
    // --- SECCIÓN: HORARIOS DE DISPONIBILIDAD ---
    // Implementación básica para evitar el crash por TODO()
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Configuración de Horarios",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary
        )
        Text(
            text = "Próximamente: Podrás configurar tus horarios de atención detallados aquí.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
    }
}

// Extraer la configuración actual de servicios técnicos en su propia función
@Composable
private fun TechnicalServiceConfig(
    atencionUrgencias: Boolean,
    onAtencionUrgenciasChange: (Boolean) -> Unit,
    vaDomicilio: Boolean,
    onVaDomicilioChange: (Boolean) -> Unit,
    envios: Boolean,
    onEnviosChange: (Boolean) -> Unit,
    turnosEnLocal: Boolean,
    onTurnosEnLocalChange: (Boolean) -> Unit,
    direccionLocal: String,
    onDireccionLocalChange: (String) -> Unit,
    provinciaLocal: String,
    onProvinciaLocalChange: (String) -> Unit,
    codigoPostalLocal: String,
    onCodigoPostalLocalChange: (String) -> Unit,
    isEmpresaMode: Boolean,
    horarioLocal: String,
    onHorarioLocalChange: (String) -> Unit,
    direccionEmpresa: String,
    mostrarInfoServicio: Boolean,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    Column {
        Spacer(modifier = Modifier.height(4.dp))
        
        SwitchOption(
            label = "Atención de urgencias 24/7",
            icon = Icons.Default.Notifications,
            checked = atencionUrgencias,
            onCheckedChange = onAtencionUrgenciasChange,
            colors = colors,
            description = "Para emergencias fuera de horario normal"
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        SwitchOption(
            label = "Servicio a domicilio",
            icon = Icons.Default.DirectionsCar,
            checked = vaDomicilio,
            onCheckedChange = onVaDomicilioChange,
            colors = colors,
            description = "Te desplazás al domicilio del cliente"
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        SwitchOption(
            label = "Envíos",
            icon = Icons.Default.LocalShipping,
            checked = envios,
            onCheckedChange = onEnviosChange,
            colors = colors,
            description = "Realizás envíos de productos o materiales"
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        SwitchOption(
            label = "Atención en local/taller",
            icon = Icons.Default.Store,
            checked = turnosEnLocal,
            onCheckedChange = onTurnosEnLocalChange,
            colors = colors,
            description = "El cliente va a tu local o taller"
        )
        
        // Campos de dirección del local (solo si turnosEnLocal está activo)
        AnimatedVisibility(
            visible = turnosEnLocal,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Si es modo empresa y tiene dirección de empresa, mostrarla
                if (isEmpresaMode && direccionEmpresa.isNotBlank()) {
                    // Mostrar dirección de la empresa
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = colors.surfaceColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = null,
                                    tint = colors.primaryOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Dirección principal",
                                    color = colors.textSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = direccionEmpresa,
                                color = colors.textPrimary,
                                fontSize = 15.sp
                            )
                        }
                    }
                    
                } else {
                    // Modo personal: campos editables con autocomplete
                    var mostrarSugsProvLocal by remember { mutableStateOf(false) }
                    var mostrarSugsLocalLocal by remember { mutableStateOf(false) }
                    var localidadLocalTexto by remember { mutableStateOf("") }
                    val provInteraction = remember { MutableInteractionSource() }
                    val locInteraction = remember { MutableInteractionSource() }
                    val provFocused by provInteraction.collectIsFocusedAsState()
                    val locFocused by locInteraction.collectIsFocusedAsState()
                    val provinciasFiltradas = if (provinciaLocal.isBlank()) emptyList()
                        else PROVINCIAS_ARGENTINA.filter { it.contains(provinciaLocal.trim(), ignoreCase = true) }
                    val localidadesDeProv = LOCALIDADES_POR_PROVINCIA.entries
                        .firstOrNull { it.key.equals(provinciaLocal.trim(), ignoreCase = true) }?.value ?: emptyList()
                    val localidadesFiltradas = if (localidadLocalTexto.isBlank()) localidadesDeProv
                        else localidadesDeProv.filter { it.nombre.contains(localidadLocalTexto.trim(), ignoreCase = true) }

                    LaunchedEffect(provFocused) { if (provFocused) mostrarSugsProvLocal = true }
                    LaunchedEffect(locFocused) { if (locFocused) mostrarSugsLocalLocal = true }

                    // 1) Provincia con autocomplete
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FloatingLabelTextField(
                            value = provinciaLocal,
                            onValueChange = { onProvinciaLocalChange(it); mostrarSugsProvLocal = true },
                            label = "Provincia",
                            leadingIcon = Icons.Default.Place,
                            interactionSource = provInteraction
                        )
                        AnimatedVisibility(
                            visible = mostrarSugsProvLocal && provinciasFiltradas.isNotEmpty(),
                            enter = fadeIn(tween(200)) + expandVertically(tween(250)),
                            exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                        ) {
                            Surface(shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp), shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    provinciasFiltradas.take(6).forEach { prov ->
                                        Text(
                                            text = prov,
                                            modifier = Modifier.fillMaxWidth().clickable {
                                                onProvinciaLocalChange(prov)
                                                mostrarSugsProvLocal = false
                                            }.padding(horizontal = 16.dp, vertical = 12.dp),
                                            fontSize = 14.sp
                                        )
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // 2) Direccion y Localidad en fila
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val scope = rememberCoroutineScope()
                    var geocodingLoading by remember { mutableStateOf(false) }
                    val locationPermissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { granted ->
                        if (granted) {
                            scope.launch {
                                geocodingLoading = true
                                try {
                                    val fusedClient = com.google.android.gms.location.LocationServices
                                        .getFusedLocationProviderClient(context)
                                    @Suppress("MissingPermission")
                                    val loc = fusedClient.lastLocation.await()
                                    if (loc != null) {
                                        val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                                        @Suppress("DEPRECATION")
                                        val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                                        if (!addresses.isNullOrEmpty()) {
                                            val addr = addresses[0]
                                            val calle = buildString {
                                                if (!addr.thoroughfare.isNullOrBlank()) append(addr.thoroughfare)
                                                if (!addr.subThoroughfare.isNullOrBlank()) append(" ${addr.subThoroughfare}")
                                            }
                                            if (calle.isNotBlank()) onDireccionLocalChange(calle)
                                            if (!addr.locality.isNullOrBlank()) localidadLocalTexto = addr.locality!!
                                            if (!addr.adminArea.isNullOrBlank()) onProvinciaLocalChange(addr.adminArea!!)
                                            if (!addr.postalCode.isNullOrBlank()) onCodigoPostalLocalChange(addr.postalCode!!)
                                        }
                                    }
                                } catch (e: Exception) {
                                    // silently ignore
                                } finally {
                                    geocodingLoading = false
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FloatingLabelTextField(
                            value = direccionLocal,
                            onValueChange = onDireccionLocalChange,
                            label = "Direcci\u00f3n",
                            leadingIcon = Icons.Default.Store,
                            trailingContent = {
                                if (geocodingLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.primaryOrange)
                                } else {
                                    IconButton(onClick = {
                                        locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                                    }) {
                                        Icon(Icons.Default.MyLocation, contentDescription = "Detectar ubicaci\u00f3n", tint = colors.primaryOrange)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            FloatingLabelTextField(
                                value = localidadLocalTexto,
                                onValueChange = { localidadLocalTexto = it; mostrarSugsLocalLocal = true },
                                label = "Localidad",
                                leadingIcon = Icons.Default.LocationCity,
                                interactionSource = locInteraction
                            )
                            AnimatedVisibility(
                                visible = mostrarSugsLocalLocal && localidadesFiltradas.isNotEmpty(),
                                enter = fadeIn(tween(200)) + expandVertically(tween(250)),
                                exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                            ) {
                                Surface(shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp), shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                                    Column {
                                        localidadesFiltradas.take(5).forEach { loc ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    localidadLocalTexto = loc.nombre
                                                    onCodigoPostalLocalChange(loc.codigoPostal)
                                                    mostrarSugsLocalLocal = false
                                                }.padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(loc.nombre, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                                Text(loc.codigoPostal, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            HorizontalDivider()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // 3) Codigo Postal
                    FloatingLabelTextField(
                        value = codigoPostalLocal,
                        onValueChange = { v ->
                            val filtered = v.filter { it.isDigit() || it.isLetter() }.uppercase()
                            onCodigoPostalLocalChange(filtered)
                        },
                        label = "Codigo Postal",
                        leadingIcon = Icons.Default.PinDrop,
                        keyboardType = KeyboardType.Text
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    HorarioSelectorField(
                        horario = horarioLocal,
                        onHorarioChange = onHorarioLocalChange
                    )
                }
            }
        }
    }
}

// COMPONENTE: Sección estilo Archivero (Colapsable)
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ArchiveroSection(
    title: String,
    sectionId: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onExpandChange: (String) -> Unit = {},
    headerTrailingContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = getPrestadorColors()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceColor,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(color)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Header clickable
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExpandChange(sectionId) }
                        .padding(bottom = if (expanded) 16.dp else 0.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    headerTrailingContent?.invoke()
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Colapsar" else "Expandir",
                        tint = color
                    )
                }
                
                // Contenido expandible
                androidx.compose.animation.AnimatedVisibility(
                    visible = expanded,
                    enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                ) {
                    LaunchedEffect(true) {
                        kotlinx.coroutines.delay(300)
                        bringIntoViewRequester.bringIntoView()
                    }
                    Column(modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester)) {
                        content()
                    }
                }
            }
        }
    }
}

// COMPONENTE: Switch con icono
@Composable
fun SwitchOption(
    label: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    description: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.backgroundColor.copy(alpha = 0.5f))
            .clickable { onCheckedChange(!checked) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) colors.primaryOrange else colors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = colors.textPrimary,
                fontWeight = if (checked) FontWeight.Medium else FontWeight.Normal
            )
            if (description != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = colors.textSecondary.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.primaryOrange,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = colors.textSecondary.copy(alpha = 0.3f)
            )
        )
    }
}


// Sección independiente: Portafolio de fotos
@Composable
fun GaleriaSection(
    imagenesJson: String,
    empresaId: String,
    onImagenesActualizadas: (String) -> Unit,
    expanded: Boolean,
    onExpandChange: (String) -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    ArchiveroSection(
        title = "Portafolio",
        sectionId = "galeria",
        icon = Icons.Default.PhotoLibrary,
        color = Color(0xFF1976D2),
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandChange = onExpandChange
    ) {
        GaleriaInlineContent(
            imagenesJson = imagenesJson,
            empresaId = empresaId,
            onImagenesActualizadas = onImagenesActualizadas,
            colors = colors
        )
    }
}

// Galería inline
@Composable
fun GaleriaInlineContent(
    imagenesJson: String,
    empresaId: String,
    onImagenesActualizadas: (String) -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    val scope = rememberCoroutineScope()
    var imagenes: List<String> by remember(imagenesJson) {
        mutableStateOf<List<String>>(
            try {
                val arr = org.json.JSONArray(imagenesJson)
                (0 until arr.length()).map { i -> arr.getString(i) }
            } catch (e: Exception) {
                emptyList()
            }
        )
    }
    var isUploading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { selectedUri ->
            scope.launch {
                isUploading = true
                val nuevaUrl: String? = try {
                    val ref = com.google.firebase.storage.FirebaseStorage.getInstance()
                        .reference
                        .child("empresas/$empresaId/galeria/${java.util.UUID.randomUUID()}.jpg")
                    ref.putFile(selectedUri).await()
                    ref.downloadUrl.await().toString()
                } catch (e: Exception) { null }
                if (nuevaUrl != null) {
                    val nuevaLista: List<String> = imagenes + nuevaUrl
                    imagenes = nuevaLista
                    val arr = org.json.JSONArray()
                    nuevaLista.forEach { s -> arr.put(s) }
                    onImagenesActualizadas(arr.toString())
                }
                isUploading = false
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Photo,
                contentDescription = null,
                tint = colors.primaryOrange,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Galeria de imágenes (${imagenes.size}/5)",
                fontSize = 15.sp,
                color = colors.textPrimary
            )
        }
        if (isUploading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = colors.primaryOrange,
                strokeWidth = 2.dp
            )
        } else {
            val llena = imagenes.size >= 5
            OutlinedButton(
                onClick = { if (!llena) launcher.launch("image/*") },
                enabled = !llena,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryOrange),
                border = BorderStroke(1.dp, if (llena) colors.textSecondary else
                colors.primaryOrange),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (llena) "Limite 5 fotos" else "Agregar", fontSize = 13.sp)
            }

        }
    }

    if (imagenes.isEmpty()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, colors.textSecondary.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = colors.textSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "Sin fotos a\u00fan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )
                Text(
                    text = "Mostr\u00e1 tus trabajos agregando fotos",
                    fontSize = 12.sp,
                    color = colors.textSecondary.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        // Grilla 2 columnas
        val filas = imagenes.chunked(2)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            filas.forEach { fila ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    fila.forEach { url ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Botón eliminar sobre la imagen
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .clickable {
                                        val nuevaLista: List<String> = imagenes.filter { it != url }
                                        imagenes = nuevaLista
                                        val arr = org.json.JSONArray()
                                        nuevaLista.forEach { s -> arr.put(s) }
                                        onImagenesActualizadas(arr.toString())
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Eliminar",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    // Si la fila tiene solo 1 foto, completar con placeholder vacío
                    if (fila.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalModalidadBlock(
    turnosEnLocal: Boolean,
    onTurnosEnLocalChange: (Boolean) -> Unit,
    vaDomicilio: Boolean,
    onVaDomicilioChange: (Boolean) -> Unit,
    atiendeVirtual: Boolean,
    onAtiendeVirtualChange: (Boolean) -> Unit,
    provinciaLocal: String,
    onProvinciaLocalChange: (String) -> Unit,
    direccionLocal: String,
    onDireccionLocalChange: (String) -> Unit,
    codigoPostalLocal: String,
    onCodigoPostalLocalChange: (String) -> Unit,
    consultorioDireccion: com.example.myapplication.prestador.data.local.entity.DireccionEntity?,
    onGuardarConsultorio: (pais: String, provincia: String, localidad: String, cp: String, calle: String, numero: String, latitud: Double?, longitud: Double?) -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    val purple = Color(0xFF9C27B0)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ModalidadToggleCard(
            icon = Icons.Default.MeetingRoom,
            label = "En consultorio / oficina",
            description = if (turnosEnLocal) "El cliente va a tu consultorio" else "Activar para cargar direccion",
            checked = turnosEnLocal,
            onCheckedChange = onTurnosEnLocalChange,
            accentColor = purple,
            colors = colors
        )
        AnimatedVisibility(
            visible = turnosEnLocal,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            var consultorioExpanded by remember { mutableStateOf(consultorioDireccion == null) }
            DireccionSection(
                titulo = "Direccion del consultorio / oficina",
                direccion = consultorioDireccion,
                expanded = consultorioExpanded,
                onExpandChange = { consultorioExpanded = !consultorioExpanded },
                onGuardar = onGuardarConsultorio
            )
        }
        ModalidadToggleCard(
            icon = Icons.Default.DirectionsCar,
            label = "Visitas a domicilio",
            description = "Te desplazas al domicilio del cliente",
            checked = vaDomicilio,
            onCheckedChange = onVaDomicilioChange,
            accentColor = purple,
            colors = colors
        )
        ModalidadToggleCard(
            icon = Icons.Default.VideoCall,
            label = "Atencion online / virtual",
            description = "Atendes por videollamada u otro medio",
            checked = atiendeVirtual,
            onCheckedChange = onAtiendeVirtualChange,
            accentColor = purple,
            colors = colors
        )
    }
}

@Composable
private fun ModalidadToggleCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
        border = BorderStroke(1.5.dp, accentColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Text(description, fontSize = 12.sp, color = colors.textSecondary)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.scale(0.85f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = colors.textSecondary.copy(alpha = 0.3f)
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SECCIÓN: Direcciones adicionales del prestador
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DireccionesAdicionalesSection(
    addresses: List<AddressProvider>,
    expanded: Boolean,
    onExpandChange: () -> Unit,
    onAddAddress: (AddressProvider) -> Unit,
    onRemoveAddress: (String) -> Unit,
    colors: PrestadorColors
) {
    var showDialog by remember { mutableStateOf(false) }

    ArchiveroSection(
        title = "Mis Direcciones",
        sectionId = "direcciones_adicionales",
        icon = Icons.Default.AddLocationAlt,
        color = Color(0xFF7B1FA2),
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandChange = { onExpandChange() }
    ) {
        if (addresses.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = colors.surfaceElevated,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.LocationOff,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No tenés direcciones adicionales",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        } else {
            addresses.forEach { addr ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = colors.surfaceElevated,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF7B1FA2).copy(alpha = 0.12f)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF7B1FA2),
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            val calleNum = listOf(addr.calle, addr.numero)
                                .filter { it.isNotBlank() }.joinToString(" ")
                            val ciudad = listOf(addr.localidad, addr.provincia)
                                .filter { it.isNotBlank() }.joinToString(", ")
                            if (calleNum.isNotBlank()) {
                                Text(
                                    calleNum,
                                    fontSize = 14.sp,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (ciudad.isNotBlank()) {
                                Text(ciudad, fontSize = 12.sp, color = colors.textSecondary)
                            }
                            if (addr.codigoPostal.isNotBlank()) {
                                Text(
                                    "CP ${addr.codigoPostal}",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        IconButton(onClick = { onRemoveAddress(addr.id) }) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Eliminar dirección",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7B1FA2)),
            border = BorderStroke(1.dp, Color(0xFF7B1FA2))
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Agregar dirección", fontSize = 13.sp)
        }
    }

    if (showDialog) {
        AgregarDireccionDialog(
            colors = colors,
            onDismiss = { showDialog = false },
            onConfirm = { addr ->
                onAddAddress(addr)
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarDireccionDialog(
    colors: PrestadorColors,
    onDismiss: () -> Unit,
    onConfirm: (AddressProvider) -> Unit
) {
    var calle by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var localidad by remember { mutableStateOf("") }
    var provincia by remember { mutableStateOf("") }
    var codigoPostal by remember { mutableStateOf("") }
    var calleError by remember { mutableStateOf(false) }
    var provinciaError by remember { mutableStateOf(false) }
    var mostrarSugerencias by remember { mutableStateOf(false) }
    var geocodingLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val provinciasFiltradas = if (provincia.isBlank()) emptyList()
        else PROVINCIAS_ARGENTINA.filter { it.contains(provincia.trim(), ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceColor,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AddLocationAlt,
                    contentDescription = null,
                    tint = Color(0xFF7B1FA2),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Nueva dirección",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Provincia con autocomplete
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = provincia,
                        onValueChange = {
                            provincia = it
                            provinciaError = false
                            mostrarSugerencias = it.isNotEmpty()
                        },
                        label = { Text("Provincia *") },
                        leadingIcon = {
                            Icon(Icons.Default.Map, contentDescription = null, tint = colors.textSecondary)
                        },
                        isError = provinciaError,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7B1FA2),
                            unfocusedBorderColor = colors.border,
                            focusedLabelColor = Color(0xFF7B1FA2)
                        )
                    )
                    AnimatedVisibility(
                        visible = mostrarSugerencias && provinciasFiltradas.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                            color = colors.surfaceElevated,
                            shadowElevation = 4.dp
                        ) {
                            Column {
                                provinciasFiltradas.take(5).forEach { prov ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                provincia = prov
                                                provinciaError = false
                                                mostrarSugerencias = false
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Text(prov, fontSize = 13.sp, color = colors.textPrimary)
                                    }
                                    HorizontalDivider(color = colors.border)
                                }
                            }
                        }
                    }
                }
                if (provinciaError) {
                    Text(
                        "La provincia es requerida",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp
                    )
                }

                FloatingLabelTextField(
                    value = localidad,
                    onValueChange = { localidad = it },
                    label = "Localidad",
                    leadingIcon = Icons.Default.LocationCity
                )
                FloatingLabelTextField(
                    value = codigoPostal,
                    onValueChange = { codigoPostal = it },
                    label = "Código Postal",
                    leadingIcon = Icons.Default.PinDrop,
                    keyboardType = KeyboardType.Number
                )
                OutlinedTextField(
                    value = calle,
                    onValueChange = { calle = it; calleError = false },
                    label = { Text("Calle *") },
                    leadingIcon = {
                        Icon(Icons.Default.EditRoad, contentDescription = null, tint = colors.textSecondary)
                    },
                    trailingIcon = {
                        if (geocodingLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF7B1FA2)
                            )
                        } else {
                            IconButton(onClick = {
                                scope.launch {
                                    geocodingLoading = true
                                    try {
                                        val fusedClient = com.google.android.gms.location.LocationServices
                                            .getFusedLocationProviderClient(context)
                                        @Suppress("MissingPermission")
                                        val loc = fusedClient.lastLocation.await()
                                        if (loc != null) {
                                            val geocoder = android.location.Geocoder(
                                                context, java.util.Locale.getDefault()
                                            )
                                            @Suppress("DEPRECATION")
                                            val addrs = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                                            if (!addrs.isNullOrEmpty()) {
                                                val a = addrs[0]
                                                val calleDetectada = buildString {
                                                    if (!a.thoroughfare.isNullOrBlank()) append(a.thoroughfare)
                                                    if (!a.subThoroughfare.isNullOrBlank()) append(" ${a.subThoroughfare}")
                                                }
                                                if (calleDetectada.isNotBlank()) {
                                                    calle = calleDetectada
                                                    calleError = false
                                                }
                                                if (!a.subThoroughfare.isNullOrBlank()) numero = a.subThoroughfare!!
                                                if (!a.locality.isNullOrBlank()) localidad = a.locality!!
                                                if (!a.adminArea.isNullOrBlank()) {
                                                    provincia = a.adminArea!!
                                                    provinciaError = false
                                                }
                                                if (!a.postalCode.isNullOrBlank()) codigoPostal = a.postalCode!!
                                            }
                                        }
                                    } catch (_: Exception) {
                                    } finally {
                                        geocodingLoading = false
                                    }
                                }
                            }) {
                                Icon(
                                    Icons.Default.MyLocation,
                                    contentDescription = "Detectar ubicación",
                                    tint = Color(0xFF7B1FA2)
                                )
                            }
                        }
                    },
                    isError = calleError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7B1FA2),
                        unfocusedBorderColor = colors.border,
                        focusedLabelColor = Color(0xFF7B1FA2)
                    )
                )
                if (calleError) {
                    Text(
                        "La calle es requerida",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp
                    )
                }
                FloatingLabelTextField(
                    value = numero,
                    onValueChange = { numero = it },
                    label = "Número",
                    leadingIcon = Icons.Default.Numbers,
                    keyboardType = KeyboardType.Number
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    calleError = calle.isBlank()
                    provinciaError = provincia.isBlank()
                    if (!calleError && !provinciaError) {
                        onConfirm(
                            AddressProvider(
                                calle = calle.trim(),
                                numero = numero.trim(),
                                localidad = localidad.trim(),
                                provincia = provincia.trim(),
                                pais = "Argentina",
                                codigoPostal = codigoPostal.trim()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = colors.textSecondary)
            }
        }
    )
}