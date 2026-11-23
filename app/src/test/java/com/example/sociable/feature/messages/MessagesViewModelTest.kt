package com.example.sociable.feature.messages

import com.example.sociable.core.AppResult
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.core.validation.ValidationError
import com.example.sociable.core.validation.Validators
import com.example.sociable.data.fake.FakeAuthRepository
import com.example.sociable.data.fake.FakeChatRepository
import com.example.sociable.data.fake.FakeCommunityRepository
import com.example.sociable.data.fake.FakeDatabase
import com.example.sociable.data.fake.FakeEventRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MessagesViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val now = Instant.parse("2026-05-10T16:00:00Z")
    private val timeProvider = TimeProvider { now }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class Setup(
        val viewModel: MessagesViewModel,
        val communities: FakeCommunityRepository,
    )

    /** Entra como Ana (user-1): organiza dois eventos e confirmou presença em outros. */
    private suspend fun TestScope.setup(): Setup {
        val db = FakeDatabase(timeProvider)
        val auth = FakeAuthRepository(db, timeProvider)
        auth.signIn("demo@sociable.app", "123456")
        val communities = FakeCommunityRepository(timeProvider)
        val viewModel = MessagesViewModel(
            eventRepository = FakeEventRepository(db, timeProvider),
            chatRepository = FakeChatRepository(db, timeProvider),
            communityRepository = communities,
            authRepository = auth,
        )
        advanceUntilIdle()
        return Setup(viewModel, communities)
    }

    @Test
    fun `aba Eventos lista so os chats de eventos que a pessoa organiza ou vai`() = runTest(dispatcher) {
        val viewModel = setup().viewModel

        viewModel.onTabChange(MessagesTab.EVENTS)
        advanceUntilIdle()

        val titles = viewModel.uiState.value.conversations.map { it.title }
        assertTrue("Festival de Tecnologia" in titles) // organiza e confirmou
        assertTrue("Feira Gastronômica" in titles) // só confirmou
        assertFalse("Rolê no Centro" in titles) // não tem relação com a Ana
        assertTrue(viewModel.uiState.value.conversations.all { it.kind == ConversationKind.EVENT })
    }

    @Test
    fun `criar comunidade abre a aba Comunidades com a nova comunidade`() = runTest(dispatcher) {
        val viewModel = setup().viewModel

        viewModel.onCreateClick()
        viewModel.onNewNameChange("Corredores de Manaus")
        viewModel.onNewDescriptionChange("Treinos de fim de semana")
        viewModel.onCreateSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showCreateDialog)
        assertEquals(MessagesTab.COMMUNITIES, state.tab)
        val community = state.conversations.single()
        assertEquals("Corredores de Manaus", community.title)
        assertTrue(community.isMember)
        assertEquals(1, community.memberCount)
    }

    @Test
    fun `nome curto de comunidade mostra erro e nao cria`() = runTest(dispatcher) {
        val viewModel = setup().viewModel

        viewModel.onCreateClick()
        viewModel.onNewNameChange("ab")
        viewModel.onCreateSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(ValidationError.TOO_SHORT, state.newNameError)
        assertTrue(state.showCreateDialog)
    }

    @Test
    fun `entrar em comunidade de outra pessoa a torna membro`() = runTest(dispatcher) {
        val setup = setup()
        val created = setup.communities.createCommunity("Jazz", "", "user-2", "Bruno Lima")
        val id = (created as AppResult.Success).data.id
        setup.viewModel.onTabChange(MessagesTab.COMMUNITIES)
        advanceUntilIdle()
        assertFalse(setup.viewModel.uiState.value.conversations.single().isMember)

        setup.viewModel.onJoinCommunity(id)
        advanceUntilIdle()

        val row = setup.viewModel.uiState.value.conversations.single()
        assertTrue(row.isMember)
        assertEquals(2, row.memberCount)
        assertNull(setup.viewModel.uiState.value.error)
    }

    @Test
    fun `comunidades que a pessoa nao entrou ficam fora da aba Todos`() = runTest(dispatcher) {
        val setup = setup()
        setup.communities.createCommunity("Jazz", "", "user-2", "Bruno Lima")
        advanceUntilIdle()

        val all = setup.viewModel.uiState.value.conversations
        assertTrue(all.none { it.kind == ConversationKind.COMMUNITY })
    }

    @Test
    fun `busca filtra pelo titulo`() = runTest(dispatcher) {
        val viewModel = setup().viewModel

        viewModel.onQueryChange("festival")
        advanceUntilIdle()

        assertEquals(
            listOf("Festival de Tecnologia"),
            viewModel.uiState.value.conversations.map { it.title },
        )
    }

    @Test
    fun `dono nao sai da comunidade e quem nao entrou nao envia mensagem`() = runTest(dispatcher) {
        val communities = FakeCommunityRepository(timeProvider)
        val id = (communities.createCommunity("Jazz", "", "user-2", "Bruno") as AppResult.Success).data.id

        assertTrue(communities.leave(id, "user-2") is AppResult.Failure)
        assertTrue(communities.sendMessage(id, "user-1", "Ana", "oi") is AppResult.Failure)
        communities.join(id, "user-1", "Ana")
        assertTrue(communities.sendMessage(id, "user-1", "Ana", "oi") is AppResult.Success)
    }

    @Test
    fun `validacao do nome e da descricao da comunidade`() {
        assertEquals(ValidationError.REQUIRED, Validators.communityName("  "))
        assertEquals(ValidationError.TOO_SHORT, Validators.communityName("ab"))
        assertNull(Validators.communityName("Corrida"))
        assertEquals(ValidationError.TOO_LONG, Validators.communityName("a".repeat(61)))
        assertNull(Validators.communityDescription(""))
        assertEquals(ValidationError.TOO_LONG, Validators.communityDescription("a".repeat(201)))
    }
}
