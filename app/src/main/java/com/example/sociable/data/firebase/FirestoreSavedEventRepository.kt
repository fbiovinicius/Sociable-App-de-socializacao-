package com.example.sociable.data.firebase

import com.example.sociable.core.AppResult
import com.example.sociable.data.repository.SavedEventRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Eventos salvos em `users/{userId}/savedEvents/{eventId}` (o id do documento é o id do
 * evento). Só a própria pessoa lê e escreve, o que as regras do Firestore garantem.
 */
class FirestoreSavedEventRepository(
    private val firestore: FirebaseFirestore,
) : SavedEventRepository {

    override fun observeSavedIds(userId: String): Flow<Set<String>> =
        savedEvents(userId)
            .snapshotFlow()
            .map { snapshot -> snapshot.documents.map { it.id }.toSet() }

    override suspend fun setSaved(userId: String, eventId: String, saved: Boolean): AppResult<Unit> =
        try {
            val reference = savedEvents(userId).document(eventId)
            if (saved) {
                reference.set(
                    mapOf(
                        FIELD_EVENT_ID to eventId,
                        FIELD_SAVED_AT to FieldValue.serverTimestamp(),
                    )
                ).await()
            } else {
                reference.delete().await()
            }
            AppResult.Success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Failure(exception.toFirebaseAppError(), exception)
        }

    private fun savedEvents(userId: String) =
        firestore.collection(COLLECTION_USERS).document(userId).collection(SUBCOLLECTION_SAVED)

    private companion object {
        const val COLLECTION_USERS = "users"
        const val SUBCOLLECTION_SAVED = "savedEvents"
        const val FIELD_EVENT_ID = "eventId"
        const val FIELD_SAVED_AT = "savedAt"
    }
}
