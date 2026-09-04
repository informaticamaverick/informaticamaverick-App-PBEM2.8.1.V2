package com.example.myapplication.prestador.dominio.motores

import com.example.myapplication.core.datos.repositorios.SincronizadorRemotoPrestador
import com.example.myapplication.core.datos.indices.busqueda.IndiceBusquedaPrestadorRepositorio
import com.example.myapplication.core.dominio.modelos.PerfilPrestadorDeepModelo
import com.example.myapplication.prestador.datos.repositorios.PerfilPrestadorDeepRepositorio
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- MOTOR DE PERFIL PRESTADOR DEEP (v2026.ELITE) ---
 * [PROPÓSITO]: Orquestador maestro de la sincronización soberana.
 * [LEY #13]: Atómico. Coordina la persistencia local, remota e indexación.
 */
@Singleton
class MotorPerfilPrestadorDeep @Inject constructor(
    private val repoLocal: PerfilPrestadorDeepRepositorio,
    private val repoRemoto: SincronizadorRemotoPrestador,
    private val repoIndice: IndiceBusquedaPrestadorRepositorio,
    private val motorConcursoPrestador: MotorIndiceConcursoPrestador
) {

    /**
     * 🔥 [ELITE]: Ejecuta el impacto total de cambios.
     * [ORDEN]: 1. Room (SSOT) -> 2. Cloud (Push Deep) -> 3. Index (Discovery).
     */
    suspend fun impactarEcosistemaYActualizarIndices(deep: PerfilPrestadorDeepModelo) {
        val uid = deep.cuenta.id
        try {
            Log.d("MOTOR_DEEP", "🚀 [CADENA_ATÓMICA_INICIO] Procesando impacto profundo para: $uid")
            
            // 1. Persistencia Local (El ancla de la verdad)
            Log.d("MOTOR_DEEP", "📦 [PASO_1_ROOM] Guardando ecosistema en base de datos local...")
            repoLocal.guardarEcosistemaLocal(deep)
            
            // 2. Sincronización Remota (Los Pilares de Identidad)
            Log.d("MOTOR_DEEP", "☁️ [PASO_2_FIREBASE] Subiendo identidades soberanas a la nube...")
            repoRemoto.subirEcosistemaCompleto(uid)

            // [FIX 04/09]: subirEcosistemaCompleto (subirIdentidadBase) ya NO sube
            // idPerfilActivo/priorizarEmpresa — ese push corre también en cada apertura de la
            // app con Room de ESTE dispositivo y pisaba el toggle hecho desde otro dispositivo.
            // Acá sí corresponde subirlos: este método solo se llama en registro y en guardado
            // explícito del usuario, donde el Room local es realmente la intención del usuario.
            repoRemoto.cambiarModoSoberania(uid, deep.cuenta.idPerfilActivo, deep.cuenta.priorizarEmpresa)

            // 3. Actualización de Visibilidad (Discovery Liviano)
            Log.d("MOTOR_DEEP", "🔍 [PASO_3_ÍNDICE] Proyectando datos livianos al índice de búsqueda...")
            repoIndice.sincronizarTodoElDescubrimiento(uid)

            // 4. Sincronización de Oportunidades de Trabajo (FCM Topics)
            val cp = deep.prestador.direcciones.firstOrNull()?.codigoPostal ?: ""
            val categorias = deep.prestador.perfil.idCategorias
            if (cp.isNotBlank() && categorias.isNotEmpty()) {
                Log.d("MOTOR_DEEP", "📡 [PASO_4_FCM] Sincronizando tópicos de concursos para CP: $cp")
                motorConcursoPrestador.sincronizarOportunidadesDeTrabajo(cp, categorias)
            }

            Log.d("MOTOR_DEEP", "✅ [CADENA_ATÓMICA_OK] El perfil es ahora visible y persistente en todo el ecosistema.")
        } catch (e: Exception) {
            Log.e("MOTOR_DEEP", "❌ [CADENA_ATÓMICA_ERR] Fallo crítico en la sincronización: ${e.message}", e)
            throw e
        }
    }
}
