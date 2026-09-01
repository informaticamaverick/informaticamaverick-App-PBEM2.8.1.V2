package com.example.myapplication.uishared.ui.modelos

import androidx.compose.ui.graphics.Color
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import com.example.myapplication.core.dominio.modelos.PromocionDominio
import com.google.android.gms.ads.nativead.NativeAd

/**
 * Define los tipos de banners disponibles en los carruseles.
 */
enum class BannerType(val label: String) {
    GOOGLE_AD("SPONSORED"),
    PROMO("PROMOCIÓN"),
    NEW_CATEGORY("NUEVA CATEGORÍA"),
    NEW_PROVIDER("NUEVOS PRESTADORES"),
    PRODUCT_SALE("VENTA DE PRODUCTO"),
    SERVICE_SALE("SERVICIO DESTACADO")
}

/**
 * Modelo de datos para banners de carrusel (SHARED v2.0).
 * UNIFICADO: Centralizado en ui-shared para ser usado por App Usuario y App Prestador.
 */
data class AccordionBanner(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    val color: Color,
    val type: BannerType,
    val originalCategory: CategoriaEntity? = null,
    val isNew: Boolean = false,
    val imageUrl: String? = null,
    val discount: Int? = null,
    val promotion: PromocionDominio? = null,
    val actionUrl: String? = null,
    val providerId: String? = null,
    val nativeAd: NativeAd? = null
)


































