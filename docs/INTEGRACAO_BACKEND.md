# Integração com backend e UI

Este documento é o guia para os parceiros de equipe conectarem o **Firebase** e a **UI definitiva**
sem alterar a camada de lógica já implementada.

## Visão geral

O app segue MVVM em camadas, sem dependência de Android/Firebase na lógica:

```
core/                 -> AppResult, AppError, validações puras, tempo injetável
data/model/           -> modelos de domínio (User, Event, Message, Review...)
data/repository/      -> CONTRATOS que o backend deve implementar
data/fake/            -> implementação em memória usada enquanto não há Firebase
feature/<área>/       -> ViewModels + UiState (regras de tela)
ui/<área>/            -> telas Compose com o visual do app (consomem UiState)
ui/common/            -> design system (StickerBox, botões, chips, campos, formatação)
ui/theme/             -> paleta e MaterialTheme
navigation/           -> rotas, NavHost e bottom tabs
di/AppModule.kt       -> grafo Koin (ÚNICO ponto de troca fake -> Firebase)
```

Regras importantes:

- A UI **nunca** acessa repositório: sempre conversa com o ViewModel via `UiState` + métodos `on...`.
- A lógica **nunca** importa classes do Firebase. Toda falha vira `AppResult.Failure(AppError)`.
- Mensagens em português ficam na UI (`ui/common/ErrorMessages.kt`), não nos dados.

## Ponto de troca: Koin

Abra `app/src/main/java/com/example/sociable/di/AppModule.kt`. Cada binding marcado com `FAKE`
deve ser substituído pela implementação real:

```kotlin
// Antes
single<EventRepository> { FakeEventRepository(get(), get()) }

// Depois
single<EventRepository> { FirestoreEventRepository(get(), get(), get()) }
```

Nenhum ViewModel ou tela precisa mudar. Mantenha os fakes e o `FakeDatabase` para os testes unitários.

## Contratos que o backend precisa cumprir

### AuthRepository (`data/repository/AuthRepository.kt`)

- `currentUser: StateFlow<User?>` → `FirebaseAuth.currentUser` + listener do documento `users/{uid}`.
- `signIn(email, password)` → `signInWithEmailAndPassword`, carregar usuário do Firestore.
- `signUp(name, email, password)` → `createUserWithEmailAndPassword` + `updateProfile(displayName)`
  + criar `users/{uid}` com `createdAt`.
- `signOut()` → `FirebaseAuth.signOut()`.
- `sendPasswordReset(email)` → `sendPasswordResetEmail`.

Mapeamento sugerido de exceções:

| Exceção Firebase | AppError |
|---|---|
| `FirebaseAuthInvalidUserException` / `FirebaseAuthInvalidCredentialsException` | `INVALID_CREDENTIALS` |
| `FirebaseAuthUserCollisionException` | `EMAIL_ALREADY_IN_USE` |
| `FirebaseAuthWeakPasswordException` | `WEAK_PASSWORD` |
| falha de rede (`IOException`, timeout) | `NETWORK` |
| demais | `UNKNOWN` |

### UserRepository

- `observeUser(userId)` → listener em `users/{userId}`.
- `updateProfile(...)` → atualizar nome/bio/foto/interesses (e `displayName` no Auth, se mudar).
- `updateSocialProfile(userId, lifeStage, lookingFor, socialVibe)` → gravar apenas as respostas do
  onboarding social, preservando os demais campos do documento.

### EventRepository

- `observeEvents(filter)` → listener na collection `events`; aplicar filtros (`query`, `categories`,
  `dateFilter`, `onlyFree`, `onlySecret`); ordenar por `startAt`.
- `observeEvent(eventId)` / `getEvent(eventId)` → documento do evento.
- `createEvent(draft)` → `add()` com `createdAt`, `goingCount = 0` e `isSecret = draft.isSecret`.
- **Segredinhos:** eventos com `isSecret = true` não aparecem no mapa (o `MapViewModel` filtra) e
  devem ocultar `venueName`/`address` de quem não tem RSVP. Veja a observação nas regras de segurança.
