package com.example.myapplication.presentation.client

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.model.*
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.fake.PrestadorSampleDataFalso
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

// =================================================================================
// --- CONSTANTES DE ESTILO (IDENTIDAD VISUAL COMPARTIDA) ---
// =================================================================================
private val ProviderDarkBg = Color(0xFF0A0A0F)
private val ProviderCardSurface = Color(0xFF16161D)
private val ProviderGeminiAccent = Color(0xFFA78BFA)
private val ProviderPremiumGold = Color(0xFFFFD700)

// =================================================================================
// --- PANTALLA PRINCIPAL ---
// =================================================================================

@Composable
fun PerfilPrestadorCliente(
    providerId: String,
    onBack: () -> Unit,
    providerViewModel: ProviderViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val providerState by providerViewModel.getProviderById(providerId).collectAsStateWithLifecycle(initialValue = null)
    //val allCategories by categoryViewModel.categories.collectAsStateWithLifecycle()
    val categories by categoryViewModel.allCategories.collectAsStateWithLifecycle()
    
    // 🔥 [RECOMENDACIÓN 1]: Estado centralizado en el ViewModel
    val perfilUiState by providerViewModel.perfilUiState.collectAsStateWithLifecycle()

    PerfilPrestadorClienteContent(
        providerState = providerState,
        allCategories = categories,
        perfilUiState = perfilUiState,
        onUpdatePage = { page, provider -> providerViewModel.updateProfilePage(page, provider) },
        onToggleHoursModal = { show -> providerViewModel.toggleHoursModal(show) },
        onBack = onBack
    )
}

