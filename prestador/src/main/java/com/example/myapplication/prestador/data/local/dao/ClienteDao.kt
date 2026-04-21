package com.example.myapplication.prestador.data.local.dao

import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.ClienteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    
    @Query("SELECT * FROM clientes ORDER BY nombre ASC")
    fun getAllClientes(): Flow<List<ClienteEntity>>
    
    @Query("SELECT * FROM clientes WHERE id = :clienteId")
    fun getClienteById(clienteId: String): Flow<ClienteEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCliente(cliente: ClienteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClientes(clientes: List<ClienteEntity>)
    
    @Update
    suspend fun updateCliente(cliente: ClienteEntity)
    
    @Delete
    suspend fun deleteCliente(cliente: ClienteEntity)
    
    @Query("DELETE FROM clientes WHERE id = :clienteId")
    suspend fun deleteClienteById(clienteId: String)

    @Query("DELETE FROM clientes")
    suspend fun deleteAllClientes()

    @Query("SELECT * FROM clientes WHERE nombre LIKE :nombre")
    fun searchClientesByNombre(nombre: String): Flow<List<ClienteEntity>>

    @Query("SELECT * FROM clientes WHERE email = :email")
    fun getClienteByEmail(email: String): Flow<ClienteEntity?>

    @Query("SELECT * FROM clientes WHERE telefono = :telefono")
    fun getClienteByTelefono(telefono: String): Flow<ClienteEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM clientes WHERE id = :clienteId)")
    suspend fun clienteExists(clienteId: String): Boolean

    @Query("SELECT COUNT(*) FROM clientes")
    suspend fun countClientes(): Int
}