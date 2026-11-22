package com.example.sociable.data.firebase

import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.data.model.ReportReason
import com.example.sociable.data.repository.ReportRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

/**
 * Denúncias de eventos no Cloud Firestore.
 *
 * Cada denúncia vira um documento em `events/{eventId}/reports/{userId}`. O
 * cliente só cria e consulta o próprio registro; a moderação acontece fora do
 * app, via Admin SDK.
 */
class FirestoreReportRepository(
    private val firestore: FirebaseFirestore,
) : ReportRepository {

    override suspend fun reportEvent(
        eventId: String,
        reporterId: String,
        reason: ReportReason,
        comment: String,
    ): AppResult<Unit> = try {
        val reportReference = firestore.collection(COLLECTION_EVENTS)
            .document(eventId)
            .collection(COLLECTION_REPORTS)
            .document(reporterId)

        // O id é determinístico: uma pessoa só pode registrar uma denúncia por
        // evento. A transação torna reenvios idempotentes sem permitir update.
        firestore.runTransaction { transaction ->
            if (!transaction.get(reportReference).exists()) {
                transaction.set(
                    reportReference,
                    mapOf(
                        FIELD_EVENT_ID to eventId,
                        FIELD_REPORTER_ID to reporterId,
                        FIELD_REASON to reason.name,
                        FIELD_COMMENT to comment.trim(),
                        FIELD_CREATED_AT to FieldValue.serverTimestamp(),
                    ),
                )
            }
        }.await()
        AppResult.Success(Unit)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        AppResult.Failure(exception.toFirebaseAppError(), exception)
    }

    private companion object {
        const val COLLECTION_EVENTS = "events"
        const val COLLECTION_REPORTS = "reports"
        const val FIELD_EVENT_ID = "eventId"
        const val FIELD_REPORTER_ID = "reporterId"
        const val FIELD_REASON = "reason"
        const val FIELD_COMMENT = "comment"
        const val FIELD_CREATED_AT = "createdAt"
    }
}
