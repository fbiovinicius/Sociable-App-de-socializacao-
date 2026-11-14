package com.example.sociable.feature.map

/**
 * Converte latitude/longitude em coordenadas normalizadas (0..1) para o mapa
 * ilustrativo em Canvas. A tela do mapa agora usa o Google Maps, então esta classe
 * não é mais usada por ela, mas continua útil para miniaturas e testes.
 */
data class MapPoint(val x: Float, val y: Float)

object ManausMapProjection {

    // Caixa aproximada de Manaus usada pelo mapa ilustrativo.
    const val MIN_LATITUDE = -3.22
    const val MAX_LATITUDE = -3.02
    const val MIN_LONGITUDE = -60.12
    const val MAX_LONGITUDE = -59.90

    fun project(latitude: Double, longitude: Double): MapPoint {
        val rawX = (longitude - MIN_LONGITUDE) / (MAX_LONGITUDE - MIN_LONGITUDE)
        val rawY = (MAX_LATITUDE - latitude) / (MAX_LATITUDE - MIN_LATITUDE)
        return MapPoint(
            x = rawX.coerceIn(0.04, 0.96).toFloat(),
            y = rawY.coerceIn(0.08, 0.92).toFloat(),
        )
    }
}
