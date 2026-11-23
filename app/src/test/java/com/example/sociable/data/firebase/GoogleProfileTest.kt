package com.example.sociable.data.firebase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GoogleProfileTest {

    @Test
    fun `usa o nome do Google quando tem pelo menos 2 caracteres`() {
        assertEquals("Ana Souza", googleDisplayName("  Ana Souza ", "ana@gmail.com"))
    }

    @Test
    fun `nome ausente ou curto vira o inicio do e-mail`() {
        assertEquals("ana.souza", googleDisplayName(null, "ana.souza@gmail.com"))
        assertEquals("ana", googleDisplayName("A", "ana@gmail.com"))
    }

    @Test
    fun `sem nome e sem e-mail usa o nome padrao`() {
        assertEquals("Usuário Sociable", googleDisplayName("", ""))
    }

    @Test
    fun `nome muito longo e cortado em 60 caracteres`() {
        assertEquals(60, googleDisplayName("a".repeat(100), "x@y.com").length)
    }

    @Test
    fun `so aceita foto https de ate 500 caracteres`() {
        assertEquals("https://lh3.googleusercontent.com/a", validPhotoUrlOrNull("https://lh3.googleusercontent.com/a"))
        assertNull(validPhotoUrlOrNull("http://x.com/a.png"))
        assertNull(validPhotoUrlOrNull("https://" + "a".repeat(500)))
    }
}
