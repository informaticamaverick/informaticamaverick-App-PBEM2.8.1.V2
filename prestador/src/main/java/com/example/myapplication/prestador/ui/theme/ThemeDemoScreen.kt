package com.example.myapplication.prestador.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * PANTALLA DE DEMOSTRACIÓN DEL SISTEMA DE TEMA
 * 
 * Muestra todos los componentes y cómo usar el sistema de colores
 * Úsala como referencia para aplicar el tema en otras pantallas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeDemoScreen(
    onBack: () -> Unit = {}
) {
    // Estado del tema (dark/light)
    val themeState = rememberThemeState()
    
    // Aplicar el tema a toda la pantalla
    OrganicTheme(darkTheme = themeState.isDarkTheme) {
        val colors = getOrganicColors()
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "Sistema de Tema",
                            color = colors.textPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Rounded.ArrowBack,
                                contentDescription = "Volver",
                                tint = colors.textPrimary
                            )
                        }
                    },
                    actions = {
                        // Toggle tema
                        IconButton(
                            onClick = { themeState.toggleTheme() },
                            modifier = Modifier
                                .background(
                                    if (colors.isDark) Color.White.copy(alpha = 0.1f) else Color.White,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (colors.isDark) Icons.Rounded.WbSunny else Icons.Rounded.DarkMode,
                                contentDescription = "Cambiar tema",
                                tint = if (colors.isDark) colors.yellow else colors.primaryOrange
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.surface
                    )
                )
            },
            containerColor = colors.background
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Header
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        OnlineBadge()
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Sistema de Tema Moderno",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Explora los componentes con efecto glass y gradientes",
                            fontSize = 14.sp,
                            color = colors.textSecondary
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(24.dp)) }
                
                // Stats Cards
                item {
                    SectionTitle("Stats con Efecto Glass")
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassStat(
                            icon = Icons.Rounded.Star,
                            value = "4.9",
                            label = "Rating",
                            accentColor = colors.yellow,
                            modifier = Modifier.weight(1f)
                        )
                        
                        GlassStat(
                            icon = Icons.Rounded.CheckCircle,
                            value = "12",
                            label = "Completados",
                            accentColor = colors.primaryOrange,
                            modifier = Modifier.weight(1f),
                            isPulsing = true
                        )
                        
                        GlassStat(
                            icon = Icons.Rounded.TrendingUp,
                            value = "98%",
                            label = "Eficacia",
                            accentColor = colors.violet,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(24.dp)) }
                
                // Glass Cards
                item {
                    SectionTitle("Cards con Cristal")
                    
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassCard {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.Campaign,
                                    contentDescription = null,
                                    tint = colors.primaryOrange,
                                    modifier = Modifier.size(32.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column {
                                    Text(
                                        "Promoción Activa",
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        "5 promociones publicadas",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                        
                        GlassCard(onClick = { }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.Notifications,
                                    contentDescription = null,
                                    tint = colors.cyan,
                                    modifier = Modifier.size(32.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column {
                                    Text(
                                        "Notificaciones",
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        "3 nuevas notificaciones",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(24.dp)) }
                
                // Botones con Gradiente
                item {
                    SectionTitle("Botones con Gradiente")
                    
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GradientButton(
                            text = "Crear Promoción",
                            onClick = { },
                            icon = Icons.Rounded.Add,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        GradientButton(
                            text = "Ver Estadísticas",
                            onClick = { },
                            icon = Icons.Rounded.BarChart,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(24.dp)) }
                
                // Acciones Rápidas
                item {
                    SectionTitle("Acciones Rápidas")
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        BubbleAction(
                            icon = Icons.Rounded.Share,
                            label = "Compartir",
                            accentColor = colors.cyan
                        )
                        
                        BubbleAction(
                            icon = Icons.Rounded.Percent,
                            label = "Promo",
                            accentColor = colors.yellow
                        )
                        
                        BubbleAction(
                            icon = Icons.Rounded.Help,
                            label = "Ayuda",
                            accentColor = colors.violet
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
                
                // Paleta de Colores
                item {
                    SectionTitle("Paleta de Colores")
                    
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ColorSwatch("Orange", colors.primaryOrange)
                        ColorSwatch("Yellow", colors.yellow)
                        ColorSwatch("Violet", colors.violet)
                        ColorSwatch("Cyan", colors.cyan)
                        ColorSwatch("Green", colors.green)
                        ColorSwatch("Red", colors.red)
                        ColorSwatch("Blue", colors.blue)
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun ColorSwatch(name: String, color: Color) {
    val colors = getOrganicColors()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color, CircleShape)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = name,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary
        )
    }
}
