package com.example.myapplication.prestador.ui.navigation

sealed class PrestadorRoutes(val route: String) {
    object Login : PrestadorRoutes("login")
    object OnboardingWizard : PrestadorRoutes("wizard?isGoogle={isGoogle}") {
        fun createRoute(isGoogle: Boolean) = "wizard?isGoogle=$isGoogle"
    }
    object Register : PrestadorRoutes("register?isGoogle={isGoogle}&tieneNegocio={tieneNegocio}") {
        fun createRoute(isGoogle: Boolean, tieneNegocio: Boolean) = "register?isGoogle=$isGoogle&tieneNegocio=$tieneNegocio"
    }
    object Success : PrestadorRoutes("success")
    object Dashboard : PrestadorRoutes("dashboard?tenderId={tenderId}") {
        fun createRoute(tenderId: String = "") = "dashboard?tenderId=$tenderId"
    }
    object Profile : PrestadorRoutes("profile")
    object Services : PrestadorRoutes("services")
    object ServiceConfig : PrestadorRoutes("service_config")
    
    object HorariosConfig : PrestadorRoutes("horarios_config?type={type}&addressId={addressId}") {
        fun createRoute(type: String? = null, addressId: String? = null) =
            "horarios_config?type=${type ?: ""}&addressId=${addressId ?: ""}"
    }

    // Configuración de horarios para empresa o sucursal específica
    object HorariosConfigEntity : PrestadorRoutes("horarios_config_entity/{owner_id}/{owner_name}?type={type}&addressId={addressId}") {
        fun createRoute(ownerId: String, ownerName: String, type: String? = null, addressId: String? = null) =
            "horarios_config_entity/$ownerId/${android.net.Uri.encode(ownerName)}?type=${type ?: ""}&addressId=${addressId ?: ""}"
    }
    
    object CrearPresupuesto : PrestadorRoutes("crear_presupuesto?origin={origin}&appointmentId={appointmentId}&tenderId={tenderId}&clientId={clientId}") {
        fun createRoute(origin: String, appointmentId: String = "", tenderId: String = "", clientId: String = "") =
            "crear_presupuesto?origin=$origin&appointmentId=$appointmentId&tenderId=$tenderId&clientId=$clientId"
    }

    object Presupuestos : PrestadorRoutes("presupuestos")
    object PresupuestoConfig : PrestadorRoutes("presupuesto_config")
    object CrearPromocion : PrestadorRoutes("crear_promocion")
    object PromocionesLista : PrestadorRoutes("promociones_lista")
    object PromocionDetalle : PrestadorRoutes("promocion_detalle/{promocionId}") {
        fun createRoute(promocionId: String) = "promocion_detalle/$promocionId"
    }
    object EditarPromocion : PrestadorRoutes("editar_promocion/{promocionId}") {
        fun createRoute(promocionId: String) = "editar_promocion/$promocionId"
    }
    object ClientePerfil : PrestadorRoutes("cliente_perfil/{clientId}") {
        fun createRoute(clientId: String) = "cliente_perfil/$clientId"
    }
    object Catalogo : PrestadorRoutes("catalogo")
    object GestionTurnos : PrestadorRoutes("gestion_turnos")
    object GestionVisitas : PrestadorRoutes("gestion_visitas")
    object Licitaciones : PrestadorRoutes("licitaciones")
    object Paywall : PrestadorRoutes("paywall")
    object AparienciaConfig : PrestadorRoutes("apariencia_config")
    object NotificacionesConfig : PrestadorRoutes("notificaciones_config")
    object LegalTerminos : PrestadorRoutes("legal_terminos")
    object LegalPrivacidad : PrestadorRoutes("legal_privacidad")
    object AcercaDe : PrestadorRoutes("acerca_de")
}
