package com.example.sociable.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.sociable.R
import com.example.sociable.ui.theme.Cream
import com.example.sociable.ui.theme.Rose

enum class Screen { Welcome, EmailLogin, Signup, Interests, Map, Secret, Chat, Profile, EventDetail, CreateEvent }

private data class Tab(val screen: Screen, val label: String, val icon: Int)

/** Navegação simples por estado. Os colegas podem trocar por Navigation Compose depois. */
@Composable
fun SociableApp() {
    var screen by remember { mutableStateOf(Screen.Welcome) }
    var profile by remember { mutableStateOf(UserProfile()) }
    val events = remember { mutableStateListOf<EventItem>().apply { addAll(sampleEvents) } }
    val joined = remember { mutableStateListOf<Int>() }
    var openEventId by remember { mutableStateOf<Int?>(null) }
    var backTo by remember { mutableStateOf(Screen.Map) }
    var createAsSecret by remember { mutableStateOf(false) }

    val tabs = listOf(
        Tab(Screen.Map, "Mapa", R.drawable.ic_pin_party),
        Tab(Screen.Secret, "Segredinhos", R.drawable.ic_pin_secret),
        Tab(Screen.Chat, "Chats", R.drawable.ic_pin_people),
        Tab(Screen.Profile, "Perfil", R.drawable.ic_pin_show),
    )

    fun openEvent(e: EventItem, from: Screen) {
        openEventId = e.id
        backTo = from
        screen = Screen.EventDetail
    }

    fun toggleJoin(id: Int) {
        if (id in joined) joined.remove(id) else joined.add(id)
    }

    BackHandler(enabled = screen in listOf(Screen.EmailLogin, Screen.Signup, Screen.EventDetail, Screen.CreateEvent)) {
        screen = when (screen) {
            Screen.EmailLogin, Screen.Signup -> Screen.Welcome
            Screen.EventDetail -> backTo
            Screen.CreateEvent -> if (createAsSecret) Screen.Secret else Screen.Map
            else -> screen
        }
    }

    when (screen) {
        Screen.Welcome -> WelcomeScreen(
            onGoogle = { email ->
                profile = profile.copy(name = nameFromEmail(email))
                screen = Screen.Signup
            },
            onEmail = { screen = Screen.EmailLogin },
        )

        Screen.EmailLogin -> EmailLoginScreen(
            onBack = { screen = Screen.Welcome },
            onLogin = { email ->
                profile = profile.copy(name = nameFromEmail(email))
                screen = Screen.Signup
            },
            onCreateAccount = { screen = Screen.Signup },
        )

        Screen.Signup -> SignupScreen(
            initialName = profile.name,
            onNext = { name, age, gender, city ->
                profile = profile.copy(name = name, age = age, gender = gender, city = city)
                screen = Screen.Interests
            },
        )

        Screen.Interests -> InterestsScreen(
            onNext = { list ->
                profile = profile.copy(interests = list)
                screen = Screen.Map
            },
        )

        else -> Scaffold(
            containerColor = Cream,
            bottomBar = {
                if (screen in tabs.map { it.screen }) {
                    NavigationBar(containerColor = Cream) {
                        tabs.forEach { t ->
                            NavigationBarItem(
                                selected = screen == t.screen,
                                onClick = { screen = t.screen },
                                icon = { Image(painterResource(t.icon), null, Modifier.size(26.dp)) },
                                label = { Text(t.label) },
                                colors = NavigationBarItemDefaults.colors(indicatorColor = Rose),
                            )
                        }
                    }
                }
            },
        ) { inner ->
            val m = Modifier.padding(inner)
            when (screen) {
                Screen.Map -> MapScreen(events, m) { openEvent(it, Screen.Map) }

                Screen.Secret -> SecretScreen(
                    events = events,
                    joined = joined.toSet(),
                    onToggleJoin = { toggleJoin(it) },
                    onOpen = { openEvent(it, Screen.Secret) },
                    onCreate = { createAsSecret = true; screen = Screen.CreateEvent },
                    modifier = m,
                )

                Screen.Chat -> ChatScreen(m)

                Screen.Profile -> ProfileScreen(
                    profile = profile,
                    onLogout = {
                        profile = UserProfile()
                        joined.clear()
                        screen = Screen.Welcome
                    },
                    modifier = m,
                )

                Screen.EventDetail -> {
                    val ev = events.firstOrNull { it.id == openEventId }
                    if (ev != null) {
                        EventDetailScreen(
                            event = ev,
                            joined = ev.id in joined,
                            onToggleJoin = { toggleJoin(ev.id) },
                            onBack = { screen = backTo },
                            modifier = m,
                        )
                    }
                }

                Screen.CreateEvent -> CreateEventScreen(
                    startAsSecret = createAsSecret,
                    onBack = { screen = if (createAsSecret) Screen.Secret else Screen.Map },
                    onCreate = { d ->
                        val id = (events.maxOfOrNull { it.id } ?: 0) + 1
                        events.add(
                            EventItem(
                                id = id, title = d.title, category = d.category,
                                place = if (d.category == Category.Secret) "Local revelado 2 h antes" else d.place,
                                whenText = d.whenText, priceText = d.priceText, distanceKm = 0.5,
                                confirmed = 1, capacity = d.capacity, tags = emptySet(),
                                happeningNow = false, description = d.description,
                                // Posição provisória no mapa. TODO: escolher o local de verdade.
                                x = 0.30f + (id % 5) * 0.08f, y = 0.42f + (id % 4) * 0.07f,
                            ),
                        )
                        joined.add(id)
                        screen = if (d.category == Category.Secret) Screen.Secret else Screen.Map
                    },
                    modifier = m,
                )

                else -> Unit
            }
        }
    }
}