@Composable
fun PerfilPrestadorClienteContent(
    providerState: Provider?,
    allCategories: List<CategoryEntity>,
    perfilUiState: PerfilUIState,
    onUpdatePage: (Int, Provider) -> Unit,
    onToggleHoursModal: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    if (providerState == null) {
        Box(modifier = Modifier.fillMaxSize().background(ProviderDarkBg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ProviderGeminiAccent)
        }
        return
    }

    PerfilPrestadorContent(
        provider = providerState,
        allCategories = allCategories,
        perfilUiState = perfilUiState,
        onUpdatePage = onUpdatePage,
        onToggleHoursModal = onToggleHoursModal,
        onNavigateBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PerfilPrestadorContent(
    provider: Provider,
    allCategories: List<CategoryEntity>,
    perfilUiState: PerfilUIState,
    onUpdatePage: (Int, Provider) -> Unit,
    onToggleHoursModal: (Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val coroutineScope = rememberCoroutineScope()

    // --- LÓGICA DE NAVEGACIÓN ---
    // 🔥 [RECOMENDACIÓN]: El ViewModel ahora es dueño del conteo de páginas y lógica derivada
    val pagerState = rememberPagerState(pageCount = { perfilUiState.totalPages })

    // 🔥 [RECOMENDACIÓN]: Sincronización con el ViewModel
    // Cuando cambia la página, notificamos al ViewModel para que recalcule el perfil activo, burbujas y banner.
    LaunchedEffect(pagerState.currentPage, provider) {
        onUpdatePage(pagerState.currentPage, provider)
    }

    // --- ESTADOS LOCALES (SOLO LO QUE ES ESTRICTAMENTE DE UI) ---
    var fullscreenImageUrl by remember { mutableStateOf<String?>(null) }

    // --- ANIMACIONES DE COLAPSO (PARALLAX EFFECT) ---
    val headerMaxHeight = 330.dp
    val headerMinHeight = 140.dp
    val density = LocalDensity.current
    val maxScroll = with(density) { (headerMaxHeight - headerMinHeight).toPx() }
    val collapseFraction = (scrollState.value.toFloat() / maxScroll).coerceIn(0f, 1f)

    val headerHeight by animateDpAsState(targetValue = headerMaxHeight - (headerMaxHeight - headerMinHeight) * collapseFraction)
    val avatarSize by animateDpAsState(targetValue = 90.dp - (35.dp * collapseFraction))

    Box(modifier = Modifier.fillMaxSize().background(ProviderDarkBg)) {

        // --- CONTENIDO SCROLLABLE (SECCIONES BENTO) ---
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
                if (page == 0) {
                    IndependentBentoSection(
                        provider = provider,
                        allCategories = allCategories,
                        emails = perfilUiState.currentEmails,
                        addresses = perfilUiState.currentAddresses,
                        onOpenHours = { onToggleHoursModal(true) },
                        onImageClick = { fullscreenImageUrl = it }
                    )
                } else {
                    val company = provider.companies.getOrNull(page - 1)
                    company?.let {
                        CompanyBentoSection(
                            company = it,
                            allCategories = allCategories,
                            addresses = perfilUiState.currentAddresses,
                            onOpenHours = { onToggleHoursModal(true) },
                            onImageClick = { fullscreenImageUrl = it }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }

        // --- HEADER DINÁMICO COLAPSABLE ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .zIndex(10f)
        ) {
            // 🔥 [RECOMENDACIÓN]: Banner dinámico provisto por el ViewModel
            AsyncImage(
                model = perfilUiState.currentBanner,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = painterResource(id = R.drawable.ic_launcher_background)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.85f))))
            )

            // Etiqueta PREMIUM (Solo si el prestador paga la suscripción)
            if (provider.isSubscribed) {
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 45.dp, end = 16.dp),
                    color = ProviderPremiumGold,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 6.dp
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("PREMIUM", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 10.sp)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                // 1. INFO PERFIL ACTIVO (CON ANIMACIÓN DE TAMAÑO Y POSICIÓN)
                // 🔥 [RECOMENDACIÓN]: Datos del perfil activo provistos por el ViewModel
                perfilUiState.activeInfo?.let { activeInfo ->
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
                        ) {
                            AsyncImage(
                                model = activeInfo.photo,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = activeInfo.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (activeInfo.isVerified) {
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = "Verificado",
                                        tint = Color(0xFF1DA1F2),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = activeInfo.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // 2. BURBUJAS DE NAVEGACIÓN (ANIMADAS POR COLAPSO)
                // 🔥 [RECOMENDACIÓN]: Lista de burbujas provista por el ViewModel
                AnimatedVisibility(
                    visible = collapseFraction < 0.5f,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                    modifier = Modifier.align(Alignment.End).padding(end = 20.dp, top = 16.dp)
                ) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(perfilUiState.displayBubbles) { item ->
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .clickable { coroutineScope.launch { pagerState.animateScrollToPage(item.first) } }
                            ) {
                                AsyncImage(
                                    model = item.second,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
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

        // --- FULLSCREEN OVERLAY ---
        if (fullscreenImageUrl != null) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black).zIndex(100f)
                    .clickable { fullscreenImageUrl = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(model = fullscreenImageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.Fit)
            }
        }

        // --- MODAL DE HORARIOS ---
        // 🔥 [RECOMENDACIÓN]: Gestión de estado de modales en el ViewModel
        if (perfilUiState.showHoursModal) {
            ModalBottomSheet(
                onDismissRequest = { onToggleHoursModal(false) },
                containerColor = Color(0xFF1A1A24),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                HoursBottomSheetContent(hoursText = perfilUiState.hoursContent, onClose = { onToggleHoursModal(false) })
            }
        }
    }
}

// =================================================================================
// --- SECCIONES DE CONTENIDO (ESTILO BENTO) ---
// =================================================================================

/**
 * Sección de Perfil Personal (Independiente)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IndependentBentoSection(
    provider: Provider,
    allCategories: List<CategoryEntity>,
    emails: List<String>,
    addresses: List<AddressProvider>,
    onOpenHours: () -> Unit,
    onImageClick: (String) -> Unit
) {
    val context = LocalContext.current
    Column {
        // TARJETA 1: DATOS PROFESIONALES
        Box(modifier = Modifier.fillMaxWidth().background(ProviderCardSurface).padding(24.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("DATOS PROFESIONALES", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onOpenHours, modifier = Modifier.background(Color.White.copy(0.05f), CircleShape)) {
                        Icon(Icons.Default.Schedule, null, tint = ProviderGeminiAccent)
                    }
                }
                Spacer(Modifier.height(16.dp))
                ProviderDataRow(emoji = "🎓", label = "TÍTULO / PROFESIÓN", value = provider.titulo ?: "Profesional")
                ProviderDataRow(emoji = "🆔", label = "CUIL / CUIT", value = provider.cuilCuit ?: "No especificado")
                ProviderDataRow(emoji = "📜", label = "MATRÍCULA", value = provider.matricula ?: "No posee")
                
                // 🔥 [RECOMENDACIÓN]: Listas procesadas provistas por el ViewModel
                emails.forEach { email ->
                    ProviderDataRow(emoji = "📧", label = "CORREO ELECTRÓNICO", value = email)
                }

                Spacer(Modifier.height(16.dp))
                Text("DIRECCIONES DE ATENCIÓN", style = MaterialTheme.typography.labelSmall, color = ProviderGeminiAccent, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                
                // 🔥 [RECOMENDACIÓN]: Listas procesadas provistas por el ViewModel
                addresses.forEach { addr ->
                    MapsStyleAddressCard(
                        address = addr,
                        onMapsClick = {
                            val uri = Uri.parse("geo:0,0?q=${Uri.encode(addr.fullString())}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // TARJETA 2: ESPECIALIDADES (ESTILO PREMIUM CON EMOJIS)
        if (provider.categories.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().background(ProviderCardSurface).padding(24.dp)) {
                Column {
                    Text("ESPECIALIDADES", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        provider.categories.forEach { catName ->
                            val categoryEntity = allCategories.find { it.name.equals(catName, ignoreCase = true) }
                            val catColor = CategoryVisuals.getColorFor(categoryEntity?.superCategory).let { Color(it) }
                            val catEmoji = categoryEntity?.icon ?: "🏷️"
                            
                            Surface(
                                color = catColor.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, catColor.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(catEmoji, fontSize = 14.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(catName, color = catColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // TARJETA 3: GALERÍA
        Box(modifier = Modifier.fillMaxWidth().background(ProviderCardSurface).padding(vertical = 24.dp)) {
            Column {
                Text("GALERÍA DE TRABAJOS", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 24.dp))
                Spacer(Modifier.height(16.dp))
                if (provider.galleryImages.isNotEmpty()) {
                    LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(provider.galleryImages) { img ->
                            AsyncImage(model = img, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(140.dp).clip(RoundedCornerShape(16.dp)).clickable { onImageClick(img) })
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 24.dp).background(Color.White.copy(0.05f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Text("Todavía no hay imágenes cargadas", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * Sección de Perfil de Empresa
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompanyBentoSection(
    company: CompanyProvider,
    allCategories: List<CategoryEntity>,
    addresses: List<AddressProvider>,
    onOpenHours: () -> Unit,
    onImageClick: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedEmployee by remember { mutableStateOf<EmployeeProvider?>(null) }

    Column {
        // TARJETA 1: INFO CORPORATIVA
        Box(modifier = Modifier.fillMaxWidth().background(ProviderCardSurface).padding(24.dp)) {
            Column {
                Text("DATOS DE LA EMPRESA", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                ProviderDataRow(emoji = "🏢", label = "RAZÓN SOCIAL", value = company.razonSocial)
                ProviderDataRow(emoji = "🆔", label = "CUIT", value = company.cuit)
                
                // RUBROS DE LA EMPRESA
                if (company.categories.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("RUBROS", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        company.categories.forEach { catName ->
                            val categoryEntity = allCategories.find { it.name.equals(catName, ignoreCase = true) }
                            val catColor = CategoryVisuals.getColorFor(categoryEntity?.superCategory).let { Color(it) }
                            val catEmoji = categoryEntity?.icon ?: "🏢"
                            
                            Surface(
                                color = catColor.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, catColor.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(catEmoji, fontSize = 12.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(catName, color = catColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("SOBRE NOSOTROS", style = MaterialTheme.typography.labelSmall, color = ProviderGeminiAccent, fontWeight = FontWeight.Bold)
                Text(company.description, color = Color.White.copy(0.8f), fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // TARJETA 2: SUCURSALES (Jerarquía Empresa -> Sucursal)
        // 🔥 [RECOMENDACIÓN]: Direcciones procesadas por el ViewModel
        addresses.forEachIndexed { index, addr ->
            val branch = company.branches.getOrNull(index)
            Box(modifier = Modifier.fillMaxWidth().background(ProviderCardSurface).padding(24.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(branch?.name?.uppercase() ?: "SUCURSAL", style = MaterialTheme.typography.labelSmall, color = ProviderGeminiAccent, fontWeight = FontWeight.Black)
                        IconButton(onClick = onOpenHours, modifier = Modifier.background(Color.White.copy(0.05f), CircleShape)) {
                            Icon(Icons.Default.Schedule, null, tint = ProviderGeminiAccent, modifier = Modifier.size(18.dp))
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    MapsStyleAddressCard(
                        address = addr,
                        onMapsClick = {
                            val uri = Uri.parse("geo:0,0?q=${Uri.encode(addr.fullString())}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        }
                    )

                    // Equipo Técnico
                    if (branch?.employees?.isNotEmpty() == true) {
                        Spacer(Modifier.height(20.dp))
                        Text("EQUIPO TÉCNICO", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(branch.employees) { emp ->
                                Surface(
                                    onClick = { selectedEmployee = emp },
                                    color = Color.White.copy(0.05f),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.width(180.dp)
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        AsyncImage(model = emp.photoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(40.dp).clip(CircleShape))
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(emp.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                                            Text(emp.position, fontSize = 10.sp, color = ProviderGeminiAccent)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Galería de la sucursal
                    if (branch?.galleryImages?.isNotEmpty() == true) {
                        Spacer(Modifier.height(20.dp))
                        Text("IMÁGENES DE ESTA SEDE", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(branch.galleryImages) { img ->
                                AsyncImage(model = img, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp)).clickable { onImageClick(img) })
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (selectedEmployee != null) {
        AlertDialog(
            onDismissRequest = { selectedEmployee = null },
            containerColor = Color(0xFF1A1A24),
            icon = { AsyncImage(model = selectedEmployee!!.photoUrl, contentDescription = null, modifier = Modifier.size(80.dp).clip(CircleShape), contentScale = ContentScale.Crop) },
            title = { Text("${selectedEmployee!!.name} ${selectedEmployee!!.lastName}", color = Color.White) },
            text = { Text(selectedEmployee!!.detail, color = Color.White.copy(0.7f), textAlign = TextAlign.Center) },
            confirmButton = { TextButton(onClick = { selectedEmployee = null }) { Text("Cerrar", color = ProviderGeminiAccent) } }
        )
    }
}

// =================================================================================
// --- COMPONENTES ATÓMICOS ---
// =================================================================================

@Composable
fun ProviderDataRow(emoji: String, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(0.05f)), contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun MapsStyleAddressCard(address: AddressProvider, onMapsClick: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (isExpanded) 180f else 0f)

    Surface(
        color = Color.Black.copy(0.3f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(ProviderGeminiAccent.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📍", fontSize = 22.sp)
                }
                
                Spacer(Modifier.width(14.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${address.calle} ${address.numero}",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "${address.localidad}, ${address.provincia}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }

            if (isExpanded) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(0.05f))
                Spacer(Modifier.height(16.dp))
                
                DetailAddressRow("País", address.pais.ifEmpty { "Argentina" })
                DetailAddressRow("Ciudad", address.localidad)
                DetailAddressRow("Provincia", address.provincia)
                DetailAddressRow("Cód. Postal", address.codigoPostal)
                
                if (address.latitude != null && address.longitude != null) {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().background(Color.White.copy(0.03f), RoundedCornerShape(8.dp)).padding(10.dp)
                    ) {
                        Column {
                            Text("COORDENADAS GPS", fontSize = 9.sp, fontWeight = FontWeight.Black, color = ProviderGeminiAccent, letterSpacing = 1.sp)
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
private fun DetailAddressRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun HoursBottomSheetContent(hoursText: String, onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, null, tint = ProviderGeminiAccent)
            Spacer(Modifier.width(12.dp))
            Text("HORARIOS DE ATENCIÓN", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
        }
        Spacer(Modifier.height(20.dp))
        Text(hoursText.ifEmpty { "Horario a convenir directamente con el prestador." }, color = Color.White.copy(0.8f))
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ProviderGeminiAccent),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("ENTENDIDO", fontWeight = FontWeight.Bold, color = Color.Black)
        }
        Spacer(Modifier.height(24.dp))
    }
}




@Preview(showBackground = true, name = "Perfil Prestador - Detalle Completo")
@Composable
fun PerfilPrestadorClientePreview() {
    val sampleProvider = PrestadorSampleDataFalso.generateMaverickProvider().toDomain()
    val sampleCategories = listOf(
        CategoryEntity(
            name = "Informatica",
            icon = "💻",
            superCategory = "Tecnología y Sistemas",
            isNew = false,
            isNewPrestador = false,
            isAd = false
        )
    )
    
    // Simulamos el estado de UI que el ViewModel generaría para el perfil principal
    val sampleUiState = PerfilUIState(
        activeInfo = ProviderActiveProfileInfo(
            id = 0,
            photo = sampleProvider.photoUrl,
            title = "${sampleProvider.name} ${sampleProvider.lastName}",
            subtitle = sampleProvider.titulo ?: "Profesional Independiente",
            isVerified = sampleProvider.isVerified
        ),
        currentBanner = sampleProvider.bannerImageUrl,
        hoursContent = sampleProvider.workingHours,
        totalPages = 1 + sampleProvider.companies.size,
        currentEmails = (listOfNotNull(sampleProvider.email) + sampleProvider.emails).distinct(),
        currentAddresses = (listOfNotNull(sampleProvider.address) + sampleProvider.addresses).distinctBy { it.id }
    )

    MyApplicationTheme {
        PerfilPrestadorClienteContent(
            providerState = sampleProvider,
            allCategories = sampleCategories,
            perfilUiState = sampleUiState,
            onUpdatePage = { _, _ -> },
            onToggleHoursModal = { _ -> },
            onBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Perfil Prestador - Cargando")
@Composable
fun PerfilPrestadorClienteLoadingPreview() {
    MyApplicationTheme {
        PerfilPrestadorClienteContent(
            providerState = null,
            allCategories = emptyList(),
            perfilUiState = PerfilUIState(),
            onUpdatePage = { _, _ -> },
            onToggleHoursModal = { _ -> },
            onBack = {}
        )
    }
}