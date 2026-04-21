package com.example.myapplication.prestador.ui.promotion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.CreatePromotionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionListScreen(
    onBack: () -> Unit,
    onPromotionClick: (String) -> Unit = {},
    viewModel: CreatePromotionViewModel = hiltViewModel()
){
    val colors = getPrestadorColors()
    val providerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val promotions by viewModel.getPromotions(providerId).collectAsState(initial = emptyList())
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    // Dialog confirmación eliminar
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("¿Eliminar promoción?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePromotion(showDeleteDialog!!)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) { Text("Eliminar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Promociones (${promotions.size})", color = colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surfaceColor)
            )
        },
        containerColor = colors.backgroundColor
    ) { paddingValues ->
        if (promotions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay promociones aún\n¡Crea tu primera promoción!",
                    fontSize = 16.sp,
                    color = colors.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(promotions) { promotion ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPromotionClick(promotion.id) },
                        colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            // Header: título + badge estado + botón eliminar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = promotion.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                // Badge estado
                                val (badgeColor, badgeText) = when (promotion.status.name) {
                                    "ACTIVE" -> Color(0xFF4CAF50) to "ACTIVA"
                                    "EXPIRED" -> Color(0xFF9E9E9E) to "EXPIRADA"
                                    "DRAFT" -> Color(0xFFFFB300) to "BORRADOR"
                                    else -> Color(0xFF9E9E9E) to promotion.status.name
                                }
                                Box(
                                    modifier = Modifier
                                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(badgeText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeColor)
                                }
                                Spacer(Modifier.width(8.dp))
                                IconButton(
                                    onClick = { showDeleteDialog = promotion.id },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, null, tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(promotion.description, fontSize = 13.sp, color = colors.textSecondary)
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Tipo
                                val (tipoColor, tipoText) = if (promotion.type.name == "STORY")
                                    Color(0xFF7B1FA2) to "📖 Historia 24h"
                                else colors.primaryOrange to "📢 Promoción 7d"
                                Text(tipoText, fontSize = 12.sp, color = tipoColor, fontWeight = FontWeight.SemiBold)
                                // Descuento
                                promotion.discount?.let {
                                    if (it > 0) Text("$it% OFF", fontSize = 12.sp, color = colors.primaryOrange, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            if (promotion.categories.isNotEmpty()) {
                                Text("📂 ${promotion.categories.joinToString(", ")}", fontSize = 11.sp, color = colors.textSecondary)
                                Spacer(Modifier.height(6.dp))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("❤️ ${promotion.likes}", fontSize = 12.sp, color = colors.textSecondary)
                                Text("👁 ${promotion.views}", fontSize = 12.sp, color = colors.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
