package com.example.sociable.navigation

/** Rotas de navegação do app. Placeholder simples para a UI provisória. */
object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val EVENTS = "events"
    const val CREATE_EVENT = "create_event"
    const val PROFILE = "profile"
    const val MAP = "map"

    const val EVENT_DETAIL = "event/{eventId}"
    const val CHAT = "event/{eventId}/chat"
    const val REVIEWS = "event/{eventId}/reviews"

    fun eventDetail(eventId: String): String = "event/$eventId"
    fun chat(eventId: String): String = "event/$eventId/chat"
    fun reviews(eventId: String): String = "event/$eventId/reviews"
}
