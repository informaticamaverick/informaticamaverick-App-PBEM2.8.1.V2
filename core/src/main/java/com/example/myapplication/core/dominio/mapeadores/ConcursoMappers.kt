package com.example.myapplication.core.dominio.mapeadores

import com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity
import com.example.myapplication.core.dominio.modelos.ConcursoDominio
import com.google.firebase.firestore.DocumentSnapshot

/**
 * --- CONCURSO MAPPER (ELITE v2026.8) ---
 * [ELITE SSOT]: Centraliza la transformación de Concursos entre Room y Firestore.
 */
object ConcursoMappers {

    fun aMapaFirestore(concurso: ConcursoPublicoEntity, huella: String): Map<String, Any?> {
        return mapOf(
            "idConcurso" to concurso.idConcurso,
            "idCliente" to concurso.idCliente,
            "titulo" to concurso.titulo,
            "descripcion" to concurso.descripcion,
            "idCategoria" to concurso.idCategoria,
            "estado" to concurso.estado,
            "fechaInicio" to concurso.fechaInicio,
            "fechaFin" to concurso.fechaFin,
            "marcaTiempo" to concurso.marcaTiempo,
            "estaActivo" to concurso.estaActivo,
            "conteoPresupuestos" to concurso.conteoPresupuestos,
            "estaSuscrito" to concurso.estaSuscrito,
            
            // --- Sector: Identidad Shallow ---
            "nombreCliente" to concurso.nombreCliente,
            "miniaturaCliente" to concurso.miniaturaCliente,
            
            // --- Sector: Ubicación ---
            "direccionCalle" to concurso.direccionCalle,
            "direccionNumero" to concurso.direccionNumero,
            "direccionLocalidad" to concurso.direccionLocalidad,
            "direccionCodigoPostal" to concurso.direccionCodigoPostal,
            "tipoUbicacion" to concurso.tipoUbicacion,
            
            // --- Sector: Cláusulas Elite ---
            "exigeVisita" to concurso.exigeVisita,
            "exigeMetodoPago" to concurso.exigeMetodoPago,
            "exigeGarantia" to concurso.exigeGarantia,
            "exigeDocPrestador" to concurso.exigeDocPrestador,
            
            // --- Sector: Red y Descubrimiento ---
            "filtrosBusqueda" to (concurso.filtrosBusqueda + huella).distinct(),
            "urlImagenes" to concurso.urlImagenes
        )
    }

