package com.example.myapplication.core.dominio.mapeadores

import com.example.myapplication.core.datos.local.entidades.ReviewEntity
import com.example.myapplication.core.dominio.modelos.ReseñaDominio

object ReviewMappers {
    fun deEntidadADominio(r: ReviewEntity): ReseñaDominio {
        return ReseñaDominio(
            id = r.id,
            idAutor = r.reviewerId,
            nombreAutor = r.reviewerName,
            fotoAutorUrl = r.reviewerPhotoUrl,
            calificacion = r.rating,
            comentario = r.text,
            fechaUtc = r.timestamp,
            respuestaPrestador = r.response
        )
    }
}



