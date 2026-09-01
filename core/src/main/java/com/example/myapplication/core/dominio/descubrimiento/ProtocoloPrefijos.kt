package com.example.myapplication.core.dominio.descubrimiento

import androidx.annotation.Keep

/**
 * --- PROTOCOLO DE PREFIJOS DE DESCUBRIMIENTO (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Definir las etiquetas de primer nivel para huellas y tópicos.
 * [LEY #9]: Estándar Maverick en Español.
 */
@Keep
object ProtocoloPrefijos {
    const val ZONA = "Z"           // Prefijo para Zona (Código Postal)
    const val PRESTADOR = "P"      // Prefijo para Búsqueda de Expertos
    const val PROXIMIDAD = "G"     // Prefijo para Cercanía (Geohash)
    const val OFERTA = "H"         // Prefijo para Promociones e Historias
    const val CONCURSO = "C"       // Prefijo para Licitaciones Públicas
}
