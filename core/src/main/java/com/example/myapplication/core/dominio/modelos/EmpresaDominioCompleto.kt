package com.example.myapplication.core.dominio.modelos

/**
 * --- MODELO DE DOMINIO EMPRESA COMPLETO (Marca e Infraestructura) ---
 * [LEY #9]: Estándar Mav (Idioma Español).
 * Agrupa la identidad legal con todas sus sucursales operativas.
 */
data class EmpresaDominioCompleto(
    val empresa: EmpresaDominio,
    val sucursales: List<SucursalDominioCompleto> = emptyList()
) {
    // --- ALIASES DE COMPATIBILIDAD (LEGACY BRIDGE) ---
    val id: String get() = empresa.id
    val nombre: String get() = empresa.nombre
    val correoElectronico: String get() = empresa.correoContacto
    val urlFoto: Any? get() = empresa.urlFoto
    val urlMiniatura: Any? get() = empresa.urlMiniatura
}

