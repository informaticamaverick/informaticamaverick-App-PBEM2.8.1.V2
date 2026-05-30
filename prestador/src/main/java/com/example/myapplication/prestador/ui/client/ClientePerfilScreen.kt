package com.example.myapplication.prestador.ui.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.uishared.components.rememberImageModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.draw.rotate
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import com.example.myapplication.prestador.data.model.ClienteDireccion
import com.example.myapplication.prestador.data.model.ClienteEmpresa
import com.example.myapplication.prestador.data.model.ClienteProfile
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.cliente.ClientePerfilUiState
import com.example.myapplication.prestador.viewmodel.cliente.ClientePerfilViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientePerfilScreen(
    onBack: () -> Unit = {},
    viewModel: ClientePerfilViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val uiState by viewModel.uiState.collectAsState()
    val refreshTick by viewModel.refreshTick.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(refreshTick) {
        if (refreshTick > 0) isRefreshing = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Perfil del cliente",
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surfaceColor
                )
            )
        },
        containerColor = colors.backgroundColor
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primaryOrange)
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = colors.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = uiState.error ?: "Error desconocido",
                            color = colors.textSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            else -> {
                PullToRefreshBox(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        isRefreshing = true
                        viewModel.refreshProfile()
                    },
                    indicator = {
                        PullToRefreshDefaults.Indicator(
                            state = pullToRefreshState,
                            isRefreshing = isRefreshing,
                            color = colors.primaryOrange
                        )
                    }
                ) {
                    ClientePerfilContent(
                        uiState = uiState,
                        paddingValues = paddingValues
                    )
                }
            }
        }
    }
}

@Composable
private fun ClientePerfilContent(
    uiState: ClientePerfilUiState,
    paddingValues: PaddingValues
) {
    val profile = uiState.profile

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { ClienteHeaderSection(profile = profile) }
        item {
            ClienteInfoSection(
                phone = profile.phoneNumber,
                email = profile.email
            )
        }
        if (profile.bio.isNotBlank()) {
            item { ClienteBioSection(bio = profile.bio) }
        }
        if (profile.personalAddresses.isNotEmpty()) {
            item { ClienteDireccionesSection(addresses = profile.personalAddresses) }
        }
        if (profile.galleryImages.isNotEmpty()) {
            item { ClienteGaleriaSection(images = profile.galleryImages) }
        }
        if (profile.companies.isNotEmpty()) {
            item { ClienteEmpresasSection(companies = profile.companies) }
        }
        item {
            ClienteAppointmentsSection()
        }
    }
}

@Composable
private fun ClienteHeaderSection(profile: ClienteProfile) {
    val colors = getPrestadorColors()

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(colors.surfaceElevated)
        ) {
            if (profile.bannerImageUrl != null) {
                AsyncImage(
                    model = rememberImageModel(profile.bannerImageUrl),
                    contentDescription = "Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp)
                    .offset(y = 36.dp)
            ) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = Color(0xFFF97316),
                    shadowElevation = 4.dp
                ) {
                    if (profile.photoUrl != null) {
                        AsyncImage(
                            model = rememberImageModel(profile.photoUrl),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = profile.fullName.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(44.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = profile.fullName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            if (profile.displayName.isNotBlank() && profile.displayName != profile.fullName) {
                Text(
                    text = "@${profile.displayName}",
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (profile.isVerified) {
                    ClienteBadge(icon = Icons.Default.Verified, label = "Verificado", color = Color(0xFF3B82F6))
                }
                if (profile.isOnline) {
                    ClienteBadge(icon = Icons.Default.Circle, label = "En línea", color = Color(0xFF10B981))
                }
                if (profile.isSubscribed) {
                    ClienteBadge(icon = Icons.Default.Star, label = "Premium", color = Color(0xFFF59E0B))
                }
            }
        }
    }
}

@Composable
private fun ClienteBadge(icon: ImageVector, label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

@Composable
private fun ClienteInfoSection(phone: String, email: String) {
    val colors = getPrestadorColors()

    val clienteSdesde = ""

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Información de contacto", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            if (phone.isNotBlank()) {
                ClienteInfoRow(icon = Icons.Default.Phone, label = "Teléfono", value = phone)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (email.isNotBlank()) {
                ClienteInfoRow(icon = Icons.Default.Email, label = "Email", value = email)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (clienteSdesde.isNotBlank()) {
                ClienteInfoRow(icon = Icons.Default.CalendarToday, label = "Cliente desde", value = clienteSdesde)
            }
        }
    }
}

@Composable
private fun ClienteInfoRow(icon: ImageVector, label: String, value: String) {
    val colors = getPrestadorColors()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = colors.textSecondary)
            Text(text = value, fontSize = 14.sp, color = colors.textPrimary)
        }
    }
}