- `setRsvp(eventId, userId, going)` → transação:
  1. ler `events/{eventId}`;
  2. ler `events/{eventId}/rsvps/{userId}` e `attendees/{userId}`;
  3. ler `users/{userId}` para montar a prévia pública;
  4. se `going` e `goingCount >= capacity`, devolver `AppError.EVENT_FULL`;
  5. criar/remover RSVP, participante público e `goingCount` na mesma transação.
- `observeRsvp(eventId, userId)` → combinar evento (capacidade/contador) com o documento de RSVP.
- `observeAttendees(eventId)` → listener em `attendees`, limitado à amostra usada pela prévia do mapa.

### ReportRepository

- `reportEvent(eventId, reporterId, reason, comment)` → cria um documento em
  `events/{eventId}/reports/{reporterId}` com `createdAt` do servidor. O id é determinístico,
  então reenvios são idempotentes. O cliente não consegue listar, alterar ou apagar denúncias;
  a moderação usa o Admin SDK.

### ChatRepository

- `observeMessages(eventId)` → `events/{eventId}/messages` ordenado por `sentAt`, `limitToLast(200)`. Só organizador e quem confirmou presença leem e enviam (regras).
- `sendMessage(...)` → `add()` com `FieldValue.serverTimestamp()`.

### ReviewRepository

- `observeReviews(eventId)` → `events/{eventId}/reviews` ordenado por `createdAt`.
- `observeRating(eventId)` → média/quantidade (pode ser derivada das reviews no MVP).
- `submitReview(...)` → documento `events/{eventId}/reviews/{authorId}` (id do documento = uid) para
  garantir **1 avaliação por usuário**; as regras só permitem criar, nunca alterar. O `id` de domínio
  continua `"${eventId}_${authorId}"`. Só quem confirmou presença, e não organiza o evento, avalia.
  A média é calculada no cliente (últimas 100 avaliações); uma Cloud Function para
  `ratingAvg`/`ratingCount` no evento fica para o plano Blaze.

## Estrutura sugerida no Firestore

```
users/{userId}
  name, email, bio, photoUrl, interests: [String],
  lifeStage: String?, lookingFor: [String], socialVibe: String?, createdAt

events/{eventId}
  title, description, category, venueName, address, latitude, longitude,
  startAt, endAt, isFree, price, capacity, imageUrl, isSecret,
  organizerId, organizerName, goingCount, ratingAvg, ratingCount, createdAt

events/{eventId}/private/location   # só eventos isSecret = true
  venueName, address

events/{eventId}/rsvps/{userId}
  createdAt

events/{eventId}/attendees/{userId}   # prévia pública de quem confirmou presença
  userId, name, photoUrl, createdAt

events/{eventId}/messages/{messageId}
  authorId, authorName, text, sentAt

events/{eventId}/reviews/{userId}
  rating, comment, authorName, createdAt

events/{eventId}/reports/{userId}      # UMA denúncia por pessoa/evento
  eventId, reporterId, reason, comment, createdAt
```

Sugestão de índices: `events` por `startAt`; combinados categoria + `startAt`; `isFree` + `startAt`;
`attendees` por `userId` + `__name__` para a sincronização paginada do perfil. Para busca textual e
"perto de mim", usar respectivamente Algolia/Typesense e geohash (`geofire-common`).

## Regras de segurança (ponto de partida)

> As regras em vigor estão em `firestore.rules` na raiz do projeto; o bloco abaixo é a referência
> inicial que originou aquelas regras.

```js
match /users/{userId} {
  allow read: if request.auth != null;
  allow write: if request.auth.uid == userId;
}

match /events/{eventId} {
  allow read: if true;
  allow create: if request.auth != null;
  allow update: if request.auth != null; // restrinja campos em produção

  match /rsvps/{userId} {
    allow read: if request.auth != null;
    allow write: if request.auth.uid == userId;
  }

  match /messages/{messageId} {
    allow read, create: if request.auth != null;
  }

  match /reviews/{userId} {
    allow read: if true;
    allow create, update: if request.auth.uid == userId;
  }
}
```

