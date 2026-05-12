package com.example.myapplication.prestador.ui.navigation

sealed class PrestadorRoutes(val route: String) {
    object Login : PrestadorRoutes("login")
    object Register : PrestadorRoutes("register?isGoogle={isGoogle}") {
        fun createRoute(isGoogle: Boolean) = "register?isGoogle=$isGoogle"
    }
    object Success : PrestadorRoutes("success")
    object Dashboard : PrestadorRoutes("dashboard")
    object EditProfile : PrestadorRoutes("edit_profile")
    object Profile : PrestadorRoutes("profile")
    object Services : PrestadorRoutes("services")
    object ServiceConfig : PrestadorRoutes("service_config")
    object CalendarioConfig : PrestadorRoutes("calendario_config")

    object CalendarConfig : PrestadorRoutes("calendario_config")
    object CrearPresupuesto : PrestadorRoutes("crear_presupuesto?origin={origin}&appointmentId={appointmentId}") {
        fun createRoute(origin: String, appointmentId: String = "") =
            "crear_presupuesto?origin=$origin&appointmentId=$appointmentId"
    }

    object Presupuestos : PrestadorRoutes("presupuestos")
    object PresupuestoConfig : PrestadorRoutes("presupuesto_config")
    object CreatePromotion : PrestadorRoutes("create_promotion")
    object PromotionsList : PrestadorRoutes("promotion_list")
    object PromotionDetail : PrestadorRoutes("promotion_detail/{promotionId}") {
        fun createRoute(promotionId: String) = "promotion_detail/$promotionId"
    }
    object EditPromotion : PrestadorRoutes("edit_promotion/{promotionId}") {
        fun createRoute(promotionId: String) = "edit_promotion/$promotionId"
    }
    object ClientePerfil : PrestadorRoutes("cliente_perfil/{clientId}") {
        fun createRoute(clientId: String) = "cliente_perfil/$clientId"
    }
}

