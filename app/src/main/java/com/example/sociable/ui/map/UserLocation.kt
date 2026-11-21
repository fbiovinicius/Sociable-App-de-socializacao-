package com.example.sociable.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/** Centro de Manaus, usado quando não há permissão ou posição do aparelho. */
internal val MANAUS_CENTER = LatLng(-3.12, -60.01)
internal const val DEFAULT_ZOOM = 12f
internal const val USER_ZOOM = 15f

private const val LOCATION_TIMEOUT_MS = 8_000L

internal fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

/**
 * Posição atual do aparelho, ou `null` se não houver permissão, o GPS estiver
 * desligado ou a leitura demorar demais. A posição fica só no aparelho: não é
 * gravada em nenhum lugar.
 */
@SuppressLint("MissingPermission")
internal suspend fun Context.currentLatLngOrNull(): LatLng? {
    if (!hasLocationPermission()) return null
    val client = LocationServices.getFusedLocationProviderClient(this)
    return try {
        val tokenSource = CancellationTokenSource()
        val location = withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
                .await()
        } ?: run {
            tokenSource.cancel()
            client.lastLocation.await()
        }
        location?.let { LatLng(it.latitude, it.longitude) }
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: Exception) {
        null
    }
}