> **Segredinhos:** as regras do Firestore não escondem campos de um documento. Para eventos
> `isSecret = true`, mantenha `venueName`/`address` genéricos em `events/{eventId}` e guarde o
> endereço real em `events/{eventId}/private/location`, liberando a leitura só para quem tem
> `events/{eventId}/rsvps/{uid}`. O repositório real decide quando revelar o local (ex.: 2 h antes).

## UI atual

A UI já está integrada aos ViewModels (não é mais placeholder). Estrutura:

- **Bottom tabs:** Descobrir (`EVENTS`), Mapa (`MAP`), Segredinhos (`SECRET`) e Perfil (`PROFILE`).
- **Chat** não é uma aba global: é aberto pelo detalhe do evento (`event/{eventId}/chat`).
- **Avaliações** também ficam no detalhe (`event/{eventId}/reviews`).
- **Criação** aceita `?secret=true` para já nascer como segredinho.
- **Onboarding social** aparece logo após o cadastro (`onboarding`), em 3 passos; em seguida vem
  a tela de interesses (`interests`). As respostas podem ser editadas no Perfil → "Sobre você".

Para manter o padrão ao evoluir as telas:

1. Colete estado sempre com `collectAsStateWithLifecycle()`.
2. Use `UiState` para renderizar loading/erro/vazio e chame apenas os métodos `on...` do ViewModel.
3. Reaproveite `ui/common/` (`StickerBox`, `SociableButton`, `SociableChip`, `SociableTextField`,
   `Pin`) para manter o visual consistente.
4. Rotas que recebem `eventId`: `event/{eventId}`, `event/{eventId}/chat`, `event/{eventId}/reviews`.

O mapa usa o Google Maps Compose. `MapViewModel` entrega `markers` com categoria, preço,
confirmados, capacidade e `isHappeningNow`, e a tela consome as coordenadas reais dos eventos.
Eventos secretos são excluídos dos markers para não revelar o local.

## Google Sign-In (pós-MVP)

Sem Google Sign-In no MVP: a tela de boas-vindas oferece apenas entrar com e-mail ou criar conta.
Para adicionar, inclua um método no `AuthRepository` (ex.: `signInWithGoogle(idToken)`) e trate o
mesmo fluxo de criação/leitura do documento `users/{uid}`.

## Notificações (bloco F1)

O F1 usa **lembretes locais**, sem servidor: ao confirmar presença, o app agenda com WorkManager um
aviso 1 h antes do início (`WorkManagerReminderScheduler` + `ReminderWorker`). Sem FCM, sem
Blaze, funciona offline e sobrevive a reiniciar o aparelho.

- `core/notifications/ReminderPolicy.kt`: regra pura de quando avisar (testada sem Android).
- `core/notifications/ReminderScheduler.kt`: contrato; a UI/ViewModel não conhece WorkManager.
- `data/notifications/WorkManagerReminderScheduler.kt`: um trabalho único por evento
  (`reminder-<eventId>`); cancelar presença cancela o lembrete.
- `data/notifications/ReminderWorker.kt`: monta a notificação no canal `event_reminders`.
- No Android 13+ a permissão `POST_NOTIFICATIONS` é pedida na tela do evento, no momento do "Eu vou!".

Limitações atuais (aceitas no MVP):

- WorkManager sem alarmes exatos: em Doze, o aviso pode chegar alguns minutos atrasado.
- O lembrete é individual e local; cancelamento de evento pelo organizador ainda não avisa ninguém.

Quando o projeto tiver Firebase Blaze, o F2 (push) deve usar Cloud Functions + FCM: o mesmo
`ReminderScheduler` continuaria, trocando apenas a implementação para registrar tokens e deixar o
servidor disparar o envio — incluindo avisos de cancelamento e mudança de horário.

## Rodando o projeto

Pré-requisitos: JDK 17+ (Android Studio JBR serve), Android SDK e emulador/aparelho.

