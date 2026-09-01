package com.example.myapplication.prestador.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.prestador.ui.pantallas.login.PrestadorLoginScreen
import com.example.myapplication.prestador.ui.pantallas.register.PrestadorOnboardingWizardScreen
import com.example.myapplication.prestador.ui.pantallas.register.PrestadorRegisterScreen
import com.example.myapplication.prestador.ui.pantallas.success.PrestadorSuccessScreen

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
                    // 🔥 [ELITE] Si no tiene perfil, guiar por el Wizard
                    navController.navigate(
                        PrestadorRoutes.OnboardingWizard.createRoute(isGoogle = true),
                        NavOptions.Builder().setPopUpTo(PrestadorRoutes.Login.route, true).build()
                    )
                }
            },
            onNavigateToRegister = {
                navController.navigate(PrestadorRoutes.OnboardingWizard.createRoute(isGoogle = false))
            }
        )
    }

    composable(
        route = PrestadorRoutes.OnboardingWizard.route,
        arguments = listOf(
            navArgument("isGoogle") { type = NavType.BoolType; defaultValue = false }
        )
    ) { backStackEntry ->
        val isGoogle = backStackEntry.arguments?.getBoolean("isGoogle") ?: false
        PrestadorOnboardingWizardScreen(
            isGoogle = isGoogle,
            onBack = { navController.popBackStack() },
            onNavigateToForm = { tieneNegocio ->
                navController.navigate(PrestadorRoutes.Register.createRoute(isGoogle, tieneNegocio))
            }
        )
    }

    composable(
        route = PrestadorRoutes.Register.route,
        arguments = listOf(
            navArgument("isGoogle") { type = NavType.BoolType; defaultValue = false },
            navArgument("tieneNegocio") { type = NavType.BoolType; defaultValue = false }
        )
    ) { backStackEntry ->
        val isGoogle = backStackEntry.arguments?.getBoolean("isGoogle") ?: false
        val tieneNegocio = backStackEntry.arguments?.getBoolean("tieneNegocio") ?: false
        
        PrestadorRegisterScreen(
            isGoogleUser = isGoogle,
            tieneNegocioInicial = tieneNegocio, // 🔥 PASAR SELECCIÓN
            onRegisterSuccess = {
                navController.navigate(
                    PrestadorRoutes.Dashboard.route,
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












































