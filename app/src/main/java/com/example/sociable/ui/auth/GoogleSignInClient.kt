package com.example.sociable.ui.auth

import android.annotation.SuppressLint
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CancellationException

/** Resultado de pedir o token de ID do Google ao Credential Manager. */
sealed interface GoogleTokenResult {
    data class Token(val idToken: String) : GoogleTokenResult

    /** A pessoa fechou a janela do Google: não é erro. */
    data object Cancelled : GoogleTokenResult

    /** Sem conta Google no aparelho, falha do serviço ou login com Google não configurado. */
    data object Unavailable : GoogleTokenResult
}

/**
 * Abre a janela "Entrar com o Google" e devolve o token de ID, que o
 * [com.example.sociable.data.repository.AuthRepository] troca por uma sessão do Firebase.
 *
 * [context] precisa ser a Activity (a janela do Google é desenhada sobre ela).
 */
suspend fun requestGoogleIdToken(context: Context): GoogleTokenResult {
    val webClientId = webClientId(context) ?: return GoogleTokenResult.Unavailable
    return try {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(webClientId).build())
            .build()
        val credential = CredentialManager.create(context).getCredential(context, request).credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            GoogleTokenResult.Token(GoogleIdTokenCredential.createFrom(credential.data).idToken)
        } else {
            GoogleTokenResult.Unavailable
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: GetCredentialCancellationException) {
        GoogleTokenResult.Cancelled
    } catch (_: GetCredentialException) {
        GoogleTokenResult.Unavailable
    } catch (_: GoogleIdTokenParsingException) {
        GoogleTokenResult.Unavailable
    }
}

/**
 * `default_web_client_id` é gerado pelo plugin do google-services só depois que o login com
 * Google é ativado no Firebase e o `google-services.json` é baixado de novo. Lendo em tempo de
 * execução, o app compila e roda mesmo antes disso (o botão apenas mostra um aviso).
 */
@SuppressLint("DiscouragedApi")
private fun webClientId(context: Context): String? {
    val id = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    if (id == 0) return null
    return context.getString(id).takeIf { it.isNotBlank() }
}
