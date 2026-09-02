package com.example.myapplication.prestador.ui.pantallas.presupuesto

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto
import com.example.myapplication.prestador.datos.local.entidades.PresupuestoEntity
import com.example.myapplication.prestador.ui.theme.LocalIsDarkTheme
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.uishared.ui.components.PlanillaPresupuestoA4Viewer
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.prestador.viewmodel.profile.PerfilPrestadorDeepViewModel
import com.example.myapplication.prestador.viewmodel.presupuesto.PrestadorPresupuestoViewModel
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

// --- UI MODELS & ENUMS (V2026.ELITE) ---

enum class NavTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Inicio", Icons.Default.Home),
    PROPOSALS("Enviados", Icons.Default.List),
    STATS("Métricas", Icons.Default.BarChart),
    EXPENSES("Límites", Icons.Default.AccountBalanceWallet)
}

data class ExpenseLimit(
    val id: String,
    val name: String,
    val icon: String,
    val spent: Double,
    val limit: Double
)

/**
 * --- PANTALLA DE GESTIÓN DE PRESUPUESTOS (ESTILO ORIGINAL RESTAURADO) ---
 * [PROPÓSITO]: Lienzo unificado para el profesional Maverick con métricas y control financiero.
 * [INTEGRACIÓN]: Conectado a los cables reales de Room y ViewModel.
 */
@Composable
fun PresupuestosScreen(
    onVolver: () -> Unit = {},
    onCrearNuevo: () -> Unit = {},
    onVerDetalle: (PresupuestoEntity) -> Unit = {},
    onNavegarConfig: () -> Unit = {},
    viewModel: PrestadorPresupuestoViewModel = hiltViewModel(),
    identidadViewModel: PerfilPrestadorDeepViewModel = hiltViewModel()
) {
    val presupuestos by viewModel.presupuestos.collectAsStateWithLifecycle()
    val selectedBudgetFull by viewModel.selectedBudgetFull.collectAsStateWithLifecycle()
    val estadisticas by viewModel.estadisticas.collectAsStateWithLifecycle()
    val stateDeep by identidadViewModel.state.collectAsStateWithLifecycle()
    val maestro = stateDeep.ecosistema

    PresupuestosScreenContent(
        presupuestos = presupuestos,
        selectedBudgetFull = selectedBudgetFull,
        estadisticas = estadisticas,
        maestro = maestro,
        onVolver = onVolver,
        onCrearNuevo = onCrearNuevo,
        onVerDetalle = { 
            viewModel.seleccionarPresupuestoParaDetalle(it.idPresupuesto)
            onVerDetalle(it) 
        },
        onCerrarDetalle = { viewModel.seleccionarPresupuestoParaDetalle(null) },
        onNavegarConfig = onNavegarConfig
    )
}

/**
 * --- PANTALLA DE GESTIÓN DE PRESUPUESTOS (ESTILO ORIGINAL RESTAURADO) ---
 * [PROPÓSITO]: Lienzo unificado para el profesional Maverick con métricas y control financiero.
 * [INTEGRACIÓN]: Versión desacoplada del ViewModel para facilitar el testing y Previews.
 */
