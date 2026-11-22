package com.example.sociable.data.repository

import com.example.sociable.core.AppResult
import com.example.sociable.data.model.ReportReason

/**
 * Denúncias de eventos.
 *
 * Implementação Firebase esperada:
 *  - subcoleção `events/{eventId}/reports/{userId}` no Firestore, com `eventId`,
 *    `reporterId`, `reason`, `comment` e `createdAt` (timestamp do servidor);
 *  - uma denúncia por pessoa/evento; o cliente só consulta o próprio registro
 *    e a moderação (via Admin SDK) faz a leitura completa.
 */
interface ReportRepository {

    suspend fun reportEvent(
        eventId: String,
        reporterId: String,
        reason: ReportReason,
        comment: String,
    ): AppResult<Unit>
}
