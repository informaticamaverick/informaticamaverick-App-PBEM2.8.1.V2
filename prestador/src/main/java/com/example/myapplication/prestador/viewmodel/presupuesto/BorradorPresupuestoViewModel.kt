package com.example.myapplication.prestador.viewmodel.presupuesto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.datos.local.dao.ProductoDao
import com.example.myapplication.core.datos.local.dao.IdentidadUsuarioDao
import com.example.myapplication.core.datos.local.dao.DireccionDao
import com.example.myapplication.core.datos.local.dao.ChatDao
import com.example.myapplication.core.datos.local.entidades.DireccionEntity
import com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity
import com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto
import com.example.myapplication.core.datos.local.entidades.TipoMensaje
import com.example.myapplication.core.datos.local.entidades.TipoPresupuesto
import com.example.myapplication.core.datos.local.entidades.TipoProducto
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.core.dominio.modelos.PerfilPrestadorDeepModelo
import com.example.myapplication.core.dominio.mapeadores.*
import com.example.myapplication.prestador.datos.local.entidades.*
import com.example.myapplication.prestador.datos.repositorios.PrestadorPresupuestoRepositorio
import com.example.myapplication.core.datos.repositorios.ConcursoPublicoRepositorio
import com.example.myapplication.core.datos.repositorios.CategoriaRepositorio
import com.example.myapplication.core.dominio.motores.CalculadoraPresupuesto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * --- SECCIONES DEL ARMADOR DE PRESUPUESTO ---
 */
enum class SeccionPresupuesto { IDENTIDAD, ITEMS, TOTALES }

/**
 * --- VIEWMODEL: GESTOR DE BORRADORES DE PRESUPUESTO (v2026.ELITE) ---
 * [PROPÓSITO]: Orquestar la creación profesional de presupuestos con auto-guardado.
 * [LEY #1]: Pantalla Tonta. Este VM centraliza todos los cálculos y búsquedas.
 */
