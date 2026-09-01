package com.example.myapplication.prestador.viewmodel.presupuesto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.datos.local.entidades.ProductoEntity
//import com.example.myapplication.prestador.datos.local.entidades.TipoProducto
import com.example.myapplication.core.datos.local.entidades.ArticuloPresupuesto
import com.example.myapplication.core.datos.local.entidades.ServicioPresupuesto
import com.example.myapplication.core.datos.local.entidades.TipoProducto
import com.example.myapplication.prestador.datos.repositorios.PrestadorRecursoRepositorio
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.myapplication.prestador.datos.local.dao.ProductoDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.UUID

/**
 * --- VIEWMODEL DE PRODUCTOS Y CATÁLOGO (ELITE v2026.FINAL) ---
 */
@HiltViewModel
class ProductoViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val productoDao: ProductoDao,
    private val categoriaRepo: com.example.myapplication.core.datos.repositorios.CategoriaRepositorio,
    private val identidadRepo: com.example.myapplication.prestador.datos.repositorios.ConsultasPrestadorRepositorio
) : ViewModel() {

    private val idPrestador = auth.currentUser?.uid ?: ""

    // 🔥 [ELITE]: Mapa maestro de categorías para resolución instantánea de Nombres/Emojis
    val mapaCategorias: StateFlow<Map<String, com.example.myapplication.core.datos.local.entidades.CategoriaEntity>> = 
        categoriaRepo.todasLasCategorias
            .map { lista -> lista.associateBy { it.id } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    // --- SECTOR: CATEGORÍAS VIGENTES ---
    val categoriasVigentes: StateFlow<List<com.example.myapplication.core.datos.local.entidades.CategoriaEntity>> = 
        combine(identidadRepo.obtenerPerfilPrestadorDeepFlujo(idPrestador), mapaCategorias) { maestro, mapa ->
            val ids = mutableSetOf<String>()
            maestro?.prestador?.perfil?.idCategorias?.let { ids.addAll(it) }
            
            ids.mapNotNull { mapa[it] }.sortedBy { it.nombre }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _busqueda = MutableStateFlow("")
    val busqueda = _busqueda.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val catalogoCompleto: StateFlow<List<ProductoEntity>> = _busqueda
        .debounce(300)
        .flatMapLatest { query ->
            productoDao.obtenerProductosPorPropietario(idPrestador).map { list ->
                if (query.isBlank()) list
                else list.filter { it.nombre.contains(query, ignoreCase = true) || it.sku?.contains(query, ignoreCase = true) == true }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val catalogoDominio: StateFlow<List<com.example.myapplication.core.dominio.modelos.ProductoDominio>> = 
        combine(catalogoCompleto, mapaCategorias) { lista, mapa ->
            lista.map { com.example.myapplication.prestador.datos.mapeadores.ProductoMappers.deEntidadADominio(it, mapa) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val catalogoArticulos: StateFlow<List<ProductoEntity>> = catalogoCompleto.map { list ->
        list.filter { it.tipo == TipoProducto.PRODUCTO }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val catalogoServicios: StateFlow<List<ProductoEntity>> = catalogoCompleto.map { list ->
        list.filter { it.tipo == TipoProducto.SERVICIO }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val catalogoGastos: StateFlow<List<ProductoEntity>> = catalogoCompleto.map { list ->
        list.filter { it.tipo == TipoProducto.GASTO }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun actualizarBusqueda(nueva: String) {
        _busqueda.value = nueva
    }

    /**
     * 🔥 [ELITE]: Valida si un SKU ya existe en el catálogo.
     * @param sku El código a validar.
     * @param idActual El ID del producto que se está editando (para ignorarse a sí mismo).
     */
    fun validarSkuUnico(sku: String, idActual: String?): Boolean {
        if (sku.isBlank()) return true
        return catalogoCompleto.value.none { 
            it.sku.equals(sku, ignoreCase = true) && it.id != idActual 
        }
    }

    fun guardarProducto(producto: ProductoEntity) {
        viewModelScope.launch {
            productoDao.insertarProducto(producto.copy(idPropietario = idPrestador))
        }
    }

    fun eliminarProducto(producto: ProductoEntity) {
        viewModelScope.launch {
            productoDao.eliminarProducto(producto)
        }
    }

    /**
     * 🔥 [ELITE]: Procesa una imagen capturada para el catálogo.
     * Comprime a WebP y genera miniatura Base64.
     */
    suspend fun procesarImagenCatalogo(context: android.content.Context, uri: android.net.Uri): Pair<String?, String?> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val bytes = com.example.myapplication.core.utilidades.ImageUtils.compressElite(context, uri)
            if (bytes != null) {
                val path = com.example.myapplication.core.utilidades.ImageUtils.saveBytesToFile(context, bytes, UUID.randomUUID().toString())
                val miniatura = com.example.myapplication.core.utilidades.ImageUtils.generateThumbnailFromBytes(bytes)
                Pair(path, miniatura)
            } else Pair(null, null)
        }
    }

    fun crearNuevoProducto(
        nombre: String, 
        descripcion: String = "",
        precioVenta: Double, 
        precioCosto: Double = 0.0,
        tipo: TipoProducto, 
        sku: String? = null,
        stock: Int = 0,
        urlImagen: String? = null,
        miniaturaBase64: String? = null,
        idCategoria: String = "GENERAL"
    ) {
        val nuevo = ProductoEntity(
            id = UUID.randomUUID().toString(),
            idPropietario = idPrestador,
            nombre = nombre,
            descripcion = descripcion,
            precioVenta = precioVenta,
            precioCosto = precioCosto,
            tipo = tipo,
            sku = sku,
            stockActual = stock,
            urlImagen = urlImagen,
            miniaturaBase64 = miniaturaBase64,
            idCategoria = idCategoria
        )
        guardarProducto(nuevo)
    }

    fun guardarArticuloEnCatalogo(item: ArticuloPresupuesto, idPropietario: String) {
        val producto = ProductoEntity(
            id = item.idProducto ?: UUID.randomUUID().toString(),
            idPropietario = idPropietario,
            nombre = item.descripcion,
            precioVenta = item.precioUnitario,
            precioCosto = item.precioCosto,
            impuestoDefault = item.porcentajeImpuesto,
            descuentoDefault = item.porcentajeDescuento,
            sku = item.codigo,
            tipo = TipoProducto.PRODUCTO,
            urlImagen = item.urlImagen,
            miniaturaBase64 = item.miniaturaBase64
        )
        guardarProducto(producto)
    }

    fun guardarServicioEnCatalogo(servicio: ServicioPresupuesto, idPropietario: String) {
        val producto = ProductoEntity(
            id = servicio.idProducto ?: UUID.randomUUID().toString(),
            idPropietario = idPropietario,
            nombre = servicio.descripcion,
            precioVenta = servicio.total,
            precioCosto = 0.0,
            tipo = TipoProducto.SERVICIO,
            sku = servicio.codigo,
            urlImagen = servicio.urlImagen,
            miniaturaBase64 = servicio.miniaturaBase64
        )
        guardarProducto(producto)
    }
}