```powershell
# Build do APK de debug
.\gradlew.bat assembleDebug

# Testes unitários
.\gradlew.bat testDebugUnitTest
```

Crie sua conta pelo próprio app (Welcome → Criar minha conta). As contas ficam no
Firebase Authentication do projeto.

## Implementação atual: Authentication, perfil, eventos e RSVP

Já usam Firebase:

- `data/firebase/FirebaseAuthRepository.kt`: cadastro, login, logout, recuperação de senha e
  observação do usuário autenticado. O cadastro cria `users/{uid}` após criar a conta no Auth.
- `data/firebase/FirestoreUserRepository.kt`: observação em tempo real e atualização de nome,
  biografia, foto e interesses.
- `data/firebase/FirestoreUserMapper.kt`: conversão entre `User` e documento Firestore.
- `data/firebase/FirebaseErrorMapper.kt`: tradução de erros Firebase para `AppError`.
- `data/firebase/FirestoreEventRepository.kt`: feed em tempo real, detalhe, criação e RSVP.
- `data/firebase/FirestoreEventMapper.kt`: conversão de eventos e do local privado do segredinho.
- `data/firebase/FirestoreChatRepository.kt` e `FirestoreChatMapper.kt`: mensagens do evento.
- `data/firebase/FirestoreReviewRepository.kt` e `FirestoreReviewMapper.kt`: avaliações e média.

O Koin já injeta essas implementações. Os repositórios fake ficam só para os testes.

### Eventos no Firestore

- A consulta do feed busca `status == ACTIVE` e `endAt >= agora`, ordenada por `endAt`, com limite
  de 200 documentos. Texto, categoria, data e "só grátis/só segredinhos" são filtrados em memória;
  paginação, busca full-text (Algolia/Typesense) e geohash ficam para depois.
- `setRsvp` roda em transação: valida capacidade, cria/remove `rsvps/{uid}`, atualiza
  `goingCount` e mantém `attendees/{uid}` em sincronia (nome e foto do perfil).
- Quando nome ou foto mudam, o app percorre todas as páginas de `attendees/{uid}` para
  reconciliar as prévias; o limite de 30 é apenas o tamanho de cada página.
- Segredinhos gravam o local real em `events/{eventId}/private/location`. O documento público fica
  com "Local secreto" e coordenadas neutras; o repositório só mescla o local real para o
  organizador ou para quem tem RSVP.
- Eventos cancelados (`status = CANCELLED`) continuam acessíveis pelo detalhe, mas não entram no
  feed nem no mapa.

### Migração e rollout dos RSVPs anteriores

A subcoleção `attendees` passou a ser criada na mesma transação do RSVP. Confirmações
anteriores à publicação destas regras não têm esse documento e, por isso, não aparecem na
prévia.

**Decisão de rollout:** manter a escrita de `rsvps/{uid}`, `goingCount` e `attendees/{uid}`
atômica e falhar de forma fechada para clientes que não escrevem o participante público.
Não relaxe as regras para aceitar um RSVP isolado: isso permitiria `goingCount` e a prévia
divergirem. Em vez disso, publique o cliente corrigido, aplique uma **versão mínima do app**
e só então publique as regras.

Ordem recomendada:

1. Publique uma versão do app que contenha a transação com `attendees/{uid}`.
2. Imponha a versão mínima no canal de distribuição (Remote Config, loja ou atualização
   obrigatória); clientes anteriores ao PR #23 não são compatíveis com as regras estritas.
3. Rode um backfill idempotente com Admin SDK/Cloud Function para copiar cada
   `rsvps/{uid}` e os dados públicos de `users/{uid}` para
   `events/{eventId}/attendees/{uid}`. Preserve o `createdAt` do RSVP e reconcilie
   `goingCount` com o número de RSVPs válidos.
4. Publique `firestore.rules` e teste um primeiro RSVP em evento público e em segredinho.
5. Monitore erros `PERMISSION_DENIED` e conclua o backfill antes de habilitar a prévia para
   todo o público.

