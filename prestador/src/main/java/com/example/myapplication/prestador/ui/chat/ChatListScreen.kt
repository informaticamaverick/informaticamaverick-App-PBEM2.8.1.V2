package com.example.myapplication.prestador.ui.chat

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.myapplication.core.domain.model.CompanyProvider
import com.example.myapplication.prestador.data.ChatData
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.chat.InboxType
import java.text.SimpleDateFormat
import java.util.*

/**
 * --- CHAT LIST SCREEN (ELITE PROVIDER EDITION) ---
 * [ELITE v9.0]: Reestructurada para paridad total con la App del Usuario.
 * Implementa el Selector Multi-Identidad (Perfil/Empresas) y el Nivel 2 de Sucursales.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatListScreen(
    conversations: List<ChatData.Conversation>,
    isSearchActive: Boolean,
    searchQuery: String,
    currentFilter: ChatFilterState,
    sortMode: SortMode,
    isDeletionMode: Boolean,
    selectedChatsForDeletion: Set<String>,
    onSearchActiveChange: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (ChatFilterState) -> Unit,
    onSortModeChange: (SortMode) -> Unit,
    onDeletionModeChange: (Boolean) -> Unit,
    onChatSelectionChange: (Set<String>) -> Unit,
    onChatClick: (userId: String, conversationId: String) -> Unit,
    onBack: () -> Unit,
    onShowNotificationDialog: () -> Unit,
    onShowVisibilityDialog: () -> Unit,
    onShowDateRangeDialog: () -> Unit,
    onShowLockDialog: () -> Unit,
    onRequestDeleteConfirmation: () -> Unit,
    onDeleteSelected: (Set<String>) -> Unit,
    selectedInbox: InboxType,
    hasCompanyInbox: Boolean,
    providerPhotoUrl: String?,
    companyPhotoUrl: String?,
    companyName: String,
    onInboxChange: (InboxType, String?, String?) -> Unit,
    providerCompanies: List<CompanyProvider>,
    activeCompanyId: String?,
    activeBranchId: String?,
    onRefresh: () -> Unit // 🔥 [NUEVO]
) {
    val colors = getPrestadorColors()
    val listState = rememberLazyListState()
    val pullState = rememberPullToRefreshState()
    val focusRequester = remember { FocusRequester() }

    // --- FILTRADO Y ORDENAMIENTO ---
    val filteredConversations = remember(conversations, searchQuery, currentFilter, sortMode) {
        Log.d("ChatListScreen", "♻️ [RENDER_LIST] Conversations received: ${conversations.size}")
        conversations.filter { conv ->
            val matchesSearch = if (searchQuery.isBlank()) true 
            else conv.userName.contains(searchQuery, ignoreCase = true) || conv.lastMessage.contains(searchQuery, ignoreCase = true)
            
            val matchesFilter = when (currentFilter) {
                ChatFilterState.ALL -> true
                ChatFilterState.NOTIFICATIONS_ON -> conv.notificationsEnabled
                ChatFilterState.VISIBLE -> conv.isVisible
                ChatFilterState.DATE_RANGE -> true
                ChatFilterState.LOCKED -> conv.isLocked
                ChatFilterState.UNREAD -> conv.unreadCount > 0
            }
            matchesSearch && matchesFilter
        }.let { list ->
            if (sortMode == SortMode.RECENT) list.sortedByDescending { it.timestamp }
            else list.sortedBy { it.userName }
        }
    }

    // --- LÓGICA DE HEADER COLAPSABLE ---
    val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val topBarAlpha by animateFloatAsState(targetValue = if (firstVisibleItemIndex > 0) 1f else 0f, label = "alpha")
    
    val headerMaxHeight = 140.dp
    val headerMinHeight = 64.dp
    val density = LocalDensity.current
    val maxScrollPx = with(density) { (headerMaxHeight - headerMinHeight).toPx() }
    val collapseFraction by remember {
        derivedStateOf {
            if (firstVisibleItemIndex == 0) (listState.firstVisibleItemScrollOffset.toFloat() / maxScrollPx).coerceIn(0f, 1f) else 1f
        }
    }

    Scaffold(
        containerColor = colors.backgroundColor,
        topBar = {
            if (isDeletionMode) {
                TopAppBar(
                    title = { Text("${selectedChatsForDeletion.size} seleccionados") },
                    navigationIcon = { IconButton(onClick = { onDeletionModeChange(false); onChatSelectionChange(emptySet()) }) { Icon(Icons.Default.Close, null) } },
                    actions = { IconButton(onClick = onRequestDeleteConfirmation, enabled = selectedChatsForDeletion.isNotEmpty()) { Icon(Icons.Default.Delete, null) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.primaryOrange, titleContentColor = Color.White, navigationIconContentColor = Color.White, actionIconContentColor = Color.White)
                )
            } else {
                Surface(modifier = Modifier.fillMaxWidth().graphicsLayer { alpha = topBarAlpha }, color = colors.surfaceColor, shadowElevation = 4.dp) {
                    Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = colors.primaryOrange) }
                        Text(text = if (selectedInbox == InboxType.PERSONAL) "Chats Personales" else companyName.ifBlank { "Bandeja Empresa" }, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }
                }
            }
        }
    ) { padding ->
        PullToRefreshBox(
            state = pullState,
            isRefreshing = false,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp)) {
                    item { Spacer(Modifier.height(headerMaxHeight)) }

                    // --- NIVEL 2: SELECTOR DE SUCURSALES ---
                    val activeCompany = providerCompanies.find { it.id == activeCompanyId }
                    if (selectedInbox == InboxType.EMPRESA && activeCompany != null && activeCompany.branches.isNotEmpty()) {
                        item {
                            LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                item { BranchSelectorChip(label = "GENERAL", isSelected = activeBranchId == null, onClick = { onInboxChange(InboxType.EMPRESA, activeCompanyId, null) }, colors = colors) }
                                items(activeCompany.branches) { branch -> BranchSelectorChip(label = branch.name.uppercase(), isSelected = activeBranchId == branch.id, onClick = { onInboxChange(InboxType.EMPRESA, activeCompanyId, branch.id) }, colors = colors) }
                            }
                        }
                    }

                    // --- LISTA DE CONVERSACIONES ---
                    if (filteredConversations.isEmpty()) {
                        item { EmptyChatState(colors) }
                    } else {
                        items(filteredConversations, key = { it.conversationId }) { conversation ->
                            ChatListItemElite(
                                conversation = conversation,
                                isSelected = selectedChatsForDeletion.contains(conversation.userId),
                                inDeletionMode = isDeletionMode,
                                onClick = {
                                    if (isDeletionMode) {
                                        val newSelection = if (selectedChatsForDeletion.contains(conversation.userId)) selectedChatsForDeletion - conversation.userId else selectedChatsForDeletion + conversation.userId
                                        onChatSelectionChange(newSelection)
                                    } else onChatClick(conversation.userId, conversation.conversationId)
                                },
                                onLongClick = { onDeletionModeChange(true); onChatSelectionChange(setOf(conversation.userId)) },
                                colors = colors
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = colors.border.copy(alpha = 0.3f))
                        }
                    }
                }

                // --- NIVEL 1: CABECERA MULTI-IDENTIDAD (Elite account switcher) ---
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(headerMaxHeight - (headerMaxHeight - headerMinHeight) * collapseFraction)
                        .zIndex(10f),
                    color = colors.backgroundColor,
                    tonalElevation = 6.dp,
                    shadowElevation = if (collapseFraction > 0.1f) 4.dp else 0.dp
                ) {
                    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = colors.primaryOrange)
                            }
                            if (isSearchActive) {
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = onSearchQueryChange,
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(focusRequester)
                                        .background(colors.surfaceElevated, RoundedCornerShape(24.dp))
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    singleLine = true,
                                    textStyle = TextStyle(color = colors.textPrimary, fontSize = 16.sp),
                                    cursorBrush = SolidColor(colors.primaryOrange),
                                    decorationBox = { inner -> 
                                        Box(contentAlignment = Alignment.CenterStart) { 
                                            if (searchQuery.isEmpty()) Text("Buscar mensajes...", color = colors.textSecondary, fontSize = 14.sp)
                                            inner() 
                                        } 
                                    }
                                )
                                IconButton(onClick = { onSearchActiveChange(false); onSearchQueryChange("") }) { 
                                    Icon(Icons.Default.Close, null, tint = colors.textSecondary) 
                                }
                            } else {
                                Text(
                                    "Mis Bandejas", 
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                    color = colors.textPrimary, 
                                    modifier = Modifier.weight(1f).graphicsLayer { alpha = (1f - collapseFraction * 2.5f).coerceIn(0f, 1f) }
                                )
                                IconButton(onClick = { onSearchActiveChange(true) }) { 
                                    Icon(Icons.Default.Search, null, tint = colors.textPrimary) 
                                }
                            }
                        }

                        if (!isSearchActive) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                item {
                                    IdentityBubble(
                                        photoUrl = providerPhotoUrl,
                                        label = "Personal",
                                        isSelected = selectedInbox == InboxType.PERSONAL,
                                        onClick = { onInboxChange(InboxType.PERSONAL, null, null) },
                                        colors = colors,
                                        collapseFraction = collapseFraction
                                    )
                                }
                                items(providerCompanies) { company ->
                                    IdentityBubble(
                                        photoUrl = company.photoUrl ?: company.thumbnailBase64,
                                        label = company.name,
                                        isSelected = selectedInbox == InboxType.EMPRESA && activeCompanyId == company.id,
                                        onClick = { onInboxChange(InboxType.EMPRESA, company.id, null) },
                                        colors = colors,
                                        collapseFraction = collapseFraction
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IdentityBubble(
    photoUrl: String?,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: PrestadorColors,
    collapseFraction: Float
) {
    val scale by animateFloatAsState(targetValue = if (isSelected) 1.1f else 1f, label = "scale")
    val size by animateDpAsState(targetValue = if (collapseFraction > 0.5f) 42.dp else 56.dp, label = "size")
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer { 
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    brush = if (isSelected) Brush.sweepGradient(listOf(colors.primaryOrange, Color.Yellow, colors.primaryOrange))
                            else SolidColor(colors.border),
                    shape = CircleShape
                )
                .background(colors.surfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = photoUrl,
                contentDescription = label,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            if (photoUrl == null) {
                Text(label.take(1).uppercase(), style = MaterialTheme.typography.titleLarge, color = colors.primaryOrange)
            }
        }
        if (collapseFraction < 0.2f) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                color = if (isSelected) colors.primaryOrange else colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp).widthIn(max = 64.dp)
            )
        }
    }
}

@Composable
fun BranchSelectorChip(label: String, isSelected: Boolean, onClick: () -> Unit, colors: PrestadorColors) {
    Surface(modifier = Modifier.clickable { onClick() }.animateContentSize(), shape = RoundedCornerShape(12.dp), color = if (isSelected) colors.primaryOrange.copy(alpha = 0.15f) else colors.surfaceElevated, border = BorderStroke(1.dp, if (isSelected) colors.primaryOrange else colors.border)) {
        Text(text = label, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = if (isSelected) colors.primaryOrange else colors.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
    }
}

@Composable
fun ChatListItemElite(
    conversation: ChatData.Conversation,
    isSelected: Boolean,
    inDeletionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    colors: PrestadorColors
) {
    val locale = LocalConfiguration.current.locales[0]
    val timeStr = remember(conversation.timestamp, locale) { 
        SimpleDateFormat("HH:mm", locale).format(Date(conversation.timestamp)) 
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = if (isSelected) colors.primaryOrange.copy(0.08f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(60.dp)) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = colors.surfaceElevated,
                    tonalElevation = 2.dp
                ) { 
                    AsyncImage(
                        model = conversation.userAvatarUrl, 
                        contentDescription = null, 
                        modifier = Modifier.fillMaxSize().clip(CircleShape), 
                        contentScale = ContentScale.Crop
                    ) 
                }
                if (conversation.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color(0xFF22C55E), CircleShape)
                            .border(2.dp, colors.backgroundColor, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = conversation.userName, 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.ExtraBold, 
                            color = colors.textPrimary, 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis
                        )
                        // 🔥 [NUEVO v10.5] Contexto de Entidad (Empresa/Sucursal)
                        // Evita confusión en bandejas con múltiples sucursales
                        if (!conversation.branchId.isNullOrBlank()) {
                            Text(
                                text = "vía ${conversation.branchId}".uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.primaryOrange.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    Text(
                        text = timeStr, 
                        style = MaterialTheme.typography.labelSmall,
                        color = if (conversation.unreadCount > 0) colors.primaryOrange else colors.textSecondary
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.lastMessage, 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = if (conversation.unreadCount > 0) colors.textPrimary else colors.textSecondary, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis, 
                        modifier = Modifier.weight(1f), 
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                    )
                    if (conversation.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .background(colors.primaryOrange, CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) { 
                            Text(
                                text = conversation.unreadCount.toString(), 
                                color = Color.White, 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Black 
                            ) 
                        }
                    }
                }
            }
            if (inDeletionMode) {
                Checkbox(
                    checked = isSelected, 
                    onCheckedChange = { onClick() }, 
                    modifier = Modifier.padding(start = 8.dp),
                    colors = CheckboxDefaults.colors(checkedColor = colors.primaryOrange)
                )
            }
        }
    }
}

@Composable
fun EmptyChatState(colors: PrestadorColors) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.ChatBubbleOutline, null, modifier = Modifier.size(80.dp), tint = colors.border)
        Spacer(Modifier.height(16.dp))
        Text("No hay conversaciones aquí", color = colors.textSecondary, fontWeight = FontWeight.Medium)
    }
}

// ==========================================
// SECCIÓN DE VISTAS PREVIAS (PREVIEWS)
// ==========================================

@Preview(showBackground = true, name = "Chat List - Multi Identity")
@Composable
fun PreviewChatListScreen() {
    val mockConversations = listOf(
        ChatData.Conversation(
            userId = "1",
            userName = "Juan Cliente",
            lastMessage = "Hola, ¿cómo estás?",
            timestamp = System.currentTimeMillis(),
            unreadCount = 2,
            isOnline = true
        ),
        ChatData.Conversation(
            userId = "2",
            userName = "Maria Gomez",
            lastMessage = "Gracias por el presupuesto",
            timestamp = System.currentTimeMillis() - 3600000,
            unreadCount = 0,
            isOnline = false
        )
    )
    
    ChatListScreen(
        conversations = mockConversations,
        isSearchActive = false,
        searchQuery = "",
        currentFilter = ChatFilterState.ALL,
        sortMode = SortMode.RECENT,
        isDeletionMode = false,
        selectedChatsForDeletion = emptySet(),
        onSearchActiveChange = {},
        onSearchQueryChange = {},
        onFilterChange = {},
        onSortModeChange = {},
        onDeletionModeChange = {},
        onChatSelectionChange = {},
        onChatClick = { _, _ -> },
        onBack = {},
        onShowNotificationDialog = {},
        onShowVisibilityDialog = {},
        onShowDateRangeDialog = {},
        onShowLockDialog = {},
        onRequestDeleteConfirmation = {},
        onDeleteSelected = {},
        selectedInbox = InboxType.PERSONAL,
        hasCompanyInbox = true,
        providerPhotoUrl = null,
        companyPhotoUrl = null,
        companyName = "Maverick Tech",
        onInboxChange = { _, _, _ -> },
        providerCompanies = listOf(
            com.example.myapplication.core.domain.model.CompanyProvider(id = "c1", name = "Empresa A")
        ),
        activeCompanyId = null,
        activeBranchId = null,
        onRefresh = {}
    )
}
