package com.example.myapplication.ui.componentes

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.uishared.estilos.AppColors
import com.example.myapplication.uishared.estilos.SharedPalette


@Composable
fun TenderSelectionDialog(
    matchingTenders: List<ConcursoPublicoEntity>,
    providerCategories: List<String>,
    appColors: AppColors,
    onDismiss: () -> Unit,
    onSelect: (ConcursoPublicoEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invitar a Licitación", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            if (matchingTenders.isEmpty()) {
                Text("No tienes licitaciones abiertas que coincidan con el rubro del prestador.", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(matchingTenders.size) { index ->
                        val tender = matchingTenders[index]
                        Surface(modifier = Modifier.fillMaxWidth().clickable { onSelect(tender) }, shape = RoundedCornerShape(12.dp), color = appColors.surfaceColor, border = BorderStroke(1.dp, appColors.accentBlue.copy(alpha = 0.3f))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(tender.titulo, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(tender.idCategoria, fontSize = 12.sp, color = appColors.accentBlue)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("CANCELAR") } },
        containerColor = Color(0xFF0F172A)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentBottomSheet(
    appColors: AppColors,
    onDismiss: () -> Unit,
    onSelectGallery: () -> Unit,
    onSelectTender: () -> Unit,
    onSelectLocation: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = appColors.backgroundColor,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f)) }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding()) {
            Text("ADJUNTAR", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp), color = SharedPalette.ElectricCyan, modifier = Modifier.padding(bottom = 20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                AttachmentItem(icon = Icons.Default.Image, label = "Galería", color = Color(0xFF8B5CF6), onClick = onSelectGallery)
                AttachmentItem(icon = Icons.AutoMirrored.Filled.Assignment, label = "Licitación", color = SharedPalette.ElectricCyan, onClick = onSelectTender)
                AttachmentItem(icon = Icons.Default.LocationOn, label = "Ubicación", color = Color(0xFF10B981), onClick = onSelectLocation)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun AttachmentItem(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Surface(modifier = Modifier.size(56.dp), shape = CircleShape, color = color.copy(alpha = 0.15f), border = BorderStroke(1.dp, color.copy(alpha = 0.3f))) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(26.dp)) }
        }
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White.copy(alpha = 0.7f))
    }
}

































