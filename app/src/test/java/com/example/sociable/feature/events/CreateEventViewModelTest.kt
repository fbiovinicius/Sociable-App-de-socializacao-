package com.example.sociable.feature.events

import com.example.sociable.core.time.ManausTime
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.core.validation.ValidationError
import com.example.sociable.core.validation.Validators
import com.example.sociable.data.fake.FakeAuthRepository
import com.example.sociable.data.fake.FakeDatabase
import com.example.sociable.data.fake.FakeEventRepository
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateEventViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val now = Instant.parse("2026-05-10T16:00:00Z") // 12:00 em Manaus
    private val timeProvider = TimeProvider { now }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun viewModel(): CreateEventViewModel {
        val db = FakeDatabase(timeProvider)
        val auth = FakeAuthRepository(db, timeProvider)
        auth.signIn("demo@sociable.app", "123456")
        return CreateEventViewModel(FakeEventRepository(db, timeProvider), auth, timeProvider)
    }

    @Test
    fun `estado inicial comeca na proxima hora cheia e dura duas horas`() = runTest(dispatcher) {
        val state = viewModel().uiState.value

        assertEquals(Instant.parse("2026-05-10T17:00:00Z"), state.startAt)
        assertEquals(Instant.parse("2026-05-10T19:00:00Z"), state.endAt)
        assertEquals(120, state.durationMinutes)
        assertFalse(state.isEndCustom)
    }

    @Test
    fun `trocar a data mantem a hora de inicio e a duracao`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onDateChange(LocalDate.of(2026, 5, 15))

        val state = vm.uiState.value
        assertEquals(Instant.parse("2026-05-15T17:00:00Z"), state.startAt)
        assertEquals(Instant.parse("2026-05-15T19:00:00Z"), state.endAt)
        assertEquals(LocalDate.of(2026, 5, 15), ManausTime.localDate(state.startAt))
        assertEquals(LocalTime.of(13, 0), ManausTime.localTime(state.startAt))
    }

    @Test
    fun `trocar a hora de inicio recalcula o fim pela duracao`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onStartTimeChange(LocalTime.of(20, 0))

        val state = vm.uiState.value
        assertEquals(Instant.parse("2026-05-11T00:00:00Z"), state.startAt)
        assertEquals(Instant.parse("2026-05-11T02:00:00Z"), state.endAt)
    }

    @Test
    fun `escolher duracao recalcula o fim`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onDurationChange(180)

        val state = vm.uiState.value
        assertEquals(180, state.durationMinutes)
        assertEquals(Instant.parse("2026-05-10T20:00:00Z"), state.endAt)
    }

    @Test
    fun `duracao e limitada entre cinco minutos e sete dias`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onDurationChange(1)
        assertEquals(5, vm.uiState.value.durationMinutes)

        vm.onDurationChange(999_999)
        assertEquals(
            Validators.MAX_EVENT_DURATION_MINUTES.toInt(),
            vm.uiState.value.durationMinutes,
        )
    }

    @Test
    fun `fim ajustado a mao nao muda ao trocar o inicio`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onEndTimeChange(LocalTime.of(23, 0))
        val customEnd = vm.uiState.value.endAt
        assertTrue(vm.uiState.value.isEndCustom)
        assertEquals(Instant.parse("2026-05-11T03:00:00Z"), customEnd)

        vm.onStartTimeChange(LocalTime.of(19, 0))

        assertEquals(Instant.parse("2026-05-10T23:00:00Z"), vm.uiState.value.startAt)
        assertEquals(customEnd, vm.uiState.value.endAt)
    }

    @Test
    fun `voltar a usar a duracao descarta o fim ajustado`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onEndTimeChange(LocalTime.of(23, 0))
        vm.onUseDuration()

        val state = vm.uiState.value
        assertFalse(state.isEndCustom)
        assertEquals(Instant.parse("2026-05-10T19:00:00Z"), state.endAt)
    }

    @Test
    fun `fim antes do inicio gera erro de periodo`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onEndDateChange(LocalDate.of(2026, 5, 9))
        vm.onSubmit()

        assertEquals(ValidationError.END_BEFORE_START, vm.uiState.value.periodError)
    }

    @Test
    fun `fim com mais de sete dias gera erro de duracao`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onEndDateChange(LocalDate.of(2026, 5, 20))
        vm.onSubmit()

        assertEquals(ValidationError.INVALID_DURATION, vm.uiState.value.periodError)
    }

    @Test
    fun `alterar a data limpa o erro de periodo`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onEndDateChange(LocalDate.of(2026, 5, 9))
        vm.onSubmit()
        assertEquals(ValidationError.END_BEFORE_START, vm.uiState.value.periodError)

        vm.onDateChange(LocalDate.of(2026, 5, 12))
        vm.onEndDateChange(LocalDate.of(2026, 5, 12))
        vm.onUseDuration()

        assertNull(vm.uiState.value.periodError)
    }

    @Test
    fun `carregar para editar preenche o formulario e a duracao do evento`() = runTest(dispatcher) {
        val viewModel = viewModel()
        val ownEvent = FakeDatabase(timeProvider).events.value.first { it.organizerId == "user-1" }

        viewModel.loadForEdit(ownEvent.id)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(ownEvent.id, state.editingEventId)
        assertEquals(ownEvent.title, state.title)
        assertEquals(ownEvent.startAt, state.startAt)
        assertEquals(ownEvent.endAt, state.endAt)
        assertEquals(
            java.time.Duration.between(ownEvent.startAt, ownEvent.endAt).toMinutes().toInt(),
            state.durationMinutes,
        )
        assertFalse(state.isEndCustom)
        assertFalse(state.isLoadingEvent)
    }

    @Test
    fun `editar evento de outra pessoa e recusado`() = runTest(dispatcher) {
        val viewModel = viewModel()
        val foreignEvent = FakeDatabase(timeProvider).events.value.first { it.organizerId != "user-1" }

        viewModel.loadForEdit(foreignEvent.id)
        advanceUntilIdle()

        assertEquals(com.example.sociable.core.AppError.PERMISSION_DENIED, viewModel.uiState.value.error)
        assertEquals("", viewModel.uiState.value.title)
    }

    @Test
    fun `evento aberto para edicao nao vira segredinho`() = runTest(dispatcher) {
        val viewModel = viewModel()
        val ownEvent = FakeDatabase(timeProvider).events.value.first { it.organizerId == "user-1" && !it.isSecret }

        viewModel.loadForEdit(ownEvent.id)
        advanceUntilIdle()
        viewModel.onSecretChange(true)

        assertFalse(viewModel.uiState.value.isSecret)
    }
}
