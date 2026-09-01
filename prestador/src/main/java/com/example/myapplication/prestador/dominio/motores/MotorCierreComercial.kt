package com.example.myapplication.prestador.dominio.motores

import com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto
import com.example.myapplication.prestador.datos.local.dao.MovimientoStockDao
import com.example.myapplication.prestador.datos.local.dao.ProductoDao
import com.example.myapplication.prestador.datos.local.entidades.MovimientoStockEntity
import com.example.myapplication.prestador.datos.local.entidades.TipoMovimientoStock
import com.example.myapplication.core.datos.local.entidades.TipoProducto
import com.example.myapplication.prestador.datos.repositorios.PrestadorPresupuestoRepositorio
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- MOTOR DE CIERRE COMERCIAL (v2026.SUPREME) ---
 * [PROPÓSITO]: Gatillar impactos colaterales (Stock, Caja, Agenda) cuando un presupuesto se concreta.
 * [LEY #16]: Integridad Contable.
 */
@Singleton
class MotorCierreComercial @Inject constructor(
    private val budgetRepo: PrestadorPresupuestoRepositorio,
    private val productoDao: ProductoDao,
    private val stockDao: MovimientoStockDao
) {

    /**
     * 🔥 [GATILLO SUPREME]: Impacta el inventario cuando un presupuesto es aceptado o pagado.
     */
    suspend fun procesarCierrePresupuesto(idPresupuesto: String, nuevoEstado: EstadoPresupuesto) {
        if (nuevoEstado != EstadoPresupuesto.ACEPTADO && nuevoEstado != EstadoPresupuesto.PAGADO) return

        // 1. Obtener el presupuesto completo con sus ítems (Cocina)
        val cocinaFull = budgetRepo.obtenerPresupuestoCocinaConItems(idPresupuesto).first() ?: return
        
        // 2. Filtrar solo materiales (PRODUCTOS) que tengan ID de catálogo vinculado
        val materiales = cocinaFull.items.filter { it.tipo == TipoProducto.PRODUCTO }

        materiales.forEach { item ->
            // 3. Registrar el movimiento de salida solo si viene del catálogo
            val idMaestro = item.idOriginal ?: return@forEach

            val movimiento = MovimientoStockEntity(
                idProducto = idMaestro,
                cantidad = -item.cantidad,
                tipo = TipoMovimientoStock.VENTA,
                motivo = "Venta Presupuesto #${cocinaFull.cabecera.numeroPresupuesto ?: idPresupuesto.takeLast(6)}",
                idReferencia = idPresupuesto
            )
            stockDao.registrarMovimiento(movimiento)

            // 4. Actualizar el caché de stock en la tabla maestra
            productoDao.descontarStock(idMaestro, item.cantidad)
        }
        
        android.util.Log.d("MotorCierre", "✅ [STOCK_TRIGGER] Impacto realizado para $idPresupuesto (${materiales.size} materiales)")
    }
}




