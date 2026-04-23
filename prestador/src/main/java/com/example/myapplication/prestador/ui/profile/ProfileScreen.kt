package com.example.myapplication.prestador.ui.profile

import android.text.Layout
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.prestador.data.model.BranchProvider
import com.example.myapplication.prestador.data.model.CompanyProvider
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.ProfileState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onSettings: () -> Unit = {},
    viewModel: EditProfileViewModel = hiltViewModel()
){
    val colors = getPrestadorColors()
    val profileState by viewModel.profileState.collectAsState()
    val provider = (profileState as? ProfileState.Success)?.provider
    val listState = rememberLazyListState()
    var showCompanyView by remember { mutableStateOf(false) }

    val topBarAlpha by animateFloatAsState(
        targetValue = (listState.firstVisibleItemScrollOffset.toFloat() / 250f).coerceIn(0f, 1f),
        label = "topBarAlpha"
    )

    Scaffold(
        containerColor = colors.backgroundColor,
        bottomBar = {
            ProfileBottomBar(
                onEdit = onEditProfile,
                onSettings = onSettings,
                colors = colors
            )
        },
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = topBarAlpha },
                color = colors.surfaceColor,
                shadowElevation = if (topBarAlpha > 0.01f) 4.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = colors.primaryOrange)
                    }
                    Text(
                        "Mi Perfil",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Default.Edit, null, tint = colors.primaryOrange)
                    }
                }
            }
        }
    ) { padding ->
        if (provider == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primaryOrange)
            }
            return@Scaffold
        }

        val firstCompany = provider.companies.firstOrNull()

        if (showCompanyView && firstCompany != null) {
            CompanyDetailView(
                company = firstCompany,
                providerImageUrl = provider.imageUrl,
                paddingValues = padding,
                onBack = { showCompanyView = false },
                colors = colors
            )
            return@Scaffold
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── 1. HEADER BANNER ─────────────────────────────────────────
            item {
                ProfileBannerHeaderView(
                    name = "${provider.name} ${provider.apellido}".trim(),
                    profesion = provider.profesion ?: "",
                    imageUrl = provider.imageUrl,
                    bannerImageUrl = provider.bannerImageUrl,
                    rating = provider.rating,
                    isSubscribed = provider.suscripto,
                    isVerified = provider.verificado,
                    paddingValues = padding,
                    onBack = onBack,
                    onEdit = onEditProfile,
                    toggleImageUrl = firstCompany?.photoUrl,
                    onToggle = if (firstCompany != null) ({ showCompanyView = true }) else null,
                    colors = colors
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ── 2. DATOS PROFESIONALES ───────────────────────────────────
            item {
                ProfileSectionCard(Icons.Default.Work, "Datos Profesionales", colors.primaryOrange, colors) {
                    if (!provider.profesion.isNullOrBlank()) {
                        ProfileInfoRow("🎓", "Profesión", provider.profesion!!, colors)
                        Spacer(Modifier.height(8.dp))
                    }
                    if (!provider.dniCuit.isNullOrBlank()) {
                        ProfileInfoRow("🆔", "DNI / CUIT", provider.dniCuit!!, colors)
                        Spacer(Modifier.height(8.dp))
                    }
                    if (provider.tieneMatricula && !provider.matricula.isNullOrBlank()) {
                        ProfileInfoRow("📜", "Matrícula", provider.matricula!!, colors)
                        Spacer(Modifier.height(8.dp))
                    }
                    if (provider.email.isNotBlank()) {
                        ProfileInfoRow("📧", "Email", provider.email, colors)
                        Spacer(Modifier.height(8.dp))
                    }
                    if (provider.phone.isNotBlank()) {
                        ProfileInfoRow("📞", "Teléfono", provider.phone, colors)
                        Spacer(Modifier.height(8.dp))
                    }
                    // Direcciones
                    val todasDirecciones = listOfNotNull(provider.address) +
                        provider.addresses.filter { it.id != "principal" }
                    todasDirecciones.forEachIndexed { index, addr ->
                        ProfileInfoRow(
                            "📍",
                            if (index == 0) "Dirección" else "Otra dirección",
                            addr.fullString(),
                            colors
                        )
                        if (index < todasDirecciones.lastIndex) Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // ── 3. SOBRE MÍ ──────────────────────────────────────────────
            if (!provider.description.isNullOrBlank()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    ProfileSectionCard(Icons.Default.Info, "Sobre mí", Color(0xFF3B82F6), colors) {
                        Text(
                            provider.description,
                            fontSize = 14.sp,
                            color = colors.textSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // ── 4. CATEGORÍAS ────────────────────────────────────────────
            if (provider.categories.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    ProfileSectionCard(Icons.Default.Category, "Especialidades", Color(0xFF00897B), colors) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            provider.categories.forEach { cat ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF00897B).copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, Color(0xFF00897B).copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        cat,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 12.sp,
                                        color = Color(0xFF00897B),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── 5. SERVICIOS ACTIVOS ─────────────────────────────────────
            val activeServices = buildList {
                if (provider.vaDomicilio)      add("🏠" to "Atención a domicilio")
                if (provider.atiendeVirtual)   add("💻" to "Atención virtual")
                if (provider.envios)           add("🚚" to "Realiza envíos")
                if (provider.atencionUrgencias)add("⚠️" to "Urgencias 24hs")
                if (provider.turnosEnLocal)    add("🏪" to "Atención en local")
                if (provider.trabajaConOtros)  add("👥" to "Trabaja con equipo")
            }
            if (activeServices.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    ProfileSectionCard(Icons.Default.Build, "Servicios", Color(0xFF3B82F6), colors) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            activeServices.forEach { (emoji, label) ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF3B82F6).copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.25f))
                                ) {
                                    Text(
                                        "$emoji $label",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 12.sp,
                                        color = Color(0xFF3B82F6),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── 6. GALERÍA ───────────────────────────────────────────────
            if (provider.galleryImages.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    ProfileSectionCard(Icons.Default.PhotoLibrary, "Galería de trabajos", Color(0xFFF59E0B), colors) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(provider.galleryImages) { imageData ->
                                val model: Any = if (imageData.startsWith("http")) imageData
                                else try {
                                    val bytes = android.util.Base64.decode(imageData, android.util.Base64.DEFAULT)
                                    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: imageData
                                } catch (e: Exception) { imageData }
                                AsyncImage(
                                    model = model,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            // ── 7. ESPACIADO FINAL ───────────────────────────────────────
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ── HEADER CON BANNER ────────────────────────────────────────────────────────
@Composable
private fun ProfileBannerHeaderView(
    name: String,
    profesion: String,
    imageUrl: String?,
    bannerImageUrl: String?,
    rating: Float,
    isSubscribed: Boolean,
    isVerified: Boolean,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    toggleImageUrl: String? = null,
    onToggle: (() -> Unit)? = null,
    colors: PrestadorColors
){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
    ) {
        // Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
        ) {
            val bannerModel: Any? = bannerImageUrl?.takeIf { it.isNotEmpty() }
            when {
                bannerModel != null && (bannerModel as String).startsWith("http") -> {
                    AsyncImage(
                        model = bannerModel,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                bannerModel != null -> {
                    val bmp = remember(bannerModel) {
                        try {
                            val b = android.util.Base64.decode(bannerModel as String, android.util.Base64.DEFAULT)
                            android.graphics.BitmapFactory.decodeByteArray(b, 0, b.size)?.asImageBitmap()
                        } catch (e: Exception) { null }
                    }
                    if (bmp != null) {
                        Image(bmp, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        BannerGradient(colors)
                    }
                }
                else -> BannerGradient(colors)
            }
            // Fade inferior
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, colors.backgroundColor.copy(alpha = 0.85f)),
                        startY = 100f
                    )
                )
            )
            // Botón volver (solo back, sin lápiz)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(Modifier.size(36.dp), CircleShape, Color.Black.copy(alpha = 0.35f)) {
                    IconButton(onClick = onBack, Modifier.fillMaxSize()) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // Foto de perfil (solapando banner)
        Box(
            modifier = Modifier
                .padding(top = 105.dp, start = 20.dp)
                .size(90.dp)
                .clip(CircleShape)
                .border(3.dp, colors.primaryOrange, CircleShape)
        ) {
            ProfilePhoto(imageUrl = imageUrl, colors = colors)
        }

        // Nombre + Profesión + Badges (abajo)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    name.ifEmpty { "Prestador" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                if (isVerified) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.Verified, null, Modifier.size(18.dp), Color(0xFF10B981))
                }
            }
            if (profesion.isNotEmpty()) {
                Text(profesion, fontSize = 13.sp, color = colors.textSecondary)
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surfaceColor,
                    border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.2f))
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, Modifier.size(12.dp), Color(0xFFFBBF24))
                        Spacer(Modifier.width(3.dp))
                        Text(
                            String.format("%.1f", if (rating == 0f) 5.0f else rating),
                            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary
                        )
                    }
                }
                if (isSubscribed) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFBBF24).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.5f))
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.WorkspacePremium, null, Modifier.size(12.dp), Color(0xFFFBBF24))
                            Spacer(Modifier.width(3.dp))
                            Text("Premium", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                        }
                    }
                }
            }
        }

        // Botón toggle empresa (bottom-end) — solo si hay empresa
        if (onToggle != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 12.dp)
                    .size(52.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    .clickable { onToggle() }
            ) {
                ProfilePhoto(imageUrl = toggleImageUrl, colors = colors)
            }
        }
    }
}

