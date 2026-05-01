package com.example.myapplication.presentation.client

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.model.AddressClient
import com.example.myapplication.data.model.BranchClient
import com.example.myapplication.data.model.CompanyClient
import com.example.myapplication.data.model.RepresentativeClient
import com.example.myapplication.presentation.profile.ProfileUiState
import com.example.myapplication.presentation.profile.ProfileViewModel
import com.example.myapplication.presentation.components.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

// --- CONSTANTES DE ESTILO LOCALES ---
val GeminiAccentLocal = Color(0xFFA78BFA)
// BentoDarkGlassBackgroundLocal eliminado por no usarse

/**
 * Convierte un string de foto (URL, content://, base64 raw) al modelo correcto para Coil.
 * - URLs https:// y content:// se usan tal cual.
 * - Strings base64 raw se decodifican a ByteArray (Coil los muestra nativamente).
 * - Strings vacíos o nulos devuelven null (Coil mostrará el error/placeholder).
 */
@Composable
fun rememberImageModel(photoString: String?): Any? {
    return remember(photoString) {
        when {
            photoString.isNullOrBlank() -> null
            photoString.startsWith("http") || photoString.startsWith("content://") || photoString.startsWith("file://") -> photoString
            photoString.startsWith("data:image") -> {
                try {
                    val b64 = photoString.substringAfter("base64,")
                    Base64.decode(b64, Base64.DEFAULT)
                } catch (e: Exception) { null }
            }
            photoString.length > 100 -> {
                // Raw base64 (JPEG starts with /9j/, PNG starts with iVBOR)
                try {
                    Base64.decode(photoString, Base64.DEFAULT)
                } catch (e: Exception) { photoString }
            }
            else -> photoString
        }
    }
}

// -- MODELO PARA ANIMACIÓN DE ÓVALO --
data class ActiveProfileInfo(
    val id: Int,
    val photo: String?,
    val title: String,
    val subtitle: String
)

