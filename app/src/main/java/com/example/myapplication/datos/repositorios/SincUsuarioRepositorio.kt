package com.example.myapplication.datos.repositorios

import com.example.myapplication.core.datos.local.dao.*
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.dominio.motores.MotorSincLocal
import com.example.myapplication.core.dominio.motores.MotorSincRemoto
import com.example.myapplication.di.ApplicationScope
import com.example.myapplication.obreros.GestorSincronizacionUsuario
import com.example.myapplication.core.utilidades.ImageUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE SINCRONIZACIÓN DE USUARIO PROPIO (US) ---
 * [PROPÓSITO]: Gestionar la subida (PUSH) del perfil del cliente y delegar la descarga (PULL) a los motores centrales.
 */
@Singleton
class SincUsuarioRepositorio @Inject constructor(
    @ApplicationContext private val contexto: Context,
    @ApplicationScope private val externalScope: CoroutineScope,
    private val nubeFirestore: FirebaseFirestore,
    private val cuentaDao: CuentaDao,
    private val usuarioDao: IdentidadUsuarioDao,
    private val direccionDao: DireccionDao,
    private val empresaDao: EmpresaDao,
    private val sucursalDao: SucursalDao,
    private val gestorSincronizacion: dagger.Lazy<GestorSincronizacionUsuario>,
    private val motorLocal: MotorSincLocal,
    private val motorRemoto: MotorSincRemoto
) {


    suspend fun existeEnRemoto(uid: String): Boolean = try {
        motorRemoto.obtenerUsuario(uid) != null
    } catch (e: Exception) { false }

    /**
     * 🔥 [ELITE]: Motor Central de Acceso para el Cliente.
     * [ORDEN]: 1. Extraer datos de cuenta -> 2. Descargar Foto Original -> 3. Semilla Room.
     */
    suspend fun finalizarAcceso(usuario: com.google.firebase.auth.FirebaseUser) {
        val uid = usuario.uid
        val email = usuario.email ?: ""
        val nombreCompleto = usuario.displayName ?: "Usuario Maverick"
        val telefono = usuario.phoneNumber ?: ""
        val originalPhotoUrl = usuario.photoUrl?.toString()?.let { ImageUtils.sanitizeGooglePhotoUrl(it) }

        // [ELITE]: Intentamos separar nombre y apellido para mayor precisión
        val partesNombre = nombreCompleto.split(" ")
        val nombre = partesNombre.firstOrNull() ?: ""
        val apellido = if (partesNombre.size > 1) partesNombre.last() else ""

        try {
            // 1. Crear Semilla Inmediata (Navegación Optimista)
            val cuentaExistente = cuentaDao.obtenerPorId(uid).first()
            if (cuentaExistente == null) {
                cuentaDao.insertar(CuentaEntity(
                    id = uid, 
                    correoGoogle = email, 
                    ultimaSincronizacion = 0L
                ))
            }

            val usuarioLocal = usuarioDao.obtenerPorId(uid).first()
            if (usuarioLocal == null) {
                var rutaLocal: String? = null
                var miniatura: String? = null

                if (originalPhotoUrl != null) {
                    withContext(Dispatchers.IO) {
                        // [SUPREME]: Descargamos la imagen original SIN COMPRESIÓN inicial
                        val bytes = ImageUtils.getBytesFromUri(contexto, android.net.Uri.parse(originalPhotoUrl))
                        bytes?.let {
                            // Guardamos el original de alta calidad
                            rutaLocal = ImageUtils.saveBytesToFile(contexto, it, "perfil_cli_$uid")
                            // Generamos la miniatura a partir del original de alta calidad
                            miniatura = ImageUtils.generateThumbnailFromBytes(it)
                        }
                    }
                }

                usuarioDao.insertar(IdentidadUsuarioEntity(
                    id = uid,
                    nombre = nombre,
                    apellido = apellido,
                    nombreVisible = nombreCompleto,
                    correoElectronico = email,
                    numeroTelefono = telefono,
                    urlFotoPerfil = rutaLocal,
                    miniaturaBase64 = miniatura,
                    ultimaSincronizacion = 0L
                ))
            }

            // 2. Disparar Restauración Pesada delegada a MotorSincLocal
            externalScope.launch {
                android.util.Log.d("SincUsuarioRepo", "⚙️ [WARMUP_GLOBAL] Delegando descarga a MotorSincLocal...")
                try {
                    motorLocal.impactarUsuarioDeep(uid)
                } catch (e: Exception) {
                    android.util.Log.e("SincUsuarioRepo", "⚠️ [WARMUP_RETRY] Fallo inicial. Encolando Worker.")
                    gestorSincronizacion.get().encolarRestauracionPull(uid)
                }
            }

        } catch (e: Exception) {
            android.util.Log.e("SincUsuarioRepo", "❌ [ACCESO_ERR] Fallo crítico al preparar Room: ${e.message}")
        }
    }

    /**
     * 🔥 [ELITE]: Sincroniza la identidad base y la cuenta en la nube (PUSH Shallow).
     */
    suspend fun subirPerfilUsuarioShallow(uid: String) {
        try {
            android.util.Log.d("SincUsuarioRepo", "📤 [PUSH_SHALLOW_START] Iniciando subida ligera para $uid...")
            val localU = usuarioDao.obtenerPorId(uid).first() ?: return
            val localC = cuentaDao.obtenerPorId(uid).first() ?: return
            
            val nubeU = motorRemoto.obtenerUsuario(uid)
            if (nubeU != null) {
                if (nubeU.ultimaSincronizacion >= localU.ultimaSincronizacion) {
                    android.util.Log.d("SincUsuarioRepo", "🛡️ [PUSH_SKIP] Nube ya está actualizada o es más reciente.")
                    return
                }
            }

            val batch = nubeFirestore.batch()
            batch.set(nubeFirestore.collection(MotorSincRemoto.COL_CLIENTE).document(uid), localU)
            batch.set(nubeFirestore.collection(MotorSincRemoto.COL_CUENTA).document(uid), localC)
            
            batch.commit().await()
            android.util.Log.d("SincUsuarioRepo", "✅ [PUSH_SHALLOW_OK] Identidad base sincronizada.")
        } catch (e: Exception) {
            android.util.Log.e("SincUsuarioRepo", "❌ [PUSH_SHALLOW_ERROR] ${e.message}")
        }
    }

    /**
     * 🔥 [ELITE]: Sincroniza el ecosistema completo (PUSH Deep).
     */
    suspend fun subirPerfilUsuarioCompleto(uid: String) {
        try {
            android.util.Log.d("SincUsuarioRepo", "📤 [PUSH_DEEP_START] Iniciando subida jerárquica para $uid...")
            subirPerfilUsuarioShallow(uid)
            
            val batch = nubeFirestore.batch()
            val rootRef = nubeFirestore.collection(MotorSincRemoto.COL_CLIENTE).document(uid)
            
            // [v2026.ELITE]: Filtramos direcciones por idSucursal para evitar duplicidad jerárquica
            val localesDir = direccionDao.obtenerPorPropietario(uid).first().filter { it.idSucursal == null }
            if (localesDir.isNotEmpty()) {
                localesDir.forEach { batch.set(rootRef.collection(MotorSincRemoto.SUB_DIRECCIONES).document(it.id), it) }
            }
            
            val empresas = empresaDao.obtenerPorPropietario(uid).first()
            empresas.forEach { emp ->
                val empRef = rootRef.collection(MotorSincRemoto.SUB_EMPRESAS).document(emp.id)
                batch.set(empRef, emp)
                
                val sucursales = sucursalDao.obtenerPorEmpresa(emp.id).first()
                sucursales.forEach { suc ->
                    val sucRef = empRef.collection(MotorSincRemoto.SUB_SUCURSALES).document(suc.id)
                    batch.set(sucRef, suc)
                    
                    // [FIX]: Buscamos por ID de sucursal explícito para mayor precisión
                    val dirsSuc = direccionDao.obtenerPorSucursal(suc.id).first()
                    dirsSuc.forEach { batch.set(sucRef.collection(MotorSincRemoto.SUB_DIRECCIONES).document(it.id), it) }
                }
            }
            
            batch.commit().await()
            android.util.Log.d("SincUsuarioRepo", "✅ [PUSH_DEEP_EXITO] Jerarquía completa sincronizada para $uid.")
        } catch (e: Exception) {
            android.util.Log.e("SincUsuarioRepo", "❌ [PUSH_DEEP_ERROR] ${e.message}")
        }
    }

    /**
     * [REDUNDANCIA ELIMINADA]: Delegado a MotorSincLocal.
     */
    suspend fun descargarPerfilUsuarioShallow(uid: String) {
        motorLocal.impactarUsuarioShallow(uid)
    }

    /**
     * [REDUNDANCIA ELIMINADA]: Delegado a MotorSincLocal.
     */
    suspend fun descargarPerfilUsuarioCompleto(uid: String) {
        motorLocal.impactarUsuarioDeep(uid)
    }

    /**
     * 🔥 [ELITE]: Guarda el perfil del usuario en Room y encola su sincronización.
     */
    suspend fun guardarUsuarioLocalYEncolar(usuario: IdentidadUsuarioEntity, direcciones: List<DireccionDominio> = emptyList()) {
        usuarioDao.insertar(usuario)
        if (direcciones.isNotEmpty()) {
            direccionDao.eliminarPorPropietario(usuario.id)
            // [FIX]: Usamos el mapeador oficial para asegurar integridad de vínculos (idSucursal, idReferencia)
            direccionDao.insertarLista(direcciones.map { 
                com.example.myapplication.core.dominio.mapeadores.DireccionMappers.deDominioAEntidad(it)
                    .copy(idPropietario = usuario.id) // Aseguramos que el dueño raíz sea el usuario
            })
        }
        android.util.Log.d("SincUsuarioRepo", "💾 [LOCAL_SAVE] Encolando sincronización en background.")
        gestorSincronizacion.get().encolarSincronizacionPush(usuario.id)
    }

    /**
     * 🔥 [ELITE]: Sincronización atómica jerárquica para Clientes (Solo PUSH).
     */
    fun sincronizarEcosistemaCompletoPush(uid: String) {
        externalScope.launch {
            android.util.Log.d("SincUsuarioRepo", "📤 [PUSH_DEEP_GLOBAL] Iniciando subida jerárquica cliente...")
            subirPerfilUsuarioCompleto(uid)
        }
    }
}



