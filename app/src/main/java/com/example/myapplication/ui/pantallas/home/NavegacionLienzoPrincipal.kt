package com.example.myapplication.ui.pantallas.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.ui.componentes.be.vm.BeCuerpoViewModel
import com.example.myapplication.ui.componentes.be.vm.BeCerebroViewModel
import com.example.myapplication.ui.pantallas.budget.ConcursoPublicoScreen
import com.example.myapplication.ui.pantallas.calendar.CalendarScreen
import com.example.myapplication.ui.pantallas.chat.*
import com.example.myapplication.ui.pantallas.profile.*
import com.example.myapplication.ui.pantallas.budget.*
import com.example.myapplication.ui.componentes.*
import com.example.myapplication.ui.componentes.sistema.*
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.viewmodel.home.*
import com.example.myapplication.viewmodel.home.CategoryViewModel

/**
 * NavegacionLienzoPrincipal.kt
 * Propósito: Definir el NavHost y las transiciones entre pantallas principales.
 * Funcionamiento: Centraliza las rutas y composables de la aplicación.
 * Relación: Es el "Lienzo" contenido dentro de NavegacionCajaPrincipal.kt.
 */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavegacionLienzoPrincipal(
    navController: NavHostController,
    innerPadding: PaddingValues,
    beViewModel: BeCerebroViewModel,
    beAssistantViewModel: BeCuerpoViewModel?,
    onLogoutRequest: () -> Unit
) {
    val navItems = listOf(
        Screen.Home,
        Screen.Concursos,
        Screen.Chat,
        Screen.Calendar,
        Screen.Promo
    )

    val mainEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        val initialIndex = getRouteIndex(initialState.destination.route, navItems)
        val targetIndex = getRouteIndex(targetState.destination.route, navItems)

        if (initialIndex != -1 && targetIndex != -1) {
            if (targetIndex > initialIndex) {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400, easing = FastOutSlowInEasing))
            } else {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400, easing = FastOutSlowInEasing))
            }
        } else {
            fadeIn(tween(300))
        }
    }

    val mainExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        val initialIndex = getRouteIndex(initialState.destination.route, navItems)
        val targetIndex = getRouteIndex(targetState.destination.route, navItems)

        if (initialIndex != -1 && targetIndex != -1) {
            if (targetIndex > initialIndex) {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400, easing = FastOutSlowInEasing))
            } else {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400, easing = FastOutSlowInEasing))
            }
        } else {
            fadeOut(tween(300))
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(SharedPalette.ROG_Dark_Bg)) {
        // 🔥 [AUDITORÍA]: Monitor de Navegación Global (Único)
        DisposableEffect(navController) {
            val listener = androidx.navigation.NavController.OnDestinationChangedListener { _, destination, arguments ->
                android.util.Log.d("MAV_NAV", "🚀 [DESTINATION] Route: ${destination.route} | Args: $arguments")
            }
            navController.addOnDestinationChangedListener(listener)
            onDispose { navController.removeOnDestinationChangedListener(listener) }
        }

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(
                route = Screen.Home.route,
                enterTransition = mainEnterTransition,
                exitTransition = mainExitTransition
            ) {
                HomeScreenComplete(
                    navController = navController,
                    brainViewModel = beViewModel,
                    assistantViewModel = beAssistantViewModel
                )
            }

            composable(
                route = Screen.Chat.route,
                arguments = listOf(
                    navArgument("providerId") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("branchId") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("categoryId") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("clientBranchId") { type = NavType.StringType; nullable = true; defaultValue = null }
                ),
                enterTransition = mainEnterTransition,
                exitTransition = mainExitTransition
            ) { backStackEntry ->
                val providerId = backStackEntry.arguments?.getString("providerId")
                val branchId = backStackEntry.arguments?.getString("branchId")
                val categoryId = backStackEntry.arguments?.getString("categoryId")
                val clientBranchId = backStackEntry.arguments?.getString("clientBranchId")

                ChatPantalla(
                    onBack = { 
                        android.util.Log.d("MAV_NAV", "🔙 [CHAT] PopBackStack")
                        navController.popBackStack() 
                    },
                    idRemoto = providerId,
                    idLocal = branchId,
                    initialPromoId = null,
                    navController = navController,
                    beBrainViewModel = beViewModel,
                    onInConversationChange = { /* Soberanía interna de ChatPantalla */ }
                )
            }

            composable(route = Screen.Calendar.route, enterTransition = mainEnterTransition, exitTransition = mainExitTransition) {
                CalendarScreen(
                    onBack = { navController.popBackStack() },
                    onChatClick = { pid -> navController.navigate(Screen.Chat.createRoute(providerId = pid)) },
                    onNavigateToProfile = { pid -> navController.navigate("perfil_prestador/$pid") }
                )
            }

            composable(route = Screen.Promo.route, enterTransition = mainEnterTransition, exitTransition = mainExitTransition) {
                PromoScreen(
                    navController = navController,
                    beViewModel = beViewModel
                )
            }

            composable(
                route = Screen.PerfilCliente.route,
                enterTransition = mainEnterTransition,
                exitTransition = mainExitTransition
            ) {
                PerfilUsuarioScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = onLogoutRequest,
                    beViewModel = beViewModel
                )
            }

            composable(
                route = Screen.Configuracion.route,
                enterTransition = mainEnterTransition,
                exitTransition = mainExitTransition
            ) {
                ConfigUserScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onAccountDeleted = { onLogoutRequest() }
                )
            }

            composable(
                route = Screen.ResultBusqueda.route,
                arguments = listOf(navArgument("category") { type = NavType.StringType }),
                enterTransition = mainEnterTransition,
                exitTransition = mainExitTransition
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("category") ?: ""
                ResultadoBusquedaPrestadorScreen(
                    idCategoria = categoryId,
                    alVolver = { navController.popBackStack() },
                    alNavegarAPerfilPrestador = { pid, cid, bid ->
                        navController.navigate(Screen.PerfilPrestador.createRoute(pid, cid, bid))
                    },
                    alNavegarAChat = { item, cid, bid ->
                        navController.navigate(Screen.Chat.createRoute(
                            providerId = item.id,
                            branchId = bid,
                            categoryId = categoryId,
                            clientBranchId = cid
                        ))
                    },
                    viewModel = hiltViewModel(),
                    brainViewModel = beViewModel
                )
            }

            composable(
                route = Screen.PerfilPrestador.route,
                arguments = listOf(
                    navArgument("providerId") { type = NavType.StringType },
                    navArgument("companyId") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("branchId") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { backStackEntry ->
                val providerId = backStackEntry.arguments?.getString("providerId") ?: ""
                val companyId = backStackEntry.arguments?.getString("companyId")
                val branchId = backStackEntry.arguments?.getString("branchId")

                PerfilPrestadorScreen(
                    providerId = providerId,
                    onBack = { navController.popBackStack() },
                    onNavigateToChat = { bid ->
                        navController.navigate(Screen.Chat.createRoute(
                            providerId = providerId,
                            branchId = bid
                        ))
                    },
                    BeCerebroViewModel = beViewModel
                )
            }

            composable(route = Screen.Urgencia.route) { 
                UrgenciasResultadosBusquedaPrestadorScreen(
                    navController = navController,
                    beViewModel = beViewModel
                ) 
            }

            composable(
                route = Screen.Concursos.route,
                enterTransition = mainEnterTransition,
                exitTransition = mainExitTransition
            ) {
                ConcursoPublicoScreen(
                    modeloVistaConcursos = hiltViewModel(),
                    alHacerClickChat = { pid, _ -> navController.navigate(Screen.Chat.createRoute(providerId = pid)) },
                    alNavegarANuevoConcurso = { navController.navigate(Screen.NuevoConcurso.route) },
                    alNavegarAPresupuestosConcurso = { id -> navController.navigate(Screen.ConcursoPresupuesto.createRoute(id)) },
                    alRegresar = { navController.popBackStack() },
                    rellenoInferior = innerPadding
                )
            }

            composable(
                route = Screen.NuevoConcurso.route,
                enterTransition = { slideInVertically(initialOffsetY = { it }) + fadeIn() },
                exitTransition = { slideOutVertically(targetOffsetY = { it }) + fadeOut() }
            ) {
                NuevoConcursoPublicoScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ConcursoPresupuesto.route,
                arguments = listOf(navArgument("idConcurso") { type = NavType.StringType }),
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ) { backStackEntry ->
                val idConcurso = backStackEntry.arguments?.getString("idConcurso") ?: ""
                ConcursoPresupuestoScreen(
                    idConcurso = idConcurso,
                    beCerebroVm = beViewModel, // 🔥 [NEW] Pass brain VM
                    alRegresar = { navController.popBackStack() },
                    alHacerClickChat = { pid, cid ->
                        navController.navigate(Screen.Chat.createRoute(providerId = pid, categoryId = cid))
                    }
                )
            }

            composable(
                route = Screen.ArchiveroChatMultimedia.route,
                arguments = listOf(
                    navArgument("idRemoto") { type = NavType.StringType },
                    navArgument("idLocal") { type = NavType.StringType }
                ),
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ) { backStackEntry ->
                val idRemoto = backStackEntry.arguments?.getString("idRemoto") ?: ""
                val idLocal = backStackEntry.arguments?.getString("idLocal") ?: ""
                com.example.myapplication.ui.pantallas.chat.componentes.ArchiveroChatMultimediaScreen(
                    idRemoto = idRemoto,
                    idLocal = idLocal,
                    alRegresar = { navController.popBackStack() },
                    alHacerClickImagen = { _: String ->
                        // Abrir visor de imágenes
                    },
                    alNavegarAChat = { pid, bid ->
                        navController.navigate(Screen.Chat.createRoute(providerId = pid, branchId = bid))
                    }
                )
            }
        }
    }
}
