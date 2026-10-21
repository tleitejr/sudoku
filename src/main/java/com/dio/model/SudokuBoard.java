package com.dio.model;

import com.dio.util.GridMath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Mutable game board: fixed givens, player values and pencil marks. Pure Java, no JavaFX. */
public final class SudokuBoard {

    private final int[] values = new int[GridMath.CELLS];
    private final boolean[] fixed = new boolean[GridMath.CELLS];
    private final List<Set<Integer>> drafts = new ArrayList<>(GridMath.CELLS);

    private SudokuBoard() {
        for (int i = 0; i < GridMath.CELLS; i++) {
            drafts.add(new TreeSet<>());
        }
    }

    public static SudokuBoard from(Puzzle puzzle) {
        SudokuBoard board = new SudokuBoard();
        int[] givens = puzzle.givens();
        for (int i = 0; i < GridMath.CELLS; i++) {
            board.values[i] = givens[i];
            board.fixed[i] = givens[i] != 0;
        }
        return board;
    }

    public int value(int row, int column) {
        return values[GridMath.index(row, column)];
    }

    public boolean isFixed(int row, int column) {
        return fixed[GridMath.index(row, column)];
    }

    public boolean isEmpty(int row, int column) {
        return value(row, column) == 0;
    }

    public Set<Integer> drafts(int row, int column) {
        return Collections.unmodifiableSet(new TreeSet<>(drafts.get(GridMath.index(row, column))));
    }

    public boolean hasUserValues() {
        for (int i = 0; i < GridMath.CELLS; i++) {
            if (!fixed[i] && values[i] != 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isFull() {
        for (int value : values) {
            if (value == 0) {
                return false;
            }
        }
        return true;
    }

    public void place(int row, int column, int value) {
        int index = GridMath.index(row, column);
        checkDigit(value);
        if (fixed[index]) {
            throw new InvalidMoveException("Esta célula é fixa e não pode ser alterada.");
        }
        if (values[index] != 0) {
            throw new InvalidMoveException("A célula já está preenchida. Remova o número antes de inserir outro.");
        }
        values[index] = value;
        drafts.get(index).clear();
    }

    /** Returns true if a number was removed, false if the cell was already empty. */
    public boolean remove(int row, int column) {
        int index = GridMath.index(row, column);
        if (fixed[index]) {
            throw new InvalidMoveException("Células fixas do puzzle não podem ser removidas.");
        }
        if (values[index] == 0) {
            return false;
        }
        values[index] = 0;
        return true;
    }

    /** Adds the pencil mark, or removes it if already present. Returns true if it is now present. */
    public boolean toggleDraft(int row, int column, int value) {
        int index = GridMath.index(row, column);
        checkDigit(value);
        if (values[index] != 0) {
            throw new InvalidMoveException("Rascunhos só podem ser marcados em células vazias.");
        }
        Set<Integer> marks = drafts.get(index);
        return marks.add(value) || !marks.remove(value);
    }

    public void clearUserEntries() {
        for (int i = 0; i < GridMath.CELLS; i++) {
            if (!fixed[i]) {
                values[i] = 0;
            }
            drafts.get(i).clear();
        }
    }

    private static void checkDigit(int value) {
        if (value < 1 || value > GridMath.SIZE) {
            throw new InvalidMoveException("O número deve estar entre 1 e 9.");
        }
    }
}
