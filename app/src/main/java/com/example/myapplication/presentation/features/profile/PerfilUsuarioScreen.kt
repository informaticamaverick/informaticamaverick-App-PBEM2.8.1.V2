package com.example.myapplication.presentation.features.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.myapplication.core.data.local.entity.UserEntity
import com.example.myapplication.core.domain.model.*
import com.example.myapplication.core.utils.ImageUtils
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.designsystem.components.shakeClick
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.global.BeBrainViewModel
import com.example.myapplication.presentation.global.HUDContext
import kotlinx.coroutines.launch
import java.util.UUID

// --- ELITE MATERIAL 3 CONSTANTS ---
private val ProviderAccent = Color(0xFF3B82F6) 
private val CardStroke = Color.White.copy(alpha = 0.1f)

// -- EDITMODE ATÓMICO --
sealed class EditMode {
    object None : EditMode()
    data class BranchAddress(val company: CompanyClient, val branch: BranchClient, val address: AddressUnico) : EditMode()
    data class Representative(val company: CompanyClient, val branch: BranchClient, val representative: RepresentativeClient?) : EditMode()
    data class PersonalAddress(val address: AddressUnico?) : EditMode()
    data class Company(val company: CompanyClient) : EditMode()
    data class Branch(val company: CompanyClient, val branch: BranchClient?) : EditMode()
    data class Email(val initialValue: String, val index: Int?) : EditMode()
    data class Phone(val initialValue: String, val index: Int?) : EditMode()
}

