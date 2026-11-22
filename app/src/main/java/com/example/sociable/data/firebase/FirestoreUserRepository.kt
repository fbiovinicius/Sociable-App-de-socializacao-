package com.example.sociable.data.firebase

import android.net.Uri
import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.data.model.Interest
import com.example.sociable.data.model.LifeStage
import com.example.sociable.data.model.LookingFor
import com.example.sociable.data.model.SocialVibe
import com.example.sociable.data.model.User
import com.example.sociable.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Perfil do usuário persistido na coleção `users` do Cloud Firestore. */
class FirestoreUserRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : UserRepository {

    override fun observeUser(userId: String): Flow<User?> = callbackFlow {
        val registration = firestore.collection(USERS).document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    trySend(snapshot?.toDomainUser())
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun updateProfile(
        userId: String,
        name: String,
        bio: String,
        photoUrl: String?,
        interests: Set<Interest>,
    ): AppResult<User> {
        return try {
            val document = firestore.collection(USERS).document(userId)
            val snapshot = document.get().await()
            val current = snapshot.toDomainUser()
                ?: return AppResult.Failure(AppError.NOT_FOUND)

            val updated = current.copy(
                name = name.trim(),
                bio = bio.trim(),
                photoUrl = photoUrl?.trim()?.takeIf { it.isNotEmpty() },
                interests = interests,
            )

            // Mantém o nome e a foto do Firebase Authentication sincronizados com o perfil.
            auth.currentUser?.takeIf { it.uid == userId }?.let { firebaseUser ->
                val request = UserProfileChangeRequest.Builder()
                    .setDisplayName(updated.name)
                    .setPhotoUri(updated.photoUrl?.let(Uri::parse))
                    .build()
                firebaseUser.updateProfile(request).await()
            }

            document.set(updated.toFirestoreMap()).await()
            syncAttendeeProfile(userId, updated.name, updated.photoUrl)
            AppResult.Success(updated)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Failure(exception.toFirebaseAppError(), exception)
        }
    }

    override suspend fun updateSocialProfile(
        userId: String,
        lifeStage: LifeStage?,
        lookingFor: Set<LookingFor>,
        socialVibe: SocialVibe?,
    ): AppResult<User> {
        return try {
            val document = firestore.collection(USERS).document(userId)
            val snapshot = document.get().await()
            val current = snapshot.toDomainUser()
                ?: return AppResult.Failure(AppError.NOT_FOUND)

            val updated = current.copy(
                lifeStage = lifeStage,
                lookingFor = lookingFor,
                socialVibe = socialVibe,
            )

            // set() substitui o documento inteiro: o mapa inclui todos os campos.
            document.set(updated.toFirestoreMap()).await()
            AppResult.Success(updated)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Failure(exception.toFirebaseAppError(), exception)
        }
    }

    /**
     * Atualiza a cópia pública usada pela prévia de participantes. É uma
     * sincronização best-effort: o perfil já foi salvo e uma falha pontual não
     * deve impedir a pessoa de continuar usando o app.
     */
    private suspend fun syncAttendeeProfile(
        userId: String,
        name: String,
        photoUrl: String?,
    ) {
        try {
            var cursor: DocumentSnapshot? = null
            while (true) {
                val query = firestore.collectionGroup(ATTENDEES)
                    .whereEqualTo(ATTENDEE_USER_ID, userId)
                    .orderBy(FieldPath.documentId())
                    .limit(ATTENDEE_SYNC_PAGE_SIZE)

                // `userId` é um campo real do participante; o id do documento
                // seria o caminho completo em uma collection group. Ordenar pelo
                // caminho completo torna a paginação estável e permite continuar
                // depois do último documento da página anterior.
                val after = cursor
                val page = if (after == null) {
                    query.get().await()
                } else {
                    query.startAfter(after).get().await()
                }
                if (page.isEmpty) return

                val batch = firestore.batch()
                page.documents.forEach { attendee ->
                    batch.update(
                        attendee.reference,
                        mapOf(
                            "name" to name,
                            "photoUrl" to photoUrl,
                        ),
                    )
                }
                batch.commit().await()

                if (page.size() < ATTENDEE_SYNC_PAGE_SIZE) return
                cursor = page.documents.last()
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            // A próxima confirmação de presença ou nova edição do perfil
            // reconcilia a prévia.
        }
    }

    private companion object {
        const val USERS = "users"
        const val ATTENDEES = "attendees"
        const val ATTENDEE_USER_ID = "userId"
        const val ATTENDEE_SYNC_PAGE_SIZE = 30L
    }
}
