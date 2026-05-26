package com.example.myapplication.presentation.client

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.ServiceDisplayModel
import com.example.myapplication.presentation.components.AccordionBanner
import com.example.myapplication.presentation.components.BannerType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.random.Random

/**
 * --- PROMO VIEWMODEL (EL ORQUESTADOR DE PUBLICIDAD UNIFICADO) ---
 * Centraliza toda la lógica de anuncios, promociones y destacados de la app.
 * Encargado del "trabajo sucio" de mezcla, filtrado táctico e inyección de publicidad.
 */
@HiltViewModel
class PromoViewModel @Inject constructor() : ViewModel() {

    // ======================================================================================
    // --- 1. ESTADOS DE CONTROL DE FILTROS ---
    // ======================================================================================
    private val _activeFilters = MutableStateFlow<Set<String>>(emptySet())
    val activeFilters: StateFlow<Set<String>> = _activeFilters.asStateFlow()

    /**
     * Aplica los filtros seleccionados desde la UI.
     */
    fun updateFilters(filters: Set<String>) {
        _activeFilters.value = filters
    }

    /**
     * Limpia todos los filtros activos.
     */
    fun clearFilters() {
        _activeFilters.value = emptySet()
    }

    // ======================================================================================
    // --- 2. ORQUESTACIÓN DE BANNERS (LÓGICA ESTABLE) ---
    // ======================================================================================

    /**
     * Genera la lista de banners orquestada.
     * [ESTABILIDAD]: Utiliza seeds para Random para evitar el efecto "loco" en el carrusel.
     * [NUEVO]: Integra lógica de PromoScreen para mostrar ofertas reales.
     */
    fun getHomeBanners(
        categories: List<CategoryEntity>,
        services: List<ServiceDisplayModel>
    ): Flow<List<AccordionBanner>> = _activeFilters.map { filters ->
        val bannerList = mutableListOf<AccordionBanner>()

        // A. MAPEADO DE NOVEDADES (Categorías nuevas)
        categories.filter { it.isNew }.take(4).forEach { cat ->
            bannerList.add(AccordionBanner(
                id = "cat_${cat.name}",
                title = cat.name,
                subtitle = "🚀 NUEVA CATEGORÍA",
                icon = cat.icon,
                color = Color(CategoryVisuals.getColorFor(cat.superCategory)),
                type = BannerType.NEW_CATEGORY,
                originalCategory = cat,
                isNew = true
            ))
        }

        // B. MAPEADO DE PROMOCIONES (Prestadores suscritos con ofertas)
        // Solo tomamos prestadores suscritos para el carrusel de alta visibilidad
        services.filter { it.isSubscribed }.take(6).forEach { service ->
            val stableSeed = Random(service.id.hashCode().toLong())
            val discount = if (stableSeed.nextBoolean()) (10..50).random(stableSeed) else null
            
            bannerList.add(AccordionBanner(
                id = "promo_${service.id}", 
                title = service.title, 
                subtitle = if (discount != null) "¡OFERTA IMPERDIBLE!" else "SERVICIO DESTACADO", 
                icon = if (service.doesProduct) "🛍️" else "🛠️", 
                color = if (discount != null) Color(0xFFE91E63) else Color(0xFF2197F5), 
                type = if (service.doesProduct) BannerType.PRODUCT_SALE else BannerType.PROMO, 
                discount = discount,
                service = service 
            ))
        }

        // C. MAPEADO DE ADS (Publicidad externa o patrocinada)
        bannerList.add(AccordionBanner(
            id = "ad_maverick_premium", 
            title = "Maverick Premium", 
            subtitle = "Sube de nivel tu negocio hoy", 
            icon = "💎", 
            color = Color(0xFFFFD700), // Dorado
            type = BannerType.GOOGLE_AD,
            imageUrl = "https://picsum.photos/seed/maverick/600/300"
        ))

        // D. FILTRADO TÁCTICO
        val filteredList = if (filters.isEmpty()) bannerList 
        else bannerList.filter { banner ->
            // El anuncio institucional siempre se muestra si no hay filtros específicos de tipo
            if (banner.type == BannerType.GOOGLE_AD) true 
            else {
                val isNovedad = banner.type == BannerType.NEW_CATEGORY || banner.type == BannerType.NEW_PROVIDER
                val isPromo = banner.type == BannerType.PROMO || banner.discount != null
                val isProd = banner.type == BannerType.PRODUCT_SALE
                val isServ = banner.type == BannerType.SERVICE_SALE

                (filters.contains("NOVEDADES") && isNovedad) ||
                (filters.contains("PROMOCIONES") && isPromo) ||
                (filters.contains("PRODUCTOS") && isProd) ||
                (filters.contains("SERVICIOS") && isServ)
            }
        }

        // E. MEZCLA ESTABLE (Evita cambios bruscos en recomposición)
        inyectarPublicidad(filteredList.sortedBy { it.id }) 
    }

    /**
     * Lógica de inyección de publicidad (1 Ad cada 2 contenidos).
     * [CORRECCIÓN]: Genera IDs únicos para cada instancia inyectada para evitar crashes por "Duplicate Key".
     */
    private fun inyectarPublicidad(items: List<AccordionBanner>): List<AccordionBanner> {
        val ads = items.filter { it.type == BannerType.GOOGLE_AD }
        val content = items.filter { it.type != BannerType.GOOGLE_AD }
        if (ads.isEmpty() || content.isEmpty()) return items

        val result = mutableListOf<AccordionBanner>()
        var adIdx = 0
        content.forEachIndexed { index, item ->
            result.add(item)
            if ((index + 1) % 2 == 0) {
                val adBase = ads[adIdx % ads.size]
                // Aseguramos ID único añadiendo el índice de inserción
                result.add(adBase.copy(id = "${adBase.id}_$index"))
                adIdx++
            }
        }
        return result
    }
}
