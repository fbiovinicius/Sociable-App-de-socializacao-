package com.example.sociable.data.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.data.repository.PhotoKind
import com.example.sociable.data.repository.PhotoUploader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

/**
 * Reduz a imagem escolhida e envia ao Firebase Storage.
 *
 * Caminhos (as regras em `storage.rules` exigem que a pasta seja do próprio uid):
 * - perfil: `profile/{uid}/photo.jpg` (a nova foto substitui a anterior);
 * - evento: `events/{uid}/{uuid}.jpg` (só quem está logado grava na própria pasta;
 *   a capa só passa a valer no evento pela edição, que já é exclusiva do organizador).
 */
class FirebaseStoragePhotoUploader(
    private val context: Context,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
) : PhotoUploader {

    override suspend fun upload(kind: PhotoKind, localUri: String): AppResult<String> {
        val uid = auth.currentUser?.uid ?: return AppResult.Failure(AppError.UNAUTHENTICATED)
        return try {
            val bytes = withContext(Dispatchers.IO) {
                compress(Uri.parse(localUri), maxSide(kind))
            } ?: return AppResult.Failure(AppError.INVALID_INPUT)

            val path = when (kind) {
                PhotoKind.PROFILE -> "profile/$uid/photo.jpg"
                PhotoKind.EVENT -> "events/$uid/${UUID.randomUUID()}.jpg"
            }
            val reference = storage.reference.child(path)
            reference.putBytes(bytes, StorageMetadata.Builder().setContentType("image/jpeg").build()).await()
            val url = reference.downloadUrl.await().toString()
            AppResult.Success(url)
        } catch (exception: Exception) {
            AppResult.Failure(exception.toFirebaseAppError(), exception)
        }
    }

    private fun maxSide(kind: PhotoKind) = when (kind) {
        PhotoKind.PROFILE -> 512
        PhotoKind.EVENT -> 1280
    }

    /** Lê com amostragem (sem estourar memória), reduz ao [maxSide] e comprime em JPEG. */
    private fun compress(uri: Uri, maxSide: Int): ByteArray? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return null
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxSide && bounds.outHeight / (sample * 2) >= maxSide) {
            sample *= 2
        }
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null

        val scale = maxSide.toFloat() / maxOf(decoded.width, decoded.height)
        val bitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                decoded,
                (decoded.width * scale).toInt().coerceAtLeast(1),
                (decoded.height * scale).toInt().coerceAtLeast(1),
                true,
            )
        } else {
            decoded
        }
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
        return output.toByteArray()
    }
}
