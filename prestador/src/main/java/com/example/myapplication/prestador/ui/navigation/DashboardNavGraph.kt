package com.example.myapplication.prestador.ui.navigation

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.example.myapplication.prestador.ui.dashboard.PrestadorDashboardScreen

fun NavGraphBuilder.dashboardNavGraph(navController: NavController) {

    composable (PrestadorRoutes.Dashboard.route) {
        val activity = LocalContext.current as? Activity

        PrestadorDashboardScreen (
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
                        .reference.child("users").child("online")
                else null

                if (ref != null) {
                    ref.setValue(false).addOnCompleteListener {
                        FirebaseAuth.getInstance().signOut()
                        try {
                            activity?.let { ctx ->
                                GoogleSignIn.getClient(
                                    ctx,
                                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
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
            onNavigateToPresupuesto = {
                navController.navigate(PrestadorRoutes.CrearPresupuesto.createRoute("dashboard"))
            },
            onNavigateToPresupuestoCita = { appointmentId ->
                navController.navigate(PrestadorRoutes.CrearPresupuesto.createRoute("calendar", appointmentId))
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
}