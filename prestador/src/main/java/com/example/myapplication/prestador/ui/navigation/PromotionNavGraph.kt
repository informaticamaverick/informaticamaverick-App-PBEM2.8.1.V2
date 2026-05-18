package com.example.myapplication.prestador.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.prestador.ui.promotion.CreatePromotionScreen
import com.example.myapplication.prestador.ui.promotion.PromotionDetailScreen
import com.example.myapplication.prestador.ui.promotion.PromotionListScreen

fun NavGraphBuilder.promotionNavGraph(navController: NavHostController) {
    composable(PrestadorRoutes.CreatePromotion.route) {
        CreatePromotionScreen(
            onBack = { navController.navigateUp() },
            onPublish = { navController.navigateUp() }
        )
    }

    composable(PrestadorRoutes.PromotionsList.route) {
        PromotionListScreen(
            onBack = { navController.navigateUp() },
            onPromotionClick = { promotionId ->
                navController.navigate(PrestadorRoutes.PromotionDetail.createRoute(promotionId))
            }
        )
    }

    composable(
        route = PrestadorRoutes.PromotionDetail.route,
        arguments = listOf(
            navArgument("promotionId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val promotionId = backStackEntry.arguments?.getString("promotionId") ?: return@composable
        PromotionDetailScreen(
            promotionId = promotionId,
            onBack = { navController.navigateUp() },
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
        val promotionId = backStackEntry.arguments?.getString("promotionId") ?: return@composable
        CreatePromotionScreen(
            promotionId = promotionId,
            onBack = { navController.navigateUp() },
            onPublish = { navController.navigateUp() }
        )
    }
}