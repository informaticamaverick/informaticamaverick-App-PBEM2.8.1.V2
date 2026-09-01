package com.example.myapplication.core.dominio.motores

import android.content.Context
import androidx.room.withTransaction
import com.example.myapplication.core.datos.local.AppDatabase
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.utilidades.ImageUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- MOTOR DE SINCRONIZACIÓN LOCAL (PERSISTENCE) ---
 */
@Singleton
class MotorSincLocal @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
    private val motorRemoto: MotorSincRemoto
) {

    private data class EquipoTrabajoWrapper(val miembros: List<EquipoTrabajoEntity> = emptyList())
    private data class RecursoWrapper(val items: List<RecursoEntity> = emptyList())

    suspend fun impactarUsuarioShallow(uid: String) {
        try {
            val nubeU = motorRemoto.obtenerUsuario(uid) ?: return
            val localU = db.usuarioDao().obtenerPorIdSync(uid)

            if (localU == null || nubeU.ultimaSincronizacion > localU.ultimaSincronizacion) {
                val nubeC = motorRemoto.obtenerCuenta(uid)
                
                // [v2026.ELITE]: Descarga de Foto de Usuario para Offline-First
                val fotoLocal = if (nubeU.urlFotoPerfil?.startsWith("http") == true) {
                    ImageUtils.downloadAndSave(context, nubeU.urlFotoPerfil, "u_$uid")
                } else nubeU.urlFotoPerfil
                
                val nubeUConFoto = nubeU.copy(urlFotoPerfil = fotoLocal)

                db.withTransaction {
                    db.usuarioDao().insertar(nubeUConFoto)
                    nubeC?.let { db.CuentaDao().insertar(it) }
                }
                android.util.Log.d("MotorSincLocal", "✅ [PULL_SHALLOW_USUARIO] Room actualizado para $uid.")
            }
        } catch (e: Exception) {
            android.util.Log.e("MotorSincLocal", "❌ [SYNC_LOC_ERR] Error en impactarUsuarioShallow: $uid", e)
        }
    }

    suspend fun impactarUsuarioDeep(uid: String) {
        try {
            val nubeU = motorRemoto.obtenerUsuario(uid) ?: return
            val localU = db.usuarioDao().obtenerPorIdSync(uid)

            if (localU == null || nubeU.ultimaSincronizacion > localU.ultimaSincronizacion) {
                impactarUsuarioShallow(uid)
                val rootRef = motorRemoto.obtenerReferenciaCliente(uid)

                db.withTransaction {
                    val dirsRoot = rootRef.collection(MotorSincRemoto.SUB_DIRECCIONES).get().await()
                        .toObjects(DireccionEntity::class.java).map { 
                            it.copy(idPropietario = uid, idSucursal = null) 
                        }
                    if (dirsRoot.isNotEmpty()) db.direccionDao().insertarLista(dirsRoot)

                    val snapEmp = rootRef.collection(MotorSincRemoto.SUB_EMPRESAS).get().await()
                    val listaEmpresas = snapEmp.toObjects(EmpresaEntity::class.java).map { emp ->
                        // [v2026.ELITE]: Descarga de Logo de Empresa para Offline-First
                        val logoLocal = if (emp.urlFoto?.startsWith("http") == true) {
                            ImageUtils.downloadAndSave(context, emp.urlFoto, "e_${emp.id}")
                        } else emp.urlFoto
                        emp.copy(urlFoto = logoLocal)
                    }
                    if (listaEmpresas.isNotEmpty()) db.empresaDao().insertarLista(listaEmpresas)

                    snapEmp.documents.forEach { doc ->
                        val snapSucs = doc.reference.collection(MotorSincRemoto.SUB_SUCURSALES).get().await()
                        val listaSucs = snapSucs.toObjects(SucursalEntity::class.java)
                        if (listaSucs.isNotEmpty()) {
                            db.sucursalDao().insertarLista(listaSucs)
                            snapSucs.documents.forEach { docSuc ->
                                val sucId = docSuc.id
                                val dirsSuc = docSuc.reference.collection(MotorSincRemoto.SUB_DIRECCIONES).get().await()
                                    .toObjects(DireccionEntity::class.java).map {
                                        it.copy(idPropietario = uid, idSucursal = sucId)
                                    }
                                if (dirsSuc.isNotEmpty()) db.direccionDao().insertarLista(dirsSuc)

                                docSuc.reference.collection(MotorSincRemoto.SUB_INFRAESTRUCTURA).document(MotorSincRemoto.DOC_HORARIO).get().await()
                                    .toObject(HorarioEntity::class.java)?.let { db.horarioDao().insertar(it) }

                                docSuc.reference.collection(MotorSincRemoto.SUB_INFRAESTRUCTURA).document(MotorSincRemoto.DOC_EQUIPO).get().await()
                                    .toObject(EquipoTrabajoWrapper::class.java)?.miembros?.let { db.equipoTrabajoDao().insertarLista(it) }

                                docSuc.reference.collection(MotorSincRemoto.SUB_INFRAESTRUCTURA).document(MotorSincRemoto.DOC_RECURSOS).get().await()
                                    .toObject(RecursoWrapper::class.java)?.items?.let { db.recursoDao().insertarLista(it) }
                            }
                        }
                    }
                }
                android.util.Log.d("MotorSincLocal", "✅ [PULL_DEEP_USUARIO] Jerarquía completa restaurada para $uid.")
            }
        } catch (e: Exception) {
            android.util.Log.e("MotorSincLocal", "❌ [SYNC_LOC_ERR] Error en impactarUsuarioDeep: $uid", e)
        }
    }

    suspend fun impactarPrestadorShallow(uid: String) {
        try {
            val nubeP = motorRemoto.obtenerPrestador(uid) ?: return
            val localP = db.prestadorDao().obtenerPorIdSync(uid)

            if (localP == null || nubeP.ultimaSincronizacion > localP.ultimaSincronizacion) {
                val nubeC = motorRemoto.obtenerCuenta(uid)
                
                // [v2026.ELITE]: Descarga de Foto de Prestador para Offline-First
                val fotoLocal = if (nubeP.urlFotoPerfil?.startsWith("http") == true) {
                    ImageUtils.downloadAndSave(context, nubeP.urlFotoPerfil, "p_$uid")
                } else nubeP.urlFotoPerfil
                
                val nubePConFoto = nubeP.copy(urlFotoPerfil = fotoLocal)

                db.withTransaction {
                    db.prestadorDao().insertar(nubePConFoto)
                    nubeC?.let { db.CuentaDao().insertar(it) }
                }
                android.util.Log.d("MotorSincLocal", "✅ [PULL_SHALLOW_PRESTADOR] Room actualizado para $uid.")
            }
        } catch (e: Exception) {
            android.util.Log.e("MotorSincLocal", "❌ [SYNC_LOC_ERR] Error en impactarPrestadorShallow: $uid", e)
        }
    }

    suspend fun impactarPrestadorDeep(uid: String) {
        try {
            val nubeP = motorRemoto.obtenerPrestador(uid) ?: return
            val localP = db.prestadorDao().obtenerPorIdSync(uid)

            // [v2026.ELITE]: Forzar descarga si no hay datos locales, hay una versión más nueva, o la carga previa fue incompleta.
            if (localP == null || nubeP.ultimaSincronizacion > localP.ultimaSincronizacion || !localP.esCargaCompleta) {
                impactarPrestadorShallow(uid)
                val rootRef = motorRemoto.obtenerReferenciaPrestador(uid)

                db.withTransaction {
                    val dirsRoot = rootRef.collection(MotorSincRemoto.SUB_DIRECCIONES).get().await()
                        .toObjects(DireccionEntity::class.java).map { 
                            it.copy(idPropietario = uid, idSucursal = null) 
                        }
                    if (dirsRoot.isNotEmpty()) db.direccionDao().insertarLista(dirsRoot)
                    
                    rootRef.collection(MotorSincRemoto.SUB_INFRAESTRUCTURA).document(MotorSincRemoto.DOC_HORARIO).get().await()
                        .toObject(HorarioEntity::class.java)?.let { db.horarioDao().insertar(it) }

                    val snapEmp = rootRef.collection(MotorSincRemoto.SUB_EMPRESAS).get().await()
                    val listaEmpresas = snapEmp.toObjects(EmpresaEntity::class.java).map { emp ->
                        // [v2026.ELITE]: Descarga de Logo de Empresa para Offline-First
                        val logoLocal = if (emp.urlFoto?.startsWith("http") == true) {
                            ImageUtils.downloadAndSave(context, emp.urlFoto, "e_${emp.id}")
                        } else emp.urlFoto
                        emp.copy(urlFoto = logoLocal)
                    }
                    if (listaEmpresas.isNotEmpty()) db.empresaDao().insertarLista(listaEmpresas)

                    snapEmp.documents.forEach { doc ->
                        val snapSucs = doc.reference.collection(MotorSincRemoto.SUB_SUCURSALES).get().await()
                        val listaSucs = snapSucs.toObjects(SucursalEntity::class.java)
                        if (listaSucs.isNotEmpty()) {
                            db.sucursalDao().insertarLista(listaSucs)
                            snapSucs.documents.forEach { docSuc ->
                                val sucId = docSuc.id
                                val dirsSuc = docSuc.reference.collection(MotorSincRemoto.SUB_DIRECCIONES).get().await()
                                    .toObjects(DireccionEntity::class.java).map {
                                        it.copy(idPropietario = uid, idSucursal = sucId)
                                    }
                                if (dirsSuc.isNotEmpty()) db.direccionDao().insertarLista(dirsSuc)
                                
                                docSuc.reference.collection(MotorSincRemoto.SUB_INFRAESTRUCTURA).document(MotorSincRemoto.DOC_HORARIO).get().await()
                                    .toObject(HorarioEntity::class.java)?.let { db.horarioDao().insertar(it) }

                                docSuc.reference.collection(MotorSincRemoto.SUB_INFRAESTRUCTURA).document(MotorSincRemoto.DOC_EQUIPO).get().await()
                                    .toObject(EquipoTrabajoWrapper::class.java)?.miembros?.let { db.equipoTrabajoDao().insertarLista(it) }

                                docSuc.reference.collection(MotorSincRemoto.SUB_INFRAESTRUCTURA).document(MotorSincRemoto.DOC_RECURSOS).get().await()
                                    .toObject(RecursoWrapper::class.java)?.items?.let { db.recursoDao().insertarLista(it) }
                            }
                        }
                    }
                }
                android.util.Log.d("MotorSincLocal", "✅ [PULL_DEEP_PRESTADOR] Ecosistema completo restaurado para $uid.")

                db.prestadorDao().obtenerPorIdSync(uid)?.let {
                    db.prestadorDao().insertar(it.copy(esCargaCompleta = true, ultimaSincronizacion = System.currentTimeMillis()))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MotorSincLocal", "❌ [SYNC_LOC_ERR] Error en impactarPrestadorDeep: $uid", e)
        }
    }
}
