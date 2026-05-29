package com.example.myapplication.presentation.registry

import com.example.myapplication.presentation.registry.MaverickIcons
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myapplication.presentation.components.DropdownItemData
import com.example.myapplication.presentation.components.ControlItem
import com.example.myapplication.presentation.components.BeMessage

/**
 * --- MAVERICK BUTTON STYLES ---
 * Define el molde físico del botón.
 */
enum class MaverickButtonStyle {
    RECTANGULAR_PREMIUM, // MaverickButton (Grande, Neón)
    ACTION_CIRCLE,      // Estilo Cabecera (Circular Glass sutil)
    ROUND_BENTO,        // MaverickRoundButton (Círculo con etiqueta)
    TACTICAL_SQUARE,     // MaverickTacticalButton (HUD Cuadrado)
    COMPACT_HUD         // MaverickCompactHudButton (Sobre semi-esfera)
}

/**
 * --- BE DICTIONARY (DICCIONARIO DE ACTIVOS VISUALES) ---
 * Única fuente de verdad para Iconos, Emojis y Colores de la aplicación.
 * Permite que los Obreros (ViewModels) operen solo con IDs de comando.
 */
object BeDictionary {

    // ======================================================================================
    // --- SECCIÓN 1: ACCIONES DEL ASISTENTE (HUD) ---
    // ======================================================================================
    
    data class ActionVisuals(
        val label: String,
        val icon: ImageVector,
        val emoji: String? = null,
        val tint: Color = Color.White,
        val isDefault: Boolean = false,
        val style: MaverickButtonStyle = MaverickButtonStyle.ACTION_CIRCLE
    )

    val Actions = mapOf(
        // --- BOTONES DE CABECERA (ACTION_CIRCLE) ---
        "cancel" to ActionVisuals("Cerrar", MaverickIcons.Close, tint = Color.Red, style = MaverickButtonStyle.ACTION_CIRCLE),
        "view_details" to ActionVisuals("Detalles", Icons.Default.PriorityHigh, emoji = "❗", style = MaverickButtonStyle.ACTION_CIRCLE),
        "expand_view" to ActionVisuals("Subir", Icons.Default.ArrowUpward, style = MaverickButtonStyle.ACTION_CIRCLE),
        
        // --- ACCIONES TÁCTICAS (TACTICAL_SQUARE) ---
        "delete_multi" to ActionVisuals("Eliminar", MaverickIcons.Delete, tint = Color.Red, style = MaverickButtonStyle.TACTICAL_SQUARE),
        "compare_selected" to ActionVisuals("Comparar", Icons.AutoMirrored.Filled.CompareArrows, emoji = "⚖️", style = MaverickButtonStyle.TACTICAL_SQUARE),
        "select_all" to ActionVisuals("Todos", MaverickIcons.SelectAll, emoji = "✅", style = MaverickButtonStyle.TACTICAL_SQUARE),
        "mark_as_read" to ActionVisuals("Leídos", MaverickIcons.DoneAll, emoji = "📖", style = MaverickButtonStyle.TACTICAL_SQUARE),
        "fav" to ActionVisuals("Favoritos", MaverickIcons.Favorite, emoji = "❤️", style = MaverickButtonStyle.TACTICAL_SQUARE),
        "fast" to ActionVisuals("Fast", Icons.Default.FlashOn, emoji = "⚡", style = MaverickButtonStyle.TACTICAL_SQUARE),

        // --- ACCIONES BENTO (ROUND_BENTO) ---
        "goto_direct_budgets" to ActionVisuals("Presupuestos", MaverickIcons.Message, emoji = "📩", tint = Color(0xFF2197F5), style = MaverickButtonStyle.ROUND_BENTO),
        "licit" to ActionVisuals("Nueva Lic", MaverickIcons.Add, emoji = "📄", tint = Color(0xFF2197F5), style = MaverickButtonStyle.ROUND_BENTO),
        "goto_history" to ActionVisuals("Historial", MaverickIcons.History, emoji = "📜", tint = Color(0xFFFF9800), style = MaverickButtonStyle.ROUND_BENTO),
        "add_company" to ActionVisuals("Empresa", Icons.Default.Business, emoji = "🏢", style = MaverickButtonStyle.ROUND_BENTO),
        "settings_profile" to ActionVisuals("Ajustes", Icons.Default.Settings, emoji = "⚙️", style = MaverickButtonStyle.ROUND_BENTO),

        // --- BOTONES PREMIUM (RECTANGULAR_PREMIUM) ---
        "save_profile" to ActionVisuals("Guardar", Icons.Default.Save, emoji = "💾", tint = Color(0xFF00FFC2), style = MaverickButtonStyle.RECTANGULAR_PREMIUM),
        "edit_profile" to ActionVisuals("Editar", MaverickIcons.Edit, emoji = "✏️", style = MaverickButtonStyle.RECTANGULAR_PREMIUM),
        "cancel_edit" to ActionVisuals("Cancelar", Icons.Default.Close, emoji = "✖️", tint = Color.Red, style = MaverickButtonStyle.RECTANGULAR_PREMIUM),

        // --- OTROS ---
        "share" to ActionVisuals("Compartir", MaverickIcons.Share, emoji = "📤", style = MaverickButtonStyle.ACTION_CIRCLE),
        "divider_v1" to ActionVisuals("", Icons.Default.VerticalAlignBottom),
        "compare_all" to ActionVisuals("Comparar Todo", Icons.AutoMirrored.Filled.CompareArrows, emoji = "⚖️", isDefault = true, style = MaverickButtonStyle.TACTICAL_SQUARE)
    )

