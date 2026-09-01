package com.example.myapplication.core.dominio.motores

import com.example.myapplication.core.datos.local.entidades.IdentidadPrestadorEntity
import com.example.myapplication.core.datos.local.entidades.IdentidadUsuarioEntity
import com.example.myapplication.core.datos.local.entidades.CuentaEntity

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- MOTOR DE SINCRONIZACIÓN REMOTA (PULL - BIG LEAGUE) ---
 * [RESPONSABILIDAD]: Único punto de entrada para consultas a Firestore.
 * [LEY #2]: Costo Zero. Centraliza la lógica de lectura para evitar duplicidad en :app y :prestador.
 */
@Singleton
class MotorSincRemoto @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        const val COL_CLIENTE = "clientes"
        const val COL_PRESTADOR = "prestadores"
        const val COL_CUENTA = "cuentas"
        const val COL_INDICE = "indice_busqueda"
        
        const val SUB_DIRECCIONES = "direcciones"
        const val SUB_EMPRESAS = "empresas"
        const val SUB_SUCURSALES = "sucursales"
        const val SUB_INFRAESTRUCTURA = "infraestructura"
        
        const val DOC_HORARIO = "horario"
        const val DOC_EQUIPO = "equipo"
        const val DOC_RECURSOS = "recursos"
    }


    suspend fun obtenerUsuario(uid: String): IdentidadUsuarioEntity? {
        return try {
            firestore.collection(COL_CLIENTE).document(uid).get().await()
                .toObject(IdentidadUsuarioEntity::class.java)
        } catch (e: Exception) {
            android.util.Log.e("MotorSincRemoto", "❌ [SYNC_REM_ERR] Error al obtener usuario $uid", e)
            null
        }
    }

    suspend fun obtenerPrestador(uid: String): IdentidadPrestadorEntity? {
        return try {
            firestore.collection(COL_PRESTADOR).document(uid).get().await()
                .toObject(IdentidadPrestadorEntity::class.java)
        } catch (e: Exception) {
            android.util.Log.e("MotorSincRemoto", "❌ [SYNC_REM_ERR] Error al obtener prestador $uid", e)
            null
        }
    }

    suspend fun obtenerCuenta(uid: String): CuentaEntity? {
        return try {
            firestore.collection(COL_CUENTA).document(uid).get().await()
                .toObject(CuentaEntity::class.java)
        } catch (e: Exception) {
            android.util.Log.e("MotorSincRemoto", "❌ [SYNC_REM_ERR] Error al obtener cuenta $uid", e)
            null
        }
    }


    // --- ACCESORES DE REFERENCIA PARA SYNC DEEP ---
    fun obtenerReferenciaCliente(uid: String) = firestore.collection(COL_CLIENTE).document(uid)
    fun obtenerReferenciaPrestador(uid: String) = firestore.collection(COL_PRESTADOR).document(uid)
}

