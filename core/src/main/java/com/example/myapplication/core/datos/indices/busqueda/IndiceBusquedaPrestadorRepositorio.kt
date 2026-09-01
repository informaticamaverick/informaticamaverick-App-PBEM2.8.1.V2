package com.example.myapplication.core.datos.indices.busqueda

import com.example.myapplication.core.datos.local.dao.DireccionDao
import com.example.myapplication.core.datos.local.dao.CuentaDao
import com.example.myapplication.core.dominio.descubrimiento.GeneradorTópicosFCM
import com.example.myapplication.core.dominio.descubrimiento.ProtocoloPrefijos
import com.example.myapplication.core.dominio.modelos.shallow.PrestadorShallowDominio
import com.example.myapplication.core.dominio.modelos.shallow.SucursalShallowDominio
import com.example.myapplication.core.dominio.mapeadores.shallow.PrestadorShallowMappers
import com.example.myapplication.core.dominio.mapeadores.shallow.SucursalShallowMappers
import com.example.myapplication.core.dominio.ubicacion.CalculadoraGeografica
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.example.myapplication.core.datos.local.dao.EmpresaDao
import com.example.myapplication.core.datos.local.dao.IdentidadPrestadorDao
import com.example.myapplication.core.datos.local.dao.SucursalDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE ESCRITURA: ÍNDICE DE BÚSQUEDA (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Orquestar la visibilidad de profesionales y sucursales en Firebase.
 * [LEY #17]: Protocolo de Bautizo. Actúa como el Escritor del índice.
 */
@Singleton
class IndiceBusquedaPrestadorRepositorio @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val generadorTopicos: GeneradorTópicosFCM,
    private val prestadorDao: IdentidadPrestadorDao,
    private val empresaDao: EmpresaDao,
    private val sucursalDao: SucursalDao,
    private val direccionDao: DireccionDao,
    private val cuentaDao: CuentaDao
) {
    companion object {
        const val COLECCION_INDICE = "indice_busqueda"
    }

    /**
     * 🔥 [ELITE]: Sincroniza todo el ecosistema de descubrimiento de un profesional.
     * Proyecta el Perfil Personal + Todas las Sucursales activas al índice global.
     */
    suspend fun sincronizarTodoElDescubrimiento(uid: String) {
        try {
            Log.d("INDICE_MAESTRO", "📡 [INICIO_SYNC] Proyectando visibilidad para: $uid")
            
            val prestador = prestadorDao.obtenerPorIdSync(uid) ?: return
            val cuenta = cuentaDao.obtenerPorIdSync(uid)
            val estaSuscrito = cuenta?.estaSuscrito ?: false

            // --- 1. PROYECCIÓN PERFIL PERSONAL ---
            // [ELITE]: Indexamos el perfil profesional (Las categorías mandan en la visibilidad del buscador)
            val direcciones = direccionDao.obtenerPorPropietarioSync(uid)
            val dirBase = direcciones.find { it.idReferencia == uid } ?: direcciones.firstOrNull()
            
            val shallowPersonal = com.example.myapplication.core.dominio.mapeadores.shallow.PrestadorShallowMappers.deEntidadADominio(
                entidad = prestador,
                direccion = dirBase,
                estaSuscrito = estaSuscrito
            )
            publicarPerfilProfesional(shallowPersonal)

            // --- 2. PROYECCIÓN SUCURSALES (MODO EMPRESA) ---
            val empresas = empresaDao.obtenerPorPropietarioSync(uid)
            empresas.forEach { emp ->
                val sucursales = sucursalDao.obtenerPorEmpresaSync(emp.id)
                sucursales.forEach { suc ->
                    // [ELITE]: La sucursal se indexa incondicionalmente (Hereda rubros de la empresa madre)
                    val dirSuc = direccionDao.obtenerPorPropietarioSync(suc.id).firstOrNull()
                    
                    val shallowSuc = com.example.myapplication.core.dominio.mapeadores.shallow.SucursalShallowMappers.deEntidadADominio(
                        sucursal = suc,
                        empresa = emp,
                        direccion = dirSuc,
                        estaSuscrito = estaSuscrito
                    )
                    publicarSucursalComoExperto(shallowSuc)
                }
            }
            
            Log.d("INDICE_MAESTRO", "✅ [FIN_SYNC] Ecosistema de búsqueda actualizado.")
        } catch (e: Exception) {
            Log.e("INDICE_MAESTRO", "❌ [SYNC_ERR] Fallo al actualizar índices: ${e.message}")
        }
    }

    /**
     * Publica el perfil profesional con etiquetas de Zona (CP) y Proximidad (Geohash).
     */
    private suspend fun publicarPerfilProfesional(perfil: PrestadorShallowDominio) {
        val etiquetasFinales = generarEtiquetasParaRubros(perfil.idCategorias, perfil.codigoPostal, perfil.latitud, perfil.longitud)
        
        Log.d("INDICE_MAESTRO", "👤 [GEN_TAGS_PERFIL] ID: ${perfil.id} | Rubros: ${perfil.idCategorias} | CP: ${perfil.codigoPostal} -> Tags: $etiquetasFinales")
        
        val perfilConEtiquetas = perfil.copy(filtrosBusqueda = etiquetasFinales)
        val mapa = PrestadorShallowMappers.deDominioAMapa(perfilConEtiquetas)
        
        firestore.collection(COLECCION_INDICE).document(perfil.id).set(mapa).await()
        Log.d("INDICE_MAESTRO", "✅ [PUBLICADO_PERFIL] Visibilidad actualizada en Firestore.")
    }

    /**
     * Publica una sucursal con el contexto de su empresa madre.
     */
    private suspend fun publicarSucursalComoExperto(sucursal: SucursalShallowDominio) {
        val etiquetasFinales = generarEtiquetasParaRubros(sucursal.idCategorias, sucursal.codigoPostal, sucursal.latitud, sucursal.longitud).toMutableList()
        
        // Etiqueta de Marca
        if (sucursal.idEmpresaPadre.isNotBlank()) {
            etiquetasFinales.add("MARCA_${sucursal.idEmpresaPadre}")
        }

        Log.d("INDICE_MAESTRO", "🏪 [GEN_TAGS_SUCURSAL] ID: ${sucursal.id} | Empresa: ${sucursal.nombreEmpresa} -> Tags: $etiquetasFinales")

        val sucursalConEtiquetas = sucursal.copy(filtrosBusqueda = etiquetasFinales.distinct())
        val mapa = SucursalShallowMappers.deDominioAMapa(sucursalConEtiquetas)
        
        firestore.collection(COLECCION_INDICE).document(sucursal.id).set(mapa).await()
        Log.d("INDICE_MAESTRO", "✅ [PUBLICADA_SUCURSAL] Punto de venta indexado.")
    }

    private fun generarEtiquetasParaRubros(rubros: List<String>, cp: String, lat: Double, lng: Double): List<String> {
        val tags = mutableListOf<String>()
        rubros.forEach { r ->
            tags.add(generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.PRESTADOR, cp, r))
            if (lat != 0.0) {
                val hash5 = CalculadoraGeografica.generarGeohash(lat, lng, 5)
                tags.add("${ProtocoloPrefijos.PROXIMIDAD}_${hash5}_$r")
            }
        }
        return tags.distinct()
    }

    suspend fun eliminarDelIndice(id: String) {
        try {
            firestore.collection(COLECCION_INDICE).document(id).delete().await()
            Log.d("INDICE_MAESTRO", "🗑️ [ELIMINADO] ID: $id removido del índice.")
        } catch (e: Exception) {
            Log.e("INDICE_MAESTRO", "❌ [ELIMINAR_ERR] No se pudo remover $id: ${e.message}")
        }
    }
}




