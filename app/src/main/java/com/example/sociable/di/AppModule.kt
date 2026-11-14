package com.example.sociable.di

import com.example.sociable.core.time.SystemTimeProvider
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.data.fake.FakeAuthRepository
import com.example.sociable.data.fake.FakeChatRepository
import com.example.sociable.data.fake.FakeDatabase
import com.example.sociable.data.fake.FakeEventRepository
import com.example.sociable.data.fake.FakeReviewRepository
import com.example.sociable.data.fake.FakeUserRepository
import com.example.sociable.data.repository.AuthRepository
import com.example.sociable.data.repository.ChatRepository
import com.example.sociable.data.repository.EventRepository
import com.example.sociable.data.repository.ReviewRepository
import com.example.sociable.data.repository.UserRepository
import com.example.sociable.feature.auth.LoginViewModel
import com.example.sociable.feature.auth.RegisterViewModel
import com.example.sociable.feature.chat.ChatViewModel
import com.example.sociable.feature.events.CreateEventViewModel
import com.example.sociable.feature.events.EventDetailViewModel
import com.example.sociable.feature.events.EventListViewModel
import com.example.sociable.feature.map.MapViewModel
import com.example.sociable.feature.profile.ProfileViewModel
import com.example.sociable.feature.review.ReviewViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Grafo de dependências.
 *
 * >>> PONTO DE TROCA PARA O BACKEND <<<
 * Quando o parceiro de backend implementar os repositórios com Firebase,
 * basta trocar as linhas marcadas com FAKE pelas implementações reais.
 * Nenhum ViewModel ou tela precisa mudar.
 */
val appModule = module {

    single<TimeProvider> { SystemTimeProvider }

    // FAKE -> trocar por FirestoreDatabase quando o backend existir
    single { FakeDatabase(get()) }

    // FAKE -> trocar por FirebaseAuthRepository
    single<AuthRepository> { FakeAuthRepository(get(), get()) }

    // FAKE -> trocar por FirestoreUserRepository
    single<UserRepository> { FakeUserRepository(get()) }

    // FAKE -> trocar por FirestoreEventRepository
    single<EventRepository> { FakeEventRepository(get(), get()) }

    // FAKE -> trocar por FirestoreChatRepository
    single<ChatRepository> { FakeChatRepository(get(), get()) }

    // FAKE -> trocar por FirestoreReviewRepository
    single<ReviewRepository> { FakeReviewRepository(get(), get()) }

    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::EventListViewModel)
    viewModelOf(::CreateEventViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::MapViewModel)

    viewModel<ChatViewModel> { params ->
        ChatViewModel(
            eventId = params.get(),
            chatRepository = get(),
            authRepository = get(),
        )
    }

    viewModel<ReviewViewModel> { params ->
        ReviewViewModel(
            eventId = params.get(),
            reviewRepository = get(),
            authRepository = get(),
        )
    }

    viewModel<EventDetailViewModel> { params ->
        EventDetailViewModel(
            eventId = params.get(),
            eventRepository = get(),
            reviewRepository = get(),
            authRepository = get(),
        )
    }
}
