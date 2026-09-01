package com.example.myapplication.prestador.viewmodel.profile

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.example.myapplication.prestador.facturacion.GestorFacturacion
import com.example.myapplication.core.datos.repositorios.SincronizadorRemotoPrestador
import com.example.myapplication.prestador.datos.repositorios.PrestadorAutenticacionRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- ARMADOR PREMIUM DEL PRESTADOR (PRO - v2026.ELITE) ---
 * [PROPÓSITO]: Orquestar la lógica de suscripción Elite y facturación con Google Play.
 * [LEY #9]: Estándar Maverick. Especialista en Monetización y Paywall.
 */
@HiltViewModel
class ArmadorPrestadorPremiumViewModel @Inject constructor(
    private val gestorFacturacion: GestorFacturacion,
    private val repoRemoto: SincronizadorRemotoPrestador,
    private val authRepo: PrestadorAutenticacionRepositorio
) : ViewModel() {

    val detallesSuscripcion: StateFlow<ProductDetails?> = gestorFacturacion.detallesSuscripcion
    val estaConectado: StateFlow<Boolean> = gestorFacturacion.estaConectado

    init {
        // Escuchar compras exitosas para actualizar el estatus en Room y Firebase
        viewModelScope.launch {
            gestorFacturacion.compraExitosa.collect { exitosa ->
                if (exitosa) {
                    actualizarEstadoSuscripcionElite(true)
                }
            }
        }
    }

    /**
     * 🔥 [ELITE]: Inicia el flujo de compra de Google Play.
     */
    fun adquirirMembresiaElite(actividad: Activity) {
        gestorFacturacion.iniciarFlujoSuscripcion(actividad)
    }

    /**
     * 🔥 [ELITE]: Verifica el estatus actual en Google Play y sincroniza localmente.
     */
    fun restaurarSuscripciones() {
        gestorFacturacion.verificarSuscripcionesActivas { activa ->
            actualizarEstadoSuscripcionElite(activa)
        }
    }

    private fun actualizarEstadoSuscripcionElite(activa: Boolean) {
        val uid = authRepo.obtenerUsuarioActual()?.uid ?: return
        viewModelScope.launch {
            Log.d("ArmadorPremium", "🔄 [SYNC_ELITE] Actualizando estatus: $activa")
            repoRemoto.actualizarEstadoSuscripcionElite(uid, activa)
        }
    }
}















































