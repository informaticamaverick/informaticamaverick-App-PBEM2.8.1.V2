package com.example.myapplication.prestador.viewmodel.promocion

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.repositorios.PromocionRepositorio
import com.example.myapplication.core.dominio.modelos.PromocionDominio
import com.example.myapplication.core.dominio.mapeadores.PromocionMappers
import com.example.myapplication.core.servicios.publicidad.BeAdsManager
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdRequest
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PromocionFeedItem {
    data class Promo(val uiModel: PromocionDominio) : PromocionFeedItem()
    data class Ad(val nativeAd: NativeAd) : PromocionFeedItem()
    data class FallbackAd(val id: String) : PromocionFeedItem()
}

/**
 * --- VIEWMODEL DE FEED DE PROMOCIONES (PRESTADOR v2026.FINAL) ---
 * [ELITE]: Ahora con soporte para Google Ads (Multimedia/Video) en el mercado B2B.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PrestadorPromocionFeedViewModel @Inject constructor(
    private val promotionRepository: PromocionRepositorio,
    private val auth: FirebaseAuth,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando.asStateFlow()

    private val _zipcodigo = MutableStateFlow<String?>(null)
    private val currentUserId = auth.currentUser?.uid ?: ""

    private val _nativeAdPool = MutableStateFlow<List<NativeAd>>(emptyList())
    
    init {
        if (BeAdsManager.isAdSystemEnabled) preCargarAnuncios(cantidad = 5)
    }

    private fun preCargarAnuncios(cantidad: Int) {
        val adLoader = AdLoader.Builder(context, BeAdsManager.TEST_NATIVE_ID)
            .forNativeAd { ad -> _nativeAdPool.update { (it + ad).distinct() } }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("PrestadorPromoVM", "❌ Fallo pre-carga: ${error.message}")
                }
            })
            .withNativeAdOptions(BeAdsManager.buildNativeAdOptions())
            .build()
        repeat(cantidad) { adLoader.loadAd(AdRequest.Builder().build()) }
    }

    fun obtenerAnuncioDelPool(): NativeAd? {
        val currentPool = _nativeAdPool.value
        if (currentPool.isEmpty()) {
            preCargarAnuncios(3)
            return null
        }
        val ad = currentPool.first()
        _nativeAdPool.value = currentPool.drop(1)
        if (_nativeAdPool.value.size < 3) preCargarAnuncios(2)
        return ad
    }

    val historias: StateFlow<List<PromocionDominio>> = _zipcodigo
        .flatMapLatest { zip -> promotionRepository.obtenerHistoriasActivas(zip) }
        .map { list -> 
            list.filter { it.idPrestador != currentUserId }
                .map { PromocionMappers.aUiModel(it) } 
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 🔥 [REQUERIMIENTO APP NARANJA]: En la app naranja, en la pantalla de promos, por el momento
    // solo se muestran anuncios de Google Ads (estáticos y multimedia).
    // Se deja comentada la lógica de promociones orgánicas con // para fácil reactivación.
    /*
    val feedItemsOrganico: StateFlow<List<PromocionFeedItem>> = _zipcodigo
        .flatMapLatest { zip -> promotionRepository.obtenerPromocionesActivas(zip) }
        .map { list -> 
            // // Filtro por ID de usuario prestador comentado momentáneamente:
            // val orgList = list.filter { it.idPrestador != currentUserId }
            val orgList = list.map { PromocionFeedItem.Promo(PromocionMappers.aUiModel(it)) }
            
            val result = mutableListOf<PromocionFeedItem>()
            if (orgList.isEmpty() && BeAdsManager.isAdSystemEnabled) {
                val ad = obtenerAnuncioDelPool()
                if (ad != null) result.add(PromocionFeedItem.Ad(ad))
                else result.add(PromocionFeedItem.FallbackAd("initial_ad"))
            } else {
                orgList.forEachIndexed { index, item ->
                    result.add(item)
                    if ((index + 1) % 4 == 0) {
                        obtenerAnuncioDelPool()?.let { result.add(PromocionFeedItem.Ad(it)) }
                    }
                }
            }
            result
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    */

    val feedItems: StateFlow<List<PromocionFeedItem>> = _nativeAdPool.map { pool ->
        val result = mutableListOf<PromocionFeedItem>()
        if (BeAdsManager.isAdSystemEnabled) {
            if (pool.isNotEmpty()) {
                pool.forEach { ad -> result.add(PromocionFeedItem.Ad(ad)) }
            } else {
                // Fallback continuo con anuncios publicitarios estáticos/multimedia
                repeat(5) { index ->
                    result.add(PromocionFeedItem.FallbackAd("ad_fallback_$index"))
                }
            }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refrescar(zip: String? = null) {
        viewModelScope.launch {
            _estaCargando.value = true
            _zipcodigo.value = zip
            if (BeAdsManager.isAdSystemEnabled && _nativeAdPool.value.size < 3) {
                preCargarAnuncios(4)
            }
            promotionRepository.sincronizarPromocionesRemotas(zip)
            _estaCargando.value = false
        }
    }

    fun toggleLike(id: String) {
        viewModelScope.launch {
            promotionRepository.alternarLike(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        _nativeAdPool.value.forEach { it.destroy() }
    }
}


