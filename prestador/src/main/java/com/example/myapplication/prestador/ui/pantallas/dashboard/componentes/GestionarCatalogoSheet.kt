package com.example.myapplication.prestador.ui.pantallas.dashboard.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.core.datos.local.entidades.ArticuloPresupuesto
import com.example.myapplication.core.datos.local.entidades.ServicioPresupuesto
import com.example.myapplication.prestador.viewmodel.presupuesto.PrestadorPresupuestoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionarCatalogoSheet(onDismiss: () -> Unit) {
    val viewModel: PrestadorPresupuestoViewModel = hiltViewModel()
    val articleCatalog by viewModel.articleCatalog.collectAsState()
    val serviceCatalog by viewModel.serviceCatalog.collectAsState()

    val articulos = remember(articleCatalog) {
        val json = articleCatalog.itemsJson
        if (json.isBlank()) emptyList<ArticuloPresupuesto>()
        else json.split("|").mapNotNull { s ->
            val p = s.split(";")
            if (p.size >= 4) ArticuloPresupuesto(codigo = p[0], descripcion = p[1], cantidad = p[2].toIntOrNull() ?: 1, precioUnitario = p[3].toDoubleOrNull() ?: 0.0)
            else null
        }
    }

    val servicios = remember(serviceCatalog) {
        val json = serviceCatalog.serviciosJson
        if (json.isBlank()) emptyList<ServicioPresupuesto>()
        else json.split("|").mapNotNull { s ->
            val p = s.split(";")
            if (p.size >= 3) ServicioPresupuesto(codigo = p[0], descripcion = p[1], precioUnitario = p[2].toDoubleOrNull() ?: 0.0)
            else null
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Artículos", "Servicios")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Mi Catálogo Maestro", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            
            TabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }

            Spacer(Modifier.height(16.dp))

            when(selectedTab) {
                0 -> CatalogoList(articulos.map { it.descripcion to it.precioUnitario })
                1 -> CatalogoList(servicios.map { it.descripcion to it.precioUnitario })
            }
        }
    }
}

@Composable
private fun CatalogoList(items: List<Pair<String, Double>>) {
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sin ítems en esta categoría", color = Color.Gray)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { (desc, price) ->
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 1.dp
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(desc, fontWeight = FontWeight.Bold)
                        Text("$ ${String.format("%.2f", price)}", color = Color(0xFFF97316))
                    }
                }
            }
        }
    }
}

