package com.example.myapplication.presentation.features.profile

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.domain.model.*
import com.example.myapplication.data.model.ProviderDisplayModel
import com.example.myapplication.data.model.BadgeDisplayData
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.designsystem.components.shakeClick
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.global.BeBrainViewModel
import com.example.myapplication.presentation.global.HUDContext
import kotlinx.coroutines.launch

// --- ELITE MATERIAL 3 CONSTANTS ---
private val ProviderAccent = Color(0xFF3B82F6) 
private val GoldAccent = Color(0xFFFFD700)
private val CardStroke = Color.White.copy(alpha = 0.1f)

@Composable
fun PerfilPrestadorScreen(
    providerId: String,
    initialCompanyId: String? = null,
    initialBranchId: String? = null,
    onBack: () -> Unit,
    onNavigateToChat: (ProviderDisplayModel, String?, String?) -> Unit,
    viewModel: ProviderViewModel = hiltViewModel(),
    beViewModel: BeBrainViewModel = hiltViewModel()
) {
    val providerDisplay by viewModel.selectedProvider.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()

    LaunchedEffect(providerId) {
        viewModel.loadFullProfile(providerId)
        beViewModel.setHUDContext(HUDContext.PROFILE_PRESTADOR)
    }

    Box(modifier = Modifier.fillMaxSize().background(MaverickColors.EliteMainBackground)) {
        providerDisplay?.let {
            PerfilPrestadorContent(
                provider = it, 
                initialCompanyId = initialCompanyId,
                initialBranchId = initialBranchId,
                allCategories = allCategories, 
                onBack = onBack,
                onNavigateToChat = onNavigateToChat
            )
        } ?: if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ProviderAccent, strokeWidth = 3.dp)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Perfil no encontrado", color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PerfilPrestadorContent(
    provider: ProviderDisplayModel,
    initialCompanyId: String? = null,
    initialBranchId: String? = null,
    allCategories: List<CategoryEntity> = emptyList(),
    onBack: () -> Unit,
    onNavigateToChat: (ProviderDisplayModel, String?, String?) -> Unit
) {
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    val companiesList = provider.companies
    val totalPages = 1 + companiesList.size
    
    val initialPage = remember(initialCompanyId, companiesList) {
        if (initialCompanyId != null) {
            val index = companiesList.indexOfFirst { it.id == initialCompanyId }
            if (index != -1) index + 1 else 0
        } else 0
    }

    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { totalPages })
    val coroutineScope = rememberCoroutineScope()

    val headerMaxHeight = 360.dp
    val headerMinHeight = 120.dp
    val density = LocalDensity.current
    val maxScroll = with(density) { (headerMaxHeight - headerMinHeight).toPx() }
    val collapseFraction by remember { derivedStateOf { (scrollState.value.toFloat() / maxScroll).coerceIn(0f, 1f) } }

    val headerHeight by animateDpAsState(targetValue = headerMaxHeight - (headerMaxHeight - headerMinHeight) * collapseFraction, label = "headerHeight")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
        floatingActionButton = {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                EliteInteractionFAB(
                    provider = provider,
                    currentPage = pagerState.currentPage,
                    onPageSelected = { coroutineScope.launch { pagerState.animateScrollToPage(it) } },
                    onLike = { },
                    onDislike = { },
                    isFavorite = provider.isFavorite,
                    onChatClick = { cid -> 
                        // 🔥 Si volvemos a la empresa inicial, restauramos el branchId
                        val bid = if (cid == initialCompanyId) initialBranchId else null
                        onNavigateToChat(provider, cid, bid)
                    }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            
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
                    userScrollEnabled = false
                ) { page ->
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        if (page == 0) EliteProviderPersonalSection(provider = provider, allCategories = allCategories)
                        else EliteProviderBusinessSection(company = companiesList[page - 1], allCategories = allCategories)
                    }
                }
                Spacer(modifier = Modifier.height(140.dp))
            }

            val pPhoto = if (pagerState.currentPage == 0) {
                provider.photoUrl
            } else {
                val company = companiesList[pagerState.currentPage - 1]
                if (collapseFraction < 0.5f && company.photoUrl != null) {
                    company.photoUrl
                } else {
                    com.example.myapplication.core.utils.ImageUtils.processImageSource(company.thumbnailBase64 ?: company.photoUrl)
                }
            }
            val pTitle = if (pagerState.currentPage == 0) provider.title else companiesList[pagerState.currentPage - 1].name
            val pSubtitle = if (pagerState.currentPage == 0) provider.subtitle ?: "Profesional" else "Empresa Certificada"
            
            EliteDynamicHeader(
                height = headerHeight,
                collapseFraction = collapseFraction,
                photoUrl = pPhoto,
                title = pTitle,
                subtitle = pSubtitle,
                rating = provider.rating.toFloat(),
                isVerified = provider.isVerified,
                onBack = onBack
            )
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
    onBack: () -> Unit
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
                .clip(RoundedCornerShape(topStart = imageCornerRadius, topEnd = imageCornerRadius, bottomStart = imageBottomCornerRadius, bottomEnd = imageBottomCornerRadius)),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_launcher_background)
        )

        Box(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = fadeOutAlpha }.background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f)), startY = 150f)))

        AnimatedVisibility(
            visible = true,
            enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(top = 40.dp, start = 6.dp).size(40.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.4f)).border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(start = textPaddingStart, top = textPaddingTop), verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, fontSize = nameTextSize, color = Color.White, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (isVerified) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.Verified, null, tint = ProviderAccent, modifier = Modifier.size(20.dp))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                EliteRatingStars(rating = rating)
                Spacer(Modifier.width(8.dp))
                Text(text = subtitle.uppercase(), style = MaterialTheme.typography.labelSmall, color = ProviderAccent, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EliteInteractionFAB(
    provider: ProviderDisplayModel,
    currentPage: Int,
    onPageSelected: (Int) -> Unit,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    isFavorite: Boolean,
    onChatClick: (String?) -> Unit
) {
    Surface(
        modifier = Modifier.padding(bottom = 16.dp).height(64.dp).wrapContentWidth().shadow(24.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF1A1A24),
        border = BorderStroke(1.dp, CardStroke)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLike, modifier = Modifier.size(40.dp)) {
                    AnimatedContent(
                        targetState = isFavorite,
                        transitionSpec = {
                            (scaleIn(spring(Spring.DampingRatioHighBouncy)) + fadeIn()).togetherWith(scaleOut() + fadeOut())
                        },
                        label = "favorite_icon"
                    ) { fav ->
                        Icon(imageVector = if (fav) Icons.Default.Favorite else Icons.Outlined.ThumbUp, null, tint = if (fav) GoldAccent else Color(0xFF4ADE80), modifier = Modifier.size(20.dp))
                    }
                }
                IconButton(onClick = onDislike, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Outlined.ThumbDown, null, tint = Color(0xFFF87171), modifier = Modifier.size(20.dp))
                }
            }
            VerticalDivider(modifier = Modifier.height(24.dp).width(1.dp), color = Color.White.copy(0.1f))
            
            Button(
                onClick = { 
                    val companyId = if (currentPage == 0) null else provider.companies[currentPage - 1].id
                    onChatClick(companyId)
                }, 
                colors = ButtonDefaults.buttonColors(containerColor = ProviderAccent), 
                shape = RoundedCornerShape(24.dp), 
                modifier = Modifier.height(40.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(16.dp))
                if (provider.companies.size <= 1) {
                    Spacer(Modifier.width(8.dp))
                    Text("CONTACTAR", fontWeight = FontWeight.Black, fontSize = 10.sp)
                }
            }
            if (provider.companies.isNotEmpty()) {
                VerticalDivider(modifier = Modifier.height(24.dp).width(1.dp), color = Color.White.copy(0.1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    EliteMiniProfileBubble(photoUrl = provider.photoUrl, isSelected = currentPage == 0, onClick = { onPageSelected(0) })
                    provider.companies.forEachIndexed { index, company ->
                        EliteMiniProfileBubble(photoUrl = com.example.myapplication.core.utils.ImageUtils.processImageSource(company.thumbnailBase64 ?: company.photoUrl), isSelected = currentPage == index + 1, onClick = { onPageSelected(index + 1) })
                    }
                }
            }
        }
    }
}

