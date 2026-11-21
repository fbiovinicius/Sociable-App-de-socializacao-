package com.example.sociable.ui.common

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.time.LocalDate

/** Máscara DD/MM/AAAA: o campo guarda só os 8 dígitos e a tela mostra as barras. */
object DateMask : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(8)
        val formatted = buildString {
            digits.forEachIndexed { index, char ->
                if (index == 2 || index == 4) append('/')
                append(char)
            }
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val shifted = when {
                    offset <= 1 -> offset
                    offset <= 3 -> offset + 1
                    else -> offset + 2
                }
                return shifted.coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val original = when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    else -> offset - 2
                }
                return original.coerceIn(0, digits.length)
            }
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}

/** Converte 8 dígitos (DDMMAAAA) em data; devolve null se não existir no calendário. */
fun parseBirthDate(digits: String): LocalDate? {
    if (digits.length != 8) return null
    val day = digits.substring(0, 2).toIntOrNull() ?: return null
    val month = digits.substring(2, 4).toIntOrNull() ?: return null
    val year = digits.substring(4, 8).toIntOrNull() ?: return null
    return runCatching { LocalDate.of(year, month, day) }.getOrNull()
}

/** Tem 18 anos completos (ou mais) em [today]? */
fun isAdult(birth: LocalDate, today: LocalDate = LocalDate.now()): Boolean =
    !birth.plusYears(18).isAfter(today)