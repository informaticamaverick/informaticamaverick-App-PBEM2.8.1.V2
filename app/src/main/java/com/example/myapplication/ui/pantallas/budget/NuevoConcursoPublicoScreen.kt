package com.example.myapplication.ui.pantallas.budget

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.componentes.sistema.cabecera.BotonBackCabeceraV3
import com.example.myapplication.ui.componentes.sistema.cabecera.ColumnaTituloSeccionV3
import com.example.myapplication.ui.componentes.sistema.cabecera.EmojiImpactoV3
import com.example.myapplication.ui.componentes.sistema.cabecera.MoldeCabeceraSuperiorPantallas
import com.example.myapplication.ui.pantallas.budget.armador.ArmadorConcursoLienzo
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.viewmodel.budget.BorradorConcursoViewModel

/**
 * --- PANTALLA DE NUEVA LICITACIÓN PÚBLICAS (v2026.ELITE) ---
 * [PROPÓSITO]: Orquestar el flujo de creación de un nuevo concurso en pantalla completa.
 * [LEY #12]: Soberanía del Coordinador (Modo Piloto).
 * [LEY #10]: Screen Anatomy. Caja (Screen) > Lienzo.
 */
@Composable
fun NuevoConcursoPublicoScreen(
    onBack: () -> Unit,
    modeloVista: BorradorConcursoViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
) {
    // 🔥 [ELITE]: Retroceso Soberano
    BackHandler {
        modeloVista.coordinador.ejecutarCierreMaestro()
        onBack()
    }

    val beConfig = remember { com.example.myapplication.ui.componentes.be.modelos.ContextoHUD.WIZARD_CONCURSO.crearConfiguracionBase() }

    // 🔥 [ELITE]: Reclamo de Soberanía (Ley #12)
    DisposableEffect(Unit) {
        modeloVista.idSoberania = beConfig.id // 🔥 [FIX]: Sincronizar ID
        modeloVista.configurarHUD(true)
        modeloVista.coordinador.navCoordinador.registrarPantalla(beConfig)
        onDispose {
            modeloVista.configurarHUD(false)
            modeloVista.coordinador.navCoordinador.removerPantalla(beConfig.id)
        }
    }

    // 🔥 [ELITE]: Escucha de Éxito Soberano para navegar atrás
    LaunchedEffect(Unit) {
        modeloVista.finalizarExitosamente.collect {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            MoldeCabeceraSuperiorPantallas(
                fraccionColapso = 0f, // Siempre expandida en Wizard
                slotIzquierdo = {
                    BotonBackCabeceraV3(onClick = onBack)
                },
                slotCentral = {
                    ColumnaTituloSeccionV3(
                        titulo = "Nueva Licitación",
                        subtitulo = "Sigue los pasos con Be",
                        fraccionColapso = 0f
                    )
                },
                slotDerecho = {
                    EmojiImpactoV3(
                        emoji = "📝",
                        fraccionColapso = 0f
                    )
                }
            )
        },
        containerColor = SharedPalette.ROG_Dark_Bg
    ) { relleno ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = relleno.calculateTopPadding())
                .background(SharedPalette.ROG_Dark_Bg)
        ) {
            ArmadorConcursoLienzo(modeloVista = modeloVista)
        }
    }
}
