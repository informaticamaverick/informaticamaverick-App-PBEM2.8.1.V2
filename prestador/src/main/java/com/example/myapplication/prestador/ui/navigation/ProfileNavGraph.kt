package com.example.myapplication.prestador.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.myapplication.prestador.ui.profile.ProfileScreen
import com.google.firebase.auth.FirebaseAuth

fun NavGraphBuilder.profileNavGraph(navController: NavController) {

    composable(PrestadorRoutes.Profile.route) {
        ProfileScreen(
            onBack = { navController.navigateUp() },
            onSettings = { navController.navigate(PrestadorRoutes.ServiceConfig.route) },
            onLogout = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(
                    PrestadorRoutes.Login.route,
                    NavOptions.Builder()
                        .setPopUpTo(navController.graph.startDestinationId, inclusive = true)
                        .build()
                )
            },
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