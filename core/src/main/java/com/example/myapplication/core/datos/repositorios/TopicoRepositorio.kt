package com.example.myapplication.core.dominio.repository

interface TopicoRepositorio {
    suspend fun subscribeToTopic(topic: String): Result<Unit>
    suspend fun unsubscribeFromTopic(topic: String): Result<Unit>
}


































