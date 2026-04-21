package com.example.myapplication.presentation.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.data.model.AddressClient
import com.example.myapplication.data.model.BranchClient
import com.example.myapplication.data.model.CompanyClient
import com.example.myapplication.data.model.RepresentativeClient
import com.example.myapplication.presentation.client.UbicacionClimaViewModel
import com.example.myapplication.presentation.components.Utilidades.BentoBottomSheetContent
import com.example.myapplication.presentation.components.Utilidades.BentoTextFieldM3
import com.example.myapplication.presentation.components.Utilidades.MaverickColors.BentoBorderBrush
import com.example.myapplication.presentation.components.Utilidades.MaverickColors.BentoDarkGlassBackground
import com.example.myapplication.presentation.components.Utilidades.MaverickColors.BentoGlassBrush
import com.example.myapplication.presentation.components.Utilidades.MaverickColors.GeminiAccent
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * 6. HOJA PARA AGREGAR/EDITAR UN CONTACTO (EMAIL O TELÉFONO)
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

    BentoBottomSheetContent(
        title = title,
        emoji = emoji,
        onClose = onClose,
        showPrimaryButton = true,
        onPrimaryButtonClick = { onSave(value) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            BentoTextFieldM3(
                value = value,
                onValueChange = { value = it },
                label = label,
                emoji = emoji
            )
        }
    }
}

/**
 * 4. HOJA DE EDICIÓN DE EMPRESA (Company)
 */
@OptIn(ExperimentalMaterial3Api::class)
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

    BentoBottomSheetContent(
        title = "Datos de Empresa",
        emoji = "🏢",
        onClose = onClose,
        showPrimaryButton = true,
        onPrimaryButtonClick = {
            onSave(company.copy(name = name, cuit = cuit, email = email, razonSocial = razonSocial))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            BentoTextFieldM3(value = name, onValueChange = { name = it }, label = "Nombre Comercial", emoji = "🏢")
            Spacer(modifier = Modifier.height(12.dp))
            BentoTextFieldM3(value = razonSocial, onValueChange = { razonSocial = it }, label = "Razón Social", emoji = "🏭")
            Spacer(modifier = Modifier.height(12.dp))
            BentoTextFieldM3(value = cuit, onValueChange = { cuit = it }, label = "CUIT", emoji = "🆔")
            Spacer(modifier = Modifier.height(12.dp))
            BentoTextFieldM3(value = email, onValueChange = { email = it }, label = "Email Corporativo", emoji = "📧")
        }
    }
}

/**
 * 3. HOJA DE EDICIÓN DE SUCURSAL (Branch)
 */
