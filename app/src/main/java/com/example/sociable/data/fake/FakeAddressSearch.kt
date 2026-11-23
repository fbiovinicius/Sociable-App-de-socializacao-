package com.example.sociable.data.fake

import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.data.places.AddressSearch
import com.example.sociable.data.places.AddressSuggestion
import com.example.sociable.data.places.ResolvedAddress

/** Busca de endereço em memória, para testes. Registra as consultas recebidas. */
class FakeAddressSearch(
    var suggestions: List<AddressSuggestion> = emptyList(),
    var resolved: ResolvedAddress = ResolvedAddress("Endereço", -3.1, -60.0),
    var failure: AppError? = null,
) : AddressSearch {

    val queries = mutableListOf<String>()

    override suspend fun suggest(query: String): AppResult<List<AddressSuggestion>> {
        queries += query
        return failure?.let { AppResult.Failure(it) } ?: AppResult.Success(suggestions)
    }

    override suspend fun resolve(suggestion: AddressSuggestion): AppResult<ResolvedAddress> =
        failure?.let { AppResult.Failure(it) } ?: AppResult.Success(resolved)
}
