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
single<EventRepository> { FirestoreEventRepository(Firebase.firestore, get()) }
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

### EventRepository

- `observeEvents(filter)` → listener na collection `events`; aplicar filtros (`query`, `categories`,
  `dateFilter`, `onlyFree`, `onlySecret`); ordenar por `startAt`.
- `observeEvent(eventId)` / `getEvent(eventId)` → documento do evento.
- `createEvent(draft)` → `add()` com `createdAt`, `goingCount = 0` e `isSecret = draft.isSecret`.
- **Segredinhos:** eventos com `isSecret = true` não aparecem no mapa (o `MapViewModel` filtra) e
  devem ocultar `venueName`/`address` de quem não tem RSVP. Veja a observação nas regras de segurança.
- `setRsvp(eventId, userId, going)` → transação:
  1. ler `events/{eventId}`;
  2. ler `events/{eventId}/rsvps/{userId}`;
  3. se `going` e `goingCount >= capacity`, devolver `AppError.EVENT_FULL`;
  4. criar/remover o documento de RSVP e atualizar `goingCount` com `FieldValue.increment(±1)`.
- `observeRsvp(eventId, userId)` → combinar evento (capacidade/contador) com o documento de RSVP.

### ChatRepository

- `observeMessages(eventId)` → `events/{eventId}/messages` ordenado por `sentAt`, `limitToLast(50)`.
- `sendMessage(...)` → `add()` com `FieldValue.serverTimestamp()`.

### ReviewRepository

- `observeReviews(eventId)` → `events/{eventId}/reviews` ordenado por `createdAt`.
- `observeRating(eventId)` → média/quantidade (pode ser derivada das reviews no MVP).
- `submitReview(...)` → documento com id `"${eventId}_${authorId}"` para garantir **1 avaliação por usuário**.
  Uma Cloud Function (ou transação) atualiza `ratingAvg`/`ratingCount` no evento.

## Estrutura sugerida no Firestore

```
users/{userId}
  name, email, bio, photoUrl, interests: [String], createdAt

events/{eventId}
  title, description, category, venueName, address, latitude, longitude,
  startAt, endAt, isFree, price, capacity, imageUrl, isSecret,
  organizerId, organizerName, goingCount, ratingAvg, ratingCount, createdAt

events/{eventId}/private/location   # só eventos isSecret = true
  venueName, address

events/{eventId}/rsvps/{userId}
  createdAt

events/{eventId}/messages/{messageId}
  authorId, authorName, text, sentAt

events/{eventId}/reviews/{userId}
  rating, comment, authorName, createdAt
```

Sugestão de índices: `events` por `startAt`; combinados categoria + `startAt`; `isFree` + `startAt`.
Para busca textual e "perto de mim", usar respectivamente Algolia/Typesense e geohash (`geofire-common`).

## Regras de segurança (ponto de partida)

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
- **Interesses** aparecem depois do cadastro (`interests`) e podem ser editados no Perfil.

Para manter o padrão ao evoluir as telas:

1. Colete estado sempre com `collectAsStateWithLifecycle()`.
2. Use `UiState` para renderizar loading/erro/vazio e chame apenas os métodos `on...` do ViewModel.
3. Reaproveite `ui/common/` (`StickerBox`, `SociableButton`, `SociableChip`, `SociableTextField`,
   `Pin`) para manter o visual consistente.
4. Rotas que recebem `eventId`: `event/{eventId}`, `event/{eventId}/chat`, `event/{eventId}/reviews`.

O mapa ainda é ilustrativo (Canvas). `MapViewModel` entrega `markers` com categoria, preço,
confirmados, capacidade e `isHappeningNow`; `ManausMapProjection` converte latitude/longitude em
posições normalizadas. Para trocar pelo Google Maps Compose, substitua apenas o Canvas da
`ui/map/MapScreen.kt` e use as coordenadas que já vêm nos markers. Eventos secretos são excluídos
dos markers para não revelar o local.

## Google Sign-In (pós-MVP)

Sem Google Sign-In no MVP: a tela de boas-vindas oferece apenas entrar com e-mail ou criar conta.
Para adicionar, inclua um método no `AuthRepository` (ex.: `signInWithGoogle(idToken)`) e trate o
mesmo fluxo de criação/leitura do documento `users/{uid}`.

## Rodando o projeto

Pré-requisitos: JDK 17+ (Android Studio JBR serve), Android SDK e emulador/aparelho.

```powershell
# Build do APK de debug
.\gradlew.bat assembleDebug

# Testes unitários
.\gradlew.bat testDebugUnitTest
```

Conta de demonstração: `demo@sociable.app` / `123456`.

## Implementação atual: Authentication e perfil

Os contratos `AuthRepository` e `UserRepository` já possuem implementações Firebase:

- `data/firebase/FirebaseAuthRepository.kt`: cadastro, login, logout, recuperação de senha e
  observação do usuário autenticado. O cadastro cria `users/{uid}` após criar a conta no Auth.
- `data/firebase/FirestoreUserRepository.kt`: observação em tempo real e atualização de nome,
  biografia, foto e interesses.
- `data/firebase/FirestoreUserMapper.kt`: conversão entre `User` e documento Firestore.
- `data/firebase/FirebaseErrorMapper.kt`: tradução de erros Firebase para `AppError`.

O Koin já injeta essas duas implementações reais. Eventos, chat e avaliações continuam usando
os repositórios fake; a troca deles será feita em etapas separadas.

### Dependência de coroutines

O projeto usa `kotlinx-coroutines-play-services` para aguardar Tasks do Firebase com `await()`.

### Regras iniciais do Firestore

O arquivo `firestore.rules` na raiz contém regras iniciais para a coleção `users`. Publique-as no
Firebase Console em **Firestore Database → Regras**. Usuários autenticados conseguem ler perfis,
mas somente o dono do UID pode criar, alterar ou excluir o próprio documento. Antes de publicar
em produção, revise a política de leitura de perfis conforme as necessidades de privacidade do app.

### Documento `users/{uid}`

Os campos persistidos são `name`, `email`, `bio`, `photoUrl`, `interests` (nomes dos valores do enum)
e `createdAt` (Timestamp do Firestore). A senha nunca é gravada no Firestore.
