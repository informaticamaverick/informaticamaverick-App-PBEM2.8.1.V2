package com.example.myapplication.core.dominio.modelos

/**
 * --- CONCURSO UI MODEL (ELITE v2026.FINAL) ---
 * [LEY #10]: Modelo de visualización para Licitaciones / Concursos Públicos.
 * Desacopla la entidad de Room de la lógica de Compose.
 */
data class ConcursoDominio(
    val idConcurso: String,
    val idCliente: String,
    val titulo: String,
    val descripcion: String,
    val idCategoria: String, // 🔥 [ELITE]: Clave Semántica
    val categoria: String? = null, // Nombre visual (Opcional)
    val iconoCategoria: String? = null,
    val nombreCliente: String,
    val urlMiniaturaCliente: String?,
    val ubicacionResumen: String, // Ej: "CABA - 1425"
    val tiempoRestante: String, // Ej: "Cierra en 3 días"
    val estado: String,
    val exigeVisita: Boolean,
    val exigeGarantia: Boolean,
    val exigePago: Boolean,
    val exigeDocumentacion: Boolean,
    val urlImagenes: List<String>,
    val marcaTiempo: Long,
    
    // --- Sector: Detalle Extendido ---
    val nombreEmpresa: String? = null,
    val nombreSucursal: String? = null,
    val direccionCalle: String? = null,
    val direccionNumero: String? = null,
    val direccionLocalidad: String? = null,
    val direccionCodigoPostal: String? = null,
    val fechaInicio: Long = 0,
    val fechaFin: Long = 0,
    val tieneMiPresupuesto: Boolean = false
)

































