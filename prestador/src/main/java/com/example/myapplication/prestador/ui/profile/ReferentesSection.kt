package com.example.myapplication.prestador.ui.profile

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.prestador.data.local.entity.ReferenteEntity
import com.example.myapplication.prestador.ui.theme.getPrestadorColors

@Composable
fun ReferentesSection(
    referentes: List<ReferenteEntity>,
    onAgregar: (nombre: String, apellido: String, cargo: String, imageUri: Uri?) -> Unit,
    onDesactivar: (ReferenteEntity) -> Unit
) {
    val colors = getPrestadorColors()
    var showDialog by remember { mutableStateOf(false) }
    var referenteAEditar by remember { mutableStateOf<ReferenteEntity?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Referentes / Equipo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Agregar referente", tint = colors.primaryOrange)
            }
        }

        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        if (referentes.isEmpty()) {
            Text("No hay referentes agregados", fontSize = 13.sp, color = colors.textSecondary,
                modifier = Modifier.padding(vertical = 8.dp))
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(referentes) { referente ->
                    ReferenteItem(
                        referente = referente,
                        onEditar = { r -> referenteAEditar = r; showDialog = true },
                        onDesactivar = onDesactivar
                    )
                }
            }
        }
    }

    if (showDialog) {
        AddReferenteDialog(
            referente = referenteAEditar,
            onDismiss = { showDialog = false; referenteAEditar = null },
            onConfirm = { nombre, apellido, cargo, imageUri ->
                onAgregar(nombre, apellido, cargo, imageUri)
                showDialog = false
                referenteAEditar = null
            }
        )
    }
}

@Composable
private fun ReferenteItem(
    referente: ReferenteEntity,
    onEditar: (ReferenteEntity) -> Unit,
    onDesactivar: (ReferenteEntity) -> Unit
) {
    val colors = getPrestadorColors()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar: imagen real o ícono
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.primaryOrange.copy(alpha = 0.1f))
                    .border(1.5.dp, colors.primaryOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!referente.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = referente.imageUrl,
                        contentDescription = "Foto de ${referente.nombre}",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = colors.primaryOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${referente.nombre} ${referente.apellido ?: ""}".trim(),
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary
                )
                if (!referente.cargo.isNullOrBlank()) {
                    Text(text = referente.cargo, fontSize = 12.sp, color = colors.textSecondary)
                }
            }
            IconButton(onClick = { onEditar(referente) }) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = colors.textSecondary)
            }
            IconButton(onClick = { onDesactivar(referente) }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
