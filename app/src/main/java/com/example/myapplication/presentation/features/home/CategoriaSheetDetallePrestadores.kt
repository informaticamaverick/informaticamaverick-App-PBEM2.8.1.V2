package com.example.myapplication.presentation.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.common.extensions.wordStartsWithSmart
import com.example.myapplication.core.common.extensions.prepareForSearch
import com.example.myapplication.presentation.components.CompactCategoryCard
import com.example.myapplication.presentation.components.SheetEmergenteVertical
import com.example.myapplication.presentation.designsystem.components.*
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.global.BeBrainViewModel

/**
 * ==========================================================================================
 * --- 🏗️ COMPONENTE: PANEL DE DETALLES DE SUPERCATEGORÍA (SMART ORCHESTRATOR) ---
 * ==========================================================================================
 * Este componente inteligente actúa como puente entre el Cerebro (BeBrain) y el contenido visual.
 * Sigue el patrón Elite SSOT al recolectar el estado global de la categoría seleccionada.
 */
@Composable
fun SuperCategoryDetailsPanel(
    beViewModel: BeBrainViewModel,
    categoryViewModel: CategoryViewModel,
    onCategoryClick: (String) -> Unit
) {
    // --- SUSCRIPCIÓN AL CEREBRO (Elite SSOT) ---
    // Recolectamos la supercategoría seleccionada y la consulta de búsqueda actual.
    val selectedSuperCategory by beViewModel.selectedSuperCategory.collectAsStateWithLifecycle()
    val searchQuery by beViewModel.searchQuery.collectAsStateWithLifecycle()

    // 🔥 [OPTIMIZACIÓN ON-DEMAND]: Notificamos al Obrero que cargue las categorías de esta supercategoría.
    LaunchedEffect(selectedSuperCategory?.title) {
        categoryViewModel.selectSuperCategoryForDetail(selectedSuperCategory?.title)
    }

    val lazyItems by categoryViewModel.selectedSuperCategoryItems.collectAsStateWithLifecycle()
    val isSearching by categoryViewModel.isSearching.collectAsStateWithLifecycle()
    val isVisible = selectedSuperCategory != null

    // 🔥 SINCRONIZACIÓN DE HUD (Elite SSOT): Informamos al Mediador sobre la visibilidad de la Sheet.
    // La ocultación de la barra inferior se delega al Coordinador para evitar desincronizaciones visuales.
    LaunchedEffect(isVisible) {
        if (isVisible) {
            beViewModel.setUIBlocked(true)
            beViewModel.setSearchActive(true)
        } else {
            beViewModel.cerrarBeAssistantCompleto()
            beViewModel.setUIBlocked(false)
        }
        beViewModel.setSheetVisible(isVisible)
    }

    val homeShortcuts by categoryViewModel.getShortcuts("home").collectAsStateWithLifecycle(emptyList())
    val shortcutIds = remember(homeShortcuts) { homeShortcuts.map { it.targetId }.toSet() }

    // Delegamos la renderización al componente "Dumb" (StateRefined UI)
    SuperCategoryDetailsPanelContent(
        selectedSuperCategory = selectedSuperCategory,
        items = lazyItems, // Pasamos los items cargados bajo demanda
        isSearching = isSearching,
        searchQuery = searchQuery,
        onClose = { beViewModel.cerrarBeAssistantCompleto() },
        onCategoryClick = onCategoryClick,
        shortcutIds = shortcutIds,
        onToggleCategoryFavorite = { category ->
            // El Obrero (CategoryViewModel) procesa la lógica de favoritos
            categoryViewModel.toggleCategoryFavorite(category)
        },
        onManageShortcut = { id, type, add, label, icon ->
            categoryViewModel.manageShortcut("home", id, type, add, label, icon)
        }
    )
}

/**
 * SuperCategoryDetailsPanelContent: Representación visual sin estado del panel.
 * Implementa el diseño ROG Dark y la grilla de categorías filtradas.
 */
