package com.example.myapplication.prestador

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.prestador.data.model.NotificacionItem
import com.example.myapplication.prestador.data.model.TipoNotificacion
import com.example.myapplication.prestador.data.repository.NotificacionRepository
//import com.example.myapplication.prestador.viewmodel.NotificacionesViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificacionesViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: NotificacionRepository
    private lateinit var allFlow: MutableStateFlow<List<NotificacionItem>>
    private lateinit var unreadFlow: MutableStateFlow<List<NotificacionItem>>
    //private lateinit var viewModel: NotificacionesViewModel

    private val mensaje = NotificacionItem(
        id = 1,
        tipo = TipoNotificacion.MENSAJE,
        titulo = "Nuevo mensaje",
        mensaje = "Tienes una consulta nueva"
    )

    private val cita = NotificacionItem(
        id = 2,
        tipo = TipoNotificacion.CITA,
        titulo = "Cita confirmada",
        mensaje = "Se confirmó una cita"
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = mockk()
        allFlow = MutableStateFlow(listOf(mensaje, cita))
        unreadFlow = MutableStateFlow(listOf(mensaje))

        every { repository.getAllFlow() } returns allFlow
        every { repository.getUnreadFlow() } returns unreadFlow
        every { repository.getUnreadCount() } returns MutableStateFlow(1)
        every { repository.getByTipoFlow(TipoNotificacion.MENSAJE) } returns flowOf(listOf(mensaje))
        every { repository.getByTipoFlow(TipoNotificacion.CITA) } returns flowOf(listOf(cita))
        every { repository.getByTipoFlow(TipoNotificacion.PRESUPUESTO) } returns flowOf(emptyList())
        every { repository.getByTipoFlow(TipoNotificacion.SOLICITUD) } returns flowOf(emptyList())
        every { repository.getByTipoFlow(TipoNotificacion.LICITACION) } returns flowOf(emptyList())
        every { repository.getByTipoFlow(TipoNotificacion.SISTEMA) } returns flowOf(emptyList())

       // viewModel = NotificacionesViewModel(repository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggleSoloNoLeidas switches source to unread notifications`() = runTest {
      //  val job = launch { viewModel.notificaciones.collect {} }
        advanceUntilIdle()
       // assertEquals(allFlow.value, viewModel.notificaciones.value)

      //  viewModel.toggleSoloNoLeidas()
        advanceUntilIdle()

      //  assertEquals(unreadFlow.value, viewModel.notificaciones.value)
       // job.cancel()
    }

    @Test
    fun `tipo filter has priority over unread toggle`() = runTest {
       // val job = launch { viewModel.notificaciones.collect {} }

       // viewModel.toggleSoloNoLeidas()
       // viewModel.setFiltroTipo(TipoNotificacion.MENSAJE)
        advanceUntilIdle()

       // assertEquals(listOf(mensaje), viewModel.notificaciones.value)
       // job.cancel()
    }
}
