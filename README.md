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
- Criação de evento com local, coordenadas, período, valor e limite de pessoas.

**Mapa**
- Mapa ilustrativo de Manaus desenhado em Canvas, com pinos por categoria.
- Busca e atalhos *Agora*, *Gratuitos*, *Música*, *Gastronomia*, *Esporte* e *Cultura*.
- Zoom, arraste e card do evento selecionado com acesso ao detalhe.
- Eventos "Segredinhos" ficam fora do mapa para não revelar o local.

**Segredinhos**
- Aba própria para eventos com local oculto; o endereço só aparece para quem confirmou presença.
- Criação a partir da aba já marcada como evento secreto.

**Evento**
- Detalhe com descrição, local, horário, preço, vagas e avaliações.
- Confirmar/cancelar presença ("Eu vou!"); o local secreto só aparece para quem confirmou.
- Chat por evento e avaliações com estrelas.

**Perfil**
- Edição de nome, bio, foto e interesses.
- Logout da conta.

**Navegação:** Descobrir • Mapa • Segredinhos • Perfil. Chat e avaliações são abertos pelo detalhe do evento.

## Status do desenvolvimento

MVP com lógica MVVM e UI em Compose integradas:

- [x] Cadastro, login e recuperação de senha com validações
- [x] Listagem, criação e filtros de eventos
- [x] Confirmação de presença ("Eu vou!")
- [x] Edição de perfil e interesses
- [x] Chat e avaliações dos eventos
- [x] Mapa ilustrativo com eventos geolocalizados
- [x] Aba de segredinhos e local oculto para quem não confirmou
- [ ] Firebase (Auth + Firestore)
- [ ] Google Maps
- [ ] Google Sign-In

O backend está simulado em memória (Koin) para o app rodar sem Firebase. O guia para os
parceiros conectarem o Firebase está em
[docs/INTEGRACAO_BACKEND.md](docs/INTEGRACAO_BACKEND.md).

### Rodando

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
```

Conta de demonstração: `demo@sociable.app` / `123456`.

## Autores

- Fábio Vinícius
- Vinícius de Souza
- Letícia Manuele
