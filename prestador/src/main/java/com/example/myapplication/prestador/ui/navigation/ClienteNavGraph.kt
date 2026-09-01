package com.example.myapplication.prestador.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.prestador.ui.pantallas.client.ClientePerfilScreen

fun NavGraphBuilder.clienteNavGraph(navController: NavHostController) {

    composable(
        route = PrestadorRoutes.ClientePerfil.route,
        arguments = listOf(
            navArgument("clientId") { type = NavType.StringType }
        )
    ) {
        ClientePerfilScreen(
            onBack = { navController.navigateUp() }
        )
    }
}












































