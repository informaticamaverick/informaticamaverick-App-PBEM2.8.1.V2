package com.example.myapplication.prestador.datos.repositorios

import androidx.paging.PagingData
import com.example.myapplication.prestador.datos.local.dao.PresupuestoDao
import com.example.myapplication.prestador.datos.local.entidades.PresupuestoEntity
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.core.datos.repositorios.PresupuestoRepositorio
import com.example.myapplication.core.datos.repositorios.ConcursoPublicoRepositorio
import com.example.myapplication.prestador.datos.local.dao.ProductoDao
import com.example.myapplication.core.datos.local.entidades.TipoProducto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE PRESUPUESTOS PRESTADOR (ELITE v2026.FINAL) ---
 * 
 * [PROPÓSITO]: Gestionar las licitaciones y presupuestos desde la perspectiva del profesional.
 * [LEY #9]: Nomenclatura 100% en español.
 */
@Singleton
class PrestadorPresupuestoRepositorio @Inject constructor(
    private val presupuestoDao: PresupuestoDao,
    private val productoDao: ProductoDao,
    private val metadataDao: com.example.myapplication.core.datos.local.dao.AppMetadataDao,
    private val cuentaDao: com.example.myapplication.core.datos.local.dao.CuentaDao,
    private val sharedPresupuestoRepositorio: PresupuestoRepositorio,
    private val concursoPublicoRepositorio: ConcursoPublicoRepositorio,
    private val motorCierre: javax.inject.Provider<com.example.myapplication.prestador.dominio.motores.MotorCierreComercial> // Inyección diferida para evitar ciclos
) {
    val todasLasLicitaciones: Flow<List<com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity>> = concursoPublicoRepositorio.todosLosConcursos
    val todosLosPresupuestos: Flow<List<PresupuestoEntity>> = presupuestoDao.obtenerTodos()

    suspend fun actualizarEstadoPresupuesto(id: String, estado: EstadoPresupuesto) {
        presupuestoDao.actualizarEstado(id, estado)
        motorCierre.get().procesarCierrePresupuesto(id, estado)
    }
    
    /**
     * 🔥 [ELITE]: Presupuestos Finales (Mesa). 
     * Estos son los documentos inmutables ya enviados al cliente.
     */
    val todosLosPresupuestosFinales: Flow<List<PresupuestoFinalEntity>> = sharedPresupuestoRepositorio.todosLosPresupuestos

    fun obtenerCatalogoArticulos(): Flow<com.example.myapplication.core.datos.local.entidades.AppMetadataEntity?> = 
        metadataDao.obtenerMetadataFlujo("article_catalog")

    fun obtenerCatalogoServicios(): Flow<com.example.myapplication.core.datos.local.entidades.AppMetadataEntity?> = 
        metadataDao.obtenerMetadataFlujo("service_catalog")

    fun obtenerCatalogoHonorarios(): Flow<com.example.myapplication.core.datos.local.entidades.AppMetadataEntity?> = 
        metadataDao.obtenerMetadataFlujo("fee_catalog")

    suspend fun guardarMetadata(metadata: com.example.myapplication.core.datos.local.entidades.AppMetadataEntity) {
        metadataDao.guardarMetadata(metadata)
    }

    fun obtenerMercadoPaginado(cp: String, categorias: List<String>): Flow<PagingData<ConcursoPublicoEntity>> {
        return concursoPublicoRepositorio.obtenerMercadoPaginado(cp, categorias)
    }

    // --- SECTOR: GESTIÓN DE BORRADORES ---

    suspend fun guardarBorrador(borrador: com.example.myapplication.prestador.datos.local.entidades.BorradorPresupuestoEntity) {
        presupuestoDao.guardarBorrador(borrador)
    }

    suspend fun obtenerBorrador(id: String): com.example.myapplication.prestador.datos.local.entidades.BorradorPresupuestoEntity? =
        presupuestoDao.obtenerBorrador(id)

    fun obtenerBorradorConItems(id: String): Flow<com.example.myapplication.prestador.datos.local.entidades.relaciones.BorradorCocinaConItems?> =
        presupuestoDao.obtenerBorradorConItems(id)

    fun obtenerPresupuestoCocinaConItems(id: String): Flow<com.example.myapplication.prestador.datos.local.entidades.relaciones.PresupuestoCocinaConItems?> =
        presupuestoDao.obtenerConItems(id)

    suspend fun eliminarBorrador(id: String) {
        presupuestoDao.eliminarBorrador(id)
    }

    suspend fun enviarPresupuesto(presupuesto: PresupuestoEntity, idBorradorOrig: String? = null, etiquetaManoObra: String = "MANO DE OBRA"): PresupuestoConItems {
        val cuenta = cuentaDao.obtenerPorIdSync(presupuesto.idPrestador)
        
        // 🔥 [ELITE v2026]: Verificación de Membresía (Sin crash)
        if (cuenta != null && !cuenta.estaSuscrito) {
            android.util.Log.w("BudgetRepo", "⚠️ [ELITE_REQUIRED] Intento de envío sin suscripción activa.")
            // Lanzamos una excepción controlada para que el ViewModel orqueste el Paywall
            throw SecurityException("MEMBERSHIP_REQUIRED")
        }

        // 1. Migrar ítems del borrador al presupuesto real si es necesario
        if (idBorradorOrig != null) {
            val itemsBorrador = productoDao.obtenerProductosPorBorrador(idBorradorOrig)
            val itemsMigrados = itemsBorrador.map { it.copy(id = java.util.UUID.randomUUID().toString(), idBorrador = null, idPresupuesto = presupuesto.idPresupuesto) }
            productoDao.insertarProductos(itemsMigrados)
        }

        // 2. Guardar localmente en la cocina privada
        presupuestoDao.insertarPresupuesto(presupuesto)
        
        // 3. Obtener los productos vinculados a este presupuesto local
        val productosLocales = productoDao.obtenerProductosPorPresupuesto(presupuesto.idPresupuesto)

        // 3. Convertir a Snapshot Final (Mesa) usando el Mappers SUPREME
        val finalRelacion = com.example.myapplication.core.dominio.mapeadores.SnapshotFinancieroMappers.crearSnapshotFinal(
            idPresupuesto = presupuesto.idPresupuesto,
            idCliente = presupuesto.idCliente,
            idPrestador = presupuesto.idPrestador,
            idConcurso = presupuesto.idConcurso,
            titulo = presupuesto.tituloTrabajo ?: "Presupuesto de Servicio",
            subtotal = presupuesto.subtotal,
            total = presupuesto.totalGeneral,
            idCategoria = presupuesto.idCategoria,
            nombrePrestador = presupuesto.nombrePrestador,
            fotoPrestador = presupuesto.urlFotoPrestador,
            articulos = productosLocales.filter { it.tipo == TipoProducto.PRODUCTO }.map { 
                ArticuloPresupuesto(idProducto = it.id, descripcion = it.nombre, cantidad = it.cantidad, precioUnitario = it.precioVenta, precioCosto = it.precioCosto, porcentajeImpuesto = it.impuestoDefault, porcentajeDescuento = it.descuentoDefault) 
            },
            servicios = productosLocales.filter { it.tipo == TipoProducto.SERVICIO }.map { 
                ServicioPresupuesto(idProducto = it.id, descripcion = it.nombre, precioUnitario = it.precioVenta, porcentajeDescuento = it.descuentoDefault) 
            },
            gastos = productosLocales.filter { it.tipo == TipoProducto.GASTO }.map { 
                GastoVarioPresupuesto(descripcion = it.nombre, precioUnitario = it.precioVenta, porcentajeDescuento = it.descuentoDefault) 
            },
            impuestos = emptyList(), // TODO: Cargar si existe desglose local
            tipo = presupuesto.tipo,
            etiquetaManoObra = etiquetaManoObra
        )

        val finalHeader = finalRelacion.cabecera.copy(
            numeroPresupuesto = presupuesto.numeroPresupuesto,
            diasValidez = presupuesto.diasValidez,
            notas = presupuesto.notas,
            metodosPago = presupuesto.metodosPago,
            urlMiniatura = presupuesto.urlMiniatura,
            tipo = presupuesto.tipo,
            estado = com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto.valueOf(presupuesto.estado.name),
            marcaTiempo = presupuesto.marcaTiempo
        )

        // 4. Enviar al repositorio compartido (Final)
        sharedPresupuestoRepositorio.enviarPresupuesto(finalHeader, finalRelacion.lineas, finalRelacion.finanzas)

        return finalRelacion.copy(cabecera = finalHeader)
    }
}





