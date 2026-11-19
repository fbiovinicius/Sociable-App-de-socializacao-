package com.example.sociable.data.model

/**
 * Situação de vida atual do usuário, perguntada no onboarding.
 * Lista fixa para o MVP; os nomes dos valores são o contrato salvo no Firestore.
 */
enum class LifeStage(val label: String) {
    STUDENT("Estudante"),
    RECENT_GRADUATE("Recém-formado"),
    WORKING("Trabalhando (CLT/PJ)"),
    ENTREPRENEUR("Empreendendo / autônomo"),
    CAREER_TRANSITION("Em transição de carreira"),
    STUDYING_FOR_EXAM("Estudando para concurso/vestibular"),
    NEW_IN_TOWN("Novo na cidade"),
    BACK_TO_MANAUS("Voltando para Manaus"),
    REMOTE_WORK("Trabalho remoto / home office"),
    PARENT("Rotina com filhos"),
    RETIRED("Aposentado"),
    SABBATICAL("Ano sabático / pausa"),
    PREFER_NOT_TO_SAY("Prefiro não dizer"),
}
