package com.example.myapplication.prestador.ui.profile

import android.R
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.prestador.data.model.AddressProvider
import com.example.myapplication.prestador.data.model.BranchProvider
import com.example.myapplication.prestador.data.model.EmployeeProvider
import com.example.myapplication.prestador.ui.register.components.FloatingLabelTextField
import com.example.myapplication.prestador.viewmodel.empresa.SucursalesViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.selection.selectable
import com.example.myapplication.prestador.viewmodel.localidades.LocalidadesViewModel

@Composable
fun SucursalesSection(
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    onUploadImage: suspend (Uri) -> String?,
    onSucursalAgregada: () -> Unit = {},
    refreshTrigger: Int = 0,
    viewModel: SucursalesViewModel = hiltViewModel(),
    localidadesViewModel: LocalidadesViewModel = hiltViewModel(),
) {
    val sucursales by viewModel.sucursales.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Estado del formulario inline para agregar nueva sucursal
    var agregando by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevaProvincia by remember { mutableStateOf("") }
    var nuevaLocalidad by remember { mutableStateOf("") }
    var mostrarSugerenciasProvincia by remember { mutableStateOf(false) }
    var mostrarSugerenciasLocalidad by remember { mutableStateOf(false) }
    val todasProvincias by localidadesViewModel.provincias.collectAsState()
    val todasLocalidades by localidadesViewModel.localidades.collectAsState()

    LaunchedEffect(nuevaProvincia) {
        localidadesViewModel.cargarLocalidades(nuevaProvincia)
    }

    val provinciasFiltradas: List<String> = if (nuevaProvincia.isBlank()) emptyList()
    else todasProvincias.filter { p -> p.contains(nuevaProvincia.trim(), ignoreCase = true) }
    val localidadesFiltradas: List<Localidad> = if (nuevaLocalidad.isBlank()) todasLocalidades
    else todasLocalidades.filter { l-> l.nombre.contains(nuevaLocalidad.trim(), ignoreCase = true) }
    var nuevaCalle by remember { mutableStateOf("") }
    var nuevoNumero by remember { mutableStateOf("") }
    var nuevoCp by remember { mutableStateOf("") }
    var nuevoHorario by remember { mutableStateOf("") }
    var nombreError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var geocodingLoading by remember { mutableStateOf(false) }
    var geocodedLat by remember { mutableStateOf<Double?>(null) }
    var geocodedLng by remember { mutableStateOf<Double?>(null) }
    var geocodingAddressLoading by remember { mutableStateOf(false) }
    var geocodingAddressResult by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

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
                    val location = fusedClient.lastLocation.await()
                    if (location != null) {
                        val geocoder = android.location.Geocoder(context,
                        java.util.Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude,1)
                        if (
                            !addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            if (!addr.thoroughfare.isNullOrBlank())
                                nuevaCalle = addr.thoroughfare!!
                            if (!addr.subThoroughfare.isNullOrBlank())
                                nuevoNumero = addr.subThoroughfare!!
                            if (!addr.locality.isNullOrBlank())
                                nuevaLocalidad = addr.locality!!
                            if (!addr.adminArea.isNullOrBlank())
                                nuevaProvincia = addr.adminArea !!
                            if (!addr.postalCode.isNullOrBlank()) nuevoCp = addr.postalCode!!
                        }
                        geocodedLat = location.latitude
                        geocodedLng = location.longitude
                        geocodingAddressResult = "✓ ${String.format("%.5f", location.latitude)}, ${String.format("%.5f", location.longitude)}"

                    }
                } catch (e: Exception) {
                } finally { geocodingLoading = false }

            }
        }
    }

    var showDeleteDialog by remember { mutableStateOf<BranchProvider?>(null) }

    LaunchedEffect(Unit) { viewModel.refreshBusinessId() }
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) viewModel.refreshBusinessId()
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Al guardar con éxito, cerrar el formulario inline
    LaunchedEffect(uiState) {
        when (uiState) {
            is SucursalesViewModel.UiState.Success -> {
                agregando = false
                nuevoNombre = ""; nuevaProvincia = ""; nuevaLocalidad = ""
                nuevaCalle = ""; nuevoNumero = ""; nuevoCp = ""; nuevoHorario = ""
                geocodedLat = null; geocodedLng = null
                geocodingAddressResult = null
                nombreError = false
                errorMessage = null
                onSucursalAgregada()
                viewModel.resetState()
            }
            is SucursalesViewModel.UiState.Error -> {
                errorMessage = (uiState as SucursalesViewModel.UiState.Error).message
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        //Cabecera: contador + boton agregar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (sucursales.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.primaryOrange.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${sucursales.size} sucursal${if (sucursales.size != 1) "es" else ""}",
                        color = colors.primaryOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            if (!agregando) {
                OutlinedButton(
                    onClick = { agregando = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryOrange),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.primaryOrange),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null, modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        "Agregar sucursal",
                        fontSize = 13.sp
                    )
                }
            }
        }
        //Formulario inline "Agregar sucursal
        AnimatedVisibility(
            visible = agregando,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.primaryOrange.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.primaryOrange.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Nueva sucursal", color = colors.primaryOrange, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Box(
                        modifier = if (nombreError)

                        Modifier.border(1.5.dp, MaterialTheme.colorScheme.error,
                            RoundedCornerShape(8.dp))
                        else Modifier
                    ){
                        FloatingLabelTextField(
                            value = nuevoNombre,
                            onValueChange = { nuevoNombre = it; nombreError = false },
                            label = "Nombre *",
                            leadingIcon = Icons.Default.Business
                        )
                    }

                    if (nombreError) {
                        Text("El nombre es obligatorio", color = MaterialTheme.colorScheme.error, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                    }

                    //Provincia con autocomplete
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = nuevaProvincia,
                            onValueChange = { nuevaProvincia = it; mostrarSugerenciasProvincia = it.isNotEmpty() },
                            label = { Text("Provincia") },
                            leadingIcon = {
                                Icon(Icons.Default.Map, contentDescription = null, tint = colors.textSecondary) },
                            trailingIcon = {
                                if (nuevaProvincia.isNotEmpty())
                                {
                                    IconButton(onClick = { nuevaProvincia = "";
                                    mostrarSugerenciasProvincia = false }) {
                                        Icon(Icons.Default.Clear, contentDescription = null, tint = colors.textSecondary)
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primaryOrange,
                                unfocusedBorderColor = colors.border,
                                focusedLabelColor = colors.primaryOrange,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )
                        AnimatedVisibility(
                            visible = mostrarSugerenciasProvincia && provinciasFiltradas.isNotEmpty(),
                            enter = fadeIn(tween(200)) + expandVertically(tween(250)),
                            exit = fadeOut(tween(150)) + shrinkVertically(tween(200))
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                                color = colors.surfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                                shadowElevation = 4.dp
                            ){
                                Column {
                                    provinciasFiltradas.take(6).forEachIndexed { index, prov ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable{
                                                    nuevaProvincia = prov
                                                    nuevaLocalidad = ""
                                                    nuevoCp = ""
                                                    mostrarSugerenciasProvincia = false
                                                }
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ){
                                            Icon(Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = colors.primaryOrange, modifier = Modifier.size(16.dp))
                                            Text(prov, fontSize = 14.sp, color = colors.textPrimary)
                                        }
                                        if (index < provinciasFiltradas.take(6).lastIndex) {
                                            HorizontalDivider(color = colors.border)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    //Localidad con autcomplete
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val localidadInteraction = remember {
                            androidx.compose.foundation.interaction.MutableInteractionSource() }
                        val localidadFocused by localidadInteraction.collectIsFocusedAsState()
                        LaunchedEffect(localidadFocused) {
                            if (localidadFocused && todasProvincias.isNotEmpty())
                                mostrarSugerenciasLocalidad = true
                            if (!localidadFocused) mostrarSugerenciasLocalidad = false
                        }
                        OutlinedTextField(
                            value = nuevaLocalidad,
                            onValueChange =  { nuevaLocalidad = it; mostrarSugerenciasLocalidad = true },
                            label = { Text("Localidad") },
                            leadingIcon = { Icon(Icons.Default.LocationCity,
                                contentDescription = null, tint = colors.textSecondary )},
                            trailingIcon = {
                                if (nuevaLocalidad.isNotEmpty()) {
                                    IconButton(onClick = {
                                        nuevaLocalidad = "";
                                        mostrarSugerenciasLocalidad = false
                                    }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = null,
                                            tint = colors.textSecondary
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            interactionSource = localidadInteraction,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primaryOrange,
                                unfocusedBorderColor = colors.border,
                                focusedLabelColor = colors.primaryOrange,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )
                        AnimatedVisibility(
                            visible = mostrarSugerenciasLocalidad && localidadesFiltradas.isNotEmpty(),
                            enter = fadeIn(tween(200)) + expandVertically(tween(250)),
                            exit = fadeOut(tween(150)) + shrinkVertically(tween(200))
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                                color = colors.surfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                                shadowElevation = 4.dp
                            ) {
                                Column {
                                    localidadesFiltradas.take(6).forEachIndexed { index, loc ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    nuevaLocalidad = loc.nombre
                                                    mostrarSugerenciasLocalidad = false
                                                    localidadesViewModel.cargarCodigoPostal(loc.nombre, nuevaProvincia) { cp ->
                                                        if (cp.isNotBlank()) nuevoCp = cp
                                                    }
                                                }
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(Icons.Default.LocationCity, contentDescription = null,
                                                tint = colors.primaryOrange, modifier = Modifier.size(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(loc.nombre, fontSize = 14.sp, color = colors.textPrimary)
                                            }
                                            Text("CP ${loc.codigoPostal}", fontSize = 12.sp, color = colors.textSecondary)
                                        }
                                        if (index < localidadesFiltradas.take(6).lastIndex) {
                                            HorizontalDivider(color = colors.border)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = nuevaCalle,
                        onValueChange = { nuevaCalle = it },
                        label = { Text("Calle") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = colors.textSecondary) },
                        trailingIcon = {
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
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primaryOrange,
                            unfocusedBorderColor = colors.border,
                            focusedLabelColor = colors.primaryOrange,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            FloatingLabelTextField(value = nuevoNumero,
                                onValueChange = { nuevoNumero = it }, label =
                                    "Número", leadingIcon = Icons.Default.Tag,
                                keyboardType = KeyboardType.Number)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            FloatingLabelTextField(value = nuevoCp,
                                onValueChange = { nuevoCp = it }, label = "Cód.Postal", leadingIcon = Icons.Default.PinDrop,
                                        keyboardType = KeyboardType.Number)
                        }
                    }

                    HorarioSelectorField(horario = nuevoHorario, onHorarioChange = { nuevoHorario = it })

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            if (nuevaCalle.isBlank() && nuevaProvincia.isBlank()) return@OutlinedButton
                            scope.launch {
                                geocodingAddressLoading = true
                                geocodingAddressResult = null
                                try {
                                    val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                                    val query = buildString {
                                        if (nuevaCalle.isNotBlank()) append(nuevaCalle.trim())
                                        if (nuevoNumero.isNotBlank()) append(" ${nuevoNumero.trim()}")
                                        if (nuevaLocalidad.isNotBlank()) append(", ${nuevaLocalidad.trim()}")
                                        if (nuevaProvincia.isNotBlank()) append(", ${nuevaProvincia.trim()}")
                                        append(", Argentina")
                                    }
                                    @Suppress("DEPRECATION")
                                    val results = geocoder.getFromLocationName(query, 1)
                                    if (!results.isNullOrEmpty()) {
                                        geocodedLat = results[0].latitude
                                        geocodedLng = results[0].longitude
                                        geocodingAddressResult = "✓ ${String.format("%.5f", geocodedLat)}, ${String.format("%.5f", geocodedLng)}"
                                    } else {
                                        geocodingAddressResult = "No se encontraron coordenadas"
                                        geocodedLat = null
                                        geocodedLng = null
                                    }
                                } catch (_: Exception) {
                                    geocodingAddressResult = "Error al obtener coordenadas"
                                } finally {
                                    geocodingAddressLoading = false
                                }
                            }
                        },
                        enabled = !geocodingAddressLoading && (nuevaCalle.isNotBlank() || nuevaProvincia.isNotBlank()),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryOrange),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.primaryOrange.copy(alpha = 0.6f))
                    ) {
                        if (geocodingAddressLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.dp, color = colors.primaryOrange)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Obteniendo coordenadas...", fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sincronizar coordenadas", fontSize = 13.sp)
                        }
                    }

                    if (geocodingAddressResult != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val esExito = geocodingAddressResult!!.startsWith("✓")
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = if (esExito) Color(0xFF2E7D32).copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp,
                                if (esExito) Color(0xFF2E7D32).copy(alpha = 0.4f) else Color.Red.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    if (esExito) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (esExito) Color(0xFF2E7D32) else Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    geocodingAddressResult!!,
                                    fontSize = 12.sp,
                                    color = if (esExito) Color(0xFF2E7D32) else Color.Red
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (errorMessage != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                agregando = false
                                nuevoNombre = ""; nuevaProvincia = ""; nuevaLocalidad = "";
                                nuevaCalle = ""; nuevoNumero = ""; nuevoCp = ""; nuevoHorario = ""
                                nombreError = false
                                errorMessage = null
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) { Text("Cancelar", fontSize = 13.sp)}
                        Button(
                            onClick = {
                                errorMessage = null
                                if (nuevoNombre.isBlank()) {
                                    nombreError = true; return@Button
                                }
                                viewModel.addSucursal(
                                    nuevoNombre,
                                    nuevaProvincia.takeIf { it.isNotBlank() },
                                    nuevaLocalidad.takeIf { it.isNotBlank() },
                                    nuevaCalle.takeIf { it.isNotBlank() },
                                    nuevoNumero.takeIf { it.isNotBlank() },
                                    nuevoCp.takeIf { it.isNotBlank() },
                                    nuevoHorario.takeIf { it.isNotBlank() },
                                    geocodedLat,
                                    geocodedLng
                                )
                            },
                            enabled = uiState !is SucursalesViewModel.UiState.Loading,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            if (uiState is SucursalesViewModel.UiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text("Guardar", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        //Lista de sucursales
        if (sucursales.isEmpty() && !agregando) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.surfaceColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info,
                        contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("No hay sucursales registradas", color = colors.textSecondary, fontSize = 13.sp)
                }
            }
        } else {
            sucursales.forEach { sucursal ->
                SucursalExpandableCard(
                    sucursal = sucursal,
                    colors = colors,
                    onDelete = { showDeleteDialog = sucursal },
                    onUpdate = { updatedBranch ->
                        viewModel.updateBranch(updatedBranch)
                    },
                    onUploadImage = onUploadImage,
                    onGuardarEncargado = { nombre, apellido, cargo, imageUrl ->
                        viewModel.updateManager(sucursal.id, nombre, apellido, cargo, imageUrl)
                    },
                    onAgregarEquipo = { nombre, apellido, cargo ->
                        viewModel.agregarMiembroEquipo(sucursal.id, nombre, apellido, cargo)
                    },
                    onDeleteEmployee = { employeeId ->
                        viewModel.deleteEmployee(employeeId)
                    }
                )
            }
        }
    }



    // ── Confirmar eliminación ────────────────────────────────────────────────
    showDeleteDialog?.let { sucursal ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar sucursal") },
            text = { Text("Seguro que queres eliminar '${sucursal.name}'?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteSucursal(sucursal.id); showDeleteDialog = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            }
        )
    }
}

// ─── Tarjeta expandible ────────────────────────────────────────────────────────

@Composable
private fun SucursalExpandableCard(
    sucursal: BranchProvider,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    onDelete: () -> Unit,
    onUpdate: (BranchProvider) -> Unit,
    onUploadImage: suspend (Uri) -> String?,
    onGuardarEncargado: (nombre: String, apellido: String?, cargo: String?, imageUrl: String?) -> Unit,
    onAgregarEquipo: (nombre: String, apellido: String?, cargo: String?) -> Unit,
    onDeleteEmployee: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val encargado = sucursal.employees.firstOrNull()
    val equipo = if (sucursal.employees.size > 1) sucursal.employees.drop(1) else emptyList()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // ── Cabecera ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(colors.primaryOrange.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Store, contentDescription = null, tint = colors.primaryOrange, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(sucursal.name, color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    val localidad = sucursal.address.localidad
                    if (localidad.isNotBlank() || sucursal.workingHours.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (localidad.isNotBlank()) {
                                Text(localidad, color = colors.textSecondary, fontSize = 11.sp)
                            }
                            if (sucursal.workingHours.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = colors.primaryOrange.copy(alpha = 0.12f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Schedule, contentDescription = null,
                                            tint = colors.primaryOrange, modifier = Modifier.size(10.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(sucursal.workingHours, color = colors.primaryOrange, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                    if (encargado != null) Text("Encargado/a: ${encargado.name}", color = colors.primaryOrange, fontSize = 11.sp)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(20.dp)
                )
            }

            // ── Contenido expandible ──────────────────────────────────────────
            AnimatedVisibility(visible = expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.15f))

                    // — Datos (nombre, dirección, horario) —
                    DatosSubseccion(
                        sucursal = sucursal,
                        colors = colors,
                        onUpdate = onUpdate
                    )

                    HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.1f))

                    // — Características —
                    BooleanosSucursalSubseccion(
                        sucursal = sucursal,
                        colors = colors,
                        onUpdate = onUpdate
                    )

                    HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.1f))

                    // — Encargado —
                    EncargadoSubseccion(
                        encargado = encargado,
                        colors = colors,
                        onUploadImage = onUploadImage,
                        onGuardar = onGuardarEncargado
                    )

                    HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.1f))

                    // — Equipo —
                    EquipoSubseccion(
                        equipo = equipo,
                        colors = colors,
                        onAgregar = onAgregarEquipo,
                        onDelete = onDeleteEmployee
                    )
                }
            }
        }
    }
}

