package com.example.myapplication.prestador.ui.pantallas.dashboard

import com.example.myapplication.uishared.estilos.SharedPalette
import android.net.Uri
import android.os.Build
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.core.dominio.modelos.Promocion
import com.example.myapplication.core.dominio.modelos.TipoCategoriaPromo
import com.example.myapplication.core.dominio.modelos.TipoPromocion
import com.example.myapplication.prestador.ui.pantallas.calendar.PrestadorCalendarScreen
import com.example.myapplication.prestador.ui.pantallas.chat.PrestadorChatScreen
import com.example.myapplication.prestador.ui.pantallas.market.MercadoConcursosScreen
import com.example.myapplication.prestador.ui.pantallas.catalogo.CatalogoScreen
import com.example.myapplication.prestador.ui.pantallas.notifications.NotificacionesScreen
import com.example.myapplication.prestador.ui.pantallas.promocion.PromocionFeedScreen
import com.example.myapplication.prestador.ui.pantallas.promocion.PromocionListaScreen
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.promocion.PrestadorPromocionFeedViewModel
import com.example.myapplication.core.dominio.mapeadores.PromocionMappers
import com.example.myapplication.prestador.viewmodel.chat.PrestadorChatViewModel
import com.example.myapplication.prestador.viewmodel.dashboard.NotificacionesViewModel
import com.example.myapplication.prestador.viewmodel.dashboard.PrestadorDashboardViewModel
import com.example.myapplication.prestador.viewmodel.promocion.PrePromocionViewModel
import com.example.myapplication.uishared.ui.components.InstagramPromoCard
import com.example.myapplication.prestador.ui.pantallas.dashboard.componentes.*
import com.example.myapplication.prestador.ui.pantallas.empresa.turnos.GestionTurnosTheme
import java.nio.channels.Selector

