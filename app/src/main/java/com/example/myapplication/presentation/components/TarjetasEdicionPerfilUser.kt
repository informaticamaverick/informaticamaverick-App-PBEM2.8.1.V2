package com.example.myapplication.presentation.components

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.presentation.features.home.UbicacionClimaViewModel
import com.example.myapplication.core.domain.model.AddressUnico
import com.example.myapplication.core.domain.model.BranchClient
import com.example.myapplication.core.domain.model.CompanyClient
import com.example.myapplication.core.domain.model.RepresentativeClient
import com.example.myapplication.presentation.features.profile.EditMode
import com.example.myapplication.presentation.features.profile.UserUiState
import com.example.myapplication.presentation.features.profile.UserViewModel
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme

private val PremiumAccent = Color(0xFF3B82F6) // Azul Premium Moderno (Pixel Style)

/**
 * --- MAVERICK ELITE PROFILE SHEETS (M3 Expressive / Android 17 style) ---
 * Rediseño premium con enfoque en tarjetas modulares, jerarquía visual y Glassmorphism.
 * Versión 3.6: Fondo mate, lógica de calle corregida y acento azul.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditSheetOrchestrator(
    editMode: EditMode,
    uiState: UserUiState,
    viewModel: UserViewModel,
    onClose: () -> Unit,
    onRequestDelete: (String, String, () -> Unit) -> Unit
) {
    if (editMode == EditMode.None) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = MaverickColors.EliteMainBackground,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.15f)) },
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.75f),
        contentWindowInsets = { WindowInsets(0) }
    ) {
        Box(modifier = Modifier.fillMaxHeight(0.92f)) {
            when (val mode = editMode) {
                is EditMode.BranchAddress -> {
                    EditAddressSheetContent(
                        address = mode.address,
                        onSave = { updatedAddr ->
                            val updatedBranch = mode.branch.copy(address = updatedAddr)
                            val updatedBranches = mode.company.branches.map { if(it.id == updatedBranch.id) updatedBranch else it }
                            val currentCompanies = uiState.companies.map { if(it.id == mode.company.id) mode.company.copy(branches = updatedBranches) else it }
                            viewModel.updateCompanies(currentCompanies)
                            onClose()
                        },
                        onClose = onClose
                    )
                }
                is EditMode.Representative -> {
                    EditRepresentativeSheetContent(
                        representative = mode.representative,
                        onSave = { updatedRep ->
                            val photoUri = updatedRep.photoUrl?.let { if (it.startsWith("content://") || it.startsWith("file://")) Uri.parse(it) else null }
                            viewModel.saveRepresentative(mode.company, mode.branch, updatedRep, photoUri)
                            onClose()
                        },
                        onClose = onClose,
                        onDelete = if (mode.representative != null) {
                            {
                                onRequestDelete("Eliminar Miembro", "¿Estás seguro que deseas eliminar a ${mode.representative.nombre} del equipo?") {
                                    val currentReps = mode.branch.representatives.filter { it.id != mode.representative.id }
                                    val updatedBranch = mode.branch.copy(representatives = currentReps)
                                    val updatedBranches = mode.company.branches.map { if(it.id == updatedBranch.id) updatedBranch else it }
                                    val currentCompanies = uiState.companies.map { if(it.id == mode.company.id) mode.company.copy(branches = updatedBranches) else it }
                                    viewModel.updateCompanies(currentCompanies)
                                    onClose()
                                }
                            }
                        } else null
                    )
                }
                is EditMode.PersonalAddress -> {
                    EditAddressSheetContent(
                        address = mode.address ?: AddressUnico(),
                        onSave = { updated ->
                            Log.d("ProfileEdit", "📍 [SAVE_ADDRESS] Nueva dirección: ${updated.calle} ${updated.numero}")
                            val current = uiState.personalAddresses.toMutableList()
                            val idx = current.indexOfFirst { it.id == updated.id }
                            if (idx != -1) {
                                current[idx] = updated
                                Log.d("ProfileEdit", "📍 [SAVE_ADDRESS] Dirección existente actualizada.")
                            } else {
                                current.add(updated)
                                Log.d("ProfileEdit", "📍 [SAVE_ADDRESS] Nueva dirección añadida a la lista.")
                            }
                            viewModel.updatePersonalAddresses(current)
                            onClose()
                        },
                        onClose = onClose
                    )
                }
                is EditMode.Company -> {
                    EditCompanySheetContent(
                        company = mode.company,
                        onSave = { updated ->
                            val photoUri = updated.photoUrl?.let { if (it.startsWith("content://") || it.startsWith("file://")) Uri.parse(it) else null }
                            viewModel.saveCompany(updated, photoUri)
                            onClose()
                        },
                        onClose = onClose
                    )
                }
                is EditMode.Branch -> {
                    EditBranchSheetContent(
                        branch = mode.branch ?: BranchClient(),
                        onSave = { updated ->
                            val current = mode.company.branches.toMutableList()
                            val idx = current.indexOfFirst { it.id == updated.id }
                            if (idx != -1) current[idx] = updated else current.add(updated)
                            val currentCompanies = uiState.companies.map { if(it.id == mode.company.id) mode.company.copy(branches = current) else it }
                            viewModel.updateCompanies(currentCompanies)
                            onClose()
                        },
                        onClose = onClose
                    )
                }
                is EditMode.Email -> {
                    EditSingleContactSheetContent(
                        initialValue = mode.initialValue,
                        title = if (mode.index == null) "Nuevo Email" else "Editar Email",
                        emoji = "📧",
                        label = "Correo Electrónico",
                        onSave = { updated ->
                            val current = uiState.additionalEmails.toMutableList()
                            if (mode.index != null) current[mode.index] = updated else current.add(updated)
                            viewModel.updateAdditionalEmails(current)
                            onClose()
                        },
                        onClose = onClose
                    )
                }
                is EditMode.Phone -> {
                    EditSingleContactSheetContent(
                        initialValue = mode.initialValue,
                        title = if (mode.index == null) "Nuevo Teléfono" else "Editar Teléfono",
                        emoji = "📱",
                        label = "Número de Teléfono",
                        onSave = { updated ->
                            val current = uiState.additionalPhones.toMutableList()
                            if (mode.index != null) current[mode.index] = updated else current.add(updated)
                            viewModel.updateAdditionalPhones(current)
                            onClose()
                        },
                        onClose = onClose
                    )
                }
                else -> {}
            }
        }
    }
}

/**
 * --- ELITE SHEET LAYOUT ---
 */