    // ======================================================================================
    // --- SECCIÓN 2: FILTROS Y ORDENAMIENTOS ---
    // ======================================================================================

    val Filters = mapOf(
        "filter_tender_active" to DropdownItemData("filter_tender_active", "Abiertas", "ESTADO", "⚡", MaverickIcons.Bolt, color = Color(0xFF00FFC2)),
        "filter_tender_closed" to DropdownItemData("filter_tender_closed", "Cerradas", "ESTADO", "🔒", MaverickIcons.Lock, color = Color.Gray),
        "filter_tender_awarded" to DropdownItemData("filter_tender_awarded", "Adjudicadas", "ESTADO", "🏆", MaverickIcons.Check, color = Color(0xFFD4AF37)),
        "filter_chat_unread" to DropdownItemData("filter_chat_unread", "No Leídos", "ESTADO", "🔔", MaverickIcons.Info, color = Color.Red),
        "filter_chat_online" to DropdownItemData("filter_chat_online", "Online", "ESTADO", "🌐", MaverickIcons.Online, color = Color(0xFF00E5FF)),
        "filter_verif" to DropdownItemData("filter_verif", "Confirmados", "ESTADO", "✅", MaverickIcons.Verified, color = Color(0xFF00FFC2)),
        "filter_fast" to DropdownItemData("filter_fast", "Pendientes", "ESTADO", "⏳", MaverickIcons.Timer, color = Color.Yellow),
        "filter_chat_business" to DropdownItemData("filter_chat_business", "Empresas", "TIPO", "🏢", MaverickIcons.Business, color = Color(0xFF2197F5)),
        "filter_chat_pro" to DropdownItemData("filter_chat_pro", "Profesionales", "TIPO", "👤", MaverickIcons.Person, color = Color.White),
        "filter_chat_sub" to DropdownItemData("filter_chat_sub", "Suscriptos", "TRAZAS", "💎", MaverickIcons.Verified, color = Color(0xFFD4AF37)),
        "filter_chat_fav" to DropdownItemData("filter_chat_fav", "Favoritos", "TRAZAS", "❤️", MaverickIcons.Favorite, color = Color.Red),
        "filter_chat_verified" to DropdownItemData("filter_chat_verified", "Verificados", "TRAZAS", "🛡️", MaverickIcons.Verified, color = Color(0xFF22D3EE)),
        "filter_chat_24h" to DropdownItemData("filter_chat_24h", "Trabaja 24hs", "SERVICIOS", "⏳", MaverickIcons.Clock24h, color = Color(0xFFFF9800)),
        "filter_chat_local" to DropdownItemData("filter_chat_local", "Local Físico", "SERVICIOS", "🏪", MaverickIcons.Local, color = Color(0xFF4CAF50)),
        "filter_products" to DropdownItemData("filter_products", "Productos", "TIPO", "🛍️", MaverickIcons.Build, color = Color(0xFFFF4081)),
        "filter_services" to DropdownItemData("filter_services", "Servicios", "TIPO", "🔧", MaverickIcons.Build, color = Color(0xFFFF9800)),
        "filter_online" to DropdownItemData("filter_online", "Online", "ESTADO", "🌐", MaverickIcons.Online, color = Color(0xFF00E5FF)),
        "filter_shipping" to DropdownItemData("filter_shipping", "Envíos", "SERVICIOS", "🚚", MaverickIcons.Location, color = Color(0xFF00BCD4)),
        "filter_visits" to DropdownItemData("filter_visits", "Visitas", "SERVICIOS", "🏠", MaverickIcons.Location, color = Color(0xFF2197F5)),
        "filter_appointments" to DropdownItemData("filter_appointments", "Turnos", "SERVICIOS", "📅", MaverickIcons.Calendar, color = Color(0xFF9C27B0)),
    )

