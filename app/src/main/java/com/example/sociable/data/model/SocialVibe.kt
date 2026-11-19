package com.example.sociable.data.model

/**
 * Jeito preferido de viver os rolês, perguntado no onboarding.
 * Escolha única; "Prefiro não dizer" também é uma resposta válida.
 */
enum class SocialVibe(val label: String) {
    SMALL_GROUPS("Grupos pequenos, conversa boa"),
    BIG_CROWD("Movimento e galera grande"),
    ACTIVITY_FIRST("Mais atividade que conversa"),
    DEPENDS_ON_THE_DAY("Depende do dia"),
    PREFER_NOT_TO_SAY("Prefiro não dizer"),
}
