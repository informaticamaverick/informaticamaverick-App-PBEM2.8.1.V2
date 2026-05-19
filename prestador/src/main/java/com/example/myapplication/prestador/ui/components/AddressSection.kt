package com.example.myapplication.prestador.ui.components

import android.location.Geocoder
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.myapplication.prestador.data.model.AddressProvider
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// TARJETA de dirección (modo vista)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AddressProviderCard(
    address: AddressProvider,
    isEditMode: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenMaps: () -> Unit
) {
    val colors = getPrestadorColors()
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = colors.primaryOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    if (address.label.isNotBlank()) {
                        Text(
                            address.label,
                            fontSize = 11.sp,
                            color = colors.primaryOrange,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        buildString {
                            append(address.calle)
                            if (address.numero.isNotBlank()) append(" ${address.numero}")
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                    Text(
                        buildString {
                            append(address.localidad)
                            if (address.provincia.isNotBlank()) append(", ${address.provincia}")
                        },
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Expanded detail
            if (expanded) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = colors.divider)
                Spacer(Modifier.height(10.dp))

                if (address.codigoPostal.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MailOutline, null, tint = colors.textSecondary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("CP: ${address.codigoPostal}", fontSize = 11.sp, color = colors.textSecondary)
                    }
                    Spacer(Modifier.height(4.dp))
                }

                if (address.provincia.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Map, null, tint = colors.textSecondary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(address.provincia, fontSize = 11.sp, color = colors.textSecondary)
                    }
                    Spacer(Modifier.height(4.dp))
                }

                if (address.pais.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Public, null, tint = colors.textSecondary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(address.pais, fontSize = 11.sp, color = colors.textSecondary)
                    }
                    Spacer(Modifier.height(4.dp))
                }

                val lat = address.latitude
                val lng = address.longitude
                if (lat != null && lng != null && (lat != 0.0 || lng != 0.0)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MyLocation, null, tint = colors.primaryOrange, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "%.5f, %.5f".format(lat, lng),
                            fontSize = 11.sp,
                            color = colors.primaryOrange
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Botones acción
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onOpenMaps,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryOrange),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(colors.primaryOrange.copy(alpha = 0.5f))
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Map, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Abrir en Maps", fontSize = 11.sp)
                    }

                    if (isEditMode) {
                        TextButton(
                            onClick = onEdit,
                            colors = ButtonDefaults.textButtonColors(contentColor = colors.primaryOrange)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Editar", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = colors.error)
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Borrar", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BOTTOM SHEET para agregar / editar dirección
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressBottomSheet(
    initial: AddressProvider = AddressProvider(),
    onDismiss: () -> Unit,
    onSave: (AddressProvider) -> Unit
) {
    val colors = getPrestadorColors()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var label    by remember { mutableStateOf(initial.label) }
    var street   by remember { mutableStateOf(initial.calle) }
    var number   by remember { mutableStateOf(initial.numero) }
    var city     by remember { mutableStateOf(initial.localidad) }
    var province by remember { mutableStateOf(initial.provincia) }
    var postalCode by remember { mutableStateOf(initial.codigoPostal) }
    var country  by remember { mutableStateOf(initial.pais) }
    var lat      by remember { mutableStateOf(initial.latitude?.let { if (it != 0.0) "%.6f".format(it) else "" } ?: "") }
    var lng      by remember { mutableStateOf(initial.longitude?.let { if (it != 0.0) "%.6f".format(it) else "" } ?: "") }
    var syncing  by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.primaryOrange,
        unfocusedBorderColor = colors.border,
        focusedLabelColor = colors.primaryOrange,
        unfocusedLabelColor = colors.textSecondary,
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary,
        cursorColor = colors.primaryOrange,
        focusedContainerColor = colors.surfaceElevated,
        unfocusedContainerColor = colors.surfaceColor
    )
    val fieldShape = RoundedCornerShape(10.dp)
    val textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = colors.textPrimary)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.backgroundColor,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(colors.border, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Título
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.LocationOn, null, tint = colors.primaryOrange, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (initial.calle.isBlank()) "Nueva dirección" else "Editar dirección",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = colors.textSecondary)
                }
            }

            HorizontalDivider(color = colors.divider)

            // Chips de etiqueta
            val labelOptions = listOf("🏠 Casa", "🏢 Oficina", "🔧 Trabajo", "📍 Otro")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                labelOptions.forEach { option ->
                    val selected = label == option
                    FilterChip(
                        selected = selected,
                        onClick = { label = if (selected) "" else option },
                        label = { Text(option, fontSize = 11.sp) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.primaryOrange,
                            selectedLabelColor = Color.White,
                            containerColor = colors.chipBackground,
                            labelColor = colors.chipText
                        )
                    )
                }
            }

            // GPS: boton de sync rápido
            OutlinedButton(
                onClick = {
                    val query = buildString {
                        append(street.trim())
                        if (number.isNotBlank()) append(" ${number.trim()}")
                        if (city.isNotBlank()) append(", ${city.trim()}")
                        if (province.isNotBlank()) append(", ${province.trim()}")
                    }
                    if (query.isBlank()) return@OutlinedButton
                    syncing = true
                    scope.launch(Dispatchers.IO) {
                        try {
                            @Suppress("DEPRECATION")
                            val addresses = Geocoder(context, Locale.getDefault())
                                .getFromLocationName(query, 1)
                            if (!addresses.isNullOrEmpty()) {
                                val addr = addresses[0]
                                withContext(Dispatchers.Main) {
                                    lat = "%.6f".format(java.util.Locale.US, addr.latitude)
                                    lng = "%.6f".format(java.util.Locale.US, addr.longitude)
                                    if (province.isBlank() && addr.adminArea != null) province = addr.adminArea
                                    if (country.isBlank() && addr.countryName != null) country = addr.countryName
                                    if (postalCode.isBlank() && addr.postalCode != null) postalCode = addr.postalCode
                                }
                            }
                        } catch (_: Exception) {
                        } finally {
                            withContext(Dispatchers.Main) { syncing = false }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryOrange),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(colors.primaryOrange)
                )
            ) {
                if (syncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = colors.primaryOrange,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Sincronizando...", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                } else {
                    Icon(Icons.Default.GpsFixed, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("SINCRONIZAR COORDENADAS", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Calle + Número
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = street,
                    onValueChange = { street = it },
                    label = { Text("Calle", fontSize = 11.sp) },
                    textStyle = textStyle,
                    shape = fieldShape,
                    colors = fieldColors,
                    modifier = Modifier.weight(2f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Número", fontSize = 11.sp) },
                    textStyle = textStyle,
                    shape = fieldShape,
                    colors = fieldColors,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // Localidad
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Localidad", fontSize = 11.sp) },
                textStyle = textStyle,
                shape = fieldShape,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Provincia + CP
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = province,
                    onValueChange = { province = it },
                    label = { Text("Provincia", fontSize = 11.sp) },
                    textStyle = textStyle,
                    shape = fieldShape,
                    colors = fieldColors,
                    modifier = Modifier.weight(2f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = postalCode,
                    onValueChange = { raw ->
                        val filtered = raw.uppercase().filter { c -> c.isLetter() || c.isDigit() }
                        val letra = filtered.firstOrNull()?.takeIf { it.isLetter() }?.toString() ?: ""
                        val digitos = filtered.drop(if (letra.isNotEmpty()) 1 else 0).filter { c -> c.isDigit() }.take(4)
                        postalCode = (letra + digitos).take(5)
                    },
                    label = { Text("CP", fontSize = 11.sp) },
                    placeholder = { Text("T4000", fontSize = 11.sp ) },
                    textStyle = textStyle,
                    shape = fieldShape,
                    colors = fieldColors,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }

            // País
            OutlinedTextField(
                value = country,
                onValueChange = { country = it },
                label = { Text("País", fontSize = 11.sp) },
                textStyle = textStyle,
                shape = fieldShape,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Latitud / Longitud (read-only tras sync)
            if (lat.isNotBlank() || lng.isNotBlank()) {
                HorizontalDivider(color = colors.divider)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = lat,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Latitud", fontSize = 11.sp) },
                        textStyle = textStyle.copy(color = colors.primaryOrange),
                        shape = fieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primaryOrange.copy(alpha = 0.5f),
                            unfocusedBorderColor = colors.primaryOrange.copy(alpha = 0.3f),
                            focusedLabelColor = colors.primaryOrange,
                            unfocusedLabelColor = colors.primaryOrange.copy(alpha = 0.7f),
                            focusedTextColor = colors.primaryOrange,
                            unfocusedTextColor = colors.primaryOrange,
                            focusedContainerColor = colors.primaryOrange.copy(alpha = 0.05f),
                            unfocusedContainerColor = colors.primaryOrange.copy(alpha = 0.05f)
                        ),
                        leadingIcon = { Icon(Icons.Default.MyLocation, null, tint = colors.primaryOrange, modifier = Modifier.size(14.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lng,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Longitud", fontSize = 11.sp) },
                        textStyle = textStyle.copy(color = colors.primaryOrange),
                        shape = fieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primaryOrange.copy(alpha = 0.5f),
                            unfocusedBorderColor = colors.primaryOrange.copy(alpha = 0.3f),
                            focusedLabelColor = colors.primaryOrange,
                            unfocusedLabelColor = colors.primaryOrange.copy(alpha = 0.7f),
                            focusedTextColor = colors.primaryOrange,
                            unfocusedTextColor = colors.primaryOrange,
                            focusedContainerColor = colors.primaryOrange.copy(alpha = 0.05f),
                            unfocusedContainerColor = colors.primaryOrange.copy(alpha = 0.05f)
                        ),
                        leadingIcon = { Icon(Icons.Default.MyLocation, null, tint = colors.primaryOrange, modifier = Modifier.size(14.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Botón Guardar
            Button(
                onClick = {
                    scope.launch {
                        // Auto-geocodificar si lat/lng están vacíos
                        if (lat.isBlank() && street.isNotBlank()) {
                            syncing = true
                            val query = buildString {
                                append(street.trim())
                                if (number.isNotBlank()) append(" ${number.trim()}")
                                if (city.isNotBlank()) append(", ${city.trim()}")
                                if (province.isNotBlank()) append(", ${province.trim()}")
                            }
                            try {
                                val addresses = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    @Suppress("DEPRECATION")
                                    android.location.Geocoder(context, java.util.Locale.getDefault())
                                        .getFromLocationName(query, 1)
                                }
                                if (!addresses.isNullOrEmpty()) {
                                    lat = "%.6f".format(java.util.Locale.US, addresses[0].latitude)
                                    lng = "%.6f".format(java.util.Locale.US, addresses[0].longitude)
                                }
                            } catch (_: Exception) {
                            } finally {
                                syncing = false
                            }
                        }
                        val newAddress = initial.copy(
                            label    = label,
                            calle    = street.trim(),
                            numero   = number.trim(),
                            localidad = city.trim(),
                            provincia = province.trim(),
                            codigoPostal = postalCode.trim(),
                            pais     = country.trim(),
                            latitude  = lat.toDoubleOrNull() ?: initial.latitude,
                            longitude = lng.toDoubleOrNull() ?: initial.longitude
                        )
                        onSave(newAddress)
                        sheetState.hide()
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryOrange,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("GUARDAR DIRECCIÓN", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
