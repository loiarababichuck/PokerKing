# PokerKing - Documento de especificação do aplicativo Android

## 1. Visão geral

### Nome do aplicativo
PokerKing

### Descrição
O PokerKing é um aplicativo Android para controlar sessões de poker em grupo ou individuais, registrar entradas, saídas de mesa, ganhos e perdas e acompanhar a evolução financeira do jogador ao longo do tempo.

### Objetivo
Organizar as sessões de jogo de forma simples e objetiva, permitindo que o usuário registre cada rodada, visualize lucros e perdas e mantenha um histórico de desempenho para melhorar sua rotina de estudo e planejamento do jogo.

### Problema que resolve
Muitas pessoas que jogam poker em grupo ou em partidas informais não têm um controle claro da sua performance. Isso dificulta acompanhar quanto foi investido, quanto foi lucrado e em qual tipo de sessão o jogador se saiu melhor.

### Público-alvo
- jogadores casuais de poker;
- estudantes que participam de partidas entre amigos;
- usuários que desejam controlar pequenos ganhos e perdas;
- pessoas que querem aprender a usar um app simples para registrar finanças de jogos.

### Diferenciais da ideia
- interface intuitiva e leve;
- registro rápido de cada sessão;
- histórico visual por partidas;
- indicadores de lucro total e desempenho geral;
- armazenamento local, sem necessidade de login ou backend.

## 2. Conceito do aplicativo

O aplicativo funciona como um gerenciador de sessões de poker. O usuário abre o app e registra a data, o nome do jogador, o valor investido na compra de fichas (buy-in), o valor final obtido na mesa (cash-out) e uma observação opcional sobre a partida.

A partir desses dados, o app calcula automaticamente o lucro ou prejuízo da sessão e mostra um resumo financeiro em cards na tela inicial. O histórico fica visível em uma lista, permitindo revisar partidas anteriores e excluir entradas caso seja necessário.

A experiência do usuário é simples: em poucos segundos ele preenche um formulário, salva a sessão e acompanha o desempenho em tempo real. Isso torna o app útil para partidas rápidas e para acompanhamento de longo prazo.

## 3. Funcionalidades

### Funcionalidades principais
- registro de nova sessão de poker;
- campo para nome do jogador;
- cadastro do valor de compra e saída da mesa;
- cálculo automático de lucro/prejuízo;
- resumo financeiro com total de partidas, lucro líquido e melhor desempenho;
- histórico em lista com todas as sessões salvas;
- exclusão de itens do histórico;
- persistência local usando SharedPreferences.

### Funcionalidades complementares
- observações de cada partida;
- destaque visual de ganhos e perdas;
- identificação do maior ganho e da maior perda;
- interface responsiva para telas pequenas;
- botões de ação com feedback visual;
- limpar todos os dados do histórico.

### Possíveis funcionalidades futuras
- filtro por data;
- gráficos de desempenho financeiro;
- cadastro de múltiplos jogadores;
- exportação de dados para arquivo;
- estatísticas por mês;
- integração com banco local ou Room.

## 4. Telas

### 1. Tela inicial / dashboard
- Objetivo: apresentar os dados gerais do usuário e permitir o registro principal
- Componentes: TextView, MaterialCardView, EditText, TextInputLayout, RecyclerView, Button
- Informações exibidas: total de partidas, lucro total, maior ganho, maior perda
- Campos: nome do jogador, valor do buy-in, valor final, observações
- Botões: Salvar sessão, Limpar tudo
- Ações: salvar sessão, limpar histórico, visualizar dados
- Comportamentos: ao salvar, a sessão entra na lista e os cards atualizam automaticamente
- Navegação: tela principal, sem rotas complexas

### 2. Tela de histórico
- Objetivo: listar todas as sessões salvas
- Componentes: RecyclerView, CardView, TextView, Button
- Informações exibidas: data, nome, lucro/prejuízo, observações
- Campos: nenhum de entrada; apenas representação visual
- Botões: Excluir
- Ações: remover item do histórico
- Comportamentos: a lista é atualizada imediatamente após exclusão
- Navegação: acessada pela própria tela principal

