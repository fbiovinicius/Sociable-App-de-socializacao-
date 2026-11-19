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

    private companion object {
        const val USERS = "users"
    }
}
