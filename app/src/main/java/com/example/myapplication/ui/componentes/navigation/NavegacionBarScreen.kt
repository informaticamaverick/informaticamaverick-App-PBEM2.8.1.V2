package com.example.myapplication.ui.componentes.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.myapplication.ui.pantallas.home.Screen
import com.example.myapplication.viewmodel.home.NavegacionBarViewModel
import kotlinx.coroutines.launch

/**
 * NavegacionBarScreen.kt
 * Propósito: Ensamblaje funcional de la barra de navegación (Sección).
 * Funcionamiento: Conecta las piezas con el ViewModel y el NavController.
 * Relación: Inyectado en el Scaffold global de NavegacionCajaPrincipal.kt.
 */

@Composable
fun NavegacionBarV3(
    navController: NavHostController,
    currentRoute: String?,
    viewModel: NavegacionBarViewModel
) {
    val navItems = remember {
        listOf(
            Screen.Home,
            Screen.Concursos,
            Screen.Chat,
            Screen.Calendar,
            Screen.Promo
        )
    }

    val alertas by viewModel.alertasNavegacion.collectAsStateWithLifecycle()
    val navigationInsets = WindowInsets.navigationBars.asPaddingValues()
    val bottomPadding = navigationInsets.calculateBottomPadding()
    val scope = rememberCoroutineScope()

    PiezaFondoNavV3(paddingInferior = bottomPadding) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { screen ->
                val isSelected = currentRoute?.startsWith(screen.route.split("?").first()) == true
                
                val tieneNotif = when (screen) {
                    is Screen.Chat -> alertas.tieneChat
                    is Screen.Concursos -> alertas.tienePresupuesto
                    is Screen.Calendar -> alertas.tieneCalendario
                    else -> false
                }

                PiezaIconoDestinoV3(
                    screen = screen,
                    estaSeleccionado = isSelected,
                    tieneNotificacion = tieneNotif,
                    alHacerClick = {
                        android.util.Log.d("MAV_NAV", "🖱️ [CLICK_NAV_BAR] Destino: ${screen.route} | IsSelected: $isSelected")
                        scope.launch {
                            val targetBase = screen.route.split("?").first().split("/").first()
                            val currentBase = currentRoute?.split("?")?.first()?.split("/")?.first()
                            
                            // 🔥 [v2026.ELITE]: Resolución de destino simplificada
                            val destination = if (screen is Screen.Chat) "chat" else screen.route

                            // Si es el mismo destino base, limpiamos estados de UI específicos
                            if (targetBase == currentBase) {
                                android.util.Log.d("MAV_NAV", "📍 [NAV] Destino base coincide, limpiando HUD. Base: $targetBase")
                                viewModel.dispararAccion("CLEAN_HUD") // Acción táctica
                                
                                // Si estamos en Chat pero en una conversación, volver a la lista
                                if (screen is Screen.Chat && currentRoute != "chat") {
                                     navController.navigate("chat") {
                                         popUpTo("chat") { inclusive = true }
                                     }
                                }
                                return@launch
                            }

                            android.util.Log.d("MAV_NAV", "🚗 [NAV] Navegando a: $destination | Base actual: $currentBase")
                            viewModel.alCambiarRuta(targetBase)
                            navController.navigate(destination) {
                                // Para Home, forzamos la limpieza del stack si venimos de otro lado para evitar restaurar estados de deep link
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = (destination != Screen.Home.route)
                                }
                                launchSingleTop = true
                                restoreState = (destination != Screen.Home.route)
                            }
                        }
                    }
                )
            }
        }
    }
}

// ==================================================================================
// --- PREVIEW ---
// ==================================================================================

@Preview(name = "Barra Completa V3", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewNavegacionBarV3() {
    // Nota: El ViewModel y NavController reales no se pueden instanciar fácilmente en Preview.
    // Se recomienda usar una versión "Stateless" para previews más profundos si es necesario.
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Vista Previa de Barra Ensamblada", color = androidx.compose.ui.graphics.Color.White)
    }
}
