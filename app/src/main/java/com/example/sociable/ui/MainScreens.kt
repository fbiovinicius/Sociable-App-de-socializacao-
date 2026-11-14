package com.example.sociable.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sociable.R
import com.example.sociable.ui.theme.Coral
import com.example.sociable.ui.theme.Cream
import com.example.sociable.ui.theme.Ink
import com.example.sociable.ui.theme.OrangeSoft
import com.example.sociable.ui.theme.Plum
import com.example.sociable.ui.theme.Rose

/* ---------- 5. Segredinhos ---------- */
@Composable
fun SecretScreen(
    events: List<EventItem>,
    joined: Set<Int>,
    onToggleJoin: (Int) -> Unit,
    onOpen: (EventItem) -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val secrets = events.filter { it.category == Category.Secret }
    Column(modifier.fillMaxSize().background(Cream).statusBarsPadding()) {
        Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp)) {
            ScreenTitle("Segredinhos ✦", "Rolês que não são divulgados por aí")
        }
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(secrets) { e ->
                val requested = e.id in joined
                StickerBox(
                    Modifier.fillMaxWidth(), fill = OrangeSoft, radius = 22.dp,
                    contentPadding = PaddingValues(16.dp), onClick = { onOpen(e) },
                ) {
                    Column {
                        Text("🔒 Secreto", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            e.title, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                        Text("${e.whenText} · ${e.place}", color = Ink, fontSize = 13.sp)
                        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("👥 ${e.spotsText}", color = Ink, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            SociableChip(
                                if (requested) "Convite enviado ✓" else "Pedir convite",
                                selected = !requested, onClick = { onToggleJoin(e.id) },
                            )
                        }
                    }
                }
            }
        }
        Box(Modifier.padding(24.dp)) { SociableButton("+ Criar evento", onClick = onCreate) }
    }
}

/* ---------- 6. Chat ---------- */
private data class Msg(val text: String, val mine: Boolean)

@Composable
fun ChatScreen(modifier: Modifier = Modifier) {
    val msgs = remember {
        mutableStateListOf(
            Msg("Oi! Vai na roda de samba hoje?", false),
            Msg("Vou sim! Bora?", true),
            Msg("Bora! Onde a gente se encontra?", false),
        )
    }
    var draft by remember { mutableStateOf("") }
    fun send(text: String) {
        if (text.isNotBlank()) { msgs.add(Msg(text.trim().capitalizeFirst(), true)); draft = "" }
    }

    Column(modifier.fillMaxSize().background(Cream).statusBarsPadding().imePadding()) {
        Row(Modifier.padding(20.dp, 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Pin(R.drawable.ic_pin_people, size = 40.dp)
            Column(Modifier.padding(start = 12.dp)) {
                Text("Roda de samba no Centro", color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("7 confirmados", color = Plum, fontSize = 12.sp)
            }
        }
        Box(Modifier.fillMaxWidth().height(2.5.dp).background(Ink))
        LazyColumn(
            Modifier.weight(1f), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(msgs) { m ->
                Box(Modifier.fillMaxWidth(), contentAlignment = if (m.mine) Alignment.CenterEnd else Alignment.CenterStart) {
                    StickerBox(
                        Modifier.fillMaxWidth(0.8f),
                        fill = if (m.mine) Coral else Rose, radius = 18.dp, shadow = 0.dp,
                        contentPadding = PaddingValues(12.dp),
                    ) {
                        Text(m.text, color = if (m.mine) Cream else Ink, fontSize = 14.sp)
                    }
                }
            }
        }
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            SociableChip("📍 Marcar local", selected = false, onClick = {
                send("📍 Shopping Ponta Negra · entrada principal · hoje 19h (local público)")
            })
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = draft, onValueChange = { draft = it },
                placeholder = { Text("Mensagem…") }, singleLine = true,
                shape = RoundedCornerShape(24.dp), modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send,
                ),
                keyboardActions = KeyboardActions(onSend = { send(draft) }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Ink, unfocusedBorderColor = Ink,
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                ),
            )
        }
    }
}

/* ---------- 7. Perfil ---------- */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(profile: UserProfile, onLogout: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().background(Cream).statusBarsPadding()
            .verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))
        Pin(R.drawable.ic_pin_party, size = 88.dp)
        Spacer(Modifier.height(12.dp))
        Text(
            profile.name.ifBlank { "Meu perfil" }, color = Ink, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
        )
        val sub = listOfNotNull(
            if (profile.age > 0) "${profile.age} anos" else null,
            profile.city.ifBlank { null },
        ).joinToString(" · ")
        if (sub.isNotEmpty()) Text(sub, color = Plum, fontSize = 14.sp)

        Spacer(Modifier.height(24.dp))
        Text(
            "Meus interesses", color = Ink, fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        )
        if (profile.interests.isEmpty()) {
            Text("Você ainda não escolheu interesses.", color = Plum, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
        } else {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                profile.interests.forEach { SociableChip(it, selected = true, onClick = {}) }
            }
        }
        Spacer(Modifier.height(32.dp))
        SociableButton("Sair", onLogout, secondary = true)
    }
}

