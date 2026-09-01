package com.example.myapplication.uishared.ui.components.profile.parts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.utilidades.GeoUtils
import kotlinx.coroutines.launch

/**
 * --- FORMULARIO DE DIRECCIÓN ELITE (SUV v2026) ---
 * [PROPÓSITO]: Captura y normalización forzosa de direcciones con validación GPS.
 */
@Composable
fun FormularioDireccionDominio(
    direccionInicial: DireccionDominio,
    estaDetectandoGps: Boolean = false,
    alDetectarGps: ((DireccionDominio) -> Unit) -> Unit = {},
    alCambiarDireccion: (DireccionDominio) -> Unit,
    onSave: (DireccionDominio) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var borrador by remember(direccionInicial) { mutableStateOf(direccionInicial) }
    var estaCalculado by remember { mutableStateOf(direccionInicial.latitud != 0.0) }
    var estaProcesando by remember { mutableStateOf(false) }
    var mostrarRescate by remember { mutableStateOf(false) }
    
    val colorAcento = Color(0xFF3B82F6)
    val colorExito = Color(0xFF4ADE80)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- CABECERA ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Mi Ubicación",
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontSize = 20.sp
            )
            
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, null, tint = Color.Gray)
            }
        }

        // --- SECCIÓN GPS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { 
                    alDetectarGps { nuevaDir ->
                        val geo = GeoUtils.calcularGeohash(nuevaDir.latitud, nuevaDir.longitud)
                        borrador = nuevaDir.copy(estaVerificadaGps = true, geohash = geo)
                        estaCalculado = true
                        alCambiarDireccion(borrador)
                    }
                },
                enabled = !estaDetectandoGps,
                colors = ButtonDefaults.buttonColors(containerColor = colorAcento.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (estaDetectandoGps) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = colorAcento)
                } else {
                    Icon(Icons.Default.MyLocation, null, tint = colorAcento, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Usar Gps", color = colorAcento, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }
            
            Text(
                "¿Tienes problemas con la Ubicación?",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { mostrarRescate = true }
            )
        }
        
        if (borrador.estaVerificadaGps) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
                Icon(Icons.Default.GpsFixed, null, tint = colorExito, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("Dirección verificada por GPS", color = colorExito, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        // --- CAMPOS DE DATOS ---
        
        OutlinedTextField(
            value = borrador.etiqueta,
            onValueChange = { 
                borrador = borrador.copy(etiqueta = it)
                estaCalculado = false // Resetear validación ante cambio manual
                alCambiarDireccion(borrador) 
            },
            label = { Text("Etiqueta (Ej: Mi Casa, Oficina)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Label, null, tint = colorAcento) }
        )

        OutlinedTextField(
            value = borrador.calle,
            onValueChange = { 
                borrador = borrador.copy(calle = it)
                estaCalculado = false 
                alCambiarDireccion(borrador) 
            },
            label = { Text("Calle / Avenida") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.AddLocation, null, tint = colorAcento) }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = borrador.numero,
                onValueChange = { 
                    borrador = borrador.copy(numero = it)
                    estaCalculado = false
                    alCambiarDireccion(borrador) 
                },
                label = { Text("Altura") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = borrador.codigoPostal,
                onValueChange = { 
                    borrador = borrador.copy(codigoPostal = it)
                    estaCalculado = false
                    alCambiarDireccion(borrador) 
                },
                label = { Text("C.P.") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = borrador.piso,
                onValueChange = { borrador = borrador.copy(piso = it); alCambiarDireccion(borrador) },
                label = { Text("Piso") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = borrador.departamento,
                onValueChange = { borrador = borrador.copy(departamento = it); alCambiarDireccion(borrador) },
                label = { Text("Depto") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        OutlinedTextField(
            value = borrador.localidad,
            onValueChange = { 
                borrador = borrador.copy(localidad = it)
                estaCalculado = false
                alCambiarDireccion(borrador) 
            },
            label = { Text("Localidad / Ciudad") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.LocationCity, null, tint = colorAcento) }
        )

        OutlinedTextField(
            value = borrador.provincia,
            onValueChange = { 
                borrador = borrador.copy(provincia = it)
                estaCalculado = false
                alCambiarDireccion(borrador) 
            },
            label = { Text("Provincia") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Public, null, tint = colorAcento) }
        )
        
        OutlinedTextField(
            value = borrador.pais,
            onValueChange = { 
                borrador = borrador.copy(pais = it)
                estaCalculado = false
                alCambiarDireccion(borrador) 
            },
            label = { Text("País") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Language, null, tint = colorAcento) }
        )

        // --- DATOS TÉCNICOS (Solo Lectura) ---
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = borrador.latitud.toString(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Latitud", fontSize = 10.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Color.Gray)
            )
            OutlinedTextField(
                value = borrador.longitud.toString(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Longitud", fontSize = 10.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Color.Gray)
            )
            OutlinedTextField(
                value = borrador.geohash,
                onValueChange = {},
                readOnly = true,
                label = { Text("Código Geo", fontSize = 10.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = colorAcento)
            )
        }

        Spacer(Modifier.height(12.dp))

        // --- BOTONES DE ACCIÓN ---
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    estaProcesando = true
                    scope.launch {
                        val addressText = borrador.aTextoCompleto()
                        val result = GeoUtils.obtenerDireccionDesdeTexto(context, addressText)
                        if (result != null) {
                            borrador = result.copy(
                                id = borrador.id,
                                etiqueta = borrador.etiqueta,
                                idPropietario = borrador.idPropietario,
                                estaVerificadaGps = false // Normalizada por texto, no sensor
                            )
                            estaCalculado = true
                            alCambiarDireccion(borrador)
                        } else {
                            estaCalculado = false
                        }
                        estaProcesando = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                enabled = !estaProcesando && borrador.calle.isNotBlank() && borrador.localidad.isNotBlank()
            ) {
                if (estaProcesando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Icon(Icons.Default.Calculate, null, tint = colorAcento)
                    Spacer(Modifier.width(12.dp))
                    Text("CALCULAR DIRECCIÓN", color = colorAcento, fontWeight = FontWeight.ExtraBold)
                }
            }

            Button(
                onClick = { onSave(borrador) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAcento),
                enabled = estaCalculado && !estaProcesando
            ) {
                Text("GUARDAR CAMBIOS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        }
        
        if (!estaCalculado && !estaProcesando && borrador.calle.isNotBlank()) {
            Text(
                "Debes calcular la dirección para validar la ubicación antes de guardar.",
                color = Color.Red.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }

    if (mostrarRescate) {
        DialogoRescateUbicacionMav(
            onDismiss = { mostrarRescate = false },
            onResolved = { nuevaDir ->
                borrador = nuevaDir
                estaCalculado = true
                alCambiarDireccion(borrador)
                mostrarRescate = false
            }
        )
    }
}

/**
 * --- DIÁLOGO DE RESCATE DE UBICACIÓN (ELITE) ---
 * Permite resolver una dirección mediante links de Google Maps o coordenadas.
 */
@Composable
fun DialogoRescateUbicacionMav(
    onDismiss: () -> Unit,
    onResolved: (DireccionDominio) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var estaProcesando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val colorAcento = Color(0xFF3B82F6)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rescate de Ubicación", fontWeight = FontWeight.Black, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Pega el enlace de Google Maps o las coordenadas (lat, lng) para identificar tu ubicación exacta.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it; error = null },
                    placeholder = { Text("Enlace o coordenadas...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedContainerColor = Color.White.copy(alpha = 0.1f)
                    )
                )
                
                if (error != null) {
                    Text(error!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                
                if (estaProcesando) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = colorAcento)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (input.isBlank()) return@Button
                    estaProcesando = true
                    scope.launch {
                        val result = GeoUtils.resolverEntradaUbicacion(context, input)
                        if (result != null) {
                            onResolved(result)
                        } else {
                            error = "No se pudo identificar la ubicación. Verifica el enlace o las coordenadas."
                        }
                        estaProcesando = false
                    }
                },
                enabled = !estaProcesando && input.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = colorAcento)
            ) {
                Text("RESOLVER Y VINCULAR", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1A1A24),
        shape = RoundedCornerShape(24.dp)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun FormularioDireccionDominioPreview() {
    FormularioDireccionDominio(
        direccionInicial = DireccionDominio(
            calle = "Av. Aconquija",
            numero = "2000",
            localidad = "Yerba Buena",
            provincia = "Tucumán"
        ),
        onSave = {},
        onCancel = {},
        alCambiarDireccion = {}
    )
}

