@Composable
fun PresupuestosScreenContent(
    presupuestos: List<PresupuestoEntity>,
    selectedBudgetFull: com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems? = null,
    estadisticas: com.example.myapplication.core.dominio.motores.CalculadoraPresupuesto.ResultadoCalculo = com.example.myapplication.core.dominio.motores.CalculadoraPresupuesto.ResultadoCalculo(),
    maestro: com.example.myapplication.core.dominio.modelos.PerfilPrestadorDeepModelo? = null,
    onVolver: () -> Unit = {},
    onCrearNuevo: () -> Unit = {},
    onVerDetalle: (PresupuestoEntity) -> Unit = {},
    onCerrarDetalle: () -> Unit = {},
    onNavegarConfig: () -> Unit = {},
) {
    // [ELITE]: Presupuestos, como Inicio, es siempre oscuro por diseño — antes seguía
    // getPrestadorColors() sin fijar el tema (quedaba blanco/crema en modo claro del
    // sistema, sin combinar con el resto de la app).
    CompositionLocalProvider(LocalIsDarkTheme provides true) {
    val colores = getPrestadorColors()

    var activeTab by remember { mutableStateOf(NavTab.DASHBOARD) }
    var selectedStatusFilter by remember { mutableStateOf<EstadoPresupuesto?>(null) }
    var selectedProposalForDetail by remember { mutableStateOf<PresupuestoEntity?>(null) }

    // Mock para límites de gastos (Futura implementación operativa)
    val expenseLimits = remember {
        listOf(
            ExpenseLimit("b1", "Materiales Eléctricos", "🔌", 450000.0, 500000.0),
            ExpenseLimit("b2", "Logística y Combustible", "🚚", 120000.0, 150000.0),
            ExpenseLimit("b3", "Herramientas y Equipos", "🔨", 310000.0, 250000.0),
            ExpenseLimit("b4", "Publicidad Maverick", "📢", 45000.0, 100000.0)
        )
    }

    Scaffold(
        containerColor = colores.backgroundColor,
        topBar = {
            Column {
                TopAppBarElite(
                    activeTab = activeTab,
                    onVolver = onVolver,
                    onCrearNuevo = onCrearNuevo,
                    onNavegarConfig = onNavegarConfig
                )
                // [ELITE]: las 4 secciones pasan de barra INFERIOR a pestañas debajo del
                // encabezado — antes quedaba una segunda barra de navegación apilada justo
                // arriba de la barra principal de la app, confundiendo cuál era cuál
                // (esta pantalla tenía su propio ítem "Inicio" además del Home real de la app).
                SelectorSeccionesPresupuesto(activeTab = activeTab, onSeleccionar = { activeTab = it })
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    NavTab.DASHBOARD -> DashboardContent(presupuestos, estadisticas) { activeTab = NavTab.PROPOSALS }
                    NavTab.PROPOSALS -> ProposalsContent(presupuestos, selectedStatusFilter, { selectedStatusFilter = it }) { 
                        selectedProposalForDetail = it
                        onVerDetalle(it)
                    }
                    NavTab.STATS -> StatsContent(presupuestos)
                    NavTab.EXPENSES -> ExpensesContent(expenseLimits)
                }
            }

            // Detail Modal Dialog
            selectedBudgetFull?.let { budgetRel ->
                val prestadorUi = maestro?.let { com.example.myapplication.core.dominio.mapeadores.PrestadorMappers.deCompletoAModeloUi(it.prestador) }

                if (prestadorUi != null) {
                    com.example.myapplication.uishared.ui.components.PlanillaPresupuestoA4Dialog(
                        prestador = prestadorUi,
                        relacion = budgetRel,
                        onDismiss = onCerrarDetalle,
                        showSendButton = false
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun SelectorSeccionesPresupuesto(activeTab: NavTab, onSeleccionar: (NavTab) -> Unit) {
    val colores = getPrestadorColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colores.surfaceColor.copy(alpha = 0.95f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        NavTab.entries.forEach { tab ->
            val selected = activeTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) colores.primaryOrange.copy(alpha = 0.12f) else Color.Transparent)
                    .border(1.dp, if (selected) colores.primaryOrange.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(10.dp))
                    .clickable { onSeleccionar(tab) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    tab.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) colores.primaryOrange else colores.textSecondary
                )
            }
        }
    }
    HorizontalDivider(color = colores.divider)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBarElite(
    activeTab: NavTab,
    onVolver: () -> Unit,
    onCrearNuevo: () -> Unit,
    onNavegarConfig: () -> Unit
) {
    val colores = getPrestadorColors()
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = colores.surfaceColor.copy(alpha = 0.95f)),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(colores.primaryOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Description, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = when(activeTab) {
                            NavTab.DASHBOARD -> "Panel de Control"
                            NavTab.PROPOSALS -> "Mis Presupuestos"
                            NavTab.STATS -> "Estadísticas"
                            NavTab.EXPENSES -> "Control Interno"
                        }, 
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colores.textPrimary
                    )
                    Text("GESTIÓN MAVERICK", fontSize = 9.sp, color = colores.textSecondary, letterSpacing = 1.sp)
                }
            }
        },
        actions = {
            IconButton(onClick = onNavegarConfig) { Icon(Icons.Default.Tune, null, tint = colores.textSecondary) }
            Button(
                onClick = onCrearNuevo,
                colors = ButtonDefaults.buttonColors(containerColor = colores.primaryOrange),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nuevo", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Black)
            }
        }
    )
}

