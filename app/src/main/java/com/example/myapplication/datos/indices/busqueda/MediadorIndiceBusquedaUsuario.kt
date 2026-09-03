package com.example.myapplication.datos.indices.busqueda

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.myapplication.core.datos.indices.busqueda.IndiceBusquedaUsuarioRepositorio
import com.example.myapplication.core.datos.local.entidades.ClaveRemotaBusquedaEntity
import com.example.myapplication.core.datos.local.entidades.vistas.ResultadoBusquedaPrestadorSQLView
import com.example.myapplication.core.datos.local.AppDatabase
import com.example.myapplication.datos.motores.ResultadosPrestadorMotorLocal
import com.example.myapplication.core.dominio.modelos.descubrimiento.ResultadoIndiceBusquedaShallowDominio
import com.example.myapplication.coordinadores.CoordinadorAcciones
import com.example.myapplication.ui.componentes.be.modelos.TipoBeToast

/**
 * --- MEDIADOR DE ÍNDICE DE BÚSQUEDA: USUARIO (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Orquestar Paging 3 con la estrategia de cascada Proximidad -> Zona.
 * [LEY #6]: Soberanía del Cliente.
 * [LEY #12]: Portavoz Be. Notifica estados al HUD global.
 */
@OptIn(ExperimentalPagingApi::class)
class MediadorIndiceBusquedaUsuario(
    private val idConsulta: String,
    private val rubro: String,
    private val cp: String,
    private val lat: Double,
    private val lng: Double,
    private val db: AppDatabase,
    private val lectorIndice: IndiceBusquedaUsuarioRepositorio,
    private val motorLocal: ResultadosPrestadorMotorLocal,
    private val coordinador: CoordinadorAcciones // 🔥 [LEY #12] Inyectado para Feedback Visual
) : RemoteMediator<Int, ResultadoBusquedaPrestadorSQLView>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ResultadoBusquedaPrestadorSQLView>
    ): MediatorResult {
        return try {
            if (loadType == LoadType.REFRESH) {
                coordinador.mostrarToast("Buscando expertos...", TipoBeToast.PROCESANDO, duracionMs = 0) // Persistente hasta terminar
            }

            val cursorId = when (loadType) {
                LoadType.REFRESH -> {
                    android.util.Log.d("BUSQUEDA_FLOW", "🔄 [INICIO_REFRESH] Limpiando lista y reiniciando búsqueda para: $idConsulta")
                    null
                }
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val clave = db.claveRemotaBusquedaDao().obtenerClave(idConsulta)
                    android.util.Log.d("BUSQUEDA_FLOW", "⏬ [INICIO_APPEND] Buscando más resultados desde cursor: ${clave?.ultimoDocumentoId}")
                    if (clave?.ultimoDocumentoId == null) return MediatorResult.Success(endOfPaginationReached = true)
                    clave.ultimoDocumentoId
                }
            }

            // --- ESTRATEGIA DE CASCADA MAVERICK ---
            var itemsShallow = if (lat != 0.0) {
                android.util.Log.d("BUSQUEDA_FLOW", "📍 [PASO_1_PROXIMIDAD] Intentando búsqueda por Geohash (GPS)")
                lectorIndice.buscarPorProximidad(lat, lng, rubro, state.config.pageSize, cursorId)
            } else emptyList()

            if (itemsShallow.isEmpty()) {
                android.util.Log.d("BUSQUEDA_FLOW", "🏙️ [PASO_2_ZONA] Sin resultados por GPS o GPS inactivo. Intentando búsqueda por Código Postal: $cp")
                itemsShallow = lectorIndice.buscarPorZona(cp, rubro, state.config.pageSize, cursorId)
            }

            android.util.Log.d("BUSQUEDA_FLOW", "📊 [RESULTADO_RED] Se encontraron ${itemsShallow.size} expertos en la nube.")

            val finalAlcanzado = itemsShallow.isEmpty()

            // --- PERSISTENCIA SOBERANA ---
            if (loadType == LoadType.REFRESH) {
                db.claveRemotaBusquedaDao().eliminarClave(idConsulta)
                // [FIX]: sin esto, un resultado que dejó de matchear en Firestore (ej. un
                // prestador que activó "Modo empresa" y se borró del índice) seguía apareciendo
                // para siempre en el caché local — solo se insertaban relaciones nuevas, nunca
                // se limpiaban las viejas que ya no vienen en la respuesta fresca.
                db.resultadoBusquedaPrestadorDao().limpiarResultadosDeConsulta(idConsulta)
            }

            val ultimoId = itemsShallow.lastOrNull()?.id
            db.claveRemotaBusquedaDao().guardarClave(ClaveRemotaBusquedaEntity(idConsulta, ultimoId))

            // Usamos el motor local especializado para guardar los resultados
            android.util.Log.d("BUSQUEDA_FLOW", "📦 [PERSISTENCIA] Impactando Room con nuevos datos para sincronización local.")
            motorLocal.guardarResultados(idConsulta, itemsShallow)

            if (loadType == LoadType.REFRESH) {
                coordinador.mostrarToast("Lista actualizada", TipoBeToast.EXITO, duracionMs = 2000)
            }

            MediatorResult.Success(endOfPaginationReached = finalAlcanzado)
        } catch (e: Exception) {
            android.util.Log.e("BUSQUEDA_FLOW", "❌ [FALLO_CRÍTICO] Error en el mediador: ${e.message}", e)
            coordinador.mostrarToast("Fallo en la sincronización", TipoBeToast.ERROR)
            MediatorResult.Error(e)
        }
    }
}

