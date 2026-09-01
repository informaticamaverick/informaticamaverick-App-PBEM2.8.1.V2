package com.example.myapplication.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity

/**
 * --- DIÁLOGO DE CONFIGURACIÓN PARA SIMULACIÓN MASIVA DE PRESTADORES ---
 * Permite al desarrollador elegir categorías, código postal y cantidad de prestadores.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderSimulationDialog(
    allCategories: List<com.example.myapplication.core.dominio.modelos.CategoriaDominio>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>, String, Int) -> Unit,
    onMigrateCategories: () -> Unit // [NUEVO] Para disparar la migración a Firestore
) {
    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
    var areaCode by remember { mutableStateOf("1425") } // Default Palermo/CABA
    var providerCount by remember { mutableStateOf("10") }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22D3EE).copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Simulación app Ultra",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Configura los parámetros para sembrar prestadores en la base de datos local.",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // --- SECCIÓN: SELECCIÓN DE CATEGORÍAS ---
                Text(
                    "Categorías (${selectedCategories.size})",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(vertical = 8.dp)
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(allCategories) { category ->
                            val isSelected = selectedCategories.contains(category.nombre)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCategories = if (isSelected) {
                                            selectedCategories - category.nombre
                                        } else {
                                            selectedCategories + category.nombre
                                        }
                                    }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(
                                            if (isSelected) Color(0xFF22D3EE) else Color.Transparent,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .border(1.dp, Color(0xFF22D3EE), RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp), tint = Color.Black)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(category.nombre, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // --- SECCIÓN: CÓDIGO DE ÁREA Y CANTIDAD ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = areaCode,
                        onValueChange = { areaCode = it },
                        label = { Text("Cód. Postal", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF22D3EE),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedLabelColor = Color(0xFF22D3EE),
                            unfocusedLabelColor = Color.Gray,
                            cursorColor = Color(0xFF22D3EE),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    OutlinedTextField(
                        value = providerCount,
                        onValueChange = { providerCount = it },
                        label = { Text("Cantidad", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF22D3EE),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedLabelColor = Color(0xFF22D3EE),
                            unfocusedLabelColor = Color.Gray,
                            cursorColor = Color(0xFF22D3EE),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- 🚀 [NUEVO] BOTÓN DE MIGRACIÓN DE CATEGORÍAS (ADMIN ONLY CONCEPT) ---
                Button(
                    onClick = onMigrateCategories,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)) // Color Naranja/Alerta
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Migrar Categorías a Firestore", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // --- BOTONES DE ACCIÓN ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
                    ) {
                        Text("Cancelar", color = Color.Gray)
                    }
                    
                    Button(
                        onClick = {
                            val count = providerCount.toIntOrNull() ?: 10
                            onConfirm(selectedCategories.toList(), areaCode, count)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22D3EE)),
                        enabled = selectedCategories.isNotEmpty() && areaCode.isNotBlank()
                    ) {
                        Text("Simular", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}











