@Composable
fun EliteSheetLayout(
    title: String,
    emoji: String,
    onClose: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(MaverickColors.EliteMainBackground)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onClose) {
                Text("Cancelar", color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 18.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    title.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    color = Color.White
                )
            }

            Button(
                onClick = onSave,
                enabled = canSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PremiumAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text("Guardar", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            content()
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
fun EliteSectionHeader(text: String, color: Color = PremiumAccent) {
    Row(
        modifier = Modifier.padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(3.dp).height(12.dp).background(color, CircleShape))
        Spacer(Modifier.width(10.dp))
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(0.4f),
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EliteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp), // Altura Pro
        readOnly = readOnly,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        label = { 
            Text(
                text = label, 
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp
            ) 
        },
        textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White),
        leadingIcon = if (emoji != null) {
            {
                Surface(
                    color = Color.White.copy(0.06f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(start = 12.dp).size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(emoji, fontSize = 16.sp)
                    }
                }
            }
        } else null,
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.White.copy(0.03f),
            unfocusedContainerColor = Color.White.copy(0.01f),
            focusedBorderColor = PremiumAccent,
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedLabelColor = PremiumAccent,
            unfocusedLabelColor = Color.Gray,
            cursorColor = PremiumAccent
        )
    )
}

/**
 * --- HOJA DE EDICIÓN DE DIRECCIÓN (PRO EDITION) ---
 */
