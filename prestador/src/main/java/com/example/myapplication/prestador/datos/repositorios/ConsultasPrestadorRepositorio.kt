package com.example.myapplication.prestador.datos.repositorios

import com.example.myapplication.core.datos.local.dao.*
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.core.dominio.mapeadores.*
import com.example.myapplication.core.datos.local.entidades.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE CONSULTAS PRESTADOR (PRO - v2026.ELITE) ---
 */
@Singleton
class ConsultasPrestadorRepositorio @Inject constructor(
    private val cuentaDao: CuentaDao,
    private val prestadorDao: IdentidadPrestadorDao,
    private val empresaDao: EmpresaDao,
    private val sucursalDao: SucursalDao
) {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun obtenerPerfilPrestadorDeepFlujo(uid: String): Flow<PerfilPrestadorDeepModelo?> {
        return cuentaDao.obtenerPorId(uid).flatMapLatest { cuenta ->
            if (cuenta == null) flowOf(null)
            else {
                combine(
                    obtenerPrestadorCompletoFlujo(uid),
                    obtenerEmpresasCompletasDeUsuarioFlujo(uid)
                ) { prestador, empresas ->
                    if (prestador == null) null
                    else PerfilPrestadorDeepModelo(cuenta, prestador, empresas)
                }
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun obtenerPrestadorCompletoFlujo(uid: String): Flow<PrestadorDominioCompleto?> {
        return combine(
            prestadorDao.obtenerPrestadorCompleto(uid),
            cuentaDao.obtenerPorId(uid)
        ) { relacion, cuenta ->
            relacion?.let { PrestadorMappers.deRelacionADominioCompleto(it, cuenta) }
        }
    }

    fun obtenerEmpresasCompletasDeUsuarioFlujo(uid: String): Flow<List<EmpresaDominioCompleto>> {
        return empresaDao.obtenerEmpresasCompletas(uid).map { lista ->
            lista.map { EmpresaMappers.deRelacionADominioCompleto(it) }
        }
    }

    fun obtenerSucursalCompletaFlujo(idSucursal: String): Flow<SucursalDominioCompleto?> {
        return sucursalDao.obtenerSucursalCompleta(idSucursal).map { relacion ->
            relacion?.let { SucursalMappers.deRelacionADominioCompleto(it) }
        }
    }
}
