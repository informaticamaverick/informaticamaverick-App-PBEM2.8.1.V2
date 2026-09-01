package com.example.myapplication.prestador.ui.pantallas.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.myapplication.core.datos.local.entidades.ConversacionEntity
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.core.utilidades.ChatPreviewUtils
import com.example.myapplication.prestador.viewmodel.chat.InboxType
import java.text.SimpleDateFormat
import java.util.*

/**
 * --- PALETA DE COLORES EXECUTIVE PRO (ESTILO INDUSTRIAL RECTANGULAR) ---
 */
private object ThemeColors {
    val DarkBg = Color(0xFF030712)
    val CardBg = Color(0xFF0F172A)
    val CardBorder = Color(0xFF334155).copy(alpha = 0.7f)
    val HeaderBg = Color(0xFF020617).copy(alpha = 0.95f)
    val Divider = Color(0xFF1E293B)
    
    val BrandOrange = Color(0xFFFF5722)
    val BrandOrangeHover = Color(0xFFF4511E)
    val BrandCyan = Color(0xFF06B6D4)
    val BrandAmber = Color(0xFFF59E0B)
    val BrandEmerald = Color(0xFF10B981)
    val BrandPurple = Color(0xFFA855F7)
    
    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)
    val SelectionBg = Color(0xFF1E293B)
}

