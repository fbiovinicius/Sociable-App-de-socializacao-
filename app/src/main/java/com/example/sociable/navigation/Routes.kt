package com.example.sociable.navigation

/** Rotas de navegação do app. */
object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val ONBOARDING = "onboarding"
    const val INTERESTS = "interests"
    const val EVENTS = "events"
    const val SECRET = "secret"
    const val PROFILE = "profile"
    const val MAP = "map"

    const val CREATE_EVENT = "create_event?secret={secret}"
    const val EDIT_EVENT = "event/{eventId}/edit"
    const val EVENT_DETAIL = "event/{eventId}"
    const val CHAT = "event/{eventId}/chat"
    const val REVIEWS = "event/{eventId}/reviews"

    fun eventDetail(eventId: String): String = "event/$eventId"
    fun editEvent(eventId: String): String = "event/$eventId/edit"
    fun chat(eventId: String): String = "event/$eventId/chat"
    fun reviews(eventId: String): String = "event/$eventId/reviews"
    fun createEvent(secret: Boolean = false): String = "create_event?secret=$secret"
}
