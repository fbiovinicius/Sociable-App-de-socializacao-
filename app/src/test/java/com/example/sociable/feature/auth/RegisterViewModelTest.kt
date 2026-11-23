package com.example.sociable.feature.auth

import com.example.sociable.core.AppError
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.data.fake.FakeAuthRepository
import com.example.sociable.data.fake.FakeDatabase
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

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

    private fun viewModel(): RegisterViewModel {
        val db = FakeDatabase(timeProvider)
        return RegisterViewModel(FakeAuthRepository(db, timeProvider))
    }

    @Test
    fun `conta Google nova vai para o onboarding`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onGoogleToken("ana")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.isRegistered)
        assertFalse(state.isSignedIn)
        assertFalse(state.isGoogleLoading)
        assertNull(state.error)
    }

    @Test
    fun `conta Google que ja existe entra direto no app`() = runTest(dispatcher) {
        val db = FakeDatabase(timeProvider)
        val auth = FakeAuthRepository(db, timeProvider)
        auth.signInWithGoogle("ana") // cria a conta
        val vm = RegisterViewModel(auth)

        vm.onGoogleToken("ana")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.isSignedIn)
        assertFalse(state.isRegistered)
    }

    @Test
    fun `token vazio mostra erro e nao entra`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onGoogleToken("  ")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(AppError.INVALID_CREDENTIALS, state.error)
        assertFalse(state.isRegistered || state.isSignedIn)
        assertFalse(state.isGoogleLoading)
    }

    @Test
    fun `Google indisponivel mostra o aviso`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onGoogleUnavailable()

        assertEquals(AppError.GOOGLE_SIGN_IN_UNAVAILABLE, vm.uiState.value.error)
    }
}
