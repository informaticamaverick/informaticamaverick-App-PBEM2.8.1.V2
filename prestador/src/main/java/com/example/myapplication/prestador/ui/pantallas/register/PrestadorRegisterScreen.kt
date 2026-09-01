package com.example.myapplication.prestador.ui.pantallas.register

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.prestador.ui.pantallas.register.componentes.*
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

/**
 * --- REGISTRO DE PRESTADOR ELITE (V2026.12) ---
 * [LEY #9]: Estándar Mav en Español.
 * Implementa un Wizard Step-by-Step para una experiencia fluida.
 */

@Composable
fun PrestadorRegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    isGoogleUser: Boolean = false,
    tieneNegocioInicial: Boolean = false,
    viewModel: PrestadorRegisterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val registerState by viewModel.registerState.collectAsStateWithLifecycle()
    val servicios by viewModel.servicios.collectAsStateWithLifecycle(initialValue = emptyList())
    val loadingServicios by viewModel.loadingServicios.collectAsStateWithLifecycle()
    val estaDetectando by viewModel.estaDetectandoUbicacion.collectAsStateWithLifecycle()
    
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = if (tieneNegocioInicial) 4 else 3
    
    var pendingLocationCallback by remember { mutableStateOf<((DireccionDominio) -> Unit)?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (locationGranted && pendingLocationCallback != null) {
            viewModel.detectarUbicacionActual(pendingLocationCallback!!)
            pendingLocationCallback = null
        }
        Log.d("Register", "Permisos procesados.")
    }

    LaunchedEffect(registerState) {
        if (registerState is EstadoRegistro.Exito) {
            onRegisterSuccess()
        }
    }

    PrestadorRegisterScreenContent(
        isLoading = registerState is EstadoRegistro.Cargando,
        errorMessage = (registerState as? EstadoRegistro.Error)?.mensaje,
        servicios = servicios,
        loadingServicios = loadingServicios,
        isGoogleUser = isGoogleUser,
        tieneNegocioInicial = tieneNegocioInicial,
        currentStep = currentStep,
        totalSteps = totalSteps,
        estaDetectando = estaDetectando,
        alDetectarUbicacion = { onResult ->
            val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (hasLocation) {
                viewModel.detectarUbicacionActual(onResult)
            } else {
                pendingLocationCallback = onResult
                permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        },
        onNextStep = { 
            if (currentStep < totalSteps) currentStep++ 
        },
        onPrevStep = { if (currentStep > 1) currentStep-- else onBackToLogin() },
        onRegisterClick = { datos, fotoUri ->
            permissionLauncher.launch(
                listOfNotNull(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null
                ).toTypedArray()
            )
            viewModel.register(
                email = datos.email,
                password = datos.password,
                nombre = datos.nombre,
                apellido = datos.apellido,
                categoria = datos.idCategoria,
                mensaje = datos.mensaje,
                serviceType = datos.tipo,
                isGoogleUser = isGoogleUser,
                profileImageUri = fotoUri,
                telefono = datos.telefono,
                dniCuit = datos.dniCuit,
                calle = datos.calle,
                numero = datos.numero,
                localidad = datos.localidad,
                provincia = datos.provincia,
                codigoPostal = datos.codigoPostal,
                latitud = datos.latitud,
                longitud = datos.longitud,
                tieneNegocio = datos.tieneNegocio,
                nombreNegocio = datos.nombreNegocio,
                razonSocial = datos.razonSocial,
                cuitNegocio = datos.cuitNegocio,
                sucursales = datos.sucursales.map { mapOf("nombre" to it.nombre, "direccion" to it.direccion) },
                isHomeService = datos.vaDomicilio,
                is24Hours = datos.atiende24h,
                hasPhysicalStore = datos.tieneNegocio,
                doesService = true,
                doesProduct = datos.tipo == "COMERCIO"
            )
        },
        onBackToLogin = onBackToLogin,
        onResetError = { viewModel.resetState() }
    )
}

data class SucursalUI(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String = "",
    val direccion: String = ""
)

