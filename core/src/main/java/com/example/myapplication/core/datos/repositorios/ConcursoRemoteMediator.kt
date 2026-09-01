package com.example.myapplication.core.datos.repositorios

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.myapplication.core.datos.local.AppDatabase
import com.example.myapplication.core.datos.local.entidades.ClaveRemotaBusquedaEntity
import com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity
import com.example.myapplication.core.datos.indices.concurso.IndiceConcursoPrestadorRepositorio
import com.example.myapplication.core.datos.indices.concurso.IndiceConcursoUsuarioRepositorio
import com.example.myapplication.core.dominio.mapeadores.discovery.IndiceConcursoShallowMappers
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * --- MEDIADOR REMOTO DE CONCURSOS (MERCADO TOPIK) ---
 */
@OptIn(ExperimentalPagingApi::class)
class ConcursoRemoteMediator(
    private val idConsulta: String,
    private val tagsConsulta: List<String>,
    private val baseDeDatos: AppDatabase,
    private val indiceRepo: IndiceConcursoPrestadorRepositorio
) : RemoteMediator<Int, ConcursoPublicoEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ConcursoPublicoEntity>
    ): MediatorResult {
        return try {
            val cursorId = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val clave = baseDeDatos.claveRemotaBusquedaDao().obtenerClave(idConsulta)
                    if (clave?.ultimoDocumentoId == null) return MediatorResult.Success(endOfPaginationReached = true)
                    clave.ultimoDocumentoId
                }
            }

            android.util.Log.d("CONCURSO_FIRESTORE", "🔥 [MEDIATOR] Query con tags: $tagsConsulta | Cursor: $cursorId")

            val snapshot = indiceRepo.obtenerMercadoLocalConDocumentos(
                tags = tagsConsulta,
                limite = state.config.pageSize,
                ultimoDocId = cursorId
            )

            if (snapshot == null) return MediatorResult.Error(Exception("Error al obtener snapshot del índice"))

            val concursos = snapshot.documents.mapNotNull { doc ->
                IndiceConcursoShallowMappers.desdeFirestore(doc)?.let { 
                    IndiceConcursoShallowMappers.deShallowAEntidad(it)
                }
            }
            
            android.util.Log.d("CONCURSO_MEDIATOR_DEBUG", "📥 [RECIBIDOS] Cantidad de concursos desde la nube: ${concursos.size}")
            
            val finalAlcanzado = concursos.isEmpty()

            baseDeDatos.withTransaction<Unit> {
                if (loadType == LoadType.REFRESH) {
                    android.util.Log.d("CONCURSO_MEDIATOR_DEBUG", "🧹 [REFRESH] Limpiando claves antiguas para: $idConsulta")
                    baseDeDatos.claveRemotaBusquedaDao().eliminarClave(idConsulta)
                }

                val ultimoId = snapshot.documents.lastOrNull()?.id
                baseDeDatos.claveRemotaBusquedaDao().guardarClave(
                    ClaveRemotaBusquedaEntity(idConsulta, ultimoId)
                )

                android.util.Log.d("CONCURSO_MEDIATOR_DEBUG", "💾 [ROOM_SAVE] Insertando ${concursos.size} concursos en Room...")
                concursos.forEach { c ->
                    android.util.Log.v("CONCURSO_MEDIATOR_DEBUG", "   📄 [DATA] ID: ${c.idConcurso} | Titulo: ${c.titulo} | CP: ${c.direccionCodigoPostal} | Cat: ${c.idCategoria}")
                }
                
                baseDeDatos.concursoPublicoDao().insertarLista(concursos)
                android.util.Log.d("CONCURSO_MEDIATOR_DEBUG", "✅ [ROOM_OK] Persistencia completada.")
            }

            MediatorResult.Success(endOfPaginationReached = finalAlcanzado)
        } catch (e: Exception) {
            android.util.Log.e("ConcursoMediator", "❌ [SYNC_ERROR] Error en mercado (Check index link)", e)
            MediatorResult.Error(e)
        }
    }
}


































