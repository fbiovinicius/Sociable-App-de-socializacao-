package com.example.sociable.data.places

import android.content.Context
import android.content.pm.PackageManager
import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import java.io.IOException

/**
 * Autocompletar de endereço com o Google Places (Places API (New)).
 *
 * A chave é a mesma do mapa, lida do `meta-data com.google.android.geo.API_KEY`
 * do manifest. A busca é enviesada para Manaus e limitada ao Brasil. Todas as
 * sugestões de uma digitação compartilham um *session token*, que termina quando
 * a pessoa escolhe uma sugestão ([resolve]): é isso que o Google cobra como uma
 * única sessão.
 */
class GooglePlacesAddressSearch(
    private val context: Context,
) : AddressSearch {

    private var client: PlacesClient? = null
    private var sessionToken: AutocompleteSessionToken? = null

    override suspend fun suggest(query: String): AppResult<List<AddressSuggestion>> {
        val places = placesClient() ?: return AppResult.Failure(AppError.UNKNOWN)
        val token = sessionToken ?: AutocompleteSessionToken.newInstance().also { sessionToken = it }
        return try {
            val center = LatLng(MANAUS_LATITUDE, MANAUS_LONGITUDE)
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setCountries(listOf("BR"))
                .setOrigin(center)
                .setLocationBias(CircularBounds.newInstance(center, BIAS_RADIUS_METERS))
                .setSessionToken(token)
                .build()
            val response = places.findAutocompletePredictions(request).await()
            AppResult.Success(
                response.autocompletePredictions.map { prediction ->
                    AddressSuggestion(
                        placeId = prediction.placeId,
                        primaryText = prediction.getPrimaryText(null).toString(),
                        secondaryText = prediction.getSecondaryText(null).toString(),
                        fullText = prediction.getFullText(null).toString(),
                    )
                }
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Failure(exception.toAddressError(), exception)
        }
    }

    override suspend fun resolve(suggestion: AddressSuggestion): AppResult<ResolvedAddress> {
        val places = placesClient() ?: return AppResult.Failure(AppError.UNKNOWN)
        return try {
            val request = FetchPlaceRequest.builder(
                suggestion.placeId,
                listOf(Place.Field.LOCATION, Place.Field.FORMATTED_ADDRESS),
            )
                .setSessionToken(sessionToken)
                .build()
            val place = places.fetchPlace(request).await().place
            // A sessão termina aqui: a próxima digitação abre outra.
            sessionToken = null
            val location = place.location
                ?: return AppResult.Failure(AppError.NOT_FOUND)
            AppResult.Success(
                ResolvedAddress(
                    address = place.formattedAddress ?: suggestion.fullText,
                    latitude = location.latitude,
                    longitude = location.longitude,
                )
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Failure(exception.toAddressError(), exception)
        }
    }

    /** Inicializa o SDK na primeira busca. Sem chave configurada, não há sugestões. */
    private fun placesClient(): PlacesClient? {
        client?.let { return it }
        val apiKey = readApiKey()
        if (apiKey.isBlank()) return null
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(context.applicationContext, apiKey)
        }
        return Places.createClient(context.applicationContext).also { client = it }
    }

    @Suppress("DEPRECATION")
    private fun readApiKey(): String = try {
        val info = context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        info.metaData?.getString("com.google.android.geo.API_KEY").orEmpty()
    } catch (_: PackageManager.NameNotFoundException) {
        ""
    }

    private fun Exception.toAddressError(): AppError = when {
        this is IOException -> AppError.NETWORK
        this is ApiException && statusCode == CommonStatusCodes.NETWORK_ERROR -> AppError.NETWORK
        else -> AppError.UNKNOWN
    }

    private companion object {
        const val MANAUS_LATITUDE = -3.1190
        const val MANAUS_LONGITUDE = -60.0217
        const val BIAS_RADIUS_METERS = 50_000.0
    }
}
