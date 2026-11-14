package com.example.sociable.feature.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapProjectionTest {

    @Test
    fun `centro da caixa de Manaus vira centro do canvas`() {
        val centerLat = (ManausMapProjection.MIN_LATITUDE + ManausMapProjection.MAX_LATITUDE) / 2
        val centerLon = (ManausMapProjection.MIN_LONGITUDE + ManausMapProjection.MAX_LONGITUDE) / 2

        val point = ManausMapProjection.project(centerLat, centerLon)

        assertEquals(0.5f, point.x, 0.001f)
        assertEquals(0.5f, point.y, 0.001f)
    }

    @Test
    fun `norte fica acima e oeste fica a esquerda`() {
        val north = ManausMapProjection.project(-3.05, -60.02)
        val south = ManausMapProjection.project(-3.19, -60.02)
        val west = ManausMapProjection.project(-3.12, -60.11)
        val east = ManausMapProjection.project(-3.12, -59.91)

        assertTrue(north.y < south.y)
        assertTrue(west.x < east.x)
    }

    @Test
    fun `coordenadas fora da caixa sao limitadas a area do canvas`() {
        val point = ManausMapProjection.project(-10.0, -70.0)

        assertTrue(point.x in 0f..1f)
        assertTrue(point.y in 0f..1f)
    }
}
