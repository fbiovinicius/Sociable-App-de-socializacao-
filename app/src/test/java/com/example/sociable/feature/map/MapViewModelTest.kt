package com.example.sociable.feature.map

import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.data.fake.FakeAuthRepository
import com.example.sociable.data.fake.FakeDatabase
import com.example.sociable.data.fake.FakeEventRepository
import com.example.sociable.data.fake.FakeReportRepository
import com.example.sociable.data.model.ReportReason
import com.example.sociable.data.repository.ReportRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

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

    private suspend fun TestScope.viewModel(
        signedIn: Boolean = true,
        reportRepositoryFactory: ((FakeDatabase) -> ReportRepository)? = null,
    ): Triple<MapViewModel, FakeDatabase, FakeAuthRepository> {
        val db = FakeDatabase(timeProvider)
        val auth = FakeAuthRepository(db, timeProvider)
        if (signedIn) auth.signIn("demo@sociable.app", "123456")
        val viewModel = MapViewModel(
            eventRepository = FakeEventRepository(db, timeProvider),
            reportRepository = reportRepositoryFactory?.invoke(db)
                ?: FakeReportRepository(db, timeProvider),
            authRepository = auth,
            timeProvider = timeProvider,
        )
        advanceUntilIdle()
        return Triple(viewModel, db, auth)
    }

    @Test
    fun `mapa carrega os eventos de hoje sem os segredinhos`() = runTest(dispatcher) {
        val (viewModel, _, _) = viewModel()

        val markers = viewModel.uiState.value.markers
        assertTrue(markers.isNotEmpty())
        assertTrue(markers.none { it.eventId == "event-6" }) // segredinho
        assertTrue(markers.any { it.eventId == "event-1" })
    }

    @Test
    fun `primeiro evento fica selecionado para o cartao do mapa`() = runTest(dispatcher) {
        val (viewModel, _, _) = viewModel()

        assertEquals("event-1", viewModel.uiState.value.selectedEventId)
    }

    @Test
    fun `selecionar um pin carrega a previa de participantes`() = runTest(dispatcher) {
        val (viewModel, _, _) = viewModel()

        viewModel.onMarkerSelected("event-1")
        advanceUntilIdle()

        val attendees = viewModel.uiState.value.attendees
        assertEquals(2, attendees.size)
        assertTrue(attendees.any { it.name == "Bruno Lima" })
        assertTrue(attendees.any { it.name == "Carla Reis" })
    }

    @Test
    fun `trocar de evento troca a previa de participantes`() = runTest(dispatcher) {
        val (viewModel, _, _) = viewModel()

        viewModel.onMarkerSelected("event-2")
        advanceUntilIdle()

        val attendees = viewModel.uiState.value.attendees
        assertEquals(1, attendees.size)
        assertEquals("Ana Souza", attendees.first().name)
    }

    @Test
    fun `denunciar o evento selecionado registra a denuncia`() = runTest(dispatcher) {
        val (viewModel, db, _) = viewModel()
        viewModel.onMarkerSelected("event-1")
        advanceUntilIdle()

        viewModel.reportEvent(ReportReason.SPAM_OR_SCAM, " (evento falso)  ")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.reportSent)
        val report = db.reports.value.single()
        assertEquals("event-1", report.eventId)
        assertEquals(ReportReason.SPAM_OR_SCAM, report.reason)
        assertEquals("(evento falso)", report.comment)
        assertEquals("user-1", report.reporterId)
    }

    @Test
    fun `toques duplicados nao enviam a mesma denuncia duas vezes`() = runTest(dispatcher) {
        val (viewModel, db, _) = viewModel()
        viewModel.onMarkerSelected("event-1")
        advanceUntilIdle()

        viewModel.reportEvent(ReportReason.OTHER, "primeiro")
        viewModel.reportEvent(ReportReason.OTHER, "segundo")
        advanceUntilIdle()

        assertEquals(1, db.reports.value.size)
        assertEquals("primeiro", db.reports.value.single().comment)
    }

    @Test
    fun `falha ao enviar denuncia mantem o erro para a tela`() = runTest(dispatcher) {
        val (viewModel, _, _) = viewModel(
            reportRepositoryFactory = {
                object : ReportRepository {
                    override suspend fun reportEvent(
                        eventId: String,
                        reporterId: String,
                        reason: ReportReason,
                        comment: String,
                    ): AppResult<Unit> = AppResult.Failure(AppError.NETWORK)
                }
            },
        )

        viewModel.reportEvent(ReportReason.OTHER, "comentário")
        advanceUntilIdle()

        assertEquals(AppError.NETWORK, viewModel.uiState.value.reportError)
        assertTrue(!viewModel.uiState.value.reportSent)
        assertTrue(!viewModel.uiState.value.isReporting)
    }

    @Test
    fun `denunciar sem estar logado mostra erro`() = runTest(dispatcher) {
        val (viewModel, _, _) = viewModel(signedIn = false)

        viewModel.reportEvent(ReportReason.OTHER, "")
        advanceUntilIdle()

        assertEquals(AppError.UNAUTHENTICATED, viewModel.uiState.value.reportError)
        assertTrue(!viewModel.uiState.value.reportSent)
    }
}
