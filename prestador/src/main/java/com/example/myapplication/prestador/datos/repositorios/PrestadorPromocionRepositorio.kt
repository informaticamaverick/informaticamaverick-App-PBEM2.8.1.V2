package com.example.myapplication.prestador.datos.repositorios

import com.example.myapplication.core.datos.local.dao.PromocionDao
import com.example.myapplication.core.datos.local.entidades.toEntity
import com.example.myapplication.core.dominio.modelos.Promocion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE PROMOCIONES PRESTADOR (ELITE v2026.FINAL) ---
 * [LEY #9]: Estándar Mav en Español. Desacoplamiento Táctico.
 */
@Singleton
class PrestadorPromocionRepositorio @Inject constructor(
    private val promocionDao: PromocionDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    /**
     * 🔥 [ELITE]: Guarda la promoción en Room y sube multimedia.
     */
    suspend fun guardarPromocionLocalConMultimedia(promo: Promocion): Promocion {
        // 1. Subir imágenes a Firebase Storage — "a lo mejor esfuerzo": si Storage no está
        // disponible (404 "Object does not exist", plan sin activar) o falla una imagen puntual,
        // se descarta esa imagen y se sigue con el resto en vez de abortar TODA la publicación.
        val urlsPublicas = promo.urlImagenes.mapNotNull { uriString ->
            if (uriString.startsWith("http")) uriString
            else try {
                subirImagen(promo.idPrestador, promo.id, Uri.parse(uriString))
            } catch (e: Exception) {
                Log.e("PROMO_UPLOAD", "❌ Fallo al subir imagen, se descarta y se sigue: ${e.message}")
                null
            }
        }

        val promoConUrls = promo.copy(urlImagenes = urlsPublicas)

        // 2. Persistencia Local (SSOT)
        promocionDao.insertarPromocion(promoConUrls.toEntity())
        return promoConUrls
    }

    private suspend fun subirImagen(uid: String, promoId: String, uri: Uri): String {
        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child("promociones/$uid/$promoId/$fileName")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun eliminarPromocionLocal(id: String) {
        promocionDao.eliminarPromocion(id)
    }

    /**
     * 🔥 [ELITE v2026.FINAL]: Sincroniza las publicaciones del prestador desde la nube.
     */
    suspend fun sincronizarMisPromociones(idPrestador: String) {
        try {
            Log.d("PROMO_SYNC", "📡 Sincronizando publicaciones para: $idPrestador")
            val snapshot = firestore.collection("indice_promociones")
                .whereEqualTo("idPrestador", idPrestador)
                .get().await()
            
            val promociones = snapshot.documents.mapNotNull { doc ->
                val promo = com.example.myapplication.core.dominio.mapeadores.PromocionMappers.desdeFirestore(doc)
                // Aseguramos que los filtros coincidan con la estructura esperada por la App Azul
                Log.d("PROMO_SYNC", "🔍 [FILTROS_SYNC] Promo ${promo?.id} con filtros: ${promo?.filtrosBusqueda}")
                promo
            }
            
            if (promociones.isNotEmpty()) {
                promocionDao.insertarListaPromociones(promociones.map { it.toEntity() })
                Log.d("PROMO_SYNC", "✅ ${promociones.size} publicaciones sincronizadas en Room.")
            }
        } catch (e: Exception) {
            Log.e("PROMO_SYNC", "❌ Fallo al sincronizar publicaciones: ${e.message}")
        }
    }
}



