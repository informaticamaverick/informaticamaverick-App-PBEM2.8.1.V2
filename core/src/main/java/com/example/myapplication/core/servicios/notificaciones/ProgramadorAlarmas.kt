package com.example.myapplication.core.servicios.notificaciones

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.myapplication.core.datos.local.entidades.EventoEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- PROGRAMADOR DE ALARMAS (v2026.ELITE) ---
 */
@Singleton
class ProgramadorAlarmas @Inject constructor(
    @ApplicationContext private val contexto: Context
) {
    private val gestorAlarmas = contexto.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Programa recordatorios para un evento de la agenda.
     */
    fun programarAvisosEvento(evento: EventoEntity) {
        val inicioMs = evento.fechaInicioUtc
        if (inicioMs <= 0L || inicioMs < System.currentTimeMillis()) return

        // 1. Recordatorio 15 minutos antes
        val quinceAntes = inicioMs - (15 * 60 * 1000)
        if (quinceAntes > System.currentTimeMillis()) {
            configurarAlarma(
                evento.id.hashCode(),
                quinceAntes,
                "Próximo: ${evento.titulo}",
                "En 15 minutos: ${evento.nombreSucursal ?: "Servicio"} en ${evento.direccion}"
            )
        }

        // 2. Notificación en el momento de inicio
        configurarAlarma(
            evento.id.hashCode() + 1,
            inicioMs,
            "Ahora: ${evento.titulo}",
            "Iniciando compromiso con ${evento.nombreSucursal ?: "Servicio"}"
        )
    }

    private fun configurarAlarma(id: Int, momento: Long, titulo: String, mensaje: String) {
        val intent = Intent("com.example.myapplication.ACCION_ALERTA_AGENDA").apply {
            putExtra("TITULO", titulo)
            putExtra("MENSAJE", mensaje)
            setPackage(contexto.packageName)
        }

        val pi = PendingIntent.getBroadcast(
            contexto, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            gestorAlarmas.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, momento, pi)
        } else {
            gestorAlarmas.setExact(AlarmManager.RTC_WAKEUP, momento, pi)
        }
    }

    fun cancelarAvisosEvento(evento: EventoEntity) {
        val intent = Intent("com.example.myapplication.ACCION_ALERTA_AGENDA")
        val pi1 = PendingIntent.getBroadcast(contexto, evento.id.hashCode(), intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        pi1?.let { gestorAlarmas.cancel(it) }
        
        val pi2 = PendingIntent.getBroadcast(contexto, evento.id.hashCode() + 1, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        pi2?.let { gestorAlarmas.cancel(it) }
    }
}
