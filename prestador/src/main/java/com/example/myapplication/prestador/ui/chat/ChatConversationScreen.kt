package com.example.myapplication.prestador.ui.chat

import android.Manifest
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.example.myapplication.core.data.local.entity.MessageEntity
import com.example.myapplication.core.domain.model.MessageType
import com.example.myapplication.core.domain.model.Provider
import com.example.myapplication.prestador.viewmodel.chat.ChatUiState
import com.example.myapplication.prestador.viewmodel.chat.ChatViewModel
import com.example.myapplication.prestador.viewmodel.chat.InboxType
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.data.model.*
import java.io.File
import com.example.myapplication.prestador.ui.presupuesto.BudgetItem
import com.example.myapplication.prestador.ui.presupuesto.BudgetService
import com.example.myapplication.prestador.ui.presupuesto.BudgetProfessionalFee
import com.example.myapplication.prestador.ui.presupuesto.BudgetMiscExpense
import com.example.myapplication.prestador.ui.presupuesto.BudgetTax

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationContent(
    userName: String,
    userPhotoUrl: String? = null,
    provider: Provider? = null,
    uiState: ChatUiState,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onSendImage: (Uri) -> Unit,
    onSendLocation: (Double, Double, String) -> Unit,
    onAudioClick: () -> Unit,
    onCancelAudio: () -> Unit,
    onTypingStatus: (Boolean) -> Unit,
    onReply: (MessageEntity) -> Unit,
    onCancelReply: () -> Unit,
    onSwitchContext: (InboxType, String?) -> Unit,
    onNavigateToClientePerfil: () -> Unit,
    onAcceptAppointment: (MessageEntity, String) -> Unit,
    onRejectAppointment: (MessageEntity, String?) -> Unit,
    onSendCalendarInvite: (String, String, String, String, String, String?, String) -> Unit,
    isClientOnline: Boolean = false,
    recordingTime: Int = 0,
    autoOpenCalendarDialog: Boolean = false
) {
    val context = LocalContext.current
    val colors = getPrestadorColors()
    val pagingMessages = uiState.pagingMessages.collectAsLazyPagingItems()
    val listState = rememberLazyListState()

    var messageText by remember { mutableStateOf("") }
    var showAttachMenu by remember { mutableStateOf(false) }
    var showBudgetSheet by remember { mutableStateOf(false) }
    var showSendCalendarDialog by remember { mutableStateOf(autoOpenCalendarDialog) }
    var pendingAppointmentType by remember { mutableStateOf("TECHNICAL_VISIT") }

    var presupuestoMsgToView by remember { mutableStateOf<MessageEntity?>(null) }
    var budgetRequestMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var zoomedImageUrl by remember { mutableStateOf<String?>(null) }

    // --- Media Launchers ---
    val tempPhotoUri = remember {
        val file = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            Log.d("ChatConversation", "📷 [IMAGE_CAPTURE_SUCCESS] Enviando URI temporal")
            onSendImage(tempPhotoUri)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { 
            Log.d("ChatConversation", "🖼️ [GALLERY_PICK_SUCCESS] URI: $it")
            onSendImage(it) 
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) cameraLauncher.launch(tempPhotoUri)
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) onAudioClick()
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            Log.d("ChatConversation", "📍 [LOCATION_PERM_GRANTED]")
            onSendLocation(-34.6037, -58.3816, "Buenos Aires, Argentina")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigateToClientePerfil() }
                    ) {
                        Box {
                            AsyncImage(
                                model = userPhotoUrl,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            if (isClientOnline) {
                                Box(
                                    modifier = Modifier.size(12.dp).background(Color.Green, CircleShape)
                                        .border(2.dp, colors.backgroundColor, CircleShape)
                                        .align(Alignment.BottomEnd)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(userName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            if (isClientOnline) {
                                Text("En línea", fontSize = 12.sp, color = Color.Green)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.Chat, "Volver", tint = colors.primaryOrange)
                    }
                },
                actions = {
                    if ((provider?.companies?.size ?: 0) > 0) {
                        Row(modifier = Modifier.padding(end = 8.dp)) {
                            provider?.companies?.forEach { company ->
                                EliteMiniProfileBubble(
                                    // 🔥 [ELITE v10.0] SSOT: Usamos thumbnailBase64 si existe
                                    photoUrl = company.thumbnailBase64 ?: company.photoUrl,
                                    isSelected = uiState.activeCompanyId == company.id,
                                    onClick = { onSwitchContext(InboxType.EMPRESA, company.id) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            EliteMiniProfileBubble(
                                // 🔥 [ELITE v10.0] SSOT: Perfil usa profileThumbnail
                                photoUrl = provider?.profileThumbnail ?: provider?.photoUrl,
                                isSelected = uiState.activeCompanyId == null,
                                onClick = { onSwitchContext(InboxType.PERSONAL, null) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.backgroundColor)
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(colors.backgroundColor).navigationBarsPadding()) {
                AnimatedVisibility(visible = uiState.pendingReply != null) {
                    uiState.pendingReply?.let { msg ->
                        Row(
                            modifier = Modifier.fillMaxWidth().background(colors.surfaceColor).padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Respondiendo a", fontSize = 12.sp, color = colors.primaryOrange)
                                Text(msg.content ?: "", maxLines = 1, fontSize = 14.sp, color = colors.textSecondary)
                            }
                            IconButton(onClick = onCancelReply) { Icon(Icons.AutoMirrored.Filled.Chat, "Cerrar") }
                        }
                    }
                }

                AnimatedVisibility(visible = showAttachMenu) {
                    AttachmentOptionsMenu(
                        serviceType = com.example.myapplication.prestador.data.model.ServiceType.fromString(provider?.serviceType),
                        onDismiss = { showAttachMenu = false },
                        onImageClick = { galleryLauncher.launch("image/*"); showAttachMenu = false },
                        onCameraClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (hasPermission) cameraLauncher.launch(tempPhotoUri)
                            else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            showAttachMenu = false
                        },
                        onLocationClick = {
                            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                            showAttachMenu = false
                        },
                        onDocumentClick = { showBudgetSheet = true; showAttachMenu = false },
                        onScheduleVisitClick = { pendingAppointmentType = "TECHNICAL_VISIT"; showSendCalendarDialog = true; showAttachMenu = false },
                        onScheduleLocalClick = { pendingAppointmentType = "LOCAL_APPOINTMENT"; showSendCalendarDialog = true; showAttachMenu = false }
                    )
                }

                MessageInputBar(
                    messageText = messageText,
                    onMessageTextChange = { 
                        messageText = it
                        onTypingStatus(it.isNotEmpty()) 
                    },
                    onSendMessage = { 
                        if (messageText.isNotBlank()) {
                            Log.d("ChatConversation", "✉️ [SEND_MESSAGE] Content size: ${messageText.length}")
                            onSendMessage(messageText)
                            messageText = ""
                        }
                    },
                    onAttachClick = { showAttachMenu = !showAttachMenu },
                    onCameraClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        if (hasPermission) cameraLauncher.launch(tempPhotoUri)
                        else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onMicClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            Log.d("ChatConversation", "🎤 [VOICE_RECORD_START]")
                            onAudioClick()
                        } else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onCancelAudio = onCancelAudio,
                    isRecording = uiState.isRecording,
                    recordingTime = recordingTime
                )
            }
        },
        containerColor = colors.backgroundColor
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // [ELITE v10.0]: Fondo normal M3 (blanco/superficie)
            Spacer(Modifier.fillMaxSize().background(colors.backgroundColor))

            LazyColumn(
                state = listState,
                reverseLayout = true,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    count = pagingMessages.itemCount,
                    key = pagingMessages.itemKey { it.id }
                ) { index ->
                    val message = pagingMessages[index] ?: return@items
                    SwipeToReply(onReply = { onReply(message) }) {
                        val isFromCurrentUser = message.senderId == com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
                        ) {
                                MessageBubble(
                                    message = mapEntityToUiMessage(message),
                                    isFromCurrentUser = isFromCurrentUser,
                                    senderAvatarUrl = if (!isFromCurrentUser) userPhotoUrl else null,
                                    clientName = userName,
                                    onVerPresupuesto = if (message.type == MessageType.BUDGET) {
                                        { presupuestoMsgToView = message }
                                    } else null,
                                    onCreateBudgetFromRequest = if (message.type == MessageType.BUDGET_REQUEST) {
                                        { budgetRequestMessage = message; showBudgetSheet = true }
                                    } else null,
                                    onImageClick = { zoomedImageUrl = it },
                                    onAccept = if (message.type == MessageType.VISIT) {
                                        { serviceTitle -> onAcceptAppointment(message, serviceTitle) }
                                    } else null,
                                    onReject = if (message.type == MessageType.VISIT) {
                                        { _, reason -> onRejectAppointment(message, reason) }
                                    } else null
                                )
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS ---
    if (zoomedImageUrl != null) {
        ImageZoomDialog(imageUrl = zoomedImageUrl!!, onDismiss = { zoomedImageUrl = null })
    }

    if (showBudgetSheet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        com.example.myapplication.prestador.ui.presupuesto.BudgetChatSheet(
            userId = uiState.clientProfile?.uid ?: "",
            userName = userName,
            providerId = provider?.uid ?: "",
            initialClientAddress = budgetRequestMessage?.budgetRequestClientAddress,
            onDismiss = { showBudgetSheet = false; budgetRequestMessage = null }
        )
    }

    if (showSendCalendarDialog) {
        SendCalendarDialog(
            providerId = provider?.id ?: "",
            onDismiss = { showSendCalendarDialog = false },
            hasPhysicalLocation = provider?.hasPhysicalLocation ?: false,
            tieneEmpresa = provider?.companies?.isNotEmpty() ?: false,
            companyId = uiState.activeCompanyId ?: "",
            initialAppointmentType = pendingAppointmentType,
            onSend = { startDate, endDate, availabilityJson, bookedSlotsJson, appointmentType, providerAddress, serviceCategory ->
                Log.d("ChatConversation", "📅 [SEND_CALENDAR_INVITE] Type: $appointmentType")
                onSendCalendarInvite(startDate, endDate, availabilityJson, bookedSlotsJson, appointmentType, providerAddress, serviceCategory)
                showSendCalendarDialog = false
            }
        )
    }

    presupuestoMsgToView?.let { msg ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && provider != null) {
            Log.d("ChatConversation", "💰 [VIEW_BUDGET] Id: ${msg.id}")
            val budgetEntity = com.example.myapplication.core.data.remote.ChatMessageMapper.parseBudgetFromJson(
                jsonString = msg.budgetDataJson ?: "{}",
                budgetId = msg.relatedId ?: msg.id,
                providerId = provider.uid,
                clientId = uiState.clientProfile?.uid ?: ""
            )

            if (budgetEntity != null) {
                com.example.myapplication.prestador.ui.presupuesto.BudgetPreviewPDFDialog(
                    prestador = provider,
                    items = budgetEntity.items.map { it.toBudgetItem() },
                    services = budgetEntity.services.map { it.toBudgetService() },
                    professionalFees = budgetEntity.professionalFees.map { it.toBudgetFee() },
                    miscExpenses = budgetEntity.miscExpenses.map { it.toBudgetMisc() },
                    taxes = budgetEntity.taxes.map { it.toBudgetTax() },
                    grandTotal = budgetEntity.grandTotal,
                    subtotal = budgetEntity.subtotal,
                    taxAmount = budgetEntity.taxAmount,
                    discountAmount = 0.0,
                    presupuestoNumero = budgetEntity.notes?.substringAfter("Presupuesto Nro: ", "")?.substringBefore("\n") ?: "",
                    clientName = userName,
                    onDismiss = { presupuestoMsgToView = null },
                    onEnviar = { presupuestoMsgToView = null }
                )
            } else {
                presupuestoMsgToView = null
            }
        }
    }
}

/**
 * Extensiones de mapeo para compatibilidad UI con el modelo BudgetEntity de Core.
 */
private fun com.example.myapplication.core.data.local.entity.BudgetItem.toBudgetItem() = BudgetItem(0L, code, description, unitPrice, quantity, taxPercentage, discountPercentage)
private fun com.example.myapplication.core.data.local.entity.BudgetService.toBudgetService() = BudgetService(0L, code, description, total)
private fun com.example.myapplication.core.data.local.entity.BudgetProfessionalFee.toBudgetFee() = BudgetProfessionalFee(0L, code, description, total)
private fun com.example.myapplication.core.data.local.entity.BudgetMiscExpense.toBudgetMisc() = BudgetMiscExpense(0L, description, amount)
private fun com.example.myapplication.core.data.local.entity.BudgetTax.toBudgetTax() = BudgetTax(0L, description, amount)

@Composable
fun ChatConversationScreen(
    userId: String,
    userName: String,
    userPhotoUrl: String? = null,
    providerId: String,
    conversationId: String? = null,
    branchId: String? = null,
    onBack: () -> Unit,
    onNavigateToClientePerfil: () -> Unit = {},
    autoOpenCalendarDialog: Boolean = false,
    rescheduleDate: String = "",
    rescheduleTime: String = ""
) {
    val viewModel: ChatViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val providerProfile by viewModel.providerProfile.collectAsStateWithLifecycle()

    val chatId = remember(userId, providerId, conversationId, uiState.activeCompanyId, branchId) {
        if (conversationId != null && conversationId.isNotBlank() && !conversationId.contains("{")) {
            conversationId 
        } else {
            com.example.myapplication.core.ChatIdHelper.generateChatId(
                uid1 = userId,
                uid2 = providerId,
                b1 = null, 
                b2 = branchId ?: uiState.activeBranchId
            )
        }
    }

    val contextResolved = remember(chatId, providerId) {
        val (_, bRemote, _) = com.example.myapplication.core.ChatIdHelper.extractOtherContext(chatId, providerId)
        val (_, bLocal, _) = com.example.myapplication.core.ChatIdHelper.extractMyContext(chatId, providerId)
        bLocal to bRemote
    }

    LaunchedEffect(chatId) {
        Log.d("ChatConversation", "🚀 [CONVERSATION_INIT] ChatId: $chatId | LocalB: ${contextResolved.first} | RemoteB: ${contextResolved.second}")
        viewModel.loadConversation(
            chatId = chatId, 
            companyId = uiState.activeCompanyId, 
            branchId = contextResolved.first, 
            clientBranchId = contextResolved.second
        )
        viewModel.markAsRead()
    }

    LaunchedEffect(autoOpenCalendarDialog) {
        if (autoOpenCalendarDialog) {
            if (rescheduleDate.isNotBlank() && rescheduleTime.isNotBlank()) {
                viewModel.sendRescheduleNotice(chatId, rescheduleDate, rescheduleTime)
            }
        }
    }

    ChatConversationContent(
        userName = userName,
        userPhotoUrl = userPhotoUrl,
        provider = providerProfile,
        uiState = uiState,
        onBack = onBack,
        onSendMessage = { viewModel.sendMessage(it) },
        onSendImage = { viewModel.sendImage(it) },
        onSendLocation = { lat, lng, addr -> viewModel.sendLocation(lat, lng, addr) },
        onAudioClick = { if (uiState.isRecording) viewModel.stopRecordingAndSend() else viewModel.startRecording() },
        onCancelAudio = { viewModel.cancelRecording() },
        onTypingStatus = { viewModel.setTypingStatus(it) },
        onReply = { viewModel.setReply(it) },
        onCancelReply = { viewModel.clearReply() },
        onSwitchContext = { type, companyId -> 
            Log.d("ChatConversation", "🔄 [SWITCH_CONTEXT] Type: $type | CompanyId: $companyId")
            viewModel.selectInbox(type, companyId) 
        },
        onNavigateToClientePerfil = onNavigateToClientePerfil,
        onAcceptAppointment = { message, service ->
            viewModel.respondToAppointmentRequest(
                messageId = message.id,
                clientName = userName,
                date = message.appointmentDate ?: "",
                time = message.appointmentTime ?: "",
                service = service,
                accepted = true
            )
        },
        onRejectAppointment = { message, reason ->
            viewModel.respondToAppointmentRequest(
                messageId = message.id,
                clientName = userName,
                date = message.appointmentDate ?: "",
                time = message.appointmentTime ?: "",
                accepted = false,
                rejectionReason = reason
            )
        },
        onSendCalendarInvite = { d1, d2, avail, booked, type, addr, cat ->
            viewModel.sendCalendarInvite(d1, d2, avail, booked, type, addr, cat)
        },
        isClientOnline = uiState.isClientOnline,
        recordingTime = uiState.recordingTime,
        autoOpenCalendarDialog = autoOpenCalendarDialog
    )
}

@Composable
fun EliteMiniProfileBubble(
    photoUrl: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (isSelected) 1.2f else 1f, label = "scale")
    Box(
        modifier = Modifier
            .size(40.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFFF97316) else Color.Gray.copy(alpha = 0.5f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

private fun mapEntityToUiMessage(entity: MessageEntity): com.example.myapplication.prestador.data.model.Message {
    val type = when (entity.type) {
        MessageType.IMAGE -> com.example.myapplication.prestador.data.model.Message.MessageType.IMAGE
        MessageType.AUDIO -> com.example.myapplication.prestador.data.model.Message.MessageType.AUDIO
        MessageType.LOCATION -> com.example.myapplication.prestador.data.model.Message.MessageType.LOCATION
        MessageType.VISIT -> com.example.myapplication.prestador.data.model.Message.MessageType.VISIT
        MessageType.BUDGET -> com.example.myapplication.prestador.data.model.Message.MessageType.BUDGET
        MessageType.CALENDAR_INVITE -> com.example.myapplication.prestador.data.model.Message.MessageType.CALENDAR_INVITE
        MessageType.APPOINTMENT_RECEIPT -> com.example.myapplication.prestador.data.model.Message.MessageType.APPOINTMENT_RECEIPT
        MessageType.BUDGET_REQUEST -> com.example.myapplication.prestador.data.model.Message.MessageType.BUDGET_REQUEST
        else -> com.example.myapplication.prestador.data.model.Message.MessageType.TEXT
    }
    
    val budgetObj = if (type == com.example.myapplication.prestador.data.model.Message.MessageType.BUDGET && entity.budgetDataJson != null) {
        try { org.json.JSONObject(entity.budgetDataJson) } catch (_: Exception) { null }
    } else null

    return com.example.myapplication.prestador.data.model.Message(
        id = entity.id,
        text = if (type == com.example.myapplication.prestador.data.model.Message.MessageType.IMAGE) null else entity.content ?: "",
        imageUrl = if (type == com.example.myapplication.prestador.data.model.Message.MessageType.IMAGE) (entity.imageLocalPath ?: entity.imageUrl ?: entity.content) else entity.imageUrl,
        audioUrl = entity.audioLocalPath ?: entity.imageUrl, 
        audioDuration = entity.durationSeconds,
        latitude = entity.latitude,
        longitude = entity.longitude,
        appointmentId = entity.relatedId,
        appointmentTitle = if (type == com.example.myapplication.prestador.data.model.Message.MessageType.VISIT) "Visita Técnica" else null,
        appointmentDate = entity.appointmentDate,
        appointmentTime = entity.appointmentTime,
        appointmentStatus = when (entity.appointmentStatus) {
            "CONFIRMED", "ACCEPTED" -> com.example.myapplication.prestador.data.model.Message.AppointmentProposalStatus.ACCEPTED
            "REJECTED" -> com.example.myapplication.prestador.data.model.Message.AppointmentProposalStatus.REJECTED
            else -> com.example.myapplication.prestador.data.model.Message.AppointmentProposalStatus.PENDING
        },
        appointmentType = entity.appointmentType,
        providerAddress = entity.providerAddress,
        rejectionReason = entity.rejectionReason,
        timestamp = entity.timestamp,
        isFromCurrentUser = entity.senderId == com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid,
        isRead = entity.isRead,
        isDelivered = entity.isDelivered,
        isSynced = entity.isSynced,
        type = type,
        budgetNumero = budgetObj?.optString("numero"),
        budgetTotal = budgetObj?.optDouble("total"),
        budgetSubtotal = budgetObj?.optDouble("subtotal"),
        budgetImpuestos = budgetObj?.optDouble("impuestos"),
        budgetItemsJson = budgetObj?.optString("items"),
        budgetServiciosJson = budgetObj?.optString("servicios"),
        budgetHonorariosJson = budgetObj?.optString("honorarios"),
        budgetGastosJson = budgetObj?.optString("gastos"),
        budgetImpuestosJson = budgetObj?.optString("impuestosJ"),
        budgetNotas = budgetObj?.optString("notas"),
        budgetValidezDias = budgetObj?.optInt("validezDias"),
        budgetTituloTrabajo = budgetObj?.optString("titulo"),
        calendarStartDate = entity.calendarStartDate,
        calendarEndDate = entity.calendarEndDate,
        availabilityJson = entity.availabilityJson,
        bookedSlotsJson = entity.bookedSlotsJson,
        calendarInviteMessageId = entity.calendarInviteMessageId,
        receiptService = entity.receiptService,
        receiptProviderName = entity.receiptProviderName,
        receiptProfession = entity.receiptProfession,
        receiptAddress = entity.receiptAddress,
        receiptCode = entity.receiptCode,
        receiptIsTechnician = entity.receiptIsTechnician ?: false,
        receiptPrioritizeCompany = entity.receiptPrioritizeCompany ?: false,
        categoryId = entity.categoryId,
        budgetRequestDescription = entity.budgetRequestDescription,
        budgetRequestClientAddress = entity.budgetRequestClientAddress,
        replyToId = entity.replyToId,
        replyToContent = entity.replyToContent,
        replyToSenderName = entity.replyToSenderName
    )
}

@Preview(showBackground = true, name = "Conversación - Modo Empresa")
@Composable
fun PreviewChatConversationCompany() {
    ChatConversationContent(
        userName = "Maria Gomez",
        userPhotoUrl = null,
        provider = Provider(
            uid = "p1", 
            displayName = "Maverick Tech", 
            name = "Juan", 
            lastName = "Maverick", 
            email = "", 
            phoneNumber = "",
            companies = listOf(
                com.example.myapplication.core.domain.model.CompanyProvider(id = "c1", name = "Maverick Tech", photoUrl = null)
            )
        ),
        uiState = ChatUiState(
            activeCompanyId = "c1",
            isClientOnline = true
        ),
        onBack = {},
        onSendMessage = {},
        onSendImage = {},
        onSendLocation = {_, _, _ ->},
        onAudioClick = {},
        onCancelAudio = {},
        onTypingStatus = {},
        onReply = {},
        onCancelReply = {},
        onSwitchContext = {_, _ ->},
        onNavigateToClientePerfil = {},
        onAcceptAppointment = {_, _ ->},
        onRejectAppointment = {_, _ ->},
        onSendCalendarInvite = {_, _, _, _, _, _, _ ->}
    )
}
