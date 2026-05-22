package com.example.myapplication.prestador.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.prestador.data.local.entity.DireccionEntity
import com.example.myapplication.prestador.ui.profile.sections.ArchiveroSection
import com.example.myapplication.prestador.ui.register.components.FloatingLabelTextField
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.viewmodel.localidades.LocalidadesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DireccionSection(
    titulo: String = "Direcci\u00f3n",
    direccion: DireccionEntity? = null,
    expanded: Boolean = false,
    onExpandChange: () -> Unit = {},
    extraContent: (@Composable ColumnScope.() -> Unit)? = null,
    localidadesViewModel: LocalidadesViewModel = hiltViewModel(),
    onGuardar: (
        pais: String,
        provincia: String,
        localidad: String,
        codigoPostal: String,
        calle: String,
        numero: String,
        latitud: Double?,
        longitud: Double?
    ) -> Unit
){
    val colors = getPrestadorColors()

    var provincia by remember(direccion) { mutableStateOf(direccion?.provincia ?: "") }
    var localidad by remember(direccion) { mutableStateOf(direccion?.localidad ?: "") }
    var codigoPostal by remember(direccion) { mutableStateOf(direccion?.codigoPostal ?: "") }
    var calle by remember(direccion) { mutableStateOf(direccion?.calle ?: "") }
    var numero by remember(direccion) { mutableStateOf(direccion?.numero ?: "") }

    // Entrar en modo edición si no hay dirección O si tiene dirección pero sin calle ni provincia (datos incompletos)
    val direccionIncompleta = direccion != null && direccion.calle.isNullOrBlank() && direccion.provincia.isNullOrBlank()
    var editando by remember(direccion) { mutableStateOf(direccion == null || direccionIncompleta) }
    var calleError by remember { mutableStateOf(false) }
    var provinciaError by remember { mutableStateOf(false) }
    var guardandoLoading by remember { mutableStateOf(false) }
    var mostrarSugerencias by remember { mutableStateOf(false) }
    var mostrarSugerenciasLocalidad by remember { mutableStateOf(false) }
    var geocodedLat by remember { mutableStateOf(direccion?.latitud) }
    var geocodedLng by remember { mutableStateOf(direccion?.longitud) }
    var geocodingAddressLoading by remember { mutableStateOf(false) }
    var geocodingAddressResult by remember { mutableStateOf<String?>(null) }
    val todasProvincias by localidadesViewModel.provincias.collectAsState()
    val todasLocalidades by localidadesViewModel.localidades.collectAsState()

    LaunchedEffect(provincia) { localidadesViewModel.cargarLocalidades(provincia)}

    val provinciasFiltradas = if (provincia.isBlank()) emptyList()
    else todasProvincias.filter { it.contains(provincia.trim(), ignoreCase = true) }
    val localidadesFiltradas = if (localidad.isBlank()) todasLocalidades
    else todasLocalidades.filter { it.nombre.contains(localidad.trim(), ignoreCase = true)}


    val context = LocalContext.current
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
                            val calleDetectada = buildString {
                                if (!addr.thoroughfare.isNullOrBlank()) append(addr.thoroughfare)
                                if (!addr.subThoroughfare.isNullOrBlank()) append(" ${addr.subThoroughfare}")
                            }
                            if (calleDetectada.isNotBlank()) { calle = calleDetectada; calleError = false }
                            if (!addr.subThoroughfare.isNullOrBlank()) numero = addr.subThoroughfare!!
                            if (!addr.locality.isNullOrBlank()) localidad = addr.locality!!
                            if (!addr.adminArea.isNullOrBlank()) { provincia = addr.adminArea!!; provinciaError = false }
                            if (!addr.postalCode.isNullOrBlank()) codigoPostal = addr.postalCode!!
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

    ArchiveroSection(
        title = titulo,
        sectionId = "direccion",
        icon = Icons.Default.LocationOn,
        color = colors.primaryOrange,
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandChange = { onExpandChange() }
    ) {

        if (!editando && direccion != null) {
            // ── MODO SOLO LECTURA ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = colors.surfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!direccion.calle.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(6.dp), color = colors.primaryOrange.copy(alpha = 0.12f)) {
                                Icon(Icons.Default.Home, contentDescription = null,
                                    tint = colors.primaryOrange, modifier = Modifier.padding(6.dp).size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Calle", fontSize = 11.sp, color = colors.textSecondary)
                                Text("${direccion.calle} ${direccion.numero ?: ""}".trim(),
                                    fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    if (!direccion.localidad.isNullOrBlank() || !direccion.provincia.isNullOrBlank()) {
                        HorizontalDivider(color = colors.border)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(6.dp), color = colors.primaryOrange.copy(alpha = 0.12f)) {
                                Icon(Icons.Default.LocationCity, contentDescription = null,
                                    tint = colors.primaryOrange, modifier = Modifier.padding(6.dp).size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Localidad / Provincia", fontSize = 11.sp, color = colors.textSecondary)
                                Text(listOfNotNull(direccion.localidad, direccion.provincia).joinToString(", "),
                                    fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    if (!direccion.codigoPostal.isNullOrBlank()) {
                        HorizontalDivider(color = colors.border)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(6.dp), color = colors.primaryOrange.copy(alpha = 0.12f)) {
                                Icon(Icons.Default.MarkunreadMailbox, contentDescription = null,
                                    tint = colors.primaryOrange, modifier = Modifier.padding(6.dp).size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("C\u00f3digo postal", fontSize = 11.sp, color = colors.textSecondary)
                                Text("${direccion.codigoPostal} \u2014 Argentina",
                                    fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = { editando = true },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryOrange),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.primaryOrange),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Editar", fontSize = 13.sp)
            }

        } else {
            // ── MODO EDICIÓN ──
            if (direccion == null) {
                Text("Ingres\u00e1 tu direcci\u00f3n personal",
                    fontSize = 12.sp, color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 10.dp))
            }

            // País — bloqueado en Argentina
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = colors.surfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Public, contentDescription = null,
                        tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pa\u00eds", fontSize = 11.sp, color = colors.textSecondary)
                        Text("Argentina", fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = colors.primaryOrange.copy(alpha = 0.1f)) {
                        Text("Fijo", fontSize = 10.sp, color = colors.primaryOrange,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Provincia con autocomplete
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = provincia,
                    onValueChange = { provincia = it; provinciaError = false; mostrarSugerencias = it.isNotEmpty() },
                    label = { Text("Provincia *") },
                    leadingIcon = { Icon(Icons.Default.Map, contentDescription = null, tint = colors.textSecondary) },
                    trailingIcon = {
                        if (provincia.isNotEmpty()) {
                            IconButton(onClick = { provincia = ""; mostrarSugerencias = false }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = colors.textSecondary)
                            }
                        }
                    },
                    isError = provinciaError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(if (provinciasFiltradas.isEmpty() || provincia.isBlank()) 8.dp else 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primaryOrange,
                        unfocusedBorderColor = colors.border,
                        focusedLabelColor = colors.primaryOrange,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        errorBorderColor = Color.Red
                    )
                )
                AnimatedVisibility(
                    visible = mostrarSugerencias && provinciasFiltradas.isNotEmpty(),
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
                            provinciasFiltradas.take(6).forEachIndexed { index, prov ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { provincia = prov; provinciaError = false; mostrarSugerencias = false }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null,
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
            if (provinciaError) {
                Text("La provincia es requerida", color = Color.Red, fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Localidad con autocomplete
            Column(modifier = Modifier.fillMaxWidth()) {
                val localidadInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                val localidadFocused by localidadInteraction.collectIsFocusedAsState()
                LaunchedEffect(localidadFocused) {
                    if (localidadFocused && todasLocalidades.isNotEmpty()) mostrarSugerenciasLocalidad = true
                    if (!localidadFocused) mostrarSugerenciasLocalidad = false
                }
                OutlinedTextField(
                    value = localidad,
                    onValueChange = { localidad = it; mostrarSugerenciasLocalidad = true },
                    label = { Text("Localidad") },
                    leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = colors.textSecondary) },
                    trailingIcon = {
                        if (localidad.isNotEmpty()) {
                            IconButton(onClick = { localidad = ""; mostrarSugerenciasLocalidad = false }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = colors.textSecondary)
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
                                            localidad = loc.nombre
                                           mostrarSugerenciasLocalidad = false
                                           localidadesViewModel.cargarCodigoPostal(loc.nombre, provincia) { cp ->
                                               if (cp.isNotBlank()) codigoPostal = cp
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
            Spacer(modifier = Modifier.height(10.dp))

            // Calle con botón GPS
            OutlinedTextField(
                value = calle,
                onValueChange = { calle = it; calleError = false },
                label = { Text("Calle *") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = colors.textSecondary) },
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
                isError = calleError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryOrange,
                    unfocusedBorderColor = colors.border,
                    focusedLabelColor = colors.primaryOrange,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    errorBorderColor = Color.Red
                )
            )
            if (calleError) {
                Text("La calle es requerida", color = Color.Red, fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FloatingLabelTextField(
                    value = numero,
                    onValueChange = { if (it.all { c -> c.isDigit() }) numero = it },
                    label = "N\u00famero",
                    leadingIcon = Icons.Default.Tag,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                FloatingLabelTextField(
                    value = codigoPostal,
                    onValueChange = { val v = it.uppercase(); if (v.all { c -> c.isLetterOrDigit() }) codigoPostal = v },
                    label = "C\u00f3d. Postal",
                    leadingIcon = Icons.Default.MarkunreadMailbox,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                "\u26A0 Verific\u00e1 tu c\u00f3digo postal",
                fontSize = 11.sp,
                color = colors.textSecondary,
                modifier = Modifier.fillMaxWidth().padding(end = 4.dp, top = 2.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Botón sincronizar: convierte dirección escrita → lat/lng
            OutlinedButton(
                onClick = {
                    if (calle.isBlank() && provincia.isBlank()) return@OutlinedButton
                    scope.launch {
                        geocodingAddressLoading = true
                        geocodingAddressResult = null
                        try {
                            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                            val query = buildString {
                                if (calle.isNotBlank()) append(calle.trim())
                                if (numero.isNotBlank()) append(" ${numero.trim()}")
                                if (localidad.isNotBlank()) append(", ${localidad.trim()}")
                                if (provincia.isNotBlank()) append(", ${provincia.trim()}")
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
                enabled = !geocodingAddressLoading && (calle.isNotBlank() || provincia.isNotBlank()),
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

            // Resultado del geocoding
            if (geocodingAddressResult != null) {
                Spacer(modifier = Modifier.height(4.dp))
                val esExito = geocodingAddressResult!!.startsWith("✓")
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = if (esExito) Color(0xFF2E7D32).copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (esExito) Color(0xFF2E7D32).copy(alpha = 0.4f) else Color.Red.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
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
                        if (esExito && geocodedLat != null && geocodedLng != null) {
                            val lat = geocodedLat!!
                            val lng = geocodedLng!!
                            TextButton(
                                onClick = {
                                    val uri = android.net.Uri.parse("geo:$lat,$lng?q=$lat,$lng")
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                    intent.setPackage("com.google.android.apps.maps")
                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        // Si no tiene Maps, abre en navegador
                                        val webUri = android.net.Uri.parse("https://maps.google.com/?q=$lat,$lng")
                                        context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, webUri))
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF1565C0))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ver", fontSize = 12.sp, color = Color(0xFF1565C0))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (direccion != null) {
                    OutlinedButton(
                        onClick = {
                            provincia = direccion.provincia ?: ""
                            localidad = direccion.localidad ?: ""
                            codigoPostal = direccion.codigoPostal ?: ""
                            calle = direccion.calle ?: ""
                            numero = direccion.numero ?: ""
                            calleError = false; provinciaError = false; editando = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary)
                    ) { Text("Cancelar", fontSize = 13.sp) }
                }
                Button(
                    onClick = {
                        var hasError = false
                        if (provincia.isBlank()) { provinciaError = true; hasError = true }
                        if (calle.isBlank()) { calleError = true; hasError = true }
                        if (!hasError) {
                            scope.launch {
                                guardandoLoading = true
                                var lat: Double? = geocodedLat
                                var lng: Double? = geocodedLng
                                // Solo geocodifica si no se sincronizó previamente
                                if (lat == null || lng == null) {
                                    try {
                                        val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                                        val query = buildString {
                                            append(calle.trim())
                                            if (numero.isNotBlank()) append(" ${numero.trim()}")
                                            if (localidad.isNotBlank()) append(", ${localidad.trim()}")
                                            append(", ${provincia.trim()}, Argentina")
                                        }
                                        @Suppress("DEPRECATION")
                                        val results = geocoder.getFromLocationName(query, 1)
                                        if (!results.isNullOrEmpty()) {
                                            lat = results[0].latitude
                                            lng = results[0].longitude
                                        }
                                    } catch (_: Exception) { /* geocoding opcional — no bloquea */ }
                                }
                                onGuardar("Argentina", provincia, localidad, codigoPostal, calle, numero, lat, lng)
                                editando = false
                                guardandoLoading = false
                            }
                        }
                    },
                    enabled = !guardandoLoading,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange)
                ) {
                    if (guardandoLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guardando...", fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guardar", fontSize = 13.sp)
                    }
                }
            }
        }

        if (extraContent != null) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.primaryOrange.copy(alpha = 0.2f))
            extraContent()
        }
    }
}
