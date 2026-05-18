package com.example.myapplication.prestador.ui.navigation

import android.os.Build
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.prestador.ui.presupuesto.CrearPresupuestoPrestadorScreen
import com.example.myapplication.prestador.ui.presupuesto.PresupuestosScreen

fun NavGraphBuilder.presupuestoNavGraph(navController: NavController) {

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
            }
        )
    ) { backStackEntry ->
        val origin = backStackEntry.arguments?.getString("origin") ?: "dashboard"
        val appointmentId =
            backStackEntry.arguments?.getString("appointmentId") ?: ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CrearPresupuestoPrestadorScreen(
                appointmentId = appointmentId,
                onBack = {
                    when (origin) {
                        "chat" -> navController.navigate("chat") {
                            popUpTo(PrestadorRoutes.CrearPresupuesto.route)
                            { inclusive = true }
                        }

                        "presupuestos" ->
                            navController.navigate(PrestadorRoutes.Presupuestos.route) {
                                popUpTo(PrestadorRoutes.CrearPresupuesto.route)
                                { inclusive = true }
                            }

                        else -> navController.navigateUp()
                    }
                }
            )
        }

        composable(PrestadorRoutes.Presupuestos.route) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PresupuestosScreen(
                    onBack = { navController.navigateUp() },
                    onCrearNuevo = {
                        navController.navigate(PrestadorRoutes.CrearPresupuesto.createRoute("presupuestos"))
                    },
                    onVerDetalle = { _ -> },
                    onNavigateToConfig = {
                        navController.navigate(PrestadorRoutes.PresupuestoConfig.route)
                    }
                )
            }
        }
    }
}