/* ---------- 8. Detalhes do evento ---------- */
@Composable
fun EventDetailScreen(
    event: EventItem,
    joined: Boolean,
    onToggleJoin: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shown = if (joined) event.copy(confirmed = event.confirmed + 1) else event
    val secret = event.category == Category.Secret

    Column(
        modifier.fillMaxSize().background(Cream).statusBarsPadding()
            .verticalScroll(rememberScrollState()).padding(24.dp),
    ) {
        BackLink(onBack)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Pin(event.category.pin, size = 56.dp)
            Spacer(Modifier.width(12.dp))
            SociableChip(event.category.label, selected = true, onClick = {})
        }
        Spacer(Modifier.height(12.dp))
        Text(event.title, color = Ink, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(16.dp))
        StickerBox(Modifier.fillMaxWidth(), fill = Rose, radius = 20.dp, contentPadding = PaddingValues(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("📅 ${event.whenText}", color = Ink, fontSize = 14.sp)
                Text("📍 ${event.place}", color = Ink, fontSize = 14.sp)
                Text("💰 ${event.priceText}", color = Ink, fontSize = 14.sp)
                Text("🧭 ${event.distanceText} de você", color = Ink, fontSize = 14.sp)
                Text("👥 ${shown.spotsText}", color = Ink, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Sobre o rolê", color = Ink, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(event.description, color = Ink, fontSize = 14.sp)
        if (secret) {
            Spacer(Modifier.height(12.dp))
            Text("🔒 O endereço completo é revelado 2 h antes, só para quem tem convite.", color = Plum, fontSize = 13.sp)
        }
        Spacer(Modifier.height(28.dp))
        SociableButton(
            text = when {
                secret && joined -> "Convite enviado ✓ (toque para cancelar)"
                secret -> "Pedir convite"
                joined -> "Você vai! ✓ (toque para cancelar)"
                else -> "Eu vou!"
            },
            onClick = onToggleJoin,
            secondary = joined,
        )
    }
}

/* ---------- 9. Criar evento ---------- */
data class EventDraft(
    val title: String,
    val place: String,
    val whenText: String,
    val category: Category,
    val priceText: String,
    val capacity: Int?,
    val description: String,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateEventScreen(
    startAsSecret: Boolean,
    onBack: () -> Unit,
    onCreate: (EventDraft) -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var whenText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(if (startAsSecret) Category.Secret else Category.People) }
    var free by remember { mutableStateOf(true) }
    var price by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val ok = title.isNotBlank() && place.isNotBlank() && whenText.isNotBlank() && (free || price.isNotBlank())

    Column(
        modifier.fillMaxSize().background(Cream).statusBarsPadding().imePadding()
            .verticalScroll(rememberScrollState()).padding(24.dp),
    ) {
        BackLink(onBack)
        Spacer(Modifier.height(8.dp))
        ScreenTitle("Criar evento", "Conte o que vai rolar")
        SociableTextField("Nome do evento", title, { title = it })
        Spacer(Modifier.height(12.dp))
        SociableTextField(
            "Local", place, { place = it },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        )
        Spacer(Modifier.height(12.dp))
        SociableTextField("Dia e horário (ex.: Sáb · 20h)", whenText, { whenText = it })
        Spacer(Modifier.height(20.dp))
        Text("Tipo", color = Ink, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Category.values().forEach { SociableChip(it.label, category == it, { category = it }) }
        }
        Spacer(Modifier.height(20.dp))
        Text("Valor", color = Ink, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SociableChip("Gratuito", free, { free = true })
            SociableChip("Pago", !free, { free = false })
        }
        if (!free) {
            Spacer(Modifier.height(12.dp))
            SociableTextField(
                "Valor em reais", price, { price = digitsOnly(it, 4) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
        Spacer(Modifier.height(12.dp))
        SociableTextField(
            "Limite de pessoas (opcional)", capacity, { capacity = digitsOnly(it, 3) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Spacer(Modifier.height(12.dp))
        SociableTextField("Descrição", description, { description = it }, singleLine = false)
        Spacer(Modifier.height(28.dp))
        SociableButton("Criar evento", {
            onCreate(
                EventDraft(
                    title = title.trim().capitalizeFirst(),
                    place = place.trim(),
                    whenText = whenText.trim().capitalizeFirst(),
                    category = category,
                    priceText = if (free) "Gratuito" else "R$ $price",
                    capacity = capacity.toIntOrNull()?.takeIf { it > 0 },
                    description = description.trim().capitalizeFirst().ifBlank { "Sem descrição ainda." },
                ),
            )
        }, enabled = ok)
    }
}