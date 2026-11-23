package com.example.sociable.feature.events

import com.example.sociable.core.notifications.EventReminder
import com.example.sociable.core.notifications.ReminderScheduler
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.data.fake.FakeAuthRepository
import com.example.sociable.data.fake.FakeDatabase
import com.example.sociable.data.fake.FakeEventRepository
import com.example.sociable.data.fake.FakeReviewRepository
import com.example.sociable.data.fake.FakeSavedEventRepository
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class RecordingReminderScheduler : ReminderScheduler {
    val scheduled = mutableListOf<EventReminder>()
    val cancelled = mutableListOf<String>()

    override fun schedule(reminder: EventReminder, leadMinutes: Long) {
        scheduled += reminder
    }

    override fun cancel(eventId: String) {
        cancelled += eventId
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class EventDetailSaveTest {

    private val dispatcher = StandardTestDispatcher()
    private val timeProvider = TimeProvider { Instant.parse("2026-05-10T16:00:00Z") }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    internal class Setup(
        val viewModel: EventDetailViewModel,
        val scheduler: RecordingReminderScheduler,
    )

    /** Ana (user-1) abrindo o "event-4": evento de outra pessoa, daqui a 3 dias, sem RSVP. */
    private suspend fun TestScope.setup(): Setup {
        val db = FakeDatabase(timeProvider)
        val auth = FakeAuthRepository(db, timeProvider)
        auth.signIn("demo@sociable.app", "123456")
        val scheduler = RecordingReminderScheduler()
        val viewModel = EventDetailViewModel(
            eventId = "event-4",
            eventRepository = FakeEventRepository(db, timeProvider),
            reviewRepository = FakeReviewRepository(db, timeProvider),
            savedEventRepository = FakeSavedEventRepository(),
            authRepository = auth,
            reminderScheduler = scheduler,
        )
        advanceUntilIdle()
        return Setup(viewModel, scheduler)
    }

    @Test
    fun `salvar o evento marca como salvo e agenda o lembrete`() = runTest(dispatcher) {
        val (viewModel, scheduler) = setup().let { it.viewModel to it.scheduler }
        assertFalse(viewModel.uiState.value.isSaved)

        viewModel.onToggleSave()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)
        assertEquals(listOf("event-4"), scheduler.scheduled.map { it.eventId })
    }

    @Test
    fun `remover dos salvos cancela o lembrete`() = runTest(dispatcher) {
        val (viewModel, scheduler) = setup().let { it.viewModel to it.scheduler }
        viewModel.onToggleSave()
        advanceUntilIdle()

        viewModel.onToggleSave()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSaved)
        assertEquals(listOf("event-4"), scheduler.cancelled)
    }

    @Test
    fun `remover dos salvos mantem o lembrete de quem vai ao evento`() = runTest(dispatcher) {
        val (viewModel, scheduler) = setup().let { it.viewModel to it.scheduler }
        viewModel.onToggleSave()
        viewModel.onToggleRsvp()
        advanceUntilIdle()
        scheduler.cancelled.clear()

        viewModel.onToggleSave()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSaved)
        assertTrue(scheduler.cancelled.isEmpty())
    }

    @Test
    fun `cancelar presenca mantem o lembrete de quem salvou o evento`() = runTest(dispatcher) {
        val (viewModel, scheduler) = setup().let { it.viewModel to it.scheduler }
        viewModel.onToggleSave()
        advanceUntilIdle()
        viewModel.onToggleRsvp() // vai
        advanceUntilIdle()

        viewModel.onToggleRsvp() // cancela a presença
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)
        assertTrue(scheduler.cancelled.isEmpty())
    }
}
