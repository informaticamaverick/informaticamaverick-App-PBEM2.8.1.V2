package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.ShortcutDao
import com.example.myapplication.data.local.entity.ShortcutEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE ACCESOS DIRECTOS (APP CLIENTE) ---
 * Gestiona los favoritos y accesos rápidos locales del usuario.
 * Este componente es específico del Cliente y no se comparte en :core.
 */
@Singleton
class ShortcutRepository @Inject constructor(
    private val shortcutDao: ShortcutDao
) {
    fun getShortcutsByContext(context: String): Flow<List<ShortcutEntity>> =
        shortcutDao.getShortcutsByContext(context)

    suspend fun addShortcut(context: String, targetId: String, type: String, label: String? = null, icon: String? = null) {
        val id = "${context}_${targetId}"
        val entity = ShortcutEntity(id, context, targetId, type, label, icon)
        shortcutDao.insertShortcut(entity)
    }

    suspend fun removeShortcut(context: String, targetId: String, type: String? = null) {
        shortcutDao.deleteShortcut(context, targetId)
    }

    suspend fun exists(context: String, targetId: String): Boolean =
        shortcutDao.exists(context, targetId)
}
