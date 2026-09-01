package com.example.myapplication.core.dominio.modelos

import androidx.compose.runtime.Immutable

/**
 * --- PERFIL PRESTADOR INSIGNIA (Atómico) ---
 * [LEY #9]: Estándar Maverick en Español.
 * Representa un badge visual de capacidad o estatus.
 */
@Immutable
data class PerfilPrestadorInsignia(
    val id: String,
    val icono: String,
    val etiqueta: String,
    val estaActiva: Boolean
) {
    companion object {
        /**
         * 🔥 [ELITE]: Genera el pack estándar de insignias (v2026).
         * Centraliza la lógica para evitar discrepancias entre Mappers (Ley #9).
         */
        fun crearPackEstandar(
            brindaServicio: Boolean = false,
            brindaProducto: Boolean = false,
            atiende24h: Boolean = false,
            tieneLocalFisico: Boolean = true,
            visitaADomicilio: Boolean = false,
            realizaEnvios: Boolean = false,
            brindaTurnos: Boolean = false
        ): List<PerfilPrestadorInsignia> {
            return listOf(
                PerfilPrestadorInsignia("serv", "🛠️", "Servicios", brindaServicio),
                PerfilPrestadorInsignia("prod", "📦", "Productos", brindaProducto),
                PerfilPrestadorInsignia("24h", "🚨", "Urgencias 24h", atiende24h),
                PerfilPrestadorInsignia("loc", "🏪", "Local Físico", tieneLocalFisico),
                PerfilPrestadorInsignia("visit", "🏠", "A Domicilio", visitaADomicilio),
                PerfilPrestadorInsignia("env", "🚚", "Envíos", realizaEnvios),
                PerfilPrestadorInsignia("date", "📅", "Turnos Online", brindaTurnos)
            )
        }
    }
}
