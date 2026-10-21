package com.dio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dio.model.Conflict;
import com.dio.model.ConflictType;
import com.dio.model.ErrorStatus;
import com.dio.model.GameStatus;
import com.dio.model.InvalidMoveException;
import com.dio.model.Puzzle;
import com.dio.model.ValidationReport;
import com.dio.util.GridMath;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SudokuServiceTest {

    private static final String PUZZLE =
            "Teste;FACIL;530070000600195000098000060800060003400803001700020006060000280000419005000080079";
    private static final String SOLUTION =
            "534678912672195348198342567859761423426853791713924856961537284287419635345286179";

    private SudokuService service;

    @BeforeEach
    void iniciarJogo() throws PuzzleFormatException {
        service = new SudokuService(new ValidationService());
        service.newGame(PuzzleLoader.parseLine(PUZZLE));
    }

    private void preencherSolucao() {
        for (int r = 0; r < GridMath.SIZE; r++) {
            for (int c = 0; c < GridMath.SIZE; c++) {
                if (service.board().isEmpty(r, c)) {
                    service.place(r, c, SOLUTION.charAt(r * GridMath.SIZE + c) - '0');
                }
            }
        }
    }

    @Nested
    class Conflitos {

        @Test
        void deveDetectarConflitoNaLinha() {
            service.place(0, 8, 7);
            ValidationReport report = service.report();
            assertEquals(ErrorStatus.COM_ERRO, report.errors());
            assertTrue(report.conflicts().contains(new Conflict(0, 8, 7, ConflictType.LINHA)));
            assertTrue(report.conflicts().stream().allMatch(c -> c.type() == ConflictType.LINHA));
        }

        @Test
        void deveDetectarConflitoNaColuna() {
            service.place(0, 3, 4);
            ValidationReport report = service.report();
            assertEquals(ErrorStatus.COM_ERRO, report.errors());
            assertTrue(report.conflicts().contains(new Conflict(0, 3, 4, ConflictType.COLUNA)));
            assertTrue(report.conflicts().stream().allMatch(c -> c.type() == ConflictType.COLUNA));
        }

        @Test
        void deveDetectarConflitoNoBloco() {
            service.place(0, 2, 9);
            ValidationReport report = service.report();
            assertEquals(ErrorStatus.COM_ERRO, report.errors());
            assertTrue(report.conflicts().contains(new Conflict(0, 2, 9, ConflictType.BLOCO)));
            assertTrue(report.hasConflictAt(2, 1));
            assertTrue(report.conflicts().stream().allMatch(c -> c.type() == ConflictType.BLOCO));
        }

        @Test
        void naoDeveReportarConflitoParaJogadaValida() {
            service.place(0, 2, 4);
            assertEquals(ErrorStatus.SEM_ERRO, service.report().errors());
            assertTrue(service.report().conflicts().isEmpty());
        }
    }

    @Nested
    class Status {

        @Test
        void deveSerNaoIniciadoSemNumerosDoUsuario() {
            ValidationReport report = service.report();
            assertEquals(GameStatus.NAO_INICIADO, report.status());
            assertEquals(ErrorStatus.SEM_ERRO, report.errors());
        }

        @Test
        void rascunhoNaoDeveIniciarOJogo() {
            service.toggleDraft(0, 2, 4);
            assertEquals(GameStatus.NAO_INICIADO, service.report().status());
        }

        @Test
        void deveSerIncompletoAposPrimeiraJogada() {
            service.place(0, 2, 4);
            assertEquals(GameStatus.INCOMPLETO, service.report().status());
        }

        @Test
        void deveSerIncompletoComErroQuandoHaConflito() {
            service.place(0, 8, 7);
            ValidationReport report = service.report();
            assertEquals(GameStatus.INCOMPLETO, report.status());
            assertEquals(ErrorStatus.COM_ERRO, report.errors());
        }

        @Test
        void deveSerCompletoSemErroComSolucaoCorreta() {
            preencherSolucao();
            ValidationReport report = service.report();
            assertEquals(GameStatus.COMPLETO, report.status());
            assertEquals(ErrorStatus.SEM_ERRO, report.errors());
        }

        @Test
        void deveSerCompletoComErroQuandoTabuleiroCheioTemConflito() {
            preencherSolucao();
            service.remove(0, 2);
            service.place(0, 2, 9);
            ValidationReport report = service.report();
            assertEquals(GameStatus.COMPLETO, report.status());
            assertEquals(ErrorStatus.COM_ERRO, report.errors());
        }
    }

    @Nested
    class InsercaoERemocao {

        @Test
        void deveInserirNumeroEmCelulaVazia() {
            service.place(0, 2, 4);
            assertEquals(4, service.board().value(0, 2));
            assertFalse(service.board().isFixed(0, 2));
        }

        @Test
        void deveRejeitarInsercaoEmCelulaFixa() {
            assertThrows(InvalidMoveException.class, () -> service.place(0, 0, 1));
            assertEquals(5, service.board().value(0, 0));
        }

        @Test
        void deveRejeitarInsercaoEmCelulaJaPreenchidaPeloJogador() {
            service.place(0, 2, 4);
            assertThrows(InvalidMoveException.class, () -> service.place(0, 2, 1));
            assertEquals(4, service.board().value(0, 2));
        }

        @Test
        void deveRejeitarNumeroForaDoIntervalo() {
            assertThrows(InvalidMoveException.class, () -> service.place(0, 2, 0));
            assertThrows(InvalidMoveException.class, () -> service.place(0, 2, 10));
        }

        @Test
        void deveRejeitarPosicaoForaDoTabuleiro() {
            assertThrows(IllegalArgumentException.class, () -> service.place(9, 0, 1));
        }

        @Test
        void deveRemoverNumeroDoJogador() {
            service.place(0, 2, 4);
            assertTrue(service.remove(0, 2));
            assertTrue(service.board().isEmpty(0, 2));
        }

        @Test
        void deveBloquearRemocaoDeCelulaFixa() {
            InvalidMoveException e = assertThrows(InvalidMoveException.class, () -> service.remove(0, 0));
            assertTrue(e.getMessage().contains("fixas"));
            assertEquals(5, service.board().value(0, 0));
        }

        @Test
        void remocaoEmCelulaVaziaNaoDeveTerEfeito() {
            assertFalse(service.remove(0, 2));
        }
    }

    @Nested
    class Rascunhos {

        @Test
        void deveAlternarRascunhoEmCelulaVazia() {
            assertTrue(service.toggleDraft(0, 2, 4));
            assertEquals(java.util.Set.of(4), service.board().drafts(0, 2));
            assertFalse(service.toggleDraft(0, 2, 4));
            assertTrue(service.board().drafts(0, 2).isEmpty());
        }

        @Test
        void deveRejeitarRascunhoEmCelulaFixa() {
            assertThrows(InvalidMoveException.class, () -> service.toggleDraft(0, 0, 1));
        }

        @Test
        void deveLimparRascunhosAoInserirNumeroDefinitivo() {
            service.toggleDraft(0, 2, 4);
            service.toggleDraft(0, 2, 6);
            service.place(0, 2, 4);
            assertTrue(service.board().drafts(0, 2).isEmpty());
        }
    }

    @Nested
    class LimparEFinalizar {

        @Test
        void limparDeveManterApenasNumerosFixos() {
            service.place(0, 2, 4);
            service.toggleDraft(0, 3, 2);
            service.clear();
            assertTrue(service.board().isEmpty(0, 2));
            assertTrue(service.board().drafts(0, 3).isEmpty());
            assertEquals(5, service.board().value(0, 0));
            assertEquals(GameStatus.NAO_INICIADO, service.report().status());
        }

        @Test
        void deveBloquearFinalizacaoComTabuleiroIncompleto() {
            service.place(0, 2, 4);
            assertThrows(InvalidMoveException.class, () -> service.finish());
        }

        @Test
        void deveBloquearFinalizacaoComTabuleiroCompletoComErro() {
            preencherSolucao();
            service.remove(0, 2);
            service.place(0, 2, 9);
            assertThrows(InvalidMoveException.class, () -> service.finish());
        }

        @Test
        void devePermitirFinalizacaoComTabuleiroCompletoSemErro() {
            preencherSolucao();
            service.finish();
        }
    }

    @Test
    void deveExigirJogoIniciado() {
        SudokuService vazio = new SudokuService(new ValidationService());
        assertThrows(IllegalStateException.class, vazio::report);
    }

    @Test
    void puzzleDeveSerImutavel() throws PuzzleFormatException {
        Puzzle puzzle = PuzzleLoader.parseLine(PUZZLE);
        puzzle.givens()[2] = 4;
        assertEquals(List.of(0), List.of(puzzle.given(0, 2)));
    }
}
