package com.example.myapplication.core.datos.repositorios

import com.example.myapplication.core.dominio.modelos.PublicidadDominio
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- PUBLICIDAD DE EMPRESAS (SSOT) ---
 * Lee en vivo la colección `publicidad` que carga el admin (HTML Admin) y devuelve
 * solo los avisos activos, vigentes hoy y dirigidos a la audiencia pedida.
 */
@Singleton
class PublicidadRepositorio @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun observarPublicidadActiva(audiencia: String): Flow<List<PublicidadDominio>> = callbackFlow {
        val listener = firestore.collection("publicidad")
            .whereEqualTo("activa", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val hoy = java.time.LocalDate.now().toString()
                val avisos = snapshot.documents.mapNotNull { doc ->
                    val audienciaDoc = doc.getString("audiencia") ?: "ambas"
                    if (audienciaDoc != audiencia && audienciaDoc != "ambas") return@mapNotNull null

                    val desde = doc.getString("vigenciaDesde")
                    val hasta = doc.getString("vigenciaHasta")
                    if (!desde.isNullOrBlank() && hoy < desde) return@mapNotNull null
                    if (!hasta.isNullOrBlank() && hoy > hasta) return@mapNotNull null

                    val empresa = doc.getString("empresa") ?: return@mapNotNull null
                    PublicidadDominio(
                        id = doc.id,
                        empresa = empresa,
                        rubro = doc.getString("rubro"),
                        direccion = doc.getString("direccion"),
                        descripcion = doc.getString("descripcion"),
                        imagenUrl = doc.getString("imagenUrl"),
                        contactoTelefono = doc.getString("contactoTelefono"),
                        contactoLink = doc.getString("contactoLink")
                    )
                }
                trySend(avisos)
            }
        awaitClose { listener.remove() }
    }
}
