package com.example.myapplication.uishared.ui.components.profile.parts

import android.net.Uri
import android.widget.Space
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.core.utilidades.ImageUtils
import com.example.myapplication.uishared.ui.components.profile.parts.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val ColorAcentoMav = Color(0xFFFF7043)

/**
 * --- SECCIONES DEL PERFIL (Ley #10 - Rompecabezas) ---
 * [PROPÓSITO]: Organismos de UI que agrupan bloques de datos (Personal, Empresa, Sucursal).
 */

@Composable
fun SeccionPerfilMaestroMav(
    identidad: PrestadorDominio,
    todasLasEntidades: List<PrestadorDominio> = emptyList(),
    esMiPropioPerfil: Boolean = false,
    todasLasCategorias: List<CategoriaDominio> = emptyList(),
    categoriasUsadasEnOtrasEmpresas: List<String> = emptyList(),
    alGuardarIdentidad: (PrestadorDominio) -> Unit = {},
    alVincularGoogle: () -> Unit = {},
    alDesvincularGoogle: () -> Unit = {},
    alConfigurarHorarios: (String) -> Unit = {},
    alNavegarAConfiguracionRecursos: (String) -> Unit = {},
    alDeshacerModoEmpresa: () -> Unit = {},
    alAñadirSucursal: (String) -> Unit = {},
    alEliminarIdentidad: (String, String) -> Unit = { _, _ -> },
    alAñadirEmpresa: () -> Unit = {},
    alEditarEquipo: (String) -> Unit = {},
    alEliminarRecurso: (RecursoDominio) -> Unit = {},
    alEliminarEmpleado: (EquipoTrabajoDominio) -> Unit = {},
    alAbrirEditorDireccion: (PrestadorDominio, DireccionDominio) -> Unit = { _, _ -> },
    alEliminarDireccion: (DireccionDominio) -> Unit = {}
) {
    val esEmpresa = identidad.tipo == TipoPrestador.EMPRESA
    
    // TODO: Vínculo con priorizarEmpresa de Cuenta
    val estaBloqueado = false 

    if (estaBloqueado) {
        CompanyModeLockedOverlay(onVolver = alDeshacerModoEmpresa)
        if (!esMiPropioPerfil) return
    }

    if (esEmpresa) {
        val sucursales = todasLasEntidades.filter { it.idEmpresa == identidad.id && it.tipo == TipoPrestador.SUCURSAL }
        SeccionEmpresaSoberanaMav(
            identidad = identidad,
            todasLasEntidades = todasLasEntidades,
            sucursalesVinculadas = sucursales,
            esMiPropioPerfil = esMiPropioPerfil,
            todasLasCategorias = todasLasCategorias,
            categoriasUsadasEnOtrasEmpresas = categoriasUsadasEnOtrasEmpresas,
            alGuardarIdentidad = alGuardarIdentidad,
            alVincularGoogle = alVincularGoogle,
            alDesvincularGoogle = alDesvincularGoogle,
            alConfigurarHorarios = alConfigurarHorarios,
            alNavegarAConfiguracionRecursos = alNavegarAConfiguracionRecursos,
            alAñadirSucursal = { alAñadirSucursal(identidad.id) },
            alEliminarSucursal = { id -> alEliminarIdentidad(id, "SUCURSAL") },
            alEditarEquipo = alEditarEquipo,
            alEliminarRecurso = alEliminarRecurso,
            alEliminarEmpleado = alEliminarEmpleado,
            alAbrirEditorDireccion = alAbrirEditorDireccion,
            alEliminarDireccion = alEliminarDireccion
        )
    } else {
        SeccionHumanoProfesionalMav(
            identidad = identidad,
            esMiPropioPerfil = esMiPropioPerfil,
            todasLasCategorias = todasLasCategorias,
            alGuardarIdentidad = alGuardarIdentidad,
            alVincularGoogle = alVincularGoogle,
            alDesvincularGoogle = alDesvincularGoogle,
            alConfigurarHorarios = alConfigurarHorarios,
            alNavegarAConfiguracionRecursos = alNavegarAConfiguracionRecursos,
            alEditarEquipo = alEditarEquipo,
            alEliminarRecurso = alEliminarRecurso,
            alEliminarEmpleado = alEliminarEmpleado,
            alAbrirEditorDireccion = alAbrirEditorDireccion,
            alEliminarDireccion = alEliminarDireccion
        )
    }
}

