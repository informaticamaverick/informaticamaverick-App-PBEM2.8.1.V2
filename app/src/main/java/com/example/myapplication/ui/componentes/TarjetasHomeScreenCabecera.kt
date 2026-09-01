/*
 * --- 🛑 ARCHIVO EN DESUSO (DEPRECATED) ---
 * PROPÓSITO: Este archivo ha sido reemplazado por la arquitectura atómica en:
 * [com.example.myapplication.ui.componentes.sistema.cabecera]
 * 
 * LEY #10: Se mantiene comentado íntegramente para trazabilidad histórica.
 ************************************************************************************

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.myapplication.ui.estilos.ClienteTheme
import com.example.myapplication.core.dominio.modelos.CuentaMaestroUsuario
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.uishared.estilos.CPCyberColors
import com.example.myapplication.uishared.estilos.AppTypography
import com.example.myapplication.ui.componentes.sistema.AutoSizeText
import com.example.myapplication.uishared.estilos.SharedPalette


// ==================================================================================
// --- SECCIÓN 1: ORQUESTADORES DE CABECERA (SMART) ---
// ==================================================================================

// ==================================================================================
// --- SECCIÓN 2: COMPONENTES VISUALES DE CABECERA (DUMB) ---
// ==================================================================================

/**
 * --- CABECERA ELITE V2 (STAINLESS PERFORMANCE) ---
 * PROPÓSITO: Orquestador visual de alta eficiencia para la Home.
 * LEY #10: Desacoplamiento y Reactividad.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TopHeaderSectionContentV2(
    navController: NavHostController,
    nombrePerfilActivo: String,
    fotoPerfilActivo: Any?,
    esPerfilPersonal: Boolean,
    idPerfilSeleccionado: String?,
    temperatura: String,
    emojiClima: String,
    descripcionClima: String,
    direccionActiva: DireccionDominio?,
    alHacerClickClima: () -> Unit,
    alActualizarUbicacion: () -> Unit,
    alAlternarGps: () -> Unit = {},
    estaGpsActivado: Boolean = true,
    alSeleccionarUbicacion: (DireccionDominio) -> Unit,
    alSeleccionarPerfil: (String?) -> Unit,
    alCerrarSesion: () -> Unit,
    cuentaMaestro: CuentaMaestroUsuario?,
    mostrarDialogoClima: Boolean = false,
    nombreCiudad: String = "",
    alEstablecerVisibilidadDetallesClima: (Boolean) -> Unit = {},
    mostrarDialogoUbicacionElevado: Boolean? = null,
    mostrarDialogoPerfilElevado: Boolean? = null,
    alAlternarDialogoUbicacion: (Boolean) -> Unit = {},
    alAlternarDialogoPerfil: (Boolean) -> Unit = {}
) {
    var mostrarDialogoUbicacionLocal by remember { mutableStateOf(false) }
    var mostrarDialogoPerfilLocal by remember { mutableStateOf(false) }

    val mostrarDialogoUbicacion = mostrarDialogoUbicacionElevado ?: mostrarDialogoUbicacionLocal
    val mostrarDialogoPerfil = mostrarDialogoPerfilElevado ?: mostrarDialogoPerfilLocal

    val setMostrarDialogoUbicacion: (Boolean) -> Unit = remember {
        { valActiva -> 
            if (mostrarDialogoUbicacionElevado != null) alAlternarDialogoUbicacion(valActiva) 
            else mostrarDialogoUbicacionLocal = valActiva 
        }
    }
    
    val setMostrarDialogoPerfil: (Boolean) -> Unit = remember {
        { valActiva -> 
            if (mostrarDialogoPerfilElevado != null) alAlternarDialogoPerfil(valActiva) 
            else mostrarDialogoPerfilLocal = valActiva 
        }
    }

    // --- OPTIMIZACIÓN: Gradientes Estáticos con Remember ---
    // val deepVoidBrush = remember { Brush.verticalGradient(listOf(CPCyberColors.DeepVoid, Color.Black)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CutCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(CPCyberColors.DeepVoid.copy(alpha = 0.95f))
            .drawBehind { drawCyberHeaderBorder() }
            .statusBarsPadding()
            .height(88.dp)
            .padding(horizontal = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IdentitySlot(
                nombrePerfilActivo = nombrePerfilActivo,
                fotoPerfilActivo = fotoPerfilActivo,
                esPerfilPersonal = esPerfilPersonal,
                alHacerClickPerfil = { setMostrarDialogoPerfil(true) },
                modifier = Modifier.weight(1f)
            )

            LocationSlot(
                direccionActiva = direccionActiva,
                alHacerClickUbicacion = { setMostrarDialogoUbicacion(true) },
                modifier = Modifier.weight(1.5f)
            )

            WeatherSlot(
                temperatura = temperatura,
                emojiClima = emojiClima,
                descripcionClima = descripcionClima,
                alHacerClickClima = alHacerClickClima,
                modifier = Modifier.weight(0.9f)
            )
        }
    }

    HeaderDialogs(
        mostrarDialogoUbicacion = mostrarDialogoUbicacion,
        mostrarDialogoPerfil = mostrarDialogoPerfil,
        cuentaMaestro = cuentaMaestro,
        direccionActiva = direccionActiva,
        idPerfilSeleccionado = idPerfilSeleccionado,
        estaGpsActivado = estaGpsActivado,
        alActualizarUbicacion = alActualizarUbicacion,
        alAlternarGps = alAlternarGps,
        alSeleccionarUbicacion = alSeleccionarUbicacion,
        alSeleccionarPerfil = alSeleccionarPerfil,
        alCerrarSesion = alCerrarSesion,
        navController = navController,
        esPerfilPersonal = esPerfilPersonal,
        mostrarDialogoClima = mostrarDialogoClima,
        temperatura = temperatura,
        emojiClima = emojiClima,
        descripcionClima = descripcionClima,
        nombreCiudad = nombreCiudad,
        alEstablecerVisibilidadDetallesClima = alEstablecerVisibilidadDetallesClima,
        setMostrarDialogoUbicacion = setMostrarDialogoUbicacion,
        setMostrarDialogoPerfil = setMostrarDialogoPerfil
    )
}

/**
 * Fragmento de Identidad (Slot Izquierdo - Premium UX)
 */
