package com.example.myapplication.prestador

import com.example.myapplication.prestador.data.local.dao.ClienteDao
import com.example.myapplication.prestador.data.local.dao.PlantillaPresupuestoDao
import com.example.myapplication.prestador.data.local.dao.PresupuestoDao
import com.example.myapplication.prestador.data.local.entity.ClienteEntity
import com.example.myapplication.prestador.data.local.entity.PlantillaPresupuestoEntity
import com.example.myapplication.prestador.data.repository.RoomPresupuestoRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RoomPresupuestoRepositoryTest {

    private lateinit var presupuestoDao: PresupuestoDao
    private lateinit var clienteDao: ClienteDao
    private lateinit var plantillaDao: PlantillaPresupuestoDao
    private lateinit var repository: RoomPresupuestoRepository

    @BeforeEach
    fun setUp() {
        presupuestoDao = mockk()
        clienteDao = mockk()
        plantillaDao = mockk()
        repository = RoomPresupuestoRepository(presupuestoDao, clienteDao, plantillaDao)
    }

    @Test
    fun `getClienteById returns first value from dao flow`() = runTest {
        val cliente = ClienteEntity(id = "c-1", nombre = "Ana", email = "ana@test.com")
        every { clienteDao.getClienteById("c-1") } returns flowOf(cliente)

        val result = repository.getClienteById("c-1")

        assertEquals(cliente, result)
    }

    @Test
    fun `getGananciasDesde returns zero when dao result is null`() = runTest {
        coEvery { presupuestoDao.getGananciasDesde("provider-1", "2025-01-01") } returns null

        val result = repository.getGananciasDesde("provider-1", "2025-01-01")

        assertEquals(0.0, result)
    }

    @Test
    fun `savePlantilla delegates insert to dao`() = runTest {
        val plantilla = PlantillaPresupuestoEntity(id = "tpl-1", nombre = "Base")
        coEvery { plantillaDao.insert(plantilla) } just runs

        repository.savePlantilla(plantilla)

        coVerify(exactly = 1) { plantillaDao.insert(plantilla) }
    }
}
