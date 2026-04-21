package com.example.myapplication.prestador.ui.register

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.ui.theme.*
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.data.model.ServiceType
import kotlinx.coroutines.delay
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.myapplication.prestador.data.model.ServicioFirebase
import com.google.firebase.auth.FirebaseAuth
import com.example.myapplication.prestador.ui.register.components.*


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PrestadorRegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    isGoogleUser: Boolean = false,
    viewModel: PrestadorRegisterViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    // Email de la cuenta de Google (solo presente cuando isGoogleUser = true)
    val googleEmail = remember {
        if (isGoogleUser) FirebaseAuth.getInstance().currentUser?.email ?: "" else ""
    }

    // Estados del formulario
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf(ServiceType.TECHNICAL) }
    var expandedTipoServicio by remember { mutableStateOf(false) }
    var expandedCategoria by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }




    // Variables requeridas por componentes reusables (no usadas en registro inicial)
    var serviciosSeleccionados by remember { mutableStateOf(listOf<String>()) }
    var searchQuery by remember { mutableStateOf("") }
    var showServiceModal by remember { mutableStateOf(false) }
    var tempSelectedServices by remember { mutableStateOf(setOf<String>()) }
    var showSuggestions by remember { mutableStateOf(false) }
    var atencionUrgencias by remember { mutableStateOf(false) }
    var vaDomicilio by remember { mutableStateOf(false) }
    var turnosEnLocal by remember { mutableStateOf(false) }
    var tieneEmpresa by remember { mutableStateOf(false) }
    var nombreEmpresa by remember { mutableStateOf("") }
    var razonSocial by remember { mutableStateOf("") }
    var cuit by remember { mutableStateOf("") }
    var sucursales by remember { mutableStateOf(listOf(Sucursal("", ""))) }
    var showMatriculaTooltip by remember { mutableStateOf(false) }

    // Sección expandida (acordeón: solo una a la vez)
    // Google users: arranca en "personal" (no necesitan datos de acceso)
    var expandedSection by remember { mutableStateOf<String?>(if (isGoogleUser) "personal" else "acceso") }

    val registerState by viewModel.registerState.collectAsState()
    val servicios by viewModel.servicios.collectAsState()
    val loadingServicios by viewModel.loadingServicios.collectAsState()
    val serviciosAgrupados = remember (servicios) { servicios.groupBy { it.superCategory }}

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> profileImageUri = uri }

    LaunchedEffect(registerState) {
        when (registerState) {
            is RegisterState.Success -> onRegisterSuccess()
            else -> {}
        }
    }

    Scaffold(
        containerColor = colors.backgroundColor,
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = colors.surfaceColor
            ) {
                Button(
                    onClick = {
                        viewModel.register(
                            email = email,
                            password = password,
                            nombre = nombre,
                            apellido = apellido,
                            categoria = categoriaSeleccionada,
                            mensaje = mensaje,
                            serviceType = serviceType.name,
                            isGoogleUser = isGoogleUser
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (registerState is RegisterState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Crear cuenta", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // ── HERO HEADER ──────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    colors.primaryOrange.copy(alpha = 0.25f),
                                    colors.backgroundColor
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Barra superior con botón volver
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBackToLogin) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = colors.primaryOrange
                                )
                            }
                            Text(
                                text = "Crear perfil",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Foto de perfil circular
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .border(3.dp, colors.primaryOrange, CircleShape)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileImageUri != null) {
                                AsyncImage(
                                    model = profileImageUri,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(colors.primaryOrange.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp),
                                        tint = colors.primaryOrange.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            // Botón cámara
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(32.dp),
                                shape = CircleShape,
                                color = colors.primaryOrange,
                                shadowElevation = 4.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = "Cambiar foto",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "$nombre $apellido".trim().ifEmpty { "Nuevo prestador" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (categoriaSeleccionada.isNotEmpty()) categoriaSeleccionada else serviceType.displayName,
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // ── SECCIÓN: Datos de acceso (solo para registro manual, no Google) ──
            if (!isGoogleUser) {
                item {
                    RegisterSectionCard(
                        title = "Datos de acceso",
                        icon = Icons.Default.Lock,
                        color = colors.primaryOrange,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        expanded = expandedSection == "acceso",
                        onExpandChange = {
                            expandedSection = if (expandedSection == "acceso") null else "acceso"
                        }
                    ) {
                        FloatingLabelTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Correo electrónico",
                            leadingIcon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        FloatingLabelTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Contraseña",
                            leadingIcon = Icons.Default.Lock,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            onTrailingIconClick = { passwordVisible = !passwordVisible }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }

            // ── SECCIÓN: Información personal ─────────────────────────────
            item {
                RegisterSectionCard(
                    title = "Información personal",
                    icon = Icons.Default.Person,
                    color = Color(0xFF1976D2),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    expanded = expandedSection == "personal",
                    onExpandChange = {
                        expandedSection = if (expandedSection == "personal") null else "personal"
                    }
                ) {
                    // Banner cuenta de Google (solo si vino de Google)
                    if (isGoogleUser && googleEmail.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1976D2).copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, Color(0xFF1976D2).copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF1976D2),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Cuenta de Google vinculada",
                                        color = Color(0xFF1976D2),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = googleEmail,
                                        color = colors.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    // Banner informativo
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = colors.primaryOrange.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, colors.primaryOrange.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Completá lo esencial ahora. Podés agregar más datos desde Editar perfil.",
                                color = colors.textPrimary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FloatingLabelTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = "Nombre",
                        leadingIcon = Icons.Default.Person
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FloatingLabelTextField(
                        value = apellido,
                        onValueChange = { apellido = it },
                        label = "Apellido",
                        leadingIcon = Icons.Default.Person
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // ── SECCIÓN: Tu servicio ───────────────────────────────────────
            item {
                RegisterSectionCard(
                    title = "Tu servicio",
                    icon = Icons.Default.Build,
                    color = Color(0xFF00897B),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    expanded = expandedSection == "servicio",
                    onExpandChange = {
                        expandedSection = if (expandedSection == "servicio") null else "servicio"
                    }
                ) {
                    // Dropdown Tipo de servicio
                    ExposedDropdownMenuBox(
                        expanded = expandedTipoServicio,
                        onExpandedChange = { expandedTipoServicio = !expandedTipoServicio }
                    ) {
                        OutlinedTextField(
                            value = serviceType.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de servicio", color = colors.textSecondary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipoServicio) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00897B),
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTipoServicio,
                            onDismissRequest = { expandedTipoServicio = false }
                        ) {
                            ServiceType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.displayName) },
                                    onClick = {
                                        serviceType = type
                                        categoriaSeleccionada = "" // resetear al cambiar tipo
                                        expandedTipoServicio = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = serviceType.description,
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dropdown Categoría
                    ExposedDropdownMenuBox(
                        expanded = expandedCategoria,
                        onExpandedChange = { expandedCategoria = !expandedCategoria }
                    ) {
                        OutlinedTextField(
                            value = categoriaSeleccionada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría", color = colors.textSecondary) },
                            trailingIcon = {
                                if (loadingServicios) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF00897B)
                                    )
                                } else {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoria)
                                }
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00897B),
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategoria,
                            onDismissRequest = { expandedCategoria = false }
                        ) {
                            if (servicios.isEmpty() && !loadingServicios) {
                                DropdownMenuItem(
                                    text = { Text("No hay categoria disponibles", color = colors.textSecondary) },
                                    onClick = {}
                                )
                            } else {
                                serviciosAgrupados.forEach { (superCategoria, items) ->
                                    //Encabezado de grupo
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = superCategoria,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF00897B)
                                            )
                                        },
                                        onClick = {},
                                        enabled = false,

                                    )
                                    //Items del grupo
                                    items.forEach { servicio ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(text = servicio.icon, fontSize = 16.sp)
                                                    Text(text = servicio.name, fontSize = 14.sp)
                                                }
                                            },
                                            onClick = {
                                                categoriaSeleccionada = servicio.name
                                                expandedCategoria = false
                                            }
                                        )
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = mensaje,
                        onValueChange = { mensaje = it },
                        label = { Text("Mensaje de presentación", color = colors.textSecondary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00897B),
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Más adelante podés completar teléfono, dirección, empresa y horarios desde Editar perfil.",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Mostrar error si lo hay
    if (registerState is RegisterState.Error) {
        val errorMsg = (registerState as RegisterState.Error).message
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {},
            title = { Text("Error al registrar") },
            text = { Text(errorMsg) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetState() }) { Text("OK") }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// Tarjeta de sección estilo Archivero (igual que EditProfileScreenUnified)
// ─────────────────────────────────────────────────────────────────