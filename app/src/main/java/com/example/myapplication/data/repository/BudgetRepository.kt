package com.example.myapplication.data.repository

import com.example.myapplication.data.local.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

/**
 * --- REPOSITORIO DE PRESUPUESTOS Y LICITACIONES ---
 * Esta clase es el corazón de la lógica de negocio para el Cliente.
 * Centraliza el acceso a los presupuestos locales y la sincronización con la nube.
 */
@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    // ==========================================================
    // 1. OBSERVABLES (FLUJOS DE DATOS EN TIEMPO REAL)
    // ==========================================================

    /**
     * Lista de todas las licitaciones creadas por el cliente.
     * Se actualiza automáticamente gracias a Flow.
     */
    val allTenders: Flow<List<TenderEntity>> = budgetDao.getAllTenders()

    /**
     * Lista de presupuestos "Varios" (sin licitación asociada).
     */
    val directBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllDirectBudgets()

    /**
     * 🔥 [NUEVO] Lista de todos los presupuestos recibidos.
     */
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()

    /**
     * Obtiene los presupuestos específicos de una licitación.
     * Útil para la pantalla de "Comparar Ofertas".
     */
    fun getBudgetsForTender(tenderId: String): Flow<List<BudgetEntity>> {
        return budgetDao.getBudgetsForTender(tenderId)
    }

    /**
     * 🔥 [NUEVO] Obtiene la lista de licitaciones abiertas (No observable).
     * Útil para procesos de simulación o tareas puntuales.
     */
    suspend fun getOpenTenders(): List<TenderEntity> {
        return budgetDao.getOpenTenders()
    }

    // ==========================================================
    // 2. ACCIONES DE ESCRITURA (LOCAL + PREPARACIÓN FIREBASE)
    // ==========================================================

    /**
     * ── SECCIÓN: CREACIÓN Y SINCRONIZACIÓN ─────────────────────────────────────────
     * Guarda una nueva licitación primero en Room y luego en Firestore.
     */
    suspend fun createNewTender(tender: TenderEntity) {
        // 1. Persistencia local inmediata (Offline First)
        budgetDao.insertTender(tender)

        // 2. Sincronización con Firestore (LicitacionesAbiertas)
        try {
            firestore.collection("LicitacionesAbiertas")
                .document(tender.tenderId)
                .set(tender)
                .await()
            Log.d("BudgetRepository", "Licitación sincronizada con Firestore: ${tender.tenderId}")
        } catch (e: Exception) {
            Log.e("BudgetRepository", "Error al sincronizar con Firestore: ${e.message}")
        }
    }

    /**
     * ── SECCIÓN: LIMPIEZA DE NUBE ─────────────────────────────────────────
     * Elimina la licitación de Firestore cuando cambia de estado o se adjudica.
     * Mantiene la copia local en Room.
     */
    suspend fun removeFromCloud(tenderId: String) {
        try {
            firestore.collection("LicitacionesAbiertas")
                .document(tenderId)
                .delete()
                .await()
            Log.d("BudgetRepository", "Licitación eliminada de la nube (Costo Cero): $tenderId")
        } catch (e: Exception) {
            Log.e("BudgetRepository", "Error al eliminar de Firestore: ${e.message}")
        }
    }

    /**
     * Sube una imagen a Firebase Storage y retorna su URL pública.
     */
    suspend fun uploadTenderImage(tenderId: String, index: Int, bytes: ByteArray): String? {
        return try {
            val ref = storage.reference.child("tenders/$tenderId/img_$index.webp")
            ref.putBytes(bytes).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("BudgetRepository", "Error subiendo imagen a Storage: ${e.message}")
            null
        }
    }

    /**
     * ── SECCIÓN: NOTIFICACIONES MASIVAS (Topics) ─────────────────────────────────────────
     * Envía una notificación push a un tema específico (CP + Rubro).
     * Esto permite notificar a cientos de prestadores con un solo clic.
     */
    suspend fun sendTopicNotification(topic: String, title: String, body: String, tenderId: String) {
        try {
            val client = OkHttpClient()
            val json = JSONObject().apply {
                put("to", "/topics/$topic")
                put("notification", JSONObject().apply {
                    put("title", title)
                    put("body", body)
                    put("sound", "default")
                })
                put("data", JSONObject().apply {
                    put("tenderId", tenderId)
                    put("click_action", "FLUTTER_NOTIFICATION_CLICK") // O la acción de tu App
                })
            }

            val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            
            // NOTA: Para producción, esto debería ir en una Cloud Function.
            // Se usa la Server Key de Firebase (Legacy API) por simplicidad en esta etapa.
            val serverKey = "TU_SERVER_KEY_AQUI" 
            
            val request = Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .addHeader("Authorization", "key=$serverKey")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Log.d("BudgetRepository", "Notificación enviada al topic: $topic")
            } else {
                Log.e("BudgetRepository", "Error enviando notificación: ${response.code}")
            }
        } catch (e: Exception) {
            Log.e("BudgetRepository", "Error en sendTopicNotification: ${e.message}")
        }
    }

    /**
     * Procesa un presupuesto recibido por el Chat o Licitación.
     * Maverick envía un JSON, la app lo parsea y este método lo guarda en Room.
     */
    suspend fun receiveBudgetFromChat(budget: BudgetEntity) {
        // Guardamos en Room para que esté disponible offline
        budgetDao.insertBudget(budget)

        // Si el presupuesto tiene un tenderId, el cliente recibirá una
        // notificación en su sección de Licitaciones automáticamente.
    }

    /**
     * Cambia el estado de un presupuesto (Aceptar/Rechazar).
     */
    suspend fun updateBudgetStatus(budgetId: String, newStatus: BudgetStatus) {
        val currentBudget = budgetDao.getBudgetById(budgetId)
        currentBudget?.let {
            val updatedBudget = it.copy(status = newStatus)
            budgetDao.updateBudgetStatus(updatedBudget)

            // TODO: INTEGRACIÓN FIREBASE
            // Notificar al prestador el cambio de estado
            // firestore.collection("budgets").document(budgetId).update("status", newStatus.name)
        }
    }

    /**
     * 🔥 [NUEVO] Marca un presupuesto como leído en la DB.
     */
    suspend fun markBudgetAsRead(budgetId: String) {
        budgetDao.markAsRead(budgetId)
    }

    /**
     * Borra una licitación localmente.
     */
    suspend fun removeTender(tenderId: String) {
        budgetDao.deleteTender(tenderId)
        // TODO: Borrar también en Firebase si es necesario
    }

    /**
     * Borra un presupuesto localmente.
     */
    suspend fun removeBudget(budgetId: String) {
        budgetDao.deleteBudget(budgetId)
    }
}