@Composable
fun SuperCategoryDetailsPanelContent(
    selectedSuperCategory: SuperCategory?,
    items: List<CategoryEntity> = emptyList(), // [NUEVO] Recibe los items bajo demanda
    isSearching: Boolean = false,
    searchQuery: String,
    onClose: () -> Unit,
    onCategoryClick: (String) -> Unit,
    shortcutIds: Set<String> = emptySet(),
    onToggleCategoryFavorite: (CategoryEntity) -> Unit = {},
    onManageShortcut: (String, String, Boolean, String?, String?) -> Unit = { _, _, _, _, _ -> }
) {
    val isVisible = selectedSuperCategory != null
    // --- [ELITE] ESTADO DE TRANSICIÓN: Prioridad a la animación ---
    var animationSettled by remember { mutableStateOf(false) }

    // Reseteamos el estado cuando la sheet se cierra para la próxima apertura
    LaunchedEffect(isVisible) {
        if (!isVisible) animationSettled = false
    }

    // --- 🚀 GESTIÓN INSTANTÁNEA DE ESTADO ---
    var lastSuperCat by remember { mutableStateOf<SuperCategory?>(null) }
    if (selectedSuperCategory != null) {
        lastSuperCat = selectedSuperCategory
    }
    val currentSuperCat = lastSuperCat

    Box(modifier = Modifier.fillMaxSize()) {
        SheetEmergenteVertical(
            isVisible = isVisible,
            onClose = onClose,
            title = currentSuperCat?.title ?: "",
            showHelperText = true,
            helperText = "Servicios encontrados en",
            emoji = currentSuperCat?.icon ?: "🔍",
            topOffset = 150.dp,
            initialAnchorIsFull = true,
            onEntryFinished = { animationSettled = true }
        ) {
            if (currentSuperCat != null) {
                // [OPTIMIZACIÓN]: Usamos 'items' (cargados por demanda) en lugar de 'superCat.items'
                val displayItems = remember(items, searchQuery) {
                    if (searchQuery.isEmpty()) {
                        items
                    } else {
                        val normQuery = searchQuery.prepareForSearch()
                        items.filter { it.name.wordStartsWithSmart(normQuery) }.sortedBy { it.name.lowercase() }
                    }
                }

                // [MODIFICACIÓN]: Column + Rows para evitar el error de "Infinite Height"
                // dentro de SheetEmergenteVertical (que ya implementa su propio scroll).
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!animationSettled || (items.isEmpty() && searchQuery.isEmpty()) || isSearching) {
                        // [NUEVO] SHIMMER SKELETON: Mientras el Obrero busca en Room o la Sheet se mueve
                        repeat(4) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                repeat(3) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        CategoryCardShimmer()
                                    }
                                }
                            }
                        }
                    } else {
                        displayItems.chunked(3).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { category ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        CompactCategoryCard(
                                            item = category,
                                            onClick = { onCategoryClick(category.name) },
                                            onToggleFavorite = { onToggleCategoryFavorite(category) },
                                            isShortcut = shortcutIds.contains(category.name),
                                            onManageShortcut = { add, label, icon ->
                                                onManageShortcut(category.name, "category", add, label, icon)
                                            }
                                        )
                                    }
                                }
                                // Rellenar espacios vacíos en la última fila para mantener alineación Bento
                                repeat(3 - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    // Espacio táctico para asegurar que el contenido no sea cubierto por elementos flotantes
                    Spacer(modifier = Modifier.height(130.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SuperCategoryDetailsPanelPreview() {
    val sampleCategories = listOf(
        CategoryEntity(name = "Plomería", icon = "🚰", superCategory = "Hogar", superCategoryIcon = "🏠", isNew = true, isNewPrestador = false, isAd = false, isFavorite = true ),
        CategoryEntity(name = "Electricidad", icon = "⚡", superCategory = "Hogar", superCategoryIcon = "🏠", isNew = false, isNewPrestador = false, isAd = false)
    )
    val sampleSuperCategory = SuperCategory(title = "Servicios del Hogar y Mantenimiento General", icon = "🏠", items = sampleCategories)
    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            SuperCategoryDetailsPanelContent(
                selectedSuperCategory = sampleSuperCategory,
                items = sampleCategories,
                searchQuery = "",
                onClose = {},
                onCategoryClick = {}
            )
        }
    }
}