    fun desdeFirestore(doc: DocumentSnapshot): ConcursoPublicoEntity? {
        if (!doc.exists()) return null
        return try {
            val d = doc.data ?: return null
            val idCliente = d["idCliente"] as? String ?: d["clientId"] as? String ?: ""
            
            if (idCliente.isBlank()) {
                android.util.Log.w("ConcursoMappers", "⚠️ [DESDE_FIRESTORE] idCliente está vacío para concurso: ${doc.id}")
            }

            ConcursoPublicoEntity(
                idConcurso = doc.id,
                titulo = d["titulo"] as? String ?: d["title"] as? String ?: "",
                idCliente = idCliente,
                descripcion = d["descripcion"] as? String ?: d["description"] as? String ?: "",
                idCategoria = d["idCategoria"] as? String ?: d["categoryId"] as? String ?: "OTROS",
                estado = d["estado"] as? String ?: d["status"] as? String ?: "ABIERTA",
                estaActivo = d["estaActivo"] as? Boolean ?: d["isActive"] as? Boolean ?: true,
                marcaTiempo = (d["marcaTiempo"] as? Number ?: d["dateTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                fechaInicio = (d["fechaInicio"] as? Number ?: d["startDate"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                fechaFin = (d["fechaFin"] as? Number ?: d["endDate"] as? Number)?.toLong() ?: 0L,
                conteoPresupuestos = (d["conteoPresupuestos"] as? Number ?: d["budgetCount"] as? Number)?.toInt() ?: 0,
                estaSuscrito = d["estaSuscrito"] as? Boolean ?: false,
                
                nombreCliente = d["nombreCliente"] as? String ?: d["clientDisplayName"] as? String,
                miniaturaCliente = d["miniaturaCliente"] as? String ?: d["clientThumbnail"] as? String,
                
                direccionCalle = d["direccionCalle"] as? String ?: d["locationAddress"] as? String,
                direccionNumero = d["direccionNumero"] as? String ?: d["locationNumber"] as? String,
                direccionLocalidad = d["direccionLocalidad"] as? String ?: d["locationLocality"] as? String,
                direccionCodigoPostal = d["direccionCodigoPostal"] as? String ?: d["locationPostalCode"] as? String,
                tipoUbicacion = d["tipoUbicacion"] as? String ?: d["locationType"] as? String,
                
                exigeVisita = d["exigeVisita"] as? Boolean ?: d["requiresVisit"] as? Boolean ?: false,
                exigeMetodoPago = d["exigeMetodoPago"] as? Boolean ?: d["requiresPaymentMethod"] as? Boolean ?: false,
                exigeGarantia = d["exigeGarantia"] as? Boolean ?: d["requiresWorkGuarantee"] as? Boolean ?: false,
                exigeDocPrestador = d["exigeDocPrestador"] as? Boolean ?: d["requiresProviderDoc"] as? Boolean ?: false,
                
                urlImagenes = (d["urlImagenes"] as? List<*> ?: d["imageUrls"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                filtrosBusqueda = (d["filtrosBusqueda"] as? List<*>)?.map { it.toString() } ?: emptyList()
            )
        } catch (e: Exception) {
            android.util.Log.e("ConcursoMappers", "❌ [DESDE_FIRESTORE_ERR] ${doc.id}: ${e.message}")
            null
        }
    }

    fun aUiModel(concurso: ConcursoPublicoEntity, nombreCat: String? = null, iconoCat: String? = null): ConcursoDominio {
        if (concurso.idCliente.isBlank()) {
            android.util.Log.e("ConcursoMappers", "❌ [A_UI_MODEL] idCliente está VACÍO para concurso: ${concurso.idConcurso}")
        }

        return ConcursoDominio(
            idConcurso = concurso.idConcurso,
            idCliente = concurso.idCliente,
            titulo = concurso.titulo,
            descripcion = concurso.descripcion,
            idCategoria = concurso.idCategoria,
            categoria = nombreCat,
            iconoCategoria = iconoCat,
            nombreCliente = concurso.nombreCliente ?: "Cliente Elite",
            urlMiniaturaCliente = concurso.miniaturaCliente,
            ubicacionResumen = "${concurso.direccionLocalidad ?: "Zona"} - ${concurso.direccionCodigoPostal ?: ""}",
            tiempoRestante = if (concurso.fechaFin == 0L && (concurso.estado == "ABIERTA" || concurso.estado == "OPEN")) "Abierto" else calcularTiempoRestante(concurso.fechaFin),
            estado = concurso.estado,
            exigeVisita = concurso.exigeVisita,
            exigeGarantia = concurso.exigeGarantia,
            exigePago = concurso.exigeMetodoPago,
            exigeDocumentacion = concurso.exigeDocPrestador,
            urlImagenes = concurso.urlImagenes,
            marcaTiempo = concurso.marcaTiempo,
            nombreEmpresa = concurso.nombreEmpresa,
            nombreSucursal = concurso.nombreSucursal,
            direccionCalle = concurso.direccionCalle,
            direccionNumero = concurso.direccionNumero,
            direccionLocalidad = concurso.direccionLocalidad,
            direccionCodigoPostal = concurso.direccionCodigoPostal,
            fechaInicio = concurso.fechaInicio,
            fechaFin = concurso.fechaFin,
            tieneMiPresupuesto = concurso.tieneMiPresupuesto
        )
    }

    private fun calcularTiempoRestante(fechaFin: Long): String {
        val ahora = System.currentTimeMillis()
        val restante = fechaFin - ahora
        if (restante <= 0) return "Cerrado"
        val dias = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(restante)
        return if (dias > 0) "Cierra en ${dias}d" else "Cierra en ${java.util.concurrent.TimeUnit.MILLISECONDS.toHours(restante)}h"
    }
}




































