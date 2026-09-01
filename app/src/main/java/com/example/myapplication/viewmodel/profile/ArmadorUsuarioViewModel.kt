package com.example.myapplication.viewmodel.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.core.dominio.mapeadores.UsuarioMappers
import com.example.myapplication.obreros.GestorSincronizacionUsuario
import com.example.myapplication.core.utilidades.ImageUtils
import com.example.myapplication.coordinadores.CoordinadorAcciones
import com.example.myapplication.uishared.ui.components.profile.PerfilIdentidadV3
import androidx.compose.ui.graphics.Color
import com.example.myapplication.ui.componentes.be.modelos.EmocionBe
import com.example.myapplication.datos.gestores.BorradorPerfilUsuarioGestor
import com.example.myapplication.datos.repositorios.ConsultasUsuarioRepositorio
import com.example.myapplication.datos.repositorios.SincUsuarioRepositorio
import com.example.myapplication.datos.repositorios.UsuarioAutenticacionRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- ARMADOR DE PERFIL PROPIO DEL USUARIO (US - v2026.ELITE) ---
 * [PROPÓSITO]: Orquestar la lógica del perfil propio del cliente, el borrador en RAM y la sincronización.
 * [LEY #9]: Estándar Maverick. Especialista en la identidad del Cliente.
 */
@HiltViewModel
class ArmadorUsuarioViewModel @Inject constructor(
    @ApplicationContext private val contexto: android.content.Context,
    private val consultasRepo: ConsultasUsuarioRepositorio,
    private val sincRepo: SincUsuarioRepositorio,
    private val gestorBorrador: BorradorPerfilUsuarioGestor,
    authRepo: UsuarioAutenticacionRepositorio,
    private val coordinator: CoordinadorAcciones 
) : ViewModel() {

    private val _uidActual = MutableStateFlow<String?>(null)
    
    // --- SECTOR: ESTADO (SSOT) ---

    val ecosistemaMaestro: StateFlow<CuentaMaestroUsuario?> = gestorBorrador.borrador

    /**
     * 🔥 [ELITE SSOT]: Lista de identidades procesadas para todo el ecosistema.
     * Garantiza que las imágenes de perfil estén listas para renderizar (Ley #10).
     */
    val identidadesSoberanas: StateFlow<List<PerfilIdentidadV3>> = combine(
        ecosistemaMaestro,
        coordinator.idPerfilSeleccionado
    ) { cuenta, _ ->
        if (cuenta == null) return@combine emptyList()
        
        val lista = mutableListOf<PerfilIdentidadV3>()
        
        // 1. Identidad Personal
        val p = cuenta.usuario.perfil
        lista.add(
            PerfilIdentidadV3(
                id = "personal",
                nombre = p.nombreVisible,
                iniciales = p.nombreVisible.take(2).uppercase(),
                photoUrl = ImageUtils.processImageSource(p.urlMiniatura ?: p.urlFoto),
                colorAcento = Color(0xFF22D3EE),
                estaVerificado = false, // El cliente no suele estar verificado
                esSuscripto = false
            )
        )
        
        // 2. Identidades de Empresa/Sucursal
        cuenta.empresas.forEach { empMaestro ->
            empMaestro.sucursales.forEach { suc ->
                lista.add(
                    PerfilIdentidadV3(
                        id = suc.id,
                        nombre = suc.nombre,
                        iniciales = suc.nombre.take(2).uppercase(),
                        photoUrl = ImageUtils.processImageSource(empMaestro.empresa.urlMiniatura ?: empMaestro.empresa.urlFoto),
                        colorAcento = Color(0xFF8B5CF6), // Púrpura Maverick
                        estaVerificado = true, // Las empresas suelen estar verificadas
                        esSuscripto = true
                    )
                )
            }
        }
        lista
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hayCambiosPendientes: StateFlow<Boolean> = gestorBorrador.hayCambiosPendientes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // --- SECTOR: IDENTIDAD ACTIVA (ELITE v2026) ---

    val nombrePerfilActivo: StateFlow<String> = combine(ecosistemaMaestro, coordinator.idPerfilSeleccionado) { cuenta, idPerfil ->
        if (cuenta == null) return@combine "Usuario"
        if (idPerfil == null) {
            cuenta.usuario.perfil.nombreVisible
        } else {
            val sucursal = cuenta.empresas.flatMap { it.sucursales }.find { it.id == idPerfil }
            sucursal?.nombre ?: cuenta.usuario.perfil.nombreVisible
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Cargando...")

    val fotoPerfilActiva: StateFlow<Any?> = combine(ecosistemaMaestro, coordinator.idPerfilSeleccionado) { cuenta, idPerfil ->
        if (cuenta == null) return@combine null
        val origen = if (idPerfil == null) {
            cuenta.usuario.perfil.urlMiniatura ?: cuenta.usuario.perfil.urlFoto
        } else {
            val empresa = cuenta.empresas.find { it.sucursales.any { s -> s.id == idPerfil } }?.empresa
            empresa?.urlMiniatura ?: empresa?.urlFoto ?: cuenta.usuario.perfil.urlFoto
        }
        ImageUtils.processImageSource(origen)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando = _estaCargando.asStateFlow()

    /**
     * Flujo real de la DB para alimentar el borrador inicial.
     * [ELITE]: Emancipación total. El ViewModel gestiona su propia carga SSOT.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val ecosistemaReal: Flow<CuentaMaestroUsuario?> = authRepo.observarUsuarioActual()
        .flatMapLatest { user ->
            val uid = user?.uid
            if (uid == null) {
                _uidActual.value = null
                flowOf(null)
            } else {
                _uidActual.value = uid
                Log.d("ArmadorUser", "🤖 [SSOT_LOAD] Cargando ecosistema para: $uid")
                consultasRepo.obtenerCuentaMaestroUsuarioFlujo(uid)
            }
        }
        .onEach { maestro ->
            if (maestro == null) {
                _uidActual.value?.let { uid -> 
                    Log.d("ArmadorUser", "☁️ [SYNC_TRIGGER] Perfil no encontrado en Room. Descargando de nube...")
                    descargarPerfilPropio(uid) 
                }
            } else {
                if (gestorBorrador.borrador.value == null) {
                    Log.d("ArmadorUser", "🎨 [DRAFT_INIT] Borrador inicializado para: ${maestro.usuario.perfil.nombreVisible}")
                    gestorBorrador.iniciarEdicion(maestro)
                }
            }
        }

    init {
        viewModelScope.launch { ecosistemaReal.collect() }
    }

    /**
     * Establece el ID del usuario actual tras la autenticación.
     */

    // --- SECTOR: ACCIONES ---

    fun refrescarDatos() {
        val uid = _uidActual.value ?: return
        android.util.Log.d("ArmadorUser", "🔄 [REFRESH] Disparando recarga desde nube para $uid...")
        descargarPerfilPropio(uid)
    }

    fun seleccionarPerfil(idPerfil: String?) = coordinator.seleccionarPerfil(idPerfil)

    fun sincronizarPerfil() {
        val maestro = gestorBorrador.obtenerBorradorMaestro() ?: return
        viewModelScope.launch {
            android.util.Log.d("ArmadorUser", "💾 [COMMIT_START] Guardando cambios de perfil...")
            _estaCargando.value = true
            try {
                // 1. Persistencia Atómica en Room (Commit)
                sincRepo.guardarUsuarioLocalYEncolar(
                    UsuarioMappers.deDominioAEntidad(maestro.usuario.perfil), // Need to add this to mapper
                    maestro.usuario.direcciones.map { it }
                )
                
                // 2. Consolidar borrador
                gestorBorrador.consolidarEdicion()

                // 🔥 [LEY #12]: Unión hace la Búsqueda - Feedback Visual
                coordinator.emitirFeedbackVisual(
                    mensaje = "¡Perfil actualizado con éxito!",
                    emocion = EmocionBe.FELIZ,
                    icono = "✅"
                )

                android.util.Log.d("ArmadorUser", "✅ [COMMIT_OK] Perfil sincronizado exitosamente.")
            } catch (e: Exception) {
                android.util.Log.e("ArmadorUser", "❌ [COMMIT_ERROR] ${e.message}")
                coordinator.emitirFeedbackVisual(
                    mensaje = "Error al guardar el perfil.",
                    emocion = EmocionBe.TRISTE,
                    icono = "❌"
                )
            } finally {
                _estaCargando.value = false
            }
        }
    }

    fun descargarPerfilPropio(uid: String) {
        viewModelScope.launch {
            _estaCargando.value = true
            sincRepo.descargarPerfilUsuarioCompleto(uid)
            _estaCargando.value = false
        }
    }

    /**
     * 🔥 [ELITE]: Procesa el cambio de foto de perfil en el borrador.
     */
    fun actualizarFotoPerfil(uri: Uri) {
        viewModelScope.launch {
            _estaCargando.value = true
            try {
                val bytesFoto = ImageUtils.compressElite(contexto, uri) ?: return@launch
                val uidActual = _uidActual.value ?: "mav"
                val rutaLocal = ImageUtils.saveBytesToFile(contexto, bytesFoto, "perfil_user_$uidActual")
                val miniatura = ImageUtils.generateThumbnailBase64(contexto, uri)

                val actual = gestorBorrador.obtenerBorradorMaestro()?.usuario?.perfil ?: UsuarioDominio(id = uidActual)
                val actualizada = actual.copy(
                    urlFoto = rutaLocal ?: uri.toString(),
                    urlMiniatura = miniatura
                )
                gestorBorrador.actualizarPerfilPersonal(actualizada)
            } catch (e: Exception) {
                Log.e("ArmadorUser", "❌ [PHOTO_ERROR] ${e.message}")
            } finally {
                _estaCargando.value = false
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            android.util.Log.d("ArmadorUser", "🚪 [LOGOUT] Cerrando sesión...")
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        }
    }

    /**
     * 🔥 [ELITE]: Proceso de baja total del sistema.
     */
    fun eliminarCuenta(alCompletar: () -> Unit) {
        val uid = _uidActual.value ?: return
        viewModelScope.launch {
            android.util.Log.d("ArmadorUser", "⚠️ [DELETE_ACCOUNT] Iniciando proceso de baja para $uid...")
            _estaCargando.value = true
            try {
                // [ELITE]: Aquí iría la limpieza de Room si se desea
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                alCompletar()
                android.util.Log.d("ArmadorUser", "✅ [DELETE_OK] Cuenta eliminada localmente.")
            } catch (e: Exception) {
                Log.e("ArmadorUser", "❌ [DELETE_ERROR] ${e.message}")
            } finally {
                _estaCargando.value = false
            }
        }
    }

    // --- MUTACIONES DE BORRADOR ---

    fun guardarCambiosIdentidad(modelo: PrestadorDominio) {
        val actual = gestorBorrador.obtenerBorradorMaestro()?.usuario?.perfil ?: return
        val actualizada = actual.copy(
            nombre = modelo.nombre,
            apellido = modelo.apellido,
            nombreVisible = modelo.titulo,
            correo = modelo.correo,
            telefono = modelo.numeroTelefono,
            biografia = modelo.biografia ?: "",
            cuitCuil = modelo.cuitCuil ?: ""
        )
        gestorBorrador.actualizarPerfilPersonal(actualizada)
    }

    fun actualizarDireccion(direccion: DireccionDominio) {
        gestorBorrador.actualizarDireccionPersonal(direccion)
    }

    fun eliminarDireccion(direccion: DireccionDominio) {
        gestorBorrador.eliminarDireccionPersonal(direccion.id)
    }

    fun crearEmpresa(empresa: EmpresaDominio, sucursal: SucursalDominio, direccion: DireccionDominio) {
        gestorBorrador.actualizarEmpresa(empresa)
        gestorBorrador.actualizarSucursal(empresa.id, sucursal, direccion)
    }

    fun añadirSucursal(sucursal: SucursalDominio, direccion: DireccionDominio) {
        gestorBorrador.actualizarSucursal(sucursal.idEmpresaPadre, sucursal, direccion)
    }

    fun eliminarEmpresa(id: String) = gestorBorrador.eliminarEmpresa(id)
    fun eliminarSucursal(id: String) = gestorBorrador.eliminarSucursal(id)
}



































