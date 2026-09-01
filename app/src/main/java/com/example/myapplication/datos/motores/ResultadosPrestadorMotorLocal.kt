package com.example.myapplication.datos.motores

import androidx.room.withTransaction
import com.example.myapplication.core.datos.local.AppDatabase
import com.example.myapplication.core.datos.local.entidades.RelacionBusquedaEntity
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.modelos.descubrimiento.ResultadoIndiceBusquedaShallowDominio
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- MOTOR LOCAL DE RESULTADOS (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Punto único de escritura para identidades encontradas en búsqueda.
 * [LEY #17]: Protocolo de Bautizo. Especializado en el cliente.
 */
@Singleton
class ResultadosPrestadorMotorLocal @Inject constructor(
    private val db: AppDatabase
) {

    /**
     * Guarda un conjunto de resultados vinculándolos a una consulta específica.
     */
    suspend fun guardarResultados(idConsulta: String, items: List<ResultadoIndiceBusquedaShallowDominio>) {
        android.util.Log.d("MOTOR_LOCAL", "💾 [PERSISTENCIA_INICIO] Guardando ${items.size} resultados para la consulta: $idConsulta")
        db.withTransaction {
            val relaciones = mutableListOf<RelacionBusquedaEntity>()
            
            items.forEachIndexed { index, item ->
                // 1. Guardar la identidad física en las tablas de Core
                impactarIdentidadFisica(item)
                
                // 2. Crear la relación soberana de búsqueda
                relaciones.add(
                    RelacionBusquedaEntity(
                        idConsulta = idConsulta,
                        idPrestador = item.id,
                        ordenRanking = index
                    )
                )
            }
            
            db.resultadoBusquedaPrestadorDao().insertarRelaciones(relaciones)
        }
        android.util.Log.d("MOTOR_LOCAL", "✅ [PERSISTENCIA_OK] Room actualizado con éxito.")
    }

    private suspend fun impactarIdentidadFisica(item: ResultadoIndiceBusquedaShallowDominio) {
        android.util.Log.d("MOTOR_LOCAL", "👤 [IMPACTO_PERFIL] ID: ${item.id} | Tipo: ${item.tipoIdentidad} | Nome: ${item.nombreVisible}")
        // --- SOBERANÍA DE CUENTA ---
        val cuentaLocal = db.CuentaDao().obtenerPorId(item.idPropietario).first()
        if (cuentaLocal == null || cuentaLocal.estaSuscrito != item.estaSuscrito) {
            db.CuentaDao().insertar(
                (cuentaLocal ?: CuentaEntity(id = item.idPropietario)).copy(
                    estaSuscrito = item.estaSuscrito
                )
            )
        }

        // --- UBICACIÓN ---
        if (item.calle.isNotBlank()) {
            db.direccionDao().insertar(
                DireccionEntity(
                    id = "dir_${item.id}",
                    idReferencia = item.id,
                    idPropietario = item.idPropietario,
                    calle = item.calle,
                    numero = item.numero,
                    codigoPostal = item.codigoPostal,
                    latitud = item.latitud,
                    longitud = item.longitud
                )
            )
        }

        // --- IDENTIDAD ESPECÍFICA ---
        if (item.tipoIdentidad == "PRESTADOR") {
            val existente = db.prestadorDao().obtenerPorId(item.id).first()
            db.prestadorDao().insertar(
                (existente ?: IdentidadPrestadorEntity(id = item.id)).copy(
                    nombreVisible = item.nombreVisible,
                    miniaturaBase64 = item.miniaturaBase64,
                    reputacion = item.reputacion,
                    totalReseñas = item.totalReseñas,
                    trabajosRealizados = item.trabajosRealizados,
                    estaVerificado = item.estaVerificado,
                    estaEnLinea = item.estaEnLinea,
                    atiende24Horas = item.atiende24h,
                    realizaEnvios = item.realizaEnvios,
                    visitaADomicilio = item.visitaADomicilio,
                    brindaServicio = item.brindaServicio,
                    brindaProducto = item.brindaProducto,
                    brindaTurnos = item.brindaTurnos,
                    tieneLocalFisico = item.tieneLocalFisico,
                    idCategorias = item.idCategorias
                )
            )
        } else if (item.tipoIdentidad == "SUCURSAL") {
            val idEmpresa = item.idPadre ?: ""
            if (idEmpresa.isNotBlank()) {
                val existenteEmp = db.empresaDao().obtenerPorId(idEmpresa).first()
                db.empresaDao().insertarEmpresa(
                    (existenteEmp ?: EmpresaEntity(id = idEmpresa, idPropietario = item.idPropietario)).copy(
                        nombre = item.nombreEmpresa ?: "Empresa",
                        miniaturaBase64 = item.miniaturaBase64,
                        urlFoto = item.urlFoto,
                        idCategorias = item.idCategorias
                    )
                )
            }

            val existenteSuc = db.sucursalDao().obtenerPorId(item.id).first()
            db.sucursalDao().insertarSucursal(
                (existenteSuc ?: SucursalEntity(id = item.id, idEmpresaPadre = idEmpresa, idPropietario = item.idPropietario)).copy(
                    nombre = item.nombreVisible,
                    reputacion = item.reputacion,
                    totalReseñas = item.totalReseñas,
                    trabajosRealizados = item.trabajosRealizados,
                    estaEnLinea = item.estaEnLinea,
                    atiende24Horas = item.atiende24h,
                    realizaEnvios = item.realizaEnvios,
                    visitaADomicilio = item.visitaADomicilio,
                    brindaServicio = item.brindaServicio,
                    brindaProducto = item.brindaProducto,
                    brindaTurnos = item.brindaTurnos
                )
            )
        }
    }
}

