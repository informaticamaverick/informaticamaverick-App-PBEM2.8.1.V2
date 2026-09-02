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
            soloAdjudicados = filtros.soloAdjudicados,
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
            soloAdjudicados = filtros.soloAdjudicados,
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
        // 🐛 FIX (01/09): "cp" y "categorias" llegaban hasta acá y se ignoraban por completo —
        // se mandaba tagsConsulta = emptyList(), así que el mediador y la query de Firestore
        // (ver IndiceConcursoPrestadorRepositorio) traían TODOS los concursos del mercado sin
        // filtrar, de cualquier categoría y zona. La infraestructura de etiquetado ya existe y
        // funciona del lado de quien publica (IndiceConcursoUsuarioRepositorio.publicarConcurso
        // guarda "filtrosBusqueda" = [C_<cp>_<categoria>, Z_<cp>] en cada documento) — acá solo
        // había que armar las mismas etiquetas para el prestador y usarlas para filtrar.
        val tagsCategoriaZona = categorias
            .map { cat -> generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.CONCURSO, cp, cat) }
            .filter { it.isNotBlank() }
            .distinct()

        val idConsulta = "MERCADO_" + tagsCategoriaZona.sorted().joinToString("_").ifBlank { "GLOBAL" }

        return Pager(
            config = PagingConfig(pageSize = 15, enablePlaceholders = false),
            remoteMediator = ConcursoRemoteMediator(
                idConsulta = idConsulta,
                tagsConsulta = tagsCategoriaZona,
                baseDeDatos = baseDeDatos,
                indiceRepo = indiceRepo
            )
        ) {
            baseDeDatos.concursoPublicoDao().obtenerMercadoPaginado(cp, categorias)
        }.flow
    }

    suspend fun guardarConcursoLocalConMultimedia(concurso: ConcursoPublicoEntity): ConcursoPublicoEntity = coroutineScope {
        try {
            val cpNormalizado = NormalizadorDirecciones.limpiarCodigoPostal(concurso.direccionCodigoPostal ?: "")

            // 🐛 FIX (01/09): Storage no está disponible en este proyecto (decisión del dueño,
            // no un problema de config) — cualquier concurso con una foto adjunta fallaba la
            // publicación COMPLETA (título, descripción, categoría, todo) solo porque una sola
            // imagen no podía subirse. Ahora cada imagen se sube "a lo mejor esfuerzo": si falla,
            // se descarta esa imagen puntual y el concurso se publica igual sin ella, en vez de
            // abortar todo. El día que Storage esté disponible, esto vuelve a subir fotos solo.
            val deferredUrls = concurso.urlImagenes.map { cadenaUri ->
                async {
                    if (cadenaUri.startsWith("http")) cadenaUri
                    else try {
                        subirImagenConcurso(concurso.idConcurso, Uri.parse(cadenaUri))
                    } catch (e: Exception) {
                        Log.e("ConcursoRepo", "⚠️ [IMG_SKIP] No se pudo subir una imagen (Storage no disponible) — se publica el concurso sin ella: ${e.message}")
                        null
                    }
                }
            }

            val urlsPublicas = deferredUrls.awaitAll().filterNotNull()

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
