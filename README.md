# Sudoku JavaFX

Jogo de Sudoku desktop em Java 17+, JavaFX 21 e Maven, com um toolkit de customizações do GitHub Copilot.

## Build e execução

```bash
mvn clean test          # testes JUnit 5
mvn clean javafx:run    # inicia o jogo
mvn javafx:run -Djavafx.args="530070000600195000098000060800060003400803001700020006060000280000419005000080079"
```

O último comando inicia com um puzzle da linha de comando (formato abaixo); o botão "Jogar puzzle da linha de comando" aparece no menu.

## Como jogar
- Menu: escolha a dificuldade e clique em **Novo jogo**.
- Selecione uma célula (clique ou setas) e digite `1–9` ou use a paleta. `Del`/`Backspace` remove. `P` alterna o modo rascunho.
- Barra superior: **Novo jogo**, **Verificar jogo** (destaca conflitos), **Verificar status**, **Limpar**, **Finalizar jogo**, **Menu**.
- O painel lateral mostra **Status** (`NAO_INICIADO`/`INCOMPLETO`/`COMPLETO`) e **Erros** (`COM_ERRO`/`SEM_ERRO`).
- **Finalizar** só habilita com tabuleiro completo e sem erros.

## Formato dos puzzles
`src/main/resources/puzzles/puzzles.txt`, uma linha por puzzle:

```
nome;DIFICULDADE;81 caracteres
```

- `DIFICULDADE`: `FACIL`, `MEDIO` ou `DIFICIL`.
- Os 81 caracteres são as linhas do tabuleiro em sequência; `1–9` = número fixo, `0` ou `.` = vazio.
- Linhas em branco e iniciadas por `#` são ignoradas.
- Argumento de linha de comando: apenas os 81 caracteres (ou a linha completa entre aspas).

## Arquitetura (pacote `com.dio`)

| Pacote | Responsabilidade |
| --- | --- |
| `model` | Regras puras: `SudokuBoard` (fixas, jogador, rascunhos), `Puzzle`, enums `GameStatus`/`ErrorStatus`/`Difficulty`/`ConflictType`, `Conflict`, `ValidationReport`, `InvalidMoveException`. Sem JavaFX. |
| `service` | `ValidationService` (conflitos, status, erros), `SudokuService` (sessão de jogo: inserir, remover, rascunho, limpar, finalizar), `PuzzleLoader` (arquivo/args). Sem JavaFX. |
| `controller` | `MenuController`, `GameController`: capturam entrada, chamam o service e atualizam a view. |
| `view` | `SceneNavigator`: carrega FXML/CSS e troca de tela, injeta controllers por construtor. |
| `util` | `GridMath`. |

Recursos: `fxml/menu.fxml`, `fxml/game.fxml`, `css/app.css`, `puzzles/puzzles.txt`.

Estados visuais das células (classes CSS): `cell-fixed`, `cell-user`, `cell-selected`, `cell-error`, `cell-draft`, `cell-empty`. Erro usa também negrito e sublinhado (não depende só de cor); blocos 3×3 têm bordas mais espessas.

## Ferramentas Copilot

| Artefato | Caminho | Uso |
| --- | --- | --- |
| Agente de lógica | `.github/agents/sudoku-logic-reviewer.agent.md` | `@sudoku-logic-reviewer revise a lógica de validação` (somente leitura) |
| Agente de testes | `.github/agents/sudoku-test-writer.agent.md` | `@sudoku-test-writer cubra conflitos de bloco` (edita testes) |
| Agente de UI | `.github/agents/javafx-ui-reviewer.agent.md` | `@javafx-ui-reviewer revise game.fxml e app.css` (somente leitura) |
| Skill de validação | `.github/skills/sudoku-validator/` | Carregada automaticamente; script: `.github/skills/sudoku-validator/scripts/validate.sh tabuleiro.txt [original.txt]` devolve JSON |
| Skill de formatação | `.github/skills/java-format/` | Regras de formatação Java (e `mvn spotless:apply` se configurado) |
| Hooks | `.github/hooks/hooks.json` | `sessionStart` (lembrete), `postEdit` (`format-java.sh`), `preCommit` (`run-tests.sh`, bloqueia commit se `mvn -q test` falhar) |
| Plugin | `sudoku-copilot-plugin/` | `copilot plugin install ./sudoku-copilot-plugin` |
| Extensão (roadmap) | `sudoku-copilot-plugin/EXTENSION.md` | Descrição da futura extensão `@sudoku` |
| Instruções gerais | `.github/copilot-instructions.md` | Convenções do projeto |

Os agentes revisam/testam exatamente as classes acima, e o script `validate.sh` implementa as mesmas regras de `ValidationService`.

## Decisões de design
- Regras em `model`/`service`, testáveis sem JavaFX (`SudokuServiceTest`, `PuzzleLoaderTest`).
- Os 81 labels do tabuleiro são criados no controller (evita 81 `fx:id`); o estilo vem só de classes CSS.
- O teclado é tratado por um filtro de eventos no nó raiz, sem exigir foco no tabuleiro.
- Rascunhos não afetam status/validação e são apagados ao inserir o número ou limpar.
- Logs com `java.util.logging`.
- Os diretórios do plugin são links simbólicos para `.github/` (fonte única).
