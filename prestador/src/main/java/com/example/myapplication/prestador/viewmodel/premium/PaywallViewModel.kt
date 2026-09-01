package com.example.myapplication.prestador.viewmodel.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.dao.CuentaDao
import com.example.myapplication.core.datos.local.AppDatabase
import com.example.myapplication.prestador.facturacion.GestorFacturacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- VIEWMODEL DEL MURO DE PAGO (ELITE v2026.FINAL) ---
 */
@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val gestorFacturacion: GestorFacturacion,
    private val db: AppDatabase,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    fun iniciarCompra(actividad: Activity) {
        gestorFacturacion.iniciarFlujoSuscripcion(actividad)
    }

    /**
     * 🔥 [DEBUG]: Simula un pago exitoso con Google Play.
     */
    fun simularPagoExitoso(onSuccess: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            // 1. Actualizar localmente (Room)
            db.CuentaDao().actualizarEstadoSuscripcion(uid, true)
            
            // 2. Actualizar en Nube (Firestore)
            firestore.collection("cuentas").document(uid).update("estaSuscrito", true)
            
            android.util.Log.d("PaywallVM", "✅ [DEBUG_PAYMENT] Membresía Elite activada vía simulador.")
            onSuccess()
        }
    }
}















































