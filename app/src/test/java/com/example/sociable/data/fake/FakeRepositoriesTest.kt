package com.example.sociable.data.fake

import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.data.model.EventCategory
import com.example.sociable.data.model.EventDraft
import com.example.sociable.data.model.EventFilter
import com.example.sociable.data.model.Interest
import com.example.sociable.data.model.LifeStage
import com.example.sociable.data.model.LookingFor
import com.example.sociable.data.model.ReportReason
import com.example.sociable.data.model.SocialVibe
import java.time.Instant
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeRepositoriesTest {

    private val now = Instant.parse("2026-05-10T16:00:00Z")
    private val timeProvider = TimeProvider { now }

    private fun database() = FakeDatabase(timeProvider)

    @Test
    fun `login com a conta demo funciona`() = runTest {
        val repository = FakeAuthRepository(database(), timeProvider)

        val result = repository.signIn("demo@sociable.app", "123456")

        assertTrue(result is AppResult.Success)
        assertEquals("Ana Souza", (result as AppResult.Success).data.name)
        assertNotNull(repository.currentUser.value)
    }

    @Test
    fun `login com senha errada falha`() = runTest {
        val repository = FakeAuthRepository(database(), timeProvider)

        val result = repository.signIn("demo@sociable.app", "senha-errada")

        assertEquals(
            AppError.INVALID_CREDENTIALS,
            (result as AppResult.Failure).error,
        )
    }

    @Test
    fun `cadastro recusa email repetido e aceita email novo`() = runTest {
        val db = database()
        val repository = FakeAuthRepository(db, timeProvider)

        val duplicated = repository.signUp("Outra Ana", "demo@sociable.app", "123456")
        assertEquals(AppError.EMAIL_ALREADY_IN_USE, (duplicated as AppResult.Failure).error)

        val created = repository.signUp("Nova Pessoa", "nova@sociable.app", "123456")
        assertTrue(created is AppResult.Success)
        assertEquals(4, db.users.value.size)
        assertNotNull(repository.currentUser.value)
    }

    @Test
    fun `confirmar e cancelar presenca atualiza o contador`() = runTest {
        val db = database()
        val repository = FakeEventRepository(db, timeProvider)

        val before = repository.observeRsvp("event-1", "user-1").first()
        assertFalse(before.isGoing)
        assertEquals(2, before.goingCount)

        val going = repository.setRsvp("event-1", "user-1", true) as AppResult.Success
        assertEquals(3, going.data.goingCount)
        assertTrue(going.data.isGoing)

        val afterGoing = repository.observeRsvp("event-1", "user-1").first()
        assertEquals(3, afterGoing.goingCount)
        assertTrue(afterGoing.isGoing)

        val cancelled = repository.setRsvp("event-1", "user-1", false) as AppResult.Success
        assertEquals(2, cancelled.data.goingCount)
        assertFalse(cancelled.data.isGoing)
    }

    @Test
    fun `evento lotado recusa nova presenca`() = runTest {
        val db = database()
        db.events.value = db.events.value.map {
            if (it.id == "event-1") it.copy(capacity = 2) else it
        }
        val repository = FakeEventRepository(db, timeProvider)

        val result = repository.setRsvp("event-1", "user-1", true)

        assertEquals(AppError.EVENT_FULL, (result as AppResult.Failure).error)
    }

    @Test
    fun `criar evento aplica trim e aparece no feed`() = runTest {
        val db = database()
        val repository = FakeEventRepository(db, timeProvider)
        val start = ZonedDateTime.of(2026, 5, 20, 19, 0, 0, 0, java.time.ZoneId.of("America/Manaus"))
            .toInstant()

        val result = repository.createEvent(
            EventDraft(
                title = "  Karaokê  ",
                description = "  Noite de karaokê com amigos  ",
                category = EventCategory.MUSIC,
                venueName = "  Studio A  ",
                address = "  Centro  ",
                latitude = -3.119,
                longitude = -60.021,
                startAt = start,
                endAt = start.plusSeconds(7200),
                isFree = false,
                price = 15.0,
                capacity = 30,
                imageUrl = " ",
                organizerId = "user-1",
                organizerName = "Ana Souza",
                isSecret = true,
            )
        )

        assertTrue(result is AppResult.Success)
        val event = (result as AppResult.Success).data
        assertEquals("Karaokê", event.title)
        assertEquals("Studio A", event.venueName)
        assertNull(event.imageUrl)
        assertTrue(event.isSecret)

        val feed = repository.observeEvents(EventFilter(query = "karaokê")).first()
        assertEquals(1, feed.size)
        assertEquals(event.id, feed.first().id)

        val secrets = repository.observeEvents(EventFilter(onlySecret = true)).first()
        assertTrue(secrets.any { it.id == event.id })
        assertTrue(secrets.all { it.isSecret })
    }

    private fun draftFrom(event: com.example.sociable.data.model.Event, title: String = event.title, capacity: Int? = event.capacity) =
        EventDraft(
            title = title,
            description = event.description,
            category = event.category,
            venueName = event.venueName,
            address = event.address,
            latitude = event.latitude,
            longitude = event.longitude,
            startAt = event.startAt,
            endAt = event.endAt,
            isFree = event.isFree,
            price = event.price,
            capacity = capacity,
            imageUrl = event.imageUrl,
            organizerId = event.organizerId,
            organizerName = event.organizerName,
        )

    @Test
    fun `organizador edita o evento e o contador de presencas e mantido`() = runTest {
        val db = database()
        val repository = FakeEventRepository(db, timeProvider)
        val original = db.events.value.first { it.id == "event-1" }

        val result = repository.updateEvent("event-1", draftFrom(original, title = "  Novo nome  "))

        val updated = (result as AppResult.Success).data
        assertEquals("Novo nome", updated.title)
        assertEquals(original.goingCount, updated.goingCount)
        assertEquals(original.organizerId, updated.organizerId)
    }

    @Test
    fun `editar por quem nao organiza e recusado`() = runTest {
        val db = database()
        val repository = FakeEventRepository(db, timeProvider)
        val original = db.events.value.first { it.id == "event-1" }

        val result = repository.updateEvent(
            "event-1",
            draftFrom(original).copy(organizerId = "user-1"),
        )

        assertEquals(AppError.PERMISSION_DENIED, (result as AppResult.Failure).error)
    }

    @Test
    fun `limite de pessoas abaixo dos confirmados e recusado`() = runTest {
        val db = database()
        val repository = FakeEventRepository(db, timeProvider)
        val original = db.events.value.first { it.id == "event-1" }

        val result = repository.updateEvent("event-1", draftFrom(original, capacity = 1))

        assertEquals(AppError.INVALID_INPUT, (result as AppResult.Failure).error)
    }

    @Test
    fun `cancelar evento tira do feed mas mantem o detalhe`() = runTest {
        val db = database()
        val repository = FakeEventRepository(db, timeProvider)

        val result = repository.cancelEvent("event-1")

        assertTrue(result is AppResult.Success)
        val feed = repository.observeEvents(EventFilter()).first()
        assertTrue(feed.none { it.id == "event-1" })
        val detail = repository.observeEvent("event-1").first()
        assertEquals(com.example.sociable.data.model.EventStatus.CANCELLED, detail?.status)
    }

    @Test
    fun `evento cancelado nao pode mais ser editado`() = runTest {
        val db = database()
        val repository = FakeEventRepository(db, timeProvider)
        val original = db.events.value.first { it.id == "event-1" }
        repository.cancelEvent("event-1")

        val result = repository.updateEvent("event-1", draftFrom(original))

        assertEquals(AppError.INVALID_INPUT, (result as AppResult.Failure).error)
    }

    @Test
    fun `avaliacao duplicada do mesmo usuario e recusada`() = runTest {
        val db = database()
        val repository = FakeReviewRepository(db, timeProvider)

        val first = repository.submitReview("event-2", "user-1", "Ana", 5, "Muito bom!")
        assertTrue(first is AppResult.Success)

        val duplicated = repository.submitReview("event-2", "user-1", "Ana", 4, "De novo")
        assertEquals(AppError.ALREADY_REVIEWED, (duplicated as AppResult.Failure).error)

        val reviews = repository.observeReviews("event-2").first()
        assertEquals(1, reviews.size)

        val rating = repository.observeRating("event-2").first()
        assertEquals(5.0, rating.average, 0.001)
        assertEquals(1, rating.count)
    }

    @Test
    fun `avaliacao com nota fora do intervalo e recusada`() = runTest {
        val repository = FakeReviewRepository(database(), timeProvider)

        val result = repository.submitReview("event-2", "user-1", "Ana", 6, "")

        assertEquals(AppError.INVALID_INPUT, (result as AppResult.Failure).error)
    }

    @Test
    fun `mensagem enviada aparece no chat do evento`() = runTest {
        val repository = FakeChatRepository(database(), timeProvider)

        val result = repository.sendMessage("event-2", "user-1", "Ana", "  Bora?  ")
        assertTrue(result is AppResult.Success)

        val messages = repository.observeMessages("event-2").first()
        assertEquals(1, messages.size)
        assertEquals("Bora?", messages.first().text)
        assertEquals("Ana", messages.first().authorName)
    }

    @Test
    fun `mensagem vazia e recusada`() = runTest {
        val repository = FakeChatRepository(database(), timeProvider)

        val result = repository.sendMessage("event-2", "user-1", "Ana", "   ")

        assertEquals(AppError.INVALID_INPUT, (result as AppResult.Failure).error)
    }

    @Test
    fun `perfil atualizado reflete no usuario observado`() = runTest {
        val repository = FakeUserRepository(database())

        val result = repository.updateProfile(
            userId = "user-1",
            name = "Ana Maria",
            bio = "  Nova bio  ",
            photoUrl = " ",
            interests = setOf(Interest.MUSIC, Interest.OUTDOORS),
        )

        assertTrue(result is AppResult.Success)
        val user = repository.observeUser("user-1").first()
        assertNotNull(user)
        assertEquals("Ana Maria", user!!.name)
        assertEquals("Nova bio", user.bio)
        assertEquals(setOf(Interest.MUSIC, Interest.OUTDOORS), user.interests)
    }

    @Test
    fun `perfil social atualizado reflete no usuario observado`() = runTest {
        val repository = FakeUserRepository(database())

        val result = repository.updateSocialProfile(
            userId = "user-1",
            lifeStage = LifeStage.NEW_IN_TOWN,
            lookingFor = setOf(LookingFor.NEW_FRIENDS, LookingFor.NEW_HOBBIES),
            socialVibe = SocialVibe.SMALL_GROUPS,
        )

        assertTrue(result is AppResult.Success)
        val user = repository.observeUser("user-1").first()
        assertNotNull(user)
        assertEquals(LifeStage.NEW_IN_TOWN, user!!.lifeStage)
        assertEquals(
            setOf(LookingFor.NEW_FRIENDS, LookingFor.NEW_HOBBIES),
            user.lookingFor,
        )
        assertEquals(SocialVibe.SMALL_GROUPS, user.socialVibe)
    }

    @Test
    fun `perfil social de usuario inexistente falha`() = runTest {
        val repository = FakeUserRepository(database())

        val result = repository.updateSocialProfile(
            userId = "user-inexistente",
            lifeStage = null,
            lookingFor = emptySet(),
            socialVibe = null,
        )

        assertEquals(AppError.NOT_FOUND, (result as AppResult.Failure).error)
    }

    @Test
    fun `denuncia repetida do mesmo usuario e evento nao duplica registro`() = runTest {
        val db = database()
        val repository = FakeReportRepository(db, timeProvider)

        val first = repository.reportEvent("event-1", "user-1", ReportReason.OTHER, "primeira")
        val second = repository.reportEvent("event-1", "user-1", ReportReason.OTHER, "segunda")

        assertTrue(first is AppResult.Success)
        assertTrue(second is AppResult.Success)
        assertEquals(1, db.reports.value.size)
        assertEquals("primeira", db.reports.value.single().comment)
    }
}
