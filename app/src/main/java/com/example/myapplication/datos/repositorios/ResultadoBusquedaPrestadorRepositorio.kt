package com.example.myapplication.datos.repositorios

import androidx.paging.*
import com.example.myapplication.core.datos.indices.busqueda.IndiceBusquedaUsuarioRepositorio
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.datos.indices.busqueda.MediadorIndiceBusquedaUsuario
import com.example.myapplication.core.datos.local.AppDatabase
import com.example.myapplication.datos.motores.ResultadosPrestadorMotorLocal
import com.example.myapplication.coordinadores.CoordinadorAcciones
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO RESULTADO BÚSQUEDA PRESTADOR (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Centralizar el flujo de datos paginados del motor de búsqueda.
 * [LEY #9]: Estándar Maverick en Español.
 */
@Singleton
class ResultadoBusquedaPrestadorRepositorio @Inject constructor(
    private val db: AppDatabase,
    private val lectorIndice: IndiceBusquedaUsuarioRepositorio,
    private val motorLocal: ResultadosPrestadorMotorLocal,
    private val coordinador: CoordinadorAcciones // 🔥 [LEY #12] Inyectado para notificaciones tácticas
) {

    /**
     * 🔥 [ELITE]: Obtiene el flujo de PagingData para el catálogo (sin filtros forzados).
     * El filtrado ocurre en Room (SQL-First) para ahorrar costos de Firebase.
     */
    @OptIn(ExperimentalPagingApi::class)
    fun obtenerResultadosDeCategoria(
        idConsulta: String,
        rubro: String,
        cp: String,
        lat: Double,
        lng: Double,
        query: String = "",
        solo24h: Boolean = false,
        soloVerificados: Boolean = false,
        conEnvio: Boolean = false,
        estaOnline: Boolean = false,
        orden: String = "reciente"
    ): Flow<PagingData<PrestadorDominio>> {
        return Pager(
            config = PagingConfig(pageSize = 15, enablePlaceholders = false),
            remoteMediator = MediadorIndiceBusquedaUsuario(
                idConsulta = idConsulta,
                rubro = rubro,
                cp = cp,
                lat = lat,
                lng = lng,
                db = db,
                lectorIndice = lectorIndice,
                motorLocal = motorLocal,
                coordinador = coordinador
            )
        ) {
            db.resultadoBusquedaPrestadorDao().obtenerResultadosPaginados(
                idConsulta = idConsulta,
                query = query,
                solo24h = solo24h,
                soloVerificados = soloVerificados,
                conEnvio = conEnvio,
                estaOnline = estaOnline,
                orden = orden
            )
        }.flow.map { pagingData ->
            pagingData.map { view -> view.aModeloDominio(lat, lng) }
        }
    }

    /**
     * 🔥 [ELITE]: Obtiene el flujo de PagingData táctico para el radar FAST.
     * Implementa filtros agresivos por defecto si es necesario.
     */
    @OptIn(ExperimentalPagingApi::class)
    fun obtenerResultadosTacticosFAST(
        idConsulta: String,
        rubro: String,
        cp: String,
        lat: Double,
        lng: Double
    ): Flow<PagingData<PrestadorDominio>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            remoteMediator = MediadorIndiceBusquedaUsuario(
                idConsulta = idConsulta,
                rubro = rubro,
                cp = cp,
                lat = lat,
                lng = lng,
                db = db,
                lectorIndice = lectorIndice,
                motorLocal = motorLocal,
                coordinador = coordinador
            )
        ) {
            // En FAST forzamos filtros de disponibilidad inmediata
            db.resultadoBusquedaPrestadorDao().obtenerResultadosPaginados(
                idConsulta = idConsulta,
                solo24h = true,
                estaOnline = true,
                orden = "cercania" 
            )
        }.flow.map { pagingData ->
            pagingData.map { view -> view.aModeloDominio(lat, lng) }
        }
    }

    /**
     * Obtiene una lista estática de resultados para el Radar.
     */
    fun obtenerListaRadar(
        idConsulta: String,
        lat: Double,
        lng: Double,
        solo24h: Boolean = true
    ): Flow<List<PrestadorDominio>> {
        return db.resultadoBusquedaPrestadorDao().obtenerListaEstatica(idConsulta, solo24h)
            .map { list ->
                list.map { view -> view.aModeloDominio(lat, lng) }
            }
    }

    /**
     * 🔥 [ELITE]: Obtiene las categorías más usadas para el radar FAST.
     */
    /**
     * 🔥 [ELITE]: Obtiene las categorías más usadas basadas en el historial real.
     */
    fun obtenerRubrosMasUsados(limite: Int = 10): Flow<List<com.example.myapplication.core.datos.local.entidades.CategoriaEntity>> {
        return db.categoriaRapidaDao().obtenerMasUsados(limite)
            .flatMapLatest { historial ->
                if (historial.isEmpty()) flowOf(emptyList())
                else {
                    combine(historial.map { h -> db.categoriaDao().obtenerPorIdFlow(h.id) }) { array ->
                        array.filterNotNull()
                    }
                }
            }
    }
}

