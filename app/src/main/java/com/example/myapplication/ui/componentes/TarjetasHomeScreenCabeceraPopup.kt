/*
package com.example.myapplication.ui.componentes

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.example.myapplication.core.dominio.modelos.CuentaMaestroUsuario
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.dominio.modelos.UsuarioDominioCompleto
import com.example.myapplication.core.datos.local.entidades.CuentaEntity
import com.example.myapplication.core.datos.local.entidades.IdentidadUsuarioEntity
import com.example.myapplication.utilidades.QRUtils
//import com.example.myapplication.viewmodel.home.Screen
import com.example.myapplication.ui.estilos.ClienteTheme
//import com.example.myapplication.ui.componentes.sistema.CPCyberColors
import com.example.myapplication.ui.componentes.sistema.DepthDividerHorizontal
import com.example.myapplication.ui.componentes.sistema.FileNode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import com.example.myapplication.ui.pantallas.home.Screen
import com.example.myapplication.uishared.estilos.CPCyberColors

// ==================================================================================
// --- SECCIÓN: COMPONENTES DE DIÁLOGOS Y POPUPS (ELITE MODERN) ---
// ==================================================================================

@Composable
fun appCloseButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color.Red.copy(alpha = 0.1f))
            .border(1.dp, Color.Red.copy(alpha = 0.3f), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 12.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Cerrar",
            tint = Color.Red,
            modifier = Modifier.size(14.dp)
        )
    }
}

/**
 * appQRDisplay: Pantalla completa estilo WhatsApp para mostrar el QR del usuario.
 */
@Composable
fun appQRDisplay(
    identidad: CuentaMaestroUsuario,
    nombrePerfilActivo: String,
    fotoPerfilActivo: Any?,
    alCerrar: () -> Unit
) {
    val cyberCyan = CPCyberColors.appCyan

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = alCerrar) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Cerrar", tint = Color.Black)
                }
                Text(
                    "MI IDENTIDAD app",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    color = Color.Black
                )
                Spacer(Modifier.size(48.dp))
            }

            Spacer(Modifier.weight(0.3f))

            Card(
                modifier = Modifier
                    .size(320.dp)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val qrContent = QRUtils.generateUniqueCode(identidad.cuenta.id, isProvider = false)
                    Icon(
                        Icons.Default.QrCode2,
                        null,
                        tint = Color.Black,
                        modifier = Modifier.fillMaxSize().padding(24.dp)
                    )
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(2.dp, cyberCyan)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("M", fontWeight = FontWeight.Black, color = cyberCyan, fontSize = 24.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            AsyncImage(
                model = fotoPerfilActivo ?: identidad.usuario.perfil.urlFotoPerfil,
                contentDescription = null,
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .border(2.dp, cyberCyan, CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = nombrePerfilActivo.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )

            Text(
                text = identidad.cuenta.correoGoogle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.5f)
            )

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(cyberCyan.copy(alpha = 0.1f))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    "ESCANEA PARA AGREGAR CONTACTO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = cyberCyan,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.weight(1f))
        }
    }
}

/**
 * UserProfilePopup: Ventana emergente compacta con detalles del perfil.
 */
