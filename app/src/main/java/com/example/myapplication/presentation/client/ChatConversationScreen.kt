package com.example.myapplication.presentation.client

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RequestQuote
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
import com.example.myapplication.data.model.Provider
import com.example.myapplication.ui.theme.AppColors
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.presentation.components.*
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import com.example.myapplication.data.local.BudgetEntity
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.getThemeColors
import com.example.myapplication.data.local.MessageEntity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * PANTALLA DE CONVERSACIÓN DE CHAT (UI PURA)
 * Sigue la Regla de Oro: Pantalla tonta que solo recibe datos y eventos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationContent(
    provider: Provider,
    companyId: String? = null,
    availableAddresses: List<com.example.myapplication.presentation.components.AddressInfo> = emptyList(),
    uiState: ChatUiState,
    events: Flow<ChatUiEvent>,
    onBack: () -> Unit,
    appColors: AppColors,
    onSendMessage: (String) -> Unit,
    onSendImage: (Uri) -> Unit,
    onSendLocation: (Double, Double) -> Unit,
    onSendAppointment: (String, String, String, String?, String?, String) -> Unit,
    onAudioClick: () -> Unit,
    onCancelAudio: () -> Unit,
    onBudgetClick: (String) -> Unit,
    onTypingStatus: (Boolean) -> Unit,
    onClearBudget: () -> Unit,
    onAcceptBudget: (BudgetEntity) -> Unit,
    onRejectBudget: (BudgetEntity) -> Unit,
    onRespondAppointment: (String, String, String, Boolean, String?, String?, String, String, String?, String?, String?) -> Unit,
    onTenderInvitation: (com.example.myapplication.data.local.TenderEntity) -> Unit,
    matchingTenders: List<com.example.myapplication.data.local.TenderEntity> = emptyList(),
    onSendBudgetRequest: (String, String) -> Unit = { _, _ -> }
) {
    // --- POLÍTICA ZERO COST: Cargar estado online desde Firebase ---
    val isProviderOnline by produceState(initialValue = false, provider.uid) {
        val ref = FirebaseDatabase.getInstance().reference
            .child("users").child(provider.uid).child("online")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                value = snapshot.getValue(Boolean::class.java) ?: false
            }
            override fun onCancelled(error: DatabaseError) { value = false }
        }
        ref.addValueEventListener(listener)
        awaitDispose { ref.removeEventListener(listener) }
    }

    // Resolver nombre de la empresa para el Header
    val resolvedProviderName = companyId?.let { cid -> 
        provider.companies.find { it.id == cid }?.name 
    } ?: provider.displayName

    val resolvedProviderPhoto = companyId?.let { cid ->
        provider.companies.find { it.id == cid }?.photoUrl
    } ?: provider.photoUrl

    var inputText by remember { mutableStateOf("") }
    var showAttachMenu by remember { mutableStateOf(false) }
    var showTenderSelectionDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showCalendarBooking by remember { mutableStateOf<MessageEntity?>(null) }
    var showBudgetRequestDialog by remember { mutableStateOf(false) }
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

    // Auto-detener grabación después de 60 segundos
    LaunchedEffect(uiState.isRecording) {
        if (uiState.isRecording) {
            var recordingSeconds = 0
            while (uiState.isRecording) {
                delay(1000)
                recordingSeconds++
                if (recordingSeconds >= 60) {
                    onAudioClick()
                    break
                }
            }
        }
    }

    // --- PERMISOS Y LAUNCHERS ---

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val fusedLocation = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocation.lastLocation.addOnSuccessListener { location ->
                    if (location != null) onSendLocation(location.latitude, location.longitude)
                }
            } catch (e: SecurityException) { }
        }
    }

    fun sendRealLocation() {
        showAttachMenu = false
        val fusedLocation = LocationServices.getFusedLocationProviderClient(context)
        val hasFine = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (hasFine) {
            fusedLocation.lastLocation.addOnSuccessListener { location ->
                if (location != null) onSendLocation(location.latitude, location.longitude)
            }
        } else {
            locationPermissionLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { onSendImage(it); showAttachMenu = false }
    }

    // --- CÁMARA LAUNCHER Y PERMISOS (REGLA DE ORO: Captura a resolución completa) ---
    // Creamos un Uri temporal para que la cámara guarde la imagen a resolución completa
    val tempPhotoUri = remember {
        val photoFile = java.io.File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(context, "com.example.myapplication.provider", photoFile)
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            // Pasamos el Uri de la foto a resolución completa al ViewModel para su procesamiento
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
    uiState.selectedBudget?.let { budget ->
        Dialog(
            onDismissRequest = { onClearBudget() },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                BudgetMultiPageScreen(
                    budget = budget,
                    onAccept = { onAcceptBudget(budget); onClearBudget() },
                    onReject = { onRejectBudget(budget); onClearBudget() },
                    onBack = { onClearBudget() }
                )
            }
        }
    }

    if (showScheduleDialog) {
        ScheduleAppointmentDialog(
            onDismiss = { showScheduleDialog = false },
            onConfirm = { date, time, notes ->
                showScheduleDialog = false
                onSendAppointment(date, time, notes, null, null, "")
            }
        )
    }

    showCalendarBooking?.let { msg ->
        com.example.myapplication.presentation.components.BookingDialog(
            message = msg,
            availableAddresses = availableAddresses,
            onDismissRequest = { showCalendarBooking = null },
            onAcceptRequest = { dateStr, timeStr, address, label, originalMsgId ->
                showCalendarBooking = null
                onSendAppointment(
                    dateStr,
                    timeStr,
                    if (label != null) "Cita en: $label ($address)" else "Turno en local del prestador",
                    msg.appointmentType,
                    address,
                    originalMsgId
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
                isOnline = isProviderOnline,
                onBack = onBack,
                appColors = appColors,
                isProviderTyping = uiState.isProviderTyping
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(appColors.backgroundColor)) {
                AnimatedVisibility(visible = showAttachMenu) {
                    AttachmentOptionsMenu(
                        onDismiss = { showAttachMenu = false },
                        onImageClick = { galleryLauncher.launch("image/*"); showAttachMenu = false },
                        onLocationClick = { sendRealLocation() },
                        onScheduleClick = { showScheduleDialog = true; showAttachMenu = false },
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
                    onAudioClick = { onAudioClick() },
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        onClick = { message.relatedId?.let { onBudgetClick(it) } }
                    )
                    MessageType.AUDIO -> AudioMessageBubble(
                        audioPath = if (message.content == "[Audio]" && message.imageUrl != null) message.imageUrl else message.content,
                        duration = message.durationSeconds ?: 0,
                        timestamp = message.timestamp,
                        appColors = appColors,
                        isFromMe = message.senderId != provider.id
                    )
                    MessageType.IMAGE -> {
                        var showViewer by remember { mutableStateOf(false) }
                        MessageBubble(
                            message = message, 
                            appColors = appColors, 
                            isFromMe = message.senderId != provider.id, 
                            budget = budget,
                            onImageClick = { showViewer = true },
                            onCalendarClick = { showCalendarBooking = message }
                        )
                        if (showViewer) ImageZoomDialog(message = message, onDismiss = { showViewer = false })
                    }
                    MessageType.LOCATION -> MessageBubble(
                        message = message, 
                        appColors = appColors, 
                        isFromMe = message.senderId != provider.id,
                        budget = budget,
                        onCalendarClick = { showCalendarBooking = message }
                    )
                    MessageType.VISIT -> AppointmentBubble(
                        message = message,
                        isMe = message.senderId != provider.id,
                        appColors = appColors,
                        providerPhotoUrl = if (message.senderId == provider.id) effectiveProviderPhoto else null,
                        onAccept = { 
                            onRespondAppointment(
                                "preview", 
                                message.id, 
                                message.relatedId ?: "", 
                                true, 
                                message.appointmentDate, 
                                message.appointmentTime, 
                                "Cita", 
                                provider.displayName, 
                                effectiveProviderPhoto,
                                message.categoryId ?: provider.categories.firstOrNull(),
                                null // Emoji se podría inferir en el ViewModel o pasar si está en el mensaje
                            ) 
                        },
                        onReject = { onRespondAppointment("preview", message.id, message.relatedId ?: "", false, null, null, "", "", null, null, null) }
                    )
                    MessageType.CALENDAR_INVITE -> CalendarInviteBubble(
                        message = message,
                        isMe = message.senderId != provider.id,
                        appColors = appColors,
                        onClick = { showCalendarBooking = message }
                    )
                    MessageType.APPOINTMENT_RECEIPT -> AppointmentReceiptBubble(
                        message = message,
                        isMe = message.senderId != provider.id,
                        appColors = appColors
                    )
                    MessageType.BUDGET_REQUEST -> BudgetRequestSentBubble(
                        message = message,
                        isMe = message.senderId != provider.id,
                        appColors = appColors
                    )
                    else -> EnhancedMessageBubble(message = message, isMe = message.senderId != provider.id, appColors = appColors, senderPhotoUrl = if (message.senderId == provider.id) effectiveProviderPhoto else null)
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

        if (showBudgetRequestDialog) {
            BudgetRequestClientDialog(
                availableAddresses = availableAddresses,
                onDismiss = { showBudgetRequestDialog = false },
                onSend = { desc, addr ->
                    onSendBudgetRequest(desc, addr)
                    showBudgetRequestDialog = false
                }
            )
        }
    }
}

@Composable
fun ChatConversationScreen(
    provider: Provider,
    companyId: String? = null,
    categoryId: String? = null,
    viewModel: ChatViewModel,
    onBack: () -> Unit,
    appColors: AppColors,
    appointmentViewModel: AppointmentViewModel = hiltViewModel(),
    budgetViewModel: BudgetViewModel = hiltViewModel(),
    beBrainViewModel: BeBrainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        // [REGLA DE ORO] Sincronizar contexto del Asistente Be
        beBrainViewModel.onRouteChanged("chat_conversation")
    }

    // 🔥 ESCUCHA DE EVENTOS DE CITAS (TOASTS)
    LaunchedEffect(Unit) {
        appointmentViewModel.uiEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    // [POLÍTICA ZERO COST] Marcar como leído al entrar y cuando cambien los mensajes
    LaunchedEffect(uiState.messages) {
        if (uiState.messages.isNotEmpty()) {
            viewModel.markAsRead()
        }
    }

    val mainCategory = provider.categories.firstOrNull() ?: ""
    val matchingTenders by viewModel.getMatchingTenders(mainCategory).collectAsStateWithLifecycle(initialValue = emptyList())
    val availableAddresses by beBrainViewModel.availableAddressInfos.collectAsStateWithLifecycle()

    ChatConversationContent(
        provider = provider,
        companyId = companyId,
        availableAddresses = availableAddresses,
        uiState = uiState,
        events = viewModel.events,
        onBack = onBack,
        appColors = appColors,
        onSendMessage = { viewModel.sendText(it) },
        onSendImage = { viewModel.sendImage(it, context) },
        onSendLocation = { lat, lng -> viewModel.sendLocation(lat, lng) },
        onSendAppointment = { d, t, n, type, addr, msgId -> viewModel.sendAppointment(d, t, n, type, addr, msgId) },
        onAudioClick = { if (uiState.isRecording) viewModel.stopRecordingAndSend() else viewModel.startRecording(context) },
        onCancelAudio = { viewModel.cancelRecording() },
        onBudgetClick = { viewModel.onBudgetClicked(it) },
        onTypingStatus = { viewModel.setTypingStatus(it) },
        onClearBudget = { viewModel.clearSelectedBudget() },
        onAcceptBudget = { budgetViewModel.acceptBudget(it) },
        onRejectBudget = { budgetViewModel.rejectBudget(it) },
        onRespondAppointment = { cid, mid, aid, acc, d, t, tit, pn, purl, cname, cemoji ->
            appointmentViewModel.respondToProviderAppointment(cid, mid, aid, acc, d, t, tit, pn, purl, cname, cemoji)
        },
        onTenderInvitation = { viewModel.sendTenderInvitation(it) },
        matchingTenders = matchingTenders,
        onSendBudgetRequest = { desc, addr -> viewModel.sendBudgetRequest(desc, addr) }
    )
}

// --- HELPERS ---
private fun isSameDayChat(t1: Long, t2: Long): Boolean {
    val fmt = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
    return fmt.format(java.util.Date(t1)) == fmt.format(java.util.Date(t2))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetRequestClientDialog(
    availableAddresses: List<com.example.myapplication.presentation.components.AddressInfo>,
    onDismiss: () -> Unit,
    onSend: (description: String, address: String) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var selectedAddress by remember { mutableStateOf(availableAddresses.firstOrNull()?.let { "${it.streetAndNumber}, ${it.locality}" } ?: "") }
    var addressExpanded by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            color = Color(0xFF1E293B),
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RequestQuote,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Solicitar Presupuesto",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("¿Qué necesitás?", color = Color(0xFF94A3B8)) },
                    placeholder = { Text("Ej: Pintura interior 3 ambientes", color = Color(0xFF64748B)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF059669),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Dirección del trabajo", fontSize = 13.sp, color = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(4.dp))

                if (availableAddresses.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = addressExpanded,
                        onExpandedChange = { addressExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedAddress,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = addressExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF059669),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = addressExpanded,
                            onDismissRequest = { addressExpanded = false }
                        ) {
                            availableAddresses.forEach { addr ->
                                DropdownMenuItem(
                                    text = { Text("${addr.streetAndNumber}, ${addr.locality}") },
                                    onClick = {
                                        selectedAddress = "${addr.streetAndNumber}, ${addr.locality}"
                                        addressExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = selectedAddress,
                        onValueChange = { selectedAddress = it },
                        placeholder = { Text("Ej: Av. Corrientes 1234, CABA", color = Color(0xFF64748B)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF059669),
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color(0xFF475569))
                    ) {
                        Text("Cancelar", color = Color(0xFF94A3B8))
                    }
                    Button(
                        onClick = { if (description.isNotBlank()) onSend(description, selectedAddress) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                    ) {
                        Text("Enviar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetRequestSentBubble(
    message: MessageEntity,
    isMe: Boolean,
    appColors: AppColors
) {
    val align = if (isMe) Alignment.End else Alignment.Start
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            color = Color(0xFF064E3B),
            border = BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.6f)),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RequestQuote,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Solicitud de presupuesto",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF34D399)
                    )
                }
                if (!message.budgetRequestDescription.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = message.budgetRequestDescription,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
                if (!message.budgetRequestClientAddress.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF6EE7B7),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = message.budgetRequestClientAddress,
                            fontSize = 12.sp,
                            color = Color(0xFF6EE7B7)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(message.timestamp)),
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}


@Composable
fun TenderSelectionDialog(
    matchingTenders: List<com.example.myapplication.data.local.TenderEntity>,
    providerCategories: List<String>,
    appColors: AppColors,
    onDismiss: () -> Unit,
    onSelect: (com.example.myapplication.data.local.TenderEntity) -> Unit
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

// --- PREVIEW ---
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
            address = com.example.myapplication.data.model.AddressProvider("Calle", "123"),
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
                ChatMessageUiModel(MessageEntity(id="2", chatId="c1", senderId="p1", receiverId="me", type=MessageType.TEXT, content="¿En qué puedo ayudarte?", timestamp=System.currentTimeMillis()-10000))
            ),
            isProviderTyping = true
        )
        ChatConversationContent(
            provider = sampleProvider,
            uiState = sampleUiState,
            events = kotlinx.coroutines.flow.flowOf(),
            onBack = {},
            appColors = getThemeColors(),
            onSendMessage = {},
            onSendImage = {},
            onSendLocation = {_,_ ->},
            onSendAppointment = {_,_,_,_,_,_ ->},
            onAudioClick = {},
            onCancelAudio = {},
            onBudgetClick = {},
            onTypingStatus = {},
            onClearBudget = {},
            onAcceptBudget = {},
            onRejectBudget = {},
            onRespondAppointment = {_,_,_,_,_,_,_,_,_,_,_ ->},
            onTenderInvitation = {}
        )
    }
}
