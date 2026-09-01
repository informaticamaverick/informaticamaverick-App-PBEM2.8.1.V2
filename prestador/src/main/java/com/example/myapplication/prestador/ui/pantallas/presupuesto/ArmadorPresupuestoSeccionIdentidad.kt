package com.example.myapplication.prestador.ui.pantallas.presupuesto

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.core.datos.local.entidades.*
import androidx.compose.ui.text.TextStyle
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.uishared.ui.components.AutoSizeText
import com.example.myapplication.uishared.ui.components.BloquearEscaladoFuente
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.text.ClickableText
import com.example.myapplication.prestador.datos.local.entidades.BorradorPresupuestoEntity

@Composable
fun TarjetaDetallesPresupuesto(
    numero: String,
    fecha: Long,
    idCategoria: String,
    nombreCategoria: String? = null,
    iconoCategoria: String? = null,
    sugerenciasCategorias: List<String>,
    onCategoriaChange: (String) -> Unit
) {
    val fechaLegible = remember(fecha) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(fecha))
    }

    val displayCategoria = if (!nombreCategoria.isNullOrBlank()) {
        if (!iconoCategoria.isNullOrBlank()) "$iconoCategoria $nombreCategoria" else nombreCategoria
    } else idCategoria

    TarjetaBentoContenedor {
        Text(
            text = "DETALLES DEL PRESUPUESTO",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = ArmadorPresupuestoTema.BrandOrange,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Nro. Documento", fontSize = 9.sp, color = ArmadorPresupuestoTema.TextSecondary)
                Text("#$numero", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Fecha de Emisión", fontSize = 9.sp, color = ArmadorPresupuestoTema.TextSecondary)
                Text(fechaLegible, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SelectorDropdownBasico(
            etiqueta = "Categoría del Trabajo / Servicio",
            opcionSeleccionada = displayCategoria.ifBlank { "Seleccionar Categoría..." },
            opciones = sugerenciasCategorias,
            onSeleccionar = onCategoriaChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaEmisorMobile(
    perfilActual: PrestadorDominio?,
    misIdentidades: List<PrestadorDominio>,
    onSeleccionar: (String) -> Unit
) {
    var mostrarMenuIdentidades by remember { mutableStateOf(false) }

    TarjetaBentoContenedor {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EMISOR (TU PERFIL)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = ArmadorPresupuestoTema.BrandOrange,
                letterSpacing = 0.5.sp
            )
            
            TextButton(
                onClick = { mostrarMenuIdentidades = true },
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.SwitchAccount, null, tint = ArmadorPresupuestoTema.BrandOrangeLight, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text("Cambiar", fontSize = 11.sp, color = ArmadorPresupuestoTema.BrandOrangeLight, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.Top) {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(4.dp),
                color = ArmadorPresupuestoTema.SurfaceInput,
                border = BorderStroke(1.dp, ArmadorPresupuestoTema.BorderGlass)
            ) {
                val imagen = perfilActual?.urlMiniatura ?: perfilActual?.urlFoto
                if (imagen != null) {
                    AsyncImage(
                        model = imagen,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = perfilActual?.titulo?.take(1)?.uppercase() ?: "M",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = ArmadorPresupuestoTema.BrandOrange
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) { // 🔥 LEY 4 OJOS
                AutoSizeText(
                    text = perfilActual?.titulo ?: "Maverick Technical Solutions",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    maxLines = 1
                )
                
                if (perfilActual?.cuitCuil != null) {
                    Text(
                        text = "CUIT/CUIL: ${perfilActual.cuitCuil}",
                        fontSize = 10.sp,
                        color = ArmadorPresupuestoTema.TextSecondary
                    )
                }

                val direccion = perfilActual?.direccionVisible ?: perfilActual?.direcciones?.firstOrNull()?.let { "${it.calle} ${it.numero}" }
                if (direccion != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(Icons.Default.LocationOn, null, tint = ArmadorPresupuestoTema.TextMuted, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(text = direccion, fontSize = 10.sp, color = ArmadorPresupuestoTema.TextMuted)
                    }
                }
            }
        }

        if (mostrarMenuIdentidades) {
            ModalBottomSheet(
                onDismissRequest = { mostrarMenuIdentidades = false },
                containerColor = ArmadorPresupuestoTema.SurfaceCardSolid
            ) {
                Column(modifier = Modifier.padding(16.dp).padding(bottom = 24.dp)) {
                    Text("Selecciona el perfil emisor", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Spacer(Modifier.height(12.dp))
                    misIdentidades.forEach { ident ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSeleccionar(ident.id)
                                    mostrarMenuIdentidades = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = ArmadorPresupuestoTema.SurfaceInput) {
                                AsyncImage(model = ident.urlMiniatura ?: ident.urlFoto, contentDescription = null, contentScale = ContentScale.Crop)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(ident.titulo, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(ident.tipo.name, fontSize = 10.sp, color = ArmadorPresupuestoTema.TextMuted)
                            }
                            if (ident.id == perfilActual?.id) {
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Default.CheckCircle, null, tint = ArmadorPresupuestoTema.BrandOrange)
                            }
                        }
                        HorizontalDivider(color = ArmadorPresupuestoTema.BorderGlass)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaClienteMobile(
    borrador: BorradorPresupuestoEntity,
    datosCliente: com.example.myapplication.core.dominio.modelos.UsuarioDominio?,
    idDireccionSeleccionada: String?,
    direccionManual: String?,
    direcciones: List<com.example.myapplication.core.dominio.modelos.DireccionDominio>,
    onSeleccionarDireccion: (String) -> Unit,
    onActualizarDireccionManual: (String?, String?, String?, String?, String?, String?, String?) -> Unit,
    onCambiarCliente: () -> Unit,
    onManualAddressClick: () -> Unit
) {
    TarjetaBentoContenedor {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CLIENTE DESTINATARIO",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = ArmadorPresupuestoTema.AccentCyan,
                letterSpacing = 0.5.sp
            )
            TextButton(
                onClick = onCambiarCliente,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = ArmadorPresupuestoTema.BrandOrangeLight,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Cambiar",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArmadorPresupuestoTema.BrandOrangeLight
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp), // 🔥 LEY #10: Esquinas rectas
            color = ArmadorPresupuestoTema.SurfaceInput,
            border = BorderStroke(1.dp, ArmadorPresupuestoTema.BorderGlass)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(4.dp)) // 🔥 LEY #10
                        .background(ArmadorPresupuestoTema.AccentCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (datosCliente?.urlMiniatura != null || datosCliente?.urlFoto != null) {
                        AsyncImage(
                            model = datosCliente.urlMiniatura ?: datosCliente.urlFoto,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = datosCliente?.nombreVisible?.take(2)?.uppercase() ?: "EA",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White // 🔥 Textos blancos
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) { // 🔥 LEY 4 OJOS
                    Text( // Cambiado AutoSizeText por Text para mayor claridad si fallaba la distinción
                        text = datosCliente?.nombreVisible ?: "Cliente Maverick",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White // 🔥 Textos blancos
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "ID: ${datosCliente?.id?.takeLast(8)?.uppercase() ?: "---"}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.7f) // 🔥 Más distinguible
                    )
                    
                    if (datosCliente?.cuitCuil != null) {
                        Text(
                            text = "CUIT/CUIL: ${datosCliente.cuitCuil}",
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.5f) // 🔥 Más distinguible
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        
        Text("Ubicación del trabajo", fontSize = 11.sp, fontWeight = FontWeight.Black, color = ArmadorPresupuestoTema.TextSecondary)
        Spacer(Modifier.height(6.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp), // 🔥 LEY #10
            color = ArmadorPresupuestoTema.SurfaceInput,
            border = BorderStroke(1.dp, ArmadorPresupuestoTema.BorderGlass)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = ArmadorPresupuestoTema.AccentCyan, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    
                    val direccionTexto = if (!direccionManual.isNullOrBlank()) direccionManual
                        else direcciones.find { it.id == idDireccionSeleccionada }?.let { 
                            "${it.calle} ${it.numero}, ${it.localidad}"
                        } ?: "Sin ubicación asignada"
                    
                    Text(text = direccionTexto, fontSize = 12.sp, color = Color.White, modifier = Modifier.weight(1f))

                    IconButton(onClick = onManualAddressClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, null, tint = ArmadorPresupuestoTema.TextMuted, modifier = Modifier.size(14.dp))
                    }
                }
                
                if (direcciones.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    
                    val opcionesDirecciones = direcciones.map { "${it.calle} ${it.numero}, ${it.localidad}" }
                    val direccionActual = direcciones.find { it.id == idDireccionSeleccionada }?.let { "${it.calle} ${it.numero}, ${it.localidad}" } ?: "Seleccionar dirección..."

                    SelectorDropdownBasico(
                        etiqueta = "Direcciones del cliente",
                        opcionSeleccionada = direccionActual,
                        opciones = opcionesDirecciones,
                        onSeleccionar = { texto ->
                            val id = direcciones.find { it.aTextoCompleto() == texto }?.id
                            if (id != null) onSeleccionarDireccion(id)
                        }
                    )
                }
                
                if (direcciones.isEmpty() && direccionManual.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    val annotatedLink = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color.Red.copy(alpha = 0.8f))) {
                            append("El cliente no mandó ninguna dirección todavía. ")
                        }
                        pushStringAnnotation(tag = "CARGAR", annotation = "cargar")
                        withStyle(style = SpanStyle(color = ArmadorPresupuestoTema.BrandOrange, textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Bold)) {
                            append("Deseas cargarla manualmente?")
                        }
                        pop()
                    }
                    ClickableText(
                        text = annotatedLink,
                        style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp),
                        onClick = { offset ->
                            annotatedLink.getStringAnnotations(tag = "CARGAR", start = offset, end = offset).firstOrNull()?.let {
                                onManualAddressClick()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TarjetaValidezOferta(
    validezOferta: Int,
    onValidezChange: (Int) -> Unit
) {
    TarjetaBentoContenedor {
        Text(
            text = "CONDICIONES COMERCIALES",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = ArmadorPresupuestoTema.AccentAmber,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        SelectorDropdownBasico(
            etiqueta = "Validez de la oferta",
            opcionSeleccionada = "$validezOferta Días Corridos",
            opciones = listOf("7 Días", "15 Días", "30 Días", "60 Días"),
            onSeleccionar = { onValidezChange(it.filter { c -> c.isDigit() }.toIntOrNull() ?: 15) }
        )
    }
}
