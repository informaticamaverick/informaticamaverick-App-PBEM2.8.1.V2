/*
package com.example.myapplication.prestador.viewmodel.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.modelos.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * --- VIEWMODEL EDITOR DE PERFIL DEL PRESTADOR (ELITE v2026.FINAL) ---
 * [LEY #9]: Estándar Mav en Español.
 */
@HiltViewModel
class PrestadorPerfilEditorViewModel @Inject constructor() : ViewModel() {

    private val _estado = MutableStateFlow(EstadoEdicionPerfilMav())
    val estado: StateFlow<EstadoEdicionPerfilMav> = _estado.asStateFlow()

    /**
     * 🔥 [ELITE]: Carga una identidad y su dirección en el editor.
     */
    fun iniciarEdicion(identidad: IdentidadPrestadorEntity, direccion: DireccionDominio? = null) {
        _estado.value = EstadoEdicionPerfilMav(
            id = identidad.id,
            tipoIdentidad = "PRESTADOR",
            nombreVisible = identidad.nombreVisible,
            nombre = identidad.nombre,
            apellido = identidad.apellido,
            biografia = identidad.biografia,
            categorias = identidad.idCategorias,
            urlFotoPerfil = identidad.urlFotoPerfil,
            brindaServicio = identidad.brindaServicio,
            brindaProducto = identidad.brindaProducto,
            direccion = direccion ?: DireccionDominio()
        )
    }

    fun actualizarNombreVisible(nuevo: String) {
        _estado.update { it.copy(nombreVisible = nuevo) }
    }

    fun actualizarEstado(nuevo: EstadoEdicionPerfilMav) {
        _estado.value = nuevo
    }

    fun cambiarFotoPerfil(uri: Uri) {
        _estado.update { it.copy(nuevaFotoLocalUri = uri) }
    }

    fun actualizarDireccion(nueva: DireccionDominio, esPrincipal: Boolean) {
        if (esPrincipal) _estado.update { it.copy(direccion = nueva) }
        else {
            _estado.update { 
                it.copy(direccionesAdicionales = it.direccionesAdicionales.toMutableList().apply { 
                    // Lógica de reemplazo por ID si fuera necesario
                })
            }
        }
    }

    fun limpiarBorrador() {
        _estado.value = EstadoEdicionPerfilMav()
    }
}
*/
