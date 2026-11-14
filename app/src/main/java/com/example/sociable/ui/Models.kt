package com.example.sociable.ui

import androidx.annotation.DrawableRes
import com.example.sociable.R

const val MAX_INTERESTS = 5

enum class Category(val label: String, @DrawableRes val pin: Int) {
    Party("Festa", R.drawable.ic_pin_party),
    People("Encontro", R.drawable.ic_pin_people),
    Show("Show", R.drawable.ic_pin_show),
    Secret("Segredinho", R.drawable.ic_pin_secret),
}

/** x e y são a posição no mapa ilustrativo, de 0 a 1. Troque por latitude/longitude quando usar o Google Maps. */
data class EventItem(
    val id: Int,
    val title: String,
    val category: Category,
    val place: String,
    val whenText: String,
    val priceText: String,
    val distanceKm: Double,
    val confirmed: Int,
    val capacity: Int?,
    val tags: Set<String>,
    val happeningNow: Boolean,
    val description: String,
    val x: Float,
    val y: Float,
) {
    val isFree: Boolean get() = priceText.equals("Gratuito", ignoreCase = true)
    val distanceText: String get() = "%.1f km".format(distanceKm).replace('.', ',')
    val spotsText: String
        get() = if (capacity != null) "$confirmed de $capacity vagas" else "$confirmed confirmados"
}

data class UserProfile(
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val city: String = "",
    val interests: List<String> = emptyList(),
)

val mapFilters = listOf(
    "Todos", "Agora", "Gratuitos", "Perto de mim",
    "Música", "Feiras", "Gastronomia", "Esporte", "Cultura",
)

fun EventItem.matches(filter: String): Boolean = when (filter) {
    "Todos" -> true
    "Agora" -> happeningNow
    "Gratuitos" -> isFree
    "Perto de mim" -> distanceKm <= 3.0
    else -> filter in tags
}

val interestGroups: List<Pair<String, List<String>>> = listOf(
    "Rolês" to listOf("🎵 Música ao vivo", "🎉 Festas", "🎤 Shows", "🍽️ Gastronomia", "🍻 Bares", "🛍️ Feiras"),
    "Cultura" to listOf("🎨 Arte e cultura", "🌿 Cultura amazônica", "🎭 Teatro", "📚 Livros e saraus", "🎬 Cinema", "📸 Fotografia"),
    "Ao ar livre" to listOf("⚽ Esporte", "🌳 Natureza", "☀️ Ao ar livre", "🚴 Pedalada", "🏖️ Praia e rio", "🥾 Trilhas"),
    "Pessoas" to listOf("🤝 Encontros", "👯 Fazer amizades", "👨‍👩‍👧 Em família", "🎲 Jogos de tabuleiro", "🐶 Pets", "💻 Tecnologia"),
)

val sampleEvents: List<EventItem> = listOf(
    EventItem(
        1, "Roda de samba no Centro", Category.Show, "Largo de São Sebastião", "Hoje · 19h",
        "Gratuito", 1.2, 7, null, setOf("Música", "Cultura"), true,
        "Roda de samba aberta, com instrumentos na mesa. Chegue cedo para pegar lugar e leve um amigo.",
        0.52f, 0.44f,
    ),
    EventItem(
        2, "Feira de artesanato", Category.People, "Ponta Negra", "Sáb · 10h",
        "Gratuito", 8.5, 23, null, setOf("Feiras", "Cultura"), false,
        "Artesanato local, comida regional e música ao vivo à beira do rio.",
        0.14f, 0.34f,
    ),
    EventItem(
        3, "Festival de comida regional", Category.Party, "Praça da Saudade", "Hoje · 18h",
        "R$ 15", 2.4, 42, 120, setOf("Gastronomia", "Feiras"), true,
        "Tacacá, pirarucu, bolo de macaxeira e muito mais, de vários cozinheiros da cidade.",
        0.70f, 0.56f,
    ),
    EventItem(
        4, "Luau na beira do rio", Category.Secret, "Local revelado 2 h antes", "Sáb · 21h",
        "R$ 20", 3.1, 5, 8, setOf("Música"), false,
        "Violão, fogueira e gente nova. O endereço chega por mensagem depois que o convite é aceito.",
        0.26f, 0.27f,
    ),
    EventItem(
        5, "Jogos de tabuleiro", Category.Secret, "Local revelado 2 h antes", "Dom · 16h",
        "Gratuito", 4.0, 3, 6, setOf("Cultura"), false,
        "Tarde de jogos só para mulheres. Traga seu jogo favorito ou aprenda um novo.",
        0.78f, 0.72f,
    ),
    EventItem(
        6, "Sarau no quintal", Category.Secret, "Local revelado 2 h antes", "Sex · 20h",
        "Gratuito", 1.8, 9, 15, setOf("Cultura", "Música"), false,
        "Poesia, violão e petiscos. Cada pessoa leva um texto ou uma música para dividir.",
        0.40f, 0.70f,
    ),
    EventItem(
        7, "Pelada no Parque do Mindu", Category.People, "Parque do Mindu", "Dom · 8h",
        "Gratuito", 5.6, 14, 22, setOf("Esporte"), false,
        "Futebol de campo misto, todos os níveis. Depois tem lanche coletivo.",
        0.84f, 0.80f,
    ),
    EventItem(
        8, "Forró no Mercado", Category.Show, "Mercado Adolpho Lisboa", "Sex · 21h",
        "R$ 25", 2.0, 56, null, setOf("Música"), false,
        "Trio de forró com pista de dança e aula rápida para quem nunca dançou.",
        0.60f, 0.38f,
    ),
)