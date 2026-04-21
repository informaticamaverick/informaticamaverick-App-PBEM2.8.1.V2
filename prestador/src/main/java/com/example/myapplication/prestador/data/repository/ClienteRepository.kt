package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.dao.ClienteDao
import com.example.myapplication.prestador.data.local.entity.ClienteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClienteRepository @Inject constructor(
    private val clienteDao: ClienteDao
) {
    suspend fun saveCliente(cliente: ClienteEntity) {
        clienteDao.insertCliente(cliente)
    }

    suspend fun saveClientes(clientes: List<ClienteEntity>) {
        clienteDao.insertClientes(clientes)
    }

    suspend fun updateCliente(cliente: ClienteEntity) {
        clienteDao.updateCliente(cliente)
    }

    suspend fun deleteCliente(clienteId: String) {
        clienteDao.deleteClienteById(clienteId)
    }

    suspend fun deleteAllClientes() {
        clienteDao.deleteAllClientes()
    }

    fun getClienteById(clienteId: String): Flow<ClienteEntity?> {
        return clienteDao.getClienteById(clienteId)
    }

    fun getAllClientes(): Flow<List<ClienteEntity>> {
        return clienteDao.getAllClientes()
    }

    fun searchClientesByNombre(nombre: String): Flow<List<ClienteEntity>> {
        return clienteDao.searchClientesByNombre("%$nombre%")
    }

    fun getClienteByEmail(email: String): Flow<ClienteEntity?> {
        return clienteDao.getClienteByEmail(email)
    }

    fun getClienteByTelefono(telefono: String): Flow<ClienteEntity?> {
        return clienteDao.getClienteByTelefono(telefono)
    }

    suspend fun clienteExists(clienteId: String): Boolean {
        return clienteDao.clienteExists(clienteId)
    }

    suspend fun countClientes(): Int {
        return clienteDao.countClientes()
    }
}