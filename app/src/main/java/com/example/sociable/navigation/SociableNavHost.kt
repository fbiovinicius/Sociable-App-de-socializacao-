package com.example.sociable.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.sociable.R
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
import com.example.sociable.ui.auth.InterestsScreen
import com.example.sociable.ui.auth.LoginScreen
import com.example.sociable.ui.auth.RegisterScreen
import com.example.sociable.ui.auth.WelcomeScreen
import com.example.sociable.ui.chat.ChatScreen
import com.example.sociable.ui.events.CreateEventScreen
import com.example.sociable.ui.events.EventDetailScreen
import com.example.sociable.ui.events.EventListScreen
import com.example.sociable.ui.map.MapScreen
import com.example.sociable.ui.onboarding.OnboardingScreen
import com.example.sociable.ui.profile.ProfileScreen
import com.example.sociable.ui.review.ReviewScreen
import com.example.sociable.ui.secret.SecretScreen
import com.example.sociable.ui.theme.SociableTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.compose.ui.unit.sp

private data class Tab(val route: String, val label: String, val icon: Int)

private val tabs = listOf(
    Tab(Routes.EVENTS, "Descobrir", R.drawable.ic_pin_show),
    Tab(Routes.MAP, "Mapa", R.drawable.ic_pin_party),
    Tab(Routes.SECRET, "Chat da Cidade", R.drawable.ic_pin_secret),
    Tab(Routes.PROFILE, "Perfil", R.drawable.ic_pin_people),
)

@Composable
fun SociableNavHost(
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showTabs = tabs.any { it.route == currentRoute }

    Scaffold(
        containerColor = SociableTheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showTabs) {
                NavigationBar(containerColor = SociableTheme.background) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(Routes.EVENTS) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Image(
                                    painter = painterResource(tab.icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp),
                                )
                            },
                            label = { Text(tab.label, maxLines = 1, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = SociableTheme.chip,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.WELCOME,
            modifier = Modifier.padding(innerPadding),
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
                    onCreateAccount = {
                        navController.navigate(Routes.REGISTER) {
                            launchSingleTop = true
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
                        navController.navigate(Routes.ONBOARDING) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    },
                    onLoginClick = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.WELCOME)
                            launchSingleTop = true
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.ONBOARDING) {
                val viewModel: OnboardingViewModel = koinViewModel()
                OnboardingScreen(
                    viewModel = viewModel,
                    onFinished = {
                        navController.navigate(Routes.INTERESTS) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }

            composable(Routes.INTERESTS) {
                val viewModel: ProfileViewModel = koinViewModel()
                val goToFeed = {
                    navController.navigate(Routes.EVENTS) {
                        popUpTo(Routes.INTERESTS) { inclusive = true }
                    }
                }
                InterestsScreen(
                    viewModel = viewModel,
                    onDone = goToFeed,
                    onSkip = goToFeed,
                )
            }

            composable(Routes.EVENTS) {
                val viewModel: EventListViewModel = koinViewModel()
                EventListScreen(
                    viewModel = viewModel,
                    onEventClick = { eventId ->
                        navController.navigate(Routes.eventDetail(eventId))
                    },
                    onCreateClick = { navController.navigate(Routes.createEvent()) },
                )
            }

            composable(Routes.SECRET) {
                val viewModel: EventListViewModel = koinViewModel()
                SecretScreen(
                    viewModel = viewModel,
                    onEventClick = { eventId ->
                        navController.navigate(Routes.eventDetail(eventId))
                    },
                    onCreateClick = { navController.navigate(Routes.createEvent(secret = true)) },
                )
            }

            composable(
                route = Routes.CREATE_EVENT,
                arguments = listOf(
                    navArgument("secret") {
                        type = NavType.BoolType
                        defaultValue = false
                    },
                ),
            ) { entry ->
                val startAsSecret = entry.arguments?.getBoolean("secret") ?: false
                val viewModel: CreateEventViewModel = koinViewModel()
                CreateEventScreen(
                    viewModel = viewModel,
                    startAsSecret = startAsSecret,
                    onCreated = { eventId ->
                        navController.navigate(Routes.eventDetail(eventId)) {
                            popUpTo(entry.destination.id) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Routes.EDIT_EVENT,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
            ) { entry ->
                val eventId = entry.arguments?.getString("eventId").orEmpty()
                val viewModel: CreateEventViewModel = koinViewModel()
                CreateEventScreen(
                    viewModel = viewModel,
                    editEventId = eventId,
                    onCreated = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Routes.EVENT_DETAIL,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "sociable://event/{eventId}"
                    },
                    navDeepLink {
                        uriPattern = "https://sociable.app/event/{eventId}"
                    },
                ),
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
                    onEditClick = { navController.navigate(Routes.editEvent(eventId)) },
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
                    onEventClick = { eventId ->
                        navController.navigate(Routes.eventDetail(eventId))
                    },
                )
            }
        }
    }
}