@Composable
fun PerfilUsuarioScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: UserViewModel = hiltViewModel(),
    beViewModel: BeBrainViewModel = hiltViewModel()
) {
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var editMode by remember { mutableStateOf<EditMode>(EditMode.None) }
    val currentUser = userState

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

    LaunchedEffect(Unit) { 
        beViewModel.onRouteChanged("perfil_cliente")
        beViewModel.setHUDContext(HUDContext.PROFILE) 
    }

    val actionIds by viewModel.actionIds.collectAsStateWithLifecycle()
    LaunchedEffect(actionIds) {
        beViewModel.setCustomActionIds(actionIds, HUDContext.PROFILE)
    }

    DisposableEffect(Unit) {
        onDispose {
            beViewModel.clearCustomActions(HUDContext.PROFILE)
        }
    }

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
                "add_company" -> { editMode = EditMode.Company(CompanyClient()) }
            }
        }
    }

    val userAvatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.updateProfilePhoto(it) }
    }

    var companyIdForPicker by remember { mutableStateOf<String?>(null) }
    val companyAvatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { newUri ->
            val targetId = companyIdForPicker
            if (targetId != null) {
                uiState.companies.find { it.id == targetId }?.let { company ->
                    viewModel.saveCompany(company, newUri)
                }
            }
            companyIdForPicker = null
        }
    }

    if (currentUser == null) {
        Box(modifier = Modifier.fillMaxSize().background(MaverickColors.EliteMainBackground), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ProviderAccent)
        }
    } else {
        PerfilUsuarioContent(
            user = currentUser,
            isEditMode = uiState.isEditMode,
            uiState = uiState,
            onRefresh = { viewModel.refreshData() },
            onNavigateBack = onNavigateBack,
            onLogout = { viewModel.logout(); onLogout() },
            onEditRequest = { editMode = it },
            onEditUserAvatar = { userAvatarPicker.launch("image/*") },
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
            onToggleEditMode = { viewModel.toggleEditMode() },
            onNavigateToSettings = onNavigateToSettings,
            onRequestDelete = requestDelete,
            snackbarHostState = snackbarHostState
        )
    }

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

    ProfileEditSheetOrchestrator(
        editMode = editMode,
        uiState = uiState,
        viewModel = viewModel,
        onClose = { editMode = EditMode.None },
        onRequestDelete = requestDelete
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioContent(
    user: UserEntity,
    isEditMode: Boolean,
    uiState: UserUiState,
    onRefresh: () -> Unit,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onEditRequest: (EditMode) -> Unit,
    onEditUserAvatar: () -> Unit,
    onEditCompanyAvatar: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onUpdatePersonalAddresses: (List<AddressUnico>) -> Unit,
    onUpdateCompanies: (List<CompanyClient>) -> Unit,
    onUpdateEmails: (List<String>) -> Unit,
    onUpdatePhones: (List<String>) -> Unit,
    onToggleEditMode: () -> Unit,
    onNavigateToSettings: () -> Unit,
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

    val headerMaxHeight = 360.dp
    val headerMinHeight = 120.dp
    val density = LocalDensity.current
    val maxScroll = with(density) { (headerMaxHeight - headerMinHeight).toPx() }
    val collapseFraction by remember { derivedStateOf { (scrollState.value.toFloat() / maxScroll).coerceIn(0f, 1f) } }

    val headerHeight by animateDpAsState(targetValue = headerMaxHeight - (headerMaxHeight - headerMinHeight) * collapseFraction, label = "headerHeight")

    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
        floatingActionButton = {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                AnimatedVisibility(
                    visible = uiState.isEditMode,
                    enter = scaleIn(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    EliteUserCancelFAB(onCancel = onToggleEditMode)
                }

                AnimatedVisibility(
                    visible = true, // Always visible once loaded
                    enter = slideInVertically { it } + fadeIn(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    EliteUserInteractionFAB(
                        uiState = uiState,
                        currentPage = pagerState.currentPage,
                        onPageSelected = { coroutineScope.launch { pagerState.animateScrollToPage(it) } },
                        onToggleEditMode = onToggleEditMode,
                        onSettingsClick = onNavigateToSettings,
                        onAddCompany = { onEditRequest(EditMode.Company(CompanyClient())) }
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = uiState.isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = uiState.isLoading,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = Color(0xFF1A1A24),
                    color = ProviderAccent
                )
            }
        ) {
            Box(modifier = Modifier.fillMaxSize().background(MaverickColors.EliteMainBackground)) {
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Spacer(modifier = Modifier.height(headerMaxHeight + 16.dp))

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = screenHeight),
                        verticalAlignment = Alignment.Top,
                        userScrollEnabled = !isEditMode
                    ) { page ->
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
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
                            else BusinessM3Section(
                                company = companiesList[page - 1],
                                isEditMode = isEditMode,
                                uiState = uiState,
                                onEditRequest = onEditRequest,
                                onUpdateCompanies = onUpdateCompanies,
                                onRequestDelete = onRequestDelete,
                                onEditAvatar = { onEditCompanyAvatar(companiesList[page - 1].id) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(140.dp))
                }

                val pPhoto = remember(pagerState.currentPage, uiState.photoUrl, companiesList, collapseFraction < 0.5f) {
                    if (pagerState.currentPage == 0) {
                        uiState.photoUrl
                    } else {
                        val company = companiesList.getOrNull(pagerState.currentPage - 1)
                        if (company != null) {
                            if (collapseFraction < 0.5f && company.photoUrl != null) {
                                company.photoUrl
                            } else {
                                ImageUtils.processImageSource(company.thumbnailBase64 ?: company.photoUrl)
                            }
                        } else null
                    }
                }
                
                val pTitle = if (pagerState.currentPage == 0) {
                    uiState.displayName.ifEmpty { "${uiState.name} ${uiState.lastName}" }
                } else {
                    companiesList[pagerState.currentPage - 1].name
                }
                
                val pSubtitle = if (pagerState.currentPage == 0) {
                    uiState.email
                } else {
                    companiesList[pagerState.currentPage - 1].razonSocial
                }
                
                EliteDynamicHeader(
                    height = headerHeight,
                    collapseFraction = collapseFraction,
                    photoUrl = pPhoto,
                    title = pTitle,
                    subtitle = pSubtitle,
                    rating = 0f, 
                    isVerified = user.email.endsWith("@gmail.com"), 
                    onBack = onNavigateBack,
                    onLogout = onLogout,
                    isEditMode = isEditMode,
                    onEditAvatar = { if (pagerState.currentPage == 0) onEditUserAvatar() else onEditCompanyAvatar(companiesList[pagerState.currentPage - 1].id) }
                )
            }
        }
    }
}

@Composable
fun EliteDynamicHeader(
    height: androidx.compose.ui.unit.Dp,
    collapseFraction: Float,
    photoUrl: Any?,
    title: String,
    subtitle: String,
    rating: Float,
    isVerified: Boolean,
    onBack: () -> Unit,
    onLogout: () -> Unit = {},
    isEditMode: Boolean = false,
    onEditAvatar: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val maxHeaderHeight = 360.dp

    val imageSizeDp = (maxHeaderHeight.value + (44f - maxHeaderHeight.value) * collapseFraction).dp
    val imageWidthPx = screenWidth + (with(density) { 44.dp.toPx() } - screenWidth) * collapseFraction
    val imageCornerRadius = (0f + (22f - 0f) * collapseFraction).dp
    val imageBottomCornerRadius = (10f + (22f - 10f) * collapseFraction).dp 
    val imagePaddingStart = (0f + (60f - 0f) * collapseFraction).dp
    val imagePaddingTop = (0f + (40f - 0f) * collapseFraction).dp

    val nameTextSize = (26f + (18f - 26f) * collapseFraction).sp
    val textPaddingStart = (20f + (110f - 20f) * collapseFraction).dp
    val textPaddingTop = ((maxHeaderHeight.value - 90f) + (42f - (maxHeaderHeight.value - 90f)) * collapseFraction).dp

    val fadeOutAlpha = 1f - (collapseFraction * 2f).coerceIn(0f, 1f)
    val headerBgColor = Color(0xFF0F0F0F).copy(alpha = collapseFraction)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .zIndex(10f)
            .graphicsLayer { 
                shadowElevation = (collapseFraction * 12f)
                clip = false
            }
            .background(headerBgColor)
    ) {
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
            modifier = Modifier
                .padding(start = imagePaddingStart, top = imagePaddingTop)
                .size(width = with(density) { imageWidthPx.toDp() }, height = imageSizeDp)
                .clip(RoundedCornerShape(topStart = imageCornerRadius, topEnd = imageCornerRadius, bottomStart = imageBottomCornerRadius, bottomEnd = imageBottomCornerRadius))
                .clickable(enabled = isEditMode) { onEditAvatar() },
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_launcher_background)
        )

        Box(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = fadeOutAlpha }.background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f)), startY = 150f)))

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 6.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = true,
                enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn()
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.4f)).border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            
            if (!isEditMode) {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn()
                ) {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.4f)).border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.PowerSettingsNew, null, tint = Color(0xFFFF1744), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(start = textPaddingStart, top = textPaddingTop), verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = title.uppercase(), fontSize = nameTextSize, color = Color.White, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (isVerified) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.Verified, null, tint = ProviderAccent, modifier = Modifier.size(20.dp))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                if (rating > 0) {
                    EliteRatingStars(rating = rating)
                    Spacer(Modifier.width(8.dp))
                }
                Text(text = subtitle.uppercase(), style = MaterialTheme.typography.labelSmall, color = ProviderAccent, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
            }
        }
    }
}

