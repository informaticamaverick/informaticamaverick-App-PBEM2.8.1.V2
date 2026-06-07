package com.example.myapplication.presentation.features.chat

import com.example.myapplication.core.domain.model.AddressUnico
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.myapplication.core.domain.model.Provider
import com.example.myapplication.core.utils.ImageUtils
import com.example.myapplication.presentation.designsystem.theme.AppColors
import com.example.myapplication.core.domain.model.MessageType
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.components.BudgetRequestDialog 
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import com.example.myapplication.core.data.local.entity.BudgetEntity
import com.example.myapplication.core.data.local.entity.MessageEntity
import com.example.myapplication.core.data.local.entity.TenderEntity
import com.example.myapplication.core.data.local.entity.CategoryEntity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import coil.compose.AsyncImage
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.designsystem.theme.getThemeColors
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.presentation.global.BeBrainViewModel
import com.example.myapplication.presentation.global.HUDContext
import com.example.myapplication.presentation.features.budget.BudgetViewModel
import com.example.myapplication.presentation.features.calendar.AppointmentViewModel
import com.example.myapplication.uishared.components.BudgetA4Viewer
import com.example.myapplication.core.domain.model.toEntity
import com.example.myapplication.presentation.features.home.UbicacionClimaViewModel

/**
 * PANTALLA DE CONVERSACIÓN DE CHAT (UI PURA)
 * Sigue la Regla de Oro: Pantalla tonta que solo recibe datos y eventos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationContent(
    provider: Provider,
    availableAddresses: List<AddressUnico> = emptyList(),
    uiState: ChatUiState,
    events: Flow<ChatUiEvent>,
    onBack: () -> Unit,
    appColors: AppColors,
    onSendMessage: (String) -> Unit,
    onSendImage: (Uri, android.content.Context) -> Unit,
    onSendLocation: (Double, Double, String) -> Unit,
    onSendAppointment: (String, String, String, String?, String?) -> Unit,
    onAudioClick: () -> Unit,
    onCancelAudio: () -> Unit,
    onBudgetClick: (String) -> Unit,
    onBudgetRequest: (String, String, Double, Double) -> Unit = { _, _, _, _ -> },
    onTypingStatus: (Boolean) -> Unit,
    onClearBudget: () -> Unit,
    onAcceptBudget: (BudgetEntity) -> Unit,
    onRejectBudget: (BudgetEntity) -> Unit,
    onRespondAppointment: (String, String, String, Boolean, String?, String?, String, String, String?, String?, String?) -> Unit,
    onTenderInvitation: (TenderEntity) -> Unit,
    onCalendarClick: () -> Unit = {},
    onAddressClick: (String) -> Unit = {},
    matchingTenders: List<TenderEntity> = emptyList(),
    ubicacionViewModel: UbicacionClimaViewModel? = null,
    allCategories: List<CategoryEntity> = emptyList(),
    // [NUEVO] Eventos para el BookingDialog (Obrero)
    onOpenBooking: (MessageEntity) -> Unit,
    onDaySelected: (DayAvailability, String) -> Unit,
    onTimeSelected: (String) -> Unit,
    onAddressSelected: (AddressUnico) -> Unit,
    onReply: (MessageEntity?) -> Unit = {},
    onSwitchContext: (String?) -> Unit = { _ -> }, // 🔥 [FIX v6]
    onShowBudgets: () -> Unit = {}, // 🔥 [NUEVO]
    onShowCalendar: () -> Unit = {}, // 🔥 [NUEVO]
    onShowSearch: () -> Unit = {}, // 🔥 [NUEVO]
    onDeleteChat: () -> Unit = {}, // 🔥 [NUEVO]
    onBlockProvider: () -> Unit = {}, // 🔥 [NUEVO]
    onReportProvider: () -> Unit = {}, // 🔥 [NUEVO]
    onToggleFavorite: () -> Unit = {}, // 🔥 [NUEVO]
    isFavorite: Boolean = false, // 🔥 [NUEVO]
    recordingTime: Int = 0 
) {
    // Resolver datos del Header usando el perfil base del prestador (Chat Unificado)
    val resolvedProviderName = provider.displayName

    var inputText by remember { mutableStateOf("") }
    var showAttachMenu by remember { mutableStateOf(false) }
    var showTenderSelectionDialog by remember { mutableStateOf(false) }
    var showBudgetRequestDialog by remember { mutableStateOf(false) }
    var showLocationSelectionDialog by remember { mutableStateOf(false) }
    var showCalendarBooking by remember { mutableStateOf<MessageEntity?>(null) }
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // --- LÓGICA DE COLAPSO DE CABECERA (Telegram Style) ---
    val collapseFraction by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset / 140f).coerceIn(0f, 1f)
        }
    }

    // 🔥 [ELITE] Botón Volver Abajo (Telegram Style)
    val showScrollToBottom by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 3 }
    }
    val coroutineScope = rememberCoroutineScope()

    // 🔥 [ELITE v4] Paging 3 Integration
    val pagingMessages = uiState.pagingMessages.collectAsLazyPagingItems()

    // 🔥 [NUEVO] AUTO-SCROLL AL RECIBIR MENSAJES (Paging 3)
    LaunchedEffect(pagingMessages.itemCount) {
        if (pagingMessages.itemCount > 0) {
            listState.animateScrollToItem(0)
        }
    }

    // Manejo de eventos del ViewModel
    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is ChatUiEvent.ShowError -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                ChatUiEvent.MessageSent -> { 
                    listState.animateScrollToItem(0) // Scroll al enviar
                }
            }
        }
    }

    // --- PERMISOS Y LAUNCHERS ---

    // VALORES PARA PREVIEW (Evitar FileProvider en modo diseño)
    val isInEditMode = LocalInspectionMode.current

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { onSendImage(it, context); showAttachMenu = false }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onAudioClick()
        } else {
            Toast.makeText(context, "Se requiere permiso de micrófono para grabar audio", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleAudioClick() {
        if (uiState.isRecording) {
            onAudioClick()
        } else {
            val hasPermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                onAudioClick()
            } else {
                micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // --- CÁMARA LAUNCHER Y PERMISOS (REGLA DE ORO: Captura a resolución completa) ---
    val tempPhotoUri = remember {
        if (isInEditMode) Uri.EMPTY
        else {
            val photoFile = java.io.File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
            FileProvider.getUriForFile(context, "com.example.myapplication.provider", photoFile)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            onSendImage(tempPhotoUri, context)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(tempPhotoUri)
        } else {
            Toast.makeText(context, "Se requiere permiso de cámara para tomar fotos", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleCameraClick() {
        val hasPermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            cameraLauncher.launch(tempPhotoUri)
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    // 🔥 [ELITE] Procesamiento centralizado de imagen (Ley #2)
    val effectiveProviderPhoto = remember(uiState.activeProvider, provider) {
        val target = uiState.activeProvider ?: provider
        ImageUtils.processImageSource(target.profileThumbnail ?: target.photoUrl)
    }

    // --- VISORES ---
    if (uiState.isFetchingFullBudget) {
        Dialog(onDismissRequest = {}) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.8f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF22D3EE))
                }
            }
        }
    }

    if (uiState.selectedBudget != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val budget = uiState.selectedBudget
        Dialog(
            onDismissRequest = { onClearBudget() },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            BudgetA4Viewer(
                prestador = provider.toEntity(),
                budget = budget,
                onDismiss = { onClearBudget() },
                clientName = "Cliente"
            ) { _, _ ->
                // Acciones del Cliente: Aceptar / Rechazar
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (budget.status == com.example.myapplication.core.data.local.entity.BudgetStatus.PENDIENTE) {
                        OutlinedButton(
                            onClick = { onRejectBudget(budget); onClearBudget() },
                            modifier = Modifier.height(42.dp),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("RECHAZAR", fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { onAcceptBudget(budget); onClearBudget() },
                            modifier = Modifier.height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22D3EE))
                        ) {
                            Text("ACEPTAR", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    } else {
                        StatusPillPremium(budget.status.name)
                    }
                }
            }
        }
    }

    if (showLocationSelectionDialog) {
        LocationSelectionDialog(
            availableAddresses = availableAddresses,
            appColors = appColors,
            onDismiss = { showLocationSelectionDialog = false },
            onSelect = { lat, lng, addr ->
                onSendLocation(lat, lng, addr)
                showLocationSelectionDialog = false
            },
            ubicacionViewModel = ubicacionViewModel
        )
    }

    if (showBudgetRequestDialog) {
        BudgetRequestDialog(
            provider = provider,
            availableAddresses = availableAddresses,
            onDismissRequest = { showBudgetRequestDialog = false },
            onAcceptRequest = { problem, address, lat, lng ->
                onBudgetRequest(problem, address, lat, lng)
                showBudgetRequestDialog = false
            }
        )
    }

    showCalendarBooking?.let { msg ->
        BookingDialog(
            message = msg,
            availableAddresses = availableAddresses,
            categories = allCategories,
            availableDays = uiState.bookingUiState.availableDays,
            selectedDay = uiState.bookingUiState.selectedDay,
            currentSlots = uiState.bookingUiState.slots,
            selectedTime = uiState.bookingUiState.selectedTime,
            selectedAddress = uiState.bookingUiState.selectedAddress,
            onDaySelected = { onDaySelected(it, msg.bookedSlotsJson ?: "[]") },
            onTimeSelected = onTimeSelected,
            onAddressSelected = onAddressSelected,
            onDismissRequest = { showCalendarBooking = null },
            onAcceptRequest = { dateStr, timeStr, address, label ->
                showCalendarBooking = null
                onSendAppointment(
                    dateStr,
                    timeStr,
                    if (label != null) "Cita en: $label ($address)" else "Turno en local del prestador",
                    msg.appointmentType,
                    address
                )
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column(modifier = Modifier.background(appColors.backgroundColor)) {
                
                AnimatedVisibility(
                    visible = uiState.replyingToMessage != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    uiState.replyingToMessage?.let { msg ->
                        ReplyPreviewBar(
                            message = msg,
                            onCancel = { onReply(null) },
                            colors = appColors
                        )
                    }
                }

                AnimatedVisibility(visible = showAttachMenu) {
                    AttachmentOptionsMenu(
                        onGallery = { galleryLauncher.launch("image/*"); showAttachMenu = false },
                        onCamera = { handleCameraClick(); showAttachMenu = false },
                        onLocation = { showLocationSelectionDialog = true; showAttachMenu = false },
                        onBudget = { showBudgetRequestDialog = true; showAttachMenu = false },
                        onAppointment = { showTenderSelectionDialog = true }
                    )
                }

                MessageInputBar(
                    value = inputText,
                    onValueChange = { inputText = it; onTypingStatus(it.isNotEmpty()) },
                    onSend = { onSendMessage(it); inputText = "" },
                    appColors = appColors,
                    onAttachmentClick = { showAttachMenu = !showAttachMenu },
                    onMicClick = { handleAudioClick() },
                    onCameraClick = { handleCameraClick() },
                    isRecording = uiState.isRecording,
                    recordingTime = recordingTime
                )
            }
        },
        containerColor = appColors.backgroundColor
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val safePadding = remember(paddingValues, layoutDirection) {
            PaddingValues(
                start = paddingValues.calculateStartPadding(layoutDirection).coerceAtLeast(0.dp),
                top = 0.dp,
                end = paddingValues.calculateEndPadding(layoutDirection).coerceAtLeast(0.dp),
                bottom = paddingValues.calculateBottomPadding().coerceAtLeast(0.dp)
            )
        }

        Box(modifier = Modifier.fillMaxSize().padding(safePadding)) {
            LazyColumn(
                state = listState,
                reverseLayout = true,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 130.dp, bottom = 12.dp, start = 4.dp, end = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    count = pagingMessages.itemCount,
                    key = pagingMessages.itemKey { it.message.id }
                ) { index ->
                    val uiModel = pagingMessages[index] ?: return@items
                    val message = uiModel.message
                    val budget = uiModel.budget
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // El separador de fecha es más complejo con Paging 3, 
                        // se recomienda usar insertSeparators en el Flow.
                        // Por ahora mantenemos la lógica básica si hay datos previos.
                        if (index < pagingMessages.itemCount - 1) {
                            val nextMsg = pagingMessages[index + 1]?.message
                            if (nextMsg != null && !isSameDayChat(message.timestamp, nextMsg.timestamp)) {
                                DateSeparator(timestamp = message.timestamp, colors = appColors)
                            }
                        }

                        when (message.type) {
                            MessageType.BUDGET -> {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    BudgetBubble(
                                        message = message,
                                        budget = budget,
                                        isMe = message.senderId != provider.id,
                                        appColors = appColors,
                                        categoryEmoji = uiModel.categoryEmoji,
                                        onClick = { 
                                            // 🔥 [FIX] Resolución de ID idéntica al ViewModel para consistencia Elite
                                            val bId = message.relatedId ?: message.id
                                            onBudgetClick(bId) 
                                        }
                                    )
                                } else {
                                    MessageBubble(
                                        message = message,
                                        appColors = appColors,
                                        isFromMe = message.senderId != provider.id,
                                        budget = budget,
                                        allCategories = allCategories,
                                        onReply = { onReply(message) },
                                        onAddressClick = onAddressClick
                                    )
                                }
                            }
                            MessageType.AUDIO -> AudioMessageBubble(
                                message = message,
                                appColors = appColors,
                                isMe = message.senderId != provider.id
                            )
                            MessageType.VISIT -> {
                                val isTechnical = message.appointmentType == "TECHNICAL_VISIT"
                                if (isTechnical) {
                                    TechnicalVisitProposalBubble(
                                        message = message,
                                        isMe = message.senderId != provider.id,
                                        appColors = appColors,
                                        categoryEmoji = uiModel.categoryEmoji,
                                        onAccept = { 
                                            onRespondAppointment(
                                                "preview", 
                                                message.id, 
                                                message.relatedId ?: "", 
                                                true, 
                                                message.appointmentDate, 
                                                message.appointmentTime, 
                                                "Visita Técnica", 
                                                provider.displayName, 
                                                effectiveProviderPhoto as? String,
                                                message.categoryId ?: provider.categories.firstOrNull(),
                                                null
                                            ) 
                                        },
                                        onReject = { onRespondAppointment("preview", message.id, message.relatedId ?: "", false, null, null, "", "", null, null, null) }
                                    )
                                } else {
                                    LocalAppointmentProposalBubble(
                                        message = message,
                                        isMe = message.senderId != provider.id,
                                        appColors = appColors,
                                        categoryEmoji = uiModel.categoryEmoji,
                                        onAccept = { 
                                            onRespondAppointment(
                                                "preview", 
                                                message.id, 
                                                message.relatedId ?: "", 
                                                true, 
                                                message.appointmentDate, 
                                                message.appointmentTime, 
                                                "Turno en local",
                                                provider.displayName, 
                                                effectiveProviderPhoto as? String,
                                                message.categoryId ?: provider.categories.firstOrNull(),
                                                null
                                            ) 
                                        },
                                        onReject = { onRespondAppointment("preview", message.id, message.relatedId ?: "", false, null, null, "", "", null, null, null) }
                                    )
                                }
                            }
                            else -> {
                                var showViewer by remember { mutableStateOf(false) }
                                MessageBubble(
                                    message = message,
                                    appColors = appColors,
                                    isFromMe = message.senderId != provider.id,
                                    budget = budget,
                                    allCategories = allCategories, 
                                    onReply = { onReply(message) }, 
                                    onImageClick = { showViewer = true },
                                    onCalendarClick = { 
                                        if (message.type == MessageType.CALENDAR_INVITE) {
                                            onOpenBooking(message)
                                            showCalendarBooking = message
                                        }
                                        else onCalendarClick() 
                                    },
                                    onAddressClick = onAddressClick,
                                    isEnabled = if (message.type == MessageType.CALENDAR_INVITE) 
                                        !uiState.confirmedInviteIds.contains(message.id) else true
                                )
                                if (showViewer) ImageZoomDialog(message = message, onDismiss = { showViewer = false })
                            }

                        }
                    }
                }
            }

            // 🔥 [ELITE] Cabecera sobrepuesta
            TelegramStyleChatHeader(
                title = resolvedProviderName,
                photoUrl = effectiveProviderPhoto,
                isOnline = uiState.isProviderOnline,
                onBack = onBack,
                appColors = appColors,
                collapseFraction = collapseFraction,
                onSearchClick = onShowSearch
            )

            // --- BOTÓN SCROLL TO BOTTOM (Elite Style) ---
            AnimatedVisibility(
                visible = showScrollToBottom,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 16.dp, end = 12.dp)
            ) {
                Surface(
                    onClick = { coroutineScope.launch { listState.animateScrollToItem(0) } },
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = com.example.myapplication.presentation.registry.MaverickIcons.ChevronDown,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        if (showTenderSelectionDialog) {
            TenderSelectionDialog(
                matchingTenders = matchingTenders,
                providerCategories = provider.categories,
                appColors = appColors,
                onDismiss = { showTenderSelectionDialog = false },
                onSelect = { onTenderInvitation(it); showTenderSelectionDialog = false }
            )
        }
    }
}

@Composable
fun ChatConversationScreen(
    provider: Provider,
    viewModel: ChatViewModel,
    chatId: String, // 🔥 Recibir el chatId real
    onBack: () -> Unit,
    appColors: AppColors,
    onNavigateToCalendar: () -> Unit = {},
    onShowBudgets: () -> Unit = {}, // 🔥 [NUEVO]
    onShowCalendar: () -> Unit = {}, // 🔥 [NUEVO]
    onShowSearch: () -> Unit = {}, // 🔥 [NUEVO]
    onDeleteChat: () -> Unit = {}, // 🔥 [NUEVO]
    onBlockProvider: () -> Unit = {}, // 🔥 [NUEVO]
    onReportProvider: () -> Unit = {}, // 🔥 [NUEVO]
    onToggleFavorite: () -> Unit = {}, // 🔥 [NUEVO]
    isFavorite: Boolean = false, // 🔥 [NUEVO]
    appointmentViewModel: AppointmentViewModel = hiltViewModel(),
    budgetViewModel: BudgetViewModel = hiltViewModel(),
    beBrainViewModel: BeBrainViewModel = hiltViewModel(),
    ubicacionViewModel: UbicacionClimaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        appointmentViewModel.uiEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(uiState.messages) {
        if (uiState.messages.isNotEmpty()) {
            viewModel.markAsRead()
        }
    }

    val mainCategory = provider.categories.firstOrNull() ?: ""
    val matchingTenders by viewModel.getMatchingTenders(mainCategory).collectAsStateWithLifecycle(initialValue = emptyList())
    val availableAddresses by beBrainViewModel.availableAddressInfos.collectAsStateWithLifecycle()
    val allCategories by beBrainViewModel.allCategories.collectAsStateWithLifecycle()
    val recordingTime by viewModel.recordingTime.collectAsStateWithLifecycle() // 🔥 [NUEVO]

    ChatConversationContent(
        provider = provider,
        availableAddresses = availableAddresses,
        uiState = uiState,
        events = viewModel.events,
        onBack = onBack,
        appColors = appColors,
        onSendMessage = { viewModel.sendText(it) },
        onSendImage = { uri, ctx -> viewModel.sendImage(uri, ctx) }, // 🔥 Pasar context
        onSendLocation = { lat, lng, addr -> viewModel.sendLocation(lat, lng, addr) },
        onSendAppointment = { d, t, n, type, addr -> viewModel.sendAppointment(d, t, n, type, addr) },
        onAudioClick = { if (uiState.isRecording) viewModel.stopRecordingAndSend() else viewModel.startRecording(context) },
        onCancelAudio = { viewModel.cancelRecording() },
        onBudgetClick = { viewModel.onBudgetClicked(it) },
        onBudgetRequest = { problem, address, lat, lng -> viewModel.sendBudgetRequest(problem, address, lat, lng) },
        onTypingStatus = { viewModel.setTypingStatus(it) },
        onClearBudget = { viewModel.clearSelectedBudget() },
        onAcceptBudget = { budgetViewModel.acceptBudget(it) },
        onRejectBudget = { budgetViewModel.rejectBudget(it) },
        onRespondAppointment = { _, mid, aid, acc, d, t, tit, pn, purl, cname, cemoji ->
            // 🔥 Pasar el chatId real para la actualización en Firebase
            appointmentViewModel.respondToProviderAppointment(chatId, mid, aid, acc, d, t, tit, pn, purl, cname, cemoji)
        },
        onTenderInvitation = { viewModel.sendTenderInvitation(it) },
        onCalendarClick = onNavigateToCalendar,
        onAddressClick = { addr ->
            val uri = Uri.parse("geo:0,0?q=${Uri.encode(addr)}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        },
        matchingTenders = matchingTenders,
        ubicacionViewModel = ubicacionViewModel,
        allCategories = allCategories,
        onOpenBooking = { viewModel.openBookingDialog(it, availableAddresses) },
        onDaySelected = { day, booked -> viewModel.onDaySelected(day, booked) },
        onTimeSelected = { viewModel.onTimeSelected(it) },
        onAddressSelected = { viewModel.onAddressSelected(it) },
        onReply = { viewModel.setReplyMessage(it) },
        onSwitchContext = { bId -> viewModel.switchChatContext(bId) }, // 🔥 [FIX v6] Un solo argumento
        onShowBudgets = onShowBudgets,
        onShowCalendar = onShowCalendar,
        onShowSearch = onShowSearch,
        onDeleteChat = onDeleteChat,
        onBlockProvider = onBlockProvider,
        onReportProvider = onReportProvider,
        onToggleFavorite = onToggleFavorite,
        isFavorite = isFavorite,
        recordingTime = recordingTime // 🔥 [NUEVO]
    )
}

private fun isSameDayChat(t1: Long, t2: Long): Boolean {
    val fmt = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
    return fmt.format(java.util.Date(t1)) == fmt.format(java.util.Date(t2))
}

@Composable
fun LocationSelectionDialog(
    availableAddresses: List<AddressUnico>,
    appColors: AppColors,
    onDismiss: () -> Unit,
    onSelect: (Double, Double, String) -> Unit,
    ubicacionViewModel: UbicacionClimaViewModel?
) {
    val context = LocalContext.current
    var isFetchingGps by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enviar Ubicación", color = Color.White) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Selecciona una dirección guardada o usa tu ubicación GPS actual.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable(enabled = !isFetchingGps) {
                            isFetchingGps = true
                            ubicacionViewModel?.ejecutarCalculoUbicacionGps(context) { _, prov, loc, calle, num, _, lat, lng ->
                                val fullAddr = if (calle.isNotBlank()) "$calle $num, $loc, $prov" else "$loc, $prov"
                                onSelect(lat, lng, fullAddr)
                                isFetchingGps = false
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = appColors.accentBlue.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, appColors.accentBlue.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isFetchingGps) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Text("🎯", fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (isFetchingGps) "Obteniendo GPS..." else "Mi ubicación actual (GPS)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                if (availableAddresses.isNotEmpty()) {
                    Text("Direcciones Guardadas", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 250.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableAddresses) { addr ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        val full = "${addr.streetAndNumber}, ${addr.localidad}, ${addr.provincia}"
                                        onSelect(addr.latitude, addr.longitude, full) 
                                    },
                                colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(addr.streetAndNumber, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("${addr.localidad}, ${addr.provincia}", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
        },
        containerColor = Color(0xFF1A1A1A)
    )
}

@Composable
fun TenderSelectionDialog(
    matchingTenders: List<TenderEntity>,
    providerCategories: List<String>,
    appColors: AppColors,
    onDismiss: () -> Unit,
    onSelect: (TenderEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invitar a Licitación", color = Color.White) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Selecciona una de tus licitaciones abiertas para invitar a este prestador.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (matchingTenders.isEmpty()) {
                    Text("No tienes licitaciones abiertas para este rubro (${providerCategories.firstOrNull() ?: ""}).", color = Color.Red, fontSize = 14.sp)
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(matchingTenders) { tender ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onSelect(tender) },
                                colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(tender.title, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(tender.category, fontSize = 12.sp, color = appColors.accentBlue)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
        },
        containerColor = Color(0xFF1A1A1A)
    )
}

@Preview(showBackground = true)
@Composable
fun ChatConversationScreenPreview() {
    MyApplicationTheme {
        val sampleProvider = Provider(
            uid = "p1", 
            email = "juan@example.com",
            displayName = "Juan Maverick", 
            name = "Juan",
            lastName = "Maverick",
            phoneNumber = "123",
            categories = listOf("Técnico"),
            matricula = null,
            profesion = "Técnico",
            cuilCuit = "20-12345678-9",
            address = com.example.myapplication.core.domain.model.AddressUnico("Calle", "123"),
            works24h = false,
            photoUrl = null,
            profileThumbnail = null,
            isSubscribed = true,
            isVerified = true,
            isOnline = true,
            isFavorite = false,
            rating = 4.8f,
            createdAt = System.currentTimeMillis()
        )
        val sampleUiState = ChatUiState(
            messages = listOf(
                ChatMessageUiModel(MessageEntity(id="1", chatId="c1", senderId="me", receiverId="p1", type=MessageType.TEXT, content="Hola!", timestamp=System.currentTimeMillis()-50000)),
                ChatMessageUiModel(MessageEntity(id="2", chatId="c1", senderId="p1", receiverId="me", type=MessageType.TEXT, content="¿En qué puedo ayudarte?", timestamp=System.currentTimeMillis()-10000)),
                ChatMessageUiModel(MessageEntity(
                    id="3", chatId="c1", senderId="p1", receiverId="me", 
                    type=MessageType.VISIT, content="Propuesta de visita", 
                    appointmentDate="2024-05-10", appointmentTime="10:00",
                    appointmentType="TECHNICAL_VISIT", categoryId="Técnico",
                    timestamp=System.currentTimeMillis()-5000
                )),
                ChatMessageUiModel(MessageEntity(
                    id="4", chatId="c1", senderId="p1", receiverId="me", 
                    type=MessageType.VISIT, content="Propuesta de turno", 
                    appointmentDate="2024-05-11", appointmentTime="16:00",
                    appointmentType="LOCAL_APPOINTMENT", categoryId="Local",
                    timestamp=System.currentTimeMillis()-2000
                ))
            ),
            isProviderTyping = true
        )
        val sampleCategories = listOf(
            CategoryEntity(name="Técnico", icon="🛠️", superCategory="", isNew=false, isNewPrestador=false, isAd=false),
            CategoryEntity(name="Local", icon="🏪", superCategory="", isNew=false, isNewPrestador=false, isAd=false)
        )
        ChatConversationContent(
            provider = sampleProvider,
            uiState = sampleUiState,
            events = kotlinx.coroutines.flow.flowOf(),
            onBack = {},
            appColors = getThemeColors(),
            onSendMessage = {},
            onSendImage = { _, _ -> },
            onSendLocation = { _, _, _ -> },
            onSendAppointment = { _, _, _, _, _ -> },
            onAudioClick = {},
            onCancelAudio = {},
            onBudgetClick = {},
            onTypingStatus = {},
            onClearBudget = {},
            onAcceptBudget = {},
            onRejectBudget = {},
            onRespondAppointment = { _, _, _, _, _, _, _, _, _, _, _ -> },
            onTenderInvitation = {},
            allCategories = sampleCategories,
            onOpenBooking = {},
            onDaySelected = { _, _ -> },
            onTimeSelected = {},
            onAddressSelected = {}
        )
    }
}
