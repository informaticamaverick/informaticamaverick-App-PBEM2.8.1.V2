/*
package com.example.myapplication.core.datos.repositorios

import com.example.myapplication.core.datos.local.dao.CategoriaRapidaDao
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import com.example.myapplication.core.datos.local.entidades.CategoriaRapidaEntity
import com.example.myapplication.core.datos.indices.busqueda.IndiceBusquedaUsuarioRepositorio
import com.example.myapplication.core.dominio.modelos.descubrimiento.ResultadoIndiceBusquedaShallowDominio
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO TÁCTICO FAST RESULTADO BÚSQUEDA PRESTADOR (Atómico - v2026.ELITE) ---
 * [RESPONSABILIDAD]: Gestionar el historial del radar y delegar la búsqueda táctica al índice.
 * [LEY #9]: Estándar Mav en Español.
 */
@Singleton
class FastResultadoBusquedaPrestadorRepositorio @Inject constructor(
    private val categoriaRapidaDao: CategoriaRapidaDao,
    private val lectorIndice: IndiceBusquedaUsuarioRepositorio
) {
    fun obtenerHistorialRadar(): Flow<List<CategoriaRapidaEntity>> = categoriaRapidaDao.obtenerMasUsados(10)

    suspend fun registrarUsoRadar(cat: CategoriaEntity) {
        categoriaRapidaDao.registrarUso(cat)
    }

    /**
     * 🔥 [ELITE]: Busca expertos en el radar usando el lector de índice centralizado.
     * Implementa la estrategia de proximidad Maverick.
     */
    suspend fun buscarEnRadar(
        codigoPostal: String,
        categoria: String,
        lat: Double = 0.0,
        lng: Double = 0.0,
        solo24h: Boolean = false
    ): List<ResultadoIndiceBusquedaShallowDominio> {
        // [ELITE v2026.11]: Delegación total al lector de índice que ya maneja Geohash y CP.
        // Primero intentamos por proximidad geográfica.
        val resultados = if (lat != 0.0) {
            lectorIndice.buscarPorProximidad(lat, lng, categoria, limite = 15)
        } else {
            lectorIndice.buscarPorZona(codigoPostal, categoria, limite = 15)
        }

        // Filtro táctico de 24hs (opcional)
        return if (solo24h) {
            resultados.filter { it.atiende24h }
        } else {
            resultados
        }
    }
}
*/
