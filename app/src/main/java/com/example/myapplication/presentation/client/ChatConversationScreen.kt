package com.example.myapplication.presentation.client

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
import com.example.myapplication.data.model.Provider
import com.example.myapplication.ui.theme.AppColors
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.presentation.components.*
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
    availableAddresses: List<com.example.myapplication.presentation.components.AddressInfo> = emptyList(),
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
    onTenderInvitation: (com.example.myapplication.data.local.TenderEntity) -> Unit,
    onCalendarClick: () -> Unit = {},
    onAddressClick: (String) -> Unit = {},
    matchingTenders: List<com.example.myapplication.data.local.TenderEntity> = emptyList(),
    ubicacionViewModel: UbicacionClimaViewModel? = null
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
        com.example.myapplication.presentation.components.BookingDialog(
            message = msg,
            availableAddresses = availableAddresses,
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
                        onClick = { message.relatedId?.let { onBudgetClick(it) } }
                    )
                    MessageType.AUDIO -> AudioMessageBubble(
                        audioPath = if (message.content == "[Audio]" && message.imageUrl != null) message.imageUrl else message.content,
                        duration = message.durationSeconds ?: 0,
                        timestamp = message.timestamp,
                        appColors = appColors,
                        isFromMe = message.senderId != provider.id
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
                                null
                            ) 
                        },
                        onReject = { onRespondAppointment("preview", message.id, message.relatedId ?: "", false, null, null, "", "", null, null, null) }
                    )
                    else -> {
                        var showViewer by remember { mutableStateOf(false) }
                        MessageBubble(
                            message = message,
                            appColors = appColors,
                            isFromMe = message.senderId != provider.id,
                            budget = budget,
                            onImageClick = { showViewer = true },
                            onCalendarClick = { 
                                if (message.type == MessageType.CALENDAR_INVITE) showCalendarBooking = message
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
        availableAddresses = availableAddresses,
        uiState = uiState,
        events = viewModel.events,
        onBack = onBack,
        appColors = appColors,
        onSendMessage = { viewModel.sendText(it) },
        onSendImage = { viewModel.sendImage(it, context) },
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
        ubicacionViewModel = ubicacionViewModel
    )
}

// --- HELPERS ---
private fun isSameDayChat(t1: Long, t2: Long): Boolean {
    val fmt = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
    return fmt.format(java.util.Date(t1)) == fmt.format(java.util.Date(t2))
}

// --- PANTALLAS Y COMPONENTES ADICIONALES ---

@Composable
fun LocationSelectionDialog(
    availableAddresses: List<com.example.myapplication.presentation.components.AddressInfo>,
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

                // Opción GPS
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
            onTenderInvitation = {}
        )
    }
}
