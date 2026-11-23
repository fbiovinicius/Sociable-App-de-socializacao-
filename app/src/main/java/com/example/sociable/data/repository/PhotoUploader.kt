package com.example.sociable.data.repository

import com.example.sociable.core.AppResult

/** Para onde a foto vai: define o tamanho máximo e a pasta no armazenamento. */
enum class PhotoKind {
    /** Foto de perfil (quadrada na tela, reduzida a 512 px). */
    PROFILE,

    /** Capa de evento (reduzida a 1280 px no maior lado). */
    EVENT,
}

/**
 * Envia uma imagem escolhida na galeria do celular e devolve a URL HTTPS pública
 * dela, que é o que o app grava em `photoUrl` / `imageUrl`.
 */
interface PhotoUploader {

    /** [localUri] é o `content://` devolvido pelo seletor de fotos do Android. */
    suspend fun upload(kind: PhotoKind, localUri: String): AppResult<String>
}
