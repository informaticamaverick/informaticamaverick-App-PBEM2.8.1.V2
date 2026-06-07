package com.example.myapplication.presentation.features.home

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.data.local.entity.UserEntity
import com.example.myapplication.core.domain.model.AddressUnico
import com.example.myapplication.data.model.ProviderDisplayModel
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.designsystem.components.*
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.global.BeBrainViewModel

/**
 * HomeScreenContentStateless: La representación visual "tonta" movida desde HomeScreenViewModel.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenClienteV4(
    navController: NavHostController,
    bannerItems: List<AccordionBanner>,
    favoriteProviders: List<ProviderDisplayModel>,
    superCategories: List<SuperCategory>,
    sortedIndividualCategories: List<CategoryEntity>,
    activeSortFilters: Set<String>,
    dropdownItems: List<DropdownItemData> = emptyList(),
    temperature: String,
    weatherEmoji: String,
    weatherDescription: String,
    cityName: String,
    isSuperCategoryView: Boolean,
    showWeatherDetails: Boolean,
    showFavoritesPanel: Boolean,
    searching: Boolean,
    searchQuery: String,
    userFromBrain: UserEntity?,
    activeName: String,
    activePhoto: Any?,
    selectedProfileId: String?,
    activeAddress: AddressUnico?,
    isRefreshing: Boolean,
    isLoadingCategories: Boolean = false,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onTriggerAction: (String) -> Unit,
    onSelectSuperCategory: (SuperCategory?) -> Unit,
    onToggleSuperCategoryFavorite: (String) -> Unit,
    onToggleCategoryFavorite: (CategoryEntity) -> Unit,
    onSetWeatherDetailsVisible: (Boolean) -> Unit,
    onSetFavoritesPanelVisible: (Boolean) -> Unit,
    onRefreshLocation: () -> Unit,
    onGpsToggle: () -> Unit,
    onProfileSelected: (String?) -> Unit,
    shortcutIds: Set<String> = emptySet(),
    superCategoryFavorites: Set<String> = emptySet(),
    onManageShortcut: (String, String, Boolean, String?, String?) -> Unit = { _, _, _, _, _ -> },
    searchAnimationSettled: Boolean,
    onWeatherClick: () -> Unit,
    onLocationSelected: (AddressUnico) -> Unit,
    detailsPanel: @Composable () -> Unit
) {
    var showLocationPopup by remember { mutableStateOf(false) }
    var showProfilePopup by remember { mutableStateOf(false) }

    val pullToRefreshState = rememberPullToRefreshState()
    var scrollAccumulator by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newScroll = (scrollAccumulator - delta).coerceIn(0f, 180f)
                val consumed = scrollAccumulator - newScroll
                scrollAccumulator = newScroll
                return if (scrollAccumulator >= 180f && delta < 0) Offset.Zero else Offset(0f, consumed)
            }
        }
    }

    val hideFraction = remember {
        derivedStateOf { (scrollAccumulator / 180f).coerceIn(0f, 1f) }
    }

    val gridState = rememberLazyGridState()
    val individualGridState = rememberLazyGridState()
    val listState = rememberLazyListState()
    val scrollState = rememberLazyListState()
    
    // 🔥 [OPTIMIZACIÓN MAVERICK]: Usamos derivedStateOf para que isScrolling no dispare recomposiciones en cada pixel.
    val isScrolling by remember { 
        derivedStateOf { 
            gridState.isScrollInProgress || 
            individualGridState.isScrollInProgress || 
            listState.isScrollInProgress || 
            scrollState.isScrollInProgress 
        } 
    }

    LaunchedEffect(activeSortFilters, searching) {
        if (!searching) {
            listState.scrollToItem(0)
            gridState.scrollToItem(0)
            individualGridState.scrollToItem(0)
            scrollState.scrollToItem(0)
        }
    }

    Scaffold(containerColor = MaverickColors.V2TechSurface) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding().coerceAtLeast(0.dp))
                .nestedScroll(nestedScrollConnection)
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = pullToRefreshState,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = isRefreshing,
                        color = MaverickColors.ElectricCyan,
                        containerColor = MaverickColors.ROG_Dark_Bg.copy(alpha = 0.9f),
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopHeaderSectionContentV2(
                        navController = navController,
                        user = userFromBrain,
                        activeName = activeName,
                        activePhoto = activePhoto,
                        isPersonalProfile = selectedProfileId == null,
                        selectedProfileId = selectedProfileId,
                        temperature = temperature,
                        weatherEmoji = weatherEmoji,
                        weatherDescription = weatherDescription,
                        activeAddress = activeAddress ?: AddressUnico(id = "searching", ownerName = "...", label = "...", calle = "...", localidad = cityName, codigoPostal = "", isCompany = false, latitude = 0.0, longitude = 0.0),
                        onWeatherClick = onWeatherClick,
                        onRefreshLocation = onRefreshLocation,
                        onGpsToggle = onGpsToggle,
                        onLocationSelected = onLocationSelected,
                        onProfileSelected = onProfileSelected,
                        onLogout = onLogout,
                        userFromBrain = userFromBrain,
                        showWeatherDialog = showWeatherDetails,
                        cityName = cityName,
                        onSetWeatherDetailsVisible = onSetWeatherDetailsVisible,
                        showLocationPopupHoisted = showLocationPopup,
                        showProfilePopupHoisted = showProfilePopup,
                        onLocationPopupToggle = { showLocationPopup = it },
                        onProfilePopupToggle = { showProfilePopup = it }
                    )

                    AnimatedVisibility(
                        visible = bannerItems.isNotEmpty() && !searching,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 12.dp * (1f - hideFraction.value))
                                .graphicsLayer {
                                    alpha = 1f - hideFraction.value
                                    translationY = -20.dp.toPx() * hideFraction.value
                                }
                                .height(150.dp * (1f - hideFraction.value))
                        ) {
                            PremiumLensCarouselV3(
                                items = bannerItems,
                                isPaused = isScrolling || showWeatherDetails || searching,
                                onItemClick = { banner ->
                                    if (banner.service != null) {
                                        navController.navigate("perfil_prestador/${banner.service.id}")
                                    } else {
                                        banner.originalCategory?.let {
                                            val encodedName = Uri.encode(it.name)
                                            if (encodedName.isNotEmpty()) {
                                                navController.navigate("result_busqueda/$encodedName")
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }

                    ListaMoldeV2(
                        modifier = Modifier.fillMaxSize(),
                        titulo = if (searching && searchQuery.isNotEmpty() && sortedIndividualCategories.isNotEmpty()) "Encontré estos servicios" else "Busca y explora servicios",
                        subtitulo = "Módulo de Exploración",
                        state = scrollState,
                        containerColor = MaverickColors.EliteSurface,
                        acciones = { fraction ->
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                //horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                var showMenu by remember { mutableStateOf(false) }

                                Box(contentAlignment = Alignment.Center) {
                                    BotonCabeceraAccion(
                                        onClick = { showMenu = !showMenu },
                                        icon = if (showMenu) Icons.Rounded.Close else Icons.Rounded.Menu,
                                        color = if (showMenu) MaverickColors.DeepRed else Color.White,
                                        collapseFraction = fraction
                                    )
                                    MoldeEliteBottomSheetV2(
                                        visible = showMenu,
                                        onDismissRequest = { showMenu = false },
                                        items = dropdownItems,
                                        activeFilters = activeSortFilters,
                                        shortcutIds = emptySet<String>(),
                                        onToggle = { actionId: String ->
                                            onTriggerAction(actionId)
                                            showMenu = false
                                        },
                                        onManageShortcuts = { _: String, _: Boolean -> }
                                    )
                                }
                            }
                        }
                    ) { perfil ->
                        val columns = if (isSuperCategoryView) 2 else 3

                        if (isLoadingCategories || (searching && searchQuery.isNotEmpty() && !searchAnimationSettled && sortedIndividualCategories.isEmpty())) {
                            items(4) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    repeat(columns) { index ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            if (isSuperCategoryView) SuperCategoryCardShimmer() else CategoryCardShimmer()
                                        }
                                    }
                                }
                            }
                        } else if (isSuperCategoryView) {
                            if (searching && searchQuery.isNotEmpty()) {
                                val searchRows = sortedIndividualCategories.chunked(columns)
                                items(
                                    items = searchRows,
                                    key = { row -> row.joinToString("-") { it.name } }
                                ) { rowItems ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)) {
                                        rowItems.forEach { category ->
                                            Box(modifier = Modifier
                                                .weight(1f)
                                                .padding(vertical = 5.dp, horizontal = 2.dp)
                                            ) {
                                                CompactCategoryCard(
                                                    item = category,
                                                    onClick = {
                                                        val encodedName = Uri.encode(category.name)
                                                        if (encodedName.isNotEmpty()) {
                                                            navController.navigate("result_busqueda/$encodedName")
                                                        }
                                                    },
                                                    onToggleFavorite = { onToggleCategoryFavorite(category) },
                                                    isShortcut = shortcutIds.contains(category.name),
                                                    onManageShortcut = { isAdd, label, icon ->
                                                        onManageShortcut(category.name, "category", isAdd, label, icon)
                                                    },
                                                    showSuperCategoryLabel = true,
                                                    isSuperCategoryFavorite = superCategoryFavorites.contains(category.superCategory)
                                                )
                                            }
                                        }
                                        repeat(columns - rowItems.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            } else {
                                val superCatRows = superCategories.chunked(columns)
                                items(
                                    items = superCatRows,
                                    key = { row -> row.joinToString("-") { it.title } }
                                ) { rowItems ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)) {
                                        rowItems.forEach { superCat ->
                                            Box(modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 2.dp, vertical = 3.dp)
                                            ) {
                                                BentoSuperCategoryCard(
                                                    superCategory = superCat,
                                                    emoji = superCat.icon,
                                                    height = 130.dp,
                                                    onClick = { onSelectSuperCategory(superCat) },
                                                    onToggleFavorite = { onToggleSuperCategoryFavorite(superCat.title) },
                                                    isShortcut = shortcutIds.contains(superCat.title),
                                                    onManageShortcut = { isAdd, label, icon ->
                                                        onManageShortcut(superCat.title, "supercategory", isAdd, label, icon)
                                                    }
                                                )
                                            }
                                        }
                                        repeat(columns - rowItems.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        } else {
                            val individualRows = sortedIndividualCategories.chunked(columns)
                            items(
                                items = individualRows,
                                key = { row -> row.joinToString("-") { it.name } }
                            ) { rowItems ->
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)) {
                                    rowItems.forEach { category ->
                                        Box(modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 5.dp, horizontal = 2.dp)
                                        ) {
                                            CompactCategoryCard(
                                                item = category,
                                                onClick = {
                                                    val encodedName = Uri.encode(category.name)
                                                    if (encodedName.isNotEmpty()) {
                                                        navController.navigate("result_busqueda/$encodedName")
                                                    }
                                                },
                                                onToggleFavorite = { onToggleCategoryFavorite(category) },
                                                isShortcut = shortcutIds.contains(category.name),
                                                onManageShortcut = { isAdd, label, icon ->
                                                    onManageShortcut(category.name, "category", isAdd, label, icon)
                                                },
                                                showSuperCategoryLabel = searching && searchQuery.isNotEmpty(),
                                                isSuperCategoryFavorite = superCategoryFavorites.contains(category.superCategory)
                                            )
                                        }
                                    }
                                    repeat(columns - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showFavoritesPanel) {
                Box(modifier = Modifier.fillMaxSize().zIndex(11f).background(Color.Black.copy(alpha = 0.65f)).clickable(null, null) { onSetFavoritesPanelVisible(false) })
            }
            AnimatedVisibility(visible = showFavoritesPanel, enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }, modifier = Modifier.align(Alignment.CenterEnd).zIndex(12f)) {
                FavoritesPanel(
                    navController = navController,
                    favorites = favoriteProviders,
                    onClose = { onSetFavoritesPanelVisible(false) },
                    onManageShortcut = onManageShortcut,
                    favoriteIds = shortcutIds
                )
            }

            Box(modifier = Modifier.align(Alignment.BottomCenter).zIndex(50f)) {
                detailsPanel()
            }
        }
    }
}

@Composable
fun FavoritesPanel(
    navController: NavHostController,
    favorites: List<ProviderDisplayModel>,
    onClose: () -> Unit,
    onManageShortcut: (String, String, Boolean, String?, String?) -> Unit = { _, _, _, _, _ -> },
    favoriteIds: Set<String> = emptySet()
) {
    Surface(
        modifier = Modifier.fillMaxHeight().width(320.dp),
        color = MaverickColors.StealthGray,
        tonalElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(20.dp).statusBarsPadding(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mis Favoritos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                IconButton(onClick = onClose, modifier = Modifier.background(Color.White.copy(0.1f), CircleShape)) { Icon(Icons.Default.Close, null, tint = Color.White) }
            }
            HorizontalDivider(color = Color.White.copy(0.1f))
            LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (favorites.isEmpty()) {
                    item { Text("No tienes favoritos guardados.", modifier = Modifier.fillMaxWidth().padding(32.dp), color = Color.Gray, textAlign = TextAlign.Center) }
                } else {
                    items(items = favorites, key = { it.id }) { service ->
                        PrestadorCardV3(
                            provider = service,
                            isCompact = false,
                            onClick = {
                                onClose()
                                navController.navigate("perfil_prestador/${service.id}")
                            },
                            onChatClick = {
                                onClose()
                                navController.navigate("chat?providerId=${service.id}")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            isShortcut = favoriteIds.contains(service.id),
                            onManageShortcut = { add ->
                                onManageShortcut(service.id, "provider", add, service.title, service.typeEmoji)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, backgroundColor = 0xFF07050E)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenPreview() {
    val sampleCategories = listOf(
        CategoryEntity(name = "Limpieza", icon = "🧹", superCategory = "Hogar", isNew = true, isNewPrestador = false, isAd = false),
        CategoryEntity(name = "Plomería", icon = "🪠", superCategory = "Hogar", isNew = false, isNewPrestador = false, isAd = false),
        CategoryEntity(name = "Electricidad", icon = "⚡", superCategory = "Hogar", isNew = false, isNewPrestador = false, isAd = false),
        CategoryEntity(name = "Peluquería", icon = "✂️", superCategory = "Belleza", isNew = false, isNewPrestador = false, isAd = false),
        CategoryEntity(name = "Masajes", icon = "💆", superCategory = "Belleza", isNew = false, isNewPrestador = false, isAd = false)
    )

    // Simulamos una supercategoría con color dinámico (Rosa Suave)
    val sampleSuperCategory = SuperCategory(
        title = "Hogar y Construcción", 
        icon = "🏠", 
        totalItems = 3, 
        isFavorite = true,
        color = 0xFFFFD1D1
    )
    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            SuperCategoryDetailsPanelContent(
                selectedSuperCategory = sampleSuperCategory,
                items = sampleCategories.take(3),
                searchQuery = "",
                onClose = {},
                onCategoryClick = {}
            )
        }
    }
}
