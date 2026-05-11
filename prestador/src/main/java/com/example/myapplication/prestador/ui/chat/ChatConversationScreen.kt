package com.example.myapplication.prestador.ui.chat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
//import com.example.myapplication.prestador.data.PPrestadorProfileFalso
import com.example.myapplication.prestador.data.model.Message
import com.example.myapplication.prestador.ui.presupuesto.BudgetItem
import com.example.myapplication.prestador.ui.presupuesto.BudgetMiscExpense
import com.example.myapplication.prestador.ui.presupuesto.BudgetProfessionalFee
import com.example.myapplication.prestador.ui.presupuesto.BudgetPreviewPDFDialog
import com.example.myapplication.prestador.ui.presupuesto.BudgetService
import com.example.myapplication.prestador.ui.presupuesto.BudgetTax
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import com.example.myapplication.prestador.utils.NotificationHelper
import com.example.myapplication.prestador.utils.displayAddress
import com.example.myapplication.prestador.utils.displayCompanyOrFullName
import com.example.myapplication.prestador.utils.PrestadorProfile
import com.example.myapplication.prestador.viewmodel.EditProfileViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(
    userId: String,
    userName: String,
    userPhotoUrl: String? = null,
    providerId: String,  // Ahora es requerido, se pasa desde ChatScreen
    onBack: () -> Unit,
    onNavigateToPresupuesto: () -> Unit = {},
    editProfileViewModel: EditProfileViewModel = hiltViewModel(),
    onNavigateToClientePerfil: () -> Unit = {},
    autoOpenCalendarDialog: Boolean = false,
    rescheduleDate: String = "",
    rescheduleTime: String = ""
){
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }

    val chatViewModel: com.example.myapplication.prestador.viewmodel.ChatViewModel = hiltViewModel()
    val chatId = remember(userId, providerId) {
        com.example.myapplication.prestador.utils.ChatIdHelper.generateChatId(userId, providerId)
    }

    // Estado para saber si este chat está visible
    var isChatVisible by remember { mutableStateOf(true) }

    // Detectar cuando el chat se muestra/oculta
    DisposableEffect(Unit) {
        isChatVisible = true
        println("🟢 ChatConversationScreen con $userName ahora VISIBLE")

        onDispose {
            isChatVisible = false
            println("🔴 ChatConversationScreen con $userName ahora OCULTO (DESTRUIDO)")
        }
    }

    //Solicitar permisos de notficaciones en Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            println("Permiso de notificaciones otorgado")
        } else {
            println("Permiso de notificaciones denegado")
        }
    }

    //Pedir permiso al iniciar
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationHelper.hasNotificationPermission()) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    val colors = getPrestadorColors()

    LaunchedEffect(chatId) {
        android.util.Log.d("DEBUG_ADDRESS", "💬 Chat abierto | chatId='$chatId' | userId='$userId' | providerId='$providerId'")
        chatViewModel.loadMessagesByConversation(chatId)
        chatViewModel.markMessagesAsRead(chatId)
    }
    val messageEntities by chatViewModel.messages.collectAsState()

    // Mapeo de entidades de Room/Firebase a modelo de UI Message
    val mappedMessages = remember(messageEntities) { messageEntities.reversed().map { entity ->
        val type = when (entity.messageType) {
            "IMAGE" -> Message.MessageType.IMAGE
            "AUDIO" -> Message.MessageType.AUDIO
            "LOCATION" -> Message.MessageType.LOCATION
            "DOCUMENT" -> Message.MessageType.DOCUMENT
            "APPOINTMENT" -> Message.MessageType.APPOINTMENT
            "BUDGET" -> Message.MessageType.BUDGET
            "CALENDAR_INVITE" -> Message.MessageType.CALENDAR_INVITE
            "APPOINTMENT_REQUEST" -> Message.MessageType.APPOINTMENT_REQUEST
            "APPOINTMENT_RECEIPT" -> Message.MessageType.APPOINTMENT_RECEIPT
            "RESCHEDULE_NOTICE" -> Message.MessageType.RESCHEDULE_NOTICE
            "COMPLETION_NOTICE" -> Message.MessageType.COMPLETION_NOTICE
            "CANCELLATION_NOTICE" -> Message.MessageType.CANCELLATION_NOTICE
            "BUDGET_REQUEST" -> Message.MessageType.BUDGET_REQUEST
            else -> Message.MessageType.TEXT
        }
        if (type == Message.MessageType.BUDGET_REQUEST) {
            android.util.Log.d("DEBUG_ADDRESS", "🗺️ Mapping BUDGET_REQUEST | id=${entity.messageId} | entity.budgetRequestClientAddress='${entity.budgetRequestClientAddress}'")
        }
        val budgetObj = if (type == Message.MessageType.BUDGET && entity.budgetDataJson != null) {
            try { org.json.JSONObject(entity.budgetDataJson) } catch (e: Exception) { null }
        } else null
        Message(
            id = entity.messageId,
            text = if (type == Message.MessageType.IMAGE) null else entity.text ?: "",
            imageUrl = if (type == Message.MessageType.IMAGE)
                (entity.imageLocalPath ?: entity.imageUrl ?: entity.text) else entity.imageUrl,
            audioUrl = entity.audioLocalPath ?: entity.audioUrl,
            audioDuration = entity.audioDuration,
            latitude = entity.latitude,
            longitude = entity.longitude,
            appointmentId = entity.appointmentId,
            appointmentTitle = if (entity.appointmentTitle.isNullOrBlank()) "Solicitud de cita" else entity.appointmentTitle,
            appointmentDate = entity.appointmentDate,
            appointmentTime = entity.appointmentTime,
            appointmentStatus = when (entity.appointmentStatus) {
                "ACCEPTED" -> Message.AppointmentProposalStatus.ACCEPTED
                "REJECTED" -> Message.AppointmentProposalStatus.REJECTED
                else ->
                    Message.AppointmentProposalStatus.PENDING
            },
            appointmentType = entity.appointmentType,
            providerAddress = entity.providerAddress,
            rejectionReason = entity.rejectionReason,
            timestamp = entity.timestamp,
            isFromCurrentUser = entity.isFromCurrentUser,
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
           // budgetCategorias = budgetObj?.optString("categorias"),
           // budgetClienteId = budgetObj?.optString("clienteId"),
            calendarStartDate = entity.calendarStartDate,
            calendarEndDate = entity.calendarEndDate,
            availabilityJson = entity.availabilityJson,
            bookedSlotsJson = entity.bookedSlotsJson,
            calendarInviteMessageId = entity.calendarInviteMessageId,
            // Campos del recibo de turno confirmado
            receiptService = entity.receiptService,
            receiptProviderName = entity.receiptProviderName,
            receiptProfession = entity.receiptProfession,
            receiptAddress = entity.receiptAddress,
            receiptCode = entity.receiptCode,
            receiptIsTechnician = entity.receiptIsTechnician,
            receiptPrioritizeCompany = entity.receiptPrioritizeCompany,
            categoryId = entity.categoryId,
            budgetRequestDescription = entity.budgetRequestDescription,
            budgetRequestClientAddress = entity.budgetRequestClientAddress,
            replyToId = entity.replyToId,
            replyToContent = entity.replyToContent,
            replyToSenderName = entity.replyToSenderName
        )
    }.toList() }

    var messageText by remember { mutableStateOf("") }
    var zoomedImageUrl by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Con reverseLayout=true: ítem[0] = abajo. scrollToItem(0) va al mensaje más nuevo (sin animación).
    LaunchedEffect(mappedMessages.size) {
        if (mappedMessages.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    // Estados para adjuntos y grabación
    var showAttachMenu by remember { mutableStateOf(false) }
    var showSendCalendarDialog by remember { mutableStateOf(false) }
    var pendingAppointmentType by remember { mutableStateOf("TECHNICAL_VISIT") }
    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf(0) }

    // Auto-abrir el diálogo de calendario al entrar desde "Reprogramar" del calendario
    LaunchedEffect(autoOpenCalendarDialog) {
        if (autoOpenCalendarDialog) {
            if (rescheduleDate.isNotBlank() && rescheduleTime.isNotBlank()) {
                chatViewModel.sendRescheduleNotice(chatId, rescheduleDate, rescheduleTime)
            }
            showSendCalendarDialog = true
        }
    }

    // Estado del sheet de presupuesto
    var showBudgetSheet by remember { mutableStateOf(false) }
    var budgetRequestMessage by remember { mutableStateOf<Message?>(null) }

    // Presupuesto a visualizar al hacer clic en "Ver presupuesto"
    var presupuestoMsgToView by remember { mutableStateOf<Message?>(null) }

    // Datos según tipo de servicio
    val profileState by editProfileViewModel.profileState.collectAsState()
    val businessEntity by editProfileViewModel.businessEntity.collectAsState()

    val currentServiceType = remember(profileState) {
        when (profileState) {
            is com.example.myapplication.prestador.viewmodel.ProfileState.Success ->
                com.example.myapplication.prestador.data.model.ServiceType.fromString(
                    (profileState as com.example.myapplication.prestador.viewmodel.ProfileState.Success).provider.serviceType
                )

            else -> com.example.myapplication.prestador.data.model.ServiceType.PROFESSIONAL
        }
    }

    // En algunos flujos, el providerId real para horarios es el del perfil (Room/Firebase),
    // no necesariamente el UID de Auth.
    val effectiveProviderId = remember(profileState, providerId) {
        val fromProfile =
            (profileState as? com.example.myapplication.prestador.viewmodel.ProfileState.Success)?.provider?.id
        if (!fromProfile.isNullOrBlank()) fromProfile else providerId
    }

    val provider =
        (profileState as? com.example.myapplication.prestador.viewmodel.ProfileState.Success)?.provider
    val providerDisplayName = provider?.displayCompanyOrFullName(businessEntity).orEmpty()
    val providerDisplayAddress = provider?.displayAddress(businessEntity).orEmpty()
    // Estado para guardar datos de cita temporalmente

    // Estados para imágenes
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Estados para grabación de audio
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFilePath by remember { mutableStateOf<String?>(null) }

    //Liberar MediaRecorder si el usuario sale del chat sin terminar la grabación
    DisposableEffect(Unit) {
        onDispose {
            try { mediaRecorder?.stop() } catch (_: Exception) {}
            try { mediaRecorder?.release() } catch (_: Exception) {}
            mediaRecorder = null
        }
    }


    // Estado para rastrear propuestas pendientes y sus timers
    var pendingProposals by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Estado para mostrar indicador de "escribiendo..."
    val isClientTyping by chatViewModel.isClientTyping.collectAsState()
    var typingClientName by remember { mutableStateOf("") }
    val isClientOnline by produceState(initialValue = false, userId) {
        val ref = FirebaseDatabase.getInstance().reference
            .child("users").child(userId).child("online")
        val listener = object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot) {
                value = snapshot.getValue(Boolean::class.java) ?: false
            }
            override fun onCancelled(error: DatabaseError) { value = false }
        }
        ref.addValueEventListener(listener)
        awaitDispose {
            ref.removeEventListener(listener)
        }
    }

    LaunchedEffect(chatId, userId) {
        android.util.Log.d("TYPING_DEBUG", "LaunchedEffect chatId=$chatId userId=$userId")
        typingClientName = userName
        chatViewModel.observeClientTyping(chatId, userId)
    }

    // ⚠️ NOTA: La lógica de auto-respuesta y mensajes espontáneos
    // ahora está en ChatScreen.kt para que persista al salir del chat

    // Función para detener grabación y enviar
    fun stopRecordingAndSend() {
        val pathToSend = audioFilePath
        val duration = recordingTime

        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            mediaRecorder?.release()
        } catch (e: Exception) { /* ignorar */
        }
        mediaRecorder = null
        isRecording = false
        recordingTime = 0
        audioFilePath = null

        pathToSend?.let { path ->
            chatViewModel.sendAudioMessage(path, duration)
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    // LaunchedEffect para contador de tiempo de grabación (límite 60 segundos)
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingTime = 0
            while (isRecording) {
                delay(1000)
                recordingTime++
                if (recordingTime >= 60) {
                    stopRecordingAndSend()
                    break
                }
            }
        } else {
            recordingTime = 0
        }
    }

    // LaunchedEffect para abrir el teclado automáticamente
    LaunchedEffect(Unit) {
        delay(600)
        keyboardController?.show()
    }

    // SideEffect para forzar el foco en el campo de texto
    DisposableEffect(Unit) {
        val job = coroutineScope.launch {
            delay(700)
            keyboardController?.show()
        }
        onDispose {
            job.cancel()
        }
    }

    // Crear URI temporal para la foto
    val tempPhotoUri = remember {
        val photoFile = File(
            context.cacheDir,
            "camera_photo_${System.currentTimeMillis()}.jpg"
        )
        FileProvider.getUriForFile(
            context,
            "com.example.myapplication.prestador.fileprovider",
            photoFile
        )
    }

    // Launcher para tomar foto con la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = object : ActivityResultContract<Uri, Boolean>() {
            override fun createIntent(context: Context, input: Uri): Intent {
                return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, input)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
            }
            override fun parseResult(resultCode: Int, intent: Intent?): Boolean
            {
                return resultCode == Activity.RESULT_OK
            }
        }
    ) { success ->
        if (success) {
            cameraImageUri = tempPhotoUri
            chatViewModel.sendImage(tempPhotoUri, context)
            cameraImageUri = null

            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    // Launcher para permisos de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(tempPhotoUri)
        } else {
            // Mostrar mensaje de error
        }
    }

    // Launcher para galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            chatViewModel.sendImage(uri, context)
            selectedImageUri = null

            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    // Función para iniciar grabación
    fun startRecording() {
        val audioFile = File(context.cacheDir, "audio_${System.currentTimeMillis()}.m4a")
        audioFilePath = audioFile.absolutePath

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(32_000)
            setAudioSamplingRate(16_000)
            setOutputFile(audioFilePath)
            try {
                prepare()
                start()
                isRecording = true
            } catch (e: Exception) {
                e.printStackTrace()
                isRecording = false
            }
        }
    }

    // Launcher para permisos de audio
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecording()
        }
    }

    // Launcher para permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted =
            permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted =
            permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            // Permiso concedido, obtener ubicación
            val fusedLocationClient =
                com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(
                    context
                )
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {

                        chatViewModel.sendLocation(location.latitude, location.longitude)
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    }
                }
            } catch (e: SecurityException) {
                android.widget.Toast.makeText(
                    context,
                    "Error al obtener ubicación",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            // Permiso denegado
            android.widget.Toast.makeText(
                context,
                "Se necesita permiso de ubicación para compartir tu posición",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Función para cancelar grabación
    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            // Eliminar archivo de audio
            audioFilePath?.let { path ->
                File(path).delete()
            }

            isRecording = false
            recordingTime = 0
            audioFilePath = null
        } catch (e: Exception) {
            e.printStackTrace()
            isRecording = false
            recordingTime = 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .clickable { onNavigateToClientePerfil() }
                            .fillMaxWidth()
                    ) {
                        // Avatar — también clickeable directamente
                        Surface(
                            onClick = { onNavigateToClientePerfil() },
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = Color(0xFFF97316)
                        ) {
                            if (userPhotoUrl != null) {
                                AsyncImage(
                                    model = userPhotoUrl,
                                    contentDescription = "Foto de $userName",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = userName.take(1).uppercase(),
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Column {
                            Text(
                                text = userName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            if (isClientTyping) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "escribiendo...",
                                        fontSize = 12.sp,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            } else {
                                if (isClientOnline) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(Color(0xFF10B981), CircleShape)
                                        )
                                        Text(
                                            text = "En línea",
                                            fontSize = 11.sp,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colors.textPrimary
                        )
                    }
                },
                actions = {
                    // Botones de acción removidos temporalmente
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surfaceColor
                )
            )
        },
        containerColor = colors.backgroundColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.backgroundColor)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.backgroundColor)
            ) {
                // Lista de mensajes(reverseLayout=true: el ítem[0] se muestra abajo)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = listState,
                    reverseLayout = true,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    items(
                        mappedMessages,
                        key = { message -> message.id }
                    ) { message ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (message.isFromCurrentUser)
                                Arrangement.End else Arrangement.Start
                        ) {
                            MessageBubble(
                                message = message,
                                isFromCurrentUser = message.isFromCurrentUser,
                                clientName = userName,
                                onVerPresupuesto = if (message.type == Message.MessageType.BUDGET) {
                                    { presupuestoMsgToView = message }
                                } else null,
                                onImageClick = { url -> zoomedImageUrl = url },
                                onAccept = if (message.type == Message.MessageType.APPOINTMENT_REQUEST &&
                                    message.appointmentStatus == Message.AppointmentProposalStatus.PENDING) {
                                    { serviceTitle ->
                                        val isVisit = message.appointmentType == "TECHNICAL_VISIT"
                                        val finalAddr = if (isVisit) message.providerAddress else (provider?.address?.fullString())
                                        chatViewModel.respondToAppointmentRequest(
                                            messageId = message.id,
                                            clientName = userName,
                                            date = message.appointmentDate ?: "",
                                            time = message.appointmentTime ?: "",
                                            service = serviceTitle,
                                            providerName = providerDisplayName,
                                            serviceType = provider?.serviceType ?: "PROFESSIONAL",
                                            doesHomeVisits = provider?.doesHomeVisits ?: false,
                                            profession = provider?.profesion,
                                            providerAddress = provider?.address?.fullString(),
                                            appointmentType = message.appointmentType ?: "TECHNICAL_VISIT",
                                            serviceCategory = message.categoryId
                                                ?: provider?.categories?.firstOrNull(),
                                            accepted = true
                                        )
                                    }
                                } else null,

                                onCreateBudgetFromRequest = if (message.type == Message.MessageType.BUDGET_REQUEST) {
                                    {
                                        android.util.Log.d("DEBUG_ADDRESS", "🖱️ Crear presupuesto clicked | budgetRequestClientAddress = '${message.budgetRequestClientAddress}'")
                                        budgetRequestMessage = message
                                        showBudgetSheet = true
                                    }
                                } else null,

                                onReject = if (message.type == Message.MessageType.APPOINTMENT_REQUEST &&
                                    message.appointmentStatus == Message.AppointmentProposalStatus.PENDING) {
                                    { msgId, reason ->
                                        chatViewModel.respondToAppointmentRequest(
                                            messageId = msgId,
                                            clientName = userName,
                                            date = message.appointmentDate ?: "",
                                            time = message.appointmentTime ?: "",
                                            accepted = false,
                                            rejectionReason = reason
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                } // end LazyColumn

                // Barra de entrada de mensajes
                MessageInputBar(
                            messageText = messageText,
                            onMessageTextChange = { messageText = it; chatViewModel.setTypingStatus(it.isNotEmpty()) },
                            onSendMessage = {
                                if (messageText.isNotBlank()) {
                                    chatViewModel.sendMessage(messageText)
                                    messageText = ""
                                    coroutineScope.launch { listState.animateScrollToItem(0) }
                                }
                            },
                            onAttachClick = { showAttachMenu = !showAttachMenu },
                            onCameraClick = {
                                // Verificar si ya tiene permisos de cámara
                                if (androidx.core.content.ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                ) {
                                    // Ya tiene permiso, abrir cámara directamente
                                    cameraLauncher.launch(tempPhotoUri)
                                } else {
                                    // Solicitar permisos
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            onMicClick = {
                                if (isRecording) {
                                    // Ya está grabando, detener y enviar
                                    stopRecordingAndSend()
                                } else {
                                    // Verificar permisos y empezar a grabar
                                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                    ) {

                                        // Ya tiene permiso, iniciar grabación directamente
                                        startRecording()
                                    } else {
                                        // Solicitar permisos
                                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            onCancelAudio = {
                                cancelRecording()
                            },
                            isRecording = isRecording,
                            recordingTime = recordingTime
                        )
                    }

                    // Menú flotante de adjuntos - FUERA del flujo normal
                    AnimatedVisibility(
                        visible = showAttachMenu,
                        enter = scaleIn(
                            animationSpec = tween(300),
                            transformOrigin = TransformOrigin(0f, 1f)
                        ) + fadeIn(tween(200)),
                        exit = scaleOut(
                            animationSpec = tween(200),
                            transformOrigin = TransformOrigin(0f, 1f)
                        ) + fadeOut(tween(200)),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 16.dp, start = 5.dp)
                    ) {
                        AttachmentOptionsMenu(
                            serviceType = currentServiceType,
                            onDismiss = { showAttachMenu = false },
                            onImageClick = {
                                showAttachMenu = false
                                galleryLauncher.launch("image/*")
                            },
                            onCameraClick = {
                                showAttachMenu = false
                                // Verificar si ya tiene permisos de cámara
                                if (androidx.core.content.ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                ) {
                                    // Ya tiene permiso, abrir cámara directamente
                                    cameraLauncher.launch(tempPhotoUri)
                                } else {
                                    // Solicitar permisos
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            onLocationClick = {
                                showAttachMenu = false
                                // Verificar permisos de ubicacion
                                val hasFineLocation =
                                    androidx.core.content.ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                val hasCoarseLocation =
                                    androidx.core.content.ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                if (hasFineLocation || hasCoarseLocation) {
                                    // Obtener ubicacion actual
                                    val fusedLocationClient =
                                        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(
                                            context
                                        )
                                    try {
                                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                            if (location != null) {

                                                chatViewModel.sendLocation(
                                                    location.latitude,
                                                    location.longitude
                                                )
                                                coroutineScope.launch {
                                                    listState.animateScrollToItem(0)
                                                }
                                            }
                                        }
                                    } catch (e: SecurityException) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Error al obtener ubicación",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    // Solicitar permisos
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            onDocumentClick = {
                                showAttachMenu = false
                                showBudgetSheet = true
                            },
                            onScheduleVisitClick = {
                                showAttachMenu = false
                                pendingAppointmentType = "TECHNICAL_VISIT"
                                showSendCalendarDialog = true
                            },
                            onScheduleLocalClick = {
                                showAttachMenu = false
                                pendingAppointmentType = "LOCAL_APPOINTMENT"
                                showSendCalendarDialog = true
                            }

                        )
                    }
                }

                // Dialog para enviar calendario de disponibilidad
                if (showSendCalendarDialog) {
                    SendCalendarDialog(
                        providerId = providerId,
                        onDismiss = { showSendCalendarDialog = false },
                        hasPhysicalLocation = provider?.hasPhysicalLocation ?: false,
                        tieneEmpresa = provider?.hasCompanyProfile ?: false,
                        initialAppointmentType = pendingAppointmentType,
                        showTypePicker = false,
                        onSend = { startDate, endDate, availabilityJson, bookedSlotsJson, appointmentType, providerAddress, serviceCategory ->
                            chatViewModel.sendCalendarInvite(
                                startDate = startDate,
                                endDate = endDate,
                                availabilityJson = availabilityJson,
                                bookedSlotsJson = bookedSlotsJson,
                                appointmentType = appointmentType,
                                providerAddress = providerAddress,
                                serviceCategory = serviceCategory
                            )
                            showSendCalendarDialog = false
                            coroutineScope.launch { listState.animateScrollToItem(0) }
                        }
                    )
                }

                // Sheet de presupuesto en el chat
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && showBudgetSheet) {
                    com.example.myapplication.prestador.ui.presupuesto.BudgetChatSheet(
                        userId = userId,
                        userName = userName,
                        providerId = effectiveProviderId,
                        initialClientAddress = budgetRequestMessage?.budgetRequestClientAddress,
                        onDismiss = { showBudgetSheet = false; budgetRequestMessage = null }
                    )
                }

                // Vista previa de presupuesto al hacer clic en "Ver presupuesto"
                presupuestoMsgToView?.let { msg ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        fun parseItems(json: String?) = if (json.isNullOrBlank()) emptyList() else
                            json.split("|").mapNotNull { s ->
                                val p = s.split(";")
                                if (p.size >= 4) BudgetItem(
                                    id = 0L, code = p[0], description = p[1],
                                    quantity = p[2].toIntOrNull() ?: 1,
                                    unitPrice = p[3].toDoubleOrNull() ?: 0.0,
                                    taxPercentage = p.getOrNull(4)?.toDoubleOrNull() ?: 0.0,
                                    discountPercentage = p.getOrNull(5)?.toDoubleOrNull() ?: 0.0
                                ) else null
                            }

                        fun parseServices(json: String?) =
                            if (json.isNullOrBlank()) emptyList() else
                                json.split("|").mapNotNull { s ->
                                    val p = s.split(";")
                                    if (p.size >= 2) BudgetService(
                                        id = 0L,
                                        code = p[0],
                                        description = p[1],
                                        total = p.getOrNull(2)?.toDoubleOrNull() ?: 0.0
                                    ) else null
                                }

                        fun parseFees(json: String?) = if (json.isNullOrBlank()) emptyList() else
                            json.split("|").mapNotNull { s ->
                                val p = s.split(";")
                                if (p.size >= 2) BudgetProfessionalFee(
                                    id = 0L,
                                    code = p[0],
                                    description = p[1],
                                    total = p.getOrNull(2)?.toDoubleOrNull() ?: 0.0
                                ) else null
                            }

                        fun parseMisc(json: String?) = if (json.isNullOrBlank()) emptyList() else
                            json.split("|").mapNotNull { s ->
                                val p = s.split(";")
                                if (p.size >= 2) BudgetMiscExpense(
                                    id = 0L,
                                    description = p[0],
                                    amount = p[1].toDoubleOrNull() ?: 0.0
                                ) else null
                            }

                        fun parseTaxes(json: String?) = if (json.isNullOrBlank()) emptyList() else
                            json.split("|").mapNotNull { s ->
                                val p = s.split(";")
                                if (p.size >= 2) BudgetTax(
                                    id = 0L,
                                    description = p[0],
                                    amount = p[1].toDoubleOrNull() ?: 0.0
                                ) else null
                            }



// 1. Mapea el objeto real directamente a la estructura que espera el Dialog
val prestador = remember(provider, businessEntity) {
    provider
}
                        // Solo mostramos si tenemos el proveedor
                        if (prestador != null) {
                            BudgetPreviewPDFDialog(
                                prestador = prestador,
                                items = parseItems(msg.budgetItemsJson),
                                services = parseServices(msg.budgetServiciosJson),
                                professionalFees = parseFees(msg.budgetHonorariosJson),
                                miscExpenses = parseMisc(msg.budgetGastosJson),
                                taxes = parseTaxes(msg.budgetImpuestosJson),
                                grandTotal = msg.budgetTotal ?: 0.0,
                                subtotal = msg.budgetSubtotal ?: 0.0,
                                taxAmount = msg.budgetImpuestos ?: 0.0,
                                discountAmount = 0.0,
                                showSendButton = false,
                                providerName = providerDisplayName,
                                providerAddress = providerDisplayAddress,
                                isProfessional = currentServiceType == com.example.myapplication.prestador.data.model.ServiceType.PROFESSIONAL,
                                presupuestoNumero = msg.budgetNumero ?: "",
                                tituloTrabajo = msg.budgetTituloTrabajo ?: "",
                                clientName = userName,
                               // category = msg.budgetCategorias ?: "",
                                onDismiss = { presupuestoMsgToView = null },
                                onEnviar = { presupuestoMsgToView = null }
                            )
                        }
                    }
                }

                // Visor de imagen con zoom al tocar una imagen en el chat
                zoomedImageUrl?.let { url ->
                    ImageZoomDialog(
                        imageUrl = url,
                        onDismiss = { zoomedImageUrl = null }
                    )
                }
            }
        }
