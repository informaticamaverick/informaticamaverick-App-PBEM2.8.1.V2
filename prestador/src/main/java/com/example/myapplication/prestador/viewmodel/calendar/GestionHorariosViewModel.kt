package com.example.myapplication.prestador.viewmodel.calendar

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.dominio.modelos.HorarioDominio
import com.example.myapplication.core.dominio.modelos.RangoHorarioDominio
import com.example.myapplication.prestador.datos.gestores.BorradorPerfilPrestadorGestor
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * --- VIEWMODEL DE GESTIÓN DE HORARIOS MAVERICK (ELITE 2026) ---
 * Gestiona el Horario Base de la sucursal o prestador utilizando el sistema de borradores.
 * [LEY #9]: Estándar en Español.
 * [SOBERANÍA]: Los cambios viven en RAM (GestorBorrador) hasta el commit final.
 */
@HiltViewModel
class GestionHorariosViewModel @Inject constructor(
    private val gestorBorrador: BorradorPerfilPrestadorGestor,
    private val auth: FirebaseAuth,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val TAG = "GestionHorariosVM"

    // ID de la sucursal o prestador a configurar
    private val idReferencia: String
        get() = savedStateHandle.get<String>("owner_id") ?: auth.currentUser?.uid ?: ""

    private val tipoConfig: String
        get() = savedStateHandle.get<String>("type") ?: "ATENCION"

    /**
     * Flujo reactivo del horario base extraído del borrador global.
     */
    val horarioBase: StateFlow<HorarioDominio?> = gestorBorrador.borrador.map { maestro ->
        if (maestro == null) return@map null
        
        // 1. Buscar en prestador raíz
        if (maestro.prestador.perfil.id == idReferencia) {
            return@map maestro.prestador.horario
        }

        // 2. Buscar en sucursales, recursos y equipo
        val todasSucursales = maestro.empresas.flatMap { it.sucursales }
        
        val horarioSucursal = todasSucursales.find { it.id == idReferencia }?.horario
        if (horarioSucursal != null) return@map horarioSucursal

        val horarioEquipo = todasSucursales.flatMap { it.equipoTrabajo }.find { it.id == idReferencia }?.horario
        if (horarioEquipo != null) return@map horarioEquipo

        val horarioRecurso = todasSucursales.flatMap { it.recursos }.find { it.id == idReferencia }?.horario
        if (horarioRecurso != null) return@map horarioRecurso

        null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _estadoUi = MutableStateFlow<EstadoDisponibilidadUi>(EstadoDisponibilidadUi.Reposo)
    val estadoUi: StateFlow<EstadoDisponibilidadUi> = _estadoUi.asStateFlow()

    /**
     * Añade un nuevo rango horario al borrador.
     */
    fun añadirRango(diaSemana: Int, inicio: String, fin: String) {
        val idRef = idReferencia
        if (idRef.isBlank()) return

        try {
            val actual = horarioBase.value ?: HorarioDominio()
            
            val nuevoRango = RangoHorarioDominio(inicio, fin)

            val actualizado = when (diaSemana) {
                1 -> actual.copy(lunes = actual.lunes + nuevoRango)
                2 -> actual.copy(martes = actual.martes + nuevoRango)
                3 -> actual.copy(miercoles = actual.miercoles + nuevoRango)
                4 -> actual.copy(jueves = actual.jueves + nuevoRango)
                5 -> actual.copy(viernes = actual.viernes + nuevoRango)
                6 -> actual.copy(sabado = actual.sabado + nuevoRango)
                7 -> actual.copy(domingo = actual.domingo + nuevoRango)
                else -> actual
            }

            gestorBorrador.actualizarHorario(idRef, actualizado)
            
            Log.d(TAG, "📝 [DRAFT_UPDATE] Rango añadido al borrador (Día $diaSemana) para $idRef")
        } catch (e: Exception) {
            _estadoUi.value = EstadoDisponibilidadUi.Error("Error al actualizar borrador")
        }
    }

    /**
     * Limpia los rangos de un día en el borrador.
     */
    fun limpiarDia(diaSemana: Int) {
        val actual = horarioBase.value ?: return
        val actualizado = when (diaSemana) {
            1 -> actual.copy(lunes = emptyList())
            2 -> actual.copy(martes = emptyList())
            3 -> actual.copy(miercoles = emptyList())
            4 -> actual.copy(jueves = emptyList())
            5 -> actual.copy(viernes = emptyList())
            6 -> actual.copy(sabado = emptyList())
            7 -> actual.copy(domingo = emptyList())
            else -> actual
        }
        gestorBorrador.actualizarHorario(idReferencia, actualizado)
    }

    /**
     * Elimina un rango específico en el borrador.
     */
    fun eliminarRango(diaSemana: Int, rango: RangoHorarioDominio) {
        val actual = horarioBase.value ?: return
        val actualizado = when (diaSemana) {
            1 -> actual.copy(lunes = actual.lunes - rango)
            2 -> actual.copy(martes = actual.martes - rango)
            3 -> actual.copy(miercoles = actual.miercoles - rango)
            4 -> actual.copy(jueves = actual.jueves - rango)
            5 -> actual.copy(viernes = actual.viernes - rango)
            6 -> actual.copy(sabado = actual.sabado - rango)
            7 -> actual.copy(domingo = actual.domingo - rango)
            else -> actual
        }
        gestorBorrador.actualizarHorario(idReferencia, actualizado)
    }

    /**
     * Aplica una lista de rangos masivamente al borrador.
     */
    fun aplicarHorarioADias(dias: List<Int>, rangos: List<RangoHorarioDominio>) {
        val idRef = idReferencia
        if (idRef.isBlank() || dias.isEmpty()) return

        val actual = horarioBase.value ?: HorarioDominio()

        var final = actual
        dias.forEach { dia ->
            final = when(dia) {
                1 -> final.copy(lunes = rangos)
                2 -> final.copy(martes = rangos)
                3 -> final.copy(miercoles = rangos)
                4 -> final.copy(jueves = rangos)
                5 -> final.copy(viernes = rangos)
                6 -> final.copy(sabado = rangos)
                7 -> final.copy(domingo = rangos)
                else -> final
            }
        }

        gestorBorrador.actualizarHorario(idRef, final)
    }

    /**
     * Copia la configuración en el borrador.
     */
    fun copiarHorarioADias(diaOrigen: Int, diasDestino: List<Int>) {
        val base = horarioBase.value ?: return
        val rangosOrigen = when(diaOrigen) {
            1 -> base.lunes
            2 -> base.martes
            3 -> base.miercoles
            4 -> base.jueves
            5 -> base.viernes
            6 -> base.sabado
            7 -> base.domingo
            else -> emptyList()
        }
        aplicarHorarioADias(diasDestino, rangosOrigen)
    }

    fun hayCambios(): Boolean = gestorBorrador.hayCambiosPendientes()

    fun revertirCambios() {
        gestorBorrador.descartarCambios()
    }

    sealed interface EstadoDisponibilidadUi {
        object Reposo : EstadoDisponibilidadUi
        object Cargando : EstadoDisponibilidadUi
        data class Exito(val mensaje: String) : EstadoDisponibilidadUi
        data class Error(val mensaje: String) : EstadoDisponibilidadUi
    }
}














































