package com.example.myapplication.presentation.features.budget

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import com.example.myapplication.core.data.local.entity.*
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// --- PALETA ANALÍTICA PRO ULTRA ---
private val DarkBg = Color(0xFF020408)
private val GlassPanel = Color(0xFF161C24)
private val ColorMaterial = Brush.verticalGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)))
private val ColorLabor = Brush.verticalGradient(listOf(Color(0xFFA855F7), Color(0xFF7E22CE)))
private val ColorTax = Brush.verticalGradient(listOf(Color(0xFFF43F5E), Color(0xFFBE123C)))
private val SuccessGreen = Color(0xFF10B981)
private val WarningAmber = Color(0xFFF59E0B)
private val MaverickBlue = Color(0xFF2197F5)
private val PremiumGold = Color(0xFFFFD700)

/** Extensiones de ayuda visual y cálculo */
private fun BudgetEntity.itemsTotal() = items.sumOf { it.unitPrice * it.quantity }
private fun BudgetEntity.servicesTotal() = services.sumOf { it.total } + professionalFees.sumOf { it.total }

/** Buscador de Activity para forzar rotación de pantalla */
private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

// =================================================================================
// --- PANTALLA PRINCIPAL ---
// =================================================================================

@Composable
fun BudgetComparisonAnalytics(
    tender: TenderEntity,
    budgets: List<BudgetEntity>,
    onBack: () -> Unit,
    onViewBudgetDetail: (String) -> Unit
) {
    // Estado Base usando el nombre de clase corregido
    val state = remember(budgets) {
        if (budgets.isEmpty()) return@remember MarketAnalyticsState()
        val avg = budgets.map { it.grandTotal }.average()
        val min = budgets.minOf { it.grandTotal }
        val max = budgets.maxOf { it.grandTotal }

        val items = budgets.map { b ->
            val total = b.grandTotal
            val isIrrisory = total > avg * 1.8 // Consideramos irrisorio si es 80% más caro que el promedio
            val isOptimal = total <= avg && total >= min * 0.9

            ChartBudgetItem(
                budget = b,
                total = total,
                mat = b.itemsTotal(),
                lab = b.servicesTotal(),
                tax = b.taxAmount,
                isIrrisory = isIrrisory,
                isOptimal = isOptimal
            )
        }
        MarketAnalyticsState(items, avg, min, max, items.count { !it.isIrrisory }, false)
    }

    BudgetComparisonAnalyticsContent(
        tender = tender,
        budgets = budgets,
        state = state,
        onBack = onBack,
        onViewBudgetDetail = onViewBudgetDetail
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetComparisonAnalyticsContent(
    tender: TenderEntity,
    budgets: List<BudgetEntity>,
    state: MarketAnalyticsState,
    onBack: () -> Unit,
    onViewBudgetDetail: (String) -> Unit
) {
    var showFullscreenLandscape by rememberSaveable { mutableStateOf(false) }
    var selectedBudgetForDeepDive by remember { mutableStateOf<BudgetEntity?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Generamos la inteligencia de mercado
    val rankedBudgets = remember(budgets, state.avgTotal) {
        MaverickAnalyticsEngine.calculateMarketIntelligence(budgets, state.avgTotal, state.minPrice, state.maxPrice).take(10)
    }

    // =============================================================================
    // MODOS DE PANTALLA
    // =============================================================================
    if (showFullscreenLandscape) {
        Dialog(
            onDismissRequest = { showFullscreenLandscape = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            val context = LocalContext.current
            val dialogView = LocalView.current

            DisposableEffect(Unit) {
                val activity = context.findActivity()
                val dialogWindow = (dialogView.parent as? DialogWindowProvider)?.window

                dialogWindow?.let { window ->
                    window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT)
                    window.setBackgroundDrawableResource(android.R.color.transparent)
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    val insetsController = WindowCompat.getInsetsController(window, dialogView)
                    insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    insetsController.hide(WindowInsetsCompat.Type.systemBars())
                }
                val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

                onDispose { activity?.requestedOrientation = originalOrientation }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                FullscreenLandscapeChart(
                    state = state,
                    onClose = { showFullscreenLandscape = false },
                    onViewBudget = { id ->
                        showFullscreenLandscape = false
                        selectedBudgetForDeepDive = budgets.find { it.budgetId == id }
                    }
                )
            }
        }
    } else {
        Scaffold(
            containerColor = DarkBg,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ANÁLISIS DE MERCADO PRO", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
                            Text(tender.title, fontSize = 10.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.Default.Close, null, tint = Color.White) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
                )
            }
        ) { padding ->
            if (state.isAnalyzing) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaverickBlue)
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 1. KPIs
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AnalyticsKpiCard(Modifier.weight(1f), "PROMEDIO MERCADO", state.avgTotal, Color.White)
                        AnalyticsKpiCard(Modifier.weight(1f), "MEJOR OFERTA", state.minPrice, SuccessGreen)
                    }

                    // 2. RANKING TOP 10 Y AWARDS
                    if (rankedBudgets.isNotEmpty()) {
                        TopRankingSection(
                            rankedBudgets = rankedBudgets,
                            onProviderClick = { selectedBudgetForDeepDive = it.budget }
                        )
                    }

                    // 3. GRÁFICO MASIVO
                    MassiveStackedBarChartCard(
                        budgetCount = state.items.size,
                        budgets = budgets,
                        onMaximizeClick = { showFullscreenLandscape = true }
                    )

                    // 4. MATRIZ Y BENCHMARKS
                    MatrixTableSection(budgets, rankedBudgets, onProviderClick = { selectedBudgetForDeepDive = it })
                    AiInsightCard(budgets, rankedBudgets)
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    // =============================================================================
    // BOTTOM SHEET DE DEEP DIVE (DASHBOARD INDIVIDUAL)
    // =============================================================================
    if (selectedBudgetForDeepDive != null) {
        val rankedInfo = rankedBudgets.find { it.budget.budgetId == selectedBudgetForDeepDive!!.budgetId }

        ModalBottomSheet(
            onDismissRequest = { selectedBudgetForDeepDive = null },
            sheetState = sheetState,
            containerColor = GlassPanel,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
        ) {
            ProviderDeepDiveSheet(
                budget = selectedBudgetForDeepDive!!,
                rankedInfo = rankedInfo,
                marketAvgTotal = state.avgTotal,
                marketAvgMat = budgets.map { it.itemsTotal() }.average(),
                marketAvgLab = budgets.map { it.servicesTotal() }.average(),
                allRankedBudgets = rankedBudgets,
                onViewFullBudget = {
                    selectedBudgetForDeepDive = null
                    onViewBudgetDetail(it)
                },
                onDismiss = { selectedBudgetForDeepDive = null }
            )
        }
    }
}

// =================================================================================
// --- SECCIÓN 1: RANKING TOP 10 ---
// =================================================================================

@Composable
fun TopRankingSection(rankedBudgets: List<RankedBudget>, onProviderClick: (RankedBudget) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        // 🔥 CORRECCIÓN: Separamos el padding para evitar conflictos (start, end, bottom)
        Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.EmojiEvents, null, tint = PremiumGold, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("MAVERICK SCORE: TOP 10", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rankedBudgets.size) { index ->
                val ranked = rankedBudgets[index]
                RankingCard(
                    rank = index + 1,
                    rankedBudget = ranked,
                    onClick = { onProviderClick(ranked) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RankingCard(rank: Int, rankedBudget: RankedBudget, onClick: () -> Unit) {
    val isTop3 = rank <= 3
    val borderColor = if (rank == 1) PremiumGold else if (rank == 2) Color(0xFF94A3B8) else if (rank == 3) Color(0xFFB45309) else Color.White.copy(0.1f)

    Surface(
        onClick = onClick,
        modifier = Modifier.width(260.dp),
        color = Color.White.copy(0.02f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(if (isTop3) 1.5.dp else 1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                // Rango y Foto
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("#$rank", fontSize = 18.sp, fontWeight = FontWeight.Black, color = borderColor)
                    Spacer(Modifier.width(12.dp))
                    AsyncImage(
                        model = rankedBudget.budget.providerPhotoUrl,
                        contentDescription = null,
                        fallback = rememberVectorPainter(Icons.Default.Person),
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.Gray.copy(0.2f)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Score Badge
                Surface(color = MaverickBlue.copy(0.2f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, MaverickBlue)) {
                    Text(String.format(Locale.getDefault(), "%.1f", rankedBudget.score), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(rankedBudget.budget.providerName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(rankedBudget.budget.providerCompanyName ?: "Independiente", color = MaverickBlue, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)

            // Rating y Trabajos
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                Icon(Icons.Default.Star, null, tint = WarningAmber, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text("${rankedBudget.rating}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(" • ${rankedBudget.jobsDone} trabajos", color = Color.Gray, fontSize = 10.sp)
            }

            // Insignias (Awards)
            if (rankedBudget.awards.isNotEmpty()) {
                FlowRow(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    rankedBudget.awards.take(2).forEach { award ->
                        val (icon, color) = when(award) {
                            "Mejor Precio" -> Icons.AutoMirrored.Filled.TrendingDown to SuccessGreen
                            "Top Rated" -> Icons.Default.WorkspacePremium to PremiumGold
                            "Smart Choice" -> Icons.Default.Lightbulb to MaverickBlue
                            else -> Icons.Default.CheckCircle to Color.LightGray
                        }
                        Surface(color = color.copy(0.1f), shape = RoundedCornerShape(4.dp), border = BorderStroke(0.5.dp, color.copy(0.3f))) {
                            Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, null, tint = color, modifier = Modifier.size(8.dp))
                                Spacer(Modifier.width(2.dp))
                                Text(award, color = color, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(0.05f))
            Spacer(Modifier.height(8.dp))

            // Precio
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text("OFERTA FINAL:", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text("$ ${String.format(Locale.getDefault(), "%,.0f", rankedBudget.budget.grandTotal)}", color = SuccessGreen, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

// =================================================================================
// --- SECCIÓN 2: DEEP DIVE BOTTOM SHEET (DASHBOARD INDIVIDUAL) ---
// =================================================================================

@Composable
fun ProviderDeepDiveSheet(
    budget: BudgetEntity,
    rankedInfo: RankedBudget?,
    marketAvgTotal: Double,
    marketAvgMat: Double,
    marketAvgLab: Double,
    allRankedBudgets: List<RankedBudget>,
    onViewFullBudget: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).padding(bottom = 24.dp).verticalScroll(rememberScrollState())) {

        // Cabecera
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = budget.providerPhotoUrl,
                contentDescription = null,
                fallback = rememberVectorPainter(Icons.Default.Person),
                modifier = Modifier.size(64.dp).clip(CircleShape).border(2.dp, Color.White.copy(0.1f), CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(budget.providerCompanyName ?: budget.providerName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                if (rankedInfo != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.Default.Star, null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                        Text(" ${rankedInfo.rating} • ${rankedInfo.jobsDone} Trabajos", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- DASHBOARD: GRÁFICO DE RADAR Y DISTRIBUCIÓN ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Radar Chart (Evaluación Multidimensional)
            Surface(modifier = Modifier.weight(1f).aspectRatio(1f), color = Color.White.copy(0.02f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color.White.copy(0.05f))) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("EVALUACIÓN GLOBAL", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (rankedInfo != null) {
                        RadarChart(
                            scores = listOf(
                                (1.0 - (budget.grandTotal / marketAvgTotal).coerceAtMost(1.5)).coerceAtLeast(0.0).toFloat(), // Precio (Inverso)
                                (rankedInfo.rating / 5.0).toFloat(), // Calidad
                                min(rankedInfo.jobsDone / 100.0, 1.0).toFloat(), // Experiencia
                                if (budget.discountAmount > 0) 1f else 0.2f, // Beneficios
                                min(rankedInfo.valueForMoneyScore / 2.0, 1.0).toFloat() // Value for Money
                            ),
                            labels = listOf("Precio", "Calidad", "Exp.", "Extras", "V.F.M.")
                        )
                    }
                }
            }

            // Donut Chart (Distribución del Dinero)
            Surface(modifier = Modifier.weight(1f).aspectRatio(1f), color = Color.White.copy(0.02f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color.White.copy(0.05f))) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DISTRIBUCIÓN", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    DonutChart(
                        values = listOf(budget.itemsTotal().toFloat(), budget.servicesTotal().toFloat(), budget.taxAmount.toFloat()),
                        colors = listOf(ColorMaterial, ColorLabor, ColorTax),
                        total = budget.grandTotal
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Comparativa de Barras Lineales
        Surface(color = Color.White.copy(0.03f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color.White.copy(0.05f))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("DESGLOSE VS MERCADO", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(Modifier.height(16.dp))

                ComparisonBar("Total Final", budget.grandTotal, marketAvgTotal, isLowerBetter = true)
                Spacer(Modifier.height(16.dp))
                ComparisonBar("Materiales", budget.itemsTotal(), marketAvgMat, isLowerBetter = true)
                Spacer(Modifier.height(16.dp))
                ComparisonBar("Mano de Obra", budget.servicesTotal(), marketAvgLab, isLowerBetter = true)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Condiciones Comerciales
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ConditionChip(Icons.Default.AccessTime, budget.executionTime ?: "Tiempo a convenir", Modifier.weight(1f))
            ConditionChip(Icons.Default.Security, budget.warrantyInfo ?: "Sin garantía esp.", Modifier.weight(1f))
            ConditionChip(Icons.Default.Payments, budget.paymentMethods ?: "Acordar pago", Modifier.weight(1f))
        }

        Spacer(Modifier.height(32.dp))

        // Botones de acción
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color.White.copy(0.2f))) {
                Text("CERRAR", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Button(onClick = { onViewFullBudget(budget.budgetId) }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaverickBlue)) {
                Text("VER PRESUPUESTO", color = Color.White, fontWeight = FontWeight.Black)
            }
        }
    }
}

// --- GRÁFICOS PERSONALIZADOS PARA EL DASHBOARD ---

@Composable
fun RadarChart(scores: List<Float>, labels: List<String>) {
    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = min(size.width, size.height) / 2
        val numPoints = scores.size
        val angleStep = (2 * Math.PI / numPoints).toFloat()

        // Dibujar telaraña (fondo)
        val pathWeb = Path()
        for (level in 1..4) {
            val levelRadius = radius * (level / 4f)
            pathWeb.reset()
            for (i in 0 until numPoints) {
                val angle = i * angleStep - Math.PI / 2
                val x = center.x + levelRadius * cos(angle).toFloat()
                val y = center.y + levelRadius * sin(angle).toFloat()
                if (i == 0) pathWeb.moveTo(x, y) else pathWeb.lineTo(x, y)
            }
            pathWeb.close()
            drawPath(pathWeb, Color.White.copy(0.1f), style = Stroke(width = 1f))
        }

        // Dibujar polígono de scores
        val pathScore = Path()
        for (i in 0 until numPoints) {
            val angle = i * angleStep - Math.PI / 2
            val scoreRadius = radius * scores[i]
            val x = center.x + scoreRadius * cos(angle).toFloat()
            val y = center.y + scoreRadius * sin(angle).toFloat()
            if (i == 0) pathScore.moveTo(x, y) else pathScore.lineTo(x, y)

            // Dibujar punto
            drawCircle(Color.White, radius = 3f, center = Offset(x, y))
        }
        pathScore.close()

        drawPath(pathScore, MaverickBlue.copy(alpha = 0.4f), style = Fill)
        drawPath(pathScore, MaverickBlue, style = Stroke(width = 3f, join = StrokeJoin.Round))
    }
}

@Composable
fun DonutChart(values: List<Float>, colors: List<Brush>, total: Double) {
    Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        var startAngle = -90f
        val sum = values.sum()

        if (sum == 0f) {
            drawArc(Color.DarkGray, startAngle, 360f, useCenter = false, style = Stroke(width = 30f, cap = StrokeCap.Round))
            return@Canvas
        }

        values.forEachIndexed { index, value ->
            val sweepAngle = (value / sum) * 360f
            drawArc(
                brush = colors[index],
                startAngle = startAngle,
                sweepAngle = sweepAngle - 5f, // Pequeño gap
                useCenter = false,
                style = Stroke(width = 30f, cap = StrokeCap.Round)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun ComparisonBar(label: String, value: Double, average: Double, isLowerBetter: Boolean) {
    val diff = if (average > 0) ((value - average) / average) * 100 else 0.0
    val isGood = if (isLowerBetter) diff <= 0 else diff >= 0
    val diffColor = if (isGood) SuccessGreen else Color(0xFFF43F5E)

    val maxScale = maxOf(value, average) * 1.5
    val valFill = if(maxScale > 0) (value / maxScale).toFloat() else 0f
    val avgFill = if(maxScale > 0) (average / maxScale).toFloat() else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "${if(diff > 0) "+" else ""}${String.format(Locale.getDefault(), "%.1f", diff)}%",
                color = diffColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(10.dp)) {
            Box(modifier = Modifier.fillMaxWidth(avgFill).fillMaxHeight().background(Color.Gray.copy(0.3f), CircleShape))
            Box(modifier = Modifier.fillMaxWidth(valFill).fillMaxHeight().background(diffColor, CircleShape))
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Oferta: $${String.format(Locale.getDefault(), "%,.0f", value)}", color = Color.LightGray, fontSize = 10.sp)
            Text("Promedio: $${String.format(Locale.getDefault(), "%,.0f", average)}", color = Color.Gray, fontSize = 10.sp)
        }
    }
}

@Composable
fun ConditionChip(icon: ImageVector, text: String, modifier: Modifier) {
    Surface(modifier = modifier, color = Color.White.copy(0.05f), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, color = Color.LightGray, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// =================================================================================
// --- SECCIÓN 3: MATRIZ DE COMPARACIÓN MEJORADA ---
// =================================================================================

@Composable
fun MatrixTableSection(budgets: List<BudgetEntity>, rankedBudgets: List<RankedBudget>, onProviderClick: (BudgetEntity) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), color = Color.White.copy(0.02f), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Color.White.copy(0.05f))) {
        Column(Modifier.padding(20.dp)) {
            Text("MATRIZ DE COMPARACIÓN TÉCNICA", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaverickBlue)
            Spacer(Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                Column {
                    // Encabezados (Nombres clickeables)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("CONCEPTO", Modifier.width(110.dp), fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        budgets.forEach { budget ->
                            Text(
                                budget.providerName.split(" ").first().uppercase(),
                                modifier = Modifier.width(100.dp).clickable { onProviderClick(budget) },
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 10.dp), color = Color.White.copy(0.1f))

                    // Datos Estáticos y Métricas
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("MÉTRICA GLOBAL", Modifier.width(110.dp), fontSize = 10.sp, color = Color.Gray)
                        budgets.forEach { b ->
                            val score = rankedBudgets.find { it.budget.budgetId == b.budgetId }?.score ?: 0.0
                            Text("${String.format(Locale.getDefault(), "%.1f", score)} / 10", modifier = Modifier.width(100.dp), fontSize = 10.sp, color = MaverickBlue, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("TIEMPO APROX.", Modifier.width(110.dp), fontSize = 10.sp, color = Color.Gray)
                        budgets.forEach { b ->
                            Text(b.executionTime ?: "-", modifier = Modifier.width(100.dp), fontSize = 9.sp, color = Color.LightGray, textAlign = TextAlign.Center, maxLines = 1)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("GARANTÍA", Modifier.width(110.dp), fontSize = 10.sp, color = Color.Gray)
                        budgets.forEach { b ->
                            Text(b.warrantyInfo ?: "-", modifier = Modifier.width(100.dp), fontSize = 9.sp, color = Color.LightGray, textAlign = TextAlign.Center, maxLines = 1)
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 10.dp), color = Color.White.copy(0.05f))

                    // Precios Desglosados
                    val minItems = budgets.minOfOrNull { it.itemsTotal() } ?: 0.0
                    val minServices = budgets.minOfOrNull { it.servicesTotal() } ?: 0.0
                    val minTaxes = budgets.minOfOrNull { it.taxAmount } ?: 0.0
                    val minTotal = budgets.minOfOrNull { it.grandTotal } ?: 0.0

                    MatrixPriceRow("Materiales", budgets, minItems) { it.itemsTotal() }
                    MatrixPriceRow("Mano de Obra", budgets, minServices) { it.servicesTotal() }
                    MatrixPriceRow("Impuestos", budgets, minTaxes) { it.taxAmount }

                    Spacer(Modifier.height(12.dp))
                    Surface(color = MaverickBlue.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
                        MatrixPriceRow("TOTAL FINAL", budgets, minTotal, isTotal = true) { it.grandTotal }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Toca el nombre del proveedor para un análisis multidimensional detallado.", color = Color.Gray, fontSize = 9.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun MatrixPriceRow(label: String, budgets: List<BudgetEntity>, bestValue: Double, isTotal: Boolean = false, valueExtractor: (BudgetEntity) -> Double) {
    Row(modifier = Modifier.padding(vertical = 6.dp).then(if(isTotal) Modifier.padding(vertical = 8.dp) else Modifier), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.width(110.dp).padding(start = if (isTotal) 8.dp else 0.dp), fontSize = if(isTotal) 10.sp else 11.sp, color = if(isTotal) Color.White else Color.LightGray, fontWeight = if(isTotal) FontWeight.ExtraBold else FontWeight.Normal)
        budgets.forEach { budget ->
            val value = valueExtractor(budget)
            val isBest = value == bestValue && value > 0
            Surface(modifier = Modifier.width(100.dp), color = if (isBest && !isTotal) SuccessGreen.copy(0.1f) else Color.Transparent, shape = RoundedCornerShape(6.dp)) {
                Text("$ ${String.format(Locale.getDefault(), "%,.0f", value)}", modifier = Modifier.padding(vertical = 4.dp), fontSize = if(isTotal) 13.sp else 11.sp, color = if (isBest) SuccessGreen else if(isTotal) Color.White else Color.Gray, fontWeight = if(isBest || isTotal) FontWeight.Black else FontWeight.Normal, textAlign = TextAlign.Center)
            }
        }
    }
    if (!isTotal) HorizontalDivider(color = Color.White.copy(0.03f))
}

// =================================================================================
// --- SECCIÓN 4: COMPONENTES AUXILIARES Y GRÁFICO MASIVO ---
// =================================================================================

@Composable
fun AnalyticsKpiCard(modifier: Modifier, label: String, value: Double, color: Color) {
    Surface(modifier = modifier, color = Color.White.copy(0.03f), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Color.White.copy(0.06f))) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
            Text("$ ${String.format(Locale.getDefault(), "%,.0f", value)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun AiInsightCard(budgets: List<BudgetEntity>, rankedBudgets: List<RankedBudget>) {
    val bestOverall = rankedBudgets.firstOrNull()
    if (bestOverall != null) {
        Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(20.dp), color = Color.Transparent, border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF581C87))))) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.AutoAwesome, null, tint = PremiumGold, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("MAVERICK AI INSIGHT", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF22D3EE), letterSpacing = 1.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("La IA de Maverick sugiere aceptar la oferta de ${bestOverall.budget.providerName}. Aunque puede no ser la más barata absoluta, tiene el mejor balance entre precio, experiencia comprobada (${bestOverall.jobsDone} trabajos) y calidad técnica.", fontSize = 11.sp, color = Color.LightGray, lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
fun MassiveStackedBarChartCard(budgetCount: Int, budgets: List<BudgetEntity>, onMaximizeClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), color = GlassPanel, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Color.White.copy(0.08f))) {
        Column(Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Insights, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("CURVA DE PRECIOS MASIVA ($budgetCount)", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
                }
                IconButton(onClick = onMaximizeClick, modifier = Modifier.size(32.dp).background(Color.White.copy(0.1f), RoundedCornerShape(8.dp))) {
                    Icon(Icons.Default.Fullscreen, "Pantalla Completa", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                if (budgets.isEmpty()) {
                    Text("Sin datos", color = Color.Gray)
                } else {
                    MiniatureStackedChart(budgets)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Toca el icono de maximizar para análisis interactivo", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MiniatureStackedChart(budgets: List<BudgetEntity>) {
    val rawMaxTotal = budgets.maxOfOrNull { it.grandTotal }?.toFloat() ?: 0f
    val maxTotal = if (rawMaxTotal > 0f) rawMaxTotal else 1f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val barWidth = size.width / budgets.size.coerceAtLeast(1)
        val gap = barWidth * 0.2f
        val actualWidth = barWidth - gap

        budgets.forEachIndexed { index, budget ->
            val total = budget.grandTotal.toFloat()
            val mat = budget.itemsTotal().toFloat()
            val lab = budget.servicesTotal().toFloat()
            val tax = budget.taxAmount.toFloat()

            // Transparencia a los valores atípicos altísimos
            val alpha = if (total > maxTotal * 0.8f) 0.3f else 1f

            val hTotal = (total / maxTotal) * size.height
            val hMat = if (total > 0f) (mat / total) * hTotal else 0f
            val hLab = if (total > 0f) (lab / total) * hTotal else 0f
            val hTax = if (total > 0f) (tax / total) * hTotal else 0f

            val x = index * barWidth + gap / 2

            val yTax = size.height - hTotal
            drawRect(Color(0xFFF43F5E), Offset(x, yTax), Size(actualWidth, hTax), alpha = alpha)

            val yLab = yTax + hTax
            drawRect(Color(0xFFA855F7), Offset(x, yLab), Size(actualWidth, hLab), alpha = alpha)

            val yMat = yLab + hLab
            drawRect(Color(0xFF3B82F6), Offset(x, yMat), Size(actualWidth, hMat), alpha = alpha)
        }
    }
}

// =================================================================================
// --- PANTALLA COMPLETA APAISADA (MODO INMERSIVO TÁCTIL V5) ---
// =================================================================================

@Composable
fun FullscreenLandscapeChart(
    state: MarketAnalyticsState,
    onClose: () -> Unit,
    onViewBudget: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var isolationMode by remember { mutableStateOf("total") }
    var isOptimalSorted by remember { mutableStateOf(false) }
    var selectedBudgetId by remember { mutableStateOf<String?>(null) }

    val displayItems = remember(state.items, isOptimalSorted) {
        if (isOptimalSorted) {
            state.items.sortedWith(compareBy({ it.isIrrisory }, { !it.isOptimal }, { it.total }))
        } else {
            state.items
        }
    }

    val validDisplayVals = displayItems.filter { !it.isIrrisory }.map {
        when(isolationMode) {
            "mat" -> it.mat
            "lab" -> it.lab
            "tax" -> it.tax
            else -> it.total
        }
    }

    val rawMax = validDisplayVals.maxOrNull() ?: 0.0
    val vMax = if (rawMax > 0.0) rawMax else 1.0
    val vMin = validDisplayVals.minOrNull() ?: 0.0
    val vAvg = if(validDisplayVals.isNotEmpty()) validDisplayVals.average() else 0.0
    val visualMax = vMax * 1.15

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { selectedBudgetId = null },
        color = DarkBg
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF2197F5).copy(0.08f), Color.Transparent), center = Offset(500f, 1500f), radius = 1500f)))

        Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.displayCutout)) {

            // --- TOP HUD COMPACTO ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF05070A).copy(0.85f))
                    .border(1.dp, Color.White.copy(0.08f))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("ANALIZANDO", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${state.items.size}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(0.1f)))
                    Column {
                        Text("PROMEDIO", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF2197F5), letterSpacing = 1.sp)
                        Text("$ ${(state.avgTotal/1000).toInt()}k", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF2197F5))
                    }
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(0.1f)))
                    Column {
                        Text("IRRISORIOS", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFFF43F5E), letterSpacing = 1.sp)
                        Text("${state.items.size - state.validCount}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFF43F5E))
                    }

                    Surface(
                        onClick = {
                            isOptimalSorted = !isOptimalSorted
                            coroutineScope.launch { listState.animateScrollToItem(0) }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOptimalSorted) SuccessGreen else SuccessGreen.copy(0.1f),
                        border = BorderStroke(1.dp, SuccessGreen.copy(0.4f)),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrackChanges, null, tint = if (isOptimalSorted) DarkBg else SuccessGreen, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isOptimalSorted) "VIENDO ÓPTIMOS" else "AGRUPAR ÓPTIMOS", fontSize = 8.sp, fontWeight = FontWeight.Black, color = if (isOptimalSorted) DarkBg else SuccessGreen)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("VISTA:", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Gray)

                    IsolationButton("Total", Icons.Default.Layers, Color.White, isolationMode == "total") { isolationMode = "total" }
                    IsolationButton("Mat", Icons.Default.Square, Color(0xFF3B82F6), isolationMode == "mat") { isolationMode = "mat" }
                    IsolationButton("Obra", Icons.Default.Square, Color(0xFFA855F7), isolationMode == "lab") { isolationMode = "lab" }
                    IsolationButton("Tasa", Icons.Default.Square, Color(0xFFF43F5E), isolationMode == "tax") { isolationMode = "tax" }

                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onClose, modifier = Modifier.background(Color.White.copy(0.05f), RoundedCornerShape(12.dp))) {
                        Icon(Icons.Default.CloseFullscreen, null, tint = Color.Gray)
                    }
                }
            }

            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                val hPx = constraints.maxHeight.toFloat()

                val animMax by animateFloatAsState(targetValue = (vMax / visualMax).toFloat(), animationSpec = tween(600), label = "max")
                val animAvg by animateFloatAsState(targetValue = (vAvg / visualMax).toFloat(), animationSpec = tween(600), label = "avg")
                val animMin by animateFloatAsState(targetValue = (vMin / visualMax).toFloat(), animationSpec = tween(600), label = "min")

                Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 40.dp, top = 20.dp)) {
                    val w = size.width
                    val h = size.height
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                    val yMax = h - (h * animMax)
                    drawLine(Color(0xFFF43F5E).copy(0.5f), Offset(0f, yMax), Offset(w, yMax), strokeWidth = 2f, pathEffect = pathEffect)

                    val yAvg = h - (h * animAvg)
                    drawLine(Color(0xFF2197F5).copy(0.5f), Offset(0f, yAvg), Offset(w, yAvg), strokeWidth = 2f, pathEffect = pathEffect)

                    val yMin = h - (h * animMin)
                    drawLine(Color(0xFF10B981).copy(0.5f), Offset(0f, yMin), Offset(w, yMin), strokeWidth = 2f, pathEffect = pathEffect)
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(bottom = 40.dp, top = 20.dp)) {
                    PeakLabel("Pico Máx: $${(vMax/1000).toInt()}k", Color(0xFFF43F5E), animMax, this.maxHeight)
                    PeakLabel("Promedio: $${(vAvg/1000).toInt()}k", Color(0xFF2197F5), animAvg, this.maxHeight)
                    PeakLabel("Pico Mín: $${(vMin/1000).toInt()}k", Color(0xFF10B981), animMin, this.maxHeight)
                }

                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(bottom = 10.dp, top = 20.dp),
                    contentPadding = PaddingValues(horizontal = 60.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    items(displayItems, key = { it.budget.budgetId }) { item ->
                        SuperimposedBarItem(
                            item = item,
                            mode = isolationMode,
                            visualMax = visualMax,
                            isSelected = selectedBudgetId == item.budget.budgetId,
                            onSelect = { selectedBudgetId = item.budget.budgetId },
                            onViewBudget = { onViewBudget(item.budget.budgetId) },
                            maxHeightPx = hPx
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IsolationButton(label: String, icon: ImageVector, color: Color, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isActive) color.copy(alpha = 0.15f) else Color.White.copy(0.05f),
        border = BorderStroke(1.dp, if (isActive) color else Color.White.copy(0.1f))
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(10.dp))
            Spacer(Modifier.width(4.dp))
            Text(label.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun BoxScope.PeakLabel(text: String, color: Color, percentage: Float, boxHeight: Dp) {
    val alphaAnim by animateFloatAsState(targetValue = 1f, animationSpec = tween(600), label = "alpha")
    Box(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .offset {
                IntOffset(
                    x = 10.dp.roundToPx(),
                    y = -((percentage * boxHeight.toPx()).toInt())
                )
            }
            .graphicsLayer { alpha = alphaAnim }
            .offset(y = (-4).dp)
            .background(Color(0xFF020408).copy(0.8f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, color = color, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun SuperimposedBarItem(
    item: ChartBudgetItem,
    mode: String,
    visualMax: Double,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onViewBudget: () -> Unit,
    maxHeightPx: Float
) {
    val totalHeightRatio = (item.total / visualMax).toFloat().coerceIn(0f, 1f)
    val matHeightRatio = (item.mat / visualMax).toFloat().coerceIn(0f, 1f)
    val labHeightRatio = (item.lab / visualMax).toFloat().coerceIn(0f, 1f)
    val taxHeightRatio = (item.tax / visualMax).toFloat().coerceIn(0f, 1f)

    val hTotal by animateFloatAsState(if(mode == "total") totalHeightRatio else 0f, tween(500), label = "hTotal")
    val hMat by animateFloatAsState(if(mode == "total" || mode == "mat") matHeightRatio else 0f, tween(500), label = "hMat")
    val hLab by animateFloatAsState(if(mode == "total" || mode == "lab") labHeightRatio else 0f, tween(500), label = "hLab")
    val hTax by animateFloatAsState(if(mode == "total" || mode == "tax") taxHeightRatio else 0f, tween(500), label = "hTax")

    val wMat by animateDpAsState(if(mode == "mat") 32.dp else 22.dp, label = "wMat")
    val alphaMat by animateFloatAsState(if(mode == "total" || mode == "mat") 1f else 0f, label = "aMat")

    val wLab by animateDpAsState(if(mode == "lab") 32.dp else 14.dp, label = "wLab")
    val alphaLab by animateFloatAsState(if(mode == "total" || mode == "lab") 1f else 0f, label = "aLab")

    val wTax by animateDpAsState(if(mode == "tax") 32.dp else 6.dp, label = "wTax")
    val alphaTax by animateFloatAsState(if(mode == "total" || mode == "tax") 1f else 0f, label = "aTax")

    Box(
        modifier = Modifier
            .width(52.dp)
            .fillMaxHeight()
            .background(if (item.isOptimal) SuccessGreen.copy(alpha = 0.08f) else Color.Transparent)
            .border(width = if (item.isOptimal) 1.dp else 0.dp, color = if (item.isOptimal) SuccessGreen.copy(0.2f) else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onSelect() }
            .zIndex(if (isSelected) 100f else 0f),
        contentAlignment = Alignment.BottomCenter
    ) {
        val fallbackAvatar = rememberVectorPainter(Icons.Default.Person)
        val avatarScale by animateFloatAsState(if(isSelected) 1.3f else 1f, label = "avatarScale")
        val avatarColor = if(isSelected) Color(0xFF2197F5) else Color.White.copy(0.1f)

        Box(modifier = Modifier.padding(bottom = 5.dp).zIndex(20f)) {
            AsyncImage(
                model = item.budget.providerPhotoUrl,
                contentDescription = null,
                fallback = fallbackAvatar,
                error = fallbackAvatar,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer { scaleX = avatarScale; scaleY = avatarScale }
                    .clip(CircleShape)
                    .border(2.dp, avatarColor, CircleShape)
                    .background(Color(0xFF161C24)),
                contentScale = ContentScale.Crop
            )
            if (item.isIrrisory) {
                Text("⚠️", fontSize = 14.sp, modifier = Modifier.align(Alignment.TopCenter).offset(y = (-18).dp))
            }
        }

        val barAlpha = if (item.isIrrisory) 0.3f else if (isSelected) 1f else 0.8f

        // ZONA DE BARRAS
        Box(modifier = Modifier.padding(bottom = 35.dp).fillMaxSize().alpha(barAlpha), contentAlignment = Alignment.BottomCenter) {
            Box(modifier = Modifier.width(32.dp).fillMaxHeight(hTotal).background(if(isSelected) Color.White.copy(0.15f) else Color.White.copy(0.03f), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).zIndex(1f))
            Box(modifier = Modifier.width(wMat).fillMaxHeight(hMat).alpha(alphaMat).background(ColorMaterial, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).shadow(10.dp).zIndex(if(mode == "mat") 10f else 2f))
            Box(modifier = Modifier.width(wLab).fillMaxHeight(hLab).alpha(alphaLab).background(ColorLabor, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).shadow(10.dp).zIndex(if(mode == "lab") 10f else 3f))
            Box(modifier = Modifier.width(wTax).fillMaxHeight(hTax).alpha(alphaTax).background(ColorTax, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).shadow(10.dp).zIndex(if(mode == "tax") 10f else 4f))
        }

        if (isSelected) {
            val density = LocalDensity.current
            val bottomPaddingPx = with(density) { 35.dp.toPx() }
            val extraHoverPx = with(density) { 20.dp.toPx() }
            val popupEstimatedHeightPx = with(density) { 200.dp.toPx() }
            val projectedY = -((hTotal * (maxHeightPx - bottomPaddingPx)) + bottomPaddingPx + extraHoverPx)
            val maxSafeY = -(maxHeightPx - popupEstimatedHeightPx)
            val topOfBarYOffset = projectedY.toFloat().coerceAtLeast(maxSafeY).toInt()

            Popup(
                alignment = Alignment.BottomCenter,
                offset = IntOffset(0, topOfBarYOffset),
                properties = PopupProperties(clippingEnabled = false, excludeFromSystemGesture = true)
            ) {
                var animateIn by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { animateIn = true }

                AnimatedVisibility(
                    visible = animateIn,
                    enter = fadeIn() + scaleIn(transformOrigin = TransformOrigin(0.5f, 1f)),
                    exit = fadeOut()
                ) {
                    Surface(
                        modifier = Modifier.width(170.dp),
                        color = Color(0xFF0A0E14).copy(alpha = 0.95f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF2197F5).copy(0.4f)),
                        shadowElevation = 25.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, Color.White.copy(0.1f)), RoundedCornerShape(6.dp)).padding(bottom = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Final:", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("$${(item.total/1000).toInt()}k", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Material:", color = Color(0xFF3B82F6), fontSize = 8.sp); Text("$${(item.mat/1000).toInt()}k", color = Color(0xFF3B82F6), fontSize = 8.sp, fontWeight = FontWeight.Bold) }
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("M. Obra:", color = Color(0xFFA855F7), fontSize = 8.sp); Text("$${(item.lab/1000).toInt()}k", color = Color(0xFFA855F7), fontSize = 8.sp, fontWeight = FontWeight.Bold) }
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Tasas:", color = Color(0xFFF43F5E), fontSize = 8.sp); Text("$${(item.tax/1000).toInt()}k", color = Color(0xFFF43F5E), fontSize = 8.sp, fontWeight = FontWeight.Bold) }

                            Spacer(Modifier.height(6.dp))
                            if (item.isOptimal) {
                                Text("ZONA ÓPTIMA", modifier = Modifier.fillMaxWidth().background(SuccessGreen.copy(0.1f), RoundedCornerShape(4.dp)).border(1.dp, SuccessGreen.copy(0.2f), RoundedCornerShape(4.dp)).padding(vertical = 2.dp), textAlign = TextAlign.Center, color = SuccessGreen, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            }
                            if (item.isIrrisory) {
                                Text("⚠️ ANOMALÍA", modifier = Modifier.fillMaxWidth().background(Color.Red.copy(0.1f), RoundedCornerShape(4.dp)).border(1.dp, Color.Red.copy(0.2f), RoundedCornerShape(4.dp)).padding(vertical = 2.dp), textAlign = TextAlign.Center, color = Color.Red, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            }

                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = Color.White.copy(0.1f))
                            Spacer(Modifier.height(6.dp))
                            Text(item.budget.providerName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            Text(item.budget.providerCompanyName ?: "Profesional Independiente", color = Color(0xFF22D3EE), fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = onViewBudget,
                                modifier = Modifier.fillMaxWidth().height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                            ) {
                                Text("DEEP DIVE", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 0.5.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}









