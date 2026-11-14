package com.example.sociable.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sociable.ui.theme.Coral
import com.example.sociable.ui.theme.SInk
import com.example.sociable.ui.theme.Plum
import com.example.sociable.ui.theme.SSand
import java.util.Calendar

/* ---------- Textos ---------- */

/** Deixa a primeira letra maiúscula (início de frase). */
fun String.capitalizeFirst(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

/** "ana.souza@gmail.com" -> "Ana Souza" */
fun nameFromEmail(email: String): String =
    email.substringBefore("@")
        .split('.', '_', '-')
        .filter { it.isNotBlank() }
        .joinToString(" ") { it.lowercase().capitalizeFirst() }

fun isValidEmail(s: String): Boolean = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(s)

/* ---------- Data de nascimento ---------- */

fun digitsOnly(s: String, max: Int): String = s.filter { it.isDigit() }.take(max)

/**
 * Recebe 8 dígitos (ddMMaaaa) e devolve a idade, ou null se a data não existir.
 * Ex.: "10122005" -> idade de quem nasceu em 10/12/2005.
 */
fun ageFromBirthDigits(d: String): Int? {
    if (d.length != 8) return null
    val day = d.substring(0, 2).toInt()
    val month = d.substring(2, 4).toInt()
    val year = d.substring(4, 8).toInt()
    val now = Calendar.getInstance()
    if (year < 1900 || year > now.get(Calendar.YEAR)) return null
    val cal = Calendar.getInstance().apply {
        isLenient = false
        clear()
        set(year, month - 1, day)
    }
    try {
        cal.time // lança exceção se a data for inválida (ex.: 31/02)
    } catch (e: IllegalArgumentException) {
        return null
    }
    var age = now.get(Calendar.YEAR) - year
    val beforeBirthday = now.get(Calendar.MONTH) < month - 1 ||
            (now.get(Calendar.MONTH) == month - 1 && now.get(Calendar.DAY_OF_MONTH) < day)
    if (beforeBirthday) age--
    return if (age < 0) null else age
}

/** Mostra "10122005" como "10/12/2005" enquanto a pessoa digita. */
class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val d = text.text.take(8)
        val out = buildString {
            d.forEachIndexed { i, c ->
                if (i == 2 || i == 4) append('/')
                append(c)
            }
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, d.length)
                return o + (if (o > 2) 1 else 0) + (if (o > 4) 1 else 0)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val t = offset.coerceIn(0, out.length)
                val o = if (t <= 2) t else if (t <= 5) t - 1 else t - 2
                return o.coerceIn(0, d.length)
            }
        }
        return TransformedText(AnnotatedString(out), mapping)
    }
}

/* ---------- Componentes ---------- */

@Composable
fun ScreenTitle(text: String, sub: String? = null) {
    Text(text, color = SInk, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
    if (sub != null) Text(sub, color = Plum, fontSize = 15.sp, modifier = Modifier.padding(bottom = 16.dp))
}

@Composable
fun BackLink(onBack: () -> Unit) {
    Text(
        "← Voltar", color = Plum, fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable(onClick = onBack).padding(vertical = 8.dp),
    )
}

/** Campo de texto do Sociable, com teclado, máscara e mensagem de apoio. */
@Composable
fun SociableTextField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    supportingText: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        isError = isError,
        singleLine = singleLine,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        supportingText = if (supportingText != null) {
            { Text(supportingText) }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Coral,
            unfocusedBorderColor = SSand,
            errorBorderColor = Coral,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            errorContainerColor = Color.White,
            focusedLabelColor = Coral,
            unfocusedLabelColor = Plum,
            errorLabelColor = Coral,
            focusedTextColor = SInk,
            unfocusedTextColor = SInk,
            errorTextColor = SInk,
        ),
    )
}