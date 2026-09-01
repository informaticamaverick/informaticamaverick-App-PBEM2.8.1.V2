package com.example.myapplication.prestador.ui.navigation

import android.os.Build
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.prestador.ui.pantallas.presupuesto.ArmadorPresupuestoScreen
import com.example.myapplication.prestador.ui.pantallas.presupuesto.PresupuestosScreen

fun NavGraphBuilder.presupuestoNavGraph(navController: NavController) {

    composable(PrestadorRoutes.Presupuestos.route) {
        PresupuestosScreen(
            onVolver = { navController.navigateUp() },
            onCrearNuevo = {
                navController.navigate(PrestadorRoutes.CrearPresupuesto.createRoute("presupuestos"))
            },
            onVerDetalle = { _ -> },
            onNavegarConfig = {
                navController.navigate(PrestadorRoutes.PresupuestoConfig.route)
            }
        )
    }

    composable(
        route = PrestadorRoutes.CrearPresupuesto.route,
        arguments = listOf(
            navArgument("origin") {
                type = NavType.StringType
                defaultValue = "dashboard"
            },
            navArgument("appointmentId") {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument("tenderId") {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument("clientId") {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { backStackEntry ->
        val origin = backStackEntry.arguments?.getString("origin") ?: "dashboard"
        val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
        val tenderId = backStackEntry.arguments?.getString("tenderId") ?: ""
        val clientId = backStackEntry.arguments?.getString("clientId") ?: ""
        
        ArmadorPresupuestoScreen(
            idCliente = clientId.ifBlank { tenderId.ifBlank { appointmentId } }, 
            idPrestador = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "",
            idConcurso = tenderId.ifBlank { null },
            onVolver = {
                when (origin) {
                    "chat" -> navController.navigate(PrestadorRoutes.Dashboard.route) {
                        popUpTo(PrestadorRoutes.CrearPresupuesto.route) { inclusive = true }
                    }
                    "presupuestos" -> navController.navigate(PrestadorRoutes.Presupuestos.route) {
                        popUpTo(PrestadorRoutes.CrearPresupuesto.route) { inclusive = true }
                    }
                    else -> navController.navigateUp()
                }
            },
            onVerCatalogo = {
                navController.navigate(PrestadorRoutes.Catalogo.route)
            },
            onNavigateToPaywall = {
                navController.navigate(PrestadorRoutes.Paywall.route)
            }
        )
    }
}













































