package com.example.myapplication.prestador

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
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
        // La app siempre usa tema oscuro visualmente (colores hardcodeados en cada pantalla,
        // no siguen el tema del sistema) - forzamos la barra de navegación a estilo oscuro
        // para que no quede clara cuando el sistema del dispositivo está en modo claro.
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        // [ELITE]: Mantener Splash hasta que el gestor decida la ruta inicial
        splashScreen.setKeepOnScreenCondition {
            gestorArranque.rutaInicial.value == "verificando"
        }

        // Inicialización fría de datos (Sembrado de categorías)
        startupManager.performInitialStartup()

        setContent {
            // La app siempre se disena en oscuro (todas las pantallas usan colores
            // hardcodeados a oscuro) - no depender de isSystemInDarkTheme(), porque si
            // el dispositivo esta en modo claro, getPrestadorColors() (que si respeta
            // LocalIsDarkTheme) cae a la paleta clara y deja ver el fondo crema
            // (BackgroundLight) detras de la barra de navegacion flotante.
            val isDark = true

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














































