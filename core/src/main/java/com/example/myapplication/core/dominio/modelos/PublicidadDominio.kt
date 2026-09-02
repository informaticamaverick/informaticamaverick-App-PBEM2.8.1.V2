package com.example.myapplication.core.dominio.modelos

/**
 * --- PUBLICIDAD DE EMPRESAS (banners de terceros cargados desde el panel admin) ---
 * Espejo de la colección Firestore `publicidad` (HTML Admin/js/schemas.js).
 */
data class PublicidadDominio(
    val id: String,
    val empresa: String,
    val rubro: String?,
    val direccion: String?,
    val descripcion: String?,
    val imagenUrl: String?,
    val contactoTelefono: String?,
    val contactoLink: String?
)
