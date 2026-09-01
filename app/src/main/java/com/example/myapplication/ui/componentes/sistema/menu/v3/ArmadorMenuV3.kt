package com.example.myapplication.ui.componentes.sistema.menu.v3

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.ui.components.profile.PerfilIdentidadV3
import com.example.myapplication.ui.componentes.DropdownItemData
import com.example.myapplication.uishared.ui.components.TextCompacto
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.myapplication.uishared.estilos.AppTypography
import com.example.myapplication.uishared.ui.components.TextCompactoAutoFit
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * --- 🏗️ ARMADOR DE CONTENIDO DE MENÚ V3 (v2026.ELITE) ---
 * [PROPÓSITO]: Centralizar la lógica de qué piezas van dentro de cada tipo de menú.
 * [FUNCIONAMIENTO]: Provee funciones de conveniencia para armar perfiles, filtros y rubros.
 * [LEY #12]: Soberanía de Contenido. Desacopla el "Qué" del "Cómo".
 */

@Composable
fun MenuPerfilContenido(
    identidades: List<PerfilIdentidadV3>,
    idPerfilActivo: String,
    alSeleccionar: (PerfilIdentidadV3) -> Unit
) {
    MenuSectionHeaderV3(text = "CAMBIAR PERFIL")
    identidades.forEach { p ->
        MenuItemEliteV3(
            label = p.nombre,
            leadingImage = p.photoUrl,
            emoji = p.emoji,
            isSelected = idPerfilActivo == p.id,
            onClick = { alSeleccionar(p) }
        )
    }
}

@Composable
fun MenuFiltrosContenido(
    items: List<DropdownItemData>,
    idsSeleccionados: Set<String>,
    alAlternar: (String) -> Unit
) {
    if (items.isEmpty()) {
        TextCompacto(
            text = "No hay filtros activos", 
            color = Color.Gray, 
            fontSize = 10.sp, 
            modifier = Modifier.padding(12.dp)
        )
    } else {
        items.forEach { item ->
            MenuItemEliteV3(
                label = item.label,
                emoji = item.emoji ?: "🔹",
                showCheckbox = true,
                isSelected = idsSeleccionados.contains(item.id),
                onClick = { alAlternar(item.id) }
            )
        }
    }
}

@Composable
fun MenuOrdenContenido(
    items: List<DropdownItemData>,
    idsSeleccionados: Set<String>,
    alAlternar: (String) -> Unit
) {
    if (items.isEmpty()) {
        TextCompacto(
            text = "Sin opciones", 
            color = Color.Gray, 
            fontSize = 10.sp, 
            modifier = Modifier.padding(12.dp)
        )
    } else {
        items.forEach { item ->
            MenuItemEliteV3(
                label = item.label,
                emoji = item.emoji ?: "🔃",
                showCheckbox = true,
                isSelected = idsSeleccionados.contains(item.id),
                onClick = { alAlternar(item.id) }
            )
        }
    }
}

@Composable
fun MenuRubrosContenido(
    items: List<DropdownItemData>,
    idsSeleccionados: Set<String>,
    alAlternar: (String) -> Unit
) {
    if (items.isEmpty()) {
        TextCompacto(
            text = "No hay rubros activos", 
            color = Color.Gray, 
            fontSize = 10.sp, 
            modifier = Modifier.padding(12.dp)
        )
    } else {
        items.forEach { cat ->
            MenuItemEliteV3(
                label = cat.label,
                emoji = cat.emoji ?: "📋",
                showCheckbox = true,
                isSelected = idsSeleccionados.contains(cat.id),
                onClick = { alAlternar(cat.id) }
            )
        }
    }
}

/**
 * --- SECTOR: UBICACIÓN TÁCTICA (EL SELECTOR DE NODOS) ---
 */
@Composable
fun MenuUbicacionContenido(
    direccionActiva: DireccionDominio?,
    direccionGpsActual: DireccionDominio? = null,
    estaGpsActivo: Boolean,
    isCargando: Boolean = false,
    direccionesDisponibles: List<DireccionDominio>,
    alAlternarGps: () -> Unit,
    alSeleccionarDireccion: (DireccionDominio) -> Unit
) {
    // --- GRUPO 1: SENSOR GPS (HARDWARE + DETALLES) ---
    MenuGrupoV3 {
        Column {
            // Fila superior: Switch y Titular
            Surface(
                onClick = alAlternarGps,
                color = if (estaGpsActivo) SharedPalette.ElectricCyan.copy(alpha = 0.08f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "🛰️", fontSize = 18.sp)
                        Column {
                            TextCompacto(
                                text = "GPS EN TIEMPO REAL",
                                color = if (estaGpsActivo) SharedPalette.ElectricCyan else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            TextCompacto(
                                text = if (estaGpsActivo) "Sensor activo y rastreando" else "Activar ubicación por hardware",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Switch(
                        checked = estaGpsActivo,
                        onCheckedChange = { alAlternarGps() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = SharedPalette.ElectricCyan,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.scale(0.7f)
                    )
                }
            }

            // Sección inferior (dentro de la misma tarjeta): Detalles GPS
            if (estaGpsActivo) {
                Column(
                    modifier = Modifier
                        .padding(top = 4.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SharedPalette.ElectricCyan))
                            Spacer(Modifier.width(8.dp))
                            TextCompacto(
                                text = "COORDENADAS DETECTADAS",
                                color = SharedPalette.ElectricCyan,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        
                        if (isCargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = SharedPalette.ElectricCyan,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    if (isCargando && direccionGpsActual == null) {
                        TextCompacto(
                            text = "CALIBRANDO HARDWARE...",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        val calle = direccionGpsActual?.calleYNumero?.uppercase()?.takeIf { it.isNotBlank() } ?: "CALLE NO IDENTIFICADA"
                        val zona = if (direccionGpsActual != null && (direccionGpsActual.localidad.isNotBlank() || direccionGpsActual.codigoPostal.isNotBlank())) {
                            "${direccionGpsActual.localidad} (${direccionGpsActual.codigoPostal})".uppercase()
                        } else "SIN DATA DE CIUDAD"

                        TextCompactoAutoFit(
                            text = calle,
                            maxFontSize = 12.sp,
                            minFontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        TextCompacto(
                            text = zona,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }

    // --- GRUPO 2: DIRECCIONES DEL PERFIL (NODOS) ---
    MenuGrupoV3 {
        MenuSectionHeaderV3(text = "NODOS DE ESTE PERFIL")
        
        if (direccionesDisponibles.isEmpty()) {
            TextoMenuInformativoV3(
                text = "No hay direcciones configuradas",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        } else {
            direccionesDisponibles.forEach { addr ->
                val isSelected = !estaGpsActivo && direccionActiva?.id == addr.id
                
                Surface(
                    onClick = { alSeleccionarDireccion(addr) },
                    color = if (isSelected) SharedPalette.ElectricCyan.copy(alpha = 0.08f) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = when {
                                addr.etiqueta.contains("casa", ignoreCase = true) -> "🏠"
                                addr.etiqueta.contains("oficina", ignoreCase = true) -> "🏢"
                                addr.esEmpresa -> "💼"
                                else -> "📍"
                            },
                            fontSize = 16.sp
                        )
                        
                        Column(modifier = Modifier.weight(1f)) {
                            val etiqueta = if (addr.esEmpresa) addr.nombreSucursal ?: "SUCURSAL" else addr.etiqueta.ifBlank { "MI DIRECCIÓN" }
                            TextCompacto(
                                text = etiqueta.uppercase(),
                                color = if (isSelected) SharedPalette.ElectricCyan else SharedPalette.ElectricCyan.copy(alpha = 0.6f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                            TextCompactoAutoFit(
                                text = addr.calleYNumero.uppercase(),
                                maxFontSize = 11.sp,
                                minFontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            TextCompacto(
                                text = "${addr.localidad} (${addr.codigoPostal})".uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SharedPalette.ElectricCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * --- SECTOR: ATMÓSFERA TÁCTICA (EL DETALLE DEL CLIMA) ---
 */
@Composable
fun MenuClimaContenido(
    temperatura: String,
    emoji: String,
    descripcion: String,
    nombreCiudad: String,
    mensajeContexto: String
) {
    // --- GRUPO 1: ATMÓSFERA PRINCIPAL ---
    MenuGrupoV3 {
        MenuSectionHeaderV3(text = "SYS_WTHR // ATMÓSFERA")
        
        Spacer(Modifier.height(4.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextCompactoAutoFit(
                text = nombreCiudad.uppercase(),
                maxFontSize = 16.sp,
                minFontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                style = AppTypography.HeaderTitle.copy(letterSpacing = 1.sp)
            )
            TextCompacto(
                text = descripcion.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = SharedPalette.ElectricCyan.copy(alpha = 0.8f),
                style = AppTypography.HeaderSubtitle.copy(letterSpacing = 1.sp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 42.sp)
            Spacer(Modifier.width(12.dp))
            TextCompacto(
                text = temperatura,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                style = AppTypography.HeaderTitle.copy(letterSpacing = (-1).sp)
            )
        }
    }

    // --- GRUPO 2: CONTEXTO TÁCTICO ---
    MenuGrupoV3 {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = mensajeContexto,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 15.sp
            )
        }
    }

    // --- GRUPO 3: ESTADO DEL FEED ---
    MenuGrupoV3 {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SharedPalette.ElectricCyan))
            Spacer(Modifier.width(8.dp))
            TextoMenuInformativoV3(text = "REAL_TIME_FEED // ONLINE")
        }
    }
}

// ==========================================================================================
// --- SECCIÓN: ORQUESTADORES DE ALTO NIVEL (PRESETS ELITE) ---
// ==========================================================================================

/**
 * Orquestador completo para el Menú de Ubicación.
 */
@Composable
fun MenuUbicacionV3(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    direccionActiva: DireccionDominio?,
    direccionGpsActual: DireccionDominio? = null,
    estaGpsActivo: Boolean,
    isCargando: Boolean = false,
    direccionesDisponibles: List<DireccionDominio>,
    alAlternarGps: () -> Unit,
    alSeleccionarDireccion: (DireccionDominio) -> Unit,
    arrowOffset: androidx.compose.ui.unit.Dp = 70.dp, 
    alignment: Alignment = Alignment.TopCenter, 
    verticalOffset: androidx.compose.ui.unit.Dp = 50.dp, 
    horizontalOffset: androidx.compose.ui.unit.Dp = 40.dp,
    isCenteredOnScreen: Boolean = false // 🔥 [v2026.ELITE]: Nuevo parámetro para control dinámico
) {
    MoldeMenuArmadorV3(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        alignment = alignment,
        arrowOffset = arrowOffset,
        verticalOffset = verticalOffset,
        horizontalOffset = horizontalOffset,
        anchoMaximo = 340.dp,
        isCenteredOnScreen = isCenteredOnScreen // 🔥 [v2026.ELITE]
    ) {
        MenuUbicacionContenido(
            direccionActiva = direccionActiva,
            direccionGpsActual = direccionGpsActual,
            estaGpsActivo = estaGpsActivo,
            isCargando = isCargando,
            direccionesDisponibles = direccionesDisponibles,
            alAlternarGps = alAlternarGps,
            alSeleccionarDireccion = alSeleccionarDireccion
        )
    }
}

/**
 * Orquestador completo para el Menú de Clima.
 */
@Composable
fun MenuClimaV3(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    temperatura: String,
    emoji: String,
    descripcion: String,
    nombreCiudad: String,
    mensajeContexto: String,
    modifier: Modifier = Modifier,
    verticalOffset: androidx.compose.ui.unit.Dp = 50.dp, // 🔥 [FIX]: Sincronizado con Ubicación
    horizontalOffset: androidx.compose.ui.unit.Dp = (0).dp, // 🔥 [FIX]: Cuerpo hacia adentro para evitar recortes
    arrowOffset: androidx.compose.ui.unit.Dp = 270.dp // 🔥 [FIX]: Al icono del clima
) {
    MoldeMenuArmadorV3(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        alignment = Alignment.TopEnd, // 🔥 [FIX]: Anclaje superior derecho
        verticalOffset = verticalOffset,
        horizontalOffset = horizontalOffset,
        arrowOffset = arrowOffset,
        anchoMaximo = 340.dp
    ) {
        MenuClimaContenido(
            temperatura = temperatura,
            emoji = emoji,
            descripcion = descripcion,
            nombreCiudad = nombreCiudad,
            mensajeContexto = mensajeContexto
        )
    }
}
