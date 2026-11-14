package com.example.sociable.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sociable.feature.auth.LoginViewModel
import com.example.sociable.feature.auth.RegisterViewModel
import com.example.sociable.feature.chat.ChatViewModel
import com.example.sociable.feature.events.CreateEventViewModel
import com.example.sociable.feature.events.EventDetailViewModel
import com.example.sociable.feature.events.EventListViewModel
import com.example.sociable.feature.map.MapViewModel
import com.example.sociable.feature.profile.ProfileViewModel
import com.example.sociable.feature.review.ReviewViewModel
import com.example.sociable.ui.auth.LoginScreen
import com.example.sociable.ui.auth.RegisterScreen
import com.example.sociable.ui.auth.WelcomeScreen
import com.example.sociable.ui.chat.ChatScreen
import com.example.sociable.ui.events.CreateEventScreen
import com.example.sociable.ui.events.EventDetailScreen
import com.example.sociable.ui.events.EventListScreen
import com.example.sociable.ui.map.MapScreen
import com.example.sociable.ui.profile.ProfileScreen
import com.example.sociable.ui.review.ReviewScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SociableNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME,
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onLogin = { navController.navigate(Routes.LOGIN) },
                onCreateAccount = { navController.navigate(Routes.REGISTER) },
            )
        }

        composable(Routes.LOGIN) {
            val viewModel: LoginViewModel = koinViewModel()
            LoginScreen(
                viewModel = viewModel,
                onLoggedIn = {
                    navController.navigate(Routes.EVENTS) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.REGISTER) {
            val viewModel: RegisterViewModel = koinViewModel()
            RegisterScreen(
                viewModel = viewModel,
                onRegistered = {
                    navController.navigate(Routes.EVENTS) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.EVENTS) {
            val viewModel: EventListViewModel = koinViewModel()
            EventListScreen(
                viewModel = viewModel,
                onEventClick = { eventId ->
                    navController.navigate(Routes.eventDetail(eventId))
                },
                onCreateClick = { navController.navigate(Routes.CREATE_EVENT) },
                onProfileClick = { navController.navigate(Routes.PROFILE) },
                onMapClick = { navController.navigate(Routes.MAP) },
            )
        }

        composable(Routes.CREATE_EVENT) {
            val viewModel: CreateEventViewModel = koinViewModel()
            CreateEventScreen(
                viewModel = viewModel,
                onCreated = { eventId ->
                    navController.navigate(Routes.eventDetail(eventId)) {
                        popUpTo(Routes.CREATE_EVENT) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.EVENT_DETAIL,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
        ) { entry ->
            val eventId = entry.arguments?.getString("eventId").orEmpty()
            val viewModel: EventDetailViewModel = koinViewModel(
                parameters = { parametersOf(eventId) },
            )
            EventDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onChatClick = { navController.navigate(Routes.chat(eventId)) },
                onReviewsClick = { navController.navigate(Routes.reviews(eventId)) },
            )
        }

        composable(
            route = Routes.CHAT,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
        ) { entry ->
            val eventId = entry.arguments?.getString("eventId").orEmpty()
            val viewModel: ChatViewModel = koinViewModel(
                parameters = { parametersOf(eventId) },
            )
            ChatScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.REVIEWS,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
        ) { entry ->
            val eventId = entry.arguments?.getString("eventId").orEmpty()
            val viewModel: ReviewViewModel = koinViewModel(
                parameters = { parametersOf(eventId) },
            )
            ReviewScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.PROFILE) {
            val viewModel: ProfileViewModel = koinViewModel()
            ProfileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSignedOut = {
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(Routes.EVENTS) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.MAP) {
            val viewModel: MapViewModel = koinViewModel()
            MapScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEventClick = { eventId ->
                    navController.navigate(Routes.eventDetail(eventId))
                },
            )
        }
    }
}
