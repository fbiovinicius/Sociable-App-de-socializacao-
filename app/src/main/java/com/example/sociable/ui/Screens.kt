package com.example.sociable.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sociable.R
import com.example.sociable.ui.theme.Coral
import com.example.sociable.ui.theme.SCream
import com.example.sociable.ui.theme.SInk
import com.example.sociable.ui.theme.OrangeSoft
import com.example.sociable.ui.theme.Plum

private val EmailKeyboard = KeyboardOptions(
    keyboardType = KeyboardType.Email,
    capitalization = KeyboardCapitalization.None,
)

/* ---------- 1. Boas-vindas ---------- */
@Composable
fun WelcomeScreen(onGoogle: (email: String) -> Unit, onEmail: () -> Unit) {
    var showGoogle by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(SCream).systemBarsPadding().padding(24.dp)) {
        Column(
            Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.logo_sociable),
                contentDescription = "Logo do Sociable",
                modifier = Modifier.size(220.dp).clip(CircleShape),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Descubra o que está rolando em Manaus\ne vá com gente nova, sem compromisso",
                color = SInk, fontSize = 15.sp, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            SociableChip("🟢 128 pessoas online agora", selected = false, onClick = {})
        }
        SociableButton("Continuar com o Google", { showGoogle = true }, secondary = true)
        Spacer(Modifier.height(14.dp))
        SociableButton("Entrar com e-mail", onEmail)
        Text(
            "Só para maiores de 18 anos.", color = Plum, fontSize = 12.sp,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
        )
    }

    if (showGoogle) {
        GoogleSignInDialog(
            onDismiss = { showGoogle = false },
            onConfirm = { email -> showGoogle = false; onGoogle(email) },
        )
    }
}

/**
 * Janela de login com o Google.
 * TODO: trocar pelo login real (Credential Manager + "Sign in with Google") quando o
 *       projeto tiver o ID de cliente OAuth configurado no Google Cloud.
 */
@Composable
private fun GoogleSignInDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    val valid = isValidEmail(email)

    Dialog(onDismissRequest = onDismiss) {
        StickerBox(Modifier.fillMaxWidth(), fill = SCream, radius = 24.dp, contentPadding = PaddingValues(20.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("G", color = Coral, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(10.dp))
                    Text("Entrar com o Google", color = SInk, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(Modifier.height(6.dp))
                Text("Escolha a conta Google que você quer usar no Sociable.", color = Plum, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))
                SociableTextField(
                    label = "E-mail da conta Google",
                    value = email,
                    onChange = { email = it.trim() },
                    isError = email.isNotEmpty() && !valid,
                    keyboardOptions = EmailKeyboard,
                )
                Spacer(Modifier.height(16.dp))
                SociableButton("Continuar", { onConfirm(email) }, enabled = valid)
                Spacer(Modifier.height(8.dp))
                SociableButton("Cancelar", onDismiss, secondary = true)
            }
        }
    }
}

/* ---------- 1b. Entrar com e-mail ---------- */
@Composable
fun EmailLoginScreen(onBack: () -> Unit, onLogin: (email: String) -> Unit, onCreateAccount: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val emailOk = isValidEmail(email)
    val passOk = password.length >= 6

    Column(
        Modifier.fillMaxSize().background(SCream).systemBarsPadding().imePadding()
            .verticalScroll(rememberScrollState()).padding(24.dp),
    ) {
        BackLink(onBack)
        Spacer(Modifier.height(12.dp))
        ScreenTitle("Entrar com e-mail", "Use seu e-mail e sua senha")
        SociableTextField(
            label = "E-mail",
            value = email,
            onChange = { email = it.trim() },
            isError = email.isNotEmpty() && !emailOk,
            keyboardOptions = EmailKeyboard,
            supportingText = if (email.isNotEmpty() && !emailOk) "Digite um e-mail válido" else null,
        )
        Spacer(Modifier.height(12.dp))
        SociableTextField(
            label = "Senha",
            value = password,
            onChange = { password = it },
            isError = password.isNotEmpty() && !passOk,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                capitalization = KeyboardCapitalization.None,
            ),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            supportingText = "Mínimo de 6 caracteres",
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (showPassword) "Ocultar senha" else "Mostrar senha",
            color = Plum, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { showPassword = !showPassword }.padding(vertical = 6.dp),
        )
        Spacer(Modifier.height(24.dp))
        // TODO: validar com o back-end. Por enquanto, quem entra segue para o cadastro.
        SociableButton("Entrar", { onLogin(email) }, enabled = emailOk && passOk)
        Spacer(Modifier.height(14.dp))
        SociableButton("Criar conta", onCreateAccount, secondary = true)
    }
}

