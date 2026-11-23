package com.example.sociable.feature.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.toAppError
import com.example.sociable.core.validation.ValidationError
import com.example.sociable.core.validation.Validators
import com.example.sociable.data.model.Community
import com.example.sociable.data.model.Event
import com.example.sociable.data.model.EventStatus
import com.example.sociable.data.model.Message
import com.example.sociable.data.repository.AuthRepository
import com.example.sociable.data.repository.ChatRepository
import com.example.sociable.data.repository.CommunityRepository
import com.example.sociable.data.repository.EventRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

enum class MessagesTab(val label: String) {
    ALL("Todos"),
    EVENTS("Eventos"),
    COMMUNITIES("Comunidades"),
}

enum class ConversationKind { EVENT, COMMUNITY }

/** Uma linha da lista: chat de um evento ou comunidade. */
data class Conversation(
    val kind: ConversationKind,
    val id: String,
    val title: String,
    /** Última mensagem ("Ana: oi") ou, sem mensagens, uma frase de apoio. */
    val preview: String,
    val lastActivity: Instant?,
    val memberCount: Int = 0,
    /** Em comunidades: se a pessoa já entrou. Em eventos, sempre verdadeiro. */
    val isMember: Boolean = true,
    val isCancelled: Boolean = false,
)

data class MessagesUiState(
    val conversations: List<Conversation> = emptyList(),
    val tab: MessagesTab = MessagesTab.ALL,
    val query: String = "",
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val showCreateDialog: Boolean = false,
    val newName: String = "",
    val newDescription: String = "",
    val newNameError: ValidationError? = null,
    val newDescriptionError: ValidationError? = null,
    val isCreating: Boolean = false,
    val createError: AppError? = null,
    /** Id da comunidade em que a pessoa está entrando (desabilita o botão). */
    val joiningId: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class MessagesViewModel(
    private val eventRepository: EventRepository,
    private val chatRepository: ChatRepository,
    private val communityRepository: CommunityRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    private val tab = MutableStateFlow(MessagesTab.ALL)
    private val query = MutableStateFlow("")

    private val userId: String
        get() = authRepository.currentUser.value?.id.orEmpty()

    init {
        val events = eventConversations()
        val communities = communityConversations()
        viewModelScope.launch {
            combine(events, communities, tab, query) { eventList, communityList, currentTab, term ->
                val base = when (currentTab) {
                    MessagesTab.ALL -> eventList + communityList.filter { it.isMember }
                    MessagesTab.EVENTS -> eventList
                    MessagesTab.COMMUNITIES -> communityList
                }
                base
                    .filter { term.isBlank() || it.title.contains(term.trim(), ignoreCase = true) }
                    .sortedWith(
                        compareByDescending<Conversation> { it.isMember }
                            .thenByDescending { it.lastActivity ?: Instant.EPOCH }
                            .thenByDescending { it.memberCount }
                            .thenBy { it.title }
                    )
            }.collect { list ->
                _uiState.update { it.copy(conversations = list, isLoading = false) }
            }
        }
    }

    private fun eventConversations(): Flow<List<Conversation>> =
        eventRepository.observeChatEvents(userId)
            .flatMapLatest { events ->
                events.map { event -> lastMessageOf(event) }.combineOrEmpty()
            }
            .catch { throwable ->
                _uiState.update { it.copy(error = throwable.toAppError()) }
                emit(emptyList())
            }

    private fun lastMessageOf(event: Event): Flow<Conversation> =
        chatRepository.observeLastMessage(event.id)
            .catch { emit(null) }
            .onStart { emit(null) }
            .map { message ->
                Conversation(
                    kind = ConversationKind.EVENT,
                    id = event.id,
                    title = event.title,
                    preview = message?.preview()
                        ?: if (event.status == EventStatus.CANCELLED) {
                            "Evento cancelado"
                        } else {
                            "Nenhuma mensagem ainda"
                        },
                    lastActivity = message?.sentAt,
                    memberCount = event.goingCount,
                    isCancelled = event.status == EventStatus.CANCELLED,
                )
            }

    private fun communityConversations(): Flow<List<Conversation>> =
        combine(
            communityRepository.observeCommunities(),
            communityRepository.observeMemberships(userId).catch { emit(emptySet()) },
        ) { communities, memberIds -> communities to memberIds }
            .flatMapLatest { (communities, memberIds) ->
                communities.map { community ->
                    if (community.id in memberIds) {
                        communityRepository.observeLastMessage(community.id)
                            .catch { emit(null) }
                            .onStart { emit(null) }
                            .map { community.toConversation(isMember = true, last = it) }
                    } else {
                        flowOf(community.toConversation(isMember = false, last = null))
                    }
                }.combineOrEmpty()
            }
            .catch { throwable ->
                _uiState.update { it.copy(error = throwable.toAppError()) }
                emit(emptyList())
            }

    private fun Community.toConversation(isMember: Boolean, last: Message?) = Conversation(
        kind = ConversationKind.COMMUNITY,
        id = id,
        title = name,
        preview = last?.preview() ?: description.ifBlank { "Comunidade de $ownerName" },
        lastActivity = last?.sentAt,
        memberCount = memberCount,
        isMember = isMember,
    )

    private fun Message.preview(): String = "$authorName: $text"

    private inline fun <reified T> List<Flow<T>>.combineOrEmpty(): Flow<List<T>> =
        if (isEmpty()) flowOf(emptyList()) else combine(this) { it.toList() }

    fun onTabChange(value: MessagesTab) {
        tab.value = value
        _uiState.update { it.copy(tab = value) }
    }

    fun onQueryChange(value: String) {
        query.value = value
        _uiState.update { it.copy(query = value) }
    }

    fun onJoinCommunity(communityId: String) {
        val user = authRepository.currentUser.value
        if (user == null) {
            _uiState.update { it.copy(error = AppError.UNAUTHENTICATED) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(joiningId = communityId, error = null) }
            val result = communityRepository.join(communityId, user.id, user.name)
            _uiState.update {
                it.copy(
                    joiningId = null,
                    error = (result as? AppResult.Failure)?.error,
                )
            }
        }
    }

    fun onCreateClick() {
        _uiState.update {
            it.copy(
                showCreateDialog = true,
                newName = "",
                newDescription = "",
                newNameError = null,
                newDescriptionError = null,
                createError = null,
            )
        }
    }

    fun onCreateDismiss() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun onNewNameChange(value: String) {
        _uiState.update { it.copy(newName = value, newNameError = null, createError = null) }
    }

    fun onNewDescriptionChange(value: String) {
        _uiState.update {
            it.copy(newDescription = value, newDescriptionError = null, createError = null)
        }
    }

    fun onCreateSubmit() {
        val state = _uiState.value
        if (state.isCreating) return
        val nameError = Validators.communityName(state.newName)
        val descriptionError = Validators.communityDescription(state.newDescription)
        if (nameError != null || descriptionError != null) {
            _uiState.update {
                it.copy(newNameError = nameError, newDescriptionError = descriptionError)
            }
            return
        }
        val user = authRepository.currentUser.value
        if (user == null) {
            _uiState.update { it.copy(createError = AppError.UNAUTHENTICATED) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, createError = null) }
            when (
                val result = communityRepository.createCommunity(
                    name = state.newName,
                    description = state.newDescription,
                    ownerId = user.id,
                    ownerName = user.name,
                )
            ) {
                is AppResult.Success -> {
                    tab.value = MessagesTab.COMMUNITIES
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            showCreateDialog = false,
                            tab = MessagesTab.COMMUNITIES,
                        )
                    }
                }

                is AppResult.Failure -> _uiState.update {
                    it.copy(isCreating = false, createError = result.error)
                }
            }
        }
    }
}
