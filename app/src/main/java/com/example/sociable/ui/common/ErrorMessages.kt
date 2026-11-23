package com.example.sociable.ui.common

import com.example.sociable.core.AppError
import com.example.sociable.core.validation.ValidationError

/**
 * Tradução dos erros de lógica para texto.
 * Quando a UI definitiva chegar, estes textos podem virar `stringResource`.
 */
fun AppError.message(): String = when (this) {
    AppError.UNKNOWN -> "Algo deu errado. Tente novamente."
    AppError.NETWORK -> "Sem conexão. Verifique sua internet."
    AppError.INVALID_CREDENTIALS -> "E-mail ou senha inválidos."
    AppError.EMAIL_ALREADY_IN_USE -> "Já existe uma conta com este e-mail."
    AppError.WEAK_PASSWORD -> "A senha é muito fraca."
    AppError.USER_NOT_FOUND -> "Usuário não encontrado."
    AppError.UNAUTHENTICATED -> "Entre na sua conta para continuar."
    AppError.PERMISSION_DENIED -> "Você não tem permissão para isso."
    AppError.NOT_FOUND -> "Não encontramos o que você procura."
    AppError.EVENT_FULL -> "Este evento já está lotado."
    AppError.ALREADY_REVIEWED -> "Você já avaliou este evento."
    AppError.INVALID_INPUT -> "Dados inválidos. Revise os campos."
    AppError.CHAT_NOT_AVAILABLE -> "O chat deste evento não está disponível."
    AppError.GOOGLE_SIGN_IN_UNAVAILABLE ->
        "Não foi possível entrar com o Google. Verifique se há uma conta Google no aparelho e tente de novo."
}

fun ValidationError.message(): String = when (this) {
    ValidationError.REQUIRED -> "Campo obrigatório."
    ValidationError.INVALID_EMAIL -> "E-mail inválido."
    ValidationError.TOO_SHORT -> "Muito curto."
    ValidationError.TOO_LONG -> "Muito longo."
    ValidationError.PASSWORDS_DO_NOT_MATCH -> "As senhas não coincidem."
    ValidationError.INVALID_NUMBER -> "Número inválido."
    ValidationError.INVALID_COORDINATE -> "Coordenada inválida."
    ValidationError.INVALID_RATING -> "Escolha uma nota de 1 a 5."
    ValidationError.END_BEFORE_START -> "O fim deve ser depois do início."
    ValidationError.INVALID_DURATION -> "O evento pode durar no máximo 7 dias."
    ValidationError.INVALID_URL -> "Informe uma URL http(s) válida."
    ValidationError.CAPACITY_BELOW_GOING -> "Já há mais pessoas confirmadas do que esse limite."
}
