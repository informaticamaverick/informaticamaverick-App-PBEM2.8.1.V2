package com.example.myapplication.core.utilidades

import com.example.myapplication.core.datos.local.entidades.EventoEntity
import com.example.myapplication.core.datos.local.entidades.TipoEvento
import org.json.JSONArray
import org.json.JSONObject

/**
 * --- COMPRESOR DE EVENTOS (V2026.8) ---
 */
@Suppress("unused", "UNUSED_VARIABLE")
object CompresorEvento {

    /**
     * Comprime un evento fijo (Compromiso) para el chat.
     */
    fun comprimirCompromiso(e: EventoEntity): String {
        val json = JSONObject()
        json.put("id", e.id)
        json.put("ti", "FIJO")
        json.put("te", e.tipo.name)
        json.put("in", e.fechaInicioUtc)
        json.put("fi", e.fechaFinUtc)
        json.put("re", e.idRecurso ?: "")
        json.put("nr", e.nombreRecurso ?: "")
        json.put("tit", e.titulo)
        json.put("dir", e.direccion)
        return json.toString()
    }

    /**
     * Comprime una propuesta de agenda abierta (Snapshot de disponibilidad).
     */
    fun comprimirPropuesta(
        idPropuesta: String,
        tipo: TipoEvento,
        titulo: String,
        dias: Int,
        recursosIds: List<String>,
        direccion: String = ""
    ): String {
        val json = JSONObject()
        json.put("id", idPropuesta)
        json.put("ti", "ABIERTO")
        json.put("te", tipo.name)
        json.put("di", dias)
        json.put("tit", titulo)
        json.put("dir", direccion)
        
        val resArray = JSONArray()
        recursosIds.forEach { resArray.put(it) }
        json.put("res", resArray)
        
        return json.toString()
    }

    /**
     * Descomprime una cadena de evento desde el chat.
     */
    fun descomprimir(compacto: String): PropuestaEvento? {
        return try {
            val json = JSONObject(compacto)
            val tipoInterno = json.optString("ti")
            
            val recursos = mutableListOf<String>()
            val resArray = json.optJSONArray("res")
            if (resArray != null) {
                for (i in 0 until resArray.length()) {
                    recursos.add(resArray.getString(i))
                }
            }

            PropuestaEvento(
                id = json.getString("id"),
                esFijo = tipoInterno == "FIJO",
                tipoEvento = TipoEvento.valueOf(json.getString("te")),
                titulo = json.optString("tit"),
                direccion = json.optString("dir"),
                fechaInicio = json.optLong("in", 0L),
                fechaFin = json.optLong("fi", 0L),
                idRecurso = json.optString("re"),
                nombreRecurso = json.optString("nr"),
                diasDisponibles = json.optInt("di", 0),
                recursosOfrecidos = recursos
            )
        } catch (e: Exception) {
            null
        }
    }

    data class PropuestaEvento(
        val id: String,
        val esFijo: Boolean,
        val tipoEvento: TipoEvento,
        val titulo: String,
        val direccion: String,
        val fechaInicio: Long = 0,
        val fechaFin: Long = 0,
        val idRecurso: String? = null,
        val nombreRecurso: String? = null,
        val diasDisponibles: Int = 0,
        val recursosOfrecidos: List<String> = emptyList()
    )
}
