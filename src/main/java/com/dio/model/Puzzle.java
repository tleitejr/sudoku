package com.dio.model;

import com.dio.util.GridMath;

/** Immutable puzzle definition: 81 givens in row-major order, 0 meaning empty. */
public record Puzzle(String name, Difficulty difficulty, int[] givens) {

    public Puzzle {
        if (givens == null || givens.length != GridMath.CELLS) {
            throw new IllegalArgumentException("O puzzle deve ter exatamente " + GridMath.CELLS + " células.");
        }
        for (int value : givens) {
            if (value < 0 || value > GridMath.SIZE) {
                throw new IllegalArgumentException("Valores do puzzle devem estar entre 0 e 9.");
            }
        }
        givens = givens.clone();
    }

    @Override
    public int[] givens() {
        return givens.clone();
    }

    public int given(int row, int column) {
        return givens[GridMath.index(row, column)];
    }
}
