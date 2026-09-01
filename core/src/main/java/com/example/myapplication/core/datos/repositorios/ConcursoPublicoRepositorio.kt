package com.example.myapplication.core.datos.repositorios

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.myapplication.core.datos.local.AppDatabase
import com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity
import com.example.myapplication.core.datos.local.entidades.relaciones.ConcursoPublicoConPresupuestos
import com.example.myapplication.core.dominio.filtros.FiltrosConcursoPublico
import com.example.myapplication.core.datos.indices.concurso.IndiceConcursoPrestadorRepositorio
import com.example.myapplication.core.dominio.descubrimiento.GeneradorTópicosFCM
import com.example.myapplication.core.dominio.descubrimiento.ProtocoloPrefijos
import com.example.myapplication.core.dominio.ubicacion.NormalizadorDirecciones
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

/**
 * --- REPOSITORIO DE CONCURSOS / LICITACIONES (Atómico - v2026.ELITE) ---
 */
@Singleton
class ConcursoPublicoRepositorio @Inject constructor(
    private val baseDeDatos: AppDatabase,
    private val firestore: FirebaseFirestore,
    private val almacenamiento: FirebaseStorage,
    private val indiceRepo: IndiceConcursoPrestadorRepositorio,
    private val generadorTopicos: GeneradorTópicosFCM
) {
    private val concursoDao = baseDeDatos.concursoPublicoDao()

    val todosLosConcursos: Flow<List<ConcursoPublicoEntity>> = concursoDao.obtenerTodos()

    fun buscarConcursosPropios(idCliente: String, filtros: FiltrosConcursoPublico): Flow<List<ConcursoPublicoEntity>> {
        val consultaFts = if (filtros.consulta.isNotBlank()) "${filtros.consulta}*" else ""
        
        return concursoDao.buscarPropiosFts(
            idCliente = idCliente,
            consulta = consultaFts,
            soloActivos = filtros.soloActivos,
            soloCerrados = filtros.soloCerrados,
            soloAdjudicados = filtros.soloAdjudicados,
            soloNoLeidos = filtros.soloNoLeidos,
            idCategoria = filtros.idsCategorias.firstOrNull(),
            orden = filtros.orden
        )
    }

    fun buscarConcursosPropiosResumen(idCliente: String, filtros: FiltrosConcursoPublico): Flow<List<com.example.myapplication.core.datos.local.entidades.vistas.ConcursoPublicoResumenSQLView>> {
        val consultaFts = if (filtros.consulta.isNotBlank()) "${filtros.consulta}*" else ""
        
        return concursoDao.buscarPropiosResumenFts(
            idCliente = idCliente,
            consulta = consultaFts,
            soloActivos = filtros.soloActivos,
            soloCerrados = filtros.soloCerrados,
            soloAdjudicados = filtros.soloAdjudicados,
            soloNoLeidos = filtros.soloNoLeidos,
            idCategoria = filtros.idsCategorias.firstOrNull(),
            orden = filtros.orden
        )
    }

    fun obtenerRubrosEnUso(idCliente: String): Flow<List<String>> = concursoDao.obtenerRubrosEnUso(idCliente)

    fun obtenerConcursoConPresupuestos(id: String): Flow<ConcursoPublicoConPresupuestos?> = 
        concursoDao.obtenerConcursoConPresupuestos(id)

    suspend fun obtenerConcursoPorId(id: String): ConcursoPublicoEntity? = concursoDao.obtenerPorIdSync(id)

    @OptIn(ExperimentalPagingApi::class)
    fun obtenerMercadoPaginado(cp: String, categorias: List<String>): Flow<PagingData<ConcursoPublicoEntity>> {
        val idConsulta = "MERCADO_GLOBAL"

        return Pager(
            config = PagingConfig(pageSize = 15, enablePlaceholders = false),
            remoteMediator = ConcursoRemoteMediator(
                idConsulta = idConsulta, 
                tagsConsulta = emptyList(), 
                baseDeDatos = baseDeDatos, 
                indiceRepo = indiceRepo
            )
        ) {
            baseDeDatos.concursoPublicoDao().obtenerMercadoPaginado()
        }.flow
    }

    suspend fun guardarConcursoLocalConMultimedia(concurso: ConcursoPublicoEntity): ConcursoPublicoEntity = coroutineScope {
        try {
            val cpNormalizado = NormalizadorDirecciones.limpiarCodigoPostal(concurso.direccionCodigoPostal ?: "")
            
            val deferredUrls = concurso.urlImagenes.map { cadenaUri ->
                async {
                    if (cadenaUri.startsWith("http")) cadenaUri 
                    else subirImagenConcurso(concurso.idConcurso, Uri.parse(cadenaUri))
                }
            }
            
            val urlsPublicas = deferredUrls.awaitAll()

            val concursoNormalizado = concurso.copy(
                direccionCodigoPostal = cpNormalizado,
                urlImagenes = urlsPublicas
            )
            
            concursoDao.insertar(concursoNormalizado)
            concursoNormalizado
                
        } catch (e: Exception) {
            Log.e("ConcursoRepo", "❌ [CONCURSO_ERR] Error al guardar localmente: ${e.message}")
            throw e
        }
    }

    private suspend fun subirImagenConcurso(idConcurso: String, uri: Uri): String {
        val nombreArchivo = "img_${System.currentTimeMillis()}.webp"
        val referencia = almacenamiento.reference.child("concursos/$idConcurso/$nombreArchivo")
        referencia.putFile(uri).await()
        return referencia.downloadUrl.await().toString()
    }

    suspend fun eliminarConcursoLocal(id: String) {
        concursoDao.eliminarPorId(id)
    }
}
