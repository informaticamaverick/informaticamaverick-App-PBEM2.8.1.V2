package com.example.myapplication.prestador.ui.navigation

import android.app.Activity
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.example.myapplication.prestador.ui.pantallas.dashboard.PrestadorDashboardScreen
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN

fun NavGraphBuilder.dashboardNavGraph(navController: NavController) {

    composable (
        route = PrestadorRoutes.Dashboard.route,
        arguments = listOf(
            navArgument("idConcurso") {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { backStackEntry ->
        val concursoId = backStackEntry.arguments?.getString("idConcurso") ?: ""
        val activity = LocalContext.current as? Activity

        PrestadorDashboardScreen (
            idConcursoInicial = concursoId,
            onNavigateToEditProfile = {
                navController.navigate(PrestadorRoutes.Profile.route)
            },
            onLogout = {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                val ref = if (uid != null)
                    com.google.firebase.database.FirebaseDatabase.getInstance()
                        .reference.child("users").child("online")
                else null

                if (ref != null) {
                    ref.setValue(false).addOnCompleteListener {
                        FirebaseAuth.getInstance().signOut()
                        try {
                            activity?.let { ctx ->
                                GoogleSignIn.getClient(
                                    ctx,
                                    Builder(DEFAULT_SIGN_IN).build()
                                ).signOut()
                            }
                        } catch (e: Exception) {}
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
                        val restartIntent = Intent(ctx, ctx:: class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                        ctx.startActivity(restartIntent)
                        ctx.finish()
                    }
                }
            },
            onNavigateToPresupuesto = { idConcurso, idCliente ->
                navController.navigate(PrestadorRoutes.CrearPresupuesto.createRoute("dashboard", tenderId = idConcurso, clientId = idCliente))
            },
            onNavigateToPromotionList = {
                navController.navigate(PrestadorRoutes.PromocionesLista.route)
            },
            onNavigateToClientePerfil = { clientId ->
                navController.navigate(PrestadorRoutes.ClientePerfil.createRoute(clientId))
            },
            onNavigateToPresupuestoConfig = {
                navController.navigate(PrestadorRoutes.PresupuestoConfig.route)
            },
            onNavigateToHorariosConfig = {
                navController.navigate(PrestadorRoutes.HorariosConfig.route)
            },
            onNavigateToApariencia = {
                navController.navigate(PrestadorRoutes.AparienciaConfig.route)
            },
            onNavigateToNotificaciones = {
                navController.navigate(PrestadorRoutes.NotificacionesConfig.route)
            },
            onNavigateToTerminos = {
                navController.navigate(PrestadorRoutes.LegalTerminos.route)
            },
            onNavigateToPrivacidad = {
                navController.navigate(PrestadorRoutes.LegalPrivacidad.route)
            },
            onNavigateToAcercaDe = {
                navController.navigate(PrestadorRoutes.AcercaDe.route)
            },
            onNavigateToPaywall = {
                navController.navigate(PrestadorRoutes.Paywall.route)
            },
            onNavigateToGestionTurnos = {
                navController.navigate(PrestadorRoutes.GestionTurnos.route)
            },
            onNavigateToGestionVisitas = {
                navController.navigate(PrestadorRoutes.GestionVisitas.route)
            }
        )
    }

    composable(route = PrestadorRoutes.Paywall.route) {
        val contexto = LocalContext.current
        val actividad = contexto as? Activity
        val viewModel: com.example.myapplication.prestador.viewmodel.premium.PaywallViewModel = hiltViewModel()

        com.example.myapplication.prestador.ui.premium.MuroDePago(
            onBack = { navController.popBackStack() },
            onSubscribeClick = {
                actividad?.let { act ->
                    viewModel.iniciarCompra(act)
                }
            },
            onSimulateClick = {
                viewModel.simularPagoExitoso {
                    navController.popBackStack()
                }
            }
        )
    }
}