O backfill deve respeitar a visibilidade restrita dos segredinhos. Como ele usa o Admin SDK,
as regras do cliente não se aplicam, mas ele nunca deve criar participante para um RSVP que
não exista.

### Publicando regras e índices

O projeto inclui `firebase.json`, `firestore.indexes.json` e `.firebaserc`. Com o Firebase CLI:

```powershell
npm install -g firebase-tools
firebase login
firebase deploy --only firestore:rules,firestore:indexes
```

As regras também têm testes executados no Firestore Emulator. Eles cobrem, entre outros
casos, o primeiro RSVP em evento secreto e a rejeição de RSVP sem o participante público.
Requer Node.js 20+, Java e o Firebase CLI:

```powershell
npm install --prefix tools/firestore-rules-test
firebase emulators:exec --only firestore --project demo-sociable "node tools/firestore-rules-test/test-rules.js"
```

Sem o CLI, cole `firestore.rules` em **Firestore Database → Regras** e crie o índice composto
`events`: `status ASC, endAt ASC` em **Índices**. O console também mostra um link direto para criar
o índice quando a primeira consulta falhar por falta dele.

> Ao trocar os fakes pelo Firestore, o feed começa vazio. Crie alguns eventos pelo próprio app para
> popular o banco; a conta `demo@sociable.app` existia apenas no banco em memória.

### Dependência de coroutines

O projeto usa `kotlinx-coroutines-play-services` para aguardar Tasks do Firebase com `await()`.

### Regras do Firestore

O arquivo `firestore.rules` na raiz cobre `users`, `events`, `rsvps`, a prévia pública de
participantes (`attendees`), o local privado dos segredinhos e a coleção de denúncias (`reports`).
Usuários autenticados leem eventos e perfis; cada pessoa só altera o próprio perfil,
o próprio RSVP e os eventos que organiza. O `goingCount` só pode mudar junto com o RSVP
correspondente, e a criação de RSVP exige que a mesma transação tenha incrementado o contador e
criado o participante público.
O documento público em `attendees` (nome e foto) é criado na mesma transação do RSVP e apagado
quando a presença é cancelada. Fotos de perfil e de participante precisam usar HTTPS. Em
eventos secretos, a lista completa de participantes fica restrita ao organizador e a quem
confirmou presença; a pessoa pode consultar somente o próprio documento, o que permite à
primeira transação de RSVP verificar se ele já existe. Uma consulta à collection group
`attendees` filtrada por `userId` também é liberada para a pessoa sincronizar o próprio
perfil; a listagem completa continua bloqueada antes da confirmação. As denúncias só podem
ser criadas pelo cliente autenticado — a leitura fica com a moderação, via Admin SDK. Antes
de produção, revise a leitura de perfis (o campo `email` ainda está visível para qualquer
usuário autenticado).

### Onboarding social

O wizard roda uma única vez após o cadastro e grava três campos no mesmo `users/{uid}`:

- `lifeStage`: nome de um valor de `LifeStage` (ex.: `NEW_IN_TOWN`) ou `null` se a etapa foi pulada.
- `lookingFor`: array com até 3 nomes de `LookingFor` (ex.: `["NEW_FRIENDS", "NEW_HOBBIES"]`).
- `socialVibe`: nome de um valor de `SocialVibe` ou `null` se a etapa foi pulada.

"Prefiro não dizer" também é uma resposta explícita (`PREFER_NOT_TO_SAY`) nas etapas de escolha
única. O limite de 3 é aplicado no app (`LookingForRules.MAX_CHOICES`) e deve ser repetido no
backend se houver validação adicional. Nenhuma mudança nas regras é necessária: as regras atuais
só exigem que `email` e `createdAt` não mudem.

### Documento `users/{uid}`

Os campos persistidos são `name`, `email`, `bio`, `photoUrl`, `interests` (nomes dos valores do enum),
`lifeStage` (nome do enum ou `null`), `lookingFor` (nomes dos valores do enum) e `socialVibe` (nome do
enum ou `null`), além de `createdAt` (Timestamp do Firestore). A senha nunca é gravada no Firestore.
