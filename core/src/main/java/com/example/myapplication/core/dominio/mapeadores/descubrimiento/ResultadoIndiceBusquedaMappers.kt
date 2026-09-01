package com.example.myapplication.core.dominio.mapeadores.descubrimiento

import com.example.myapplication.core.dominio.modelos.descubrimiento.ResultadoIndiceBusquedaShallowDominio
import com.google.firebase.firestore.DocumentSnapshot

/**
 * --- MAPPER DE RESULTADOS DE BÚSQUEDA (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Transformar documentos de Firestore en el modelo unificado de búsqueda.
 * [LEY #17]: Protocolo de Bautizo.
 */
object ResultadoIndiceBusquedaMappers {

    fun desdeFirestore(doc: DocumentSnapshot): ResultadoIndiceBusquedaShallowDominio? {
        if (!doc.exists()) return null
        return try {
            val d = doc.data ?: return null
            
            // Extracción segura de tipos numéricos (Firebase devuelve Long/Double según el valor)
            val rep = (d["reputacion"] as? Number)?.toFloat() ?: 0f
            val res = (d["totalReseñas"] as? Number)?.toInt() ?: 0
            val trab = (d["trabajosRealizados"] as? Number)?.toInt() ?: 0
            val lat = (d["latitud"] as? Number)?.toDouble() ?: 0.0
            val lng = (d["longitud"] as? Number)?.toDouble() ?: 0.0

            ResultadoIndiceBusquedaShallowDominio(
                id = doc.id,
                idPropietario = d["idPropietario"] as? String ?: "",
                idPadre = d["idPadre"] as? String,
                tipoIdentidad = d["tipoIdentidad"] as? String ?: "PRESTADOR",
                nombreVisible = d["nombreVisible"] as? String ?: "",
                nombreEmpresa = d["nombreEmpresa"] as? String,
                urlFoto = d["urlFoto"] as? String,
                // [ELITE]: Fallback para llaves antiguas 'urlMiniatura' -> 'miniaturaBase64'
                miniaturaBase64 = (d["miniaturaBase64"] as? String) ?: (d["urlMiniatura"] as? String),
                reputacion = rep,
                totalReseñas = res,
                trabajosRealizados = trab,
                estaSuscrito = d["estaSuscrito"] as? Boolean ?: false,
                estaVerificado = d["estaVerificado"] as? Boolean ?: false,
                estaEnLinea = d["estaEnLinea"] as? Boolean ?: false,
                calle = d["calle"] as? String ?: "",
                numero = d["numero"] as? String ?: "",
                codigoPostal = d["codigoPostal"] as? String ?: "",
                latitud = lat,
                longitud = lng,
                geohash = d["geohash"] as? String ?: "",
                idCategorias = (d["idCategorias"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                filtrosBusqueda = (d["filtrosBusqueda"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                brindaServicio = d["brindaServicio"] as? Boolean ?: false,
                brindaProducto = d["brindaProducto"] as? Boolean ?: false,
                brindaTurnos = d["brindaTurnos"] as? Boolean ?: false,
                atiende24h = d["atiende24h"] as? Boolean ?: false,
                realizaEnvios = d["realizaEnvios"] as? Boolean ?: false,
                visitaADomicilio = d["visitaADomicilio"] as? Boolean ?: false,
                tieneLocalFisico = d["tieneLocalFisico"] as? Boolean ?: false
            )
        } catch (e: Exception) {
            android.util.Log.e("BUSQUEDA_MAPPER", "❌ Error al mapear resultado ${doc.id}: ${e.message}")
            null
        }
    }
}



