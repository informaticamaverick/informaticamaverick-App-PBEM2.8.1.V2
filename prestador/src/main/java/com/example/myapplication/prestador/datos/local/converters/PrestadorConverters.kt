package com.example.myapplication.prestador.datos.local.converters

import androidx.room.TypeConverter
import com.example.myapplication.prestador.datos.local.entidades.TipoMovimientoStock

/**
 * --- CONVERSORES PRIVADOS DEL PRESTADOR (App Naranja) ---
 */
class PrestadorConverters {

    @TypeConverter
    fun fromTipoMovimientoStock(value: TipoMovimientoStock): String = value.name

    @TypeConverter
    fun toTipoMovimientoStock(value: String?): TipoMovimientoStock = 
        try { TipoMovimientoStock.valueOf(value ?: "") } 
        catch(e: Exception) { TipoMovimientoStock.VENTA }
}