@Composable
fun EliteMiniProfileBubble(photoUrl: Any?, isSelected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (isSelected) 1.2f else 1f, label = "scale")
    AsyncImage(
        model = photoUrl, 
        contentDescription = null, 
        modifier = Modifier
            .size(32.dp)
            .scale(scale)
            .clip(CircleShape)
            .border(
                if (isSelected) 2.dp else 1.dp, 
                if (isSelected) ProviderAccent else Color.White.copy(0.2f), 
                CircleShape
            )
            .shakeClick { onClick() }
            .background(Color.Black), 
        contentScale = ContentScale.Crop, 
        error = painterResource(id = R.drawable.ic_launcher_foreground)
    )
}

@Composable
fun ProfileStaggeredEntry(
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
fun EliteProviderPersonalSection(provider: ProviderDisplayModel, allCategories: List<CategoryEntity> = emptyList()) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ProfileStaggeredEntry(index = 0) {
            ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))) {
                Column(modifier = Modifier.padding(14.dp)) {
                    EliteSectionHeader(title = "INFORMACIÓN DE CONTACTO", emoji = "📇")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (provider.email.isNotBlank()) EliteM3DataRow(label = if (provider.email.endsWith("@gmail.com")) "Cuenta de Google G" else "Correo Electrónico", value = provider.email, icon = Icons.Default.Email)
                        provider.emails.filter { it != provider.email }.forEach { EliteM3DataRow(label = "Email Adicional", value = it, icon = Icons.Default.AlternateEmail) }
                        if (provider.phoneNumber.isNotBlank()) EliteM3DataRow(label = "Teléfono de Contacto", value = provider.phoneNumber, icon = Icons.Default.Phone)
                        provider.profesion?.let { EliteM3DataRow(label = "Profesión / Oficio", value = it, icon = Icons.Default.Work) }
                        provider.matricula?.let { EliteM3DataRow(label = "Matrícula Profesional", value = it, icon = Icons.Default.Badge) }
                        provider.cuilCuit?.let { EliteM3DataRow(label = "Identificación (DNI/CUIT)", value = it, icon = Icons.Default.Fingerprint) }
                    }
                }
            }
        }
        if (provider.categories.isNotEmpty()) {
            ProfileStaggeredEntry(index = 1) {
                ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        EliteSectionHeader(title = "ESPECIALIDADES", emoji = "🏷️")
                        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            provider.categories.forEach { categoryName ->
                                val emoji = allCategories.find { it.name == categoryName }?.icon ?: "🔧"
                                Surface(modifier = Modifier.height(32.dp), shape = RoundedCornerShape(8.dp), color = Color.White.copy(0.05f), border = BorderStroke(1.dp, Color.White.copy(0.1f))) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp)) {
                                        Text(emoji, fontSize = 14.sp)
                                        VerticalDivider(modifier = Modifier.padding(horizontal = 8.dp).height(16.dp), color = Color.White.copy(0.1f))
                                        Text(text = categoryName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        ProfileStaggeredEntry(index = 2) { EliteServicePassport(provider = provider) }
        ProfileStaggeredEntry(index = 3) { EliteScheduleTableCard(schedule = provider.workingHours) }
        ProfileStaggeredEntry(index = 4) {
            ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))) {
                Column(modifier = Modifier.padding(14.dp)) {
                    EliteSectionHeader(title = "UBICACIONES Y PUNTOS DE VENTA", emoji = "📍")
                    if (provider.addresses.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) { Text("No hay ubicaciones guardadas", color = Color.Gray, fontSize = 12.sp) }
                    } else {
                        //Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { provider.addresses.forEach { EliteSuperAddressCard(address = it) } }
                    }
                }
            }
        }
        if (provider.description.isNotBlank()) {
            ProfileStaggeredEntry(index = 5) {
                ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        EliteSectionHeader(title = "SOBRE EL PROFESIONAL", emoji = "✨")
                        Text(text = provider.description, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.8f), lineHeight = 24.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EliteM3DataRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = ProviderAccent.copy(alpha = 0.1f)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = ProviderAccent, modifier = Modifier.size(16.dp)) }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun EliteSuperAddressCard(address: AddressUnico) {
    val context = LocalContext.current
    Surface(onClick = { val uri = "geo:0,0?q=${Uri.encode(address.fullString())}".toUri(); context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") }) }, color = Color.White.copy(0.03f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color.White.copy(0.05f))) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(ProviderAccent.copy(0.1f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Place, null, tint = ProviderAccent, modifier = Modifier.size(24.dp)) }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (address.label.isNotEmpty()) Text(address.label.uppercase(), style = MaterialTheme.typography.labelSmall, color = ProviderAccent, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text("${address.calle} ${address.numero}", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
                Text("${address.localidad}, ${address.provincia}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("Tocar para abrir en Maps", style = MaterialTheme.typography.labelSmall, color = ProviderAccent.copy(0.8f), modifier = Modifier.padding(top = 4.dp))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.Gray.copy(0.5f), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun EliteServicePassport(provider: ProviderDisplayModel) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))) {
        Column(modifier = Modifier.padding(14.dp)) {
            EliteSectionHeader(title = "PASAPORTE DE SERVICIOS", emoji = "🛂")
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                provider.badgeList.forEach { badge ->
                    Surface(modifier = Modifier.height(32.dp), color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color.White.copy(0.1f))) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(badge.icon, fontSize = 14.sp)
                            VerticalDivider(modifier = Modifier.padding(horizontal = 8.dp).height(16.dp), color = Color.White.copy(0.1f))
                            Text(text = badge.label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EliteRatingStars(rating: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index -> Icon(imageVector = if (index < rating.toInt()) Icons.Default.Star else Icons.Default.StarOutline, contentDescription = null, tint = if (index < rating.toInt()) GoldAccent else Color.Gray.copy(0.3f), modifier = Modifier.size(16.dp)) }
        Spacer(Modifier.width(6.dp))
        Text(text = "%.1f".format(rating), color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EliteProviderBusinessSection(company: CompanyProvider, allCategories: List<CategoryEntity> = emptyList()) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ProfileStaggeredEntry(index = 0) {
            ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))) {
                Column(modifier = Modifier.padding(14.dp)) {
                    EliteSectionHeader(title = "PERFIL CORPORATIVO", emoji = "🏢")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (company.email.isNotBlank()) EliteM3DataRow(label = "Email Corporativo", value = company.email, icon = Icons.Default.Email)
                        EliteM3DataRow(label = "Razón Social", value = company.razonSocial, icon = Icons.Default.Business)
                        EliteM3DataRow(label = "Identificación Fiscal (CUIT)", value = company.cuit, icon = Icons.Default.Badge)
                    }
                }
            }
        }
        if (company.categories.isNotEmpty()) {
            ProfileStaggeredEntry(index = 1) {
                ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        EliteSectionHeader(title = "RUBROS DE LA EMPRESA", emoji = "🏷️")
                        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            company.categories.forEach { categoryName ->
                                val emoji = allCategories.find { it.name == categoryName }?.icon ?: "🏢"
                                Surface(modifier = Modifier.height(32.dp), shape = RoundedCornerShape(8.dp), color = Color.White.copy(0.05f), border = BorderStroke(1.dp, Color.White.copy(0.1f))) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp)) {
                                        Text(emoji, fontSize = 14.sp)
                                        VerticalDivider(modifier = Modifier.padding(horizontal = 8.dp).height(16.dp), color = Color.White.copy(0.1f))
                                        Text(text = categoryName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (company.description.isNotBlank()) {
            ProfileStaggeredEntry(index = 2) {
                ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        EliteSectionHeader(title = "SOBRE LA COMPAÑÍA", emoji = "✨")
                        Text(text = company.description, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.8f), lineHeight = 24.sp)
                    }
                }
            }
        }
        if (company.branches.isNotEmpty()) {
            val branchPagerState = rememberPagerState(pageCount = { company.branches.size })
            ProfileStaggeredEntry(index = 3) {
                Column {
                    EliteSectionHeader(title = "PUNTOS DE ATENCIÓN", emoji = "🏢", modifier = Modifier.padding(start = 4.dp, top = 8.dp))
                    HorizontalPager(state = branchPagerState, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 4.dp), pageSpacing = 12.dp) { page -> EliteBranchCard(branch = company.branches[page]) }
                }
            }
        }
    }
}

