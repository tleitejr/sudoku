package com.dio.model;

import java.util.List;

public record ValidationReport(GameStatus status, ErrorStatus errors, List<Conflict> conflicts) {

    public ValidationReport {
        conflicts = List.copyOf(conflicts);
    }

    public boolean hasConflictAt(int row, int column) {
        return conflicts.stream().anyMatch(c -> c.row() == row && c.column() == column);
    }

    public boolean canFinish() {
        return status == GameStatus.COMPLETO && errors == ErrorStatus.SEM_ERRO;
    }
}
