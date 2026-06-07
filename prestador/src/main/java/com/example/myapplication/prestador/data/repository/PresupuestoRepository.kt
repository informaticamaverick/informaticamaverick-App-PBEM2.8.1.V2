package com.example.myapplication.prestador.data.repository

import com.example.myapplication.core.data.local.dao.UserDao
import com.example.myapplication.prestador.data.local.dao.PresupuestoDao
import com.example.myapplication.core.domain.model.User
import com.example.myapplication.prestador.data.local.entity.PlantillaPresupuestoEntity
import com.example.myapplication.prestador.data.local.entity.PresupuestoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.example.myapplication.prestador.data.local.dao.PlantillaPresupuestoDao

interface PresupuestoRepository {
    fun getAllPresupuestos(): Flow<List<PresupuestoEntity>>
    fun getPresupuestosByEstado(estado: String): Flow<List<PresupuestoEntity>>
    fun getPresupuestosByAppointment(appointmentId: String): Flow<List<PresupuestoEntity>>
    suspend fun getPresupuestoById(id: String): PresupuestoEntity?
    fun getCatalogPresupuesto(id: String): Flow<PresupuestoEntity?>
    suspend fun getLatestPresupuestoForAppointment(appointmentId: String): PresupuestoEntity?
    suspend fun insertPresupuesto(presupuesto: PresupuestoEntity)
    suspend fun updatePresupuesto(presupuesto: PresupuestoEntity)
    suspend fun updateEstado(id: String, estado: String)
    suspend fun deletePresupuesto(presupuesto: PresupuestoEntity)
    suspend fun getTotalGanancias(prestadorId: String): Double
    suspend fun getGananciasDesde(prestadorId: String, fromDate: String): Double

    // Clientes (Delegado a Core)
    fun getAllClientes(): Flow<List<User>>
    suspend fun getClienteById(id: String): User?
    suspend fun insertCliente(cliente: User)

    // Plantillas
    fun getAllPlantillas(): Flow<List<PlantillaPresupuestoEntity>>
    suspend fun savePlantilla(plantillaPresupuestoEntity: PlantillaPresupuestoEntity)
    suspend fun deletePlantilla(id: String)
}

// Implementación con Room (local)
@Singleton
class RoomPresupuestoRepository @Inject constructor(
    private val presupuestoDao: PresupuestoDao,
    private val userDao: UserDao,
    private val plantillaDao: PlantillaPresupuestoDao
) : PresupuestoRepository {
    
    override fun getAllPresupuestos(): Flow<List<PresupuestoEntity>> =
        presupuestoDao.getAllPresupuestos()
    
    override fun getPresupuestosByEstado(estado: String): Flow<List<PresupuestoEntity>> =
        presupuestoDao.getPresupuestosByEstado(estado)

    override fun getPresupuestosByAppointment(appointmentId: String): Flow<List<PresupuestoEntity>> =
        presupuestoDao.getPresupuestosByAppointment(appointmentId)

    override suspend fun getPresupuestoById(id: String): PresupuestoEntity? =
        presupuestoDao.getPresupuestoById(id)

    override fun getCatalogPresupuesto(id: String): Flow<PresupuestoEntity?> =
        presupuestoDao.getPresupuestoByIdAsFlow(id)

    override suspend fun getLatestPresupuestoForAppointment(appointmentId: String): PresupuestoEntity? =
        presupuestoDao.getLatestPresupuestoForAppointment(appointmentId)

    override suspend fun insertPresupuesto(presupuesto: PresupuestoEntity) =
        presupuestoDao.insertPresupuesto(presupuesto)

    override suspend fun updatePresupuesto(presupuesto: PresupuestoEntity) =
        presupuestoDao.updatePresupuesto(presupuesto)

    override suspend fun updateEstado(id: String, estado: String) =
        presupuestoDao.updateEstado(id, estado)

    override suspend fun deletePresupuesto(presupuesto: PresupuestoEntity) =
        presupuestoDao.deletePresupuesto(presupuesto)

    override suspend fun getTotalGanancias(prestadorId: String): Double =
        presupuestoDao.getTotalGanancias(prestadorId) ?: 0.0

    override suspend fun getGananciasDesde(prestadorId: String, fromDate: String): Double =
        presupuestoDao.getGananciasDesde(prestadorId, fromDate) ?: 0.0
    
    override fun getAllClientes(): Flow<List<User>> =
        userDao.getAllUsers().map { it.map { e -> e.toDomain() } }
    
    override suspend fun getClienteById(id: String): User? =
        userDao.getUserByIdFlow(id).firstOrNull()?.toDomain()
    
    override suspend fun insertCliente(cliente: User) =
        userDao.insertOrUpdateUser(com.example.myapplication.core.data.local.entity.UserEntity.fromDomain(cliente))


    //PLANTILLAS

    override suspend fun savePlantilla(plantillaPresupuestoEntity: PlantillaPresupuestoEntity) {
        plantillaDao.insert(plantillaPresupuestoEntity)
    }

    override fun getAllPlantillas(): Flow<List<PlantillaPresupuestoEntity>> {
        return plantillaDao.getAll()
    }

    suspend fun getAllPlantillasSync(): List<PlantillaPresupuestoEntity> {
        return plantillaDao.getAllSync()
    }

    override suspend fun deletePlantilla(id: String) {
        plantillaDao.deleteById(id)
    }



}
