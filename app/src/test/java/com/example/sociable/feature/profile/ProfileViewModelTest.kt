package com.example.sociable.feature.profile

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

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

    private suspend fun dependencies(): Dependencies {
        val db = FakeDatabase(timeProvider)
        val auth = FakeAuthRepository(db, timeProvider)
        auth.signIn("demo@sociable.app", "123456")
        return Dependencies(db, auth, FakeUserRepository(db))
    }

    @Test
    fun `salvar persiste perfil basico e respostas sociais`() = runTest(dispatcher) {
        val deps = dependencies()
        val viewModel = ProfileViewModel(deps.users, deps.auth)
        advanceUntilIdle()

        viewModel.onNameChange("Ana Maria")
        viewModel.onLifeStageSelect(LifeStage.NEW_IN_TOWN)
        viewModel.onLookingForToggle(LookingFor.NEW_FRIENDS)
        viewModel.onLookingForToggle(LookingFor.NEW_HOBBIES)
        viewModel.onSocialVibeSelect(SocialVibe.SMALL_GROUPS)
        viewModel.onSaveClick()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)
        val user = deps.db.users.value.first { it.id == "user-1" }
        assertEquals("Ana Maria", user.name)
        assertEquals(LifeStage.NEW_IN_TOWN, user.lifeStage)
        assertEquals(
            setOf(LookingFor.NEW_FRIENDS, LookingFor.NEW_HOBBIES),
            user.lookingFor,
        )
        assertEquals(SocialVibe.SMALL_GROUPS, user.socialVibe)
    }

    @Test
    fun `o que procura limita a tres escolhas no perfil`() = runTest(dispatcher) {
        val deps = dependencies()
        val viewModel = ProfileViewModel(deps.users, deps.auth)
        advanceUntilIdle()

        val options = LookingFor.entries.take(LookingForRules.MAX_CHOICES + 1)
        options.forEach(viewModel::onLookingForToggle)

        assertEquals(
            LookingForRules.MAX_CHOICES,
            viewModel.uiState.value.lookingFor.size,
        )
        assertFalse(options.last() in viewModel.uiState.value.lookingFor)
    }

    @Test
    fun `tocar de novo remove a escolha unica`() = runTest(dispatcher) {
        val deps = dependencies()
        val viewModel = ProfileViewModel(deps.users, deps.auth)
        advanceUntilIdle()

        viewModel.onLifeStageSelect(LifeStage.STUDENT)
        viewModel.onLifeStageSelect(LifeStage.STUDENT)
        viewModel.onSocialVibeSelect(SocialVibe.BIG_CROWD)
        viewModel.onSocialVibeSelect(SocialVibe.BIG_CROWD)

        assertEquals(null, viewModel.uiState.value.lifeStage)
        assertEquals(null, viewModel.uiState.value.socialVibe)
    }
}
