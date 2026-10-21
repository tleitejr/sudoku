package com.dio.util;

public final class GridMath {

    public static final int SIZE = 9;
    public static final int BLOCK_SIZE = 3;
    public static final int CELLS = SIZE * SIZE;

    private GridMath() {
    }

    public static int index(int row, int column) {
        checkBounds(row, column);
        return row * SIZE + column;
    }

    public static int blockOf(int row, int column) {
        return (row / BLOCK_SIZE) * BLOCK_SIZE + column / BLOCK_SIZE;
    }

    public static void checkBounds(int row, int column) {
        if (row < 0 || row >= SIZE || column < 0 || column >= SIZE) {
            throw new IllegalArgumentException("Posição fora do tabuleiro: (" + row + ", " + column + ")");
        }
    }
}
