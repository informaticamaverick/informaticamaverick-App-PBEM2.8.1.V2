package com.example.myapplication.core.data.repository

import android.util.Log
import com.example.myapplication.core.data.local.dao.BudgetDao
import com.example.myapplication.core.data.local.entity.BudgetEntity
import com.example.myapplication.core.data.local.entity.BudgetStatus
import com.example.myapplication.core.data.local.entity.TenderEntity
import com.example.myapplication.core.data.remote.BudgetDataMapper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE PRESUPUESTOS Y LICITACIONES (COMPARTIDO) ---
 * Centraliza la lógica de negocio para la gestión de ofertas.
 * Este repositorio permite que el Prestador publique presupuestos y que el 
 * Cliente los reciba, acepte o rechace, manteniendo la sincronización
 * entre Room y Firestore.
 */
@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    // --- SECCIÓN 1: OBSERVABLES ---

    val allTenders: Flow<List<TenderEntity>> = budgetDao.getAllTenders()
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()
    val directBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllDirectBudgets()

    fun getBudgetsForTender(tenderId: String): Flow<List<BudgetEntity>> =
        budgetDao.getBudgetsForTender(tenderId)

    suspend fun getBudgetById(budgetId: String): BudgetEntity? =
        budgetDao.getBudgetById(budgetId)

    suspend fun getBudgetsByIds(budgetIds: List<String>): List<BudgetEntity> =
        budgetDao.getBudgetsByIds(budgetIds)

    /**
     * Obtiene solo las licitaciones abiertas.
     */
    suspend fun getOpenTenders(): List<TenderEntity> =
        budgetDao.getOpenTenders()


    // --- SECCIÓN 2: GESTIÓN DE LICITACIONES (CLIENTE) ---

    /**
     * Sincroniza licitaciones desde la nube por categoría (Costo Zero).
     */
    suspend fun syncTendersByCategory(category: String) {
        try {
            val snapshot = firestore.collection("LicitacionesAbiertas")
                .whereEqualTo("category", category)
                .whereEqualTo("status", "ABIERTA")
                .get().await()

            val tenders = snapshot.documents.mapNotNull { BudgetDataMapper.fromFirestoreTender(it) }
            tenders.forEach { budgetDao.insertTender(it) }
            Log.d("BudgetRepository", "Sincronizadas ${tenders.size} licitaciones de $category")
        } catch (e: Exception) {
            Log.e("BudgetRepository", "Error sincronizando licitaciones: ${e.message}")
        }
    }

    /**
     * Publica una nueva solicitud de servicio.
     */
    suspend fun createNewTender(tender: TenderEntity) {
        budgetDao.insertTender(tender)
        try {
            firestore.collection("LicitacionesAbiertas")
                .document(tender.tenderId)
                .set(tender)
                .await()
        } catch (e: Exception) {
            Log.e("BudgetRepository", "Error sincronizando Licitación: ${e.message}")
        }
    }


    // --- SECCIÓN 3: GESTIÓN DE PRESUPUESTOS (PRESTADOR/CLIENTE) ---

    /**
     * Sincroniza un presupuesto específico desde la nube.
     */
    suspend fun syncBudgetFromRemote(budgetId: String) {
        try {
            val doc = firestore.collection("presupuestos").document(budgetId).get().await()
            val entity = BudgetDataMapper.fromFirestoreBudget(doc)
            entity?.let { budgetDao.insertBudget(it) }
        } catch (e: Exception) {
            Log.e("BudgetRepository", "Error sincronizando presupuesto: ${e.message}")
        }
    }

    /**
     * Registra un presupuesto recibido por Chat.
     */
    suspend fun receiveBudgetFromChat(budget: BudgetEntity) {
        budgetDao.insertBudget(budget)
    }

    /**
     * Registra un presupuesto recibido en la base de datos local.
     */
    suspend fun receiveBudget(budget: BudgetEntity) {
        budgetDao.insertBudget(budget)
    }

    suspend fun markBudgetAsRead(budgetId: String) {
        budgetDao.markAsRead(budgetId)
    }

    suspend fun removeBudget(budgetId: String) {
        budgetDao.deleteBudget(budgetId)
    }

    suspend fun removeTender(tenderId: String) {
        budgetDao.deleteTender(tenderId)
    }

    /**
     * Actualiza el estado del presupuesto y prepara la sincronización.
     */
    suspend fun updateBudgetStatus(budgetId: String, newStatus: BudgetStatus) {
        val current = budgetDao.getBudgetById(budgetId)
        current?.let {
            val updated = it.copy(status = newStatus)
            budgetDao.updateBudgetStatus(updated)
            // Aquí se dispararía la notificación al otro participante vía Firebase
        }
    }

    /**
     * Sube imágenes relacionadas con el trabajo a Firebase Storage.
     */
    suspend fun uploadBudgetFile(path: String, bytes: ByteArray): String? {
        return try {
            val ref = storage.reference.child(path)
            ref.putBytes(bytes).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("BudgetRepository", "Error subiendo archivo: ${e.message}")
            null
        }
    }

    suspend fun uploadTenderImage(tenderId: String, index: Int, bytes: ByteArray): String? {
        val path = "licitaciones/$tenderId/img_$index.webp"
        return uploadBudgetFile(path, bytes)
    }

    suspend fun sendTopicNotification(topic: String, title: String, body: String, tenderId: String) {
        // En un entorno Maverick real, esto dispararía una función en la nube o un mensaje a un topic de FCM
        Log.d("BudgetRepository", "Simulando envío de notificación a topic: $topic - $title")
    }

    suspend fun removeFromCloud(tenderId: String) {
        try {
            firestore.collection("LicitacionesAbiertas").document(tenderId).delete().await()
        } catch (e: Exception) {
            Log.e("BudgetRepository", "Error borrando de la nube: ${e.message}")
        }
    }
}