/**
 * Pantalla de Buzón de Mensajes Pro v2 (Estilo Ejecutivo / Android)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatListScreen(
    conversations: List<ConversacionEntity>,
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
    providerPhotoUrl: Any?,
    companyPhotoUrl: Any?,
    companyName: String,
    onInboxChange: (InboxType, String?, String?) -> Unit,
    providerCompanies: List<PrestadorDominio>,
    companyBranches: List<PrestadorDominio>,
    activeCompanyId: String?,
    activeBranchId: String?,
    onRefresh: () -> Unit
) {
    val pullState = rememberPullToRefreshState()
    val scrollState = rememberLazyListState()

    var filtroChipSeleccionado by remember { mutableStateOf("TODOS") }

    val conversationsFiltradas = remember(conversations, searchQuery, filtroChipSeleccionado) {
        conversations.filter { conv ->
            val coincideBusqueda = searchQuery.isBlank() ||
                    conv.nombreRemoto.contains(searchQuery, ignoreCase = true) ||
                    conv.ultimoMensaje.contains(searchQuery, ignoreCase = true)

            val coincideChip = when (filtroChipSeleccionado) {
                "NO_LEIDOS" -> conv.contadorNoLeidos > 0
                "PRESUPUESTOS" -> conv.ultimoMensaje.contains("presupuesto", ignoreCase = true) || conv.ultimoMensaje.contains("#PRES", ignoreCase = true)
                "CONCURSOS" -> conv.ultimoMensaje.contains("concurso", ignoreCase = true) || conv.ultimoMensaje.contains("#CONC", ignoreCase = true)
                else -> true
            }

            coincideBusqueda && coincideChip
        }
    }

    val totalSinLeer = remember(conversations) {
        conversations.sumOf { it.contadorNoLeidos }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ThemeColors.DarkBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (!isDeletionMode) {
                FloatingActionButton(
                    onClick = { },
                    containerColor = ThemeColors.BrandOrange,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Nuevo Chat", modifier = Modifier.size(20.dp))
                }
            }
        }
    ) { paddingValues ->

        PullToRefreshBox(
            state = pullState,
            isRefreshing = false,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {

                CabeceraBandeja(
                    titulo = if (selectedInbox == InboxType.PERSONAL) "BANDEJA DE MENSAJES" else companyName.uppercase(),
                    subtitulo = if (selectedInbox == InboxType.PERSONAL) "Perfil Particular" else "Bandeja Corporativa",
                    conteoSinLeer = totalSinLeer,
                    isSearchActive = isSearchActive,
                    searchQuery = searchQuery,
                    onBack = onBack,
                    onToggleSearch = { onSearchActiveChange(!isSearchActive) },
                    onSearchQueryChange = onSearchQueryChange
                )

                CarpetasIdentidad(
                    selectedInbox = selectedInbox,
                    providerCompanies = providerCompanies,
                    activeCompanyId = activeCompanyId,
                    onInboxChange = onInboxChange
                )

                if (selectedInbox == InboxType.EMPRESA && companyBranches.isNotEmpty()) {
                    SelectorSucursales(
                        branches = companyBranches,
                        activeBranchId = activeBranchId,
                        onBranchClick = { onInboxChange(InboxType.EMPRESA, activeCompanyId, it) }
                    )
                }

                ChipsFiltradoRapido(
                    chipSeleccionado = filtroChipSeleccionado,
                    onChipSeleccionado = { filtroChipSeleccionado = it }
                )

                HorizontalDivider(color = ThemeColors.Divider, thickness = 1.dp)

                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    if (conversationsFiltradas.isEmpty()) {
                        item {
                            EstadoBandejaVacia(modifier = Modifier.fillParentMaxSize())
                        }
                    } else {
                        items(conversationsFiltradas, key = { it.idChat }) { conversation ->
                            TarjetaChatListItem(
                                conversation = conversation,
                                isSelected = selectedChatsForDeletion.contains(conversation.idChat),
                                onClick = {
                                    if (isDeletionMode) {
                                        val newSet = selectedChatsForDeletion.toMutableSet()
                                        if (newSet.contains(conversation.idChat)) newSet.remove(conversation.idChat)
                                        else newSet.add(conversation.idChat)
                                        onChatSelectionChange(newSet)
                                    } else {
                                        onChatClick(conversation.idIdentidadRemota, conversation.idChat)
                                    }
                                },
                                onLongClick = {
                                    onDeletionModeChange(true)
                                    onChatSelectionChange(setOf(conversation.idChat))
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 72.dp),
                                color = ThemeColors.Divider.copy(alpha = 0.5f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CabeceraBandeja(
    titulo: String,
    subtitulo: String,
    conteoSinLeer: Int,
    isSearchActive: Boolean,
    searchQuery: String,
    onBack: () -> Unit,
    onToggleSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ThemeColors.HeaderBg,
        border = BorderStroke(1.dp, ThemeColors.Divider)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .background(ThemeColors.CardBg, RoundedCornerShape(8.dp))
                            .border(1.dp, ThemeColors.CardBorder, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = ThemeColors.TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = titulo,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ThemeColors.TextPrimary,
                                letterSpacing = 0.5.sp
                            )

                            if (conteoSinLeer > 0) {
                                Surface(
                                    color = ThemeColors.BrandOrange.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(2.dp),
                                    border = BorderStroke(1.dp, ThemeColors.BrandOrange.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "$conteoSinLeer NUEVOS",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ThemeColors.BrandOrange,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = subtitulo,
                            fontSize = 11.sp,
                            color = ThemeColors.TextSecondary
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onToggleSearch,
                        modifier = Modifier
                            .size(36.dp)
                            .background(ThemeColors.CardBg, RoundedCornerShape(8.dp))
                            .border(1.dp, ThemeColors.CardBorder, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = ThemeColors.TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Buscar cliente, mensaje o #presupuesto...", fontSize = 11.sp, color = ThemeColors.TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ThemeColors.CardBg,
                        unfocusedContainerColor = ThemeColors.CardBg,
                        focusedBorderColor = ThemeColors.BrandOrange,
                        unfocusedBorderColor = ThemeColors.CardBorder,
                        focusedTextColor = ThemeColors.TextPrimary,
                        unfocusedTextColor = ThemeColors.TextPrimary
                    ),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = ThemeColors.TextMuted, modifier = Modifier.size(16.dp)) }
                )
            }
        }
    }
}

@Composable
private fun CarpetasIdentidad(
    selectedInbox: InboxType,
    providerCompanies: List<PrestadorDominio>,
    activeCompanyId: String?,
    onInboxChange: (InboxType, String?, String?) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ThemeColors.CardBg,
        border = BorderStroke(1.dp, ThemeColors.Divider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PestanaCarpeta(
                etiqueta = "PERSONAL",
                icono = Icons.Default.Person,
                esSeleccionado = selectedInbox == InboxType.PERSONAL,
                onClick = { onInboxChange(InboxType.PERSONAL, null, null) }
            )

            providerCompanies.forEach { company ->
                PestanaCarpeta(
                    etiqueta = company.titulo.uppercase(),
                    icono = Icons.Default.Business,
                    esSeleccionado = selectedInbox == InboxType.EMPRESA && activeCompanyId == company.id,
                    onClick = { onInboxChange(InboxType.EMPRESA, company.id, null) }
                )
            }
        }
    }
}

@Composable
private fun PestanaCarpeta(
    etiqueta: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    esSeleccionado: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(42.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = if (esSeleccionado) ThemeColors.BrandOrange else ThemeColors.TextMuted,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = etiqueta,
                fontSize = 11.sp,
                fontWeight = if (esSeleccionado) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (esSeleccionado) ThemeColors.BrandOrange else ThemeColors.TextSecondary
            )
        }

        if (esSeleccionado) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(ThemeColors.BrandOrange)
            )
        }
    }
}

@Composable
private fun ChipsFiltradoRapido(
    chipSeleccionado: String,
    onChipSeleccionado: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ThemeColors.DarkBg)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BotonChip("TODOS", "TODOS", chipSeleccionado, onChipSeleccionado)
        BotonChip("NO_LEIDOS", "SIN LEER", chipSeleccionado, onChipSeleccionado, icono = Icons.Default.MarkUnreadChatAlt)
        BotonChip("PRESUPUESTOS", "PRESUPUESTOS", chipSeleccionado, onChipSeleccionado, icono = Icons.AutoMirrored.Filled.ReceiptLong, colorAcento = ThemeColors.BrandCyan)
        BotonChip("CONCURSOS", "CONCURSOS", chipSeleccionado, onChipSeleccionado, icono = Icons.Default.Gavel, colorAcento = ThemeColors.BrandAmber)
    }
}

@Composable
private fun BotonChip(
    idChip: String,
    etiqueta: String,
    chipSeleccionado: String,
    onChipSeleccionado: (String) -> Unit,
    icono: androidx.compose.ui.graphics.vector.ImageVector? = null,
    colorAcento: Color = ThemeColors.BrandOrange
) {
    val esSeleccionado = chipSeleccionado == idChip

    Surface(
        onClick = { onChipSeleccionado(idChip) },
        shape = RoundedCornerShape(6.dp),
        color = if (esSeleccionado) ThemeColors.CardBg else Color.Transparent,
        border = BorderStroke(1.dp, if (esSeleccionado) colorAcento else ThemeColors.CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icono != null) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = if (esSeleccionado) colorAcento else ThemeColors.TextMuted,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = etiqueta,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = if (esSeleccionado) ThemeColors.TextPrimary else ThemeColors.TextSecondary
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TarjetaChatListItem(
    conversation: ConversacionEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val nombreLimpio = remember(conversation.nombreRemoto) {
        if (conversation.nombreRemoto.isBlank()) "Chat Profesional" else conversation.nombreRemoto
    }

    val esPresupuesto = remember(conversation.ultimoMensaje) {
        conversation.ultimoMensaje.contains("presupuesto", ignoreCase = true) || conversation.ultimoMensaje.contains("#PRES", ignoreCase = true)
    }
    val esConcurso = remember(conversation.ultimoMensaje) {
        conversation.ultimoMensaje.contains("concurso", ignoreCase = true) || conversation.ultimoMensaje.contains("#CONC", ignoreCase = true)
    }

    val colorAcentoLateral = when {
        esPresupuesto -> ThemeColors.BrandCyan
        esConcurso -> ThemeColors.BrandAmber
        conversation.contadorNoLeidos > 0 -> ThemeColors.BrandOrange
        else -> Color.Transparent
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = if (isSelected) ThemeColors.SelectionBg else ThemeColors.DarkBg
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(colorAcentoLateral)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(46.dp)) {
                    AsyncImage(
                        model = conversation.fotoRemotaUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, ThemeColors.CardBorder, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(ThemeColors.BrandEmerald, CircleShape)
                            .border(1.5.dp, ThemeColors.DarkBg, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = nombreLimpio,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThemeColors.TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (esPresupuesto) {
                                Surface(
                                    color = ThemeColors.BrandCyan.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(2.dp)
                                ) {
                                    Text(
                                        text = "PRESUPUESTO",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ThemeColors.BrandCyan,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            } else if (esConcurso) {
                                Surface(
                                    color = ThemeColors.BrandAmber.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(2.dp)
                                ) {
                                    Text(
                                        text = "CONCURSO",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ThemeColors.BrandAmber,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        val horaFormateada = remember(conversation.fechaUltimoMensaje) {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(conversation.fechaUltimoMensaje))
                        }
                        Text(
                            text = horaFormateada,
                            fontSize = 10.sp,
                            fontWeight = if (conversation.contadorNoLeidos > 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (conversation.contadorNoLeidos > 0) ThemeColors.BrandOrange else ThemeColors.TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ChatPreviewUtils.obtenerTextoVistaPrevia(conversation.tipoUltimoMensaje, conversation.ultimoMensaje),
                            fontSize = 11.sp,
                            color = if (conversation.contadorNoLeidos > 0) ThemeColors.TextPrimary else ThemeColors.TextSecondary,
                            fontWeight = if (conversation.contadorNoLeidos > 0) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (conversation.contadorNoLeidos > 0) {
                            Surface(
                                color = ThemeColors.BrandOrange,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(start = 6.dp)
                            ) {
                                Text(
                                    text = conversation.contadorNoLeidos.toString(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectorSucursales(
    branches: List<PrestadorDominio>,
    activeBranchId: String?,
    onBranchClick: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(ThemeColors.CardBg)
            .padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            ChipSucursal("TODAS", activeBranchId == null) { onBranchClick(null) }
        }
        items(branches) { branch ->
            ChipSucursal(branch.nombreSucursal?.uppercase() ?: "SUCURSAL", activeBranchId == branch.id) {
                onBranchClick(branch.id)
            }
        }
    }
}

@Composable
private fun ChipSucursal(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        color = if (isSelected) ThemeColors.BrandCyan.copy(alpha = 0.15f) else ThemeColors.DarkBg,
        border = BorderStroke(1.dp, if (isSelected) ThemeColors.BrandCyan else ThemeColors.CardBorder)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) ThemeColors.BrandCyan else ThemeColors.TextMuted,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun EstadoBandejaVacia(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                color = ThemeColors.CardBg,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ThemeColors.CardBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Forum,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = ThemeColors.BrandOrange.copy(alpha = 0.6f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sin conversaciones activas",
                color = ThemeColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tus chats con clientes y presupuestos aparecerán aquí.",
                color = ThemeColors.TextMuted,
                fontSize = 11.sp
            )
        }
    }
}
