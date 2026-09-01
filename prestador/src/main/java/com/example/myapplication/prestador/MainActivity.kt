package com.example.myapplication.prestador

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.core.datos.repositorios.ThemeMode
import javax.inject.Inject
import com.example.myapplication.prestador.ui.navigation.PrestadorNavGraph
import com.example.myapplication.prestador.ui.theme.PrestadorTheme
import com.example.myapplication.prestador.viewmodel.config.PreConfiguracionViewModel
import com.example.myapplication.prestador.coordinadores.PrestadorArranqueViewModel
import com.example.myapplication.prestador.coordinadores.PrestadorStartupManager
import com.example.myapplication.prestador.viewmodel.profile.PerfilPrestadorDeepViewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var startupManager: PrestadorStartupManager

    private val gestorArranque: PrestadorArranqueViewModel by viewModels()
    private val identidadViewModel: PerfilPrestadorDeepViewModel by viewModels()
    private val configuracionViewModel: PreConfiguracionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // [ELITE]: Mantener Splash hasta que el gestor decida la ruta inicial
        splashScreen.setKeepOnScreenCondition {
            gestorArranque.rutaInicial.value == "verificando"
        }

        // Inicialización fría de datos (Sembrado de categorías)
        startupManager.performInitialStartup()

        setContent {
            // Simplificación temporal del tema para evitar errores
            val isDark = isSystemInDarkTheme()

            val tenderId = remember { intent.getStringExtra("tenderId") }

            PrestadorTheme(darkTheme = isDark, dynamicColor = false) {
                val navController = rememberNavController()
                val rutaInicial by gestorArranque.rutaInicial.collectAsStateWithLifecycle()

                // Disparamos verificación
                LaunchedEffect(Unit) {
                    gestorArranque.realizarVerificacionInicial()
                }

                if (rutaInicial != "verificando") {
                    PrestadorNavGraph(
                        navController = navController,
                        initialTenderId = tenderId,
                        startDestinationRoute = if (rutaInicial == "login") "login" else "dashboard"
                    )
                }
            }
        }
    }
}














































