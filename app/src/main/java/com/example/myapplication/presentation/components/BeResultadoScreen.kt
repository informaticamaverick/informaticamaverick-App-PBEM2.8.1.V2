
/**
package com.example.myapplication.presentation.components

import com.example.myapplication.presentation.features.home.*

import com.example.myapplication.presentation.features.auth.*

import com.example.myapplication.presentation.features.home.*

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.domain.model.Provider
import com.example.myapplication.presentation.features.BeBrainViewModel
import com.example.myapplication.presentation.features.SearchProcessorViewModel
import com.example.myapplication.presentation.features.SuperCategory
import com.example.myapplication.presentation.designsystem.components.*
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme

/*
 * REFACTORIZACIÓN COMPLETADA:
 * - Se movió 'AutoSizeText' a Utilidades/TextosEstilos.kt (Unificado).
 * - Se movió 'CollapsibleSectionHeader' a Utilidades/TextosEstilos.kt.
 * - Se extrajo 'CyberRed' a Utilidades/Colores.kt.
 * - Se usan MaverickColors y MaverickStyles para consistencia visual.
 * - Se mantiene la lógica de ViewModel y navegación intacta.
 */

/*** Pantalla de resultados inteligente de Be. */
@Composable
fun BeResultadoScreen(
    viewModel: BeBrainViewModel,
    onClose: () -> Unit,
    onProviderClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    allCategories: List<CategoryEntity> = emptyList(),
    onCategoryClick: (String) -> Unit = {},
    onSuperCategoryClick: (SuperCategory) -> Unit = {},
    onSettingClick: (ControlItem) -> Unit = {}
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isVisible by viewModel.isResultadoVisible.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val allCatsFromVm by viewModel.allCategories.collectAsStateWithLifecycle()

    val categories = remember(searchResults) { searchResults.categories }
    val superCategories = remember(searchResults) { searchResults.superCategories }
    val filteredFavorites = remember(searchResults) { searchResults.favorites }

    BeResultadoContent(
        searchQuery = searchQuery,
        isVisible = isVisible,
        categories = categories,
        superCategories = superCategories,
        favorites = filteredFavorites,
        settings = emptyList(),
        allCategories = if (allCategories.isEmpty()) allCatsFromVm else allCategories,
        modifier = modifier,
        onClose = {
            viewModel.cerrarBeAssistantCompleto()
            onClose() 
        },
        onCategoryClick = { categoryName ->
            viewModel.cerrarBeAssistantCompleto() 
            onCategoryClick(categoryName) 
        },
        onSuperCategoryClick = { superCat ->
            viewModel.selectSuperCategory(superCat)
            onSuperCategoryClick(superCat)
        },
        onProviderClick = { providerId ->
            viewModel.cerrarBeAssistantCompleto() 
            onProviderClick(providerId) 
        },
        onSettingClick = { setting ->
            viewModel.cerrarBeAssistantCompleto()
            onSettingClick(setting)
        }
    )
}