// -- EDITMODE ATÓMICO --
sealed class EditMode {
    object None : EditMode()
    data class BranchAddress(val company: CompanyClient, val branch: BranchClient, val address: AddressClient) : EditMode()
    data class Representative(val company: CompanyClient, val branch: BranchClient, val representative: RepresentativeClient?) : EditMode()
    data class PersonalAddress(val address: AddressClient?) : EditMode()
    data class Company(val company: CompanyClient) : EditMode()
    data class Branch(val company: CompanyClient, val branch: BranchClient?) : EditMode()
    data class Email(val initialValue: String, val index: Int?) : EditMode()
    data class Phone(val initialValue: String, val index: Int?) : EditMode()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    beViewModel: BeBrainViewModel = hiltViewModel()
) {
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var editMode by remember { mutableStateOf<EditMode>(EditMode.None) }
    val currentUser = userState

    // Estados para diálogos de confirmación
    var showConfirmDelete by remember { mutableStateOf(false) }
    var pendingDeleteAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var deleteTitle by remember { mutableStateOf("") }
    var deleteMessage by remember { mutableStateOf("") }

    val requestDelete: (String, String, () -> Unit) -> Unit = { title, msg, action ->
        deleteTitle = title
        deleteMessage = msg
        pendingDeleteAction = action
        showConfirmDelete = true
    }

    LaunchedEffect(Unit) { beViewModel.setHUDContext(HUDContext.PROFILE) }

    val profileBeActions by viewModel.beActions.collectAsStateWithLifecycle()
    LaunchedEffect(profileBeActions) {
        val hydratedActions = profileBeActions.map { action ->
            action.copy(onClick = { beViewModel.triggerAction(action.id) })
        }
        beViewModel.setCustomActions(hydratedActions)
    }

    // --- GESTIÓN DE SNACKBAR PARA FEEDBACK ---
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.successMessage, uiState.error) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearMessages()
        }
        uiState.error?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Long)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(Unit) {
        beViewModel.actionEvent.collect { actionId ->
            when (actionId) {
                "edit_profile" -> viewModel.setEditMode(true)
                "save_profile" -> viewModel.saveProfile()
                "cancel_edit" -> viewModel.setEditMode(false)
                "add_company" -> { editMode = EditMode.Company(CompanyClient()) }
            }
        }
    }

    val userAvatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.updateProfilePhoto(it) }
    }
    val userBannerPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.updateBannerPhoto(it) }
    }

    var companyIdForPicker by remember { mutableStateOf<String?>(null) }
    val companyAvatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { newUri ->
            val updatedCompanies = uiState.companies.map {
                if (it.id == companyIdForPicker) it.copy(photoUrl = newUri.toString()) else it
            }
            viewModel.updateCompanies(updatedCompanies)
            companyIdForPicker = null
        }
    }

    if (currentUser == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0E14)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF007AFF))
        }
    } else {
        // =================================================================================
        // --- SECCIÓN: RENDERIZADO DE CONTENIDO PRINCIPAL ---
        // =================================================================================
        PerfilUsuarioContent(
            user = currentUser,
            isEditMode = uiState.isEditMode,
            uiState = uiState,
            // [MODIFICADO] Pull-to-Refresh Inteligente:
            // Solo sincroniza con Firebase si el flag 'isSynced' es falso en el ViewModel.
            // Si está sincronizado, simplemente refresca la vista local ahorrando datos/lecturas.
            onRefresh = { viewModel.refreshData(forceRemote = false) },
            onNavigateBack = onNavigateBack,
            onLogout = { viewModel.logout(); onLogout() },
            onEditRequest = { editMode = it },
            onEditUserBanner = { userBannerPicker.launch("image/*") },
            onEditUserAvatar = { userAvatarPicker.launch("image/*") },
            onEditCompanyBanner = { id -> companyIdForPicker = id },
            onEditCompanyAvatar = { id -> companyIdForPicker = id; companyAvatarPicker.launch("image/*") },
            onDisplayNameChange = { viewModel.onDisplayNameChange(it) },
            onNameChange = { viewModel.onNameChange(it) },
            onLastNameChange = { viewModel.onLastNameChange(it) },
            onPhoneNumberChange = { viewModel.onPhoneNumberChange(it) },
            onBioChange = { viewModel.onBioChange(it) },
            onUpdatePersonalAddresses = { viewModel.updatePersonalAddresses(it) },
            onUpdateCompanies = { viewModel.updateCompanies(it) },
            onUpdateEmails = { viewModel.updateAdditionalEmails(it) },
            onUpdatePhones = { viewModel.updateAdditionalPhones(it) },
            onRequestDelete = requestDelete,
            snackbarHostState = snackbarHostState
        )
    }

    // Diálogo de Confirmación
    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            containerColor = Color(0xFF1A1A24),
            title = { Text(deleteTitle, color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(deleteMessage, color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteAction?.invoke()
                        showConfirmDelete = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // --- MANEJO ÚNICO DE BOTTOM SHEETS ---
    if (editMode != EditMode.None) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { editMode = EditMode.None },
            sheetState = sheetState,
            containerColor = Color.Transparent,
            dragHandle = null,
            tonalElevation = 0.dp,
            contentWindowInsets = { WindowInsets(0) }
        ) {
            when (val mode = editMode) {
                is EditMode.BranchAddress -> {
                    EditAddressSheetContent(
                        address = mode.address,
                        onSave = { updatedAddr ->
                            val updatedBranch = mode.branch.copy(address = updatedAddr)
                            val updatedBranches = mode.company.branches.map { if(it.id == updatedBranch.id) updatedBranch else it }
                            val currentCompanies = uiState.companies.map { if(it.id == mode.company.id) mode.company.copy(branches = updatedBranches) else it }
                            viewModel.updateCompanies(currentCompanies)
                            editMode = EditMode.None
                        },
                        onClose = { editMode = EditMode.None }
                    )
                }
                is EditMode.Representative -> {
                    EditRepresentativeSheetContent(
                        representative = mode.representative,
                        onSave = { updatedRep ->
                            val currentReps = mode.branch.representatives.toMutableList()
                            val idx = currentReps.indexOfFirst { it.id == updatedRep.id }
                            if (idx != -1) currentReps[idx] = updatedRep else currentReps.add(updatedRep)
                            val updatedBranch = mode.branch.copy(representatives = currentReps)
                            val updatedBranches = mode.company.branches.map { if(it.id == updatedBranch.id) updatedBranch else it }
                            val currentCompanies = uiState.companies.map { if(it.id == mode.company.id) mode.company.copy(branches = updatedBranches) else it }
                            viewModel.updateCompanies(currentCompanies)
                            editMode = EditMode.None
                        },
                        onClose = { editMode = EditMode.None },
                        onDelete = if (mode.representative != null) {
                            {
                                requestDelete("Eliminar Miembro", "¿Estás seguro que deseas eliminar a ${mode.representative.nombre} del equipo?") {
                                    val currentReps = mode.branch.representatives.filter { it.id != mode.representative.id }
                                    val updatedBranch = mode.branch.copy(representatives = currentReps)
                                    val updatedBranches = mode.company.branches.map { if(it.id == updatedBranch.id) updatedBranch else it }
                                    val currentCompanies = uiState.companies.map { if(it.id == mode.company.id) mode.company.copy(branches = updatedBranches) else it }
                                    viewModel.updateCompanies(currentCompanies)
                                    editMode = EditMode.None
                                }
                            }
                        } else null
                    )
                }
                is EditMode.PersonalAddress -> {
                    EditAddressSheetContent(
                        address = mode.address ?: AddressClient(),
                        onSave = { updated ->
                            val current = uiState.personalAddresses.toMutableList()
                            val idx = current.indexOfFirst { it.id == updated.id }
                            if (idx != -1) current[idx] = updated else current.add(updated)
                            viewModel.updatePersonalAddresses(current)
                            editMode = EditMode.None
                        },
                        onClose = { editMode = EditMode.None }
                    )
                }
                is EditMode.Company -> {
                    EditCompanySheetContent(
                        company = mode.company,
                        onSave = { updated ->
                            val current = uiState.companies.toMutableList()
                            val idx = current.indexOfFirst { it.id == updated.id }
                            if (idx != -1) current[idx] = updated else current.add(updated)
                            viewModel.updateCompanies(current)
                            editMode = EditMode.None
                        },
                        onClose = { editMode = EditMode.None }
                    )
                }
                is EditMode.Branch -> {
                    EditBranchSheetContent(
                        branch = mode.branch ?: BranchClient(),
                        onSave = { updated ->
                            val current = mode.company.branches.toMutableList()
                            val idx = current.indexOfFirst { it.id == updated.id }
                            if (idx != -1) current[idx] = updated else current.add(updated)
                            val currentCompanies = uiState.companies.map { if(it.id == mode.company.id) mode.company.copy(branches = current) else it }
                            viewModel.updateCompanies(currentCompanies)
                            editMode = EditMode.None
                        },
                        onClose = { editMode = EditMode.None }
                    )
                }
                is EditMode.Email -> {
                    EditSingleContactSheetContent(
                        initialValue = mode.initialValue,
                        title = if (mode.index == null) "Nuevo Email" else "Editar Email",
                        emoji = "📧",
                        label = "Correo Electrónico",
                        onSave = { updated ->
                            val current = uiState.additionalEmails.toMutableList()
                            if (mode.index != null) current[mode.index] = updated else current.add(updated)
                            viewModel.updateAdditionalEmails(current)
                            editMode = EditMode.None
                        },
                        onClose = { editMode = EditMode.None }
                    )
                }
                is EditMode.Phone -> {
                    EditSingleContactSheetContent(
                        initialValue = mode.initialValue,
                        title = if (mode.index == null) "Nuevo Teléfono" else "Editar Teléfono",
                        emoji = "📱",
                        label = "Número de Teléfono",
                        onSave = { updated ->
                            val current = uiState.additionalPhones.toMutableList()
                            if (mode.index != null) current[mode.index] = updated else current.add(updated)
                            viewModel.updateAdditionalPhones(current)
                            editMode = EditMode.None
                        },
                        onClose = { editMode = EditMode.None }
                    )
                }
                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PerfilUsuarioContent(
    user: UserEntity,
    isEditMode: Boolean,
    uiState: ProfileUiState,
    onRefresh: () -> Unit, // [NUEVO] Callback para actualización forzada
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onEditRequest: (EditMode) -> Unit,
    onEditUserBanner: () -> Unit,
    onEditUserAvatar: () -> Unit,
    onEditCompanyBanner: (String) -> Unit,
    onEditCompanyAvatar: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onUpdatePersonalAddresses: (List<AddressClient>) -> Unit,
    onUpdateCompanies: (List<CompanyClient>) -> Unit,
    onUpdateEmails: (List<String>) -> Unit,
    onUpdatePhones: (List<String>) -> Unit,
    onRequestDelete: (String, String, () -> Unit) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val companiesList = uiState.companies
    val totalPages = 1 + companiesList.size
    val pagerState = rememberPagerState(pageCount = { totalPages })
    val coroutineScope = rememberCoroutineScope()

    val userPhoto = uiState.photoUrl

    // Sincronización de UI post-guardado
    LaunchedEffect(uiState.isEditMode) {
        if (!uiState.isEditMode) {
            if (pagerState.currentPage >= totalPages) {
                coroutineScope.launch { pagerState.animateScrollToPage(0) }
            }
        }
    }

    // Información del perfil activo para el Header
    val currentActiveInfo = remember(pagerState.currentPage, companiesList, uiState.displayName, userPhoto) {
        if (pagerState.currentPage > 0 && pagerState.currentPage <= companiesList.size) {
            val company = companiesList[pagerState.currentPage - 1]
            ActiveProfileInfo(
                id = pagerState.currentPage,
                photo = company.photoUrl,
                title = company.name.ifEmpty { "Mi Empresa" },
                subtitle = company.razonSocial
            )
        } else {
            ActiveProfileInfo(
                id = 0,
                photo = userPhoto,
                title = uiState.displayName.ifEmpty { "${uiState.name} ${uiState.lastName}" },
                subtitle = uiState.email
            )
        }
    }

    // Lógica de burbujas (Perfiles inactivos)
    val displayBubbles = remember(pagerState.currentPage, companiesList, userPhoto) {
        val allPerfiles = mutableListOf<Pair<Int, String?>>()
        allPerfiles.add(0 to userPhoto)
        companiesList.forEachIndexed { index, company -> allPerfiles.add((index + 1) to company.photoUrl) }

        val activeIdx = pagerState.currentPage
        val result = mutableListOf<Pair<Int, String?>>()
        for (i in 1 until allPerfiles.size) {
            val idx = (activeIdx + i) % allPerfiles.size
            result.add(allPerfiles[idx])
        }
        result
    }

    val currentBannerUrl = if (pagerState.currentPage > 0 && pagerState.currentPage <= companiesList.size) {
        companiesList[pagerState.currentPage - 1].bannerImageUrl
    } else {
        uiState.coverPhotoUrl
    }

    // Dimensiones dinámicas para el Header colapsable
    val headerMaxHeight = 330.dp
    val headerMinHeight = 140.dp
    val density = LocalDensity.current
    val maxScroll = with(density) { (headerMaxHeight - headerMinHeight).toPx() }
    
    // [CORRECCIÓN] Uso de derivedStateOf para optimizar lecturas de scrollState que cambia frecuentemente
    val collapseFraction by remember {
        derivedStateOf { (scrollState.value.toFloat() / maxScroll).coerceIn(0f, 1f) }
    }

    val headerHeight by animateDpAsState(targetValue = headerMaxHeight - (headerMaxHeight - headerMinHeight) * collapseFraction)
    val avatarSize by animateDpAsState(targetValue = 90.dp - (35.dp * collapseFraction))

    val pullToRefreshState = rememberPullToRefreshState() // [CORRECCIÓN] Estado para PullToRefresh

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        // --- SECCIÓN: PULL TO REFRESH (Estilo Instagram) ---
        // Envuelve todo el contenido para permitir la actualización al deslizar hacia abajo
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = uiState.isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            // Indicador de carga estilizado para el tema oscuro
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = uiState.isLoading,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = Color(0xFF1A1A24),
                    color = GeminiAccentLocal
                )
            }
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0F))) {

                // --- CONTENIDO SCROLLABLE ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Spacer(modifier = Modifier.height(headerMaxHeight))

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = screenHeight),
                        verticalAlignment = Alignment.Top
                    ) { page ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (page == 0) PersonalM3Section(
                                user = user,
                                isEditMode = isEditMode,
                                uiState = uiState,
                                onEditRequest = onEditRequest,
                                onDisplayNameChange = onDisplayNameChange,
                                onNameChange = onNameChange,
                                onLastNameChange = onLastNameChange,
                                onPhoneNumberChange = onPhoneNumberChange,
                                onBioChange = onBioChange,
                                onUpdatePersonalAddresses = onUpdatePersonalAddresses,
                                onUpdateEmails = onUpdateEmails,
                                onUpdatePhones = onUpdatePhones,
                                onRequestDelete = onRequestDelete
                            )
                            else if (page <= companiesList.size) BusinessM3Section(
                                company = companiesList[page - 1],
                                isEditMode = isEditMode,
                                uiState = uiState,
                                onEditRequest = onEditRequest,
                                onUpdateCompanies = onUpdateCompanies,
                                onRequestDelete = onRequestDelete
                            )
                        }
                    }

                    if (!isEditMode) {
                        Spacer(modifier = Modifier.height(24.dp))
                        BentoActionButtonLocal(
                            text = "Cerrar Sesión",
                            emoji = "🚪",
                            color = Color(0xFFEF4444),
                            modifier = Modifier.padding(horizontal = 24.dp),
                            onClick = onLogout
                        )
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }

                // --- HEADER DINÁMICO ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(headerHeight)
                        .zIndex(10f)
                ) {
                    AsyncImage(
                        model = currentBannerUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = isEditMode) {
                                if (pagerState.currentPage == 0) onEditUserBanner()
                                else onEditCompanyBanner(companiesList[pagerState.currentPage - 1].id)
                            },
                        error = painterResource(id = R.drawable.ic_launcher_background)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.8f))
                                )
                            )
                    )

                    // Icono Eliminar Empresa en Banner
                    if (isEditMode && pagerState.currentPage > 0 && pagerState.currentPage <= companiesList.size) {
                        val currentComp = companiesList[pagerState.currentPage - 1]
                        IconButton(
                            onClick = {
                                onRequestDelete("Eliminar Empresa", "¿Deseas eliminar '${currentComp.name}' y todas sus sucursales?") {
                                    val newList = uiState.companies.filter { it.id != currentComp.id }
                                    onUpdateCompanies(newList)
                                    coroutineScope.launch { pagerState.animateScrollToPage(0) }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 45.dp, end = 12.dp)
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.8f))
                        }
                    }

                    // --- CONTENIDO DEL HEADER (ORGANIZADO VERTICALMENTE) ---
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        // 1. INFO PERFIL ACTIVO
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(avatarSize)
                                    .shadow(12.dp, CircleShape)
                                    .clip(CircleShape)
                                    .border(2.5.dp, Color.White, CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .clickable(enabled = isEditMode) {
                                        if (pagerState.currentPage == 0) onEditUserAvatar()
                                        else onEditCompanyAvatar(companiesList[pagerState.currentPage - 1].id)
                                    }
                            ) {
                                AsyncImage(
                                    model = rememberImageModel(currentActiveInfo.photo),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    error = painterResource(id = R.drawable.ic_launcher_foreground)
                                )
                            }

                            Spacer(Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = currentActiveInfo.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentActiveInfo.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // 2. BURBUJAS DE PERFILES
                        AnimatedVisibility(
                            visible = collapseFraction < 0.5f,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(end = 20.dp, top = 16.dp)
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(displayBubbles, key = { it.first }) { item ->
                                    BubbleItem(
                                        photoUrl = item.second,
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(item.first)
                                            }
                                        },
                                        modifier = Modifier.size(42.dp).animateItem()
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(top = 45.dp, start = 12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                }
            }
        }
    }
}


