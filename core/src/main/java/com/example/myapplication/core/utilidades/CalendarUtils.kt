package com.example.myapplication.core.utilidades

import com.example.myapplication.core.datos.local.entidades.EventoEntity
import com.example.myapplication.core.datos.local.entidades.EstadoEvento
import java.text.SimpleDateFormat
import java.util.*

/**
 * --- UTILIDADES DE TIEMPO ATÓMICO (ELITE v2026) ---
 * Centraliza la lógica de conversión de fechas y horas.
 * Actualizado para soportar EventoEntity exclusivamente.
 */
object CalendarUtils {

    /**
     * Convierte strings de fecha y hora a un Timestamp (Long) en hora local.
     */
    fun convertToUtc(date: String?, time: String?): Long {
        if (date.isNullOrBlank()) return 0L
        
        val cleanDate = date.replace("/", "-").trim()
        val cleanTime = (time ?: "00:00").replace("hs", "").trim()
        
        // Priorizamos ISO y formatos con año completo para evitar ambigüedades (Elite v2026)
        val formats = listOf(
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd",
            "dd-MM-yyyy HH:mm",
            "dd-MM-yyyy"
        )
        
        val dateTimeStr = if (cleanTime.contains(":")) "$cleanDate $cleanTime" else "$cleanDate 00:00"
        
        for (fmt in formats) {
            try {
                val sdf = SimpleDateFormat(fmt, Locale.getDefault()).apply {
                    // [ESTRICTO]: Evita que 2026 se interprete como días/meses inválidos
                    isLenient = false 
                }
                val result = sdf.parse(dateTimeStr)?.time ?: 0L
                if (result != 0L) return result
            } catch (e: Exception) {
                continue
            }
        }
        
        return 0L
    }

    fun formatIsoDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(timestamp))
    }

    /**
     * [ELITE 2026] Detección de Conflictos para el nuevo motor.
     */
    fun verificarConflictos(
        nuevoEvento: EventoEntity,
        eventosExistentes: List<EventoEntity>,
        capacidad: Int = 1
    ): List<EventoEntity> {
        val solapados = eventosExistentes.filter { existente ->
            existente.id != nuevoEvento.id &&
            existente.estado != EstadoEvento.CANCELADO &&
            maxOf(nuevoEvento.fechaInicioUtc, existente.fechaInicioUtc) < minOf(nuevoEvento.fechaFinUtc, existente.fechaFinUtc)
        }
        
        return if (solapados.size >= capacidad) solapados else emptyList()
    }

    /**
     * 🔥 [ELITE] Detección de Conflictos por Recurso Físico.
     */
    fun verificarConflictoRecurso(
        idRecurso: String,
        inicio: Long,
        fin: Long,
        eventosExistentes: List<EventoEntity>
    ): EventoEntity? {
        return eventosExistentes.find { existente ->
            existente.idRecurso == idRecurso &&
            existente.estado != EstadoEvento.CANCELADO &&
            maxOf(inicio, existente.fechaInicioUtc) < minOf(fin, existente.fechaFinUtc)
        }
    }
}
