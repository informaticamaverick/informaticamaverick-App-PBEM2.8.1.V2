package com.example.myapplication.presentation.client

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
//import com.example.myapplication.utils.ChatIdHelper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.Provider
import com.example.myapplication.data.model.CompanyProvider
import com.example.myapplication.data.model.AddressProvider
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.util.ChatIdHelper
import com.example.myapplication.ui.theme.AppColors
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.getThemeColors
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * VISTA DE LISTA DE CHATS (Estilo WhatsApp + Maverick Glass)
 * [ACTUALIZADO] Soporte para la nueva estructura de categorías (List) y campos de Provider.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatListView(
    providersList: List<Provider>,
    allCategories: List<CategoryEntity>,
    unreadCounts: Map<String, Int> = emptyMap(),
    lastMessages: Map<String, com.example.myapplication.data.local.ChatLastMessage> = emptyMap(),
    currentUserId: String = "",
    onChatClick: (String) -> Unit,
    onBack: () -> Unit,
    appColors: AppColors,
    navController: NavHostController? = null
) {
    // --- ESTADOS NAVEGACIÓN Y BÚSQUEDA ---
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // --- ESTADOS PANEL TÁCTICO FAB ---
    var isFabExpanded by remember { mutableStateOf(false) }
    var activeFilters by remember { mutableStateOf(setOf<String>()) }

    // --- ESTADOS DE MULTISELECCIÓN ---
    var multiSelectEnabled by remember { mutableStateOf(false) }
    val selectedChatIds = remember { mutableStateListOf<String>() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // --- ESTADOS DE ORDENAMIENTO ---
    var sortByUnread by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // --- NUEVO: ESTADO DE CARGA INTELIGENTE ---
    var minimumWaitDone by remember { mutableStateOf(false) }

    // --- 🏗️ SECCIÓN: LÓGICA DE ANIMACIÓN DE CABECERA (SCROLL) ---
    val collapseFraction by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset.toFloat() / 250f).coerceIn(0f, 1f)
        }
    }

    LaunchedEffect(Unit) {
        delay(1500) // Tiempo mínimo de animación premium
        minimumWaitDone = true
    }

    val showLoadingScreen = !minimumWaitDone

    LaunchedEffect(sortByUnread) {
        if (providersList.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }
    /**
    // 🔥 LÓGICA DE CATEGORÍAS CONTEXTUALES ACTUALIZADA
    val dynamicCategoriesForPanel = remember(providersList) {
    val extractedCategories = providersList.flatMap { it.categories }.distinct()
    extractedCategories.map { catName ->
    ControlItem(
    label = catName,
    icon = null,
    emoji = getChatCategoryEmoji(catName),
    color = getChatCategoryColor(catName),
    id = "cat_${catName.lowercase()}"
    )
    }
    }
     **/
    // --- LÓGICA DE FILTRADO Y ORDENAMIENTO (ACTUALIZADA) ---
    val filteredProviders = remember(providersList, activeFilters, searchQuery, sortByUnread, unreadCounts) {
        val selectedCats = activeFilters.filter { it.startsWith("cat_") }.map { it.removePrefix("cat_").lowercase() }

        val baseList = providersList.filter { provider ->
            val matchesCategory = selectedCats.isEmpty() || provider.categories.any { it.lowercase() in selectedCats }
            val matchesSearch = searchQuery.isEmpty() || provider.displayName.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }

        if (sortByUnread) {
            baseList.sortedWith(compareByDescending<Provider> { provider ->
                val chatId = ChatIdHelper.generateChat(currentUserId, provider.id)
                unreadCounts[chatId] ?: 0
            }.thenByDescending { it.createdAt })
        } else {
            baseList.sortedByDescending { it.createdAt }
        }
    }

    val cancelSelection = {
        selectedChatIds.clear()
        multiSelectEnabled = false
        isFabExpanded = false
    }

    Box(modifier = Modifier.fillMaxSize().background(appColors.backgroundColor)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (!isSearchActive) {
                    val totalUnread = unreadCounts.values.sum()
                    // ==========================================================================================
                    // --- 🏗️ SECCIÓN: CABECERA DINÁMICA MAVERICK (REEMPLAZO DE TOPAPPBAR) ---
                    // ==========================================================================================
                    BarraCabezera(
                        title = "Mensajes",
                        subtitle = if (totalUnread > 0) "$totalUnread mensajes sin leer" else "Bandeja de Entrada",
                        emoji = "💬",
                        onBack = onBack,
                        onInfoClick = { sortByUnread = !sortByUnread }, // Mantenemos lógica de ordenamiento táctico
                        collapseFraction = collapseFraction,
                        accentColor = Color(0xFF2197F5)
                    )
                }
            }
        )
{ paddingValues ->
            val safePadding = PaddingValues(
                start = paddingValues.calculateStartPadding(LocalLayoutDirection.current).coerceAtLeast(0.dp),
                top = paddingValues.calculateTopPadding().coerceAtLeast(0.dp),
                end = paddingValues.calculateEndPadding(LocalLayoutDirection.current).coerceAtLeast(0.dp),
                bottom = paddingValues.calculateBottomPadding().coerceAtLeast(0.dp)
            )

            if (showLoadingScreen) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(safePadding),
                    contentAlignment = Alignment.Center
                ) {
                    // Reemplazamos por el nuevo componente inmersivo de Be
                    LoadingBeAssistantScreen(
                        mainText = "CARGANDO CHATS...",
                        subText = "Recuperando mensajes cifrados"
                    )
                }
            } else if (filteredProviders.isEmpty()) {

                Box(
                    modifier = Modifier.fillMaxSize().padding(safePadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.AutoMirrored.Filled.Message,
                            null,
                            tint = Color.Gray.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if (searchQuery.isNotEmpty()) "No hay resultados para '$searchQuery'" else "No tienes conversaciones activas",
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(safePadding),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(filteredProviders, key = { it.id }) { provider ->
                        val chatId = ChatIdHelper.generateChat(currentUserId, provider.id)
                        val unreadCount = unreadCounts[chatId] ?: 0
                        val isSelected = selectedChatIds.contains(provider.id)


                        val chatLastMsg = lastMessages[chatId]
                        ChatListItem(
                            provider = provider,
                            unreadCount = unreadCount,
                            lastMessage = chatLastMsg?.lastMessage,
                            lastTimestamp = chatLastMsg?.lastTimestamp,
                            isSentByMe = chatLastMsg?.lastSenderId == currentUserId,
                            isSelected = isSelected,
                            isMultiSelectMode = multiSelectEnabled,
                            chatId = chatId,
                            providerId = provider.id,
                            onClick = {
                                if (multiSelectEnabled) {
                                    if (isSelected) selectedChatIds.remove(provider.id) else selectedChatIds.add(
                                        provider.id
                                    )
                                    if (selectedChatIds.isEmpty()) multiSelectEnabled = false
                                } else onChatClick(provider.id)
                            },
                            onLongClick = {
                                multiSelectEnabled = true
                                if (!isSelected) selectedChatIds.add(provider.id)
                            },
                            onAvatarClick = { navController?.navigate("perfil_prestador/${provider.id}") }
                        )
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor = Color(0xFF161C24),
                titleContentColor = Color.White,
                textContentColor = Color.LightGray,
                icon = { Icon(Icons.Default.DeleteForever, null, tint = Color(0xFFE91E63)) },
                title = { Text("Eliminar Chats") },
                text = { Text("¿Estás seguro de que deseas eliminar ${selectedChatIds.size} conversación(es)?") },
                confirmButton = { TextButton(onClick = { cancelSelection(); showDeleteDialog = false }) { Text("Eliminar", color = Color(0xFFE91E63)) } },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar", color = Color.White) } }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListItem(
    provider: Provider,
    unreadCount: Int,
    lastMessage: String? = null,
    lastTimestamp: Long? = null,
    isSentByMe: Boolean = false,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onAvatarClick: () -> Unit,
    chatId: String = "",
    providerId: String = ""
) {
    val defaultBackground = Brush.verticalGradient(listOf(Color(0xFF1A1F26), Color(0xFF05070A)))
    val selectedBackground = Brush.verticalGradient(listOf(Color(0xFF2197F5).copy(alpha = 0.2f), Color(0xFF05070A)))
    val fallbackAvatar = rememberVectorPainter(Icons.Default.Person)
    val mainCompany = provider.companies.firstOrNull()

    val isProviderTyping by produceState(initialValue = false, chatId, providerId) {
        if (chatId.isEmpty() || providerId.isEmpty()) return@produceState
        val ref = FirebaseDatabase.getInstance().reference
            .child("chats").child(chatId).child("typing").child(providerId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                value = snapshot.getValue(Boolean::class.java) ?: false
            }

            override fun onCancelled(error: DatabaseError) {
                value = false
            }
        }
        ref.addValueEventListener(listener)
        awaitDispose { ref.removeEventListener(listener) }

    }

    val isProviderOnline by produceState(initialValue = false, provider.id) {
        val ref = FirebaseDatabase.getInstance().reference
            .child("users").child(provider.id).child("online")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { value = snapshot.getValue(Boolean::class.java) ?: false }
            override fun onCancelled(error: DatabaseError) { value = false }
        }
        ref.addValueEventListener(listener)
        awaitDispose { ref.removeEventListener(listener) }
    }

    Box(modifier = Modifier.fillMaxWidth().background(if (isSelected) selectedBackground else defaultBackground).combinedClickable(onClick = onClick, onLongClick = onLongClick)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.BottomEnd) {
                ProviderPhoto(
                    photoData = provider.photoUrl,
                    modifier = Modifier.size(56.dp).clip(CircleShape).border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape).clickable { onAvatarClick() }
                )
                if (isMultiSelectMode) {
                    Box(modifier = Modifier.offset(x = 4.dp, y = 4.dp).size(20.dp).background(if (isSelected) Color(0xFF2197F5) else Color.Transparent, CircleShape).border(2.dp, Color(0xFF05070A), CircleShape), contentAlignment = Alignment.Center) {
                        if (isSelected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                } else if (isProviderOnline) {
                    Box(modifier = Modifier.offset(x = (-2).dp, y = (-2).dp).size(14.dp).background(Color(0xFF00E676), CircleShape).border(2.dp, Color(0xFF05070A), CircleShape))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(text = provider.displayName, color = Color.White, fontSize = 16.sp, fontWeight = if (unreadCount > 0) FontWeight.Black else FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text(text = formatDateShortChat(lastTimestamp ?: provider.createdAt), color = if (unreadCount > 0) Color(0xFF10B981) else Color.Gray, fontSize = 11.sp)
                }

                if (mainCompany != null && mainCompany.name.isNotEmpty()) {
                    Text(text = mainCompany.name.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color(0xFF22D3EE), fontWeight = FontWeight.Black, fontSize = 9.sp, letterSpacing = 1.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    val categoryText = provider.categories.firstOrNull() ?: "Servicios"
                    if (isProviderTyping) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(12.dp))
                            Text("escribiendo...", color = Color(0xFF00E676), fontSize = 12.sp, maxLines = 1)
                        }
                    } else {
                        Text(text = lastMessage ?: "Chat de $categoryText", color = if (unreadCount > 0) Color.LightGray else Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    }

                    if (unreadCount > 0) {
                        Surface(color = Color(0xFF10B981), shape = CircleShape, modifier = Modifier.defaultMinSize(minWidth = 20.dp).padding(start = 6.dp)) {
                            Text(text = unreadCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(start = 88.dp).align(Alignment.BottomEnd), color = Color.White.copy(alpha = 0.05f))
    }
}

private fun formatDateShortChat(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Date()
    val fmt = if (date.date == now.date && date.month == now.month) "HH:mm" else "dd/MM"
    return SimpleDateFormat(fmt, Locale.getDefault()).format(date)
}

private fun getChatCategoryEmoji(title: String): String {
    return when {
        title.contains("Hogar", ignoreCase = true) -> "🏠"
        title.contains("Informatica", ignoreCase = true) -> "💻"
        title.contains("Electricidad", ignoreCase = true) -> "⚡"
        else -> "💬"
    }
}

private fun getChatCategoryColor(title: String): Color {
    return when {
        title.contains("Hogar", ignoreCase = true) -> Color(0xFFFAD2E1)
        title.contains("Informatica", ignoreCase = true) -> Color(0xFF38BDF8)
        else -> Color(0xFF10B981)
    }
}

@Preview(showBackground = true)
@Composable
fun ChatListViewPreview() {
    MyApplicationTheme {
        val sampleProviders = listOf(
            Provider(
                uid = "1",
                email = "provider1@example.com",
                displayName = "Provider One",
                name = "Provider",
                lastName = "One",
                phoneNumber = "123456789",
                categories = listOf("Plomería"),
                matricula = "12345",
                titulo = "Lic. en Plomería",
                cuilCuit = "20-12345678-9",
                address = AddressProvider(calle = "Falsa", numero = "123"),
                works24h = true,
                photoUrl = null,
                bannerImageUrl = null,
                hasCompanyProfile = false,
                isSubscribed = true,
                isVerified = true,
                isOnline = true,
                isFavorite = false,
                rating = 4.5f,
                createdAt = System.currentTimeMillis()
            )
        )
        ChatListView(
            providersList = sampleProviders,
            allCategories = emptyList(),
            unreadCounts = mapOf("chat__1" to 5),
            onChatClick = {},
            onBack = {},
            appColors = getThemeColors()
        )
    }
}

