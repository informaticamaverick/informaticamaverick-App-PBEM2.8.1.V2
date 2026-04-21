package com.example.myapplication.presentation.client

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.presentation.components.CompactCategoryCard
import com.example.myapplication.presentation.components.SheetActionButton
import com.example.myapplication.presentation.components.SheetEmergenteVertical
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun SuperCategoryDetailsPanel(
    beViewModel: BeBrainViewModel,
    categoryViewModel: CategoryViewModel,
    onCategoryClick: (String) -> Unit
) {
    val selectedSuperCategory by beViewModel.selectedSuperCategory.collectAsStateWithLifecycle()
    val searchQuery by beViewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by beViewModel.searchResults.collectAsStateWithLifecycle()
    val isVisible = selectedSuperCategory != null

    LaunchedEffect(isVisible) {
        if (isVisible) {
            beViewModel.setUIBlocked(true)
            beViewModel.setSearchActive(true)
            beViewModel.setBottomBarVisible(false)
        } else {
            beViewModel.setUIBlocked(false)
            beViewModel.setSearchActive(false)
            beViewModel.setBottomBarVisible(true)
            beViewModel.updateSearchQuery("")
        }
    }

    SuperCategoryDetailsPanelContent(
        selectedSuperCategory = selectedSuperCategory,
        searchQuery = searchQuery,
        searchResults = searchResults,
        onClose = { beViewModel.selectSuperCategory(null) },
        onCategoryClick = onCategoryClick,
        onToggleCategoryFavorite = { category -> categoryViewModel.toggleCategoryFavorite(category) }
    )
}

@Composable
fun SuperCategoryDetailsPanelContent(
    selectedSuperCategory: SuperCategory?,
    searchQuery: String,
    searchResults: BeBrainViewModel.SearchResult,
    onClose: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onToggleCategoryFavorite: (CategoryEntity) -> Unit = {}
) {
    val isVisible = selectedSuperCategory != null

    // --- 🚀 GESTIÓN INSTANTÁNEA DE ESTADO: Evita "Header Vacío" durante la animación ---
    var lastSuperCat by remember { mutableStateOf<SuperCategory?>(null) }
    if (selectedSuperCategory != null) {
        lastSuperCat = selectedSuperCategory
    }
    val currentSuperCat = lastSuperCat

    // [SECCIÓN: CONTENEDOR MAESTRO] - Delegamos el fondo ROG Dark y el comportamiento de cierre
    // al nuevo sistema integrado en SheetEmergenteVertical.
    Box(modifier = Modifier.fillMaxSize()) {
        // --- 🏗️ ESTRUCTURA: NUEVO MOLDE SHEET EMERGENTE VERTICAL ---
        // Nota: El fondo ROG Dark ahora está integrado dentro de SheetEmergenteVertical 
        // y respeta el topOffset para no tapar al asistente.
        SheetEmergenteVertical(
            isVisible = isVisible,
            onClose = onClose,
            title = currentSuperCat?.title ?: "",
            // helperText = if (searchQuery.isNotEmpty()) "BUSCANDO EN GRUPO" else "EXPLORAR GRUPO", // --- 💬 COMENTADO POR PEDIDO ---
            showHelperText = true, // --- 🚫 DESACTIVAMOS LA VISIBILIDAD ---
            helperText = "Servicios encontrados en",
            emoji = currentSuperCat?.icon ?: "🔍",
            topOffset = 150.dp, // --- 📐 ALTURA TÁCTICA: JUSTO DEBAJO DE LA BÚSQUEDA ---

/**
            actions = {
                   SheetActionButton(
                    icon = "🌪️",
                    label = "Filtro",
                    onClick = { /* TODO: Implementar filtros */ },
                    active = true
                )
            }
**/
        ) {
            // --- 📊 CONTENIDO: GRILLA DE CATEGORÍAS ---
            currentSuperCat?.let { superCat ->
                val displayItems = remember(superCat.items, searchResults, searchQuery) {
                    if (searchQuery.isEmpty()) superCat.items else searchResults.categories
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = displayItems, key = { it.name }) { category ->
                        CompactCategoryCard(
                            item = category,
                            onClick = { onCategoryClick(category.name) },
                            onToggleFavorite = { onToggleCategoryFavorite(category) }
                        )
                    }
                    // Espacio táctico para no ser cubierto por elementos flotantes inferiores
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(130.dp))
                    }
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
                searchQuery = "",
                searchResults = BeBrainViewModel.SearchResult.Empty,
                onClose = {},
                onCategoryClick = {}
            )
        }
    }
}