// ─── Subsección Datos unificados (nombre + dirección + horario) ───────────────

@Composable
private fun DatosSubseccion(
    sucursal: BranchProvider,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    onUpdate: (BranchProvider) -> Unit
) {
    val direccion = sucursal.address
    var editando by remember(sucursal) { mutableStateOf(false) }
    var editNombre by remember(sucursal) { mutableStateOf(sucursal.name) }
    var editProvincia by remember(sucursal) { mutableStateOf(direccion.provincia) }
    var editLocalidad by remember(sucursal) { mutableStateOf(direccion.localidad) }
    var editCalle by remember(sucursal) { mutableStateOf(direccion.calle) }
    var editNumero by remember(sucursal) { mutableStateOf(direccion.numero) }
    var editCp by remember(sucursal) { mutableStateOf(direccion.codigoPostal) }
    var editHorario by remember(sucursal) { mutableStateOf(sucursal.workingHours) }
    var editGeoLat by remember(sucursal) { mutableStateOf(direccion.latitude) }
    var editGeoLng by remember(sucursal) { mutableStateOf(direccion.longitude) }
    var editGeoResult by remember(sucursal) { mutableStateOf<String?>(null) }
    var editGeoLoading by remember { mutableStateOf(false) }
    val editScope = rememberCoroutineScope()
    val context = LocalContext.current


    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Business, contentDescription = null, tint = colors.primaryOrange, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Datos de la sucursal", color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        if (!editando) {
            TextButton(onClick = { editando = true}, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
    Spacer(modifier = Modifier.height(6.dp))

    if (!editando) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            InfoRow(icon = Icons.Default.Business, value = sucursal.name, colors = colors)
            val dir = listOf(direccion.calle, direccion.numero).filter { it.isNotBlank() }.joinToString(" ")
            if (dir.isNotBlank()) InfoRow(icon = Icons.Default.Home, value = dir, colors = colors)
            val loc = listOf(direccion.localidad, direccion.provincia).filter { it.isNotBlank() }.joinToString(", ")
            if (loc.isNotBlank()) InfoRow(icon = Icons.Default.LocationCity, value = loc, colors = colors)
            if (direccion.codigoPostal.isNotBlank()) InfoRow(icon = Icons.Default.PinDrop, value = "CP ${direccion.codigoPostal}", colors = colors)
            if (sucursal.workingHours.isNotBlank()) InfoRow(icon = Icons.Default.Schedule, value = sucursal.workingHours, colors = colors)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FloatingLabelTextField(value = editNombre, onValueChange = { editNombre = it }, label = "Nombre *", leadingIcon = Icons.Default.Business)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    FloatingLabelTextField(value = editProvincia, onValueChange = { editProvincia = it }, label = "Provincia", leadingIcon = Icons.Default.Map)
                }
                Box(modifier = Modifier.weight(1f)) {
                    FloatingLabelTextField(value = editLocalidad, onValueChange = { editLocalidad = it }, label = "Localidad", leadingIcon = Icons.Default.LocationCity)
                }
            }
            FloatingLabelTextField(value = editCalle, onValueChange = { editCalle = it }, label = "Calle", leadingIcon = Icons.Default.Home)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    FloatingLabelTextField(value = editNumero, onValueChange = { editNumero = it }, label = "Número", leadingIcon = Icons.Default.Tag, keyboardType = KeyboardType.Number)
                }
                Box(modifier = Modifier.weight(1f)) {
                    FloatingLabelTextField(value = editCp, onValueChange = { editCp = it }, label = "Cód. Postal", leadingIcon = Icons.Default.PinDrop, keyboardType = KeyboardType.Number)
                }
            }
            HorarioSelectorField(horario = editHorario, onHorarioChange = { editHorario = it })

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedButton(
                onClick = {
                    if (editCalle.isBlank() && editProvincia.isBlank()) return@OutlinedButton
                    editScope.launch {
                        editGeoLoading = true
                        editGeoResult = null
                        try {
                            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                            val query = buildString {
                                if (editCalle.isNotBlank()) append(editCalle.trim())
                                if (editNumero.isNotBlank()) append(" ${editNumero.trim()}")
                                if (editLocalidad.isNotBlank()) append(", ${editLocalidad.trim()}")
                                if (editProvincia.isNotBlank()) append(", ${editProvincia.trim()}")
                                append(", Argentina")
                            }
                            @Suppress("DEPRECATION")
                            val results = geocoder.getFromLocationName(query, 1)
                            if (!results.isNullOrEmpty()) {
                                editGeoLat = results[0].latitude
                                editGeoLng = results[0].longitude
                                editGeoResult = "✓ ${String.format("%.5f", editGeoLat)}, ${String.format("%.5f", editGeoLng)}"
                            } else {
                                editGeoResult = "No se encontraron coordenadas"
                                editGeoLat = null
                                editGeoLng = null
                            }
                        } catch (_: Exception) {
                            editGeoResult = "Error al obtener coordenadas"
                        } finally {
                            editGeoLoading = false
                        }
                    }
                },
                enabled = !editGeoLoading && (editCalle.isNotBlank() || editProvincia.isNotBlank()),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryOrange),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.primaryOrange.copy(alpha = 0.6f))
            ) {
                if (editGeoLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.dp, color = colors.primaryOrange)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Obteniendo coordenadas...", fontSize = 13.sp)
                } else {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sincronizar coordenadas", fontSize = 13.sp)
                }
            }

            if (editGeoResult != null) {
                val esExito = editGeoResult!!.startsWith("✓")
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = if (esExito) Color(0xFF2E7D32).copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp,
                        if (esExito) Color(0xFF2E7D32).copy(alpha = 0.4f) else Color.Red.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (esExito) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (esExito) Color(0xFF2E7D32) else Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(editGeoResult!!, fontSize = 12.sp,
                            color = if (esExito) Color(0xFF2E7D32) else Color.Red)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    editNombre = sucursal.name; editProvincia = direccion.provincia
                    editLocalidad = direccion.localidad; editCalle = direccion.calle
                    editNumero = direccion.numero; editCp = direccion.codigoPostal
                    editHorario = sucursal.workingHours
                    editGeoLat = direccion.latitude; editGeoLng = direccion.longitude
                    editGeoResult = null; editando = false
                },
                modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 6.dp)
            ) { Text("Cancelar", fontSize = 13.sp) }
            Button(
                onClick = {
                    if (editNombre.isNotBlank()) {
                        onUpdate(sucursal.copy(
                            name = editNombre,
                            workingHours = editHorario,
                            address = direccion.copy(
                                provincia = editProvincia,
                                localidad = editLocalidad,
                                calle = editCalle,
                                numero = editNumero,
                                codigoPostal = editCp,
                                latitude = editGeoLat,
                                longitude = editGeoLng
                            )
                        ))
                        editando = false
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) { Text("Guardar", fontSize = 13.sp) }
        }
    }
}

