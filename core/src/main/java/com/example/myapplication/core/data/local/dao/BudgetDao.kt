package com.example.myapplication.core.data.local.dao

import androidx.room.*
import com.example.myapplication.core.data.local.entity.BudgetEntity
import com.example.myapplication.core.data.local.entity.TenderEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE PRESUPUESTOS Y LICITACIONES (COMPARTIDO) ---
 * Gestiona la persistencia de las ofertas comerciales (Presupuestos)
 * y las solicitudes de servicio (Licitaciones).//////////////////////////////////////
 */
@Dao
interface BudgetDao {

    // --- 1. GESTIÓN DE LICITACIONES (TENDERS) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTender(tender: TenderEntity)

    @Query("SELECT * FROM tenders ORDER BY dateTimestamp DESC")
    fun getAllTenders(): Flow<List<TenderEntity>>

    @Query("SELECT * FROM tenders WHERE status = 'ABIERTA'")
    suspend fun getOpenTenders(): List<TenderEntity>

    @Query("SELECT * FROM tenders WHERE category = :providerCategory AND status = 'ABIERTA' ORDER BY dateTimestamp DESC")
    fun getOpenTendersByCategory(providerCategory: String): Flow<List<TenderEntity>>

    @Query("DELETE FROM tenders WHERE tenderId = :tId")
    suspend fun deleteTender(tId: String)


    // --- 2. GESTIÓN DE PRESUPUESTOS (BUDGETS) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Query("SELECT * FROM budgets ORDER BY dateTimestamp DESC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    /**
     * Obtiene presupuestos directos (no asociados a una licitación pública).
     */
    @Query("SELECT * FROM budgets WHERE tenderId IS NULL ORDER BY dateTimestamp DESC")
    fun getAllDirectBudgets(): Flow<List<BudgetEntity>>

    /**
     * Obtiene todos los presupuestos recibidos para una licitación específica.
     * Permite al cliente comparar ofertas por precio (grandTotal).
     */
    @Query("SELECT * FROM budgets WHERE tenderId = :tId ORDER BY grandTotal ASC")
    fun getBudgetsForTender(tId: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE budgetId = :bId")
    suspend fun getBudgetById(bId: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE budgetId IN (:bIds)")
    suspend fun getBudgetsByIds(bIds: List<String>): List<BudgetEntity>

    @Update
    suspend fun updateBudgetStatus(budget: BudgetEntity)

    @Query("UPDATE budgets SET isRead = 1 WHERE budgetId = :bId")
    suspend fun markAsRead(bId: String)

    @Query("DELETE FROM budgets WHERE budgetId = :bId")
    suspend fun deleteBudget(bId: String)
}
