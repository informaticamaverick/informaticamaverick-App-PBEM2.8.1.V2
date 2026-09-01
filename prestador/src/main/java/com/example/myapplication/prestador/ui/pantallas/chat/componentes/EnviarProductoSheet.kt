package com.example.myapplication.prestador.ui.pantallas.chat.componentes

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.prestador.datos.local.entidades.ProductoEntity
import com.example.myapplication.core.datos.local.entidades.TipoProducto
import com.example.myapplication.core.utilidades.CalculadoraFinanciera
import com.example.myapplication.prestador.viewmodel.chat.PrestadorChatViewModel
import com.example.myapplication.prestador.viewmodel.presupuesto.ProductoViewModel
import com.example.myapplication.uishared.ui.components.chat.ProductoChatBubble
import com.example.myapplication.core.dominio.modelos.ProductoMensajeDominio
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

// Paleta de colores Elite
private val ColorDarkBg = Color(0xFF0B0F17)
private val ColorCardBg = Color(0xFF131B2E)
private val ColorSurfaceBg = Color(0xFF1A233A)
private val ColorBorderDark = Color(0xFF283552)
private val ColorAccentOrange = Color(0xFFF97316)
private val ColorAccentEmerald = Color(0xFF10B981)
private val ColorAccentBlue = Color(0xFF3B82F6)
private val ColorTextMuted = Color(0xFF94A3B8)
private val ColorChatBg = Color(0xFF0B141A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnviarProductoSheet(
    idEmisor: String,
    idReceptor: String,
    onClose: () -> Unit = {},
    productoViewModel: ProductoViewModel = hiltViewModel(),
    chatViewModel: PrestadorChatViewModel = hiltViewModel()
) {
    val catalogo by productoViewModel.catalogoCompleto.collectAsStateWithLifecycle()
    val busqueda by productoViewModel.busqueda.collectAsStateWithLifecycle()

    var currentStep by remember { mutableIntStateOf(1) }
    var selectedProduct by remember { mutableStateOf<ProductoEntity?>(null) }

    // --- ESTADOS DE CONFIGURACIÓN ---
    var inputPriceText by remember { mutableStateOf("") }
    var discountPercent by remember { mutableStateOf("0") }
    var discountAmount by remember { mutableStateOf("0") }
    var isDiscountByPercent by remember { mutableStateOf(true) }

    var formaPagoSeleccionada by remember { mutableStateOf("EFECTIVO") } 
    val cuotasPredeterminadas = listOf(1, 3, 6, 9, 12, 18, 24)
    var selectedCuotasSet by remember { mutableStateOf(setOf(3)) }
    var isManualCuotas by remember { mutableStateOf(false) }
    var inputManualCuotas by remember { mutableStateOf("1") }
    var tasaInteresAnual by remember { mutableStateOf("45") }
    
    // --- ESTADOS DE ENVÍO ---
    var tipoEnvio by remember { mutableStateOf("CONVENIR") } // CONVENIR, COBRO_CLIENTE, GRATIS
    var inputCostoEnvioText by remember { mutableStateOf("0") }

    // --- LÓGICA DE CÁLCULO SSOT ---
    val baseSalePrice = inputPriceText.toDoubleOrNull() ?: (selectedProduct?.precioVenta ?: 0.0)
    
    val calculatedDiscount = if (isDiscountByPercent) {
        val p = discountPercent.toDoubleOrNull() ?: 0.0
        baseSalePrice * (p / 100.0)
    } else {
        discountAmount.toDoubleOrNull() ?: 0.0
    }

    val baseFinanciera = (baseSalePrice - calculatedDiscount).coerceAtLeast(0.0)
    val effectiveCuotas = if (isManualCuotas) (inputManualCuotas.toIntOrNull() ?: 1) else (selectedCuotasSet.maxOrNull() ?: 1)
    
    val tna = tasaInteresAnual.toDoubleOrNull() ?: 0.0
    val cuotaMensual = remember(baseFinanciera, effectiveCuotas, tna, formaPagoSeleccionada) {
        if (formaPagoSeleccionada == "TARJETA_INTERES") {
            CalculadoraFinanciera.calcularCuotaFrances(baseFinanciera, tna, effectiveCuotas)
        } else {
            baseFinanciera / effectiveCuotas
        }
    }

    val totalFinanciado = if (formaPagoSeleccionada == "TARJETA_INTERES") {
        cuotaMensual * effectiveCuotas
    } else baseFinanciera

    val extraEnvio = if (tipoEnvio == "COBRO_CLIENTE") (inputCostoEnvioText.toDoubleOrNull() ?: 0.0) else 0.0
    val finalPriceForClient = totalFinanciado + extraEnvio
    val realShippingCost = if (tipoEnvio != "CONVENIR") (inputCostoEnvioText.toDoubleOrNull() ?: 0.0) else 0.0
    
    val netProfit = baseFinanciera - (selectedProduct?.precioCosto ?: 0.0) - (if (tipoEnvio == "GRATIS") realShippingCost else 0.0)

    val cuotasTexto = remember(selectedCuotasSet, isManualCuotas, effectiveCuotas, cuotaMensual, formaPagoSeleccionada) {
        when {
            formaPagoSeleccionada == "EFECTIVO" || (effectiveCuotas <= 1 && !isManualCuotas) -> "Pago único"
            isManualCuotas -> CalculadoraFinanciera.generarTextoCuotas(effectiveCuotas, cuotaMensual, formaPagoSeleccionada == "TARJETA_SIN_INTERES")
            selectedCuotasSet.size == cuotasPredeterminadas.size -> "Hasta 24 cuotas ${if(formaPagoSeleccionada == "TARJETA_SIN_INTERES") "sin interés" else "fijas"}"
            else -> {
                val listStr = selectedCuotasSet.sorted().joinToString(", ")
                "$listStr cuotas ${if(formaPagoSeleccionada == "TARJETA_SIN_INTERES") "sin interés" else "fijas"}"
            }
        }
    }

    // Sincronización de descuentos
    LaunchedEffect(discountPercent, baseSalePrice) {
        if (isDiscountByPercent) {
            val p = discountPercent.toDoubleOrNull() ?: 0.0
            discountAmount = (baseSalePrice * (p / 100.0)).toInt().toString()
        }
    }
    LaunchedEffect(discountAmount, baseSalePrice) {
        if (!isDiscountByPercent) {
            val a = discountAmount.toDoubleOrNull() ?: 0.0
            discountPercent = if (baseSalePrice > 0) ((a / baseSalePrice) * 100).toInt().toString() else "0"
        }
    }

    // Estado para animación de envío
    var isSending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = ColorDarkBg,
        dragHandle = null,
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            containerColor = ColorDarkBg,
            topBar = {
                HeaderPresupuesto(
                    currentStep = currentStep,
                    onStepSelected = { step -> if (step == 1 || selectedProduct != null) currentStep = step },
                    onBack = { if (currentStep > 1) currentStep -= 1 else onClose() }
                )
            },
            bottomBar = {
                FooterBar(
                    currentStep = currentStep,
                    totalPrice = finalPriceForClient,
                    isSending = isSending,
                    onMainAction = {
                        when (currentStep) {
                            1 -> if (selectedProduct != null) currentStep = 2
                            2 -> currentStep = 3
                            3 -> {
                                scope.launch {
                                    isSending = true
                                    delay(1000)
                                    selectedProduct?.let { p ->
                                        val uiModel = ProductoMensajeDominio(
                                            idProducto = p.sku ?: p.id,
                                            titulo = p.nombre,
                                            descripcion = p.descripcion ?: "",
                                            marca = "Maverick",
                                            idCategoria = p.idCategoria,
                                            esServicio = p.tipo == TipoProducto.SERVICIO,
                                            urlImagen = p.urlImagen ?: "",
                                            miniaturaBase64 = p.miniaturaBase64,
                                            precioActual = finalPriceForClient,
                                            precioAnterior = if (calculatedDiscount > 0 || totalFinanciado > baseFinanciera) baseSalePrice else null,
                                            porcentajeDescuento = if (baseSalePrice > 0) ((calculatedDiscount / baseSalePrice) * 100).toInt() else 0,
                                            cuotasTexto = cuotasTexto,
                                            envioGratis = tipoEnvio == "GRATIS",
                                            tipoEnvio = tipoEnvio,
                                            costoEnvio = realShippingCost,
                                            metodoPago = formaPagoSeleccionada,
                                            estaSolicitado = false
                                        )
                                        chatViewModel.enviarProductoElite(uiModel, idEmisor, idReceptor)
                                    }
                                    isSending = false
                                    onClose()
                                }
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(Modifier.fillMaxSize().padding(paddingValues)) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "StepTransition"
                ) { step ->
                    when (step) {
                        1 -> Step1CatalogView(
                            products = catalogo,
                            searchQuery = busqueda,
                            onSearchChange = productoViewModel::actualizarBusqueda,
                            selectedProduct = selectedProduct,
                            onProductSelect = { product ->
                                selectedProduct = product
                                inputPriceText = product.precioVenta.toInt().toString()
                                currentStep = 2
                            }
                        )
                        2 -> selectedProduct?.let { product ->
                            Step2ConfigView(
                                product = product,
                                inputPriceText = inputPriceText,
                                onPriceChange = { inputPriceText = it },
                                discountPercent = discountPercent,
                                onDiscountPercentChange = { discountPercent = it },
                                discountAmount = discountAmount,
                                onDiscountAmountChange = { discountAmount = it },
                                isPercentMode = isDiscountByPercent,
                                onToggleDiscountMode = { isDiscountByPercent = it },
                                formaPago = formaPagoSeleccionada,
                                onFormaPagoChange = { formaPagoSeleccionada = it },
                                selectedCuotasSet = selectedCuotasSet,
                                onCuotasSetChange = { selectedCuotasSet = it },
                                cuotasPredeterminadas = cuotasPredeterminadas,
                                isManualCuotas = isManualCuotas,
                                onToggleManualCuotas = { isManualCuotas = it },
                                inputManualCuotas = inputManualCuotas,
                                onManualCuotasChange = { inputManualCuotas = it },
                                tasa = tasaInteresAnual,
                                onTasaChange = { tasaInteresAnual = it },
                                tipoEnvio = tipoEnvio,
                                onTipoEnvioChange = { tipoEnvio = it },
                                inputCostoEnvio = inputCostoEnvioText,
                                onCostoEnvioChange = { inputCostoEnvioText = it }
                            )
                        }
                        3 -> selectedProduct?.let { product ->
                            Step3PreviewView(
                                product = product,
                                finalPrice = finalPriceForClient,
                                originalPrice = baseSalePrice,
                                discount = calculatedDiscount,
                                cuotasInfo = cuotasTexto,
                                tipoEnvio = tipoEnvio,
                                costoEnvio = realShippingCost,
                                metodoPago = formaPagoSeleccionada, // 🔥 [NEW]
                                netProfit = netProfit
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderPresupuesto(currentStep: Int, onStepSelected: (Int) -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(ColorCardBg).border(1.dp, ColorBorderDark).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.ArrowBack, null, tint = ColorTextMuted) }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(text = "#PROP-${System.currentTimeMillis().toString().takeLast(5)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorAccentOrange, modifier = Modifier.background(ColorAccentOrange.copy(0.1f), RoundedCornerShape(4.dp)).border(1.dp, ColorAccentOrange.copy(0.2f), RoundedCornerShape(4.dp)).padding(6.dp, 2.dp))
                    Text(text = when(currentStep){ 1->"Catálogo"; 2->"Oferta"; else->"Preview" }, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Surface(color = ColorAccentEmerald.copy(0.1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, ColorAccentEmerald.copy(0.3f))) {
                Text("V2026.ELITE", modifier = Modifier.padding(8.dp, 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Black, color = ColorAccentEmerald)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            TabButton(1, "Catálogo", currentStep == 1, currentStep > 1, modifier = Modifier.weight(1f)) { onStepSelected(1) }
            TabButton(2, "Oferta", currentStep == 2, currentStep > 2, modifier = Modifier.weight(1f)) { onStepSelected(2) }
            TabButton(3, "Preview", currentStep == 3, false, modifier = Modifier.weight(1f)) { onStepSelected(3) }
        }
    }
}

@Composable
private fun TabButton(step: Int, label: String, active: Boolean, done: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val color = if (active) ColorAccentOrange else if (done) ColorAccentEmerald else ColorTextMuted
    Row(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(color.copy(0.1f)).border(1.dp, color.copy(0.4f), RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(8.dp), Arrangement.Center, Alignment.CenterVertically) {
        Box(Modifier.size(16.dp).background(color, CircleShape), Alignment.Center) { Text(step.toString(), fontSize = 9.sp, fontWeight = FontWeight.Black, color = ColorDarkBg) }
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun Step1CatalogView(products: List<ProductoEntity>, searchQuery: String, onSearchChange: (String) -> Unit, selectedProduct: ProductoEntity?, onProductSelect: (ProductoEntity) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(value = searchQuery, onValueChange = onSearchChange, placeholder = { Text("Buscar en catálogo...", color = ColorTextMuted) }, leadingIcon = { Icon(Icons.Default.Search, null, tint = ColorTextMuted) }, modifier = Modifier.fillMaxWidth().background(ColorCardBg, RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorAccentOrange, unfocusedBorderColor = ColorBorderDark))
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(products) { p ->
                val sel = selectedProduct?.id == p.id
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if(sel) ColorAccentOrange.copy(0.1f) else ColorCardBg).border(1.dp, if(sel) ColorAccentOrange else ColorBorderDark, RoundedCornerShape(16.dp)).clickable { onProductSelect(p) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(ColorSurfaceBg), Alignment.Center) {
                        if (p.urlImagen != null) AsyncImage(p.urlImagen, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Icon(if (p.tipo == TipoProducto.SERVICIO) Icons.Default.Handyman else Icons.Outlined.Inventory2, null, tint = ColorAccentOrange)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(p.nombre, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("$${p.precioVenta.toInt()}", fontSize = 13.sp, color = ColorAccentOrange, fontWeight = FontWeight.Black)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = ColorTextMuted)
                }
            }
        }
    }
}

@Composable
private fun Step2ConfigView(
    product: ProductoEntity,
    inputPriceText: String, onPriceChange: (String) -> Unit,
    discountPercent: String, onDiscountPercentChange: (String) -> Unit,
    discountAmount: String, onDiscountAmountChange: (String) -> Unit,
    isPercentMode: Boolean, onToggleDiscountMode: (Boolean) -> Unit,
    formaPago: String, onFormaPagoChange: (String) -> Unit,
    selectedCuotasSet: Set<Int>, onCuotasSetChange: (Set<Int>) -> Unit,
    cuotasPredeterminadas: List<Int>,
    isManualCuotas: Boolean, onToggleManualCuotas: (Boolean) -> Unit,
    inputManualCuotas: String, onManualCuotasChange: (String) -> Unit,
    tasa: String, onTasaChange: (String) -> Unit,
    tipoEnvio: String, onTipoEnvioChange: (String) -> Unit,
    inputCostoEnvio: String, onCostoEnvioChange: (String) -> Unit
) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // --- CARD: VALORES BASE ---
        item {
            Card(colors = CardDefaults.cardColors(containerColor = ColorCardBg), border = BorderStroke(1.dp, ColorBorderDark)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("VALORES BASE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = ColorAccentOrange)
                    FieldRow("Precio Sugerido", "$", inputPriceText, onPriceChange)
                    
                    HorizontalDivider(color = ColorBorderDark.copy(0.4f))
                    
                    Text("DESCUENTO ESPECIAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorTextMuted)
                    
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f).clickable { onToggleDiscountMode(true) }) {
                            FieldRowCompact("%", discountPercent, onDiscountPercentChange, isPercentMode, ColorAccentEmerald)
                        }
                        Box(Modifier.weight(1f).clickable { onToggleDiscountMode(false) }) {
                            FieldRowCompact("$", discountAmount, onDiscountAmountChange, !isPercentMode, ColorAccentEmerald)
                        }
                    }
                }
            }
        }

        // --- CARD: FINANCIACIÓN ---
        item {
            Card(colors = CardDefaults.cardColors(containerColor = ColorCardBg), border = BorderStroke(1.dp, ColorBorderDark)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("MÉTODO Y FINANCIACIÓN", fontSize = 11.sp, fontWeight = FontWeight.Black, color = ColorAccentBlue)
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                        PaymentChip("EFECTIVO", formaPago == "EFECTIVO", Icons.Outlined.Payments, Modifier.weight(1f)) { onFormaPagoChange("EFECTIVO") }
                        PaymentChip("TARJETA", formaPago.startsWith("TARJETA"), Icons.Outlined.CreditCard, Modifier.weight(1f)) { onFormaPagoChange("TARJETA_SIN_INTERES") }
                    }

                    if (formaPago.startsWith("TARJETA")) {
                        HorizontalDivider(color = ColorBorderDark.copy(0.5f))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("Tipo de Plan", fontSize = 13.sp, color = Color.White)
                            Row(Modifier.background(ColorSurfaceBg, CircleShape).padding(4.dp)) {
                                PlanSubChip("SIN INTERÉS", formaPago == "TARJETA_SIN_INTERES") { onFormaPagoChange("TARJETA_SIN_INTERES") }
                                PlanSubChip("CON INTERÉS", formaPago == "TARJETA_INTERES") { onFormaPagoChange("TARJETA_INTERES") }
                            }
                        }

                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("PLANES DISPONIBLES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorTextMuted)
                            Row(Modifier.clickable {
                                if (selectedCuotasSet.size == cuotasPredeterminadas.size) onCuotasSetChange(emptySet())
                                else onCuotasSetChange(cuotasPredeterminadas.toSet())
                            }, verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedCuotasSet.size == cuotasPredeterminadas.size,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(checkedColor = ColorAccentBlue)
                                )
                                Text("TODAS", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                        
                        LazyRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(cuotasPredeterminadas) { o ->
                                FilterChip(
                                    selected = !isManualCuotas && selectedCuotasSet.contains(o),
                                    onClick = {
                                        onToggleManualCuotas(false)
                                        val newSet = selectedCuotasSet.toMutableSet()
                                        if (newSet.contains(o)) newSet.remove(o) else newSet.add(o)
                                        onCuotasSetChange(newSet)
                                    },
                                    label = { Text(o.toString(), fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ColorAccentBlue, selectedLabelColor = Color.Black)
                                )
                            }
                            item {
                                FilterChip(
                                    selected = isManualCuotas,
                                    onClick = { onToggleManualCuotas(true) },
                                    label = { Text("OTRO", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ColorAccentOrange, selectedLabelColor = Color.Black)
                                )
                            }
                        }

                        if (isManualCuotas) {
                            FieldRow("Cuotas Manuales", "#", inputManualCuotas, onManualCuotasChange, ColorAccentOrange)
                        }

                        if (formaPago == "TARJETA_INTERES") {
                            FieldRow("Tasa Nominal Anual (TNA)", "%", tasa, onTasaChange, ColorAccentBlue)
                        }
                    }
                }
            }
        }

        // --- CARD: ENVÍOS ---
        item {
            Card(colors = CardDefaults.cardColors(containerColor = ColorCardBg), border = BorderStroke(1.dp, ColorBorderDark)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("ENVÍOS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = ColorAccentEmerald)
                    
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column {
                            Text("A convenir / Retiro", fontSize = 13.sp, color = Color.White)
                            Text("El cliente coordina la entrega", fontSize = 10.sp, color = ColorTextMuted)
                        }
                        Switch(
                            checked = tipoEnvio == "CONVENIR",
                            onCheckedChange = { if(it) onTipoEnvioChange("CONVENIR") else onTipoEnvioChange("COBRO_CLIENTE") },
                            colors = SwitchDefaults.colors(checkedTrackColor = ColorAccentEmerald)
                        )
                    }

                    if (tipoEnvio != "CONVENIR") {
                        HorizontalDivider(color = ColorBorderDark.copy(0.4f))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("Costo de envío", fontSize = 13.sp, color = if(tipoEnvio == "GRATIS") ColorTextMuted else Color.White)
                            Row(modifier = Modifier.alpha(if (tipoEnvio == "GRATIS") 0.4f else 1f).background(ColorSurfaceBg, RoundedCornerShape(8.dp)).border(1.dp, ColorBorderDark, RoundedCornerShape(8.dp)).padding(8.dp, 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("$ ", fontSize = 13.sp, color = ColorTextMuted)
                                BasicTextField(value = inputCostoEnvio, onValueChange = onCostoEnvioChange, enabled = tipoEnvio != "GRATIS", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End), modifier = Modifier.width(60.dp))
                            }
                        }
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Column {
                                Text("Envío GRATIS", fontSize = 13.sp, color = ColorAccentEmerald, fontWeight = FontWeight.Bold)
                                Text("Tú absorbes el costo logístico", fontSize = 10.sp, color = ColorTextMuted)
                            }
                            Switch(checked = tipoEnvio == "GRATIS", onCheckedChange = { if(it) onTipoEnvioChange("GRATIS") else onTipoEnvioChange("COBRO_CLIENTE") }, colors = SwitchDefaults.colors(checkedTrackColor = ColorAccentEmerald))
                        }
                    }
                }
            }
        }

        item {
             Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(ColorCardBg.copy(0.5f)).border(1.dp, ColorBorderDark, RoundedCornerShape(14.dp)).padding(14.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                 InfoLabel("COSTO", "$${product.precioCosto.toInt()}")
                 Box(Modifier.width(1.dp).height(24.dp).background(ColorBorderDark))
                 InfoLabel("STOCK", product.stockActual.toString())
             }
        }
    }
}

@Composable
private fun FieldRowCompact(symbol: String, value: String, onValueChange: (String) -> Unit, active: Boolean, color: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .alpha(if (active) 1f else 0.4f)
            .background(if (active) ColorSurfaceBg else Color.Transparent, RoundedCornerShape(8.dp))
            .border(1.dp, if (active) color else ColorBorderDark, RoundedCornerShape(8.dp))
            .padding(10.dp, 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(symbol, fontSize = 13.sp, color = ColorTextMuted, fontWeight = FontWeight.Bold)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = active,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(color = if(active) Color.White else ColorTextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End),
            cursorBrush = SolidColor(color),
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        )
    }
}

@Composable
private fun PaymentChip(label: String, selected: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Surface(modifier = modifier, onClick = onClick, shape = RoundedCornerShape(12.dp), color = if(selected) ColorAccentBlue.copy(0.15f) else ColorSurfaceBg, border = BorderStroke(1.dp, if(selected) ColorAccentBlue else ColorBorderDark)) {
        Column(Modifier.padding(12.dp), Arrangement.Center, Alignment.CenterHorizontally) {
            Icon(icon, null, tint = if(selected) ColorAccentBlue else ColorTextMuted, modifier = Modifier.size(20.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Black, color = if(selected) Color.White else ColorTextMuted, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun PlanSubChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(label, fontSize = 9.sp, fontWeight = FontWeight.Black, color = if(selected) Color.Black else ColorTextMuted, modifier = Modifier.clip(CircleShape).background(if(selected) ColorAccentBlue else Color.Transparent).clickable { onClick() }.padding(10.dp, 6.dp))
}

@Composable
private fun FieldRow(label: String, symbol: String, value: String, onValueChange: (String) -> Unit, color: Color = Color.White) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, color = Color.White)
        Row(modifier = Modifier.background(ColorSurfaceBg, RoundedCornerShape(8.dp)).border(1.dp, ColorBorderDark, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("$symbol ", fontSize = 13.sp, color = ColorTextMuted)
            BasicTextField(value, onValueChange, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), textStyle = TextStyle(color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End), cursorBrush = SolidColor(ColorAccentOrange), modifier = Modifier.width(70.dp))
        }
    }
}

@Composable
private fun InfoLabel(l: String, v: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(l, fontSize = 8.sp, color = ColorTextMuted, fontWeight = FontWeight.Bold)
        Text(v, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun Step3PreviewView(
    product: ProductoEntity, 
    finalPrice: Double, 
    originalPrice: Double, 
    discount: Double, 
    cuotasInfo: String, 
    tipoEnvio: String, 
    costoEnvio: Double, 
    metodoPago: String, // 🔥 [NEW]
    netProfit: Double
) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("VISTA PREVIA REAL DEL MENSAJE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = ColorTextMuted) }
        
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ColorChatBg)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                ProductoChatBubble(
                    producto = ProductoMensajeDominio(
                        idProducto = product.sku ?: product.id,
                        titulo = product.nombre,
                        descripcion = product.descripcion ?: "",
                        marca = "Maverick",
                        idCategoria = product.idCategoria,
                        urlImagen = product.urlImagen ?: "",
                        precioActual = finalPrice,
                        precioAnterior = if (discount > 0 || finalPrice > (originalPrice - discount)) originalPrice else null,
                        porcentajeDescuento = if (originalPrice > 0) ((discount / originalPrice) * 100).toInt() else 0,
                        cuotasTexto = cuotasInfo,
                        envioGratis = tipoEnvio == "GRATIS",
                        tipoEnvio = tipoEnvio,
                        costoEnvio = costoEnvio,
                        metodoPago = metodoPago,
                        estaSolicitado = false
                    ),
                    esEntrante = false,
                    horaMensaje = java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(java.util.Date()),
                    mostrarBotonComprar = true 
                )
            }
        }
        
        item {
            Card(colors = CardDefaults.cardColors(containerColor = ColorCardBg), border = BorderStroke(1.dp, ColorBorderDark)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("TU RENTABILIDAD (PRIVADO)", fontSize = 10.sp, fontWeight = FontWeight.Black, color = ColorTextMuted)
                    HorizontalDivider(color = ColorBorderDark.copy(0.4f))
                    ResumenRow("Ingreso Bruto Cliente", "$${String.format(Locale.getDefault(), "%,.0f", finalPrice)}", Color.White)
                    ResumenRow("Descuento Aplicado", "-$${discount.toInt()}", Color.Red)
                    ResumenRow("Costo de Producto", "-$${product.precioCosto.toInt()}", Color.Red)
                    if (tipoEnvio == "GRATIS") ResumenRow("Costo Logística (Absorbido)", "-$${costoEnvio.toInt()}", Color.Red)
                    
                    HorizontalDivider(color = ColorBorderDark.copy(0.4f))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Ganancia Neta Estimada", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("$${String.format(Locale.getDefault(), "%,.0f", netProfit)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if(netProfit>0) ColorAccentEmerald else Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenRow(l: String, v: String, c: Color) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text(l, fontSize = 12.sp, color = ColorTextMuted); Text(v, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = c) }
}

@Composable
private fun FooterBar(currentStep: Int, totalPrice: Double, isSending: Boolean, onMainAction: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(ColorCardBg).border(1.dp, ColorBorderDark).padding(16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Column {
            Text("LO QUE PAGA EL CLIENTE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = ColorTextMuted)
            Text("$${String.format(Locale.getDefault(), "%,.0f", totalPrice)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
        }
        Button(onClick = onMainAction, enabled = !isSending, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = if (currentStep == 3) ColorAccentEmerald else ColorAccentOrange), modifier = Modifier.height(48.dp)) {
            if (isSending) CircularProgressIndicator(Modifier.size(18.dp), ColorDarkBg, 2.dp)
            else {
                Text(if(currentStep==3) "ENVIAR AL CHAT" else "CONTINUAR", fontWeight = FontWeight.Black, color = ColorDarkBg)
                Icon(if(currentStep==3) Icons.Default.Send else Icons.Default.ChevronRight, null, tint = ColorDarkBg)
            }
        }
    }
}

