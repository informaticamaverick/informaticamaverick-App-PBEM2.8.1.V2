package com.example.myapplication.prestador.ui.pantallas.config

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.core.dominio.modelos.PresupuestoConfig
import androidx.compose.foundation.border
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import com.example.myapplication.prestador.viewmodel.presupuesto.PrePresupuestoConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresupuestoConfigScreen(
    onBack: () -> Unit,
    viewModel: PrePresupuestoConfigViewModel = hiltViewModel()
) {
    val colors = PrestadorColors(
        primaryOrange = Color(0xFFFF5722),
        primaryOrangeDark = Color(0xFFF4511E),
        primaryOrangeLight = Color(0xFFFB923C),
        backgroundColor = Color(0xFF030712),
        surfaceColor = Color(0xFF0F172A),
        surfaceElevated = Color(0xFF1E293B),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8),
        border = Color(0xFF334155).copy(alpha = 0.7f),
        divider = Color(0xFF1E293B),
        chipBackground = Color(0xFF1E293B),
        chipText = Color(0xFFF8FAFC),
        error = Color(0xFFEF4444),
        success = Color(0xFF10B981)
    )
    val config by viewModel.config.collectAsState()

    //Secciones expandibles
    var validezExpanded by remember { mutableStateOf(true) }
    var monedaExpanded by remember { mutableStateOf(false) }
    var numeracionExpanded by remember { mutableStateOf(false) }

    //Estado local del prefijo para edición
    var prefijoText by remember(config.prefijo) { mutableStateOf(config.prefijo)}
    var proximoNumeroText by remember(config.proximoNumero) { mutableStateOf(config.proximoNumero.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        //Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceColor)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .background(colors.backgroundColor, RoundedCornerShape(8.dp))
                    .border(1.dp, colors.divider, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colors.textPrimary, modifier = Modifier.size(16.dp))
            }
            Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                Text(
                    "CONFIGURACIÓN DE PRESUPUESTOS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    "Valores por defecto y numeracion",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }
        HorizontalDivider(color = colors.divider)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            //Seccion 1: VALIDEZ
            ConfigSection(
                title = "Validez por defecto",
                icon = Icons.Default.Schedule,
                expanded = validezExpanded,
                onToggle = { validezExpanded = !validezExpanded },
                colors = colors
            ) {
                Text(
                    "Días que tendrá Vigencia cada presupuesto al crearlo",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                //Chips de opciones rápidas
                val opciones = listOf(7, 15, 30, 60)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    opciones.forEach { dias ->
                        FilterChip(
                            selected = config.validezDias == dias,
                            onClick = { viewModel.establecerValidezDias(dias) },
                            label = { Text("$dias dias") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primaryOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "✓ Vigencia actual: ${config.validezDias} días",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primaryOrange
                )
            }

            //Seccion 2: MONEDA
            ConfigSection(
                title = "Moneda",
                icon = Icons.Default.AttachMoney,
                expanded =monedaExpanded,
                onToggle = { monedaExpanded = !monedaExpanded },
                colors = colors
            ) {
                Text(
                    "Moneda que se mostrará en los presupuestos y PDFs",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                val monedas= listOf(
                    "ARS" to "🇦🇷 Pesos (ARS)",
                    "USD" to "🇺🇸 Dólares (USD)"
                )
                monedas.forEach { (codigo, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.establecerMoneda(codigo) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = config.moneda == codigo,
                            onClick = { viewModel.establecerMoneda(codigo) },
                            colors = RadioButtonDefaults.colors(selectedColor = colors.primaryOrange)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(label, fontSize = 15.sp, color = colors.textPrimary)
                    }
                }
            }

            //Sección 3: NUMERACIÓN
            ConfigSection(
                title = "Numeración automática",
                icon = Icons.Default.Tag,
                expanded = numeracionExpanded,
                onToggle = { numeracionExpanded = !numeracionExpanded },
                colors = colors
            ) {
                Text(
                    "Prefijo y número inicial para generar el codigo del presupuesto",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = prefijoText,
                        onValueChange = { prefijoText = it.uppercase().take(6)},
                        label = { Text("Prefijo")},
                        placeholder = { Text("PRES")},
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primaryOrange,
                            focusedLabelColor = colors.primaryOrange
                        ),
                        trailingIcon = {
                            if (prefijoText != config.prefijo) {
                                IconButton(onClick = { viewModel.establecerPrefijo(prefijoText) } ) {
                                    Icon(Icons.Default.Check, null, tint = colors.primaryOrange)
                                }
                            }
                        }
                    )
                    OutlinedTextField(
                        value = proximoNumeroText,
                        onValueChange = { proximoNumeroText = it.filter { c -> c.isDigit() }.take(6)},
                        label = { Text("Próximo N°")},
                        placeholder = { Text("1")},
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primaryOrange,
                            focusedLabelColor = colors.primaryOrange
                        ),
                        trailingIcon = {
                            if (proximoNumeroText != config.proximoNumero.toString()) {
                                IconButton(onClick = {
                                    val n = proximoNumeroText.toIntOrNull() ?: 1
                                    viewModel.establecerProximoNumero(n)
                                }) {
                                    Icon(Icons.Default.Check, null, tint = colors.primaryOrange)
                                }
                            }
                        }
                    )

                }
                Spacer(Modifier.height(8.dp))
                //Vista previa del número
                val preview = "${config.prefijo}-%04d".format(config.proximoNumero)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.primaryOrange.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Visibility, null, tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Vista previa. $preview",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.primaryOrange
                        )
                    }
                }
            }

            //Seccion 4: NOTA LEGAL
            var notaLegalExpanded by remember { mutableStateOf(false) }
            var notaLegalText by remember(config.notaLegal) { mutableStateOf(config.notaLegal) }
            var notaLegalSaved by remember { mutableStateOf(false) }

            // Auto-guarda 800 ms después de que el usuario para de escribir
            LaunchedEffect(notaLegalText) {
                if (notaLegalText != config.notaLegal) {
                    notaLegalSaved = false
                    kotlinx.coroutines.delay(800)
                    viewModel.establecerNotaLegal(notaLegalText)
                    notaLegalSaved = true
                }
            }

            ConfigSection(
                title = "Nota legal",
                icon = Icons.Default.Gavel,
                expanded = notaLegalExpanded,
                onToggle = { notaLegalExpanded = !notaLegalExpanded },
                colors = colors
            ) {
                Text(
                    "Texto que aparece al pie de cada presupuesto generado",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = notaLegalText,
                    onValueChange = { notaLegalText = it },
                    label = { Text("Nota legal") },
                    placeholder = { Text("Ej: Precios sujetos a cambio sin previo aviso.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primaryOrange,
                        focusedLabelColor = colors.primaryOrange
                    ),
                    trailingIcon = {
                        if (notaLegalText == config.notaLegal && config.notaLegal.isNotBlank()) {
                            Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50))
                        }
                    },
                    supportingText = {
                        if (notaLegalText != config.notaLegal && notaLegalText.isNotBlank()) {
                            Text("Guardando...", fontSize = 11.sp, color = colors.textSecondary)
                        } else if (notaLegalSaved || (notaLegalText == config.notaLegal && config.notaLegal.isNotBlank())) {
                            Text("✓ Guardado", fontSize = 11.sp, color = Color(0xFF4CAF50))
                        }
                    }
                )
                if (config.notaLegal.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.primaryOrange.copy(alpha = 0.06f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "✓ ${config.notaLegal}",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(10.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Sección 5: SECCIONES POR DEFECTO
            var seccionesExpanded by remember { mutableStateOf(false) }
            ConfigSection(
                title = "Secciones por defecto",
                icon = Icons.Default.ViewList,
                expanded = seccionesExpanded,
                onToggle = { seccionesExpanded = !seccionesExpanded },
                colors = colors
            ) {
                Text(
                    "Elegí qué secciones aparecen activas al crear un nuevo presupuesto",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                @Composable
                fun SeccionRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggle(!checked) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, fontSize = 14.sp, color = colors.textPrimary, modifier = Modifier.weight(1f))
                        Switch(
                            checked = checked,
                            onCheckedChange = onToggle,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.primaryOrange)
                        )
                    }
                }

                SeccionRow("Artículos / Repuestos",    config.showArticlesByDefault)    { viewModel.establecerMostrarArticulosDefault(it) }
                SeccionRow("Mano de Obra / Servicios", config.showServicesByDefault)    { viewModel.establecerMostrarServiciosDefault(it) }
                SeccionRow("Honorarios profesionales", config.showFeesByDefault)        { viewModel.establecerMostrarHonorariosDefault(it) }
                SeccionRow("Gastos varios",            config.showMiscByDefault)        { viewModel.establecerMostrarVariosDefault(it) }
                SeccionRow("Impuestos / IIBB",         config.showTaxesByDefault)       { viewModel.establecerMostrarImpuestosDefault(it) }
                SeccionRow("Adjuntos",                 config.showAttachmentsByDefault) { viewModel.establecerMostrarAdjuntosDefault(it) }
            }


            //Seccion 6: NOTA / OBSERVACIONES POR DEFECTO
            var notaExpanded by remember { mutableStateOf(false) }
            ConfigSection(
                title = "Nota / Observaciones por defecto",
                icon = Icons.Default.StickyNote2,
                expanded = notaExpanded,
                onToggle = { notaExpanded = !notaExpanded },
                colors = colors
            ) {
                Text(
                    "Este texto se pre-carga en el campo de notas al crear un nuevo presupuesto",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = config.notaObservacionesDefault,
                    onValueChange = {
                        viewModel.establecerNotaObservacionesDefault(it) },
                    label = { Text("Nota por defecto")},
                    placeholder = { Text("Ej: Precios sujetos a cambios sin previo aviso")},
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primaryOrange,
                        focusedLabelColor = colors.primaryOrange,
                        cursorColor = colors.primaryOrange
                    )
                )
            }

            //Seccion 7: Descuento por defecto
            var descuentoExpanded by remember { mutableStateOf(false) }
            var descuentoText by remember(config.descuentoDefault) {
                mutableStateOf(if (config.descuentoDefault > 0) config.descuentoDefault.toString() else "")
            }
            ConfigSection(
                title = "Descuento por defecto",
                icon = Icons.Default.Discount,
                expanded = descuentoExpanded,
                onToggle = { descuentoExpanded = !descuentoExpanded },
                colors = colors
            ) {
                Text(
                    "% de descuento pre-cargado en cada artículo nuevo (editable por presupuesto)",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = descuentoText,
                    onValueChange = {
                        descuentoText = it
                        val d = it.toDoubleOrNull() ?: 0.0
                        viewModel.establecerDescuentoDefaultPct(d)
                    },
                    label = { Text("Descuento (%)") },
                    placeholder = { Text("Ej: 5") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    trailingIcon = { Text("%", color = colors.textSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primaryOrange,
                        focusedLabelColor = colors.primaryOrange,
                        cursorColor = colors.primaryOrange
                    )
                )
            }

            // Sección 8: CATEGORÍA POR DEFECTO
            var catExpanded by remember { mutableStateOf(false) }
            ConfigSection(
                title = "Categoría por defecto",
                icon = Icons.Default.Category,
                expanded = catExpanded,
                onToggle = { catExpanded = !catExpanded },
                colors = colors
            ) {
                Text(
                    "Categoría pre-seleccionada al crear un nuevo presupuesto",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = config.categoriaDefault,
                    onValueChange = { viewModel.establecerCategoriaDefault(it) },
                    label = { Text("Categoría por defecto") },
                    placeholder = { Text("Ej: Reparación") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primaryOrange,
                        focusedLabelColor = colors.primaryOrange,
                        cursorColor = colors.primaryOrange
                    )
                )
            }

        }
    }
}

//Componente reutilisable: Sección con headr expandible

@Composable
private fun ConfigSection(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceColor,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = colors.primaryOrange, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null,
                    tint = colors.textSecondary
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                exit = shrinkVertically(tween(300)) + fadeOut(tween(200))
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    HorizontalDivider(color = colors.divider)
                    Spacer(Modifier.height(12.dp))
                    content()
                }
            }
        }
    }
}














































