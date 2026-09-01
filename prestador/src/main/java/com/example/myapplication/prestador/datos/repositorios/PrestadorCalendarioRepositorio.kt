package com.example.myapplication.prestador.datos.repositorios

import com.example.myapplication.core.datos.local.dao.EventoDao
import com.example.myapplication.core.datos.local.dao.HorarioDao
import com.example.myapplication.core.datos.local.dao.ExcepcionHorariaDao
import com.example.myapplication.core.datos.local.entidades.EventoEntity
import com.example.myapplication.core.datos.local.entidades.EstadoEvento
import com.example.myapplication.core.datos.local.entidades.HorarioEntity
import com.example.myapplication.core.dominio.mapeadores.HorarioMappers
import com.example.myapplication.core.dominio.motores.CalculadoraDisponibilidad
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE CALENDARIO PRESTADOR (ELITE v2026.SUPREME) ---
 * [TÍTULO]: Repositorio de Calendario y Disponibilidad
 * [PROPÓSITO]: Centralizar la gestión de la agenda y el cálculo de huecos libres.
 */
@Singleton
class PrestadorCalendarioRepositorio @Inject constructor(
    private val eventoDao: EventoDao,
    private val horarioDao: HorarioDao,
    private val excepcionDao: ExcepcionHorariaDao,
    private val calculadora: CalculadoraDisponibilidad
) {
    fun obtenerAgendaSucursal(idSucursal: String, inicio: Long, fin: Long): Flow<List<EventoEntity>> =
        eventoDao.obtenerEventosDiaSucursal(idSucursal, inicio, fin)

    /**
     * 🔥 [ELITE]: Calcula la disponibilidad real en cascada.
     */
    suspend fun calcularDisponibilidadReal(
        idSucursal: String,
        idRecurso: String?,
        fechaMillis: Long
    ): List<CalculadoraDisponibilidad.BloqueHorario> {
        val fecha = Date(fechaMillis)
        
        // 1. Obtener Reglas (Entities)
        val horarioSucursalEntity = horarioDao.obtenerPorReferenciaSync(idSucursal) ?: return emptyList()
        val horarioRecursoEntity = if (idRecurso != null) horarioDao.obtenerPorReferenciaSync(idRecurso) else null
        
        // 2. Obtener Obstrucciones
        val excepciones = excepcionDao.obtenerPorReferencia(idSucursal).first()
        
        val inicioDia = Calendar.getInstance().apply { time = fecha; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }.timeInMillis
        val finDia = Calendar.getInstance().apply { time = fecha; set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59) }.timeInMillis
        
        val eventosDia = eventoDao.obtenerEventosDiaSucursal(idSucursal, inicioDia, finDia).first()
        val eventosFiltrados = if (idRecurso != null) eventosDia.filter { it.idRecurso == idRecurso } else eventosDia

        // 3. Mapear a Modelos de Dominio (Masticar)
        val horarioSucursal = HorarioMappers.deEntidadAModelo(horarioSucursalEntity)
        val horarioRecurso = horarioRecursoEntity?.let { HorarioMappers.deEntidadAModelo(it) }

        // 4. Ejecutar Motor SUPREME
        return calculadora.obtenerBloquesLibres(
            horarioSucursal = horarioSucursal,
            horarioRecurso = horarioRecurso,
            excepciones = excepciones,
            eventosOcupados = eventosFiltrados,
            fecha = fecha
        )
    }

    fun obtenerTodosLosEventos(idPropietario: String): Flow<List<EventoEntity>> =
        eventoDao.obtenerPorPropietarioSucursal(idPropietario)

    suspend fun agendarEventoInterno(evento: EventoEntity) {
        eventoDao.insertar(evento)
    }

    suspend fun actualizarEstado(idEvento: String, estado: EstadoEvento) {
        eventoDao.actualizarEstado(idEvento, estado)
    }

    suspend fun eliminarEvento(idEvento: String) {
        eventoDao.eliminarPorId(idEvento)
    }
}
