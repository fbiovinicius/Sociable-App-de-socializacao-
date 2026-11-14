package com.example.sociable.data.fake

import com.example.sociable.core.time.TimeProvider
import com.example.sociable.data.model.Event
import com.example.sociable.data.model.EventCategory
import com.example.sociable.data.model.Interest
import com.example.sociable.data.model.Message
import com.example.sociable.data.model.Review
import com.example.sociable.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.temporal.ChronoUnit

/**
 * "Banco de dados" em memória usado enquanto o Firestore não existe.
 *
 * Todos os repositórios fake compartilham esta instância (singleton no Koin),
 * então criar um evento, confirmar presença ou mandar mensagem reflete nas
 * outras telas — exatamente como acontecerá com o Firestore.
 */
class FakeDatabase(timeProvider: TimeProvider) {

    private val now = timeProvider.now()

    /** email normalizado -> senha. Só existe no fake; no Firebase fica no Auth. */
    val credentials: MutableMap<String, String> = mutableMapOf(
        "demo@sociable.app" to "123456",
        "bruno@sociable.app" to "123456",
        "carla@sociable.app" to "123456",
    )

    val users = MutableStateFlow(
        listOf(
            User(
                id = "user-1",
                name = "Ana Souza",
                email = "demo@sociable.app",
                bio = "Amo música, café e rolês no Centro.",
                interests = setOf(Interest.MUSIC, Interest.FOOD, Interest.TECH),
                createdAt = now.minus(30, ChronoUnit.DAYS),
            ),
            User(
                id = "user-2",
                name = "Bruno Lima",
                email = "bruno@sociable.app",
                bio = "Sempre procurando um show novo.",
                interests = setOf(Interest.MUSIC, Interest.PARTY),
                createdAt = now.minus(20, ChronoUnit.DAYS),
            ),
            User(
                id = "user-3",
                name = "Carla Reis",
                email = "carla@sociable.app",
                bio = "Gastronomia e eventos ao ar livre.",
                interests = setOf(Interest.FOOD, Interest.OUTDOORS),
                createdAt = now.minus(10, ChronoUnit.DAYS),
            ),
        )
    )

