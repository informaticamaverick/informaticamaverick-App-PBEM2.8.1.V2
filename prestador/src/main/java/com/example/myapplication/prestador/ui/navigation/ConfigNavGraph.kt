package com.example.myapplication.prestador.ui.navigation


import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.prestador.ui.pantallas.config.AcercaDeScreen
import com.example.myapplication.prestador.ui.pantallas.config.AparienciaScreen
import com.example.myapplication.prestador.ui.pantallas.config.HorariosConfigScreen
import com.example.myapplication.prestador.ui.pantallas.config.ConfiguracionScreen
import com.example.myapplication.prestador.ui.pantallas.config.NotificacionesConfigScreen
import com.example.myapplication.prestador.ui.pantallas.config.PresupuestoConfigScreen
import com.example.myapplication.prestador.ui.pantallas.config.PrivacidadScreen
import com.example.myapplication.prestador.ui.pantallas.config.TerminosScreen
import com.example.myapplication.prestador.ui.pantallas.empresa.turnos.GestionTurnosScreen
import com.example.myapplication.prestador.ui.pantallas.empresa.visitas.GestionVisitasScreen
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
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
            onNavigateToCalendario = { navController.navigate(PrestadorRoutes.HorariosConfig.route) },
            onNavigateToPresupuestoConfig = { navController.navigate(PrestadorRoutes.PresupuestoConfig.route) },
            onNavigateToApariencia = { navController.navigate(PrestadorRoutes.AparienciaConfig.route) },
            onNavigateToNotificaciones = { navController.navigate(PrestadorRoutes.NotificacionesConfig.route) },
            onNavigateToTerminos = { navController.navigate(PrestadorRoutes.LegalTerminos.route) },
            onNavigateToPrivacidad = { navController.navigate(PrestadorRoutes.LegalPrivacidad.route) },
            onNavigateToAcercaDe = { navController.navigate(PrestadorRoutes.AcercaDe.route) },
            onNavigateToGestionTurnos = { navController.navigate(PrestadorRoutes.GestionTurnos.route) },
            onNavigateToGestionVisitas = { navController.navigate(PrestadorRoutes.GestionVisitas.route) },
            onSignOut = {
                navController.navigate(PrestadorRoutes.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    composable(
        route = PrestadorRoutes.HorariosConfig.route,
        arguments = listOf(
            navArgument("type") { type = NavType.StringType; nullable = true; defaultValue = null },
            navArgument("addressId") { type = NavType.StringType; nullable = true; defaultValue = null }
        )
    ) {
        HorariosConfigScreen(
            onBack = { navController.navigateUp() }
        )
    }

    composable(
        route = PrestadorRoutes.HorariosConfigEntity.route,
        arguments = listOf(
            navArgument("owner_id") { type = NavType.StringType },
            navArgument("owner_name") { type = NavType.StringType },
            navArgument("type") { type = NavType.StringType; nullable = true; defaultValue = null },
            navArgument("addressId") { type = NavType.StringType; nullable = true; defaultValue = null }
        )
    ) {
        HorariosConfigScreen(
            onBack = { navController.navigateUp() }
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

    composable(PrestadorRoutes.GestionTurnos.route) {
        GestionTurnosScreen(
            onBack = { navController.navigateUp() },
            onNavigateToHorarios = { id, name ->
                navController.navigate(PrestadorRoutes.HorariosConfigEntity.createRoute(id, name, type = "TURNOS"))
            }
        )
    }

    composable(PrestadorRoutes.GestionVisitas.route) {
        GestionVisitasScreen(
            onBack = { navController.navigateUp() },
            onNavigateToHorarios = { id, name ->
                navController.navigate(PrestadorRoutes.HorariosConfigEntity.createRoute(id, name, type = "VISITAS"))
            }
        )
    }
}












































