package com.dio.controller;

import com.dio.model.InvalidMoveException;
import com.dio.model.SudokuBoard;
import com.dio.model.ValidationReport;
import com.dio.service.SudokuService;
import com.dio.util.GridMath;
import com.dio.view.SceneNavigator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;

public class GameController {

    private static final String MSG_INFO = "message-info";
    private static final String MSG_ERROR = "message-error";

    private final SudokuService service;
    private final SceneNavigator navigator;
    private final Label[][] cells = new Label[GridMath.SIZE][GridMath.SIZE];
    private final BooleanProperty canFinish = new SimpleBooleanProperty(false);
    private int selectedRow = -1;
    private int selectedColumn = -1;
    private boolean showConflicts;

    @FXML
    private BorderPane root;
    @FXML
    private GridPane boardGrid;
    @FXML
    private TilePane palette;
    @FXML
    private ToggleButton btnDraftMode;
    @FXML
    private Button btnFinish;
    @FXML
    private Label lblStatus;
    @FXML
    private Label lblErrors;
    @FXML
    private Label lblFinishHint;
    @FXML
    private Label lblMessage;

    public GameController(SudokuService service, SceneNavigator navigator) {
        this.service = service;
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        buildBoard();
        buildPalette();
        btnFinish.disableProperty().bind(canFinish.not());
        lblFinishHint.visibleProperty().bind(canFinish.not());
        lblFinishHint.managedProperty().bind(canFinish.not());
        root.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);
        showMessage("Selecione uma célula e digite 1–9 (P alterna rascunho, Del remove).", false);
        render();
    }

    @FXML
    private void onNewGame() {
        navigator.startGame(service.puzzle().difficulty());
    }

    @FXML
    private void onCheck() {
        showConflicts = true;
        render();
        int conflicts = service.report().conflicts().size();
        if (conflicts == 0) {
            showMessage("Nenhum conflito encontrado.", false);
        } else {
            showMessage("Conflitos destacados no tabuleiro: " + conflicts + ".", true);
        }
    }

    @FXML
    private void onStatus() {
        ValidationReport report = service.report();
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Status do jogo");
        alert.setHeaderText(null);
        alert.setContentText("Status: " + report.status().label() + "\nErros: " + report.errors().label());
        alert.showAndWait();
    }

    @FXML
    private void onClear() {
        service.clear();
        showConflicts = false;
        showMessage("Tabuleiro limpo: apenas os números fixos foram mantidos.", false);
        render();
    }

    @FXML
    private void onFinish() {
        try {
            service.finish();
        } catch (InvalidMoveException e) {
            showMessage(e.getMessage(), true);
            return;
        }
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Vitória");
        alert.setHeaderText("Parabéns!");
        alert.setContentText("Você completou o Sudoku sem erros.");
        alert.showAndWait();
        navigator.showMenu();
    }

    @FXML
    private void onMenu() {
        navigator.showMenu();
    }

    @FXML
    private void onRemove() {
        if (!hasSelection()) {
            showMessage("Selecione uma célula primeiro.", true);
            return;
        }
        try {
            if (service.remove(selectedRow, selectedColumn)) {
                showMessage("Número removido.", false);
            } else {
                showMessage("A célula já está vazia.", false);
            }
        } catch (InvalidMoveException e) {
            showMessage(e.getMessage(), true);
        }
        showConflicts = false;
        render();
    }

    private void onDigit(int digit) {
        if (!hasSelection()) {
            showMessage("Selecione uma célula primeiro.", true);
            return;
        }
        try {
            if (btnDraftMode.isSelected()) {
                boolean added = service.toggleDraft(selectedRow, selectedColumn, digit);
                showMessage((added ? "Rascunho adicionado: " : "Rascunho removido: ") + digit, false);
            } else {
                service.place(selectedRow, selectedColumn, digit);
                showMessage("Número " + digit + " inserido.", false);
            }
        } catch (InvalidMoveException e) {
            showMessage(e.getMessage(), true);
        }
        showConflicts = false;
        render();
    }

    private void onKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        String text = event.getText();
        if (text.length() == 1 && text.charAt(0) >= '1' && text.charAt(0) <= '9') {
            onDigit(text.charAt(0) - '0');
        } else if (code == KeyCode.DELETE || code == KeyCode.BACK_SPACE) {
            onRemove();
        } else if (code == KeyCode.P) {
            btnDraftMode.setSelected(!btnDraftMode.isSelected());
        } else if (code.isArrowKey()) {
            moveSelection(code);
        } else {
            return;
        }
        event.consume();
    }

    private void moveSelection(KeyCode code) {
        if (!hasSelection()) {
            select(0, 0);
            return;
        }
        int row = selectedRow;
        int column = selectedColumn;
        switch (code) {
            case UP -> row = Math.max(0, row - 1);
            case DOWN -> row = Math.min(GridMath.SIZE - 1, row + 1);
            case LEFT -> column = Math.max(0, column - 1);
            case RIGHT -> column = Math.min(GridMath.SIZE - 1, column + 1);
            default -> {
            }
        }
        select(row, column);
    }

    private void select(int row, int column) {
        selectedRow = row;
        selectedColumn = column;
        render();
    }

    private boolean hasSelection() {
        return selectedRow >= 0 && selectedColumn >= 0;
    }

    private void buildBoard() {
        for (int r = 0; r < GridMath.SIZE; r++) {
            for (int c = 0; c < GridMath.SIZE; c++) {
                Label cell = new Label();
                final int row = r;
                final int column = c;
                cell.setOnMouseClicked(e -> select(row, column));
                cells[r][c] = cell;
                boardGrid.add(cell, c, r);
            }
        }
    }

    private void buildPalette() {
        for (int digit = 1; digit <= GridMath.SIZE; digit++) {
            Button button = new Button(String.valueOf(digit));
            button.getStyleClass().add("btn-digit");
            final int value = digit;
            button.setOnAction(e -> onDigit(value));
            palette.getChildren().add(button);
        }
    }

    private void render() {
        SudokuBoard board = service.board();
        ValidationReport report = service.report();
        for (int r = 0; r < GridMath.SIZE; r++) {
            for (int c = 0; c < GridMath.SIZE; c++) {
                renderCell(cells[r][c], board, report, r, c);
            }
        }
        lblStatus.setText("Status: " + report.status().label());
        lblErrors.setText("Erros: " + report.errors().label());
        canFinish.set(report.canFinish());
    }

    private void renderCell(Label cell, SudokuBoard board, ValidationReport report, int r, int c) {
        List<String> styles = new ArrayList<>(List.of("cell"));
        boolean rightEdge = c % GridMath.BLOCK_SIZE == GridMath.BLOCK_SIZE - 1 && c < GridMath.SIZE - 1;
        boolean bottomEdge = r % GridMath.BLOCK_SIZE == GridMath.BLOCK_SIZE - 1 && r < GridMath.SIZE - 1;
        if (rightEdge && bottomEdge) {
            styles.add("block-corner");
        } else if (rightEdge) {
            styles.add("block-right");
        } else if (bottomEdge) {
            styles.add("block-bottom");
        }

        int value = board.value(r, c);
        Set<Integer> drafts = board.drafts(r, c);
        if (board.isFixed(r, c)) {
            styles.add("cell-fixed");
            cell.setText(String.valueOf(value));
        } else if (value != 0) {
            styles.add("cell-user");
            cell.setText(String.valueOf(value));
        } else if (!drafts.isEmpty()) {
            styles.add("cell-empty");
            styles.add("cell-draft");
            cell.setText(draftText(drafts));
        } else {
            styles.add("cell-empty");
            cell.setText("");
        }
        if (r == selectedRow && c == selectedColumn) {
            styles.add("cell-selected");
        }
        if (showConflicts && report.hasConflictAt(r, c)) {
            styles.add("cell-error");
        }
        cell.getStyleClass().setAll(styles);
    }

    private static String draftText(Set<Integer> drafts) {
        StringBuilder text = new StringBuilder();
        for (int digit = 1; digit <= GridMath.SIZE; digit++) {
            text.append(drafts.contains(digit) ? digit : ' ');
            if (digit % GridMath.BLOCK_SIZE == 0 && digit < GridMath.SIZE) {
                text.append('\n');
            } else if (digit < GridMath.SIZE) {
                text.append(' ');
            }
        }
        return text.toString();
    }

    private void showMessage(String message, boolean error) {
        lblMessage.setText(message);
        lblMessage.getStyleClass().setAll(error ? MSG_ERROR : MSG_INFO);
    }
}
