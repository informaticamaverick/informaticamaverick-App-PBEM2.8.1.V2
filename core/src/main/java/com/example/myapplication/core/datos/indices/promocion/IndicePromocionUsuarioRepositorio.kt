  package com.example.myapplication.core.datos.indices.promocion

import com.example.myapplication.core.dominio.descubrimiento.GeneradorTópicosFCM
import com.example.myapplication.core.dominio.descubrimiento.ProtocoloPrefijos
import com.example.myapplication.core.dominio.modelos.discovery.IndicePromocionShallowDominio
import com.example.myapplication.core.dominio.mapeadores.discovery.IndicePromocionShallowMappers
import com.example.myapplication.core.dominio.ubicacion.CalculadoraGeografica
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

/**
 * --- REPOSITORIO DE LECTURA: ÍNDICE DE PROMOCIONES (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Consultar ofertas e historias cercanas al usuario.
 * [LEY #17]: Protocolo de Bautizo. Actúa como el Lector del índice.
 */
@Singleton
class IndicePromocionUsuarioRepositorio @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val generadorTopicos: GeneradorTópicosFCM
) {

    /**
     * Obtiene el feed de promociones por proximidad (rejilla de 9 vecinos).
     */
    suspend fun obtenerFeedCercano(
        latitud: Double,
        longitud: Double,
        rubrosInteres: List<String>,
        limite: Int = 30,
        cursorId: String? = null
    ): List<IndicePromocionShallowDominio> {
        val hashRaiz = CalculadoraGeografica.generarGeohash(latitud, longitud, 5)
        val rejilla9Vecinos = CalculadoraGeografica.obtener9Vecinos(hashRaiz)
        
        val tagsBusqueda = mutableListOf<String>()
        rejilla9Vecinos.forEach { hash ->
            rubrosInteres.forEach { rubro ->
                tagsBusqueda.add("${ProtocoloPrefijos.PROXIMIDAD}_${hash}_$rubro")
            }
        }

        if (tagsBusqueda.isEmpty()) return emptyList()

        return ejecutarConsultaFeed(tagsBusqueda, limite, cursorId)
    }

    /**
     * 🔥 [ELITE]: Obtiene el feed de promociones por Zona (CP) y Rubro/Afinidad.
     * [LEY #17]: Permite descubrir ofertas por Categoría o SuperCategoría en la zona.
     */
    suspend fun obtenerFeedPorZona(
        codigoPostal: String,
        rubrosOAfinidades: List<String>,
        limite: Int = 30,
        cursorId: String? = null
    ): List<IndicePromocionShallowDominio> {
        val tagsBusqueda = if (rubrosOAfinidades.isEmpty()) {
            listOf(generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.OFERTA, codigoPostal))
        } else {
            rubrosOAfinidades.map { id ->
                generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.OFERTA, codigoPostal, id)
            }
        }

        return ejecutarConsultaFeed(tagsBusqueda, limite, cursorId)
    }

    private suspend fun ejecutarConsultaFeed(
        tags: List<String>,
        limite: Int,
        cursorId: String?
    ): List<IndicePromocionShallowDominio> {
        return try {
            // Firebase limits whereArrayContainsAny to 10 elements
            val tagsLimitados = tags.distinct().take(10)
            
            var query = firestore.collection(IndicePromocionPrestadorRepositorio.COLECCION_PROMOCIONES)
                .whereArrayContainsAny("filtrosBusqueda", tagsLimitados)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .limit(limite.toLong())

            if (cursorId != null) {
                val lastDoc = firestore.collection(IndicePromocionPrestadorRepositorio.COLECCION_PROMOCIONES).document(cursorId).get().await()
                query = query.startAfter(lastDoc)
            }

            val snapshot = query.get().await()
            snapshot.documents.mapNotNull { doc -> IndicePromocionShallowMappers.desdeFirestore(doc) }
        } catch (e: Exception) {
            Log.e("INDICE_PROMO", "❌ Error al consultar feed: ${e.message}")
            emptyList()
        }
    }
}

