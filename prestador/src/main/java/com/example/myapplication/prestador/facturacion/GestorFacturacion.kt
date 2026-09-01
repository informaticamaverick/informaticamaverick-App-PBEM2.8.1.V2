package com.example.myapplication.prestador.facturacion

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.example.myapplication.core.datos.local.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- GESTOR DE FACTURACIÓN (ELITE v2026.FINAL) ---
 * [PROPÓSITO]: Centralizar la lógica de suscripciones con Google Play Store.
 * [LEY #9]: Estándar Mav en Español. Exclusivo para la App del Prestador.
 */
@Singleton
class GestorFacturacion @Inject constructor(
    @ApplicationContext private val contexto: Context,
    private val db: AppDatabase,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _estaConectado = MutableStateFlow(false)
    val estaConectado = _estaConectado.asStateFlow()

    private val _detallesSuscripcion = MutableStateFlow<ProductDetails?>(null)
    val detallesSuscripcion = _detallesSuscripcion.asStateFlow()

    private val _compraExitosa = MutableStateFlow(false)
    val compraExitosa = _compraExitosa.asStateFlow()

    private val comprasActualizadasListener = PurchasesUpdatedListener { resultado, compras ->
        if (resultado.responseCode == BillingClient.BillingResponseCode.OK && compras != null) {
            for (compra in compras) {
                procesarCompra(compra)
            }
        } else {
            Log.e("FacturacionApp", "❌ Error en compra: ${resultado.debugMessage}")
        }
    }

    private val clienteFacturacion = BillingClient.newBuilder(contexto)
        .setListener(comprasActualizadasListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .build()

    init {
        conectarConGooglePlay()
    }

    fun conectarConGooglePlay() {
        clienteFacturacion.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(resultado: BillingResult) {
                if (resultado.responseCode == BillingClient.BillingResponseCode.OK) {
                    _estaConectado.value = true
                    Log.d("FacturacionApp", "✅ Conectado a Google Play Store.")
                    consultarProductoElite()
                }
            }
            override fun onBillingServiceDisconnected() {
                _estaConectado.value = false
                Log.w("FacturacionApp", "🔌 Desconectado de Google Play Store.")
            }
        })
    }

    private fun consultarProductoElite() {
        alcance.launch {
            val listaProductos = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("suscripcion_elite_mensual")
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )

            val parametros = QueryProductDetailsParams.newBuilder()
                .setProductList(listaProductos)
                .build()

            val resultado = clienteFacturacion.queryProductDetails(parametros)
            if (resultado.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _detallesSuscripcion.value = resultado.productDetailsList?.firstOrNull()
                Log.d("FacturacionApp", "💎 Producto Elite recuperado: ${_detallesSuscripcion.value?.name}")
            }
        }
    }

    fun iniciarFlujoSuscripcion(actividad: Activity) {
        val detalles = _detallesSuscripcion.value ?: return
        val offerToken = detalles.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return

        val listaParametrosProducto = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(detalles)
                .setOfferToken(offerToken)
                .build()
        )

        val parametrosFlujo = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listaParametrosProducto)
            .build()

        clienteFacturacion.launchBillingFlow(actividad, parametrosFlujo)
    }

    private fun procesarCompra(compra: Purchase) {
        if (compra.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!compra.isAcknowledged) {
                val parametrosReconocimiento = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(compra.purchaseToken)
                    .build()

                alcance.launch {
                    val resultado = clienteFacturacion.acknowledgePurchase(parametrosReconocimiento)
                    if (resultado.responseCode == BillingClient.BillingResponseCode.OK) {
                        actualizarEstadoMembresiaLocalYRemoto(true)
                        _compraExitosa.value = true
                        Log.d("FacturacionApp", "🎉 Suscripción Elite reconocida con éxito.")
                    }
                }
            } else {
                // Ya estaba reconocida pero llegó la actualización
                actualizarEstadoMembresiaLocalYRemoto(true)
            }
        }
    }

    private fun actualizarEstadoMembresiaLocalYRemoto(activa: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        alcance.launch(Dispatchers.IO) {
            try {
                // 1. Room
                db.CuentaDao().actualizarEstadoSuscripcion(uid, activa)
                // 2. Firestore
                firestore.collection("cuentas").document(uid).update("estaSuscrito", activa)
                Log.d("FacturacionApp", "✅ Membresía sincronizada: $activa")
            } catch (e: Exception) {
                Log.e("FacturacionApp", "❌ Error sincronizando membresía: ${e.message}")
            }
        }
    }

    /**
     * 🔥 [ELITE]: Verifica si el usuario tiene suscripciones activas (Restore Purchases).
     */
    fun verificarSuscripcionesActivas(alFinalizar: (Boolean) -> Unit) {
        val parametros = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        clienteFacturacion.queryPurchasesAsync(parametros) { resultado, compras ->
            if (resultado.responseCode == BillingClient.BillingResponseCode.OK) {
                val activa = compras.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                actualizarEstadoMembresiaLocalYRemoto(activa)
                alFinalizar(activa)
            } else {
                alFinalizar(false)
            }
        }
    }
}

