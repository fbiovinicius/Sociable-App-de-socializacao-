<h1 align="center">Sociable</h1>

<p align="center">
  Aplicativo mobile para descobrir eventos locais em tempo real e encontrar pessoas com os mesmos interesses.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/plataforma-mobile-D4AE9E" alt="Plataforma: mobile">
  <img src="https://img.shields.io/badge/cidade-Manaus%20%E2%80%93%20AM-8E3B5A" alt="Cidade: Manaus – AM">
</p>

---

## Sobre

O **Sociable** é um aplicativo mobile que mostra, em um mapa, os eventos que estão acontecendo ao redor do usuário, com foco em eventos gratuitos, na cultura local e em experiências próximas. A primeira cidade atendida é **Manaus – AM**.

O usuário abre o app, vê o que está rolando agora, filtra o que combina com ele e, ao escolher um evento, confirma presença e conversa com quem também vai.

## O problema

Saber o que está acontecendo na cidade ainda é difícil:

- **Falta de informação em tempo real:** não há uma visão simples do que está acontecendo por perto, agora ou nas próximas horas.
- **Eventos invisíveis:** muitos eventos comunitários e gratuitos ficam fora das grandes plataformas de ingressos e das redes sociais.
- **Pouca conexão entre pessoas:** é difícil encontrar quem tem interesses em comum e vontade de sair de casa.

## Como o Sociable propõe resolver

| Problema | Solução |
|---|---|
| Descoberta complexa | **Feed e mapa** com eventos geolocalizados, filtros por data, categoria e preço. |
| Eventos invisíveis | Destaque para **eventos gratuitos, comunitários e da cultura local**, não apenas os que vendem ingresso. |
| Desconexão social | **Interesses no perfil** e **chat por evento** para combinar os detalhes do rolê. |

## Funcionalidades

**Descobrir**
- Feed de eventos de Manaus com busca ("O que você quer viver?").
- Filtros por data (hoje, amanhã, próximos 7 dias), categoria e somente gratuitos.
- Cards com categoria, data/hora, local, preço e quantidade de confirmados.
- Criação de evento com data, hora de início e duração (fim calculado, com ajuste manual opcional), local, coordenadas, valor e limite de pessoas.

**Mapa**
- Mapa interativo com Google Maps Compose e pinos por categoria.
- Busca e atalhos *Agora*, *Gratuitos*, *Música*, *Gastronomia*, *Esporte* e *Cultura*.
- Zoom, arraste e card do evento selecionado com acesso ao detalhe.
- Card do evento mostra **quem vai**: número de pessoas confirmadas e prévia das fotos de perfil.
- **Compartilhar** o evento (menu de compartilhamento do Android) e **denunciar** (motivo + comentário).
- Eventos "Segredinhos" ficam fora do mapa para não revelar o local.

**Segredinhos**
- Aba própria para eventos com local oculto; o endereço só aparece para quem confirmou presença.
- Criação a partir da aba já marcada como evento secreto.

**Evento**
- Detalhe com descrição, local, horário, preço, vagas e avaliações.
- Confirmar/cancelar presença ("Eu vou!"); o local secreto só aparece para quem confirmou.
- **Lembrete local**: ao confirmar presença, o app agenda um aviso 1 h antes do rolê
  (WorkManager + notificação local; funciona offline e sobrevive a reiniciar o aparelho).
- Chat por evento e avaliações com estrelas.

**Perfil**
- Edição de nome, bio, foto, interesses e perfil social (situação de vida, o que procura e vibe).
- Logout da conta.

**Onboarding social**
- Logo após o cadastro, um wizard de 3 passos pergunta a situação de vida, o que a pessoa procura
  (até 3 opções) e a sua vibe social.
- Cada etapa pode ser pulada ("Prefiro não dizer" também é uma resposta válida) e tudo é
  editável depois no Perfil, na seção "Sobre você".

**Navegação:** Descobrir • Mapa • Segredinhos • Perfil. Chat e avaliações são abertos pelo detalhe do evento.

## Status do desenvolvimento

MVP com lógica MVVM e UI em Compose integradas:

- [x] Cadastro, login e recuperação de senha com validações
- [x] Listagem, criação e filtros de eventos
- [x] Confirmação de presença ("Eu vou!")
- [x] Edição de perfil e interesses
- [x] Onboarding social no cadastro (situação de vida, o que procura e vibe social)
- [x] Chat e avaliações dos eventos
- [x] Mapa com Google Maps e eventos geolocalizados
- [x] Aba de segredinhos e local oculto para quem não confirmou
- [x] Firebase Authentication e Firestore (perfil, eventos e presença)
- [x] Google Maps
- [x] Lembretes locais (WorkManager)
- [x] Tema claro/escuro com seletor no perfil (sistema, claro ou escuro)
- [x] Chat e avaliações persistidos no Firestore
- [ ] Google Sign-In

A autenticação, o perfil, os eventos, o RSVP, o chat, as avaliações e as denúncias já usam Firebase.
Publique o `firestore.rules` do repositório antes de rodar esta versão (ele cobre também a prévia de
participantes e a coleção de denúncias). O guia de backend está em
[docs/INTEGRACAO_BACKEND.md](docs/INTEGRACAO_BACKEND.md).

### Rodando

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
```

Crie sua conta pelo próprio app (Welcome → Criar minha conta). As contas ficam no
Firebase Authentication do projeto.

### Google Maps

O mapa usa o Maps SDK for Android. Para ele aparecer, crie uma chave de API (com o
**Maps SDK for Android** ativado no Google Cloud) e adicione no `local.properties`
(arquivo ignorado pelo Git):

```properties
MAPS_API_KEY=sua_chave_aqui
```

Sem a chave o app compila e roda, mas o mapa fica em branco.

## Autores

- Fábio Vinícius
- Vinícius de Souza
- Letícia Manuele