@Composable
fun EliteUserInteractionFAB(
    modifier: Modifier = Modifier,
    uiState: UserUiState,
    currentPage: Int,
    onPageSelected: (Int) -> Unit,
    onToggleEditMode: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddCompany: () -> Unit
) {
    Surface(
        modifier = modifier.height(64.dp).wrapContentWidth().shadow(24.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF1A1A24),
        border = BorderStroke(1.dp, CardStroke)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shakeClick(onToggleEditMode),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = uiState.isEditMode,
                    transitionSpec = {
                        (scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn()).togetherWith(scaleOut() + fadeOut())
                    },
                    label = "edit_icon"
                ) { editing ->
                    Icon(
                        imageVector = if (editing) Icons.Default.Save else Icons.Default.Edit, 
                        contentDescription = null, 
                        tint = if (editing) Color(0xFF4ADE80) else ProviderAccent, 
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            VerticalDivider(modifier = Modifier.height(24.dp).width(1.dp), color = Color.White.copy(0.1f))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shakeClick(if (uiState.isEditMode) onAddCompany else onSettingsClick),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = uiState.isEditMode,
                    transitionSpec = {
                        (scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn()).togetherWith(scaleOut() + fadeOut())
                    },
                    label = "settings_icon"
                ) { editing ->
                    Icon(
                        imageVector = if (editing) Icons.Default.AddBusiness else Icons.Default.Settings, 
                        contentDescription = null, 
                        tint = if (editing) ProviderAccent else Color.Gray, 
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            VerticalDivider(modifier = Modifier.height(24.dp).width(1.dp), color = Color.White.copy(0.1f))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                EliteMiniProfileBubble(photoUrl = uiState.photoUrl, isSelected = currentPage == 0, onClick = { onPageSelected(0) })
                uiState.companies.forEachIndexed { index, company ->
                    val companyPhoto = remember(company.thumbnailBase64, company.photoUrl) {
                        ImageUtils.processImageSource(company.thumbnailBase64 ?: company.photoUrl)
                    }
                    EliteMiniProfileBubble(
                        photoUrl = companyPhoto, 
                        isSelected = currentPage == index + 1, 
                        onClick = { onPageSelected(index + 1) }
                    )
                }
            }
        }
    }
}



