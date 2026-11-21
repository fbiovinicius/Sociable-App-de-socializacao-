package com.example.sociable.ui.events

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Endereço aproximado de um ponto, usando o Geocoder do próprio Android
 * (sem chave de API). Pode falhar sem internet: nesse caso devolve `null` e a
 * pessoa digita o endereço.
 */
internal suspend fun Context.addressFor(latitude: Double, longitude: Double): String? {
    if (!Geocoder.isPresent()) return null
    val geocoder = Geocoder(this, Locale("pt", "BR"))
    return try {
        val addresses: List<Address>? = if (Build.VERSION.SDK_INT >= 33) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1,
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            continuation.resume(addresses)
                        }

                        override fun onError(errorMessage: String?) {
                            continuation.resume(null)
                        }
                    },
                )
            }
        } else {
            withContext(Dispatchers.IO) {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)
            }
        }
        addresses?.firstOrNull()?.getAddressLine(0)?.takeIf { it.isNotBlank() }
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: Exception) {
        null
    }
}
