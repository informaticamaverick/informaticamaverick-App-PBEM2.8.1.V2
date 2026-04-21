/**
package com.example.myapplication.data.remote

import com.example.myapplication.data.model.LoginResponse
import com.example.myapplication.data.model.LoginResquest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginResquest): Response<LoginResponse>
}
**/