package com.example.myapplication.core.datos.local.entidades

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * --- ENTIDAD DE CONVERSACIÓN MAVERICK (UNIFICADO 2026) ---
 * Representa el resumen de un chat en la lista de mensajes.
 * [LEY #4]: Permite carga instantánea de la lista de chats.
 */
@Entity(
    tableName = "conversaciones",
    indices = [Index(value = ["idChat"], unique = true)]
)
data class ConversacionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    val rowid: Long = 0, // 🔥 [ELITE]: Para compatibilidad FTS4/5
    val idChat: String,  // idIdentidadLocal_idIdentidadRemota

    // --- CONTEXTO DE FILTRADO ---
    val idIdentidadLocal: String,    // Perfil con el que el usuario está viendo el chat
    val idIdentidadRemota: String,   // Perfil del otro participante

    // --- CONTENIDO RECIENTE ---
    val ultimoMensaje: String = "",
    val fechaUltimoMensaje: Long = System.currentTimeMillis(),
    val tipoUltimoMensaje: String = "TEXTO",

    // --- METADATOS DE UI ---
    val nombreRemoto: String = "",
    val fotoRemotaUrl: String? = null,
    val miniaturaRemotaBase64: String? = null,
    val idCategoriaRemota: String? = null, // 🔥 [NEW v2026.ELITE]: Para filtrado por rubro instantáneo
    val contadorNoLeidos: Int = 0,

    // --- ESTADOS ---
    val estaFijada: Boolean = false,
    val estaSilenciada: Boolean = false,
    val estaArchivada: Boolean = false
)
