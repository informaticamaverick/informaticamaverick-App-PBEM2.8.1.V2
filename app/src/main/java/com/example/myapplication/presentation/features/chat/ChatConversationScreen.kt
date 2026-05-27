package com.example.myapplication.presentation.features.chat

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import com.example.myapplication.presentation.designsystem.theme.AppColors
import com.example.myapplication.core.domain.model.MessageType
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.components.BudgetRequestDialog 
import kotlinx.coroutines.flow.Flow
import com.example.myapplication.core.data.local.entity.BudgetEntity
import com.example.myapplication.core.data.local.entity.MessageEntity
import com.example.myapplication.core.data.local.entity.TenderEntity
import com.example.myapplication.core.data.local.entity.CategoryEntity
import androidx.compose.ui.tooling.preview.Preview
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
    availableAddresses: List<AddressInfo> = emptyList(),
    uiState: ChatUiState,
    events: Flow<ChatUiEvent>,
    onBack: () -> Unit,
    appColors: AppColors,
    onSendMessage: (String) -> Unit,
    onSendImage: (Uri) -> Unit,
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
    onAddressSelected: (AddressInfo) -> Unit,
    onReply: (MessageEntity?) -> Unit = {} 
) {
    // Resolver datos del Header usando el perfil base del prestador (Chat Unificado)
    val resolvedProviderName = provider.displayName
    val resolvedProviderPhoto = provider.photoUrl

    var inputText by remember { mutableStateOf("") }
    var showAttachMenu by remember { mutableStateOf(false) }
    var showTenderSelectionDialog by remember { mutableStateOf(false) }
    var showBudgetRequestDialog by remember { mutableStateOf(false) }
    var showLocationSelectionDialog by remember { mutableStateOf(false) }
    var showCalendarBooking by remember { mutableStateOf<MessageEntity?>(null) }
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Manejo de eventos del ViewModel
    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is ChatUiEvent.ShowError -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                ChatUiEvent.MessageSent -> { /* Scroll logic if needed */ }
            }
        }
    }

    // --- PERMISOS Y LAUNCHERS ---

    // VALORES PARA PREVIEW (Evitar FileProvider en modo diseño)
    val isInEditMode = androidx.compose.ui.platform.LocalInspectionMode.current

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { onSendImage(it); showAttachMenu = false }
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
            onSendImage(tempPhotoUri)
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

    val effectiveProviderPhoto = uiState.providerPhotoUrl ?: provider.photoUrl

    // --- VISORES ---
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
        modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
        topBar = {
            ChatHeader(
                providerName = resolvedProviderName,
                providerPhoto = resolvedProviderPhoto,
                isOnline = uiState.isProviderOnline, 
                onBack = onBack,
                appColors = appColors,
                isProviderTyping = uiState.isProviderTyping
            )
        },
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
                            onCancelReply = { onReply(null) }, 
                            appColors = appColors
                        )
                    }
                }

                AnimatedVisibility(visible = showAttachMenu) {
                    AttachmentOptionsMenu(
                        onDismiss = { showAttachMenu = false },
                        onImageClick = { galleryLauncher.launch("image/*"); showAttachMenu = false },
                        onLocationClick = { showLocationSelectionDialog = true; showAttachMenu = false },
                        onInviteClick = { showTenderSelectionDialog = true },
                        onBudgetRequestClick = { showBudgetRequestDialog = true; showAttachMenu = false }
                    )
                }

                MessageInputBar(
                    inputText = inputText,
                    onInputChange = { inputText = it; onTypingStatus(it.isNotEmpty()) },
                    onSendMessage = { onSendMessage(it); inputText = "" },
                    appColors = appColors,
                    onAttachMenuToggle = { showAttachMenu = !showAttachMenu },
                    onCameraClick = { handleCameraClick() },
                    onAudioClick = { handleAudioClick() },
                    onCancelAudio = { onCancelAudio() },
                    isRecordingAudio = uiState.isRecording
                )
            }
        },
        containerColor = appColors.backgroundColor
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val safePadding = remember(paddingValues, layoutDirection) {
            PaddingValues(
                start = paddingValues.calculateStartPadding(layoutDirection).coerceAtLeast(0.dp),
                top = paddingValues.calculateTopPadding().coerceAtLeast(0.dp),
                end = paddingValues.calculateEndPadding(layoutDirection).coerceAtLeast(0.dp),
                bottom = paddingValues.calculateBottomPadding().coerceAtLeast(0.dp)
            )
        }

        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = Modifier.fillMaxSize().padding(safePadding),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(uiState.messages.reversed()) { index, uiModel ->
                val message = uiModel.message
                val budget = uiModel.budget
                val reversedMessages = uiState.messages.reversed()
                if (index == reversedMessages.size - 1 || !isSameDayChat(message.timestamp, reversedMessages[index + 1].message.timestamp)) {
                    DateSeparator(timestamp = message.timestamp, appColors = appColors)
                }

                when (message.type) {
                    MessageType.BUDGET -> BudgetBubble(
                        message = message,
                        budget = budget,
                        isMe = message.senderId != provider.id,
                        appColors = appColors,
                        categoryEmoji = uiModel.categoryEmoji,
                        onClick = { message.relatedId?.let { onBudgetClick(it) } }
                    )
                    MessageType.AUDIO -> AudioMessageBubble(
                        message = message,
                        appColors = appColors,
                        isFromMe = message.senderId != provider.id
                    )
                    MessageType.VISIT -> {
                        val isTechnical = message.appointmentType == "TECHNICAL_VISIT"
                        if (isTechnical) {
                            TechnicalVisitProposalBubble(
                                message = message,
                                isMe = message.senderId != provider.id,
                                appColors = appColors,
                                providerPhotoUrl = if (message.senderId == provider.id) effectiveProviderPhoto else null,
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
                                        effectiveProviderPhoto,
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
                                providerPhotoUrl = if (message.senderId == provider.id) effectiveProviderPhoto else null,
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
                                        effectiveProviderPhoto,
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
    onBack: () -> Unit,
    appColors: AppColors,
    onNavigateToCalendar: () -> Unit = {},
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

    ChatConversationContent(
        provider = provider,
        availableAddresses = availableAddresses,
        uiState = uiState,
        events = viewModel.events,
        onBack = onBack,
        appColors = appColors,
        onSendMessage = { viewModel.sendText(it) },
        onSendImage = { viewModel.sendImage(it) },
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
        onRespondAppointment = { cid, mid, aid, acc, d, t, tit, pn, purl, cname, cemoji ->
            appointmentViewModel.respondToProviderAppointment(cid, mid, aid, acc, d, t, tit, pn, purl, cname, cemoji)
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
        onReply = { viewModel.setReplyMessage(it) }
    )
}

private fun isSameDayChat(t1: Long, t2: Long): Boolean {
    val fmt = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
    return fmt.format(java.util.Date(t1)) == fmt.format(java.util.Date(t2))
}

@Composable
fun LocationSelectionDialog(
    availableAddresses: List<AddressInfo>,
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
                                        val full = "${addr.streetAndNumber}, ${addr.locality}, ${addr.province}"
                                        onSelect(addr.lat, addr.lng, full) 
                                    },
                                colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(addr.streetAndNumber, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("${addr.locality}, ${addr.province}", fontSize = 12.sp, color = Color.Gray)
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
            titulo = null,
            cuilCuit = "20-12345678-9",
            address = com.example.myapplication.core.domain.model.AddressProvider("Calle", "123"),
            works24h = false,
            photoUrl = null,
            bannerImageUrl = null,
            hasCompanyProfile = false,
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
            onSendImage = {},
            onSendLocation = {_,_,_ ->},
            onSendAppointment = {_,_,_,_,_ ->},
            onAudioClick = {},
            onCancelAudio = {},
            onBudgetClick = {},
            onTypingStatus = {},
            onClearBudget = {},
            onAcceptBudget = {},
            onRejectBudget = {},
            onRespondAppointment = {_,_,_,_,_,_,_,_,_,_,_ ->},
            onTenderInvitation = {},
            allCategories = sampleCategories,
            onOpenBooking = {},
            onDaySelected = {_,_ ->},
            onTimeSelected = {},
            onAddressSelected = {}
        )
    }
}
