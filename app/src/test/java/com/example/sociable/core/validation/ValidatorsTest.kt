package com.example.sociable.core.validation

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    @Test
    fun `email valido e aceito`() {
        assertNull(Validators.email("ana@exemplo.com"))
        assertNull(Validators.email("ana.souza+teste@dominio.com.br"))
    }

    @Test
    fun `email invalido e recusado`() {
        assertEquals(ValidationError.REQUIRED, Validators.email("  "))
        assertEquals(ValidationError.INVALID_EMAIL, Validators.email("ana@"))
        assertEquals(ValidationError.INVALID_EMAIL, Validators.email("ana.com"))
    }

    @Test
    fun `senha respeita tamanho minimo e maximo`() {
        assertEquals(ValidationError.REQUIRED, Validators.password(""))
        assertEquals(ValidationError.TOO_SHORT, Validators.password("123"))
        assertNull(Validators.password("123456"))
        assertEquals(ValidationError.TOO_LONG, Validators.password("1".repeat(65)))
    }

    @Test
    fun `confirmacao precisa ser igual a senha`() {
        assertEquals(
            ValidationError.PASSWORDS_DO_NOT_MATCH,
            Validators.confirmPassword("123456", "654321"),
        )
        assertNull(Validators.confirmPassword("123456", "123456"))
    }

    @Test
    fun `nome e validado com trim`() {
        assertEquals(ValidationError.REQUIRED, Validators.name("   "))
        assertEquals(ValidationError.TOO_SHORT, Validators.name(" A "))
        assertNull(Validators.name("  Ana  "))
    }

    @Test
    fun `periodo do evento precisa terminar depois do inicio`() {
        val start = Instant.parse("2026-05-10T20:00:00Z")
        assertEquals(
            ValidationError.END_BEFORE_START,
            Validators.eventPeriod(start, start.minusSeconds(3600)),
        )
        assertEquals(ValidationError.END_BEFORE_START, Validators.eventPeriod(start, start))
        assertNull(Validators.eventPeriod(start, start.plusSeconds(3600)))
    }

    @Test
    fun `duracao do evento precisa ser positiva e no maximo sete dias`() {
        val start = Instant.parse("2026-05-10T20:00:00Z")
        assertEquals(
            ValidationError.END_BEFORE_START,
            Validators.eventDuration(start, start.minusSeconds(3600)),
        )
        assertEquals(
            ValidationError.END_BEFORE_START,
            Validators.eventDuration(start, start),
        )
        assertNull(Validators.eventDuration(start, start.plusSeconds(3600)))
        assertNull(
            Validators.eventDuration(
                start,
                start.plusSeconds(Validators.MAX_EVENT_DURATION_MINUTES * 60),
            ),
        )
        assertEquals(
            ValidationError.INVALID_DURATION,
            Validators.eventDuration(
                start,
                start.plusSeconds(Validators.MAX_EVENT_DURATION_MINUTES * 60 + 60),
            ),
        )
    }

    @Test
    fun `preco aceita virgula e recusa valores invalidos`() {
        assertNull(Validators.price(""))
        assertNull(Validators.price("20,50"))
        assertEquals(ValidationError.INVALID_NUMBER, Validators.price("-1"))
        assertEquals(ValidationError.INVALID_NUMBER, Validators.price("abc"))
        assertEquals(ValidationError.INVALID_NUMBER, Validators.price("100001"))
    }

    @Test
    fun `capacidade recusa zero e valores nao numericos`() {
        assertNull(Validators.capacity(""))
        assertNull(Validators.capacity("40"))
        assertEquals(ValidationError.INVALID_NUMBER, Validators.capacity("0"))
        assertEquals(ValidationError.INVALID_NUMBER, Validators.capacity("-3"))
        assertEquals(ValidationError.INVALID_NUMBER, Validators.capacity("dez"))
    }

    @Test
    fun `coordenadas respeitam os limites`() {
        assertNull(Validators.latitude("-3,1190"))
        assertNull(Validators.longitude("-60,0217"))
        assertEquals(ValidationError.INVALID_COORDINATE, Validators.latitude("91"))
        assertEquals(ValidationError.INVALID_COORDINATE, Validators.longitude("-181"))
        assertEquals(ValidationError.INVALID_COORDINATE, Validators.latitude("abc"))
    }

    @Test
    fun `mensagem do chat nao pode ser vazia`() {
        assertEquals(ValidationError.REQUIRED, Validators.message("   "))
        assertNull(Validators.message("Bora!"))
        assertEquals(
            ValidationError.TOO_LONG,
            Validators.message("x".repeat(Validators.MESSAGE_MAX + 1)),
        )
    }

    @Test
    fun `nota precisa estar entre 1 e 5`() {
        assertEquals(ValidationError.INVALID_RATING, Validators.rating(0))
        assertEquals(ValidationError.INVALID_RATING, Validators.rating(6))
        assertNull(Validators.rating(1))
        assertNull(Validators.rating(5))
    }

    @Test
    fun `url de imagem aceita http e https`() {
        assertNull(Validators.imageUrl(""))
        assertNull(Validators.imageUrl("https://exemplo.com/foto.jpg"))
        assertEquals(ValidationError.INVALID_URL, Validators.imageUrl("exemplo.com/foto.jpg"))
        assertEquals(ValidationError.INVALID_URL, Validators.imageUrl("ftp://exemplo.com/foto.jpg"))
    }
}