## 5. Experiência do usuário

Ao abrir o aplicativo, o usuário vê um painel com um resumo do desempenho. Na parte superior, há cards com estatísticas gerais. Logo abaixo, há um formulário simples para registrar a sessão atual. O aluno preenche as informações e clica em salvar. O sistema calcula automaticamente o resultado da sessão e mostra na lista de histórico.

O processo é rápido e intuitivo, favorecendo o uso em situações reais de mesa, onde a agilidade é importante. A partir do histórico, o usuário avalia se está ganhando ou perdendo em média e consegue identificar padrões de desempenho.

## 6. Fluxo de navegação

```text
Tela Inicial
    │
    ├── Registrar nova sessão
    │       └── Salvar
    │              └── Atualizar dashboard
    │
    ├── Visualizar histórico
    │       └── Excluir sessão
    │
    └── Limpar todos os dados
```

## 7. Interface

A interface será clean, com predominância de cores escuras e detalhes em amarelo, verde e vermelho, lembrando a atmosfera de mesas de poker. O app usará:

- TextView para títulos e textos informativos;
- TextInputLayout + TextInputEditText para entradas do usuário;
- MaterialCardView para os cards estatísticos e itens do histórico;
- RecyclerView para exibir as sessões;
- Button para salvar e excluir;
- ScrollView para ajustar o layout em telas menores;
- Toolbar não é obrigatório, mas pode ser incorporado ao topo.

## 8. Modelo de dados

### Entidade: Sessão de poker

| Campo | Tipo | Obrigatório | Descrição |
| ----- | ---- | ----------- | --------- |
| id | String | Sim | Identificador único da sessão |
| playerName | String | Sim | Nome do jogador |
| date | String | Sim | Data da sessão |
| buyIn | double | Sim | Valor investido na compra de fichas |
| cashOut | double | Sim | Valor final obtido |
| notes | String | Não | Observação opcional |
| profit | double | Sim | Calculado automaticamente como cashOut - buyIn |

### Relacionamento
O aplicativo trabalha com uma lista simples de sessões em memória e armazenamento local. Cada item da lista representa uma partida independente, sem relacionamentos complexos entre entidades.

## 9. Armazenamento

A estratégia escolhida é o uso de `SharedPreferences`, porque o projeto é simples e não exige um banco de dados robusto. Esse tipo de armazenamento é ideal para guardar um pequeno histórico local do usuário com facilidade de implementação e baixo custo de desenvolvimento.

Vantagens:
- baixa complexidade;
- rápido para implementar;
- suficiente para o escopo do app;
- sem necessidade de servidor.

## 10. Regras de negócio

- o nome do jogador deve ser informado;
- o buy-in deve ser maior ou igual a zero;
- o cash-out deve ser maior ou igual a zero;
- a diferença entre cashOut e buyIn define o lucro ou prejuízo;
- uma sessão inválida não pode ser salva;
- excluir uma sessão remove o item do histórico e recalcula os cards;
- limpar todos os dados deve zerar o histórico e as estatísticas.

## 11. Validações

| Campo/Ação | Regra | Mensagem |
| ---------- | ----- | -------- |
| Nome do jogador | obrigatório | "Informe o nome do jogador." |
| Buy-in | deve ser numérico e >= 0 | "Valor de compra inválido." |
| Cash-out | deve ser numérico e >= 0 | "Valor final inválido." |
| Salvar sessão | todos os campos válidos | "Sessão salva com sucesso!" |
| Limpar histórico | confirmação do usuário | "Histórico limpo." |

## 12. Tratamento de erros

- se o usuário tentar salvar dados vazios, o app exibe uma mensagem de erro;
- se o valor numérico não puder ser convertido, mostra uma mensagem informando que o valor é inválido;
- se o usuário tentar remover uma sessão já apagada, a ação é ignorada com segurança;
- em caso de falha de leitura de dados, o app inicia com uma lista vazia.