@Composable
private fun ClienteBioSection(bio: String) {
    val colors = getPrestadorColors()
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Sobre el cliente", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = bio, fontSize = 14.sp, color = colors.textSecondary, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun ClienteDireccionesSection(addresses: List<ClienteDireccion>) {
    val colors = getPrestadorColors()
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Direcciones", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            addresses.forEach { dir ->
                Row(
                    modifier = Modifier.padding(bottom = 10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = dir.label.ifBlank { "📍" }, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "${dir.calle} ${dir.numero}".trim(), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                        Text(text = "${dir.localidad}, ${dir.provincia}", fontSize = 12.sp, color = colors.textSecondary)
                        if (dir.codigoPostal.isNotBlank()) {
                            Text(text = "CP ${dir.codigoPostal}", fontSize = 12.sp, color = colors.textSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClienteGaleriaSection(images: List<String>) {
    val colors = getPrestadorColors()
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(text = "Galería", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.padding(bottom = 10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(images) { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier.size(110.dp).clip(RoundedCornerShape(10.dp)).background(colors.surfaceElevated),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun ClienteEmpresasSection(companies: List<ClienteEmpresa>) {
    val colors = getPrestadorColors()
    var expandedIndex by remember { mutableStateOf(-1) }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = Color(0xFFF97316),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Empresas",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            companies.forEachIndexed { index, empresa ->
                EmpresaColapsable(
                    empresa = empresa,
                    colors = colors,
                    isExpanded = expandedIndex == index,
                    onToggle = { expandedIndex = if (expandedIndex == index) -1 else index }
                )
                if (index < companies.lastIndex) {
                    HorizontalDivider(
                        color = colors.divider,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmpresaColapsable(
    empresa: ClienteEmpresa,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "arrow"
    )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = empresa.razonSocial.ifBlank { empresa.name },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                if (empresa.cuit.isNotBlank()) {
                    Text(text = "CUIT: ${empresa.cuit}", fontSize = 12.sp, color = colors.textSecondary)
                }
            }
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFFF97316),
                    modifier = Modifier.rotate(rotation)
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(tween(300)) + fadeIn(tween(300)),
            exit = shrinkVertically(tween(300)) + fadeOut(tween(300))
        ) {
            Column(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
                if (empresa.email.isNotBlank()) {
                    ClienteInfoRow(icon = Icons.Default.Email, label = "Email", value = empresa.email)
                    Spacer(modifier = Modifier.height(6.dp))
                }
                if (empresa.phoneNumber.isNotBlank()) {
                    ClienteInfoRow(icon = Icons.Default.Phone, label = "Teléfono", value = empresa.phoneNumber)
                    Spacer(modifier = Modifier.height(6.dp))
                }
                if (empresa.branches.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Sucursales", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    empresa.branches.forEach { sucursal ->
                        Row(modifier = Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.Top) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (sucursal.isMainBranch) "${sucursal.name} (Casa Central)" else sucursal.name,
                                    fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary
                                )
                                val dir = sucursal.address
                                val dirText = "${dir.calle} ${dir.numero}".trim()
                                if (dirText.isNotBlank()) Text(text = dirText, fontSize = 12.sp, color = colors.textSecondary)
                                if (dir.localidad.isNotBlank()) Text(text = "${dir.localidad}, ${dir.provincia}", fontSize = 12.sp, color = colors.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClienteAppointmentsSection() {
    val colors = getPrestadorColors()
    val appointments = emptyList<Any>()
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Historial de citas", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Spacer(modifier = Modifier.weight(1f))
                Surface(shape = CircleShape, color = Color(0xFFF97316).copy(alpha = 0.15f)) {
                    Text(
                        text = "0",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF97316),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Sin citas registradas", fontSize = 14.sp, color = colors.textSecondary)
        }
    }
}