/**
 * --- PRESTADOR DASHBOARD (ORQUESTADOR ELITE v2026.FINAL) ---
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrestadorDashboardScreen(
    onNavigateToEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToPresupuesto: (idConcurso: String, idCliente: String) -> Unit = { _, _ -> },
    onNavigateToThemeDemo: () -> Unit = {},
    dashboardViewModel: PrestadorDashboardViewModel = hiltViewModel(),
    notificacionesViewModel: NotificacionesViewModel = hiltViewModel(),
    chatViewModel: PrestadorChatViewModel = hiltViewModel(),
    createPromotionViewModel: PrePromocionViewModel = hiltViewModel(),
    onNavigateToClientePerfil: (idUsuario: String) -> Unit = {},
    onNavigateToPromotionList: () -> Unit = {},
    onNavigateToPresupuestoList: () -> Unit = {},
    onNavigateToPresupuestoConfig: () -> Unit = {},
    onNavigateToHorariosConfig: () -> Unit = {},
    onNavigateToApariencia: () -> Unit = {},
    onNavigateToNotificaciones: () -> Unit = {},
    onNavigateToTerminos: () -> Unit = {},
    onNavigateToPrivacidad: () -> Unit = {},
    onNavigateToAcercaDe: () -> Unit = {},
    onNavigateToPaywall: () -> Unit = {},
    onNavigateToGestionTurnos: () -> Unit = {},
    onNavigateToGestionVisitas: () -> Unit = {},
    idConcursoInicial: String? = null,
) {
    val colores = getPrestadorColors()
    val estadoUi by dashboardViewModel.uiState.collectAsStateWithLifecycle()

    var pestanaSeleccionada by rememberSaveable { mutableIntStateOf(2) }
    var estaEnConversacion by remember { mutableStateOf(false) }
    var idUsuarioChatObjetivo by remember { mutableStateOf<String?>(null) }

    val feedScrollState = rememberLazyListState()

    var mostrarSheetPromocion by remember { mutableStateOf(false) }
    val estadoSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val conteoNoLeidosNotif by notificacionesViewModel.unreadCount.collectAsStateWithLifecycle(initialValue = 0)
    val conteoNoLeidosChat by chatViewModel.totalNoLeidos.collectAsStateWithLifecycle(initialValue = 0)

    LaunchedEffect(idConcursoInicial) {
        if (!idConcursoInicial.isNullOrBlank()) {
            pestanaSeleccionada = 5
            notificacionesViewModel.alHacerClickConcurso(idConcursoInicial)
        }
    }

    if (estadoUi.estaCargando) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF030712)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFFF7043))
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            // [ELITE]: Inicio es siempre oscuro por diseño (todas las tarjetas de
            // InicioComponents usan su propia paleta hardcodeada, sin seguir el sistema) —
            // `colores.backgroundColor` sí cambia con el tema claro/oscuro DEL DISPOSITIVO,
            // y en modo claro caía en el crema del tema base, asomando detrás de la barra
            // de navegación flotante. Fijo al mismo oscuro que ya usan las tarjetas.
            containerColor = Color(0xFF030712),
            contentWindowInsets = WindowInsets(0, 0, 0, 0), // 🔥 [ELITE] Inmersión total
            floatingActionButton = {
                if (!estaEnConversacion) {
                    when (pestanaSeleccionada) {
                        2 -> {
                            // 🔥 [DEBUG] Botón Provisional para Simular Pago
                            FloatingActionButton(
                                onClick = onNavigateToPaywall,
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White,
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.CreditCard, "Simular Pago")
                            }
                        }
                        4 -> {
                            // 🔥 [FIX]: naranja alineado al resto del rediseño oscuro (#FF5722, no
                            // el #F97316 viejo de `colores`), y ya no se colapsa con el scroll —
                            // se pidió sacar esa animación por distractiva.
                            ExtendedFloatingActionButton(
                                onClick = { pestanaSeleccionada = 6 },
                                containerColor = Color(0xFFFF5722),
                                contentColor = Color.White,
                                icon = { Icon(Icons.Default.History, null) },
                                text = { Text("Mis Publicaciones", fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                }
            },
            bottomBar = {
                if (!estaEnConversacion || pestanaSeleccionada != 1) {
                    BandaNavegacionPrestador(
                        seleccionada = pestanaSeleccionada,
                        conteoAlertas = conteoNoLeidosNotif,
                        conteoChat = conteoNoLeidosChat,
                        alSeleccionar = { pestanaSeleccionada = it }
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                AnimatedContent(
                    targetState = pestanaSeleccionada,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.92f, animationSpec = tween(300))
                    },
                    label = "tab_transition"
                ) { pestana ->
                    when (pestana) {
                        0 -> ContenidoPresupuestos(onNavigateToPresupuesto = onNavigateToPresupuesto, alVolverAInicio = { pestanaSeleccionada = 2 }, onNavegarConfig = onNavigateToPresupuestoConfig)
                        1 -> PrestadorChatScreen(
                            onBack = { pestanaSeleccionada = 2; idUsuarioChatObjetivo = null },
                            onInConversationChange = { estaEnConversacion = it },
                            onNavigateToClientePerfil = onNavigateToClientePerfil,
                            onNavigateToProfile = onNavigateToEditProfile,
                            onNavigateToCreateBudget = { onNavigateToPresupuesto("", it) },
                            initialChatUserId = idUsuarioChatObjetivo
                        )
                        2 -> InicioScreen(
                            state = estadoUi,
                            onNavigateToEditProfile = onNavigateToEditProfile,
                            onLogout = { dashboardViewModel.cerrarSesion(); onLogout() },
                            onNavigateToCalendar = { pestanaSeleccionada = 8 },
                            onNavigateToCreatePromo = { mostrarSheetPromocion = true },
                            onNavigateToPromotionList = { pestanaSeleccionada = 6 },
                            onNavigateToCrearPresupuesto = { onNavigateToPresupuesto("", "") },
                            onNavigateToCatalogo = { pestanaSeleccionada = 7 },
                            onNavigateToConcursos = { pestanaSeleccionada = 3 },
                            onNavigateToChat = { idCli -> idUsuarioChatObjetivo = idCli; pestanaSeleccionada = 1 },
                            onNavigateToPresupuestoConfig = onNavigateToPresupuestoConfig,
                            onNavigateToHorariosConfig = onNavigateToHorariosConfig,
                            onNavigateToApariencia = onNavigateToApariencia,
                            onNavigateToNotificaciones = onNavigateToNotificaciones,
                            onNavigateToTerminos = onNavigateToTerminos,
                            onNavigateToPrivacidad = onNavigateToPrivacidad,
                            onNavigateToAcercaDe = onNavigateToAcercaDe,
                            onNavigateToGestionTurnos = onNavigateToGestionTurnos,
                            onNavigateToGestionVisitas = onNavigateToGestionVisitas,
                        )
                        3 -> MercadoConcursosScreen(
                            onBack = { pestanaSeleccionada = 2 },
                            onNavigateToPresupuesto = { onNavigateToPresupuesto(it, "") },
                            onNavigateToPaywall = onNavigateToPaywall,
                            onNavigateToClientePerfil = onNavigateToClientePerfil
                        )
                        4 -> PromocionFeedScreen(
                            onBack = { pestanaSeleccionada = 2 },
                            onNavigateToProfile = onNavigateToClientePerfil,
                            scrollState = feedScrollState
                        )
                        5 -> NotificacionesScreen(
                            onNavigateBack = { pestanaSeleccionada = 2 },
                            onAccion = { ruta ->
                                if (ruta.startsWith("open_chat/")) {
                                    idUsuarioChatObjetivo = ruta.removePrefix("open_chat/"); pestanaSeleccionada = 1
                                } else if (ruta.startsWith("crear_presupuesto_concurso/")) {
                                    onNavigateToPresupuesto(ruta.removePrefix("crear_presupuesto_concurso/"), "")
                                }
                            }
                        )
                        6 -> PromocionListaScreen(
                            onBack = { pestanaSeleccionada = 4 },
                            onNavigateToCreatePromotion = { mostrarSheetPromocion = true }
                        )
                        7 -> CatalogoScreen(onBack = { pestanaSeleccionada = 2 })
                        8 -> PrestadorCalendarScreen(onBack = { pestanaSeleccionada = 2 })
                    }
                }

                if (mostrarSheetPromocion) {
                    ModalBottomSheet(
                        onDismissRequest = { mostrarSheetPromocion = false },
                        sheetState = estadoSheet,
                        containerColor = GestionTurnosTheme.CardBg,
                        contentColor = GestionTurnosTheme.TextPrimary,
                        dragHandle = { BottomSheetDefaults.DragHandle(color = GestionTurnosTheme.TextMuted) },
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    ) {
                        ContenidoCrearPromoSheet(
                            viewModel = createPromotionViewModel,
                            alCerrar = { mostrarSheetPromocion = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContenidoCrearPromoSheet(
    viewModel: PrePromocionViewModel,
    alCerrar: () -> Unit
) {
    val contexto = LocalContext.current
    val colors = GestionTurnosTheme
    val perfilActivo by viewModel.perfilActivo.collectAsStateWithLifecycle()
    val identidades by viewModel.todasMisIdentidades.collectAsStateWithLifecycle()

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var etiquetaPromocion by remember { mutableStateOf("") }
    var tipoSeleccionado by remember { mutableStateOf(TipoPromocion.PROMOCION) }
    var categoriaSeleccionada by remember { mutableStateOf(TipoCategoriaPromo.SERVICIO) }
    var imagenesSeleccionadas by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var descuento by remember { mutableStateOf("") }
    var modoPrevisualizacion by remember { mutableStateOf(false) }

    val estaCargando by viewModel.estaCargando.collectAsState()
    val errorPublicacion by viewModel.error.collectAsState()

    LaunchedEffect(errorPublicacion) {
        errorPublicacion?.let {
            android.widget.Toast.makeText(contexto, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.limpiarError()
        }
    }
    val rubros by viewModel.rubrosDisponibles.collectAsStateWithLifecycle()
    var rubroSeleccionadoId by remember { mutableStateOf<String?>(null) }

    var mostrarMenuIdentidades by remember { mutableStateOf(false) }

    // Auto-seleccionar el primer rubro si cambia el perfil o los rubros
    LaunchedEffect(rubros) {
        if (rubros.isNotEmpty()) {
            // Si el rubro actual no está en la lista del nuevo perfil, seleccionamos el primero
            if (rubroSeleccionadoId == null || rubros.none { it.first == rubroSeleccionadoId }) {
                rubroSeleccionadoId = rubros.first().first
            }
        }
    }

    val seleccionadorImagenes =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (imagenesSeleccionadas.size + uris.size <= 3) imagenesSeleccionadas =
                imagenesSeleccionadas + uris
            else android.widget.Toast.makeText(
                contexto,
                "Máximo 3 imágenes",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (modoPrevisualizacion) "Vista Previa" else "Nueva Publicación",
                fontSize = 18.sp, fontWeight = FontWeight.Black, color = colors.TextPrimary
            )
            IconButton(onClick = alCerrar) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = colors.TextMuted)
            }
        }
        Spacer(Modifier.height(16.dp))

        if (modoPrevisualizacion) {
            val promoVistaPrevia = Promocion(
                id = "preview",
                idPrestador = perfilActivo?.id ?: "preview_id",
                nombrePrestador = perfilActivo?.titulo ?: "App",
                urlFotoPrestador = com.example.myapplication.core.utilidades.ImageUtils.prepareForStorage(perfilActivo?.urlMiniatura)
                    ?: com.example.myapplication.core.utilidades.ImageUtils.prepareForStorage(perfilActivo?.urlFoto),
                tipo = tipoSeleccionado,
                tipoPromocion = categoriaSeleccionada,
                titulo = titulo,
                descripcion = descripcion,
                urlImagenes = imagenesSeleccionadas.map { it.toString() },
                porcentajeDescuento = descuento.toIntOrNull(),
                etiquetaPromocion = etiquetaPromocion
            )

            val uiModelVistaPrevia = PromocionMappers.aUiModel(promoVistaPrevia)

            Surface(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                color = colors.SurfaceInput
            ) {
                InstagramPromoCard(promotion = uiModelVistaPrevia, isClickable = false)
            }

            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { modoPrevisualizacion = false },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.TextPrimary),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                ) { Text("EDITAR DATOS", fontWeight = FontWeight.Black, fontSize = 12.sp) }

                Button(
                    onClick = {
                        perfilActivo?.let { perfil ->
                            viewModel.createPromotion(
                                idPrestador = perfil.id,
                                nombrePrestador = perfil.titulo,
                                urlFotoPrestador = com.example.myapplication.core.utilidades.ImageUtils.prepareForStorage(perfil.urlMiniatura)
                                    ?: com.example.myapplication.core.utilidades.ImageUtils.prepareForStorage(perfil.urlFoto),
                                tipo = tipoSeleccionado,
                                tipoCategoria = categoriaSeleccionada,
                                titulo = titulo,
                                descripcion = descripcion,
                                urisImagenes = imagenesSeleccionadas,
                                descuento = descuento,
                                etiquetaPromocion = etiquetaPromocion,
                                idCategorias = rubroSeleccionadoId?.let { setOf(it) } ?: emptySet(),
                                codigoPostal = perfil.codigoPostal,
                                idEmpresa = perfil.idEmpresa,
                                idSucursal = if (perfil.tipo == com.example.myapplication.core.dominio.modelos.TipoPrestador.SUCURSAL) perfil.id else null,
                                onSuccess = { alCerrar() }
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22D3EE)),
                    enabled = !estaCargando && perfilActivo != null
                ) {
                    if (estaCargando) CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black
                    )
                    else Text(
                        "PUBLICAR AHORA",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            // --- IDENTIDAD ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.SurfaceInput, RoundedCornerShape(10.dp))
                    .border(BorderStroke(1.dp, colors.BorderGlass), RoundedCornerShape(10.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "PUBLICAR COMO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = colors.BrandOrange
                )

                Surface(
                    onClick = { mostrarMenuIdentidades = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.04f),
                    border = BorderStroke(1.dp, colors.BorderGlass)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val img = perfilActivo?.urlMiniatura ?: perfilActivo?.urlFoto
                        if (img != null) {
                            AsyncImage(
                                model = img,
                                contentDescription = null,
                                Modifier.size(32.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                Modifier.size(32.dp)
                                    .background(colors.BrandOrange.copy(0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    perfilActivo?.titulo?.take(1) ?: "?",
                                    color = colors.BrandOrange,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                perfilActivo?.titulo ?: "Cargando...",
                                color = colors.TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                perfilActivo?.subtitulo ?: "Identidad App",
                                color = colors.TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                        if (identidades.size > 1) {
                            Icon(Icons.Default.ArrowDropDown, null, tint = colors.TextSecondary)
                        }
                    }
                }

                DropdownMenu(
                    expanded = mostrarMenuIdentidades,
                    onDismissRequest = { mostrarMenuIdentidades = false },
                    modifier = Modifier.fillMaxWidth(0.9f).background(colors.CardBg)
                ) {
                    identidades.forEach { iden ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val mini = iden.urlMiniatura ?: iden.urlFoto
                                    if (mini != null) {
                                        AsyncImage(
                                            model = mini,
                                            null,
                                            Modifier.size(24.dp).clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            iden.titulo,
                                            color = colors.TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            iden.subtitulo ?: "",
                                            color = colors.TextSecondary,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            },
                            onClick = {
                                viewModel.setPerfilPublicador(iden)
                                mostrarMenuIdentidades = false
                            }
                        )
                    }
                }
            }

            // --- TIPO DE PUBLICACION ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "TIPO DE PUBLICACIÓN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = colors.TextMuted
                )
                SelectorTipoSegmentado(tipoSeleccionado) { tipoSeleccionado = it }
            }

            // --- RUBRO VINCULO ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "RUBRO VINCULADO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = colors.TextMuted
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(rubros) { (id, nombre) ->
                        val seleccionado = rubroSeleccionadoId == id
                        Surface(
                            onClick = { rubroSeleccionadoId = id },
                            shape = RoundedCornerShape(20.dp),
                            color = if (seleccionado) colors.BrandOrange.copy(alpha = 0.14f) else Color.White.copy(
                                alpha = 0.04f
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (seleccionado) colors.BrandOrange else colors.BorderGlass
                            )
                        ) {
                            Text(
                                nombre,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (seleccionado) colors.BrandOrange else colors.TextSecondary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // --  CATEGORIA DE LA OFERTA ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "CATEGORÍA DE LA OFERTA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = colors.TextMuted
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OpcionCategoria(
                        tipo = TipoCategoriaPromo.SERVICIO,
                        seleccionado = categoriaSeleccionada == TipoCategoriaPromo.SERVICIO,
                        onClick = { categoriaSeleccionada = TipoCategoriaPromo.SERVICIO },
                        modifier = Modifier.weight(1f)
                    )
                    OpcionCategoria(
                        tipo = TipoCategoriaPromo.PRODUCTO,
                        seleccionado = categoriaSeleccionada == TipoCategoriaPromo.PRODUCTO,
                        onClick = { categoriaSeleccionada = TipoCategoriaPromo.PRODUCTO },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- FOTOS ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "FOTOS (${imagenesSeleccionadas.size}/3)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = colors.TextMuted
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Surface(
                            onClick = {
                                if (imagenesSeleccionadas.size < 3) seleccionadorImagenes.launch(
                                    "image/*"
                                )
                            },
                            modifier = Modifier.size(80.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.04f),
                            border = BorderStroke(1.dp, colors.BorderGlass)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AddAPhoto, null, tint = colors.TextSecondary)
                            }
                        }
                    }
                    items(imagenesSeleccionadas) { uri ->
                        Box(modifier = Modifier.size(80.dp)) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = {
                                    imagenesSeleccionadas =
                                        imagenesSeleccionadas.filter { it != uri }
                                },
                                modifier = Modifier.align(Alignment.TopEnd).size(22.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- DETALLES ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.SurfaceInput, RoundedCornerShape(10.dp))
                    .border(BorderStroke(1.dp, colors.BorderGlass), RoundedCornerShape(10.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "DETALLES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = colors.BrandOrange
                )

                OutlinedTextField(
                    value = etiquetaPromocion,
                    onValueChange = { if (it.length <= 15) etiquetaPromocion = it },
                    label = { Text("Etiqueta (Opcional)", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = campoPublicacionColors(colors)
                )

                OutlinedTextField(
                    value = titulo,
                    onValueChange = { if (it.length <= 60) titulo = it },
                    label = { Text("Título de la oferta", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = campoPublicacionColors(colors)
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { if (it.length <= 300) descripcion = it },
                    label = { Text("Descripción", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = campoPublicacionColors(colors)
                )
            }
            Button(
                onClick = { modoPrevisualizacion = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.BrandOrange),
                shape = RoundedCornerShape(12.dp),
                enabled = titulo.isNotBlank() && descripcion.isNotBlank()
            ) {
                Icon(Icons.Default.Visibility, null, tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text(
                    "VER VISTA PREVIA",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }

        }
    }
}

@Composable
private fun campoPublicacionColors(colors: GestionTurnosTheme) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = colors.TextPrimary,
    unfocusedTextColor = colors.TextPrimary,
    cursorColor = colors.BrandOrange,
    focusedBorderColor = colors.BrandOrange,
    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
    focusedLabelColor = colors.BrandOrange,
    unfocusedLabelColor = Color.Gray,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)







@Composable
fun OpcionCategoria(tipo:  TipoCategoriaPromo, seleccionado: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = GestionTurnosTheme
    Surface(
        onClick = onClick, modifier = modifier.height(52.dp), shape = RoundedCornerShape(10.dp),
        color = if (seleccionado) colors.BrandOrange.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, if (seleccionado) colors.BrandOrange else colors.BorderGlass)
    ) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text(tipo.icono, fontSize = 17.sp)
            Spacer(Modifier.width(8.dp))
            Text(text = tipo.etiqueta, fontSize = 12.sp, fontWeight = if (seleccionado) FontWeight.Black else FontWeight.Bold, color = if (seleccionado) colors.BrandOrange else colors.TextSecondary)
        }
    }
}

@Composable
fun SelectorTipoSegmentado(seleccionado: TipoPromocion, alSeleccionar: (TipoPromocion) -> Unit) {
    val colors = GestionTurnosTheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(colors.SurfaceInput, RoundedCornerShape(10.dp))
            .border(BorderStroke(1.dp, colors.BorderGlass), RoundedCornerShape(10.dp))
            .padding(4.dp)
    ) {
        TipoPromocion.entries.forEach { tipo ->
            val esSeleccionado = seleccionado == tipo
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(8.dp))
                    .background(if (esSeleccionado) colors.BrandOrange else Color.Transparent)
                    .clickable { alSeleccionar(tipo) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (tipo == TipoPromocion.HISTORIA) "Historia" else "Promoción",
                    color = if (esSeleccionado) Color.Black else colors.TextSecondary,
                    fontWeight = if (esSeleccionado) FontWeight.Black else FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun BandaNavegacionPrestador(seleccionada: Int, conteoAlertas: Int, conteoChat: Int, alSeleccionar: (Int) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(32.dp), color = Color(0xFF1A1C1E), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            ItemNavegacion(Icons.Outlined.Description, "Presup.", seleccionada == 0) { alSeleccionar(0) }
            ItemNavegacion(Icons.Outlined.Email, "Mensajes", seleccionada == 1, conteoChat > 0, conteoChat) { alSeleccionar(1) }
            Box(modifier = Modifier.size(60.dp).offset(y = (-4).dp).shadow(12.dp, CircleShape).background(brush = Brush.verticalGradient(colors = listOf(Color(0xFFFF7043), Color(0xFFFF5722))), shape = CircleShape).clickable { alSeleccionar(2) }, contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Home, "Inicio", tint = Color.White, modifier = Modifier.size(26.dp)) }
            ItemNavegacion(Icons.Outlined.Gavel, "Concursos", seleccionada == 3) { alSeleccionar(3) }
            ItemNavegacion(Icons.Outlined.Campaign, "Promos", seleccionada == 4) { alSeleccionar(4) }
        }
    }
}

@Composable
fun RowScope.ItemNavegacion(icono: ImageVector, etiqueta: String, seleccionado: Boolean, mostrarBadge: Boolean = false, conteo: Int = 0, onClick: () -> Unit) {
    val tinte = if (seleccionado) Color(0xFFFF7043) else Color.White.copy(alpha = 0.5f)
    Column(modifier = Modifier.weight(1f).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        BadgedBox(badge = { if (mostrarBadge && conteo > 0) { Badge(containerColor = Color(0xFFEF4444), contentColor = Color.White) { Text(if (conteo > 9) "9+" else conteo.toString(), fontSize = 9.sp) } } }) { Icon(icono, etiqueta, tint = tinte, modifier = Modifier.size(24.dp)) }
        Spacer(Modifier.height(4.dp))
        Text(etiqueta, fontSize = 9.sp, color = tinte, fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
    }
}

@Composable
fun ContenidoPresupuestos(onNavigateToPresupuesto: (String, String) -> Unit, alVolverAInicio: () -> Unit, onNavegarConfig: () -> Unit = {}) {
    com.example.myapplication.prestador.ui.pantallas.presupuesto.PresupuestosScreen(onVolver = alVolverAInicio, onCrearNuevo = { onNavigateToPresupuesto("", "") }, onVerDetalle = { }, onNavegarConfig = onNavegarConfig)
}

