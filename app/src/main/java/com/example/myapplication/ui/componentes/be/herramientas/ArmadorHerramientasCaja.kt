package com.example.myapplication.ui.componentes.be.herramientas

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.myapplication.ui.componentes.be.modelos.*
import com.example.myapplication.ui.estilos.PBEMTheme
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * --- CAJA DE HERRAMIENTAS BE (v2026.ELITE) ---
 * [PROPÓSITO]: Contenedor visual soberano de la barra de acciones/log de Be.
 * [LEY #10]: Screen Anatomy. Caja > Lienzo.
 * [LEY #9]: Estándar Mav en Español.
 */

@Composable
fun ArmadorHerramientasCaja(
    estadoUi: EstadoUiBeAsistente,
    estaVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val modo = estadoUi.modoBarraHerramientas
    val toastActivo = estadoUi.toastActivo
    val visibleRealmente = (estaVisible && (
        estadoUi.configuracion.mostrarHerramientas && (
            estadoUi.herramientasPrimarias.isNotEmpty() || 
            estadoUi.herramientasSistema.isNotEmpty() || 
            estadoUi.herramientasNavegacion.isNotEmpty() || 
            estadoUi.herramientasEdicion.isNotEmpty()
        )
    )) || estadoUi.estaBusquedaActiva || toastActivo != null

    Box(
        modifier = modifier
            .wrapContentWidth()
            .height(80.dp)
            .zIndex(100f),
        contentAlignment = Alignment.BottomEnd
    ) {
        AnimatedVisibility(
            visible = visibleRealmente,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.padding(end = 4.dp)
        ) {
            ArmadorHerramientasLienzo(
                estadoUi = estadoUi
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewArmadorHerramientasCaja() {
    PBEMTheme {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.CenterEnd) {
            ArmadorHerramientasCaja(
                estaVisible = true,
                estadoUi = EstadoUiBeAsistente(
                    toastActivo = BeToastState("Procesando licitación...", TipoBeToast.PROCESANDO)
                )
            )
        }
    }
}