@Composable
fun DashboardContent(
    presupuestos: List<PresupuestoEntity>, 
    estadisticas: com.example.myapplication.core.dominio.motores.CalculadoraPresupuesto.ResultadoCalculo,
    onSeeAll: () -> Unit
) {
    val colores = getPrestadorColors()
    val totalAmount = presupuestos.sumOf { it.totalGeneral }
    
    // Segmentación
    val publicProposals = presupuestos.filter { it.idConcurso != null }
    val chatProposals = presupuestos.filter { it.idConcurso == null }
    
    val totalCount = presupuestos.size.coerceAtLeast(1)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Main Summary Card (FinFlow Style)
            Card(
                colors = CardDefaults.cardColors(containerColor = colores.surfaceColor),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL COTIZADO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colores.textSecondary)
                        Text("JULIO 2026", fontSize = 10.sp, color = colores.primaryOrange)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(formatCurrency(totalAmount), fontSize = 28.sp, fontWeight = FontWeight.Black, color = colores.textPrimary)
                    Text("Total generado en $totalCount propuestas enviadas", fontSize = 11.sp, color = colores.textSecondary)

                    Spacer(modifier = Modifier.height(18.dp))

                    // Progress Split Bar (Public vs Chat)
                    Row(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color.DarkGray)) {
                        Box(
                            modifier = Modifier.fillMaxHeight().weight((publicProposals.size.toFloat() / totalCount).coerceAtLeast(0.01f)).background(colores.primaryOrange)
                        )
                        Box(
                            modifier = Modifier.fillMaxHeight().weight((chatProposals.size.toFloat() / totalCount).coerceAtLeast(0.01f)).background(Color(0xFF10B981))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("🏛️ Concursos (${publicProposals.size})", fontSize = 10.sp, color = colores.textSecondary)
                        Text("💬 Chats (${chatProposals.size})", fontSize = 10.sp, color = colores.textSecondary)
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // 🔥 [BE-PROFIT] Implementación de Rentabilidad Real usando Snapshot local
                MetricCardElite(Modifier.weight(1f), "Ingresos", formatCurrency(estadisticas.totalGeneral), "Cobrados", Color(0xFF10B981))
                MetricCardElite(Modifier.weight(1f), "Ganancia", formatCurrency(estadisticas.gananciaEstimada), "Be-Profit", Color(0xFF06B6D4))
            }
        }

        item {
            Button(
                onClick = onSeeAll,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colores.surfaceElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.List, null, tint = colores.primaryOrange, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("VER HISTORIAL COMPLETO", fontSize = 11.sp, color = colores.textPrimary, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Text("RECIENTES", fontSize = 11.sp, fontWeight = FontWeight.Black, color = colores.textSecondary, letterSpacing = 1.sp)
        }

        items(presupuestos.take(3)) { p ->
            ProposalCardItem(p) { /* Ver Detalle */ }
        }
    }
}

@Composable
fun ProposalsContent(presupuestos: List<PresupuestoEntity>, statusFilter: EstadoPresupuesto?, onFilter: (EstadoPresupuesto?) -> Unit, onClick: (PresupuestoEntity) -> Unit) {
    val filtered = presupuestos.filter { statusFilter == null || it.estado == statusFilter }
    val colores = getPrestadorColors()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Historial de Propuestas", fontSize = 18.sp, fontWeight = FontWeight.Black, color = colores.textPrimary)
        Text("Visualiza y gestiona tus envíos comerciales", fontSize = 11.sp, color = colores.textSecondary)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
            item { FilterChip(selected = statusFilter == null, onClick = { onFilter(null) }, label = { Text("Todos") }) }
            EstadoPresupuesto.entries.forEach { status ->
                item {
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = { onFilter(status) },
                        label = { Text(status.name) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = colores.primaryOrange, selectedLabelColor = Color.Black)
                    )
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filtered) { p -> ProposalCardItem(p) { onClick(p) } }
        }
    }
}

