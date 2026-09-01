package com.example.myapplication.core.datos.local.entidades

import androidx.room.Entity
import androidx.room.Index
import androidx.annotation.Keep

/**
 * --- UNIÓN USUARIO-EMPRESA (Relación N:M) ---
 * 
 * [PROPÓSITO]: Vincular Usuarios (Clientes) con Empresas. 
 * Permite que un usuario sea dueño o esté vinculado a múltiples empresas (sólo nombres).
 */
@Keep
@Entity(
    tableName = "usuario_empresa_xref",
    primaryKeys = ["idUsuario", "idEmpresa"],
    indices = [
        Index(value = ["idUsuario"]),
        Index(value = ["idEmpresa"])
    ]
)
data class UsuarioEmpresaXRef(
    val idUsuario: String,
    val idEmpresa: String
)