    val Sorts = mapOf(
        "sort_alpha" to DropdownItemData("sort_alpha", "A-Z", "ORDEN", "🔤", MaverickIcons.SortAlpha, color = Color.White),
        "sort_date" to DropdownItemData("sort_date", "Fecha", "ORDEN", "📅", MaverickIcons.Calendar, color = Color(0xFF22D3EE)),
        "sort_ranking" to DropdownItemData("sort_ranking", "Ranking", "ORDEN", "⭐", MaverickIcons.Favorite, color = Color.Yellow),
        "sort_price" to DropdownItemData("sort_price", "Precio", "ORDEN", "💰", MaverickIcons.Budget, color = Color(0xFF4CAF50)),
        "sort_distance" to DropdownItemData("sort_distance", "Cercanía", "ORDEN", "📍", MaverickIcons.Location, color = Color(0xFFFF5252)),
        "sort_hot" to DropdownItemData("sort_hot", "Más Usados", "ORDEN", "🔥", MaverickIcons.Timer, color = Color(0xFFFF9800)),
        "sort_nombre_asc" to DropdownItemData("sort_nombre_asc", "A-Z", "ORDEN", "🔤", MaverickIcons.Sort, color = Color.White),
        "sort_random" to DropdownItemData("sort_random", "Aleatorio", "ORDEN", "🎲", MaverickIcons.Refresh, color = MaverickColors.MagentaNeon),
        "view_compact" to DropdownItemData("view_compact", "Compacto", "VISTA", "📱", MaverickIcons.Person, color = Color.White),
        "view_grid" to DropdownItemData("view_grid", "Grilla", "VISTA", "🔳", MaverickIcons.Map, color = Color.White),
        "view_bento" to DropdownItemData("view_bento", "Bento", "VISTA", "🍱", MaverickIcons.Map, color = Color.White)
    )

    // ======================================================================================
    // --- SECCIÓN 3: CONFIGURACIÓN DE PANTALLAS (CONTEXTOS) ---
    // ======================================================================================


    data class ScreenContextVisuals(
        val title: String,
        val subtitle: String,
        val emoji: String,
        val accentColor: Color
    )

    val Contexts = mapOf(
        "home" to ScreenContextVisuals("Maverick", "Exploración Táctica", "🏠", Color(0xFF00F0FF)),
        "presupuestos" to ScreenContextVisuals("Licitaciones", "Gestión de Concursos", "⚖️", Color(0xFF00F0FF)),
        "chat" to ScreenContextVisuals("Mensajes", "Bandeja de Entrada", "💬", Color(0xFF00F0FF)),
        "calendar" to ScreenContextVisuals("Agenda", "Compromisos Activos", "🗓️", Color(0xFF00F0FF)),
        "perfil_cliente" to ScreenContextVisuals("Perfil", "Mi Cuenta Elite", "👤", Color(0xFF00F0FF))
    )
}
