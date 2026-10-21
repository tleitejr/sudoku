package com.dio.model;

/** A cell whose value repeats inside a row, column or block (0-based coordinates). */
public record Conflict(int row, int column, int value, ConflictType type) {
}
