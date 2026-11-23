package com.example.sociable.core

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Erros de negócio/infraestrutura conhecidos pela aplicação.
 *
 * Importante: a mensagem em português NÃO fica aqui. A UI (ou o parceiro de UI)
 * mapeia cada valor para um `stringResource`. Isso evita acoplar a camada de
 * dados a textos de tela e facilita a internacionalização.
 */
enum class AppError {
    UNKNOWN,
    NETWORK,
    INVALID_CREDENTIALS,
    EMAIL_ALREADY_IN_USE,
    WEAK_PASSWORD,
    USER_NOT_FOUND,
    UNAUTHENTICATED,
    PERMISSION_DENIED,
    NOT_FOUND,
    EVENT_FULL,
    ALREADY_REVIEWED,
    INVALID_INPUT,
    CHAT_NOT_AVAILABLE,
    GOOGLE_SIGN_IN_UNAVAILABLE,
}

fun Throwable.toAppError(): AppError = when (this) {
    is UnknownHostException, is SocketTimeoutException, is IOException -> AppError.NETWORK
    else -> AppError.UNKNOWN
}
