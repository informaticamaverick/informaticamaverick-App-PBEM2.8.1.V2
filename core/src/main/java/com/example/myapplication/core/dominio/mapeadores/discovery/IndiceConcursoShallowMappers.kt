package com.example.myapplication.core.dominio.mapeadores.discovery

import com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity
import com.example.myapplication.core.dominio.modelos.discovery.IndiceConcursoShallowDominio
import com.example.myapplication.core.dominio.mapeadores.shallow.UsuarioShallowMappers
import com.example.myapplication.core.utilidades.ImageUtils
import com.google.firebase.firestore.DocumentSnapshot

/**
 * --- ÍNDICE CONCURSO SHALLOW MAPPER (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Crear el sobre de descubrimiento para licitaciones.
 * [LEY #17]: Protocolo de Bautizo.
 */
object IndiceConcursoShallowMappers {

    fun deEntidadADominio(
        concurso: ConcursoPublicoEntity, 
        usuarioShallow: com.example.myapplication.core.dominio.modelos.shallow.UsuarioShallowDominio
    ): IndiceConcursoShallowDominio {
        return IndiceConcursoShallowDominio(
            idConcurso = concurso.idConcurso,
            idCliente = concurso.idCliente,
            idPropietario = concurso.idCliente, // 🔥 [ELITE] Alineación con IndiceBusqueda
            tipoIdentidad = "CONCURSO",
            titulo = concurso.titulo,
            descripcion = concurso.descripcion,
            idCategoria = concurso.idCategoria,
            estado = concurso.estado,
            urlImagenes = concurso.urlImagenes,
            marcaTiempo = concurso.marcaTiempo,
            fechaFin = concurso.fechaFin,
            codigoPostal = concurso.direccionCodigoPostal ?: "",
            filtrosBusqueda = concurso.filtrosBusqueda,
            autor = usuarioShallow
        )
    }

    fun deDominioAMapa(dominio: IndiceConcursoShallowDominio): Map<String, Any?> {
        // [SUPREME.FIX]: Limpieza de punteros de memoria y normalización de miniatura (Modelo IndiceBusqueda)
        fun procesarCampoImagen(campo: Any?): String? {
            return when (campo) {
                is ByteArray -> ImageUtils.bytesToBase64(campo)
                else -> {
                    val s = campo?.toString()
                    if (s?.startsWith("[B@") == true) null else s
                }
            }
        }

        val miniLimpia = procesarCampoImagen(dominio.autor.urlMiniatura)

        return mapOf(
            // --- 1. IDENTIFICADORES RAÍZ (Modelo IndiceBusqueda) ---
            "id" to dominio.idConcurso,
            "idConcurso" to dominio.idConcurso,
            "idCliente" to dominio.idCliente,
            "idPropietario" to dominio.idPropietario.ifBlank { dominio.idCliente }, 
            "tipoIdentidad" to dominio.tipoIdentidad,

            // --- 2. DATOS DEL CONCURSO ---
            "titulo" to dominio.titulo,
            "descripcion" to dominio.descripcion,
            "idCategoria" to dominio.idCategoria,
            "estado" to dominio.estado,
            "urlImagenes" to dominio.urlImagenes,
            "marcaTiempo" to dominio.marcaTiempo,
            "fechaFin" to dominio.fechaFin,
            "codigoPostal" to dominio.codigoPostal,
            "filtrosBusqueda" to dominio.filtrosBusqueda,

            // --- 3. IDENTIDAD APLANADA (Modelo IndiceBusqueda) ---
            "nombreVisible" to dominio.autor.nombreVisible,
            "urlMiniatura" to miniLimpia,
            "miniaturaBase64" to miniLimpia,

            // --- 4. OBJETO AUTOR (Reglas de Seguridad actuales) ---
            "autor" to UsuarioShallowMappers.deDominioAMapa(dominio.autor).toMutableMap().apply {
                this["urlMiniatura"] = miniLimpia
            }
        )
    }

    fun desdeFirestore(doc: DocumentSnapshot): IndiceConcursoShallowDominio? {
        if (!doc.exists()) return null
        return try {
            val d = doc.data ?: return null
            val autorMap = d["autor"] as? Map<*, *>
            
            // [ELITE]: Fallback para campos aplanados (Modelo IndiceBusqueda)
            val nombreAutor = d["nombreVisible"] as? String ?: autorMap?.get("nombreVisible") as? String ?: ""
            val miniAutor = d["miniaturaBase64"] as? String ?: d["urlMiniatura"] as? String ?: autorMap?.get("urlMiniatura") as? String
            val idAutor = d["idPropietario"] as? String ?: autorMap?.get("id") as? String ?: ""

            IndiceConcursoShallowDominio(
                idConcurso = d["idConcurso"] as? String ?: d["id"] as? String ?: doc.id,
                idCliente = d["idCliente"] as? String ?: idAutor,
                titulo = d["titulo"] as? String ?: "",
                descripcion = d["descripcion"] as? String ?: "",
                idCategoria = d["idCategoria"] as? String ?: "",
                estado = d["estado"] as? String ?: "ABIERTA",
                urlImagenes = (d["urlImagenes"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                marcaTiempo = (d["marcaTiempo"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                fechaFin = (d["fechaFin"] as? Number)?.toLong() ?: 0L,
                codigoPostal = d["codigoPostal"] as? String ?: "",
                filtrosBusqueda = (d["filtrosBusqueda"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                autor = com.example.myapplication.core.dominio.modelos.shallow.UsuarioShallowDominio(
                    id = idAutor,
                    nombreVisible = nombreAutor,
                    urlMiniatura = miniAutor,
                    reputacion = (autorMap?.get("reputacion") as? Number)?.toFloat() ?: 0f,
                    estaEnLinea = autorMap?.get("estaEnLinea") as? Boolean ?: false,
                    estaSuscrito = autorMap?.get("estaSuscrito") as? Boolean ?: false
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("IndiceConcursoMappers", "❌ Error al mapear desde Firestore: ${e.message}")
            null
        }
    }

    fun deShallowAEntidad(dominio: IndiceConcursoShallowDominio): com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity {
        return com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity(
            idConcurso = dominio.idConcurso,
            titulo = dominio.titulo,
            idCliente = dominio.idCliente.ifEmpty { dominio.autor.id },
            descripcion = dominio.descripcion,
            idCategoria = dominio.idCategoria,
            estado = dominio.estado,
            marcaTiempo = dominio.marcaTiempo,
            fechaFin = dominio.fechaFin,
            direccionCodigoPostal = dominio.codigoPostal,
            urlImagenes = dominio.urlImagenes,
            filtrosBusqueda = dominio.filtrosBusqueda,
            nombreCliente = dominio.autor.nombreVisible,
            miniaturaCliente = dominio.autor.urlMiniatura,
            estaSuscrito = dominio.autor.estaSuscrito
        )
    }
}



