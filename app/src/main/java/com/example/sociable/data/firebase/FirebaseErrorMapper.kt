package com.example.sociable.data.firebase

import com.example.sociable.core.AppError
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

internal fun Throwable.toFirebaseAppError(): AppError = when (this) {
    is FirebaseAuthUserCollisionException -> AppError.EMAIL_ALREADY_IN_USE
    is FirebaseAuthWeakPasswordException -> AppError.WEAK_PASSWORD
    is FirebaseAuthInvalidUserException,
    is FirebaseAuthInvalidCredentialsException -> AppError.INVALID_CREDENTIALS
    is FirebaseFirestoreException -> when (code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> AppError.PERMISSION_DENIED
        FirebaseFirestoreException.Code.NOT_FOUND -> AppError.NOT_FOUND
        FirebaseFirestoreException.Code.UNAVAILABLE,
        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> AppError.NETWORK
        else -> AppError.UNKNOWN
    }
    is UnknownHostException, is SocketTimeoutException, is IOException -> AppError.NETWORK
    else -> AppError.UNKNOWN
}
