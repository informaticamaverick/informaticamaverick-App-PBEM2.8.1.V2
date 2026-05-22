package com.example.myapplication.prestador.data.repository

import android.util.Log
import com.example.myapplication.prestador.data.remote.GeorefApiService
import com.example.myapplication.prestador.ui.profile.Localidad
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeorefRepository @Inject constructor(
    private val api: GeorefApiService
) {
    private var provinciasCache: List<Pair<String, String>>? = null
    private val localidadesCache = mutableMapOf<String, List<Localidad>>()

    suspend fun getProvincias(): List<String> {
        if (provinciasCache == null) {
            provinciasCache = api.getProvincias(
                    max = 100,
                    orden = "nombre"
                ).provincias.map { it.id to it.nombre }
                Log.d("GeorefRepository", "Provincias cargadas: ${provinciasCache!!.size}")
        }
        return provinciasCache!!.map { it.second }
    }

    suspend fun getLocalidades(provinciaNombre: String): List<Localidad> {
        localidadesCache[provinciaNombre]?.let { return it }

        if (provinciasCache == null) {
            provinciasCache = api.getProvincias(
                max = 100,
                orden = "nombre"
            ).provincias.map { it.id to it.nombre }
        }

        val id = provinciasCache!!
            .firstOrNull { it.second.equals(provinciaNombre, ignoreCase = true) }?.first
            ?: run {
                Log.w("GeorefRepository", "Provincia no encontrada: '$provinciaNombre'")
                return emptyList()
            }

        val result = api.getLocalidades(
            provinciaId = id,
            max = 1000,
            orden = "nombre"
        ).localidades.map {
            Localidad(nombre = it.nombre, codigoPostal = it.codigoPostal ?: "")
        }

        Log.d("GeorefRepository", "Localidades de $provinciaNombre: ${result.size}, " +
            "con CP: ${result.count { it.codigoPostal.isNotBlank() }}")

        localidadesCache[provinciaNombre] = result
        return result
    }

    suspend fun getCodigoPostalPorLocalidad(localidadNombre: String, provinciaNombre: String): String {
        if (provinciasCache == null) {
            provinciasCache = api.getProvincias(max = 100, orden = "nombre")
                .provincias.map { it.id to it.nombre }
        }
        val id = provinciasCache!!
            .firstOrNull { it.second.equals(provinciaNombre, ignoreCase = true) }?.first
            ?: return ""

        val cp = api.buscarCodigoPostal(
            nombre = localidadNombre,
            provinciaId = id,
            campos = "nombre,codigo_postal",
            max = 1
        ).localidades.firstOrNull()?.codigoPostal ?: ""

        Log.d("GeorefRepository", "CP de '$localidadNombre' en '$provinciaNombre': '$cp'")
        return cp
    }
}
