package com.example.myapplication.prestador.ui.config

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.ui.theme.PrestadorOrange
import com.example.myapplication.prestador.ui.theme.getPrestadorColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceConfigScreen(
    onBack: () -> Unit,
    viewModel: ServiceConfigViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val configState by viewModel.configState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var hasPhysicalStore by remember { mutableStateOf(false) }
    var is24Hours by remember { mutableStateOf(false) }
    var hasHomeVisits by remember { mutableStateOf(false) }
    var hasStoreAppointments by remember { mutableStateOf(false) }
    var selectedServices by remember { mutableStateOf(listOf<String>()) }
    
    val availableServices = listOf(
        "Electricista", "Plomero", "Gasista", "Técnico PC",
        "Albañil", "Carpintero", "Pintor", "Jardinero",
        "Cerrajero", "Herrería", "Vidriería", "Climatización"
    )
    
    // Cargar configuración actual
    LaunchedEffect(Unit) {
        viewModel.loadCurrentConfig()
    }
    
    // Actualizar UI cuando se carga la configuración
    LaunchedEffect(configState) {
        configState?.let { config ->
            hasPhysicalStore = config.hasPhysicalStore
            is24Hours = config.is24Hours
            hasHomeVisits = config.hasHomeVisits
            hasStoreAppointments = config.hasStoreAppointments
            selectedServices = config.services
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        // TopBar personalizado
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.surfaceColor,
            shadowElevation = 2.dp
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
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = colors.textPrimary
                    )
                }
                
                Text(
                    text = "Configuración de Servicio",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 48.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        
        HorizontalDivider(
            color = colors.divider,
            thickness = 1.dp
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Card principal de configuración
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header de sección
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "CONFIGURACIÓN DE SERVICIO",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textSecondary,
                            letterSpacing = 0.5.sp
                        )
                    }
                    
                    // Switch: Local físico
                    SwitchRow(
                        text = "¿Local físico accesible al público?",
                        checked = hasPhysicalStore,
                        onCheckedChange = { 
                            hasPhysicalStore = it
                            if (!it) hasStoreAppointments = false
                        }
                    )
                    
                    // Switch: Servicio 24hs
                    SwitchRow(
                        text = "Servicio de Urgencia 24hs",
                        checked = is24Hours,
                        onCheckedChange = { is24Hours = it }
                    )
                    
                    // Switch: Visitas a domicilio
                    SwitchRow(
                        text = "Visitas técnicas a domicilio",
                        checked = hasHomeVisits,
                        onCheckedChange = { hasHomeVisits = it }
                    )
                    
                    // Switch: Turnos en local (depende de local físico)
                    SwitchRow(
                        text = "Brinda turnos en local",
                        checked = hasStoreAppointments,
                        onCheckedChange = { hasStoreAppointments = it },
                        enabled = hasPhysicalStore
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Card de servicios
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "SERVICIOS QUE PRESTA",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textSecondary,
                            letterSpacing = 0.5.sp
                        )
                    }
                    
                    Text(
                        text = "Selecciona los servicios que ofreces",
                        fontSize = 14.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Grid de servicios con FilterChips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableServices.forEach { service ->
                            FilterChip(
                                selected = selectedServices.contains(service),
                                onClick = {
                                    selectedServices = if (selectedServices.contains(service)) {
                                        selectedServices - service
                                    } else {
                                        selectedServices + service
                                    }
                                },
                                label = { Text(service) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primaryOrange.copy(alpha = 0.2f),
                                    selectedLabelColor = colors.primaryOrange
                                )
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Mensaje de error
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Botón guardar
            Button(
                onClick = {
                    if (selectedServices.isEmpty()) {
                        viewModel.setError("Selecciona al menos un servicio")
                    } else {
                        viewModel.saveConfiguration(
                            hasPhysicalStore = hasPhysicalStore,
                            is24Hours = is24Hours,
                            hasHomeVisits = hasHomeVisits,
                            hasStoreAppointments = hasStoreAppointments,
                            services = selectedServices
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryOrange
                ),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Guardar Cambios",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SwitchRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    val colors = getPrestadorColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            color = if (enabled) colors.textPrimary else colors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.primaryOrange,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = colors.border
            )
        )
    }
}
