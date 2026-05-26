package com.example.myapplication

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.myapplication.presentation.features.auth.LoginScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.presentation.global.BeBrainViewModel
import com.example.myapplication.presentation.global.InitialNavTarget
import com.example.myapplication.presentation.features.home.AppNavigation
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // [OPTIMIZACIÓN MAVERICK]: Fondo base inmediato para evitar flash blanco
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050508))) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent // Surface transparente para ver el fondo base
                    ) {
                        // Se llama a la navegación raíz de la aplicación.
                        RootNavigation()
                    }
                }
            }
        }
    }
}

/**
 * RootNavigation maneja la navegación principal de la aplicación.
 * Decide si mostrar la pantalla de startup, login, completar perfil o la navegación principal del cliente.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RootNavigation(beBrainViewModel: BeBrainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val navTarget by beBrainViewModel.initialNavTarget.collectAsState()

    // [OPTIMIZACIÓN MAVERICK V5]: Decisión de destino inicial proactiva.
    // Si todavía estamos chequeando (CHECKING), mostramos un contenedor vacío con el color de splash.
    // Esto evita que se renderice la LoginScreen aunque sea por milisegundos.
    if (navTarget == InitialNavTarget.CHECKING) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D0221)))
        return
    }

    val startDestination = when (navTarget) {
        InitialNavTarget.LOGIN -> "login"
        else -> "main_screen"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        // ======================================================================================
        // 0. PANTALLA DE STARTUP (DESACTIVADA)
        // ======================================================================================
        /*
        composable("startup") {
            StartupScreen( ... )
        }
        */

        // ======================================================================================
        // 1. PANTALLA DE LOGIN
        // ======================================================================================
        composable("login") {
            LoginScreen(
                onLoginSuccess = { targetRoute ->
                    // MAVERICK V5: Navegación limpia y atómica.
                    val finalRoute = if (targetRoute == "perfil_cliente_edit") "main_screen?target=profile" else "main_screen"
                    navController.navigate(finalRoute) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // ======================================================================================
        // 2. PANTALLA PRINCIPAL (Contiene Home, Perfil, Chat, etc.)
        // ======================================================================================
        composable(
            route = "main_screen?target={target}",
            arguments = listOf(
                navArgument("target") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val target = backStackEntry.arguments?.getString("target")
            AppNavigation(
                initialTarget = target,
                onLogoutRequest = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
