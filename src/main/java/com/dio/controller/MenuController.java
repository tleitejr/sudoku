package com.dio.controller;

import com.dio.model.Difficulty;
import com.dio.model.Puzzle;
import com.dio.view.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class MenuController {

    private final SceneNavigator navigator;

    @FXML
    private ComboBox<Difficulty> cmbDifficulty;
    @FXML
    private Button btnStart;
    @FXML
    private Button btnStartCli;
    @FXML
    private Label lblMessage;

    public MenuController(SceneNavigator navigator) {
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        cmbDifficulty.getItems().setAll(Difficulty.values());
        cmbDifficulty.getSelectionModel().select(Difficulty.FACIL);
        btnStart.setDisable(!navigator.hasPuzzles());
        btnStartCli.setDisable(navigator.cliPuzzle().isEmpty());
        btnStartCli.setVisible(navigator.cliPuzzle().isPresent());
        btnStartCli.setManaged(navigator.cliPuzzle().isPresent());
        lblMessage.setText(navigator.startupMessage());
    }

    @FXML
    private void onStart() {
        navigator.startGame(cmbDifficulty.getValue());
    }

    @FXML
    private void onStartCli() {
        navigator.cliPuzzle().ifPresent((Puzzle puzzle) -> navigator.showGame(puzzle));
    }

    @FXML
    private void onExit() {
        Platform.exit();
    }
}