@Composable
fun EliteScheduleTableCard(schedule: String) {
    val statusText = remember(schedule) {
        if (schedule.isBlank()) "sin Horarios"
        else try {
            val now = java.util.Calendar.getInstance()
            val currentMinutes = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)
            val parts = schedule.split("-")
            if (parts.size >= 2) {
                val start = parts[0].trim().split(":")
                val end = parts[1].trim().split(":")
                val startM = start[0].toInt() * 60 + start[1].take(2).toInt()
                val endM = end[0].toInt() * 60 + end[1].take(2).toInt()
                if (currentMinutes in startM..endM || currentMinutes in (16 * 60)..(20 * 60)) "ABIERTO AHORA ✅" else "CERRADO AHORA 🔴"
            } else "Horario Especial"
        } catch (_: Exception) { "Horario Especial" }
    }
    val statusColor = if (statusText.contains("ABIERTO")) Color(0xFF4ADE80) else if (statusText == "sin Horarios") Color.Gray else Color(0xFFF87171)
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                EliteSectionHeader(title = "HORARIOS DE ATENCIÓN", emoji = "⌚", modifier = Modifier.weight(1f))
                Text(text = statusText, style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Black)
            }
            if (schedule.isNotBlank()) {
                Column(modifier = Modifier.fillMaxWidth().border(0.5.dp, Color.White.copy(alpha = 0.1f))) {
                    Row(modifier = Modifier.fillMaxWidth().background(Color.White.copy(0.05f))) {
                        Box(modifier = Modifier.size(width = 50.dp, height = 30.dp).border(0.5.dp, Color.White.copy(0.1f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Schedule, null, tint = ProviderAccent, modifier = Modifier.size(14.dp)) }
                        listOf("LUN", "MAR", "MIE", "JUE", "VIE", "SAB", "DOM").forEach { day -> Box(modifier = Modifier.weight(1f).height(30.dp).border(0.5.dp, Color.White.copy(0.1f)), contentAlignment = Alignment.Center) { Text(day, fontSize = 9.sp, fontWeight = FontWeight.Black, color = ProviderAccent) } }
                    }
                    EliteScheduleDataRow(label = "DESDE", value = schedule.split("-").firstOrNull()?.trim() ?: "08:00")
                    EliteScheduleDataRow(label = "HASTA", value = schedule.split("-").getOrNull(1)?.trim() ?: "13:00")
                    EliteScheduleDataRow(label = "DESDE", value = "16:00")
                    EliteScheduleDataRow(label = "HASTA", value = "20:00")
                }
            }
        }
    }
}

