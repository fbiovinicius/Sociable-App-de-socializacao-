package com.example.sociable.feature.profile

import com.example.sociable.core.time.TimeProvider
import com.example.sociable.core.AppError
import com.example.sociable.data.fake.FakeAuthRepository
import com.example.sociable.data.fake.FakeDatabase
import com.example.sociable.data.fake.FakePhotoUploader
import com.example.sociable.data.fake.FakeUserRepository
import com.example.sociable.data.model.LifeStage
import com.example.sociable.data.model.LookingFor
import com.example.sociable.data.model.LookingForRules
import com.example.sociable.data.model.SocialVibe
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class ProfileViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val now = Instant.parse("2026-05-10T16:00:00Z")
    private val timeProvider = TimeProvider { now }

    private data class Dependencies(
        val db: FakeDatabase,
        val auth: FakeAuthRepository,
        val users: FakeUserRepository,
        val photos: FakePhotoUploader = FakePhotoUploader(),
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
        val viewModel = ProfileViewModel(deps.users, deps.auth, deps.photos)
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
        val viewModel = ProfileViewModel(deps.users, deps.auth, deps.photos)
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
        val viewModel = ProfileViewModel(deps.users, deps.auth, deps.photos)
        advanceUntilIdle()

        viewModel.onLifeStageSelect(LifeStage.STUDENT)
        viewModel.onLifeStageSelect(LifeStage.STUDENT)
        viewModel.onSocialVibeSelect(SocialVibe.BIG_CROWD)
        viewModel.onSocialVibeSelect(SocialVibe.BIG_CROWD)

        assertEquals(null, viewModel.uiState.value.lifeStage)
        assertEquals(null, viewModel.uiState.value.socialVibe)
    }

    @Test
    fun `salvar grava o instagram sem arroba e o anel acompanha o preenchimento`() = runTest(dispatcher) {
        val deps = dependencies()
        val viewModel = ProfileViewModel(deps.users, deps.auth, deps.photos)
        advanceUntilIdle()
        val before = viewModel.uiState.value.completion

        viewModel.onInstagramChange("@ana.souza")
        viewModel.onSaveClick()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)
        assertEquals("ana.souza", deps.users.observeUser("user-1").first()!!.instagram)
        assertTrue(viewModel.uiState.value.completion > before)
    }

    @Test
    fun `instagram invalido bloqueia o salvamento`() = runTest(dispatcher) {
        val deps = dependencies()
        val viewModel = ProfileViewModel(deps.users, deps.auth, deps.photos)
        advanceUntilIdle()

        viewModel.onInstagramChange("https://instagram.com/ana")
        viewModel.onSaveClick()
        advanceUntilIdle()

        assertEquals(
            com.example.sociable.core.validation.ValidationError.INVALID_HANDLE,
            viewModel.uiState.value.instagramError,
        )
        assertFalse(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `foto escolhida na galeria e enviada e so grava ao salvar`() = runTest(dispatcher) {
        val deps = dependencies()
        val viewModel = ProfileViewModel(deps.users, deps.auth, deps.photos)
        advanceUntilIdle()

        viewModel.onPhotoPicked("content://media/1")
        advanceUntilIdle()

        assertEquals("https://fake.test/profile/1.jpg", viewModel.uiState.value.photoUrl)
        assertFalse(viewModel.uiState.value.isUploadingPhoto)
        assertNull(deps.users.observeUser("user-1").first()!!.photoUrl)

        viewModel.onSaveClick()
        advanceUntilIdle()
        assertEquals("https://fake.test/profile/1.jpg", deps.users.observeUser("user-1").first()!!.photoUrl)
    }

    @Test
    fun `falha no envio mostra erro e mantem a foto anterior`() = runTest(dispatcher) {
        val deps = dependencies()
        deps.photos.failWith = AppError.NETWORK
        val viewModel = ProfileViewModel(deps.users, deps.auth, deps.photos)
        advanceUntilIdle()

        viewModel.onPhotoPicked("content://media/1")
        advanceUntilIdle()

        assertEquals(AppError.NETWORK, viewModel.uiState.value.error)
        assertEquals("", viewModel.uiState.value.photoUrl)
    }
}
