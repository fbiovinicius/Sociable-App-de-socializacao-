package com.example.sociable.core.location

import org.junit.Assert.assertEquals
import org.junit.Test

class GeoDistanceTest {

    @Test
    fun `mesmo ponto tem distancia zero`() {
        assertEquals(0.0, distanceKm(-3.119, -60.0217, -3.119, -60.0217), 0.0001)
    }

    @Test
    fun `um grau de latitude tem cerca de 111 km`() {
        assertEquals(111.19, distanceKm(0.0, 0.0, 1.0, 0.0), 0.1)
    }

    @Test
    fun `um grau de longitude no equador tem cerca de 111 km`() {
        assertEquals(111.19, distanceKm(0.0, 0.0, 0.0, 1.0), 0.1)
    }

    @Test
    fun `distancia e simetrica`() {
        val ida = distanceKm(-3.119, -60.0217, -3.0830, -60.1054)
        val volta = distanceKm(-3.0830, -60.1054, -3.119, -60.0217)

        assertEquals(ida, volta, 0.0001)
    }

    @Test
    fun `centro de Manaus ate Ponta Negra fica entre 9 e 11 km`() {
        val km = distanceKm(-3.1190, -60.0217, -3.0830, -60.1054)

        assertEquals(10.0, km, 1.0)
    }
}
