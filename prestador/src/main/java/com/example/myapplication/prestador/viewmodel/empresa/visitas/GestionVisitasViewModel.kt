package com.example.myapplication.prestador.viewmodel.empresa.visitas

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.dominio.modelos.InventarioActivoDominio
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.core.dominio.modelos.TipoActivo
import com.example.myapplication.core.dominio.mapeadores.EquipoTrabajoMappers
import com.example.myapplication.prestador.datos.gestores.BorradorPerfilPrestadorGestor
import com.example.myapplication.prestador.datos.repositorios.PerfilPrestadorDeepRepositorio
import com.example.myapplication.prestador.ui.pantallas.empresa.turnos.componentes.SlotTurnoSimulado
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- VIEWMODEL: GESTIÓN DE VISITAS TÉCNICAS (v2026.SUPREME) ---
 */
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class GestionVisitasViewModel @Inject constructor(
    private val deepRepo: PerfilPrestadorDeepRepositorio,
    private val gestorBorrador: BorradorPerfilPrestadorGestor,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(GestionVisitasUiState())
    val uiState: StateFlow<GestionVisitasUiState> = _uiState.asStateFlow()

    init {
        observarBorrador()
    }

    private fun observarBorrador() {
        viewModelScope.launch {
            gestorBorrador.borrador.collectLatest { maestro ->
                maestro?.let { m ->
                    val identidades = m.aModelosUi()
                    val sucursales = identidades.filter { iden -> iden.tipo.name == "SUCURSAL" }
                    val sucActual = _uiState.value.sucursalSeleccionada ?: sucursales.firstOrNull()
                    
                    val tecnicosBorrador = if (sucActual != null) {
                        val sucDeep = m.empresas.flatMap { e -> e.sucursales }.find { s -> s.id == sucActual.id }
                        val equipo = sucDeep?.equipoTrabajo ?: emptyList()
                        
                        equipo.map { e ->
                            InventarioActivoDominio(
                                id = e.id,
                                nombre = e.nombreCompleto,
                                tipo = TipoActivo.EQUIPO,
                                habilitado = e.estaHabilitado,
                                categoria = e.cargo,
                                subTitulo = e.cargo,
                                idSucursal = e.idSucursal,
                                especialidad = e.cargo,
                                horario = e.horario
                            )
                        }
                    } else emptyList()

                    _uiState.update { state ->
                        state.copy(
                            sucursales = sucursales,
                            sucursalSeleccionada = sucActual,
                            tecnicos = tecnicosBorrador
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

    fun toggleExpansionTecnico(id: String) {
        _uiState.update { state ->
            val yaExpandido = state.idTecnicoExpandido == id
            state.copy(idTecnicoExpandido = if (yaExpandido) null else id)
        }
    }

    fun cambiarFecha(nuevaFecha: java.time.LocalDate) {
        _uiState.update { it.copy(fechaSeleccionada = nuevaFecha, mostrarDatePicker = false) }
    }

    fun toggleDatePicker(visible: Boolean) {
        _uiState.update { it.copy(mostrarDatePicker = visible) }
    }

    fun alternarHabilitacion(tecnico: InventarioActivoDominio) {
        val sucursalId = _uiState.value.sucursalSeleccionada?.id ?: return
        val actual = gestorBorrador.borrador.value?.empresas?.flatMap { it.sucursales }?.find { it.id == sucursalId }?.equipoTrabajo?.find { it.id == tecnico.id } ?: return
        gestorBorrador.upsertEquipo(sucursalId, actual.copy(estaHabilitado = !actual.estaHabilitado))
    }

    fun abrirEditor(id: String? = null) {
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
            _uiState.update { it.copy(tecnicoEnEdicion = entidad, mostrarEditorTecnico = true) }
        }
    }

    fun cerrarEditor() {
        _uiState.update { it.copy(mostrarEditorTecnico = false, tecnicoEnEdicion = null) }
    }

    fun guardarTecnico(entidad: com.example.myapplication.core.datos.local.entidades.EquipoTrabajoEntity) {
        val sucursalId = _uiState.value.sucursalSeleccionada?.id ?: return
        val uid = auth.currentUser?.uid ?: return
        
        val borradorActual = gestorBorrador.borrador.value
        val previo = borradorActual?.empresas?.flatMap { it.sucursales }?.flatMap { it.equipoTrabajo }?.find { it.id == entidad.id }
        val horarioActual = previo?.horario ?: com.example.myapplication.core.dominio.modelos.HorarioDominio()

        val entidadVinculada = entidad.copy(idSucursal = sucursalId, idPropietario = uid)
        val modelo = EquipoTrabajoMappers.deEntidadAModelo(entidadVinculada).copy(
            horario = horarioActual,
            tipoHorario = com.example.myapplication.core.datos.local.entidades.TipoHorario.Horario_DisponibilidadVisitas
        )
        
        gestorBorrador.upsertEquipo(sucursalId, modelo)
        
        if (previo?.horario == null) {
            gestorBorrador.actualizarHorario(modelo.id, horarioActual)
        }
        
        cerrarEditor()
    }

    fun confirmarCambiosGlobales() {
        val maestro = gestorBorrador.obtenerBorradorMaestro() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true) }
            try {
                deepRepo.guardarEcosistemaLocal(maestro)
                gestorBorrador.consolidarEdicion()
                _uiState.update { it.copy(snackbarMensaje = "¡Agenda de visitas guardada!", estaCargando = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMensaje = "Error: ${e.message}", estaCargando = false) }
            }
        }
    }

    fun obtenerSlotsTurno(tecnico: InventarioActivoDominio): List<SlotTurnoSimulado> {
        val slots = mutableListOf<SlotTurnoSimulado>()
        val horario = tecnico.horario ?: return emptyList<SlotTurnoSimulado>()
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

        val duracion = 60 // Visitas técnicas suelen ser más largas
        val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")

        rangosHoy.forEach { rango ->
            try {
                var horaActual = java.time.LocalTime.parse(rango.inicio)
                val limite = java.time.LocalTime.parse(rango.fin)
                var count = 0
                while (horaActual.plusMinutes(duracion.toLong()).isBefore(limite) || horaActual.plusMinutes(duracion.toLong()) == limite) {
                    slots.add(SlotTurnoSimulado(
                        hora = horaActual.format(formatter),
                        ocupado = count % 3 == 0 
                    ))
                    horaActual = horaActual.plusMinutes(duracion.toLong())
                    count++
                }
            } catch (e: Exception) {}
        }
        return slots
    }

    fun limpiarSnackbar() {
        _uiState.update { it.copy(snackbarMensaje = null) }
    }
}

