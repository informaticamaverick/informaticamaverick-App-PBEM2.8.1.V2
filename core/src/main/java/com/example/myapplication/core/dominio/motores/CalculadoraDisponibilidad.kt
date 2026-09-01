package com.example.myapplication.core.dominio.motores

import androidx.annotation.Keep
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.modelos.HorarioDominio
import com.example.myapplication.core.dominio.modelos.RangoHorarioDominio
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- CALCULADORA DE DISPONIBILIDAD (MOTOR UNIFICADO v2026.SUPREME) ---
 */
@Keep
@Singleton
class CalculadoraDisponibilidad @Inject constructor() {

    @Keep
    data class BloqueHorario(
        val horaTexto: String,      // Formato "HH:mm"
        val estaOcupado: Boolean,
        val inicioUtc: Long,
        val finUtc: Long
    )

    fun obtenerBloquesLibres(
        horarioSucursal: HorarioDominio,
        horarioRecurso: HorarioDominio?,
        excepciones: List<ExcepcionHorariaEntity>,
        eventosOcupados: List<EventoEntity>,
        fecha: Date,
        duracionMinutos: Int = 30
    ): List<BloqueHorario> {
        val fechaString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(fecha)
        if (excepciones.any { it.estaCerrado && esMismaFecha(it.fechaLong, fecha) }) {
            return emptyList()
        }

        val rangosBase = if (horarioRecurso != null) {
            obtenerRangosParaDia(horarioRecurso, fecha)
        } else {
            obtenerRangosParaDia(horarioSucursal, fecha)
        }

        if (rangosBase.isEmpty()) return emptyList()

        return generarBloquesParaDia(fechaString, rangosBase, eventosOcupados, duracionMinutos)
    }

    fun generarBloquesParaDia(
        fechaIso: String,
        rangos: List<RangoHorarioDominio>,
        eventosExistentes: List<EventoEntity>,
        duracionMinutos: Int = 30
    ): List<BloqueHorario> {
        val bloques = mutableListOf<BloqueHorario>()
        val calBase = Calendar.getInstance().apply {
            val partes = fechaIso.split("-")
            if (partes.size >= 3) {
                set(partes[0].toInt(), partes[1].toInt() - 1, partes[2].toInt())
            }
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        
        for (rango in rangos.filter { it.estaHabilitado }) {
            val duracionMs = duracionMinutos * 60 * 1000L
            val calInicio = Calendar.getInstance().apply {
                time = calBase.time
                val p = rango.inicio.split(":")
                if (p.size >= 2) { set(Calendar.HOUR_OF_DAY, p[0].toInt()); set(Calendar.MINUTE, p[1].toInt()) }
            }
            val calFin = Calendar.getInstance().apply {
                time = calBase.time
                val p = rango.fin.split(":")
                if (p.size >= 2) { set(Calendar.HOUR_OF_DAY, p[0].toInt()); set(Calendar.MINUTE, p[1].toInt()) }
            }
            
            var posicionActual = calInicio.timeInMillis
            val limiteFinal = calFin.timeInMillis
            val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
            
            while (posicionActual + duracionMs <= limiteFinal) {
                val finBloque = posicionActual + duracionMs
                val ocupado = eventosExistentes.any { ev -> posicionActual < ev.fechaFinUtc && finBloque > ev.fechaInicioUtc }
                bloques.add(BloqueHorario(formatoHora.format(Date(posicionActual)), ocupado, posicionActual, finBloque))
                posicionActual += duracionMs
            }
        }
        return bloques.distinctBy { it.inicioUtc }.sortedBy { it.inicioUtc }
    }

    private fun obtenerRangosParaDia(horario: HorarioDominio, fecha: Date): List<RangoHorarioDominio> {
        val cal = Calendar.getInstance(); cal.time = fecha
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> horario.lunes
            Calendar.TUESDAY -> horario.martes
            Calendar.WEDNESDAY -> horario.miercoles
            Calendar.THURSDAY -> horario.jueves
            Calendar.FRIDAY -> horario.viernes
            Calendar.SATURDAY -> horario.sabado
            Calendar.SUNDAY -> horario.domingo
            else -> emptyList()
        }
    }

    private fun esMismaFecha(timestamp: Long, fecha: Date): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = timestamp }
        val c2 = Calendar.getInstance().apply { time = fecha }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }
}
