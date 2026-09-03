package com.example.myapplication.prestador.viewmodel.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.dominio.modelos.PerfilPrestadorDeepModelo
import com.example.myapplication.core.dominio.modelos.CategoriaDominio
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.core.dominio.modelos.TipoPrestador
import com.example.myapplication.core.dominio.modelos.EmpresaDominio
import com.example.myapplication.core.dominio.modelos.SucursalDominio
import com.example.myapplication.core.dominio.mapeadores.CategoriaMappers
import com.example.myapplication.core.datos.repositorios.CategoriaRepositorio
import com.example.myapplication.core.datos.repositorios.GestorUbicacionGps
import com.example.myapplication.core.utilidades.ImageUtils
import com.example.myapplication.prestador.datos.gestores.BorradorPerfilPrestadorGestor
import com.example.myapplication.prestador.datos.repositorios.PerfilPrestadorDeepRepositorio
import com.example.myapplication.prestador.datos.repositorios.PrestadorAutenticacionRepositorio
import com.example.myapplication.prestador.dominio.motores.MotorPerfilPrestadorDeep
import com.example.myapplication.core.datos.repositorios.SincronizadorRemotoPrestador
import com.example.myapplication.core.datos.repositorios.ChatMotorSincRepositorio
import com.example.myapplication.core.datos.indices.busqueda.IndiceBusquedaPrestadorRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- VIEWMODEL DE PERFIL DEEP (v2026.ELITE) ---
 * [PROPÓSITO]: Orquestar el ecosistema profesional profundo y soberano.
 * [LEY #9]: Estándar Maverick en Español.
 */
@HiltViewModel
class PerfilPrestadorDeepViewModel @Inject constructor(
    @ApplicationContext private val contexto: Context,
    private val deepRepo: PerfilPrestadorDeepRepositorio,
    private val motorDeep: MotorPerfilPrestadorDeep,
    private val repoRemoto: SincronizadorRemotoPrestador,
    private val authRepo: PrestadorAutenticacionRepositorio,
    private val categoryRepo: CategoriaRepositorio,
    private val gestorBorrador: BorradorPerfilPrestadorGestor,
    private val gestorUbicacion: GestorUbicacionGps,
    private val repoIndice: IndiceBusquedaPrestadorRepositorio,
    private val chatRepository: ChatMotorSincRepositorio
) : ViewModel() {

    private val _state = MutableStateFlow(PerfilPrestadorDeepUiState())
    val state: StateFlow<PerfilPrestadorDeepUiState> = _state.asStateFlow()

    init {
        inicializarEcosistema()
        cargarCategorias()
    }

    private fun inicializarEcosistema() {
        val uid = authRepo.obtenerUsuarioActual()?.uid ?: return
        viewModelScope.launch {
            deepRepo.obtenerPerfilDeepFlujo(uid).collect { modelo ->
                modelo?.let {
                    if (gestorBorrador.borrador.value == null) {
                        gestorBorrador.iniciarEdicion(it)
                    }
                    _state.update { s -> s.copy(ecosistema = modelo, estaCargando = false) }
                }
            }
        }

        viewModelScope.launch {
            gestorBorrador.hayCambiosPendientes.collect { hay ->
                _state.update { it.copy(hayCambiosPendientes = hay) }
            }
        }
    }

    private fun cargarCategorias() {
        viewModelScope.launch {
            categoryRepo.todasLasCategorias.collect { list ->
                val dominios = list.map { CategoriaMappers.deEntidadADominio(it) }
                _state.update { it.copy(todasLasCategorias = dominios) }
            }
        }
    }

    /**
     * 🔥 [ELITE]: Gestiona el guardado táctico en el borrador de RAM.
     * Identifica el tipo de identidad y redirige al gestor correspondiente.
     */
    fun guardarCambiosLocales(modelo: PrestadorDominio) {
        try {
            Log.d("VIEWMODEL_DEEP", "💾 [DRAFT_SAVE] Actualizando borrador para: ${modelo.titulo} (${modelo.tipo})")
            
            when (modelo.tipo) {
                TipoPrestador.INDIVIDUAL -> {
                    gestorBorrador.actualizarPerfilPersonal(modelo)
                }
                TipoPrestador.EMPRESA -> {
                    val empresa = EmpresaDominio(
                        id = modelo.id,
                        idPropietario = modelo.idPropietario,
                        nombre = modelo.titulo,
                        idCategorias = modelo.idCategorias,
                        cuit = modelo.cuitCuil ?: "",
                        descripcion = modelo.biografia ?: ""
                    )
                    gestorBorrador.actualizarEmpresa(empresa)
                }
                TipoPrestador.SUCURSAL -> {
                    val sucursal = SucursalDominio(
                        id = modelo.id,
                        idEmpresaPadre = modelo.idEmpresa ?: "",
                        idPropietario = modelo.idPropietario,
                        nombre = modelo.titulo,
                        atiende24Horas = modelo.atiende24h,
                        visitaADomicilio = modelo.visitaADomicilio,
                        realizaEnvios = modelo.realizaEnvios,
                        brindaTurnos = modelo.brindaTurnos,
                        brindaServicio = modelo.brindaServicio,
                        brindaProducto = modelo.brindaProducto
                    )
                    val dir = modelo.direcciones.firstOrNull()
                    gestorBorrador.actualizarSucursal(modelo.idEmpresa ?: "", sucursal, dir)
                }
            }
        } catch (e: Exception) {
            Log.e("VIEWMODEL_DEEP", "❌ Error al actualizar borrador: ${e.message}")
            _state.update { it.copy(error = "Error al actualizar datos locales") }
        }
    }

    /**
     * 🔥 [ELITE]: Alterna entre modo Personal y modo Empresa (Cambio Estructural).
     */
    fun alternarSoberania(idPerfil: String?, esEmpresa: Boolean) {
        val uid = authRepo.obtenerUsuarioActual()?.uid ?: return
        viewModelScope.launch {
            _state.update { it.copy(estaCargando = true) }
            try {
                // [FIX]: antes esto terminaba en sincronizarEcosistemaCloud(), que sube el
                // borrador viejo en RAM y pisa este cambio en Room con los valores anteriores.
                // Acá el cambio va directo a Firestore Y a Room, sin pasar por el borrador.
                repoRemoto.cambiarModoSoberania(uid, idPerfil, esEmpresa)
                deepRepo.actualizarModoSoberaniaLocal(uid, idPerfil, esEmpresa)
                gestorBorrador.actualizarModoSoberania(idPerfil, esEmpresa)
                // [FIX]: hasta acá solo cambia el flag — sin esto, el perfil personal seguía
                // publicado (o dejaba de publicarse) recién en el próximo guardado manual.
                repoIndice.sincronizarTodoElDescubrimiento(uid)
            } finally { _state.update { it.copy(estaCargando = false) } }
        }
    }

    /**
     * 🔥 [ELITE]: Procesa una captura de cámara para el perfil.
     */
    fun actualizarFotoDesdeBitmap(bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            _state.update { it.copy(estaCargando = true) }
            try {
                val stream = java.io.ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP, 80, stream)
                val bytes = stream.toByteArray()
                
                val uid = authRepo.obtenerUsuarioActual()?.uid ?: "mav"
                val ruta = ImageUtils.saveBytesToFile(contexto, bytes, "perfil_deep_cam_$uid")
                val mini = ImageUtils.generateThumbnailFromBytes(bytes)
                
                val actual = gestorBorrador.obtenerBorradorMaestro()?.prestador?.perfil ?: PrestadorDominio(id = uid)
                gestorBorrador.actualizarPerfilPersonal(actual.copy(urlFoto = ruta, urlMiniatura = mini))
            } catch (e: Exception) {
                Log.e("VIEWMODEL_DEEP", "❌ [CAMERA_PHOTO_ERR] ${e.message}")
            } finally {
                _state.update { it.copy(estaCargando = false) }
            }
        }
    }

    fun refrescarDatos() {
        val uid = authRepo.obtenerUsuarioActual()?.uid ?: return
        viewModelScope.launch {
            _state.update { it.copy(estaCargando = true) }
            try {
                deepRepo.descargarEcosistemaCompleto(uid)
            } finally {
                _state.update { it.copy(estaCargando = false) }
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            authRepo.cerrarSesion()
        }
    }

    fun eliminarEmpresa(id: String) {
        gestorBorrador.eliminarEmpresa(id)
    }

    fun eliminarSucursal(id: String) {
        gestorBorrador.eliminarSucursal(id)
    }

    fun actualizarDireccion(direccion: DireccionDominio) {
        val uid = authRepo.obtenerUsuarioActual()?.uid ?: return
        if (direccion.idPropietario == null || direccion.idPropietario == uid) {
            gestorBorrador.actualizarDireccionPersonal(direccion)
        } else {
            val maestro = gestorBorrador.obtenerBorradorMaestro()
            val idEmpresa = maestro?.empresas?.find { emp -> emp.sucursales.any { it.id == direccion.idPropietario } }?.empresa?.id
            if (idEmpresa != null) {
                val sucComp = maestro.empresas.flatMap { it.sucursales }.find { it.id == direccion.idPropietario }
                if (sucComp != null) {
                    gestorBorrador.actualizarSucursal(idEmpresa, sucComp.sucursal, direccion)
                }
            }
        }
    }

    fun eliminarDireccion(direccion: DireccionDominio) {
        gestorBorrador.eliminarDireccionPersonal(direccion.id)
    }

    fun anadirEmpresa(datos: Triple<EmpresaDominio, SucursalDominio, DireccionDominio>) {
        val (empresa, sucursal, direccion) = datos
        gestorBorrador.actualizarEmpresa(empresa)
        gestorBorrador.actualizarSucursal(empresa.id, sucursal, direccion)
        // [FIX]: sin esto, una sucursal creada en la misma sesión (sin reiniciar la app)
        // no queda escuchando su buzón de mensajes hasta el próximo arranque en frío.
        chatRepository.agregarIdentidadASincronizacion(sucursal.id)
    }

    fun anadirSucursal(idEmpresa: String, sucursal: SucursalDominio, direccion: DireccionDominio) {
        gestorBorrador.actualizarSucursal(idEmpresa, sucursal, direccion)
        chatRepository.agregarIdentidadASincronizacion(sucursal.id)
    }

    /**
     * 🔥 [ELITE]: Detecta ubicación. Si se pasa callback, se devuelve el resultado a la UI.
     * Si no, se actualiza el borrador directamente.
     */
    fun detectarUbicacionActual(alRecibir: ((DireccionDominio?) -> Unit)? = null) {
        viewModelScope.launch {
            _state.update { it.copy(estaDetectandoGps = true) }
            val dir = gestorUbicacion.detectarUbicacionActual()
            if (alRecibir != null) {
                alRecibir(dir)
            } else {
                dir?.let { 
                    gestorBorrador.actualizarDireccionPersonal(it) 
                }
            }
            _state.update { it.copy(estaDetectandoGps = false) }
        }
    }

    fun sincronizarEcosistemaCloud() {
        val maestro = gestorBorrador.obtenerBorradorMaestro() ?: return
        
        viewModelScope.launch {
            _state.update { it.copy(estaCargando = true) }
            try {
                // 🔥 [ELITE]: Ejecuta impacto total (Room + Cloud + Search Index)
                motorDeep.impactarEcosistemaYActualizarIndices(maestro)
                
                gestorBorrador.consolidarEdicion()
                _state.update { it.copy(mensajeExito = "Perfil e Índices actualizados ✅") }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Fallo en la sincronización: ${e.message}") }
            } finally {
                _state.update { it.copy(estaCargando = false) }
            }
        }
    }

    fun detectarGps() {
        viewModelScope.launch {
            _state.update { it.copy(estaDetectandoGps = true) }
            val dir = gestorUbicacion.detectarUbicacionActual()
            dir?.let { gestorBorrador.actualizarDireccionPersonal(it) }
            _state.update { it.copy(estaDetectandoGps = false) }
        }
    }

    fun actualizarFoto(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(estaCargando = true) }
            val bytes = ImageUtils.compressElite(contexto, uri)
            bytes?.let {
                val uid = authRepo.obtenerUsuarioActual()?.uid ?: "mav"
                val ruta = ImageUtils.saveBytesToFile(contexto, it, "perfil_deep_$uid")
                val mini = ImageUtils.generateThumbnailFromBytes(it)
                
                val actual = gestorBorrador.obtenerBorradorMaestro()?.prestador?.perfil ?: PrestadorDominio(id = uid)
                gestorBorrador.actualizarPerfilPersonal(actual.copy(urlFoto = ruta, urlMiniatura = mini))
            }
            _state.update { it.copy(estaCargando = false) }
        }
    }
}