@Composable
fun EliteScheduleDataRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(width = 50.dp, height = 24.dp).border(0.5.dp, Color.White.copy(0.1f)).background(Color.White.copy(0.02f)), contentAlignment = Alignment.Center) { Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray) }
        repeat(7) { Box(modifier = Modifier.weight(1f).height(24.dp).border(0.5.dp, Color.White.copy(0.1f)), contentAlignment = Alignment.Center) { Text(value, fontSize = 8.sp, color = Color.White) } }
    }
}

@Composable
fun EliteGallerySection(images: List<String>) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        EliteSectionHeader(title = "GALERÍA DE TRABAJOS", emoji = "📸")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 4.dp)) { items(images) { imageUrl -> Card(modifier = Modifier.size(width = 240.dp, height = 160.dp), shape = RoundedCornerShape(16.dp)) { AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(), error = painterResource(id = R.drawable.ic_launcher_background)) } } }
    }
}

@Composable
fun EliteSectionHeader(title: String, emoji: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) { Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp) }
}

@Composable
fun EliteBranchCard(branch: BranchProvider) {
    ElevatedCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), shape = RoundedCornerShape(6.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(branch.name.ifEmpty { "Sucursal" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
            if (branch.description.isNotBlank()) Text(text = branch.description, style = MaterialTheme.typography.bodyMedium, color = Color.LightGray, modifier = Modifier.padding(top = 8.dp))
            if (branch.workingHours.isNotBlank()) { Spacer(modifier = Modifier.height(16.dp)); EliteScheduleTableCard(schedule = branch.workingHours) }
            Spacer(modifier = Modifier.height(16.dp))
            EliteSectionHeader(title = "UBICACIÓN DE LA SUCURSAL", emoji = "📍")
            EliteSuperAddressCard(address = branch.address)
            if (branch.team.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp)); EliteSectionHeader(title = "EQUIPO DE TRABAJO", emoji = "👥")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) { items(branch.team) { EliteEmployeeItem(employee = it) } }
            }
        }
    }
}

