package com.example.myapplication.prestador.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.myapplication.prestador.ui.pantallas.profile.PerfilPrestadorScreen

fun NavGraphBuilder.profileNavGraph(navController: NavController) {

    composable(PrestadorRoutes.Profile.route) {
        PerfilPrestadorScreen(
            alVolver = { navController.navigateUp() },
            alCerrarSesion = {
                navController.navigate(
                    PrestadorRoutes.Login.route,
                    NavOptions.Builder()
                        .setPopUpTo(navController.graph.startDestinationId, inclusive = true)
                        .build()
                )
            },
            onConfig = {
                navController.navigate(PrestadorRoutes.ServiceConfig.route)
            },
            alHorarios = { ownerId ->
                navController.navigate(PrestadorRoutes.HorariosConfigEntity.createRoute(ownerId, "Configurar Horarios"))
            },
            onNavigateToPaywall = {
                navController.navigate(PrestadorRoutes.Paywall.route)
            }
        )
    }
}












































