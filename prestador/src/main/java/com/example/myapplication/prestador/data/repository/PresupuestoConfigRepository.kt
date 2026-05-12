package com.example.myapplication.prestador.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class PresupuestoConfig(
    val validezDias: Int = 7,
    val moneda: String = "ARS",
    val prefijo: String = "PRES",
    val proximoNumero: Int = 1,
    val notaLegal: String = "",
    val lastAcknowledgedMoneda: String = "ARS",
    val showArticlesByDefault: Boolean = true,
    val showServicesByDefault: Boolean = true,
    val showFeesByDefault: Boolean = true,
    val showMiscByDefault: Boolean = true,
    val showTaxesByDefault: Boolean = true,
    val showAttachmentsByDefault: Boolean = true,
    val notaObservacionesDefault: String = "",
    val descuentoDefault: Double = 0.0,
    val categoriaDefault: String = ""
)

@Singleton
class PresupuestoConfigRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences("presupuesto_config", Context.MODE_PRIVATE)
    }

    private val _config = MutableStateFlow(loadFromPrefs())
    val config: StateFlow<PresupuestoConfig> = _config.asStateFlow()

    private fun loadFromPrefs(): PresupuestoConfig = PresupuestoConfig(
        validezDias = prefs.getInt("validez_dias", 7),
        moneda = prefs.getString("moneda", "ARS") ?: "ARS",
        prefijo = prefs.getString("prefijo", "PRES") ?: "PRES",
        proximoNumero = prefs.getInt("proximo_numero", 1),
        notaLegal = prefs.getString("nota_legal", "") ?: "",
        lastAcknowledgedMoneda = prefs.getString("last_ack_moneda", "ARS") ?: "ARS",
        showArticlesByDefault = prefs.getBoolean("show_articles", true),
        showServicesByDefault = prefs.getBoolean("show_services", true),
        showFeesByDefault = prefs.getBoolean("show_fees", true),
        showMiscByDefault = prefs.getBoolean("show_misc", true),
        showTaxesByDefault = prefs.getBoolean("show_taxes", true),
        showAttachmentsByDefault = prefs.getBoolean("show_attachments", true),
        notaObservacionesDefault = prefs.getString("nota_obs_default", "") ?: "",
        descuentoDefault = prefs.getFloat("descuento_default_pct", 0f).toDouble(),
        categoriaDefault = prefs.getString("categoria_default", "") ?: ""
    )

    suspend fun getConfig(): PresupuestoConfig = _config.value

    suspend fun saveConfig(config: PresupuestoConfig) {
        prefs.edit()
            .putInt("validez_dias", config.validezDias)
            .putString("moneda", config.moneda)
            .putString("prefijo", config.prefijo)
            .putInt("proximo_numero", config.proximoNumero)
            .putString("nota_legal", config.notaLegal)
            .putString("last_ack_moneda", config.lastAcknowledgedMoneda)
            .putBoolean("show_articles", config.showArticlesByDefault)
            .putBoolean("show_services", config.showServicesByDefault)
            .putBoolean("show_fees", config.showFeesByDefault)
            .putBoolean("show_misc", config.showMiscByDefault)
            .putBoolean("show_taxes", config.showTaxesByDefault)
            .putBoolean("show_attachments", config.showAttachmentsByDefault)
            .putString("nota_obs_default", config.notaObservacionesDefault)
            .putFloat("descuento_default_pct", config.descuentoDefault.toFloat())
            .putString("categoria_default", config.categoriaDefault)
            .apply()
        _config.value = config
    }
}
