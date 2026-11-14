package com.example.sociable.ui.common

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.sociable.core.validation.ValidationError

/** Atalho para os formulários: traduz o erro do domínio para o texto de apoio. */
@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: ValidationError? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    isPassword: Boolean = false,
) {
    SociableTextField(
        label = label,
        value = value,
        onChange = onValueChange,
        modifier = modifier,
        isError = error != null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        singleLine = singleLine,
        minLines = minLines,
        supportingText = error?.message(),
    )
}
