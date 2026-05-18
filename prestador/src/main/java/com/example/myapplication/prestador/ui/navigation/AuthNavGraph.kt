package com.example.myapplication.prestador.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.prestador.ui.login.PrestadorLoginScreen
import com.example.myapplication.prestador.ui.register.PrestadorRegisterScreen
import com.example.myapplication.prestador.ui.success.PrestadorSuccessScreen

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {

    composable(PrestadorRoutes.Login.route) {
        PrestadorLoginScreen(
            onLoginSuccess = { hasProfile ->
                if (hasProfile) {
                    navController.navigate(
                        PrestadorRoutes.Dashboard.route,
                        NavOptions.Builder().setPopUpTo(PrestadorRoutes.Login.route, true).build()
                    )
                } else {
                    navController.navigate(
                        PrestadorRoutes.Register.createRoute(isGoogle = true),
                        NavOptions.Builder().setPopUpTo(PrestadorRoutes.Login.route, true).build()
                    )
                }
            },
            onNavigateToRegister = {
                navController.navigate(PrestadorRoutes.Register.createRoute(isGoogle = false))
            }
        )
    }

    composable(
        route = PrestadorRoutes.Register.route,
        arguments = listOf(
            navArgument("isGoogle") {
                type = NavType.BoolType
                defaultValue = false
            }
        )
    ) { backStackEntry ->
        val isGoogle = backStackEntry.arguments?.getBoolean("isGoogle") ?: false
        PrestadorRegisterScreen(
            isGoogleUser = isGoogle,
            onRegisterSuccess = {
                navController.navigate(
                    PrestadorRoutes.Success.route,
                    NavOptions.Builder().setPopUpTo(PrestadorRoutes.Register.route, true).build()
                )
            },
            onBackToLogin = {
                navController.navigate(
                    PrestadorRoutes.Login.route,
                    NavOptions.Builder().setPopUpTo(PrestadorRoutes.Register.route, true).build()
                )
            }
        )
    }

    composable(PrestadorRoutes.Success.route) {
        PrestadorSuccessScreen(
            onNavigateToDashboard = {
                navController.navigate(
                    PrestadorRoutes.Dashboard.route,
                    NavOptions.Builder().setPopUpTo(PrestadorRoutes.Success.route, true).build()
                )
            }
        )
    }
}