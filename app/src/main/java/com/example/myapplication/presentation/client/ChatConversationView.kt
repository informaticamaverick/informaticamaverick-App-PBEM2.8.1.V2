package com.example.myapplication.presentation.client

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.data.model.Provider
import com.example.myapplication.ui.theme.AppColors
import com.example.myapplication.ui.theme.getAppColors
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.presentation.components.*
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationView(
    provider: Provider,
    viewModel: ChatViewModel,
    onBack: () -> Unit,
    appColors: AppColors,
    appointmentViewModel: AppointmentViewModel = hiltViewModel(),
    budgetViewModel: BudgetViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val selectedBudget by viewModel.selectedBudget.collectAsStateWithLifecycle()
    val isProviderTyping by viewModel.isProviderTyping.collectAsStateWithLifecycle()
    val isProviderOnline by produceState(initialValue = false, provider.uid) {
        val ref = FirebaseDatabase.getInstance().reference
            .child("users").child(provider.uid).child("online")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                value = snapshot.getValue(Boolean::class.java) ?: false
            }
            override fun onCancelled(error: DatabaseError) {
                value = false
            }
        }
        ref.addValueEventListener(listener)
        awaitDispose {
            ref.removeEventListener(listener)
        }
    }

    var inputText by remember { mutableStateOf("") }
    var showAttachMenu by remember { mutableStateOf(false) }
    var showTenderSelectionDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Auto-detener grabación después de 60 segundos
    LaunchedEffect(Unit) {
        viewModel.observeProviderTyping()
    }
    var recordingSeconds by remember { mutableStateOf(0) }
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                delay(1000)
                recordingSeconds++
                if (recordingSeconds >= 60) {
                    viewModel.stopRecordingAndSend()
                    break
                }
            }
        } else {
            recordingSeconds = 0
        }
    }

    // --- PERMISOS Y LAUNCHERS ---

    // Ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val fusedLocation = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocation.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.sendLocation(location.latitude, location.longitude)
                    } else {
                        Toast.makeText(context, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: SecurityException) {
                Toast.makeText(context, "Error al obtener ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sendRealLocation() {
        showAttachMenu = false
        val fusedLocation = LocationServices.getFusedLocationProviderClient(context)
        val hasFine = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            try {
                fusedLocation.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.sendLocation(location.latitude, location.longitude)
                    } else {
                        Toast.makeText(context, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: SecurityException) {
                Toast.makeText(context, "Error al obtener ubicación", Toast.LENGTH_SHORT).show()
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Audio
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.startRecording(context)
    }

    fun launchAudio() {
        val perm = android.Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(context, perm) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            viewModel.startRecording(context)
        } else {
            audioPermissionLauncher.launch(perm)
        }
    }

    // Cámara y galería
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.sendImage(it); showAttachMenu = false }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) cameraImageUri?.let { viewModel.sendImage(it) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            try {
                val file = java.io.File.createTempFile("img_", ".jpg", context.cacheDir)
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context, "${context.packageName}.provider", file
                )
                cameraImageUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) { }
        }
    }

    fun launchCamera() {
        val perm = android.Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(context, perm) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            try {
                val file = java.io.File.createTempFile("img_", ".jpg", context.cacheDir)
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context, "${context.packageName}.provider", file
                )
                cameraImageUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) { }
        } else {
            cameraPermissionLauncher.launch(perm)
        }
    }

    // --- DATOS ---
    val mainCategory = provider.categories.firstOrNull() ?: ""
    val matchingTenders by viewModel.getMatchingTenders(mainCategory)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val providerPhotoUrl by viewModel.providerPhotoUrl.collectAsStateWithLifecycle()
    val effectiveProviderPhoto = providerPhotoUrl ?: provider.photoUrl

    // --- VISOR DE PRESUPUESTO A4 ---
    if (selectedBudget != null) {
        Dialog(
            onDismissRequest = { viewModel.clearSelectedBudget() },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                BudgetMultiPageScreen(
                    budget = selectedBudget!!,
                    onAccept = {
                        budgetViewModel.acceptBudget(selectedBudget!!)
                        viewModel.clearSelectedBudget()
                    },
                    onReject = {
                        budgetViewModel.rejectBudget(selectedBudget!!)
                        viewModel.clearSelectedBudget()
                    },
                    onBack = { viewModel.clearSelectedBudget() }
                )
            }
        }
    }

    if (showScheduleDialog) {
        ScheduleAppointmentDialog(
            onDismiss = { showScheduleDialog = false },
            onConfirm = { date, time, notes ->
                showScheduleDialog = false
                // Convierte "dd/MM/yyyy" → "yyyy-MM-dd" para el repositorio
                val parts = date.split("/")
                val isoDate = if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else date
                viewModel.sendAppointment(date, time, notes)
                appointmentViewModel.solicitarCita(
                    provider = provider,
                    date = isoDate,
                    time = time,
                    service = "Cita agendada por chat",
                    notes = notes
                )
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
        topBar = {
            ChatHeader(
                providerName = provider.displayName,
                providerPhoto = effectiveProviderPhoto,
                isOnline = isProviderOnline,
                onBack = onBack,
                appColors = appColors,
                isProviderTyping = isProviderTyping
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
                        onInviteClick = { showTenderSelectionDialog = true }
                    )
                }


                MessageInputBar(
                    inputText = inputText,
                    onInputChange = { inputText = it; viewModel.setTypingStatus(it.isNotEmpty()) },
                    onSendMessage = { viewModel.sendText(it); inputText = "" },
                    appColors = appColors,
                    onAttachMenuToggle = { showAttachMenu = !showAttachMenu },
                    onCameraClick = { launchCamera() },
                    onAudioClick = {
                        if (isRecording) viewModel.stopRecordingAndSend()
                        else launchAudio()
                    },
                    onCancelAudio = { viewModel.cancelRecording() },
                    isRecordingAudio = isRecording
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

            itemsIndexed(messages.reversed()) { index, message ->
                val reversedMessages = messages.reversed()
                if (index == reversedMessages.size - 1 || !isSameDay(message.timestamp, reversedMessages[index + 1].timestamp)) {
                    DateSeparator(timestamp = message.timestamp, appColors = appColors)
                }
                val resolvedPhoto = effectiveProviderPhoto

                when (message.type) {
                    MessageType.BUDGET -> BudgetBubble(
                        message = message,
                        isMe = message.senderId == viewModel.currentUserId,
                        appColors = appColors,
                        onClick = { message.relatedId?.let { viewModel.onBudgetClicked(it) } }
                    )
                    MessageType.AUDIO -> AudioMessageBubble(
                        audioPath = if (message.content == "[Audio]" && message.imageUrl != null) message.imageUrl else message.content,
                        duration = message.durationSeconds ?: 0,
                        timestamp = message.timestamp,
                        appColors = appColors,
                        isFromMe = message.senderId == viewModel.currentUserId
                    )

                    MessageType.IMAGE -> {
                        var showViewer by remember { mutableStateOf(false) }
                        MessageBubble(
                            message = message,
                            appColors = appColors,
                            currentUserId = viewModel.currentUserId,
                            onImageClick = { showViewer = true }
                        )
                        if (showViewer) {
                            ImageZoomDialog(
                                message = message,
                                onDismiss = { showViewer = false }
                            )
                        }
                    }


                    MessageType.LOCATION -> MessageBubble(
                        // ← AGREGAR ESTO
                        message = message,
                        appColors = appColors,
                        currentUserId = viewModel.currentUserId
                    )

                    MessageType.VISIT -> AppointmentBubble(
                        message = message,
                        isMe = message.senderId == viewModel.currentUserId,
                        appColors = appColors,
                        chatId = viewModel.chatId,
                        providerPhotoUrl = if (message.senderId != viewModel.currentUserId) resolvedPhoto else null,
                        onAccept = { appointmentViewModel.respondToProviderAppointment(
                            chatId = viewModel.chatId,
                            messageId = message.id,
                            appointmentId = message.relatedId,
                            accept = true,
                            date = message.appointmentDate,
                            time = message.appointmentTime,
                            title = message.content.split("|").firstOrNull()?.trim()?.ifBlank { null }?.replaceFirstChar { it.uppercase() } ?: "Cita agendada",
                            providerName = provider.displayName,
                            providerPhotoUrl = resolvedPhoto
                        )},
                        onReject = { appointmentViewModel.respondToProviderAppointment(
                            chatId = viewModel.chatId,
                            messageId = message.id,
                            appointmentId = message.relatedId,
                            accept = false
                        )}
                    )

                    else -> EnhancedMessageBubble(
                        message = message,
                        isMe = message.senderId == viewModel.currentUserId,
                        appColors = appColors,
                        senderPhotoUrl = if (message.senderId != viewModel.currentUserId) resolvedPhoto else null
                    )
                }
            }
        }

        if (showTenderSelectionDialog) {
            TenderSelectionDialog(
                matchingTenders = matchingTenders,
                providerCategories = provider.categories,
                appColors = appColors,
                onDismiss = { showTenderSelectionDialog = false },
                onSelect = { viewModel.sendTenderInvitation(it); showTenderSelectionDialog = false }
            )
        }
    }
}

// --- COMPONENTE BURBUJA PARA PRESUPUESTOS ---

@Composable
fun AppointmentBubble(
    message: com.example.myapplication.data.local.MessageEntity,
    isMe: Boolean,
    appColors: AppColors,
    chatId: String = "",
    providerPhotoUrl: String? = null,
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null
) {
    // Contenido: "titulo|fecha|hora|notas"
    val parts = message.content.split("|")
    val titulo = parts.getOrElse(0) { "" }.ifBlank { if (isMe) "Solicitud de Turno" else "Propuesta de Turno" }
    val fecha  = parts.getOrElse(1) { "" }.ifBlank { message.appointmentDate ?: "" }
    val hora   = parts.getOrElse(2) { "" }.ifBlank { message.appointmentTime ?: "" }
    val notas  = parts.getOrElse(3) { "" }

    // Formatear fecha de yyyy-MM-dd a dd/MM/yyyy
    val fechaDisplay = if (fecha.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
        val p = fecha.split("-"); "${p[2]}/${p[1]}/${p[0]}"
    } else fecha

    // Estado dinámico del turno (distinto al estado de entrega del mensaje)
    val statusStr = message.appointmentStatus ?: "PENDING"
    val accentColor = when {
        statusStr == "ACCEPTED" -> Color(0xFF10B981)
        statusStr == "REJECTED" -> Color(0xFFEF4444)
        else -> Color(0xFF7C3AED)
    }
    val statusLabel = when {
        statusStr == "ACCEPTED" -> "✓  Turno confirmado"
        statusStr == "REJECTED" -> "✗  Turno rechazado"
        else -> "⏳  Pendiente de respuesta"
    }

    val alignment = if (isMe) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp, topEnd = 18.dp,
                bottomStart = if (isMe) 18.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 18.dp
            ),
            color = appColors.surfaceColor,
            border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.4f)),
            modifier = Modifier.widthIn(min = 220.dp, max = 290.dp)
        ) {
            Column {
                // Encabezado con color de estado
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(accentColor)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!providerPhotoUrl.isNullOrBlank()) {
                            ProviderPhoto(
                                photoData = providerPhotoUrl,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Icon(Icons.Default.CalendarMonth, null, tint = Color.White, modifier = Modifier.size(15.dp))
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isMe) "Solicitud enviada" else "Propuesta recibida",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = titulo,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                // Cuerpo
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    // Fecha y hora
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .background(accentColor.copy(alpha = 0.07f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, null, tint = accentColor, modifier = Modifier.size(13.dp))
                            Column {
                                Text("Fecha", fontSize = 9.sp, color = appColors.textSecondaryColor)
                                Text(fechaDisplay.ifBlank { "—" }, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = appColors.textPrimaryColor)
                            }
                        }
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .background(accentColor.copy(alpha = 0.07f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Schedule, null, tint = accentColor, modifier = Modifier.size(13.dp))
                            Column {
                                Text("Hora", fontSize = 9.sp, color = appColors.textSecondaryColor)
                                Text(hora.ifBlank { "—" }, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = appColors.textPrimaryColor)
                            }
                        }
                    }

                    // Notas
                    if (notas.isNotBlank()) {
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Notes, null, tint = appColors.textSecondaryColor, modifier = Modifier.size(13.dp).padding(top = 2.dp))
                            Text(notas, fontSize = 11.sp, color = appColors.textSecondaryColor)
                        }
                    }

                    HorizontalDivider(color = accentColor.copy(alpha = 0.12f))

                    // Badge de estado + hora
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(20.dp), color = accentColor.copy(alpha = 0.1f)) {
                            Text(
                                text = statusLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                            fontSize = 10.sp,
                            color = appColors.textSecondaryColor
                        )
                    }

                    // Botones Aceptar / Rechazar (solo si vino del prestador y está pendiente)
                    if (!isMe && statusStr != "ACCEPTED" && statusStr != "REJECTED") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onReject?.invoke() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                                modifier = Modifier.weight(1f).height(34.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Rechazar", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = { onAccept?.invoke() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier.weight(1f).height(34.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Done, null, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Aceptar", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENTE BURBUJA PARA PRESUPUESTOS ---

@Composable
fun BudgetBubble(
    message: com.example.myapplication.data.local.MessageEntity,
    isMe: Boolean,
    appColors: AppColors,
    onClick: () -> Unit
) {
    val alignment = if (isMe) Alignment.End else Alignment.Start
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalAlignment = alignment) {
        Surface(
            color = if (isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else appColors.surfaceColor,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier.widthIn(max = 280.dp).clickable(onClick = onClick)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, "Presupuesto", tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Propuesta Técnica", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(8.dp))
                Text(message.content, fontSize = 14.sp, color = appColors.textPrimaryColor)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                    Text("VER DETALLE")
                }
            }
        }
    }
}

