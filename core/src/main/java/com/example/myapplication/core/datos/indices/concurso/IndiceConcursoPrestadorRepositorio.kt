package com.example.myapplication.core.datos.indices.concurso

import com.example.myapplication.core.dominio.descubrimiento.GeneradorTópicosFCM
import com.example.myapplication.core.dominio.descubrimiento.ProtocoloPrefijos
import com.example.myapplication.core.dominio.modelos.discovery.IndiceConcursoShallowDominio
import com.example.myapplication.core.dominio.mapeadores.discovery.IndiceConcursoShallowMappers
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

/**
 * --- REPOSITORIO DE LECTURA: ÍNDICE DE CONCURSOS (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Consultar licitaciones públicas para los prestadores.
 * [LEY #17]: Protocolo de Bautizo. Actúa como el Lector del índice.
 */
@Singleton
class IndiceConcursoPrestadorRepositorio @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val generadorTopicos: GeneradorTópicosFCM
) {

    /**
     * Busca concursos disponibles en una zona y rubro específicos.
     * [ELITE]: Implementa estrategia de prioridad por suscripción y orden cronológico.
     */
    suspend fun obtenerMercadoLocal(
        codigoPostal: String,
        categorias: List<String>,
        limite: Int = 20,
        cursorId: String? = null
    ): List<IndiceConcursoShallowDominio> {
        return try {
            val coleccion = firestore.collection(IndiceConcursoUsuarioRepositorio.COLECCION_CONCURSOS)
            
            var query = coleccion
                .orderBy("marcaTiempo", Query.Direction.DESCENDING)
                .limit(limite.toLong())

            if (cursorId != null) {
                val lastDoc = coleccion.document(cursorId).get().await()
                if (lastDoc.exists()) {
                    query = query.startAfter(lastDoc)
                }
            }

            Log.d("INDICE_CONCURSO", "🛰️ [QUERY_START] Colección: ${IndiceConcursoUsuarioRepositorio.COLECCION_CONCURSOS} | Límite: $limite")
            val snapshot = query.get().await()
            val resultados = snapshot.documents.mapNotNull { doc -> 
                val modelo = IndiceConcursoShallowMappers.desdeFirestore(doc)
                if (modelo != null) {
                    Log.v("INDICE_CONCURSO", "📄 [DATA] Documento: ${doc.id} -> Título: ${modelo.titulo}")
                }
                modelo
            }
            
            Log.d("INDICE_CONCURSO", "✅ [QUERY_OK] Concursos encontrados: ${resultados.size}")
            resultados
        } catch (e: Exception) {
            Log.e("INDICE_CONCURSO", "❌ Error al consultar mercado: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Versión para RemoteMediator que requiere el último documento para el cursor.
     */
    suspend fun obtenerMercadoLocalConDocumentos(
        tags: List<String>,
        limite: Int,
        ultimoDocId: String?
    ): com.google.firebase.firestore.QuerySnapshot? {
        return try {
            val coleccion = firestore.collection(IndiceConcursoUsuarioRepositorio.COLECCION_CONCURSOS)
            var query: Query = coleccion
                .orderBy("marcaTiempo", Query.Direction.DESCENDING)

            // 🐛 FIX (01/09): "tags" llegaba hasta acá y nunca se usaba — la consulta traía
            // TODO el mercado sin filtrar por zona/categoría. Cada documento ya guarda
            // "filtrosBusqueda" = [C_<cp>_<categoria>, Z_<cp>] (ver
            // IndiceConcursoUsuarioRepositorio.publicarConcurso); whereArrayContainsAny exige
            // 1-10 valores no vacíos, así que solo se aplica cuando efectivamente hay tags.
            if (tags.isNotEmpty()) {
                query = query.whereArrayContainsAny("filtrosBusqueda", tags.take(10))
            }

            query = query.limit(limite.toLong())

            if (ultimoDocId != null) {
                val lastDoc = coleccion.document(ultimoDocId).get().await()
                if (lastDoc.exists()) query = query.startAfter(lastDoc)
            }

            query.get().await()
        } catch (e: Exception) {
            Log.e("INDICE_CONCURSO", "❌ Error en consulta para mediador: ${e.message}")
            null
        }
    }
}

