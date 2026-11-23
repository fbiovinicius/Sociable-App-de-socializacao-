package com.example.sociable.feature.profile

import com.example.sociable.data.model.Interest
import com.example.sociable.data.model.LifeStage
import com.example.sociable.data.model.LookingFor
import com.example.sociable.data.model.SocialVibe

/** Quantos itens o perfil tem; cada um preenchido vale 1/[TOTAL] do anel de progresso. */
object ProfileCompletion {
    const val TOTAL = 7

    /**
     * Fração de 0f a 1f do perfil preenchido: foto, bio, interesses, situação de vida,
     * o que procura, vibe social e Instagram.
     */
    fun of(
        photoUrl: String,
        bio: String,
        interests: Set<Interest>,
        lifeStage: LifeStage?,
        lookingFor: Set<LookingFor>,
        socialVibe: SocialVibe?,
        instagram: String,
    ): Float {
        val filled = listOf(
            photoUrl.isNotBlank(),
            bio.isNotBlank(),
            interests.isNotEmpty(),
            lifeStage != null,
            lookingFor.isNotEmpty(),
            socialVibe != null,
            instagram.isNotBlank(),
        ).count { it }
        return filled.toFloat() / TOTAL
    }
}