@Composable
private fun BannerGradient(colors: PrestadorColors) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(
                listOf(
                    colors.primaryOrange.copy(alpha = 0.8f),
                    Color(0xFFFF5722).copy(alpha = 0.5f),
                    colors.backgroundColor
                )
            )
        )
    )
}

@Composable
private fun ProfilePhoto(imageUrl: String?, colors: PrestadorColors) {
    when {
        !imageUrl.isNullOrEmpty() && imageUrl.startsWith("http") -> {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        !imageUrl.isNullOrEmpty() -> {
            val bmp = remember(imageUrl) {
                try {
                    val b = android.util.Base64.decode(imageUrl, android.util.Base64.DEFAULT)
                    android.graphics.BitmapFactory.decodeByteArray(b, 0, b.size)?.asImageBitmap()
                } catch (e: Exception) { null }
            }
            if (bmp != null) {
                Image(bmp, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                PhotoPlaceholder(colors)
            }
        }
        else -> PhotoPlaceholder(colors)
    }
}

@Composable
private fun PhotoPlaceholder(colors: PrestadorColors) {
    Box(
        Modifier.fillMaxSize().background(colors.primaryOrange.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Person, null, Modifier.size(48.dp), tint = colors.primaryOrange.copy(alpha = 0.5f))
    }
}

// ── CARD SECCIÓN GENÉRICA ────────────────────────────────────────────────────
@Composable
private fun ProfileSectionCard(
    icon: ImageVector,
    title: String,
    iconColor: Color,
    colors: PrestadorColors,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, Modifier.size(20.dp), iconColor)
                }
                Spacer(Modifier.width(10.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

// ── FILA DE DATO CON EMOJI ───────────────────────────────────────────────────
@Composable
private fun ProfileInfoRow(
    emoji: String,
    label: String,
    value: String,
    colors: PrestadorColors
) {
    Row(verticalAlignment = Alignment.Top) {
        Text(emoji, fontSize = 14.sp, modifier = Modifier.width(26.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 13.sp, color = colors.textPrimary)
        }
    }
}

// ── CARD EMPRESA CON SUCURSALES Y EQUIPO ─────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileEmpresaCard(company: CompanyProvider, colors: PrestadorColors) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header empresa
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Business, null, Modifier.size(20.dp), Color(0xFF8B5CF6))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    company.name.ifBlank { "Empresa" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colors.textPrimary
                )
            }

            Spacer(Modifier.height(12.dp))

            if (company.razonSocial.isNotBlank()) {
                ProfileInfoRow("🏢", "Razón Social", company.razonSocial, colors)
                Spacer(Modifier.height(8.dp))
            }
            if (company.cuit.isNotBlank()) {
                ProfileInfoRow("🆔", "CUIT", company.cuit, colors)
                Spacer(Modifier.height(8.dp))
            }
            if (company.description.isNotBlank()) {
                Text(company.description, fontSize = 13.sp, color = colors.textSecondary, lineHeight = 18.sp)
                Spacer(Modifier.height(8.dp))
            }

            // Rubros empresa
            if (company.categories.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    company.categories.forEach { cat ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF8B5CF6).copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.25f))
                        ) {
                            Text(
                                cat,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                color = Color(0xFF8B5CF6)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Sucursales
            company.branches.forEachIndexed { index, branch ->
                BranchSection(index = index, branch = branch, colors = colors)
            }
        }
    }
}

