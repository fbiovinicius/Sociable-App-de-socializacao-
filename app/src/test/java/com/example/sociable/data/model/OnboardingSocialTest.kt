package com.example.sociable.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingSocialTest {

    @Test
    fun `situacao de vida e vibe oferecem preferir nao dizer`() {
        assertTrue(LifeStage.PREFER_NOT_TO_SAY in LifeStage.entries)
        assertTrue(SocialVibe.PREFER_NOT_TO_SAY in SocialVibe.entries)
        assertFalse(LookingFor.entries.any { it.name == "PREFER_NOT_TO_SAY" })
    }

    @Test
    fun `rotulos sao unicos e nao vazios em cada categoria`() {
        listOf(
            LifeStage.entries.map { it.label },
            LookingFor.entries.map { it.label },
            SocialVibe.entries.map { it.label },
        ).forEach { labels ->
            assertEquals(labels.size, labels.distinct().size)
            assertTrue(labels.all { it.isNotBlank() })
        }
    }

    @Test
    fun `o que procura permite no maximo tres escolhas`() {
        assertEquals(3, LookingForRules.MAX_CHOICES)
        assertTrue(LookingFor.entries.size > LookingForRules.MAX_CHOICES)
    }

    @Test
    fun `wizard tem a quantidade esperada de opcoes`() {
        assertEquals(13, LifeStage.entries.size)
        assertEquals(15, LookingFor.entries.size)
        assertEquals(5, SocialVibe.entries.size)
    }
}
