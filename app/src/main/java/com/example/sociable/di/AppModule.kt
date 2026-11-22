package com.example.sociable.di

import com.example.sociable.core.notifications.ReminderScheduler
import com.example.sociable.core.time.SystemTimeProvider
import org.koin.android.ext.koin.androidContext
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.data.repository.AuthRepository
import com.example.sociable.data.repository.ChatRepository
import com.example.sociable.data.repository.EventRepository
import com.example.sociable.data.repository.ReportRepository
import com.example.sociable.data.repository.ReviewRepository
import com.example.sociable.data.repository.UserRepository
import com.example.sociable.data.firebase.FirebaseAuthRepository
import com.example.sociable.data.firebase.FirestoreChatRepository
import com.example.sociable.data.firebase.FirestoreEventRepository
import com.example.sociable.data.firebase.FirestoreReportRepository
import com.example.sociable.data.firebase.FirestoreReviewRepository
import com.example.sociable.data.firebase.FirestoreUserRepository
import com.example.sociable.data.notifications.WorkManagerReminderScheduler
import com.example.sociable.data.places.AddressSearch
import com.example.sociable.data.places.GooglePlacesAddressSearch
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
 * Auth, perfil, eventos, presença, chat, avaliações e denúncias usam Firebase.
 * Os repositórios fake continuam em data/fake apenas para os testes.
 */
val appModule = module {

    single<TimeProvider> { SystemTimeProvider }

    // Implementações reais: autenticação e perfil persistidos no Firebase.
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single<AuthRepository> { FirebaseAuthRepository(get(), get(), get()) }
    single<UserRepository> { FirestoreUserRepository(get(), get()) }

    // Eventos e presença já persistidos no Cloud Firestore.
    single<EventRepository> { FirestoreEventRepository(get(), get(), get()) }

    // Chat e avaliações persistidos no Cloud Firestore.
    single<ChatRepository> { FirestoreChatRepository(get(), get()) }
    single<ReviewRepository> { FirestoreReviewRepository(get(), get()) }

    // Denúncias de eventos (coleção `reports`; leitura bloqueada no cliente).
    single<ReportRepository> { FirestoreReportRepository(get()) }

    // Sugestões de endereço ao digitar (Google Places).
    single<AddressSearch> { GooglePlacesAddressSearch(androidContext()) }

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
