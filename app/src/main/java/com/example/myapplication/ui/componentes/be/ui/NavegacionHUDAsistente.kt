package com.example.myapplication.ui.componentes.be.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.myapplication.ui.componentes.be.vm.*
import com.example.myapplication.ui.componentes.be.modelos.*
import com.example.myapplication.ui.componentes.be.ui.BeAssistantEyes
import com.example.myapplication.ui.componentes.be.ui.BarraBusquedaTacticaV3
import com.example.myapplication.ui.componentes.sistema.cabecera.MoldeCabeceraBusquedaBeV3
import com.example.myapplication.viewmodel.home.CategoryViewModel

/**
 * --- NAVEGACIÓN HUD ASISTENTE (EL MAESTRO DEL HUD v2026.ELITE) ---
 * [PROPÓSITO]: Orquestar el asistente Be y la barra de búsqueda táctica global.
 */
@Composable
fun NavegacionHUDAsistente(
    beCuerpoVm: BeCuerpoViewModel,
    beBusquedaVm: BeBusquedaViewModel = hiltViewModel(),
    beFisicaVm: BeFisicaViewModel = hiltViewModel()
) {
    val beUiState by beCuerpoVm.uiState.collectAsStateWithLifecycle()
    val physicsState by beFisicaVm.estadoFisico.collectAsStateWithLifecycle()
    val estaBusquedaActiva by beBusquedaVm.estaBusquedaActiva.collectAsStateWithLifecycle()
    val consultaBusquedaFlow = beBusquedaVm.consultaBusqueda
    
    // 🔥 [SUPREME.FIX]: El recolector de teclado debe estar fuera de AnimatedVisibility 
    // para poder capturar el evento y activar la búsqueda si está cerrada.
    LaunchedEffect(Unit) {
        beCuerpoVm.coordinador.eventoAccion.collect { actionId ->
            if (actionId == "teclado") {
                if (!estaBusquedaActiva) {
                    beBusquedaVm.alternarBusqueda()
                } else {
                    beBusquedaVm.abrirTeclado()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        
        // --- 0. CABECERA DE BÚSQUEDA SOBERANA (BE v2026.ELITE) ---
        AnimatedVisibility(
            visible = estaBusquedaActiva,
            modifier = Modifier.zIndex(BeZIndex.ASISTENTE_ESCÁNER),
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
            val eyeScaleY by animateFloatAsState(targetValue = if (beUiState.estaDormido || physicsState.estaParpadeando) 0.1f else 1f, label = "parpadeo_cabecera")

            LaunchedEffect(estaBusquedaActiva) {
                if (estaBusquedaActiva && beUiState.configuracion.abrirTecladoEnBusqueda) {
                    kotlinx.coroutines.delay(300L)
                    focusRequester.requestFocus()
                }
            }

            LaunchedEffect(Unit) {
                beBusquedaVm.solicitarTeclado.collect {
                    focusRequester.requestFocus()
                }
            }

            MoldeCabeceraBusquedaBeV3(
                slotBusqueda = {
                    BarraBusquedaTacticaV3(
                        alCambiarConsulta = { beBusquedaVm.actualizarConsulta(it) },
                        flujoConsulta = consultaBusquedaFlow,
                        alBuscar = { beBusquedaVm.alternarBusqueda() },
                        requeridorFoco = focusRequester,
                        alLimpiarTexto = { beBusquedaVm.actualizarConsulta("") },
                        textoPista = beUiState.configuracion.pistaBusqueda
                    )
                },
                slotIdentidad = {
                    BeAssistantEyes(
                        size = 72.dp,
                        emocion = EmocionBe.NORMAL,
                        eyeScaleY = eyeScaleY,
                        pupilaX = 5f, // 🔥 [LOOK RIGHT]: Be ahora mira hacia la barra a su derecha
                        estaDormido = beUiState.estaDormido
                    )
                }
            )
        }

        // --- 1. ASISTENTE BE (FAB) ---
        // 🔥 [ELITE]: Asistente en posición fija (Be vive en un solo lugar)
        val beVerticalBias = 0.85f

        AnimatedVisibility(
            visible = beUiState.mostrarBe, // 🔥 [SANEAMIENTO]: El estado ya está consolidado en el VM
            modifier = Modifier.zIndex(BeZIndex.ASISTENTE_FAB), 
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))) {
                val accionesAsistente = remember(beCuerpoVm, beBusquedaVm) {
                    AccionesAsistenteBe(
                        alHacerClick = { beCuerpoVm.alHacerClickBe(alAlternarBusqueda = { beBusquedaVm.alternarBusqueda() }) },
                        alHacerDobleClick = { beCuerpoVm.alHacerDobleClickBe() },
                        alHacerClickLargo = { beCuerpoVm.alHacerClickLargoBe() },
                        alCambiarConsultaBusqueda = { beBusquedaVm.actualizarConsulta(it) },
                        alEnviarBusqueda = { beBusquedaVm.alternarBusqueda() },
                        alHacerClickAccionBurbuja = { beCuerpoVm.ocultarBurbuja() },
                        alHacerClickAccionReaccion = { beCuerpoVm.dispararAccion(it) },
                        alCerrarReaccion = { beCuerpoVm.ocultarBurbuja() }
                    )
                }

                FabAsistenteBe(
                    modifier = Modifier.align(BiasAlignment(horizontalBias = 1f, verticalBias = beVerticalBias)),
                    estadoUi = beUiState,
                    estadoFisico = physicsState,
                    acciones = accionesAsistente
                )
            }
        }
    }
}

