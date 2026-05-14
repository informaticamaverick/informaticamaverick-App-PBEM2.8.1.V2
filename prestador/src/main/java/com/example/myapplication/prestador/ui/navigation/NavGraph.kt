package com.example.myapplication.prestador.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.example.myapplication.prestador.ui.chat.PrestadorChatScreen
import com.example.myapplication.prestador.ui.login.PrestadorLoginScreen
import com.example.myapplication.prestador.ui.register.PrestadorRegisterScreen
import com.example.myapplication.prestador.ui.success.PrestadorSuccessScreen
import com.example.myapplication.prestador.ui.dashboard.PrestadorDashboardScreen
import com.example.myapplication.prestador.ui.config.ConfiguracionScreen
import com.example.myapplication.prestador.ui.config.CalendarioConfigScreen
import com.example.myapplication.prestador.ui.config.PresupuestoConfigScreen
import com.example.myapplication.prestador.ui.presupuesto.CrearPresupuestoPrestadorScreen
import com.example.myapplication.prestador.ui.presupuesto.PresupuestosScreen
import com.example.myapplication.prestador.ui.promotion.CreatePromotionScreen
import com.example.myapplication.prestador.ui.promotion.PromotionListScreen
import com.example.myapplication.prestador.ui.promotion.PromotionDetailScreen
import com.example.myapplication.prestador.ui.theme.getPrestadorColors


import com.example.myapplication.prestador.ui.profile.ProfileScreen
import com.example.myapplication.prestador.ui.client.ClientePerfilScreen