// ─── Subsección Encargado ──────────────────────────────────────────────────────

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, colors: com.example.myapplication.prestador.ui.theme.PrestadorColors) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(value, color = colors.textPrimary, fontSize = 13.sp)
    }
}

@Composable
private fun EncargadoSubseccion(
    encargado: EmployeeProvider?,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    onUploadImage: suspend (Uri) -> String?,
    onGuardar: (nombre: String, apellido: String?, cargo: String?, imageUrl: String?) -> Unit
) {
    val scope = rememberCoroutineScope()
    var editando by remember(encargado) { mutableStateOf(encargado == null) }
    var nombre by remember(encargado) { mutableStateOf(encargado?.name ?: "") }
    var apellido by remember(encargado) { mutableStateOf(encargado?.lastName ?: "") }
    var cargo by remember(encargado) { mutableStateOf(encargado?.position ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) imageUri = uri
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Person, contentDescription = null, tint = colors.primaryOrange, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Encargado/a", color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        if (!editando) {
            TextButton(onClick = { editando = true }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Editar", fontSize = 12.sp)
            }
        }
    }
    Spacer(modifier = Modifier.height(6.dp))

    if (!editando && encargado != null) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).border(2.dp, colors.primaryOrange, CircleShape).background(colors.primaryOrange.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (encargado.photoUrl != null) {
                    AsyncImage(model = encargado.photoUrl, contentDescription = encargado.name, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = colors.primaryOrange, modifier = Modifier.size(26.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("${encargado.name}${if (encargado.lastName.isNotBlank()) " ${encargado.lastName}" else ""}", color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                if (encargado.position.isNotBlank()) Text(encargado.position, color = colors.textSecondary, fontSize = 12.sp)
            }
        }
    } else {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(60.dp).clip(CircleShape).border(2.dp, colors.primaryOrange, CircleShape).background(colors.primaryOrange.copy(alpha = 0.08f)).clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                when {
                    imageUri != null -> AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                    encargado?.photoUrl != null -> AsyncImage(model = encargado.photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                    else -> Icon(Icons.Default.AddAPhoto, contentDescription = "Foto", tint = colors.primaryOrange, modifier = Modifier.size(26.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FloatingLabelTextField(value = nombre, onValueChange = { nombre = it }, label = "Nombre *", leadingIcon = Icons.Default.Person)
                FloatingLabelTextField(value = apellido, onValueChange = { apellido = it }, label = "Apellido", leadingIcon = Icons.Default.Person)
                FloatingLabelTextField(value = cargo, onValueChange = { cargo = it }, label = "Cargo (ej: Gerente)", leadingIcon = Icons.Default.Work)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (encargado != null) {
                OutlinedButton(
                    onClick = { nombre = encargado.name; apellido = encargado.lastName; cargo = encargado.position; imageUri = null; editando = false },
                    modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 6.dp)
                ) { Text("Cancelar", fontSize = 13.sp) }
            }
            Button(
                onClick = {
                    if (nombre.isNotBlank()) {
                        uploading = true
                        scope.launch {
                            val url = imageUri?.let { onUploadImage(it) } ?: encargado?.photoUrl
                            onGuardar(nombre, apellido.takeIf { it.isNotBlank() }, cargo.takeIf { it.isNotBlank() }, url)
                            uploading = false
                            editando = false
                        }
                    }
                },
                enabled = !uploading,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                if (uploading) CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Guardar", fontSize = 13.sp)
            }
        }
    }
}

// ─── Subsección Equipo ─────────────────────────────────────────────────────────

@Composable
private fun EquipoSubseccion(
    equipo: List<EmployeeProvider>,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    onAgregar: (nombre: String, apellido: String?, cargo: String?) -> Unit,
    onDelete: (String) -> Unit
) {
    var agregando by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevoApellido by remember { mutableStateOf("") }
    var nuevoCargo by remember { mutableStateOf("") }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Group, contentDescription = null, tint = colors.primaryOrange, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Equipo de trabajo", color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        if (!agregando) {
            TextButton(onClick = { agregando = true }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Agregar", fontSize = 12.sp)
            }
        }
    }
    Spacer(modifier = Modifier.height(4.dp))

    if (equipo.isEmpty() && !agregando) {
        Text("Sin miembros de equipo", color = colors.textSecondary, fontSize = 12.sp,
            modifier = Modifier.padding(start = 22.dp))
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            equipo.forEach { miembro ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(colors.primaryOrange.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (miembro.photoUrl != null) {
                            AsyncImage(model = miembro.photoUrl, contentDescription = miembro.name, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, tint = colors.primaryOrange, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${miembro.name}${if (miembro.lastName.isNotBlank()) " ${miembro.lastName}" else ""}",
                            color = colors.textPrimary, fontSize = 13.sp
                        )
                        if (miembro.position.isNotBlank())
                            Text(miembro.position, color = colors.textSecondary, fontSize = 11.sp)
                    }
                    IconButton(onClick = { onDelete(miembro.id) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }

    AnimatedVisibility(visible = agregando, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
        Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FloatingLabelTextField(value = nuevoNombre, onValueChange = { nuevoNombre = it }, label = "Nombre *", leadingIcon = Icons.Default.Person)
            FloatingLabelTextField(value = nuevoApellido, onValueChange = { nuevoApellido = it }, label = "Apellido", leadingIcon = Icons.Default.Person)
            FloatingLabelTextField(value = nuevoCargo, onValueChange = { nuevoCargo = it }, label = "Cargo", leadingIcon = Icons.Default.Work)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { agregando = false; nuevoNombre = ""; nuevoApellido = ""; nuevoCargo = "" },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) { Text("Cancelar", fontSize = 13.sp) }
                Button(
                    onClick = {
                        if (nuevoNombre.isNotBlank()) {
                            onAgregar(
                                nuevoNombre,
                                nuevoApellido.takeIf { it.isNotBlank() },
                                nuevoCargo.takeIf { it.isNotBlank() }
                            )
                            agregando = false; nuevoNombre = ""; nuevoApellido = ""; nuevoCargo = ""
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) { Text("Guardar", fontSize = 13.sp) }
            }
        }
    }
}

@Composable
private fun BooleanosSucursalSubseccion(
    sucursal: BranchProvider,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    onUpdate: (BranchProvider) -> Unit
) {
    var local by remember(sucursal) { mutableStateOf(sucursal) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Tune, contentDescription = null, tint = colors.primaryOrange, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Características", color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
    Spacer(modifier = Modifier.height(6.dp))
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        ServicioSwitch("Ofrece servicios", Icons.Default.Build, local.doesService) {
            local = local.copy(doesService = it); onUpdate(local)
        }
        ServicioSwitch("Vende productos", Icons.Default.ShoppingBag, local.doesProduct) {
            local = local.copy(doesProduct = it); onUpdate(local)
        }
        ServicioSwitch("Atención 24hs", Icons.Default.AccessTime, local.works24h) {
            local = local.copy(works24h = it); onUpdate(local)
        }
        ServicioSwitch("Tiene local físico", Icons.Default.Store, local.hasPhysicalLocation) {
            local = local.copy(hasPhysicalLocation = it); onUpdate(local)
        }
        ServicioSwitch("Va a domicilio", Icons.Default.Home, local.doesHomeVisits) {
            local = local.copy(doesHomeVisits = it); onUpdate(local)
        }
        ServicioSwitch("Hace envíos", Icons.Default.LocalShipping, local.doesShipping) {
            local = local.copy(doesShipping = it); onUpdate(local)
        }
        ServicioSwitch("Acepta turnos", Icons.Default.CalendarToday, local.acceptsAppointments) {
            local = local.copy(acceptsAppointments = it); onUpdate(local)
        }
    }
}

@Composable
private fun ServicioSwitch(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(label, fontSize = 13.sp)
        }
        Switch(checked, onCheckedChange = onCheckedChange)
    }
}
