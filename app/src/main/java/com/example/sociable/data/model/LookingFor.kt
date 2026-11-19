package com.example.sociable.data.model

/**
 * Objetivos do usuário no app, perguntados no onboarding.
 * A pessoa pode escolher até [LookingForRules.MAX_CHOICES] opções.
 */
enum class LookingFor(val label: String) {
    NEW_FRIENDS("Fazer novos amigos"),
    WIDER_SOCIAL_CIRCLE("Ampliar meu círculo social"),
    NEW_HOBBIES("Descobrir novos hobbies"),
    GOING_OUT("Companhia para sair"),
    RELATIONSHIP("Conhecer pessoas para relacionamento"),
    NETWORKING("Networking / contatos profissionais"),
    WORKOUT_PARTNER("Parceria para treinar / esporte"),
    GAMING("Gente para jogar"),
    SHOWS_AND_CULTURE("Ir a shows e eventos culturais"),
    RESTAURANTS("Companhia para restaurantes"),
    VOLUNTEERING("Voluntariado / causas"),
    LANGUAGES("Praticar idiomas"),
    STUDY_GROUP("Grupo de estudos"),
    PROJECT_PARTNERS("Sócios ou parcerias para projetos"),
    BREAK_ROUTINE("Sair mais da rotina"),
}

/** Regras compartilhadas entre onboarding, perfil e persistência. */
object LookingForRules {
    const val MAX_CHOICES = 3
}
