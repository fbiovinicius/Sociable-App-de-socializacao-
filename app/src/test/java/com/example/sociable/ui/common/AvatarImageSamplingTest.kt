package com.example.sociable.ui.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AvatarImageSamplingTest {

    @Test
    fun `imagem muito larga tambem e amostrada`() {
        val sampleSize = calculateAvatarSampleSize(
            width = 10_000,
            height = 100,
            maxDimension = 256,
        )

        assertEquals(64, sampleSize)
        assertTrue(10_000 / sampleSize <= 256)
    }

    @Test
    fun `amostragem considera a maior dimensao`() {
        val sampleSize = calculateAvatarSampleSize(
            width = 1_000,
            height = 1_000,
            maxDimension = 256,
        )

        assertEquals(4, sampleSize)
        assertTrue(1_000 / sampleSize <= 256)
    }

    @Test
    fun `imagem dentro do limite nao e reduzida`() {
        assertEquals(
            1,
            calculateAvatarSampleSize(
                width = 256,
                height = 128,
                maxDimension = 256,
            ),
        )
    }
}
