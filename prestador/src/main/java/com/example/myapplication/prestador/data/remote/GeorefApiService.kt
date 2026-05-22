package com.example.myapplication.prestador.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface GeorefApiService {

    @GET("provincias")
    suspend fun getProvincias(
        @Query("max") max: Int,
        @Query("orden") orden: String
    ): ProvinciaResponse

    @GET("localidades")
    suspend fun getLocalidades(
        @Query("provincia") provinciaId: String,
        @Query("max") max: Int,
        @Query("orden") orden: String
    ): LocalidadResponse

    @GET("localidades")
    suspend fun buscarCodigoPostal(
        @Query("nombre") nombre: String,
        @Query("provincia") provinciaId: String,
        @Query(value = "campos", encoded = true) campos: String,
        @Query("max") max: Int
    ): LocalidadResponse
}