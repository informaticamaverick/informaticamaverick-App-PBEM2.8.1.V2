package com.example.myapplication.uishared.ui.components.profile.parts

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.dominio.modelos.*

/**
 * --- HOJAS DESLIZABLES DEL PERFIL (Ley #10) ---
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaEditorDireccionDominio(
    direccion: DireccionDominio,
    estaDetectandoGps: Boolean = false,
    alDetectarGps: ((DireccionDominio) -> Unit) -> Unit = {},
    onDismiss: () -> Unit,
    onSave: (DireccionDominio) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F0F0F),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null // El formulario tiene su propia cabecera
    ) {
        Column(modifier = Modifier.padding(bottom = 20.dp).verticalScroll(rememberScrollState())) {
            FormularioDireccionDominio(
                direccionInicial = direccion,
                estaDetectandoGps = estaDetectandoGps,
                alDetectarGps = alDetectarGps,
                alCambiarDireccion = { /* Se maneja internamente */ },
                onSave = onSave,
                onCancel = onDismiss
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HojaRegistroEmpresaMav(
    idPropietario: String,
    todasLasCategorias: List<CategoriaDominio>,
    estaDetectandoGps: Boolean = false,
    alDetectarGps: ((DireccionDominio) -> Unit) -> Unit = {},
    onDismiss: () -> Unit,
    onFinalizar: (EmpresaDominio, SucursalDominio, DireccionDominio) -> Unit
) {
    var paso by remember { mutableStateOf(1) }
    
    // --- ESTADO DEL FORMULARIO ---
    var nombreEmpresa by remember { mutableStateOf("") }
    var categoriasSeleccionadas by remember { mutableStateOf(setOf<String>()) }
    var direccionBorrador by remember { mutableStateOf(DireccionDominio()) }
    
    val colorAcento = Color(0xFF3B82F6)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F0F0F),
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 40.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "PASO $paso DE 3", 
                    color = colorAcento, 
                    fontWeight = FontWeight.Black, 
                    fontSize = 10.sp, 
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.width(12.dp))
                LinearProgressIndicator(
                    progress = { paso / 3f }, 
                    modifier = Modifier.weight(1f).height(4.dp), 
                    color = colorAcento, 
                    trackColor = Color.White.copy(alpha = 0.1f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            when (paso) {
                1 -> {
                    Text("IDENTIDAD CORPORATIVA", fontWeight = FontWeight.Black, color = Color.White, fontSize = 20.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("¿Cuál es el nombre de la empresa o base operativa?", color = Color.Gray, fontSize = 14.sp)
                    
                    Spacer(Modifier.height(32.dp))
                    OutlinedTextField(
                        value = nombreEmpresa,
                        onValueChange = { nombreEmpresa = it },
                        label = { Text("Nombre de la Empresa") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Business, null, tint = colorAcento) }
                    )
                }
                
                2 -> {
                    if (todasLasCategorias.isEmpty()) {
                        // 🔥 [ELITE]: Si no hay categorías, este paso se omite automáticamente
                        LaunchedEffect(Unit) { paso++ }
                    } else {
                        Text("RUBROS Y SERVICIOS", fontWeight = FontWeight.Black, color = Color.White, fontSize = 20.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Selecciona las categorías principales de tu empresa.", color = Color.Gray, fontSize = 14.sp)
                        
                        Spacer(Modifier.height(24.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            todasLasCategorias.forEach { cat ->
                                val estaSeleccionada = categoriasSeleccionadas.contains(cat.id)
                                FilterChip(
                                    selected = estaSeleccionada,
                                    onClick = {
                                        categoriasSeleccionadas = if (estaSeleccionada) categoriasSeleccionadas - cat.id else categoriasSeleccionadas + cat.id
                                    },
                                    label = { Text(cat.nombre, fontSize = 11.sp) },
                                    leadingIcon = { Text(cat.icono) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colorAcento.copy(alpha = 0.2f),
                                        selectedLabelColor = colorAcento
                                    )
                                )
                            }
                        }
                    }
                }
                
                3 -> {
                    Text("UBICACIÓN PRINCIPAL", fontWeight = FontWeight.Black, color = Color.White, fontSize = 20.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Configura la dirección de esta base operativa.", color = Color.Gray, fontSize = 14.sp)
                    
                    Spacer(Modifier.height(24.dp))
                    FormularioDireccionDominio(
                        direccionInicial = direccionBorrador,
                        estaDetectandoGps = estaDetectandoGps,
                        alDetectarGps = alDetectarGps,
                        alCambiarDireccion = { direccionBorrador = it },
                        onSave = { 
                            val empresa = EmpresaDominio(
                                id = "emp_${System.currentTimeMillis()}",
                                idPropietario = idPropietario,
                                nombre = nombreEmpresa,
                                idCategorias = categoriasSeleccionadas.toList()
                            )
                            val sucursal = SucursalDominio(
                                id = "suc_${System.currentTimeMillis()}",
                                idEmpresaPadre = empresa.id,
                                idPropietario = idPropietario,
                                nombre = "Sede Principal"
                            )
                            onFinalizar(empresa, sucursal, it)
                        },
                        onCancel = { if (todasLasCategorias.isEmpty()) paso = 1 else paso = 2 }
                    )
                }
            }
            
            if (paso < 3) {
                Spacer(Modifier.height(40.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (paso > 1) {
                        OutlinedButton(
                            onClick = { paso-- },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("ATRÁS", color = Color.Gray) }
                    }
                    
                    Button(
                        onClick = { paso++ },
                        enabled = (paso == 1 && nombreEmpresa.isNotBlank()) || (paso == 2 && (categoriasSeleccionadas.isNotEmpty() || todasLasCategorias.isEmpty())),
                        modifier = Modifier.weight(1.5f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorAcento)
                    ) { Text("SIGUIENTE", fontWeight = FontWeight.ExtraBold) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaRegistroSucursalMav(
    idEmpresaPadre: String,
    idPropietario: String,
    estaDetectandoGps: Boolean = false,
    alDetectarGps: ((DireccionDominio) -> Unit) -> Unit = {},
    onDismiss: () -> Unit,
    onFinalizar: (SucursalDominio, DireccionDominio) -> Unit
) {
    var nombreSucursal by remember { mutableStateOf("") }
    val colorAcento = Color(0xFF3B82F6)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F0F0F),
        modifier = Modifier.fillMaxHeight(0.85f)
    ) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 40.dp).verticalScroll(rememberScrollState())) {
            Text("NUEVA SUCURSAL", fontWeight = FontWeight.Black, color = Color.White, fontSize = 20.sp)
            Spacer(Modifier.height(24.dp))
            
            OutlinedTextField(
                value = nombreSucursal,
                onValueChange = { nombreSucursal = it },
                label = { Text("Nombre de la Sucursal (Ej: Sucursal Centro)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Store, null, tint = colorAcento) }
            )
            
            Spacer(Modifier.height(24.dp))
            
            FormularioDireccionDominio(
                direccionInicial = DireccionDominio(),
                estaDetectandoGps = estaDetectandoGps,
                alDetectarGps = alDetectarGps,
                alCambiarDireccion = { },
                onSave = { 
                    val sucursal = SucursalDominio(
                        id = "suc_${System.currentTimeMillis()}",
                        idEmpresaPadre = idEmpresaPadre,
                        idPropietario = idPropietario,
                        nombre = nombreSucursal.ifBlank { "Nueva Sucursal" }
                    )
                    onFinalizar(sucursal, it)
                },
                onCancel = onDismiss
            )
        }
    }
}

































