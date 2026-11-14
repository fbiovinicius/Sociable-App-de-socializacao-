package com.example.sociable.core

/**
 * Resultado padronizado de toda operação que pode falhar.
 *
 * A camada de dados (fake ou Firebase) devolve [AppResult], nunca lança exceção
 * para a UI. Assim o ViewModel só precisa tratar dois casos.
 */
sealed interface AppResult<out T> {

    data class Success<T>(val data: T) : AppResult<T>

    data class Failure(
        val error: AppError,
        val cause: Throwable? = null,
    ) : AppResult<Nothing>
}

inline fun <T> AppResult<T>.onSuccess(block: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) block(data)
    return this
}

inline fun <T> AppResult<T>.onFailure(block: (AppError) -> Unit): AppResult<T> {
    if (this is AppResult.Failure) block(error)
    return this
}

fun <T> AppResult<T>.getOrNull(): T? = (this as? AppResult.Success)?.data

fun <T> AppResult<T>.getOrElse(fallback: T): T = getOrNull() ?: fallback
