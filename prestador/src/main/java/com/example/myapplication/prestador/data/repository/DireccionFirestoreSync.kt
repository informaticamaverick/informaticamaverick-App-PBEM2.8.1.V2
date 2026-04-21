package com.example.myapplication.prestador.data.repository

import android.util.Log
import com.example.myapplication.prestador.data.local.entity.DireccionEntity
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ARCHIVO REDUNDANTE - MANTENIDO PARA COMPATIBILIDAD LEGACY
 * 
 * Este repositorio ha sido consolidado en [ProviderRepository.syncProviderWithFirebase].
 * La nueva arquitectura utiliza un Single Source of Truth (SSOT) donde la persistencia
 * de direcciones personales y de sucursales se gestiona de forma atómica a través
 * del flujo jerárquico de ProviderEntity.
 * 
 * Se recomienda migrar los pocos usos restantes a ProviderRepository.
 */

private const val TAG = "DireccionSync"

@Singleton
class DireccionFirestoreSync @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val direccionRestory: DireccionRepository
) {
    /* 
    // SECCIÓN COMENTADA - LÓGICA CONSOLIDADA EN ProviderRepository
    
    suspend fun subirDireccion(direccion: DireccionEntity): Result<Unit> {
        // La lógica de subida ahora reside en ProviderRepository.syncProviderWithFirebase
        // encargándose de la subcolección 'addresses' bajo 'providers/{uid}'
        return Result.success(Unit)
    }

    suspend fun bajarDireccion(referenciaId: String, referenciaTipo: String): Result<DireccionEntity?> {
        // El flujo de descarga ahora es parte de EditProfileViewModel.loadFromFirebase
        return Result.success(null)
    }

    suspend fun sincronizar(referenciaId: String, referenciaTipo: String): DireccionEntity? {
        return null
    }
    */
}