// --- OTROS COMPONENTES ---

@Composable
fun DateSeparator(timestamp: Long, appColors: AppColors) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Surface(color = appColors.surfaceColor.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp)) {
            Text(
                text = SimpleDateFormat("dd 'de' MMMM", Locale.getDefault()).format(Date(timestamp)),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 11.sp,
                color = appColors.textSecondaryColor
            )
        }
    }
}

@Composable
fun EnhancedMessageBubble(
    message: com.example.myapplication.data.local.MessageEntity,
    isMe: Boolean,
    appColors: AppColors,
    senderPhotoUrl: String? = null
) {
    val colors = if (isMe) {
        BubbleColors(container = MaterialTheme.colorScheme.primary, content = MaterialTheme.colorScheme.onPrimary)
    } else {
        BubbleColors(container = appColors.surfaceColor, content = appColors.textPrimaryColor)
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMe) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (!senderPhotoUrl.isNullOrBlank()) {
                    ProviderPhoto(
                        photoData = senderPhotoUrl,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(
                        text = message.senderId.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
        }
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            Surface(
                modifier = Modifier.widthIn(max = 300.dp),
                color = colors.container,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = 8.dp,
                        bottom = 4.dp
                    )
                ) {
                    val formattedTime = remember(message.timestamp) {
                        java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date(message.timestamp))
                    }
                    val isRead = message.isRead || message.status == "READ"
                    val isDelivered = message.status == "DELIVERED" || isRead
                    val isSynced = message.isSynced
                    val timeColor = colors.content.copy(alpha = 0.7f)
                    val tickColor = if (isRead) Color(0xFF53BDEB) else timeColor

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = message.content,
                            color = colors.content,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = formattedTime,
                                fontSize = 10.sp,
                                color = timeColor
                            )
                            if (isMe) {
                                Spacer(modifier = Modifier.width(2.dp))
                                when {
                                    !isSynced -> Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = timeColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    isDelivered -> Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = null,
                                        tint = tickColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    else -> Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = null,
                                        tint = timeColor,
                                        modifier = Modifier.size(14.dp)
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

data class BubbleColors(val container: Color, val content: Color)

fun isSameDay(t1: Long, t2: Long): Boolean {
    val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    return fmt.format(Date(t1)) == fmt.format(Date(t2))
}

// --- DIALOG PARA AGENDAR CITA ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAppointmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (date: String, time: String, notes: String) -> Unit
) {
    val appColors = getAppColors()
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedTime by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val selectedDateText = selectedDateMillis?.let { dateFormatter.format(Date(it)) } ?: ""

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = appColors.accentBlue,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Agendar Cita",
                    style = MaterialTheme.typography.titleLarge,
                    color = appColors.textPrimaryColor
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selector de fecha
                val dateInteractionSource = remember { MutableInteractionSource() }
                OutlinedTextField(
                    value = selectedDateText,
                    onValueChange = {},
                    label = { Text("Fecha") },
                    placeholder = { Text("Seleccionar fecha") },
                    readOnly = true,
                    interactionSource = dateInteractionSource,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = appColors.accentBlue)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.accentBlue,
                        focusedLabelColor = appColors.accentBlue,
                        unfocusedBorderColor = appColors.textSecondaryColor.copy(alpha = 0.5f),
                        disabledBorderColor = appColors.textSecondaryColor.copy(alpha = 0.5f),
                        disabledTextColor = appColors.textPrimaryColor
                    ),
                    singleLine = true
                )
                LaunchedEffect(dateInteractionSource) {
                    dateInteractionSource.interactions.collect { interaction ->
                        if (interaction is PressInteraction.Release) showDatePicker = true
                    }
                }

                // Selector de hora
                val timeInteractionSource = remember { MutableInteractionSource() }
                OutlinedTextField(
                    value = selectedTime,
                    onValueChange = {},
                    readOnly = true,
                    interactionSource = timeInteractionSource,
                    label = { Text("Hora") },
                    placeholder = { Text("Seleccionar hora") },
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = appColors.accentBlue)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.accentBlue,
                        focusedLabelColor = appColors.accentBlue,
                        unfocusedBorderColor = appColors.textSecondaryColor.copy(alpha = 0.5f),
                        disabledBorderColor = appColors.textSecondaryColor.copy(alpha = 0.5f),
                        disabledTextColor = appColors.textPrimaryColor
                    ),
                    singleLine = true
                )
                LaunchedEffect(timeInteractionSource) {
                    timeInteractionSource.interactions.collect { interaction ->
                        if (interaction is PressInteraction.Release) showTimePicker = true
                    }
                }

                // Notas
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (opcional)") },
                    placeholder = { Text("Detalles del servicio...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.accentBlue,
                        focusedLabelColor = appColors.accentBlue,
                        unfocusedBorderColor = appColors.textSecondaryColor.copy(alpha = 0.5f)
                    ),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (selectedDateText.isNotEmpty() && selectedTime.isNotEmpty()) onConfirm(selectedDateText, selectedTime, notes) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors.accentBlue,
                    disabledContainerColor = appColors.accentBlue.copy(alpha = 0.5f)
                ),
                enabled = selectedDateText.isNotEmpty() && selectedTime.isNotEmpty()
            ) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = appColors.textSecondaryColor)
            ) { Text("Cancelar") }
        },
        containerColor = appColors.surfaceColor,
        titleContentColor = appColors.textPrimaryColor,
        textContentColor = appColors.textPrimaryColor
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selected = datePickerState.selectedDateMillis
                    val today = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    if (selected != null && selected >= today) {
                        selectedDateMillis = selected
                        showDatePicker = false
                    }
                }) { Text("Aceptar", color = appColors.accentBlue) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = appColors.textSecondaryColor)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = appColors.surfaceColor)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = appColors.surfaceColor,
                    titleContentColor = appColors.textPrimaryColor,
                    headlineContentColor = appColors.textPrimaryColor,
                    weekdayContentColor = appColors.textSecondaryColor,
                    subheadContentColor = appColors.textSecondaryColor,
                    dayContentColor = appColors.textPrimaryColor,
                    selectedDayContainerColor = appColors.accentBlue,
                    todayContentColor = appColors.accentBlue,
                    todayDateBorderColor = appColors.accentBlue
                )
            )
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.split(":").getOrNull(0)?.toIntOrNull() ?: 9,
            initialMinute = selectedTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0,
            is24Hour = true
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = appColors.surfaceColor, shadowElevation = 8.dp) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Seleccionar Hora",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimaryColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = appColors.surfaceColor,
                            clockDialSelectedContentColor = Color.White,
                            clockDialUnselectedContentColor = appColors.textPrimaryColor,
                            selectorColor = appColors.accentBlue,
                            containerColor = appColors.surfaceColor,
                            periodSelectorBorderColor = appColors.textSecondaryColor.copy(alpha = 0.3f),
                            periodSelectorSelectedContainerColor = appColors.accentBlue,
                            periodSelectorUnselectedContainerColor = Color.Transparent,
                            periodSelectorSelectedContentColor = Color.White,
                            periodSelectorUnselectedContentColor = appColors.textSecondaryColor,
                            timeSelectorSelectedContainerColor = appColors.accentBlue.copy(alpha = 0.2f),
                            timeSelectorUnselectedContainerColor = appColors.surfaceColor,
                            timeSelectorSelectedContentColor = appColors.accentBlue,
                            timeSelectorUnselectedContentColor = appColors.textPrimaryColor
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancelar", color = appColors.textSecondaryColor)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            val hour = timePickerState.hour.toString().padStart(2, '0')
                            val minute = timePickerState.minute.toString().padStart(2, '0')
                            selectedTime = "$hour:$minute"
                            showTimePicker = false
                        }) {
                            Text("Aceptar", color = appColors.accentBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
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
    // TODO: implementar lista de licitaciones para invitar al prestador
}

