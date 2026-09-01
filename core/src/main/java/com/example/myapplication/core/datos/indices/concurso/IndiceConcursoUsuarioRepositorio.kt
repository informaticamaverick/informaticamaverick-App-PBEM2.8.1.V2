package com.example.myapplication.core.datos.indices.concurso

import com.example.myapplication.core.dominio.descubrimiento.GeneradorTópicosFCM
import com.example.myapplication.core.dominio.descubrimiento.ProtocoloPrefijos
import com.example.myapplication.core.dominio.modelos.discovery.IndiceConcursoShallowDominio
import com.example.myapplication.core.dominio.mapeadores.discovery.IndiceConcursoShallowMappers
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

/**
 * --- REPOSITORIO DE ESCRITURA: ÍNDICE DE CONCURSOS (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Publicar licitaciones públicas (Topiks) en Firebase.
 * [LEY #17]: Protocolo de Bautizo. Actúa como el Escritor del índice.
 */
@Singleton
class IndiceConcursoUsuarioRepositorio @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val generadorTopicos: GeneradorTópicosFCM
) {
    companion object {
        const val COLECCION_CONCURSOS = "indice_concursos"
    }

    /**
     * Publica un sobre de concurso con etiquetas de descubrimiento por zona y rubro.
     */
    suspend fun publicarConcurso(sobre: IndiceConcursoShallowDominio) {
        val uidAuth = firestore.app.get(com.google.firebase.auth.FirebaseAuth::class.java).currentUser?.uid
        Log.d("INDICE_CONCURSO", "🕵️ [INICIO_PUBLISH] UID Auth: $uidAuth | ID Concurso: ${sobre.idConcurso}")

        // 1. Generar etiquetas de descubrimiento: C_CP_Rubro
        val huellaPrincipal = generadorTopicos.generarTópicoMaestro(
            ProtocoloPrefijos.CONCURSO, 
            sobre.codigoPostal, 
            sobre.idCategoria
        )

        // 🔥 [ELITE]: Huella de Zona (Ley #9). Permite el descubrimiento masivo por CP.
        val huellaZona = generadorTopicos.generarTópicoMaestro(
            ProtocoloPrefijos.ZONA, 
            sobre.codigoPostal
        )
        
        Log.d("INDICE_CONCURSO", "🏷️ [TAG_GEN] C_Tag: $huellaPrincipal | Z_Tag: $huellaZona")

        // [ELITE]: Consolidamos etiquetas de Categoría y Zona.
        val tagsFinales = listOf(huellaPrincipal, huellaZona).filter { it.isNotBlank() }.distinct()
        
        val sobreConTags = sobre.copy(filtrosBusqueda = tagsFinales)
        val mapa = IndiceConcursoShallowMappers.deDominioAMapa(sobreConTags)
        
        Log.d("INDICE_CONCURSO", "📦 [DATA_MAP] Preparando envío a Firestore. Colección: $COLECCION_CONCURSOS | Mapa: $mapa")

        try {
            firestore.collection(COLECCION_CONCURSOS).document(sobre.idConcurso).set(mapa).await()
            Log.d("INDICE_CONCURSO", "✅ [FIRESTORE_OK] Concurso publicado exitosamente: ${sobre.idConcurso}")
        } catch (e: Exception) {
            Log.e("INDICE_CONCURSO", "❌ [FIRESTORE_ERR] Error al escribir en '$COLECCION_CONCURSOS': ${e.message}", e)
            throw e
        }
    }

    /**
     * Elimina una licitación del mercado.
     */
    suspend fun eliminarConcurso(idConcurso: String) {
        firestore.collection(COLECCION_CONCURSOS).document(idConcurso).delete().await()
        Log.d("INDICE_CONCURSO", "🗑️ Concurso eliminado del índice: $idConcurso")
    }
}

