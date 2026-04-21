package com.example.myapplication.presentation.registry

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MarkChatUnread
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.ui.graphics.Color
import com.example.myapplication.presentation.components.ControlItem

/** 📝 PASOS PARA AGREGAR NUEVOS FILTROS O ORDENAMIENTOS:
 * 1. Define el botón en 'BeMenuRegistry' con un ID único.
 * 2. Agrégalo a la lista correspondiente en 'availableFilters' o 'availableSortOptions' según el HUDContext en el ViewModel.
 */
object BeMenuRegistry {
    // --- ORDENAMIENTOS ---
    val SORT_ALPHA = ControlItem("Nombre", Icons.Default.SortByAlpha, "ABC", Color(0xFF2197F5), "sort_alpha")
    val SORT_DATE = ControlItem("Fecha", Icons.Default.CalendarToday, "📅", Color(0xFF9B51E0), "sort_date")
    val SORT_UNREAD = ControlItem("No Leídos", Icons.AutoMirrored.Filled.Message, "📩", Color(0xFFEF4444), "sort_unread")
    val SORT_PLAZO = ControlItem("Plazo", Icons.Default.HourglassBottom, "⏳", Color(0xFF22D3EE), "sort_plazo")
    val SORT_PRICE = ControlItem("Precio", Icons.Default.AttachMoney, "💰", Color(0xFF10B981), "sort_price")
    val SORT_DISTANCE = ControlItem("Cercanía", Icons.Default.NearMe, "📍", Color(0xFF22D3EE), "sort_distance")
    val SORT_RATING = ControlItem("Rating", Icons.Default.Star, "⭐", Color(0xFFFACC15), "sort_rating")
    val SORT_HOT = ControlItem("Fuego", null, "🔥", Color(0xFFE11D48), "sort_hot")
    val VIEW_COMPACT = ControlItem("Compacta", Icons.Default.ViewStream, "📱", Color(0xFFF59E0B), "view_compact")

    // --- FILTROS ---
    val FILTER_SUBSCRIBED = ControlItem("Suscrito", Icons.Default.Verified, "✅", Color(0xFF9B51E0), "filter_sub")
    val FILTER_FAVORITE = ControlItem("Favorito", Icons.Default.Favorite, "❤️", Color(0xFFE91E63), "filter_fav")
    val FILTER_ONLINE = ControlItem("Online", Icons.Default.Circle, "🌐", Color(0xFF10B981), "filter_online")
    val FILTER_PRODUCTS = ControlItem("Productos", Icons.Default.ShoppingBag, "🛍️", Color(0xFF22D3EE), "filter_products")
    val FILTER_SERVICES = ControlItem("Servicios", Icons.Default.Build, "🔧", Color(0xFFF59E0B), "filter_services")
    val FILTER_24H = ControlItem("24hs", Icons.Default.AccessTimeFilled, "⏳", Color(0xFFFF9800), "filter_24h")
    val FILTER_SHIPPING = ControlItem("Envios", Icons.Default.LocalShipping, "🚚", Color(0xFF9B51E0), "filter_shipping")
    val FILTER_VISITS = ControlItem("Visitas", Icons.Default.HomeWork, "🏠", Color(0xFF4CAF50), "filter_visits")
    val FILTER_LOCAL = ControlItem("Local", Icons.Default.Storefront, "🏪", Color(0xFF2197F5), "filter_local")
    val FILTER_APPOINTMENTS = ControlItem("Turnos", Icons.Default.EventAvailable, "📅", Color(0xFF00FFC2), "filter_appointments")

    // Filtros de Licitación (PresupuestosScreen)
    val FILTER_TENDER_ACTIVE = ControlItem("Abierta", Icons.Default.LockOpen, "📂", Color(0xFF10B981), "filter_tender_active")
    val FILTER_TENDER_CLOSED = ControlItem("Cerrada", Icons.Default.Lock, "📁", Color(0xFF64748B), "filter_tender_closed")
    val FILTER_TENDER_CANCELED = ControlItem("Cancelada", Icons.Default.Block, "🚫", Color(0xFFEF4444), "filter_tender_canceled")
    val FILTER_TENDER_AWARDED = ControlItem("Adjudicada", Icons.Default.EmojiEvents, "🏆", Color(0xFFFACC15), "filter_tender_awarded")
}
