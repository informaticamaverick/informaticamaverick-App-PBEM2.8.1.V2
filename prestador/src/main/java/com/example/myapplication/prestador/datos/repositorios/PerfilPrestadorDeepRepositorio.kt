package com.example.myapplication.prestador.datos.repositorios

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.room.withTransaction
import com.example.myapplication.core.datos.local.AppDatabase
import com.example.myapplication.core.datos.local.dao.*
import com.example.myapplication.core.datos.local.entidades.CuentaEntity
import com.example.myapplication.core.datos.local.entidades.IdentidadPrestadorEntity
import com.example.myapplication.core.dominio.mapeadores.*
import com.example.myapplication.core.dominio.modelos.PerfilPrestadorDeepModelo
import com.example.myapplication.core.dominio.motores.MotorSincLocal
import com.example.myapplication.core.utilidades.ImageUtils
import com.example.myapplication.prestador.di.ApplicationScope
import com.example.myapplication.prestador.obreros.GestorSincronizacionPrestador
import com.google.firebase.auth.FirebaseUser
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE PERFIL DEEP (v2026.ELITE) ---
 * [PROPÓSITO]: Proveer el ecosistema profesional completo mediante flujos reactivos.
 * [LEY #9]: Estándar Maverick en Español.
 */
@Singleton
class PerfilPrestadorDeepRepositorio @Inject constructor(
    private val db: AppDatabase,
    private val cuentaDao: CuentaDao,
    private val prestadorDao: IdentidadPrestadorDao,
    private val empresaDao: EmpresaDao,
    private val sucursalDao: SucursalDao,
    private val direccionDao: DireccionDao,
    private val horarioDao: HorarioDao,
    private val equipoTrabajoDao: EquipoTrabajoDao,
    private val recursoDao: RecursoDao,
    private val consultasRepo: ConsultasPrestadorRepositorio,
    private val motorLocal: MotorSincLocal,
    @ApplicationScope private val externalScope: CoroutineScope,
    @ApplicationContext private val contexto: Context,
    private val gestorSincronizacion: Lazy<GestorSincronizacionPrestador>
) {

    /**
     * 🔥 [ELITE]: Obtiene el ecosistema profesional completo en tiempo real.
     * Ensambla el modelo Deep desde la Fuente Única de Verdad (SSOT) en Room.
     */
    fun obtenerPerfilDeepFlujo(uid: String): Flow<PerfilPrestadorDeepModelo?> {
        return consultasRepo.obtenerPerfilPrestadorDeepFlujo(uid)
            .onStart { Log.d("REPO_DEEP", "📡 [SUSCRIPCIÓN] Iniciando flujo Deep para $uid") }
    }

    /**
     * 🔥 [ELITE]: Persiste el ecosistema profesional completo en Room (SSOT Local).
     * [LEY #13]: Atómico. Realiza una limpieza total antes de insertar la nueva jerarquía.
     */
    suspend fun guardarEcosistemaLocal(deep: PerfilPrestadorDeepModelo) {
        val uid = deep.cuenta.id
        try {
            Log.d("REPO_DEEP", "💾 [INICIO_TRANSACCION] Limpiando ecosistema previo para $uid...")
            db.withTransaction {
                // 1. Limpieza de hilos antiguos (Cascada Manual)
                // [FIX]: horarioDao.eliminarPorReferencia(uid) se sacó de acá — borraba el
                // horario SIEMPRE, incluso cuando "deep.prestador.horario" llegaba null (ej. un
                // snapshot del borrador que no cargó el horario), y como el insert de abajo es
                // condicional (?.let), el borrado quedaba sin reemplazo: el horario desaparecía
                // de Room aunque el usuario nunca lo haya tocado. Ahora el borrado+reinserción
                // van juntos, atados a que realmente haya un horario nuevo para escribir.
                direccionDao.eliminarPorPropietario(uid)
                empresaDao.eliminarPorPropietario(uid)
                sucursalDao.eliminarPorPropietario(uid)
                equipoTrabajoDao.eliminarPorPropietario(uid)
                recursoDao.eliminarPorPropietario(uid)
                Log.d("REPO_DEEP", "🧹 [LIMPIEZA_OK] Tablas de infraestructura reseteadas.")

                // 2. Persistencia de Pilares de Identidad
                cuentaDao.insertar(deep.cuenta)
                prestadorDao.insertar(PrestadorMappers.deDominioAEntidad(deep.prestador.perfil))

                deep.prestador.direcciones.forEach {
                    direccionDao.insertar(DireccionMappers.deDominioAEntidad(it))
                }
                deep.prestador.horario?.let {
                    horarioDao.eliminarPorReferencia(uid)
                    horarioDao.insertar(HorarioMappers.deModeloAEntidad(it, uid))
                }
                Log.d("REPO_DEEP", "👤 [IDENTIDAD_OK] Pilar #1 y #2 guardados con ${deep.prestador.direcciones.size} direcciones.")

                // 3. Persistencia de Red de Empresas
                Log.d("REPO_DEEP", "🏢 [PROCESANDO_EMPRESAS] Iniciando guardado de ${deep.empresas.size} empresas.")
                deep.empresas.forEach { empComp ->
                    empresaDao.insertarEmpresa(EmpresaMappers.deDominioAEntidad(empComp.empresa))
                    
                    empComp.sucursales.forEach { sucComp ->
                        sucursalDao.insertarSucursal(SucursalMappers.deDominioAEntidad(sucComp.sucursal))
                        
                        // Infraestructura Local de la Sucursal
                        // [FIX]: forzamos idSucursal acá porque el formulario de alta (HojaRegistroSucursalMav)
                        // nunca lo setea en el DireccionDominio — sin esto se guardaba con idSucursal=null y
                        // el @Relation de SucursalCompletaRelacionesBD (join por idSucursal) nunca encontraba
                        // la dirección, dejando la tarjeta y la hoja de edición completamente en blanco.
                        sucComp.direccion?.let {
                            direccionDao.insertar(DireccionMappers.deDominioAEntidad(it.copy(idSucursal = sucComp.sucursal.id, idPropietario = uid)))
                        }
                        // [FIX]: se limpian los horarios viejos de esta sucursal (branch/equipo/
                        // recurso) ANTES de reinsertar — mismo criterio "borrar+reinsertar" que ya
                        // usa el horario personal (arriba). Sin esto, cada sync agregaba una fila
                        // NUEVA (id = UUID random) en vez de reemplazar, porque REPLACE necesita
                        // que el id coincida para pisar la fila existente.
                        horarioDao.eliminarPorSucursal(sucComp.sucursal.id)
                        sucComp.horario?.let {
                            horarioDao.insertar(HorarioMappers.deModeloAEntidad(it, sucComp.sucursal.id, idSucursal = sucComp.sucursal.id))
                        }
                        sucComp.equipoTrabajo.forEach {
                            equipoTrabajoDao.insertar(EquipoTrabajoMappers.deModeloAEntidad(it, sucComp.sucursal.id))
                            it.horario?.let { h ->
                                horarioDao.insertar(HorarioMappers.deModeloAEntidad(h, it.id, sucComp.sucursal.id, idSucursal = sucComp.sucursal.id, tipo = it.tipoHorario))
                            }
                        }
                        sucComp.recursos.forEach {
                            recursoDao.insertar(RecursoMappers.deModeloAEntidad(it, sucComp.sucursal.id))
                            it.horario?.let { h ->
                                horarioDao.insertar(HorarioMappers.deModeloAEntidad(h, it.id, sucComp.sucursal.id, idSucursal = sucComp.sucursal.id, tipo = it.tipoHorario))
                            }
                        }
                    }
                    Log.d("REPO_DEEP", "🏪 [SUCURSALES_OK] Empresa ${empComp.empresa.nombre} guardada con sus activos.")
                }
            }
            Log.d("REPO_DEEP", "✅ [PERSISTENCIA_FINAL_OK] Ecosistema profesional consolidado en Room.")
        } catch (e: Exception) {
            Log.e("REPO_DEEP", "❌ [PERSISTENCIA_ERR] Fallo crítico al guardar en Room: ${e.message}", e)
            throw e
        }
    }

    /**
     * 🔥 [ELITE]: Descarga la jerarquía completa del prestador desde Firestore e impacta en Room.
     */
    suspend fun descargarEcosistemaCompleto(uid: String) {
        try {
            Log.d("REPO_DEEP", "📥 [PULL_DEEP] Iniciando descarga para $uid")
            motorLocal.impactarPrestadorDeep(uid)
        } catch (e: Exception) {
            Log.e("REPO_DEEP", "❌ [PULL_ERR] Fallo al descargar: ${e.message}")
        }
    }

    suspend fun descargarPerfilShallow(uid: String) {
        motorLocal.impactarPrestadorShallow(uid)
    }

    /**
     * 🔥 [ELITE]: Motor Central de Acceso.
     * Prepara Room para la navegación inmediata y dispara restauración profunda.
     */
    suspend fun finalizarAcceso(usuario: FirebaseUser) {
        val uid = usuario.uid
        val email = usuario.email ?: ""
        val nombreCompleto = usuario.displayName ?: "Prestador Maverick"
        val telefono = usuario.phoneNumber ?: ""
        val originalPhotoUrl = usuario.photoUrl?.toString()?.let { ImageUtils.sanitizeGooglePhotoUrl(it) }

        val partesNombre = nombreCompleto.split(" ")
        val nombre = partesNombre.firstOrNull() ?: ""
        val apellido = if (partesNombre.size > 1) partesNombre.last() else ""

        try {
            // 1. Crear Semilla Inmediata (Navegación Optimista)
            val cuentaExistente = cuentaDao.obtenerPorIdSync(uid)
            if (cuentaExistente == null) {
                cuentaDao.insertar(CuentaEntity(id = uid, correoGoogle = email, ultimaSincronizacion = 0L))
            }

            val prestadorLocal = prestadorDao.obtenerPorIdSync(uid)
            if (prestadorLocal == null) {
                var rutaLocal: String? = null
                var miniatura: String? = null

                if (originalPhotoUrl != null) {
                    withContext(Dispatchers.IO) {
                        // [SUPREME]: Descargamos original SIN COMPRESIÓN
                        val bytes = ImageUtils.getBytesFromUri(contexto, originalPhotoUrl.toUri())
                        bytes?.let {
                            rutaLocal = ImageUtils.saveBytesToFile(contexto, it, "perfil_pre_$uid")
                            miniatura = ImageUtils.generateThumbnailFromBytes(it)
                        }
                    }
                }

                prestadorDao.insertar(IdentidadPrestadorEntity(
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
                Log.d("REPO_DEEP", "⚙️ [WARMUP_GLOBAL] Delegando descarga a MotorSincLocal...")
                try {
                    motorLocal.impactarPrestadorDeep(uid)
                } catch (e: Exception) {
                    Log.e("REPO_DEEP", "⚠️ [WARMUP_RETRY] Fallo inicial. Encolando Worker de respaldo.")
                    gestorSincronizacion.get().encolarRestauracionPull(uid)
                }
            }

        } catch (e: Exception) {
            Log.e("REPO_DEEP", "❌ [ACCESO_ERR] Fallo crítico al preparar Room: ${e.message}")
        }
    }
}
