package com.example.sociable.feature.onboarding

import com.example.sociable.core.AppError
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.data.fake.FakeAuthRepository
import com.example.sociable.data.fake.FakeDatabase
import com.example.sociable.data.fake.FakeUserRepository
import com.example.sociable.data.model.LifeStage
import com.example.sociable.data.model.LookingFor
import com.example.sociable.data.model.LookingForRules
import com.example.sociable.data.model.SocialVibe
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
class OnboardingViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val now = Instant.parse("2026-05-10T16:00:00Z")
    private val timeProvider = TimeProvider { now }

    private data class Dependencies(
        val db: FakeDatabase,
        val auth: FakeAuthRepository,
        val users: FakeUserRepository,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun dependencies(signedIn: Boolean = true): Dependencies {
        val db = FakeDatabase(timeProvider)
        val auth = FakeAuthRepository(db, timeProvider)
        if (signedIn) auth.signIn("demo@sociable.app", "123456")
        return Dependencies(db, auth, FakeUserRepository(db))
    }

    @Test
    fun `avanca e volta entre os tres passos`() = runTest(dispatcher) {
        val deps = dependencies()
        val viewModel = OnboardingViewModel(deps.users, deps.auth)

        assertEquals(0, viewModel.uiState.value.step)
        viewModel.onNext()
        assertEquals(1, viewModel.uiState.value.step)
        viewModel.onNext()
        assertEquals(2, viewModel.uiState.value.step)
        viewModel.onBack()
        assertEquals(1, viewModel.uiState.value.step)
        viewModel.onBack()
        assertEquals(0, viewModel.uiState.value.step)
        viewModel.onBack()
        assertEquals(0, viewModel.uiState.value.step)
    }

    @Test
    fun `pular limpa a escolha da etapa e avanca`() = runTest(dispatcher) {
        val deps = dependencies()
        val viewModel = OnboardingViewModel(deps.users, deps.auth)

        viewModel.onLifeStageSelect(LifeStage.STUDENT)
        viewModel.onSkipStep()
        assertEquals(1, viewModel.uiState.value.step)
        assertNull(viewModel.uiState.value.lifeStage)

        viewModel.onLookingForToggle(LookingFor.NEW_FRIENDS)
        viewModel.onSkipStep()
        assertEquals(2, viewModel.uiState.value.step)
        assertTrue(viewModel.uiState.value.lookingFor.isEmpty())
    }

    @Test
    fun `o que procura aceita no maximo tres escolhas`() = runTest(dispatcher) {
        val deps = dependencies()
        val viewModel = OnboardingViewModel(deps.users, deps.auth)

        val options = LookingFor.entries.take(LookingForRules.MAX_CHOICES + 1)
        options.forEach(viewModel::onLookingForToggle)

        assertEquals(
            LookingForRules.MAX_CHOICES,
            viewModel.uiState.value.lookingFor.size,
        )
        assertFalse(options.last() in viewModel.uiState.value.lookingFor)
    }

    @Test
    fun `concluir salva o perfil social e sinaliza fim`() = runTest(dispatcher) {
        val deps = dependencies()
        val viewModel = OnboardingViewModel(deps.users, deps.auth)

        viewModel.onLifeStageSelect(LifeStage.RECENT_GRADUATE)
        viewModel.onLookingForToggle(LookingFor.NEW_FRIENDS)
        viewModel.onLookingForToggle(LookingFor.NETWORKING)
        viewModel.onNext()
        viewModel.onNext()
        viewModel.onSocialVibeSelect(SocialVibe.BIG_CROWD)
        viewModel.onNext()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isDone)
        assertNull(state.error)
        val user = deps.db.users.value.first { it.id == "user-1" }
        assertEquals(LifeStage.RECENT_GRADUATE, user.lifeStage)
        assertEquals(
            setOf(LookingFor.NEW_FRIENDS, LookingFor.NETWORKING),
            user.lookingFor,
        )
        assertEquals(SocialVibe.BIG_CROWD, user.socialVibe)
    }

    @Test
    fun `pular a ultima etapa conclui sem a vibe`() = runTest(dispatcher) {
        val deps = dependencies()
        val viewModel = OnboardingViewModel(deps.users, deps.auth)

        viewModel.onLifeStageSelect(LifeStage.STUDENT)
        viewModel.onNext()
        viewModel.onLookingForToggle(LookingFor.NEW_FRIENDS)
        viewModel.onNext()
        viewModel.onSocialVibeSelect(SocialVibe.SMALL_GROUPS)
        viewModel.onSkipStep()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isDone)
        val user = deps.db.users.value.first { it.id == "user-1" }
        assertEquals(LifeStage.STUDENT, user.lifeStage)
        assertEquals(setOf(LookingFor.NEW_FRIENDS), user.lookingFor)
        assertNull(user.socialVibe)
    }

    @Test
    fun `sem usuario autenticado nao conclui`() = runTest(dispatcher) {
        val deps = dependencies(signedIn = false)
        val viewModel = OnboardingViewModel(deps.users, deps.auth)

        viewModel.onNext()
        viewModel.onNext()
        viewModel.onNext()
        advanceUntilIdle()

        assertEquals(AppError.UNAUTHENTICATED, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isDone)
    }
}