@Composable
fun EditBranchSheetContent(
    branch: BranchClient,
    onSave: (BranchClient) -> Unit,
    onClose: () -> Unit
) {
    var name by remember { mutableStateOf(branch.name) }
    var isMain by remember { mutableStateOf(branch.isMainBranch) }

    BentoBottomSheetContent(
        title = "Editar Sucursal",
        emoji = "🏪",
        onClose = onClose,
        showPrimaryButton = true,
        onPrimaryButtonClick = { onSave(branch.copy(name = name, isMainBranch = isMain)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            BentoTextFieldM3(value = name, onValueChange = { name = it }, label = "Nombre de Sucursal", emoji = "🏪")

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 16.dp)) {
                Checkbox(
                    checked = isMain,
                    onCheckedChange = { isMain = it },
                    colors = CheckboxDefaults.colors(checkedColor = GeminiAccent, uncheckedColor = Color.Gray)
                )
                Spacer(Modifier.width(8.dp))
                Text("Es Casa Central / Sede Principal", color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

/**
 * 1. HOJA DE EDICIÓN DE UBICACIÓN (Address)
 * ACTUALIZADA CON FUNCIONES DE GPS Y SINCRONIZACIÓN TOTAL INTELIGENTE
 */
@Composable
fun EditAddressSheetContent(
    address: AddressClient,
    onSave: (AddressClient) -> Unit,
    onClose: () -> Unit = {},
    ubicacionViewModel: UbicacionClimaViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    EditAddressSheetContentUI(
        address = address,
        onSave = onSave,
        onClose = onClose,
        onGpsClick = { onResultado ->
            ubicacionViewModel.ejecutarCalculoUbicacionGps(context, onResultado = onResultado)
        },
        onSyncClick = { lat, lng, fullAddress, onResultado ->
            if (lat != 0.0 && lng != 0.0) {
                ubicacionViewModel.ejecutarBusquedaPorCoordenadas(context, lat, lng, onResultado)
            } else {
                ubicacionViewModel.ejecutarBusquedaPorTexto(context, fullAddress, onResultado)
            }
        }
    )
}

@Composable
fun EditAddressSheetContentUI(
    address: AddressClient,
    onSave: (AddressClient) -> Unit,
    onClose: () -> Unit = {},
    onGpsClick: (onResultado: (String, String, String, String, String, String, Double, Double) -> Unit) -> Unit = {},
    onSyncClick: (lat: Double, lng: Double, fullAddress: String, onResultado: (String, String, String, String, String, String, Double, Double) -> Unit) -> Unit = { _, _, _, _ -> }
) {
    var calle by remember { mutableStateOf(address.calle) }
    var numero by remember { mutableStateOf(address.numero) }
    var localidad by remember { mutableStateOf(address.localidad) }
    var provincia by remember { mutableStateOf(address.provincia) }
    var pais by remember { mutableStateOf(address.pais) }
    var codigoPostal by remember { mutableStateOf(address.codigoPostal) }
    var label by remember { mutableStateOf(address.label) }

    // --- VARIABLES PARA COORDENADAS ---
    var latitude by remember { mutableDoubleStateOf(address.latitude) }
    var longitude by remember { mutableDoubleStateOf(address.longitude) }

    // --- [NUEVO] VALIDACIÓN PARA HABILITAR BOTÓN ACEPTAR ---
    val isFormValid = codigoPostal.isNotBlank() && latitude != 0.0 && longitude != 0.0

    val emojis = listOf("🏠 Casa", "🏢 Oficina", "👵 Abuela", "🏖️ Vacaciones", "📍 Otro")

    BentoBottomSheetContent(
        title = "Ubicación",
        emoji = "📍",
        onClose = onClose,
        showPrimaryButton = isFormValid, // El botón solo aparece si el formulario es válido
        onPrimaryButtonClick = {
            onSave(address.copy(
                calle = calle,
                numero = numero,
                localidad = localidad,
                provincia = provincia,
                pais = pais,
                codigoPostal = codigoPostal,
                label = label,
                latitude = latitude,
                longitude = longitude
            ))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // --- SECCIÓN: BOTÓN GPS (GPS -> DIRECCIÓN COMPLETA) ---
            Button(
                onClick = {
                    onGpsClick { p, prov, loc, c, n, cp, lat, lng ->
                        pais = p; provincia = prov; localidad = loc; calle = c; numero = n; codigoPostal = cp; latitude = lat; longitude = lng
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GeminiAccent.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GeminiAccent.copy(alpha = 0.3f))
            ) {
                Icon(Icons.Default.MyLocation, null, tint = GeminiAccent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(text = "USAR MI UBICACIÓN GPS ACTUAL", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Text(text = "Tipo de dirección", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                items(emojis) { item ->
                    val isSelected = label == item
                    Surface(
                        onClick = { label = item },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) GeminiAccent.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                        border = if (isSelected) BorderStroke(1.dp, GeminiAccent) else null
                    ) {
                        Text(text = item, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = if (isSelected) GeminiAccent else Color.White)
                    }
                }
            }

            BentoTextFieldM3(value = label, onValueChange = { label = it }, label = "Etiqueta personalizada", emoji = "🏷️")
            Spacer(modifier = Modifier.height(12.dp))
            BentoTextFieldM3(value = calle, onValueChange = { calle = it }, label = "Calle / Avenida", emoji = "📍")

            Spacer(modifier = Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(0.4f)) {
                    BentoTextFieldM3(value = numero, onValueChange = { numero = it }, label = "Nº", emoji = "🔢")

                    // --- [MEJORADO] BOTÓN SINCRONIZAR TOTAL ---
                    TextButton(
                        onClick = {
                            val fullAddress = "$calle $numero, $localidad, $provincia, $pais"
                            onSyncClick(latitude, longitude, fullAddress) { p, prov, loc, c, n, cp, lat, lng ->
                                pais = p; provincia = prov; localidad = loc; calle = c; numero = n; codigoPostal = cp; if (lat != 0.0) latitude = lat; if (lng != 0.0) longitude = lng
                            }
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.Sync, null, modifier = Modifier.size(14.dp), tint = GeminiAccent)
                        Spacer(Modifier.width(4.dp))
                        Text(text = "SINCRONIZAR", fontSize = 9.sp, color = GeminiAccent, fontWeight = FontWeight.Bold)
                    }
                }
                Box(Modifier.weight(0.6f)) { BentoTextFieldM3(value = localidad, onValueChange = { localidad = it }, label = "Localidad", emoji = "🏙️") }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(0.6f)) { BentoTextFieldM3(value = provincia, onValueChange = { provincia = it }, label = "Provincia", emoji = "🗺️") }
                Box(Modifier.weight(0.4f)) { BentoTextFieldM3(value = codigoPostal, onValueChange = { codigoPostal = it }, label = "C.P.", emoji = "📮") }
            }
            Spacer(modifier = Modifier.height(12.dp))
            BentoTextFieldM3(value = pais, onValueChange = { pais = it }, label = "País", emoji = "🌐")

            // --- SECCIÓN: COORDENADAS GPS (MANDATORIAS) ---
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "COORDENADAS (OBLIGATORIAS PARA GUARDAR)", color = if(latitude == 0.0) Color.Red.copy(alpha=0.6f) else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    BentoTextFieldM3(
                        value = if(latitude != 0.0) latitude.toString() else "",
                        onValueChange = { latitude = it.toDoubleOrNull() ?: 0.0 },
                        label = "Latitud",
                        emoji = "🌐"
                    )
                }
                Box(Modifier.weight(1f)) {
                    BentoTextFieldM3(
                        value = if(longitude != 0.0) longitude.toString() else "",
                        onValueChange = { longitude = it.toDoubleOrNull() ?: 0.0 },
                        label = "Longitud",
                        emoji = "🌐"
                    )
                }
            }
        }
    }
}

/**
 * 2. HOJA DE EDICIÓN DE EQUIPO DE TRABAJO (Representative)
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

    BentoBottomSheetContent(
        title = if (representative == null) "Nuevo Miembro" else "Editar Miembro",
        emoji = "👤",
        onClose = onClose,
        showPrimaryButton = true,
        onPrimaryButtonClick = {
            onSave((representative ?: RepresentativeClient()).copy(
                nombre = nombre,
                apellido = apellido,
                cargo = cargo,
                photoUrl = photoUrl
            ))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // FOTO DE PERFIL CON EFECTO PREMIUM
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .shadow(elevation = 16.dp, shape = CircleShape, ambientColor = GeminiAccent, spotColor = GeminiAccent)
                    .clip(CircleShape)
                    .background(BentoDarkGlassBackground)
                    .background(BentoGlassBrush)
                    .border(1.5.dp, BentoBorderBrush, CircleShape)
                    .clickable { photoLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        null,
                        modifier = Modifier.size(50.dp),
                        tint = Color.White.copy(alpha = 0.4f)
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .size(32.dp)
                        .background(GeminiAccent, CircleShape)
                        .border(2.dp, BentoDarkGlassBackground, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AddAPhoto, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            BentoTextFieldM3(value = nombre, onValueChange = { nombre = it }, label = "Nombre", emoji = "👤")
            Spacer(modifier = Modifier.height(12.dp))
            BentoTextFieldM3(value = apellido, onValueChange = { apellido = it }, label = "Apellido", emoji = "👤")
            Spacer(modifier = Modifier.height(12.dp))
            BentoTextFieldM3(value = cargo, onValueChange = { cargo = it }, label = "Cargo / Puesto", emoji = "💼")

            if (onDelete != null) {
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.7f))) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Eliminar de la sucursal", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun PreviewEditAddressSheetContent() {
    val sampleAddress = AddressClient(
        calle = "9 de Julio",
        numero = "100",
        localidad = "San Miguel de Tucumán",
        provincia = "Tucumán",
        pais = "Argentina",
        codigoPostal = "4000",
        latitude = -26.8241,
        longitude = -65.2226,
        label = "🏠 Casa"
    )

    MyApplicationTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
            EditAddressSheetContentUI(
                address = sampleAddress,
                onSave = {},
                onClose = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun PreviewEditBranchSheetContent() {
    val sampleBranch = BranchClient(
        name = "Sucursal Central",
        isMainBranch = true
    )

    MyApplicationTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
            EditBranchSheetContent(
                branch = sampleBranch,
                onSave = {},
                onClose = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun PreviewEditCompanySheetContent() {
    val sampleCompany = CompanyClient(
        name = "Informática Maverick",
        razonSocial = "Maverick S.A.",
        cuit = "30-12345678-9",
        email = "contacto@maverick.com"
    )

    MyApplicationTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
            EditCompanySheetContent(
                company = sampleCompany,
                onSave = {},
                onClose = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun PreviewEditSingleContactSheetContent() {
    MyApplicationTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
            EditSingleContactSheetContent(
                initialValue = "example@mail.com",
                title = "Editar Email",
                emoji = "📧",
                label = "Correo Electrónico",
                onSave = {},
                onClose = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun PreviewEditRepresentativeSheetContent() {
    val sampleRepresentative = RepresentativeClient(
        nombre = "Juan",
        apellido = "Pérez",
        cargo = "Gerente de Ventas"
    )

    MyApplicationTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
            EditRepresentativeSheetContent(
                representative = sampleRepresentative,
                onSave = {},
                onClose = {},
                onDelete = {}
            )
        }
    }
}
