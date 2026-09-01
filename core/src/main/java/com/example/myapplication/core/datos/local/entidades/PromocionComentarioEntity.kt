package com.example.myapplication.core.datos.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.example.myapplication.core.dominio.modelos.PromocionComentario

/**
 * --- ENTIDAD DE PERSISTENCIA: COMENTARIO DE PROMOCIÓN (ROOM) ---
 * [ELITE v2026.7]: Almacena los comentarios de promociones localmente.
 * Cumple con la Ley #2: Todo dato que llega de la nube debe impactar en Room (Local-First).
 * [LEY #9]: Variables en español y unificación SSOT.
 */
@Entity(
    tableName = "comentarios_promo",
    indices = [Index(value = ["idPromocion"])]
)
data class PromocionComentarioEntity(
    @PrimaryKey
    val id: String, // UID único del comentario
    val idPromocion: String, // Vínculo con la promoción madre
    val nombreUsuario: String, // Nombre del autor del comentario
    val urlFotoUsuario: String? = null, // Foto de perfil del autor
    val texto: String, // Contenido textual del comentario
    val marcaTiempo: Long = System.currentTimeMillis() // Fecha de publicación local
) {

    // --- SECTOR: MAPEADORES (ELITE MAPPING) ---

    /**
     * Convierte la entidad de Room al modelo de dominio para la UI.
     * [Huella de Pan]: Asegura que el dominio no conozca detalles de persistencia.
     */
    fun toDomain(): PromocionComentario = PromocionComentario(
        id = id,
        nombreUsuario = nombreUsuario,
        urlFotoUsuario = urlFotoUsuario,
        texto = texto,
        marcaTiempo = marcaTiempo
    )

    companion object {
        /**
         * Crea una entidad a partir del modelo de dominio.
         * [Huella de Pan]: Usado al descargar datos de Firestore para impactar en Room.
         */
        fun fromDomain(idPromocion: String, domain: PromocionComentario): PromocionComentarioEntity = PromocionComentarioEntity(
            id = domain.id,
            idPromocion = idPromocion,
            nombreUsuario = domain.nombreUsuario,
            urlFotoUsuario = domain.urlFotoUsuario,
            texto = domain.texto,
            marcaTiempo = domain.marcaTiempo
        )
    }
}