@Composable
fun ProposalCardItem(presupuesto: PresupuestoEntity, onClick: () -> Unit) {
    val colores = getPrestadorColors()
    val isPublic = presupuesto.idConcurso != null

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = colores.surfaceColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(if(isPublic) colores.primaryOrange.copy(0.1f) else Color(0xFF10B981).copy(0.1f)), Alignment.Center) {
                        Text(if(isPublic) "🏛️" else "💬", fontSize = 16.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(presupuesto.tituloTrabajo ?: "Sin Título", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colores.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(if(isPublic) "Concurso Público" else "Presupuesto Chat", fontSize = 10.sp, color = colores.textSecondary)
                    }
                }
                PastillaEstadoElite(presupuesto.estado)
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(0.03f))
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(formatCurrency(presupuesto.totalGeneral), fontSize = 15.sp, fontWeight = FontWeight.Black, color = colores.textPrimary)
                Text(formatearFecha(presupuesto.marcaTiempo), fontSize = 10.sp, color = colores.textSecondary)
            }
        }
    }
}

@Composable
fun StatsContent(presupuestos: List<PresupuestoEntity>) {
    val colores = getPrestadorColors()
    val totalCount = presupuestos.size.coerceAtLeast(1)
    val accepted = presupuestos.count { it.estado == EstadoPresupuesto.ACEPTADO }
    val pending = presupuestos.count { it.estado == EstadoPresupuesto.PENDIENTE }
    val rejected = presupuestos.count { it.estado == EstadoPresupuesto.RECHAZADO }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("ANÁLISIS COMERCIAL", fontSize = 11.sp, fontWeight = FontWeight.Black, color = colores.textSecondary, letterSpacing = 1.sp)
        
        Card(colors = CardDefaults.cardColors(containerColor = colores.surfaceColor), shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Distribución de Éxito", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colores.textPrimary)
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(170.dp)) {
                    Canvas(modifier = Modifier.size(150.dp)) {
                        val strokeWidth = 35f
                        val angleA = (accepted.toFloat() / totalCount) * 360f
                        val angleP = (pending.toFloat() / totalCount) * 360f
                        val angleR = (rejected.toFloat() / totalCount) * 360f

                        drawArc(Color(0xFF10B981), -90f, angleA, false, style = Stroke(strokeWidth))
                        drawArc(Color(0xFFF59E0B), -90f + angleA, angleP, false, style = Stroke(strokeWidth))
                        drawArc(Color(0xFFF43F5E), -90f + angleA + angleP, angleR, false, style = Stroke(strokeWidth))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$totalCount", fontSize = 22.sp, fontWeight = FontWeight.Black, color = colores.textPrimary)
                        Text("TOTALES", fontSize = 9.sp, color = colores.textSecondary)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                    LegendItem("Aceptado", Color(0xFF10B981))
                    LegendItem("Pendiente", Color(0xFFF59E0B))
                    LegendItem("Rechazado", Color(0xFFF43F5E))
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 10.sp, color = getPrestadorColors().textSecondary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ExpensesContent(limits: List<ExpenseLimit>) {
    val colores = getPrestadorColors()
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("LÍMITES DE GASTOS INTERNOS", fontSize = 16.sp, fontWeight = FontWeight.Black, color = colores.textPrimary)
            Text("Controla tus costos operativos y márgenes", fontSize = 11.sp, color = colores.textSecondary)
        }

        items(limits) { limit ->
            val ratio = (limit.spent / limit.limit).toFloat()
            Card(colors = CardDefaults.cardColors(containerColor = colores.surfaceColor), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(limit.icon, fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(limit.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colores.textPrimary)
                        }
                        Text("${(ratio*100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Black, color = if(ratio > 1) Color.Red else colores.primaryOrange)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { ratio.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = if(ratio > 1) Color.Red else colores.primaryOrange,
                        trackColor = Color.White.copy(0.05f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${formatCurrency(limit.spent)} de ${formatCurrency(limit.limit)}", fontSize = 10.sp, color = colores.textSecondary)
                }
            }
        }
    }
}

@Composable
private fun MetricCardElite(modifier: Modifier = Modifier, title: String, value: String, subtitle: String, color: Color) {
    val colores = getPrestadorColors()
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colores.surfaceColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(0.03f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 11.sp, color = colores.textSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = colores.textPrimary)
            Text(subtitle, fontSize = 9.sp, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PastillaEstadoElite(estado: EstadoPresupuesto) {
    val color = when(estado) {
        EstadoPresupuesto.ACEPTADO -> Color(0xFF10B981)
        EstadoPresupuesto.PENDIENTE -> Color(0xFFF59E0B)
        else -> Color(0xFFF43F5E)
    }
    Surface(color = color.copy(0.12f), shape = CircleShape, border = BorderStroke(0.5.dp, color.copy(0.3f))) {
        Text(estado.name, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 8.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
fun PresupuestoDetalleDialog(presupuesto: PresupuestoEntity, onDismiss: () -> Unit) {
    val colores = getPrestadorColors()
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = colores.surfaceColor)) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(presupuesto.numeroPresupuesto ?: "DETALLE", fontSize = 11.sp, color = colores.primaryOrange, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text(presupuesto.tituloTrabajo ?: "Presupuesto Técnico", fontSize = 20.sp, fontWeight = FontWeight.Black, color = colores.textPrimary)
                
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colores.backgroundColor).padding(16.dp)) {
                    Column {
                        Text("MONTO COTIZADO", fontSize = 9.sp, color = colores.textSecondary, fontWeight = FontWeight.Bold)
                        Text(formatCurrency(presupuesto.totalGeneral), fontSize = 24.sp, fontWeight = FontWeight.Black, color = colores.primaryOrange)
                    }
                }
                
                Text(presupuesto.nombreEmpresaPrestador ?: "Maverick Tech", fontSize = 12.sp, color = colores.textSecondary)

                Button(
                    onClick = onDismiss, 
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colores.surfaceElevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CERRAR", color = colores.textPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- UTILS ---

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount)
}

fun formatearFecha(timestamp: Long): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).format(formatter)
    } else {
        java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(java.util.Date(timestamp))
    }
}

// --- PREVIEW ---

@Preview(showBackground = true, backgroundColor = 0xFF030712)
@Composable
fun PreviewPresupuestosScreenElite() {
    val mockPresupuestos = listOf(
        PresupuestoEntity(
            idPresupuesto = "1",
            tituloTrabajo = "Instalación Eléctrica Residencia",
            totalGeneral = 450000.0,
            estado = EstadoPresupuesto.ACEPTADO,
            marcaTiempo = System.currentTimeMillis(),
            nombreEmpresaPrestador = "Maverick Elite Services"
        ),
        PresupuestoEntity(
            idPresupuesto = "2",
            tituloTrabajo = "Mantenimiento Preventivo Industrial",
            totalGeneral = 890000.0,
            estado = EstadoPresupuesto.PENDIENTE,
            marcaTiempo = System.currentTimeMillis(),
            idConcurso = "concurso_premium_1"
        ),
        PresupuestoEntity(
            idPresupuesto = "3",
            tituloTrabajo = "Urgencia 24h: Cortocircuito",
            totalGeneral = 125000.0,
            estado = EstadoPresupuesto.RECHAZADO,
            marcaTiempo = System.currentTimeMillis()
        )
    )

    PresupuestosScreenContent(
        presupuestos = mockPresupuestos,
        estadisticas = com.example.myapplication.core.dominio.motores.CalculadoraPresupuesto.ResultadoCalculo(
            totalGeneral = 450000.0,
            gananciaEstimada = 180000.0
        )
    )
}


