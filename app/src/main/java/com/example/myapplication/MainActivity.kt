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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.coordinadores.UsuarioArranqueViewModel
import com.example.myapplication.ui.componentes.be.vm.BeCerebroViewModel
import com.example.myapplication.ui.pantallas.auth.AutenticacionScreen
import com.example.myapplication.ui.pantallas.home.NavegacionCajaPrincipal
import com.example.myapplication.ui.estilos.ClienteTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.myapplication.core.servicios.publicidad.BeAdsManager
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val gestorArranque: UsuarioArranqueViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // [ELITE]: Mantener Splash hasta que el gestor decida la ruta inicial
        splashScreen.setKeepOnScreenCondition {
            gestorArranque.rutaInicial.value == "verificando"
        }

        // --- INICIALIZACIÓN MAVERICK ADS ---
        BeAdsManager.initialize(this)

        enableEdgeToEdge()
        
        // [ELITE v2026.6] Procesamiento de Intent para Deep Linking (FCM)
        val deepLinkRoute = processIntentExtras(intent)

        setContent {
            ClienteTheme {
                // [OPTIMIZACIÓN app]: Fondo base inmediato para evitar flash blanco
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050508))) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent 
                    ) {
                        RootNavigation(gestorArranque = gestorArranque, initialDeepLink = deepLinkRoute)
                    }
                }
            }
        }
    }

    private fun processIntentExtras(intent: android.content.Intent?): String? {
        val extras = intent?.extras ?: return null
        val type = extras.getString("target_type") ?: return null
        
        return when (type) {
            "promo" -> {
                val pId = extras.getString("providerId") ?: ""
                val bId = extras.getString("branchId") ?: ""
                val cId = extras.getString("companyId") ?: ""
                val promoId = extras.getString("promoId") ?: ""
                "chat?providerId=$pId&branchId=$bId&companyId=$cId&promoId=$promoId"
            }
            "budget", "tender_update", "concurso", "presupuesto", "concursos" -> "concursos"
            "profile" -> "perfil_cliente"
            "message" -> "chat?providerId=${extras.getString("senderId")}"
            else -> null
        }
    }
}

/**
 * RootNavigation maneja la navegación principal de la aplicación.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RootNavigation(
    BeCerebroViewModel: BeCerebroViewModel = hiltViewModel(),
    gestorArranque: UsuarioArranqueViewModel, // Inyectado desde el host
    initialDeepLink: String? = null
) {
    val navController = rememberNavController()
    val rutaInicial by gestorArranque.rutaInicial.collectAsStateWithLifecycle()

    // Verificación ya iniciada en el host si es necesario, o aquí
    LaunchedEffect(Unit) {
        if (rutaInicial == "verificando") {
            gestorArranque.realizarVerificacionInicial()
        }
    }

    if (rutaInicial == "verificando") {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D0221)))
        return
    }

    val startDestination = when (rutaInicial) {
        "login" -> "login"
        else -> {
            if (initialDeepLink != null) "main_screen?target=$initialDeepLink"
            else "main_screen"
        }
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
        composable("login") {
            AutenticacionScreen(
                alExito = { targetRoute ->
                    val finalRoute = if (targetRoute == "perfil_cliente_edit") "main_screen?target=perfil_cliente" else "main_screen"
                    navController.navigate(finalRoute) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

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
            NavegacionCajaPrincipal(
                initialTarget = target,
                beViewModel = BeCerebroViewModel, 
                onLogoutRequest = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}


