@Composable
fun UserProfilePopup(
    identidad: CuentaMaestroUsuario, 
    esPerfilPersonal: Boolean = true,
    idPerfilSeleccionado: String? = null,
    alSeleccionarPerfil: (String?) -> Unit = {},
    esDesplazable: Boolean = true,
    alCerrar: () -> Unit, 
    alCerrarSesion: () -> Unit, 
    alHacerClickPerfil: () -> Unit
) {
    val cyberCyan = CPCyberColors.appCyan
    val deepGlass = Color(0xFF0B0F19).copy(alpha = 0.98f)

    var mostrarMiQR by remember { mutableStateOf(false) }

    if (mostrarMiQR) {
        Dialog(
            onDismissRequest = { mostrarMiQR = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val nombreActivo = if (esPerfilPersonal) identidad.usuario.perfil.nombreVisible else identidad.empresas.find { it.empresa.id == idPerfilSeleccionado }?.empresa?.nombre ?: identidad.usuario.perfil.nombreVisible
            val fotoActiva = if (esPerfilPersonal) identidad.usuario.perfil.miniaturaBase64 ?: identidad.usuario.perfil.urlFotoPerfil else identidad.empresas.find { it.empresa.id == idPerfilSeleccionado }?.empresa?.miniaturaBase64 ?: identidad.usuario.perfil.urlFotoPerfil

            appQRDisplay(
                identidad = identidad,
                nombrePerfilActivo = nombreActivo,
                fotoPerfilActivo = fotoActiva,
                alCerrar = { mostrarMiQR = false }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CutCornerShape(topEnd = 24.dp, bottomStart = 24.dp))
            .background(deepGlass)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(cyberCyan.copy(alpha = 0.6f), Color.Transparent, cyberCyan.copy(alpha = 0.3f))),
                shape = CutCornerShape(topEnd = 24.dp, bottomStart = 24.dp)
            )
            .then(if (esDesplazable) Modifier.heightIn(max = 600.dp).verticalScroll(rememberScrollState()) else Modifier)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SYS_ID // PROFILE",
                color = cyberCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            appCloseButton(onClick = alCerrar)
        }
        
        Spacer(Modifier.height(16.dp))

        val empresaActiva = remember(identidad, idPerfilSeleccionado) { identidad.empresas.find { it.empresa.id == idPerfilSeleccionado } }
        val nombreActivo = remember(identidad, esPerfilPersonal, empresaActiva) { if (esPerfilPersonal) identidad.usuario.perfil.nombreVisible else empresaActiva?.empresa?.nombre ?: identidad.usuario.perfil.nombreVisible }
        val emailActivo = remember(identidad, esPerfilPersonal, empresaActiva) { if (esPerfilPersonal) identidad.cuenta.correoGoogle else empresaActiva?.empresa?.correoContacto ?: identidad.cuenta.correoGoogle }
        val fotoActiva = remember(identidad, esPerfilPersonal, empresaActiva) {
            val source = if (esPerfilPersonal) identidad.usuario.perfil.miniaturaBase64 ?: identidad.usuario.perfil.urlFotoPerfil
            else empresaActiva?.empresa?.miniaturaBase64 ?: empresaActiva?.empresa?.urlFoto ?: identidad.usuario.perfil.urlFotoPerfil
            com.example.myapplication.core.utilidades.ImageUtils.processImageSource(source)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .clickable { alHacerClickPerfil() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
                AsyncImage(
                    model = fotoActiva,
                    contentDescription = null,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, cyberCyan.copy(alpha = 0.4f), CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = nombreActivo.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        letterSpacing = (-0.2).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (esPerfilPersonal && identidad.cuenta.estaSuscrito) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.Verified, null, tint = cyberCyan, modifier = Modifier.size(12.dp))
                    }
                }
                Text(
                    text = emailActivo,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(18.dp))
        }

        Spacer(Modifier.height(16.dp))
        DepthDividerHorizontal(shadowColor = Color.Black.copy(0.4f), highlightColor = Color.White.copy(0.05f))
        Spacer(Modifier.height(16.dp))

        val perfilesInactivos = remember(identidad, idPerfilSeleccionado, esPerfilPersonal) {
            val list = mutableListOf<ProfileBubbleData>()
            if (!esPerfilPersonal) {
                val photo = com.example.myapplication.core.utilidades.ImageUtils.processImageSource(identidad.usuario.perfil.miniaturaBase64?.takeIf { it.isNotBlank() } ?: identidad.usuario.perfil.urlFotoPerfil)
                list.add(ProfileBubbleData(null, identidad.usuario.perfil.nombreVisible, photo, false))
            }
            identidad.empresas.forEach { company ->
                if (esPerfilPersonal || company.empresa.id != idPerfilSeleccionado) {
                    val photo = com.example.myapplication.core.utilidades.ImageUtils.processImageSource(company.empresa.miniaturaBase64?.takeIf { it.isNotBlank() } ?: company.empresa.urlFoto ?: identidad.usuario.perfil.urlFotoPerfil)
                    list.add(ProfileBubbleData(company.empresa.id, company.empresa.nombre, photo, false))
                }
            }
            list
        }

        if (perfilesInactivos.isNotEmpty()) {
            Text(
                "🏢 NETWORK // CAMBIO RÁPIDO",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(12.dp))

            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(perfilesInactivos.size) { index ->
                    val profile = perfilesInactivos[index]
                    ProfileBubbleSwitchCompact(
                        data = profile,
                        onClick = { alSeleccionarPerfil(profile.id) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            DepthDividerHorizontal(shadowColor = Color.Black.copy(0.4f), highlightColor = Color.White.copy(0.05f))
            Spacer(Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SharingActionBottonCompact(icon = Icons.Default.QrCodeScanner, label = "ESCANEAR") { /* TODO */ }
            SharingActionBottonCompact(icon = Icons.Default.QrCode2, label = "MI QR") { mostrarMiQR = true }
            SharingActionBottonCompact(icon = Icons.Default.Share, label = "COMPARTIR") { /* TODO */ }
        }

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFFF1744).copy(0.08f))
                .border(1.dp, Color(0xFFFF1744).copy(0.2f), RoundedCornerShape(8.dp))
                .clickable { alCerrarSesion() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PowerSettingsNew, null, tint = Color(0xFFFF1744), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text("TERMINAR SESIÓN", color = Color(0xFFFF1744), fontWeight = FontWeight.ExtraBold, fontSize = 9.sp, letterSpacing = 0.5.sp)
            }
        }
    }
}

