package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep

/**
 * --- TIPO DE PRESUPUESTO (v2026.ELITE) ---
 * [PROPÓSITO]: Clasificar el origen y propósito de la oferta para aplicar estilos y reglas de negocio.
 */
@Keep
enum class TipoPresupuesto {
    NUEVO,          // Creación directa desde el dashboard
    CONVERSACION,   // Originado en un chat 1 a 1
    CONCURSO,       // Postulación a una licitación pública
    RAPIDO          // Snapshot veloz de un solo ítem
}