@Composable
private fun IdentitySlot(
    nombrePerfilActivo: String,
    fotoPerfilActivo: Any?,
    esPerfilPersonal: Boolean,
    alHacerClickPerfil: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Transparent) // 🔥 Transparente para ver el fondo
            .clickable { alHacerClickPerfil() }
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)))
                .border(1.dp, Brush.sweepGradient(listOf(MaverickColors.ElectricCyan, CPCyberColors.ElectricPurple, MaverickColors.ElectricCyan)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (fotoPerfilActivo != null) {
                AsyncImage(
                    model = fotoPerfilActivo,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Person, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(24.dp))
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = if (esPerfilPersonal) "BIENVENIDO" else "ENTIDAD ACTIVA",
                style = MaverickTypography.HeaderTitle.copy(
                    fontSize = 7.sp, 
                    color = MaverickColors.ElectricCyan.copy(alpha = 0.8f), 
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Black
                )
            )
            Text(
                text = nombrePerfilActivo.uppercase(),
                style = MaverickTypography.HeaderTitle.copy(
                    fontSize = 13.sp, 
                    color = Color.White, 
                    letterSpacing = 0.4.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Fragmento de Ubicación (Slot Central - Diseño Hi-Tech)
 */
@Composable
private fun LocationSlot(
    direccionActiva: DireccionDominio?,
    alHacerClickUbicacion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Transparent) // 🔥 Transparente para ver el fondo
            .clickable { alHacerClickUbicacion() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val isGps = direccionActiva?.id == "gps_current"
        val modeLabel = when {
            isGps -> "COORD_LIVE"
            direccionActiva?.esEmpresa == true -> "HQ_NETWORK"
            else -> "HOME_STATION"
        }
        val modeColor = when {
            isGps -> MaverickColors.ElectricCyan
            direccionActiva?.esEmpresa == true -> MaverickColors.ElectricPurple
            else -> Color.White.copy(alpha = 0.5f)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(if(isGps) modeColor.copy(alpha = dotAlpha) else modeColor.copy(alpha = 0.3f))
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = modeLabel,
                fontSize = 7.sp,
                fontWeight = FontWeight.Black,
                color = modeColor,
                letterSpacing = 1.5.sp
            )
        }

        Spacer(Modifier.height(4.dp))

        AutoSizeText(
            text = (direccionActiva?.etiqueta ?: direccionActiva?.calle ?: "SCANNING...").uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 0.3.sp
            ),
            maxLines = 1
        )
        
        Text(
            text = (direccionActiva?.localidad ?: "BUSCANDO...").uppercase(),
            fontSize = 7.sp,
            color = Color.White.copy(alpha = 0.35f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Fragmento de Clima (Slot Derecho - Diseño de Impacto)
 */
@Composable
private fun WeatherSlot(
    temperatura: String,
    emojiClima: String,
    descripcionClima: String,
    alHacerClickClima: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Transparent) // 🔥 Transparente para ver el fondo
            .clickable { alHacerClickClima() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = emojiClima,
                fontSize = 32.sp,
                modifier = Modifier
                    .graphicsLayer { alpha = 0.25f }
                    .offset(x = 4.dp, y = (-2).dp)
            )
            Text(
                text = temperatura,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = (-1).sp
            )
        }
        
        AutoSizeText(
            text = descripcionClima.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 6.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.4.sp
            ),
            color = Color.White.copy(alpha = 0.45f),
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Orquestador de Diálogos para limpiar el componente principal
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HeaderDialogs(
    mostrarDialogoUbicacion: Boolean,
    mostrarDialogoPerfil: Boolean,
    cuentaMaestro: CuentaMaestroUsuario?,
    direccionActiva: DireccionDominio?,
    idPerfilSeleccionado: String?,
    estaGpsActivado: Boolean,
    alActualizarUbicacion: () -> Unit,
    alAlternarGps: () -> Unit,
    alSeleccionarUbicacion: (DireccionDominio) -> Unit,
    alSeleccionarPerfil: (String?) -> Unit,
    alCerrarSesion: () -> Unit,
    navController: NavHostController,
    esPerfilPersonal: Boolean,
    mostrarDialogoClima: Boolean,
    temperatura: String,
    emojiClima: String,
    descripcionClima: String,
    nombreCiudad: String,
    alEstablecerVisibilidadDetallesClima: (Boolean) -> Unit,
    setMostrarDialogoUbicacion: (Boolean) -> Unit,
    setMostrarDialogoPerfil: (Boolean) -> Unit
) {
    val informacionDireccionesDisponibles = remember(cuentaMaestro) {
        if (cuentaMaestro == null) emptyList<DireccionDominio>()
        else {
            val list = mutableListOf<DireccionDominio>()
            list.addAll(cuentaMaestro.usuario.direcciones.map { it.aModelo() })
            cuentaMaestro.empresas.forEach { company ->
                company.sucursales.forEach { branch ->
                    branch.direccion?.let { list.add(it.aModelo()) }
                }
            }
            list
        }
    }

    LocationDialog(
        show = mostrarDialogoUbicacion,
        informacionDireccionesDisponibles = informacionDireccionesDisponibles,
        direccionActiva = direccionActiva,
        idPerfilSeleccionado = idPerfilSeleccionado,
        estaGpsActivado = estaGpsActivado,
        alActualizar = { alActualizarUbicacion() },
        alAlternarGps = alAlternarGps,
        alSeleccionarUbicacion = { direccion -> 
            alSeleccionarUbicacion(direccion)
            setMostrarDialogoUbicacion(false) 
        },
        onDismiss = { setMostrarDialogoUbicacion(false) }
    )

    if (cuentaMaestro != null) {
        ProfileDialog(
            show = mostrarDialogoPerfil,
            identidad = cuentaMaestro,
            esPerfilPersonal = esPerfilPersonal,
            idPerfilSeleccionado = idPerfilSeleccionado,
            alSeleccionarPerfil = alSeleccionarPerfil,
            navController = navController,
            alCerrarSesion = { alCerrarSesion(); setMostrarDialogoPerfil(false) },
            onDismiss = { setMostrarDialogoPerfil(false) }
        )
    }

    WeatherDialog(
        show = mostrarDialogoClima,
        temperatura = temperatura,
        emojiClima = emojiClima,
        descripcionClima = descripcionClima,
        nombreCiudad = nombreCiudad,
        onDismiss = { alEstablecerVisibilidadDetallesClima(false) }
    )
}

/**
 * Lógica de dibujo del borde Cyberpunk extraída
 */
private fun DrawScope.drawCyberHeaderBorder() {
    val strokeWidth = 1.2.dp.toPx()
    val path = Path().apply {
        moveTo(0f, size.height - 16.dp.toPx())
        lineTo(16.dp.toPx(), size.height)
        lineTo(size.width - 16.dp.toPx(), size.height)
        lineTo(size.width, size.height - 16.dp.toPx())
    }
    
    val borderGradient = Brush.horizontalGradient(
        0.0f to MaverickColors.ElectricCyan.copy(alpha = 0.05f),
        0.15f to MaverickColors.ElectricCyan,
        0.85f to MaverickColors.ElectricCyan,
        1.0f to MaverickColors.ElectricCyan.copy(alpha = 0.05f)
    )

    drawPath(
        path = path,
        brush = borderGradient,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    drawPath(
        path = path,
        brush = borderGradient,
        style = Stroke(width = strokeWidth * 2.5f, cap = StrokeCap.Round),
        alpha = 0.15f
    )

/******************************  EFECTO DE LUZ ATRAS DEL LA FOTO DE PERFIL ******************************************************

    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(CPCyberColors.appCyan.copy(alpha = 0.1f), Color.Transparent),
            center = Offset(size.width / 2, size.height / 2),
            radius = size.width / 0.5f
        ),
        alpha = 0.6f
    )

************************************************************************************************************************************/
}



@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
fun TopHeaderSectionV2Preview() {
    val mockAddress = DireccionDominio(
        id = "gps_current",
        calle = "Calle Falsa",
        numero = "123",
        localidad = "San Miguel de Tucumán",
        provincia = "Tucumán",
        pais = "Argentina",
        codigoPostal = "T4000",
        latitud = -26.8,
        longitud = -65.2,
        etiqueta = "GPS",
        esEmpresa = false
    )

    ClienteTheme {
        TopHeaderSectionContentV2(
            navController = rememberNavController(),
            nombrePerfilActivo = "JUAN PEREZ",
            fotoPerfilActivo = null,
            esPerfilPersonal = true,
            idPerfilSeleccionado = null,
            temperatura = "24°C",
            emojiClima = "☀️",
            descripcionClima = "Despejado",
            direccionActiva = mockAddress,
            alHacerClickClima = {},
            alActualizarUbicacion = {},
            alAlternarGps = {},
            estaGpsActivado = true,
            alSeleccionarUbicacion = {},
            alSeleccionarPerfil = {},
            alCerrarSesion = {},
            cuentaMaestro = null
        )
    }
}

*************************************************************************************/
