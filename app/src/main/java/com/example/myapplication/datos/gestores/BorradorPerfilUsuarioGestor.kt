package com.example.myapplication.datos.gestores

import com.example.myapplication.core.dominio.modelos.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- GESTOR DE BORRADOR DE PERFIL USUARIO (App Azul - v2026.ELITE) ---
 * [PROPÓSITO]: Centralizar el estado temporal de edición del cliente en memoria (RAM).
 * [LEY #9]: Estándar Maverick en Español.
 * [SOBERANÍA]: Exclusivo de :app (Cliente).
 */
@Singleton
class BorradorPerfilUsuarioGestor @Inject constructor() {

    private val _estadoOriginal = MutableStateFlow<CuentaMaestroUsuario?>(null)
    private val _borrador = MutableStateFlow<CuentaMaestroUsuario?>(null)

    val borrador: StateFlow<CuentaMaestroUsuario?> = _borrador.asStateFlow()

    /**
     * 🔥 [ELITE]: Flujo reactivo que indica si el borrador difiere de la DB.
     */
    val hayCambiosPendientes: Flow<Boolean> = combine(_estadoOriginal, _borrador) { original, borrador ->
        val differ = if (original == null || borrador == null) false
        else original != borrador
        
        if (differ) {
            android.util.Log.d("BorradorUserGestor", "🔍 [CAMBIOS_DETECTADOS] El borrador del cliente difiere de la línea base.")
        }
        differ
    }

    /**
     * Inicializa el borrador con los datos actuales de la base de datos.
     */
    fun iniciarEdicion(maestro: CuentaMaestroUsuario) {
        _estadoOriginal.value = maestro
        _borrador.value = maestro.copy()
    }

    /**
     * 🔥 [ELITE]: Consolida el borrador actual como el nuevo estado original.
     * Se llama tras un guardado exitoso en Room.
     */
    fun consolidarEdicion() {
        val actual = _borrador.value
        if (actual != null) {
            android.util.Log.d("BorradorUserGestor", "💾 [CONSOLIDAR] Sincronizando línea base del cliente con el borrador.")
            _estadoOriginal.value = actual.copy()
        }
    }

    /**
     * Limpia el borrador descartando cualquier cambio en memoria.
     */
    fun descartarCambios() {
        _borrador.value = _estadoOriginal.value
    }

    // --- SECTOR: MUTACIONES TÁCTICAS (100% Dominio) ---

    fun actualizarPerfilPersonal(nuevoPerfil: UsuarioDominio) {
        _borrador.update { actual ->
            actual?.copy(
                usuario = actual.usuario.copy(perfil = nuevoPerfil)
            )
        }
    }

    fun actualizarDireccionPersonal(direccion: DireccionDominio) {
        _borrador.update { actual ->
            if (actual == null) return@update null
            val nuevasDirs = actual.usuario.direcciones.toMutableList()
            val index = nuevasDirs.indexOfFirst { it.id == direccion.id }
            
            // Aseguramos la integridad de la propiedad (Ley de Soberanía)
            val direccionLimpia = direccion.copy(idPropietario = actual.usuario.perfil.id)
            
            if (index != -1) nuevasDirs[index] = direccionLimpia else nuevasDirs.add(direccionLimpia)
            
            actual.copy(
                usuario = actual.usuario.copy(direcciones = nuevasDirs)
            )
        }
    }

    fun eliminarDireccionPersonal(idDireccion: String) {
        _borrador.update { actual ->
            actual?.copy(
                usuario = actual.usuario.copy(
                    direcciones = actual.usuario.direcciones.filter { it.id != idDireccion }
                )
            )
        }
    }

    fun actualizarEmpresa(empresa: EmpresaDominio) {
        _borrador.update { actual ->
            if (actual == null) return@update null
            val lista = actual.empresas.toMutableList()
            val index = lista.indexOfFirst { it.empresa.id == empresa.id }
            
            // Sincronización de propiedad
            val empresaLimpia = empresa.copy(idPropietario = actual.usuario.perfil.id)
            
            if (index != -1) {
                lista[index] = lista[index].copy(empresa = empresaLimpia)
            } else {
                lista.add(EmpresaDominioCompleto(empresaLimpia, emptyList()))
            }
            actual.copy(empresas = lista)
        }
    }

    fun eliminarEmpresa(idEmpresa: String) {
        _borrador.update { actual ->
            actual?.copy(
                empresas = actual.empresas.filter { it.empresa.id != idEmpresa }
            )
        }
    }

    /**
     * 🔥 [ELITE]: Actualiza una sucursal en el borrador.
     * [REGLA #4]: No se copian campos de dirección a la sucursal (Soberanía de DireccionDominio).
     * [INTEGRIDAD]: Se aseguran los vínculos idEmpresaPadre e idPropietario.
     */
    fun actualizarSucursal(idEmpresa: String, sucursal: SucursalDominio, direccion: DireccionDominio?) {
        _borrador.update { actual ->
            if (actual == null) return@update null
            
            // Vínculos de dominio puros (Sin campos espejo de ubicación)
            val sucursalLimpia = sucursal.copy(
                idEmpresaPadre = idEmpresa,
                idPropietario = actual.usuario.perfil.id
            )
            
            val direccionLimpia = direccion?.copy(
                idPropietario = actual.usuario.perfil.id,
                idSucursal = sucursal.id
            )

            val nuevasEmpresas = actual.empresas.map { emp ->
                if (emp.empresa.id == idEmpresa) {
                    val sucs = emp.sucursales.toMutableList()
                    val index = sucs.indexOfFirst { it.id == sucursal.id }
                    
                    val sucComp = if (index != -1) {
                        sucs[index].copy(sucursal = sucursalLimpia, direccion = direccionLimpia)
                    } else {
                        SucursalDominioCompleto(sucursalLimpia, direccionLimpia)
                    }
                    if (index != -1) sucs[index] = sucComp else sucs.add(sucComp)
                    emp.copy(sucursales = sucs)
                } else emp
            }
            actual.copy(empresas = nuevasEmpresas)
        }
    }

    fun eliminarSucursal(idSucursal: String) {
        _borrador.update { actual ->
            if (actual == null) return@update null
            val nuevasEmpresas = actual.empresas.map { emp ->
                emp.copy(sucursales = emp.sucursales.filter { it.id != idSucursal })
            }
            actual.copy(empresas = nuevasEmpresas)
        }
    }

    fun obtenerBorradorMaestro(): CuentaMaestroUsuario? = _borrador.value
}