@Composable
fun SeccionHumanoProfesionalMav(
    identidad: PrestadorDominio,
    esMiPropioPerfil: Boolean,
    todasLasCategorias: List<CategoriaDominio>,
    alGuardarIdentidad: (PrestadorDominio) -> Unit,
    alVincularGoogle: () -> Unit,
    alDesvincularGoogle: () -> Unit,
    alConfigurarHorarios: (String) -> Unit,
    alNavegarAConfiguracionRecursos: (String) -> Unit,
    alEditarEquipo: (String) -> Unit = {},
    alEliminarRecurso: (RecursoDominio) -> Unit = {},
    alEliminarEmpleado: (EquipoTrabajoDominio) -> Unit = {},
    alAbrirEditorDireccion: (PrestadorDominio, DireccionDominio) -> Unit = { _, _ -> },
    alEliminarDireccion: (DireccionDominio) -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        val cornerRadius = 16.dp

        if (identidad.tipo == TipoPrestador.INDIVIDUAL) {
            TarjetaDatosPersonalesMav(
                identidad = identidad,
                esMiPropioPerfil = esMiPropioPerfil,
                alGuardar = alGuardarIdentidad,
                alVincularGoogle = alVincularGoogle,
                alDesvincularGoogle = alDesvincularGoogle
            )
            Spacer(Modifier.height(12.dp))
        }

        if (identidad.esPerfilComercial) {
            TarjetaActividadProfesionalMav(
                identidad = identidad,
                esMiPropioPerfil = esMiPropioPerfil,
                todasLasCategorias = todasLasCategorias,
                alGuardar = alGuardarIdentidad
            )

            Spacer(Modifier.height(12.dp))

            TarjetaHorariosMav(identidad, esMiPropioPerfil, alGuardarIdentidad, { alConfigurarHorarios(identidad.id) })
        }
        
        Spacer(Modifier.height(24.dp))

        CabeceraSeccionMav(
            titulo = if (identidad.esPerfilComercial) "MIS UBICACIONES Y BASES" else "MIS DIRECCIONES",
            emoji = "📍",
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        Spacer(Modifier.height(12.dp))

        identidad.direcciones.forEach { dir ->
            TarjetaDireccionEliteMav(
                direccion = dir,
                esSoloLectura = !esMiPropioPerfil,
                alEditar = { alAbrirEditorDireccion(identidad, it) },
                alBorrar = { alEliminarDireccion(dir) },
                mostrarBotonBorrar = esMiPropioPerfil,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (esMiPropioPerfil) {
            Button(
                onClick = { alAbrirEditorDireccion(identidad, DireccionDominio(idPropietario = identidad.idPropietario)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.AddLocationAlt, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text("AÑADIR UBICACIÓN", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SeccionEmpresaSoberanaMav(
    identidad: PrestadorDominio,
    todasLasEntidades: List<PrestadorDominio>,
    sucursalesVinculadas: List<PrestadorDominio>,
    esMiPropioPerfil: Boolean,
    todasLasCategorias: List<CategoriaDominio>,
    categoriasUsadasEnOtrasEmpresas: List<String>,
    alGuardarIdentidad: (PrestadorDominio) -> Unit,
    alVincularGoogle: () -> Unit,
    alDesvincularGoogle: () -> Unit,
    alConfigurarHorarios: (String) -> Unit,
    alNavegarAConfiguracionRecursos: (String) -> Unit,
    alAñadirSucursal: () -> Unit,
    alEliminarSucursal: (String) -> Unit,
    alEditarEquipo: (String) -> Unit = {},
    alEliminarRecurso: (RecursoDominio) -> Unit = {},
    alEliminarEmpleado: (EquipoTrabajoDominio) -> Unit = {},
    alAbrirEditorDireccion: (PrestadorDominio, DireccionDominio) -> Unit = { _, _ -> },
    alEliminarDireccion: (DireccionDominio) -> Unit = {}
) {
    var modoEdicionSucursales by remember { mutableStateOf(false) } 
    val pagerState = rememberPagerState(pageCount = { sucursalesVinculadas.size })
    val coroutineScope = rememberCoroutineScope()
    var sucursalABorrar by remember { mutableStateOf<PrestadorDominio?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        val cornerRadius = 16.dp
        
        if (esMiPropioPerfil) {
            TarjetaVinculoGoogleMav(emailGoogle = identidad.correo, enModoEdicion = true, alVincular = alVincularGoogle, alDesvincular = alDesvincularGoogle, forma = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius, bottomStart = 4.dp, bottomEnd = 4.dp))
            Spacer(Modifier.height(2.dp))
        }

        TarjetaDatosCorporativosMav(identidad = identidad, esMiPropioPerfil = esMiPropioPerfil, alGuardar = alGuardarIdentidad, forma = if (esMiPropioPerfil) RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = cornerRadius, bottomEnd = cornerRadius) else RoundedCornerShape(cornerRadius))
        
        Spacer(Modifier.height(12.dp))
        
        // TODO: CardRubrosEspecialidades para Empresa
        
        Spacer(Modifier.height(32.dp))

        CabeceraSeccionMav(
            titulo = "SUCURSALES / PUNTOS DE VENTA",
            emoji = "🏢",
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        if (esMiPropioPerfil) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.End) {
                if (modoEdicionSucursales && sucursalesVinculadas.size < 3) {
                    IconButton(onClick = alAñadirSucursal) { Icon(Icons.Default.AddBusiness, null, tint = ColorAcentoMav) }
                }
                IconButton(onClick = { modoEdicionSucursales = !modoEdicionSucursales }) { 
                    Icon(if(modoEdicionSucursales) Icons.Default.Close else Icons.Default.Edit, null, tint = if(modoEdicionSucursales) ColorAcentoMav else Color.Gray) 
                }
            }
        }

        if (sucursalesVinculadas.isNotEmpty()) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                contentColor = ColorAcentoMav,
                edgePadding = 0.dp,
                divider = {},
                indicator = { tabPositions ->
                    if (tabPositions.isNotEmpty()) { SecondaryIndicator(modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]), color = ColorAcentoMav) }
                }
            ) {
                sucursalesVinculadas.forEachIndexed { index, suc ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(suc.titulo.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                if (modoEdicionSucursales) {
                                    Spacer(Modifier.width(8.dp))
                                    IconButton(onClick = { sucursalABorrar = suc }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(14.dp)) }
                                }
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) { page ->
                val suc = sucursalesVinculadas[page]
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // [FIX]: forzamos idSucursal/idPropietario acá SIEMPRE, sin importar si la
                    // dirección ya venía bien vinculada desde Room — así el editor y el enrutamiento
                    // de actualizarDireccion (PerfilPrestadorDeepViewModel) identifican correctamente
                    // a qué sucursal pertenece, incluso con datos viejos guardados antes de este fix.
                    val direccionDeSucursal = remember(suc) {
                        (suc.direcciones.firstOrNull() ?: DireccionDominio(calle = suc.direccionVisible ?: ""))
                            .copy(idSucursal = suc.id, idPropietario = suc.idPropietario)
                    }
                    TarjetaDireccionEliteMav(
                        direccion = direccionDeSucursal,
                        esSoloLectura = !esMiPropioPerfil,
                        alEditar = { dir -> alAbrirEditorDireccion(suc, dir) },
                        alBorrar = { /* sucursales se eliminan desde la pestaña */ },
                        mostrarBotonBorrar = false,
                        esPuntoDeVenta = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (suc.esPerfilComercial) {
                        Column {
                            TarjetaHorariosMav(suc, esMiPropioPerfil, alGuardarIdentidad, { alConfigurarHorarios(suc.id) }, forma = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Box(modifier = Modifier.weight(1f)) { TarjetaCapacidadesMav(suc, esMiPropioPerfil, alGuardarIdentidad, forma = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, topEnd = 4.dp, bottomEnd = 4.dp)) }
                            Box(modifier = Modifier.weight(1f)) { TarjetaComercialesMav(suc, esMiPropioPerfil, alGuardarIdentidad, forma = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp, topStart = 4.dp, bottomStart = 4.dp)) }
                        }
                    } else {
                        // 🔥 [ELITE]: Para clientes, solo mostramos una etiqueta informativa de que es un punto de gestión
                        Surface(
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "PUNTO DE GESTIÓN Y ENTREGA",
                                modifier = Modifier.padding(16.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Gray,
                                letterSpacing = 1.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            Text("Este prestador no tiene sucursales activas visibles.", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
        }
    }

    // [FIX]: el ícono de borrado solo seteaba "sucursalABorrar" pero nada lo leía — nunca se
    // mostraba confirmación ni se llamaba a alEliminarSucursal, el botón no hacía nada.
    val sucursalPendienteDeBorrado = sucursalABorrar
    if (sucursalPendienteDeBorrado != null) {
        DialogoConfirmacion(
            titulo = "¿ELIMINAR SUCURSAL?",
            mensaje = "Se eliminará \"${sucursalPendienteDeBorrado.titulo}\" y toda su información asociada.",
            textoConfirmar = "ELIMINAR",
            onConfirm = {
                alEliminarSucursal(sucursalPendienteDeBorrado.id)
                sucursalABorrar = null
            },
            onDismiss = { sucursalABorrar = null }
        )
    }
}

// --- TARJETAS ESPECÍFICAS (Organismos) ---

@Composable
fun TarjetaDatosPersonalesMav(
    identidad: PrestadorDominio,
    esMiPropioPerfil: Boolean,
    alGuardar: (PrestadorDominio) -> Unit,
    alVincularGoogle: () -> Unit = {},
    alDesvincularGoogle: () -> Unit = {}
) {
    var mostrarHoja by remember { mutableStateOf(false) }
    val apodo = identidad.titulo
    val correo = identidad.correo
    val telefono = identidad.numeroTelefono
    val matricula = identidad.matricula ?: ""

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column {
                CabeceraSeccionMav(titulo = "PERFIL", emoji = "📇")
                Spacer(Modifier.height(14.dp))

                if (esMiPropioPerfil) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            FilaDatoPerfilCompactaMav("NOMBRE PÚBLICO", apodo, false)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            FilaDatoPerfilCompactaMav("TELÉFONO", telefono, false)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            FilaDatoPerfilCompactaMav("CORREO", correo, false, esEmail = true)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            Column {
                                FilaDatoPerfilCompactaMav("MATRÍCULA", matricula, false)
                                if (matricula.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    ChipEstadoMatriculaMav(estaVerificado = identidad.estaVerificado)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                    Spacer(Modifier.height(14.dp))

                    TarjetaVinculoGoogleMav(
                        emailGoogle = identidad.correo,
                        enModoEdicion = !identidad.esGoogle,
                        alVincular = alVincularGoogle,
                        alDesvincular = alDesvincularGoogle,
                        modoCompacto = true
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = ColorAcentoMav.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Badge,null, tint = ColorAcentoMav, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("NOMBRE PÚBLICO", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(apodo, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                    Spacer(Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Por privacidad, solo se comparte el nombre público de este usuario.",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                lineHeight = 14.sp
                            )
                        }
                    }
            }

            if (esMiPropioPerfil) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(ColorAcentoMav.copy(alpha = 0.1f))
                        .clickable { mostrarHoja = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, null, tint = ColorAcentoMav, modifier = Modifier.size(15.dp))
                }
            }
        }
    }

    if (mostrarHoja) {
        HojaEditarDatosPersonalesMav(
            identidad = identidad,
            onDismiss = { mostrarHoja = false },
            onGuardar = { nuevaIdentidad ->
                alGuardar(nuevaIdentidad)
                mostrarHoja = false
            }
        )
    }
}

@Composable
private fun ChipEstadoMatriculaMav(estaVerificado: Boolean) {
    val color = if (estaVerificado) Color(0xFF10B981) else Color(0xFFF59E0B)
    val texto = if (estaVerificado) "Verificado" else "En revisión"
    val icono = if (estaVerificado) Icons.Default.Verified else Icons.Default.Schedule
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Icon(icono, null, tint = color, modifier = Modifier.size(11.dp))
        Spacer(Modifier.width(4.dp))
        Text(texto, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaEditarDatosPersonalesMav(
    identidad: PrestadorDominio,
    onDismiss: () -> Unit,
    onGuardar: (PrestadorDominio) -> Unit
) {
    var apodo by remember { mutableStateOf(identidad.titulo) }
    var correo by remember { mutableStateOf(identidad.correo) }
    var telefono by remember { mutableStateOf(identidad.numeroTelefono) }
    var matricula by remember { mutableStateOf(identidad.matricula ?: "") }
    var matriculaFotoLocal by remember { mutableStateOf(identidad.matriculaFotoUrl) }
    var comprimiendoFoto by remember { mutableStateOf(false) }

    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()
    val uid = identidad.id

    val selectorFotoMatricula = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            comprimiendoFoto = true
            scope.launch {
                val ruta = withContext(Dispatchers.IO) {
                    val bytes = ImageUtils.compressElite(contexto, uri)
                    bytes?.let { ImageUtils.saveBytesToFile(contexto, it, "matricula_$uid") }
                }
                if (ruta != null) matriculaFotoLocal = ruta
                comprimiendoFoto = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF16161D),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { Box(modifier = Modifier.padding(top = 10.dp).size(width = 36.dp, height = 4.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 28.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Editar perfil", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(18.dp))

            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                FilaDatoPerfilCompactaMav("NOMBRE PÚBLICO", apodo, true) { apodo = it }
                FilaDatoPerfilCompactaMav("TELÉFONO", telefono, true) { telefono = it }
                FilaDatoPerfilCompactaMav("CORREO DE CONTACTO", correo, true) { correo = it }
                FilaDatoPerfilCompactaMav("MATRÍCULA PROFESIONAL", matricula, true) { matricula = it }
            }

            Spacer(Modifier.height(14.dp))

            FilaFotoMatriculaMav(
                fotoActual = matriculaFotoLocal,
                cargando = comprimiendoFoto,
                onSeleccionar = { selectorFotoMatricula.launch("image/*") },
                onQuitar = { matriculaFotoLocal = null }
            )

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                ) { Text("Cancelar", fontSize = 13.sp, fontWeight = FontWeight.Black) }

                Button(
                    onClick = {
                        onGuardar(identidad.copy(titulo = apodo, correo = correo, numeroTelefono = telefono, matricula = matricula, matriculaFotoUrl = matriculaFotoLocal))
                    },
                    modifier = Modifier.weight(2f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorAcentoMav)
                ) { Text("Guardar cambios", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White) }
            }
        }
    }
}

@Composable
private fun FilaFotoMatriculaMav(
    fotoActual: Any?,
    cargando: Boolean,
    onSeleccionar: () -> Unit,
    onQuitar: () -> Unit
) {
    Column {
        Text("FOTO DE TU MATRÍCULA (OPCIONAL)", fontSize = 9.5.sp, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(12.dp))
                    .clickable(enabled = !cargando) { onSeleccionar() },
                contentAlignment = Alignment.Center
            ) {
                when {
                    cargando -> CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = ColorAcentoMav)
                    fotoActual != null -> {
                        val esArchivoLocal = fotoActual is String && !fotoActual.startsWith("http") && !fotoActual.startsWith("data:")
                        AsyncImage(
                            model = if (esArchivoLocal) java.io.File(fotoActual) else fotoActual,
                            contentDescription = "Foto de la matrícula",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    if (fotoActual != null) "Foto cargada" else "Agregar foto",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    "Ayuda al admin a verificar tu matrícula más rápido",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
                if (fotoActual != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Quitar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF87171),
                        modifier = Modifier.clickable(enabled = !cargando) { onQuitar() }
                    )
                }
            }
        }
    }
}

@Composable
fun TarjetaDatosCorporativosMav(identidad: PrestadorDominio, esMiPropioPerfil: Boolean, alGuardar: (PrestadorDominio) -> Unit, forma: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp)) {
    var modoEdicion by remember { mutableStateOf(false) }
    var nombreComercial by remember(identidad) { mutableStateOf(identidad.titulo) }

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = forma, colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                CabeceraSeccionMav("DATOS CORPORATIVOS", "🏢")
                
                if (esMiPropioPerfil) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (modoEdicion) {
                            IconButton(onClick = { 
                                alGuardar(identidad.copy(titulo = nombreComercial))
                                modoEdicion = false 
                            }) { Icon(Icons.Default.Check, null, tint = Color(0xFF4ADE80)) }
                            IconButton(onClick = { 
                                modoEdicion = false 
                                nombreComercial = identidad.titulo
                            }) { Icon(Icons.Default.Close, null, tint = Color.Red.copy(alpha = 0.8f)) }
                        } else {
                            IconButton(onClick = { modoEdicion = true }) { 
                                Icon(Icons.Default.Edit, null, tint = ColorAcentoMav, modifier = Modifier.size(20.dp)) 
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            FilaDatoPerfilMav("NOMBRE COMERCIAL", nombreComercial, Icons.Default.Business, modoEdicion, { nombreComercial = it })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TarjetaRubrosEspecialidadesMav(identidad: PrestadorDominio, esMiPropioPerfil: Boolean, todasLasCategorias: List<CategoriaDominio>, alGuardar: (PrestadorDominio) -> Unit, esEmpresa: Boolean = false, forma: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp)) {
    var modoEdicion by remember { mutableStateOf(false) }
    var categoriasSeleccionadas by remember(identidad) { mutableStateOf(identidad.idCategorias) }
    var textoBusqueda by remember { mutableStateOf("") }

    val resultadosBusqueda = remember(textoBusqueda, todasLasCategorias) {
        if (textoBusqueda.isBlank()) emptyList()
        else todasLasCategorias.filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }.take(15)
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = forma, colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                CabeceraSeccionMav(if (esEmpresa) "RUBROS DE LA EMPRESA" else "RUBROS Y ESPECIALIDADES", "🛠️")
                
                if (esMiPropioPerfil) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (modoEdicion) {
                            IconButton(onClick = { 
                                alGuardar(identidad.copy(idCategorias = categoriasSeleccionadas))
                                modoEdicion = false 
                                textoBusqueda = ""
                            }) { Icon(Icons.Default.Check, null, tint = Color(0xFF4ADE80)) }
                            IconButton(onClick = { 
                                modoEdicion = false 
                                categoriasSeleccionadas = identidad.idCategorias
                                textoBusqueda = ""
                            }) { Icon(Icons.Default.Close, null, tint = Color.Red.copy(alpha = 0.8f)) }
                        } else {
                            IconButton(onClick = { modoEdicion = true }) { 
                                Icon(Icons.Default.Edit, null, tint = ColorAcentoMav, modifier = Modifier.size(20.dp)) 
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))

            if (modoEdicion) {
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    placeholder = { Text("Buscar nuevos rubros...", fontSize = 13.sp, color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = ColorAcentoMav, modifier = Modifier.size(20.dp)) },
                    trailingIcon = { if(textoBusqueda.isNotEmpty()) IconButton(onClick = { textoBusqueda = "" }) { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    ),
                    singleLine = true
                )
                
                if (resultadosBusqueda.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("RESULTADOS DEL CATÁLOGO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = ColorAcentoMav, letterSpacing = 1.sp)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        resultadosBusqueda.forEach { cat ->
                            val estaSeleccionada = categoriasSeleccionadas.contains(cat.id)
                            FilterChip(
                                selected = estaSeleccionada,
                                onClick = {
                                    categoriasSeleccionadas = if (estaSeleccionada) categoriasSeleccionadas - cat.id else categoriasSeleccionadas + cat.id
                                },
                                label = { Text(cat.nombre, fontSize = 10.sp) },
                                leadingIcon = { Text(cat.icono) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ColorAcentoMav.copy(alpha = 0.2f),
                                    selectedLabelColor = ColorAcentoMav
                                ),
                                border = null
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text("MIS RUBROS SELECCIONADOS", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
                Spacer(Modifier.height(8.dp))
            }
            
            if (categoriasSeleccionadas.isEmpty() && !modoEdicion) {
                Text("No hay rubros configurados.", color = Color.Gray, fontSize = 12.sp)
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (catId in categoriasSeleccionadas) {
                        val cat = todasLasCategorias.find { it.id == catId }
                        SuggestionChip(
                            onClick = { 
                                if (modoEdicion) categoriasSeleccionadas = categoriasSeleccionadas - catId 
                            },
                            label = { Text((cat?.nombre ?: catId).uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black) },
                            icon = { Text(cat?.icono ?: "🛠️") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (modoEdicion) Color.Red.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
                                labelColor = Color.White
                            ),
                            border = BorderStroke(1.dp, if (modoEdicion) Color.Red.copy(alpha = 0.3f) else ColorAcentoMav.copy(alpha = 0.2f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaCapacidadesMav(identidad: PrestadorDominio, esMiPropioPerfil: Boolean, alGuardar: (PrestadorDominio) -> Unit, forma: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp)) {
    var modoEdicion by remember { mutableStateOf(false) }
    var visitaADomicilio by remember(identidad) { mutableStateOf(identidad.visitaADomicilio) }
    var brindaTurnos by remember(identidad) { mutableStateOf(identidad.brindaTurnos) }

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = forma, colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column {
                CabeceraSeccionMav("CAPACIDADES", "🔌")
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EtiquetaFlagMav(
                        titulo = "Visitas Técnicas",
                        emoji = "🏠",
                        habilitado = visitaADomicilio,
                        enModoEdicion = modoEdicion,
                        alCambiar = { visitaADomicilio = it }
                    )
                    EtiquetaFlagMav(
                        titulo = "Turnos Online",
                        emoji = "📅",
                        habilitado = brindaTurnos,
                        enModoEdicion = modoEdicion,
                        alCambiar = { brindaTurnos = it }
                    )
                }
            }

            if (esMiPropioPerfil) {
                Row(modifier = Modifier.align(Alignment.TopEnd), verticalAlignment = Alignment.CenterVertically) {
                    if (modoEdicion) {
                        IconButton(onClick = { 
                            alGuardar(identidad.copy(visitaADomicilio = visitaADomicilio, brindaTurnos = brindaTurnos))
                            modoEdicion = false 
                        }) { Icon(Icons.Default.Check, null, tint = Color(0xFF4ADE80), modifier = Modifier.size(22.dp)) }
                        IconButton(onClick = { 
                            modoEdicion = false 
                            visitaADomicilio = identidad.visitaADomicilio
                            brindaTurnos = identidad.brindaTurnos
                        }) { Icon(Icons.Default.Close, null, tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(22.dp)) }
                    } else {
                        IconButton(onClick = { modoEdicion = true }) { 
                            Icon(Icons.Default.Edit, null, tint = ColorAcentoMav, modifier = Modifier.size(20.dp)) 
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaComercialesMav(identidad: PrestadorDominio, esMiPropioPerfil: Boolean, alGuardar: (PrestadorDominio) -> Unit, forma: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp)) {
    var modoEdicion by remember { mutableStateOf(false) }
    var realizaEnvios by remember(identidad) { mutableStateOf(identidad.realizaEnvios) }
    var brindaProducto by remember(identidad) { mutableStateOf(identidad.brindaProducto) }

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = forma, colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column {
                CabeceraSeccionMav("COMERCIALES", "📦")
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EtiquetaFlagMav(
                        titulo = "Realizo Envíos",
                        emoji = "🚚",
                        habilitado = realizaEnvios,
                        enModoEdicion = modoEdicion,
                        alCambiar = { realizaEnvios = it }
                    )
                    EtiquetaFlagMav(
                        titulo = "Venta Productos",
                        emoji = "🛍️",
                        habilitado = brindaProducto,
                        enModoEdicion = modoEdicion,
                        alCambiar = { brindaProducto = it }
                    )
                }
            }

            if (esMiPropioPerfil) {
                Row(modifier = Modifier.align(Alignment.TopEnd), verticalAlignment = Alignment.CenterVertically) {
                    if (modoEdicion) {
                        IconButton(onClick = { 
                            alGuardar(identidad.copy(realizaEnvios = realizaEnvios, brindaProducto = brindaProducto))
                            modoEdicion = false 
                        }) { Icon(Icons.Default.Check, null, tint = Color(0xFF4ADE80), modifier = Modifier.size(22.dp)) }
                        IconButton(onClick = { 
                            modoEdicion = false 
                            realizaEnvios = identidad.realizaEnvios
                            brindaProducto = identidad.brindaProducto
                        }) { Icon(Icons.Default.Close, null, tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(22.dp)) }
                    } else {
                        IconButton(onClick = { modoEdicion = true }) { 
                            Icon(Icons.Default.Edit, null, tint = ColorAcentoMav, modifier = Modifier.size(20.dp)) 
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TarjetaActividadProfesionalMav(
    identidad: PrestadorDominio,
    esMiPropioPerfil: Boolean,
    todasLasCategorias: List<CategoriaDominio>,
    alGuardar: (PrestadorDominio) -> Unit,
    forma: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp)
) {
    var mostrarHoja by remember { mutableStateOf(false) }

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = forma, colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                CabeceraSeccionMav("ACTIVIDAD PROFESIONAL", "🛠️")

                if (esMiPropioPerfil) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(ColorAcentoMav.copy(alpha = 0.1f))
                            .clickable { mostrarHoja = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, null, tint = ColorAcentoMav, modifier = Modifier.size(15.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (identidad.idCategorias.isEmpty()) {
                Text("No hay rubros configurados.", color = Color.Gray, fontSize = 12.sp)
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (catId in identidad.idCategorias) {
                        val cat = todasLasCategorias.find { it.id == catId }
                        Surface(
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, ColorAcentoMav.copy(alpha = 0.2f))
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(cat?.icono ?: "🛠️", fontSize = 12.sp)
                                Spacer(Modifier.width(6.dp))
                                Text((cat?.nombre ?: catId).uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    EtiquetaFlagMav("Visitas Técnicas", "🏠", identidad.visitaADomicilio, false)
                }
                Box(modifier = Modifier.weight(1f)) {
                    EtiquetaFlagMav("Turnos Online", "📅", identidad.brindaTurnos, false)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    EtiquetaFlagMav("Realizo Envíos", "🚚", identidad.realizaEnvios, false)
                }
                Box(modifier = Modifier.weight(1f)) {
                    EtiquetaFlagMav("Venta Productos", "🛍️", identidad.brindaProducto, false)
                }
            }
        }
    }

    if (mostrarHoja) {
        HojaEditarActividadProfesionalMav(
            identidad = identidad,
            todasLasCategorias = todasLasCategorias,
            onDismiss = { mostrarHoja = false },
            onGuardar = { nuevaIdentidad ->
                alGuardar(nuevaIdentidad)
                mostrarHoja = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun HojaEditarActividadProfesionalMav(
    identidad: PrestadorDominio,
    todasLasCategorias: List<CategoriaDominio>,
    onDismiss: () -> Unit,
    onGuardar: (PrestadorDominio) -> Unit
) {
    var categoriasSeleccionadas by remember { mutableStateOf(identidad.idCategorias) }
    var textoBusqueda by remember { mutableStateOf("") }
    var visitaADomicilio by remember { mutableStateOf(identidad.visitaADomicilio) }
    var brindaTurnos by remember { mutableStateOf(identidad.brindaTurnos) }
    var realizaEnvios by remember { mutableStateOf(identidad.realizaEnvios) }
    var brindaProducto by remember { mutableStateOf(identidad.brindaProducto) }

    val resultadosBusqueda = remember(textoBusqueda, todasLasCategorias) {
        if (textoBusqueda.isBlank()) emptyList()
        else todasLasCategorias.filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }.take(15)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF16161D),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { Box(modifier = Modifier.padding(top = 10.dp).size(width = 36.dp, height = 4.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 28.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Editar actividad profesional", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(18.dp))

            Text("RUBROS Y ESPECIALIDADES", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                placeholder = { Text("Buscar nuevos rubros...", fontSize = 13.sp, color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Search, null, tint = ColorAcentoMav, modifier = Modifier.size(20.dp)) },
                trailingIcon = { if (textoBusqueda.isNotEmpty()) IconButton(onClick = { textoBusqueda = "" }) { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) } },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = ColorAcentoMav,
                    focusedBorderColor = ColorAcentoMav,
                    unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                ),
                singleLine = true
            )

            Spacer(Modifier.height(14.dp))

            val chipsAMostrar = if (textoBusqueda.isBlank()) {
                categoriasSeleccionadas.mapNotNull { id -> todasLasCategorias.find { it.id == id } }
            } else resultadosBusqueda

            if (chipsAMostrar.isEmpty() && textoBusqueda.isBlank()) {
                Text("No hay rubros seleccionados todavía.", color = Color.Gray, fontSize = 12.sp)
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    chipsAMostrar.forEach { cat ->
                        val estaSeleccionada = categoriasSeleccionadas.contains(cat.id)
                        FilterChip(
                            selected = estaSeleccionada,
                            onClick = {
                                categoriasSeleccionadas = if (estaSeleccionada) categoriasSeleccionadas - cat.id else categoriasSeleccionadas + cat.id
                            },
                            label = { Text(cat.nombre, fontSize = 11.sp) },
                            leadingIcon = { Text(cat.icono) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White.copy(alpha = 0.05f),
                                labelColor = Color.Gray,
                                selectedContainerColor = ColorAcentoMav.copy(alpha = 0.15f),
                                selectedLabelColor = ColorAcentoMav
                            ),
                            border = null
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
            Spacer(Modifier.height(20.dp))

            Text("CAPACIDADES Y SERVICIOS", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
            Spacer(Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EtiquetaFlagMav("Visitas Técnicas", "🏠", visitaADomicilio, true) { visitaADomicilio = it }
                EtiquetaFlagMav("Turnos Online", "📅", brindaTurnos, true) { brindaTurnos = it }
                EtiquetaFlagMav("Realizo Envíos", "🚚", realizaEnvios, true) { realizaEnvios = it }
                EtiquetaFlagMav("Venta Productos", "🛍️", brindaProducto, true) { brindaProducto = it }
            }

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                ) { Text("Cancelar", fontSize = 13.sp, fontWeight = FontWeight.Black) }

                Button(
                    onClick = {
                        onGuardar(
                            identidad.copy(
                                idCategorias = categoriasSeleccionadas,
                                visitaADomicilio = visitaADomicilio,
                                brindaTurnos = brindaTurnos,
                                realizaEnvios = realizaEnvios,
                                brindaProducto = brindaProducto
                            )
                        )
                    },
                    modifier = Modifier.weight(2f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorAcentoMav)
                ) { Text("Guardar cambios", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White) }
            }
        }
    }
}

@Composable
fun TarjetaHorariosMav(
    identidad: PrestadorDominio, 
    esMiPropioPerfil: Boolean, 
    alGuardar: (PrestadorDominio) -> Unit, 
    alConfigurar: () -> Unit, 
    forma: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp)
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(), 
        shape = forma, 
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // HEADER Y LEYENDA (Estilo Elite)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    CabeceraSeccionMav("HORARIOS", "📅")
                    Text(
                        text = if (identidad.atiende24h) "Disponible 24hs (Urgencias)" else "Mapa de disponibilidad semanal",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                if (esMiPropioPerfil) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(ColorAcentoMav.copy(alpha = 0.1f))
                            .clickable(onClick = alConfigurar),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, null, tint = ColorAcentoMav, modifier = Modifier.size(15.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (identidad.atiende24h) {
                // Estado 24hs simplificado
                Surface(
                    color = Color(0xFF4ADE80).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF4ADE80).copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, null, tint = Color(0xFF4ADE80), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("ATENCIÓN CONTINUA 24/7", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 1.sp)
                    }
                }
            } else {
                // Matriz de Horarios Táctica
                ResumenHorarioMatrix(identidad.horario)
            }
        }
    }
}

/**
 * --- MATRIZ DE HORARIOS ELITE (v2026.FINAL) ---
 * [PROPÓSITO]: Visualización de tipo 'Heatmap' para la disponibilidad semanal.
 */
@Composable
fun ResumenHorarioMatrix(horario: HorarioDominio?) {
    val horasEscala = listOf(8, 10, 12, 14, 16, 18, 20)
    val inicialesDias = listOf("L", "M", "X", "J", "V", "S", "D")
    val colorAcento = Color(0xFFFF7043)
    val colorVacio = Color.White.copy(alpha = 0.05f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // 1. Fila de Días
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(42.dp))
            inicialesDias.forEach { dia ->
                Text(
                    text = dia,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Gray
                )
            }
        }

        // 2. Filas de Horas (Grid)
        horasEscala.forEach { horaActual ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Etiqueta de la hora
                Text(
                    text = "%02d:00".format(horaActual),
                    modifier = Modifier.width(42.dp).padding(end = 8.dp),
                    textAlign = TextAlign.End,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                // Celdas por día
                (1..7).forEach { diaNum ->
                    val rangosDia = when(diaNum) {
                        1 -> horario?.lunes ?: emptyList()
                        2 -> horario?.martes ?: emptyList()
                        3 -> horario?.miercoles ?: emptyList()
                        4 -> horario?.jueves ?: emptyList()
                        5 -> horario?.viernes ?: emptyList()
                        6 -> horario?.sabado ?: emptyList()
                        7 -> horario?.domingo ?: emptyList()
                        else -> emptyList()
                    }

                    // Determinar si la hora cae dentro de algún rango
                    val estaAbierto = rangosDia.any { rango ->
                        val hStart = rango.inicio.split(":").firstOrNull()?.toIntOrNull() ?: 0
                        val hEnd = rango.fin.split(":").firstOrNull()?.toIntOrNull() ?: 0
                        horaActual in hStart until hEnd
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                            .height(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (estaAbierto) colorAcento else colorVacio)
                            .then(
                                if (estaAbierto) Modifier.border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                else Modifier
                            )
                    )
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        // 3. Mini Leyenda
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colorAcento))
            Spacer(Modifier.width(6.dp))
            Text("ABIERTO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Gray)
            Spacer(Modifier.width(16.dp))
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colorVacio))
            Spacer(Modifier.width(6.dp))
            Text("CERRADO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Gray)
        }
    }
}

// TarjetaUbicacionBaseMav eliminada en favor de TarjetaDireccionEliteMav directa

// --- PREVIEWS ---

@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun PreviewSeccionesMav() {
    val m = PrestadorPerfilMocks.elenaRodriguez
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CabeceraSeccionMav("Vista Previa Secciones", "🧩")
        SeccionHumanoProfesionalMav(
            identidad = m,
            esMiPropioPerfil = true,
            todasLasCategorias = emptyList(),
            alGuardarIdentidad = {},
            alVincularGoogle = {},
            alDesvincularGoogle = {},
            alConfigurarHorarios = {},
            alNavegarAConfiguracionRecursos = {}
        )
    }
}

































