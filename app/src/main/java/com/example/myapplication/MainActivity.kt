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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.presentation.auth.StartupScreen
//import com.example.myapplication.presentation.admin.AdminInitScreen
// Se importa la navegación del cliente y se le da un alias para evitar conflictos.
import com.example.myapplication.presentation.client.AppNavigation as ClientAppNavigation
import com.example.myapplication.presentation.auth.LoginScreen
// import com.example.myapplication.presentation.profile.CompleteProfileScreen // DEPRECATED MAVERICK V5
import com.example.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Se llama a la navegación raíz de la aplicación.
                    RootNavigation()
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
fun RootNavigation() {
    val navController = rememberNavController()
    // SECCIÓN B: La ruta inicial ahora es "startup" para la Experiencia Premium
    val startDestination = "startup"

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
        // 0. PANTALLA DE STARTUP (PRE-CARGA INTELIGENTE)
        // ======================================================================================
        composable("startup") {
            StartupScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("startup") { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate("main_screen") {
                        popUpTo("startup") { inclusive = true }
                    }
                },
                onNavigateToProfileEdit = {
                    // Si el perfil está incompleto, vamos directamente al perfil en modo edición
                    navController.navigate("main_screen?target=profile") {
                        popUpTo("startup") { inclusive = true }
                    }
                }
            )
        }

        // ======================================================================================
        // 1. PANTALLA DE LOGIN
        // ======================================================================================
        composable("login") {
            LoginScreen(
                onLoginSuccess = { targetRoute ->
                    // El LoginViewModel ahora decide si ir a Home o Perfil
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
            ClientAppNavigation(
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
