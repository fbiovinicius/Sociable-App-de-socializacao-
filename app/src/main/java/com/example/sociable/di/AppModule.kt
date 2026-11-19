package com.example.sociable.di

import com.example.sociable.core.notifications.ReminderScheduler
import com.example.sociable.core.time.SystemTimeProvider
import org.koin.android.ext.koin.androidContext
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.data.fake.FakeChatRepository
import com.example.sociable.data.fake.FakeDatabase
import com.example.sociable.data.fake.FakeReviewRepository
import com.example.sociable.data.repository.AuthRepository
import com.example.sociable.data.repository.ChatRepository
import com.example.sociable.data.repository.EventRepository
import com.example.sociable.data.repository.ReviewRepository
import com.example.sociable.data.repository.UserRepository
import com.example.sociable.data.firebase.FirebaseAuthRepository
import com.example.sociable.data.firebase.FirestoreEventRepository
import com.example.sociable.data.firebase.FirestoreUserRepository
import com.example.sociable.data.notifications.WorkManagerReminderScheduler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.sociable.feature.auth.LoginViewModel
import com.example.sociable.feature.auth.RegisterViewModel
import com.example.sociable.feature.chat.ChatViewModel
import com.example.sociable.feature.events.CreateEventViewModel
import com.example.sociable.feature.events.EventDetailViewModel
import com.example.sociable.feature.events.EventListViewModel
import com.example.sociable.feature.map.MapViewModel
import com.example.sociable.feature.onboarding.OnboardingViewModel
import com.example.sociable.feature.profile.ProfileViewModel
import com.example.sociable.feature.review.ReviewViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Grafo de dependências.
 *
 * >>> PONTO DE TROCA PARA O BACKEND <<<
 * Auth, perfil, eventos e presença já usam Firebase. Chat e avaliações
 * permanecem em memória até suas implementações Firestore serem concluídas.
 * Nenhum ViewModel ou tela precisa mudar.
 */
val appModule = module {

    single<TimeProvider> { SystemTimeProvider }

    // Banco em memória mantido temporariamente para chat e avaliações.
    single { FakeDatabase(get()) }

    // Implementações reais: autenticação e perfil persistidos no Firebase.
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single<AuthRepository> { FirebaseAuthRepository(get(), get(), get()) }
    single<UserRepository> { FirestoreUserRepository(get(), get()) }

    // Eventos e presença já persistidos no Cloud Firestore.
    single<EventRepository> { FirestoreEventRepository(get(), get(), get()) }

    // FAKE -> trocar por FirestoreChatRepository
    single<ChatRepository> { FakeChatRepository(get(), get()) }

    // FAKE -> trocar por FirestoreReviewRepository
    single<ReviewRepository> { FakeReviewRepository(get(), get()) }

    // Lembretes locais (bloco F1).
    single<ReminderScheduler> { WorkManagerReminderScheduler(androidContext()) }

    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::EventListViewModel)
    viewModelOf(::CreateEventViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::MapViewModel)

    viewModel<ChatViewModel> { params ->
        ChatViewModel(
            eventId = params.get(),
            chatRepository = get(),
            eventRepository = get(),
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
            reminderScheduler = get(),
        )
    }
}