@Composable
private fun BranchSection(index: Int, branch: BranchProvider, colors: PrestadorColors) {
    val branchName = branch.name.ifBlank { if (index == 0) "Casa Central"
    else "Sucursal ${index + 1}" }
    val dir = branch.address.fullString()

    Text(branchName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
    Spacer(Modifier.height(12.dp))

    if (dir.isNotBlank()) {
        BranchInfoRow(
            icon = Icons.Default.LocationOn,
            iconColor = Color(0xFFE53935),
            label = "Dirección",
            value = dir,
            colors = colors
        )
    }
    if (branch.workingHours.isNotBlank()) {
        if (dir.isNotBlank()) Spacer(Modifier.height(12.dp))
        BranchInfoRow(
            icon = Icons.Default.Schedule,
            iconColor = Color(0xFF8B5CF6),
            label = "Horario",
            value = branch.workingHours,
            colors = colors
        )
    }
    if (branch.employees.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        Text("EQUIPO DE TRABAJO", fontSize = 11.sp, fontWeight = FontWeight.Bold,
            color =  colors.textSecondary, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(branch.employees) { emp ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape)
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!emp.photoUrl.isNullOrEmpty()) {
                            AsyncImage(emp.photoUrl, null,
                                Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {

                            Icon(Icons.Default.Person, null, Modifier.size(24.dp), Color(0xFF8B5CF6))

                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(emp.name, fontSize = 10.sp, color = colors.textPrimary,
                        textAlign = TextAlign.Center, maxLines = 1)
                    if(emp.position.isNotBlank()) {

                        Text(emp.position, fontSize = 9.sp, color = colors.textSecondary,
                            textAlign = TextAlign.Center, maxLines = 1)

                    }
                }
            }
        }
    }
    if (branch.galleryImages.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        Text("GALERÍA DE ESTA SEDE", fontSize = 11.sp, fontWeight = FontWeight.Bold,
            color = colors.textSecondary, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(branch.galleryImages) { imageData ->
                val model: Any = if (imageData.startsWith("http")) imageData
                else try {
                    val b = android.util.Base64.decode(imageData, android.util.Base64.DEFAULT)
                    android.graphics.BitmapFactory.decodeByteArray(b, 0, b.size) ?: imageData
                } catch (e: Exception) { imageData }
                AsyncImage(model, null, Modifier.size(80.dp).clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop)
            }
        }
    }
}

@Composable
private fun BranchInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    colors: PrestadorColors
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center

        ) {
            Icon(icon, null, Modifier.size(18.dp), tint = iconColor)
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = iconColor, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 14.sp, color = colors.textPrimary, lineHeight = 20.sp)
        }
    }
}

