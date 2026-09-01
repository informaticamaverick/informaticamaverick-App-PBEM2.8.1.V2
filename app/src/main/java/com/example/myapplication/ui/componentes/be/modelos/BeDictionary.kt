package com.example.myapplication.ui.componentes.be.modelos

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Close
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myapplication.uishared.estilos.AppPalette
import com.example.myapplication.ui.componentes.appFilterItem

enum class AppButtonStyle {
    ACTION_CIRCLE,
    RECTANGULAR_PREMIUM,
    ROUND_BENTO,
    TACTICAL_SQUARE,
    COMPACT_HUD
}

object BeDictionary {

    data class VisualesAccion(
        val icon: ImageVector,
        val label: String,
        val emoji: String? = null,
        val tint: Color = Color.White,
        val isDefault: Boolean = false,
        val style: AppButtonStyle = AppButtonStyle.ACTION_CIRCLE
    )

    data class VisualesContexto(
        val title: String,
        val subtitle: String,
        val emoji: String,
        val accentColor: Color = AppPalette.ElectricCyan
    )

    val Actions = mapOf(
        "sim_chat" to VisualesAccion(Icons.AutoMirrored.Filled.Chat, "Sim Chat", "💬", AppPalette.ElectricCyan, true, AppButtonStyle.ACTION_CIRCLE),
        "sim_tender" to VisualesAccion(Icons.Default.Gavel, "Sim Licit", "⚖️", AppPalette.ElectricPurple, true, AppButtonStyle.ACTION_CIRCLE),
        "sim_massive" to VisualesAccion(Icons.Default.PersonAdd, "Sim Prov", "👥", AppPalette.SuccessGreen, true, AppButtonStyle.ACTION_CIRCLE),
        "migrate_cats" to VisualesAccion(Icons.Default.CloudUpload, "Migrar", "☁️", Color.Yellow, true, AppButtonStyle.ACTION_CIRCLE),
        "fast" to VisualesAccion(Icons.Default.FlashOn, "Urgente", "⚡", AppPalette.RogCrimson, true, AppButtonStyle.ROUND_BENTO),
        "fav" to VisualesAccion(Icons.Default.Favorite, "Favoritos", "❤️", Color.Red, true, AppButtonStyle.ROUND_BENTO),
        "share" to VisualesAccion(Icons.Default.Share, "Compartir", "📤", Color.White, true, AppButtonStyle.ROUND_BENTO),
        "cancel" to VisualesAccion(Icons.Default.Close, "Cancelar", null, Color.Red, true, AppButtonStyle.ACTION_CIRCLE),
        "select_all" to VisualesAccion(Icons.Default.DoneAll, "Todo", null, AppPalette.ElectricCyan, true, AppButtonStyle.ACTION_CIRCLE),
        "delete_multi" to VisualesAccion(Icons.Default.Delete, "Borrar", null, Color.Red, true, AppButtonStyle.ACTION_CIRCLE),
        "add_fav_multi" to VisualesAccion(Icons.Default.Favorite, "Favoritos", "❤️", Color.White, true, AppButtonStyle.ACTION_CIRCLE),
        "remove_fav_multi" to VisualesAccion(Icons.Default.FavoriteBorder, "Quitar", "💔", Color.White, true, AppButtonStyle.ACTION_CIRCLE),
        "teclado" to VisualesAccion(Icons.Default.Keyboard, "Teclado", null, Color.White, true, AppButtonStyle.ACTION_CIRCLE),
        "cerrar_todo" to VisualesAccion(Icons.Default.Close, "Cerrar", null, AppPalette.DeepRed, true, AppButtonStyle.ACTION_CIRCLE),
        "goto_history" to VisualesAccion(Icons.Default.History, "Historial", "🕰️", Color.White, true, AppButtonStyle.ACTION_CIRCLE),
        "goto_direct_budgets" to VisualesAccion(Icons.Default.AttachMoney, "Directos", "💰", AppPalette.SuccessGreen, true, AppButtonStyle.ACTION_CIRCLE),
        "concurso_nuevo" to VisualesAccion(Icons.Default.Add, "Nuevo", "📝", AppPalette.AcidGreen, true, AppButtonStyle.ACTION_CIRCLE),
        "archivo_chat" to VisualesAccion(Icons.Default.Folder, "Archivo", "📂", Color.Yellow, true, AppButtonStyle.ACTION_CIRCLE),
        "compare_budgets" to VisualesAccion(Icons.Default.Assessment, "Comparar", "📊", AppPalette.ElectricCyan, true, AppButtonStyle.ACTION_CIRCLE),
        "config" to VisualesAccion(Icons.Default.Settings, "Config", "⚙️", Color.Gray, true, AppButtonStyle.ACTION_CIRCLE),
        "sig" to VisualesAccion(Icons.AutoMirrored.Filled.ArrowForward, "Sig", "➡️", AppPalette.SuccessGreen, true, AppButtonStyle.ACTION_CIRCLE),
        "atras" to VisualesAccion(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", "⬅️", Color.White, true, AppButtonStyle.ACTION_CIRCLE),
        "ir" to VisualesAccion(Icons.Default.PlayArrow, "Ir", "🚀", AppPalette.ElectricCyan, true, AppButtonStyle.ACTION_CIRCLE),
        "publicar" to VisualesAccion(Icons.Default.CloudUpload, "Publicar", "🚀", AppPalette.AcidGreen, true, AppButtonStyle.ACTION_CIRCLE),
        "cerrar_wizard" to VisualesAccion(Icons.Default.Close, "Cerrar", "✖️", AppPalette.DeepRed, true, AppButtonStyle.ACTION_CIRCLE),
        "edit" to VisualesAccion(Icons.Default.Edit, "Editar", "📝", Color.Yellow, true, AppButtonStyle.ACTION_CIRCLE),
        "menu" to VisualesAccion(Icons.Default.Menu, "Menú", "☰", Color.White, true, AppButtonStyle.ACTION_CIRCLE),
        "view_details" to VisualesAccion(Icons.Default.Info, "Detalles", "ℹ️", AppPalette.ElectricCyan, true, AppButtonStyle.ACTION_CIRCLE),
        "force_close" to VisualesAccion(Icons.Default.StopCircle, "Cerrar", "🛑", AppPalette.RogCrimson, true, AppButtonStyle.ACTION_CIRCLE)
    )

    /**
     * --- SECTOR: FILTROS Y ORDENAMIENTO (v2026.SCREEN) ---
     * [PROPÓSITO]: Repositorio central de visuales para las Barras de Filtros de las pantallas.
     * [NOTA]: El Asistente Be ya no maneja estos datos directamente (Ley #12).
     */
    val Filters = mapOf(
        "filter_online" to appFilterItem("filter_online", "Online", "status", "🌐", null, AppPalette.SuccessGreen),
        "filter_chat_24h" to appFilterItem("filter_chat_24h", "24 Horas", "status", "🌙", null, AppPalette.ElectricPurple),
        "filter_chat_sub" to appFilterItem("filter_chat_sub", "Suscrito", "status", "💎", null, AppPalette.ElectricCyan),
        "filter_chat_local" to appFilterItem("filter_chat_local", "Local", "status", "📍", null, Color.White),
        "filter_unread" to appFilterItem("filter_unread", "No leídos", "status", "🔔", null, AppPalette.ElectricCyan),
        "filter_verified" to appFilterItem("filter_verified", "Verificados", "status", "✅", null, AppPalette.SuccessGreen),
        "filter_concurso_activo" to appFilterItem("filter_concurso_activo", "Abiertos", "status", "🟢"),
        "filter_concurso_cerrado" to appFilterItem("filter_concurso_cerrado", "Cerrados", "status", "🔴"),
        "filter_concurso_adjudicado" to appFilterItem("filter_concurso_adjudicado", "Adjudicados", "status", "🏆"),
        "filter_concurso_no_leidos" to appFilterItem("filter_concurso_no_leidos", "No leídos", "status", "🔔"),
        "filter_event_visit" to appFilterItem("filter_event_visit", "Visitas", "status", "🛠️"),
        "filter_event_appointment" to appFilterItem("filter_event_appointment", "Turnos", "status", "🗓️"),
        "filter_event_shipping" to appFilterItem("filter_event_shipping", "Envíos", "status", "📦")
    )

    val Sorts = mapOf(
        "sort_hot" to appFilterItem("sort_hot", "Destacados", "sort", "🔥"),
        "sort_favorites" to appFilterItem("sort_favorites", "Favoritos Primero", "sort", "⭐"),
        "sort_alpha_asc" to appFilterItem("sort_alpha_asc", "A - Z", "sort", "🔤"),
        "sort_alpha_desc" to appFilterItem("sort_alpha_desc", "Z - A", "sort", "🔡"),
        "sort_nombre_asc" to appFilterItem("sort_nombre_asc", "Nombre", "sort", "🔤"),
        "sort_random" to appFilterItem("sort_random", "Aleatorio", "sort", "🎲"),
        "view_grid" to appFilterItem("view_grid", "Cuadrícula", "view", "📱"),
        "view_bento" to appFilterItem("view_bento", "Bento", "view", "🍱"),
        "sort_date" to appFilterItem("sort_date", "Fecha", "sort", "📅"),
        "sort_alpha" to appFilterItem("sort_alpha", "Alfabético", "sort", "ABC"),
        "sort_concursos_conteo" to appFilterItem("sort_concursos_conteo", "Cantidad", "sort", "📊")
    )


    val Contexts = mapOf(
        "concursos" to VisualesContexto("MIS CONCURSOS", "Gestión de solicitudes", "💰", AppPalette.ElectricPurple),
        "chat" to VisualesContexto("MIS CHATS", "Mensajería encriptada", "💬", AppPalette.ElectricCyan),
        "agenda" to VisualesContexto("MI AGENDA", "Próximos compromisos", "📅", AppPalette.ElectricCyan)
    )

    /**
     * Resuelve el icono final basado en el estado de la acción.
     * [LEY #12]: Centralización de lógica visual.
     */
    fun obtenerIconoSoberano(id: String, estaSeleccionado: Boolean): ImageVector? {
        val visuales = Actions[id] ?: return null
        return if (id == "select_all" && estaSeleccionado) Icons.Rounded.Close else visuales.icon
    }

    /**
     * Resuelve la etiqueta final basada en el estado de la acción.
     */
    fun obtenerEtiquetaSoberana(id: String, estaSeleccionado: Boolean): String {
        val visuales = Actions[id] ?: return ""
        return when {
            id == "select_all" && estaSeleccionado -> "Ninguno"
            id == "force_close" -> "Terminar"
            else -> visuales.label
        }
    }
}
