package com.example.sociable.ui.common

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattingDistanceTest {

    @Test
    fun `menos de 100 metros`() {
        assertEquals("menos de 100 m", formatDistance(0.04))
    }

    @Test
    fun `abaixo de 1 km arredonda de 50 em 50 metros`() {
        assertEquals("350 m", formatDistance(0.36))
        assertEquals("950 m", formatDistance(0.96))
    }

    @Test
    fun `ate 10 km mostra uma casa com virgula`() {
        assertEquals("2,3 km", formatDistance(2.34))
        assertEquals("1,0 km", formatDistance(1.0))
    }

    @Test
    fun `acima de 10 km mostra inteiro`() {
        assertEquals("27 km", formatDistance(27.4))
        assertEquals("10 km", formatDistance(10.2))
    }
}
