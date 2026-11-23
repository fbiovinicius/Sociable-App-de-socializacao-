package com.example.sociable.feature.events

import com.example.sociable.core.time.TimeProvider
import com.example.sociable.data.fake.FakeAuthRepository
import com.example.sociable.data.fake.FakeDatabase
import com.example.sociable.data.fake.FakeEventRepository
import com.example.sociable.data.fake.FakeSavedEventRepository
import java.time.Instant
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventListSavedTest {

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

    @Test
    fun `filtro Salvos mostra so os eventos salvos pela pessoa`() = runTest(dispatcher) {
        val db = FakeDatabase(timeProvider)
        val auth = FakeAuthRepository(db, timeProvider)
        auth.signIn("demo@sociable.app", "123456")
        val saved = FakeSavedEventRepository()
        saved.setSaved("user-1", "event-4", true)
        saved.setSaved("user-2", "event-3", true) // salvo por outra pessoa: não conta
        val viewModel = EventListViewModel(FakeEventRepository(db, timeProvider), saved, auth)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.events.size > 1)
        assertTrue(viewModel.uiState.value.showsDefaultFeed)

        viewModel.onSavedOnlyChange(true)
        advanceUntilIdle()

        assertEquals(listOf("event-4"), viewModel.uiState.value.events.map { it.id })
        assertFalse(viewModel.uiState.value.showsDefaultFeed)
    }

    @Test
    fun `limpar filtros desliga o filtro Salvos`() = runTest(dispatcher) {
        val db = FakeDatabase(timeProvider)
        val auth = FakeAuthRepository(db, timeProvider)
        auth.signIn("demo@sociable.app", "123456")
        val viewModel = EventListViewModel(
            FakeEventRepository(db, timeProvider),
            FakeSavedEventRepository(),
            auth,
        )
        viewModel.onSavedOnlyChange(true)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.events.isEmpty())

        viewModel.clearFilters()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.savedOnly)
        assertTrue(viewModel.uiState.value.events.isNotEmpty())
    }
}
