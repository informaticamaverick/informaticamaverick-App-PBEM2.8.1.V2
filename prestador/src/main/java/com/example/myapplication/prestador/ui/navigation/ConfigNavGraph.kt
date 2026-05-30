package com.example.myapplication.prestador.ui.navigation


import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.prestador.ui.config.AcercaDeScreen
import com.example.myapplication.prestador.ui.config.AparienciaScreen
import com.example.myapplication.prestador.ui.config.CalendarioConfigScreen
import com.example.myapplication.prestador.ui.config.ConfiguracionScreen
import com.example.myapplication.prestador.ui.config.NotificacionesConfigScreen
import com.example.myapplication.prestador.ui.config.PresupuestoConfigScreen
import com.example.myapplication.prestador.ui.config.PrivacidadScreen
import com.example.myapplication.prestador.ui.config.TerminosScreen
import com.example.myapplication.prestador.viewmodel.profile.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.profile.ProfileState

fun NavGraphBuilder.configNavGraph(navController: NavController) {

    composable(
        route = PrestadorRoutes.ServiceConfig.route,
        enterTransition = {
            slideInHorizontally(animationSpec = tween(320)) { -it } +
            fadeIn(animationSpec = tween(320))
        },
        exitTransition = {
            slideOutHorizontally(animationSpec = tween(280)) { -it } +
            fadeOut(animationSpec = tween(280))
        },
        popEnterTransition = {
            slideInHorizontally(animationSpec = tween(320)) { -it } +
            fadeIn(animationSpec = tween(320))
        },
        popExitTransition = {
            slideOutHorizontally(animationSpec = tween(280)) { -it } +
            fadeOut(animationSpec = tween(280))
        }
    ) {
        ConfiguracionScreen(
            onBack = { navController.navigateUp() },
            onNavigateToCalendario = { navController.navigate(PrestadorRoutes.CalendarConfig.route) },
            onNavigateToPresupuestoConfig = { navController.navigate(PrestadorRoutes.PresupuestoConfig.route) },
            onNavigateToApariencia = { navController.navigate(PrestadorRoutes.AparienciaConfig.route) },
            onNavigateToNotificaciones = { navController.navigate(PrestadorRoutes.NotificacionesConfig.route) },
            onNavigateToTerminos = { navController.navigate(PrestadorRoutes.LegalTerminos.route) },
            onNavigateToPrivacidad = { navController.navigate(PrestadorRoutes.LegalPrivacidad.route) },
            onNavigateToAcercaDe = { navController.navigate(PrestadorRoutes.AcercaDe.route) },
            onSignOut = {
                navController.navigate(PrestadorRoutes.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    composable(PrestadorRoutes.CalendarConfig.route) {
        val profileVm: EditProfileViewModel = hiltViewModel()
        val profileState by profileVm.profileState.collectAsState()
        val firstCompany = (profileState as? ProfileState.Success) ?.provider?.companies?.firstOrNull()


        CalendarioConfigScreen(
            onBack = { navController.navigateUp() },
            onGoToEditProfile = {
                navController.navigate(PrestadorRoutes.Profile.route)
            },
            onNavigateToCalendarioEmpresa = {
                if (firstCompany != null ) {
                    navController.navigate(
                        PrestadorRoutes.CalendarioConfigEntity.createRoute(
                            firstCompany.id,
                            firstCompany.name.ifBlank { "Empresa" }
                        )
                    )
                }
            }
        )

    }

    composable(
        route = PrestadorRoutes.CalendarioConfigEntity.route,
        arguments = listOf(
            navArgument("owner_id") { type = NavType.StringType },
            navArgument("owner_name") { type = NavType.StringType }
    )
    ) { backStackEntry ->
        val ownerName = backStackEntry.arguments?.getString("owner_name") ?: ""
        CalendarioConfigScreen(
            onBack = { navController.navigateUp() },
            onGoToEditProfile = {
                navController.navigate(PrestadorRoutes.Profile.route) },
            ownerName = ownerName
        )
    }

    composable(PrestadorRoutes.PresupuestoConfig.route) {
        PresupuestoConfigScreen(onBack = { navController.navigateUp() })
    }

    composable(PrestadorRoutes.AparienciaConfig.route) {
        AparienciaScreen(onBack = { navController.navigateUp() })
    }

    composable(PrestadorRoutes.NotificacionesConfig.route) {
        NotificacionesConfigScreen(onBack = { navController.navigateUp() })
    }

    composable(PrestadorRoutes.LegalTerminos.route) {
        TerminosScreen(onBack = { navController.navigateUp() })
    }

    composable(PrestadorRoutes.LegalPrivacidad.route) {
        PrivacidadScreen(onBack = { navController.navigateUp() })
    }

    composable(PrestadorRoutes.AcercaDe.route) {
        AcercaDeScreen(onBack = { navController.navigateUp() })
    }
}