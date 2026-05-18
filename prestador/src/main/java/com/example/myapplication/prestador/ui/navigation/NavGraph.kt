package com.example.myapplication.prestador.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PrestadorNavGraph(
    navController: NavHostController,
    startDestination: String = if (FirebaseAuth.getInstance().currentUser != null)
        PrestadorRoutes.Dashboard.route
    else
        PrestadorRoutes.Login.route
) {
    val colors = getPrestadorColors()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.92f, animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.92f, animationSpec = tween(300)) }
        ) {
            authNavGraph(navController)
            dashboardNavGraph(navController)
            configNavGraph(navController)
            profileNavGraph(navController)
            chatNavGraph(navController)
            presupuestoNavGraph(navController)
            promotionNavGraph(navController)
            clienteNavGraph(navController)
        }
    }
}
