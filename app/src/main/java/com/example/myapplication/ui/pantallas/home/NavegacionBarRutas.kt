package com.example.myapplication.ui.pantallas.home

import android.net.Uri

/**
 * NavegacionBarRutas.kt
 * Propósito: SSOT (Single Source of Truth) de las rutas y destinos de navegación.
 * Funcionamiento: Define la estructura de pantallas y utilidades de mapeo visual.
 * Relación: Utilizado por el NavHost y la Barra de Navegación V3.
 */

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Inicio")
    object Concursos : Screen("concursos", "Concursos")
    object Chat : Screen("chat?providerId={providerId}&branchId={branchId}&categoryId={categoryId}&clientBranchId={clientBranchId}", "Chat") {
        fun createRoute(providerId: String? = null, branchId: String? = null, categoryId: String? = null, clientBranchId: String? = null) =
            "chat?providerId=${providerId ?: ""}&branchId=${branchId ?: ""}&categoryId=${categoryId ?: ""}&clientBranchId=${clientBranchId ?: ""}"
    }
    object Calendar : Screen("calendar", "Calendario")
    object Promo : Screen("promo", "Promociones")
    object PerfilPrestador : Screen("perfil_prestador/{providerId}?companyId={companyId}&branchId={branchId}", "Perfil del Prestador") {
        fun createRoute(providerId: String, companyId: String? = null, branchId: String? = null): String {
            var path = "perfil_prestador/$providerId"
            val params = mutableListOf<String>()
            if (!companyId.isNullOrBlank()) params.add("companyId=$companyId")
            if (!branchId.isNullOrBlank()) params.add("branchId=$branchId")
            if (params.isNotEmpty()) {
                path += "?" + params.joinToString("&")
            }
            return path
        }
    }
    object PerfilCliente : Screen("perfil_cliente", "Mi Perfil")
    object ResultBusqueda : Screen("result_busqueda/{category}", "Resultados de Búsqueda") {
        fun createRoute(category: String) = "result_busqueda/${Uri.encode(category)}"
    }
    object NuevoConcurso : Screen("nuevo_concurso", "Nueva Licitación")
    object Urgencia : Screen("urgencia", "Urgente")
    object Configuracion : Screen("config_user", "Ajustes")
    object ConcursoPresupuesto : Screen("concurso_presupuesto/{idConcurso}", "Ofertas Recibidas") {
        fun createRoute(idConcurso: String) = "concurso_presupuesto/$idConcurso"
    }
    object ArchiveroChatMultimedia : Screen("archivero_chat_multimedia/{idRemoto}/{idLocal}", "Centro Multimedia") {
        fun createRoute(idRemoto: String, idLocal: String) = "archivero_chat_multimedia/$idRemoto/$idLocal"
    }
}

/**
 * Mapeo de Emojis representativos para cada destino.
 */
fun getEmojiForScreen(screen: Screen): String = when (screen) {
    Screen.Home -> "🏠"
    Screen.Concursos -> "💰"
    Screen.Chat -> "💬"
    Screen.Calendar -> "📅"
    Screen.Promo -> "🔥"
    else -> ""
}

/**
 * Obtiene el índice de una ruta dentro de una lista de destinos para animaciones de deslizamiento.
 */
fun getRouteIndex(route: String?, navItems: List<Screen>): Int {
    if (route == null) return -1
    val baseRoute = route.substringBefore("?").substringBefore("/")
    return navItems.indexOfFirst { it.route.substringBefore("?").substringBefore("/") == baseRoute }
}
