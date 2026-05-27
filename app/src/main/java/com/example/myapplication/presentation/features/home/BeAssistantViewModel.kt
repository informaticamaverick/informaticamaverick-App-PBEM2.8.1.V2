package com.example.myapplication.presentation.features.home

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.presentation.components.BeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BE ASSISTANT VIEWMODEL (EL COREÓGRAFO)
 * Maneja el "trabajo sucio" de las animaciones, estados de transición,
 * y coordinación física del asistente y la barra de navegación.
 */
@HiltViewModel
class BeAssistantViewModel @Inject constructor() : ViewModel() {

    // ======================================================================================
    // --- 1. ESTADOS DE POSICIÓN Y FÍSICA ---
    // ======================================================================================
    private val _offsetX = MutableStateFlow(0f)
    val offsetX: StateFlow<Float> = _offsetX.asStateFlow()

    private val _offsetY = MutableStateFlow(0f)
    val offsetY: StateFlow<Float> = _offsetY.asStateFlow()

    private val _isDragging = MutableStateFlow(false)
    val isDragging: StateFlow<Boolean> = _isDragging.asStateFlow()

    fun updateOffset(x: Float, y: Float) {
        _offsetX.value = x
        _offsetY.value = y
    }

    fun setDragging(dragging: Boolean) {
        _isDragging.value = dragging
    }

    fun resetPosition() {
        _offsetX.value = 0f
        _offsetY.value = 0f
    }

    // ======================================================================================
    // --- 2. ESTADOS DE ANIMACIÓN Ocular ---
    // ======================================================================================
    private val _targetPupilX = MutableStateFlow(0f)
    val targetPupilX: StateFlow<Float> = _targetPupilX.asStateFlow()

    private val _targetPupilY = MutableStateFlow(0f)
    val targetPupilY: StateFlow<Float> = _targetPupilY.asStateFlow()

    private val _isBlinking = MutableStateFlow(false)
    val isBlinking: StateFlow<Boolean> = _isBlinking.asStateFlow()

    private var eyeLogicJob: Job? = null

    init {
        startEyeLogic()
    }

    private fun startEyeLogic() {
        eyeLogicJob?.cancel()
        eyeLogicJob = viewModelScope.launch {
            launch {
                while (true) {
                    delay((2500..7000).random().toLong())
                    _isBlinking.value = true
                    delay(150)
                    _isBlinking.value = false
                }
            }
            // La lógica de mirada se actualizará según el estado dictado por BeBrain
        }
    }

    fun updateMirada(state: BeState, hasEmotion: Boolean = false) {
        viewModelScope.launch {
            when (state) {
                BeState.IDLE -> {
                    _targetPupilX.value = (-2..2).random().toFloat()
                    _targetPupilY.value = (-3..3).random().toFloat()
                }
                BeState.NOTIFICATION_READY -> {
                    _targetPupilX.value = -2.5f
                    _targetPupilY.value = -3f
                }
                BeState.TALKING -> {
                    if (hasEmotion) {
                        _targetPupilX.value = 0f
                        _targetPupilY.value = 4f
                    } else {
                        _targetPupilX.value = 0f
                        _targetPupilY.value = 0f
                    }
                }
            }
        }
    }

    // ======================================================================================
    // --- 3. ESTADOS DE ESTABILIDAD DE TOOLBAR ---
    // ======================================================================================
    private val _isToolbarStable = MutableStateFlow(false)
    val isToolbarStable: StateFlow<Boolean> = _isToolbarStable.asStateFlow()

    private var stabilityJob: Job? = null

    fun notifyToolboxChanged() {
        _isToolbarStable.value = false
        stabilityJob?.cancel()
        stabilityJob = viewModelScope.launch {
            delay(150)
            _isToolbarStable.value = true
        }
    }

    // ======================================================================================
    // --- 4. LAYOUT Y PADDING (COORDINACIÓN CON NAV BAR) ---
    // ======================================================================================
    private val _beBottomPadding = MutableStateFlow(0.dp)
    val beBottomPadding: StateFlow<Dp> = _beBottomPadding.asStateFlow()

    /**
     * Actualiza el padding inferior dinámico de Be.
     * @param isBottomBarVisible Indica si la barra de navegación está presente.
     * @param isSearchActive Indica si Be está en modo búsqueda (se ancla arriba).
     */
    fun updateLayout(isBottomBarVisible: Boolean, isSearchActive: Boolean) {
        viewModelScope.launch {
            // Aumentamos de 62.dp a 72.dp para dar un margen de 10.dp sobre la barra
            _beBottomPadding.value = if (isSearchActive) 0.dp else if (isBottomBarVisible) 72.dp else 8.dp
        }
    }
}