@Composable
fun PrestadorNavGraph(
    navController: NavHostController,
    startDestination: String = if (FirebaseAuth.getInstance().currentUser != null)
        PrestadorRoutes.Dashboard.route
    else
        PrestadorRoutes.Login.route
) {
    val colors = getPrestadorColors()
    val activity = LocalContext.current as? Activity
    val scope = rememberCoroutineScope()
    
    // Eliminado: println de ChatSimulationViewModel innecesario

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,

            // ANIMACIÓN GLOBAL: SUAVE Y PROFESIONAL
            enterTransition = {
                // Entra desvaneciéndose y creciendo un poco
                fadeIn(animationSpec = tween(300)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(300))
            },
            exitTransition = {
                // Sale desvaneciéndose y encogiéndose un poco
                fadeOut(animationSpec = tween(300)) +
                        scaleOut(targetScale = 0.92f, animationSpec = tween(300))
            },

            // Para que al volver atrás no se sienta raro
            popEnterTransition = {
                fadeIn(animationSpec = tween(300)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(300))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) +
                        scaleOut(targetScale = 0.92f, animationSpec = tween(300))
            }
        ) {

            composable(PrestadorRoutes.Login.route) {
                PrestadorLoginScreen(
                    onLoginSuccess = { hasProfile ->
                        if (hasProfile) {
                            // Usuario existente con perfil completo
                            navController.navigate(PrestadorRoutes.Dashboard.route) {
                                popUpTo(PrestadorRoutes.Login.route) { inclusive = true }
                            }
                        } else {
                            // Usuario nuevo de Google, necesita completar registro (sin email/contraseña)
                            navController.navigate(PrestadorRoutes.Register.createRoute(isGoogle = true)) {
                                popUpTo(PrestadorRoutes.Login.route) { inclusive = true }
                            }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(PrestadorRoutes.Register.createRoute(isGoogle = false))
                    }
                )
            }

            composable(
                route = PrestadorRoutes.Register.route,
                arguments = listOf(
                    navArgument("isGoogle") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val isGoogle = backStackEntry.arguments?.getBoolean("isGoogle") ?: false
                PrestadorRegisterScreen(
                    isGoogleUser = isGoogle,
                    onRegisterSuccess = {
                        navController.navigate(PrestadorRoutes.Success.route) {
                            popUpTo(PrestadorRoutes.Register.route) { inclusive = true }
                        }
                    },
                    onBackToLogin = {
                        navController.navigate(PrestadorRoutes.Login.route) {
                            popUpTo(PrestadorRoutes.Register.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(PrestadorRoutes.Success.route) {
                PrestadorSuccessScreen(
                    onNavigateToDashboard = {
                        navController.navigate(PrestadorRoutes.Dashboard.route) {
                            popUpTo(PrestadorRoutes.Success.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(PrestadorRoutes.Dashboard.route) {
                // SECCIÓN: Dashboard principal
                // Se utiliza ChatViewModel (real) para obtener datos persistentes reales.
                
                PrestadorDashboardScreen(
                    onNavigateToEditProfile = {
                        navController.navigate(PrestadorRoutes.Profile.route)
                    },
                    onNavigateToServiceConfig = {
                        navController.navigate(PrestadorRoutes.ServiceConfig.route)
                    },
                    onLogout = {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        val ref = if (uid != null)
                            com.google.firebase.database.FirebaseDatabase.getInstance()
                                .reference.child("users").child(uid).child("online")
                        else null

                        if (ref != null) {
                            ref.setValue(false).addOnCompleteListener {
                                FirebaseAuth.getInstance().signOut()
                                try {
                                    activity?.let { ctx ->
                                        GoogleSignIn.getClient(
                                            ctx,
                                            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                .build()
                                        ).signOut()
                                    }
                                } catch (e: Exception) {
                                }
                                activity?.also { ctx ->
                                    val restartIntent = Intent(ctx, ctx::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    }
                                    ctx.startActivity(restartIntent)
                                    ctx.finish()
                                }
                            }
                        } else {
                            FirebaseAuth.getInstance().signOut()
                            activity?.also { ctx ->
                                val restartIntent = Intent(ctx, ctx::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                }
                                ctx.startActivity(restartIntent)
                                ctx.finish()
                            }
                        }
                    },
                    onNavigateToPresupuesto = {
                        navController.navigate(PrestadorRoutes.CrearPresupuesto.createRoute("dashboard"))
                    },
                    onNavigateToPresupuestoCita = { appointmentId ->
                        navController.navigate(
                            PrestadorRoutes.CrearPresupuesto.createRoute(
                                "calendar",
                                appointmentId
                            )
                        )
                    },
                    onNavigateToPresupuestos = {
                        navController.navigate(PrestadorRoutes.Presupuestos.route)
                    },
                    onNavigateToPromotion = {
                        navController.navigate(PrestadorRoutes.CreatePromotion.route)
                    },
                    onNavigateToPromotionList = {
                        navController.navigate(PrestadorRoutes.PromotionsList.route)
                    },
                    onNavigateToClientePerfil = { clientId ->
                        navController.navigate(PrestadorRoutes.ClientePerfil.createRoute(clientId))
                    }
                )
            }

            composable(PrestadorRoutes.ServiceConfig.route) {
                ConfiguracionScreen(
                    onBack = { navController.navigateUp() },
                    onNavigateToCalendario = {
                        navController.navigate(PrestadorRoutes.CalendarioConfig.route)
                    },
                    onNavigateToPresupuestoConfig = {
                        navController.navigate(PrestadorRoutes.PresupuestoConfig.route)
                    }
                )
            }

            composable(PrestadorRoutes.CalendarioConfig.route) {
                CalendarioConfigScreen(
                    onBack = { navController.navigateUp() },
                    onGoToEditProfile = {
                        navController.navigate(PrestadorRoutes.Profile.route)
                    }
                )
            }

            composable(PrestadorRoutes.Profile.route) {
                ProfileScreen(
                    onBack = { navController.navigateUp() },
                    onNavigateToCalendarioConfig = {
                        navController.navigate(PrestadorRoutes.CalendarioConfig.route)
                    }
                )
            }

            composable("chat") {
                // Se debe inyectar la dependencia de ViewModel real de chat aquí
                // Para simplificar, asumiremos que PrestadorChatScreen obtiene el suyo
                // o que debe ser pasado explícitamente desde aquí,
                // si es necesario usar hiltViewModel() como se indica en la declaración.
                PrestadorChatScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onNavigateToPresupuesto = {
                        navController.navigate(PrestadorRoutes.CrearPresupuesto.createRoute("chat"))
                    }
                )
            }

            composable(
                route = PrestadorRoutes.CrearPresupuesto.route,
                arguments = listOf(
                    navArgument("origin") {
                        type = NavType.StringType
                        defaultValue = "dashboard"
                    },
                    navArgument("appointmentId") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val origin = backStackEntry.arguments?.getString("origin") ?: "dashboard"
                val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
                CrearPresupuestoPrestadorScreen(
                    appointmentId = appointmentId,
                    onBack = {
                        when (origin) {

                            "chat" -> navController.navigate("chat") {
                                popUpTo(PrestadorRoutes.CrearPresupuesto.route) { inclusive = true }
                            }

                            "presupuestos" -> navController.navigate(PrestadorRoutes.Presupuestos.route) {
                                popUpTo(PrestadorRoutes.CrearPresupuesto.route) { inclusive = true }
                            }

                            else -> navController.popBackStack()
                        }
                    }
                )
            }

            composable(PrestadorRoutes.Presupuestos.route) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    PresupuestosScreen(
                        onBack = { navController.popBackStack() },
                        onCrearNuevo = {
                            navController.navigate(PrestadorRoutes.CrearPresupuesto.createRoute("presupuestos"))
                        },
                        onVerDetalle = { presupuesto ->
                            // TODO: Navegar a detalle de presupuesto
                        },
                        onNavigateToConfig = {

                            navController.navigate(PrestadorRoutes.PresupuestoConfig.route)
                        }
                    )
                }
            }

            composable(PrestadorRoutes.PresupuestoConfig.route) {
                PresupuestoConfigScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(PrestadorRoutes.CreatePromotion.route) {
                CreatePromotionScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onPublish = { promotion ->
                        //Implementar lógica de publicacion
                        navController.popBackStack()
                    }
                )
            }

            composable(PrestadorRoutes.PromotionsList.route) {
                PromotionListScreen(
                    onBack = { navController.popBackStack() },
                    onPromotionClick = { promotionId ->
                        navController.navigate(
                            PrestadorRoutes.PromotionDetail.createRoute(
                                promotionId
                            )
                        )
                    }
                )
            }

            composable(
                route = PrestadorRoutes.PromotionDetail.route,
                arguments = listOf(
                    navArgument("promotionId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val promotionId =
                    backStackEntry.arguments?.getString("promotionId") ?: return@composable
                PromotionDetailScreen(
                    promotionId = promotionId,
                    onBack = { navController.popBackStack() },
                    onEdit = { id ->
                        navController.navigate(PrestadorRoutes.EditPromotion.createRoute(id))
                    }
                )
            }

            

            composable(
                route = PrestadorRoutes.EditPromotion.route,
                arguments = listOf(
                    navArgument("promotionId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val promotionId =
                    backStackEntry.arguments?.getString("promotionId") ?: return@composable
                CreatePromotionScreen(
                    promotionId = promotionId,
                    onBack = { navController.popBackStack() },
                    onPublish = { navController.popBackStack() }
                )
            }
            composable(
                route = PrestadorRoutes.ClientePerfil.route,
                arguments = listOf(
                    navArgument("clientId") { type = NavType.StringType }
                )
            ) {
                ClientePerfilScreen(
                    onBack = { navController.popBackStack() }
                )
            }

        }

        // LaunchedEffect eliminado porque dependía de chatSimulationViewModel que se eliminó.
        // Si se necesita manejar notificaciones, deberá implementarse de otra forma.

    }
}
