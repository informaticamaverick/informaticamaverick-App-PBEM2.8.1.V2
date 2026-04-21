package com.example.myapplication.prestador.ui.promotion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.myapplication.prestador.data.model.PromotionStatus
import com.example.myapplication.prestador.data.model.PromotionType
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.CreatePromotionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionDetailScreen(
    promotionId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: CreatePromotionViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val promotion by viewModel.getPromotionByIdAsModel(promotionId).collectAsState(initial = null)
    var showDeleteDialog by remember { mutableStateOf(false) }
    val successMsg by viewModel.successMessage.collectAsState()

    // Volver automáticamente al eliminar
    LaunchedEffect(successMsg) {
        if (successMsg == "Promoción eliminada") {
            viewModel.clearMessages()
            onBack()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar promoción?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePromotion(promotionId)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) { Text("Eliminar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    val promo = promotion

    if (promo == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colors.primaryOrange)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detalle de publicación",
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colors.textPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(promotionId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = colors.primaryOrange)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFE53935))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surfaceColor)
            )
        },
        containerColor = colors.backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── IMÁGENES ──────────────────────────────────────────────
            if (promo.imageUrls.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(promo.imageUrls) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(250.dp)
                                .height(180.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                    }
                }
            }

            // ── MÉTRICAS ─────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricTile(
                        emoji = "👁",
                        value = promo.views.toString(),
                        label = "Vistas",
                        color = colors.primaryOrange
                    )
                    VerticalDivider(
                        modifier = Modifier.height(50.dp),
                        color = colors.border
                    )
                    MetricTile(
                        emoji = "❤️",
                        value = promo.likes.toString(),
                        label = "Likes",
                        color = Color(0xFFE53935)
                    )
                    VerticalDivider(
                        modifier = Modifier.height(50.dp),
                        color = colors.border
                    )
                    MetricTile(
                        emoji = "⭐",
                        value = String.format("%.1f", promo.rating),
                        label = "Rating",
                        color = Color(0xFFFFB300)
                    )
                    VerticalDivider(
                        modifier = Modifier.height(50.dp),
                        color = colors.border
                    )
                    val hoursLeft = promo.hoursUntilExpiration()
                    val timeColor = when {
                        promo.isExpired() -> Color(0xFF9E9E9E)
                        hoursLeft < 12   -> Color(0xFFFF7043)
                        else             -> Color(0xFF4CAF50)
                    }
                    val timeValue = when {
                        promo.isExpired() -> "Expiró"
                        hoursLeft < 24   -> "${hoursLeft}h"
                        else             -> "${hoursLeft / 24}d"
                    }
                    MetricTile(
                        emoji = "⏱",
                        value = timeValue,
                        label = "Restante",
                        color = timeColor
                    )
                }
            }

            // ── ESTADO Y TIPO ─────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val (statusColor, statusText) = when (promo.status) {
                    PromotionStatus.ACTIVE   -> Color(0xFF4CAF50) to "✓ ACTIVA"
                    PromotionStatus.EXPIRED  -> Color(0xFF9E9E9E) to "✗ EXPIRADA"
                    PromotionStatus.DRAFT    -> Color(0xFFFFB300) to "⏸ BORRADOR"
                    PromotionStatus.ARCHIVED -> Color(0xFF607D8B) to "📦 ARCHIVADA"
                }
                DetailChip(statusText, statusColor)

                val (typeColor, typeText) = if (promo.type == PromotionType.STORY)
                    Color(0xFF7B1FA2) to "📖 Historia · 24h"
                else
                    colors.primaryOrange to "📢 Promoción · 7 días"
                DetailChip(typeText, typeColor)
            }

            // ── TÍTULO Y DESCRIPCIÓN ──────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        promo.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    promo.discount?.let { disc ->
                        if (disc > 0) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        colors.primaryOrange.copy(alpha = 0.12f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "$disc% OFF",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.primaryOrange
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = colors.border)
                    Text(
                        promo.description,
                        fontSize = 14.sp,
                        color = colors.textSecondary,
                        lineHeight = 22.sp
                    )
                }
            }

            // ── CATEGORÍAS ────────────────────────────────────────────
            if (promo.categories.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Category,
                                null,
                                tint = colors.primaryOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Categorías",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(promo.categories) { cat ->
                                DetailChip(cat, colors.primaryOrange)
                            }
                        }
                    }
                }
            }

            // ── FECHAS ────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Publicada",
                        value = sdf.format(Date(promo.createdAt)),
                        iconTint = colors.primaryOrange
                    )
                    HorizontalDivider(color = colors.border)
                    DateRow(
                        icon = Icons.Default.AccessTime,
                        label = "Expira",
                        value = sdf.format(Date(promo.expiresAt)),
                        iconTint = if (promo.isExpired()) Color(0xFF9E9E9E) else Color(0xFF4CAF50)
                    )
                }
            }

            // ── BOTÓN ARCHIVAR ────────────────────────────────────────
            if (promo.status == PromotionStatus.ACTIVE) {
                OutlinedButton(
                    onClick = { viewModel.archivePromotion(promotionId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF607D8B)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF607D8B))
                ) {
                    Icon(Icons.Default.Archive, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Archivar publicación", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── COMPONENTES AUXILIARES ────────────────────────────────────────────

@Composable
private fun MetricTile(emoji: String, value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(emoji, fontSize = 18.sp)
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, fontSize = 11.sp, color = Color(0xFF9E9E9E))
    }
}

@Composable
private fun DetailChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun DateRow(icon: ImageVector, label: String, value: String, iconTint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
        Text(
            "$label: ",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF9E9E9E)
        )
        Text(value, fontSize = 13.sp, color = Color(0xFF9E9E9E))
    }
}
