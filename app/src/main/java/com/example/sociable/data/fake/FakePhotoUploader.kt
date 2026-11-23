package com.example.sociable.data.fake

import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.data.repository.PhotoKind
import com.example.sociable.data.repository.PhotoUploader

/** Envio de foto em memória para testes: devolve uma URL HTTPS previsível. */
class FakePhotoUploader(var failWith: AppError? = null) : PhotoUploader {

    val uploads = mutableListOf<Pair<PhotoKind, String>>()

    override suspend fun upload(kind: PhotoKind, localUri: String): AppResult<String> {
        failWith?.let { return AppResult.Failure(it) }
        uploads += kind to localUri
        return AppResult.Success("https://fake.test/${kind.name.lowercase()}/${uploads.size}.jpg")
    }
}
