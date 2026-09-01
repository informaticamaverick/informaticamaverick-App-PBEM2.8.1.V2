package com.example.myapplication.core.dominio.modelos

/**
 * --- MODELO DE DOMINIO SUCURSAL COMPLETO (SSOT Operativo) ---
 * [LEY #9]: Estándar Mav (Idioma Español).
 * Agrupa todos los activos de un Punto de Venta (POS) para su consumo en lógica.
 */
data class SucursalDominioCompleto(
    val sucursal: SucursalDominio,
    val direccion: DireccionDominio? = null,
    val horario: HorarioDominio? = null,
    val equipoTrabajo: List<EquipoTrabajoDominio> = emptyList(),
    val recursos: List<RecursoDominio> = emptyList(),
    val reseñas: List<ReseñaDominio> = emptyList()
) {
    // --- ALIASES DE COMPATIBILIDAD (LEGACY BRIDGE) ---
    val id: String get() = sucursal.id
    val nombre: String get() = sucursal.nombre
    val idEmpresaPadre: String get() = sucursal.idEmpresaPadre
}

