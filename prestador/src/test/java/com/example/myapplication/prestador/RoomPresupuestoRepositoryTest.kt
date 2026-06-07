package com.example.myapplication.prestador

import com.example.myapplication.core.data.local.dao.UserDao
import com.example.myapplication.core.data.local.entity.UserEntity
import com.example.myapplication.prestador.data.local.dao.PlantillaPresupuestoDao
import com.example.myapplication.prestador.data.local.dao.PresupuestoDao
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
    private lateinit var userDao: UserDao
    private lateinit var plantillaDao: PlantillaPresupuestoDao
    private lateinit var repository: RoomPresupuestoRepository

    @BeforeEach
    fun setUp() {
        presupuestoDao = mockk()
        userDao = mockk()
        plantillaDao = mockk()
        repository = RoomPresupuestoRepository(presupuestoDao, userDao, plantillaDao)
    }

    @Test
    fun `getClienteById returns first value from dao flow`() = runTest {
        val userEntity = UserEntity(id = "c-1", name = "Ana", email = "ana@test.com")
        every { userDao.getUserByIdFlow("c-1") } returns flowOf(userEntity)

        val result = repository.getClienteById("c-1")

        assertEquals(userEntity.toDomain(), result)
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
