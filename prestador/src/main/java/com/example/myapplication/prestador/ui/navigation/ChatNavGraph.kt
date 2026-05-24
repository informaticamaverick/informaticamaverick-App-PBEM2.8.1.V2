package com.example.myapplication.prestador.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.myapplication.prestador.ui.chat.PrestadorChatScreen

fun NavGraphBuilder.chatNavGraph(navController: NavHostController) {

    composable("chat") {
        PrestadorChatScreen(
            onBack = { navController.navigateUp() },
            onNavigateToPresupuesto = {

                navController.navigate(PrestadorRoutes.CrearPresupuesto.createRoute("chat"))
            }
        )
    }
}
