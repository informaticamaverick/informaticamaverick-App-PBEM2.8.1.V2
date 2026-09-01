/*
package com.example.myapplication.core.datos.repositorios

import com.example.myapplication.core.dominio.modelos.discovery.IndiceConcursoShallowDominio
import com.example.myapplication.core.dominio.modelos.discovery.IndicePromocionShallowDominio
import com.example.myapplication.core.dominio.modelos.shallow.PrestadorShallowDominio
import com.example.myapplication.core.dominio.modelos.shallow.SucursalShallowDominio
import com.example.myapplication.core.dominio.mapeadores.discovery.IndiceConcursoShallowMappers
import com.example.myapplication.core.dominio.mapeadores.discovery.IndicePromocionShallowMappers
import com.example.myapplication.core.dominio.mapeadores.shallow.PrestadorShallowMappers
import com.example.myapplication.core.dominio.mapeadores.shallow.SucursalShallowMappers
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE ÍNDICE MAESTRO (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Única puerta de salida a las colecciones 'indice_' de Firebase.
 * [LEY #17]: Protocolo de Bautizo.
 * [ESTADO]: OBSOLETO. Se han creado repositorios específicos por dominio en la carpeta 'indices'.
 */
@Singleton
class RepositorioIndiceMaestroMav @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        const val COL_BUSQUEDA = "indice_busqueda"
        const val COL_CONCURSOS = "indice_concursos"
        const val COL_PROMOCIONES = "indice_promociones"
    }

    suspend fun publicarEnIndiceBusqueda(shallow: PrestadorShallowDominio) {
        val mapa = PrestadorShallowMappers.deDominioAMapa(shallow)
        firestore.collection(COL_BUSQUEDA).document(shallow.id).set(mapa).await()
    }

    suspend fun publicarSucursalEnIndiceBusqueda(shallow: SucursalShallowDominio) {
        val mapa = SucursalShallowMappers.deDominioAMapa(shallow)
        firestore.collection(COL_BUSQUEDA).document(shallow.id).set(mapa).await()
    }

    suspend fun publicarEnIndiceConcursos(sobre: IndiceConcursoShallowDominio) {
        val mapa = IndiceConcursoShallowMappers.deDominioAMapa(sobre)
        firestore.collection(COL_CONCURSOS).document(sobre.idConcurso).set(mapa).await()
    }

    suspend fun publicarEnIndicePromociones(sobre: IndicePromocionShallowDominio) {
        val mapa = IndicePromocionShallowMappers.deDominioAMapa(sobre)
        firestore.collection(COL_PROMOCIONES).document(sobre.idPromocion).set(mapa).await()
    }

    suspend fun eliminarDelIndice(coleccion: String, id: String) {
        firestore.collection(coleccion).document(id).delete().await()
    }
}
*/

