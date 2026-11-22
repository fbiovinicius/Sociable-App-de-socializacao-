package com.example.sociable.ui.common

/**
 * Calcula a potência de dois usada pelo `BitmapFactory` para manter a imagem
 * decodificada dentro do limite de memória.
 *
 * A maior dimensão precisa ser considerada: imagens muito largas ou muito
 * altas não podem ficar sem amostragem só porque a outra dimensão é pequena.
 */
internal fun calculateAvatarSampleSize(
    width: Int,
    height: Int,
    maxDimension: Int,
): Int {
    require(width > 0 && height > 0) { "A imagem precisa ter dimensões positivas" }
    require(maxDimension > 0) { "O limite precisa ser positivo" }

    var sampleSize = 1
    while (width / sampleSize > maxDimension || height / sampleSize > maxDimension) {
        check(sampleSize <= Int.MAX_VALUE / 2) { "Dimensão de imagem não suportada" }
        sampleSize *= 2
    }
    return sampleSize
}
