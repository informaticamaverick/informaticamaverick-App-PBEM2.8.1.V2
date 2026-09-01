package com.example.myapplication.prestador.ui.pantallas.catalogo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.prestador.datos.local.entidades.ProductoEntity
import com.example.myapplication.core.datos.local.entidades.TipoProducto
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import com.example.myapplication.prestador.viewmodel.presupuesto.ProductoViewModel
import java.io.File
import java.util.UUID
import java.util.Locale
import kotlinx.coroutines.launch

private object CatalogoEliteTheme {
    val BackgroundDark = Color(0xFF030712)
    val SurfaceCard = Color(0xEA0F172A)
    val SurfaceCardSolid = Color(0xFF0F172A)
    val SurfaceInput = Color(0xFF020617)
    val BorderGlass = Color(0x1AFFFFFF)

    val BrandOrange = Color(0xFFF97316)
    val BrandOrangeLight = Color(0xFFFB923C)

    val AccentCyan = Color(0xFF06B6D4)
    val AccentEmerald = Color(0xFF10B981)

    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)
}

/**
 * --- PANTALLA DE CATÁLOGO (V2026.FINAL) ---
 * [ELITE]: Gestión premium de productos y servicios para el prestador.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val catalogoArticulos by viewModel.catalogoArticulos.collectAsStateWithLifecycle()
    val catalogoServicios by viewModel.catalogoServicios.collectAsStateWithLifecycle()
    val catalogoGastos by viewModel.catalogoGastos.collectAsStateWithLifecycle()
    val busqueda by viewModel.busqueda.collectAsStateWithLifecycle()
    val categoriasVigentes by viewModel.categoriasVigentes.collectAsStateWithLifecycle()
    val mapaCategorias by viewModel.mapaCategorias.collectAsStateWithLifecycle()
    
    var filtroActivo by remember { mutableStateOf(TipoProducto.PRODUCTO) }
    var mostrarDialogoEdicion by remember { mutableStateOf<ProductoEntity?>(null) }
    var mostrarDialogoCreacion by remember { mutableStateOf(false) }
    var itemAEliminar by remember { mutableStateOf<ProductoEntity?>(null) }

    // --- ESTADOS DE IMAGEN TEMPORAL ---
    var imagenTemporalPath by remember { mutableStateOf<String?>(null) }
    var miniaturaTemporalBase64 by remember { mutableStateOf<String?>(null) }
    var uriCamara by remember { mutableStateOf<Uri?>(null) }

    val lanzadorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            scope.launch {
                val result = viewModel.procesarImagenCatalogo(context, it)
                imagenTemporalPath = result.first
                miniaturaTemporalBase64 = result.second
            }
        }
    }

    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito) {
            uriCamara?.let { uri ->
                scope.launch {
                    val result = viewModel.procesarImagenCatalogo(context, uri)
                    imagenTemporalPath = result.first
                    miniaturaTemporalBase64 = result.second
                }
            }
        }
    }

    val lanzadorPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) {
            val file = File(context.cacheDir, "catalogo_cam_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            uriCamara = uri
            lanzadorCamara.launch(uri)
        }
    }

    val alCapturarImagen: () -> Unit = {
        val tienePermiso = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (tienePermiso) {
            val file = File(context.cacheDir, "catalogo_cam_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            uriCamara = uri
            lanzadorCamara.launch(uri)
        } else {
            lanzadorPermiso.launch(android.Manifest.permission.CAMERA)
        }
    }


    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("MI CATÁLOGO", fontWeight = FontWeight.Black, letterSpacing = 1.sp, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CatalogoEliteTheme.BackgroundDark,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    imagenTemporalPath = null
                    miniaturaTemporalBase64 = null
                    mostrarDialogoCreacion = true 
                },
                containerColor = CatalogoEliteTheme.BrandOrange,
                contentColor = Color.Black,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, null)
            }
        },
        containerColor = CatalogoEliteTheme.BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Buscador Premium (Estilo Elite Box)
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { viewModel.actualizarBusqueda(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar por nombre o SKU...", color = CatalogoEliteTheme.TextMuted, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = CatalogoEliteTheme.BrandOrange, modifier = Modifier.size(18.dp)) },
                    shape = RoundedCornerShape(8.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CatalogoEliteTheme.BrandOrange,
                        unfocusedBorderColor = CatalogoEliteTheme.BorderGlass,
                        focusedContainerColor = CatalogoEliteTheme.SurfaceInput,
                        unfocusedContainerColor = CatalogoEliteTheme.SurfaceInput
                    ),
                    singleLine = true
                )
            }

            // Tarjetas de Filtro (Elite Tabs - Straight Style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TarjetaFiltroCatalogo(
                    titulo = "PRODUCTOS",
                    icono = Icons.Default.Inventory2,
                    activo = filtroActivo == TipoProducto.PRODUCTO,
                    conteo = catalogoArticulos.size,
                    onClick = { filtroActivo = TipoProducto.PRODUCTO },
                    modifier = Modifier.weight(1f)
                )
                TarjetaFiltroCatalogo(
                    titulo = "SERVICIOS",
                    icono = Icons.Default.Handyman,
                    activo = filtroActivo == TipoProducto.SERVICIO,
                    conteo = catalogoServicios.size,
                    onClick = { filtroActivo = TipoProducto.SERVICIO },
                    modifier = Modifier.weight(1f)
                )
                TarjetaFiltroCatalogo(
                    titulo = "GASTOS",
                    icono = Icons.Default.LocalShipping,
                    activo = filtroActivo == TipoProducto.GASTO,
                    conteo = catalogoGastos.size,
                    onClick = { filtroActivo = TipoProducto.GASTO },
                    modifier = Modifier.weight(1f)
                )
            }

            // --- LEYENDA INFORMATIVA ---
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                color = CatalogoEliteTheme.BrandOrange.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, CatalogoEliteTheme.BrandOrange.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = CatalogoEliteTheme.BrandOrange, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = when(filtroActivo) {
                            TipoProducto.PRODUCTO -> "Carga aquí materiales, repuestos o insumos físicos."
                            TipoProducto.SERVICIO -> "Carga aquí mano de obra, mantenimiento, honorarios o consultoría."
                            TipoProducto.GASTO -> "Carga aquí logística, movilidad, viáticos o envíos."
                        },
                        fontSize = 10.sp,
                        color = CatalogoEliteTheme.TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            val itemsAMostrar = when(filtroActivo) {
                TipoProducto.PRODUCTO -> catalogoArticulos
                TipoProducto.SERVICIO -> catalogoServicios
                TipoProducto.GASTO -> catalogoGastos
            }

            if (itemsAMostrar.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (busqueda.isBlank()) "Tu catálogo está vacío" else "Sin coincidencias", 
                        color = CatalogoEliteTheme.TextMuted,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(itemsAMostrar, key = { it.id }) { itemL ->
                        ItemProductoCatalogo(
                            producto = itemL,
                            mapaCategorias = mapaCategorias,
                            onClick = { 
                                imagenTemporalPath = itemL.urlImagen
                                miniaturaTemporalBase64 = itemL.miniaturaBase64
                                mostrarDialogoEdicion = itemL 
                            },
                            onDelete = { itemAEliminar = itemL }
                        )
                    }
                }
            }
        }
    }

    // --- DIÁLOGO DE CONFIRMACIÓN DE ELIMINACIÓN ---
    if (itemAEliminar != null) {
        AlertDialog(
            onDismissRequest = { itemAEliminar = null },
            icon = { Icon(Icons.Default.DeleteForever, null, tint = Color.Red.copy(alpha = 0.8f)) },
            title = { 
                Text(
                    text = "¿ELIMINAR ÍTEM?", 
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color.White
                ) 
            },
            text = { 
                Text(
                    "Se borrará '${itemAEliminar!!.nombre}' permanentemente del catálogo. Esta acción no se puede deshacer.",
                    color = CatalogoEliteTheme.TextSecondary,
                    fontSize = 13.sp
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarProducto(itemAEliminar!!)
                        itemAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("BORRAR", fontWeight = FontWeight.Black, color = Color.Red, fontSize = 12.sp) }
            },
            dismissButton = {
                TextButton(onClick = { itemAEliminar = null }) {
                    Text("CANCELAR", color = CatalogoEliteTheme.TextMuted, fontSize = 12.sp)
                }
            },
            containerColor = CatalogoEliteTheme.SurfaceCardSolid,
            shape = RoundedCornerShape(12.dp)
        )
    }

    if (mostrarDialogoEdicion != null) {
        DialogoProducto(
            item = mostrarDialogoEdicion!!,
            categoriasVigentes = categoriasVigentes,
            mapaCategorias = mapaCategorias,
            imagenActual = imagenTemporalPath,
            alCapturar = alCapturarImagen,
            alSeleccionarGaleria = { lanzadorGaleria.launch("image/*") },
            onDismiss = { mostrarDialogoEdicion = null },
            validarSku = { sku -> viewModel.validarSkuUnico(sku, mostrarDialogoEdicion?.id) },
            onConfirm = { p ->
                viewModel.guardarProducto(p.copy(
                    urlImagen = imagenTemporalPath,
                    miniaturaBase64 = miniaturaTemporalBase64
                ))
                mostrarDialogoEdicion = null
            }
        )
    }

    if (mostrarDialogoCreacion) {
        DialogoProducto(
            item = null,
            tipoPredeterminado = filtroActivo,
            categoriasVigentes = categoriasVigentes,
            mapaCategorias = mapaCategorias,
            imagenActual = imagenTemporalPath,
            alCapturar = alCapturarImagen,
            alSeleccionarGaleria = { lanzadorGaleria.launch("image/*") },
            onDismiss = { mostrarDialogoCreacion = false },
            validarSku = { sku -> viewModel.validarSkuUnico(sku, null) },
            onConfirm = { p ->
                viewModel.guardarProducto(p.copy(
                    urlImagen = imagenTemporalPath,
                    miniaturaBase64 = miniaturaTemporalBase64
                ))
                mostrarDialogoCreacion = false
            }
        )
    }
}

@Composable
private fun TarjetaFiltroCatalogo(
    titulo: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    activo: Boolean,
    conteo: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorAcento = if (activo) CatalogoEliteTheme.BrandOrange else CatalogoEliteTheme.TextMuted
    val colorFondo = if (activo) CatalogoEliteTheme.BrandOrange.copy(alpha = 0.12f) else CatalogoEliteTheme.SurfaceInput
    val colorBorde = if (activo) CatalogoEliteTheme.BrandOrange.copy(alpha = 0.5f) else CatalogoEliteTheme.BorderGlass

    Surface(
        onClick = onClick,
        modifier = modifier.height(58.dp),
        color = colorFondo,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, colorBorde)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icono, null, tint = colorAcento, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = titulo, 
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Black, 
                    color = if (activo) Color.White else CatalogoEliteTheme.TextSecondary
                )
                Text(
                    text = "$conteo ÍTEMS", 
                    fontSize = 8.sp, 
                    fontWeight = FontWeight.Bold,
                    color = colorAcento.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ItemProductoCatalogo(
    producto: ProductoEntity,
    mapaCategorias: Map<String, CategoriaEntity>,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoria = remember(producto.idCategoria, mapaCategorias) { mapaCategorias[producto.idCategoria] }
    
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = CatalogoEliteTheme.SurfaceCard,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, CatalogoEliteTheme.BorderGlass)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(6.dp),
                color = CatalogoEliteTheme.SurfaceInput,
                border = BorderStroke(1.dp, CatalogoEliteTheme.BorderGlass)
            ) {
                if (producto.urlImagen != null) {
                    AsyncImage(model = producto.urlImagen, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (producto.tipo == TipoProducto.SERVICIO) Icons.Default.Handyman else Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = CatalogoEliteTheme.TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = producto.nombre, 
                    fontWeight = FontWeight.Bold, 
                    color = Color.White, 
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${categoria?.icono ?: "📂"} ${categoria?.nombre ?: producto.idCategoria}",
                        color = CatalogoEliteTheme.AccentCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (producto.tipo == TipoProducto.PRODUCTO) {
                        Spacer(Modifier.width(6.dp))
                        Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(CatalogoEliteTheme.TextMuted))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "STOCK: ${producto.stockActual}",
                            color = if (producto.stockActual > 0) CatalogoEliteTheme.AccentEmerald else Color.Red,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val locStr = Locale.getDefault()
                val precioFormateado = String.format(locStr, "%,.0f", producto.precioVenta)
                Text(
                    text = "$ $precioFormateado", 
                    fontWeight = FontWeight.Black, 
                    color = CatalogoEliteTheme.BrandOrange, 
                    fontSize = 15.sp
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp).offset(x = 4.dp)) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoProducto(
    item: ProductoEntity?,
    tipoPredeterminado: TipoProducto = TipoProducto.PRODUCTO,
    categoriasVigentes: List<CategoriaEntity>,
    mapaCategorias: Map<String, CategoriaEntity>,
    imagenActual: String? = null,
    alCapturar: () -> Unit,
    alSeleccionarGaleria: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (ProductoEntity) -> Unit,
    validarSku: (String) -> Boolean = { true },
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // --- ESTADOS LOCALES ---
    var nombre by remember { mutableStateOf(item?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(item?.descripcion ?: "") }
    var precioVenta by remember { mutableStateOf(item?.precioVenta?.toInt()?.toString() ?: "0") }
    var precioCosto by remember { mutableStateOf(item?.precioCosto?.toInt()?.toString() ?: "0") }
    var sku by remember { mutableStateOf(item?.sku ?: "") }
    var stock by remember { mutableStateOf(item?.stockActual?.toString() ?: "0") }
    var idCategoria by remember { mutableStateOf(item?.idCategoria ?: if (categoriasVigentes.isNotEmpty()) categoriasVigentes.first().id else "GENERAL") }
    var tipo by remember { mutableStateOf(item?.tipo ?: tipoPredeterminado) }

    // --- DESCUENTOS E INTERESES DEFAULT ---
    var porcentajeDescuento by remember { mutableStateOf(item?.descuentoDefault?.toString() ?: "0") }
    var montoDescuentoFijo by remember { mutableStateOf("0") }
    var porcentajeInteres by remember { mutableStateOf(item?.interesDefault?.toString() ?: "0") }
    var montoInteresFijo by remember { mutableStateOf("0") }

    var expandedCats by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }
    val skuEsValido = remember(sku) { validarSku(sku) }
    val categoriaSeleccionada = remember(idCategoria, mapaCategorias) { mapaCategorias[idCategoria] }

    // Sincronización inicial
    LaunchedEffect(item, precioVenta) {
        val pVenta = precioVenta.toDoubleOrNull() ?: 0.0
        if (pVenta > 0) {
            val pDesc = item?.descuentoDefault ?: 0.0
            montoDescuentoFijo = (pVenta * pDesc / 100).toInt().toString()
            val pInt = item?.interesDefault ?: 0.0
            montoInteresFijo = (pVenta * pInt / 100).toInt().toString()
        }
    }

    fun syncDescuentoDesdeMonto(monto: String) {
        montoDescuentoFijo = monto
        val pVenta = precioVenta.toDoubleOrNull() ?: 0.0
        if (pVenta > 0) {
            val porc = (monto.toDoubleOrNull() ?: 0.0) * 100 / pVenta
            porcentajeDescuento = if (porc % 1.0 == 0.0) porc.toInt().toString() else String.format(Locale.getDefault(), "%.1f", porc)
        }
    }

    fun syncDescuentoDesdePorcentaje(porc: String) {
        porcentajeDescuento = porc
        val pVenta = precioVenta.toDoubleOrNull() ?: 0.0
        val monto = pVenta * (porc.toDoubleOrNull() ?: 0.0) / 100
        montoDescuentoFijo = if (monto % 1.0 == 0.0) monto.toInt().toString() else String.format(Locale.getDefault(), "%.1f", monto)
    }

    fun syncInteresDesdeMonto(monto: String) {
        montoInteresFijo = monto
        val pVenta = precioVenta.toDoubleOrNull() ?: 0.0
        if (pVenta > 0) {
            val porc = (monto.toDoubleOrNull() ?: 0.0) * 100 / pVenta
            porcentajeInteres = if (porc % 1.0 == 0.0) porc.toInt().toString() else String.format(Locale.getDefault(), "%.1f", porc)
        }
    }

    fun syncInteresDesdePorcentaje(porc: String) {
        porcentajeInteres = porc
        val pVenta = precioVenta.toDoubleOrNull() ?: 0.0
        val monto = pVenta * (porc.toDoubleOrNull() ?: 0.0) / 100
        montoInteresFijo = if (monto % 1.0 == 0.0) monto.toInt().toString() else String.format(Locale.getDefault(), "%.1f", monto)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CatalogoEliteTheme.SurfaceCardSolid,
        dragHandle = { BottomSheetDefaults.DragHandle(color = CatalogoEliteTheme.TextMuted) },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
                .imePadding() // 🔥 [FIX]: Empujar contenido hacia arriba con el teclado
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (item == null) "Nuevo Ítem" else "Editar Ítem",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Red)
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // --- MULTIMEDIA SECCION ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(90.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = CatalogoEliteTheme.SurfaceInput,
                    border = BorderStroke(1.dp, CatalogoEliteTheme.BorderGlass)
                ) {
                    if (imagenActual != null) {
                        AsyncImage(model = imagenActual, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AddAPhoto, null, tint = CatalogoEliteTheme.TextMuted, modifier = Modifier.size(32.dp))
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = alCapturar,
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("TOMAR FOTO", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                    Button(
                        onClick = alSeleccionarGaleria,
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("GALERÍA", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            // Fila 1: SKU y Tipo
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it.uppercase() },
                    label = { Text("Código / Ref.", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = !skuEsValido,
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedTextFieldColorsElite()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = !expandedType },
                    modifier = Modifier.weight(1.3f)
                ) {
                    OutlinedTextField(
                        value = when (tipo) {
                            TipoProducto.PRODUCTO -> "Producto / Ítem"
                            TipoProducto.SERVICIO -> "Servicio"
                            TipoProducto.GASTO -> "Gasto Vario"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo *", color = Color.Gray) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true),
                        shape = RoundedCornerShape(12.dp),
                        colors = outlinedTextFieldColorsElite()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false },
                        modifier = Modifier.background(CatalogoEliteTheme.SurfaceCardSolid)
                    ) {
                        TipoProducto.entries.forEach { t ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (t) {
                                            TipoProducto.PRODUCTO -> "Producto / Ítem"
                                            TipoProducto.SERVICIO -> "Servicio"
                                            TipoProducto.GASTO -> "Gasto Vario"
                                        },
                                        color = Color.White
                                    )
                                },
                                onClick = { tipo = t; expandedType = false }
                            )
                        }
                    }
                }
            }

            // Fila 2: Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Concepto *", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = outlinedTextFieldColorsElite()
            )

            // Fila 3: Descripción
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Detalles para el Cliente", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                shape = RoundedCornerShape(12.dp),
                colors = outlinedTextFieldColorsElite()
            )

            // Fila 4: Stock y Precio Costo
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = if (tipo == TipoProducto.PRODUCTO) stock else "N/A",
                    onValueChange = { stock = it },
                    label = { Text("Stock Actual", color = Color.Gray) },
                    enabled = tipo == TipoProducto.PRODUCTO,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedTextFieldColorsElite()
                )

                OutlinedTextField(
                    value = precioCosto,
                    onValueChange = { precioCosto = it },
                    label = { Text("Costo Unit. ($)", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedTextFieldColorsElite()
                )
            }

            // Fila 5: Descuentos
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = montoDescuentoFijo,
                    onValueChange = { syncDescuentoDesdeMonto(it) },
                    label = { Text("Desc. Fijo ($)", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedTextFieldColorsElite()
                )
                OutlinedTextField(
                    value = porcentajeDescuento,
                    onValueChange = { syncDescuentoDesdePorcentaje(it) },
                    label = { Text("Desc. %", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedTextFieldColorsElite()
                )
            }

            // Fila 6: Intereses
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = montoInteresFijo,
                    onValueChange = { syncInteresDesdeMonto(it) },
                    label = { Text("Recargo Fijo ($)", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedTextFieldColorsElite(accent = Color(0xFF8B5CF6))
                )
                OutlinedTextField(
                    value = porcentajeInteres,
                    onValueChange = { syncInteresDesdePorcentaje(it) },
                    label = { Text("Recargo %", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedTextFieldColorsElite(accent = Color(0xFF8B5CF6))
                )
            }

            // Fila 7: Categoría y Precio Venta
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                ExposedDropdownMenuBox(
                    expanded = expandedCats,
                    onExpandedChange = { expandedCats = !expandedCats },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "${categoriaSeleccionada?.icono ?: ""} ${categoriaSeleccionada?.nombre ?: idCategoria}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rubro", color = Color.Gray) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCats) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true),
                        shape = RoundedCornerShape(12.dp),
                        colors = outlinedTextFieldColorsElite()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCats,
                        onDismissRequest = { expandedCats = false },
                        modifier = Modifier.background(CatalogoEliteTheme.SurfaceCardSolid)
                    ) {
                        categoriasVigentes.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.icono} ${cat.nombre}", fontSize = 14.sp, color = Color.White) },
                                onClick = { idCategoria = cat.id; expandedCats = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = precioVenta,
                    onValueChange = { precioVenta = it },
                    label = { Text("Precio Venta ($)", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedTextFieldColorsElite(accent = CatalogoEliteTheme.AccentEmerald)
                )
            }

            if (!skuEsValido) {
                Text("⚠️ El SKU ya existe en tu catálogo", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))

            // Botones de acción (Cancelar | Guardar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Text("Cancelar", color = Color.White)
                }

                Button(
                    onClick = {
                        val base = item ?: ProductoEntity(
                            id = UUID.randomUUID().toString(),
                            idPropietario = "",
                            nombre = nombre,
                            tipo = tipo
                        )
                        val finalProd = base.copy(
                            nombre = nombre,
                            descripcion = descripcion,
                            precioVenta = precioVenta.toDoubleOrNull() ?: 0.0,
                            precioCosto = precioCosto.toDoubleOrNull() ?: 0.0,
                            sku = sku,
                            stockActual = if (tipo == TipoProducto.PRODUCTO) (stock.toIntOrNull() ?: 0) else 0,
                            idCategoria = idCategoria,
                            tipo = tipo,
                            descuentoDefault = porcentajeDescuento.toDoubleOrNull() ?: 0.0,
                            interesDefault = porcentajeInteres.toDoubleOrNull() ?: 0.0
                        )
                        onConfirm(finalProd)
                    },
                    enabled = nombre.isNotBlank() && precioVenta.isNotBlank() && skuEsValido && sku.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CatalogoEliteTheme.BrandOrange,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.5f).height(50.dp)
                ) {
                    Text(
                        text = if (item == null) "CREAR ÍTEM" else "GUARDAR CAMBIOS",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun outlinedTextFieldColorsElite(accent: Color = CatalogoEliteTheme.BrandOrange) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = accent,
    focusedBorderColor = accent,
    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
    focusedLabelColor = accent,
    unfocusedLabelColor = Color.Gray,
    disabledTextColor = Color.Gray,
    disabledBorderColor = Color.White.copy(alpha = 0.1f)
)

