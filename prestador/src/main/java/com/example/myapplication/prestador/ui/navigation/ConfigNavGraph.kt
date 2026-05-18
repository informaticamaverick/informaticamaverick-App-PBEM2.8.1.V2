package com.example.myapplication.prestador.ui.navigation


import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.prestador.ui.config.AparienciaScreen
import com.example.myapplication.prestador.ui.config.CalendarioConfigScreen
import com.example.myapplication.prestador.ui.config.ConfiguracionScreen
import com.example.myapplication.prestador.ui.config.NotificacionesConfigScreen
import com.example.myapplication.prestador.ui.config.PresupuestoConfigScreen
import com.example.myapplication.prestador.viewmodel.profile.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.profile.ProfileState

fun NavGraphBuilder.configNavGraph(navController: NavController) {

    composable(PrestadorRoutes.ServiceConfig.route) {
        ConfiguracionScreen(
            onBack = { navController.navigateUp() },
            onNavigateToCalendario = { navController.navigate(PrestadorRoutes.CalendarConfig.route) },
            onNavigateToPresupuestoConfig = { navController.navigate(PrestadorRoutes.PresupuestoConfig.route) },
            onNavigateToApariencia = { navController.navigate(PrestadorRoutes.AparienciaConfig.route) },
            onNavigateToNotificaciones = { navController.navigate(PrestadorRoutes.NotificacionesConfig.route) }
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
}