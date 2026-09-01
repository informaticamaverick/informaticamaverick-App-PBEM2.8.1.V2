package com.example.myapplication.core.datos.repositorios

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.myapplication.core.datos.local.dao.*
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.mapeadores.PresupuestoMappers
import com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE ARCHIVERO MULTIMEDIA (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Orquestar flujos de datos paginados para el historial de chats.
 * [LEY #14]: Filtrado en la fuente (SQL). Mapeo centralizado a Dominio.
 */
@Singleton
class ArchiveroChatMultimediaRepositorio @Inject constructor(
    private val budgetDao: PresupuestoFinalDao,
    private val chatDao: ChatDao,
    private val categoryDao: CategoriaDao,
    private val categoriaRepositorio: CategoriaRepositorio
) {

    /**
     * Obtiene presupuestos paginados resolviendo identidad vía SQL View.
     */
    fun obtenerPresupuestosPaginados(
        idLocal: String,
        idRemoto: String? = null,
        idConcurso: String? = null,
        query: String,
        filters: Set<String>
    ): Flow<PagingData<PresupuestoResumenDominio>> {
        val idCat = filters.find { it.startsWith("cat_") }?.removePrefix("cat_")
        val soloPendientes = filters.contains("filter_pending")
        val soloAceptados = filters.contains("filter_accepted")
        val soloNoLeidos = filters.contains("filter_unread")
        val orden = filters.find { it.startsWith("sort_") } ?: "sort_date"

        return Pager(PagingConfig(pageSize = 20)) {
            budgetDao.buscarPresupuestosSoberanosPaginados(
                idLocal, idRemoto, idConcurso, query, idCat, soloPendientes, soloAceptados, soloNoLeidos, orden
            )
        }.flow.map { pagingData ->
            val allCats = categoryDao.obtenerTodasSync().associateBy { it.id }
            pagingData.map { view ->
                val entity = view.presupuesto
                val cat = entity.idCategoria?.let { allCats[it] }
                
                PresupuestoMappers.aResumenDominio(
                    entidad = entity,
                    foto = view.fotoSoberana,
                    miniatura = entity.urlMiniatura, // 🔥 Usamos el snapshot del documento
                    nombreCat = cat?.nombre ?: "Servicio",
                    iconoCat = cat?.icono ?: "📋",
                    suscrito = view.estaVerificadoSoberano
                )
            }
        }
    }

    /**
     * Obtiene imágenes paginadas de uno o todos los chats.
     */
    fun obtenerImagenesPaginadas(
        idLocal: String,
        idRemoto: String,
        query: String
    ): Flow<PagingData<MensajeEntity>> {
        return Pager(PagingConfig(pageSize = 20)) {
            chatDao.obtenerImagenesSoberanasPaginadas(idLocal, idRemoto, query)
        }.flow
    }

    /**
     * Versión reactiva de rubros en uso combinando flujos soberanos.
     */
    fun observarRubrosEnUso(idLocal: String): Flow<List<CategoriaEntity>> {
        return kotlinx.coroutines.flow.combine(
            budgetDao.obtenerRubrosEnUso(idLocal),
            categoriaRepositorio.todasLasCategorias
        ) { idsEnUso, allCats ->
            val mapa = allCats.associateBy { it.id }
            idsEnUso.mapNotNull { id ->
                if (id == "SIN_RUBRO") {
                    CategoriaEntity(id = "SIN_RUBRO", nombre = "SIN RUBRO", icono = "❔", idSuperCategoria = "SISTEMA")
                } else {
                    mapa[id]
                }
            }.sortedBy { it.nombre }
        }
    }

    fun obtenerPresupuestosIds(
        idLocal: String,
        idRemoto: String? = null,
        idConcurso: String? = null,
        query: String,
        filters: Set<String>
    ): Flow<List<String>> {
        val idCat = filters.find { it.startsWith("cat_") }?.removePrefix("cat_")
        val soloPendientes = filters.contains("filter_pending")
        val soloAceptados = filters.contains("filter_accepted")
        val soloNoLeidos = filters.contains("filter_unread")
        return budgetDao.buscarPresupuestosSoberanosIds(idLocal, idRemoto, idConcurso, query, idCat, soloPendientes, soloAceptados, soloNoLeidos)
    }

    fun obtenerImagenesIds(
        idLocal: String,
        idRemoto: String,
        query: String
    ): Flow<List<String>> {
        return chatDao.obtenerImagenesSoberanasIds(idLocal, idRemoto, query)
    }
}