/*** Contenido de la pantalla de resultados de Be con secciones colapsables.*/
@Composable
fun BeResultadoContent(
    searchQuery: String,
    isVisible: Boolean,
    categories: List<CategoryEntity>,
    superCategories: List<SuperCategory>,
    favorites: List<Provider>,
    settings: List<ControlItem>,
    allCategories: List<CategoryEntity>,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onSuperCategoryClick: (SuperCategory) -> Unit,
    onProviderClick: (String) -> Unit,
    onSettingClick: (ControlItem) -> Unit
) {
    // Estados locales para colapsar/expandir secciones
    var categoriesExpanded by remember { mutableStateOf(true) }
    var superCategoriesExpanded by remember { mutableStateOf(true) }
    var favoritesExpanded by remember { mutableStateOf(true) }
    var settingsExpanded by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClose() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.78f) 
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp) 
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { }, 
                    color = MaverickColors.CyberBackground,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    tonalElevation = 16.dp,
                    border = BorderStroke(2.dp, geminiGradientBrush(isAnimated = false))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp, vertical = 24.dp)
                        ) {
                            Spacer(modifier = Modifier.height(8.dp)) 

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp, end = 40.dp) 
                            ) {
                                AutoSizeText(
                                    text = if (searchQuery.isEmpty()) "Análisis de Be" else "Resultados para: ${searchQuery.uppercase()}",
                                    color = MaverickColors.TextMain,
                                    style = MaverickStyles.ResultTitle
                                )
                                Text(
                                    text = "Inteligencia Maverick en acción ✨",
                                    style = MaverickStyles.IntelligentTag.copy(
                                        brush = geminiGradientBrush(isAnimated = false)
                                    ),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.7f), thickness = 1.dp)

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                            ) {
                                if (settings.isNotEmpty()) {
                                    item {
                                        CollapsibleSectionHeader(
                                            title = "Configuración",
                                            count = settings.size,
                                            isExpanded = settingsExpanded,
                                            onToggle = { settingsExpanded = !settingsExpanded }
                                        )
                                    }
                                    if (settingsExpanded) {
                                        items(settings) { setting ->
                                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                                SettingResultItem(setting = setting, onClick = { onSettingClick(setting) })
                                            }
                                        }
                                    }
                                }

                                if (categories.isNotEmpty()) {
                                    item {
                                        CollapsibleSectionHeader(
                                            title = "Servicios",
                                            count = categories.size,
                                            isExpanded = categoriesExpanded,
                                            onToggle = { categoriesExpanded = !categoriesExpanded }
                                        )
                                    }

                                    if (categoriesExpanded) {
                                        item {
                                            LazyRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                                            ) {
                                                items(categories) { category ->
                                                    Box(modifier = Modifier.width(150.dp)) {
                                                        CompactCategoryCard(
                                                            item = category,
                                                            onClick = { onCategoryClick(category.name) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (superCategories.isNotEmpty()) {
                                    item {
                                        CollapsibleSectionHeader(
                                            title = "Grupos",
                                            count = superCategories.size,
                                            isExpanded = superCategoriesExpanded,
                                            onToggle = { superCategoriesExpanded = !superCategoriesExpanded }
                                        )
                                    }
                                    if (superCategoriesExpanded) {
                                        item {
                                            LazyRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                            ) {
                                                items(superCategories) { superCat ->
                                                    Box(modifier = Modifier.width(280.dp)) {
                                                        BentoSuperCategoryCard(
                                                            superCategory = superCat,
                                                            emoji = superCat.icon,
                                                            height = 180.dp,
                                                            onClick = { onSuperCategoryClick(superCat) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (favorites.isNotEmpty()) {
                                    item {
                                        CollapsibleSectionHeader(
                                            title = "Mis Favoritos",
                                            count = favorites.size,
                                            isExpanded = favoritesExpanded,
                                            onToggle = { favoritesExpanded = !favoritesExpanded }
                                        )
                                    }
                                    if (favoritesExpanded) {
                                        item {
                                            LazyRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(0.dp),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                            ) {
                                                // items(favorites) { ... }
                                            }
                                        }
                                    }
                                }

                                if (categories.isEmpty() && superCategories.isEmpty() && favorites.isEmpty() && settings.isEmpty()) {
                                    item {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = null,
                                                tint = MaverickColors.TextMuted.copy(alpha = 0.3f),
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "Busca categorías, grupos o profesionales favoritos para ver resultados.",
                                                color = MaverickColors.TextMuted,
                                                fontSize = 14.sp,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 32.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Surface(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 0.dp, end = 8.dp)
                        .size(38.dp)
                        .shadow(12.dp, CircleShape, spotColor = MaverickColors.CyberRed),
                    shape = CircleShape,
                    color = MaverickColors.CyberBackground,
                    border = BorderStroke(1.5.dp, MaverickColors.CyberRed),
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaverickColors.CyberRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingResultItem(setting: ControlItem, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = MaverickColors.GlassWhite,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaverickColors.GlassBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(setting.color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = setting.emoji ?: "⚙️", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = setting.label,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
        }
    }
}

/*
// COMENTADO POR REFACTORIZACIÓN: Movido a Utilidades/TextosEstilos.kt
@Composable
fun AutoSizeText(...) { ... }

@Composable
fun CollapsibleSectionHeader(...) { ... }
*/

@Preview(showBackground = true)
@Composable
fun BeResultadoContentPreview() {
    MyApplicationTheme(darkTheme = true) {
        BeResultadoContent(
            searchQuery = "Soporte",
            isVisible = true,
            categories = emptyList(),
            superCategories = emptyList(),
            favorites = emptyList(),
            settings = emptyList(),
            allCategories = emptyList(),
            onClose = {},
            onCategoryClick = {},
            onSuperCategoryClick = {},
            onProviderClick = {},
            onSettingClick = {}
        )
    }
}
**/









