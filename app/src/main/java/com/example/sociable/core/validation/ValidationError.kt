package com.example.sociable.core.validation

/**
 * Erros possíveis de um campo de formulário.
 * A UI traduz cada valor para uma string; nenhum texto fica na camada de lógica.
 */
enum class ValidationError {
    REQUIRED,
    INVALID_EMAIL,
    TOO_SHORT,
    TOO_LONG,
    PASSWORDS_DO_NOT_MATCH,
    INVALID_NUMBER,
    INVALID_COORDINATE,
    INVALID_RATING,
    END_BEFORE_START,
    INVALID_DURATION,
    INVALID_URL,
}
