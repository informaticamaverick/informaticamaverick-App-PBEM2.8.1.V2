package com.example.myapplication.prestador.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.myapplication.prestador.ui.pantallas.dashboard.componentes.ChatSoporteSheet
import com.example.myapplication.prestador.ui.pantallas.dashboard.componentes.MatriculaVerificadaDialog
import com.example.myapplication.prestador.ui.pantallas.login.CuentaSuspendidaDialog
import androidx.compose.ui.graphics.Color
import com.example.myapplication.prestador.viewmodel.global.SesionGuardianViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PrestadorNavGraph(
    navController: NavHostController,
    initialTenderId: String? = null,
    startDestinationRoute: String? = null
) {


    val sesionGuardian: SesionGuardianViewModel = hiltViewModel()
    val motivoSuspensionEnVivo by sesionGuardian.cuentaSuspendida.collectAsState()
    val matriculaVerificadaEnVivo by sesionGuardian.matriculaVerificada.collectAsState()

    val startDestination = startDestinationRoute ?: if (FirebaseAuth.getInstance().currentUser != null) {
        if (!initialTenderId.isNullOrBlank()) PrestadorRoutes.Dashboard.createRoute(initialTenderId)
        else PrestadorRoutes.Dashboard.route
    } else {
        PrestadorRoutes.Login.route
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.92f, animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.92f, animationSpec = tween(300)) }
        ) {
            authNavGraph(navController)
            dashboardNavGraph(navController)
            configNavGraph(navController)
            profileNavGraph(navController)
            // chatNavGraph(navController) // Integrado en Dashboard
            presupuestoNavGraph(navController)
            promocionNavGraph(navController)
            clienteNavGraph(navController)
        }
    }

    // [ELITE]: Baneo EN VIVO — si el panel admin banea a este prestador mientras ya
    // tiene la sesión abierta, este diálogo aparece sobre CUALQUIER pantalla (vive a
    // nivel de NavGraph raíz, no dentro de una pantalla puntual) y lo expulsa al cerrar.
    var mostrarSoporteEnVivo by remember { mutableStateOf(false) }

    if (motivoSuspensionEnVivo != null) {
        CuentaSuspendidaDialog(
            motivo = motivoSuspensionEnVivo,
            onDismiss = {
                sesionGuardian.cerrarSesionPorBaneo()
                navController.navigate(PrestadorRoutes.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onContactarSoporte = { mostrarSoporteEnVivo = true }
        )
    }

    if (mostrarSoporteEnVivo) {
        ChatSoporteSheet(onDismiss = { mostrarSoporteEnVivo = false })
    }

    if (matriculaVerificadaEnVivo) {
        MatriculaVerificadaDialog(
            onDismiss = { sesionGuardian.limpiarAlertaVerificacion() }
        )
    }
}
