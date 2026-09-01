package com.example.myapplication.datos.repositorios

import com.example.myapplication.core.datos.local.dao.*
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.core.dominio.mapeadores.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE CONSULTAS USUARIO (App Azul - v2026.ELITE) ---
 */
@Singleton
class ConsultasUsuarioRepositorio @Inject constructor(
    private val cuentaDao: CuentaDao,
    private val usuarioDao: IdentidadUsuarioDao,
    private val direccionDao: DireccionDao,
    private val empresaDao: EmpresaDao,
    private val sucursalDao: SucursalDao
) {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun obtenerCuentaMaestroUsuarioFlujo(uid: String): Flow<CuentaMaestroUsuario?> {
        return cuentaDao.obtenerPorId(uid).flatMapLatest { cuenta ->
            if (cuenta == null) flowOf(null)
            else {
                combine(
                    obtenerUsuarioCompletoFlujo(uid),
                    obtenerEmpresasCompletasDeUsuarioFlujo(uid)
                ) { usuario, empresas ->
                    if (usuario == null) null
                    else CuentaMaestroUsuario(
                        cuenta = cuenta,
                        usuario = usuario,
                        empresas = empresas
                    )
                }
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun obtenerUsuarioCompletoFlujo(uid: String): Flow<UsuarioDominioCompleto?> {
        return usuarioDao.obtenerPorId(uid).flatMapLatest { perfil ->
            if (perfil == null) flowOf(null)
            else {
                direccionDao.obtenerPorPropietario(uid).map { direcciones ->
                    UsuarioDominioCompleto(
                        perfil = UsuarioMappers.deRelacionADominio(com.example.myapplication.core.datos.local.relaciones.UsuarioConDireccionesRelacionesBD(perfil, emptyList())),
                        direcciones = direcciones.map { DireccionMappers.deEntidadAModelo(it) }
                    )
                }
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun obtenerEmpresasCompletasDeUsuarioFlujo(uid: String): Flow<List<EmpresaDominioCompleto>> {
        return empresaDao.obtenerPorPropietario(uid).flatMapLatest { listaEmpresas ->
            if (listaEmpresas.isEmpty()) flowOf(emptyList())
            else {
                val flujosEmpresas = listaEmpresas.map { empresa ->
                    sucursalDao.obtenerPorEmpresa(empresa.id).map { sucursales ->
                        EmpresaDominioCompleto(
                            empresa = EmpresaMappers.deEntidadADominio(empresa),
                            sucursales = sucursales.map { SucursalDominioCompleto(SucursalMappers.deEntidadADominio(it)) } 
                        )
                    }
                }
                combine(flujosEmpresas) { it.toList() }
            }
        }
    }
}