/* ---------- 2. Cadastro (18+) ---------- */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SignupScreen(
    initialName: String = "",
    onNext: (name: String, age: Int, gender: String, city: String) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var birth by remember { mutableStateOf("") } // guarda só os dígitos (ddMMaaaa)
    var gender by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Manaus, AM") }

    val age = ageFromBirthDigits(birth)
    val dateInvalid = birth.length == 8 && age == null
    val underage = age != null && age < 18
    val ok = name.isNotBlank() && age != null && age >= 18 && gender.isNotEmpty() && city.isNotBlank()

    Column(
        Modifier.fillMaxSize().background(SCream).systemBarsPadding().imePadding()
            .verticalScroll(rememberScrollState()).padding(24.dp),
    ) {
        ScreenTitle("Quem é você?", "Só o básico pra começar")
        SociableTextField(
            label = "Como quer ser chamado(a)?",
            value = name,
            onChange = { name = it },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        )
        Spacer(Modifier.height(12.dp))
        SociableTextField(
            label = "Data de nascimento",
            value = birth,
            onChange = { birth = digitsOnly(it, 8) },
            isError = underage || dateInvalid,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = DateVisualTransformation(),
            supportingText = if (dateInvalid) "Essa data não existe" else "Digite só os números: dd/mm/aaaa",
        )
        Spacer(Modifier.height(14.dp))
        StickerBox(
            Modifier.fillMaxWidth(), fill = if (underage) Coral else OrangeSoft,
            radius = 14.dp, contentPadding = PaddingValues(14.dp),
        ) {
            Text(
                if (underage) "Você precisa ter 18 anos ou mais para usar o Sociable."
                else "✦ O Sociable é só para maiores de 18 anos.",
                color = if (underage) SCream else SInk, fontSize = 13.sp,
            )
        }
        Spacer(Modifier.height(20.dp))
        Text("Gênero", color = SInk, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Masculino", "Feminino", "Não-binário", "Prefiro não dizer").forEach {
                SociableChip(it, gender == it, { gender = it })
            }
        }
        Spacer(Modifier.height(16.dp))
        SociableTextField(
            label = "Bairro / cidade",
            value = city,
            onChange = { city = it },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        )
        Spacer(Modifier.height(32.dp))
        SociableButton("Próximo", { if (ok && age != null) onNext(name.trim(), age, gender, city.trim()) }, enabled = ok)
    }
}

/* ---------- 3. Gostos (máximo de 5) ---------- */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestsScreen(onNext: (List<String>) -> Unit) {
    val selected = remember { mutableStateListOf<String>() }

    Column(Modifier.fillMaxSize().background(SCream).systemBarsPadding().padding(24.dp)) {
        ScreenTitle("Do seu jeito", "Escolha até $MAX_INTERESTS · ${selected.size} de $MAX_INTERESTS")
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            interestGroups.forEach { (group, items) ->
                Text(group, color = SInk, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items.forEach { t ->
                        SociableChip(t, t in selected, {
                            if (t in selected) selected.remove(t)
                            else if (selected.size < MAX_INTERESTS) selected.add(t)
                        })
                    }
                }
            }
        }
        if (selected.size == MAX_INTERESTS) {
            Text(
                "Você chegou ao limite de $MAX_INTERESTS. Toque em um para trocar.",
                color = Plum, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        SociableButton("Próximo", { onNext(selected.toList()) }, enabled = selected.isNotEmpty())
    }
}