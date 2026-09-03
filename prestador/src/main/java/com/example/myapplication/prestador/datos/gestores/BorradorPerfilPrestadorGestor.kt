package com.example.myapplication.prestador.datos.gestores

import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.modelos.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- GESTOR DE BORRADOR DE PERFIL (App Naranja - v2026.ELITE) ---
 * [PROPÓSITO]: Centralizar el estado temporal de edición del prestador en memoria (RAM).
 * [LEY #9]: Estándar Maverick en Español.
 * [SOBERANÍA]: Exclusivo del módulo :prestador.
 */
@Singleton
class BorradorPerfilPrestadorGestor @Inject constructor() {

    private val _estadoOriginal = MutableStateFlow<PerfilPrestadorDeepModelo?>(null)
    private val _borrador = MutableStateFlow<PerfilPrestadorDeepModelo?>(null)

    val borrador: StateFlow<PerfilPrestadorDeepModelo?> = _borrador.asStateFlow()

    /**
     * 🔥 [ELITE]: Flujo reactivo que indica si el borrador difiere de la DB.
     */
    val hayCambiosPendientes: Flow<Boolean> = combine(_estadoOriginal, _borrador) { original, borrador ->
        val differ = if (original == null || borrador == null) false
        else original != borrador
        
        if (differ) {
            android.util.Log.d("BorradorGestor", "🔍 [CAMBIOS_DETECTADOS] El borrador difiere de la línea base.")
        }
        differ
    }

    /**
     * Inicializa el borrador con los datos actuales de la base de datos.
     */
    fun iniciarEdicion(maestro: PerfilPrestadorDeepModelo) {
        _estadoOriginal.value = maestro
        _borrador.value = maestro.copy()
    }

    /**
     * Compara el estado original con el borrador para detectar cambios.
     */
    fun hayCambiosPendientes(): Boolean {
        val original = _estadoOriginal.value ?: return false
        val actual = _borrador.value ?: return false
        // [ELITE]: Comparación profunda de ecosistemas
        return original != actual
    }

    /**
     * 🔥 [ELITE]: Consolida el borrador actual como el nuevo estado original.
     * Se llama tras un guardado exitoso en Room.
     */
    fun consolidarEdicion() {
        val actual = _borrador.value
        if (actual != null) {
            android.util.Log.d("BorradorGestor", "💾 [CONSOLIDAR] Sincronizando línea base con el borrador actual.")
            _estadoOriginal.value = actual.copy() // Forzamos nueva referencia por seguridad
        }
    }

    /**
     * Limpia el borrador descartando cualquier cambio en memoria.
     */
    fun descartarCambios() {
        _borrador.value = _estadoOriginal.value
    }

    // --- SECTOR: MUTACIONES TÁCTICAS (100% Español) ---

    /**
     * 🔥 [ELITE]: Refleja en el borrador un cambio que YA se persistió (Room + Firestore)
     * por fuera del flujo normal de edición — como alternarSoberania(). Se actualizan
     * borrador Y línea base juntos (no es un cambio "pendiente" de guardar) para que un
     * guardado posterior de otro campo no lo pise con el valor viejo.
     */
    fun actualizarModoSoberania(idPerfilActivo: String?, priorizarEmpresa: Boolean) {
        val actualizarCuenta: (CuentaEntity) -> CuentaEntity = {
            it.copy(idPerfilActivo = idPerfilActivo, priorizarEmpresa = priorizarEmpresa)
        }
        _borrador.update { actual -> actual?.copy(cuenta = actualizarCuenta(actual.cuenta)) }
        _estadoOriginal.update { actual -> actual?.copy(cuenta = actualizarCuenta(actual.cuenta)) }
    }

    fun actualizarPerfilPersonal(nuevoPerfil: PrestadorDominio) {
        _borrador.update { actual ->
            actual?.copy(
                prestador = actual.prestador.copy(perfil = nuevoPerfil)
            )
        }
    }

    fun actualizarHorario(idReferencia: String, nuevoHorario: HorarioDominio) {
        _borrador.update { actual ->
            if (actual == null) return@update null
            
            // 1. ¿Es el horario del prestador raíz?
            if (actual.prestador.perfil.id == idReferencia) {
                return@update actual.copy(
                    prestador = actual.prestador.copy(horario = nuevoHorario)
                )
            }

            // 2. ¿Es el horario de una sucursal, recurso o empleado?
            val nuevasEmpresas = actual.empresas.map { emp ->
                emp.copy(
                    sucursales = emp.sucursales.map { suc ->
                        if (suc.id == idReferencia) {
                            suc.copy(horario = nuevoHorario)
                        } else {
                            // Buscar en equipo y recursos de esta sucursal
                            val nuevoEquipo = suc.equipoTrabajo.map { empStaff ->
                                if (empStaff.id == idReferencia) empStaff.copy(horario = nuevoHorario) else empStaff
                            }
                            val nuevosRecursos = suc.recursos.map { res ->
                                if (res.id == idReferencia) res.copy(horario = nuevoHorario) else res
                            }
                            suc.copy(equipoTrabajo = nuevoEquipo, recursos = nuevosRecursos)
                        }
                    }
                )
            }
            actual.copy(empresas = nuevasEmpresas)
        }
    }

    fun actualizarDireccionPersonal(direccion: DireccionDominio) {
        _borrador.update { actual ->
            if (actual == null) return@update null
            val nuevasDirs = actual.prestador.direcciones.toMutableList()
            val index = nuevasDirs.indexOfFirst { it.id == direccion.id }
            if (index != -1) nuevasDirs[index] = direccion else nuevasDirs.add(direccion)
            
            // [ELITE]: Sincronizar el CP si es la dirección de referencia
            val perfilActualizado = if (direccion.idReferencia == actual.prestador.perfil.id) {
                actual.prestador.perfil.copy(
                    codigoPostal = direccion.codigoPostal
                )
            } else actual.prestador.perfil

            actual.copy(
                prestador = actual.prestador.copy(
                    perfil = perfilActualizado,
                    direcciones = nuevasDirs
                )
            )
        }
    }

    fun eliminarDireccionPersonal(idDireccion: String) {
        _borrador.update { actual ->
            actual?.copy(
                prestador = actual.prestador.copy(
                    direcciones = actual.prestador.direcciones.filter { it.id != idDireccion }
                )
            )
        }
    }

    fun actualizarEmpresa(empresa: EmpresaDominio) {
        _borrador.update { actual ->
            if (actual == null) return@update null
            val lista = actual.empresas.toMutableList()
            val index = lista.indexOfFirst { it.empresa.id == empresa.id }
            if (index != -1) {
                lista[index] = lista[index].copy(empresa = empresa)
            } else {
                lista.add(EmpresaDominioCompleto(empresa, emptyList()))
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

    fun actualizarSucursal(idEmpresa: String, sucursal: SucursalDominio, direccion: DireccionDominio?) {
        _borrador.update { actual ->
            if (actual == null) return@update null
            val nuevasEmpresas = actual.empresas.map { emp ->
                if (emp.empresa.id == idEmpresa) {
                    val sucs = emp.sucursales.toMutableList()
                    val index = sucs.indexOfFirst { it.id == sucursal.id }
                    
                    val sucComp = if (index != -1) {
                        sucs[index].copy(sucursal = sucursal, direccion = direccion)
                    } else {
                        SucursalDominioCompleto(sucursal, direccion)
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

    fun actualizarEquipoSucursal(idSucursal: String, equipo: List<EquipoTrabajoDominio>) {
        _borrador.update { actual ->
            if (actual == null) return@update null
            val nuevasEmpresas = actual.empresas.map { emp ->
                emp.copy(
                    sucursales = emp.sucursales.map { suc ->
                        if (suc.id == idSucursal) suc.copy(equipoTrabajo = equipo)
                        else suc
                    }
                )
            }
            actual.copy(empresas = nuevasEmpresas)
        }
    }

    fun actualizarRecursosSucursal(idSucursal: String, recursos: List<RecursoDominio>) {
        _borrador.update { actual ->
            if (actual == null) return@update null
            val nuevasEmpresas = actual.empresas.map { emp ->
                emp.copy(
                    sucursales = emp.sucursales.map { suc ->
                        if (suc.id == idSucursal) suc.copy(recursos = recursos)
                        else suc
                    }
                )
            }
            actual.copy(empresas = nuevasEmpresas)
        }
    }

    /**
     * 🔥 [ELITE]: Inserta o actualiza un recurso individual en el borrador.
     */
    fun upsertRecurso(idSucursal: String, recurso: RecursoDominio) {
        _borrador.update { actual ->
            if (actual == null) return@update null
            val nuevasEmpresas = actual.empresas.map { emp ->
                emp.copy(
                    sucursales = emp.sucursales.map { suc ->
                        if (suc.id == idSucursal) {
                            val lista = suc.recursos.toMutableList()
                            val index = lista.indexOfFirst { it.id == recurso.id }
                            if (index != -1) lista[index] = recurso else lista.add(recurso)
                            suc.copy(recursos = lista)
                        } else suc
                    }
                )
            }
            actual.copy(empresas = nuevasEmpresas)
        }
    }

    /**
     * 🔥 [ELITE]: Inserta o actualiza un miembro del equipo individual en el borrador.
     */
    fun upsertEquipo(idSucursal: String, persona: EquipoTrabajoDominio) {
        _borrador.update { actual ->
            if (actual == null) return@update null
            val nuevasEmpresas = actual.empresas.map { emp ->
                emp.copy(
                    sucursales = emp.sucursales.map { suc ->
                        if (suc.id == idSucursal) {
                            val lista = suc.equipoTrabajo.toMutableList()
                            val index = lista.indexOfFirst { it.id == persona.id }
                            if (index != -1) lista[index] = persona else lista.add(persona)
                            suc.copy(equipoTrabajo = lista)
                        } else suc
                    }
                )
            }
            actual.copy(empresas = nuevasEmpresas)
        }
    }

    fun obtenerBorradorMaestro(): PerfilPrestadorDeepModelo? = _borrador.value
}













