// ── VISTA DETALLE DE EMPRESA ─────────────────────────────────────────────────
@Composable
private fun CompanyDetailView(
    company: CompanyProvider,
    providerImageUrl: String?,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    colors: PrestadorColors
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // ── HEADER EMPRESA ───────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(295.dp)
            ) {
                // Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                ) {
                    val bannerModel = company.bannerImageUrl?.takeIf { it.isNotEmpty() }
                    when {
                        bannerModel != null && bannerModel.startsWith("http") ->
                            AsyncImage(bannerModel, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        bannerModel != null -> {
                            val bmp = remember(bannerModel) {
                                try {
                                    val b = android.util.Base64.decode(bannerModel, android.util.Base64.DEFAULT)
                                    android.graphics.BitmapFactory.decodeByteArray(b, 0, b.size)?.asImageBitmap()
                                } catch (e: Exception) { null }
                            }
                            if (bmp != null) Image(bmp, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            else BannerGradientCompany(colors)
                        }
                        else -> BannerGradientCompany(colors)
                    }
                    // Fade inferior
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, colors.backgroundColor.copy(alpha = 0.85f)),
                                startY = 100f
                            )
                        )
                    )
                    // Botón volver
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = paddingValues.calculateTopPadding())
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Surface(Modifier.size(36.dp), CircleShape, Color.Black.copy(alpha = 0.35f)) {
                            IconButton(onClick = onBack, Modifier.fillMaxSize()) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                // Logo empresa (solapando banner)
                Box(
                    modifier = Modifier
                        .padding(top = 148.dp, start = 20.dp)
                        .size(90.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color(0xFF8B5CF6), CircleShape)
                ) {
                    val logoUrl = company.photoUrl
                    if (!logoUrl.isNullOrEmpty() && logoUrl.startsWith("http")) {
                        AsyncImage(logoUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Box(
                            Modifier.fillMaxSize().background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Business, null, Modifier.size(48.dp), tint = Color(0xFF8B5CF6).copy(alpha = 0.6f))
                        }
                    }
                }

                // Nombre empresa y razón social
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, bottom = 10.dp)
                ) {
                    Text(
                        company.name.ifBlank { "Empresa" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    if (company.razonSocial.isNotBlank()) {
                        Text(company.razonSocial, fontSize = 13.sp, color = colors.textSecondary)
                    }
                }

                // Botón toggle: foto del prestador (volver a perfil personal)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 12.dp)
                        .size(52.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        .clickable { onBack() }
                ) {
                    ProfilePhoto(imageUrl = providerImageUrl, colors = colors)
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

        // ── DATOS DEL NEGOCIO ────────────────────────────────────────────
        item {
            ProfileSectionCard(Icons.Default.Business, "Datos del Negocio", Color(0xFF8B5CF6), colors) {
                if (company.name.isNotBlank()) {
                    ProfileInfoRow("🏬", "Nombre Comercial", company.name, colors)
                    Spacer(Modifier.height(8.dp))
                }
                if (company.razonSocial.isNotBlank()) {
                    ProfileInfoRow("🏢", "Razón Social", company.razonSocial, colors)
                    Spacer(Modifier.height(8.dp))
                }
                if (company.cuit.isNotBlank()) {
                    ProfileInfoRow("🆔", "CUIT", company.cuit, colors)
                    Spacer(Modifier.height(8.dp))
                }
                if (company.description.isNotBlank()) {
                    Text(company.description, fontSize = 13.sp, color = colors.textSecondary, lineHeight = 18.sp)
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }

        // ── SUCURSALES ───────────────────────────────────────────────────
        if (company.branches.isNotEmpty()) {
            item {
                BranchesPager(branches = company.branches, colors = colors)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BranchesPager(
    branches: List<BranchProvider>,
    colors: PrestadorColors
) {
    val pagerState = rememberPagerState(pageCount = { branches.size })

    Column {
        // Tabs con nombre de cada sucursal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            branches.forEachIndexed { index, branch ->
                val isSelected = pagerState.currentPage == index
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(0xFF8B5CF6) else colors.surfaceColor,
                    border = if (!isSelected) BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.2f)) else null
                ) {
                    Text(
                        text = branch.name.ifBlank { if (index == 0) "Casa Central" else "Sucursal ${index + 1}" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color.White else colors.textSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Pager de sucursales
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    BranchSection(index = page, branch = branches[page], colors = colors)
                }
            }
        }

        // Indicadores de puntos (solo si hay más de 1)
        if (branches.size > 1) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                branches.forEachIndexed { index, _ ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color(0xFF8B5CF6)
                                else colors.textSecondary.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun BannerGradientCompany(colors: PrestadorColors) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(
                listOf(
                    Color(0xFF8B5CF6).copy(alpha = 0.8f),
                    Color(0xFF6D28D9).copy(alpha = 0.5f),
                    colors.backgroundColor
                )
            )
        )
    )
}

// ── BARRA INFERIOR DE ACCIONES ───────────────────────────────────────────────
@Composable
private fun ProfileBottomBar(
    onEdit: () -> Unit,
    onSettings: () -> Unit,
    colors: PrestadorColors
) {
    Surface(
        color = colors.surfaceColor,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileBottomBarButton(
                icon = Icons.Default.Edit,
                label = "EDITAR",
                tint = colors.primaryOrange,
                onClick = onEdit
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(colors.textSecondary.copy(alpha = 0.2f))
            )
            ProfileBottomBarButton(
                icon = Icons.Default.Settings,
                label = "AJUSTES",
                tint = colors.textPrimary,
                onClick = onSettings
            )
        }
    }
}

@Composable
private fun ProfileBottomBarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(3.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = tint,
            letterSpacing = 0.5.sp
        )
    }
}