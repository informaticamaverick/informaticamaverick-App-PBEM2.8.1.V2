package com.example.myapplication.core.datos.indices.promocion

import com.example.myapplication.core.dominio.descubrimiento.GeneradorTópicosFCM
import com.example.myapplication.core.dominio.descubrimiento.ProtocoloPrefijos
import com.example.myapplication.core.dominio.modelos.discovery.IndicePromocionShallowDominio
import com.example.myapplication.core.dominio.mapeadores.discovery.IndicePromocionShallowMappers
import com.example.myapplication.core.dominio.ubicacion.CalculadoraGeografica
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

/**
 * --- REPOSITORIO DE ESCRITURA: ÍNDICE DE PROMOCIONES (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Publicar ofertas e historias en Firebase.
 * [LEY #17]: Protocolo de Bautizo. Actúa como el Escritor del índice.
 */
@Singleton
class IndicePromocionPrestadorRepositorio @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val generadorTopicos: GeneradorTópicosFCM,
    private val categoriaDao: com.example.myapplication.core.datos.local.dao.CategoriaDao
) {
    companion object {
        const val COLECCION_PROMOCIONES = "indice_promociones"
    }

    /**
     * Publica un sobre de promoción con etiquetas de descubrimiento por zona y proximidad.
     * [LEY #17]: Protocolo de Bautizo. Genera tags por Categoría y SuperCategoría.
     */
    suspend fun publicarPromocion(sobre: IndicePromocionShallowDominio, codigoPostal: String) {
        val etiquetasFinales = mutableListOf<String>()
        
        // 🔥 [SUPREME.FIX]: Usar los rubros específicos de la oferta para mayor precisión
        val rubrosAEtiquetar = sobre.idCategorias.ifEmpty { sobre.emisor.idCategorias }

        android.util.Log.d("INDICE_PROMO_DEBUG", "🏷️ [PUBLISH] Generando tags para CP: $codigoPostal | Rubros: $rubrosAEtiquetar")

        // 0. Etiquetas de Zona Masiva - 🔥 [ELITE FIX]: Para descubrimiento global en la zona
        etiquetasFinales.add(generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.ZONA, codigoPostal))
        etiquetasFinales.add(generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.OFERTA, codigoPostal)) // H_CP (Global Offers)

        // 1. Generar etiquetas por cada rubro vinculado
        rubrosAEtiquetar.forEach { rubroId ->
            // A. Etiqueta de Zona (CP + Categoria): H_4000_plomeria
            val tagCat = generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.OFERTA, codigoPostal, rubroId)
            etiquetasFinales.add(tagCat)
            
            // B. Etiqueta de Afinidad (CP + SuperCategoria): H_4000_hogar
            val categoria = categoriaDao.obtenerPorId(rubroId)
            categoria?.idSuperCategoria?.let { superId ->
                val tagSuper = generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.OFERTA, codigoPostal, superId)
                etiquetasFinales.add(tagSuper)
            }
            
            // C. Etiqueta de Proximidad (Geohash 5): G_hash_plomeria
            if (sobre.emisor.latitud != 0.0) {
                val hash5 = CalculadoraGeografica.generarGeohash(sobre.emisor.latitud, sobre.emisor.longitud, 5)
                etiquetasFinales.add("${ProtocoloPrefijos.PROXIMIDAD}_${hash5}_$rubroId")
            }
        }

        val sobreConTags = sobre.copy(filtrosBusqueda = etiquetasFinales.distinct())
        
        android.util.Log.d("INDICE_PROMO_DEBUG", "📡 [FIRESTORE] Subiendo con tags finales: ${sobreConTags.filtrosBusqueda}")

        val mapa = IndicePromocionShallowMappers.deDominioAMapa(sobreConTags)
        
        firestore.collection(COLECCION_PROMOCIONES).document(sobre.idPromocion).set(mapa).await()
        Log.d("INDICE_PROMO", "🔥 Promoción publicada exitosamente en el índice: ${sobre.idPromocion}")
    }

    /**
     * Elimina una promoción o historia del feed.
     */
    suspend fun eliminarPromocion(idPromocion: String) {
        firestore.collection(COLECCION_PROMOCIONES).document(idPromocion).delete().await()
        Log.d("INDICE_PROMO", "🗑️ Promoción eliminada del índice: $idPromocion")
    }
}

