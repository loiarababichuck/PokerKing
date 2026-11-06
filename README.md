# PokerKing

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java" />
  <img src="https://img.shields.io/badge/Texas%20Hold%27em-Poker-8B5CF6?style=for-the-badge" alt="Texas Hold'em" />
</p>

Uma simulação de mesa de poker em Android, com fluxo de partidas, bots, apostas, flop/turn/river e showdown, desenvolvida em Java com UI nativa.

## 📌 Visão geral

O PokerKing é um projeto de estudo/protótipo para reproduzir a dinâmica de uma mesa de Texas Hold'em em um app Android. O jogador controla o personagem principal (`Você`) e compete contra três bots em um torneio simplificado, com sistema de fichas, blinds, rodadas de ação e avaliação de mãos.

O objetivo do projeto é demonstrar lógica de jogo, UI de mesa e cálculo de vencedores em uma aplicação nativa Android.

## 🎯 Funcionalidades

- Mesa de 4 jogadores
- 2 cartas hole por jogador
- Cartas comunitárias em flop, turn e river
- Sistema de blinds e sequência de ação
- Ações do jogador: passar, apostar, aumentar, desistir e all-in
- Bots com decisão automática
- Cálculo de pots e distribuição de fichas
- Avaliação da melhor mão com combinação de 7 cartas
- Eliminação por chips zerados
- Ranking local e reinício de mesa
- Visual de cassino com mesa e cartas

## 🃏 Como funciona o jogo

1. A mão começa com a distribuição de cartas e blind.
2. O primeiro jogador ativo a agir é definido pela posição da mesa.
3. Cada participante decide entre passar, pagar, aumentar ou desistir.
4. O jogo avança para flop, turn e river conforme as rodadas terminam.
5. Ao fim, o showdown compara as melhores mãos e distribui os pots.
6. O torneio continua até sobrar um único jogador ativo ou a mesa ser resetada.

## 🧠 Arquitetura do projeto

O projeto foi estruturado para separar bem a lógica do jogo da camada visual:

- `MainActivity`: orquestra a interface, a rodada, turnos e ações
- `Player`: representa cada jogador, fichas, cartas e estado da mão
- `Card`: modela a carta com naipe, valor e label visual
- `Deck`: gera e embaralha o baralho
- `HandEvaluator`: avalia a melhor combinação de cartas
- `PotCalculator` e `PotEvaluator`: calculam pots, apostas e vencedores

## 🛠️ Stack tecnológica

- Android SDK
- Java
- Gradle
- AppCompat
- Material Components
- JUnit para testes unitários

## 🚀 Pré-requisitos

- Android Studio
- JDK 11 ou superior
- Android SDK configurado
- Emulador Android ou dispositivo físico

## ▶️ Executando o projeto

### Clonar o repositório

```bash
git clone https://github.com/seu-usuario/PokerKing.git
cd PokerKing
```

### Abrir no Android Studio

1. Abra a pasta do projeto.
2. Aguarde o Gradle sincronizar dependências.
3. Selecione um emulador ou dispositivo conectado.
4. Clique em Run.

### Compilar via terminal

Linux/macOS:

```bash
./gradlew assembleDebug
```

Windows:

```powershell
.
\gradlew.bat assembleDebug
```

## 🧪 Testes

Para rodar os testes unitários:

Linux/macOS:

```bash
./gradlew testDebugUnitTest
```

Windows:

```powershell
.
\gradlew.bat testDebugUnitTest
```

## 📁 Estrutura do projeto

```text
PokerKing/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/pokerking/
│   │   │   │   ├── Card.java
│   │   │   │   ├── Deck.java
│   │   │   │   ├── HandEvaluator.java
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── Player.java
│   │   │   │   ├── Pot.java
│   │   │   │   ├── PotCalculator.java
│   │   │   │   ├── PotCalculationResult.java
│   │   │   │   ├── PotEvaluator.java
│   │   │   │   └── ...
│   │   │   └── res/
│   │   │       ├── drawable/
│   │   │       ├── layout/
│   │   │       └── values/
│   │   └── test/java/com/example/pokerking/
│   │       ├── HandEvaluatorTest.java
│   │       ├── PotCalculatorTest.java
│   │       └── ExampleUnitTest.java
│   └── build.gradle
├── build.gradle
├── gradlew
├── gradlew.bat
├── settings.gradle
├── docs/
│   └── PROJETO_APP_ANDROID.md
├── status.md
├── README.md
└── .gitignore
```

## ✅ Validação

O projeto foi validado localmente com:

```bash
./gradlew.bat assembleDebug --no-daemon
```

Resultado esperado: build bem-sucedido.

## 📈 Melhorias futuras

- IA mais estratégica para os bots
- apostas com valores configuráveis
- animações de cartas e fichas
- tela de menu principal
- histórico de partidas e estatísticas
- suporte a multiplayer local ou online
- refinamento visual do layout da mesa

## 🤝 Contribuição

Contribuições são bem-vindas. Para colaborar:

1. Faça um fork do projeto
2. Crie uma branch para sua funcionalidade
3. Faça o commit das mudanças
4. Abra um pull request

## 📌 Observação

Este é um protótipo funcional de poker para Android, com foco em lógica de jogo, UX de mesa e aprendizado de desenvolvimento nativo em Java.
