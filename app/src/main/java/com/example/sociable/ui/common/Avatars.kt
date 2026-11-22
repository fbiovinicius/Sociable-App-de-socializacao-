package com.example.sociable.ui.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sociable.data.model.Attendee
import com.example.sociable.ui.theme.SociableTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Avatar circular de participante.
 *
 * Com foto de perfil mostra a imagem; sem foto, mostra as iniciais sobre uma
 * cor derivada do nome (cada pessoa mantém sempre a mesma cor).
 */
@Composable
fun AttendeeAvatar(
    attendee: Attendee,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
) {
    val shape = CircleShape
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(attendeeColor(attendee.userId))
            .border(2.dp, SociableTheme.ink, shape),
        contentAlignment = Alignment.Center,
    ) {
        val photo = rememberRemoteImage(attendee.photoUrl)
        if (photo != null) {
            Image(
                bitmap = photo,
                contentDescription = "Foto de ${attendee.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size),
            )
        } else {
            Text(
                text = attendee.initials,
                color = SociableTheme.ink,
                fontSize = (size.value / 2.6f).sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

/**
 * Pilha de avatares com sobreposição (estilo "quem vai" das redes sociais).
 * Avatares extras viram um selo "+N".
 *
 * [totalCount] permite mostrar o total real de confirmados mesmo quando a
 * consulta traz apenas uma pequena amostra dos documentos públicos.
 */
@Composable
fun AttendeeAvatarStack(
    attendees: List<Attendee>,
    modifier: Modifier = Modifier,
    totalCount: Int? = null,
    maxVisible: Int = 4,
    size: Dp = 36.dp,
) {
    if (attendees.isEmpty()) return

    val confirmedCount = (totalCount ?: attendees.size).coerceAtLeast(0)
    val visibleLimit = maxVisible.coerceAtLeast(0).coerceAtMost(confirmedCount)
    val visible = attendees.take(visibleLimit)
    if (visible.isEmpty()) return

    val extra = (confirmedCount - visible.size).coerceAtLeast(0)
    val overlap = size * 0.72f
    val step = size - overlap
    val width = step * visible.size + overlap + if (extra > 0) step else 0.dp

    Box(modifier = modifier.size(width = width, height = size)) {
        visible.forEachIndexed { index, attendee ->
            Box(
                modifier = Modifier
                    .offset(x = step * index)
                    .size(size),
            ) {
                AttendeeAvatar(attendee = attendee, size = size)
            }
        }
        if (extra > 0) {
            Box(
                modifier = Modifier
                    .offset(x = step * visible.size)
                    .size(size)
                    .clip(CircleShape)
                    .background(SociableTheme.accent)
                    .border(2.dp, SociableTheme.ink, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+$extra",
                    color = SociableTheme.onAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

/** Paleta de avatares: cor estável por pessoa (hash do id). */
private val AVATAR_COLORS = listOf(
    Color(0xFFFFD6A5),
    Color(0xFFC7E9B0),
    Color(0xFFB5D8FF),
    Color(0xFFFFC2D1),
    Color(0xFFE3C7FF),
    Color(0xFFFFE9A8),
)

private fun attendeeColor(userId: String): Color {
    val hash = userId.fold(0) { acc, char -> acc * 31 + char.code }
    // Converte para unsigned antes do módulo: Math.abs(Int.MIN_VALUE) continua
    // negativo e poderia gerar um índice inválido na paleta.
    val index = (hash.toLong() and 0xFFFFFFFFL) % AVATAR_COLORS.size
    return AVATAR_COLORS[index.toInt()]
}

/**
 * Carrega uma imagem remota (foto de perfil) sem bloquear a interface.
 * Usa um cache em memória simples: a prévia do mapa recarrega as mesmas
 * fotos várias vezes conforme os cartões são reconstruídos.
 */
@Composable
private fun rememberRemoteImage(url: String?): ImageBitmap? {
    var image by remember(url) { mutableStateOf(RemoteImageCache.get(url)) }
    LaunchedEffect(url) {
        if (url.isNullOrBlank()) {
            image = null
            return@LaunchedEffect
        }
        val cached = RemoteImageCache.get(url)
        if (cached != null) {
            image = cached
            return@LaunchedEffect
        }
        val loaded = withContext(Dispatchers.IO) {
            runCatching { downloadAndDecodeAvatar(url) }.getOrNull()
        }
        if (loaded != null) RemoteImageCache.put(url, loaded)
        image = loaded
    }
    return image
}

private const val MAX_REMOTE_IMAGE_BYTES = 2 * 1024 * 1024
private const val MAX_DECODED_IMAGE_SIZE = 256
private const val IMAGE_CONNECT_TIMEOUT_MS = 10_000
private const val IMAGE_READ_TIMEOUT_MS = 15_000
private const val MAX_IMAGE_REDIRECTS = 3

/**
 * Baixa somente HTTPS, com timeout e limite de bytes. Redirecionamentos são
 * seguidos manualmente para impedir que uma URL HTTPS caia em HTTP ou em um
 * host local durante o download.
 */
private fun downloadAndDecodeAvatar(rawUrl: String): ImageBitmap? {
    var currentUrl = runCatching { URL(rawUrl) }.getOrNull() ?: return null
    val bytes = downloadImageBytes(currentUrl) ?: return null
    return decodeSampledBitmap(bytes)
}

private fun downloadImageBytes(initialUrl: URL): ByteArray? {
    var currentUrl = initialUrl
    for (attempt in 0..MAX_IMAGE_REDIRECTS) {
        if (!currentUrl.protocol.equals("https", ignoreCase = true)) return null
        val connection = currentUrl.openConnection() as? HttpURLConnection ?: return null
        connection.instanceFollowRedirects = false
        connection.connectTimeout = IMAGE_CONNECT_TIMEOUT_MS
        connection.readTimeout = IMAGE_READ_TIMEOUT_MS
        connection.setRequestProperty("Accept", "image/*")

        try {
            val status = connection.responseCode
            if (status in 300..399) {
                val location = connection.getHeaderField("Location") ?: return null
                currentUrl = URL(currentUrl, location)
                continue
            }
            if (status !in 200..299) return null
            return connection.inputStream.use { it.readAtMost(MAX_REMOTE_IMAGE_BYTES) }
        } finally {
            connection.disconnect()
        }
    }
    return null
}

/** Lê no máximo [limit] bytes; corpo maior é descartado sem ficar em memória. */
private fun InputStream.readAtMost(limit: Int): ByteArray? {
    val output = ByteArrayOutputStream(minOf(limit, 8 * 1024))
    val buffer = ByteArray(8 * 1024)
    var total = 0
    while (true) {
        val read = read(buffer)
        if (read < 0) break
        total += read
        if (total > limit) return null
        output.write(buffer, 0, read)
    }
    return output.toByteArray()
}

/** Decodifica com amostragem para não reserva memória para imagens gigantes. */
private fun decodeSampledBitmap(bytes: ByteArray): ImageBitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (
        bounds.outWidth / (sampleSize * 2) >= MAX_DECODED_IMAGE_SIZE &&
        bounds.outHeight / (sampleSize * 2) >= MAX_DECODED_IMAGE_SIZE
    ) {
        sampleSize *= 2
    }
    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)?.asImageBitmap()
}

private object RemoteImageCache {

    private const val MAX_ENTRIES = 24
    private val entries = object : LinkedHashMap<String, ImageBitmap>(MAX_ENTRIES, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ImageBitmap>?): Boolean =
            size > MAX_ENTRIES
    }

    @Synchronized
    fun get(url: String?): ImageBitmap? {
        if (url.isNullOrBlank()) return null
        return entries[url]
    }

    @Synchronized
    fun put(url: String, image: ImageBitmap) {
        entries[url] = image
    }
}
