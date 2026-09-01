package com.example.myapplication.uishared.ui.components.profile.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.core.dominio.modelos.ReseñaDominio
import java.text.SimpleDateFormat
import java.util.*

/**
 * --- HOJA DE RESEÑAS ELITE (v2026.FINAL) ---
 * [PROPÓSITO]: Mostrar las opiniones certificadas de los clientes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaReseñasPrestador(
    reseñas: List<ReseñaDominio>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A24),
        contentColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "OPINIONES DE CLIENTES",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            if (reseñas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aún no hay reseñas para este prestador.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(reseñas) { item ->
                        ItemReseñaDominio(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemReseñaDominio(item: ReseñaDominio) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val fecha = sdf.format(Date(item.fechaUtc))

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = item.fotoAutorUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.nombreAutor, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                Text(fecha, color = Color.Gray, fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("%.1f".format(item.calificacion), color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        Surface(
            color = Color.White.copy(alpha = 0.03f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = item.comentario,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                modifier = Modifier.padding(12.dp)
            )
        }

        val respuesta = item.respuestaPrestador
        if (respuesta != null) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.padding(start = 24.dp)) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(Color(0xFFF97316).copy(alpha = 0.5f))
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = respuesta,
                    color = Color(0xFFF97316).copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

































