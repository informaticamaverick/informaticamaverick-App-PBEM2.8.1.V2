package com.example.myapplication.core.datos.local

import androidx.room.TypeConverter
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.modelos.RangoHorarioDominio
import com.example.myapplication.core.dominio.modelos.TipoDireccion
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * --- CONVERSORES DE ROOM (v2026.ELITE) ---
 * [PROPÓSITO]: Persistir tipos complejos y enums en la base de datos SQL.
 */
class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>): String = Gson().toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromRangoHorarioList(value: List<RangoHorarioDominio>): String = Gson().toJson(value)

    @TypeConverter
    fun toRangoHorarioList(value: String): List<RangoHorarioDominio> {
        val listType = object : TypeToken<List<RangoHorarioDominio>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromTipoDireccion(value: TipoDireccion): String = value.name

    @TypeConverter
    fun toTipoDireccion(value: String?): TipoDireccion = try { 
        TipoDireccion.valueOf(value ?: "PERFIL_USUARIO") 
    } catch(e: Exception) { 
        TipoDireccion.PERFIL_USUARIO 
    }

    @TypeConverter
    fun fromTipoMensaje(value: TipoMensaje): String = value.name

    @TypeConverter
    fun toTipoMensaje(value: String?): TipoMensaje = try { 
        TipoMensaje.valueOf(value ?: "TEXTO") 
    } catch(e: Exception) { 
        TipoMensaje.TEXTO 
    }

    @TypeConverter
    fun fromTipoAvisoEstado(value: TipoAvisoEstado): String = value.name

    @TypeConverter
    fun toTipoAvisoEstado(value: String?): TipoAvisoEstado = try { 
        TipoAvisoEstado.valueOf(value ?: "") 
    } catch(e: Exception) { 
        TipoAvisoEstado.COMPLETADO 
    }

    @TypeConverter
    fun fromTipoProducto(value: TipoProducto): String = value.name

    @TypeConverter
    fun toTipoProducto(value: String?): TipoProducto = try { 
        TipoProducto.valueOf(value ?: "PRODUCTO") 
    } catch(e: Exception) { 
        TipoProducto.PRODUCTO 
    }

    @TypeConverter
    fun fromTipoPresupuesto(value: TipoPresupuesto): String = value.name

    @TypeConverter
    fun toTipoPresupuesto(value: String?): TipoPresupuesto = try { 
        TipoPresupuesto.valueOf(value ?: "NUEVO") 
    } catch(e: Exception) { 
        TipoPresupuesto.NUEVO 
    }

    @TypeConverter
    fun fromTipoEvento(value: TipoEvento): String = value.name

    @TypeConverter
    fun toTipoEvento(value: String?): TipoEvento = try { 
        TipoEvento.valueOf(value ?: "") 
    } catch(e: Exception) { 
        TipoEvento.VISITA_TECNICA 
    }

    @TypeConverter
    fun fromEstadoEvento(value: EstadoEvento): String = value.name

    @TypeConverter
    fun toEstadoEvento(value: String?): EstadoEvento = try { 
        EstadoEvento.valueOf(value ?: "") 
    } catch(e: Exception) { 
        EstadoEvento.SOLICITADO 
    }

    @TypeConverter
    fun fromEstadoMensaje(value: EstadoMensaje): String = value.name

    @TypeConverter
    fun toEstadoMensaje(value: String?): EstadoMensaje = try { 
        EstadoMensaje.valueOf(value ?: "") 
    } catch(e: Exception) { 
        EstadoMensaje.ENVIANDO 
    }

    @TypeConverter
    fun fromEstadoPresupuesto(value: EstadoPresupuesto): String = value.name

    @TypeConverter
    fun toEstadoPresupuesto(value: String?): EstadoPresupuesto = try { 
        EstadoPresupuesto.valueOf(value ?: "PENDIENTE") 
    } catch(e: Exception) { 
        EstadoPresupuesto.PENDIENTE 
    }
}