@Composable
fun EliteEmployeeItem(employee: EmployeeProvider) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
        Surface(modifier = Modifier.size(52.dp), shape = CircleShape, color = ProviderAccent.copy(alpha = 0.1f), border = BorderStroke(1.dp, ProviderAccent.copy(0.4f))) {
            Box(contentAlignment = Alignment.Center) { Text(text = employee.name.take(1).uppercase() + employee.lastName.take(1).uppercase(), color = ProviderAccent, fontWeight = FontWeight.Black, fontSize = 16.sp) }
        }
        Spacer(Modifier.height(8.dp))
        Text(text = employee.name, style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = employee.position.uppercase(), style = MaterialTheme.typography.labelSmall, color = ProviderAccent, fontSize = 7.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp, maxLines = 1)
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilPrestadorElitePreview() {
    val sampleProvider = Provider(uid = "prov_elite", email = "expert@maverick.com", phoneNumber = "3811234567", displayName = "Maverick Group", name = "Max", lastName = "N", profesion = "Ingeniero", description = "Bio...", isVerified = true, rating = 4.8f, categories = listOf("Ingeniería"), addresses = listOf(AddressUnico(calle = "Av. Aconquija", numero = "1500", localidad = "Yerba Buena")), companies = listOf(CompanyProvider(id = "c1", name = "Maverick Dynamics", branches = listOf(BranchProvider(id = "b1", name = "Norte", team = listOf(EmployeeProvider(id = "e1", name = "Sofia", lastName = "V", position = "Senior")))))))
}
