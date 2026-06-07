package com.example.myapplication.prestador.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.prestador.data.model.ServiceType
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import org.json.JSONArray

private const val MAX_CATEGORIAS = 5

data class CategoriaItem(val nombre: String, val icono: ImageVector)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CategoriasSelector(
    categoriasJson: String,
    onCategoriasActualizadas: (String) -> Unit,
    serviceType: com.example.myapplication.prestador.data.model.ServiceType = com.example.myapplication.prestador.data.model.ServiceType.TECHNICAL,
    allCategories: List<CategoryEntity> = emptyList()
) {
    val colors = getPrestadorColors()
    var seleccionadas by remember(categoriasJson) {
        mutableStateOf(categoriasJsonToSet(categoriasJson))
    }
    val lleno = seleccionadas.size >= MAX_CATEGORIAS

    var busqueda by remember { mutableStateOf("") }
    var mostrarsugerencias by remember { mutableStateOf(false) }
    val sugerencias = remember(busqueda, seleccionadas, allCategories) {
        if (busqueda.isBlank()) emptyList<CategoryEntity>()
        else allCategories
            .filter { it.name.contains(busqueda, ignoreCase = true) && it.name !in seleccionadas }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {

        // Barra de progreso
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${seleccionadas.size}/$MAX_CATEGORIAS seleccionadas",
                fontSize = 12.sp,
                color = if (lleno) MaterialTheme.colorScheme.error else colors.textSecondary
            )
        }
        LinearProgressIndicator(
            progress = { seleccionadas.size / MAX_CATEGORIAS.toFloat() },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = if (lleno) MaterialTheme.colorScheme.error else colors.primaryOrange,
            trackColor = colors.primaryOrange.copy(alpha = 0.15f)
        )
        if (lleno) {
            Text("Límite alcanzado. Deseleccioná una para cambiar.", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(4.dp))

        //Campo de busqueda con autocomplete
        Box {
            OutlinedTextField(
                value = busqueda,
                onValueChange = {
                    busqueda = it
                    mostrarsugerencias = it.isNotBlank()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar categoria") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.textSecondary) },
                trailingIcon = {
                    if (busqueda.isNotBlank())
                    {
                        IconButton(onClick = {
                            busqueda = ""; mostrarsugerencias = false }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = colors.textSecondary)
                        }
                    }
                },
                enabled = !lleno,
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryOrange,
                    focusedLabelColor = colors.primaryOrange,
                    unfocusedBorderColor = colors.border
                )
            )

            if (mostrarsugerencias && sugerencias.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column {
                        sugerencias.take(6).forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name, fontSize = 14.sp) },
                                leadingIcon = {
                                    if (cat.icon.isNotBlank()) {
                                        Text(text = cat.icon, fontSize = 16.sp)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Category,
                                            contentDescription = null,
                                            tint = colors.primaryOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    seleccionadas = seleccionadas + cat.name
                                    onCategoriasActualizadas(setToJsonArray(seleccionadas))
                                    busqueda = ""
                                    mostrarsugerencias = false
                                }
                            )
                        }
                    }
                }
            }
        }

        //Chips de categorias seleccionadas
        if (seleccionadas.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                seleccionadas.forEach { nombre ->
                    val catInfo = allCategories.find { it.name == nombre }

                    FilterChip(
                        selected = true,
                        onClick = {
                            seleccionadas = seleccionadas - nombre
                            onCategoriasActualizadas(setToJsonArray(seleccionadas))
                        },
                        label = { Text(text = nombre, fontSize = 12.sp)},
                        leadingIcon = {
                            if (catInfo != null && catInfo.icon.isNotBlank()) {
                                Text(text = catInfo.icon, fontSize = 14.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Quitar",
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.primaryOrange,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            selectedTrailingIconColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

private fun categoriasJsonToSet(json: String): Set<String> {
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { arr.getString(it) }.toSet()
    } catch (e: Exception) { emptySet() }
}

private fun setToJsonArray(set: Set<String>): String {
    val arr = JSONArray()
    set.forEach { arr.put(it) }
    return arr.toString()
}
