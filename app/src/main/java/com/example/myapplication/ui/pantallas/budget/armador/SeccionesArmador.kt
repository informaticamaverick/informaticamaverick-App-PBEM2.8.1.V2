package com.example.myapplication.ui.pantallas.budget.armador

import androidx.compose.animation.*
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import com.example.myapplication.core.dominio.modelos.CuentaMaestroUsuario
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.ui.componentes.sistema.contexto.MoldeTarjetaPerfilDirec
import com.example.myapplication.ui.estilos.PBEMTheme
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.uishared.ui.components.TextCompacto
import com.example.myapplication.uishared.ui.components.TextCompactoAutoFit

/**
 * --- SECCIONES DEL ARMADOR DE CONCURSO (v2026.ELITE) ---
 * [LEY #1]: Stateless UI. Solo reflejan el estado recibido.
 * [LEY #9]: Estándar Mav en Español.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionSolicitante(
    estadoCuenta: CuentaMaestroUsuario?,
    todasLasCategorias: List<CategoriaEntity>,
    idCategoriaSeleccionada: String,
    nombreCategoria: String,
    iconoCategoria: String,
    descripcionCategoria: String,
    alSeleccionarCategoria: (CategoriaEntity) -> Unit,
    queryCategoria: String,
    alCambiarQueryCategoria: (String) -> Unit,
    menuCategoriasExpandido: Boolean,
    alAlternarMenuCategorias: (Boolean) -> Unit,
    idPerfilSeleccionado: String?,
    alCambiarPerfil: (String?, String?) -> Unit,
    mostrarMenuPerfil: Boolean,
    alAlternarMenuPerfil: (Boolean) -> Unit,
    direccionSeleccionada: DireccionDominio?,
    alSeleccionarDireccion: (DireccionDominio) -> Unit,
    mostrarMenuUbicacion: Boolean,
    alAlternarMenuUbicacion: (Boolean) -> Unit,
    estaGpsActivo: Boolean,
    alAlternarGps: () -> Unit,
    esDireccionManual: Boolean,
    alActivarDireccionManual: (Boolean) -> Unit,
    calleManual: String,
    alCambiarCalle: (String) -> Unit,
    numeroManual: String,
    alCambiarNumero: (String) -> Unit,
    ciudadManual: String,
    alCambiarCiudad: (String) -> Unit,
    cpManual: String,
    alCambiarCp: (String) -> Unit
) {
    val categoriasFiltradas = remember(queryCategoria, todasLasCategorias) {
        if (queryCategoria.isBlank()) todasLasCategorias
        else todasLasCategorias.filter { 
            it.nombre.contains(queryCategoria, ignoreCase = true) || 
            (it.descripcion?.contains(queryCategoria, ignoreCase = true) == true) 
        }
    }

    val perfilActivo = remember(estadoCuenta, idPerfilSeleccionado) {
        if (idPerfilSeleccionado == null) estadoCuenta?.usuario?.perfil?.nombreVisible ?: "Mi Perfil"
        else estadoCuenta?.empresas?.flatMap { it.sucursales }?.find { it.sucursal.id == idPerfilSeleccionado }?.sucursal?.nombre ?: "Empresa"
    }

    val fotoPerfilActiva = remember(estadoCuenta, idPerfilSeleccionado) {
        if (idPerfilSeleccionado == null) estadoCuenta?.usuario?.perfil?.urlMiniatura ?: estadoCuenta?.usuario?.perfil?.urlFoto
        else null
    }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        TextCompacto(
            text = "PASO 1: IDENTIDAD Y UBICACIÓN",
            color = SharedPalette.ElectricCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )

        // --- TARJETA DE PERFIL Y DIRECCIÓN ELITE ---
        MoldeTarjetaPerfilDirec(
            usuario = estadoCuenta,
            nombrePerfilActivo = perfilActivo,
            fotoPerfilActivo = fotoPerfilActiva,
            direccionActiva = direccionSeleccionada,
            estaGpsActivo = estaGpsActivo,
            alHacerClickPerfil = { alAlternarMenuPerfil(true) },
            alHacerClickUbicacion = { alAlternarMenuUbicacion(true) },
            alAlternarGps = alAlternarGps,
            alSeleccionarDireccion = alSeleccionarDireccion,
            alSeleccionarPerfil = alCambiarPerfil,
            mostrarMenuPerfil = mostrarMenuPerfil,
            mostrarMenuUbicacion = mostrarMenuUbicacion,
            alOcultarMenu = { 
                alAlternarMenuPerfil(false)
                alAlternarMenuUbicacion(false)
            }
        )

        if (!esDireccionManual) {
            Text(
                text = "¿Quieres agregar una dirección manualmente?",
                color = SharedPalette.ElectricCyan,
                fontSize = 11.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable { alActivarDireccionManual(true) }
                    .padding(vertical = 4.dp)
            )
        } else {
            // Formulario Manual
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TacticalTextField(valor = calleManual, alCambiarValor = alCambiarCalle, etiqueta = "Calle", icono = Icons.Default.Map, pista = "Ej: Av. Rivadavia", modificador = Modifier.weight(2f))
                    TacticalTextField(valor = numeroManual, alCambiarValor = alCambiarNumero, etiqueta = "Altura", icono = Icons.Default.Pin, pista = "123", modificador = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TacticalTextField(valor = ciudadManual, alCambiarValor = alCambiarCiudad, etiqueta = "Ciudad / Localidad", icono = Icons.Default.LocationCity, pista = "Ej: CABA", modificador = Modifier.weight(2f))
                    TacticalTextField(valor = cpManual, alCambiarValor = alCambiarCp, etiqueta = "CP", icono = Icons.Default.Numbers, pista = "1406", modificador = Modifier.weight(1f))
                }
                
                TextButton(onClick = { alActivarDireccionManual(false) }) {
                    Text("Volver a mis direcciones guardadas", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }

        // --- BUSCADOR DE RUBRO / CATEGORÍA ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TextCompacto("RUBRO DEL SERVICIO", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Black)
            
            // 🔥 [ELITE FIX]: Gestión de cursor estable para buscador de rubros
            var textoInterno by remember { 
                mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(queryCategoria)) 
            }
            
            LaunchedEffect(queryCategoria) {
                if (queryCategoria != textoInterno.text) {
                    textoInterno = textoInterno.copy(
                        text = queryCategoria,
                        selection = androidx.compose.ui.text.TextRange(queryCategoria.length)
                    )
                }
            }

            OutlinedTextField(
                value = textoInterno,
                onValueChange = { nuevoValor ->
                    textoInterno = nuevoValor
                    if (nuevoValor.text != queryCategoria) {
                        alCambiarQueryCategoria(nuevoValor.text)
                    }
                    alAlternarMenuCategorias(nuevoValor.text.isNotEmpty())
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { 
                    TextCompacto(
                        text = if (idCategoriaSeleccionada.isNotEmpty()) nombreCategoria else "Buscar rubro (ej: Plomería...)",
                        color = if (idCategoriaSeleccionada.isNotEmpty()) Color.White else Color.Gray,
                        fontSize = 14.sp
                    ) 
                },
                leadingIcon = { 
                    if (idCategoriaSeleccionada.isNotEmpty()) Text(iconoCategoria, fontSize = 20.sp)
                    else Icon(Icons.Default.Search, null)
                },
                trailingIcon = {
                    if (queryCategoria.isNotEmpty()) {
                        IconButton(onClick = { alCambiarQueryCategoria(""); alAlternarMenuCategorias(false) }) {
                            Icon(Icons.Default.Close, null, tint = Color.Gray)
                        }
                    } else {
                        Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SharedPalette.AcidGreen,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                )
            )

            // 🔥 [ELITE]: Resultados Integrados (Sin Popups) para evitar problemas de foco
            AnimatedVisibility(
                visible = menuCategoriasExpandido && categoriasFiltradas.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F14)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        categoriasFiltradas.take(15).forEach { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        alSeleccionarCategoria(cat)
                                        alCambiarQueryCategoria("")
                                        alAlternarMenuCategorias(false)
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cat.icono, fontSize = 18.sp)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    TextCompacto(cat.nombre, fontWeight = FontWeight.Bold, color = Color.White)
                                    cat.descripcion?.let { 
                                        TextCompacto(it, fontSize = 10.sp, color = Color.Gray, maxLines = 1)
                                    }
                                }
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.03f))
                        }
                    }
                }
            }
            
            if (idCategoriaSeleccionada.isNotEmpty() && !menuCategoriasExpandido) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SharedPalette.AcidGreen.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, SharedPalette.AcidGreen.copy(alpha = 0.2f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        TextCompacto(nombreCategoria.uppercase(), color = SharedPalette.AcidGreen, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        TextCompacto(descripcionCategoria, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SeccionDetalleLicitacion(
    titulo: String,
    alCambiarTitulo: (String) -> Unit,
    descripcion: String,
    alCambiarDescripcion: (String) -> Unit,
    urisDeImagenes: List<String>,
    alAgregarImagen: () -> Unit,
    alEliminarImagen: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextCompacto(
            text = "PASO 2: ESPECIFICACIONES TÉCNICAS",
            color = SharedPalette.ElectricCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )

        TacticalTextField(
            valor = titulo,
            alCambiarValor = alCambiarTitulo,
            etiqueta = "Título de la contratación",
            icono = Icons.Default.Title,
            pista = "Ej: Remodelación integral de cocina"
        )

        TacticalTextField(
            valor = descripcion,
            alCambiarValor = alCambiarDescripcion,
            etiqueta = "Alcance y detalles del problema",
            icono = Icons.Default.Description,
            pista = "Sé lo más descriptivo posible (medidas, materiales, urgencia...)",
            lineaUnica = false,
            modificador = Modifier.height(140.dp)
        )

        // Imágenes
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TextCompacto(
                text = "MULTIMEDIA Y PLANOS (MÁX 6)",
                color = Color.Gray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .border(1.dp, SharedPalette.AcidGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { alAgregarImagen() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, null, tint = SharedPalette.AcidGreen, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.height(4.dp))
                            TextCompacto("ADJUNTAR", color = SharedPalette.AcidGreen, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
                items(urisDeImagenes) { uri ->
                    Box(modifier = Modifier.size(80.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { alEliminarImagen(uri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(22.dp)
                                .offset(x = 4.dp, y = (-4).dp)
                                .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionRequisitosYPlazos(
    exigeVisita: Boolean,
    alCambiarExigeVisita: (Boolean) -> Unit,
    exigeGarantia: Boolean,
    alCambiarExigeGarantia: (Boolean) -> Unit,
    exigeMetodoPago: Boolean,
    alCambiarExigeMetodoPago: (Boolean) -> Unit,
    exigeDocPrestador: Boolean,
    alCambiarExigeDocPrestador: (Boolean) -> Unit,
    duracionDias: Int,
    alCambiarDuracion: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextCompacto(
            text = "PASO 3: CLÁUSULAS Y TIEMPOS",
            color = SharedPalette.ElectricCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ClausulaSwitch(icono = Icons.Default.Visibility, etiqueta = "Exigir Visita Técnica", marcado = exigeVisita, alCambiarMarcado = alCambiarExigeVisita)
            ClausulaSwitch(icono = Icons.Default.Security, etiqueta = "Exigir Garantía Escrita", marcado = exigeGarantia, alCambiarMarcado = alCambiarExigeGarantia)
            ClausulaSwitch(icono = Icons.Default.Payments, etiqueta = "Exigir Detalle de Pago", marcado = exigeMetodoPago, alCambiarMarcado = alCambiarExigeMetodoPago)
            ClausulaSwitch(icono = Icons.Default.VerifiedUser, etiqueta = "Exigir ART / Seguro", marcado = exigeDocPrestador, alCambiarMarcado = alCambiarExigeDocPrestador)
        }

        // Duración
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TextCompacto("TIEMPO LÍMITE PARA RECIBIR OFERTAS", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Black)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(3, 7, 14, 30).forEach { dias ->
                    FilterChip(
                        selected = duracionDias == dias,
                        onClick = { alCambiarDuracion(dias) },
                        label = { TextCompacto("$dias Días") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SharedPalette.ElectricCyan.copy(alpha = 0.2f),
                            selectedLabelColor = SharedPalette.ElectricCyan
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewSeccionesArmador() {
    PBEMTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            SeccionSolicitante(
                estadoCuenta = null,
                todasLasCategorias = listOf(CategoriaEntity(id = "1", nombre = "Plomería", icono = "🪠", idSuperCategoria = "HOGAR", descripcion = "Instalaciones de agua")),
                idCategoriaSeleccionada = "1",
                nombreCategoria = "Plomería",
                iconoCategoria = "🪠",
                descripcionCategoria = "Instalaciones de agua",
                alSeleccionarCategoria = { },
                queryCategoria = "",
                alCambiarQueryCategoria = {},
                menuCategoriasExpandido = false,
                alAlternarMenuCategorias = {},
                idPerfilSeleccionado = null,
                alCambiarPerfil = { _, _ -> },
                mostrarMenuPerfil = false,
                alAlternarMenuPerfil = {},
                direccionSeleccionada = null,
                alSeleccionarDireccion = { },
                mostrarMenuUbicacion = false,
                alAlternarMenuUbicacion = {},
                estaGpsActivo = false,
                alAlternarGps = {},
                esDireccionManual = false,
                alActivarDireccionManual = { },
                calleManual = "",
                alCambiarCalle = { },
                numeroManual = "",
                alCambiarNumero = { },
                ciudadManual = "",
                alCambiarCiudad = { },
                cpManual = "",
                alCambiarCp = { }
            )
        }
    }
}
