package com.example.myapplication.core.datos.repositorios

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.myapplication.core.datos.local.AppDatabase
import com.example.myapplication.core.datos.local.dao.PromocionDao
import com.example.myapplication.core.datos.local.dao.CategoriaDao
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.datos.local.dao.TelemetryDao
import com.example.myapplication.core.dominio.descubrimiento.GeneradorTópicosFCM
import com.example.myapplication.core.dominio.descubrimiento.ProtocoloPrefijos
import com.example.myapplication.core.dominio.ubicacion.NormalizadorDirecciones
import com.example.myapplication.core.dominio.modelos.Promocion
import com.example.myapplication.core.dominio.modelos.PromocionComentario
import com.example.myapplication.core.dominio.mapeadores.discovery.IndicePromocionShallowMappers
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE PROMOCIONES (SSOT Core v2026.ELITE) ---
 */
@Singleton
class PromocionRepositorio @Inject constructor(
    private val baseDeDatos: AppDatabase,
    private val categoryDao: CategoriaDao,
    private val telemetryDao: TelemetryDao,
    private val generadorTopicos: GeneradorTópicosFCM,
    private val suscripcionDao: com.example.myapplication.core.datos.local.dao.SuscripcionTopicDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val promocionDao = baseDeDatos.promotionDao()

    @OptIn(ExperimentalPagingApi::class)
    fun obtenerPromocionesPaginadas(cp: String, superCategoria: String? = null, categoria: String? = null): Flow<PagingData<PromocionEntity>> {
        val cpStd = NormalizadorDirecciones.limpiarCodigoPostal(cp)
        val idConsulta = generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.OFERTA, cpStd, categoria ?: superCategoria)
        
        val tagsConsulta = mutableListOf<String>()
        if (categoria != null) tagsConsulta.add(generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.OFERTA, cpStd, categoria))
        if (superCategoria != null) tagsConsulta.add(generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.OFERTA, cpStd, superCategoria))

        if (tagsConsulta.isEmpty()) tagsConsulta.add(generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.OFERTA, cpStd))

        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            remoteMediator = PromocionRemoteMediator(idConsulta, tagsConsulta.distinct(), baseDeDatos, firestore)
        ) {
            promocionDao.obtenerActivasPaginadas(System.currentTimeMillis(), idConsulta)
        }.flow
    }

    fun obtenerPromocionesActivas(zipCode: String?): Flow<List<Promocion>> =
        if (zipCode == null) {
            promocionDao.obtenerActivas(System.currentTimeMillis()).map { list -> list.map { it.aModelo() } }
        } else {
            // Filtrar por tópicos suscritos en Room (ELITE v2026)
            kotlinx.coroutines.flow.combine(
                promocionDao.obtenerActivas(System.currentTimeMillis()),
                suscripcionDao.obtenerSuscripcionesActivas()
            ) { promos, suscripciones ->
                val topics = suscripciones.map { it.topic }.toSet()
                val gson = com.google.gson.Gson()
                val typeList = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
                promos.filter { p -> 
                    val filtros: List<String> = try { gson.fromJson(p.filtrosBusquedaJson, typeList) } catch (_: Exception) { emptyList() }
                    filtros.any { it in topics }
                }.map { it.aModelo() }
            }
        }

    fun obtenerHistoriasActivas(zipCode: String?): Flow<List<Promocion>> =
        if (zipCode == null) {
            promocionDao.obtenerHistoriasActivas(System.currentTimeMillis()).map { list -> list.map { it.aModelo() } }
        } else {
            val cpNormalizado = NormalizadorDirecciones.limpiarCodigoPostal(zipCode)
            promocionDao.obtenerHistoriasActivasPorZona(System.currentTimeMillis(), cpNormalizado).map { list -> list.map { it.aModelo() } }
        }

    fun obtenerPromocionPorId(id: String): Flow<Promocion?> =
        kotlinx.coroutines.flow.flow { emit(promocionDao.obtenerPorId(id)?.aModelo()) }

    fun obtenerHistoriasCascada(cp: String, superCategoria: String? = null, categoria: String? = null): Flow<List<Promocion>> {
        val cpNormal = NormalizadorDirecciones.limpiarCodigoPostal(cp)
        val tagRubro = if (categoria != null) generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.OFERTA, cpNormal, categoria) else null
        val tagSuper = if (superCategoria != null) generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.OFERTA, cpNormal, superCategoria) else null
        val tagZona = generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.ZONA, cpNormal)

        return promocionDao.obtenerHistoriasActivas(System.currentTimeMillis()).map { todas ->
            if (tagRubro != null) {
                val xRubro = todas.filter { it.filtrosBusquedaJson.contains(tagRubro) }
                if (xRubro.isNotEmpty()) return@map xRubro.map { it.aModelo() }
            }
            if (tagSuper != null) {
                val xSuper = todas.filter { it.filtrosBusquedaJson.contains(tagSuper) }
                if (xSuper.isNotEmpty()) return@map xSuper.map { it.aModelo() }
            }
            val xZona = todas.filter { it.filtrosBusquedaJson.contains(tagZona) || it.codigoPostal == cpNormal }
            xZona.map { it.aModelo() }
        }
    }

    fun obtenerPromocionesPorPrestador(idPrestador: String): Flow<List<Promocion>> =
        promocionDao.obtenerPorPrestador(idPrestador).map { list -> 
            list.map { it.aModelo() }
        }

    suspend fun alternarLike(idPromocion: String) {
        val miId = auth.currentUser?.uid ?: return
        val current = promocionDao.esPromocionFavorita(idPromocion) ?: false
        val newStatus = !current
        promocionDao.insertarReaccion(PromocionLikeEntity(idPromocion, miId, newStatus))
        promocionDao.actualizarEstadoLike(idPromocion, if (newStatus) 1 else -1)
        telemetryDao.registrarEvento(TelemetryEntity(type = TelemetryType.LIKE, targetId = idPromocion))
    }

    fun obtenerComentarios(idPromocion: String): Flow<List<PromocionComentario>> =
        promocionDao.obtenerComentariosPorPromo(idPromocion).map { list -> 
            list.map { it.toDomain() }
        }

    suspend fun agregarComentario(idPromocion: String, comment: PromocionComentario) {
        try {
            val entity = PromocionComentarioEntity.fromDomain(idPromocion, comment)
            promocionDao.insertarComentarios(listOf(entity))
            firestore.collection("indice_promociones")
                .document(idPromocion)
                .collection("comentarios")
                .document(comment.id)
                .set(comment)
                .await()
        } catch (e: Exception) {
            android.util.Log.e("PromocionRepo", "Error: ${e.message}")
        }
    }

    suspend fun sincronizarPromocionesRemotas(zipCode: String?, categories: List<String> = emptyList()) {
        if (zipCode == null) return
        try {
            val cpLimpio = NormalizadorDirecciones.limpiarCodigoPostal(zipCode)
            val huellasConsulta = if (categories.isEmpty()) {
                listOf(generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.ZONA, cpLimpio))
            } else {
                categories.flatMap { cat ->
                    val category = categoryDao.obtenerPorId(cat)
                    val list = mutableListOf<String>()
                    list.add(generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.OFERTA, cpLimpio, cat))
                    category?.idSuperCategoria?.let { s ->
                        list.add(generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.OFERTA, cpLimpio, s))
                    }
                    list
                }
            }
            val tagsFinales = huellasConsulta.distinct().take(10)
            val snapshot = firestore.collection("indice_promociones")
                .whereArrayContainsAny("filtrosBusqueda", tagsFinales)
                .orderBy("fechaCreacion", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get().await()
            val remotePromos = snapshot.documents.mapNotNull { doc ->
                IndicePromocionShallowMappers.desdeFirestore(doc)
            }
            
            promocionDao.eliminarExpiradas(System.currentTimeMillis())
            
            promocionDao.insertarListaPromociones(remotePromos.map { 
                IndicePromocionShallowMappers.deShallowAEntidad(it)
            })
        } catch (e: Exception) {
            android.util.Log.e("PromocionRepo", "Sync Error: ${e.message}")
        }
    }

    suspend fun sincronizarTelemetriaPendiente() {
        val pendientes = telemetryDao.obtenerPendientes()
        if (pendientes.isEmpty()) return
        try {
            val ids = pendientes.map { it.id }
            telemetryDao.marcarComoSincronizados(ids)
        } catch (e: Exception) {
            android.util.Log.e("PromocionRepo", "Error sync telemetría: ${e.message}")
        }
    }
}
