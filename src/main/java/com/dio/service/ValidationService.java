package com.dio.service;

import com.dio.model.Conflict;
import com.dio.model.ConflictType;
import com.dio.model.ErrorStatus;
import com.dio.model.GameStatus;
import com.dio.model.SudokuBoard;
import com.dio.model.ValidationReport;
import com.dio.util.GridMath;
import java.util.ArrayList;
import java.util.List;

public class ValidationService {

    public ValidationReport validate(SudokuBoard board) {
        GameStatus status = computeStatus(board);
        if (status == GameStatus.NAO_INICIADO) {
            return new ValidationReport(status, ErrorStatus.SEM_ERRO, List.of());
        }
        List<Conflict> conflicts = findConflicts(board);
        ErrorStatus errors = conflicts.isEmpty() ? ErrorStatus.SEM_ERRO : ErrorStatus.COM_ERRO;
        return new ValidationReport(status, errors, conflicts);
    }

    public GameStatus computeStatus(SudokuBoard board) {
        if (!board.hasUserValues()) {
            return GameStatus.NAO_INICIADO;
        }
        return board.isFull() ? GameStatus.COMPLETO : GameStatus.INCOMPLETO;
    }

    /** Reports, for each filled cell, one conflict per unit (row/column/block) where its value repeats. */
    public List<Conflict> findConflicts(SudokuBoard board) {
        List<Conflict> conflicts = new ArrayList<>();
        for (int r = 0; r < GridMath.SIZE; r++) {
            for (int c = 0; c < GridMath.SIZE; c++) {
                int value = board.value(r, c);
                if (value == 0) {
                    continue;
                }
                boolean row = false;
                boolean column = false;
                boolean block = false;
                for (int r2 = 0; r2 < GridMath.SIZE; r2++) {
                    for (int c2 = 0; c2 < GridMath.SIZE; c2++) {
                        if ((r2 == r && c2 == c) || board.value(r2, c2) != value) {
                            continue;
                        }
                        row |= r2 == r;
                        column |= c2 == c;
                        block |= GridMath.blockOf(r2, c2) == GridMath.blockOf(r, c);
                    }
                }
                if (row) {
                    conflicts.add(new Conflict(r, c, value, ConflictType.LINHA));
                }
                if (column) {
                    conflicts.add(new Conflict(r, c, value, ConflictType.COLUNA));
                }
                if (block) {
                    conflicts.add(new Conflict(r, c, value, ConflictType.BLOCO));
                }
            }
        }
        return conflicts;
    }
}