data class DatosRegistro(
    val email: String = "",
    val password: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val telefono: String = "",
    val dniCuit: String = "",
    val calle: String = "",
    val numero: String = "",
    val localidad: String = "",
    val provincia: String = "",
    val codigoPostal: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val idCategoria: String = "",
    val tipo: String = "TECNICO",
    val mensaje: String = "",
    val tieneNegocio: Boolean = false,
    val nombreNegocio: String = "",
    val razonSocial: String = "",
    val cuitNegocio: String = "",
    val sucursales: List<SucursalUI> = emptyList(), // 🔥 [v2026.25]: Eliminada 'Casa Central' por defecto para forzar soberanía
    val vaDomicilio: Boolean = false,
    val atiende24h: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrestadorRegisterScreenContent(
    isLoading: Boolean,
    errorMessage: String?,
    servicios: List<CategoriaEntity>,
    loadingServicios: Boolean,
    isGoogleUser: Boolean,
    tieneNegocioInicial: Boolean,
    currentStep: Int,
    totalSteps: Int,
    estaDetectando: Boolean,
    alDetectarUbicacion: ((DireccionDominio) -> Unit) -> Unit,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    onRegisterClick: (DatosRegistro, Uri?) -> Unit,
    onBackToLogin: () -> Unit,
    onResetError: () -> Unit,
) {
    val colors = getPrestadorColors()
    var datos by remember { mutableStateOf(DatosRegistro(tieneNegocio = tieneNegocioInicial)) }
    var passwordVisible by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    
    var queryCategoria by remember { mutableStateOf("") }
    var menuCategoriaExpandido by remember { mutableStateOf(false) }
    val categoriasFiltradas = remember(queryCategoria, servicios) {
        if (queryCategoria.isBlank()) servicios
        else servicios.filter { it.nombre.contains(queryCategoria, ignoreCase = true) }
    }

    LaunchedEffect(isGoogleUser) {
        if (isGoogleUser) {
            val user = FirebaseAuth.getInstance().currentUser
            user?.let {
                datos = datos.copy(
                    nombre = it.displayName?.substringBefore(" ") ?: "",
                    apellido = it.displayName?.substringAfter(" ", "") ?: "",
                    email = it.email ?: ""
                )
                if (profileImageUri == null) profileImageUri = it.photoUrl
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> profileImageUri = uri }

    Scaffold(
        containerColor = colors.backgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Paso $currentStep de $totalSteps", fontSize = 12.sp, color = colors.primaryOrange, fontWeight = FontWeight.Black)
                        Text("Registro Maverick", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onPrevStep) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colors.backgroundColor)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = colors.surfaceColor) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = onPrevStep,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ATRÁS", fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Button(
                        onClick = { 
                            if (currentStep < totalSteps) onNextStep() 
                            else onRegisterClick(datos, profileImageUri) 
                        },
                        modifier = Modifier.weight(2f).height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange)
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text(if (currentStep < totalSteps) "CONTINUAR" else "CREAR CUENTA", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
            
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clip(CircleShape),
                color = colors.primaryOrange,
                trackColor = Color.White.copy(0.1f)
            )

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }.using(SizeTransform(clip = false))
                }, label = "RegisterWizard"
            ) { step ->
                when (step) {
                    1 -> PasoIdentidadDigital(
                        isGoogleUser = isGoogleUser,
                        datos = datos,
                        profileImageUri = profileImageUri,
                        onDatosChange = { datos = it },
                        onPickImage = { imagePickerLauncher.launch("image/*") },
                        passwordVisible = passwordVisible,
                        onTogglePassword = { passwordVisible = !passwordVisible }
                    )
                    2 -> PasoPerfilProfesional(
                        datos = datos,
                        onDatosChange = { datos = it },
                        queryCategoria = queryCategoria,
                        onQueryChange = { queryCategoria = it },
                        menuCategoriaExpandido = menuCategoriaExpandido,
                        onMenuExpandChange = { menuCategoriaExpandido = it },
                        todasLasCategorias = servicios,
                        categoriasFiltradas = categoriasFiltradas,
                        loadingServicios = loadingServicios
                    )
                    3 -> if (tieneNegocioInicial) PasoInfraestructuraEmpresa(
                        datos = datos,
                        onDatosChange = { datos = it }
                    ) else PasoUbicacionYAlcance(
                        datos = datos,
                        onDatosChange = { datos = it },
                        estaDetectando = estaDetectando,
                        alDetectarUbicacion = { 
                            alDetectarUbicacion { dir ->
                                datos = datos.copy(
                                    calle = dir.calle,
                                    numero = dir.numero,
                                    localidad = dir.localidad,
                                    provincia = dir.provincia,
                                    codigoPostal = dir.codigoPostal,
                                    latitud = dir.latitud,
                                    longitud = dir.longitud
                                )
                            }
                        }
                    )
                    4 -> PasoUbicacionYAlcance(
                        datos = datos,
                        onDatosChange = { datos = it },
                        estaDetectando = estaDetectando,
                        alDetectarUbicacion = { 
                            alDetectarUbicacion { dir ->
                                datos = datos.copy(
                                    calle = dir.calle,
                                    numero = dir.numero,
                                    localidad = dir.localidad,
                                    provincia = dir.provincia,
                                    codigoPostal = dir.codigoPostal,
                                    latitud = dir.latitud,
                                    longitud = dir.longitud
                                )
                            }
                        }
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }

    if (errorMessage != null) {
        AlertDialog(onDismissRequest = onResetError, title = { Text("Error") }, text = { Text(errorMessage) }, confirmButton = { TextButton(onClick = onResetError) { Text("OK") } })
    }
}

@Composable
fun PasoIdentidadDigital(
    isGoogleUser: Boolean,
    datos: DatosRegistro,
    profileImageUri: Uri?,
    onDatosChange: (DatosRegistro) -> Unit,
    onPickImage: () -> Unit,
    passwordVisible: Boolean,
    onTogglePassword: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier.size(120.dp).padding(top = 16.dp).align(Alignment.CenterHorizontally).clip(CircleShape).border(2.dp, Color(0xFFFF7043), CircleShape).clickable { onPickImage() },
            contentAlignment = Alignment.Center
        ) {
            if (profileImageUri != null) {
                AsyncImage(model = profileImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(40.dp), tint = Color.Gray)
            }
        }
        
        Text("Foto de Perfil", modifier = Modifier.align(Alignment.CenterHorizontally), fontSize = 14.sp, fontWeight = FontWeight.Bold)

        FloatingLabelTextField(valor = datos.nombre, onValorCambio = { onDatosChange(datos.copy(nombre = it)) }, etiqueta = "Nombre", iconoPrimario = Icons.Default.Person)
        FloatingLabelTextField(valor = datos.apellido, onValorCambio = { onDatosChange(datos.copy(apellido = it)) }, etiqueta = "Apellido", iconoPrimario = Icons.Default.Person)
        
        if (!isGoogleUser) {
            FloatingLabelTextField(valor = datos.email, onValorCambio = { onDatosChange(datos.copy(email = it)) }, etiqueta = "Email", iconoPrimario = Icons.Default.Email)
            FloatingLabelTextField(
                valor = datos.password, onValorCambio = { onDatosChange(datos.copy(password = it)) }, etiqueta = "Contraseña", iconoPrimario = Icons.Default.Lock,
                transformacionVisual = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                iconoSecundario = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                alClickIconoSecundario = onTogglePassword
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasoPerfilProfesional(
    datos: DatosRegistro,
    onDatosChange: (DatosRegistro) -> Unit,
    queryCategoria: String,
    onQueryChange: (String) -> Unit,
    menuCategoriaExpandido: Boolean,
    onMenuExpandChange: (Boolean) -> Unit,
    todasLasCategorias: List<CategoriaEntity>,
    categoriasFiltradas: List<CategoriaEntity>,
    loadingServicios: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ExposedDropdownMenuBox(expanded = menuCategoriaExpandido, onExpandedChange = onMenuExpandChange) {
            OutlinedTextField(
                value = queryCategoria.ifBlank { 
                    todasLasCategorias.find { it.id == datos.idCategoria }?.nombre ?: ""
                },
                onValueChange = { onQueryChange(it); onMenuExpandChange(true) },
                label = { Text("Buscar Categoría Principal") },
                trailingIcon = { if (loadingServicios) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuCategoriaExpandido) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true).fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
            if (categoriasFiltradas.isNotEmpty()) {
                ExposedDropdownMenu(expanded = menuCategoriaExpandido, onDismissRequest = { onMenuExpandChange(false) }) {
                    categoriasFiltradas.forEach { servicio ->
                        DropdownMenuItem(text = { Text("${servicio.icono} ${servicio.nombre}") }, onClick = { onDatosChange(datos.copy(idCategoria = servicio.id)); onQueryChange(servicio.nombre); onMenuExpandChange(false) })
                    }
                }
            }
        }
        
        FloatingLabelTextField(valor = datos.telefono, onValorCambio = { onDatosChange(datos.copy(telefono = it)) }, etiqueta = "Teléfono de Contacto", iconoPrimario = Icons.Default.Phone, tipoTeclado = KeyboardType.Phone)
        FloatingLabelTextField(valor = datos.dniCuit, onValorCambio = { onDatosChange(datos.copy(dniCuit = it)) }, etiqueta = "DNI / CUIT Personal", iconoPrimario = Icons.Default.Badge)
        
        OutlinedTextField(value = datos.mensaje, onValueChange = { onDatosChange(datos.copy(mensaje = it)) }, label = { Text("Breve biografía / especialidades") }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(8.dp))
    }
}

@Composable
fun PasoInfraestructuraEmpresa(datos: DatosRegistro, onDatosChange: (DatosRegistro) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Datos Corporativos", fontWeight = FontWeight.Black, fontSize = 16.sp)
        FloatingLabelTextField(valor = datos.nombreNegocio, onValorCambio = { onDatosChange(datos.copy(nombreNegocio = it)) }, etiqueta = "Nombre de Fantasía", iconoPrimario = Icons.Default.Store)
        FloatingLabelTextField(valor = datos.razonSocial, onValorCambio = { onDatosChange(datos.copy(razonSocial = it)) }, etiqueta = "Razón Social", iconoPrimario = Icons.Default.Description)
        FloatingLabelTextField(valor = datos.cuitNegocio, onValorCambio = { onDatosChange(datos.copy(cuitNegocio = it)) }, etiqueta = "CUIT Empresa", iconoPrimario = Icons.Default.Tag, tipoTeclado = KeyboardType.Number)
        
        Spacer(Modifier.height(8.dp))
        Text("Configuración de Sucursales (Máx 3)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        
        datos.sucursales.forEachIndexed { index, sucursal ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.05f))) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Sucursal ${index + 1}", fontWeight = FontWeight.Bold)
                        if (index > 0) IconButton(onClick = { onDatosChange(datos.copy(sucursales = datos.sucursales.toMutableList().apply { removeAt(index) })) }) { Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(18.dp)) }
                    }
                    OutlinedTextField(value = sucursal.nombre, onValueChange = { n -> onDatosChange(datos.copy(sucursales = datos.sucursales.toMutableList().apply { set(index, sucursal.copy(nombre = n)) })) }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = sucursal.direccion, onValueChange = { d -> onDatosChange(datos.copy(sucursales = datos.sucursales.toMutableList().apply { set(index, sucursal.copy(direccion = d)) })) }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
            }
        }
        
        if (datos.sucursales.size < 3) {
            TextButton(onClick = { onDatosChange(datos.copy(sucursales = datos.sucursales + SucursalUI())) }) { Icon(Icons.Default.Add, null); Text("AÑADIR OTRA SUCURSAL") }
        }
    }
}