/**
 * LocationPopup: Ventana emergente compacta para selección de ubicación.
 */
@Composable
fun LocationPopup(
    informacionDireccionesDisponibles: List<DireccionDominio>,
    alCerrar: () -> Unit,
    alActualizar: () -> Unit,
    alSeleccionarUbicacion: (DireccionDominio) -> Unit,
    alAlternarGps: () -> Unit,
    direccionActiva: DireccionDominio?,
    idPerfilSeleccionado: String? = null,
    estaGpsActivado: Boolean = true 
) {
    val cyberCyan = CPCyberColors.appCyan
    val deepGlass = Color(0xFF0B0F19).copy(alpha = 0.98f)
    var estaActualizando by remember { mutableStateOf(false) }

    val rotationAnim by animateFloatAsState(
        targetValue = if (estaActualizando) 360f else 0f,
        animationSpec = if (estaActualizando) {
            infiniteRepeatable(animation = tween(1000, easing = LinearOutSlowInEasing))
        } else tween(0),
        label = "GpsRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CutCornerShape(topEnd = 24.dp, bottomStart = 24.dp))
            .background(deepGlass)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(cyberCyan.copy(alpha = 0.6f), Color.Transparent, cyberCyan.copy(alpha = 0.3f))),
                shape = CutCornerShape(topEnd = 24.dp, bottomStart = 24.dp)
            )
            .heightIn(max = 600.dp)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GEO_HUD // LOCATOR",
                color = cyberCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            appCloseButton(onClick = alCerrar)
        }

        Spacer(Modifier.height(16.dp))

        val isGpsActive = direccionActiva?.id == "gps_current"

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (isGpsActive) cyberCyan.copy(0.05f) else Color.White.copy(alpha = 0.03f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, (if (isGpsActive) cyberCyan else Color.White).copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isGpsActive) cyberCyan.copy(0.1f) else Color.White.copy(0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isGpsActive) Icons.Default.SatelliteAlt else if (direccionActiva?.esEmpresa == true) Icons.Default.Business else Icons.Default.Home,
                        contentDescription = null,
                        tint = if (isGpsActive) cyberCyan else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = (direccionActiva?.calleYNumero ?: "SCANNING...").uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = (-0.2).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = (direccionActiva?.localidad ?: "BUSCANDO NODO...").uppercase(),
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(0.02f))
                .clickable { alAlternarGps() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                null,
                tint = if (isGpsActive) cyberCyan else Color.White.copy(0.3f),
                modifier = Modifier.size(16.dp).rotate(rotationAnim)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = if (isGpsActive) "RASTREADOR GPS ACTIVO" else "ACTIVAR RASTREADOR GPS",
                color = if (isGpsActive) cyberCyan else Color.White.copy(0.5f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(Modifier.height(16.dp))
        DepthDividerHorizontal(shadowColor = Color.Black.copy(0.4f), highlightColor = Color.White.copy(0.05f))
        Spacer(Modifier.height(16.dp))

        Text(
            "📂 NODOS DISPONIBLES",
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            val nodosUbicacion = remember(informacionDireccionesDisponibles, idPerfilSeleccionado) {
                val personal = informacionDireccionesDisponibles.filter { !it.esEmpresa && it.id != "gps_current" }
                val empresas = informacionDireccionesDisponibles.filter { it.esEmpresa }
                val nodes = mutableListOf<FileNode>()

                if (personal.isNotEmpty()) {
                    nodes.add(FileNode(
                        name = "Mis Direcciones",
                        isDirectory = true,
                        children = personal.map { addr ->
                            FileNode(name = "${addr.calleYNumero} (${addr.localidad})", isDirectory = false, icon = Icons.Default.Home)
                        }
                    ))
                }
                if (empresas.isNotEmpty()) {
                    nodes.add(FileNode(
                        name = "Empresas",
                        isDirectory = true,
                        children = empresas.groupBy { it.nombreSucursal }.map { (companyName, branches) ->
                            FileNode(
                                name = companyName ?: "EMPRESA",
                                isDirectory = true,
                                children = branches.map { branch ->
                                    FileNode(name = "${branch.etiqueta}: ${branch.calleYNumero}", isDirectory = false, icon = Icons.Default.Business)
                                }
                            )
                        }
                    ))
                }
                nodes
            }

            DirectoryTreeCompact(
                nodes = nodosUbicacion,
                onNodeClick = { node ->
                    if (node.isDirectory == false) {
                        val seleccionada = informacionDireccionesDisponibles.find { addr ->
                            val personalName = "${addr.calleYNumero} (${addr.localidad})"
                            val companyName = "${addr.etiqueta}: ${addr.calleYNumero}"
                            node.name == personalName || node.name == companyName
                        }
                        seleccionada?.let { alSeleccionarUbicacion(it) }
                    }
                }
            )
        }
    }
}

