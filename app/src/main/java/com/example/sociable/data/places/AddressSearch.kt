package com.example.sociable.data.places

import com.example.sociable.core.AppResult

/** Uma sugestão da lista que aparece enquanto a pessoa digita o endereço. */
data class AddressSuggestion(
    val placeId: String,
    /** Parte principal, em destaque (ex.: "Av. Djalma Batista, 1000"). */
    val primaryText: String,
    /** Complemento (ex.: "Chapada, Manaus - AM"). */
    val secondaryText: String,
    /** Texto completo, usado para preencher o campo. */
    val fullText: String,
)

/** Endereço já resolvido, com as coordenadas do ponto. */
data class ResolvedAddress(
    val address: String,
    val latitude: Double,
    val longitude: Double,
)

/**
 * Busca de endereços com autocompletar. A implementação real usa o Google
 * Places; os testes usam [com.example.sociable.data.fake.FakeAddressSearch].
 */
interface AddressSearch {
    suspend fun suggest(query: String): AppResult<List<AddressSuggestion>>

    /** Obtém as coordenadas da sugestão escolhida (encerra a sessão de busca). */
    suspend fun resolve(suggestion: AddressSuggestion): AppResult<ResolvedAddress>
}
