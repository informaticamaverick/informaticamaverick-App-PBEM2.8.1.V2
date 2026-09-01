package com.example.myapplication.core.dominio.modelos

/**
 * --- MODELO DE DOMINIO: PROMOCIÓN (SSOT) ---
 */
data class Promocion(
    val id: String,
    val idPrestador: String,
    val idEmpresa: String? = null,
    val idSucursal: String? = null,
    val nombrePrestador: String,
    val urlFotoPrestador: String? = null,
    val estaVerificado: Boolean = false,
    val estaSuscrito: Boolean = false, 
    
    val tipo: TipoPromocion = TipoPromocion.PROMOCION,
    val titulo: String = "",
    val descripcion: String = "",
    val urlImagenes: List<String> = emptyList(),
    
    val etiquetas: List<EtiquetaPromo> = emptyList(),
    val porcentajeDescuento: Int? = null,
    val idCategorias: List<String> = emptyList(),
    val codigoPostal: String? = null,
    val estado: EstadoPromocion = EstadoPromocion.ACTIVA,
    val tipoPromocion: TipoCategoriaPromo = TipoCategoriaPromo.SERVICIO,
    val tipoCta: String = "CHAT", 
    
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaExpiracion: Long = System.currentTimeMillis(),
    val etiquetaPromocion: String? = null, 
    
    val conteoLikes: Int = 0,
    val conteoVistas: Int = 0,
    val conteoComentarios: Int = 0,
    val leGustaAlUsuario: Boolean = false,
    val reputacion: Float = 0f,
    val filtrosBusqueda: List<String> = emptyList()
)

enum class TipoPromocion(val etiquetaDuracion: String) {
    HISTORIA("24 Horas"),
    PROMOCION("7 Días");

    companion object {
        fun desdeNombre(nombre: String): TipoPromocion = entries.find { it.name == nombre } ?: PROMOCION
    }
}

enum class EtiquetaPromo(val etiqueta: String, val colorHex: Long) {
    OFERTA("OFERTA", 0xFFFF9800),
    DESCUENTO("DESCUENTO", 0xFFE91E63),
    NUEVO("NUEVO", 0xFF4CAF50),
    VENTA_FLASH("RELÁMPAGO", 0xFFF44336),
    LIMITADO("LIMITADO", 0xFF2196F3);

    companion object {
        fun desdeNombre(nombre: String): EtiquetaPromo? = entries.find { it.name == nombre }
    }
}

enum class EstadoPromocion {
    BORRADOR,
    ACTIVA,
    EXPIRADA,
    ARCHIVADA;

    companion object {
        fun desdeNombre(nombre: String): EstadoPromocion = entries.find { it.name == nombre } ?: ACTIVA
    }
}

data class PromocionComentario(
    val id: String = "",
    val nombreUsuario: String, 
    val urlFotoUsuario: String? = null, 
    val texto: String, 
    val marcaTiempo: Long = System.currentTimeMillis() 
)

enum class TipoCategoriaPromo(val etiqueta: String, val icono: String) {
    PRODUCTO("PRODUCTO", "📦"),
    SERVICIO("SERVICIO", "🛠️");

    companion object {
        fun desdeNombre(nombre: String): TipoCategoriaPromo = entries.find { it.name == nombre } ?: SERVICIO
    }
}
