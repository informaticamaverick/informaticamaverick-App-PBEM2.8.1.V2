package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio
import com.example.myapplication.uishared.ui.components.TarjetaPresupuestoMini
import com.example.myapplication.uishared.ui.components.TarjetaPresupuestoMiniSkeleton

/**
 * --- ARCHIVERO: PRESUPUESTOS (v2026.ELITE) ---
 */
@Composable
fun ArchiveroPresupuestosSheet(
    presupuestos: List<PresupuestoResumenDominio>?, // 🔥 Ahora puede ser null (Cargando)
    busqueda: String,
    alCambiarBusqueda: (String) -> Unit,
    alCerrar: () -> Unit,
    alSeleccionar: (PresupuestoResumenDominio) -> Unit
) {
    ArchiveroMoldeSheet(
        titulo = "Historial de Presupuestos",
        subtitulo = "Reutiliza presupuestos enviados",
        busqueda = busqueda,
        alCambiarBusqueda = alCambiarBusqueda,
        alCerrar = alCerrar,
        colorAcento = Color(0xFFF97316) // Naranja Maverick
    ) {
        if (presupuestos == null) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(6) { TarjetaPresupuestoMiniSkeleton() }
            }
        } else if (presupuestos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes presupuestos en este chat", color = Color.White.copy(alpha = 0.3f))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(presupuestos) { p ->
                    TarjetaPresupuestoMini(
                        titulo = p.tituloTrabajo ?: "Presupuesto",
                        total = "$ ${String.format(java.util.Locale.getDefault(), "%,.2f", p.totalGeneral)}",
                        estado = p.estado,
                        miniaturaBase64 = p.urlMiniatura,
                        leido = true,
                        alHacerClick = { alSeleccionar(p) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
private fun PreviewArchiveroPresupuestosCargando() {
    ArchiveroPresupuestosSheet(
        presupuestos = null,
        busqueda = "",
        alCambiarBusqueda = {},
        alCerrar = {},
        alSeleccionar = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
private fun PreviewArchiveroPresupuestosVacio() {
    ArchiveroPresupuestosSheet(
        presupuestos = emptyList(),
        busqueda = "",
        alCambiarBusqueda = {},
        alCerrar = {},
        alSeleccionar = {}
    )
}
