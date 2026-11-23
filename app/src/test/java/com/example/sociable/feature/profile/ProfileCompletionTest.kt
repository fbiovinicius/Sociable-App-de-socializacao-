package com.example.sociable.feature.profile

import com.example.sociable.core.validation.ValidationError
import com.example.sociable.core.validation.Validators
import com.example.sociable.data.model.Interest
import com.example.sociable.data.model.LifeStage
import com.example.sociable.data.model.LookingFor
import com.example.sociable.data.model.SocialVibe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileCompletionTest {

    @Test
    fun `perfil vazio esta em zero e completo esta em cem por cento`() {
        val empty = ProfileCompletion.of("", "", emptySet(), null, emptySet(), null, "")
        val full = ProfileCompletion.of(
            photoUrl = "https://x.com/a.png",
            bio = "oi",
            interests = setOf(Interest.MUSIC),
            lifeStage = LifeStage.entries.first(),
            lookingFor = setOf(LookingFor.entries.first()),
            socialVibe = SocialVibe.entries.first(),
            instagram = "ana",
        )

        assertEquals(0f, empty, 0.0001f)
        assertEquals(1f, full, 0.0001f)
    }

    @Test
    fun `sem foto e sem Instagram sao cinco de sete itens`() {
        val value = ProfileCompletion.of(
            photoUrl = "",
            bio = "oi",
            interests = setOf(Interest.MUSIC),
            lifeStage = LifeStage.entries.first(),
            lookingFor = setOf(LookingFor.entries.first()),
            socialVibe = SocialVibe.entries.first(),
            instagram = "",
        )

        assertEquals(5f / 7f, value, 0.0001f)
        assertEquals(71, (value * 100).toInt())
    }

    @Test
    fun `texto em branco nao conta como preenchido`() {
        val value = ProfileCompletion.of("  ", "   ", emptySet(), null, emptySet(), null, " ")

        assertEquals(0f, value, 0.0001f)
    }

    @Test
    fun `instagram aceita usuario com ou sem arroba e rejeita link ou espaco`() {
        assertEquals("ana.souza_1", Validators.normalizeInstagram("  @ana.souza_1 "))
        assertNull(Validators.instagram(""))
        assertNull(Validators.instagram("@ana.souza_1"))
        assertEquals(ValidationError.INVALID_HANDLE, Validators.instagram("https://instagram.com/ana"))
        assertEquals(ValidationError.INVALID_HANDLE, Validators.instagram("ana souza"))
        assertEquals(ValidationError.INVALID_HANDLE, Validators.instagram("a".repeat(31)))
    }
}
