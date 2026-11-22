package com.example.sociable.core.location

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_KM = 6371.0088

/**
 * Distância em linha reta entre dois pontos, em quilômetros (fórmula de haversine).
 * É a distância "em voo de pássaro": não considera ruas nem trânsito.
 */
fun distanceKm(
    fromLatitude: Double,
    fromLongitude: Double,
    toLatitude: Double,
    toLongitude: Double,
): Double {
    val lat1 = Math.toRadians(fromLatitude)
    val lat2 = Math.toRadians(toLatitude)
    val deltaLat = lat2 - lat1
    val deltaLon = Math.toRadians(toLongitude - fromLongitude)

    val a = sin(deltaLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(deltaLon / 2).pow(2)
    return 2 * EARTH_RADIUS_KM * asin(sqrt(a.coerceIn(0.0, 1.0)))
}
