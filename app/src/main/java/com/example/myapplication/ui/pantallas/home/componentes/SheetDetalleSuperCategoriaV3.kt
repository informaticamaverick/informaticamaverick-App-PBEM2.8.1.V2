package com.example.myapplication.ui.pantallas.home.componentes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.ui.componentes.be.vm.*
import com.example.myapplication.ui.componentes.be.modelos.*
import com.example.myapplication.core.dominio.modelos.CategoriaDominio
import com.example.myapplication.core.dominio.modelos.SuperCategoriaDominio
import com.example.myapplication.ui.componentes.CompactCategoryCard
import com.example.myapplication.ui.componentes.sistema.lista.ArmadorListaModoBusqueda
import androidx.compose.ui.text.style.TextAlign
import com.example.myapplication.uishared.estilos.AppTypography
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.ui.estilos.PBEMTheme
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.viewmodel.home.CategoryViewModel

/**
 * SheetDetalleSuperCategoriaV3.kt
 * Propósito: Mostrar las categorías pertenecientes a una supercategoría seleccionada.
 * Funcionamiento: Utiliza ArmadorListaModoBusqueda para integrarse perfectamente con el HUD de Be.
 * LEY #1: Recibe estados de ViewModels y emite eventos de navegación.
 * LEY #12: Soberanía por Contrato.
 */

@Composable
fun SheetDetalleSuperCategoriaV3(
    beViewModel: BeCerebroViewModel,
    categoryViewModel: CategoryViewModel,
    beArchitectViewModel: BeCuerpoViewModel?,
    alHacerClickCategoria: (String) -> Unit
) {
    val superCategoriaSeleccionada by categoryViewModel.superCategoriaSeleccionada.collectAsStateWithLifecycle()
    val itemsCategoriasFiltrados by categoryViewModel.categoriasOrdenadas.collectAsStateWithLifecycle()
    val estaVisible by categoryViewModel.estaHojaVisible.collectAsStateWithLifecycle()
    val uiState by categoryViewModel.uiState.collectAsStateWithLifecycle()

    // 🔥 [v2026.ELITE]: Cierre Maestro en Back (Gesto/Botón)
    BackHandler(enabled = estaVisible && superCategoriaSeleccionada != null) {
        beArchitectViewModel?.dispararAccion("cerrar_todo")
    }

    // 🔥 [v2026.SUPREME]: Contrato de Soberanía Táctica para Be
    val configuracionBe = remember(superCategoriaSeleccionada) {
        superCategoriaSeleccionada?.let { superCat ->
            ConfiguracionContextoBe(
                id = "detalle_super_${superCat.id.lowercase()}",
                primarias = listOf("fast", "fav"), 
                sistema = listOf("teclado", "cerrar_todo"),
                mensajes = emptyList(),
                pistaBusqueda = "BUSCAR EN ${superCat.titulo.uppercase()}...",
                mostrarHerramientas = true,
                abrirTecladoEnBusqueda = false
            )
        }
    }

    ArmadorListaModoBusqueda(
        estaVisible = estaVisible && superCategoriaSeleccionada != null,
        alCerrar = { 
            beArchitectViewModel?.dispararAccion("cerrar_todo")
            categoryViewModel.deseleccionarTodo() // 🔥 [SANEAMIENTO]
        },
        titulo = superCategoriaSeleccionada?.titulo ?: "",
        icono = superCategoriaSeleccionada?.icono,
        cantidadItems = itemsCategoriasFiltrados.size,
        pistaBusqueda = superCategoriaSeleccionada?.let { "BUSCAR EN ${it.titulo.uppercase()}..." },
        colorAcento = SharedPalette.ElectricCyan,
        usaGrid = true,
        columnasGrid = 3,
        configuracionSoberana = configuracionBe,
        beCerebroVm = beViewModel,
        contenidoGrid = {
            items(
                items = itemsCategoriasFiltrados, 
                key = { it.id },
                contentType = { "categoria_táctica" }
            ) { categoria ->
                CompactCategoryCard(
                    item = categoria,
                    onClick = { alHacerClickCategoria(categoria.id) },
                    onInfoClick = { categoryViewModel.establecerCategoriaParaDetalle(it) },
                    isInfoVisible = uiState.categoriaParaDetalle?.id == categoria.id,
                    onDismissInfo = { categoryViewModel.establecerCategoriaParaDetalle(null) }
                )
            }
        }
    )
}

// ==================================================================================
// --- PREVIEWS (LEY #10: MODO LECTURA) ---
// ==================================================================================

@Preview(name = "Sheet Detalle - Vista Elite", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewContenidoSheetDetalleV3() {
    PBEMTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            val mockCategorias = listOf(
                CategoriaDominio(id = "CAMPO_AGRI", nombre = "Agricultor", icono = "🚜", idSuperCategoria = "CAMPO", superCategoria = "Campo"),
                CategoriaDominio(id = "CAMPO_API", nombre = "Apicultor", icono = "🐝", idSuperCategoria = "CAMPO", superCategoria = "Campo"),
                CategoriaDominio(id = "CAMPO_COSE", nombre = "Cosechador", icono = "🌾", idSuperCategoria = "CAMPO", superCategoria = "Campo")
            )

            ArmadorListaModoBusqueda(
                estaVisible = true,
                alCerrar = {},
                titulo = "Agricultura y Ganadería",
                icono = "🌾",
                cantidadItems = mockCategorias.size,
                usaGrid = true,
                contenidoGrid = {
                    items(mockCategorias) { categoria ->
                        CompactCategoryCard(item = categoria, onClick = {})
                    }
                }
            )
        }
    }
}

// Eliminamos ContenidoSheetDetalleV3 ya que su lógica ahora está en el Molde
