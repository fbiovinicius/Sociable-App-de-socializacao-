package com.example.sociable.data.fake

import com.example.sociable.core.AppResult
import com.example.sociable.data.repository.SavedEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Eventos salvos em memória, para os testes. */
class FakeSavedEventRepository : SavedEventRepository {

    /** userId -> ids dos eventos salvos. */
    private val saved = MutableStateFlow<Map<String, Set<String>>>(emptyMap())

    override fun observeSavedIds(userId: String): Flow<Set<String>> =
        saved.map { it[userId].orEmpty() }

    override suspend fun setSaved(userId: String, eventId: String, saved: Boolean): AppResult<Unit> {
        this.saved.update { all ->
            val current = all[userId].orEmpty()
            all + (userId to if (saved) current + eventId else current - eventId)
        }
        return AppResult.Success(Unit)
    }
}
