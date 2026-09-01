package com.example.myapplication.core.datos.indices.busqueda

import com.example.myapplication.core.dominio.descubrimiento.GeneradorTópicosFCM
import com.example.myapplication.core.dominio.descubrimiento.ProtocoloPrefijos
import com.example.myapplication.core.dominio.mapeadores.descubrimiento.ResultadoIndiceBusquedaMappers
import com.example.myapplication.core.dominio.modelos.descubrimiento.ResultadoIndiceBusquedaShallowDominio
import com.example.myapplication.core.dominio.ubicacion.CalculadoraGeografica
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE LECTURA: ÍNDICE DE BÚSQUEDA (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Consultar el índice de expertos con estrategia de cascada.
 * [LEY #17]: Protocolo de Bautizo. Actúa como el Lector del índice.
 */
@Singleton
class IndiceBusquedaUsuarioRepositorio @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val generadorTopicos: GeneradorTópicosFCM
) {

    /**
     * Búsqueda por Proximidad (Geohash 9 Vecinos).
     * [RADIO]: ~5km (Geohash precisión 5).
     */
    suspend fun buscarPorProximidad(
        latitud: Double,
        longitud: Double,
        rubro: String,
        limite: Int = 20,
        cursorId: String? = null
    ): List<ResultadoIndiceBusquedaShallowDominio> {
        val hashRaiz = CalculadoraGeografica.generarGeohash(latitud, longitud, 5)
        val rejilla9Vecinos = CalculadoraGeografica.obtener9Vecinos(hashRaiz)
        
        // Creamos las etiquetas de búsqueda: PROXIMIDAD_hash_rubro
        val tagsBusqueda = rejilla9Vecinos.map { "${ProtocoloPrefijos.PROXIMIDAD}_${it}_$rubro" }
        android.util.Log.d("INDICE_RED", "📍 [PROXIMIDAD] Buscando en rejilla de 9 vecinos para '$rubro'. Tags: $tagsBusqueda")
        
        return ejecutarConsultaDeIndice(tagsBusqueda, limite, cursorId)
    }

    /**
     * Búsqueda por Zona (Código Postal).
     * [ALCANCE]: Toda la localidad vinculada al CP.
     */
    suspend fun buscarPorZona(
        codigoPostal: String,
        rubro: String,
        limite: Int = 20,
        cursorId: String? = null
    ): List<ResultadoIndiceBusquedaShallowDominio> {
        val tagZona = generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.PRESTADOR, codigoPostal, rubro)
        android.util.Log.d("INDICE_RED", "🏙️ [ZONA] Buscando en Código Postal '$codigoPostal' para '$rubro'. Tag: $tagZona")
        return ejecutarConsultaDeIndice(listOf(tagZona), limite, cursorId)
    }

    private suspend fun ejecutarConsultaDeIndice(
        tags: List<String>,
        limite: Int,
        cursorId: String?
    ): List<ResultadoIndiceBusquedaShallowDominio> {
        return try {
            android.util.Log.d("INDICE_RED", "🛰️ [FIRESTORE] Ejecutando query en 'indice_busqueda'. Límite: $limite | Cursor: $cursorId")
            var query = firestore.collection(IndiceBusquedaPrestadorRepositorio.COLECCION_INDICE)
                .whereArrayContainsAny("filtrosBusqueda", tags)
                .orderBy("reputacion", Query.Direction.DESCENDING)
                .limit(limite.toLong())

            if (cursorId != null) {
                val lastDoc = firestore.collection(IndiceBusquedaPrestadorRepositorio.COLECCION_INDICE).document(cursorId).get().await()
                query = query.startAfter(lastDoc)
            }

            val snapshot = query.get().await()
            val resultados = snapshot.documents.mapNotNull { doc -> ResultadoIndiceBusquedaMappers.desdeFirestore(doc) }
            android.util.Log.d("INDICE_RED", "✅ [FIRESTORE_OK] Query finalizada. Documentos recibidos: ${resultados.size}")
            resultados
        } catch (e: Exception) {
            android.util.Log.e("INDICE_RED", "❌ [FIRESTORE_ERR] Error en consulta: ${e.message}", e)
            emptyList()
        }
    }
}

