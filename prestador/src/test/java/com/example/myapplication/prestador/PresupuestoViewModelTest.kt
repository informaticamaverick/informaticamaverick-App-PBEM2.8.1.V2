package com.example.myapplication.prestador

import com.example.myapplication.core.domain.model.User
import com.example.myapplication.prestador.data.local.entity.PlantillaPresupuestoEntity
import com.example.myapplication.prestador.data.local.entity.PresupuestoEntity
import com.example.myapplication.prestador.data.repository.PresupuestoRepository
import com.example.myapplication.prestador.ui.presupuesto.BudgetItem
//import com.example.myapplication.prestador.viewmodel.PresupuestoViewModel
import com.example.myapplication.prestador.viewmodel.presupuesto.PresupuestoViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PresupuestoViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: PresupuestoRepository
    private lateinit var appScope: CoroutineScope
    private lateinit var presupuestosFlow: MutableStateFlow<List<PresupuestoEntity>>
    private lateinit var viewModel: PresupuestoViewModel

    private val presupuestoA = PresupuestoEntity(
        id = "p-1",
        numeroPresupuesto = "0001",
        clienteId = "c-1",
        prestadorId = "provider-1",
        fecha = "2025-01-10",
        subtotal = 100.0,
        impuestos = 21.0,
        total = 121.0,
        estado = "Pendiente"
    )

    private val presupuestoB = PresupuestoEntity(
        id = "p-2",
        numeroPresupuesto = "0002",
        clienteId = "c-2",
        prestadorId = "provider-1",
        fecha = "2025-01-11",
        subtotal = 200.0,
        impuestos = 42.0,
        total = 242.0,
        estado = "Pendiente"
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = mockk()
        appScope = CoroutineScope(SupervisorJob() + dispatcher)
        presupuestosFlow = MutableStateFlow(listOf(presupuestoA, presupuestoB))

        every { repository.getAllPresupuestos() } returns presupuestosFlow
        every { repository.getAllClientes() } returns flowOf(emptyList<User>())
        every { repository.getCatalogPresupuesto(any()) } returns flowOf(null)
        every { repository.getAllPlantillas() } returns flowOf(emptyList<PlantillaPresupuestoEntity>())

        viewModel = PresupuestoViewModel(repository, appScope)
    }

    @AfterEach
    fun tearDown() {
        appScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `saveArticleToSuggestions creates catalog when it does not exist`() = runTest {
        val item = BudgetItem(
            code = "ART-1",
            description = "Filtro de aire",
            quantity = 1,
            unitPrice = 1500.0,
            taxPercentage = 21.0,
            discountPercentage = 0.0
        )
        coEvery { repository.getPresupuestoById("__catalog_articles__") } returns null
        coEvery { repository.insertPresupuesto(any()) } just runs

        viewModel.saveArticleToSuggestions(item)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.insertPresupuesto(match {
                it.id == "__catalog_articles__" &&
                    it.estado == "__catalog__" &&
                    it.itemsJson == "ART-1;Filtro de aire;1;1500.0;21.0;0.0"
            })
        }
    }

    @Test
    fun `deleteSelected removes selected budgets and clears selection`() = runTest {
        coEvery { repository.deletePresupuesto(any()) } just runs
        val collectorJob = launch { viewModel.presupuestos.collect {} }
        val selectionCollectorJob = launch { viewModel.selectedIds.collect {} }
        advanceUntilIdle()

        viewModel.toggleSelection(presupuestoA.id)
        viewModel.deleteSelected()
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.deletePresupuesto(presupuestoA) }
        assertTrue(viewModel.selectedIds.value.isEmpty())

        collectorJob.cancel()
        selectionCollectorJob.cancel()
    }
}
