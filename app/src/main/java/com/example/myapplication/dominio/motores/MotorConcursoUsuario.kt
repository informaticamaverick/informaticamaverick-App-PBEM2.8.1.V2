package com.example.myapplication.dominio.motores

import com.example.myapplication.core.datos.repositorios.ConcursoPublicoRepositorio
import com.example.myapplication.core.datos.indices.concurso.IndiceConcursoUsuarioRepositorio
import com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity
import com.example.myapplication.core.dominio.mapeadores.discovery.IndiceConcursoShallowMappers
import com.example.myapplication.core.dominio.mapeadores.shallow.UsuarioShallowMappers
import com.example.myapplication.core.datos.local.dao.IdentidadUsuarioDao
import com.example.myapplication.core.datos.local.dao.CuentaDao
import android.util.Log
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- MOTOR DE CONCURSO USUARIO (Azul - v2026.ELITE) ---
 * [PROPÓSITO]: Orquestador de licitaciones públicas para el cliente.
 * [LEY #13]: Atómico. Coordina persistencia local e indexación remota.
 */
@Singleton
class MotorConcursoUsuario @Inject constructor(
    private val repoLocal: ConcursoPublicoRepositorio,
    private val repoIndice: IndiceConcursoUsuarioRepositorio,
    private val usuarioDao: IdentidadUsuarioDao,
    private val cuentaDao: CuentaDao,
    private val firestore: com.google.firebase.firestore.FirebaseFirestore
) {

    /**
     * 🔥 [ELITE]: Publica un nuevo concurso ejecutando el impacto total.
     * [ORDEN]: 1. Room + Multimedia (SSOT) -> 2. Index (Discovery).
     */
    suspend fun publicarNuevoConcurso(concurso: ConcursoPublicoEntity) {
        try {
            Log.d("MOTOR_CONCURSO", "🚀 [ATÓMICO_INICIO] Iniciando publicación de: ${concurso.idConcurso}")
            
            // 1. Persistencia Local y Multimedia (El ancla de la verdad)
            val concursoNormalizado = repoLocal.guardarConcursoLocalConMultimedia(concurso)
            Log.d("MOTOR_CONCURSO", "💾 [ROOM_OK] Concurso persistido localmente. ID: ${concursoNormalizado.idConcurso}")
            
            // 2. Proyección al Índice (Discovery Liviano)
            val usuarioInfo = usuarioDao.obtenerPorId(concurso.idCliente).first()
            val cuentaInfo = cuentaDao.obtenerPorId(concurso.idCliente).first()
            
            if (usuarioInfo != null) {
                Log.d("MOTOR_CONCURSO", "📋 [SHALLOW_PREP] Usuario encontrado en Room: ${usuarioInfo.nombreVisible} | UID: ${concurso.idCliente}")
                val usuarioShallow = UsuarioShallowMappers.deEntidadADominio(
                    usuarioInfo, 
                    cuentaInfo?.estaSuscrito ?: false
                )
                val sobreShallow = IndiceConcursoShallowMappers.deEntidadADominio(
                    concursoNormalizado, 
                    usuarioShallow
                )
                
                Log.d("MOTOR_CONCURSO", "📦 [SHALLOW_READY] Sobre preparado para indexar. CP: ${sobreShallow.codigoPostal} | Categoria: ${sobreShallow.idCategoria}")

                // --- 🔥 [NUEVA LÓGICA]: Índice Shallow de Descubrimiento ---
                repoIndice.publicarConcurso(sobreShallow)
                Log.d("MOTOR_CONCURSO", "✅ [ATÓMICO_OK] Licitación persistida e indexada exitosamente.")

                // --- ⚠️ [LOGICA_ANTERIOR]: Subida completa a Firebase (COMENTADA POR LEY #12) ---
                /*
                val huella = "C_${concursoNormalizado.direccionCodigoPostal}_${concursoNormalizado.idCategoria}"
                val mapaLegacy = com.example.myapplication.core.dominio.mapeadores.ConcursoMappers.aMapaFirestore(concursoNormalizado, huella)
                firestore.collection("concursos").document(concursoNormalizado.idConcurso).set(mapaLegacy).await()
                Log.d("MOTOR_CONCURSO", "📤 [LEGACY_SYNC] Licitación subida a colección 'concursos'.")
                */

            } else {
                Log.e("MOTOR_CONCURSO", "❌ [ATÓMICO_ERR] No se encontró info del usuario (${concurso.idCliente}) en Room. No se puede indexar.")
                // Opcional: lanzar error si la indexación es crítica
                throw Exception("Usuario no encontrado en Room para indexación.")
            }
            
        } catch (e: Exception) {
            Log.e("MOTOR_CONCURSO", "❌ [ATÓMICO_ERR] Fallo en la publicación: ${e.message}", e)
            throw e
        }
    }

    /**
     * Elimina un concurso de todo el sistema.
     */
    suspend fun eliminarConcurso(idConcurso: String) {
        Log.d("MOTOR_CONCURSO", "🗑️ [ELIMINAR] Borrando licitación: $idConcurso")
        repoLocal.eliminarConcursoLocal(idConcurso)
        repoIndice.eliminarConcurso(idConcurso)
    }
}




