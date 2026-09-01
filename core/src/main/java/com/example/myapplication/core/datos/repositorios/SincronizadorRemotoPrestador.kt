package com.example.myapplication.core.datos.repositorios

import com.google.firebase.storage.FirebaseStorage
import androidx.core.net.toUri
import android.net.Uri
import com.example.myapplication.core.datos.local.dao.*
import com.example.myapplication.core.dominio.motores.MotorSincRemoto
import com.example.myapplication.core.utilidades.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

/**
 * --- SINCRONIZADOR REMOTO PRESTADOR (PUSH - v2026.ELITE) ---
 * [RESPONSABILIDAD]: Único encargado de subir identidades soberanas a Firestore.
 * [LEY #13]: Atómico. Solo maneja la subida de datos de identidad y empresa.
 */
@Singleton
class SincronizadorRemotoPrestador @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val prestadorDao: IdentidadPrestadorDao,
    private val cuentaDao: CuentaDao,
    private val empresaDao: EmpresaDao,
    private val sucursalDao: SucursalDao,
    private val direccionDao: DireccionDao
) {

    /**
     * 🔥 [ELITE]: Sincroniza la identidad base y la cuenta en la nube (PUSH Shallow).
     * [ORDEN]: 1. Extraer datos -> 2. Resolver Foto Remota -> 3. Push Batch.
     */
    suspend fun subirIdentidadBase(uid: String) {
        try {
            Log.d("SYNC_REMOTO", "☁️ [INICIO_BATCH_BASE] Preparando subida de Identidad y Cuenta para $uid")
            val localP = prestadorDao.obtenerPorIdSync(uid) ?: return
            val localC = cuentaDao.obtenerPorIdSync(uid) ?: return

            // [SUPREME.FIX]: Garantizamos que en la nube (DEEP) vaya la URL original de Google
            val photoUrlGoogle = FirebaseAuth.getInstance().currentUser?.photoUrl?.toString()?.let { 
                ImageUtils.sanitizeGooglePhotoUrl(it) 
            }

            // [v2026.SUPREME]: Subida de Foto Local a Storage antes del Push de Identidad
            val photoUrlFinal = if (photoUrlGoogle != null) {
                photoUrlGoogle
            } else if (localP.urlFotoPerfil != null && !localP.urlFotoPerfil.startsWith("http")) {
                subirFotoPerfil(uid, localP.urlFotoPerfil) ?: localP.urlFotoPerfil
            } else {
                localP.urlFotoPerfil
            }

            val pMap = mapOf(
                "id" to localP.id,
                "nombre" to localP.nombre,
                "apellido" to localP.apellido,
                "nombreVisible" to localP.nombreVisible,
                "biografia" to localP.biografia,
                "cuitCuil" to localP.cuitCuil,
                "correoElectronico" to localP.correoElectronico,
                "numeroTelefono" to localP.numeroTelefono,
                "urlFotoPerfil" to photoUrlFinal,
                "miniaturaBase64" to localP.miniaturaBase64,
                "idCategorias" to localP.idCategorias,
                "especialidades" to localP.especialidades,
                "matricula" to localP.matricula,
                "reputacion" to localP.reputacion,
                "totalReseñas" to localP.totalReseñas,
                "trabajosRealizados" to localP.trabajosRealizados,
                "likes" to localP.likes,
                "dislikes" to localP.dislikes,
                "nivelElite" to localP.nivelElite,
                "estaVerificado" to localP.estaVerificado,
                "estaEnLinea" to localP.estaEnLinea,
                "brindaServicio" to localP.brindaServicio,
                "brindaProducto" to localP.brindaProducto,
                "atiende24Horas" to localP.atiende24Horas,
                "visitaADomicilio" to localP.visitaADomicilio,
                "realizaEnvios" to localP.realizaEnvios,
                "brindaTurnos" to localP.brindaTurnos,
                "usaAgendaRecursos" to localP.usaAgendaRecursos,
                "capacidadSimultanea" to localP.capacidadSimultanea,
                "tieneLocalFisico" to localP.tieneLocalFisico,
                "esCargaCompleta" to localP.esCargaCompleta,
                "ultimaSincronizacion" to localP.ultimaSincronizacion,
                "fechaCreacion" to localP.fechaCreacion
            )

            val batch = firestore.batch()
            batch.set(firestore.collection(MotorSincRemoto.COL_PRESTADOR).document(uid), pMap)
            batch.set(firestore.collection(MotorSincRemoto.COL_CUENTA).document(uid), localC)
            
            batch.commit().await()
            Log.d("SYNC_REMOTO", "✅ [IDENTIDAD_BASE_OK] Perfil (con Foto Google) y Cuenta sincronizados.")
        } catch (e: Exception) {
            Log.e("SYNC_REMOTO", "❌ [IDENTIDAD_BASE_ERR] Fallo al subir identidad base: ${e.message}")
            throw e
        }
    }

    /**
     * 🔥 [ELITE]: Sincroniza el ecosistema jerárquico completo (PUSH Deep).
     * [LEY #6]: Soberanía. Excluye infraestructura privada (Horarios, Recursos).
     */
    suspend fun subirEcosistemaCompleto(uid: String) {
        try {
            Log.d("SYNC_REMOTO", "📤 [INICIO_PUSH_DEEP] Subiendo jerarquía profesional completa para $uid")
            
            // 1. Identidad Base
            subirIdentidadBase(uid)

            val batch = firestore.batch()
            val rootRef = firestore.collection(MotorSincRemoto.COL_PRESTADOR).document(uid)
            
            // [v2026.ELITE]: Filtramos direcciones por idSucursal para evitar duplicidad jerárquica
            val direccionesP = direccionDao.obtenerPorPropietarioSync(uid).filter { it.idSucursal == null }
            direccionesP.forEach { 
                batch.set(rootRef.collection(MotorSincRemoto.SUB_DIRECCIONES).document(it.id), it) 
            }
            Log.d("SYNC_REMOTO", "📍 [DEEP_STEP] ${direccionesP.size} direcciones personales añadidas al batch.")

            // 3. Empresas y sus Sucursales
            val empresas = empresaDao.obtenerPorPropietarioSync(uid)
            Log.d("SYNC_REMOTO", "🏢 [DEEP_STEP] Procesando ${empresas.size} empresas vinculadas...")

            empresas.forEach { emp ->
                val empRef = rootRef.collection(MotorSincRemoto.SUB_EMPRESAS).document(emp.id)
                
                // [v2026.SUPREME]: Subida de Logo de Empresa a Storage
                val logoUrlFinal = if (emp.urlFoto != null && !emp.urlFoto.startsWith("http")) {
                    subirFotoEmpresa(uid, emp.id, emp.urlFoto) ?: emp.urlFoto
                } else emp.urlFoto
                
                val empConUrl = emp.copy(urlFoto = logoUrlFinal)
                batch.set(empRef, empConUrl)
                
                val sucursales = sucursalDao.obtenerPorEmpresaSync(emp.id)
                Log.d("SYNC_REMOTO", "🏪 [DEEP_STEP] Empresa ${emp.nombre} -> Añadiendo ${sucursales.size} sucursales.")

                sucursales.forEach { suc ->
                    val sucRef = empRef.collection(MotorSincRemoto.SUB_SUCURSALES).document(suc.id)
                    batch.set(sucRef, suc)
                    
                    // [FIX]: Buscamos por ID de sucursal explícito para mayor precisión
                    val direccionesS = direccionDao.obtenerPorSucursalSync(suc.id)
                    direccionesS.forEach { 
                        batch.set(sucRef.collection(MotorSincRemoto.SUB_DIRECCIONES).document(it.id), it) 
                    }
                }
            }
            
            Log.d("SYNC_REMOTO", "⏳ [COMMIT_BATCH] Enviando batch jerárquico a Firestore...")
            batch.commit().await()
            Log.d("SYNC_REMOTO", "✅ [PUSH_DEEP_OK] Toda la jerarquía soberana de $uid ha sido sincronizada.")
        } catch (e: Exception) {
            Log.e("SYNC_REMOTO", "❌ [PUSH_DEEP_ERR] Fallo crítico en la subida jerárquica: ${e.message}")
            throw e
        }
    }

    suspend fun actualizarEstadoSuscripcionElite(uid: String, activa: Boolean) {
        try {
            firestore.collection(MotorSincRemoto.COL_CUENTA).document(uid).update("estaSuscrito", activa).await()
            Log.d("SYNC_REMOTO", "💳 [ELITE_SYNC] Estatus de suscripción actualizado en nube: $activa")
        } catch (e: Exception) {
            Log.e("SYNC_REMOTO", "❌ [ELITE_SYNC_ERR] ${e.message}")
        }
    }

    suspend fun cambiarModoSoberania(uid: String, idPerfilActivo: String?, priorizarEmpresa: Boolean) {
        try {
            val updates = mapOf(
                "idPerfilActivo" to idPerfilActivo,
                "priorizarEmpresa" to priorizarEmpresa
            )
            firestore.collection(MotorSincRemoto.COL_CUENTA).document(uid).update(updates).await()
            Log.d("SYNC_REMOTO", "⚖️ [SOBERANIA_SYNC] Modo de soberanía actualizado en nube.")
        } catch (e: Exception) {
            Log.e("SYNC_REMOTO", "❌ [SOBERANIA_ERR] ${e.message}")
        }
    }

    /**
     * Verifica si el prestador ya tiene un registro de identidad en la nube.
     */
    suspend fun existeEnRemoto(uid: String): Boolean = try {
        val doc = firestore.collection(MotorSincRemoto.COL_PRESTADOR).document(uid).get().await()
        doc.exists()
    } catch (e: Exception) {
        false
    }

    private suspend fun subirFotoPerfil(uid: String, pathLocal: String): String? {
        return try {
            val file = java.io.File(pathLocal)
            if (!file.exists()) return null
            
            val ref = storage.reference.child("perfiles/$uid/foto_perfil.webp")
            ref.putFile(file.toUri()).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("SYNC_REMOTO", "❌ Error al subir foto de perfil: ${e.message}")
            null
        }
    }

    private suspend fun subirFotoEmpresa(uid: String, empresaId: String, pathLocal: String): String? {
        return try {
            val file = java.io.File(pathLocal)
            if (!file.exists()) return null
            
            val ref = storage.reference.child("empresas/$uid/$empresaId/logo.webp")
            ref.putFile(file.toUri()).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("SYNC_REMOTO", "❌ Error al subir logo de empresa: ${e.message}")
            null
        }
    }
}