@HiltViewModel
class BorradorPresupuestoViewModel @Inject constructor(
    private val presupuestoRepositorio: PrestadorPresupuestoRepositorio,
    private val concursoPublicoRepositorio: ConcursoPublicoRepositorio,
    private val productoDao: ProductoDao,
    private val usuarioDao: IdentidadUsuarioDao,
    private val direccionDao: DireccionDao,
    private val chatDao: ChatDao,
    private val chatRepositorio: com.example.myapplication.core.datos.repositorios.ChatMotorSincRepositorio,
    private val identidadRepo: com.example.myapplication.prestador.datos.repositorios.ConsultasPrestadorRepositorio,
    private val categoryRepo: CategoriaRepositorio,
    private val auth: com.google.firebase.auth.FirebaseAuth
) : ViewModel() {

    private val categoriasMap = categoryRepo.todasLasCategorias
        .map { lista -> lista.associateBy { it.id } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val idPrestadorActual = auth.currentUser?.uid ?: ""

    private val _seccionActual = MutableStateFlow(SeccionPresupuesto.IDENTIDAD)
    val seccionActual = _seccionActual.asStateFlow()

    fun cambiarSeccion(nueva: SeccionPresupuesto) {
        _seccionActual.value = nueva
    }

    private val _estadoBorrador = MutableStateFlow(BorradorPresupuestoEntity(idBorrador = "", idPrestador = ""))
    val estadoBorrador: StateFlow<BorradorPresupuestoEntity> = _estadoBorrador.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val infoCategoriaActual: StateFlow<Pair<String, String>?> = _estadoBorrador.flatMapLatest { borrador ->
        if (borrador.idCategoria == null) flowOf(null)
        else categoriasMap.map { map ->
            map[borrador.idCategoria]?.let { it.nombre to it.icono }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _articulos = MutableStateFlow<List<com.example.myapplication.core.datos.local.entidades.ArticuloPresupuesto>>(emptyList())
    val articulos = _articulos.asStateFlow()

    private val _servicios = MutableStateFlow<List<com.example.myapplication.core.datos.local.entidades.ServicioPresupuesto>>(emptyList())
    val servicios = _servicios.asStateFlow()

    private val _gastosVarios = MutableStateFlow<List<com.example.myapplication.core.datos.local.entidades.GastoVarioPresupuesto>>(emptyList())
    val gastosVarios = _gastosVarios.asStateFlow()

    private val _impuestosDetalle = MutableStateFlow<List<com.example.myapplication.core.datos.local.entidades.ImpuestoPresupuesto>>(emptyList())
    val impuestosDetalle = _impuestosDetalle.asStateFlow()

    val misIdentidades: StateFlow<List<com.example.myapplication.core.dominio.modelos.PrestadorDominio>> = 
        identidadRepo.obtenerPerfilPrestadorDeepFlujo(idPrestadorActual)
            .map { maestro ->
                maestro?.aModelosUi() ?: emptyList()
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val perfilPrestador: StateFlow<com.example.myapplication.core.dominio.modelos.PrestadorDominio?> = 
        combine(misIdentidades, _estadoBorrador) { identidades, borrador ->
            identidades.find { it.id == borrador.idIdentidadEmisora } ?: identidades.firstOrNull()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val categoriasVigentes: StateFlow<List<String>> = combine(perfilPrestador, categoriasMap) { perfil, catMap ->
        perfil?.idCategorias?.map { id ->
            catMap[id]?.nombre ?: id
        } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun seleccionarIdentidadEmisora(id: String) {
        _estadoBorrador.update { it.copy(idIdentidadEmisora = id) }
        autoGuardar()
    }

    fun actualizarDireccionManual(
        calle: String?, 
        numero: String?, 
        piso: String?, 
        depto: String?, 
        localidad: String?, 
        provincia: String?, 
        cp: String?
    ) {
        val formatoVisible = if (calle.isNullOrBlank()) null 
            else "$calle $numero${if(!piso.isNullOrBlank()) ", Piso $piso $depto" else ""}, $localidad"

        _estadoBorrador.update { it.copy(
            direccionManual = formatoVisible,
            calleManual = calle,
            numeroManual = numero,
            pisoManual = piso,
            deptoManual = depto,
            localidadManual = localidad,
            provinciaManual = provincia,
            cpManual = cp,
            idDireccionCliente = null 
        ) }
        autoGuardar()
    }

    fun actualizarMetodosPago(metodo: String?) {
        _estadoBorrador.update { it.copy(metodosPago = metodo) }
        autoGuardar()
    }

    fun actualizarValidez(dias: Int) {
        _estadoBorrador.update { it.copy(diasValidez = dias) }
        autoGuardar()
    }

    fun actualizarNotas(notas: String?) {
        _estadoBorrador.update { it.copy(notas = notas) }
        autoGuardar()
    }

    fun actualizarCategoria(nombreOCat: String) {
        val targetId = categoriasMap.value.entries.find { it.value.nombre == nombreOCat }?.key ?: nombreOCat
        _estadoBorrador.update { it.copy(idCategoria = targetId) }
        autoGuardar()
    }

    private val _busquedaSku = MutableStateFlow("")
    @OptIn(ExperimentalCoroutinesApi::class)
    val sugerenciasProductos: StateFlow<List<com.example.myapplication.core.dominio.modelos.ProductoDominio>> = combine(
        _busquedaSku.debounce(300),
        categoriasMap
    ) { query, map ->
        Pair(query, map)
    }.flatMapLatest { (query, map) ->
        if (query.length < 2) flowOf(emptyList())
        else productoDao.obtenerProductosPorPropietario(idPrestadorActual).map { lista ->
            lista.filter { 
                it.sku?.contains(query, ignoreCase = true) == true || 
                it.nombre.contains(query, ignoreCase = true) 
            }.map { com.example.myapplication.prestador.datos.mapeadores.ProductoMappers.deEntidadADominio(it, map) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ESTADO DE ALERTAS DE UI ---
    private val _mostrarAlertaDuplicado = MutableStateFlow(false)
    val mostrarAlertaDuplicado = _mostrarAlertaDuplicado.asStateFlow()

    fun ocultarAlertaDuplicado() { _mostrarAlertaDuplicado.value = false }

    private val _concursoVinculado = MutableStateFlow<ConcursoPublicoEntity?>(null)
    val concursoVinculado = _concursoVinculado.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val datosCliente: StateFlow<com.example.myapplication.core.dominio.modelos.UsuarioDominio?> = combine(
        _estadoBorrador.map { it.idBorrador }.filter { it.isNotEmpty() },
        _concursoVinculado
    ) { sesionId, concurso ->
        concurso?.idCliente ?: sesionId
    }.flatMapLatest { usuarioDao.obtenerPorId(it) }
     .map { it?.let { com.example.myapplication.core.dominio.mapeadores.UsuarioMappers.deEntidadAModeloUi(it) } }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val direccionesCliente: StateFlow<List<com.example.myapplication.core.dominio.modelos.DireccionDominio>> = combine(
        _estadoBorrador.map { it.idBorrador }.filter { it.isNotEmpty() },
        _concursoVinculado
    ) { sesionId, concurso ->
        val idChat = com.example.myapplication.core.utilidades.ChatIdHelper.generateChatId(idPrestadorActual, sesionId)
        
        val realesFlow = direccionDao.obtenerPorPropietario(sesionId)
        val chatLocsFlow = chatDao.obtenerMensajesPorTipo(idChat, TipoMensaje.UBICACION)
        
        combine(realesFlow, chatLocsFlow) { reales, chatMsgs ->
            val chatSinteticas = chatMsgs.map { msg ->
                DireccionDominio(
                    id = msg.id,
                    idPropietario = sesionId,
                    calle = msg.direccionTexto?.split(" ")?.dropLast(1)?.joinToString(" ") ?: "Ubicación de Chat",
                    numero = msg.direccionTexto?.split(" ")?.lastOrNull() ?: "",
                    localidad = "Enviada por Chat",
                    codigoPostal = "",
                    tipo = TipoDireccion.PERFIL_USUARIO
                )
            }
            
            val syntheticConcurso = if (concurso != null) {
                listOf(DireccionDominio(
                    id = concurso.idConcurso,
                    idPropietario = sesionId,
                    calle = concurso.direccionCalle ?: "Ubicación Obra",
                    numero = concurso.direccionNumero ?: "",
                    localidad = concurso.direccionLocalidad ?: "Zona",
                    codigoPostal = concurso.direccionCodigoPostal ?: "",
                    tipo = TipoDireccion.PERFIL_USUARIO
                ))
            } else emptyList()
            
            syntheticConcurso + chatSinteticas + reales.map { com.example.myapplication.core.dominio.mapeadores.DireccionMappers.deEntidadAModelo(it) }
        }.first()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calculos = combine(_articulos, _servicios, _gastosVarios, _impuestosDetalle) { art, svc, gas, imp ->
        CalculadoraPresupuesto.calcularTodo(art, svc, gas, imp)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalculadoraPresupuesto.ResultadoCalculo(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0))

    /**
     * Inicializa el borrador recuperándolo de Room si existe.
     * 🔥 [ELITE v2026.FINAL]: Soporta inicialización desde Concursos Públicos.
     */
    fun inicializarBorrador(idCliente: String, idPrestador: String, idConcurso: String? = null) {
        viewModelScope.launch {
            val sesionId = idConcurso ?: idCliente
            
            // Intentar recuperar el concurso si existe
            concursoPublicoRepositorio.obtenerConcursoPorId(sesionId)?.let {
                _concursoVinculado.value = it
            }

            presupuestoRepositorio.obtenerBorradorConItems(sesionId).first()?.let { borradorFull ->
                _estadoBorrador.value = borradorFull.cabecera
                
                // Mapear ítems de la base de datos a los flujos del VM
                _articulos.value = borradorFull.items.filter { it.tipo == TipoProducto.PRODUCTO }.map { 
                    com.example.myapplication.core.datos.local.entidades.ArticuloPresupuesto(
                        id = it.id.hashCode().toLong(),
                        idProducto = it.id,
                        codigo = it.sku ?: "",
                        descripcion = it.nombre,
                        cantidad = it.cantidad,
                        precioUnitario = it.precioVenta,
                        porcentajeImpuesto = it.impuestoDefault,
                        porcentajeDescuento = it.descuentoDefault,
                        urlImagen = it.urlImagen
                    )
                }
                
                _servicios.value = borradorFull.items.filter { it.tipo == TipoProducto.SERVICIO }.map {
                    com.example.myapplication.core.datos.local.entidades.ServicioPresupuesto(
                        id = it.id.hashCode().toLong(),
                        idProducto = it.id,
                        codigo = it.sku ?: "",
                        descripcion = it.nombre,
                        precioUnitario = it.precioVenta,
                        porcentajeDescuento = it.descuentoDefault
                    )
                }

                _gastosVarios.value = borradorFull.items.filter { it.tipo == TipoProducto.GASTO }.map {
                    com.example.myapplication.core.datos.local.entidades.GastoVarioPresupuesto(
                        id = it.id.hashCode().toLong(),
                        descripcion = it.nombre,
                        precioUnitario = it.precioVenta,
                        porcentajeDescuento = it.descuentoDefault,
                        monto = it.precioVenta * it.cantidad
                    )
                }

                android.util.Log.d("BorradorVM", "♻️ [RESTORE] Borrador recuperado para: $sesionId")
            } ?: run {
                var borradorBase = BorradorPresupuestoEntity(
                    idBorrador = sesionId, 
                    idPrestador = idPrestador,
                    tipo = if (idConcurso != null) TipoPresupuesto.CONCURSO else TipoPresupuesto.CONVERSACION
                )
                
                // Si venimos de un concurso, pre-poblamos datos tácticos
                idConcurso?.let { idC ->
                    concursoPublicoRepositorio.obtenerConcursoPorId(idC)?.let { concurso ->
                        _concursoVinculado.value = concurso
                        borradorBase = borradorBase.copy(
                            tituloTrabajo = concurso.titulo,
                            idCategoria = concurso.idCategoria,
                            idDireccionCliente = concurso.idConcurso 
                        )
                        android.util.Log.d("BorradorVM", "🎯 [TOPIK_INIT] Borrador iniciado desde concurso: ${concurso.titulo}")
                    }
                }
                
                _estadoBorrador.value = borradorBase
            }
        }
    }

    fun actualizarBusqueda(query: String) {
        _busquedaSku.value = query
    }

    /**
     * --- RESULTADO DE VALIDACIÓN DE CATÁLOGO ---
     */
    data class ResultadoValidacionCatalogo(
        val nuevos: List<ProductoEntity> = emptyList(),
        val modificados: List<ProductoEntity> = emptyList(),
        val tieneCambios: Boolean = false
    )

    private val _resultadoValidacion = MutableStateFlow(ResultadoValidacionCatalogo())
    val resultadoValidacion = _resultadoValidacion.asStateFlow()

    fun limpiarValidacion() {
        _resultadoValidacion.value = ResultadoValidacionCatalogo()
    }

    /**
     * Compara los ítems del borrador con el catálogo en Room.
     * Si no hay cambios, avanza automáticamente a la siguiente sección.
     */
    fun validarSincronizacionCatalogo(onSinCambios: () -> Unit) {
        viewModelScope.launch {
            val nuevos = mutableListOf<ProductoEntity>()
            val modificados = mutableListOf<ProductoEntity>()

            // 1. Procesar Artículos
            _articulos.value.forEach { art ->
                val idProd = art.idProducto ?: return@forEach
                val enDb = productoDao.obtenerProductoPorId(idProd)
                if (enDb == null) {
                    nuevos.add(ProductoEntity(
                        id = idProd, idPropietario = idPrestadorActual,
                        nombre = art.descripcion, precioVenta = art.precioUnitario,
                        tipo = TipoProducto.PRODUCTO, sku = art.codigo, urlImagen = art.urlImagen
                    ))
                } else if (enDb.nombre != art.descripcion || Math.abs(enDb.precioVenta - art.precioUnitario) > 0.1) {
                    modificados.add(enDb.copy(nombre = art.descripcion, precioVenta = art.precioUnitario))
                }
            }

            // 2. Procesar Servicios
            _servicios.value.forEach { svc ->
                val idProd = svc.idProducto ?: return@forEach
                val enDb = productoDao.obtenerProductoPorId(idProd)
                if (enDb == null) {
                    nuevos.add(ProductoEntity(
                        id = idProd, idPropietario = idPrestadorActual,
                        nombre = svc.descripcion, precioVenta = svc.precioUnitario,
                        tipo = TipoProducto.SERVICIO, sku = svc.codigo
                    ))
                } else if (enDb.nombre != svc.descripcion || Math.abs(enDb.precioVenta - svc.precioUnitario) > 0.1) {
                    modificados.add(enDb.copy(nombre = svc.descripcion, precioVenta = svc.precioUnitario))
                }
            }

            val resultado = ResultadoValidacionCatalogo(
                nuevos = nuevos.distinctBy { it.id },
                modificados = modificados.distinctBy { it.id },
                tieneCambios = nuevos.isNotEmpty() || modificados.isNotEmpty()
            )

            if (resultado.tieneCambios) {
                _resultadoValidacion.value = resultado
            } else {
                onSinCambios()
            }
        }
    }

    /**
     * Aplica los cambios al catálogo (Room) en segundo plano (Dispatchers.IO) 
     * para garantizar una navegación fluida y sin bloqueos en la UI.
     */
    fun sincronizarCatalogo(lista: List<ProductoEntity>) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                productoDao.insertarProductos(lista)
                limpiarValidacion()
                android.util.Log.d("BorradorVM", "✅ Sincronización de catálogo exitosa (${lista.size} ítems)")
            } catch (e: Exception) {
                android.util.Log.e("BorradorVM", "❌ Error al sincronizar catálogo", e)
            }
        }
    }

    fun intentarAgregarArticulo(art: com.example.myapplication.core.datos.local.entidades.ArticuloPresupuesto, esEdicion: Boolean, onSuccess: () -> Unit) {
        val actual = _articulos.value
        val existe = art.idProducto != null && actual.any { it.idProducto == art.idProducto && it.id != art.id }
        
        if (existe && !esEdicion) {
            _mostrarAlertaDuplicado.value = true
        } else {
            _articulos.update { lista ->
                if (esEdicion) lista.map { if (it.id == art.id) art else it }
                else lista + art
            }
            autoGuardar()
            onSuccess()
        }
    }

    fun intentarAgregarServicio(svc: com.example.myapplication.core.datos.local.entidades.ServicioPresupuesto, esEdicion: Boolean, onSuccess: () -> Unit) {
        val actual = _servicios.value
        val existe = svc.idProducto != null && actual.any { it.idProducto == svc.idProducto && it.id != svc.id }

        if (existe && !esEdicion) {
            _mostrarAlertaDuplicado.value = true
        } else {
            _servicios.update { lista ->
                if (esEdicion) lista.map { if (it.id == svc.id) svc else it }
                else lista + svc
            }
            autoGuardar()
            onSuccess()
        }
    }

    fun intentarAgregarGasto(gasto: com.example.myapplication.core.datos.local.entidades.GastoVarioPresupuesto, esEdicion: Boolean, onSuccess: () -> Unit) {
        _gastosVarios.update { actual ->
            if (esEdicion) actual.map { if (it.id == gasto.id) gasto else it }
            else actual + gasto
        }
        autoGuardar()
        onSuccess()
    }

    fun eliminarArticulo(id: Long) {
        _articulos.update { actual -> actual.filter { it.id != id } }
        autoGuardar()
    }

    fun eliminarServicio(id: Long) {
        _servicios.update { actual -> actual.filter { it.id != id } }
        autoGuardar()
    }

    fun eliminarGastoVario(id: Long) {
        _gastosVarios.update { actual -> actual.filter { it.id != id } }
        autoGuardar()
    }

    fun seleccionarDireccionCliente(id: String) {
        _estadoBorrador.update { it.copy(idDireccionCliente = id, direccionManual = null) }
        autoGuardar()
    }

    private fun autoGuardar() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val b = _estadoBorrador.value
                presupuestoRepositorio.guardarBorrador(b)
                
                // 🔥 [ELITE]: Persistencia de ítems vinculados
                val items = _articulos.value.map { 
                    ProductoEntity(
                        id = it.idProducto ?: UUID.randomUUID().toString(),
                        idPropietario = idPrestadorActual, idBorrador = b.idBorrador,
                        idOriginal = it.idProducto, // 🔥 [SUPREME]
                        nombre = it.descripcion, precioVenta = it.precioUnitario, cantidad = it.cantidad,
                        tipo = TipoProducto.PRODUCTO, sku = it.codigo
                    )
                } + _servicios.value.map {
                    ProductoEntity(
                        id = it.idProducto ?: UUID.randomUUID().toString(),
                        idPropietario = idPrestadorActual, idBorrador = b.idBorrador,
                        idOriginal = it.idProducto, // 🔥 [SUPREME]
                        nombre = it.descripcion, precioVenta = it.precioUnitario,
                        tipo = TipoProducto.SERVICIO, sku = it.codigo
                    )
                } + _gastosVarios.value.map {
                    ProductoEntity(
                        id = UUID.randomUUID().toString(),
                        idPropietario = idPrestadorActual, idBorrador = b.idBorrador,
                        nombre = it.descripcion, precioVenta = it.precioUnitario, cantidad = 1,
                        tipo = TipoProducto.GASTO
                    )
                }
                
                productoDao.insertarProductos(items)

            } catch (e: Exception) {
                android.util.Log.e("BorradorVM", "⚠️ Error en auto-guardado", e)
            }
        }
    }

    private val _navegarAPaywall = MutableSharedFlow<Unit>()
    val navegarAPaywall = _navegarAPaywall.asSharedFlow()

    fun enviarPresupuesto(miniatura: String? = null) {
        android.util.Log.d("MavElite", "[ENVIO_PRESUPUESTO_DESDE_BORRADOR]")
        viewModelScope.launch {
            try {
                val b = _estadoBorrador.value
                val p = perfilPrestador.value
                val res = calculos.value
                
                val idEmisorReal = p?.id ?: b.idPrestador
                val idReceptorReal = _concursoVinculado.value?.idCliente ?: b.idBorrador
                
                val definitivo = PresupuestoEntity(
                    idPresupuesto = UUID.randomUUID().toString(),
                    idCliente = idReceptorReal,
                    idPrestador = idEmisorReal,
                    idConcurso = _concursoVinculado.value?.idConcurso,
                    nombrePrestador = p?.titulo ?: "Prestador",
                    nombreEmpresaPrestador = p?.subtitulo,
                    subtotal = res.subtotal,
                    montoImpuestos = res.montoImpuestos,
                    montoDescuento = res.montoDescuento,
                    totalGeneral = res.totalGeneral,
                    urlMiniatura = miniatura,
                    estado = EstadoPresupuesto.PENDIENTE,
                    metodosPago = b.metodosPago,
                    diasValidez = b.diasValidez,
                    notas = b.notas,
                    idCategoria = b.idCategoria,
                    tituloTrabajo = b.tituloTrabajo.ifBlank { "Presupuesto de Servicio" },
                    tipo = b.tipo,
                    marcaTiempo = System.currentTimeMillis()
                )
                
                // 1. Persistencia Local y Envío Shared (Manejo de Elite)
                val finalConItems = presupuestoRepositorio.enviarPresupuesto(definitivo, b.idBorrador, "MANO DE OBRA")
                
                // 2. Envío al Chat (Tránsito Realtime)
                val idChat = com.example.myapplication.core.utilidades.ChatIdHelper.generateChatId(idEmisorReal, idReceptorReal)
                
                chatRepositorio.enviarMensajePresupuesto(
                    idChat = idChat,
                    emisor = idEmisorReal,
                    receptor = idReceptorReal,
                    presupuesto = finalConItems
                )

                // 4. Limpiar Borrador
                presupuestoRepositorio.eliminarBorrador(b.idBorrador)
            } catch (e: SecurityException) {
                if (e.message == "MEMBERSHIP_REQUIRED") {
                    _navegarAPaywall.emit(Unit)
                }
            } catch (e: Exception) {
                android.util.Log.e("BorradorVM", "❌ Error al enviar presupuesto", e)
            }
        }
    }
}




