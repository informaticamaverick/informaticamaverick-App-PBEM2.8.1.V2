package com.example.myapplication.prestador.viewmodel.empresa.turnos

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.dao.RecursoDao
import com.example.myapplication.core.datos.local.dao.EquipoTrabajoDao
import com.example.myapplication.core.datos.repositorios.InventarioRepositorio
import com.example.myapplication.core.dominio.modelos.InventarioActivoDominio
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.core.dominio.modelos.TipoActivo
import com.example.myapplication.core.dominio.mapeadores.RecursoMappers
import com.example.myapplication.core.dominio.mapeadores.EquipoTrabajoMappers
import com.example.myapplication.prestador.ui.pantallas.empresa.turnos.componentes.SlotTurnoSimulado
import com.example.myapplication.prestador.datos.gestores.BorradorPerfilPrestadorGestor
import com.example.myapplication.prestador.datos.repositorios.PerfilPrestadorDeepRepositorio
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- VIEWMODEL: GESTIÓN DE TURNOS (v2026.SUPREME) ---
 * [PROPÓSITO]: Orquestar la gestión corporativa multi-pestaña usando el Borrador SSOT.
 * [LEY #9]: Estándar Maverick en Español.
 */
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class GestionTurnosViewModel @Inject constructor(
    private val deepRepo: PerfilPrestadorDeepRepositorio,
    private val gestorBorrador: BorradorPerfilPrestadorGestor,
    private val recursoDao: RecursoDao,
    private val equipoDao: EquipoTrabajoDao,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(GestionTurnosUiState())
    val uiState: StateFlow<GestionTurnosUiState> = _uiState.asStateFlow()

    init {
        observarBorrador()
    }

    /**
     * Observa el Borrador como Fuente Única de Verdad (RAM).
     */
    private fun observarBorrador() {
        viewModelScope.launch {
            gestorBorrador.borrador.collectLatest { maestro ->
                maestro?.let { m ->
                    val identidades = m.aModelosUi()
                    val sucursales = identidades.filter { iden -> iden.tipo.name == "SUCURSAL" }
                    
                    val sucActual = _uiState.value.sucursalSeleccionada ?: sucursales.firstOrNull()
                    
                    val inventarioBorrador = if (sucActual != null) {
                        val sucDeep = m.empresas.flatMap { e -> e.sucursales }.find { s -> s.id == sucActual.id }
                        val recursos = sucDeep?.recursos ?: emptyList()
                        val equipo = sucDeep?.equipoTrabajo ?: emptyList()
                        
                        recursos.map { r -> 
                            InventarioActivoDominio(
                                id = r.id,
                                nombre = r.nombre,
                                tipo = TipoActivo.RECURSO,
                                habilitado = r.estaActivo,
                                categoria = r.tipoRecurso,
                                subTitulo = r.tipoRecurso,
                                idSucursal = r.idSucursal,
                                equipamiento = r.descripcion,
                                horario = r.horario
                            )
                        } + equipo.map { e ->
                            InventarioActivoDominio(
                                id = e.id,
                                nombre = e.nombreCompleto,
                                tipo = TipoActivo.EQUIPO,
                                habilitado = e.estaHabilitado,
                                categoria = e.cargo,
                                subTitulo = e.cargo,
                                idSucursal = e.idSucursal,
                                especialidad = e.cargo,
                                horario = e.horario,
                                idRecursoVinculado = e.idRecursoVinculado
                            )
                        }
                    } else emptyList()

                    _uiState.update { state ->
                        state.copy(
                            sucursales = sucursales,
                            sucursalSeleccionada = sucActual,
                            inventario = inventarioBorrador,
                            recursosListos = inventarioBorrador.count { it.tipo == TipoActivo.RECURSO && it.habilitado },
                            staffActivo = inventarioBorrador.count { it.tipo == TipoActivo.EQUIPO && it.habilitado },
                            ocupacionHoy = (30..85).random()
                        )
                    }
                }
            }
        }
    }

    fun seleccionarSucursal(sucursal: PrestadorDominio) {
        _uiState.update { it.copy(sucursalSeleccionada = sucursal) }
    }

    fun seleccionarTab(index: Int) {
        _uiState.update { it.copy(tabSeleccionada = index) }
    }

    fun seleccionarFiltroResumen(filtro: TipoFiltroResumen) {
        _uiState.update { it.copy(filtroResumen = filtro) }
    }

    fun toggleExpansionStaff(idStaff: String) {
        _uiState.update { state ->
            val yaExpandido = state.idStaffExpandido == idStaff
            state.copy(idStaffExpandido = if (yaExpandido) null else idStaff)
        }
    }

    fun cambiarFecha(nuevaFecha: java.time.LocalDate) {
        _uiState.update { it.copy(fechaSeleccionada = nuevaFecha, mostrarDatePicker = false) }
        recalcularMetricasParaFecha(nuevaFecha)
    }

    fun toggleDatePicker(visible: Boolean) {
        _uiState.update { it.copy(mostrarDatePicker = visible) }
    }

    private fun recalcularMetricasParaFecha(fecha: java.time.LocalDate) {
        val inventario = _uiState.value.inventario
        val diaSemana = fecha.dayOfWeek.value // 1-7
        
        _uiState.update { state ->
            state.copy(
                staffActivo = inventario.count { it.tipo == TipoActivo.EQUIPO && it.habilitado && (it.horario?.estaDisponibleElDia(diaSemana) ?: true) },
                recursosListos = inventario.count { it.tipo == TipoActivo.RECURSO && it.habilitado && (it.horario?.estaDisponibleElDia(diaSemana) ?: true) },
                ocupacionHoy = if (diaSemana > 5) (10..40).random() else (40..90).random()
            )
        }
    }

    private fun com.example.myapplication.core.dominio.modelos.HorarioDominio.estaDisponibleElDia(dia: Int): Boolean {
        return when(dia) {
            1 -> lunes.isNotEmpty()
            2 -> martes.isNotEmpty()
            3 -> miercoles.isNotEmpty()
            4 -> jueves.isNotEmpty()
            5 -> viernes.isNotEmpty()
            6 -> sabado.isNotEmpty()
            7 -> domingo.isNotEmpty()
            else -> false
        }
    }

    fun actualizarBusqueda(query: String) {
        _uiState.update { it.copy(busqueda = query) }
    }

    fun alternarActivo(item: InventarioActivoDominio) {
        val sucursalId = _uiState.value.sucursalSeleccionada?.id ?: return
        if (item.tipo == TipoActivo.RECURSO) {
            val actual = gestorBorrador.borrador.value?.empresas?.flatMap { it.sucursales }?.find { it.id == sucursalId }?.recursos?.find { it.id == item.id } ?: return
            gestorBorrador.upsertRecurso(sucursalId, actual.copy(estaActivo = !actual.estaActivo))
        } else {
            val actual = gestorBorrador.borrador.value?.empresas?.flatMap { it.sucursales }?.find { it.id == sucursalId }?.equipoTrabajo?.find { it.id == item.id } ?: return
            gestorBorrador.upsertEquipo(sucursalId, actual.copy(estaHabilitado = !actual.estaHabilitado))
        }
    }

    fun guardarRecurso(entidad: com.example.myapplication.core.datos.local.entidades.RecursoEntity) {
        val sucursalId = _uiState.value.sucursalSeleccionada?.id ?: return
        val uid = auth.currentUser?.uid ?: return
        
        val borradorActual = gestorBorrador.borrador.value
        val recursoPrevio = borradorActual?.empresas?.flatMap { it.sucursales }?.flatMap { it.recursos }?.find { it.id == entidad.id }
        val horarioActual = recursoPrevio?.horario ?: com.example.myapplication.core.dominio.modelos.HorarioDominio()

        val entidadVinculada = entidad.copy(idSucursal = sucursalId, idPropietario = uid)
        val modelo = RecursoMappers.deEntidadAModelo(entidadVinculada).copy(
            horario = horarioActual,
            tipoHorario = com.example.myapplication.core.datos.local.entidades.TipoHorario.Horario_DisponibilidadTurnos
        )
        
        gestorBorrador.upsertRecurso(sucursalId, modelo)
        
        if (recursoPrevio?.horario == null) {
            gestorBorrador.actualizarHorario(modelo.id, horarioActual)
        }
        
        cerrarEditores()
    }

    fun guardarEquipo(entidad: com.example.myapplication.core.datos.local.entidades.EquipoTrabajoEntity) {
        val sucursalId = _uiState.value.sucursalSeleccionada?.id ?: return
        val uid = auth.currentUser?.uid ?: return
        
        val borradorActual = gestorBorrador.borrador.value
        val equipoPrevio = borradorActual?.empresas?.flatMap { it.sucursales }?.flatMap { it.equipoTrabajo }?.find { it.id == entidad.id }
        val horarioActual = equipoPrevio?.horario ?: com.example.myapplication.core.dominio.modelos.HorarioDominio()

        val entidadVinculada = entidad.copy(idSucursal = sucursalId, idPropietario = uid)
        val modelo = EquipoTrabajoMappers.deEntidadAModelo(entidadVinculada).copy(
            horario = horarioActual,
            tipoHorario = com.example.myapplication.core.datos.local.entidades.TipoHorario.Horario_DisponibilidadTurnos
        )
        
        gestorBorrador.upsertEquipo(sucursalId, modelo)
        
        if (equipoPrevio?.horario == null) {
            gestorBorrador.actualizarHorario(modelo.id, horarioActual)
        }
        
        cerrarEditores()
    }

    fun confirmarCambiosGlobales() {
        val maestro = gestorBorrador.obtenerBorradorMaestro() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true) }
            try {
                deepRepo.guardarEcosistemaLocal(maestro)
                gestorBorrador.consolidarEdicion()
                _uiState.update { it.copy(snackbarMensaje = "¡Cambios guardados con éxito!", estaCargando = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMensaje = "Error al guardar: ${e.message}", estaCargando = false) }
            }
        }
    }

    fun eliminarActivo(item: InventarioActivoDominio) {
        val sucursalId = _uiState.value.sucursalSeleccionada?.id ?: return
        if (item.tipo == TipoActivo.RECURSO) {
            val lista = gestorBorrador.borrador.value?.empresas?.flatMap { it.sucursales }?.find { it.id == sucursalId }?.recursos?.filter { it.id != item.id } ?: return
            gestorBorrador.actualizarRecursosSucursal(sucursalId, lista)
        } else {
            val lista = gestorBorrador.borrador.value?.empresas?.flatMap { it.sucursales }?.find { it.id == sucursalId }?.equipoTrabajo?.filter { it.id != item.id } ?: return
            gestorBorrador.actualizarEquipoSucursal(sucursalId, lista)
        }
        _uiState.update { it.copy(snackbarMensaje = "Activo eliminado del borrador") }
    }

    fun obtenerSlotsTurno(activo: InventarioActivoDominio): List<SlotTurnoSimulado> {
        val slots = mutableListOf<SlotTurnoSimulado>()
        val horario = activo.horario ?: return emptyList<SlotTurnoSimulado>()
        
        val fecha = _uiState.value.fechaSeleccionada
        val diaSemana = fecha.dayOfWeek.value
        
        val rangosHoy = when(diaSemana) {
            1 -> horario.lunes
            2 -> horario.martes
            3 -> horario.miercoles
            4 -> horario.jueves
            5 -> horario.viernes
            6 -> horario.sabado
            7 -> horario.domingo
            else -> emptyList()
        }
        
        if (rangosHoy.isEmpty()) return emptyList()

        val duracion = 20
        val receso = 5
        val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")

        rangosHoy.forEach { rango ->
            try {
                var horaActual = java.time.LocalTime.parse(rango.inicio)
                val limite = java.time.LocalTime.parse(rango.fin)
                
                var count = 0
                while (horaActual.plusMinutes(duracion.toLong()).isBefore(limite) || horaActual.plusMinutes(duracion.toLong()) == limite) {
                    slots.add(SlotTurnoSimulado(
                        hora = horaActual.format(formatter),
                        ocupado = count % 4 == 0 
                    ))
                    horaActual = horaActual.plusMinutes((duracion + receso).toLong())
                    count++
                }
            } catch (e: Exception) {}
        }
        
        return slots
    }

    fun abrirEditorRecurso(id: String? = null) {
        viewModelScope.launch {
            val entidad = if (id != null) recursoDao.obtenerPorIdSync(id) else null
            _uiState.update { it.copy(recursoEnEdicion = entidad, mostrarEditorRecurso = true) }
        }
    }

    fun abrirEditorEquipo(id: String? = null) {
        viewModelScope.launch {
            val entidad = if (id != null) {
                gestorBorrador.borrador.value?.empresas?.flatMap { e -> e.sucursales }
                    ?.flatMap { s -> s.equipoTrabajo }?.find { it.id == id }?.let { modelo ->
                        com.example.myapplication.core.datos.local.entidades.EquipoTrabajoEntity(
                            id = modelo.id,
                            idPropietario = auth.currentUser?.uid ?: "",
                            idSucursal = modelo.idSucursal,
                            nombre = modelo.nombreCompleto.substringBefore(" "),
                            apellido = modelo.nombreCompleto.substringAfter(" ", ""),
                            cargo = modelo.cargo,
                            estaHabilitado = modelo.estaHabilitado,
                            idRecursoVinculado = modelo.idRecursoVinculado
                        )
                    }
            } else null
            _uiState.update { it.copy(equipoEnEdicion = entidad, mostrarEditorEquipo = true) }
        }
    }

    fun cerrarEditores() {
        _uiState.update { it.copy(mostrarEditorRecurso = false, mostrarEditorEquipo = false, recursoEnEdicion = null, equipoEnEdicion = null) }
    }

    fun limpiarSnackbar() {
        _uiState.update { it.copy(snackbarMensaje = null) }
    }
}


