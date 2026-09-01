package com.example.myapplication.prestador.dominio.motores

import com.example.myapplication.core.datos.indices.promocion.IndicePromocionPrestadorRepositorio
import com.example.myapplication.core.datos.local.dao.IdentidadPrestadorDao
import com.example.myapplication.core.datos.local.dao.CuentaDao
import com.example.myapplication.core.dominio.mapeadores.discovery.IndicePromocionShallowMappers
import com.example.myapplication.core.dominio.mapeadores.shallow.PrestadorShallowMappers
import com.example.myapplication.core.dominio.modelos.Promocion
import com.example.myapplication.prestador.datos.repositorios.PrestadorPromocionRepositorio
import android.util.Log
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- MOTOR DE PROMOCIÓN PRESTADOR (Naranja - v2026.ELITE) ---
 * [PROPÓSITO]: Orquestador de ofertas e historias para el prestador.
 * [LEY #13]: Atómico. Coordina persistencia e indexación de marketing.
 */
@Singleton
class MotorPromocionPrestador @Inject constructor(
    private val repoLocal: PrestadorPromocionRepositorio,
    private val repoIndice: IndicePromocionPrestadorRepositorio,
    private val prestadorDao: IdentidadPrestadorDao,
    private val cuentaDao: CuentaDao
) {

    /**
     * 🔥 [ELITE]: Publica una nueva promoción ejecutando el impacto total.
     * [ORDEN]: 1. Room + Multimedia (SSOT) -> 2. Index (Discovery).
     */
    suspend fun publicarNuevaPromocion(promo: Promocion) {
        try {
            Log.d("MOTOR_PROMO", "🚀 [ATÓMICO_INICIO] Publicando oferta: ${promo.id}")
            
            // 0. Enriquecimiento de Identidad (SSOT)
            val prestadorInfo = prestadorDao.obtenerPorId(promo.idPrestador).firstOrNull()
            val cuentaInfo = cuentaDao.obtenerPorIdSync(promo.idPrestador)
            val estaSuscrito = cuentaInfo?.estaSuscrito ?: false
            
            val promoConIdentidad = if (prestadorInfo != null) {
                promo.copy(
                    nombrePrestador = prestadorInfo.nombreVisible,
                    urlFotoPrestador = prestadorInfo.urlFotoPerfil,
                    reputacion = prestadorInfo.reputacion,
                    estaVerificado = prestadorInfo.estaVerificado,
                    estaSuscrito = estaSuscrito
                )
            } else {
                promo.copy(estaSuscrito = estaSuscrito)
            }

            // 1. Persistencia Local y Multimedia
            val promoFinal = repoLocal.guardarPromocionLocalConMultimedia(promoConIdentidad)
            
            // 2. Proyección al Índice (Discovery Liviano)
            if (prestadorInfo != null) {
                val prestadorShallow = PrestadorShallowMappers.deEntidadADominio(
                    entidad = prestadorInfo,
                    estaSuscrito = estaSuscrito
                )
                
                val sobreShallow = IndicePromocionShallowMappers.deDominioAShallow(
                    promo = promoFinal,
                    prestadorShallow = prestadorShallow
                )
                
                android.util.Log.d("MOTOR_PROMO_DEBUG", "📦 [SHALLOW_PREP] Sobre listo para indexar. ID: ${sobreShallow.idPromocion} | Categorias: ${sobreShallow.idCategorias}")
                
                repoIndice.publicarPromocion(sobreShallow, promo.codigoPostal ?: "")
                Log.d("MOTOR_PROMO", "✅ [ATÓMICO_OK] Oferta persistida e indexada.")
            } else {
                Log.w("MOTOR_PROMO", "⚠️ [ATÓMICO_WARN] No se encontró info del prestador para el índice.")
            }
            
        } catch (e: Exception) {
            Log.e("MOTOR_PROMO", "❌ [ATÓMICO_ERR] Fallo en la publicación: ${e.message}", e)
            throw e
        }
    }

    /**
     * Elimina una promoción de todo el sistema.
     */
    suspend fun eliminarPromocion(idPromocion: String) {
        Log.d("MOTOR_PROMO", "🗑️ [ELIMINAR] Borrando oferta: $idPromocion")
        repoLocal.eliminarPromocionLocal(idPromocion)
        repoIndice.eliminarPromocion(idPromocion)
    }
}




