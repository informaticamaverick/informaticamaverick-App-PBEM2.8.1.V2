/*
package com.example.myapplication.prestador.viewmodel.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.core.datos.repositorios.GestorUbicacionGps
import com.example.myapplication.prestador.obreros.GestorSincronizacionPrestador
import com.example.myapplication.core.datos.repositorios.CategoriaRepositorio
import com.example.myapplication.core.utilidades.ImageUtils
import com.example.myapplication.prestador.datos.gestores.BorradorPerfilPrestadorGestor
import com.example.myapplication.prestador.datos.repositorios.ConsultasPrestadorRepositorio
import com.example.myapplication.prestador.datos.repositorios.SincPrestadorRepositorio
import com.example.myapplication.prestador.datos.repositorios.SincPrestadorTopicksRepositorio
import com.example.myapplication.prestador.datos.repositorios.PrestadorAutenticacionRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- ARMADOR DEL ECOSISTEMA PRESTADOR (PRO - v2026.ELITE) ---
 * [PROPÓSITO]: Orquestar la lógica soberana, el borrador en RAM y la sincronización.
 * [LEY #9]: Estándar Maverick. Único dueño del ecosistema profesional.
 */
@Deprecated("Usar PerfilPrestadorDeepViewModel para la nueva arquitectura soberana v2026.")
@HiltViewModel
class ArmadorPrestadorViewModel @Inject constructor(
    @ApplicationContext private val contexto: Context,
    private val consultasRepo: ConsultasPrestadorRepositorio,
    private val sincRepo: SincPrestadorRepositorio,
    private val sincTopicsRepo: SincPrestadorTopicksRepositorio,
    private val authRepo: PrestadorAutenticacionRepositorio,
    private val categoryRepo: CategoriaRepositorio,
    private val chatRepository: com.example.myapplication.core.datos.repositorios.ChatMotorSincRepositorio, // 🔥 [NEW]
    private val gestorBorrador: BorradorPerfilPrestadorGestor,
    private val gestorSincronizacion: GestorSincronizacionPrestador,
    private val gestorUbicacion: GestorUbicacionGps
) : ViewModel() {

    // --- SECTOR: ESTADO (SSOT) ---

    val todasLasCategorias: StateFlow<List<CategoriaDominio>> = categoryRepo.todasLasCategorias
        .map { list -> list.map { com.example.myapplication.core.dominio.mapeadores.CategoriaMappers.deEntidadADominio(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ecosistemaMaestro: StateFlow<PerfilPrestadorDeepModelo?> = gestorBorrador.borrador
    
    val hayCambiosPendientes: StateFlow<Boolean> = gestorBorrador.hayCambiosPendientes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando = _estaCargando.asStateFlow()

    private val _estaDetectandoUbicacion = MutableStateFlow(false)
    val estaDetectandoUbicacion = _estaDetectandoUbicacion.asStateFlow()

    /**
     * Flujo real de la DB para alimentar el borrador inicial.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val ecosistemaReal: Flow<PerfilPrestadorDeepModelo?> = authRepo.observarUsuarioActual()
        .flatMapLatest { user ->
            if (user == null) flowOf(null)
            else consultasRepo.obtenerPerfilPrestadorDeepFlujo(user.uid)
        }
        .onEach { maestro ->
            if (maestro == null) {
                authRepo.obtenerUsuarioActual()?.uid?.let { uid -> descargarPerfilPropio(uid) }
            } else {
                // Sincronizar hilos de red y cargar borrador si está vacío
                sincronizarTopicos(maestro)

                // 🔥 [ELITE v2026]: Registrar identidades delegadas para Chat en tiempo real
                maestro.empresas.forEach { empComp ->
                    empComp.sucursales.forEach { sucComp ->
                        chatRepository.agregarIdentidadASincronizacion(sucComp.sucursal.id)
                    }
                }
                
                if (gestorBorrador.borrador.value == null) {
                    gestorBorrador.iniciarEdicion(maestro)
                }
            }
        }

    init {
        viewModelScope.launch { ecosistemaReal.collect() }
    }

    fun refrescarDatos() {
        val uid = authRepo.obtenerUsuarioActual()?.uid ?: return
        descargarPerfilPropio(uid)
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            authRepo.cerrarSesion()
        }
    }

    /**
     * 🔥 [ELITE]: Alterna entre modo Personal y modo Empresa (Cambio Estructural).
     */
    fun alternarSoberania(idPerfil: String?, esEmpresa: Boolean) {
        val uid = authRepo.obtenerUsuarioActual()?.uid ?: return
        viewModelScope.launch {
            _estaCargando.value = true
            try {
                sincRepo.cambiarModoSoberania(uid, idPerfil, esEmpresa)
                // Tras cambio estructural, forzamos refresco de nube
                sincronizarEcosistema()
            } finally { _estaCargando.value = false }
        }
    }

    /**
     * 🔥 [ELITE]: Detecta ubicación. Si se pasa callback, se devuelve el resultado a la UI.
     * Si no, se actualiza el borrador directamente.
     */
    fun detectarUbicacionActual(alRecibir: ((DireccionDominio?) -> Unit)? = null) {
        viewModelScope.launch {
            _estaDetectandoUbicacion.value = true
            val dir = gestorUbicacion.detectarUbicacionActual()
            if (alRecibir != null) {
                alRecibir(dir)
            } else {
                dir?.let { 
                    gestorBorrador.actualizarDireccionPersonal(it) 
                }
            }
            _estaDetectandoUbicacion.value = false
        }
    }

    /**
     * 🔥 [ELITE]: Actualiza una dirección específica en el borrador.
     */
    fun actualizarDireccionSoberana(direccion: DireccionDominio) {
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

    /**
     * 🔥 [ELITE]: Procesa una captura de cámara para el perfil.
     */
    fun actualizarFotoPerfilDesdeBitmap(bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            _estaCargando.value = true
            try {
                val stream = java.io.ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP, 80, stream)
                val bytes = stream.toByteArray()
                procesarBytesFotoPerfil(bytes, "camera_capture_${System.currentTimeMillis()}")
            } catch (e: Exception) {
                Log.e("ArmadorPre", "❌ [CAMERA_PHOTO_ERR] ${e.message}")
            } finally {
                _estaCargando.value = false
            }
        }
    }

    fun actualizarFotoPerfil(uri: Uri) {
        viewModelScope.launch {
            _estaCargando.value = true
            try {
                val bytesFoto = ImageUtils.compressElite(contexto, uri) ?: return@launch
                procesarBytesFotoPerfil(bytesFoto, uri.toString())
            } catch (e: Exception) {
                Log.e("ArmadorPre", "❌ [PHOTO_ERROR] ${e.message}")
            } finally {
                _estaCargando.value = false
            }
        }
    }

    private fun procesarBytesFotoPerfil(bytes: ByteArray, sourceHint: String) {
        val uidActual = authRepo.obtenerUsuarioActual()?.uid ?: "mav"
        val rutaLocal = ImageUtils.saveBytesToFile(contexto, bytes, "perfil_prestador_$uidActual")
        val miniatura = ImageUtils.generateThumbnailFromBytes(bytes)

        val actual = gestorBorrador.obtenerBorradorMaestro()?.prestador?.perfil ?: PrestadorDominio(id = uidActual)
        val actualizado = actual.copy(
            urlFoto = rutaLocal ?: sourceHint,
            urlMiniatura = miniatura
        )
        gestorBorrador.actualizarPerfilPersonal(actualizado)
        Log.d("ArmadorPre", "✅ [PHOTO_DRAFT_OK] Foto actualizada en borrador.")
    }

    // --- SECTOR: SINCRONIZACIÓN ---

    fun sincronizarEcosistema() {
        val maestro = gestorBorrador.obtenerBorradorMaestro() ?: return
        viewModelScope.launch {
            _estaCargando.value = true
            try {
                // 1. Persistencia y Encolado
                sincRepo.guardarEcosistemaLocalYEncolar(maestro)
                // 2. Consolidar borrador
                gestorBorrador.consolidarEdicion()
                Log.d("ArmadorPre", "✅ [COMMIT_EXITO] Ecosistema sincronizado localmente y encolado.")
            } catch (e: Exception) {
                Log.e("ArmadorPre", "❌ [COMMIT_ERR] ${e.message}")
            } finally { _estaCargando.value = false }
        }
    }

    fun descargarPerfilPropio(uid: String) {
        viewModelScope.launch {
            _estaCargando.value = true
            sincRepo.descargarPerfilPrestadorCompleto(uid)
            _estaCargando.value = false
        }
    }

    private fun sincronizarTopicos(maestro: PerfilPrestadorDeepModelo) {
        viewModelScope.launch { sincTopicsRepo.sincronizarMatrizDeRed(maestro) }
    }

    // --- SECTOR: GESTIÓN DE BORRADOR (Mutaciones) ---

    fun guardarCambiosIdentidad(modelo: PrestadorDominio) {
        try {
            when (modelo.tipo) {
                TipoPrestador.INDIVIDUAL -> {
                    gestorBorrador.actualizarPerfilPersonal(modelo)
                }
                TipoPrestador.EMPRESA -> {
                    val empresa = EmpresaDominio(
                        id = modelo.id,
                        idPropietario = modelo.idPropietario,
                        nombre = modelo.titulo,
                        idCategorias = modelo.idCategorias
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
            Log.e("ArmadorPre", "❌ Error al actualizar borrador: ${e.message}")
        }
    }

    fun eliminarEmpresa(id: String) = gestorBorrador.eliminarEmpresa(id)
    fun eliminarSucursal(id: String) = gestorBorrador.eliminarSucursal(id)
    fun eliminarDireccion(direccion: DireccionDominio) = gestorBorrador.eliminarDireccionPersonal(direccion.id)
    fun descartarCambios() = gestorBorrador.descartarCambios()
}
*/


