package com.example.myapplication.viewmodel.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.repositorios.PromocionRepositorio
import com.example.myapplication.core.dominio.descubrimiento.ProtocoloPrefijos
import com.example.myapplication.core.dominio.descubrimiento.GeneradorTópicosFCM
import com.example.myapplication.core.dominio.ubicacion.NormalizadorDirecciones
import com.example.myapplication.core.dominio.modelos.PromocionDominio
import com.example.myapplication.core.dominio.modelos.PromocionComentario
import com.example.myapplication.core.dominio.mapeadores.PromocionMappers
import com.example.myapplication.datos.repositorios.ConsultasUsuarioRepositorio
import com.example.myapplication.coordinadores.CoordinadorAcciones
import com.example.myapplication.uishared.ui.modelos.AccordionBanner
import com.example.myapplication.uishared.ui.modelos.BannerType
import com.example.myapplication.core.servicios.publicidad.BeAdsManager
import com.example.myapplication.ui.componentes.DropdownItemData
import com.example.myapplication.core.datos.repositorios.CategoriaRepositorio
import com.example.myapplication.core.datos.repositorios.AccesoDirectoRepositorio
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth

/**
 * --- PROMO VIEWMODEL (ORQUESTADOR ELITE v2026.OPTIMIZED) ---
 * [PROPÓSITO]: Centraliza el descubrimiento de historias y promociones usando Paging 3.
 * [LEY #12]: Soberanía por Contrato. Be es un portavoz sordo a la lógica de negocio.
 * [LEY #9]: Estándar Mav en Español.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PromoViewModel @Inject constructor(
    private val PromocionRepositorio: PromocionRepositorio,
    private val consultasUserRepo: ConsultasUsuarioRepositorio,
    private val categoryRepository: CategoriaRepositorio,
    private val AccesoDirectoRepositorio: AccesoDirectoRepositorio,
    private val coordinator: CoordinadorAcciones,
    private val generadorTopicos: GeneradorTópicosFCM,
    private val suscripcionDao: com.example.myapplication.core.datos.local.dao.SuscripcionTopicDao,
    private val auth: FirebaseAuth,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _nativeAdPool = MutableStateFlow<List<NativeAd>>(emptyList())
    
    // --- SECTOR: FILTROS (ELITE v2026) ---
    private val _filtrosActivos = MutableStateFlow<Set<String>>(emptySet())
    val filtrosActivos = _filtrosActivos.asStateFlow()

    val itemsFiltro = flowOf(listOf(
        DropdownItemData("type_discount", "Descuentos", emoji = "🏷️"),
        DropdownItemData("type_event", "Eventos", emoji = "📅"),
        DropdownItemData("type_new", "Novedades", emoji = "✨")
    )).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val itemsOrden = flowOf(listOf(
        DropdownItemData("sort_recent", "Más Recientes", emoji = "📅"),
        DropdownItemData("sort_popular", "Más Populares", emoji = "🔥")
    )).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val itemsCategoria: StateFlow<List<DropdownItemData>> = categoryRepository.obtenerMetadatosSuperCategorias()
        .map { list ->
            list.map { 
                DropdownItemData(
                    id = "super_${it.id}",
                    label = it.titulo,
                    emoji = it.icono
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        if (BeAdsManager.isAdSystemEnabled) preCargarAnuncios(cantidad = 5)
        
        // Carga proactiva si Room está vacío
        viewModelScope.launch {
            val count = PromocionRepositorio.obtenerPromocionesActivas(null).firstOrNull()?.size ?: 0
            if (count == 0) {
                android.util.Log.d("DIAGNOSTICO_HOME", "⚠️ [PROMO_EMPTY] Room vacío, disparando sincronización forzada...")
                PromocionRepositorio.sincronizarPromocionesRemotas("4000") // Default inicial
            }
        }
    }

    private fun preCargarAnuncios(cantidad: Int) {
        val adLoader = AdLoader.Builder(context, BeAdsManager.TEST_NATIVE_ID)
            .forNativeAd { ad -> 
                _nativeAdPool.update { (it + ad).distinct() } 
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("PromoVM_DEBUG", "❌ [AD_LOAD_FAIL] Error al cargar anuncio: ${error.message}")
                }
            })
            .withNativeAdOptions(BeAdsManager.buildNativeAdOptions())
            .build()
        
        repeat(cantidad) { adLoader.loadAd(AdRequest.Builder().build()) }
    }

    override fun onCleared() {
        super.onCleared()
        _nativeAdPool.value.forEach { it.destroy() }
    }

    private val activeZipCode: StateFlow<String?> = coordinator.direccionActiva
        .map { it?.codigoPostal?.let { cp -> NormalizadorDirecciones.limpiarCodigoPostal(cp) } }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeTopics = suscripcionDao.obtenerSuscripcionesActivas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUserPhoto: StateFlow<String?> = auth.currentUser?.uid?.let { uid ->
        consultasUserRepo.obtenerUsuarioCompletoFlujo(uid).map { it?.perfil?.urlFoto as? String }
    }?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null) ?: MutableStateFlow(null)

    val stories: StateFlow<List<com.example.myapplication.core.dominio.modelos.PromocionDominio>> = combine(activeZipCode, _filtrosActivos, _nativeAdPool) { zip, filters, ads ->
        val cpFinal = zip ?: "4000"
        val catFilter = filters.find { it.startsWith("super_") }?.removePrefix("super_")
        
        // Obtenemos todas las historias de la zona
        val todasLasHistorias = PromocionRepositorio.obtenerHistoriasCascada(cpFinal, null, catFilter).first()
            .map { PromocionMappers.aUiModel(it) }
            .toMutableList()
            
        // 🔥 [SUPREME.FIX]: Inyectar anuncio de Google como historia si existe
        if (BeAdsManager.isAdSystemEnabled && ads.isNotEmpty()) {
            val ad = ads.first()
            val fakeStoryAd = com.example.myapplication.core.dominio.modelos.PromocionDominio(
                id = "google_ad_story",
                idPrestador = "google_ads",
                titulo = ad.headline ?: "Recomendado",
                descripcion = ad.body ?: "Contenido Patrocinado",
                nombrePrestador = ad.advertiser ?: "Google Ads",
                urlMiniaturaPrestador = null, 
                urlImagen = null,
                reputacion = 5f,
                estaVerificado = true,
                tiempoRelativo = "Anuncio",
                etiquetaOferta = "PROMO",
                esHistoria = true,
                leGustaAlUsuario = false,
                conteoLikes = 0,
                esNuevo = true,
                esPublicidad = true,
                nativeAd = ad
            )
            // Insertamos el anuncio en la segunda posición para visibilidad táctica
            if (todasLasHistorias.size >= 1) todasLasHistorias.add(1, fakeStoryAd)
            else todasLasHistorias.add(fakeStoryAd)
        }
        
        todasLasHistorias.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * 🔥 [ELITE v2026]: Feed Paginado con Backfill de Publicidad y Filtros Reactivos.
     */
    val feedPagingData: Flow<PagingData<PromoItem>> = combine(
        activeZipCode,
        activeTopics,
        _filtrosActivos,
        _nativeAdPool
    ) { zip, _, filters, ads ->
        Triple(zip, filters, ads)
    }.flatMapLatest { (zip, filters, ads) ->
        val cp = if (zip.isNullOrBlank()) "4000" else zip
        
        android.util.Log.d("PROMO_VIEWMODEL", "📱 [FEED_REQUEST] Cargando promos para CP: $cp | Filtros: $filters")
        
        PromocionRepositorio.obtenerPromocionesPaginadas(cp)
            .map { pagingData ->
                val mapped = pagingData.map { entity ->
                    PromoItem.RealPromo(PromocionMappers.aUiModel(entity.aModelo())) as PromoItem
                }
                
                // Inyectamos anuncios de forma estable usando el pool
                mapped.insertSeparators { before, after ->
                    if (after == null && ads.isNotEmpty()) {
                        PromoItem.GoogleNativeAd(ads.last())
                    } else if (before is PromoItem.RealPromo && (before.promotion.id.hashCode() % 6 == 0) && ads.isNotEmpty()) {
                        // Usamos un hash para elegir un anuncio del pool de forma estable
                        val adIndex = Math.abs(before.promotion.id.hashCode() % ads.size)
                        PromoItem.GoogleNativeAd(ads[adIndex])
                    } else null
                }
            }
    }.cachedIn(viewModelScope)

    fun alternarFiltro(id: String) {
        _filtrosActivos.update { current ->
            if (id == "CLEAR_ALL") emptySet()
            else if (current.contains(id)) current - id
            else current + id
        }
    }

    fun toggleLike(idPromocion: String) {
        viewModelScope.launch { PromocionRepositorio.alternarLike(idPromocion) }
    }

    fun getComments(idPromocion: String): Flow<List<PromocionComentario>> = 
        PromocionRepositorio.obtenerComentarios(idPromocion)

    fun addComment(idPromocion: String, texto: String) {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { uid ->
                val user = consultasUserRepo.obtenerUsuarioCompletoFlujo(uid).first()
                val comment = PromocionComentario(
                    id = java.util.UUID.randomUUID().toString(),
                    nombreUsuario = user?.perfil?.nombreVisible ?: "Usuario",
                    urlFotoUsuario = user?.perfil?.urlFoto as? String,
                    texto = texto
                )
                PromocionRepositorio.agregarComentario(idPromocion, comment)
            }
        }
    }

    /**
     * 🔥 [ELITE]: Generador de Banners con Ad-Priority.
     */
    fun generateHomeBanners(categories: List<com.example.myapplication.core.dominio.modelos.CategoriaDominio>, localPromotions: List<com.example.myapplication.core.dominio.modelos.PromocionDominio>): List<AccordionBanner> {
        android.util.Log.d("DIAGNOSTICO_HOME", "🛰️ [GEN_BANNERS] Categories: ${categories.size}, Promos: ${localPromotions.size}, Ads: ${_nativeAdPool.value.size}")
        
        val banners = mutableListOf<AccordionBanner>()
        
        localPromotions.take(5).forEach { promo ->
            banners.add(AccordionBanner(
                id = promo.id, title = promo.titulo, subtitle = promo.descripcion,
                icon = "🔥", color = Color(0xFFF59E0B), type = BannerType.PROMO,
                promotion = promo, imageUrl = promo.urlImagen
            ))
        }

        if (BeAdsManager.isAdSystemEnabled) {
            val adsNeeded = if (banners.isEmpty()) 2 else 1
            repeat(adsNeeded) { i ->
                val ad = _nativeAdPool.value.getOrNull(i)
                if (ad != null) {
                    banners.add(AccordionBanner(
                        id = "ad_home_$i", title = "Recomendado Elite", subtitle = "CONTENIDO PATROCINADO",
                        icon = "✨", color = Color(0xFF0F172A), type = BannerType.GOOGLE_AD,
                        nativeAd = ad
                    ))
                }
            }
        }
        
        if (banners.isEmpty()) {
            categories.filter { it.esNueva }.take(2).forEach { cat ->
                banners.add(AccordionBanner(
                    id = "new_cat_${cat.nombre}", title = cat.nombre.uppercase(), subtitle = "NUEVA CATEGORÍA",
                    icon = cat.icono, color = Color(0xFF3B82F6), type = BannerType.NEW_CATEGORY,
                    isNew = true
                ))
            }
        }

        return if (banners.size > 1) banners.shuffled() else banners
    }

    val promotions: StateFlow<List<com.example.myapplication.core.dominio.modelos.PromocionDominio>> = activeZipCode
        .flatMapLatest { zip -> PromocionRepositorio.obtenerPromocionesActivas(zip) }
        .map { list -> list.map { PromocionMappers.aUiModel(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

sealed class PromoItem {
    data class RealPromo(val promotion: com.example.myapplication.core.dominio.modelos.PromocionDominio) : PromoItem()
    data class GoogleAd(val id: String, val fallbackImageUrl: String?, val nativeAd: NativeAd? = null) : PromoItem()
    data class GoogleNativeAd(val nativeAd: NativeAd) : PromoItem()
}


