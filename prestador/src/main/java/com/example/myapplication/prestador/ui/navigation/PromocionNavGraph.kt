package com.example.myapplication.prestador.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.prestador.ui.pantallas.promocion.CrearPromocionScreen
import com.example.myapplication.prestador.ui.pantallas.promocion.PromocionListaScreen

fun NavGraphBuilder.promocionNavGraph(navController: NavHostController) {
    composable(PrestadorRoutes.CrearPromocion.route) {
        CrearPromocionScreen(
            promocionId = null,
            onBack = { navController.navigateUp() },
            onPublish = { navController.navigateUp() }
        )
    }

    composable(PrestadorRoutes.PromocionesLista.route) {
        PromocionListaScreen(
            onBack = { navController.navigateUp() },
            onNavigateToCreatePromotion = { navController.navigate(PrestadorRoutes.CrearPromocion.route) }
        )
    }
}
