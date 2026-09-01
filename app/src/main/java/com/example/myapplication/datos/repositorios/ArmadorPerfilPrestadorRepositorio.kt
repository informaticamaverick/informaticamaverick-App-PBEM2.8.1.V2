package com.example.myapplication.datos.repositorios

import com.example.myapplication.core.datos.local.dao.*
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.core.dominio.mapeadores.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO ARMADOR DE PERFIL PRESTADOR (v2026.ELITE) ---
 * [PROPÓSITO]: Especialista en ensamblar perfiles polimórficos (Individuo, Empresa, Sucursal).
 * [LEY #2]: Costo Zero. Lectura Local-First optimizada.
 */
@Singleton
class ArmadorPerfilPrestadorRepositorio @Inject constructor(
    private val prestadorDao: IdentidadPrestadorDao,
    private val cuentaDao: CuentaDao,
    private val empresaDao: EmpresaDao,
    private val sucursalDao: SucursalDao
) {

    /**
     * 🔥 [ELITE]: Obtiene el perfil de UI unificado buscando en la jerarquía polimórfica.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun obtenerPerfilPolimorficoFlujo(id: String): Flow<PrestadorDominio?> {
        val pFlow = prestadorDao.obtenerPrestadorCompleto(id)
        val eFlow = empresaDao.obtenerEmpresaCompleta(id)
        val sFlow = sucursalDao.obtenerSucursalCompleta(id)
        val cFlow = cuentaDao.obtenerPorId(id)

        return combine(pFlow, eFlow, sFlow, cFlow) { pRel, eRel, sRel, cuenta ->
            when {
                pRel != null -> PrestadorMappers.deRelacionADominioCompleto(pRel, cuenta).let { 
                    PrestadorMappers.deCompletoAModeloUi(it) 
                }
                eRel != null -> EmpresaMappers.deRelacionADominioCompleto(eRel).let { 
                    PrestadorMappers.deEmpresaAModeloUi(it) 
                }
                sRel != null -> SucursalMappers.deRelacionADominioCompleto(sRel).let { 
                    PrestadorMappers.deSucursalAModeloUi(it) 
                }
                else -> null
            }
        }
    }

    /**
     * Mantenemos compatibilidad para el flujo completo si es necesario.
     */
    fun obtenerPrestadorCompletoFlujo(id: String): Flow<PrestadorDominioCompleto?> {
        return combine(
            prestadorDao.obtenerPrestadorCompleto(id),
            cuentaDao.obtenerPorId(id)
        ) { relacion, cuenta ->
            relacion?.let { PrestadorMappers.deRelacionADominioCompleto(it, cuenta) }
        }
    }
}




