package com.dio.view;

import com.dio.controller.GameController;
import com.dio.controller.MenuController;
import com.dio.model.Difficulty;
import com.dio.model.Puzzle;
import com.dio.service.PuzzleFormatException;
import com.dio.service.PuzzleLoader;
import com.dio.service.SudokuService;
import com.dio.service.ValidationService;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class SceneNavigator {

    private static final Logger LOG = Logger.getLogger(SceneNavigator.class.getName());
    private static final String CSS = "/css/app.css";

    private final Stage stage;
    private final Random random = new Random();
    private List<Puzzle> library = List.of();
    private Optional<Puzzle> cliPuzzle = Optional.empty();
    private String startupMessage = "";

    public SceneNavigator(Stage stage, List<String> args) {
        this.stage = stage;
        try {
            library = PuzzleLoader.loadFromResource(PuzzleLoader.DEFAULT_RESOURCE);
        } catch (IOException | PuzzleFormatException e) {
            LOG.log(Level.SEVERE, "Falha ao carregar puzzles", e);
            startupMessage = "Não foi possível carregar os puzzles: " + e.getMessage();
        }
        try {
            cliPuzzle = PuzzleLoader.fromArgs(args);
        } catch (PuzzleFormatException e) {
            LOG.log(Level.WARNING, "Puzzle inválido na linha de comando", e);
            startupMessage = "Puzzle da linha de comando inválido: " + e.getMessage();
        }
    }

    public void showMenu() {
        show(load("/fxml/menu.fxml", new MenuController(this)), "Sudoku");
    }

    public void startGame(Difficulty difficulty) {
        Optional<Puzzle> puzzle = PuzzleLoader.pickRandom(library, difficulty, random);
        if (puzzle.isPresent()) {
            showGame(puzzle.get());
        } else {
            LOG.warning(() -> "Nenhum puzzle para a dificuldade " + difficulty);
        }
    }

    public void showGame(Puzzle puzzle) {
        SudokuService service = new SudokuService(new ValidationService());
        service.newGame(puzzle);
        show(load("/fxml/game.fxml", new GameController(service, this)), "Sudoku - " + puzzle.name());
    }

    public Optional<Puzzle> cliPuzzle() {
        return cliPuzzle;
    }

    public boolean hasPuzzles() {
        return !library.isEmpty();
    }

    public String startupMessage() {
        return startupMessage;
    }

    private Parent load(String fxml, Object controller) {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxml));
        loader.setControllerFactory(type -> controller);
        try {
            return loader.load();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Falha ao carregar " + fxml, e);
            throw new IllegalStateException("Não foi possível carregar a tela " + fxml, e);
        }
    }

    private void show(Parent root, String title) {
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 940, 700);
            scene.getStylesheets().add(SceneNavigator.class.getResource(CSS).toExternalForm());
            stage.setScene(scene);
            stage.setMinWidth(860);
            stage.setMinHeight(660);
        } else {
            scene.setRoot(root);
        }
        stage.setTitle(title);
    }
}