@Composable
fun PasoUbicacionYAlcance(
    datos: DatosRegistro,
    onDatosChange: (DatosRegistro) -> Unit,
    estaDetectando: Boolean = false,
    alDetectarUbicacion: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ubicación Principal", fontWeight = FontWeight.Black, fontSize = 16.sp)
            
            Button(
                onClick = alDetectarUbicacion,
                enabled = !estaDetectando,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6).copy(0.2f))
            ) {
                if (estaDetectando) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF3B82F6), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.MyLocation, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("DETECTAR", color = Color(0xFF3B82F6), fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        FloatingLabelTextField(
            valor = datos.calle, 
            onValorCambio = { onDatosChange(datos.copy(calle = it)) }, 
            etiqueta = "Calle", 
            iconoPrimario = Icons.Default.AddLocation
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                FloatingLabelTextField(
                    valor = datos.numero, 
                    onValorCambio = { onDatosChange(datos.copy(numero = it)) }, 
                    etiqueta = "Número", 
                    iconoPrimario = Icons.Default.Tag, 
                    tipoTeclado = KeyboardType.Number
                )
            }
            Box(modifier = Modifier.weight(2f)) {
                FloatingLabelTextField(
                    valor = datos.codigoPostal, 
                    onValorCambio = { onDatosChange(datos.copy(codigoPostal = it)) }, 
                    etiqueta = "Código Postal", 
                    iconoPrimario = Icons.Default.Map
                )
            }
        }

        FloatingLabelTextField(
            valor = datos.localidad, 
            onValorCambio = { onDatosChange(datos.copy(localidad = it)) }, 
            etiqueta = "Localidad / Ciudad", 
            iconoPrimario = Icons.Default.LocationCity
        )

        FloatingLabelTextField(
            valor = datos.provincia, 
            onValorCambio = { onDatosChange(datos.copy(provincia = it)) }, 
            etiqueta = "Provincia", 
            iconoPrimario = Icons.Default.Public
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(0.1f))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = datos.vaDomicilio, onCheckedChange = { onDatosChange(datos.copy(vaDomicilio = it)) })
            Spacer(Modifier.width(12.dp)); Text("Ofrezco Visitas Técnicas a Domicilio")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = datos.atiende24h, onCheckedChange = { onDatosChange(datos.copy(atiende24h = it)) })
            Spacer(Modifier.width(12.dp)); Text("Atención de Urgencias 24 Horas")
        }
        
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF3B82F6).copy(0.05f))) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text("Usamos tu ubicación para que los clientes cercanos puedan encontrarte más rápido.", fontSize = 11.sp, color = Color.White.copy(0.7f))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRegistro() {
    PrestadorRegisterScreenContent(
        isLoading = false,
        errorMessage = null,
        servicios = emptyList(),
        loadingServicios = false,
        isGoogleUser = false,
        tieneNegocioInicial = false,
        currentStep = 1,
        totalSteps = 3,
        estaDetectando = false,
        alDetectarUbicacion = {},
        onNextStep = {},
        onPrevStep = {},
        onRegisterClick = { _, _ -> },
        onBackToLogin = {},
        onResetError = {}
    )
}















































