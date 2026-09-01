package com.example.myapplication.core.dominio.modelos

import androidx.annotation.Keep
import com.example.myapplication.core.datos.local.entidades.EstadoEvento
import com.example.myapplication.core.datos.local.entidades.TipoEvento

/**
 * --- PAN DE MIGA (BREADCRUMB) ---
 * [TÍTULO]: Modelo de Interfaz de Evento
 * [PROPÓSITO]: UI Stateless. Los datos ya vienen formateados para Jetpack Compose.
 */
@Keep
data class EventoDominio(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val fechaTexto: String,   // Ejemplo: "Lunes, 28 de Julio"
    val horaTexto: String,    // Ejemplo: "14:30 hs"
    val horaFinTexto: String? = null,
    val tipo: TipoEvento,
    val estado: EstadoEvento,
    val direccion: String,
    val colorAcentoHex: Long, 
    val emojiTipo: String,
    val idParticipante: String, // UID del Prestador o Cliente
    val nombreParticipante: String, 
    val urlFotoParticipante: String? = null,
    val esMio: Boolean = false,
    val idChat: String? = null,
    val precioTotal: String? = null,
    val marcaTiempoUtc: Long = 0L,
    val marcaTiempoFinUtc: Long = 0L,

    // --- SECTOR: ESPECIALIZACIÓN v2026.ELITE ---
    val idRepresentante: String? = null,
    val nombreRepresentante: String? = null,
    val idPresupuestoAsociado: String? = null,
    val direccionOrigen: String? = null,
    val direccionDestino: String? = null
)
