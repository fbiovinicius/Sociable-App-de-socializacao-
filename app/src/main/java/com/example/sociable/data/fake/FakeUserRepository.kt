package com.example.sociable.data.fake

import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.data.model.Interest
import com.example.sociable.data.model.LifeStage
import com.example.sociable.data.model.LookingFor
import com.example.sociable.data.model.SocialVibe
import com.example.sociable.data.model.User
import com.example.sociable.data.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeUserRepository(
    private val db: FakeDatabase,
) : UserRepository {

    override fun observeUser(userId: String): Flow<User?> =
        db.users.map { users -> users.firstOrNull { it.id == userId } }

    override suspend fun updateProfile(
        userId: String,
        name: String,
        bio: String,
        photoUrl: String?,
        interests: Set<Interest>,
    ): AppResult<User> {
        delay(300)
        val current = db.users.value.firstOrNull { it.id == userId }
            ?: return AppResult.Failure(AppError.NOT_FOUND)

        val updated = current.copy(
            name = name.trim(),
            bio = bio.trim(),
            photoUrl = photoUrl?.trim()?.takeIf { it.isNotBlank() },
            interests = interests,
        )
        db.users.update { users ->
            users.map { if (it.id == userId) updated else it }
        }
        return AppResult.Success(updated)
    }

    override suspend fun updateSocialProfile(
        userId: String,
        lifeStage: LifeStage?,
        lookingFor: Set<LookingFor>,
        socialVibe: SocialVibe?,
    ): AppResult<User> {
        delay(300)
        val current = db.users.value.firstOrNull { it.id == userId }
            ?: return AppResult.Failure(AppError.NOT_FOUND)

        val updated = current.copy(
            lifeStage = lifeStage,
            lookingFor = lookingFor,
            socialVibe = socialVibe,
        )
        db.users.update { users ->
            users.map { if (it.id == userId) updated else it }
        }
        return AppResult.Success(updated)
    }
}
