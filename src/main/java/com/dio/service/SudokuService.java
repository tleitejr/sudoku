package com.dio.service;

import com.dio.model.InvalidMoveException;
import com.dio.model.Puzzle;
import com.dio.model.SudokuBoard;
import com.dio.model.ValidationReport;
import java.util.logging.Logger;

/** Game session facade: all board mutations and rule checks go through here. */
public class SudokuService {

    private static final Logger LOG = Logger.getLogger(SudokuService.class.getName());

    private final ValidationService validator;
    private Puzzle puzzle;
    private SudokuBoard board;

    public SudokuService(ValidationService validator) {
        this.validator = validator;
    }

    public void newGame(Puzzle puzzle) {
        this.puzzle = puzzle;
        this.board = SudokuBoard.from(puzzle);
        LOG.info(() -> "Novo jogo: " + puzzle.name());
    }

    public Puzzle puzzle() {
        requireGame();
        return puzzle;
    }

    public SudokuBoard board() {
        requireGame();
        return board;
    }

    public void place(int row, int column, int value) {
        requireGame();
        board.place(row, column, value);
    }

    public boolean remove(int row, int column) {
        requireGame();
        return board.remove(row, column);
    }

    public boolean toggleDraft(int row, int column, int value) {
        requireGame();
        return board.toggleDraft(row, column, value);
    }

    public void clear() {
        requireGame();
        board.clearUserEntries();
    }

    public ValidationReport report() {
        requireGame();
        return validator.validate(board);
    }

    /** Succeeds only when the board is complete and conflict-free. */
    public void finish() {
        if (!report().canFinish()) {
            throw new InvalidMoveException("Todos os espaços devem ser preenchidos corretamente para finalizar.");
        }
    }

    private void requireGame() {
        if (board == null) {
            throw new IllegalStateException("Nenhum jogo iniciado.");
        }
    }
}
