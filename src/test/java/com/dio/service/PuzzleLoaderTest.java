package com.dio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dio.model.Difficulty;
import com.dio.model.Puzzle;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PuzzleLoaderTest {

    private static final String GRID =
            "530070000600195000098000060800060003400803001700020006060000280000419005000080079";

    @Nested
    class ParseDeLinha {

        @Test
        void deveLerLinhaCompleta() throws PuzzleFormatException {
            Puzzle puzzle = PuzzleLoader.parseLine("Meu;DIFICIL;" + GRID);
            assertEquals("Meu", puzzle.name());
            assertEquals(Difficulty.DIFICIL, puzzle.difficulty());
            assertEquals(5, puzzle.given(0, 0));
            assertEquals(0, puzzle.given(0, 2));
        }

        @Test
        void deveAceitarPontoComoCelulaVazia() throws PuzzleFormatException {
            Puzzle puzzle = PuzzleLoader.parseLine(GRID.replace('0', '.'));
            assertEquals(0, puzzle.given(0, 2));
        }

        @Test
        void deveAceitarSomenteOTabuleiro() throws PuzzleFormatException {
            assertEquals(Difficulty.MEDIO, PuzzleLoader.parseLine(GRID).difficulty());
        }

        @Test
        void deveRejeitarTamanhoInvalido() {
            assertThrows(PuzzleFormatException.class, () -> PuzzleLoader.parseLine("123"));
        }

        @Test
        void deveRejeitarCaractereInvalido() {
            assertThrows(PuzzleFormatException.class, () -> PuzzleLoader.parseLine("x" + GRID.substring(1)));
        }

        @Test
        void deveRejeitarDificuldadeDesconhecida() {
            assertThrows(PuzzleFormatException.class, () -> PuzzleLoader.parseLine("a;IMPOSSIVEL;" + GRID));
        }
    }

    @Nested
    class Arquivo {

        @Test
        void deveIgnorarComentariosELinhasEmBranco() throws Exception {
            String text = "# comentário\n\nA;FACIL;" + GRID + "\n";
            assertEquals(1, PuzzleLoader.parseAll(new StringReader(text)).size());
        }

        @Test
        void deveInformarONumeroDaLinhaInvalida() {
            PuzzleFormatException e = assertThrows(PuzzleFormatException.class,
                    () -> PuzzleLoader.parseAll(new StringReader("# c\nruim\n")));
            assertTrue(e.getMessage().startsWith("Linha 2"));
        }

        @Test
        void deveCarregarRecursoPadraoComTodasAsDificuldades() throws IOException, PuzzleFormatException {
            List<Puzzle> puzzles = PuzzleLoader.loadFromResource(PuzzleLoader.DEFAULT_RESOURCE);
            for (Difficulty difficulty : Difficulty.values()) {
                assertTrue(PuzzleLoader.pickRandom(puzzles, difficulty, new Random(1)).isPresent());
            }
        }

        @Test
        void deveFalharParaRecursoInexistente() {
            assertThrows(IOException.class, () -> PuzzleLoader.loadFromResource("/nao-existe.txt"));
        }
    }

    @Nested
    class ArgumentosDeLinhaDeComando {

        @Test
        void deveUsarOPrimeiroArgumentoComoPuzzle() throws PuzzleFormatException {
            Optional<Puzzle> puzzle = PuzzleLoader.fromArgs(List.of(GRID));
            assertTrue(puzzle.isPresent());
            assertEquals(5, puzzle.get().given(0, 0));
        }

        @Test
        void deveRetornarVazioSemArgumentos() throws PuzzleFormatException {
            assertFalse(PuzzleLoader.fromArgs(List.of()).isPresent());
        }

        @Test
        void deveRejeitarArgumentoInvalido() {
            assertThrows(PuzzleFormatException.class, () -> PuzzleLoader.fromArgs(List.of("abc")));
        }
    }
}