@Composable
fun DirectoryTreeCompact(
    nodes: List<FileNode>,
    onNodeClick: (FileNode) -> Unit,
    depth: Int = 0
) {
    Column(modifier = Modifier.padding(start = (depth * 8).dp)) {
        nodes.forEach { node ->
            var isExpanded by remember { mutableStateOf(depth == 0) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        if (node.isDirectory) isExpanded = !isExpanded
                        else onNodeClick(node)
                    }
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (node.isDirectory) {
                        if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.ChevronRight
                    } else node.icon ?: Icons.Default.Description,
                    contentDescription = null,
                    tint = if (node.isDirectory) CPCyberColors.appCyan.copy(0.6f) else Color.White.copy(0.3f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = node.name.uppercase(),
                    color = if (node.isDirectory) Color.White.copy(0.8f) else Color.White.copy(0.5f),
                    fontSize = if (node.isDirectory) 10.sp else 9.sp,
                    fontWeight = if (node.isDirectory) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (node.isDirectory && isExpanded) {
                DirectoryTreeCompact(nodes = node.children, onNodeClick = onNodeClick, depth = depth + 1)
            }
        }
    }
}

// ==================================================================================
// --- COMPONENTES AUXILIARES ---
// ==================================================================================

@Composable
private fun ProfileBubbleSwitchCompact(
    data: ProfileBubbleData,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (data.photo != null) {
                AsyncImage(
                    model = data.photo,
                    contentDescription = data.name,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = if (data.id == null) Icons.Default.Person else Icons.Default.Business,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = data.name.split(" ").firstOrNull()?.uppercase() ?: "",
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.4f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(46.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SharingActionBottonCompact(icon: ImageVector, label: String, color: Color = Color.White.copy(0.6f), onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color.copy(0.05f), CircleShape)
                .border(1.dp, color.copy(0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = Color.White.copy(0.4f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
    }
}

private data class ProfileBubbleData(
    val id: String?,
    val name: String,
    val photo: Any?,
    val isActive: Boolean
)

// ==================================================================================
// --- DIÁLOGOS WRAPPERS ---
// ==================================================================================

@Composable
fun ProfileDialog(
    show: Boolean,
    identidad: CuentaMaestroUsuario,
    esPerfilPersonal: Boolean = true,
    idPerfilSeleccionado: String? = null,
    alSeleccionarPerfil: (String?) -> Unit = {},
    navController: NavHostController,
    alCerrarSesion: () -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        Popup(
            onDismissRequest = onDismiss,
            properties = PopupProperties(focusable = true, dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            var animateIn by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            LaunchedEffect(Unit) { animateIn = true }

            fun closeWithAnimation() {
                animateIn = false
                scope.launch { delay(300); onDismiss() }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.statusBarsPadding().height(110.dp))
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.5f)).clickable(remember { MutableInteractionSource() }, null) { closeWithAnimation() })
                }

                Box(modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 110.dp, start = 16.dp, end = 16.dp).fillMaxWidth()) {
                    AnimatedVisibility(
                        visible = animateIn,
                        enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                        exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
                    ) {
                        UserProfilePopup(
                            identidad = identidad,
                            esPerfilPersonal = esPerfilPersonal,
                            idPerfilSeleccionado = idPerfilSeleccionado,
                            alSeleccionarPerfil = { alSeleccionarPerfil(it); closeWithAnimation() },
                            alCerrar = { closeWithAnimation() },
                            alCerrarSesion = { closeWithAnimation(); alCerrarSesion() },
                            alHacerClickPerfil = { closeWithAnimation(); navController.navigate(
                                Screen.PerfilCliente.route) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LocationDialog(
    show: Boolean,
    informacionDireccionesDisponibles: List<DireccionDominio>,
    direccionActiva: DireccionDominio?,
    idPerfilSeleccionado: String? = null,
    estaGpsActivado: Boolean = true,
    alActualizar: () -> Unit,
    alAlternarGps: () -> Unit,
    alSeleccionarUbicacion: (DireccionDominio) -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        Popup(
            onDismissRequest = onDismiss,
            properties = PopupProperties(focusable = true, dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            var animateIn by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            LaunchedEffect(Unit) { animateIn = true }

            fun closeWithAnimation() {
                animateIn = false
                scope.launch { delay(300); onDismiss() }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.statusBarsPadding().height(110.dp))
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.5f)).clickable(remember { MutableInteractionSource() }, null) { closeWithAnimation() })
                }

                Box(modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 110.dp, start = 16.dp, end = 16.dp).fillMaxWidth()) {
                    AnimatedVisibility(
                        visible = animateIn,
                        enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                        exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
                    ) {
                        LocationPopup(
                            informacionDireccionesDisponibles = informacionDireccionesDisponibles,
                            alCerrar = { closeWithAnimation() },
                            alActualizar = { alActualizar(); closeWithAnimation() },
                            alAlternarGps = { alAlternarGps(); closeWithAnimation() },
                            alSeleccionarUbicacion = { alSeleccionarUbicacion(it); closeWithAnimation() },
                            direccionActiva = direccionActiva,
                            idPerfilSeleccionado = idPerfilSeleccionado,
                            estaGpsActivado = estaGpsActivado
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherDialog(
    show: Boolean,
    temperatura: String,
    emojiClima: String,
    descripcionClima: String,
    nombreCiudad: String,
    onDismiss: () -> Unit
) {
    if (show) {
        Popup(
            onDismissRequest = onDismiss,
            properties = PopupProperties(focusable = true, dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            var animateIn by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            LaunchedEffect(Unit) { animateIn = true }

            fun closeWithAnimation() {
                animateIn = false
                scope.launch { delay(300); onDismiss() }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.statusBarsPadding().height(110.dp))
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.5f)).clickable(remember { MutableInteractionSource() }, null) { closeWithAnimation() })
                }

                Box(modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 110.dp, start = 16.dp, end = 16.dp).fillMaxWidth()) {
                    AnimatedVisibility(
                        visible = animateIn,
                        enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                        exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
                    ) {
                        WeatherExpandedCard(
                            temperatura = temperatura,
                            emojiClima = emojiClima,
                            descripcionClima = descripcionClima,
                            nombreCiudad = nombreCiudad,
                            onDismiss = { closeWithAnimation() }
                        )
                    }
                }
            }
        }
    }
}

// ==================================================================================
// --- PREVIEWS ---
// ==================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
fun UserProfilePopupPreview() {
    val mockIdentidad = CuentaMaestroUsuario(
        cuenta = CuentaEntity(id = "1", correoGoogle = "juan.perez@gmail.com"),
        usuario = UsuarioDominioCompleto(
            perfil = IdentidadUsuarioEntity(
                id = "1",
                nombre = "Juan",
                apellido = "Pérez",
                nombreVisible = "JUAN",
                correoElectronico = "juan.perez@gmail.com"
            )
        )
    )
    ClienteTheme {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            UserProfilePopup(identidad = mockIdentidad, alCerrar = {}, alCerrarSesion = {}, alHacerClickPerfil = {})
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherExpandedCard(temperatura: String, emojiClima: String, descripcionClima: String, nombreCiudad: String, onDismiss: () -> Unit) {
    val cyberCyan = CPCyberColors.appCyan
    val deepGlass = Color(0xFF0B0F19).copy(alpha = 0.98f)

    val mensajeContexto = remember(temperatura, descripcionClima) {
        val valorTemp = temperatura.replace("°C", "").trim().toIntOrNull() ?: 20
        val estaLloviendo = descripcionClima.contains("lluvia", ignoreCase = true) || descripcionClima.contains("tormenta", ignoreCase = true)

        // --- LÓGICA HORARIA ELITE ---
        val hora = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val esNoche = hora >= 20 || hora < 7

        when {
            esNoche -> "🌙 MODO NOCTURNO: Recomendamos usar el servicio FAST o aplicar el filtro '24 HS' para encontrar profesionales activos ahora."
            estaLloviendo -> "⚠️ PRECAUCIÓN: Lluvia detectada. Las visitas técnicas externas podrían reprogramarse por seguridad."
            valorTemp > 30 -> "🔥 ALERTA TÉRMICA: Calor intenso. Asegúrate de tener hidratación disponible para recibir tus envíos."
            valorTemp < 10 -> "❄️ CLIMA FRÍO: Ideal para turnos en locales físicos. Abrígate antes de salir a tu cita."
            else -> "✨ CLIMA ÓPTIMO: Condiciones ideales para visitas técnicas y recepciones de pedidos."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CutCornerShape(topEnd = 24.dp, bottomStart = 24.dp))
            .background(deepGlass)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(cyberCyan, Color.Transparent, cyberCyan)),
                shape = CutCornerShape(topEnd = 24.dp, bottomStart = 24.dp)
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "SYS_WTHR // ATMÓSFERA", color = cyberCyan, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
            appCloseButton(onClick = onDismiss)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = nombreCiudad.uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
        Text(text = descripcionClima.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = cyberCyan.copy(alpha = 0.8f), letterSpacing = 1.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text(text = emojiClima, fontSize = 64.sp)
            Spacer(modifier = Modifier.width(24.dp))
            Text(text = temperatura, fontSize = 64.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = (-2).sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(text = mensajeContexto, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, lineHeight = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).background(cyberCyan, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(text = "REAL_TIME_FEED // ONLINE", color = Color.White.copy(alpha = 0.3f), fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
fun WeatherExpandedCardPreview() {
    ClienteTheme {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            WeatherExpandedCard(temperatura = "32°C", emojiClima = "☀️", descripcionClima = "Despejado y caluroso", nombreCiudad = "San Miguel de Tucumán", onDismiss = {})
        }
    }
}

*/

