
/**
package com.example.myapplication.presentation.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
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
import com.example.myapplication.presentation.client.BeBrainViewModel
import com.example.myapplication.presentation.client.HUDContext
import com.example.myapplication.presentation.client.PerfilUsuarioContent
import com.example.myapplication.presentation.profile.ProfileUiState
import com.example.myapplication.presentation.profile.ProfileViewModel
import com.example.myapplication.presentation.components.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

// --- CONSTANTES DE ESTILO LOCALES ---
val GeminiAccentLocal = Color(0xFFA78BFA)
val BentoDarkGlassBackgroundLocal = Color(0xFF12121A).copy(alpha = 0.65f)

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

    // Estado para Diálogo de Confirmación
    var showDeleteConfirm by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }

    LaunchedEffect(currentUser) { beViewModel.updateProfile(currentUser) }
    LaunchedEffect(Unit) { beViewModel.setHUDContext(HUDContext.PROFILE) }

    val profileBeActions by viewModel.beActions.collectAsStateWithLifecycle()
    LaunchedEffect(profileBeActions) {
        val hydratedActions = profileBeActions.map { action ->
            action.copy(onClick = { beViewModel.triggerAction(action.id) })
        }
        beViewModel.setCustomActions(hydratedActions)
    }

    LaunchedEffect(Unit) {
        beViewModel.actionEvent.collect { actionId ->
            when (actionId) {
                "edit_profile" -> viewModel.setEditMode(true)
                "save_profile" -> viewModel.saveProfile()
                "cancel_edit" -> viewModel.setEditMode(false)
                "add_company" -> { editMode = EditMode.Company(CompanyClient()) }
                "add_location" -> { editMode = EditMode.PersonalAddress(null) }
            }
        }
    }

    // --- DIÁLOGO DE CONFIRMACIÓN ÚNICO ---
    showDeleteConfirm?.let { (message, onConfirm) ->
        ConfirmationDialogLocal(
            message = message,
            onConfirm = {
                onConfirm()
                showDeleteConfirm = null
            },
            onDismiss = { showDeleteConfirm = null }
        )
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
        PerfilUsuarioContent(
            user = currentUser,
            isEditMode = uiState.isEditMode,
            uiState = uiState,
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
            onShowDeleteConfirm = { msg, action -> showDeleteConfirm = msg to action }
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
                        onDelete = {
                            val currentReps = mode.branch.representatives.filter { it.id != mode.representative?.id }
                            val updatedBranch = mode.branch.copy(representatives = currentReps)
                            val updatedBranches = mode.company.branches.map { if(it.id == updatedBranch.id) updatedBranch else it }
                            val currentCompanies = uiState.companies.map { if(it.id == mode.company.id) mode.company.copy(branches = updatedBranches) else it }
                            viewModel.updateCompanies(currentCompanies)
                            editMode = EditMode.None
                        },
                        onClose = { editMode = EditMode.None }
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
                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PerfilUsuarioContent(
    user: UserEntity,
    isEditMode: Boolean,
    uiState: ProfileUiState,
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
    onShowDeleteConfirm: (String, () -> Unit) -> Unit
) {
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val companiesList = if (isEditMode) uiState.companies else user.companies
    val totalPages = 1 + companiesList.size
    val pagerState = rememberPagerState(pageCount = { totalPages })
    val coroutineScope = rememberCoroutineScope()

    val userPhoto = if (isEditMode) uiState.photoUrl else user.photoUrl

    // Información del perfil activo para el Header
    val currentActiveInfo = remember(pagerState.currentPage, companiesList, user, uiState.displayName, userPhoto) {
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
                title = if (isEditMode) uiState.displayName.ifEmpty { "${uiState.name} ${uiState.lastName}" } else user.fullName,
                subtitle = if (isEditMode) uiState.email else user.email
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
        if (isEditMode) uiState.coverPhotoUrl else user.bannerImageUrl
    }

    // Dimensiones dinámicas para el Header colapsable
    val headerMaxHeight = 330.dp
    val headerMinHeight = 140.dp
    val density = LocalDensity.current
    val maxScroll = with(density) { (headerMaxHeight - headerMinHeight).toPx() }
    val collapseFraction = (scrollState.value.toFloat() / maxScroll).coerceIn(0f, 1f)

    val headerHeight by animateDpAsState(targetValue = headerMaxHeight - (headerMaxHeight - headerMinHeight) * collapseFraction)
    val avatarSize by animateDpAsState(targetValue = 90.dp - (35.dp * collapseFraction))

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
                        onShowDeleteConfirm = onShowDeleteConfirm
                    )
                    else if (page <= companiesList.size) BusinessM3Section(
                        company = companiesList[page - 1],
                        isEditMode = isEditMode,
                        uiState = uiState,
                        onEditRequest = onEditRequest,
                        onUpdateCompanies = onUpdateCompanies,
                        onShowDeleteConfirm = onShowDeleteConfirm
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

            // Botón Eliminar Empresa (Solo en modo edición y si estamos en una página de empresa)
            if (isEditMode && pagerState.currentPage > 0) {
                IconButton(
                    onClick = {
                        val target = companiesList[pagerState.currentPage - 1]
                        onShowDeleteConfirm("¿Estás seguro de que deseas eliminar la empresa '${target.name}'?") {
                            val updated = uiState.companies.filter { it.id != target.id }
                            onUpdateCompanies(updated)
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        }
                    },
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 45.dp, end = 12.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.8f))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )

            // --- CONTENIDO DEL HEADER ---
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
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
                            model = currentActiveInfo.photo,
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

@Composable
fun BubbleItem(photoUrl: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            error = painterResource(id = R.drawable.ic_launcher_foreground)
        )
    }
}

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
    onShowDeleteConfirm: (String, () -> Unit) -> Unit
) {
    val context = LocalContext.current

    Column {
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

                val emailValue = if (isEditMode) uiState.email else user.email
                ProfileDataFieldItem(
                    emoji = "📧",
                    label = "CORREO ELECTRÓNICO",
                    value = emailValue,
                    isEditMode = isEditMode,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (emailValue.contains("@gmail.com", ignoreCase = true)) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_google_logo), // Necesitarías este recurso o un icono similar
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            if (isEditMode) {
                                Spacer(Modifier.width(8.dp))
                                IconButton(onClick = { /* Lógica para agregar otro correo */ }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Add, null, tint = GeminiAccentLocal)
                                }
                            }
                        }
                    }
                )

                ProfileDataFieldItem(
                    emoji = "📱",
                    label = "TELÉFONO",
                    value = if (isEditMode) uiState.phoneNumber else user.phoneNumber,
                    isEditMode = isEditMode,
                    onValueChange = onPhoneNumberChange
                )
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

        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF16161D)).padding(vertical = 24.dp)) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("DIRECCIONES PERSONALES", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    if (isEditMode) {
                        IconButton(onClick = { onEditRequest(EditMode.PersonalAddress(null)) }) {
                            Icon(Icons.Default.Add, null, tint = GeminiAccentLocal)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val addresses = if (isEditMode) uiState.personalAddresses else user.personalAddresses
                if (addresses.isNotEmpty()) {
                    addresses.forEach { addr ->
                        AddressDisplayCardLocal(
                            address = addr,
                            isEditMode = isEditMode,
                            onEdit = { onEditRequest(EditMode.PersonalAddress(addr)) },
                            onDelete = {
                                onShowDeleteConfirm("¿Estás seguro de que deseas eliminar esta dirección?") {
                                    val current = uiState.personalAddresses.toMutableList()
                                    current.remove(addr)
                                    onUpdatePersonalAddresses(current)
                                }
                            },
                            onEmojiClick = {
                                // Lógica para cambiar emoji al tocarlo en edición
                            },
                            onClick = {
                                try {
                                    val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(addr.fullString())}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {}
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                } else if (isEditMode) {
                    EmptyFieldPlaceHolder(text = "No hay direcciones cargadas")
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
    onShowDeleteConfirm: (String, () -> Unit) -> Unit
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

            // Lógica de Reordenamiento de Sucursales (Casa Central Primero)
            val sortedBranches = remember(company.branches) {
                company.branches.sortedByDescending { it.isMainBranch }
            }

            if (sortedBranches.isNotEmpty()) {
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
                                Surface(color = GeminiAccentLocal, shape = CircleShape, modifier = Modifier.padding(bottom = 12.dp)) {
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

                                        // Selector de Casa Central
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 4.dp).clickable {
                                                val updatedBranches = company.branches.map {
                                                    it.copy(isMainBranch = it.id == branch.id)
                                                }
                                                val updatedCompanies = uiState.companies.map { if (it.id == company.id) it.copy(branches = updatedBranches) else it }
                                                onUpdateCompanies(updatedCompanies)
                                            }
                                        ) {
                                            Icon(
                                                if (branch.isMainBranch) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                null,
                                                tint = if (branch.isMainBranch) GeminiAccentLocal else Color.Gray,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text("Casa Central", fontSize = 11.sp, color = if (branch.isMainBranch) Color.White else Color.Gray)
                                        }
                                    } else {
                                        Text(branch.name.ifEmpty { "Sucursal" }, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }

                                if (isEditMode) {
                                    IconButton(onClick = {
                                        onShowDeleteConfirm("¿Estás seguro de que deseas eliminar la sucursal '${branch.name}'?") {
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

                            Surface(
                                onClick = {
                                    if (isEditMode) {
                                        onEditRequest(EditMode.BranchAddress(company, branch, branch.address))
                                    } else {
                                        try {
                                            val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(branch.address.fullString())}")
                                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                            context.startActivity(mapIntent)
                                        } catch (e: Exception) {}
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("📍", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(branch.address.fullString().ifEmpty { "Añadir dirección" }, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(if(isEditMode) "Toca para editar" else "Abrir mapas", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                }
                            }

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
                                                        onShowDeleteConfirm("¿Deseas eliminar esta imagen?") {
                                                            val updatedImages = branch.galleryImages.filter { it != imgUrl }
                                                            val updatedBranches = company.branches.map { if (it.id == branch.id) it.copy(galleryImages = updatedImages) else it }
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
                                EmptyFieldPlaceHolder(text = "Todavía no hay imágenes")
                            }
                        }
                    }
                }

                if (sortedBranches.size > 1) {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.Center) {
                        repeat(sortedBranches.size) { iteration ->
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
// --- COMPONENTES VISUALES BASE (MODIFICADOS) ---
// =================================================================================

@Composable
fun ProfileDataFieldItem(
    emoji: String,
    label: String,
    value: String,
    isEditMode: Boolean = false,
    onValueChange: (String) -> Unit = {},
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    // REGLA: Ocultar si está vacío y NO estamos en modo edición
    if (!isEditMode && value.isBlank()) return

    Row(modifier = Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
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

@Composable
fun AddressDisplayCardLocal(
    address: AddressClient,
    isEditMode: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onEmojiClick: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = if (isEditMode) onEdit else onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            //Text(
               // text = address.emoji.ifEmpty { "📍" },
               // fontSize = 24.sp,
               // modifier = Modifier.clickable(enabled = isEditMode) { onEmojiClick() }
           // )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (address.label.isNotBlank()) address.label.uppercase() else "DIRECCIÓN",
                    style = MaterialTheme.typography.labelSmall,
                    color = GeminiAccentLocal,
                    fontWeight = FontWeight.Bold
                )
                Text(address.fullString(), fontWeight = FontWeight.Bold, color = Color.White)
                Text(if(isEditMode) "Toca para editar" else "Toca para abrir en Google Maps", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            if (isEditMode) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun EmptyFieldPlaceHolder(text: String) {
    Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
        Text(text, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ConfirmationDialogLocal(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF1C1C26))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(16.dp))
                Text("¿Confirmar acción?", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(message, color = Color.Gray, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancelar", color = Color.White)
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Eliminar", color = Color.White)
                    }
                }
            }
        }
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
            onRequestDelete = { _, _, _ -> }
        )
    }
}
**/