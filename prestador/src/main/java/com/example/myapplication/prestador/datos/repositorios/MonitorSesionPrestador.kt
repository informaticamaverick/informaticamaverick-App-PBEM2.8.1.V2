package com.example.myapplication.prestador.datos.repositorios

import com.example.myapplication.core.dominio.motores.MotorSincRemoto
import com.example.myapplication.prestador.di.ApplicationScope
import com.example.myapplication.core.datos.local.dao.IdentidadPrestadorDao
import com.example.myapplication.core.datos.local.entidades.NotificacionEntity
import com.example.myapplication.core.datos.repositorios.NotificacionRepositorio
import com.example.myapplication.core.dominio.modelos.TipoNotificacion
import com.example.myapplication.core.servicios.notificaciones.Notificador
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- MONITOR DE SESIÓN EN VIVO (BANEO EN TIEMPO REAL) ---
 * [PROPÓSITO]: Detectar si el prestador logueado fue baneado desde el panel admin
 * MIENTRAS sigue navegando la app (no solo al próximo login). Se suscribe con un
 * listener de Firestore a `prestadores/{uid}` apenas hay sesión activa, y se apaga
 * solo cuando el usuario cierra sesión.
 */
@Singleton
class MonitorSesionPrestador @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: PrestadorAutenticacionRepositorio,
    private val prestadorDao: IdentidadPrestadorDao,
    private val notificador: Notificador,
    private val notificacionRepository: NotificacionRepositorio,
    @ApplicationScope private val scope: CoroutineScope
) {
    private val _cuentaSuspendida = MutableStateFlow<String?>(null)
    val cuentaSuspendida: StateFlow<String?> = _cuentaSuspendida.asStateFlow()

    private val _matriculaVerificada = MutableStateFlow(false)
    val matriculaVerificada: StateFlow<Boolean> = _matriculaVerificada.asStateFlow()

    private var ultimoEstaVerificadoConocido: Boolean? = null

    private var listener: ListenerRegistration? = null

    init {
        scope.launch {
            authRepository.observarUsuarioActual().collect { usuario ->
                listener?.remove()
                listener = null

                if (usuario == null) {
                    _cuentaSuspendida.value = null
                    ultimoEstaVerificadoConocido = null
                    return@collect
                }

                // Arranca desde lo que Room ya tenía persistido (no desde null), para no repetir
                // el aviso de "recién verificado" en cada reinicio de la app si ya estaba
                // verificado de una sesión anterior — solo debe avisar la primera vez real.
                ultimoEstaVerificadoConocido = prestadorDao.obtenerPorIdSync(usuario.uid)?.estaVerificado

                listener = firestore.collection(MotorSincRemoto.COL_PRESTADOR)
                    .document(usuario.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                        val baneado = snapshot.getBoolean("banned") == true
                        _cuentaSuspendida.value = if (baneado) (snapshot.getString("banReason") ?: "") else null
                        //Matricula verificada desde el panel de admin: el pull normal (MotorSincLocal)
                        // no lo baja porque el panel no toca 'ultimaSincronizacion'. Se escribe Acá
                        // directo de Room para el badge de la app refleje el cambio en vivo.
                        // Matrícula verificada desde el panel admin: el pull normal (MotorSincLocal)
                        // no lo baja porque el panel no toca 'ultimaSincronizacion'. Se escribe acá
                        // directo a Room para que el badge de la app refleje el cambio en vivo.
                        val verificado = snapshot.getBoolean("estaVerificado") == true
                        // Comparación SÍNCRONA (no contra Room) para evitar el duplicado: Firestore
                        // entrega el snapshot "desde caché" y luego "desde servidor" casi al mismo
                        // tiempo, y si comparáramos contra Room (escritura async) ambos podrían leer
                        // el valor viejo antes de que el primero termine de escribir.
                        val yaEstabaVerificado = ultimoEstaVerificadoConocido == true
                        ultimoEstaVerificadoConocido = verificado

                        scope.launch {
                            prestadorDao.actualizarVerificacion(usuario.uid, verificado)

                            // Recién verificado ahora (no lo estaba antes): aviso amigable,
                            // mismo Notificador que ya usa el sistema de mensajes/licitaciones,
                            // más un mensaje dentro de la app por si el usuario ya está navegando.
                            if (verificado && !yaEstabaVerificado) {
                                val titulo = "🎖️ ¡Tu matrícula fue verificada!"
                                val mensaje = "Ya tenés la insignia de verificado en tu perfil. Mostrale a tus clientes que sos un profesional confiable."
                                notificador.mostrarAvisoGeneral(titulo, mensaje)
                                notificacionRepository.insertar(
                                    NotificacionEntity(
                                        tipo = TipoNotificacion.SISTEMA.name,
                                        titulo = titulo,
                                        mensaje = mensaje,
                                        fechaMs = System.currentTimeMillis(),
                                        leida = false,
                                        rutaAccion = null
                                    )
                                )
                                _matriculaVerificada.value = true
                            }
                        }
                    }
            }
        }
    }

    fun limpiarAlertaSuspension() {
        _cuentaSuspendida.value = null
    }

    fun limpiarAlertaVerificacion() {
        _matriculaVerificada.value = false
    }
}