## 13. Recursos visuais

- ícone do app: cartas de poker ou símbolo de chip;
- cores: preto, dourado, verde e vermelho;
- imagens: elementos gráficos simples em vetor ou SVG;
- alternativas gratuitas: assets do Material Design, ícones de `androidx` e figuras criadas com vetor.

## 14. Estrutura do projeto Android

```text
app/
├── java/
│   └── com/example/pokerking/
│       ├── MainActivity.java
│       ├── Session.java
│       └── SessionAdapter.java
├── res/
│   ├── layout/
│   │   ├── activity_main.xml
│   │   └── item_session.xml
│   ├── drawable/
│   ├── mipmap/
│   └── values/
│       ├── strings.xml
│       ├── colors.xml
│       └── themes.xml
├── AndroidManifest.xml
└── build.gradle
```

## 15. Arquitetura

O projeto seguirá uma arquitetura simples e direta, adequada para quatro aulas. A responsabilidade será dividida da seguinte forma:

- `MainActivity`: coordenar a interface, salvar dados e atualizar estatísticas.
- `Session`: representar cada partida com seus campos e cálculo de lucro.
- `SessionAdapter`: conectar os dados do histórico à RecyclerView.
- `strings.xml` e `colors.xml`: centralizar textos e identidade visual.

Essa organização é suficiente para um app funcional e didático, sem excesso de abstração.

## 16. Plano de desenvolvimento em 4 aulas

### Aula 1 — Estrutura e interface
- criar o projeto Android;
- configurar as dependências básicas;
- montar o layout principal com cards e formulário;
- definir identidade visual da aplicação.

### Aula 2 — Funcionalidades
- implementar registro de sessão;
- calcular lucro e prejuízo;
- atualizar o dashboard com estatísticas;
- listar sessões na RecyclerView.

### Aula 3 — Dados e recursos
- persistir sessões no SharedPreferences;
- implementar exclusão de itens;
- validar entradas do usuário;
- ajustar estilos e recursos visuais.

### Aula 4 — Finalização
- revisar a navegação e a usabilidade;
- ajustar textos e mensagens;
- testar cenários de erro;
- organizar o código e preparar a versão final.

## 17. Prompts para o GitHub Copilot

### Prompt 1 — Estrutura inicial
- Objetivo: criar a base do app
- Arquivos envolvidos: `MainActivity.java`, `activity_main.xml`, `strings.xml`
- Implementação esperada: tela principal com cards e formulário de registro
- Regras: usar Java, XML e Material Components

### Prompt 2 — Modelo de sessão
- Objetivo: criar a classe de dados
- Arquivos envolvidos: `Session.java`
- Implementação esperada: atributos de sessão, cálculo de lucro e serialização para JSON
- Regras: usar campos simples e métodos de apoio para formatação monetária

### Prompt 3 — Lista de histórico
- Objetivo: renderizar o histórico de partidas
- Arquivos envolvidos: `SessionAdapter.java`, `item_session.xml`
- Implementação esperada: RecyclerView com cards e botão de exclusão
- Regras: manter layout simples e legível

### Prompt 4 — Persistência local
- Objetivo: salvar dados no dispositivo
- Arquivos envolvidos: `MainActivity.java`
- Implementação esperada: carregar e salvar sessões em SharedPreferences
- Regras: a lista deve continuar disponível após fechar o app

### Prompt 5 — Validations and feedback
- Objetivo: melhorar a experiência do usuário
- Arquivos envolvidos: `MainActivity.java`, `strings.xml`
- Implementação esperada: validações de campos, mensagens de erro e toasts
- Regras: evitar erros de entrada e manter UX clara

## Conclusão

O PokerKing atende ao objetivo de criar um aplicativo Android funcional, visualmente interessante e adequado para um projeto acadêmico. Ele é acessível para um desenvolvimento em quatro aulas, pode ser expandido futuramente e está alinhado com a proposta de usar Java, XML e GitHub Copilot como apoio.