    val events = MutableStateFlow(
        listOf(
            Event(
                id = "event-1",
                title = "Rolê no Centro",
                description = "Música ao vivo, petiscos e gente nova. Vista seu look favorito!",
                category = EventCategory.PARTY,
                venueName = "Bar do Zé",
                address = "Rua 10 de Julho, 123 - Centro",
                latitude = -3.1341,
                longitude = -60.0234,
                startAt = now.minus(1, ChronoUnit.HOURS),
                endAt = now.plus(3, ChronoUnit.HOURS),
                isFree = false,
                price = 20.0,
                capacity = 40,
                organizerId = "user-2",
                organizerName = "Bruno Lima",
                goingCount = 2,
                createdAt = now.minus(5, ChronoUnit.DAYS),
            ),
            Event(
                id = "event-2",
                title = "Festival de Tecnologia",
                description = "Palestras, stands e networking sobre Android, IA e startups.",
                category = EventCategory.TECH,
                venueName = "Centro de Convenções",
                address = "Av. Rodrigo Otávio, 3555 - Japiim",
                latitude = -3.1065,
                longitude = -59.9866,
                startAt = now.plus(1, ChronoUnit.DAYS),
                endAt = now.plus(1, ChronoUnit.DAYS).plus(8, ChronoUnit.HOURS),
                isFree = true,
                capacity = 100,
                organizerId = "user-1",
                organizerName = "Ana Souza",
                goingCount = 1,
                createdAt = now.minus(4, ChronoUnit.DAYS),
            ),
            Event(
                id = "event-3",
                title = "Feira Gastronômica",
                description = "Comidas regionais, food trucks e apresentações culturais.",
                category = EventCategory.FOOD,
                venueName = "Praça da Saudade",
                address = "Av. Sete de Setembro - Centro",
                latitude = -3.1320,
                longitude = -60.0170,
                startAt = now.plus(2, ChronoUnit.DAYS),
                endAt = now.plus(2, ChronoUnit.DAYS).plus(6, ChronoUnit.HOURS),
                isFree = true,
                capacity = null,
                organizerId = "user-3",
                organizerName = "Carla Reis",
                goingCount = 1,
                createdAt = now.minus(3, ChronoUnit.DAYS),
            ),
            Event(
                id = "event-4",
                title = "Corrida do Parque",
                description = "Corrida de 5 km no Parque do Mindu, com alongamento e hidratação.",
                category = EventCategory.SPORTS,
                venueName = "Parque do Mindu",
                address = "Rua José Romão - Parque 10",
                latitude = -3.0950,
                longitude = -60.0122,
                startAt = now.plus(3, ChronoUnit.DAYS),
                endAt = now.plus(3, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS),
                isFree = true,
                capacity = 80,
                organizerId = "user-2",
                organizerName = "Bruno Lima",
                goingCount = 0,
                createdAt = now.minus(2, ChronoUnit.DAYS),
            ),
            Event(
                id = "event-5",
                title = "Noite de Jazz",
                description = "Quarteto amazonense tocando standards e MPB instrumental.",
                category = EventCategory.MUSIC,
                venueName = "Teatro Amazonas",
                address = "Praça São Sebastião, s/n - Centro",
                latitude = -3.1302,
                longitude = -60.0236,
                startAt = now.plus(4, ChronoUnit.DAYS),
                endAt = now.plus(4, ChronoUnit.DAYS).plus(3, ChronoUnit.HOURS),
                isFree = false,
                price = 50.0,
                capacity = 200,
                organizerId = "user-1",
                organizerName = "Ana Souza",
                goingCount = 0,
                createdAt = now.minus(1, ChronoUnit.DAYS),
            ),
            Event(
                id = "event-6",
                title = "Luau na beira do rio",
                description = "Violão, fogueira e gente nova. O endereço chega por mensagem depois que o convite é aceito.",
                category = EventCategory.MUSIC,
                venueName = "Praia da Ponta Negra",
                address = "Av. Coronel Teixeira, s/n - Ponta Negra",
                latitude = -3.1000,
                longitude = -60.0800,
                startAt = now.plus(6, ChronoUnit.HOURS),
                endAt = now.plus(10, ChronoUnit.HOURS),
                isFree = false,
                price = 20.0,
                capacity = 8,
                organizerId = "user-3",
                organizerName = "Carla Reis",
                goingCount = 5,
                createdAt = now.minus(12, ChronoUnit.HOURS),
                isSecret = true,
            ),
            Event(
                id = "event-7",
                title = "Sarau no quintal",
                description = "Poesia, violão e petiscos. Cada pessoa leva um texto ou uma música para dividir.",
                category = EventCategory.ART,
                venueName = "Casa da Carla",
                address = "Rua 24 de Maio, 120 - Centro",
                latitude = -3.1280,
                longitude = -60.0050,
                startAt = now.plus(2, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS),
                endAt = now.plus(2, ChronoUnit.DAYS).plus(7, ChronoUnit.HOURS),
                isFree = true,
                capacity = 15,
                organizerId = "user-2",
                organizerName = "Bruno Lima",
                goingCount = 9,
                createdAt = now.minus(2, ChronoUnit.DAYS),
                isSecret = true,
            ),
        )
    )

    /** eventId -> ids dos usuários que confirmaram presença. */
    val rsvps = MutableStateFlow(
        mapOf(
            "event-1" to setOf("user-2", "user-3"),
            "event-2" to setOf("user-1"),
            "event-3" to setOf("user-1"),
        )
    )

    val messages = MutableStateFlow(
        mapOf(
            "event-1" to listOf(
                Message(
                    id = "message-1",
                    eventId = "event-1",
                    authorId = "user-2",
                    authorName = "Bruno Lima",
                    text = "Alguém já chegou no bar?",
                    sentAt = now.minus(40, ChronoUnit.MINUTES),
                ),
                Message(
                    id = "message-2",
                    eventId = "event-1",
                    authorId = "user-3",
                    authorName = "Carla Reis",
                    text = "Estou chegando em 15 min!",
                    sentAt = now.minus(25, ChronoUnit.MINUTES),
                ),
            )
        )
    )

    val reviews = MutableStateFlow(
        mapOf(
            "event-1" to listOf(
                Review(
                    id = "event-1_user-2",
                    eventId = "event-1",
                    authorId = "user-2",
                    authorName = "Bruno Lima",
                    rating = 4,
                    comment = "Ambiente muito bom, só demorou um pouco o atendimento.",
                    createdAt = now.minus(2, ChronoUnit.DAYS),
                ),
            )
        )
    )
}
