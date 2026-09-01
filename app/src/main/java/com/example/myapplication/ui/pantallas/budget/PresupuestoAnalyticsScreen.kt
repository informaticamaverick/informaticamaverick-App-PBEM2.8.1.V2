package com.example.myapplication.ui.pantallas.budget

import android.app.Activity
import android.util.Log
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity
import com.example.myapplication.core.dominio.motores.EstadoAnaliticaMercado
import com.example.myapplication.core.dominio.motores.ModeloPresupuestoAnalitico
import com.example.myapplication.core.dominio.motores.PresupuestoClasificado
import com.example.myapplication.ui.pantallas.budget.analiticas.*
import com.example.myapplication.viewmodel.budget.PresupuestoAnalyticsViewModel
import com.example.myapplication.ui.componentes.be.vm.BeCerebroViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.uishared.ui.components.BloquearEscalaFuente
import com.example.myapplication.ui.componentes.sistema.lista.MoldeSheetEmergenteV3
import com.example.myapplication.ui.componentes.sistema.lista.MoldeSheetEliteV3

/**
 * --- PANTALLA DE ANALÍTICAS DE PRESUPUESTOS (V2026.ELITE) ---
 * Título: Comparativa Analítica de Presupuestos
 * Propósito: Ofrecer una visión multidimensional y técnica de los presupuestos recibidos.
 * Funcionamiento Interno: Implementa la Ley #1 (Pantallas Tontas) delegando cálculos al ViewModel.
 * Relación: Invocada desde el Archivero o la Bandeja de Licitaciones.
 * [LEY #9]: Estándar Mav en Español.
 */

private val DarkBg = Color(0xFF020408)
private val GlassPanel = Color(0xFF161C24)
private val appBlue = Color(0xFF2197F5)

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetComparisonAnalytics(
    tender: ConcursoPublicoEntity,
    budgets: List<ModeloPresupuestoAnalitico>, 
    onBack: () -> Unit,
    onViewBudgetDetail: (String) -> Unit,
    viewModel: PresupuestoAnalyticsViewModel? = null
) {
    // 🔥 [ELITE] Obtenemos el ViewModel hilt si no se pasó uno (para previews o inyección externa)
    val vm: PresupuestoAnalyticsViewModel = viewModel ?: hiltViewModel()
    val cerebro: BeCerebroViewModel = hiltViewModel()

    val beConfig = remember { com.example.myapplication.ui.componentes.be.modelos.ContextoHUD.ANALITICAS.crearConfiguracionBase() }

    DisposableEffect(Unit) {
        cerebro.navCoordinador.registrarPantalla(beConfig)
        onDispose {
            cerebro.navCoordinador.removerPantalla(beConfig.id)
        }
    }

    // Si no se inicializó, lo hacemos con los datos que llegan (Ley de compatibilidad)
    LaunchedEffect(budgets) {
        if (budgets.isNotEmpty()) {
            vm.inicializarConPresupuestos(budgets.map { it.presupuesto.cabecera.idPresupuesto })
        }
    }

    val estadoMercado by vm.estadoMercado.collectAsStateWithLifecycle()
    val rankedBudgets by vm.presupuestosRankeados.collectAsStateWithLifecycle()
    val analiticos by vm.presupuestosAnaliticos.collectAsStateWithLifecycle()
    val concurso by vm.concursoVirtual.collectAsStateWithLifecycle()

    var seleccionadoParaProfundizar by remember { mutableStateOf<PresupuestoClasificado?>(null) }
    var mostrarGraficoPantallaCompleta by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (mostrarGraficoPantallaCompleta) {
        Dialog(
            onDismissRequest = { mostrarGraficoPantallaCompleta = false }, 
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            val context = LocalContext.current
            val dialogView = LocalView.current
            DisposableEffect(Unit) {
                val activity = context.findActivity(); val dialogWindow = (dialogView.parent as? DialogWindowProvider)?.window
                dialogWindow?.let { window ->
                    window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT); window.setBackgroundDrawableResource(android.R.color.transparent)
                    WindowCompat.setDecorFitsSystemWindows(window, false); val insetsController = WindowCompat.getInsetsController(window, dialogView)
                    insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE; insetsController.hide(WindowInsetsCompat.Type.systemBars())
                }
                val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                onDispose { activity?.requestedOrientation = originalOrientation }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                GraficoComparativoHorizontal(
                    estadoMercado = estadoMercado, 
                    alCerrar = { mostrarGraficoPantallaCompleta = false }, 
                    alVerPresupuesto = { id: String -> 
                        mostrarGraficoPantallaCompleta = false
                        onViewBudgetDetail(id)
                    }
                )
            }
        }
    }

    BloquearEscalaFuente {
        MoldeSheetEliteV3(
            estaVisible = true,
            alCerrar = onBack,
            titulo = "ANÁLISIS DE MERCADO",
            subtitulo = "${concurso?.titulo ?: tender.titulo} • ${tender.direccionLocalidad ?: "ZONA PBEM"}",
            icono = "📊",
            colorAcento = appBlue,
            paddingSuperiorHUD = 0.dp // No requiere espacio para búsqueda
        ) {
            if (estadoMercado.estaAnalizando) {
                Box(
                    modifier = Modifier.fillMaxSize(), 
                    contentAlignment = Alignment.Center
                ) { 
                    CircularProgressIndicator(color = appBlue) 
                }
            } else {
                ArmadorCuerpoAnaliticas(
                    estadoMercado = estadoMercado,
                    presupuestosAnaliticos = analiticos,
                    presupuestosRankeados = rankedBudgets,
                    alSeleccionarPrestador = { seleccionadoParaProfundizar = it },
                    alHacerClickGrafico = { 
                        Log.d("MavElite", "[ACCION] Abriendo Pantalla Completa")
                        mostrarGraficoPantallaCompleta = true 
                    }
                )
            }
        }
    }

    // --- DIALOGO DE PROFUNDIZACIÓN (DEEP DIVE) ---
    if (seleccionadoParaProfundizar != null) {
        ModalBottomSheet(
            onDismissRequest = { seleccionadoParaProfundizar = null },
            sheetState = sheetState,
            containerColor = GlassPanel,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
        ) {
            HojaDetalleProfundoPrestador(
                budget = seleccionadoParaProfundizar!!.presupuesto,
                rankedInfo = seleccionadoParaProfundizar,
                marketAvgTotal = estadoMercado.promedioTotal,
                alVerPresupuestoCompleto = { budgetId ->
                    onViewBudgetDetail(budgetId)
                    seleccionadoParaProfundizar = null
                },
                onDismiss = { seleccionadoParaProfundizar = null }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF020408)
@Composable
private fun PreviewBudgetComparisonAnalytics() {
    val mockTender = ConcursoPublicoEntity(
        idConcurso = "1",
        titulo = "Remodelación Living",
        direccionLocalidad = "Tucumán"
    )
    val mockBudgets = listOf(
        ModeloPresupuestoAnalitico(
            presupuesto = PresupuestoConItems(
                cabecera = PresupuestoFinalEntity(idPresupuesto = "1", totalGeneral = 45000.0, nombrePrestador = "Maverick"),
                lineas = emptyList(),
                finanzas = emptyList()
            ),
            nombrePrestador = "Maverick",
            fotoPrestador = null,
            direccionPrestador = null
        )
    )

    com.example.myapplication.ui.estilos.ClienteTheme {
        BudgetComparisonAnalytics(
            tender = mockTender,
            budgets = mockBudgets,
            onBack = {},
            onViewBudgetDetail = {}
        )
    }
}
