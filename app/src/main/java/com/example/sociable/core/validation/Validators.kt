package com.example.sociable.core.validation

import java.time.Duration
import java.time.Instant

/**
 * Validações puras (sem Android, sem Firebase) para serem testáveis com JUnit.
 * São a fonte única de verdade usada pelos ViewModels e pelos testes unitários.
 */
object Validators {

    const val NAME_MIN = 2
    const val NAME_MAX = 60
    const val PASSWORD_MIN = 6
    const val PASSWORD_MAX = 64
    const val TITLE_MIN = 3
    const val TITLE_MAX = 80
    const val DESCRIPTION_MIN = 10
    const val DESCRIPTION_MAX = 1000
    const val PLACE_MAX = 120
    const val BIO_MAX = 160
    const val MESSAGE_MAX = 500
    const val COMMUNITY_NAME_MIN = 3
    const val COMMUNITY_NAME_MAX = 60
    const val COMMUNITY_DESCRIPTION_MAX = 200
    const val REVIEW_COMMENT_MAX = 300
    const val IMAGE_URL_MAX = 500
    const val PHOTO_URL_MAX = 500
    const val MAX_PRICE = 100_000.0
    const val MAX_CAPACITY = 10_000
    const val MAX_EVENT_DURATION_MINUTES = 7L * 24 * 60

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val URL_REGEX = Regex("^https?://.+", RegexOption.IGNORE_CASE)
    private val PHOTO_URL_REGEX = Regex("^https://.+", RegexOption.IGNORE_CASE)

    fun name(value: String): ValidationError? {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> ValidationError.REQUIRED
            trimmed.length < NAME_MIN -> ValidationError.TOO_SHORT
            trimmed.length > NAME_MAX -> ValidationError.TOO_LONG
            else -> null
        }
    }

    fun email(value: String): ValidationError? {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> ValidationError.REQUIRED
            !EMAIL_REGEX.matches(trimmed) -> ValidationError.INVALID_EMAIL
            else -> null
        }
    }

    fun password(value: String): ValidationError? = when {
        value.isEmpty() -> ValidationError.REQUIRED
        value.length < PASSWORD_MIN -> ValidationError.TOO_SHORT
        value.length > PASSWORD_MAX -> ValidationError.TOO_LONG
        else -> null
    }

    fun confirmPassword(password: String, confirmation: String): ValidationError? = when {
        confirmation.isEmpty() -> ValidationError.REQUIRED
        password != confirmation -> ValidationError.PASSWORDS_DO_NOT_MATCH
        else -> null
    }

    fun eventTitle(value: String): ValidationError? {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> ValidationError.REQUIRED
            trimmed.length < TITLE_MIN -> ValidationError.TOO_SHORT
            trimmed.length > TITLE_MAX -> ValidationError.TOO_LONG
            else -> null
        }
    }

    fun eventDescription(value: String): ValidationError? {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> ValidationError.REQUIRED
            trimmed.length < DESCRIPTION_MIN -> ValidationError.TOO_SHORT
            trimmed.length > DESCRIPTION_MAX -> ValidationError.TOO_LONG
            else -> null
        }
    }

    fun place(value: String): ValidationError? {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> ValidationError.REQUIRED
            trimmed.length > PLACE_MAX -> ValidationError.TOO_LONG
            else -> null
        }
    }

    fun bio(value: String): ValidationError? =
        if (value.length > BIO_MAX) ValidationError.TOO_LONG else null

    fun price(value: String): ValidationError? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null
        val parsed = trimmed.replace(',', '.').toDoubleOrNull()
            ?: return ValidationError.INVALID_NUMBER
        return when {
            parsed < 0.0 -> ValidationError.INVALID_NUMBER
            parsed > MAX_PRICE -> ValidationError.INVALID_NUMBER
            else -> null
        }
    }

    fun capacity(value: String): ValidationError? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null
        val parsed = trimmed.toIntOrNull() ?: return ValidationError.INVALID_NUMBER
        return when {
            parsed <= 0 -> ValidationError.INVALID_NUMBER
            parsed > MAX_CAPACITY -> ValidationError.INVALID_NUMBER
            else -> null
        }
    }

    fun latitude(value: String): ValidationError? {
        val parsed = value.trim().replace(',', '.').toDoubleOrNull()
            ?: return ValidationError.INVALID_COORDINATE
        return if (parsed in -90.0..90.0) null else ValidationError.INVALID_COORDINATE
    }

    fun longitude(value: String): ValidationError? {
        val parsed = value.trim().replace(',', '.').toDoubleOrNull()
            ?: return ValidationError.INVALID_COORDINATE
        return if (parsed in -180.0..180.0) null else ValidationError.INVALID_COORDINATE
    }

    fun eventPeriod(startAt: Instant, endAt: Instant): ValidationError? =
        if (endAt.isAfter(startAt)) null else ValidationError.END_BEFORE_START

    /**
     * Fim depois do início e duração dentro do limite. Evita que um erro de
     * digitação (ex.: ano trocado) crie um evento de meses.
     */
    fun eventDuration(startAt: Instant, endAt: Instant): ValidationError? {
        val minutes = Duration.between(startAt, endAt).toMinutes()
        return when {
            minutes <= 0 -> ValidationError.END_BEFORE_START
            minutes > MAX_EVENT_DURATION_MINUTES -> ValidationError.INVALID_DURATION
            else -> null
        }
    }

    fun communityName(value: String): ValidationError? {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> ValidationError.REQUIRED
            trimmed.length < COMMUNITY_NAME_MIN -> ValidationError.TOO_SHORT
            trimmed.length > COMMUNITY_NAME_MAX -> ValidationError.TOO_LONG
            else -> null
        }
    }

    /** A descrição da comunidade é opcional. */
    fun communityDescription(value: String): ValidationError? =
        if (value.trim().length > COMMUNITY_DESCRIPTION_MAX) ValidationError.TOO_LONG else null

    fun message(value: String): ValidationError? {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> ValidationError.REQUIRED
            trimmed.length > MESSAGE_MAX -> ValidationError.TOO_LONG
            else -> null
        }
    }

    fun reviewComment(value: String): ValidationError? =
        if (value.length > REVIEW_COMMENT_MAX) ValidationError.TOO_LONG else null

    fun rating(value: Int): ValidationError? =
        if (value in 1..5) null else ValidationError.INVALID_RATING

    fun imageUrl(value: String): ValidationError? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null
        return when {
            trimmed.length > IMAGE_URL_MAX -> ValidationError.TOO_LONG
            !URL_REGEX.matches(trimmed) -> ValidationError.INVALID_URL
            else -> null
        }
    }

    /** Foto de perfil: exige HTTPS para evitar tráfego inseguro e URLs locais. */
    fun photoUrl(value: String): ValidationError? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null
        return when {
            trimmed.length > PHOTO_URL_MAX -> ValidationError.TOO_LONG
            !PHOTO_URL_REGEX.matches(trimmed) -> ValidationError.INVALID_URL
            else -> null
        }
    }
}
