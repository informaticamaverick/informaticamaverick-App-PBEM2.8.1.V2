package com.example.myapplication.prestador.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.ProfileState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val profileState by viewModel.profileState.collectAsState()
    val businessEntity by viewModel.businessEntity.collectAsState()
    val scrollState = rememberScrollState()

    val provider = (profileState as? ProfileState.Success)?.provider

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Default.Edit, contentDescription =
                            "Editar perfil")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surfaceColor
                )
            )
        },
        containerColor = colors.backgroundColor
    ) { padding ->
        if (provider == null ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primaryOrange)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            //Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(colors.primaryOrange, colors.primaryOrange.copy(alpha = 0.75f))
                        ),
                        RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    //Foto
                    Box(contentAlignment = Alignment.BottomEnd) {
                        val avatarBitmap = remember(provider.imageUrl) {
                            val b64 = provider.imageUrl
                            if (!b64.isNullOrBlank()) {
                                try {
                                    val bytes = android.util.Base64.decode(b64, android.util.Base64.DEFAULT)
                                    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                        ?.asImageBitmap()
                                } catch (e: Exception) { null }
                            } else null
                        }
                        if (avatarBitmap != null) {
                            Image(
                                bitmap = avatarBitmap,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, Color.White, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.3f))
                                    .border(3.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person, null,
                                    modifier = Modifier.size(56.dp),
                                    tint = Color.White
                                )
                            }
                        }

                        if (provider.verificado) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Verified, null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    //Nombre
                    Text(
                        "${provider.name} ${provider.apellido ?: ""}".trim(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    //Profesión
                    if (!provider.profesion.isNullOrBlank()) {
                        Text(provider.profesion, fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f))
                    }

                    Spacer(Modifier.height(8.dp))

                    //Rating
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //Rating
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, null, modifier = Modifier.size(14.dp), tint = Color(0xFFFBBF24))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    String.format("%.1f", if (provider.rating == 0f) 5.0f else provider.rating),
                                    fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White
                                )
                            }
                        }

                        if (provider.suscripto) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFFBBf24).copy(alpha = 0.3f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.WorkspacePremium, null, modifier = Modifier.size(14.dp), tint = Color(0xFFFBBF24))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Premium", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                //Descripcion
                if (!provider.description.isNullOrBlank()) {
                    ProfileInfoCard(
                        icon = Icons.Default.Info,
                        title = "Sobre mí",
                        iconColor = colors.primaryOrange
                    ) {
                        Text(
                            provider.description,
                            fontSize = 14.sp,
                            color = colors.textSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }

                //Servicios
                ProfileInfoCard(
                    icon = Icons.Default.Build,
                    title = "Servicios",
                    iconColor = Color(0xFF3B82F6)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProfileServiceRow(Icons.Default.Home, "Atención a domicilio", provider.vaDomicilio, colors)
                        ProfileServiceRow(Icons.Default.Videocam, "Atencion virtual", provider.atiendeVirtual, colors)
                        ProfileServiceRow(Icons.Default.LocalShipping, "Envíos", provider.envios, colors)
                        ProfileServiceRow(Icons.Default.Warning, "Urgencias24hs", provider.atencionUrgencias, colors)
                        ProfileServiceRow(Icons.Default.Store, "Atención en local", provider.turnosEnLocal, colors)
                        ProfileServiceRow(Icons.Default.Group, "Trabaja con equipo", provider.trabajaConOtros, colors)
                    }
                }

                //Contacto
                ProfileInfoCard(
                    icon = Icons.Default.ContactPhone,
                    title = "Contacto",
                    iconColor = Color(0xFF10B981)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (provider.email.isNotBlank()) {
                            ProfileDataRow(Icons.Default.Email, "Email", provider.email, colors)
                        }
                        if (provider.phone.isNotBlank()) {
                            ProfileDataRow(Icons.Default.Phone, "Teléfono", provider.phone, colors)
                        }
                        if (provider.address != null) {
                            ProfileDataRow(Icons.Default.LocationOn, "Dirección", provider.address?.fullString().orEmpty(), colors)
                        }
                    }
                }
                // ── EMPRESA ──────────────────────────────────────────────────────
                if (provider.tieneEmpresa &&
                    !provider.nombreEmpresa.isNullOrBlank()) {
                    ProfileInfoCard(
                        icon = Icons.Default.Business,
                        title = "Empresa",
                        iconColor = Color(0xFF8B5CF6)
                    ) {
                        Column(verticalArrangement =
                            Arrangement.spacedBy(8.dp)) {
                            ProfileDataRow(Icons.Default.Business, "Nombre",
                                provider.nombreEmpresa!!, colors)
                            if (!provider.cuitEmpresa.isNullOrBlank()) {
                                ProfileDataRow(Icons.Default.Badge, "CUIT",
                                    provider.cuitEmpresa!!, colors)
                            }
                            if (!provider.direccionEmpresa.isNullOrBlank())
                            {
                                ProfileDataRow(Icons.Default.LocationOn,
                                    "Dirección", provider.direccionEmpresa!!, colors)
                            }
                        }
                    }
                }
                // ── MATRÍCULA ────────────────────────────────────────────────────
                if (provider.tieneMatricula &&
                    !provider.matricula.isNullOrBlank()) {
                    ProfileInfoCard(
                        icon = Icons.Default.School,
                        title = "Habilitación Profesional",
                        iconColor = Color(0xFFF59E0B)
                    ) {
                        ProfileDataRow(Icons.Default.VerifiedUser,
                            "Matrícula", provider.matricula!!, colors)
                    }
                }

                // ── BOTÓN EDITAR ─────────────────────────────────────────────────
                Button(
                    onClick = onEditProfile,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor =
                        colors.primaryOrange)
                ) {
                    Icon(Icons.Default.Edit, null, modifier =
                        Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Editar Perfil", fontSize = 16.sp, fontWeight =
                        FontWeight.Bold)
                }

                Spacer(Modifier.height(16.dp))
            }

        }
    }
}

@Composable
private fun ProfileInfoCard(
    icon: ImageVector,
    title: String,
    iconColor: Color,
    conten: @Composable ColumnScope.() -> Unit
) {
    val colors = getPrestadorColors()
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                ){
                    Icon(icon, null, modifier = Modifier.size(20.dp), tint = iconColor)
                }

                Spacer(Modifier.width(10.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary)
            }
            Spacer(Modifier.height(12.dp))
            conten()
        }
    }
}


@Composable
private fun ProfileDataRow(
    icon: ImageVector,
    label: String,
    value: String,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = colors.textSecondary)
        Spacer(Modifier.width(8.dp))
        Text("$label:", fontSize = 13.sp, color = colors.textSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
    }
}

@Composable
private fun ProfileServiceRow(
    icon: ImageVector,
    label: String,
    active: Boolean,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon, null,
            modifier = Modifier.size(16.dp),
            tint = if (active) Color(0xFF10B981) else colors.textSecondary.copy(alpha = 0.4f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            fontSize = 13.sp,
            color = if (active) colors.textPrimary else colors.textSecondary.copy(alpha = 0.5f),
            fontWeight = if (active) FontWeight.Medium else FontWeight.Normal

        )
        Spacer(Modifier.weight(1f))
        if (active) {
            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp), tint = Color(0xFF10B981))
        }
    }
}