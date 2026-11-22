package com.example.sociable.data.fake

import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.data.model.EventReport
import com.example.sociable.data.model.ReportReason
import com.example.sociable.data.repository.ReportRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update

/** Denúncias guardadas em memória (usado nos testes e no modo fake). */
class FakeReportRepository(
    private val db: FakeDatabase,
    private val timeProvider: TimeProvider,
) : ReportRepository {

    override suspend fun reportEvent(
        eventId: String,
        reporterId: String,
        reason: ReportReason,
        comment: String,
    ): AppResult<Unit> {
        delay(300)
        val alreadyReported = db.reports.value.any {
            it.eventId == eventId && it.reporterId == reporterId
        }
        if (alreadyReported) return AppResult.Success(Unit)

        val eventExists = db.events.value.any { it.id == eventId }
        if (!eventExists) return AppResult.Failure(AppError.NOT_FOUND)

        val report = EventReport(
            eventId = eventId,
            reporterId = reporterId,
            reason = reason,
            comment = comment.trim(),
            createdAt = timeProvider.now(),
        )
        db.reports.update { it + report }
        return AppResult.Success(Unit)
    }
}
