package com.example.myapplication.prestador.datos.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.core.datos.local.Converters
import com.example.myapplication.prestador.datos.local.dao.PresupuestoDao
import com.example.myapplication.prestador.datos.local.dao.ProductoDao
import com.example.myapplication.prestador.datos.local.dao.MovimientoStockDao
import com.example.myapplication.core.datos.local.dao.ExcepcionHorariaDao
import com.example.myapplication.prestador.datos.local.dao.PrestadorExcepcionHorariaDao
import com.example.myapplication.prestador.datos.local.converters.PrestadorConverters
import com.example.myapplication.prestador.datos.local.entidades.*
import com.example.myapplication.core.datos.local.entidades.*

/**
 * --- BASE DE DATOS LOCAL DEL PRESTADOR (COCINA PRIVADA - v2026.ELITE) ---
 * [LEY #9]: Estándar Mav en Español.
 * [PROPÓSITO]: Gestionar el catálogo privado y borradores del prestador.
 */
@Database(
    entities = [
        // --- SECTOR A: COCINA PRIVADA ---
        PresupuestoEntity::class,
        ProductoEntity::class,
        ProductoFtsEntity::class,
        MovimientoStockEntity::class, // 🔥 [SUPREME]
        BorradorPresupuestoEntity::class,
        PlantillaPresupuestoEntity::class,
        com.example.myapplication.core.datos.local.entidades.ExcepcionHorariaEntity::class
    ],
    version = 11, // 🔥 [SUPREME]: Actualización de esquemas financieros y stock
    exportSchema = true
)
@TypeConverters(Converters::class, PrestadorConverters::class)
abstract class PrestadorDatabase : RoomDatabase() {
    abstract fun presupuestoDao(): PresupuestoDao
    abstract fun ProductoDao(): ProductoDao
    abstract fun movimientoStockDao(): MovimientoStockDao
    abstract fun excepcionHorariaDao(): PrestadorExcepcionHorariaDao // 🔥 [SUPREME]
}

