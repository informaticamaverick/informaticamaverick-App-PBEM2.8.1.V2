package com.example.myapplication.prestador.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.myapplication.prestador.ui.profile.ProfileScreen

fun NavGraphBuilder.profileNavGraph(navController: NavHostController) {

    composable(PrestadorRoutes.Profile.route) {
        ProfileScreen(
            onBack = { navController.navigateUp() },
            onNavigateToCalendarioConfig = {

                navController.navigate(PrestadorRoutes.CalendarioConfig.route)
            },
            onNavigateToCalendarioConfigEntity = { ownerId, ownerName ->
                navController.navigate(

                    PrestadorRoutes.CalendarioConfigEntity.createRoute(ownerId, ownerName)
                )
            }
        )
    }
}