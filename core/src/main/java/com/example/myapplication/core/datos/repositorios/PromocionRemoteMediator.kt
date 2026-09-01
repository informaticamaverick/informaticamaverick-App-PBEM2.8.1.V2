package com.example.myapplication.core.datos.repositorios

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.myapplication.core.datos.local.AppDatabase
import com.example.myapplication.core.datos.local.entidades.ClaveRemotaBusquedaEntity
import com.example.myapplication.core.datos.local.entidades.PromocionEntity
import com.example.myapplication.core.datos.local.entidades.toEntity
import com.example.myapplication.core.dominio.modelos.Promocion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

import com.example.myapplication.core.dominio.mapeadores.discovery.IndicePromocionShallowMappers

/**
 * --- MEDIADOR REMOTO DE PROMOCIONES (OFERTAS ELITE) ---
 * [PROPÓSITO]: Sincronizar ofertas de Firestore con Room usando Paging 3.
 */
@OptIn(ExperimentalPagingApi::class)
class PromocionRemoteMediator(
    private val idConsulta: String,
    private val tagsConsulta: List<String>,
    private val baseDeDatos: AppDatabase,
    private val firestore: FirebaseFirestore
) : RemoteMediator<Int, PromocionEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PromocionEntity>
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

            android.util.Log.d("PROMO_FIRESTORE", "🔥 Query con tags: $tagsConsulta")

            // Validación de seguridad para evitar crash de Firestore
            if (tagsConsulta.isEmpty()) {
                android.util.Log.w("PromocionMediator", "⚠️ [EMPTY_TAGS] Cancelando consulta remota por falta de tags.")
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            val queryResult = try {
                var query = firestore.collection("indice_promociones")
                    .whereArrayContainsAny("filtrosBusqueda", tagsConsulta)
                    .orderBy("estaSuscrito", Query.Direction.DESCENDING)
                    .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                    .limit(state.config.pageSize.toLong())

                if (cursorId != null) {
                    val lastDoc = firestore.collection("indice_promociones").document(cursorId).get().await()
                    if (lastDoc.exists()) query = query.startAfter(lastDoc)
                }
                query.get().await()
            } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
                if (e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                    android.util.Log.w("PromocionMediator", "⚠️ [INDEX_MISSING] Reintentando fallback paginado sin índice...")
                    
                    // 🔥 [CORRECCIÓN CRÍTICA]: El fallback también DEBE usar el cursor/startAfter
                    // de lo contrario entra en un bucle infinito pidiendo siempre la página 1.
                    var fallbackQuery = firestore.collection("indice_promociones")
                        .whereArrayContainsAny("filtrosBusqueda", tagsConsulta)
                        .limit(state.config.pageSize.toLong())

                    if (cursorId != null) {
                        val lastDoc = firestore.collection("indice_promociones").document(cursorId).get().await()
                        if (lastDoc.exists()) fallbackQuery = fallbackQuery.startAfter(lastDoc)
                    }
                    
                    fallbackQuery.get().await()
                } else throw e
            }

            val promocionesRemotas = queryResult.documents.mapNotNull { doc ->
                IndicePromocionShallowMappers.desdeFirestore(doc)?.let {
                    IndicePromocionShallowMappers.deShallowAEntidad(it)
                }
            }
            val finalAlcanzado = promocionesRemotas.isEmpty()

            baseDeDatos.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    baseDeDatos.claveRemotaBusquedaDao().eliminarClave(idConsulta)
                }

                val ultimoId = queryResult.documents.lastOrNull()?.id
                baseDeDatos.claveRemotaBusquedaDao().guardarClave(
                    ClaveRemotaBusquedaEntity(idConsulta, ultimoId)
                )

                android.util.Log.d("PROMO_SYNC_DEBUG", "💾 [ROOM_SAVE] Insertando ${promocionesRemotas.size} promociones en Room para consulta: $idConsulta")
                promocionesRemotas.forEach { p ->
                    android.util.Log.v("PROMO_SYNC_DEBUG", "   📄 [DATA] ID: ${p.id} | Titulo: ${p.titulo} | Tipo: ${p.tipo}")
                }

                baseDeDatos.promotionDao().insertarListaPromociones(promocionesRemotas)
                android.util.Log.d("PROMO_SYNC_DEBUG", "✅ [ROOM_OK] Persistencia completada.")
            }

            MediatorResult.Success(endOfPaginationReached = finalAlcanzado)
        } catch (e: Exception) {
            android.util.Log.e("PromocionMediator", "❌ [SYNC_ERROR] Fallo crítico: ${e.message}")
            MediatorResult.Error(e)
        }
    }
}

