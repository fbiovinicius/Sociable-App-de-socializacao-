# Sociable – Lista de tarefas

App mobile (Android, Kotlin + Jetpack Compose) para descobrir eventos locais em tempo real e encontrar pessoas com os mesmos interesses. Cidade inicial: Manaus – AM.

Legenda: `[ ]` a fazer · `[x]` feito

---

## Fase 0 – Base do projeto

- [x] Criar projeto no Android Studio (Kotlin + Compose)
- [ ] Criar o `README.md` com descrição, autores e como rodar
- [ ] Definir a arquitetura (sugestão: MVVM + Repository)
- [ ] Organizar pacotes: `data`, `domain`, `ui`, `navigation`, `di`
- [ ] Adicionar dependências ao `libs.versions.toml`:
  - [ ] Navigation Compose
  - [ ] ViewModel + StateFlow
  - [ ] Hilt (injeção de dependência)
  - [ ] Coil (imagens)
  - [ ] Google Maps Compose + Play Services Location
  - [ ] Firebase (Auth, Firestore, Cloud Messaging)
- [ ] Criar o tema do app (cores, tipografia, formas) conforme o design
- [ ] Configurar a chave da API do Google Maps (fora do git, em `local.properties`)
- [ ] Adicionar `*.jks`, `*.keystore` e `google-services.json` ao `.gitignore` se necessário

## Fase 1 – Navegação e telas base

- [ ] Barra de navegação inferior: Explorar · Salvos · Conversas · Perfil
- [ ] Criar o grafo de navegação (NavHost)
- [ ] Tela de splash
- [ ] Tela de boas-vindas / onboarding
- [ ] Tela de login e cadastro
- [ ] Tela de perfil (esqueleto)

## Fase 2 – Explorar (mapa)

- [ ] Pedir permissão de localização (com tela de explicação)
- [ ] Exibir o mapa centralizado em Manaus
- [ ] Mostrar a localização atual do usuário
- [ ] Criar o modelo `Evento` (título, descrição, local, lat/lng, horário, categoria, gratuito, nota, nº de participantes)
- [ ] Marcadores no mapa com ícone por categoria
- [ ] Barra de busca "O que você quer viver?"
- [ ] Atalhos rápidos: **Agora**, **Gratuitos**, **Perto de mim**
- [ ] Card com o próximo encontro em destaque
- [ ] Botão para criar evento direto no mapa

## Fase 3 – Filtros ("Do seu jeito")

- [ ] Tela/bottom sheet de filtros
- [ ] Categorias: Música, Arte e cultura, Gastronomia, Esporte, Natureza, Encontros e Feiras
- [ ] Vibes: Cultura amazônica, Fazer amizades, Ao ar livre, Em família
- [ ] Slider de distância (1 km até toda Manaus)
- [ ] Switch "só gratuitos"
- [ ] Switch "só o que está acontecendo agora"
- [ ] Aplicar os filtros ao mapa e à lista
- [ ] Botão de limpar filtros

## Fase 4 – Evento

- [ ] Tela de detalhes (descrição, local, distância, horário, categoria, avaliação, nº de pessoas que vão)
- [ ] Botão **Salvar**
- [ ] Botão **Eu vou!!** (confirmar presença)
- [ ] Avaliar evento
- [ ] Denunciar evento
- [ ] Tela de criar evento (formulário + escolher local no mapa)
- [ ] Validação do formulário
- [ ] Upload de foto do evento

## Fase 5 – Salvos

- [ ] Listar eventos salvos
- [ ] Remover dos salvos
- [ ] Estado vazio ("Você ainda não salvou nenhum evento")

## Fase 6 – Chat do evento

- [ ] Lista de conversas (um chat por evento confirmado)
- [ ] Tela de chat em tempo real
- [ ] Liberar o chat só para quem confirmou presença
- [ ] Notificações de novas mensagens
- [ ] Moderação básica (denunciar mensagem, bloquear usuário)

## Fase 7 – Perfil

- [ ] Foto, nome e bio
- [ ] Escolher interesses (usados nos filtros e recomendações)
- [ ] Eventos que o usuário criou / vai participar
- [ ] Configurações (notificações, sair da conta)

## Fase 8 – Backend e dados

- [ ] Definir a estrutura do Firestore (usuários, eventos, presenças, mensagens, denúncias)
- [ ] Regras de segurança do Firestore
- [ ] Repositórios (Evento, Usuário, Chat)
- [ ] Atualização em tempo real dos eventos (listeners)
- [ ] Cache local com Room (modo offline básico)
- [ ] Cadastrar eventos iniciais de Manaus para testes

## Fase 9 – Qualidade e lançamento

- [ ] Testes unitários dos ViewModels e repositórios
- [ ] Testes de UI das telas principais
- [ ] Tratar estados de erro, carregamento e sem internet
- [ ] Revisar acessibilidade (descrições de conteúdo, contraste, tamanho de fonte)
- [ ] Testar em aparelhos e versões de Android diferentes
- [ ] Ícone do app e tela de splash finais
- [ ] Política de privacidade e termos de uso
- [ ] Gerar o build assinado (AAB)
- [ ] Publicar na Play Store (teste interno primeiro)

---

## Ideias para depois

- [ ] Recomendações por interesse
- [ ] Compartilhar evento por link
- [ ] Lembretes antes do evento começar
- [ ] Perfil de organizador verificado
- [ ] Expandir para outras cidades

## Autores

- Fábio Vinícius
- Vinícius de Souza
- Letícia Manuele