@Composable
fun BubbleItem(photoUrl: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val imageModel = rememberImageModel(photoUrl)
    Box(
        modifier = modifier
            .clip(CircleShape)
            .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = imageModel,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            error = painterResource(id = R.drawable.ic_launcher_foreground)
        )
    }
}

// =================================================================================
// --- SECCIONES REESTRUCTURADAS (ESTILO PRESTADOR) ---
// =================================================================================

@Composable
fun PersonalM3Section(
    user: UserEntity,
    isEditMode: Boolean,
    uiState: ProfileUiState,
    onEditRequest: (EditMode) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onUpdatePersonalAddresses: (List<AddressClient>) -> Unit,
    onUpdateEmails: (List<String>) -> Unit,
    onUpdatePhones: (List<String>) -> Unit,
    onRequestDelete: (String, String, () -> Unit) -> Unit
) {
    val context = LocalContext.current
    
    Column {
        // --- TARJETA 1: DATOS PERSONALES ---
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF16161D)).padding(vertical = 24.dp)) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text("DATOS PERSONALES", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

                ProfileDataFieldItem(
                    emoji = "🎭", 
                    label = "APODO / NOMBRE PÚBLICO", 
                    value = if (isEditMode) uiState.displayName else user.displayName,
                    isEditMode = isEditMode,
                    onValueChange = onDisplayNameChange
                )
                ProfileDataFieldItem(
                    emoji = "👤", 
                    label = "NOMBRE", 
                    value = if (isEditMode) uiState.name else user.name,
                    isEditMode = isEditMode,
                    onValueChange = onNameChange
                )
                ProfileDataFieldItem(
                    emoji = "👤", 
                    label = "APELLIDO", 
                    value = if (isEditMode) uiState.lastName else user.lastName,
                    isEditMode = isEditMode,
                    onValueChange = onLastNameChange
                )
                
                // --- SECCIÓN DE EMAILS ---
                val primaryEmail = if (isEditMode) uiState.email else user.email
                ProfileDataFieldItem(
                    emoji = "📧", 
                    label = "CORREO ELECTRÓNICO (PRINCIPAL)", 
                    value = primaryEmail,
                    isEditMode = isEditMode,
                    onValueChange = {},
                    readOnly = true,
                    isGoogleAccount = primaryEmail.endsWith("@gmail.com") || primaryEmail.endsWith("@google.com"),
                    trailingIcon = if (isEditMode) {
                        {
                            IconButton(onClick = { onEditRequest(EditMode.Email("", null)) }) {
                                Icon(Icons.Default.Add, null, tint = GeminiAccentLocal)
                            }
                        }
                    } else null
                )

                // Emails adicionales
                val additionalEmails = if (isEditMode) uiState.additionalEmails else user.additionalEmails
                additionalEmails.forEachIndexed { index, email ->
                    ProfileDataFieldItem(
                        emoji = "📧",
                        label = "EMAIL ADICIONAL",
                        value = email,
                        isEditMode = isEditMode,
                        onValueChange = { /* No se usa aquí, abrimos el sheet */ },
                        readOnly = true,
                        trailingIcon = if (isEditMode) {
                            {
                                Row {
                                    IconButton(onClick = { onEditRequest(EditMode.Email(email, index)) }) {
                                        Icon(Icons.Default.Edit, null, tint = GeminiAccentLocal, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = {
                                        onRequestDelete("Eliminar Email", "¿Deseas eliminar este correo electrónico?") {
                                            val newList = uiState.additionalEmails.toMutableList()
                                            newList.removeAt(index)
                                            onUpdateEmails(newList)
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        } else null
                    )
                }

                // --- SECCIÓN DE TELÉFONOS ---
                ProfileDataFieldItem(
                    emoji = "📱", 
                    label = "TELÉFONO (PRINCIPAL)", 
                    value = if (isEditMode) uiState.phoneNumber else user.phoneNumber,
                    isEditMode = isEditMode,
                    onValueChange = onPhoneNumberChange,
                    trailingIcon = if (isEditMode) {
                        {
                            IconButton(onClick = { onEditRequest(EditMode.Phone("", null)) }) {
                                Icon(Icons.Default.Add, null, tint = GeminiAccentLocal)
                            }
                        }
                    } else null
                )

                // Teléfonos adicionales
                val additionalPhones = if (isEditMode) uiState.additionalPhones else user.additionalPhones
                additionalPhones.forEachIndexed { index, phone ->
                    ProfileDataFieldItem(
                        emoji = "📱",
                        label = "TELÉFONO ADICIONAL",
                        value = phone,
                        isEditMode = isEditMode,
                        onValueChange = { /* No se usa aquí, abrimos el sheet */ },
                        readOnly = true,
                        trailingIcon = if (isEditMode) {
                            {
                                Row {
                                    IconButton(onClick = { onEditRequest(EditMode.Phone(phone, index)) }) {
                                        Icon(Icons.Default.Edit, null, tint = GeminiAccentLocal, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = {
                                        onRequestDelete("Eliminar Teléfono", "¿Deseas eliminar este número de teléfono?") {
                                            val newList = uiState.additionalPhones.toMutableList()
                                            newList.removeAt(index)
                                            onUpdatePhones(newList)
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        } else null
                    )
                }

                ProfileDataFieldItem(
                    emoji = "📝", 
                    label = "BIOGRAFÍA", 
                    value = if (isEditMode) uiState.bio else user.bio,
                    isEditMode = isEditMode,
                    onValueChange = onBioChange
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- TARJETA 2: DIRECCIONES PERSONALES ---
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF16161D)).padding(vertical = 24.dp)) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "MIS DIRECCIONES",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    if (isEditMode) {
                        IconButton(onClick = { onEditRequest(EditMode.PersonalAddress(null)) }) {
                            Icon(Icons.Default.Add, null, tint = GeminiAccentLocal)
                        }
                    }
                }

                val addresses = if (isEditMode) uiState.personalAddresses else user.personalAddresses
                if (addresses.isEmpty()) {
                    Text(
                        "Sin direcciones registradas",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    addresses.forEachIndexed { index, addr ->
                        MapsStyleAddressCardLocal(
                            address = addr,
                            isEditMode = isEditMode,
                            onEdit = { onEditRequest(EditMode.PersonalAddress(addr)) },
                            onDelete = {
                                onRequestDelete("Eliminar Dirección", "¿Deseas eliminar esta dirección?") {
                                    val newList = uiState.personalAddresses.toMutableList()
                                    newList.removeAt(index)
                                    onUpdatePersonalAddresses(newList)
                                }
                            },
                            onMapsClick = {
                                val gmmIntentUri = android.net.Uri.parse("geo:0,0?q=${android.net.Uri.encode(addr.fullString())}")
                                val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                context.startActivity(mapIntent)
                            }
                        )
                        if (index < addresses.lastIndex) Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BusinessM3Section(
    company: CompanyClient,
    isEditMode: Boolean,
    uiState: ProfileUiState,
    onEditRequest: (EditMode) -> Unit,
    onUpdateCompanies: (List<CompanyClient>) -> Unit,
    onRequestDelete: (String, String, () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        val updatedBranches = company.branches.map { branch ->
            branch.copy(galleryImages = branch.galleryImages + uris.map { it.toString() })
        }
        val currentCompanies = uiState.companies.map { if (it.id == company.id) company.copy(branches = updatedBranches) else it }
        onUpdateCompanies(currentCompanies)
    }

    Column {
        // --- TARJETA 1: DATOS DEL NEGOCIO ---
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF16161D)).padding(vertical = 24.dp)) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text("DATOS DEL NEGOCIO", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

                ProfileDataFieldItem(
                    emoji = "🏢", 
                    label = "NOMBRE COMERCIAL", 
                    value = company.name,
                    isEditMode = isEditMode,
                    onValueChange = { newVal ->
                        val updated = uiState.companies.map { if (it.id == company.id) it.copy(name = newVal) else it }
                        onUpdateCompanies(updated)
                    }
                )
                ProfileDataFieldItem(
                    emoji = "🏭", 
                    label = "RAZÓN SOCIAL", 
                    value = company.razonSocial,
                    isEditMode = isEditMode,
                    onValueChange = { newVal ->
                        val updated = uiState.companies.map { if (it.id == company.id) it.copy(razonSocial = newVal) else it }
                        onUpdateCompanies(updated)
                    }
                )
                ProfileDataFieldItem(
                    emoji = "🆔", 
                    label = "CUIT", 
                    value = company.cuit,
                    isEditMode = isEditMode,
                    onValueChange = { newVal ->
                        val updated = uiState.companies.map { if (it.id == company.id) it.copy(cuit = newVal) else it }
                        onUpdateCompanies(updated)
                    }
                )
                ProfileDataFieldItem(
                    emoji = "📧", 
                    label = "EMAIL CORPORATIVO", 
                    value = company.email,
                    isEditMode = isEditMode,
                    onValueChange = { newVal ->
                        val updated = uiState.companies.map { if (it.id == company.id) it.copy(email = newVal) else it }
                        onUpdateCompanies(updated)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- TARJETA 2: SUCURSALES ---
        Column(modifier = Modifier.fillMaxWidth().background(Color.Transparent).padding(vertical = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("SUCURSALES (${company.branches.size})", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                if (isEditMode) {
                    IconButton(onClick = { onEditRequest(EditMode.Branch(company, null)) }) {
                        Icon(Icons.Default.Add, null, tint = GeminiAccentLocal)
                    }
                }
            }

            if (company.branches.isNotEmpty()) {
                val sortedBranches = remember(company.branches) {
                    company.branches.sortedByDescending { it.isMainBranch }
                }
                val branchPagerState = rememberPagerState(pageCount = { sortedBranches.size })

                HorizontalPager(
                    state = branchPagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { branchPage ->
                    val branch = sortedBranches[branchPage]

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF16161D))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            if (branch.isMainBranch) {
                                Surface(color = GeminiAccentLocal, shape = CircleShape, modifier = Modifier.padding(bottom = 4.dp)) {
                                    Text("📍 CASA CENTRAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    if (isEditMode) {
                                        BasicTextField(
                                            value = branch.name,
                                            onValueChange = { newVal: String ->
                                                val updatedBranches = company.branches.map { if (it.id == branch.id) it.copy(name = newVal) else it }
                                                val updatedCompanies = uiState.companies.map { if (it.id == company.id) it.copy(branches = updatedBranches) else it }
                                                onUpdateCompanies(updatedCompanies)
                                            },
                                            textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp),
                                            cursorBrush = SolidColor(GeminiAccentLocal),
                                            decorationBox = { innerTextField ->
                                                Box {
                                                    if (branch.name.isEmpty()) Text("Nombre sucursal", color = Color.Gray, fontSize = 18.sp)
                                                    innerTextField()
                                                }
                                            }
                                        )
                                        
                                        // Texto Casa Central seleccionable en modo edición
                                        Text(
                                            text = if (branch.isMainBranch) "Casa Central seleccionada" else "Marcar como Casa Central",
                                            color = if (branch.isMainBranch) GeminiAccentLocal else Color.Gray,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(top = 4.dp).clickable {
                                                if (!branch.isMainBranch) {
                                                    val updatedBranches = company.branches.map { 
                                                        it.copy(isMainBranch = it.id == branch.id) 
                                                    }
                                                    val updatedCompanies = uiState.companies.map { 
                                                        if (it.id == company.id) it.copy(branches = updatedBranches) else it 
                                                    }
                                                    onUpdateCompanies(updatedCompanies)
                                                }
                                            }
                                        )
                                    } else {
                                        Text(branch.name.ifEmpty { "Sucursal" }, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                
                                if (isEditMode) {
                                    IconButton(onClick = {
                                        onRequestDelete("Eliminar Sucursal", "¿Estás seguro que deseas eliminar la sucursal '${branch.name}'?") {
                                            val current = company.branches.toMutableList()
                                            current.remove(branch)
                                            val updatedCompanies = uiState.companies.map { if (it.id == company.id) it.copy(branches = current) else it }
                                            onUpdateCompanies(updatedCompanies)
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // --- [MODIFICADO] Usando nueva tarjeta estilo Maps Android 16 para sucursales ---
                            MapsStyleAddressCardLocal(
                                address = branch.address,
                                isEditMode = isEditMode,
                                onEdit = { onEditRequest(EditMode.BranchAddress(company, branch, branch.address)) },
                                onDelete = {}, // No se usa borrar individual desde aquí
                                onMapsClick = {
                                    try {
                                        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(branch.address.fullString())}")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        context.startActivity(mapIntent)
                                    } catch (e: Exception) {}
                                },
                                hideDelete = true
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.05f))

                            // Equipo de Trabajo
                            Text("EQUIPO DE TRABAJO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(branch.representatives) { rep ->
                                    Surface(
                                        onClick = { if (isEditMode) onEditRequest(EditMode.Representative(company, branch, rep)) },
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.Black.copy(alpha = 0.2f),
                                        modifier = Modifier.width(200.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            AsyncImage(
                                                model = rep.photoUrl,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.size(40.dp).clip(CircleShape),
                                                error = painterResource(id = R.drawable.ic_launcher_foreground)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text("${rep.nombre} ${rep.apellido}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text(rep.cargo, fontSize = 11.sp, color = GeminiAccentLocal)
                                            }
                                        }
                                    }
                                }
                                if (isEditMode) {
                                    item {
                                        IconButton(onClick = { onEditRequest(EditMode.Representative(company, branch, null)) }) {
                                            Icon(Icons.Default.PersonAdd, null, tint = GeminiAccentLocal)
                                        }
                                    }
                                }
                            }

                            // Galería
                            Row(modifier = Modifier.padding(top = 20.dp, bottom = 8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("GALERÍA DE ESTA SEDE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                if (isEditMode) {
                                    IconButton(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.size(20.dp)) {
                                        Icon(Icons.Default.AddPhotoAlternate, null, tint = GeminiAccentLocal, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            
                            if (branch.galleryImages.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(branch.galleryImages) { imgUrl ->
                                        Box {
                                            AsyncImage(
                                                model = imgUrl,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                                error = painterResource(id = R.drawable.ic_launcher_background)
                                            )
                                            if (isEditMode) {
                                                IconButton(
                                                    onClick = {
                                                        onRequestDelete("Eliminar Imagen", "¿Deseas quitar esta imagen de la galería?") {
                                                            val updatedImages = branch.galleryImages.filter { it != imgUrl }
                                                            val updatedBranch = branch.copy(galleryImages = updatedImages)
                                                            val updatedBranches = company.branches.map { if (it.id == branch.id) updatedBranch else it }
                                                            val updatedCompanies = uiState.companies.map { if (it.id == company.id) it.copy(branches = updatedBranches) else it }
                                                            onUpdateCompanies(updatedCompanies)
                                                        }
                                                    },
                                                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                                ) {
                                                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (isEditMode) {
                                Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Text("Todavía no hay imágenes", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                if (company.branches.size > 1) {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.Center) {
                        repeat(company.branches.size) { iteration ->
                            val color = if (branchPagerState.currentPage == iteration) GeminiAccentLocal else Color.Gray.copy(alpha = 0.5f)
                            Box(modifier = Modifier.padding(3.dp).size(6.dp).clip(CircleShape).background(color))
                        }
                    }
                }
            }
        }
    }
}

// =================================================================================
// --- COMPONENTES VISUALES BASE (DARK VERSION) ---
// =================================================================================

@Composable
fun ProfileDataFieldItem(
    emoji: String, 
    label: String, 
    value: String,
    isEditMode: Boolean = false,
    onValueChange: (String) -> Unit = {},
    readOnly: Boolean = false,
    isGoogleAccount: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    if (!isEditMode && value.isEmpty()) return

    Row(modifier = Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
            if (isGoogleAccount) {
                Icon(
                    painter = painterResource(R.drawable.ic_google_logo),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(emoji, fontSize = 18.sp)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
            if (isEditMode && !readOnly) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    cursorBrush = SolidColor(GeminiAccentLocal),
                    decorationBox = { innerTextField ->
                        Column {
                            Box {
                                if (value.isEmpty()) Text("Completar...", color = Color.Gray, fontSize = 14.sp)
                                innerTextField()
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))
                        }
                    }
                )
            } else {
                Text(value.ifEmpty { "No especificado" }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        if (trailingIcon != null) {
            trailingIcon()
        }
    }
}

/**
 * [NUEVO] Tarjeta de Dirección estilo Google Maps para Perfil Usuario (Android 16 Concept)
 */
@Composable
fun MapsStyleAddressCardLocal(
    address: AddressClient,
    isEditMode: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMapsClick: () -> Unit,
    hideDelete: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (isExpanded) 180f else 0f)

    Surface(
        onClick = if (isEditMode) onEdit else { {} },
        color = Color.Black.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val emoji = address.label.split(" ").firstOrNull()?.let { 
                    if (it.isNotEmpty() && it[0].isSurrogate()) it else "📍" 
                } ?: "📍"
                
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(GeminiAccentLocal.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 22.sp)
                }
                
                Spacer(Modifier.width(14.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    if (address.label.isNotBlank()) {
                        Text(address.label.uppercase(), style = MaterialTheme.typography.labelSmall, color = GeminiAccentLocal, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "${address.calle} ${address.numero}",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Text(
                        text = address.localidad,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                if (isEditMode && !hideDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                    }
                } else {
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.rotate(rotation)
                        )
                    }
                }
            }

            if (isExpanded && !isEditMode) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(0.05f))
                Spacer(Modifier.height(16.dp))
                
                DetailRowLocal("País", address.pais.ifEmpty { "Argentina" })
                DetailRowLocal("Ciudad", address.localidad)
                DetailRowLocal("Provincia", address.provincia)
                DetailRowLocal("Cód. Postal", address.codigoPostal)
                
                // [CORRECCIÓN] Se eliminan checks nulos en Double (siempre son true)
                // Se verifica que no sean el valor por defecto si se desea filtrar
                if (address.latitude != 0.0 || address.longitude != 0.0) {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().background(Color.White.copy(0.03f), RoundedCornerShape(8.dp)).padding(10.dp)
                    ) {
                        Column {
                            Text("COORDENADAS GPS", fontSize = 9.sp, fontWeight = FontWeight.Black, color = GeminiAccentLocal, letterSpacing = 1.sp)
                            Text(
                                text = "LAT: ${address.latitude} | LON: ${address.longitude}",
                                fontSize = 11.sp,
                                color = Color.White.copy(0.6f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onMapsClick,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.05f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(0.1f))
                ) {
                    Icon(Icons.Default.Map, null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("ABRIR EN MAPS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun DetailRowLocal(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun BentoActionButtonLocal(
    text: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    color: Color = GeminiAccentLocal,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(58.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF16161D),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            if (emoji != null) {
                Text(text = emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Preview(showBackground = true, name = "Perfil Usuario - Modo Lectura")
@Composable
fun PerfilUsuarioScreenPreview() {
    val sampleUser = UserEntity(
        id = "user123",
        email = "max@maverick.com",
        name = "Maximiliano",
        lastName = "Nanterne",
        displayName = "Maverick",
        phoneNumber = "3811234567",
        bio = "Tech Lead & Software Engineer",
        photoUrl = "https://picsum.photos/seed/max/200/200",
        bannerImageUrl = "https://picsum.photos/seed/maverick_banner/800/400",
        personalAddresses = listOf(
            AddressClient(calle = "9 de Julio", numero = "123", localidad = "Tucumán", label = "🏠 Casa")
        ),
        hasCompanyProfile = true,
        companies = listOf(
            CompanyClient(
                name = "Maverick Tech",
                razonSocial = "Maverick Soluciones S.A.",
                cuit = "30-12345678-9",
                email = "contacto@maverick.com",
                branches = listOf(
                    BranchClient(
                        name = "Casa Central",
                        isMainBranch = true,
                        address = AddressClient(calle = "Lavalle", numero = "450", localidad = "Tucumán"),
                        representatives = listOf(
                            RepresentativeClient(nombre = "Ana", apellido = "Gómez", cargo = "Soporte", photoUrl = "https://picsum.photos/seed/ana/100/100")
                        ),
                        galleryImages = listOf("https://picsum.photos/seed/office1/400/300")
                    )
                )
            )
        )
    )

    val sampleUiState = ProfileUiState(
        displayName = "Maverick",
        name = "Maximiliano",
        lastName = "Nanterne",
        email = "max@maverick.com",
        phoneNumber = "3811234567",
        bio = "Tech Lead & Software Engineer",
        photoUrl = "https://picsum.photos/seed/max/200/200",
        coverPhotoUrl = "https://picsum.photos/seed/maverick_banner/800/400",
        personalAddresses = sampleUser.personalAddresses,
        companies = sampleUser.companies,
        isEditMode = false
    )

    MyApplicationTheme {
        PerfilUsuarioContent(
            user = sampleUser,
            isEditMode = sampleUiState.isEditMode,
            uiState = sampleUiState,
            onRefresh = {},
            onNavigateBack = {},
            onLogout = {},
            onEditRequest = {},
            onEditUserBanner = {},
            onEditUserAvatar = {},
            onEditCompanyBanner = {},
            onEditCompanyAvatar = {},
            onDisplayNameChange = {},
            onNameChange = {},
            onLastNameChange = {},
            onPhoneNumberChange = {},
            onBioChange = {},
            onUpdatePersonalAddresses = {},
            onUpdateCompanies = {},
            onUpdateEmails = {},
            onUpdatePhones = {},
            onRequestDelete = { _, _, _ -> },
            snackbarHostState = SnackbarHostState()
        )
    }
}
