package com.example.myapplication.ui.pantallas.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.myapplication.ui.componentes.PrestadorBusinessCard
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.viewmodel.home.FavoritosViewModel
import com.example.myapplication.viewmodel.profile.ArmadorUsuarioViewModel
import com.example.myapplication.ui.componentes.be.vm.BeCerebroViewModel
import com.example.myapplication.core.dominio.modelos.TipoPrestador

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.myapplication.uishared.estilos.AppTypography
import com.example.myapplication.core.dominio.modelos.PrestadorDominio

/**
 * --- PANEL DE FAVORITOS (DESACOPLADO v2026) ---
 * [ELITE]: Componente independiente con su propio ViewModel para optimizar la Home.
 */
@Composable
fun FavoritosPanel(
    navController: NavHostController,
    onClose: () -> Unit,
    viewModel: FavoritosViewModel = hiltViewModel(),
    userViewModel: ArmadorUsuarioViewModel = hiltViewModel(),
    brainViewModel: BeCerebroViewModel = hiltViewModel()
) {
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteProviders.collectAsStateWithLifecycle()
    val accountState by userViewModel.ecosistemaMaestro.collectAsStateWithLifecycle()
    val idPerfilActivo by brainViewModel.coordinador.idPerfilSeleccionado.collectAsStateWithLifecycle()

    val cyberShape = CutCornerShape(topStart = 32.dp, bottomStart = 32.dp)

    val beConfig = remember { 
        com.example.myapplication.ui.componentes.be.modelos.ContextoHUD.FAVORITOS_SCREEN.crearConfiguracionBase()
    }

    DisposableEffect(Unit) {
        viewModel.navCoordinador.registrarPantalla(beConfig)
        onDispose {
            viewModel.navCoordinador.removerPantalla(beConfig.id)
        }
    }

    BackHandler(enabled = true) {
        onClose()
    }

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(320.dp)
            .drawBehind {
                // 🔥 [ELITE] Dibujado Quirúrgico: Lado Izquierdo (Simetría con Menú)
                val strokeWidth = 1.2.dp.toPx()
                val radio = 32.dp.toPx()
                val h = size.height
                
                val path = Path().apply {
                    moveTo(radio, 0f)
                    lineTo(0f, radio)
                    lineTo(0f, h - radio)
                    lineTo(radio, h)
                }

                val gradiente = Brush.verticalGradient(
                    listOf(
                        SharedPalette.ElectricCyan,
                        Color.Transparent,
                        SharedPalette.DeepRed
                    )
                )

                drawPath(
                    path = path,
                    brush = gradiente,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                drawPath(
                    path = path,
                    brush = gradiente,
                    style = Stroke(width = strokeWidth * 2f, cap = StrokeCap.Round),
                    alpha = 0.2f
                )
            },
        color = SharedPalette.V2DeepVoid,
        shape = cyberShape
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // CABECERA ELITE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .statusBarsPadding(), 
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "MIS FAVORITOS", 
                        style = AppTypography.HeaderTitle.copy(
                            fontSize = 18.sp,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        "ACCESO RÁPIDO",
                        style = AppTypography.HeaderTitle.copy(
                            fontSize = 9.sp,
                            color = SharedPalette.ElectricCyan,
                            letterSpacing = 1.5.sp
                        )
                    )
                }
                
                IconButton(
                    onClick = onClose, 
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(0.05f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                ) { 
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp)) 
                }
            }
            
            HorizontalDivider(color = Color.White.copy(0.05f), thickness = 1.dp)
            
            // LISTA DE PRESTADORES
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp), 
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (favorites.isEmpty()) {
                    item { 
                        Column(
                            modifier = Modifier.fillParentMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "🔍", 
                                fontSize = 42.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                "AÚN NO TIENES FAVORITOS", 
                                style = AppTypography.HeaderTitle.copy(
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    letterSpacing = 1.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Mantén presionado un prestador para agregarlo aquí.", 
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.Gray.copy(0.6f)
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                            )
                        }
                    }
                } else {
                    items(items = favorites, key = { it.id }) { service ->
                        val esFavorito = favoriteIds.contains(service.id)
                        PrestadorBusinessCard(
                            provider = service,
                            user = accountState,
                            onAvatarClick = {
                                onClose()
                                navController.navigate(
                                    Screen.PerfilPrestador.createRoute(
                                        service.id, 
                                        service.idEmpresa, 
                                        if (service.tipo == TipoPrestador.SUCURSAL) service.id else null
                                    )
                                )
                            },
                            onChatClick = { sender ->
                                onClose()
                                val clientBranchId = if (sender != null && sender.id != "personal") sender.id else null
                                navController.navigate(
                                    Screen.Chat.createRoute(
                                        providerId = service.id,
                                        branchId = if (service.tipo == TipoPrestador.SUCURSAL) service.id else null,
                                        clientBranchId = clientBranchId
                                    )
                                )
                            },
                            accentColor = SharedPalette.ElectricCyan,
                            idPerfilActivo = idPerfilActivo,
                            isShortcut = esFavorito,
                            onManageShortcut = { add ->
                                viewModel.manageShortcut(service.id, add, service.titulo, service.urlMiniatura?.toString())
                            },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