@Composable
fun EditAddressSheetContent(
    address: AddressUnico,
    onSave: (AddressUnico) -> Unit,
    onClose: () -> Unit,
    ubicacionViewModel: UbicacionClimaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var calle by remember { mutableStateOf(address.calle) }
    var numero by remember { mutableStateOf(address.numero) }
    var piso by remember { mutableStateOf(address.piso) }
    var departamento by remember { mutableStateOf(address.departamento) }
    var localidad by remember { mutableStateOf(address.localidad) }
    var provincia by remember { mutableStateOf(address.provincia) }
    var pais by remember { mutableStateOf(address.pais) }
    var codigoPostal by remember { mutableStateOf(address.codigoPostal) }
    var label by remember { mutableStateOf(address.label) }
    var latitude by remember { mutableDoubleStateOf(address.latitude) }
    var longitude by remember { mutableDoubleStateOf(address.longitude) }

    val isFormValid = codigoPostal.isNotBlank() && latitude != 0.0 && longitude != 0.0

    EliteSheetLayout(
        title = "Dirección",
        emoji = "📍",
        onClose = onClose,
        onSave = {
            onSave(address.copy(
                calle = calle, numero = numero, piso = piso, departamento = departamento,
                localidad = localidad, provincia = provincia, pais = pais, 
                codigoPostal = codigoPostal, label = label, latitude = latitude, longitude = longitude
            ))
        },
        canSave = isFormValid
    ) {
        // TARJETA GPS DE ALTA PRECISIÓN
        Surface(
            onClick = {
                ubicacionViewModel.ejecutarCalculoUbicacionGps(context) { p, prov, loc, c, n, cp, lat, lng ->
                    pais = p; provincia = prov; localidad = loc; calle = c; numero = n; codigoPostal = cp; latitude = lat; longitude = lng
                }
            },
            color = PremiumAccent.copy(0.12f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, PremiumAccent.copy(0.3f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(color = PremiumAccent, shape = CircleShape, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MyLocation, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Autodetectar Ubicación", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 15.sp)
                    Text("Sincronización GPS de precisión", fontSize = 11.sp, color = PremiumAccent)
                }
            }
        }

        EliteSectionHeader("Identificación")
        EliteTextField(value = label, onValueChange = { label = it }, label = "Nombre Dirección (Ej: Casa)", emoji = "🏷️")
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
            items(listOf("🏠 Casa", "🏢 Trabajo", "📍 Local", "📦 Depósito", "👵 Familia")) { item ->
                val isSelected = label == item
                FilterChip(
                    selected = isSelected,
                    onClick = { label = item },
                    label = { Text(item, fontSize = 10.sp) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PremiumAccent.copy(0.2f),
                        selectedLabelColor = PremiumAccent,
                        containerColor = Color.White.copy(0.04f)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true, selected = isSelected,
                        borderColor = Color.White.copy(0.1f), selectedBorderColor = PremiumAccent
                    )
                )
            }
        }

        EliteSectionHeader("Datos Postales")
        Card(
            colors = CardDefaults.cardColors(containerColor = MaverickColors.EliteSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(0.05f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        EliteTextField(value = calle, onValueChange = { calle = it }, label = "Calle / Avenida", emoji = "🛣️")
                    }
                    IconButton(
                        onClick = {
                            if (calle.isNotBlank()) {
                                // Mantenemos el número actual por si Google no lo devuelve exacto
                                val numeroActual = numero 
                                ubicacionViewModel.ejecutarBusquedaPorTexto(context, "$calle $numero, $localidad, $provincia, $pais") { p, prov, loc, c, n, cp, lat, lng ->
                                    pais = p
                                    provincia = prov
                                    localidad = loc
                                    // Solo sobreescribimos la calle si no está vacía
                                    if (c.isNotBlank()) calle = c
                                    // Si Google devuelve un número lo usamos, sino mantenemos el que el usuario escribió
                                    numero = if (n.isNotBlank()) n else numeroActual
                                    codigoPostal = cp
                                    latitude = lat
                                    longitude = lng
                                }
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.White.copy(0.06f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(0.12f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Sync, null, tint = PremiumAccent, modifier = Modifier.size(22.dp))
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(0.25f)) { EliteTextField(value = numero, onValueChange = { numero = it }, label = "Nº", emoji = "🔢") }
                    Box(Modifier.weight(0.25f)) { EliteTextField(value = piso, onValueChange = { piso = it }, label = "Piso") }
                    Box(Modifier.weight(0.5f)) { EliteTextField(value = departamento, onValueChange = { departamento = it }, label = "Depto") }
                }
                
                Spacer(Modifier.height(12.dp))

                EliteTextField(value = localidad, onValueChange = { localidad = it }, label = "Ciudad", emoji = "🏙️")
                
                Spacer(Modifier.height(12.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(0.55f)) { EliteTextField(value = provincia, onValueChange = { provincia = it }, label = "Provincia", emoji = "🗺️") }
                    Box(Modifier.weight(0.45f)) { EliteTextField(value = codigoPostal, onValueChange = { codigoPostal = it }, label = "C.P.", emoji = "📮") }
                }
                
                Spacer(Modifier.height(12.dp))
                EliteTextField(value = pais, onValueChange = { pais = it }, label = "País", emoji = "🌐")
            }
        }

        EliteSectionHeader("Geolocalización", color = if(latitude == 0.0) Color.Red.copy(0.7f) else PremiumAccent)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaverickColors.EliteSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if(latitude == 0.0) Color.Red.copy(0.3f) else Color.White.copy(0.05f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("LATITUD", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
                    Text(if(latitude != 0.0) latitude.toString().take(12) else "...", fontSize = 14.sp, color = if(latitude == 0.0) Color.Red.copy(0.6f) else Color.White, fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f)) {
                    Text("LONGITUD", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
                    Text(if(longitude != 0.0) longitude.toString().take(12) else "...", fontSize = 14.sp, color = if(longitude == 0.0) Color.Red.copy(0.6f) else Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * --- MIEMBRO DEL EQUIPO ---
 */
@Composable
fun EditRepresentativeSheetContent(
    representative: RepresentativeClient?,
    onSave: (RepresentativeClient) -> Unit,
    onClose: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var nombre by remember { mutableStateOf(representative?.nombre ?: "") }
    var apellido by remember { mutableStateOf(representative?.apellido ?: "") }
    var cargo by remember { mutableStateOf(representative?.cargo ?: "") }
    var photoUrl by remember { mutableStateOf(representative?.photoUrl) }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUrl = uri?.toString()
    }

    EliteSheetLayout(
        title = "Miembro",
        emoji = "👤",
        onClose = onClose,
        onSave = {
            onSave((representative ?: RepresentativeClient()).copy(
                nombre = nombre, apellido = apellido, cargo = cargo, photoUrl = photoUrl
            ))
        }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.05f))
                    .border(2.dp, PremiumAccent, CircleShape)
                    .clickable { photoLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (photoUrl != null) {
                    AsyncImage(model = photoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(50.dp), tint = Color.Gray)
                }
                Box(modifier = Modifier.align(Alignment.BottomEnd).size(28.dp).background(PremiumAccent, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PhotoCamera, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(Modifier.height(28.dp))
            EliteTextField(value = nombre, onValueChange = { nombre = it }, label = "Nombre", emoji = "👤")
            Spacer(Modifier.height(12.dp))
            EliteTextField(value = apellido, onValueChange = { apellido = it }, label = "Apellido", emoji = "👤")
            Spacer(Modifier.height(12.dp))
            EliteTextField(value = cargo, onValueChange = { cargo = it }, label = "Cargo / Puesto", emoji = "💼")

            if (onDelete != null) {
                Spacer(Modifier.height(24.dp))
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(0.7f))) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Eliminar Miembro", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EditCompanySheetContent(
    company: CompanyClient,
    onSave: (CompanyClient) -> Unit,
    onClose: () -> Unit
) {
    var name by remember { mutableStateOf(company.name) }
    var cuit by remember { mutableStateOf(company.cuit) }
    var email by remember { mutableStateOf(company.email) }
    var razonSocial by remember { mutableStateOf(company.razonSocial) }
    var photoUrl by remember { mutableStateOf(company.photoUrl) }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUrl = uri?.toString()
    }

    EliteSheetLayout(
        title = "Negocio",
        emoji = "🏢",
        onClose = onClose,
        onSave = { onSave(company.copy(name = name, cuit = cuit, email = email, razonSocial = razonSocial, photoUrl = photoUrl)) }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.05f))
                    .border(2.dp, PremiumAccent, CircleShape)
                    .clickable { photoLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (photoUrl != null) {
                    AsyncImage(model = photoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.Business, null, modifier = Modifier.size(50.dp), tint = Color.Gray)
                }
                Box(modifier = Modifier.align(Alignment.BottomEnd).size(28.dp).background(PremiumAccent, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PhotoCamera, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        EliteSectionHeader("Identidad")
        EliteTextField(value = name, onValueChange = { name = it }, label = "Nombre Fantasía", emoji = "🏢")
        Spacer(Modifier.height(12.dp))
        EliteTextField(value = razonSocial, onValueChange = { razonSocial = it }, label = "Razón Social", emoji = "🏭")
        
        EliteSectionHeader("Contacto")
        EliteTextField(value = cuit, onValueChange = { cuit = it }, label = "CUIT", emoji = "🆔")
        Spacer(Modifier.height(12.dp))
        EliteTextField(value = email, onValueChange = { email = it }, label = "Email Comercial", emoji = "📧")
    }
}

@Composable
fun EditBranchSheetContent(
    branch: BranchClient,
    onSave: (BranchClient) -> Unit,
    onClose: () -> Unit
) {
    var name by remember { mutableStateOf(branch.name) }
    var isMain by remember { mutableStateOf(branch.isMainBranch) }

    EliteSheetLayout(
        title = "Sede",
        emoji = "🏪",
        onClose = onClose,
        onSave = { onSave(branch.copy(name = name, isMainBranch = isMain)) }
    ) {
        EliteSectionHeader("Sucursal")
        EliteTextField(value = name, onValueChange = { name = it }, label = "Nombre de la Sede", emoji = "🏪")
        
        Spacer(Modifier.height(20.dp))
        
        Surface(
            onClick = { isMain = !isMain },
            color = if(isMain) PremiumAccent.copy(0.1f) else Color.White.copy(0.02f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if(isMain) PremiumAccent.copy(0.3f) else Color.White.copy(0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isMain, onCheckedChange = { isMain = it }, colors = CheckboxDefaults.colors(checkedColor = PremiumAccent))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Sede Principal", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text("Prioridad máxima en el ecosistema", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

/**
 * --- CONTACTO ÚNICO ---
 */
@Composable
fun EditSingleContactSheetContent(
    initialValue: String,
    title: String,
    emoji: String,
    label: String,
    onSave: (String) -> Unit,
    onClose: () -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }

    EliteSheetLayout(
        title = title,
        emoji = emoji,
        onClose = onClose,
        onSave = { onSave(value) }
    ) {
        EliteSectionHeader(label)
        EliteTextField(value = value, onValueChange = { value = it }, label = label, emoji = emoji)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
fun PreviewPremiumAddressSheet() {
    MyApplicationTheme(darkTheme = true) {
        EditAddressSheetContent(
            address = AddressUnico(calle = "9 de Julio", numero = "123", localidad = "Tucumán", codigoPostal = "4000", latitude = -26.0, longitude = -65.0),
            onSave = {},
            onClose = {}
        )
    }
}