@Composable
fun EliteUserCancelFAB(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit
) {
    Surface(
        modifier = modifier.size(64.dp).shadow(24.dp, CircleShape),
        shape = CircleShape,
        color = Color(0xFF1A1A24),
        border = BorderStroke(1.dp, CardStroke)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().shakeClick(onCancel),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Close, null, tint = Color(0xFFF87171), modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun StaggeredVerticalEntry(
    index: Int,
    content: @Composable () -> Unit
) {
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = visibleState,
        enter = slideInVertically(
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            initialOffsetY = { it / 2 }
        ) + fadeIn(animationSpec = tween(durationMillis = 400, delayMillis = index * 100)),
        exit = fadeOut()
    ) {
        content()
    }
}

@Composable
fun PersonalM3Section(
    user: UserEntity,
    isEditMode: Boolean,
    uiState: UserUiState,
    onEditRequest: (EditMode) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onUpdatePersonalAddresses: (List<AddressUnico>) -> Unit,
    onUpdateEmails: (List<String>) -> Unit,
    onUpdatePhones: (List<String>) -> Unit,
    onRequestDelete: (String, String, () -> Unit) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        StaggeredVerticalEntry(index = 0) {
            ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))) {
                Column(modifier = Modifier.padding(14.dp)) {
                    EliteSectionHeader(title = "IDENTIDAD DIGITAL", emoji = "📇")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EliteM3DataRow(
                            label = "APODO / NOMBRE PÚBLICO", 
                            value = if (isEditMode) uiState.displayName else user.displayName, 
                            icon = Icons.Default.Face,
                            isEditMode = isEditMode,
                            onValueChange = onDisplayNameChange
                        )
                        EliteM3DataRow(
                            label = "NOMBRE", 
                            value = if (isEditMode) uiState.name else user.name, 
                            icon = Icons.Default.Person,
                            isEditMode = isEditMode,
                            onValueChange = onNameChange
                        )
                        EliteM3DataRow(
                            label = "APELLIDO", 
                            value = if (isEditMode) uiState.lastName else user.lastName, 
                            icon = Icons.Default.Person,
                            isEditMode = isEditMode,
                            onValueChange = onLastNameChange
                        )
                        
                        val primaryEmail = if (isEditMode) uiState.email else user.email
                        EliteM3DataRow(
                            label = "CORREO ELECTRÓNICO (PRINCIPAL)", 
                            value = primaryEmail, 
                            icon = Icons.Default.Email,
                            isGoogle = primaryEmail.endsWith("@gmail.com"),
                            trailingIcon = if (isEditMode) { { IconButton(onClick = { onEditRequest(EditMode.Email("", null)) }) { Icon(Icons.Default.Add, null, tint = ProviderAccent) } } } else null
                        )

                        val additionalEmails = if (isEditMode) uiState.additionalEmails else user.additionalEmails
                        additionalEmails.forEachIndexed { index, email ->
                            EliteM3DataRow(
                                label = "EMAIL ADICIONAL", 
                                value = email, 
                                icon = Icons.Default.AlternateEmail,
                                trailingIcon = if (isEditMode) { { 
                                    Row {
                                        IconButton(onClick = { onEditRequest(EditMode.Email(email, index)) }) { Icon(Icons.Default.Edit, null, tint = ProviderAccent, modifier = Modifier.size(18.dp)) }
                                        IconButton(onClick = { onRequestDelete("Eliminar Email", "¿Deseas eliminar este correo electrónico?") { val newList = uiState.additionalEmails.toMutableList(); newList.removeAt(index); onUpdateEmails(newList) } }) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(18.dp)) }
                                    }
                                } } else null
                            )
                        }

                        EliteM3DataRow(
                            label = "TELÉFONO (PRINCIPAL)", 
                            value = if (isEditMode) uiState.phoneNumber else user.phoneNumber, 
                            icon = Icons.Default.Phone,
                            isEditMode = isEditMode,
                            onValueChange = onPhoneNumberChange,
                            trailingIcon = if (isEditMode) { { IconButton(onClick = { onEditRequest(EditMode.Phone("", null)) }) { Icon(Icons.Default.Add, null, tint = ProviderAccent) } } } else null
                        )

                        val additionalPhones = if (isEditMode) uiState.additionalPhones else user.additionalPhones
                        additionalPhones.forEachIndexed { index, phone ->
                            EliteM3DataRow(
                                label = "TELÉFONO ADICIONAL", 
                                value = phone, 
                                icon = Icons.Default.Smartphone,
                                trailingIcon = if (isEditMode) { { 
                                    Row {
                                        IconButton(onClick = { onEditRequest(EditMode.Phone(phone, index)) }) { Icon(Icons.Default.Edit, null, tint = ProviderAccent, modifier = Modifier.size(18.dp)) }
                                        IconButton(onClick = { onRequestDelete("Eliminar Teléfono", "¿Deseas eliminar este número de teléfono?") { val newList = uiState.additionalPhones.toMutableList(); newList.removeAt(index); onUpdatePhones(newList) } }) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(18.dp)) }
                                    }
                                } } else null
                            )
                        }
                    }
                }
            }
        }

        StaggeredVerticalEntry(index = 1) {
            ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))) {
                Column(modifier = Modifier.padding(14.dp)) {
                    EliteSectionHeader(title = "BIOGRAFÍA Y PERFIL", emoji = "✨")
                    if (isEditMode) {
                        BasicTextField(
                            value = uiState.bio,
                            onValueChange = onBioChange,
                            textStyle = TextStyle(color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            cursorBrush = SolidColor(ProviderAccent),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (uiState.bio.isEmpty()) Text("Cuéntanos sobre ti...", color = Color.Gray, fontSize = 14.sp)
                                    innerTextField()
                                }
                            }
                        )
                    } else {
                        Text(text = user.bio.ifEmpty { "Sin biografía disponible" }, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.8f), lineHeight = 24.sp)
                    }
                }
            }
        }

        StaggeredVerticalEntry(index = 2) {
            ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        EliteSectionHeader(title = "MIS DIRECCIONES", emoji = "📍")
                        if (isEditMode) {
                            IconButton(onClick = { onEditRequest(EditMode.PersonalAddress(null)) }) { Icon(Icons.Default.AddLocationAlt, null, tint = ProviderAccent) }
                        }
                    }
                    
                    val addresses = if (isEditMode) uiState.personalAddresses else user.personalAddresses
                    if (addresses.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) { Text("No hay direcciones guardadas", color = Color.Gray, fontSize = 12.sp) }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { 
                            addresses.forEachIndexed { index, addr -> 
                                EliteSuperAddressCard(
                                    address = addr, 
                                    isEditMode = isEditMode, 
                                    onEdit = { onEditRequest(EditMode.PersonalAddress(addr)) },
                                    onDelete = { onRequestDelete("Eliminar Dirección", "¿Deseas eliminar esta dirección?") { val newList = uiState.personalAddresses.toMutableList(); newList.removeAt(index); onUpdatePersonalAddresses(newList) } }
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
fun BusinessM3Section(
    company: CompanyClient,
    isEditMode: Boolean,
    uiState: UserUiState,
    onEditRequest: (EditMode) -> Unit,
    onUpdateCompanies: (List<CompanyClient>) -> Unit,
    onRequestDelete: (String, String, () -> Unit) -> Unit,
    onEditAvatar: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        StaggeredVerticalEntry(index = 0) {
            ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))) {
                Column(modifier = Modifier.padding(14.dp)) {
                    EliteSectionHeader(title = "PERFIL CORPORATIVO", emoji = "🏢")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EliteM3DataRow(
                            label = "NOMBRE COMERCIAL", 
                            value = company.name, 
                            icon = Icons.Default.Business,
                            isEditMode = isEditMode,
                            onValueChange = { newVal -> val updated = uiState.companies.map { if (it.id == company.id) it.copy(name = newVal) else it }; onUpdateCompanies(updated) }
                        )
                        EliteM3DataRow(
                            label = "RAZÓN SOCIAL", 
                            value = company.razonSocial, 
                            icon = Icons.Default.Factory,
                            isEditMode = isEditMode,
                            onValueChange = { newVal -> val updated = uiState.companies.map { if (it.id == company.id) it.copy(razonSocial = newVal) else it }; onUpdateCompanies(updated) }
                        )
                        EliteM3DataRow(
                            label = "IDENTIFICACIÓN FISCAL (CUIT)", 
                            value = company.cuit, 
                            icon = Icons.Default.Badge,
                            isEditMode = isEditMode,
                            onValueChange = { newVal -> val updated = uiState.companies.map { if (it.id == company.id) it.copy(cuit = newVal) else it }; onUpdateCompanies(updated) }
                        )
                        EliteM3DataRow(
                            label = "EMAIL CORPORATIVO", 
                            value = company.email, 
                            icon = Icons.Default.Email,
                            isEditMode = isEditMode,
                            onValueChange = { newVal -> val updated = uiState.companies.map { if (it.id == company.id) it.copy(email = newVal) else it }; onUpdateCompanies(updated) }
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            EliteSectionHeader(title = "SUCURSALES Y PUNTOS DE VENTA", emoji = "🏢")
            if (isEditMode && company.branches.size < 3) {
                IconButton(onClick = { onEditRequest(EditMode.Branch(company, null)) }) { Icon(Icons.Default.AddBusiness, null, tint = ProviderAccent) }
            }
        }

        if (company.branches.isNotEmpty()) {
            val branchPagerState = rememberPagerState(pageCount = { company.branches.size })
            HorizontalPager(
                state = branchPagerState, 
                modifier = Modifier.fillMaxWidth(), 
                contentPadding = PaddingValues(horizontal = 4.dp), 
                pageSpacing = 12.dp
            ) { page -> 
                StaggeredVerticalEntry(index = page + 1) {
                    EliteBranchClientCard(
                        branch = company.branches[page], 
                        company = company, 
                        isEditMode = isEditMode,
                        uiState = uiState,
                        onUpdateCompanies = onUpdateCompanies,
                        onEditRequest = onEditRequest,
                        onRequestDelete = onRequestDelete
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                Text("No hay sucursales registradas", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun EliteM3DataRow(
    label: String, 
    value: String, 
    icon: ImageVector,
    isEditMode: Boolean = false,
    onValueChange: (String) -> Unit = {},
    isGoogle: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = ProviderAccent.copy(alpha = 0.1f)) {
            Box(contentAlignment = Alignment.Center) { 
                if (isGoogle) {
                    Icon(painter = painterResource(R.drawable.ic_google_logo), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(16.dp))
                } else {
                    Icon(icon, null, tint = ProviderAccent, modifier = Modifier.size(16.dp)) 
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            if (isEditMode && !isGoogle && trailingIcon == null) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth(),
                    cursorBrush = SolidColor(ProviderAccent),
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isEmpty()) Text("Completar...", color = Color.Gray, fontSize = 14.sp)
                            innerTextField()
                        }
                    }
                )
            } else {
                Text(value.ifEmpty { "No especificado" }, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
        if (trailingIcon != null) trailingIcon()
    }
}

@Composable
fun EliteSuperAddressCard(
    address: AddressUnico, 
    isEditMode: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val context = LocalContext.current
    Surface(
        onClick = { 
            if (isEditMode) onEdit() 
            else {
                val uri = "geo:0,0?q=${Uri.encode(address.fullString())}".toUri()
                context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") }) 
            }
        }, 
        color = Color.White.copy(0.03f), 
        shape = RoundedCornerShape(8.dp), 
        border = BorderStroke(1.dp, Color.White.copy(0.05f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(ProviderAccent.copy(0.1f)), contentAlignment = Alignment.Center) { 
                Icon(Icons.Default.Place, null, tint = ProviderAccent, modifier = Modifier.size(24.dp)) 
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (address.label.isNotEmpty()) Text(address.label.uppercase(), style = MaterialTheme.typography.labelSmall, color = ProviderAccent, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text("${address.calle} ${address.numero}", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
                Text("${address.localidad}, ${address.provincia}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                if (!isEditMode) Text("Tocar para abrir en Maps", style = MaterialTheme.typography.labelSmall, color = ProviderAccent.copy(0.8f), modifier = Modifier.padding(top = 4.dp))
            }
            if (isEditMode) {
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(0.6f), modifier = Modifier.size(20.dp)) }
            } else {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.Gray.copy(0.5f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun EliteBranchClientCard(
    branch: BranchClient, 
    company: CompanyClient,
    isEditMode: Boolean,
    uiState: UserUiState,
    onUpdateCompanies: (List<CompanyClient>) -> Unit,
    onEditRequest: (EditMode) -> Unit,
    onRequestDelete: (String, String, () -> Unit) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), shape = RoundedCornerShape(6.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                if (isEditMode) {
                    BasicTextField(
                        value = branch.name,
                        onValueChange = { newVal ->
                            val updatedBranches = company.branches.map { if (it.id == branch.id) it.copy(name = newVal) else it }
                            val updatedCompanies = uiState.companies.map { if (it.id == company.id) it.copy(branches = updatedBranches) else it }
                            onUpdateCompanies(updatedCompanies)
                        },
                        textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp),
                        modifier = Modifier.weight(1f),
                        cursorBrush = SolidColor(ProviderAccent),
                        decorationBox = { innerTextField ->
                            Box {
                                if (branch.name.isEmpty()) Text("Nombre sucursal", color = Color.Gray, fontSize = 18.sp)
                                innerTextField()
                            }
                        }
                    )
                } else {
                    Text(branch.name.ifEmpty { "Sucursal" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
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

            if (branch.isMainBranch) {
                Surface(color = ProviderAccent, shape = CircleShape, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)) {
                    Text("📍 CASA CENTRAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            } else if (isEditMode) {
                Text(
                    text = "MARCAR COMO CASA CENTRAL", 
                    color = Color.Gray, 
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp).clickable {
                        val updatedBranches = company.branches.map { it.copy(isMainBranch = it.id == branch.id) }
                        val updatedCompanies = uiState.companies.map { if (it.id == company.id) it.copy(branches = updatedBranches) else it }
                        onUpdateCompanies(updatedCompanies)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            EliteSectionHeader(title = "UBICACIÓN DE LA SUCURSAL", emoji = "📍")
            EliteSuperAddressCard(
                address = branch.address,
                isEditMode = isEditMode,
                onEdit = { onEditRequest(EditMode.BranchAddress(company, branch, branch.address)) }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            EliteSectionHeader(title = "EQUIPO REPRESENTANTE", emoji = "👥")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) { 
                items(branch.representatives) { rep -> 
                    EliteRepresentativeItem(rep = rep, isEditMode = isEditMode, onClick = { if (isEditMode) onEditRequest(EditMode.Representative(company, branch, rep)) }) 
                } 
                if (isEditMode) {
                    item {
                        IconButton(onClick = { onEditRequest(EditMode.Representative(company, branch, null)) }) {
                            Icon(Icons.Default.PersonAdd, null, tint = ProviderAccent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EliteRepresentativeItem(rep: RepresentativeClient, isEditMode: Boolean, onClick: () -> Unit) {
    val repPhoto = remember(rep.thumbnailBase64, rep.photoUrl) {
        ImageUtils.processImageSource(rep.thumbnailBase64 ?: rep.photoUrl)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp).clickable(enabled = isEditMode) { onClick() }) {
        AsyncImage(
            model = repPhoto,
            contentDescription = null,
            modifier = Modifier.size(52.dp).clip(CircleShape).border(1.dp, ProviderAccent.copy(0.4f), CircleShape),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_launcher_foreground)
        )
        Spacer(Modifier.height(8.dp))
        Text(text = rep.nombre, style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = rep.cargo.uppercase(), style = MaterialTheme.typography.labelSmall, color = ProviderAccent, fontSize = 7.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp, maxLines = 1)
    }
}







fun String.toUri(): Uri = Uri.parse(this)

@Preview(showBackground = true)
@Composable
fun PerfilUsuarioElitePreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize().background(MaverickColors.EliteMainBackground)) {
            // Preview
        }
    }
}
